package org.square.qa.analysis;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;


import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.square.qa.algorithms.BayesGeneralized;
import org.square.qa.algorithms.BinaryMapEstRY;
import org.square.qa.algorithms.MajorityVoteGeneralized;
import org.square.qa.algorithms.ZenCrowdEM;
import org.square.qa.utilities.constructs.GeneralUtils;
import org.square.qa.utilities.constructs.Models;
import org.square.qa.utilities.constructs.Pair;
import org.square.qa.utilities.constructs.Results;
import org.square.qa.utilities.constructs.workersDataStruct;
import org.square.qa.utilities.fileParsers.FileParserJStrings;

final class Main {
	private String responsesFile;
	private String goldFile;
	private String groundTruthFile;
	private String categoriesFile;
	private String categoriesPriorFile;
	private estType estimationType;
	private int numIterations;
	private int nFold;
	private static Logger log = LogManager.getLogger(Main.class);
	private File outDir = null;
	private File loadDir = null;
	
	private ArrayList<String> chosenMethods;
	
	private Main(){
		responsesFile = null;
		goldFile = null;
		groundTruthFile = null;
		categoriesFile = null;
		categoriesPriorFile = null;
		nFold = 0;
		numIterations = 50;
		estimationType = estType.unsupervised;
		chosenMethods = new ArrayList<String>();}
	
	public enum estType{
		unsupervised,semiSupervised,supervised;}
	
	/**
	 * Loads environment variables 
	 * @param args is a String[] of command line arguments 
	 */
	public void setupEnvironment(String[] args){
		  int argIndex = 0;
		  boolean minReq = false;
		  
		  while (argIndex < args.length && args[argIndex].startsWith("--")){
			  if(args[argIndex].equalsIgnoreCase("--responses")){
				  assert minReq == false:"Load path defined!";
				
				  responsesFile = args[++argIndex];
				  minReq = true;
				  log.info("Worker responses file specified: "+responsesFile);}
			  
			  if(args[argIndex].equalsIgnoreCase("--method")){
				  argIndex++;
				  String printChosen = new String("");
				  while(!args[argIndex].startsWith("--")){
					  if(args[argIndex].equalsIgnoreCase("All")){
						  chosenMethods.clear();
						  chosenMethods.add("All");
						  printChosen = "Raykar Bayes Zen Majority";
						  break;
					  } else if (args[argIndex].equalsIgnoreCase("Majority")) {
						  chosenMethods.add("Majority");
						  printChosen = printChosen + "Majority ";
					  } else if (args[argIndex].equalsIgnoreCase("Raykar")) {
						  chosenMethods.add("Raykar");
						  printChosen = printChosen + "Raykar ";
					  } else if (args[argIndex].equalsIgnoreCase("Bayes")){
						  chosenMethods.add("Bayes");
						  printChosen = printChosen + "Bayes ";
					  } else if (args[argIndex].equalsIgnoreCase("Zen")){
						  chosenMethods.add("Zen");
						  printChosen = printChosen + "Zen ";}}
				  log.info("Chosen estimation method: "+printChosen);}
			
			  if(args[argIndex].equalsIgnoreCase("--estimation")){
				  argIndex++;
				  if(args[argIndex].equalsIgnoreCase("unsupervised")){
					  estimationType = estType.unsupervised;
				  } else if (args[argIndex].equalsIgnoreCase("semiSupervised")) {
					  estimationType = estType.semiSupervised;
				  } else if (args[argIndex].equalsIgnoreCase("supervised")) {
					  estimationType = estType.supervised;
				  } else {
					  log.error("Unable to parse estimation type");}
				  log.info("Chosen estimation type: "+estimationType);}
			  
			  if(args[argIndex].equalsIgnoreCase("--category")){
				  categoriesFile = args[++argIndex]; 
				  log.info("Categories file specified: " + categoriesFile);}
			  
			  if(args[argIndex].equalsIgnoreCase("--categoryPrior")){
				  categoriesPriorFile = args[++argIndex]; 
				  log.info("Categories file specified: "+categoriesPriorFile);}
			  
			  if(args[argIndex].equalsIgnoreCase("--gold")){
				  goldFile = args[++argIndex];
				  log.info("Gold responses file specified: "+goldFile);}
			  
			  if(args[argIndex].equalsIgnoreCase("--groundTruth")){
				  groundTruthFile = args[++argIndex];
				  log.info("Ground truth file specified: "+groundTruthFile);}
			  
			  if(args[argIndex].equalsIgnoreCase("--numIterations")){
				  numIterations = Integer.parseInt(args[++argIndex]);
				  log.info("Number of iterations specified: "+numIterations);}
			  
			  if(args[argIndex].equalsIgnoreCase("--nFold")){
				  GeneralUtils.nFoldSet.useNFold = true;
				  nFold = Integer.parseInt(args[++argIndex]);
				  log.info(nFold+"-Fold Evaluation.");}
			  
			  if(args[argIndex].equalsIgnoreCase("--saveDir")){
				  outDir = new File(args[++argIndex]);
				  log.info("Saving files in: " + outDir);}
			  
			  if(args[argIndex].equalsIgnoreCase("--loadDir")){
				  assert minReq == false:"Worker Responses defined! Loading will overwrite";
				  minReq = true;
				  loadDir = new File(args[++argIndex]);
				  log.info("Loading files from: " + loadDir);}
			  argIndex++;}
		  assert minReq:"\nResponses File Not Defined -- see usage";}
	
	/**
	 * Loads data from input files 
	 * @throws IOException
	 */
	private void initializeFromFiles() throws IOException{
		assert responsesFile!=null:"Environment not initialized";
		
		FileParserJStrings fParser = new FileParserJStrings();
		
		log.info("Attempting to parse worker responses.");
		
		fParser.setFileName(responsesFile);
		Map<String,workersDataStruct<String,String> > workersMap = fParser.parseWorkerLabels();
		GeneralUtils.nFoldSet.workersMap = workersMap;
	
		Map<String,String> goldResponses = null;	
		if(goldFile!=null){
			log.info("Attempting to parse gold responses.");
			fParser.setFileName(goldFile);
			goldResponses = fParser.parseGoldStandard();
			GeneralUtils.nFoldSet.gold = goldResponses;}
		
		Map<String,String> groundTruth = null;
		if(groundTruthFile!=null){
			log.info("Attempting to parse ground truth responses.");
			fParser.setFileName(groundTruthFile); 
			groundTruth = fParser.parseGoldStandard();
			GeneralUtils.nFoldSet.gt = groundTruth;
			GeneralUtils.nFoldSet.hasGT = true;}
		
		Map<String,Double> categMapPriors=null;
		if(categoriesPriorFile!=null){
			fParser.setFileName(categoriesPriorFile);
			categMapPriors = fParser.parseCategoriesWPrior();
			if(log.isDebugEnabled())
				log.debug("Contents of Category and Prior Map:\n"+categMapPriors);}
		
		TreeSet<String> responseCategories=null;
		if(categoriesFile!=null){
			responseCategories = new TreeSet<String>();
			fParser.setFileName(categoriesFile);
			responseCategories.addAll(fParser.parseCategories());
			if(log.isDebugEnabled())
				log.debug("Contents of sorted response categories set:\n"+responseCategories);}
		
		if(categoriesPriorFile!=null && responseCategories==null){
			responseCategories = new TreeSet<String>();
			for(String category:categMapPriors.keySet()){
				responseCategories.add(category);}
			if(log.isDebugEnabled())
				log.debug("Contents of sorted response categories set:\n"+responseCategories);}
		
		if(GeneralUtils.nFoldSet.hasGT){
			log.info("Restricting Responses to Questions with Ground Truth");
			List<String> questionsWGT = new ArrayList<String>(); 
			questionsWGT.addAll(GeneralUtils.getParamUtils().getQuestionsGT(GeneralUtils.nFoldSet.gt));
			GeneralUtils.nFoldSet.workersMap = GeneralUtils.getParamUtils().getFilteredWorkerMap(GeneralUtils.nFoldSet.workersMap,questionsWGT);}
		
		GeneralUtils.nFoldSet.responseCategories = responseCategories;
		GeneralUtils.fillNFoldClass(responseCategories);
		
		if(GeneralUtils.nFoldSet.useNFold){
			log.info("Adopting N Fold Evaluation.");
			if(nFold>0){
				int tempFoldNum = 100/nFold;
				log.info("Using " + tempFoldNum + "% train fraction");
			}else{
				int tempFoldNum = nFold*-1;
				tempFoldNum = 100/tempFoldNum;
				tempFoldNum = 100 - tempFoldNum;
				log.info("Using " + tempFoldNum + "% train fraction");}
			
			GeneralUtils.nFoldSet.useNFold = true;
			GeneralUtils.setNFoldSets(nFold);}
		
	}

	private void loadFromSavedFiles() throws IOException{
		GeneralUtils.loadNFoldSet(loadDir);}
	
	/**
	 * Performs n-fold evaluation
	 * @throws IOException
	 */
	public void flow() throws IOException{
		if(loadDir!=null)
			loadFromSavedFiles();
		
		if(outDir == null)
			outDir = new File("./results");
		
		if(loadDir==null){
			initializeFromFiles();
			GeneralUtils.printAll(outDir);
			GeneralUtils.printStatistics(GeneralUtils.nFoldSet.workersMap,new File(outDir.getAbsolutePath()+"/statistics/train"));}
			
		GeneralUtils.printStatistics(GeneralUtils.nFoldSet.workersMap);
		Models<String,String,String> models = new Models<String, String, String>();
		Map<String,Results<String,String> > resultObjects = new HashMap<String, Results<String,String>>();
		
		if(!GeneralUtils.nFoldSet.useNFold){
			if(estimationType.equals(estType.unsupervised)){
				for (String chosenMethod:chosenMethods)
				{
					if(chosenMethod.equals("Zen")||chosenMethod.equals("All"))
					{
						models.getZenModel().useClassPrior = true;
						models.getZenModel().useWorkerPrior = true;}
					if(chosenMethod.equals("Raykar")||chosenMethod.equals("All"))
					{
						models.getRaykarModel().useClassPrior = true;
						models.getRaykarModel().useWorkerPrior = true;
					}
				}
			}
			
			if(estimationType.equals(estType.supervised)) //FIX
			{
				for (String chosenMethod:chosenMethods)
				{
					if(chosenMethod.equals("Bayes")||chosenMethod.equals("All")){
//						models.getBayesModel().setGoldStandard(GeneralUtils.nFoldSet.gold); //Bayes is supervised
//						models.getBayesModel().setWorkersMapGold(GeneralUtils.nFoldSet.);
					} //Bayes is supervised
					if(chosenMethod.equals("Zen")||chosenMethod.equals("All")){
//						models.getZenModel().setGoldStandard(GeneralUtils.nFoldSet.gtSetTune.get(i));
//						models.getZenModel().setWorkersMap(GeneralUtils.nFoldSet.workerMapsTrainTune.get(i));
//						models.getZenModel().useClassPrior = true;
//						models.getZenModel().useWorkerPrior = true;
//						models.setTuneGT(GeneralUtils.nFoldSet.gtSetTune.get(i));
//						models.setWorkersMapTune(GeneralUtils.nFoldSet.workerMapsTune.get(i));
					}
					if(chosenMethod.equals("Raykar")||chosenMethod.equals("All")){
//						models.getRaykarModel().setGoldStandard(GeneralUtils.nFoldSet.gtSetTune.get(i));
//						models.getRaykarModel().setWorkersMap(GeneralUtils.nFoldSet.workerMapsTrainTune.get(i));
//						models.getRaykarModel().useClassPrior = true;
//						models.getRaykarModel().useWorkerPrior = true;
//						models.setTuneGT(GeneralUtils.nFoldSet.gtSetTune.get(i));
//						models.setWorkersMapTune(GeneralUtils.nFoldSet.workerMapsTune.get(i));
					}
				}
			}
			
			
			if(estimationType.equals(estType.semiSupervised))//FIX
			{
				for (String chosenMethod:chosenMethods)
				{
					if(chosenMethod.equals("Zen")||chosenMethod.equals("All"))
					{
//						models.getZenModel().useClassPrior = true;
//						models.getZenModel().useWorkerPrior = true;
					}
					if(chosenMethod.equals("Raykar")||chosenMethod.equals("All"))
					{
//						models.getRaykarModel().useClassPrior = true;
//						models.getRaykarModel().useWorkerPrior = true;
					}
//					models.setTuneGT(GeneralUtils.nFoldSet.gtSetTune.get(i));
//					models.setWorkersMapTune(GeneralUtils.nFoldSet.workerMapsTune.get(i));
				}
			}
			models.setWorkersMap(GeneralUtils.nFoldSet.workersMap);
			models.setResponseCategories(GeneralUtils.nFoldSet.responseCategories);
			estimateLabels(models, resultObjects, null); //####
		}else{
			for(int i = 0;i<GeneralUtils.nFoldSet.workerMaps.size();i++){
				models.setWorkersMap(GeneralUtils.nFoldSet.workerMaps.get(i));
				models.setResponseCategories(GeneralUtils.nFoldSet.responseCategories);
				
				if(estimationType.equals(estType.supervised))
				{
					for (String chosenMethod:chosenMethods)
					{
						if(chosenMethod.equals("Bayes")||chosenMethod.equals("All"))
						{
							models.getBayesModel().setGoldStandard(GeneralUtils.nFoldSet.gtSetTune.get(i)); //Bayes is supervised
							models.getBayesModel().setWorkersMapGold(GeneralUtils.nFoldSet.workerMapsTune.get(i));
						} //Bayes is supervised
						if(chosenMethod.equals("Zen")||chosenMethod.equals("All")){
							models.getZenModel().setGoldStandard(GeneralUtils.nFoldSet.gtSetTune.get(i));
							models.getZenModel().setWorkersMap(GeneralUtils.nFoldSet.workerMapsTrainTune.get(0));
							models.getZenModel().useClassPrior = true;
							models.getZenModel().useWorkerPrior = true;
							models.setTuneGT(GeneralUtils.nFoldSet.gtSetTune.get(i));
							models.setWorkersMapTune(GeneralUtils.nFoldSet.workerMapsTune.get(i));}
						if(chosenMethod.equals("Raykar")||chosenMethod.equals("All")){
							models.getRaykarModel().setGoldStandard(GeneralUtils.nFoldSet.gtSetTune.get(i));
							models.getRaykarModel().setWorkersMap(GeneralUtils.nFoldSet.workerMapsTrainTune.get(0));
							models.getRaykarModel().useClassPrior = true;
							models.getRaykarModel().useWorkerPrior = true;
							models.setTuneGT(GeneralUtils.nFoldSet.gtSetTune.get(i));
							models.setWorkersMapTune(GeneralUtils.nFoldSet.workerMapsTune.get(i));
						}
					}
				}
				
				
				if(estimationType.equals(estType.semiSupervised))
				{
					for (String chosenMethod:chosenMethods)
					{
						if(chosenMethod.equals("Zen")||chosenMethod.equals("All"))
						{
							models.getZenModel().useClassPrior = true;
							models.getZenModel().useWorkerPrior = true;
						}
						if(chosenMethod.equals("Raykar")||chosenMethod.equals("All"))
						{
							models.getRaykarModel().useClassPrior = true;
							models.getRaykarModel().useWorkerPrior = true;
						}
						models.setTuneGT(GeneralUtils.nFoldSet.gtSetTune.get(i));
						models.setWorkersMapTune(GeneralUtils.nFoldSet.workerMapsTune.get(i));
					}
				}
				estimateLabels(models, resultObjects, GeneralUtils.nFoldSet.foldRetain.get(i));
			}
		}
		printResults(resultObjects);
	}

	public void printResults (Map<String,Results<String,String> > resultObjects) throws IOException{
		
		Map<String,String> printStrings = null;
		Map<String,String> printRLabelStrings = new HashMap<String, String>();
		
		if(GeneralUtils.nFoldSet.hasGT)
			printStrings = new HashMap<String, String>();
		
		for(String methodString:resultObjects.keySet())
		{
			Results<String,String> thisResults = resultObjects.get(methodString); 
			thisResults.setCategToInt(GeneralUtils.nFoldSet.categToInt);
			thisResults.computeComparableResuts();
			thisResults.computeComparableResultVector();
			
			if(GeneralUtils.nFoldSet.hasGT){
				thisResults.setGroundTruth(GeneralUtils.nFoldSet.gt);
				//need to check if gold or tune fraction exists for non n-fold eval
				thisResults.computeMetrics(false, false);
				log.info(methodString + ": " + thisResults.getMetrics()+"\n");
				printStrings.put(methodString, thisResults.getMetrics().getPrintString(true));}
			
			printRLabelStrings.put(methodString,thisResults.printComparableResults(GeneralUtils.nFoldSet.questionToInt));
			printResultFiles(printStrings, printRLabelStrings);
				
			if(log.isDebugEnabled()){
				log.debug(thisResults.getComparableResultVector());
			}
		}
	}
	
	public void printResultFiles(Map<String,String> printStrings, Map<String,String> printRLabelStrings) throws IOException{
		//print to file n-fold eval
		File resultsDir = null;
		if(printStrings!=null){
			resultsDir = new File(outDir.getAbsolutePath()+"/results/nFold");
			if(!resultsDir.exists()){
				resultsDir.mkdirs();}}
		
		File aggregatedLabelsDir = new File(outDir.getAbsolutePath()+"/results/nFold/aggregated");
		if(!aggregatedLabelsDir.exists()){
			aggregatedLabelsDir.mkdirs();}
		
		for(String key:printRLabelStrings.keySet()){
			String name = "supervised";
			
			if(key.equals("Majority"))
				name = "unsupervised";
			
			if(key.equals("Zen")){
				if(estimationType.toString().equals("unsupervised"))
					name = "unsupervised";
				if(estimationType.toString().equals("semiSupervised"))
					name = "semisupervised";
				if(estimationType.toString().equals("supervised"))
					name = "supervised";}
			
			if(key.equals("Raykar")){
				if(estimationType.toString().equals("unsupervised"))
					name = "unsupervised";
				if(estimationType.toString().equals("semiSupervised"))
					name = "semisupervised";
				if(estimationType.toString().equals("supervised"))
					name = "supervised";}
			
			if(printStrings!=null){
				PrintWriter out = new PrintWriter(new File(resultsDir.getAbsolutePath() + "/"+key+"_"+name+"_results.txt"));
				out.print(printStrings.get(key));
				out.close();}
			
			PrintWriter out = new PrintWriter(new File(aggregatedLabelsDir.getAbsolutePath() + "/"+key+"_"+name+"_aggregated.txt"));
			out.print(printRLabelStrings.get(key));
			out.close();
			}
	}
	
	
	/**
	 * 
	 * @param models
	 * @return
	 */
	public void estimateLabels(Models<String,String,String> models, Map<String, Results<String,String>> resultObjects, Set<String> foldRetain){
		
		for(String chosenMethod:chosenMethods){
			if(chosenMethod.equalsIgnoreCase("Majority")||chosenMethod.equalsIgnoreCase("all")){
				if(!estimationType.equals(estType.unsupervised)){
					log.info("In the current implementation Majority Vote does not need supervision.");
				} else {
					MajorityVoteGeneralized<String, String, String> majority = new MajorityVoteGeneralized<String, String, String>(models.getMajorityModel());
					majority.computeLabelEstimates();
					Map<String, Pair<String, Map<String, Double>>> thisEstLabels = models.getMajorityModel().getCombinedEstLabels();
					if(foldRetain!=null)
						thisEstLabels.keySet().retainAll(foldRetain);
					if(!resultObjects.containsKey("Majority")){
						resultObjects.put("Majority", new Results<String, String>());}
					resultObjects.get("Majority").addCompiledResults(thisEstLabels);}}
			if (chosenMethod.equalsIgnoreCase("Raykar")||chosenMethod.equalsIgnoreCase("all")) {
				if(models.getRaykarModel().getResponseCategories().size()>2){
					//Raykar currently only supports binary estimation
					log.info("In the current implementation Raykar is a binary estimation method.");
				} else {
					models.getRaykarModel().setPositiveClass(models.getRaykarModel().getResponseCategories().iterator().next());
					BinaryMapEstRY<String, String, String> raykar = new BinaryMapEstRY<String, String, String>(models.getRaykarModel());
					raykar.computeLabelEstimates();
					Map<String, Pair<String, Map<String, Double>>> thisEstLabels = models.getRaykarModel().getCombinedEstLabels();
					if(foldRetain!=null)
						thisEstLabels.keySet().retainAll(foldRetain);
					if(!resultObjects.containsKey("Raykar")){
						resultObjects.put("Raykar", new Results<String, String>());}
					resultObjects.get("Raykar").addCompiledResults(thisEstLabels);}}
			if (chosenMethod.equalsIgnoreCase("Zen")||chosenMethod.equalsIgnoreCase("all")) {
				ZenCrowdEM<String, String, String> zen = new ZenCrowdEM<String, String, String>(models.getZenModel());
				zen.computeLabelEstimates();
				Map<String, Pair<String, Map<String, Double>>> thisEstLabels = models.getZenModel().getCombinedEstLabels();
				if(foldRetain!=null)
					thisEstLabels.keySet().retainAll(foldRetain);
				if(!resultObjects.containsKey("Zen")){
					resultObjects.put("Zen", new Results<String, String>());}
				resultObjects.get("Zen").addCompiledResults(thisEstLabels);}
			if (chosenMethod.equalsIgnoreCase("Bayes")||chosenMethod.equalsIgnoreCase("all")) {
				if(!estimationType.equals(estType.supervised)){
					log.info("Bayes estimation requires supervision");
				} else {
				if(models.getBayesModel().hasGoldStandard()){
					models.getBayesModel().setLapAlpha(1.0d);
					models.getBayesModel().setLapBeta((double)models.getBayesModel().getResponseCategories().size());
					models.getBayesModel().computeDefaultNewWorkerConfusion();
					BayesGeneralized<String, String, String> bayes = new BayesGeneralized<String, String, String>(models.getBayesModel(),false);
					bayes.computeLabelEstimates();
					Map<String, Pair<String, Map<String, Double>>> thisEstLabels = models.getBayesModel().getCombinedEstLabels();
					if(foldRetain!=null)
						thisEstLabels.keySet().retainAll(foldRetain);
					if(!resultObjects.containsKey("Bayes")){
						resultObjects.put("Bayes", new Results<String, String>());}
					resultObjects.get("Bayes").addCompiledResults(thisEstLabels);}}
			}
		}
	}
	
	/**
	 * Main function
	 * @param args is a String[] which holds command line args
	 * @throws IOException
	 */
	public static void main(String... args) throws IOException {
		assert args.length != 0 : "Usage: org.square.qa.analysis.Main --responses [responsesFile] --category [categoriesFile] --gold [goldFile] --groundTruth [groundTruthFile] --categoryPrior [categoryPriorFile] --numIteration [numIterations] --method <Majority|Bayes|Raykar|Zen|All> --nfold [n]\nRequired Parameters: --responses --category\nOptional usage(specify file with args): --file [file]";
		
		boolean usingFile = false;
		
		Main obj;
		
		if(args[0].equalsIgnoreCase("--file")){
			usingFile = true;
			File argsFile = new File(args[1]);
			Scanner lineScan = new Scanner(argsFile);
			while(lineScan.hasNextLine()){
				String tempArgs = lineScan.nextLine();
				if(tempArgs.equals(""))
					continue;
				String[] currentArgs = tempArgs.split(" ");
				obj = new Main();
				obj.setupEnvironment(currentArgs);
				obj.flow();
			}
			lineScan.close();
		}
		
		if(!usingFile){
			obj = new Main();
			obj.setupEnvironment(args);
			obj.flow();}}}
