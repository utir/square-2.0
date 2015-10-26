function [voxelCellArray, probabilities, vXYZLinMat] = buildVoxelGrid(data, bins, range)
%myHist4(data)
%Inputs:
% data -> is a mx3 matrix of co-ordinates
%Optional Inputs:
% bins -> row vector with number of voxels in each dimension (default [10 10 10])
%Outputs:
% centers -> Voxel centers
% weights -> Voxel probability
% variance -> Voxel variance
    assert(size(data,2) == 3, 'Data exceeds dimension');

    if(~isempty(bins))
        assert(size(bins,1)==1);
        if(length(bins) == 1)
            bins = repmat(bins,1,3);
        else
            assert(size(bins,2)==3);
        end
    else
        bins = [10 10 10];
    end

    xVals = data(:,1);
    yVals = data(:,2);
    zVals = data(:,3);

    if(~isempty(range))
        assert(size(range,1)==3 & size(range,2)==2);
        [xMin,xMax] = range(:,1);
        [yMin,yMax] = range(:,2);
        [zMin,zMax] = range(:,3);
    else
        xMin = min(xVals);
        xMax = max(xVals);
        yMin = min(yVals);
        yMax = max(yVals);
        zMin = min(zVals);
        zMax = max(zVals);    
    end
    xOutOfBoundL = xVals>xMax & xVals < xMin;
    xVals(xOutOfBoundL) = []; yVals(xOutOfBoundL) = []; zVals(xOutOfBoundL) = [];
    
    yOutOfBoundL = yVals>yMax & yVals < yMin;
    xVals(yOutOfBoundL) = []; yVals(yOutOfBoundL) = []; zVals(yOutOfBoundL) = [];
    
    zOutOfBoundL = zVals>zMax & zVals < zMin;
    xVals(zOutOfBoundL) = []; yVals(zOutOfBoundL) = []; zVals(zOutOfBoundL) = [];
    
    probabilities = (0.01).*ones(bins(1),bins(2),bins(3));
     
    xVIdx = getIdx(xVals,bins(1));
    yVIdx = getIdx(yVals,bins(2));
    zVIdx = getIdx(zVals,bins(3));
    
    
    voxelCellArray = cell(bins(1),bins(2),bins(3));
    
    xyzVIdx = [xVIdx,yVIdx,zVIdx];
    
    uniqueXYZVIdx = unique(xyzVIdx,'rows');
    
    for i=1:size(uniqueXYZVIdx,1)
        xyz = uniqueXYZVIdx(i,:);
        xL = xVIdx == xyz(1);
        yL = yVIdx == xyz(2);
        zL = zVIdx == xyz(3);
        relIdx = ((xL & yL) & zL);
        relXYZVals = [xVals(relIdx) yVals(relIdx) zVals(relIdx)];
        relXYZMean = mean(relXYZVals,1);
        relXYZZmeanVals = relXYZVals - repmat(relXYZMean,size(relXYZVals,1),1);
        relXYZVariance = (relXYZZmeanVals' * relXYZZmeanVals)./size(relXYZZmeanVals,1); 
        probabilities(xyz(1),xyz(2),xyz(3)) = probabilities(xyz(1),xyz(2),xyz(3)) + size(relXYZVals,1);
        voxel.points = relXYZVals;
        voxel.mean = relXYZMean;
        voxel.covariance = relXYZVariance;
        voxelCellArray(xyz(1),xyz(2),xyz(3)) = {voxel};
    end
    probabilities = probabilities./sum(probabilities(:));
    vXYZLinMat = 1:bins(1)*bins(2)*bins(3);
    vXYZLinMat = reshape(vXYZLinMat,bins(1),bins(2),bins(3));
end