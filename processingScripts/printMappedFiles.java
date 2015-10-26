import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
public class printMappedFiles {
	Map<String,String> original;
	Map<String,String> mapped;
	Map<String,String> categMap;
	public printMappedFiles(){
		original = new HashMap<String,String>();
		mapped = new HashMap<String,String>();}

	public void loadData(File name, Boolean type) throws IOException{
		Scanner in = new Scanner(name);
		while(in.hasNextLine()){
			String line = in.nextLine();
			
			String parts[] = line.split("\t");
			if(parts.length == 1)
				parts = line.split(" ");

			if(type){
				original.put(parts[0],categMap.get(parts[parts.length-1]));
			} else {
				mapped.put(parts[0],parts[1]);}}}

	public void loadCategMap(File name) throws IOException{
		Scanner in = new Scanner(name);
		categMap = new HashMap<String,String>();
		while(in.hasNextLine()){
			String line = in.nextLine();
			String parts[] = line.split(" ");
			categMap.put(parts[1],parts[0]);}}

	public void printMappedFile(File saveFile) throws IOException{
		PrintWriter out = new PrintWriter(saveFile);
		for(String question:mapped.keySet()){
			out.println(question + " " + original.get(mapped.get(question)));}
		out.close();}

	public static void main(String... args) throws IOException{
		printMappedFiles obj = new printMappedFiles();
		obj.loadCategMap(new File(args[2]));
		obj.loadData(new File(args[0]),true);
		obj.loadData(new File(args[1]),false);
		obj.printMappedFile(new File(args[3]));}}