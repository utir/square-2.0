function resultVec = flowGLAD(nFoldDataEval,nFoldDataTune, nFoldDiscard, nFoldGoldQuestions, nFoldGoldResponses, hasSupervision)
% flowGLAD creates temporary files required by the GLAD implementation
% and makes a call to the algorithm
% Inputs:
%   nFoldDataEval -
%   nFoldDataTune -
%   hasSupervision -
%   hasSemiSupervision -
% Outputs:
%   resultVec -
% ***********************************************************************
    resultVec = [];
    aggregatedLabels = [];
    for i = 1:length(nFoldDataEval)
        tic;
        if(length(unique(nFoldGoldResponses))>2)
            return;
        end
        if(hasSupervision)
            P_Z1 = sum(nFoldDataTune(i).goldResponses)/length(nFoldDataTune(i).goldResponses);
            allQuestions = [unique(nFoldDataEval(i).workerQuestions);unique(nFoldDataTune(i).workerQuestions)];
            allQuestions = sort(allQuestions,1,'ascend');
            P_Clamped = ones(length(allQuestions),1).*P_Z1;
            for j=1:length(allQuestions)
                idx = nFoldDataTune(i).goldQuestions == allQuestions(j);
                if(sum(idx) == 0)
                    continue;
                end
                if(nFoldDataTune(i).goldResponses(idx) == 1)
                    P_Clamped(j) = 0.999;
                else
                    P_Clamped(j) = 0.001;
                end
            end
            [ imageStats, labelerStats ] = em([nFoldDataEval(i).workerQuestions;nFoldDataTune(i).workerQuestions],...
                                            [nFoldDataEval(i).workerIds;nFoldDataTune(i).workerIds],...
                                            [nFoldDataEval(i).workerResponses;nFoldDataTune(i).workerResponses],...
                                            P_Clamped, ones(length(unique([nFoldDataEval(i).workerIds;nFoldDataTune(i).workerIds])), 1),...
                                            ones(nFoldDataEval(i).numQuestions+nFoldDataTune(i).numQuestions, 1));
        else
            P_Z1 = sum(nFoldDataTune(i).goldResponses)/length(nFoldDataTune(i).goldResponses);
            [ imageStats, labelerStats ] = em(nFoldDataEval(i).workerQuestions, nFoldDataEval(i).workerIds, nFoldDataEval(i).workerResponses, P_Z1, ones(nFoldDataEval(i).numWorkers, 1), ones(nFoldDataEval(i).numQuestions, 1));
        end
        toc;
        estResponses = imageStats{2} >= 0.5;
        estQuestions = imageStats{1};
        logicalEst = ismember(estQuestions,nFoldDiscard(i).discardQuestions);
        estQuestions(logicalEst) = [];
        estResponses(logicalEst) = [];
        estResponses = estResponses + 1;
        
        aggregatedLabels = [aggregatedLabels;[estQuestions estResponses]];
    end
    categories = unique(nFoldGoldResponses);
    [acc, pr, re, fm, categ] = getMetrics(aggregatedLabels(:,2)-1,aggregatedLabels(:,1),nFoldGoldQuestions,nFoldGoldResponses,categories);
    resultVec = [acc' pr' re' fm'];
    
%     write to file
    [path name ext] = fileparts(nFoldDataEval(1).path);
    if(hasSupervision)
        name = 'GLAD_supervised_results.txt';
        aggLName = 'GLAD_supervised_aggregated.txt';
    else
        name = 'GLAD_semisupervised_results.txt';
        aggLName = 'GLAD_semisupervised_aggregated.txt';
    end
    
    fid = fopen([path '/results/nFold/' name],'w');
    fprintf(fid,'%s\n','%Accuracy Precision Recall Fmeasure');
    fprintf(fid,'%s\n',['% ' num2str(categ'+1)]);
    fprintf(fid,'%s',num2str(resultVec));
    fclose(fid);
    
    aggregatedGold = [nFoldGoldQuestions nFoldGoldResponses];
    if(exist([path '/results/nFold/aggregated'],'dir'))
        fid = fopen([path '/results/nFold/aggregated/' aggLName],'w');
        fidGold = fopen([path '/results/nFold/aggregated/gold.txt'],'w');
        for i = 1:size(aggregatedLabels,1)
            fprintf(fid,'%s\n',num2str(aggregatedLabels(i,:)));
        end
        for i = 1:size(aggregatedGold,1)
            fprintf(fidGold,'%s\n',num2str(aggregatedGold(i,:)));
        end
        fclose(fid);
        fclose(fidGold);
    end
end