function [contributionVec confusionMats rawWorkerData] = inferWorkerStats(workerIds,allWorkerQuestions,allWorkerResponses,allGoldQuestions,allGoldResponses)
% inferWorkerStats infers worker statistics from real data
% Inputs:
%   workerIds -
%   allWorkerQuestions -
%   allWorkerResponses -
%   allGoldQuestions -
%   allGoldResponses -
% Outputs:
%   contributionVec -
%   confusionMats -
%   rawWorkerData -
% **************************************
    ids = unique(workerIds);
    categories = unique(allGoldResponses);
    confusionMats = [];
    rawWorkerData = struct();
    contributionVec = [];
    for i = 1:length(ids)
        workerLogical = logical(workerIds == ids(i));
        answeredQuestions = allWorkerQuestions(workerLogical);
        rawWorkerData(i).answeredQuestions = answeredQuestions;
        rawWorkerData(i).answeredLogical = workerLogical;
        answeredResponses = allWorkerResponses(workerLogical);
        uniqueAnswered = unique(answeredQuestions);
        percentContrib = length(uniqueAnswered)/length(allGoldQuestions);
        rawWorkerData(i).percentContribution = percentContrib;
        contributionVec = [contributionVec; percentContrib];
        currentConfusion = ones(length(categories),length(categories)); %May have to use priors? dont know -- some smoothing maybe
        
        for j=1:length(uniqueAnswered)
            if(ismember(uniqueAnswered(j),allGoldQuestions))
                actualResponseLogical = logical(allGoldQuestions == uniqueAnswered(j));
                actualResponse = allGoldResponses(actualResponseLogical);
                relAnsweredResponseLogical = logical(answeredQuestions == uniqueAnswered(j));
                relAnsweredResponses = answeredResponses(relAnsweredResponseLogical);
                for k = 1:length(relAnsweredResponses)
                    if(relAnsweredResponses(k) == actualResponse)
                        currentConfusion(actualResponse,actualResponse) = currentConfusion(actualResponse,actualResponse)+1;
                    end
                    currentConfusion(actualResponse,relAnsweredResponses(k)) = currentConfusion(actualResponse,relAnsweredResponses(k))+1;
                end
            end
        end
        rawWorkerData(i).confusionMat = currentConfusion;
        confusionMats = cat(3,confusionMats,currentConfusion./repmat(sum(currentConfusion,2),1,size(currentConfusion,2)));
    end
%     meanConfusion = mean(confusionMats,3);
%     zmeanConfusion = confusionMats - repmat(meanConfusion,[1,1,size(confusionMats,3)]);
%     confusionSquaredSum = sum(zmeanConfusion.*zmeanConfusion,3);
%     varianceConfusion = confusionSquaredSum./(size(zmeanConfusion,3));
%     varianceConfusion = sqrt(varianceConfusion);
end