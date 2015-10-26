package org.square.qa.algorithms;

import java.util.Map;
import java.util.TreeSet;

import org.square.qa.utilities.constructs.Pair;
import org.square.qa.utilities.constructs.workersDataStruct;


public interface AlgorithmInterface<TypeWID,TypeQ,TypeR> {
	/**
	 * Get all responses stored for each worker
	 * @return a Map from Worker ID to a worker object holding questions and responses
	 */
	public Map<TypeWID,workersDataStruct<TypeQ,TypeR> > getWorkersMap();
	
	/**
	 * Set responses to questions for each worker
	 * @param workersMap is a Map from Worker ID to a worker object holding questions and responses
	 */
	public void setWorkersMap(Map<TypeWID,workersDataStruct<TypeQ,TypeR> > workersMap);
	
	/**
	 * Get valid response categories 
	 * @return is a sorted set of response categories
	 */
	public TreeSet<TypeR> getResponseCategories();
	
	/**
	 * Set response categories 
	 * @param responseCategories is a TreeSet of TypeR
	 */
	public void setResponseCategories(TreeSet<TypeR> responseCategories);
	
	/**
	 * Get worker responses from the Tune partition
	 * @return a Map from workers to objects containing worker questions and responses
	 */
	public Map<TypeWID,workersDataStruct<TypeQ,TypeR> > getWorkersMapTune();
	
	/**
	 * Set worker responses from the Tune partition
	 * @param workersMapTune is a Map from workers to objects containing worker questions and responses
	 */
	public void setWorkersMapTune(Map<TypeWID,workersDataStruct<TypeQ,TypeR> > workersMapTune);
	
	/**
	 * Get worker responses from the Gold partition
	 * @return a Map from workers to objects containing worker questions and responses
	 */
	public Map<TypeWID,workersDataStruct<TypeQ,TypeR> > getWorkersMapGold();
	
	/**
	 * Set worker responses from the Gold partition 
	 * @param workersMapGold is a Map from workers to objects containing worker questions and responses
	 */
	public void setWorkersMapGold(Map<TypeWID,workersDataStruct<TypeQ,TypeR> > workersMapGold);
	
	/**
	 * Get ground truth question response pairs from the tune partition 
	 * @return is a Map from questions to responses
	 */
	public Map<TypeQ,TypeR> getTuneGT();
	
	/**
	 * Set ground truth question response pairs from the tune partition
	 * @param tuneGT is a Map from questions to responses
	 */
	public void setTuneGT(Map<TypeQ,TypeR> tuneGT);
	
	/**
	 * Get ground truth question response pairs from the gold partition
	 * @return is a Map from questions to responses
	 */
	public Map<TypeQ,TypeR> getGoldStandard();
	
	/**
	 * Set ground truth question response pairs from the gold partition
	 * @param goldStandard is a Map from questions to responses
	 */
	public void setGoldStandard(Map<TypeQ,TypeR> goldStandard);
	
	/**
	 * Checks for presence of worker responses from the evaluation partition
	 * @return true if workers responses are loaded
	 */
	public boolean hasWorkersMap();
	
	/**
	 * Checks for presence of worker responses from the tune partition
	 * @return true if workers responses are loaded
	 */
	public boolean hasWorkersMapTune();
	
	/**
	 * Checks for presence of worker responses from the gold partition
	 * @return true if workers responses are loaded
	 */
	public boolean hasWorkersMapGold();
	
	/**
	 * Checks for presence of response categories
	 * @return true if response categories are loaded
	 */
	public boolean hasResponseCategories();
	
	/**
	 * Checks if gold standard is used by the algorithm (supervised)
	 * @return true if gold standard is available 
	 */
	public boolean hasGoldStandard();
	
	/**
	 * Checks if ground truth for tune partition is used by the algorithm (lightly-supervised)
	 * @return true if ground truth for tune partition is available
	 */
	public boolean hasTuneGT();
	
	/**
	 * Get computed/partially-computed results
	 * @return a Map from questions to Pair of predicted response and response probabilities
	 */
	public Map<TypeQ, Pair<TypeR,Map<TypeR,Double> > > getCombinedEstLabels();
	
	/**
	 * Checks if results are computed
	 * @return true if results have been computed
	 */
	public boolean hasCombinedEstLabels();
	
	/**
	 * Load computed/partially-computed results
	 * @param combinedEstLabels is a Map from questions to Pair of predicted response and response probabilities
	 */
	public void setCombinedEstLabels(Map<TypeQ, Pair<TypeR,Map<TypeR,Double> > > combinedEstLabels);}
