#!/bin/bash

exec=linear_regression_test.sh
#inputFile=gaborLists/trainListPathsGabor
inputFile=data/weatherGov/weatherGovGenEvalGabor.gz
paramsFile=results/output/weatherGov/alignments/model_3_gabor_no_cond_null_bigrams/0.exec/stage1.params.obj
featureType=values
startIndex=4
model=gaborLists/lengthPrediction.values.linear-reg.model
#model=${inputPaths}.lengthPrediction.${featureType}.linear-reg.model

folds=1

# Test
./${exec} ${inputFile} \
	${paramsFile} \
	${model} \
	${featureType} \
	${startIndex}

# Train
#./${exec} ${inputFile} \
#	${inputPaths}.${featureType}.features.csv \
#	${paramsFile} \
#	${model} \
#	${featureType} \
#	${startIndex}

