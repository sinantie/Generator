#!/bin/bash

exec=linear_regression.sh
inputFolder=robocupLists
paramsFile=results/output/robocup/alignments/model_3_percy_oneEvent_unk_no_generic_newField_POS
featureType=values
startIndex=3

folds=4
for (( f=1; f<=folds; f++ ))
do
	./${exec} ${inputFolder}/robocupFold${f}PathsTrain \
	${inputFolder}/robocupFold${f}PathsTrain.${featureType}.features.csv \
	${paramsFile}/fold${f}/stage1.params.obj.gz \
	${inputFolder}/robocupFold${f}PathsTrain.lengthPrediction.${featureType}.linear-reg.model \
	${featureType} \
	${startIndex}
done
