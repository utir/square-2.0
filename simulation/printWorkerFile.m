function printWorkerFile(fileName, data, questions, goldRelevance, redundancy, varargin)
    if(nargin == 6)
        [voxelCellArray, probabilities, vXYZLinMat] = buildVoxelGrid(data,varargin{1},[]);
    else
        [voxelCellArray, probabilities, vXYZLinMat] = buildVoxelGrid(data,[5 5 5],[]);
    end
    
    sampleWorkers = datasample(vXYZLinMat(:),1000,'Replace',true,'Weights',probabilities(:));
    workerId = 1;
    numQuestions = length(questions);
    allQuestions = [];
    for j=1:redundancy
        allQuestions = [allQuestions; randperm(numQuestions)'];
    end

    allQuestions = allQuestions(randperm(length(allQuestions)));
    fid = fopen([fileName '_Responses.txt'],'w');

    while(~isempty(allQuestions))
        if(isempty(sampleWorkers))
            sampleWorkers = datasample(vXYZLinMat(:),1000,'Replace',true,'Weights',probabilities(:));
        end
        linIdx = sampleWorkers(1);
        sampleWorkers(1) = [];
        [x y z] = ind2sub(size(probabilities),linIdx);
        if(isempty(voxelCellArray{x,y,z}))
            continue;
        end
        worker = mvnrnd(voxelCellArray{x,y,z}.mean,voxelCellArray{x,y,z}.covariance,1);
        firstProb = worker(1);
        secondProb = worker(2);
        contribution = worker(3);
        numThisWorkerQuestions = numQuestions*contribution;
        numThisWorkerQuestions = ceil(numThisWorkerQuestions);

        if(numThisWorkerQuestions>length(allQuestions))
            numThisWorkerQuestions = length(allQuestions);
        end
        thisWorkerQuestionsIdx = allQuestions(1:numThisWorkerQuestions);
        allQuestions(1:numThisWorkerQuestions) = [];

        thisWorkerQuestions = questions(thisWorkerQuestionsIdx);
        thisWorkerQuestionsGold = goldRelevance(thisWorkerQuestionsIdx);
        for j=1:numThisWorkerQuestions
            if(thisWorkerQuestionsGold(j) == 1)
                retain = getBernoulliSample(firstProb);
                if(retain==1)
                    thisQuestionResponse = 1;
                else
                    thisQuestionResponse = 2;
                end
            else
                retain = getBernoulliSample(secondProb);
                if(retain==1)
                    thisQuestionResponse = 2;
                else
                    thisQuestionResponse = 1;
                end
            end 
            fprintf(fid,'%u %u %u\n',workerId,thisWorkerQuestions(j),thisQuestionResponse);
        end
        workerId = workerId + 1;
    end
    fclose(fid); 
end