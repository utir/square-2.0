function sample = getBernoulliSample(prob,varargin)
% getBernoulliSample draws a bernoulli sample for a given probability
% Inputs:
%   prob -
%   numSamples -
% Outputs:
%   sample -
% ***********************************************

    if(isempty(varargin))
        sample = binornd(1,prob);
    else
        sample = binornd(1,prob,1,varargin{1});
    end
end