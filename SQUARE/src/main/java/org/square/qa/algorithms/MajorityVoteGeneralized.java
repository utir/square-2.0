package org.square.qa.algorithms;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;


import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.square.qa.utilities.constructs.Models;
import org.square.qa.utilities.constructs.Pair;
import org.square.qa.utilities.constructs.workersDataStruct;

public class MajorityVoteGeneralized<TypeWID,TypeQ,TypeR> {
	private static Logger log = LogManager.getLogger(MajorityVoteGeneralized.class);
	Models<TypeWID, TypeQ, TypeR>.MajorityModel currentModel;
	
	/**
	 * Constructor for MajorityVoteGeneralized
	 * @param majorityModel model is of type MajorityModel from Models
	 */
	public MajorityVoteGeneralized(Models<TypeWID, TypeQ, TypeR>.MajorityModel majorityModel){
		this.currentModel = majorityModel;
	}
	
	/**
	 * Get MajorityModel
	 * @return the current MajorityModel
	 */
	public Models<TypeWID, TypeQ, TypeR>.MajorityModel getCurrentModel(){
		return currentModel;}
	
	/**
	 * Compute label estimates
	 */
	public void computeLabelEstimates(){
		Map<TypeQ, Pair<TypeR,Map<TypeR,Double> > > majorityResponses = new HashMap<TypeQ, Pair<TypeR, Map<TypeR,Double> > >();
		Map<TypeQ, Map<TypeR,Integer> > responseCounts = new HashMap<TypeQ, Map<TypeR,Integer> >();
		
		log.info("Begin counting worker responses");
		for(TypeWID key:currentModel.getWorkersMap().keySet()){
			log.debug("Counting responses from worker: "+key);
			workersDataStruct<TypeQ,TypeR> thisWorker = currentModel.getWorkersMap().get(key);
			Map <TypeQ,List<TypeR> > thisWorkerAllResponses = thisWorker.getWorkerResponses();
			for(TypeQ keyInner:thisWorkerAllResponses.keySet()){
				List<TypeR> thisWorkerCurrResponses = thisWorkerAllResponses.get(keyInner);
				if(!responseCounts.containsKey(keyInner)){
					Map<TypeR,Integer> innerMap = new HashMap<TypeR, Integer>();
					for(TypeR keyInnerInner:currentModel.getResponseCategories()){
						innerMap.put(keyInnerInner, 0);
					}
					responseCounts.put(keyInner, innerMap);
				}
				for(TypeR repeatIter:thisWorkerCurrResponses){
					Map<TypeR,Integer> innerMap = responseCounts.get(keyInner);
					int count = innerMap.get(repeatIter);
					count++;
					innerMap.put(repeatIter, count);
				}
			}
		}
		log.info("Done counting... Calculating Majority Estimates and Soft Labels");
		for(TypeQ key:responseCounts.keySet()){
			Map<TypeR,Integer> innerMap = responseCounts.get(key);
			Pair<TypeR,Integer> trackMax = new Pair<TypeR, Integer>(null, 0);
			double overallCount = 0;
			for(TypeR keyInner:currentModel.getResponseCategories()){
				int currentClassCount = innerMap.get(keyInner);
				overallCount = overallCount + currentClassCount; 
				if(currentClassCount>trackMax.getSecond()){
					trackMax.putFirst(keyInner);
					trackMax.putSecond(currentClassCount);
				} else if (currentClassCount == trackMax.getSecond()) {
					if(currentModel.hasClassPriors()){
						double maxClassPrior = currentModel.getClassPriors().get(trackMax.getFirst());
						double currentClassPrior = currentModel.getClassPriors().get(keyInner);
						if(maxClassPrior>currentClassPrior){
							continue;
						} else if (maxClassPrior < currentClassPrior){
							trackMax.putFirst(keyInner);
							trackMax.putSecond(currentClassCount);
							continue;}} 
					Random number = new Random();
					if(number.nextDouble()>=0.5){
						trackMax.putFirst(keyInner);
						trackMax.putSecond(currentClassCount);}}}
			
			Map<TypeR,Double> majorityMap = new HashMap<TypeR, Double>();
			for(TypeR keyInner:currentModel.getResponseCategories()){
				double currentClassCount = innerMap.get(keyInner);
				double currentClassProb = currentClassCount/overallCount;
				majorityMap.put(keyInner, currentClassProb);
			}
			majorityResponses.put(key, new Pair<TypeR,Map<TypeR,Double> >(trackMax.getFirst(),majorityMap));
			log.debug("Question: "+key+" Response: "+trackMax.getFirst()+" with Probability: "+majorityMap.get(trackMax.getFirst()));
		}
		currentModel.setCombinedEstLabels(majorityResponses);
		log.info("Done calculating majority estimates and soft labels");}}
