package org.square.qa.algorithms;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.jblas.DoubleMatrix; 	
import org.square.qa.utilities.constructs.Models;
import org.square.qa.utilities.constructs.Pair;
import org.square.qa.utilities.constructs.workersDataStruct;

public class BinaryMapEstRY<TypeWID,TypeQ,TypeR> {
	private static Logger log = LogManager.getLogger(BinaryMapEstRY.class);
	private Map<TypeQ,TypeR> goldStandard;
	private Map<TypeWID,workersDataStruct<TypeQ,TypeR> > workersMap;
	private Set<TypeR> responseCategories;
	private int numIterations;
	private Pair<Double,Double> posClassBetaParam;
	private TypeR positiveClass; 
	private Map<TypeWID,Pair<DoubleMatrix,DoubleMatrix> > workerIndicatorMat;
	private Map<TypeQ,Integer> questionIndexMap;
	private DoubleMatrix muMat;
	private Map<TypeWID, Pair<Pair<Double,Double>,Pair<Double,Double> > > workerPriors; 
	private Map<TypeWID,Pair<Double,Double> > workerParams;
	private boolean supervised;
	
	private boolean loadWPrior;
	private boolean loadCPrior;
	private boolean useClassPrior;
	private boolean useWorkerPrior;
	private boolean loadFromModel;
	
	private final double sensitivityMu = 0.7;
	private final double sensitivityVar = 0.2;
	private final double specificityMu = 0.7;
	private final double specificityVar = 0.2;
	
	private final double posClassMu = 0.6;
	private final double posClassVar = 0.3;
	
	private Models<TypeWID,TypeQ,TypeR>.RaykarModel currentModel;
	
	/**
	 * Constructor for BinaryMapEstRY
	 * @param model is of type RaykarModel from Models
	 */
	public BinaryMapEstRY(Models<TypeWID,TypeQ,TypeR>.RaykarModel model){
		//TODO:Initialize majority responses
		this.currentModel = model;
		this.workersMap = model.getWorkersMap();
		this.responseCategories = model.getResponseCategories();
		
		if(model.isSemiSupervisedModel()){
			log.info("Semi-Supervised Estimation -- Priors being inferred from data");
			supervised = false;
			loadWPrior = true;
			loadCPrior = true;
			this.goldStandard = null;
		} else if(model.isSupervisedModel()){
			log.info("Supervised Estimation");
			supervised = true;
			loadWPrior = true;
			loadCPrior = true;
			this.goldStandard = model.getGoldStandard();
		} else if(model.isUnsupervisedModel()){
			log.info("Completely Unsupervised Estimation");
			supervised = false;
			loadWPrior = false;
			loadCPrior = false;
			this.goldStandard = null;}
		useClassPrior = model.useClassPrior;
		useWorkerPrior = model.useWorkerPrior;
		loadFromModel = false;
		positiveClass = model.getPositiveClass();
		this.numIterations = 50;}
	private void initializeFromModel(){
		this.posClassBetaParam = currentModel.getPosClassBetaParam();
		this.workerPriors = currentModel.getWorkerPriors();}
	
	/**
	 * Compute label estimates
	 */
	public void computeLabelEstimates(){
		log.info("Begin estimating labels");
		initializeWorkers();
		if(loadFromModel){
			initializeFromModel();
		} else {
			if(useWorkerPrior)
				inferWorkerPriors();
			if(useClassPrior)
				setPosClassPrior();}
		questionIndexMap = new HashMap<TypeQ, Integer>();
		workerIndicatorMat = new HashMap<TypeWID, Pair<DoubleMatrix,DoubleMatrix>>();
		generateMats();
		workerParams = new HashMap<TypeWID, Pair<Double,Double>>();
		for(int i = 1;i<= numIterations; i++){
			double p = eStep();
			mStep(p);}
		log.info("Done estimating labels");
		currentModel.setCombinedEstLabels(getResults());}
	
	/**
	 * Set positive class prior
	 */
	public void setPosClassPrior(){
		if(loadCPrior){
			inferPosClassPrior();
		} else {
			posClassBetaParam = getBetaParam(posClassMu, posClassVar);}}
	
	/**
	 * infer positive class prior from tune set
	 */
	public void inferPosClassPrior(){
		posClassBetaParam = new Pair<Double, Double>(5.0d, 5.0d);
		for(TypeQ key:currentModel.getTuneGT().keySet()){
			TypeR gtResponse = currentModel.getTuneGT().get(key);
			if(gtResponse.equals(positiveClass)){
				double currentCount = posClassBetaParam.getFirst();
				currentCount++;
				posClassBetaParam.putFirst(currentCount);
			} else {
				double currentCount = posClassBetaParam.getSecond();
				currentCount++;
				posClassBetaParam.putSecond(currentCount);}}
		currentModel.setPosClassBetaParam(posClassBetaParam);}
	
	/**
	 * Set number of EM iterations
	 * @param numIterations is an int
	 */
	public void setNumIterations(int numIterations){
		this.numIterations = numIterations;}
	private Map<TypeQ, Pair<TypeR,Map<TypeR,Double> > > getResults(){
		Map<TypeQ, Pair<TypeR,Map<TypeR,Double> > > results = new HashMap<TypeQ, Pair<TypeR,Map<TypeR,Double> > >();
		for(TypeQ key:questionIndexMap.keySet()){
			Map<TypeR,Double> responseMap = new HashMap<TypeR, Double>();
			TypeR estClass = positiveClass;
			double posProbability = muMat.get(questionIndexMap.get(key));
			for(TypeR keyInner:responseCategories){
				if(keyInner.equals(positiveClass)){
					responseMap.put(keyInner, posProbability);
					if(posProbability >= 0.5d){
						estClass = positiveClass;}
				} else {
					responseMap.put(keyInner, 1.0d - posProbability);
					if(posProbability < 0.5d){
						estClass = keyInner;}}
				results.put(key, new Pair<TypeR,Map<TypeR,Double> >(estClass, responseMap));}}
		return results;}
	private double eStep(){
		double p = 0;
		for(TypeWID key:workerIndicatorMat.keySet()){
			double numSensitivity = 0;
			double denSensitivity = 0;
			double numSpecificity = 0;
			double denSpecificity = 0;
			if(useWorkerPrior){
				numSensitivity = workerPriors.get(key).getFirst().getFirst() - 1.0d + muMat.dot(workerIndicatorMat.get(key).getFirst());
				denSensitivity =  workerPriors.get(key).getFirst().getFirst() + workerPriors.get(key).getFirst().getSecond() - 2.0d + muMat.dot(workerIndicatorMat.get(key).getFirst().add(workerIndicatorMat.get(key).getSecond()));
				numSpecificity = workerPriors.get(key).getSecond().getFirst() - 1.0d + muMat.add(-1.0d).neg().dot(workerIndicatorMat.get(key).getSecond());
				denSpecificity = workerPriors.get(key).getSecond().getFirst() + workerPriors.get(key).getSecond().getSecond() - 2.0d + muMat.add(-1.0d).neg().dot(workerIndicatorMat.get(key).getFirst().add(workerIndicatorMat.get(key).getSecond()));;
			} else {
				numSensitivity = muMat.dot(workerIndicatorMat.get(key).getFirst());
				denSensitivity =  muMat.dot(workerIndicatorMat.get(key).getFirst().add(workerIndicatorMat.get(key).getSecond()));
				numSpecificity = muMat.add(-1.0d).neg().dot(workerIndicatorMat.get(key).getSecond());
				denSpecificity = muMat.add(-1.0d).neg().dot(workerIndicatorMat.get(key).getFirst().add(workerIndicatorMat.get(key).getSecond()));}
		    workerParams.put(key, new Pair<Double, Double>(numSensitivity/denSensitivity, numSpecificity/denSpecificity));}
		double pNum = 0;
		double pDen = 0;
		if(useClassPrior){
			pNum = posClassBetaParam.getFirst() - 1.0d + muMat.sum();
			pDen = posClassBetaParam.getFirst() + posClassBetaParam.getSecond() - 2.0d + muMat.length;
		} else {
			pNum = muMat.sum();
			pDen = muMat.length;}
		p = pNum/pDen;
		return p;}
	
	private void mStep(double p){
		DoubleMatrix a = DoubleMatrix.ones(muMat.length);
		DoubleMatrix b = DoubleMatrix.ones(muMat.length);
		
		for(TypeWID key:workerParams.keySet()){
			double sensitivity = workerParams.get(key).getFirst();
			double specificity = workerParams.get(key).getSecond();
			DoubleMatrix aTempPos = workerIndicatorMat.get(key).getFirst().mul(sensitivity);
			DoubleMatrix aTempNeg = workerIndicatorMat.get(key).getSecond().mul(1.0d - sensitivity);
			DoubleMatrix bTempNeg = workerIndicatorMat.get(key).getSecond().mul(specificity);
			DoubleMatrix bTempPos = workerIndicatorMat.get(key).getFirst().mul(1.0d - specificity);
			DoubleMatrix unanswered = workerIndicatorMat.get(key).getFirst().add(workerIndicatorMat.get(key).getSecond());
			DoubleMatrix aTemp = aTempPos.add(aTempNeg);
			DoubleMatrix bTemp = bTempPos.add(bTempNeg);
			unanswered.eqi(0.0d);
			a.muli(aTemp.add(unanswered));
			b.muli(bTemp.add(unanswered));}
		
		a.muli(p);
		b.muli(1.0d - p);
		
		muMat = a.div(a.add(b));
		
		if(supervised)
			overrideKnownProb();}
	
	private Pair<Double, Double> getBetaParam(double mean, double variance){
		double a,b;
		double meanCube = mean*mean*mean;
		double varianceSq = variance*variance;
		a = (meanCube + meanCube/mean - mean*(varianceSq))/varianceSq;
		b = (a*(1.0d - mean))/mean;
		return (new Pair<Double,Double>(a,b));}
	
	private void initializeWorkers(){
		workerPriors = new HashMap<TypeWID, Pair<Pair<Double,Double>,Pair<Double,Double> > >();
		Pair<Double,Double> sensitivity = new Pair<Double,Double>(0.0d,0.0d);
		Pair<Double,Double> specificity = new Pair<Double,Double>(0.0d,0.0d);
		for(TypeWID key:workersMap.keySet()){
			workerPriors.put(key, new Pair< Pair<Double,Double>, Pair<Double,Double> > (sensitivity,specificity));}}
	
	private void inferWorkerPriors(){
		if(loadWPrior){
			Pair<Pair<Double,Double>,Pair<Double,Double> > betaParam = getBetaParamFromPrior();
			for(TypeWID key:workersMap.keySet()){
					workerPriors.put(key, betaParam);}
			currentModel.setWorkerPriors(workerPriors);
	    }else{
			for(TypeWID key:workersMap.keySet()){
					workerPriors.put(key, new Pair< Pair<Double,Double>, Pair<Double,Double> > (getBetaParam(sensitivityMu,sensitivityVar),getBetaParam(specificityMu,specificityVar)));}}}
	
	private Pair<Pair<Double,Double>,Pair<Double,Double> > getBetaParamFromPrior(){
		int numWorkers = currentModel.getWorkersMapTune().size();
		DoubleMatrix correctCountsPos = DoubleMatrix.zeros(numWorkers);
		DoubleMatrix wrongCountsPos = DoubleMatrix.zeros(numWorkers);
		DoubleMatrix correctCountsNeg = DoubleMatrix.zeros(numWorkers);
		DoubleMatrix wrongCountsNeg = DoubleMatrix.zeros(numWorkers);
		int i = 0;
		for(TypeWID key:currentModel.getWorkersMapTune().keySet()){
			int correctPos = 5;
			int wrongPos = 5;
			int correctNeg = 5;
			int wrongNeg = 5;
			Map<TypeQ,List<TypeR> > thisWorkerAllResponses = currentModel.getWorkersMapTune().get(key).getWorkerResponses();
			for(TypeQ keyInner:currentModel.getTuneGT().keySet()){
				if(thisWorkerAllResponses.containsKey(keyInner)){
					for(TypeR keyInnerInner:thisWorkerAllResponses.get(keyInner)){
						if(keyInnerInner.equals(positiveClass)){
							if(keyInnerInner.equals(currentModel.getTuneGT().get(keyInner))){
								correctPos++;
							} else {
								wrongPos++;}
						}else{
							if(keyInnerInner.equals(currentModel.getTuneGT().get(keyInner))){
								correctNeg++;
							} else {
								wrongNeg++;}}}}}
			correctCountsPos.put(i, correctPos);
			wrongCountsNeg.put(i, wrongNeg);
			wrongCountsPos.put(i, wrongPos);
			correctCountsNeg.put(i, correctNeg);
			i++;}
		Pair<Double,Double> sensitivityPrior = new Pair<Double, Double>(correctCountsPos.mean(), wrongCountsPos.mean());
		Pair<Double,Double> specificityPrior = new Pair<Double, Double>(correctCountsNeg.mean(), wrongCountsNeg.mean());
		return (new Pair<Pair<Double,Double>, Pair<Double,Double> >(sensitivityPrior, specificityPrior));}
	
	private void generateMats(){
		initializeMuMat();
		for(TypeWID key:workersMap.keySet()){
			DoubleMatrix posClass = DoubleMatrix.zeros(questionIndexMap.size());
			DoubleMatrix negClass = DoubleMatrix.zeros(questionIndexMap.size());
			Map<TypeQ,List<TypeR> > currWorker = workersMap.get(key).getWorkerResponses();
			for(TypeQ keyInner:currWorker.keySet()){
				List<TypeR> currWorkerResponses = currWorker.get(keyInner);
//				if(currWorkerResponses.size()>1)
//					continue; //For now eliminating workers with repeated answers on the same question
				if(currWorkerResponses.get(0).equals(positiveClass)){
					posClass.put(questionIndexMap.get(keyInner), 1.0d);
				} else {
					negClass.put(questionIndexMap.get(keyInner), 1.0d);}}
			workerIndicatorMat.put(key, new Pair<DoubleMatrix,DoubleMatrix>(posClass,negClass));}}
	
	private void initializeMuMat(){
		Map<TypeQ,Pair<TypeR, Map<TypeR,Double> > > majorityResponses = computeMajorityResponses(); 
		int i = 0;
		muMat = DoubleMatrix.zeros(majorityResponses.size());
		for(TypeQ key:majorityResponses.keySet()){
			questionIndexMap.put(key, i);
			muMat.put(i, majorityResponses.get(key).getSecond().get(positiveClass));
			i++;}
		if(supervised){
			overrideKnownProb();}}
	
	private void overrideKnownProb(){
		double overflow = responseCategories.size()-1;
		overflow = 0.01/overflow;
		for(TypeQ key:goldStandard.keySet()){
			if(questionIndexMap.containsKey(key)){
				if(goldStandard.get(key).equals(positiveClass))
					muMat.put(questionIndexMap.get(key), 0.999d);
				else
					muMat.put(questionIndexMap.get(key), 0.001d);}}}
	
	private Map<TypeQ,Pair<TypeR, Map<TypeR,Double> > > computeMajorityResponses(){
		Models<TypeWID,TypeQ,TypeR>.MajorityModel majorityModel = new Models<TypeWID, TypeQ, TypeR>().getMajorityModel();
		majorityModel.setWorkersMap(currentModel.getWorkersMap());
		majorityModel.setResponseCategories(currentModel.getResponseCategories());
		MajorityVoteGeneralized<TypeWID, TypeQ, TypeR> majorityVoteAlgo = new MajorityVoteGeneralized<TypeWID, TypeQ, TypeR>(majorityModel);
		majorityVoteAlgo.computeLabelEstimates();
		majorityModel = majorityVoteAlgo.getCurrentModel();
		Map<TypeQ,Pair<TypeR, Map<TypeR,Double> > > majorityResponses = majorityModel.getCombinedEstLabels();
		return majorityResponses;}}

