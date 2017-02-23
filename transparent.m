I = imread('OilAndGas-PossibleLogov2.png');
figure, image(I);
%57, 58, 61
R = I(:,:,1);
G = I(:,:,2);
B = I(:,:,3);
alphamatrix = ~(R==57 & G==58 & B==61);
imwrite(I, 'transparentlogo.png', 'Alpha', double(alphamatrix));