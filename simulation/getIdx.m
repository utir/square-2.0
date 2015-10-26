function idx = getIdx(vals,nBins)
    steps = linspace(min(vals),max(vals),nBins);
    steps = steps - (steps(2) - steps(1))/2;
    repSteps = repmat(steps,length(vals),1);
    subMat = repmat(vals,1,nBins) - repSteps;
    subMat(subMat<0) = inf;
    [minMat idx] = min(subMat,[],2);
end