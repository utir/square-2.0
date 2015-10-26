package org.square.qa.utilities.constructs;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import org.jblas.DoubleMatrix;
import org.square.qa.algorithms.AlgorithmInterface;
import org.square.qa.algorithms.ExtendedAlgorithmInterface;


class BaseModels<TypeWID,TypeQ,TypeR> implements AlgorithmInterface<TypeWID, TypeQ, TypeR> {
	protected Map<TypeWID,workersDataStruct<TypeQ,TypeR> > workersMap = null;
	protected TreeSet<TypeR> responseCategories = null;
	protected Map<TypeWID,workersDataStruct<TypeQ,TypeR> > workersMapTune = null;
	protected Map<TypeWID, workersDataStruct<TypeQ, TypeR> > workersMapGold = null;
	protected Map<TypeQ,TypeR> tuneGT = null;
	protected Map<TypeQ,TypeR> goldStandard = null;
	protected Map<TypeQ, Pair<TypeR,Map<TypeR,Double> > > combinedEstLabels = null;
	
	public Map<TypeWID,workersDataStruct<TypeQ,TypeR> > getWorkersMap() {
		assert hasWorkersMap():"Workers Map not initiliazed";
		return workersMap;}
	
	public void setWorkersMap(Map<TypeWID,workersDataStruct<TypeQ,TypeR> > workersMap) {
		this.workersMap = workersMap;}
	
	public TreeSet<TypeR> getResponseCategories() {
		assert hasResponseCategories():"Response Categories not initiliazed";
		return responseCategories;}
	
	public void setResponseCategories(TreeSet<TypeR> responseCategories) {
		this.responseCategories = responseCategories;}
	
	public Map<TypeWID,workersDataStruct<TypeQ,TypeR> > getWorkersMapTune() {
		assert hasWorkersMapTune():"Workers Tune Map not initiliazed";
		return workersMapTune;}
	
	public void setWorkersMapTune(Map<TypeWID,workersDataStruct<TypeQ,TypeR> > workersMapTune) {
		this.workersMapTune = workersMapTune;}
	
	public Map<TypeWID,workersDataStruct<TypeQ,TypeR> > getWorkersMapGold() {
		assert hasWorkersMapGold():"Workers Gold Map not initiliazed";
		return workersMapGold;}
	
	public void setWorkersMapGold(Map<TypeWID,workersDataStruct<TypeQ,TypeR> > workersMapGold) {
		this.workersMapGold = workersMapGold;}
	
	public Map<TypeQ,TypeR> getTuneGT() {
		assert hasTuneGT():"GT Tune not initiliazed";
		return tuneGT;}
	
	public void setTuneGT(Map<TypeQ,TypeR> tuneGT) {
		this.tuneGT = tuneGT;}
	
	public Map<TypeQ,TypeR> getGoldStandard() {
		assert hasGoldStandard():"Gold Standard not initiliazed";
		return goldStandard;}
	
	public void setGoldStandard(Map<TypeQ,TypeR> goldStandard) {
		this.goldStandard = goldStandard;}
	
	public boolean hasWorkersMap(){
		if(workersMap!=null)
			return true;
		else
			return false;}
	
	public boolean hasWorkersMapTune(){
		if(workersMapTune!=null)
			return true;
		else
			return false;}
	
	public boolean hasWorkersMapGold(){
		if(workersMapGold!=null)
			return true;
		else
			return false;}
	
	public boolean hasResponseCategories(){
		if(responseCategories!=null)
			return true;
		else
			return false;}
	
	public boolean hasGoldStandard(){
		if(goldStandard!=null)
			return true;
		else 
			return false;}
	
	public boolean hasTuneGT(){
		if(tuneGT!=null)
			return true;
		else
			return false;}
	
	public Map<TypeQ, Pair<TypeR,Map<TypeR,Double> > > getCombinedEstLabels() {
		assert hasCombinedEstLabels():"Combined Labels not initiliazed";
		return combinedEstLabels;}
	
	public boolean hasCombinedEstLabels(){
		if(combinedEstLabels!=null)
			return true;
		else
			return false;}
	
	public void setCombinedEstLabels(Map<TypeQ, Pair<TypeR,Map<TypeR,Double> > > combinedEstLabels) {
		this.combinedEstLabels = combinedEstLabels;}}

public class Models<TypeWID,TypeQ,TypeR> {
	
	/**
	 * 
	 */
	public Models(){
		this.raykarModel = new RaykarModel();
		this.rTestModel = new RaykarModel();
		
		this.zenModel = new ZenModel();
		this.zTestModel = new ZenModel();
		
		this.bayesModel = new BayesModel();
		this.bTestModel = new BayesModel();
		
		this.majorityModel = new MajorityModel();
		this.mTestModel = new MajorityModel();}
	
	/**
	 * Set workers responses for each model
	 * @param workersMap is a Map from workers to an object holding questions and responses
	 */
	public void setWorkersMap(Map<TypeWID,workersDataStruct<TypeQ,TypeR> > workersMap){
		raykarModel.setWorkersMap(workersMap);
		zenModel.setWorkersMap(workersMap);
		bayesModel.setWorkersMap(workersMap);
		majorityModel.setWorkersMap(workersMap);}
	
	/**
	 * Set response categories for each model
	 * @param responseCategories is a TreeSet of response categories
	 */
	public void setResponseCategories(TreeSet<TypeR> responseCategories) {
		raykarModel.setResponseCategories(responseCategories);
		zenModel.setResponseCategories(responseCategories);
		bayesModel.setResponseCategories(responseCategories);
		majorityModel.setResponseCategories(responseCategories);}
	
	/**
	 * Set workers responses from the tune (light-supervision) partition
	 * @param workersMapTune is a Map from workers to an object holding questions and responses
	 */
	public void setWorkersMapTune(Map<TypeWID,workersDataStruct<TypeQ,TypeR> > workersMapTune) {
		raykarModel.setWorkersMapTune(workersMapTune);
		
		zenModel.setWorkersMapTune(workersMapTune);}
	
	/**
	 * Set workers responses from the gold (full-supervision) partition
	 * @param workersMapGold is a Map from workers to an object holding questions and responses
	 */
	public void setWorkersMapGold(Map<TypeWID,workersDataStruct<TypeQ,TypeR> > workersMapGold) {
		bayesModel.setWorkersMapGold(workersMapGold);}
	
	/**
	 * Set ground truth for tune (light-supervision) partition
	 * @param tuneGT is a Map from questions to responses
	 */
	public void setTuneGT(Map<TypeQ,TypeR> tuneGT) {
		raykarModel.setTuneGT(tuneGT);
		
		zenModel.setTuneGT(tuneGT);}
	
	/**
	 * Set ground truth for gold (full-supervision) partition
	 * @param goldStandard is a Map from workers to an object holding questions and responses
	 */
	public void setGoldStandard(Map<TypeQ,TypeR> goldStandard) {
//		zenModel.setGoldStandard(goldStandard);
		
//		bayesModel.setGoldStandard(goldStandard);
	}
	
	/**
	 * Set workers responses for each of the test models
	 * @param workersMap is a Map from workers to an object holding questions and responses
	 */
	public void setWorkersMapTest(Map<TypeWID,workersDataStruct<TypeQ,TypeR> > workersMap){
		rTestModel.setWorkersMap(workersMap);
		
		zTestModel.setWorkersMap(workersMap);
		
		bTestModel.setWorkersMap(workersMap);
		
		mTestModel.setWorkersMap(workersMap);}
	
	/**
	 * Set response categories for each of the test models
	 * @param responseCategories is a TreeSet of response categories
	 */
	public void setResponseCategoriesTest(TreeSet<TypeR> responseCategories) {
		rTestModel.setResponseCategories(responseCategories);
		
		zTestModel.setResponseCategories(responseCategories);
		
		bTestModel.setResponseCategories(responseCategories);
		
		mTestModel.setResponseCategories(responseCategories);}
	
//******************* START Raykar Model
	public class RaykarModel extends BaseModels<TypeWID, TypeQ, TypeR> implements ExtendedAlgorithmInterface<TypeWID, TypeQ, TypeR>{
		
		private Map<TypeWID, Pair<Pair<Double,Double>,Pair<Double,Double> > > workerPriors = null;
		private Pair<Double,Double> posClassBetaParam = null;
		private TypeR positiveClass= null;
		public boolean useClassPrior = false;
		public boolean useWorkerPrior = false;

		public boolean isUnsupervisedModel(){
			return hasWorkersMap()&&hasResponseCategories()&&(!hasTuneGT())&&(!hasWorkersMapTune())&&(!hasGoldStandard());}
		
		public boolean isSemiSupervisedModel(){
			return hasWorkersMap()&&hasResponseCategories()&&(hasTuneGT())&&(hasWorkersMapTune())&&(!hasGoldStandard());}
		
		public boolean isSupervisedModel(){
			return hasWorkersMap()&&hasResponseCategories()&&(hasGoldStandard());
//			return hasWorkersMap()&&hasResponseCategories()&&(hasTuneGT())&&(hasWorkersMapTune())&&(hasGoldStandard());
		}

		public boolean hasWorkerPriors(){
			if(workerPriors!=null)
				return true;
			else
				return false;}
		
		/**
		 * Get worker priors
		 * @return a Map from workers to priors
		 */
		public Map<TypeWID, Pair<Pair<Double,Double>,Pair<Double,Double> > > getWorkerPriors() {
			assert hasWorkerPriors():"Worker priors not initiliazed";
			return workerPriors;}
		
		/**
		 * Set worker priors
		 * @param workerPriors is a Map from workers to priors
		 */
		public void setWorkerPriors(Map <TypeWID, Pair<Pair<Double,Double>,Pair<Double,Double> > > workerPriors) {
			this.workerPriors = workerPriors;}
		
		/**
		 * Check if positive class Beta distribution is set
		 * @return true is Beta parameters are available
		 */
		public boolean hasPosClassBetaParam(){
			if(posClassBetaParam != null)
				return true;
			else
				return false;}
		
		/**
		 * Get positive class Beta distribution parameters
		 * @return a Pair of Beta parameters
		 */
		public Pair<Double,Double> getPosClassBetaParam() {
			assert hasPosClassBetaParam():"Class priors not initiliazed";
			return posClassBetaParam;}
		
		/**
		 * Set positive class Beta distribution parameters
		 * @param posClassBetaParam is a Pair of Doubles holding positive class beta parameters
		 */
		public void setPosClassBetaParam(Pair<Double,Double> posClassBetaParam) {
			this.posClassBetaParam = posClassBetaParam;}
		
		/**
		 * Update worker priors
		 * @param workerPriors is a Map from workers to priors
		 */
		public void updateWorkerPriors(Map<TypeWID, Pair<Pair<Double,Double>,Pair<Double,Double> > > workerPriors){
			assert hasWorkerPriors():"Worker priors not initiliazed";
			for(TypeWID key:workerPriors.keySet()){
				if(this.workerPriors.containsKey(key)){
					Pair<Pair<Double,Double>,Pair<Double,Double> > first = this.workerPriors.get(key);
					Pair<Pair<Double,Double>,Pair<Double,Double> > second = workerPriors.get(key);
					Pair<Double,Double> firstFirst = first.getFirst();
					Pair<Double,Double> firstSecond = first.getSecond(); 
					Pair<Double,Double> secondFirst = second.getFirst();
					Pair<Double,Double> secondSecond = second.getSecond(); 
					Pair<Double,Double> firstAvg = new Pair<Double, Double>((firstFirst.getFirst()+secondFirst.getFirst())/2.0d, (firstFirst.getSecond()+secondFirst.getSecond())/2.0d);
					Pair<Double,Double> secondAvg =  new Pair<Double, Double>((firstSecond.getFirst()+secondSecond.getFirst())/2.0d, (firstSecond.getSecond()+secondSecond.getSecond())/2.0d);
					this.workerPriors.put(key, new Pair<Pair<Double,Double>,Pair<Double,Double> >(firstAvg,secondAvg));
				} else {
					this.workerPriors.put(key, workerPriors.get(key));}}}
		
		/**
		 * Update positive class prior
		 * @param posClassBetaParam is a Pair of Doubles holding positive class beta parameters
		 */
		public void updateClassPriorParam(Pair<Double,Double> posClassBetaParam){
			assert hasPosClassBetaParam():"Class priors not initiliazed";
			double thisFirst = this.posClassBetaParam.getFirst();
			double otherFirst = posClassBetaParam.getFirst();
			double thisSecond = this.posClassBetaParam.getSecond();
			double otherSecond = posClassBetaParam.getSecond();
			this.posClassBetaParam = new Pair<Double, Double>((thisFirst+otherFirst)/2.0d, (thisSecond+otherSecond)/2.0d);}
		
		/**
		 * Check for Postive Class
		 * @return a boolean indicating presence of Postive class
		 */
		public boolean hasPositiveClass(){
			if(positiveClass!=null)
				return true;
			else
				return false;}
		
		/**
		 * Get Positive class
		 * @return positive class
		 */
		public TypeR getPositiveClass() {
			assert hasPositiveClass():"Positive Class Not Initialized";
			return positiveClass;}
		
		/**
		 * Set positive class
		 * @param positiveClass is of response type holding positive class 
		 */
		public void setPositiveClass(TypeR positiveClass) {
			this.positiveClass = positiveClass;}}
	
	private RaykarModel rTestModel;
	private RaykarModel raykarModel;
	
	/**
	 * Get RaykarModel -- test configuration
	 * @return RaykarModel from Models
	 */
	public RaykarModel getRaykarTestModel() {
		return rTestModel;}
	
	/**
	 * Set RaykarModel -- test configuration
	 * @param rTestModel is a RaykarModel from Models
	 */
	public void setRaykarTestModel(RaykarModel rTestModel) {
		this.rTestModel = rTestModel;}
	
	/**
	 * Get RaykarModel
	 * @return RaykarModel
	 */
	public RaykarModel getRaykarModel() {
		return raykarModel;}
	
	/**
	 * Set RaykarModel
	 * @param raykarModel is a RaykarModel
	 */
	public void setRaykarModel(RaykarModel raykarModel) {
		this.raykarModel = raykarModel;}
	
//******************* END Raykar Model
	
//******************* START Zen Crowd Model
	
	public class ZenModel extends BaseModels<TypeWID, TypeQ, TypeR> implements ExtendedAlgorithmInterface<TypeWID, TypeQ, TypeR>{
		
		private Map<TypeWID,Double> workerReliabilityMap = null;
		private Map<TypeWID, Pair<Double,Double> > workerPriors = null;
		private DoubleMatrix classPriorParam = null;
		public boolean useClassPrior = false;
		public boolean useWorkerPrior = false;

		public boolean isUnsupervisedModel(){
			return hasWorkersMap()&&hasResponseCategories()&&(!hasTuneGT())&&(!hasWorkersMapTune())&&(!hasGoldStandard());}
		
		public boolean isSemiSupervisedModel(){
			return hasWorkersMap()&&hasResponseCategories()&&(hasTuneGT())&&(hasWorkersMapTune())&&(!hasGoldStandard());}
		
		public boolean isSupervisedModel(){
			return hasWorkersMap()&&hasResponseCategories()&&(hasGoldStandard());
//			return hasWorkersMap()&&hasResponseCategories()&&(hasTuneGT())&&(hasWorkersMapTune())&&(hasGoldStandard());
		}
		
		/**
		 * Get worker reliability
		 * @return worker reliability as a Map from workers to doubles
		 */
		public Map<TypeWID,Double> getWorkerReliabilityMap() {
			assert hasWorkerReliabilityMap():"Worker Reliability Map not initiliazed";
			return workerReliabilityMap;}
		
		/**
		 * Set worker reliability
		 * @param workerReliabilityMap is a Map from workers to doubles
		 */
		public void setWorkerReliabilityMap(Map<TypeWID,Double> workerReliabilityMap) {
			this.workerReliabilityMap = workerReliabilityMap;}
		
		/**
		 * Check is worker reliability is computed/loaded
		 * @return true if worker reliability scores are available
		 */
		public boolean hasWorkerReliabilityMap(){
			if(workerReliabilityMap!=null)
				return true;
			else
				return false;}
		
		public boolean hasWorkerPriors(){
			if(workerPriors!=null)
				return true;
			else
				return false;}
		
		/**
		 * Get worker priors
		 * @return worker priors as a Map from workers to priors
		 */
		public Map<TypeWID, Pair<Double,Double> > getWorkerPriors() {
			assert hasWorkerPriors():"Worker priors not initiliazed";
			return workerPriors;}
		
		/**
		 * Set worker priors
		 * @param workerPriors is a Map from workers to priors
		 */
		public void setWorkerPriors(Map<TypeWID, Pair<Double,Double> > workerPriors) {
			this.workerPriors = workerPriors;}
		
		/**
		 * Check availability of class prior
		 * @return true if class prior is available
		 */
		public boolean hasClassPriorParam(){
			if(classPriorParam != null)
				return true;
			else
				return false;}
		
		/**
		 * Get class prior
		 * @return class prior as a DoubleMatrix
		 */
		public DoubleMatrix getClassPriorParam() {
			assert hasClassPriorParam():"Class priors not initiliazed";
			return classPriorParam;}
		
		/**
		 * Set class prior
		 * @param classPriorParam is a DoubleMatrix of class priors
		 */
		public void setClassPriorParam(DoubleMatrix classPriorParam) {
			this.classPriorParam = classPriorParam;}
		
		/**
		 * Update worker priors
		 * @param workerPriors is a Map from workers to priors
		 */
		public void updateWorkerPriors(Map<TypeWID, Pair<Double,Double> > workerPriors){
			assert hasWorkerPriors():"Worker priors not initiliazed";
			for(TypeWID key:workerPriors.keySet()){
				if(this.workerPriors.containsKey(key)){
					Pair<Double,Double> first = this.workerPriors.get(key);
					Pair<Double,Double> second = workerPriors.get(key);
					Pair<Double,Double> avg = new Pair<Double, Double>((first.getFirst()+second.getFirst())/2.0d, (first.getSecond()+second.getSecond())/2.0d);
					this.workerPriors.put(key, avg);
				} else {
					this.workerPriors.put(key, workerPriors.get(key));}}}
		
		/**
		 * Update class priors
		 * @param classPriorParam is a DoubleMatrix of class priors
		 */
		public void updateClassPriorParam(DoubleMatrix classPriorParam){
			assert hasClassPriorParam():"Class priors not initiliazed";
			this.classPriorParam.addi(classPriorParam).divi(2.0d);}}
	
	private ZenModel zTestModel;
	private ZenModel zenModel;
	
	/**
	 * Get ZenModel -- test configuration
	 * @return ZenModel
	 */
	public ZenModel getZenTestModel() {
		return zTestModel;}
	
	/**
	 * Set ZenModel -- test configuration
	 * @param zTestModel is a ZenModel 
	 */
	public void setZenTestModel(ZenModel zTestModel) {
		this.zTestModel = zTestModel;}
	
	/**
	 * Get ZenModel
	 * @return ZenModel
	 */
	public ZenModel getZenModel() {
		return zenModel;}
	
	/**
	 * Set ZenModel
	 * @param zenModel is a ZenModel
	 */
	public void setZenModel(ZenModel zenModel) {
		this.zenModel = zenModel;}
	
//******************* END Zen Crowd Model	

//******************* START Bayes Model
	
	public class BayesModel extends BaseModels<TypeWID, TypeQ, TypeR>{
		private Map<TypeWID, Map<TypeR,Map<TypeR,Double> > > workerConfusionMaps = null;
		private double lapAlpha = -1;
		private double lapBeta = -1;
		private Map<TypeR,Map<TypeR,Double> > newWorkerConfusionMap = null;
		
		/**
		 * Check for presence of worker confusion maps
		 * @return true if worker confusion maps are present
		 */
		public boolean hasWorkerConfusionMaps(){
			if(workerConfusionMaps!=null)
				return true;
			else
				return false;}
		
		/**
		 * Get worker confusion maps
		 * @return worker confusion maps -- Map from workers to confusion maps
		 */
		public Map<TypeWID,Map<TypeR,Map<TypeR,Double> > > getWorkerConfustionMaps() {
			assert hasWorkerConfusionMaps():"Worker condusion maps not initiliazed";
			return workerConfusionMaps;}
		
		/**
		 * Set worker confusion maps
		 * @param workerConfusionMaps is Map from workers to confusion maps
		 */
		public void setWorkerConfusionMaps(Map<TypeWID,Map<TypeR,Map<TypeR,Double> > > workerConfusionMaps) {
			this.workerConfusionMaps = workerConfusionMaps;}
		
		/**
		 * Update worker confusion maps
		 * @param workerConfusionMaps is Map form workers to confusion maps
		 */
		public void updateWorkerConfusionMaps(Map<TypeWID,Map<TypeR,Map<TypeR,Double> > > workerConfusionMaps){
			for(TypeWID key:workerConfusionMaps.keySet()){
				if(this.workerConfusionMaps.containsKey(key)){
					Map<TypeR,Map<TypeR,Double> > first = this.workerConfusionMaps.get(key);
					Map<TypeR,Map<TypeR,Double> > second = workerConfusionMaps.get(key);
					Map<TypeR,Map<TypeR,Double> > avg = new HashMap<TypeR, Map<TypeR,Double>>();
					for(TypeR iter:responseCategories){
						Map<TypeR,Double> firstInner = first.get(iter);
						Map<TypeR,Double> secondInner = second.get(iter);
						Map<TypeR,Double> avgInner = new HashMap<TypeR, Double>();
						for(TypeR innerIter:responseCategories){
							avgInner.put(innerIter, ((firstInner.get(innerIter)+secondInner.get(innerIter))/2.0d));}
						avg.put(iter, avgInner);}
					this.workerConfusionMaps.put(key, avg);
				} else {
					this.workerConfusionMaps.put(key, workerConfusionMaps.get(key));}}}
		
		/**
		 * Check is Laplacian alpha smoothing parameter is loaded
		 * @return true if the Laplacian aplha smoothing parameter is loaded
		 */
		public boolean hasLapAlpha(){
			if(lapAlpha!=-1)
				return true;
			else
				return false;}
		
		/**
		 * Get Laplacian alpha smoothing parameter
		 * @return double holding Laplacian alpha smoothing parameter
		 */
		public double getLapAlpha() {
			assert hasLapAlpha():"Lap Alpha not initiliazed";
			return lapAlpha;}
		
		/**
		 * Set Laplacian alpha smoothing parameter
		 * @param lapAlpha is a double holding Laplacian alpha smoothing parameter
		 */
		public void setLapAlpha(double lapAlpha) {
			this.lapAlpha = lapAlpha;}
		
		/**
		 * Check is Laplacian beta smoothing parameter is loaded
		 * @return true if the Laplacian beta smoothing parameter is loaded
		 */
		public boolean hasLapBeta(){
			if(lapBeta!=-1)
				return true;
			else
				return false;}
		
		/**
		 * Get Laplacian beta smoothing parameter
		 * @return double holding Laplacian beta smoothing parameter
		 */
		public double getLapBeta() {
			assert hasLapBeta():"Lap Beta not initiliazed";
			return lapBeta;}
		
		/**
		 * Set Laplacian beta smoothing parameter
		 * @param lapBeta is a double holding Laplacian beta smoothing parameter
		 */
		public void setLapBeta(double lapBeta) {
			this.lapBeta = lapBeta;}
		
		/**
		 * Check presence of confusion map default for new workers  
		 * @return true if a default confusion map is loaded 
		 */
		public boolean hasNewWorkerConfusion(){
			if(newWorkerConfusionMap!=null)
				return true;
			else
				return false;}
		
		/**
		 * Get default worker confusion map
		 * @return a Map from responses to confusion maps
		 */
		public Map<TypeR,Map<TypeR,Double> > getNewWorkerConfusion(){
			assert hasNewWorkerConfusion():"New worker confusion not initialized";
		
			return newWorkerConfusionMap;}
		
		/**
		 * Set default worker confusion map
		 * @param newWorkerConfusionMap is a Map from responses to confusion maps
		 */
		public void setNewWorkerConfusion(HashMap<TypeR, Map<TypeR,Double> > newWorkerConfusionMap){
			this.newWorkerConfusionMap = newWorkerConfusionMap;}
		
		/**
		 * Compute default worker confusion map
		 */
		public void computeDefaultNewWorkerConfusion(){
			assert hasResponseCategories():"Response Categories not initialized";
			newWorkerConfusionMap = new HashMap<TypeR, Map<TypeR,Double> >();
			for(TypeR iter:responseCategories){
				Map<TypeR,Double> tempMap = new HashMap<TypeR, Double>();
				for(TypeR innerIter:responseCategories){
					tempMap.put(innerIter, 1.0d/(double)responseCategories.size());}
				newWorkerConfusionMap.put(iter, tempMap);}}}
	
	private BayesModel bTestModel;
	private BayesModel bayesModel;
	
	/**
	 * Get BayesModel -- test configuration 
	 * @return BayesModel
	 */
	public BayesModel getBayesTestModel() {
		return bTestModel;}
	
	/**
	 * Set BayesModel -- test configuration
	 * @param bTestModel is a BayesModel
	 */
	public void setBayesTestModel(BayesModel bTestModel) {
		this.bTestModel = bTestModel;}
	
	/**
	 * Get BayesModel
	 * @return BayesModel
	 */
	public BayesModel getBayesModel() {
		return bayesModel;}
	
	/**
	 * Set BayesModel
	 * @param bayesModel is a BayesModel
	 */
	public void setBayesModel(BayesModel bayesModel) {
		this.bayesModel = bayesModel;}
	
//******************* END Bayes Model	

//******************* START Majority Vote Model
	
	public class MajorityModel extends BaseModels<TypeWID, TypeQ, TypeR> {
		Map<TypeR,Double> classPriors = null;
		
		/**
		 * Set class priors
		 * @param classPriors is a Map from class categories to doubles
		 */
		public void setClassPriors(Map<TypeR,Double> classPriors){
			this.classPriors = classPriors;}
		
		/**
		 * Set class priors
		 * @param classPriors is an array of doubles
		 */
		public void setClassPriors(double... classPriors){
			this.classPriors = new HashMap<TypeR, Double>();
			int i = 0;
			for(TypeR categ:responseCategories){
				this.classPriors.put(categ, classPriors[i]);
				i++;}}
		
		/**
		 * Get class priors
		 * @return class priors as a Map from class categories to doubles
		 */
		public Map<TypeR,Double> getClassPriors(){
			assert hasClassPriors():"Class Priors not Defined";
			return classPriors;}
		
		/**
		 * Check for presence of class priors
		 * @return true if class priors are present
		 */
		public boolean hasClassPriors(){
			if(classPriors!=null)
				return true;
			else
				return false;}}
	
	private MajorityModel mTestModel;
	private MajorityModel majorityModel;
	
	/**
	 * Get MajorityModel -- test configuration
	 * @return MajorityModel
	 */
	public MajorityModel getMajorityTestModel() {
		return mTestModel;}
	
	/**
	 * Set MajorityModel -- test configuration
	 * @param mTestModel is a MajorityModel
	 */
	public void setMajorityTestModel(MajorityModel mTestModel) {
		this.mTestModel = mTestModel;}
	
	/**
	 * Get MajorityModel
	 * @return MajorityModel
	 */
	public MajorityModel getMajorityModel() {
		return majorityModel;}
	
	/**
	 * Set MajorityModel
	 * @param majorityModel is a MajorityModel
	 */
	public void setMajorityModel(MajorityModel majorityModel) {
		this.majorityModel = majorityModel;}	
//******************* END Majority Vote Model
}
