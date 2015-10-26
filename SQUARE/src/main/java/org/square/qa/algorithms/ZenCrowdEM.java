package org.square.qa.algorithms;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.jblas.DoubleMatrix;
import org.square.qa.utilities.constructs.GeneralUtils;
import org.square.qa.utilities.constructs.GeneralUtilsParameterized;
import org.square.qa.utilities.constructs.Models;
import org.square.qa.utilities.constructs.Pair;
import org.square.qa.utilities.constructs.workersDataStruct;


//import org.apache.log4j.Logger;
public class ZenCrowdEM<TypeWID,TypeQ,TypeR> {
	private static Logger log = LogManager.getLogger(ZenCrowdEM.class);
	private Map<TypeQ,TypeR> goldStandard;
	private Map<TypeWID,workersDataStruct<TypeQ,TypeR> > workersMap;
	private Set<TypeR> responseCategories;
	private boolean supervised;
	private Map<TypeQ,Integer> questionIndexMap;
	private Map<TypeWID,Map<TypeR,DoubleMatrix> > workerIndicatorMap;
	private Map<TypeR,DoubleMatrix> probMats;
	private Map<TypeWID, Double> workerReliabilityMap;
	private int numIterations;
	private Map<TypeWID,Pair<Double,Double> > workerPriors;
	private boolean loadWPrior;
	private boolean loadCPrior;
	private Map<TypeR,Integer> classIndexMap;
	private DoubleMatrix classPriorParam;
	private DoubleMatrix classProb;
	private boolean useClassPrior;
	private boolean useWorkerPrior;
	private boolean loadFromModel;
	private final double workerBetaParamMu = 0.7;
	private final double workerBetaParamVar = 0.3;
	
	private Models<TypeWID,TypeQ,TypeR>.ZenModel currentModel;
	
	/**
	 * Constructor for ZenCrowdEM
	 * @param model is of type ZenModel from Models
	 */
	public ZenCrowdEM(Models<TypeWID,TypeQ,TypeR>.ZenModel model){
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
			this.goldStandard = null;
		}
		useClassPrior = model.useClassPrior;
		useWorkerPrior = model.useWorkerPrior;
		loadFromModel = false;
		this.numIterations = 50;
	}
	
	/**
	 * Constructor for ZenCrowdEM
	 * @param model is of type ZenModel from Models
	 * @param loadFromModel is a boolean indication if model already exists
	 */
	public ZenCrowdEM(Models<TypeWID,TypeQ,TypeR>.ZenModel model, boolean loadFromModel){
		assert loadFromModel == true:"Required to load from model";
		this.currentModel = model;
		this.workersMap = model.getWorkersMap();
		this.responseCategories = model.getResponseCategories();
		this.loadFromModel = true;
		this.numIterations = 50;
		useClassPrior = model.useClassPrior;
		useWorkerPrior = model.useWorkerPrior;
	}
	
	/**
	 * Get ZenModel
	 * @return the current ZenModel
	 */
	public Models<TypeWID,TypeQ,TypeR>.ZenModel getCurrentModel(){
		return currentModel;
	}
	
	/**
	 * Set number of EM iterations 
	 * @param numIterations is an int 
	 */
	public void setNumIterations(int numIterations){
		this.numIterations = numIterations;
	}
	
	private void initializeMats(){
		log.debug("Initializing Matrices");
		questionIndexMap = new HashMap<TypeQ, Integer>();
		workerIndicatorMap = new HashMap<TypeWID,Map<TypeR,DoubleMatrix> >();
		probMats = new HashMap<TypeR, DoubleMatrix>();
		GeneralUtilsParameterized<TypeWID, TypeQ, TypeR> util = new GeneralUtilsParameterized<TypeWID, TypeQ, TypeR>();
		Set<TypeQ> questions = util.getQuestions(workersMap);
		for(TypeR iter:responseCategories){
			probMats.put(iter, DoubleMatrix.zeros(questions.size()));
		}
		log.debug("Initilized probability matrices");
		
		int i = 0;
		for(TypeQ key:questions){
			questionIndexMap.put(key, i);
			i++;
		}
		
		if(log.isDebugEnabled())
			log.debug("Computed Map between Questions and Matrix Idx:\n "+questionIndexMap);
		
		if(supervised){
			log.debug("Supervised: Overiding known probabilities");
			overrideKnownProb();
		}
		
		log.debug("Computing indicator matrices for each response class...");
		for(TypeWID key:workersMap.keySet()){
			Map<TypeQ,List<TypeR> > currWorker = workersMap.get(key).getWorkerResponses();
			Map<TypeR,DoubleMatrix> currWorkerIndicator = new HashMap<TypeR, DoubleMatrix>();
			
			for(TypeR keyInner:responseCategories){
				DoubleMatrix wIndicator = DoubleMatrix.zeros(questions.size());
				currWorkerIndicator.put(keyInner, wIndicator);
			}
			
			workerIndicatorMap.put(key, currWorkerIndicator);
			for(TypeQ keyInner:currWorker.keySet()){
				for(TypeR iter:currWorker.get(keyInner)){ //TODO:Think of a better way to handle multiple responses by same worker
					workerIndicatorMap.get(key).get(iter).put(questionIndexMap.get(keyInner), 1.0d);
				}
			}
			if(log.isDebugEnabled())
				log.debug(key+" Indicator matrices:\n"+workerIndicatorMap.get(key));
		}
		
		log.debug("Done Initinializing Matrices");
	}
	
	private void overrideKnownProb(){
		double overflow = responseCategories.size()-1;
		overflow = 0.01/overflow;
		for(TypeQ key:goldStandard.keySet()){
			for(TypeR keyInner:responseCategories){
				if(keyInner.equals(goldStandard.get(key))){
					probMats.get(keyInner).put(questionIndexMap.get(key), 0.99d);
				} else {
					probMats.get(keyInner).put(questionIndexMap.get(key), overflow);
				}
			}
		}
	}
	
	private Map<TypeQ,TypeR> computeMajorityResponses(){
		Models<TypeWID,TypeQ,TypeR>.MajorityModel majorityModel = new Models<TypeWID, TypeQ, TypeR>().getMajorityModel();
		majorityModel.setWorkersMap(currentModel.getWorkersMap());
		majorityModel.setResponseCategories(currentModel.getResponseCategories());
		MajorityVoteGeneralized<TypeWID, TypeQ, TypeR> majorityVoteAlgo = new MajorityVoteGeneralized<TypeWID, TypeQ, TypeR>(majorityModel);
		majorityVoteAlgo.computeLabelEstimates();
		majorityModel = majorityVoteAlgo.getCurrentModel();
		Map<TypeQ,Pair<TypeR, Map<TypeR,Double> > > majorityResponses = majorityModel.getCombinedEstLabels();
		GeneralUtilsParameterized<TypeWID, TypeQ, TypeR> gu = new GeneralUtilsParameterized<TypeWID, TypeQ, TypeR>();
		Map<TypeQ,TypeR> mResponses = gu.getQuestionResultPair(majorityResponses);
		if(supervised){
			for(TypeQ key:goldStandard.keySet()){
				mResponses.put(key, goldStandard.get(key));
			}
		}
		return mResponses;
	}
	
	/**
	 * Compute label estimates
	 */
	public void computeLabelEstimates(){
		log.info("Begin estimating labels...");
		
		workerReliabilityMap = new HashMap<TypeWID, Double>();
		Map<TypeQ,TypeR> estResultsMap = null;
		boolean first = true;
		
		initializeMats();
		
		if(loadFromModel){
			initializeFromModel();
		} else {
			if(useWorkerPrior)
				initializeWorkers();
		
			if(useClassPrior)
				initializeClassProb();
		}
		
		for(int i = 1;i<= numIterations; i++){
			if(first){
				Map<TypeQ,TypeR> initEstimates = computeMajorityResponses();
				eStep(initEstimates);	
				estResultsMap = mStep();
				first = false;
			} else {
				eStep(estResultsMap);	
				estResultsMap = mStep();
			}
		}
		currentModel.setCombinedEstLabels(getResults(estResultsMap));
		log.info("Done estimating labels");
	}
	
	private void eStep(Map<TypeQ,TypeR> estResultsMap){
		computeWorkerReliability(estResultsMap);
		if(useClassPrior){
			DoubleMatrix classCounts = DoubleMatrix.zeros(responseCategories.size());
			if(estResultsMap != null){
				for(TypeQ key:estResultsMap.keySet()){
					int currentIndex = classIndexMap.get(estResultsMap.get(key)); 
					double currentCount = classCounts.get(currentIndex);
					classCounts.put(currentIndex,currentCount + 1.0d );
				}
			}
			double normSum = classCounts.sum();
			double paramSum = classPriorParam.sum();
			classCounts.addi(classPriorParam.add(-1.0d));
			classProb = classCounts.divi(normSum+paramSum+responseCategories.size());
		}
	}
	
	private Map<TypeQ,TypeR> mStep(){
		Map<TypeR,DoubleMatrix> accumulator = new HashMap<TypeR, DoubleMatrix>();
		
		//Initialize accumulator
		for(TypeR key:responseCategories){
			accumulator.put(key, DoubleMatrix.ones(questionIndexMap.size()));
		}
		
		for(TypeR key:responseCategories){
			DoubleMatrix tempMat = DoubleMatrix.ones(questionIndexMap.size());
			for(TypeWID keyInner:workerIndicatorMap.keySet()){
				DoubleMatrix currentIndicator = workerIndicatorMap.get(keyInner).get(key);
				tempMat.muli(currentIndicator.mul(workerReliabilityMap.get(keyInner)).add(currentIndicator.eq(0.0d)));
				for(TypeR keyInnerInner:responseCategories){
					if(keyInnerInner.equals(key))
						continue;
					DoubleMatrix restIndicator = workerIndicatorMap.get(keyInner).get(keyInnerInner);
					tempMat.muli(restIndicator.mul(1.0d - workerReliabilityMap.get(keyInner)).add(restIndicator.eq(0.0d)));
				}
			}
			accumulator.get(key).muli(tempMat);
		}
		DoubleMatrix normMat = DoubleMatrix.zeros(questionIndexMap.size());
		
		for(TypeR key:responseCategories){
			if(useClassPrior){
				accumulator.get(key).muli(classProb.get(classIndexMap.get(key)));
			}
			normMat.addi(accumulator.get(key));
		}
		
		for(TypeR key:responseCategories){
			accumulator.get(key).divi(normMat);
		}
		probMats = accumulator;
		return updateResults();
	}
	
	private Map<TypeQ,TypeR> updateResults(){
		Map<TypeQ,TypeR> estResultsMap = new HashMap<TypeQ,TypeR>();
		if(supervised){
			overrideKnownProb();
		}
		for(TypeQ key:questionIndexMap.keySet()){
			double maxProb = 0;
			TypeR estResponse = null;
			for(TypeR keyInner:probMats.keySet()){
				double currProb = probMats.get(keyInner).get(questionIndexMap.get(key));
				if(currProb>maxProb){
					maxProb = currProb;
					estResponse = keyInner;
				}
			}
			estResultsMap.put(key, estResponse);
		}
		return estResultsMap;
	}
	
	private void initializeWorkers(){
		log.debug("Assiging worker priors...");
		workerPriors = new HashMap<TypeWID, Pair<Double,Double> >();
		if(loadWPrior){
			//TODO:Read From File
			log.info("Inferring worker priors from tune set");
			Pair<Double,Double> betaParam = getBetaParamFromPrior();
			for(TypeWID key:workersMap.keySet()){
				workerPriors.put(key, betaParam);
			}
			currentModel.setWorkerPriors(workerPriors);
		} else {
			log.info("Priors not specified, assigning default priors to worker: Mean - " + workerBetaParamMu+" Variance - "+workerBetaParamVar);
			for(TypeWID key:workersMap.keySet()){
				workerPriors.put(key, GeneralUtils.getBetaParam(workerBetaParamMu, workerBetaParamVar));
			}
		}
		if(log.isDebugEnabled())
			log.debug("Assigned worker priors:\n"+workerPriors);
	}
	
	private Pair<Double,Double> getBetaParamFromPrior(){
		int numWorkers = currentModel.getWorkersMapTune().size();
		DoubleMatrix correctCounts = DoubleMatrix.zeros(numWorkers);
		DoubleMatrix wrongCounts = DoubleMatrix.zeros(numWorkers);
		int i = 0;
		for(TypeWID key:currentModel.getWorkersMapTune().keySet()){
			int correct = 5;
			int wrong = 5;
			Map<TypeQ,List<TypeR> > thisWorkerAllResponses = currentModel.getWorkersMapTune().get(key).getWorkerResponses();
			for(TypeQ keyInner:currentModel.getTuneGT().keySet()){
				if(thisWorkerAllResponses.containsKey(keyInner)){
					for(TypeR keyInnerInner:thisWorkerAllResponses.get(keyInner)){
						if(keyInnerInner.equals(currentModel.getTuneGT().get(keyInner))){
							correct++;
						} else {
							wrong++;
						}
					}
				}
			}
			correctCounts.put(i, correct);
			wrongCounts.put(i, wrong);
			i++;
		}
		return new Pair<Double, Double>(correctCounts.mean(), wrongCounts.mean());
	}
	
	private void initializeClassProb(){
		log.debug("Assigning class priors...");
		classIndexMap = new HashMap<TypeR, Integer>();
		int i = 0;
		log.debug("Computing a mapping from categories to matrix idx");
		for(TypeR iter:responseCategories){
			classIndexMap.put(iter, i);
			i++;
		}
		
		if(log.isDebugEnabled())
			log.debug("Computed category-index map:\n"+classIndexMap);
		
		classProb = DoubleMatrix.zeros(responseCategories.size());
		log.debug("Initilized class probability matrix to zeros");
		
		if(loadCPrior){
			//TODO:Read From File
			log.info("Inferring class priors from tune set");
			classPriorParam = DoubleMatrix.ones(responseCategories.size()).muli(5.0);
			for(TypeQ key:currentModel.getTuneGT().keySet()){
				TypeR gtResponse = currentModel.getTuneGT().get(key);
				double currentCount = classPriorParam.get(classIndexMap.get(gtResponse));
				currentCount++;
				classPriorParam.put(classIndexMap.get(gtResponse),currentCount);
			}
			currentModel.setClassPriorParam(classPriorParam);
		} else {
			log.info("Class priors not specified.. loading default values");
			classPriorParam = DoubleMatrix.ones(responseCategories.size()).mul(3);
		}
		if(log.isDebugEnabled())
			log.debug("Assigned class priors:\n"+classPriorParam);
	}
	
	private void computeWorkerReliability(Map<TypeQ,TypeR> labels){
		for(TypeWID key:workersMap.keySet()){
			double correct = 0.0d;
			double total = 0.0d;
			Map<TypeQ,List<TypeR> > thisWorkerAllResponses = workersMap.get(key).getWorkerResponses();
			if(labels != null){
				for(TypeQ keyInner:labels.keySet()){
					if(thisWorkerAllResponses.containsKey(keyInner)){
						for(TypeR keyInnerInner:thisWorkerAllResponses.get(keyInner)){
							if(keyInnerInner.equals(labels.get(keyInner))){
								correct++;
							}
							total++;
						}
					}
				}
			}
			double reliability = 0.5d;
			if(useWorkerPrior){
				reliability = (workerPriors.get(key).getFirst() + (-1.0d) +correct)/(workerPriors.get(key).getFirst() + workerPriors.get(key).getSecond() + (-2.0d) + total);
			} else {
				if(total!=0){
					reliability = correct/total;
				}
			}
			workerReliabilityMap.put(key,reliability);
		}	
	}
	
	private Map<TypeQ, Pair<TypeR,Map<TypeR,Double> > > getResults(Map<TypeQ,TypeR> estResultsMap){
		Map<TypeQ, Pair<TypeR,Map<TypeR,Double> > > results = new HashMap<TypeQ, Pair<TypeR,Map<TypeR,Double> > >();
		for(TypeQ key:questionIndexMap.keySet()){
			Map<TypeR,Double> tempMap = new HashMap<TypeR, Double>();
			for(TypeR keyInner:probMats.keySet()){
				double currProb = probMats.get(keyInner).get(questionIndexMap.get(key));
				tempMap.put(keyInner, currProb);
			}
			results.put(key, new Pair<TypeR,Map<TypeR,Double>>(estResultsMap.get(key),tempMap));
		}
		return results;
	}
	
	private void initializeFromModel(){
		if(currentModel.hasWorkerPriors()){
			workerPriors = currentModel.getWorkerPriors();
			Pair<Double,Double> priorVal = workerPriors.entrySet().iterator().next().getValue();
			for(TypeWID key:workersMap.keySet()){
				if(workerPriors.containsKey(key))
					continue;
				else
					workerPriors.put(key, priorVal);
			}
		}
		
		if(currentModel.hasClassPriorParam()){
			log.debug("Assigning class priors...");
			classIndexMap = new HashMap<TypeR, Integer>();
			int i = 0;
			log.debug("Computing a mapping from categories to matrix idx");
			for(TypeR iter:currentModel.getResponseCategories()){
				classIndexMap.put(iter, i);
				i++;
			}
		
			if(log.isDebugEnabled())
				log.debug("Computed category-index map:\n"+classIndexMap);
		
			classProb = DoubleMatrix.zeros(responseCategories.size());
			log.debug("Initilized class probability matrix to zeros");
		
			log.info("Loading class priors...");
			classPriorParam = currentModel.getClassPriorParam();
		}
	}
}
