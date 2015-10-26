function unsupervisedData = loadUnsupervised(unsupervisedDir, varargin)
% loadNFold loads data from nFold Evaluation directory
% Inputs: 
%   unsupervisedDir -
%   /Users/aashish/dev/java/crwdQA/crowdData/nFoldSets/nFoldSet_10
% Variable Inputs
%   Dataset specifier-
%   {'data_set_A','data_set_B'}
% Outputs:
%   unsupervisedData -
% ****************************************************

    %Params
    unsupervisedData = struct();
    allDir = dir(unsupervisedDir);
    unsupervisedDataIdx = 1;
    assert(length(varargin)<=1,'Only one additional argument can be processed');
    bSelectDataOnly = false;
    if(length(varargin) == 1)
        bSelectDataOnly = true;
        selectData = varargin{1};
    end
    for i=1:length(allDir)
        
        if(strcmp(allDir(i).name(1),'.'))
            continue;
        end
        if(bSelectDataOnly)
            if(~ismember(allDir(i).name,selectData))
                continue;
            end
        end
        filesInDirName = [unsupervisedDir '/' allDir(i).name]; 
        
        responsesFile = [filesInDirName '/responses_eval.txt']; 
        gtFile = [filesInDirName '/responses_gt.txt']; 
        
        responses = load(responsesFile);
        workerQuestions = responses(:,1);
        workerIds = responses(:,2);
        workerResponses = responses(:,3) - 1;
        unsupervisedData(unsupervisedDataIdx).workerQuestions = workerQuestions;
        unsupervisedData(unsupervisedDataIdx).workerIds = workerIds;
        unsupervisedData(unsupervisedDataIdx).workerResponses = workerResponses;
        numWorkers = length(unique(workerIds));
        numQuestions = length(unique(workerQuestions));
        disp(['Read Inputs from ' responsesFile]);
        disp(['Number of workers: ' num2str(numWorkers)]);
        disp(['Number of questions: ' num2str(numQuestions)]);
        disp(['Number of responses: ' num2str(length(workerResponses))]);
        
        if(exist(gtFile,'file')==2)
            gold = load(gtFile);
            goldQuestions = gold(:,1);
            goldResponses = gold(:,2) - 1;
            numGold = length(goldQuestions);
            disp(['Read gold responses from ' gtFile]);
            disp(['Number of gold responses: ' num2str(numGold)]);
            unsupervisedData(unsupervisedDataIdx).goldQuestions = goldQuestions;
            unsupervisedData(unsupervisedDataIdx).goldResponses = goldResponses;
            unsupervisedData(unsupervisedDataIdx).hasGT = true;
        end
        unsupervisedData(unsupervisedDataIdx).name = allDir(i).name;
        unsupervisedData(unsupervisedDataIdx).path = [unsupervisedDir '/' allDir(i).name];
        unsupervisedDataIdx = unsupervisedDataIdx + 1;
    end
end