#!/bin/bash

exec=linear_regression.sh
inputPaths=robocupLists/robocupAllPathsTrain
paramsFile=results/output/robocup/alignments/model_3_percy_oneEvent_unk_no_generic_newField_POS/fold1/stage1.params.obj.gz
featureType=counts
startIndex=3


./${exec} ${inputPaths} \
	${inputPaths}.${featureType}.features.csv \
	${paramsFile} \
	${inputPaths}.lengthPrediction.${featureType}.linear-reg.model \
	${featureType} \
	${startIndex}

