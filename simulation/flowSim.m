clear all;
close all;

%set desired redundancy
redundancy = 3;

%file to simulate on
gtToSimulateOn = '../nFoldSets/unsupervised/HyunCatherines_Binary/responses_gt.txt';

%directory with real crowd data
datasetsDirToLearnModels = '../nFoldSets/unsupervised';

%datasets to consider
modelToLearnFromDatasetDir = {'HyunCatherines_Binary'};

%generate responses file for each modeled crowd
simulateCrowd(redundancy, gtToSimulateOn, datasetsDirToLearnModels, modelToLearnFromDatasetDir);


    