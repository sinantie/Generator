#!/bin/bash

exec=linear_regression_train.sh
inputLists=data/branavan/winHelpHLA/folds/docs.newAnnotation.removedOutliers
modelsPath=results/output/winHelp/alignments/model_3_docs_no_null_newAnnotation
featureType=counts
startIndex=2

folds=10

for (( f=1; f<=folds; f++ ))
do	
	./${exec} ${inputLists}/winHelpFold${f}Train \
	${inputLists}/winHelpFold${f}Train.${featureType}.features.csv \
	${modelsPath}/fold${f}/stage1.params.obj.gz \
	${inputLists}/winHelpFold${f}Train.lengthPrediction.${featureType}.linear-reg.model \
	${featureType} \
	${startIndex}
done

