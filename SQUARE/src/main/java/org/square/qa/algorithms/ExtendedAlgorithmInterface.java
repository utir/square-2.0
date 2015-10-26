package org.square.qa.algorithms;

public interface ExtendedAlgorithmInterface<TypeWID,TypeQ,TypeR> extends AlgorithmInterface<TypeWID,TypeQ,TypeR> {
	/**
	 * Checks if algorithm is configured to run 'unsupervised'
	 * @return true if model is configured to estimate labels without supervision
	 */
	public boolean isUnsupervisedModel();
	
	/**
	 * Checks if algorithm is configured to run with 'light-supervision'
	 * @return true if model is configured to estimate labels with light-supervision
	 */
	public boolean isSemiSupervisedModel();
	
	/**
	 * Checks if algorithm is configured to run with 'supervision'
	 * @return true if model is configured to estimate labels with supervision
	 */
	public boolean isSupervisedModel();
	
	/**
	 * Checks if worker priors are supported
	 * @return true if worker priors are supported
	 */
	public boolean hasWorkerPriors();
}
