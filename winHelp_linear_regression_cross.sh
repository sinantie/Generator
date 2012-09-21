#!/bin/bash

exec=linear_regression.sh
inputFolder=data/branavan/winHelpHLA/folds
paramsFolder=results/output/winHelp/alignments/model_3_no_null_pos_auto
featureType=counts
startIndex=2

folds=10

for (( f=1; f<=folds; f++ ))
do	
	./${exec} ${inputFolder}/winHelpFold${f}Train \
	${inputFolder}/winHelpFold${f}Train.${featureType}.features.csv \
	${paramsFolder}/fold${f}/stage1.params.obj.gz \
	${inputFolder}/winHelpFold${f}Train.lengthPrediction.${featureType}.linear-reg.model \
	${featureType} \
	${startIndex}
done

