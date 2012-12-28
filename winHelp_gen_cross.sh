#!/bin/bash

exec=winHelp_gen.sh
kBest=$1
interpolationFactor=$2
inputLists=data/branavan/winHelpHLA/folds/docs.newAnnotation
#output=results/output/winHelp/generation/generative/pos/no_null/model_3_no_null_pos_auto_${kBest}-best_inter${interpolationFactor}_goldLength
#output=results/output/winHelp/generation/generative/pos/no_null/model_3_no_null_pos_auto_${kBest}-best_inter${interpolationFactor}_countPredLength
#output=results/output/winHelp/generation/generative/no_pos/no_null/model_3_docs_${kBest}-best_treebank
output=results/output/winHelp/generation/generative/no_pos/no_null/model_3_docs_newAnnotation_${kBest}-best_iter${interpolationFactor}_gold
#modelPath=results/output/winHelp/alignments/model_3
#modelPath=results/output/winHelp/alignments/model_3_no_null_pos_auto
#modelPath=results/output/winHelp/alignments/model_3_docs_staged_no_null_cleaned_objType_externalTreebank
modelPath=results/output/winHelp/alignments/model_3_docs_no_null_newAnnotation
suffix=NewAnnotationFold
ngramModelPath=winHelpLM/docs.newAnnotation
dmvPath=results/output/winHelp/dmv/train/winHelp_uniformZ_initNoise_POS_auto_100
lengthPredictionModelFolder=data/branavan/winHelpHLA/folds
lengthPredictionFeatureType=counts
numThreads=2
folds=10

#rm -fr $output
mkdir -p $output

for (( f=1; f<=folds; f++ ))
do	
	./${exec} ${inputLists}/winHelpFold${f}Eval \
	${output}/fold${f} \
	${modelPath}/fold${f}/stage1.params.obj.gz \
	${dmvPath}/fold${f} \
	${ngramModelPath}/kylm-abs-winHelpRL-docs-fold${f}-3-gram.model.arpa \
	${kBest} \
	${interpolationFactor} \
	${lengthPredictionModelFolder}/winHelpFold${f}Train.lengthPrediction.${lengthPredictionFeatureType}.linear-reg.model \
	${lengthPredictionFeatureType}
done



