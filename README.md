SQUARE-2.0 (Statistical QUality Assurance Robustness Evaluation)
================================================================

#Whats New!
1. **An improved evaluation methodology** 
    
    SQUARE 2.0 implements n-fold evaluation such that the culmination of an n-fold run results in an aggregated file with 100% of the questions. Evaluation metrics are comupted only after 100% of the result set has been computed. 
    
    As an example consider 20% supervision, at each iteration only 20% of the evaluation data is retained to populate the result set, with the rest 60% discarded, similarly rolling the 20% over the whole dataset results in populating 100% of the result set. Upon computing the result set (aggregated data) evaluation metrics are computed.
    
    Usupervised estimation is now possible with out breaking the data into folds. 

2. **A new simulation framework**

    A realistic simulation framework is implemented where workers are represented in a 3-d voxel grid spanning true-positive, 
true-negative and participation rates. The framework enables generating crowd data from n crowd types on a single ground truth
instance. Additionally, this opens up experiments with mixing the crowd with experts. For more information on modeling and possible use cases read [A Collaborative Approach to IR Evaluation] (http://www.aashish.me/docs/thesis.pdf).

4. **More datasets**

    Evaluation on datasets from CrowdScale 2013 (http://www.crowdscale.org/shared-task) and MediaEval 2013 (http://www.multimediaeval.org/mediaeval2013/crowd2013/).
    
    [Benchmark Results] (square-2.0/blob/master/benchmark_results.png)
    
    [Unsupervised Plot] (square-2.0/blob/master/real_unsupervised_plot.png)

5. **Algorithm tweaks**

    Majority voting ties are broken with class prior if available. 

6. **Updated dependencies and Simple Usage**

    Upgraded to Java 8, updated logging and linear algebra dependencies. 
    HCB dataset included with exmaples to get the framework running.


#Installation

**Building SQUARE**

1. cd SQUARE

2. mvn install

#Usage
1. **Run *SQUARE* first before running the benchmark.**

    — Running SQUARE sets up a directory structure which other scripts depend on.
    
    **Getting the data ready for SQUARE**
    1. Create a data folder.
    2. Create a folder to hold crowd responses, name the folder to reflect contents.
    3. Add the following files to the folder:
        
        a) *categories.txt* - each line names the category 
        
        b) *responses.txt* - space separated values with each line of the format
                            *workerId* *question* *response*
        
        c) *groundTruth.txt* - space separated values with each line of the format
                              *question* *response*
        
        As an example look at the included data folder.

    **Setting up the run environment**
    — You can either run each configuration individually, or group them into a file and process the file.
    Look at the *_CLA.txt files for batch processing. 

    — To generate the complete directory structure you are required to run all the supervision/semi-supervision
    levels including unsupervised. As an example file *genNFoldRealData_CLA.txt* contains run configurations
    for supervision/semi-supervision and the file *genUnsupervisedRealData_CLA.txt* contains unsupervised
    configuration. 

    — Running SQUARE will present a directory structure with a folder with the dataset name at the top level.
      Following are some of the important folders:
          — Results: Computed performance metrics for each algorithm. The sub folder aggregated contains resulting aggregated 
                     files from each algorithm.
          — Statistics: Worker, response, participation statistics.
          — Model: Contains mapping information from the original question, response, workerID types to integers. 

    Note that if you do not have ground truth, you can only run SQUARE unsupervised.
    
    With the files ready you can run the following from the root folder:

    ```
    java -Xmx2048m -ea -cp ./SQUARE/target/lib/jblas-1.2.4.jar:./SQUARE/target/lib/log4j-core-2.4.jar:./SQUARE/target/lib/log4j-api-2.4.jar:./SQUARE/target/qa-2.0.jar org.square.qa.analysis.Main --file genNFoldRealData_CLA.txt

    java -Xmx2048m -ea -cp ./SQUARE/target/lib/jblas-1.2.4.jar:./SQUARE/target/lib/log4j-core-2.4.jar:./SQUARE/target/lib/log4j-api-2.4.jar:./SQUARE/target/qa-2.0.jar org.square.qa.analysis.Main --file genUnsupervisedRealData_CLA.txt
    ```

2. **Running the benchmark.**

    — Running the benchmark requires an installation of Matlab. 

    — You will need to populate the 3rd party folder with the algorithms referenced in SQUARE, which include *GLAD*, *CUBAM* and *DS*.

    — Within the folder processingScripts you will find a Matlab function *printAllResults*, this function takes as input the directory
      structure created by SQUARE to run all the other algorithms.

    — Interfaces to each of the algorithms are implemented in the flow* files. You will need to edit the flow* files to make them function
      correctly on your device.

    — Additionally you will also find a java class printMappedFiles. SQUARE maps questions, responses, workers to integers — this class enables
      changing the aggregated file to the original form, i.e., with original questions and response categories.
      Here is an example, run from processingScripts:

    ```
    java printMappedFiles ../nFoldSets/unsupervised/HyunCatherines_Binary/results/nFold/aggregated/Majority_unsupervised_aggregated.txt ../nFoldSets/unsupervised/HyunCatherines_Binary/model/map_question_integer.txt ../nFoldSets/unsupervised/HyunCatherines_Binary/model/map_category_integer.txt ./new_aggregated_file_with_original_questions_response_categories.txt
    ```

3. **Generating Simulated data.**

    — The Matlab script *flowSim.m* under simulation is a starting point to generating simulated data for a given ground truth file. 
      You can tweak worker redundancy and models to apply.
    
    — The script as is learns a model from the included dataset and produces a new file with simulated responses for the same data set 
      with a worker redundancy of 3. See *data/HCBHCB3*.
    
    — Files *genNFoldSimData_CLA.txt* and *genUnsupervisedSimData_CLA.txt* are batch files for SQUARE to process the simulated data and 
      nFoldSetsSim has benchmark results for this simulated dataset. 



**If you use the code please cite**:

@inproceedings{Sheshadri13,
  author = {Aashish Sheshadri and Matthew Lease},
  title = {{SQUARE: A Benchmark for Research on Computing Crowd Consensus}},
  booktitle = {{Proceedings of the 1st AAAI Conference on Human Computation (HCOMP)}},
  year = {2013}
}

@mastersthesis{sheshadri2014collaborative,
  title={A collaborative approach to IR evaluation},
  author={Sheshadri, Aashish},
  year={2014}
}

**If you use the HCB dataset please cite**:

@inproceedings{Buckley10-notebook,
  author = {Chris Buckley and Matthew Lease and Mark D. Smucker},
  title = {{Overview of the TREC 2010 Relevance Feedback Track (Notebook)}},
  booktitle = {{The Nineteenth Text Retrieval Conference (TREC) Notebook}},
  institute = {{National Institute of Standards and Technology (NIST)}},
  year = {2010},
  url = {../papers/trec-notebook-2010.pdf}
}

@inproceedings{Tang11-cir,
  author = {Wei Tang and Matthew Lease},
  title = {Semi-Supervised Consensus Labeling for Crowdsourcing},
  booktitle = {{ACM SIGIR Workshop on Crowdsourcing for Information Retrieval (CIR)}},
  year = {2011},
  pages = {36--41},
  url = {http://www.ischool.utexas.edu/~ml/papers/tang-cir11.pdf},
  confurl = {https://sites.google.com/site/cir2011ws/program}
}


 






