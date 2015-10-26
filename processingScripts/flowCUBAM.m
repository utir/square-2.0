function resultVec = flowCUBAM(unsupervisedData)
% flowCUBAM creates temporary files required by the CUBAM implementation
% and makes a system call to the algorithm
% Inputs:
%   unsupervisedData - struct
% Outputs:
%   resultVec -
% ***********************************************************************
    tic;
    if(length(unique(unsupervisedData.workerResponses))>2)
            return;
    end
    resultVec = []; 
    
    binFile = '../3rdParty/cubam/demo/run1dModel.py';
    tempFile = pwd;
    tempFile = [tempFile '/temp.txt'];
    
    
    
    
    saveMat = [unsupervisedData.workerQuestions unsupervisedData.workerIds unsupervisedData.workerResponses];
    actualQuestions = saveMat(:,1);
    
    if(unsupervisedData.hasGT)
        goldMat = [unsupervisedData.goldQuestions unsupervisedData.goldResponses];
        actualQuestions = saveMat(:,1);
        [saveMat, goldMat] = changeToCUBAM(saveMat,goldMat);
    else
        saveMat = changeToCUBAMWOGT(saveMat);
    end
    numQuestions = length(unique(unsupervisedData.workerQuestions)); 
    numWorkers = length(unique(unsupervisedData.workerIds)); 
    saveMat = [[numQuestions numWorkers length(unsupervisedData.workerResponses)];saveMat];
    fid = fopen(tempFile,'w');
    for j = 1:size(saveMat,1)
        fprintf(fid,'%d %d %d\n',saveMat(j,1),saveMat(j,2),saveMat(j,3));
    end
    fclose(fid);

    currentEvn = getenv('DYLD_LIBRARY_PATH');
    setenv('DYLD_LIBRARY_PATH','');
    system(['python ' binFile ' --evalFile=' tempFile ' --estFile=' pwd '/estLabels.txt']);
    setenv('DYLD_LIBRARY_PATH',currentEvn);
    toc;
    estLabels = load([pwd '/estLabels.txt']);
    estResponses = estLabels(:,2);
    estQuestions = estLabels(:,1);
    
    if(unsupervisedData.hasGT)
        goldQuestions = goldMat(:,1);
        goldResponses = goldMat(:,2);
        categories = unique(unsupervisedData.goldResponses);
        [acc, pr, re, fm, categ] = getMetrics(estResponses,estQuestions,goldQuestions,goldResponses,categories);
        resultVec = [acc' pr' re' fm'];
        name = 'CUBCAM_unsupervised_results.txt';
        fid = fopen([unsupervisedData.path '/results/nFold/' name],'w');
        fprintf(fid,'%s\n','%Accuracy Precision Recall Fmeasure');
        fprintf(fid,'%s\n',['% ' num2str(categ'+1)]);
        fprintf(fid,'%s',num2str(resultVec));
        fclose(fid);
    end
    
    estQuestions = estQuestions + 1;
    questions = unique(actualQuestions,'stable');
    actualQuestions = questions(estQuestions);

    aggregatedLabels = [actualQuestions (estResponses+1)]; 
    aggLName = 'CUBCAM_unsupervised_aggregated.txt';
    
    if(exist([path '/results/nFold/aggregated'],'dir'))
        fid = fopen([unsupervisedData.path '/results/nFold/aggregated/' aggLName],'w');
        for i = 1:size(aggregatedLabels,1)
            fprintf(fid,'%s\n',num2str(aggregatedLabels(i,:)));
        end
        fclose(fid);
    end    
end