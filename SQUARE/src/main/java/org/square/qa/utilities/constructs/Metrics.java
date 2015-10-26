package org.square.qa.utilities.constructs;

import java.util.List;
import org.jblas.DoubleMatrix;

public class Metrics{
	private double avgAccuracy;
	private double avgPrecision;
	private double avgRecall;
	private double avgFMeasure;
	private DoubleMatrix precision;
	private DoubleMatrix recall;
	private DoubleMatrix accuracy;
	private DoubleMatrix fMeasure;
	private List<Integer> categoriesList;
	
	/**
	 * Constructor
	 * @param categoriesList is a List holding Doubles mapped to response categories
	 */
	public Metrics(List<Integer> categoriesList){
		setAvgAccuracy(0.0d);
		setAvgPrecision(0.0d);
		setAvgRecall(0.0d);
		setAvgFMeasure(0.0d);
		this.categoriesList = categoriesList;
		setPrecision(DoubleMatrix.zeros(categoriesList.size()));
		setRecall(DoubleMatrix.zeros(categoriesList.size()));
		setAccuracy(DoubleMatrix.zeros(categoriesList.size()));
		setfMeasure(DoubleMatrix.zeros(categoriesList.size()));}
	
	/**
	 * Get average accuracy
	 * @return a double holding average accuracy
	 */
	public double getAvgAccuracy() {
		return avgAccuracy;}
	
	/**
	 * Set average accuracy
	 * @param avgAccuracy is a double
	 */
	public void setAvgAccuracy(double avgAccuracy) {
		this.avgAccuracy = avgAccuracy;}
	
	/**
	 * Get average precision
	 * @return a double holding average precision
	 */
	public double getAvgPrecision() {
		return avgPrecision;}
	
	/**
	 * Set average precision
	 * @param avgPrecision is a double
	 */
	public void setAvgPrecision(double avgPrecision) {
		this.avgPrecision = avgPrecision;}
	
	/**
	 * Get average recall
	 * @return a double holding average recall 
	 */
	public double getAvgRecall() {
		return avgRecall;}
	
	/**
	 * Set average recall
	 * @param avgRecall is a double 
	 */
	public void setAvgRecall(double avgRecall) {
		this.avgRecall = avgRecall;}
	
	/**
	 * Get average f-measure
	 * @return a double holding average f-measure
	 */
	public double getAvgFMeasure() {
		return avgFMeasure;}
	
	/**
	 * Set average f-measure 
	 * @param avgFMeasure is a double 
	 */
	public void setAvgFMeasure(double avgFMeasure) {
		this.avgFMeasure = avgFMeasure;}
	
	/**
	 * Get precision for each category
	 * @return a DoubleMatrix with precision for each category (in order of categories list)
	 */
	public DoubleMatrix getPrecision() {
		return precision;}
	
	/**
	 * Set precision for each category
	 * @param precision is a DoubleMatrix with precision computed for each category (in order of categories list)
	 */
	public void setPrecision(DoubleMatrix precision) {
		this.precision = precision;}
	
	/**
	 * Get recall for each category
	 * @return a DoubleMatrix with recall computed for each category (in order of categories list)
	 */
	public DoubleMatrix getRecall() {
		return recall;}
	
	/**
	 * Set Recall for each category
	 * @param recall is a DoubleMatrix holding computed recall for each category (in order of categories list)
	 */
	public void setRecall(DoubleMatrix recall) {
		this.recall = recall;}
	
	/**
	 * Get accuracy for each category
	 * @return a DoubleMatrix with accuracy computed for each category (in order of categories list)
	 */
	public DoubleMatrix getAccuracy() {
		return accuracy;}
	
	/**
	 * Set Accuracy for each category
	 * @param accuracy is a DoubleMatrix holding computed accuracy for each category (in order of categories list)
	 */
	public void setAccuracy(DoubleMatrix accuracy) {
		this.accuracy = accuracy;}
	
	/**
	 * Get categories 
	 * @return List of Doubles -- mapped response categories
	 */
	public List<Integer> getCategoriesList() {
		return categoriesList;}
	
	/**
	 * Get f-measure for each category
	 * @return a DoubleMatrix with f-measure computed for each category (in order of categories list)
	 */
	public DoubleMatrix getfMeasure() {
		return fMeasure;}
	
	/**
	 * Set f-measure for each category
	 * @param fMeasure is a DoubleMatrix holding computed f-measure for each category (in order of categories list)
	 */
	public void setfMeasure(DoubleMatrix fMeasure) {
		this.fMeasure = fMeasure;}
	
	public String toString(){
		return "Accuracy: "+avgAccuracy+" FMeasure: "+avgFMeasure;}
	
	/**
	 * Print computed metrics
	 * @param printInfo is a boolean -- when set true prints column headers
	 * @return a String with formatted metrics
	 */
	public String getPrintString(boolean printInfo){
		String outString = new String();
		if(printInfo){
			outString = "%Accuracy\tPrecision\tRecall\tFMeasure\n%";
			for(double categ:categoriesList)
				outString = outString+(int)categ+"\t";
			outString = outString+"\n";}
		for(int i = 0;i<accuracy.length;i++){
			Double temp = accuracy.get(i);
			if(temp.isNaN())
				temp = 1.0d;
			if(temp.isInfinite())
				temp = 0.0d;
			outString = outString +String.format("%1.5f", temp)+ "\t";}
		for(int i = 0;i<precision.length;i++){
			Double temp = precision.get(i);
			if(temp.isNaN())
				temp = 1.0d;
			if(temp.isInfinite())
				temp = 0.0d;
			outString = outString + String.format("%1.5f", temp) + "\t";}
		for(int i = 0;i<recall.length;i++){
			Double temp = recall.get(i);
			if(temp.isNaN())
				temp = 1.0d;
			if(temp.isInfinite())
				temp = 0.0d;
			outString = outString + String.format("%1.5f", temp) + "\t";}
		for(int i = 0;i<fMeasure.length;i++){
			Double temp = fMeasure.get(i);
			if(temp.isNaN())
				temp = 1.0d;
			if(temp.isInfinite())
				temp = 0.0d;
			outString = outString + String.format("%1.5f", temp) + "\t";}
		outString = outString+"\n";
		return outString;}}
