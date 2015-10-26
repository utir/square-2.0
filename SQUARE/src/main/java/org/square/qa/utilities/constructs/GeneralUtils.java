package org.square.qa.utilities.constructs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.jblas.DoubleMatrix;
import org.square.qa.utilities.fileParsers.FileParserJStrings;


@SuppressWarnings("unused")
public class GeneralUtils {
	private static Logger log = LogManager.getLogger(GeneralUtils.class);
	private static GeneralUtilsParameterized<String, String, String> paramUtils = new GeneralUtilsParameterized<String, String, String>();

	public static class nFoldSet{
		public static boolean hasGT = false;
		public static boolean useNFold = false;
		public static Map<String,workersDataStruct<String,String> > workersMap = null;
		public static Map<String,String> gold = null;
		public static Map<String,String> gt = null;
		public static TreeSet<String> responseCategories=null;
		public static List<Map<String,workersDataStruct<String,String> > > workerMaps = null;
		public static List<Map<String,workersDataStruct<String,String> > > workerMapsTune = null;
		public static List<Map<String,workersDataStruct<String,String> > > workerMapsTrainTune = null;
		public static List<Map<String,String> > goldSet = null;
		public static List<Map<String,String> > gtSet = null;
		public static List<Map<String,String> > gtSetTune = null;
		public static List<Map<String,Double> > classPriorSet = null;
		public static Map<String,Integer> workerToInt = null;
		public static Map<String,Integer> questionToInt = null;
		public static Map<String,Integer> categToInt = null;
		public static List<Set<String> > foldRetain = null;
		public static List<Set<String> > foldDiscard = null;}
	
	/**
	 * Get parameterized utils object
	 * @return GeneralUtilsParameterized object with worker ids, questions and responses instantiated as strings
	 */
	public static GeneralUtilsParameterized<String, String, String> getParamUtils(){
		return paramUtils;}
	
	/**
	 * Get the parameters of a Beta distribution for input mean and variance
	 * @param mean is double holding mean of the desired Beta distribution
	 * @param variance is a double holding the variance of the desired Beta distribution
	 * @return a Pair of Doubles holding a and b parameters of the corresponding Beta distribution
	 */
	public static Pair<Double, Double> getBetaParam(double mean, double variance){
		double a,b;
		double meanCube = mean*mean*mean;
		double varianceSq = variance*variance;
		a = (meanCube + meanCube/mean - mean*(varianceSq))/varianceSq;
		b = (a*(1.0d - mean))/mean;
		return (new Pair<Double,Double>(a,b));}
	
	/**
	 * Extract questions from workers map
	 * @param workersMap is a Map from workers (String) to an object holding worker questions and responses (Strings)
	 * @return a Set of questions (String)
	 */
	public static Set<String> getQuestions(Map<String,workersDataStruct<String,String> > workersMap){
		Set<String> questions = new HashSet<String>();
		for(String key:workersMap.keySet()){
			questions.addAll(workersMap.get(key).getWorkerResponses().keySet());}
		return questions;}
	
	/**
	 * 
	 * @param resultVector
	 * @param metrics is a Metrics object to hold computed metrics
	 * @param groundTruth is a Pair of DoubleMatrix and DoubleMatrix holding questions and responses
	 * @param gold is the supervised/semi-supervised data to be excluded from metrics computation
	 */
	@SafeVarargs
	public static void computeMetrics(DoubleMatrix resultVector,Metrics metrics,Pair<DoubleMatrix,DoubleMatrix> groundTruth, Pair<DoubleMatrix,DoubleMatrix>... gold){
		assert gold.length<=1:"Only one additional parameter accepted";
		boolean usedGold = false;
		if(gold.length == 1)
			usedGold = true;
		DoubleMatrix gtRelavent = resultVector.mul(groundTruth.getSecond());
		DoubleMatrix gt = groundTruth.getFirst();
		if(usedGold){
			gtRelavent.muli(gold[0].getSecond().eq(0.0d));
			gt = gt.mul(gold[0].getSecond().eq(0.0d));}
		int index = 0;
//		double accNum = 0;
//		double accDen = 0;
		for(int categ:metrics.getCategoriesList()){
			DoubleMatrix tempR = gtRelavent.eq((double)categ);
			DoubleMatrix tempGT = gt.eq((double)categ);
			double allCorrect = tempGT.get(tempGT.findIndices()).length;
			DoubleMatrix tempRTP = tempR.mul(tempGT);
			DoubleMatrix tempRFP = tempR.mul(tempGT.eq(0.0d));
			DoubleMatrix tempRTN = tempR.eq(0.0d).mul(gt).mul(tempGT.eq(0.0d).mul(gt));
			double tp = tempRTP.get(tempRTP.findIndices()).length;
			double fp = tempRFP.get(tempRFP.findIndices()).length;
			double tn = tempRTN.get(tempRTN.findIndices()).length;
			
			
			double precision = tp/(tp+fp);
			double recall = tp/allCorrect;
			double accuracy = (tp+tn)/(double)gt.get(gt.findIndices()).length;
			
			double fMeasure = 2.0d * ((precision*recall)/(precision+recall));
			metrics.getPrecision().put(index, precision);
			metrics.getRecall().put(index,recall);
			metrics.getfMeasure().put(index,fMeasure);
			metrics.getAccuracy().put(index, accuracy);
			index++;}
//		accNum = gtRelavent.get(gtRelavent.eq(gt).mul(gt).findIndices()).length;
//		accDen = gt.get(gt.findIndices()).length;
//		double accuracy = accNum/accDen;
//		metrics.setAccuracy(DoubleMatrix.ones(index).mul(accuracy));
		metrics.setAvgAccuracy(metrics.getAccuracy().mean());
		metrics.setAvgPrecision(metrics.getPrecision().mean());
		metrics.setAvgRecall(metrics.getRecall().mean());
		metrics.setAvgFMeasure(metrics.getfMeasure().mean());}
	
	/**
	 * Update NFold class with mapping from workers, questions and categories to Integer
	 * @param responseCategories is a Set of String holding response categories
	 */
	public static void fillNFoldClass(Set<String> responseCategories){
		nFoldSet.workerToInt = paramUtils.getWorkerIntMap(nFoldSet.workersMap);
		nFoldSet.questionToInt = paramUtils.getQuestionIntMap(nFoldSet.workersMap);
		nFoldSet.categToInt = paramUtils.getCategIntMap(responseCategories);}
	
	/**
	 * Compute n folds from data 5 fold -> 20% => n = 5 80% => n = -5
	 * @param n is an int (positive till 50% use symmetrically negative for higher folds)
	 */
	public static void setNFoldSets(int n){
		List<String> questions = new ArrayList<String>();
		nFoldSet.workerMaps = new ArrayList<Map<String,workersDataStruct<String,String> > >();
		nFoldSet.gtSet = new ArrayList<Map<String,String> >();
		nFoldSet.gtSetTune = new ArrayList<Map<String,String> >();
		nFoldSet.classPriorSet = new ArrayList<Map<String,Double> >();
		nFoldSet.workerMapsTune = new ArrayList<Map<String,workersDataStruct<String,String> > >();
		nFoldSet.workerMapsTrainTune = new ArrayList<Map<String,workersDataStruct<String,String> > >();
		nFoldSet.foldRetain = new ArrayList<Set<String> >();
		nFoldSet.foldDiscard = new ArrayList<Set<String> >();
		
		questions.addAll(getQuestions(nFoldSet.workersMap));
//		Collections.shuffle(questions);
		List<String> trainQuestions = questions; //Disregarding a test split.
		boolean reverse = false;
		if(n<0){
			reverse = true;
			n = n*(-1);}
		double splits = 1.0d/(double)n;
		int initIdx = 0;
		int splitIdx = 0;
		
		nFoldSet.workerMapsTrainTune.add(paramUtils.getFilteredWorkerMap(nFoldSet.workersMap, trainQuestions));
		
		for(int i = 1;i<=n;i++){
			initIdx = (int)Math.floor((i-1)*splits*(trainQuestions.size()));
			splitIdx = (int)Math.floor((i*splits*(trainQuestions.size())));
			List<String> tuneSet = new ArrayList<String>();
			tuneSet.addAll(trainQuestions.subList(initIdx, splitIdx));
			List<String> trainSet = new ArrayList<String>();
			if(initIdx!=0){
				trainSet.addAll(trainQuestions.subList(0, initIdx));}
			
			if(splitIdx!=trainQuestions.size()){
				trainSet.addAll(trainQuestions.subList(splitIdx, trainQuestions.size()));}
			
			if(!reverse){
				nFoldSet.workerMaps.add(paramUtils.getFilteredWorkerMap(nFoldSet.workersMap, trainSet));
				nFoldSet.workerMapsTune.add(paramUtils.getFilteredWorkerMap(nFoldSet.workersMap, tuneSet));
				Map<String, String> tempTune = paramUtils.getFilteredGT(nFoldSet.gt, tuneSet); 
				Map<String, String> tempTrain = paramUtils.getFilteredGT(nFoldSet.gt, trainSet); 
				nFoldSet.gtSetTune.add(tempTune);
				nFoldSet.foldRetain.add(tempTune.keySet());
				nFoldSet.foldDiscard.add(tempTrain.keySet());
				nFoldSet.gtSet.add(tempTrain);
			}else{
				nFoldSet.workerMaps.add(paramUtils.getFilteredWorkerMap(nFoldSet.workersMap, tuneSet));
				nFoldSet.workerMapsTune.add(paramUtils.getFilteredWorkerMap(nFoldSet.workersMap, trainSet));
				Map<String, String> tempTune = paramUtils.getFilteredGT(nFoldSet.gt, trainSet);
				Map<String, String> tempTrain = paramUtils.getFilteredGT(nFoldSet.gt, tuneSet); 
				nFoldSet.gtSetTune.add(tempTune);
				nFoldSet.foldRetain.add(tempTrain.keySet());
				nFoldSet.foldDiscard.add(tempTune.keySet());
				nFoldSet.gtSet.add(tempTrain);}
			}
			if(!reverse){
				Collections.rotate(nFoldSet.foldRetain, 1);
				Collections.rotate(nFoldSet.foldDiscard, 1);}
	}
	
	/**
	 * Print NFold data to files
	 * @param dir is a File holding the print path for files 
	 * @throws IOException
	 */
	public static void printAll(File dir) throws IOException{
		if(!dir.isDirectory())
			dir.mkdirs();
		String absPath;
		if(!dir.isAbsolute())
			absPath = dir.getAbsolutePath();
		else
			absPath = dir.getPath();

		if (GeneralUtils.nFoldSet.useNFold){
			paramUtils.printNumberedResponses(GeneralUtils.nFoldSet.workerMapsTrainTune.get(0),new File(absPath+"/responses_nFold_tuneEval_"+String.format("%03d",1) + ".txt"));
			paramUtils.printNumberedGT(GeneralUtils.nFoldSet.gt,new File(absPath+"/gt_nFold_tuneEval_"+String.format("%03d",1) + ".txt"));
			for(int i = 0;i<GeneralUtils.nFoldSet.workerMaps.size();i++){
				paramUtils.printNumberedResponses(GeneralUtils.nFoldSet.workerMaps.get(i),new File(absPath+"/responses_nFold_eval_"+String.format("%03d", i+1) + ".txt"));
				paramUtils.printNumberedResponses(GeneralUtils.nFoldSet.workerMapsTune.get(i),new File(absPath+"/responses_nFold_tune_"+String.format("%03d", i+1) + ".txt"));
				paramUtils.printNumberedGT(GeneralUtils.nFoldSet.gtSet.get(i),new File(absPath+"/gt_nFold_eval_"+String.format("%03d", i+1) + ".txt"));
				paramUtils.printNumberedGT(GeneralUtils.nFoldSet.gtSetTune.get(i),new File(absPath+"/gt_nFold_tune_"+String.format("%03d", i+1) + ".txt"));
				paramUtils.printNumberedQuestions(GeneralUtils.nFoldSet.foldRetain.get(i), new File(absPath+"/fold_retain_"+String.format("%03d", i+1) + ".txt"));
				paramUtils.printNumberedQuestions(GeneralUtils.nFoldSet.foldDiscard.get(i), new File(absPath+"/fold_discard_"+String.format("%03d", i+1) + ".txt"));}
		}else{
			paramUtils.printNumberedResponses(GeneralUtils.nFoldSet.workersMap,new File(absPath+"/responses_eval.txt"));
			if(GeneralUtils.nFoldSet.hasGT)
				paramUtils.printNumberedGT(GeneralUtils.nFoldSet.gt,new File(absPath+"/responses_gt.txt"));}
		
		File paramDir = new File(absPath+"/model");
		if(!paramDir.isDirectory())
			paramDir.mkdir();
		paramUtils.printMapFileWI(GeneralUtils.nFoldSet.workerToInt, new File(paramDir.getAbsolutePath()+"/map_worker_integer.txt"));
		paramUtils.printMapFileQI(GeneralUtils.nFoldSet.questionToInt, new File(paramDir.getAbsolutePath()+"/map_question_integer.txt"));
		paramUtils.printMapFileRI(GeneralUtils.nFoldSet.categToInt, new File(paramDir.getAbsolutePath()+"/map_category_integer.txt"));}
	
	/**
	 * Print data statistics 
	 * @param workersMap workersMap is a Map from workers to an object holding questions and responses
	 * @param outDir outDir of type File is the output directory to print files
	 * @throws FileNotFoundException
	 */
	public static void printStatistics(Map<String,workersDataStruct<String,String> > workersMap, File... outDir) throws FileNotFoundException{
		assert outDir.length<2:"Only one output directory path accepted";
		if(outDir.length==1)
			paramUtils.printStatistics(nFoldSet.questionToInt,nFoldSet.workerToInt,workersMap,outDir[0]);
		else
			paramUtils.printStatistics(nFoldSet.questionToInt,nFoldSet.workerToInt,workersMap);}
	
	/**
	 * Load n fold sets from files
	 * @param loadDir is a File holding path of the n fold files
	 * @throws IOException
	 */
	public static void loadNFoldSet(File loadDir) throws IOException{
		File files[] = loadDir.listFiles();
		nFoldSet.workerMaps = new ArrayList<Map<String,workersDataStruct<String,String> > >();
		nFoldSet.workerMapsTune = new ArrayList<Map<String,workersDataStruct<String,String> > >();
		nFoldSet.workerMapsTrainTune = new ArrayList<Map<String,workersDataStruct<String,String> > >();
		nFoldSet.gtSet = new ArrayList<Map<String,String> >();
		nFoldSet.gtSetTune = new ArrayList<Map<String,String> >();
		nFoldSet.responseCategories = new TreeSet<String>();
		nFoldSet.foldRetain = new ArrayList<Set<String> >();
		nFoldSet.foldDiscard = new ArrayList<Set<String> >();
		nFoldSet.categToInt = new HashMap<String, Integer>();
		nFoldSet.questionToInt = new HashMap<String, Integer>();
		nFoldSet.workerToInt = new HashMap<String, Integer>();
		
		//!!fix reading in map files and add support for fold retain and fold discard
		SortedMap<Integer,Map<String,workersDataStruct<String,String> > > tempWorkerMaps = new TreeMap<Integer,Map<String,workersDataStruct<String,String> > >();
		SortedMap<Integer,Map<String,workersDataStruct<String,String> > > tempWorkerMapsTune = new TreeMap<Integer,Map<String,workersDataStruct<String,String> > >();
		SortedMap<Integer,Map<String,workersDataStruct<String,String> > > tempWorkerMapsTrainTune = new TreeMap<Integer,Map<String,workersDataStruct<String,String> > >();
		SortedMap<Integer,Map<String,String> > tempGTSet = new TreeMap<Integer,Map<String,String> >();
		SortedMap<Integer,Map<String,String> > tempGTSetTune = new TreeMap<Integer,Map<String,String> >();
		SortedMap<Integer,Set<String> > tempFoldRetain = new TreeMap<Integer,Set<String> >();
		SortedMap<Integer,Set<String> > tempFoldDiscard = new TreeMap<Integer,Set<String> >();
		
		FileParserJStrings fParser = new FileParserJStrings();
		
		for(File file:files){
			if(file.isDirectory()){
				if(file.getName().equals("model"))
				{
					File modelFiles[] = file.listFiles();
					for(File modelFile:modelFiles)
					{
						String modelFileName = modelFile.getName();
						if(modelFileName.equalsIgnoreCase("map_category_integer.txt"))
						{
							fParser.setFileName(modelFile.getAbsolutePath());
							Map<String,String> categToInt = fParser.parseGoldStandard();
							for(String oldName:categToInt.keySet())
							{
								nFoldSet.responseCategories.add(categToInt.get(oldName));
								nFoldSet.categToInt.put(categToInt.get(oldName), Integer.parseInt(categToInt.get(oldName)));
							}
						}
						
						if(modelFileName.equalsIgnoreCase("map_question_integer.txt"))
						{
							fParser.setFileName(modelFile.getAbsolutePath());
							Map<String,String> questionToInt = fParser.parseGoldStandard();
							for(String oldName:questionToInt.keySet())
							{
								nFoldSet.questionToInt.put(questionToInt.get(oldName), Integer.parseInt(questionToInt.get(oldName)));
							}
						}
						
						if(modelFileName.equalsIgnoreCase("map_worker_integer.txt"))
						{
							fParser.setFileName(modelFile.getAbsolutePath());
							Map<String,String> workerToInt = fParser.parseGoldStandard();
							for(String oldName:workerToInt.keySet())
							{
								nFoldSet.workerToInt.put(workerToInt.get(oldName), Integer.parseInt(workerToInt.get(oldName)));
							}
						}
						
						
						
					}
				} else {
					continue;}}
			
			String filename = file.getName();
			String[] parts = filename.split("\\.");
			
			if(parts.length <= 1)
				continue;
			
			if(parts[1].equalsIgnoreCase("txt")){
				String[] innerParts = parts[0].split("_");
				if(innerParts[innerParts.length-1].equalsIgnoreCase("eval")){
					//unsupervised may or may not have gold available
					fParser.coloumnSwitch = true;
					fParser.setFileName(file.getAbsolutePath());
					nFoldSet.workersMap = fParser.parseWorkerLabels();
					fParser.coloumnSwitch = false;
				} else if(innerParts[innerParts.length-1].equalsIgnoreCase("gt")){
					//unsupervised gt
					nFoldSet.hasGT = true;
					fParser.setFileName(file.getAbsolutePath());
					nFoldSet.gt = fParser.parseGoldStandard();
				}else if(innerParts[innerParts.length-2].equalsIgnoreCase("eval")){
					Integer idx = Integer.valueOf(innerParts[innerParts.length-1]);
					if(innerParts[0].equalsIgnoreCase("responses")){
						fParser.coloumnSwitch = true;
						fParser.setFileName(file.getAbsolutePath());
						tempWorkerMaps.put(idx,fParser.parseWorkerLabels());
						fParser.coloumnSwitch = false;
					} else if(innerParts[0].equalsIgnoreCase("gt")){
						fParser.setFileName(file.getAbsolutePath());
						tempGTSet.put(idx,fParser.parseGoldStandard());}
				}else if(innerParts[innerParts.length-2].equals("tune")){
					Integer idx = Integer.valueOf(innerParts[innerParts.length-1]);
					if(innerParts[0].equalsIgnoreCase("responses")){
						fParser.coloumnSwitch = true;
						fParser.setFileName(file.getAbsolutePath());
						tempWorkerMapsTune.put(idx,fParser.parseWorkerLabels());
						fParser.coloumnSwitch = false;
					} else if(innerParts[0].equalsIgnoreCase("gt")){
						fParser.setFileName(file.getAbsolutePath());
						tempGTSetTune.put(idx,fParser.parseGoldStandard());}
				}else if(innerParts[innerParts.length-2].equals("tuneEval")){
					Integer idx = Integer.valueOf(innerParts[innerParts.length-1]);
					if(innerParts[0].equalsIgnoreCase("responses")){
						nFoldSet.useNFold = true;
						
						fParser.coloumnSwitch = true;
						fParser.setFileName(file.getAbsolutePath());
						nFoldSet.workerMapsTrainTune.add(fParser.parseWorkerLabels());
						nFoldSet.workersMap = nFoldSet.workerMapsTrainTune.get(0);
						fParser.coloumnSwitch = false;
					}else if(innerParts[0].equalsIgnoreCase("gt")){
						nFoldSet.hasGT = true;
						
						fParser.setFileName(file.getAbsolutePath());
						nFoldSet.gt = fParser.parseGoldStandard();
					}
				} else if(innerParts[innerParts.length-2].equals("retain")){
					//fold retain
					Integer idx = Integer.valueOf(innerParts[innerParts.length-1]);
					fParser.setFileName(file.getAbsolutePath());
					tempFoldRetain.put(idx, fParser.parseFoldRR());
				} else if(innerParts[innerParts.length-2].equals("discard")){
					//fold discard
					Integer idx = Integer.valueOf(innerParts[innerParts.length-1]);
					fParser.setFileName(file.getAbsolutePath());
					tempFoldDiscard.put(idx, fParser.parseFoldRR());
				}}}
		
		for(int idx:tempGTSet.keySet()){
			nFoldSet.workerMaps.add(tempWorkerMaps.get(idx)); 
			nFoldSet.workerMapsTune.add(tempWorkerMapsTune.get(idx)); 
			nFoldSet.gtSet.add(tempGTSet.get(idx)); 
			nFoldSet.gtSetTune.add(tempGTSetTune.get(idx));
			nFoldSet.foldRetain.add(tempFoldRetain.get(idx));
			nFoldSet.foldDiscard.add(tempFoldDiscard.get(idx));}}
}
