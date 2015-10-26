function allData = loadDatasets(unsupervisedDir)
% loadDatasets loads data from unsupervised folders
% Inputs: 
%   unsupervisedDir - directory with unsupervised data with gold
% Outputs:
%   allData - a struct with worker questions, responses and gold
% ************************************************************** 
    allData = struct();
    allDir = dir(unsupervisedDir);
    allDataIdx = 1;
    for i=1:length(allDir)
        if(strcmp(allDir(i).name(1),'.'))
            continue;
        end
        filesInDir = dir([unsupervisedDir '/' allDir(i).name]); 
        for j=1:length(filesInDir)
            [path, name, ext] = fileparts(filesInDir(j).name);
            if(~strcmp(ext,'.txt'))
                continue;
            end
            if(strcmp(name,'responses_eval'))
                responses = load([unsupervisedDir '/' allDir(i).name '/' filesInDir(j).name]);
                allData(allDataIdx).workerIds = responses(:,2);
                allData(allDataIdx).workerQuestions = responses(:,1);
                allData(allDataIdx).workerResponses = responses(:,3);
                allData(allDataIdx).numWorkers = length(unique(allData(allDataIdx).workerIds));
                allData(allDataIdx).numQuestions = length(unique(allData(allDataIdx).workerQuestions));
                disp(['Read Inputs from ' allDir(i).name '/' name ext]);
                disp(['Number of workers: ' num2str(allData(allDataIdx).numWorkers)]);
                disp(['Number of questions: ' num2str(allData(allDataIdx).numQuestions)]);
                disp(['Number of responses: ' num2str(length(allData(allDataIdx).workerResponses))]);
            elseif(strcmp(name,'responses_gt'))
                gold = load([unsupervisedDir '/' allDir(i).name '/' filesInDir(j).name]);
                allData(allDataIdx).goldQuestions = gold(:,1);
                allData(allDataIdx).goldResponses = gold(:,2);
                allData(allDataIdx).numGold = length(allData(allDataIdx).goldQuestions);
                disp(['Read gold responses from ' allDir(i).name '/' name ext]);
                disp(['Number of gold responses: ' num2str(allData(allDataIdx).numGold)]);
            end
        end
        allData(allDataIdx).name = allDir(i).name;
        allDataIdx = allDataIdx + 1;
    end
end