function [accuracy, precision, recall, fmeasure, categories] = getMetrics(estLabels,estQuestions,gtQuestions,gtLabels,categories)
% getMetrics computes metrics 
% Inputs:
%   estLabels - 
%   estQuestions - 
%   gtQuestions - 
%   gtLabels - 
%   categories -
% Outputs:
%   accuracy - 
%   precision - 
%   recall - 
%   fmeasure -
%   categories -
% ***************************

   categories = sort(categories,1,'ascend');
   accuracy = [];
   precision = [];
   recall = [];
   fmeasure = [];
   
   for i = 1:length(categories)
       thisCategPred = 0;
       thisCategPredCorr = 0;
       thisCategPredWrong = 0;
       restCategPredCorr = 0;
       for j = 1:length(gtQuestions)
           allThisCateg = sum(gtLabels == categories(i));
           allRestCateg = length(gtLabels) - allThisCateg;
           currRelLogical = estQuestions == gtQuestions(j);
           if(estLabels(currRelLogical) == categories(i))
               if(gtLabels(j)==categories(i))
                   thisCategPredCorr = thisCategPredCorr + 1;
               else
                   thisCategPredWrong = thisCategPredWrong + 1;
               end
           else
               if(gtLabels(j)~=categories(i))
                   restCategPredCorr = restCategPredCorr + 1;
               end
           end
       end
       thisRecall = thisCategPredCorr/allThisCateg;
       thisPrecision = thisCategPredCorr/(thisCategPredCorr+thisCategPredWrong);
       if(isnan(thisRecall))
           thisRecall = 1;
       end
       if(isnan(thisPrecision))
           thisPrecision = 1;
       end
       if(isinf(thisRecall))
           thisRecall = 0;
       end
       if(isinf(thisPrecision))
           thisPrecision = 0;
       end
       thisFMeasure = (2.*thisRecall*thisPrecision)/(thisRecall+thisPrecision);
       thisAccuracy = (thisCategPredCorr+restCategPredCorr)/(allThisCateg+allRestCateg);
       if(isnan(thisAccuracy))
           thisAccuracy = 1;
       end
       if(isinf(thisAccuracy))
           thisAccuracy = 0;
       end
       accuracy = [accuracy;thisAccuracy];
       precision = [precision;thisPrecision];
       recall = [recall;thisRecall];
       fmeasure = [fmeasure;thisFMeasure];
   end
end