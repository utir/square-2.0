function aggregatedLabels = flowGLADUnsupervised(unsupervisedData)
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
    
    if(length(unique(unsupervisedData.workerResponses))>2)
            return;
    end
    tic;
    P_Z1 = 0.5;
    [ imageStats, labelerStats ] = em(unsupervisedData.workerQuestions, unsupervisedData.workerIds, unsupervisedData.workerResponses, P_Z1);
    toc;
    estResponses = imageStats{2} >= 0.5;
    estQuestions = imageStats{1};
    aggregatedLabels = [estQuestions (estResponses+1)];
    
    if(unsupervisedData.hasGT)
        categories = unique(unsupervisedData.goldResponses);
        [acc pr re fm categ] = getMetrics(estResponses,estQuestions,unsupervisedData.goldQuestions,unsupervisedData.goldResponses,categories);
        resultVec = [acc' pr' re' fm'];
        name = 'GLAD_unsupervised_results.txt';
        fid = fopen([unsupervisedData.path '/results/nFold/' name],'w');
        fprintf(fid,'%s\n','%Accuracy Precision Recall Fmeasure');
        fprintf(fid,'%s\n',['% ' num2str(categ'+1)]);
        fprintf(fid,'%s',num2str(resultVec));
        fclose(fid);
    end
    
    aggLName = 'GLAD_unsupervised_aggregated.txt';
    
    if(exist([unsupervisedData.path '/results/nFold/aggregated'],'dir'))
        fid = fopen([unsupervisedData.path '/results/nFold/aggregated/' aggLName],'w');
        for i = 1:size(aggregatedLabels,1)
            fprintf(fid,'%s\n',num2str(aggregatedLabels(i,:)));
        end
        fclose(fid);
    end
end