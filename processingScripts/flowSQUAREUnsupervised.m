function flowSQUAREUnsupervised(loadPath, savePath)
% flowSQUAREUnsupervised(loadPath, savePath)
% Inputs:
%   loadPath -- Path to original data
%   savePath -- Path where data is to be saved
% Example usage:
%   flowSQUAREUnsupervised('./nFoldSets/unsupervised'...
%               ,'./nFoldSets/unsupervised')
% ********************************************************************************************
    targetPath = '../SQUARE';
    targetClass = 'org.square.qa.analysis.Main';
    bin = ['java -Xmx2048m -ea -cp '...
        targetPath '/target/lib/jblas-1.2.4.jar:'...
        targetPath '/target/lib/log4j-core-2.4.jar:'...
        targetPath '/target/lib/log4j-api-2.4.jar:'...
        targetPath '/target/qa-2.0.jar ' targetClass];
        
    parameters = [' --method All --saveDir ' savePath ' --loadDir ' loadPath];
    
    %Set env to use Java 1.8+
    setenv('DYLD_LIBRARY_PATH','/usr/libexec/java_home');
    
    system([bin ' --estimation unsupervised' parameters]);
end