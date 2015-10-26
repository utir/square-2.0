package org.square.qa.algorithms;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.square.qa.utilities.constructs.Models;
import org.square.qa.utilities.constructs.Pair;
import org.square.qa.utilities.constructs.workersDataStruct;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.List;


public class BayesGeneralized<TypeWID,TypeQ,TypeR> {
	private static Logger log = LogManager.getLogger(BayesGeneralized.class);

	private Map<TypeQ,Pair<TypeR, Map<TypeR,Double> > > majorityResponses;
	private Map<TypeWID, Map<TypeR,Map<TypeR,Double> > > workerPriors = null;
	private double lapAlpha;
	private double lapBeta;
	private boolean loadFromModel;
	
	private Models<TypeWID,TypeQ,TypeR>.BayesModel currentModel;
	
	/**
	 * Constructor for BayesGeneralized
	 * @param model is of type BayesModel from Models
	 * @param loadFromModel is a boolean indication if model already exists
	 */
	public BayesGeneralized(Models<TypeWID,TypeQ,TypeR>.BayesModel model, boolean loadFromModel){
		this.currentModel = model;
		this.loadFromModel = loadFromModel;
		if(!loadFromModel){
			lapAlpha = currentModel.getLapAlpha();
			lapBeta = currentModel.getLapBeta();}}
	
	/**
	 * Get BayesModel
	 * @return the current BayesModel
	 */
	public Models<TypeWID,TypeQ,TypeR>.BayesModel getCurrentModel(){
		return currentModel;}
	
	private void estWorkerConfusion(){
		log.info("Estimating worker confusion matrices...");
		workerPriors = new HashMap<TypeWID, Map<TypeR,Map<TypeR,Double> > >();
		log.info("Begin counting worker responses.");
		for(TypeWID key:currentModel.getWorkersMapGold().keySet()){
			log.debug("Counting responses of worker: "+key);
			workersDataStruct<TypeQ,TypeR> thisWorker = currentModel.getWorkersMapGold().get(key);
			Map<TypeQ,List<TypeR> > thisWorkerAllResponses = thisWorker.getWorkerResponses();
			Map<TypeR,Map<TypeR,Integer> > workerOverallPerformance = new HashMap<TypeR,Map<TypeR,Integer> >();
			
			/*Initialize*/
			for(TypeR keyInner:currentModel.getResponseCategories()){
				Map<TypeR,Integer> tempMap = new HashMap<TypeR, Integer>();
				for(TypeR keyInnerInner:currentModel.getResponseCategories()){
					tempMap.put(keyInnerInner, 0);}
				workerOverallPerformance.put(keyInner, tempMap);}
			/*End Initialize*/
			log.debug("Initialized response counts to zero.. count responses");
			
			boolean noneAnswered = true;
			for(TypeQ keyInner:currentModel.getGoldStandard().keySet()){
				if(!thisWorkerAllResponses.containsKey(keyInner))
					continue;
				noneAnswered = false;
				TypeR trueResponse = currentModel.getGoldStandard().get(keyInner);
				List<TypeR> thisWorkerCurrResponses = thisWorkerAllResponses.get(keyInner);
				for(TypeR respIter:thisWorkerCurrResponses){
					Map<TypeR,Integer> temp = workerOverallPerformance.get(trueResponse);
					int currCount = temp.get(respIter); 
					currCount++;
					temp.put(respIter, currCount);}}
			log.debug("Done counting responses");
			if(noneAnswered){
				if(log.isDebugEnabled()){
					log.debug(key + " worker responses has no overlap with supervised set");
					log.debug("Assigning values from new worker map");}
				
				setNewWorkerConfusion(key);
			}else{
				log.debug(key + " worker responses has an overlap with supervised set");
				
				Map<TypeR,Map<TypeR,Double> > temp = new HashMap<TypeR,Map<TypeR,Double> >();
				for(TypeR keyInner:currentModel.getResponseCategories()){
					Map<TypeR,Integer> tempInner = workerOverallPerformance.get(keyInner);
					Map<TypeR,Double> tempMap = new HashMap<TypeR,Double>();
					double overallCount = 0;
					for(TypeR keyInnerInner:currentModel.getResponseCategories()){
						tempMap.put(keyInnerInner, tempInner.get(keyInnerInner).doubleValue());
						overallCount = overallCount + tempInner.get(keyInnerInner).doubleValue();}
					for(TypeR keyInnerInner:currentModel.getResponseCategories()){
						double tempVal = tempMap.get(keyInnerInner);
						tempVal = (tempVal+lapAlpha)/(overallCount+lapBeta);
						tempMap.put(keyInnerInner, tempVal);}
					temp.put(keyInner, tempMap);}
				log.debug("Estimated confusion matrix:\n"+temp);
				workerPriors.put(key, temp);}}
		currentModel.setWorkerConfusionMaps(workerPriors);
		log.info("Done counting and estimating worker confusion matrices");}
	
	private void setNewWorkerConfusion(Map<TypeWID, workersDataStruct<TypeQ, TypeR> > workerMap){
		for(TypeWID key:workerMap.keySet())
			if(!this.workerPriors.containsKey(key))
				setNewWorkerConfusion(key);}
	
	private void setNewWorkerConfusion(TypeWID workerId){
		workerPriors.put(workerId, currentModel.getNewWorkerConfusion());}
	
	private Map<TypeQ,Pair<TypeR,Double> > bayesBinaryClassifier(TypeR positiveClass){
		Map<TypeQ,Pair<TypeR,Double> > bayesResponses = new HashMap<TypeQ, Pair<TypeR,Double> >();
		
		log.debug("Initialize bayes responses from majority estimates");
		
		for(TypeQ key:majorityResponses.keySet()){
			Map<TypeR, Double> majorityResponse = majorityResponses.get(key).getSecond();
			double positiveClassPrior = majorityResponse.get(positiveClass);
			positiveClassPrior = checkPriorValidity(positiveClassPrior,-1);
			bayesResponses.put(key,new Pair<TypeR,Double>(positiveClass,Math.log(positiveClassPrior/(1.0d - positiveClassPrior))));}
		
		for(TypeWID key:currentModel.getWorkersMap().keySet()){
			workersDataStruct<TypeQ,TypeR> thisWorker = currentModel.getWorkersMap().get(key);
			Map <TypeQ,List<TypeR> > thisWorkerAllResponses = thisWorker.getWorkerResponses();
			for(TypeQ keyInner:thisWorkerAllResponses.keySet()){
				Pair<TypeR,Double> existingEntry = bayesResponses.get(keyInner);
				double logOddsAccumilator = existingEntry.getSecond();
				List<TypeR> repeatResp = thisWorkerAllResponses.get(keyInner);
				for(TypeR keyInnerInner:repeatResp){
					Map<TypeR,Map<TypeR,Double> > currentWorkerProbs = workerPriors.get(key);
					Map<TypeR,Double> currentWorkerConditionals = currentWorkerProbs.get(positiveClass);
					double relaventPositiveConditional = currentWorkerConditionals.get(keyInnerInner);
					double relaventNegativeConditional = 0;
					for(TypeR restTypes:currentModel.getResponseCategories()){
						if(restTypes.equals(keyInnerInner)){
							continue;}
						relaventNegativeConditional = relaventNegativeConditional + currentWorkerConditionals.get(restTypes);}
					logOddsAccumilator = logOddsAccumilator + Math.log(relaventPositiveConditional/relaventNegativeConditional);
					assert !Double.isInfinite(logOddsAccumilator):"Log Odds Infinite";
					assert !Double.isNaN(logOddsAccumilator):"Log Odds NaN";}
				existingEntry.putSecond(logOddsAccumilator);
				bayesResponses.put(keyInner, existingEntry);}}
		
		if(log.isDebugEnabled()){
			log.debug("Done estimate odds positive class: "+positiveClass);}
		
		return bayesResponses;}
	
	/**
	 * Compute label estimates
	 */
	public void computeLabelEstimates(){
		log.info("Begin estimating labels. One vs All");
		if(loadFromModel){
			this.workerPriors = currentModel.getWorkerConfustionMaps();
			setNewWorkerConfusion(currentModel.getWorkersMap());
		} else {
			estWorkerConfusion();
			setNewWorkerConfusion(currentModel.getWorkersMap());}
		computeMajorityResponses();
		Map<TypeQ, Pair<TypeR,Map<TypeR,Double> > > bayesResponses = new HashMap<TypeQ, Pair<TypeR, Map<TypeR,Double> > >();
		List<Map<TypeQ,Pair<TypeR,Double> > > aggregatedResponses = new ArrayList<Map<TypeQ,Pair<TypeR,Double> > >();
		for(TypeR positiveClass:currentModel.getResponseCategories()){
			log.debug("Estimating log odds for positive class: "+positiveClass);
			aggregatedResponses.add(bayesBinaryClassifier(positiveClass));}
		log.info("Done computing One Vs All... Choosing most probable.");
		Map<TypeQ,Pair<TypeR,Double> > firstClassResponses = aggregatedResponses.get(0);
		for(TypeQ key:firstClassResponses.keySet()){
			Pair<TypeR,Double> maxPair = new Pair<TypeR,Double>(firstClassResponses.get(key).getFirst(),firstClassResponses.get(key).getSecond());
			Map<TypeR,Double> classConfidence = new HashMap<TypeR, Double>();
			Pair<TypeR,Map<TypeR,Double> > questionResponse = new Pair<TypeR,Map<TypeR,Double> >(firstClassResponses.get(key).getFirst(), classConfidence);
			for(Map<TypeQ,Pair<TypeR,Double> > response:aggregatedResponses){
				TypeR className = response.get(key).getFirst();
				double classScore = response.get(key).getSecond();
				classConfidence.put(className, classScore);
				if(classScore>maxPair.getSecond()){
					maxPair.putFirst(response.get(key).getFirst());
					maxPair.putSecond(response.get(key).getSecond());}}
			questionResponse.putFirst(maxPair.getFirst());
			bayesResponses.put(key, questionResponse);}
		log.info("Done estimating labels.");
		currentModel.setCombinedEstLabels(bayesResponses);}
	
	private double checkPriorValidity(double val, double substVal){
		if(Double.isNaN(val))
			return substVal;
		if(Double.isInfinite(substVal))
			return substVal;
		if(val == 1)
			return  0.99;
		if(val == 0)
			return  0.01;
		return val;}
	
	private void computeMajorityResponses(){
		Models<TypeWID,TypeQ,TypeR>.MajorityModel majorityModel = new Models<TypeWID, TypeQ, TypeR>().getMajorityModel();
		majorityModel.setWorkersMap(currentModel.getWorkersMap());
		majorityModel.setResponseCategories(currentModel.getResponseCategories());
		MajorityVoteGeneralized<TypeWID, TypeQ, TypeR> majorityVoteAlgo = new MajorityVoteGeneralized<TypeWID, TypeQ, TypeR>(majorityModel);
		majorityVoteAlgo.computeLabelEstimates();
		majorityModel = majorityVoteAlgo.getCurrentModel();
		this.majorityResponses = majorityModel.getCombinedEstLabels();}
	
	/**
	 * Print worker priors
	 */
	public void printWorkerPrior(){
		for(TypeWID key:currentModel.getWorkersMap().keySet()){
			System.out.println("Worker ID: "+key);
			currentModel.getWorkersMap().get(key).printWorkerResponses();
			Map<TypeR,Map<TypeR,Double> > printTemp = workerPriors.get(key);
			for(TypeR keyInner:printTemp.keySet()){
				for(TypeR keyInnerInner:printTemp.get(keyInner).keySet()){
					System.out.println(keyInnerInner + " Given " + keyInner + " -> " + printTemp.get(keyInner).get(keyInnerInner));}}}}}


