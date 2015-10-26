package org.square.qa.utilities.constructs;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class workersDataStruct<TypeQ,TypeR> {
	private Map<TypeQ,List<TypeR> > responses;
	private static Logger log = LogManager.getLogger(workersDataStruct.class);
	private int numQuestions;
	private int numResponses;
	public workersDataStruct(){
		responses = new HashMap<TypeQ, List<TypeR> >();
		numQuestions = 0;
		numResponses = 0;}
	
	/**
	 * Insert worker responses for each question
	 * @param question is of TypeQ 
	 * @param response is of TypeR
	 */
	public void insertWorkerResponse(TypeQ question,TypeR response){
		if(responses.containsKey(question)){
			++numResponses;
			responses.get(question).add(response);
			if(log.isDebugEnabled())
				log.debug("Adding a worker who has answered the same question multiple times!!!");
		} else {
			++numResponses;
			responses.put(question, new ArrayList<TypeR>());
			responses.get(question).add(response);}
			++numQuestions;}
	
	/**
	 * Returns contained worker responses 
	 * @return is a Map of question to list of responses 
	 */
	public Map<TypeQ,List<TypeR> > getWorkerResponses(){
		assert responses!=null:"Attemped to retrieve responses from null object";
		return responses;}
	
	/**
	 * Prints contained responses 
	 */
	public void printWorkerResponses(){
		assert responses!=null:"Attemped to retrieve responses from null object";
		for(TypeQ key:responses.keySet()){
			List<TypeR> repeatResponse = responses.get(key);
			String repeats = " Responses:";
			for(TypeR keyInner:repeatResponse){
				repeats = repeats +"  "+keyInner;}
			System.out.println("\t\tQuestion: "+key+repeats);}
		}
	/**
	 * Function returns number of questions answered by the worker
	 * @return int 
	 */
	public int getNumQuestionsAnswered(){
		return numQuestions;}
	
	/**
	 * Function returns number of responses by the worker
	 * @return int
	 */
	public int getNumResponses(){
		return numResponses;}
	
	//This functions deems equality of workers based on questions answered
	public boolean equals(Object obj){
		if(!(obj instanceof workersDataStruct<?,?>))
			return false;
		final workersDataStruct<?,?> other = (workersDataStruct<?,?>) obj;
		return responses.equals(other.getWorkerResponses());}
	
	public int hashCode(){
		return responses.hashCode() + numQuestions;
	}
}
