function  [workerProfiles] = computeConfusionMatrices(datasetDir, varargin)
% [workerProfiles] = computeConfusionMatrices(datasetDir)
% Function to compute confusion matrices and save workerProfiles
% Inputs:
%   datasetDir -> Directory with real worker data (unsupervised)
% Outputs:
%   workerProfiles -> object holding confusion matrices for each data set
% eg:
% computeConfusionMatrices('../nFoldSets/unsupervised')
% ******************************************************************************************

datasets = loadDatasets(datasetDir);
workerProfiles = struct();
workerProfileCount = 0;
if(nargin == 2)
    includeList = varargin{1};
else
    includeList = {};
end
for i = 1:length(datasets)
    
    if(~isempty(includeList))
        if(sum(strcmp(includeList,datasets(i).name))==0)
            continue;
        end
    end
    
    workerProfileCount = workerProfileCount + 1;
    allWorkerQuestions = datasets(i).workerQuestions; 
    allWorkerResponses = datasets(i).workerResponses;
    allGoldQuestions = datasets(i).goldQuestions;
    allGoldResponses = datasets(i).goldResponses;
    workerIds = datasets(i).workerIds;
    ids = unique(workerIds);
    
    categories = unique(allGoldResponses);
    if(length(categories) == 2)
        if(sum(allGoldResponses == categories(1)) < 0.5)
            tempGoldResponses = allGoldResponses;
            tempAllWorkerResponses = allWorkerResponses;
            tempGoldResponses(allGoldResponses == categories(1)) = categories(2);
            tempGoldResponses(allGoldResponses == categories(2)) = categories(1);
            allGoldResponses = tempGoldResponses;
            tempAllWorkerResponses(allWorkerResponses == categories(1)) = categories(2);
            tempAllWorkerResponses(allWorkerResponses == categories(2)) = categories(1);
            allWorkerResponses = tempAllWorkerResponses;
        end
    end
    
    [contributionVec, confusionMats, rawWorkerData] = inferWorkerStats(workerIds,allWorkerQuestions,allWorkerResponses,allGoldQuestions,allGoldResponses);
    workerProfiles(workerProfileCount).name = datasets(i).name;
    workerProfiles(workerProfileCount).ids = ids;
    workerProfiles(workerProfileCount).rawWorkerData = rawWorkerData;
    workerProfiles(workerProfileCount).confusionMats = confusionMats;
    workerProfiles(workerProfileCount).contributionVec = contributionVec;
end
