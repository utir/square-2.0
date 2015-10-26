function printAllResults(nfoldDir)
% printAllResults calls third-party algorithms on the n-fold data
% inputs:
%   nfoldDir - ./nFoldSets
% ***************************************************************
directories = dir(nfoldDir);

%Process supervised/full-supervison and semi-supervised/light-supervision settings
for j = 1:length(directories)
    if(directories(j).name(1) == '.')
        continue;
    end

    if(strcmp(directories(j).name,'unsupervised'))
        continue;
    else
        allData = loadNFold([nfoldDir '/' directories(j).name]);
    end
    
    for i = 1:length(allData)
        idx = strfind(allData(i).both(1).path,'/');
        
        flowSQUARE(allData(i).both(1).path(1:idx(end)),allData(i).both(1).path(1:idx(end)));
        
        flowGAL(allData(i).eval,allData(i).tune,allData(i).discard,allData(i).both.goldQuestions,allData(i).both.goldResponses,true);
        flowGAL(allData(i).eval,allData(i).tune,allData(i).discard,allData(i).both.goldQuestions,allData(i).both.goldResponses,false);
        
        flowGLAD(allData(i).eval,allData(i).tune,allData(i).discard,allData(i).both.goldQuestions,allData(i).both.goldResponses,true);
        flowGLAD(allData(i).eval,allData(i).tune,allData(i).discard,allData(i).both.goldQuestions,allData(i).both.goldResponses,false);
    end
end

%Process unsupervised setting
allUnsupervisedData = loadUnsupervised([nfoldDir '/unsupervised']);
for i = 1:length(allUnsupervisedData)
    flowSQUAREUnsupervised(allUnsupervisedData(i).path,allUnsupervisedData(i).path);
    
    flowGALUnsupervised(allUnsupervisedData(i));
    
    flowGLADUnsupervised(allUnsupervisedData(i));
    
    flowCUBAM(allUnsupervisedData(i));
end



