#!/bin/bash

exec=linear_regression.sh
#inputPaths=gaborLists/trainListPathsGabor
inputPaths=gaborLists/genEvalListPathsGabor
paramsFile=results/output/weatherGov/alignments/model_3_gabor_no_cond_null_bigrams/0.exec/stage1.params.obj
featureType=values
startIndex=4

folds=10

./${exec} ${inputPaths} \
	${inputPaths}.${featureType}.features.csv \
	${paramsFile} \
	${inputPaths}.lengthPrediction.${featureType}.linear-reg.model \
	${featureType} \
	${startIndex}

