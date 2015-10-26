function aggregatedLabelsDS = flowGALUnsupervised(unsupervisedData)
% flowGAL creates temporary files required by the GetAnotherLabel implementation
% and makes a system call to the algorithm
% Inputs:
%   nFoldDataEval -
%   nFoldDataTune -
%   hasSupervision -
%   hasSemiSupervision -
% Outputs:
%   resultVec -
% ******************************************************************************

    tic;
    
    %update executable as necessary
    bin = 'java -ea -cp ../3rdParty/Get-Another-Label/target/dependency/args4j-2.0.16.jar:../3rdParty/Get-Another-Label/target/dependency/commons-beanutils-1.8.3.jar:../3rdParty/Get-Another-Label/target/dependency/commons-collections-3.2.1.jar:../3rdParty/Get-Another-Label/target/dependency/commons-lang3-3.1.jar:../3rdParty/Get-Another-Label/target/dependency/commons-logging-1.1.1.jar:../3rdParty/Get-Another-Label/target/dependency/commons-math3-3.0.jar:../3rdParty/Get-Another-Label/target/dependency/hamcrest-core-1.1.jar:../3rdParty/Get-Another-Label/target/dependency/junit-4.10.jar:../3rdParty/Get-Another-Label/target/dependency/opencsv-2.3.jar:../3rdParty/Get-Another-Label/target/dependency/slf4j-api-1.6.6.jar:../3rdParty/Get-Another-Label/target/dependency/junit-4.10.jar:../3rdParty/Get-Another-Label/target/get-another-label-2.2.0-SNAPSHOT.jar com.ipeirotis.gal.Main';
    
    filePathPrefix = pwd;
    resultFile = [filePathPrefix '/results/object-probabilities.txt'];
    
    
    
    writeTempFiles(unsupervisedData.workerIds,unsupervisedData.workerQuestions,unsupervisedData.workerResponses, filePathPrefix);
    system([bin ' --cost ' filePathPrefix '/cost.txt ' '--input ' filePathPrefix '/responses.txt ' '--categories ' filePathPrefix '/categories.txt']);
    toc;
    fid = fopen(resultFile);
    inputDataStruct = textscan(fid,'%s%s%s%s%s%s%*[^\n]');
    fclose(fid);

    estQuestions = inputDataStruct{1};
    estResponsesDS = inputDataStruct{3};


    estQuestions(1) = [];
    estResponsesDS(1) = [];


    estQuestions = cellfun(@str2num,estQuestions);
    estResponsesDS = cellfun(@str2num,estResponsesDS);
        
    aggregatedLabelsDS = [estQuestions (estResponsesDS + 1)]; 
    
    if(unsupervisedData.hasGT)
        categories = unique(unsupervisedData.goldResponses);
        [acc pr re fm categ] = getMetrics(estResponsesDS,estQuestions,unsupervisedData.goldQuestions,unsupervisedData.goldResponses,categories);
        resultVec = [acc' pr' re' fm'];
        name = 'DS_unsupervised_results.txt';
        fid = fopen([unsupervisedData.path '/results/nFold/' name],'w');
        fprintf(fid,'%s\n','%Accuracy Precision Recall Fmeasure');
        fprintf(fid,'%s\n',['% ' num2str(categ'+1)]);
        fprintf(fid,'%s',num2str(resultVec));
        fclose(fid);
    end
        
    aggLNameDS = 'DS_unsupervised_aggregated.txt';
    
%     write to file
    if(exist([unsupervisedData.path '/results/nFold/aggregated'],'dir'))
        fidDS = fopen([unsupervisedData.path '/results/nFold/aggregated/' aggLNameDS],'w');
        for i = 1:size(aggregatedLabelsDS,1)
            fprintf(fidDS,'%s\n',num2str(aggregatedLabelsDS(i,:)));
        end
        fclose(fidDS);
    end
end

function writeTempFiles(evalW,evalQ,evalR,filePathPrefix)
    fid = fopen([filePathPrefix '/responses.txt'],'w');
    for i=1:length(evalW)
        fprintf(fid,'%s\t%s\t%s\n',num2str(evalW(i)),num2str(evalQ(i)),num2str(evalR(i)));
    end
    fclose(fid);
    
    categories = unique(evalR);
    fid = fopen([filePathPrefix '/categories.txt'],'w');
    for i=1:length(categories)
        fprintf(fid,'%s\n',num2str(categories(i)));
    end
    fclose(fid);
    
    fid = fopen([filePathPrefix '/cost.txt'],'w');
    costMat = perms(categories);
    costMat = costMat(:,1:2);
    costMat = unique(costMat,'rows');
    costs = ones(size(costMat,1),1);
    costMat = [costMat costs];
    addOn = [categories categories];
    addOnCost = zeros(length(categories),1);
    addOn = [addOn addOnCost];
    costMat = [costMat;addOn];
    for i=1:size(costMat,1)
        fprintf(fid,'%s\t%s\t%s\n',num2str(costMat(i,1)),num2str(costMat(i,2)),num2str(costMat(i,3)));
    end
    fclose(fid);
end