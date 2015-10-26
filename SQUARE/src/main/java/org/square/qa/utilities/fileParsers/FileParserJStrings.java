package org.square.qa.utilities.fileParsers;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.square.qa.utilities.constructs.GeneralUtils;
import org.square.qa.utilities.constructs.workersDataStruct;

public class FileParserJStrings {
	private static Logger log = LogManager.getLogger(FileParserJStrings.class);
	public boolean coloumnSwitch = false; 
	private static class parseTypeGS{
		private static String question = null;
		private static String response = null;
	}
	
	private static class parseTypeWR{
		private static String workerId = null;
		private static String question = null;
		private static String response = null;
	}
	
	private static class parseTypeCategories{
		private static String category = null;
		private static Double prior = -1.0d;
	}
	
	private static class parseTypeFoldQuestions{
		private static String question = null;
	}
	
	private String fileName;
	
	/**
	 * Set filename  
	 * @param fileName is a String
	 */
	public void setFileName(String fileName){
		this.fileName = fileName;
	}
	
	/**
	 * Get filename
	 * @param fileName is a String
	 */
	public void getFileName(String fileName){
		this.fileName = fileName;
	}
	
	/**
	 * Parse worker labels from worker responses file -- last line cannot be a blank line
	 * @return worker responses stored as a Map from workers to responses 
	 * @throws IOException
	 */
	public Map<String,workersDataStruct<String,String> > parseWorkerLabels() throws IOException{
		log.info("Parsing worker labels from file: "+fileName);
		Map<String,workersDataStruct<String,String> > workersMap = new HashMap<String, workersDataStruct<String,String>>();
		File file = new File(fileName);
	
		Scanner lineScan = new Scanner(file);
		boolean fileEnd = true;
		while(fileEnd){
			fileEnd = lineParseWR(lineScan);
			fileEnd = lineScan.hasNextLine();
			log.debug("Parsing response by "+parseTypeWR.workerId);
			if(workersMap.containsKey(parseTypeWR.workerId)){
				workersDataStruct<String,String> currentWorkerStruct = workersMap.get(parseTypeWR.workerId);
				currentWorkerStruct.insertWorkerResponse(parseTypeWR.question, parseTypeWR.response);
				workersMap.put(parseTypeWR.workerId, currentWorkerStruct);
			}
			else{
				workersDataStruct<String,String> newWorker = new workersDataStruct<String,String>();
				newWorker.insertWorkerResponse(parseTypeWR.question, parseTypeWR.response);
				workersMap.put(parseTypeWR.workerId, newWorker);
			}
		}
		log.info("Number of workers: "+workersMap.size());
		log.info("Number of questions: "+GeneralUtils.getQuestions(workersMap).size());
		log.info("Finished loading worker labels from file: "+fileName);
		return workersMap;
	}
	
	/**
	 * Parse gold data from gold standard file
	 * @return gold responses as a Map from questions to responses
	 * @throws IOException
	 */
	public Map<String,String> parseGoldStandard() throws IOException{
		log.info("Parsing gold/groundTruth labels from file: "+fileName);
		File file = new File(fileName);
		Scanner lineScan = new Scanner(file);
		Map<String,String> goldResponses = new HashMap<String, String>();
		boolean fileEnd = true;
		while(fileEnd){
			fileEnd = lineParseGS(lineScan);
			fileEnd = lineScan.hasNextLine();
			goldResponses.put(parseTypeGS.question, parseTypeGS.response);
		}
		lineScan.close();
		log.info("Number of gold responses: "+goldResponses.size());
		log.info("Done loading gold labels from file: "+fileName);
		return goldResponses;
	}
	
	/**
	 * Parse classes/categories with priors from input file
	 * @return categories and priors as a Map
	 * @throws IOException
	 */
	public Map<String,Double> parseCategoriesWPrior() throws IOException{
		log.info("Parsing Categories and Priors from file: "+fileName);
		File file = new File(fileName);
		Scanner lineScan = new Scanner(file);
		Map<String,Double> categories = new HashMap<String, Double>();
		boolean fileEnd = true;
		while(fileEnd){
			fileEnd = lineParseCategories(lineScan,true);
			fileEnd = lineScan.hasNextLine();
			
			categories.put(parseTypeCategories.category, parseTypeCategories.prior);
			log.info("Category: " + parseTypeCategories.category + " Prior: " + parseTypeCategories.prior);
		}
		lineScan.close();
		log.info("Done Loading Categories and Priors");
		return categories;
	}
	
	/**
	 * Parse classes/categories/classes from input file
	 * @return categories as a Set
	 * @throws IOException
	 */
	public Set<String> parseCategories() throws IOException{
		log.info("Parsing Categories and Priors from file: "+fileName);
		File file = new File(fileName);
		Scanner lineScan = new Scanner(file);
		Set<String> categories = new HashSet<String>();
		boolean fileEnd = true;
		while(fileEnd){
			fileEnd = lineParseCategories(lineScan,false);
			fileEnd = lineScan.hasNextLine();
			
			categories.add(parseTypeCategories.category);
			log.info("Category: " + parseTypeCategories.category);
		}
		lineScan.close();
		log.info("Done Loading Categories");
		return categories;
	}
	
	/**
	 * Parse retain reject fold fraction from input file
	 * @return questions as a Set
	 * @throws IOException
	 */
	public Set<String> parseFoldRR() throws IOException{
		log.info("Parsing fold retain/reject questions from file: "+fileName);
		File file = new File(fileName);
		Scanner lineScan = new Scanner(file);
		Set<String> questions = new HashSet<String>();
		boolean fileEnd = true;
		while(fileEnd){
			fileEnd = lineParseFoldQuestions(lineScan);
			fileEnd = lineScan.hasNextLine();
			questions.add(parseTypeFoldQuestions.question);
		}
		lineScan.close();
		log.info("Done Loading fold retain/reject set");
		return questions;
	}
	
	private boolean lineParseFoldQuestions(Scanner lineScan) {
		parseTypeFoldQuestions.question = lineScan.next();
		return lineScan.hasNextLine();
	}

	private boolean lineParseGS(Scanner lineScan){
		parseTypeGS.question = lineScan.next();
		parseTypeGS.response = lineScan.next();
		return lineScan.hasNextLine();
	}
	
	private boolean lineParseWR(Scanner lineScan){
		if(!coloumnSwitch){
			parseTypeWR.workerId = lineScan.next();
			parseTypeWR.question = lineScan.next();
			parseTypeWR.response = lineScan.next();
		} else {
			parseTypeWR.question = lineScan.next();
			parseTypeWR.workerId = lineScan.next();
			parseTypeWR.response = lineScan.next();
		}
		return lineScan.hasNextLine();
	}
	
	private boolean lineParseCategories(Scanner lineScan, boolean wPrior){
		if(wPrior){
			parseTypeCategories.category = lineScan.next();
			parseTypeCategories.prior = lineScan.nextDouble();
		} else {
			parseTypeCategories.category = lineScan.next();
		}
		return lineScan.hasNextLine();
	}

}
