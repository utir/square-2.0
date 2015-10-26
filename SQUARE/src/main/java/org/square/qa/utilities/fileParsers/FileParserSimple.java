package org.square.qa.utilities.fileParsers;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.io.File;
import java.io.IOException;

import org.square.qa.utilities.constructs.workersDataStruct;

public class FileParserSimple {
	
	private static class parseTypeGS{
		private static int question = -1;
		private static int response = -1;
	}
	
	private static class parseTypeWR{
		private static String workerId = null;
		private static int question = -1;
		private static int response = -1;
	}
	
	private String fileName;
	
	public void setFileName(String fileName){
		this.fileName = fileName;
	}
	
	public void getFileName(String fileName){
		this.fileName = fileName;
	}
	
	public Map<String,workersDataStruct<Integer,Integer> > parseWorkerLabels() throws IOException{
		Map<String,workersDataStruct<Integer,Integer> > workersMap = new HashMap<String, workersDataStruct<Integer,Integer>>();
		File file = new File(fileName);
	
		Scanner lineScan = new Scanner(file);
		boolean fileEnd = true;
		while(fileEnd){
			fileEnd = lineParseWR(lineScan);
			fileEnd = lineScan.hasNextLine();
			if(workersMap.containsKey(parseTypeWR.workerId)){
				workersDataStruct<Integer,Integer> currentWorkerStruct = workersMap.get(parseTypeWR.workerId);
				currentWorkerStruct.insertWorkerResponse(parseTypeWR.question, parseTypeWR.response);
				workersMap.put(parseTypeWR.workerId, currentWorkerStruct);
			}
			else{
				workersDataStruct<Integer,Integer> newWorker = new workersDataStruct<Integer,Integer>();
				newWorker.insertWorkerResponse(parseTypeWR.question, parseTypeWR.response);
				workersMap.put(parseTypeWR.workerId, newWorker);
			}
		}
//		lineScan.close();
		return workersMap;
	}
	public Map<Integer,Integer> parseGoldStandard() throws IOException{
		File file = new File(fileName);
		Scanner lineScan = new Scanner(file);
		Map<Integer,Integer> goldResponses = new HashMap<Integer, Integer>();
		boolean fileEnd = true;
		while(fileEnd){
			fileEnd = lineParseGS(lineScan);
			fileEnd = lineScan.hasNextLine();
			goldResponses.put(parseTypeGS.question, parseTypeGS.response);
		}
//		lineScan.close();
		return goldResponses;
	}
	private boolean lineParseGS(Scanner lineScan){
		parseTypeGS.question = lineScan.nextInt();
		parseTypeGS.response = lineScan.nextInt();
		return lineScan.hasNextLine();
	}
	private boolean lineParseWR(Scanner lineScan){
		parseTypeWR.workerId = lineScan.next();
		parseTypeWR.question = lineScan.nextInt();
		parseTypeWR.response = lineScan.nextInt();
		return lineScan.hasNextLine();
	}
}


