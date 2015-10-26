package org.square.qa.utilities.constructs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.jblas.DoubleMatrix;

public class Results<TypeQ,TypeR> {
	private static Logger log = LogManager.getLogger(Results.class);
	private Map<TypeQ,Pair<TypeR,Map<TypeR,Double> > > compiledResults = null;
	private List<Pair<TypeR,Double> > sortedRePrPairs = null;
	private SortedSet<TypeQ> sortedQuestions = null;
	private Map<TypeR,Integer> categToInt = null;
	private DoubleMatrix resultVector = null;
	private Pair<DoubleMatrix,DoubleMatrix> groundTruthVector = null;
	private Pair<DoubleMatrix,DoubleMatrix> tuneVector = null;
	private Pair<DoubleMatrix,DoubleMatrix> goldVector = null;
	private Metrics metrics = null; 
	
	/**
	 * Add results from consensus algorithm
	 * @param compiledResults is a Map from questions to Pair of predicted response and response probabilities
	 */
	public void addCompiledResults(Map<TypeQ,Pair<TypeR,Map<TypeR,Double> > > compiledResults){
		if(this.compiledResults == null){
			this.compiledResults = compiledResults;
		} else {
			this.compiledResults.putAll(compiledResults);}}
	
	
	
	/**
	 * Loads gold responses
	 * @param gold is a Map from questions to responses
	 */
	public void setGold(Map<TypeQ,TypeR> gold){
		log.debug("Setting gold...");
		assert categToInt!=null:"Result Vector not computed.";
		DoubleMatrix indicator = DoubleMatrix.zeros(sortedQuestions.size());
		DoubleMatrix goldVec = DoubleMatrix.zeros(sortedQuestions.size());
		int idx = 0;
		for(TypeQ iter:sortedQuestions){
			if(gold.containsKey(iter)){
				indicator.put(idx, 1.0d);
				goldVec.put(idx, categToInt.get(gold.get(iter)));}	
			idx++;}
		goldVector = new Pair<DoubleMatrix, DoubleMatrix>(goldVec, indicator);}
	
	/**
	 * Loads tune responses
	 * @param tuneSet is a Map from questions to responses
	 */
	public void setTune(Map<TypeQ,TypeR> tuneSet){
		log.debug("Setting tune set...");
		assert categToInt!=null:"Result Vector not computed.";
		DoubleMatrix indicator = DoubleMatrix.zeros(sortedQuestions.size());
		DoubleMatrix tuneVec = DoubleMatrix.zeros(sortedQuestions.size());
		int idx = 0;
		for(TypeQ iter:sortedQuestions){
			if(tuneSet.containsKey(iter)){
				indicator.put(idx, 1.0d);
				tuneVec.put(idx, categToInt.get(tuneSet.get(iter)));}	
			idx++;}
		tuneVector = new Pair<DoubleMatrix, DoubleMatrix>(tuneVec, indicator);}
	
	/**
	 * Loads ground truth responses
	 * @param groundTruth is a Map from questions to responses
	 */
	public void setGroundTruth(Map<TypeQ,TypeR> groundTruth){
		log.debug("Setting ground truth...");
		assert categToInt!=null:"Result Vector not computed.";
		DoubleMatrix indicator = DoubleMatrix.zeros(sortedQuestions.size());
		DoubleMatrix gtVec = DoubleMatrix.zeros(sortedQuestions.size());
		int idx = 0;
		for(TypeQ iter:sortedQuestions){
			if(groundTruth.containsKey(iter)){
				indicator.put(idx, 1.0d);
				gtVec.put(idx, categToInt.get(groundTruth.get(iter)));}	
			idx++;}
		groundTruthVector = new Pair<DoubleMatrix, DoubleMatrix>(gtVec, indicator);}
	
	public void setCategToInt(Map<TypeR,Integer> categToInt){
		this.categToInt = categToInt;}
	
	/**
	 * Extracts results from compiled results in sorted order of questions
	 */
	public void computeComparableResuts(){
		assert compiledResults!=null:"Results not loaded from consensus algorithm";
		log.info("Extracting results...");
		
		sortedQuestions = new TreeSet<TypeQ>();
		sortedQuestions.addAll(compiledResults.keySet());
		sortedRePrPairs = new ArrayList<Pair<TypeR,Double> >();
		for(TypeQ iter:sortedQuestions){
			TypeR estLabel = compiledResults.get(iter).getFirst();
			sortedRePrPairs.add(new Pair<TypeR,Double>(estLabel,compiledResults.get(iter).getSecond().get(estLabel)));}
		if(log.isDebugEnabled())
			log.debug("Question Sorted Response pairs:\n"+sortedRePrPairs);
		log.info("Done Extracting.");}
	
	/**
	 * @return comparable results as a list of pairs with response and probabilities -- corresponding to sorted questions 
	 */
	public List<Pair<TypeR,Double> > getComparableResults(){
		assert sortedRePrPairs!=null:"Attempted to access null vector.";
		return sortedRePrPairs;}
	
	/**
	 * Computes results as a vector of type DoubleMatrix
	 */
	public void computeComparableResultVector(){
		log.info("Computing comparable result vector...");
		assert sortedRePrPairs!=null:"Attempted to compute results vector before results.";
		resultVector = DoubleMatrix.zeros(sortedQuestions.size());
		
		int temp = 0;
		for(Pair<TypeR,Double> iter:sortedRePrPairs){
			resultVector.put(temp, categToInt.get(iter.getFirst()));
			temp++;}
		if(log.isDebugEnabled())
			log.debug("Computed results vector:\n"+resultVector);
		log.info("Computed result vector.");}
	
	/**
	 * @return a DoubleMatrix with results as a vector 
	 */
	public DoubleMatrix getComparableResultVector(){
		assert resultVector!=null:"Attempted to access null vector.";
		return resultVector;
	}
	
	/**
	 * Get Result String
	 * @return result string
	 */
	public String printComparableResults(Map<String,Integer> questionToInt) {
		String outString = "";
		int i = 0;
		for(TypeQ question:sortedQuestions){
			Double tempResult = resultVector.get(i);
			if(i!=0)
				outString+="\n";
//			if(tempResult.intValue() == 0)
//				continue;
			
			if(groundTruthVector!=null){
				Double evalInd = groundTruthVector.getSecond().get(i);
				if(evalInd.intValue() == 0){
					++i;
					continue;}}
			++i;
			outString += questionToInt.get(question) + " " + tempResult.intValue();}
		if(log.isDebugEnabled()){
			log.debug(outString);}
		return outString;}
	
	
	
	/**
	 * Computes metrics
	 * @param tune is a boolean
	 * @param supervised is a boolean
	 */
	public void computeMetrics(boolean tune,boolean supervised){
		if(supervised){
			assert goldVector!=null:"Supervised data not available";}
		ArrayList<Integer> sortedMappedCategories = new ArrayList<Integer>();
		sortedMappedCategories.addAll(categToInt.values());
		Collections.sort(sortedMappedCategories);
		metrics = new Metrics(sortedMappedCategories);
		if(supervised){
			log.info("Computing Metrics -- supervised");
			GeneralUtils.computeMetrics(resultVector, metrics, groundTruthVector, goldVector);
			log.info("Updated metrics");
		} else if (!tune){
			log.info("Computing Metrics -- unsupervised || Computing Metrics -- nFold");
			GeneralUtils.computeMetrics(resultVector, metrics, groundTruthVector);
			log.info("Updated metrics");
		} else if(tune){
			log.info("Computing Metrics -- tune");
			GeneralUtils.computeMetrics(resultVector, metrics, tuneVector);
			log.info("Updated metrics");}}
	
	/**
	 * @return Metrics object with computed metrics
	 */
	public Metrics getMetrics(){
		assert metrics!=null:"Metrics not computed";
		return metrics;}
}
