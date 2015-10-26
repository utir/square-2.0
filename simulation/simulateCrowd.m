function simulateCrowd(redundancy, gtFile, datasetDir, datasetFilter)    
    
    fid = fopen(gtFile,'r');
    fileData = textscan(fid,'%u %u');
    questions = fileData{1};
    labels = fileData{2};
    fclose(fid);

    
    workerProfiles = computeConfusionMatrices(datasetDir, datasetFilter);
    

    for i = 1:length(workerProfiles)
        confusionMats = workerProfiles(i).confusionMats;
        data = [];
        if(size(confusionMats,1)>2)
            continue;
        end
        contributionVec = workerProfiles(i).contributionVec;
        for j=1:size(confusionMats,3)
            data = [data;[diag(confusionMats(:,:,j))' contributionVec(j)]];
        end
        
        [x,name,y] = fileparts(gtFile);
        newName = ['./' name '_SimModel_' workerProfiles(i).name];
        printWorkerFile(newName, data, questions, labels, redundancy, [10 10 5]);
    end
end