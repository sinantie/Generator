#!/bin/bash

exec=winHelp_gen_pcfg.sh
kBest=$1
interpolationFactor=$2
inputLists=data/branavan/winHelpHLA/folds/docs.newAnnotation.removedOutliers
output=results/output/winHelp/generation/generative/no_pos/no_null/pcfg/rst/model_3_docs_newAnnotation_aligned_rst_${kBest}-best_iter${interpolationFactor}_014_gold
#modelPath=results/output/winHelp/alignments/model_3_docs_no_null_newAnnotation_markov1_externalTreebank
modelPath=results/output/winHelp/alignments/model_3_docs_no_null_newAnnotation_alignments_rst_externalTreebank
treebankRules=data/branavan/winHelpHLA/folds/treebanks
#suffix=NewAnnotationMarkov1Fold
suffix=AlignedRstFold
ngramModelPath=winHelpLM/docs.newAnnotation
dmvPath=results/output/winHelp/dmv/train/winHelp_uniformZ_initNoise_POS_auto_100
lengthPredictionModelFolder=data/branavan/winHelpHLA/folds
lengthPredictionFeatureType=counts
folds=10

#rm -fr $output
mkdir -p $output

for (( f=1; f<=folds; f++ ))
do	
	./${exec} ${inputLists}/winHelpFold${f}Eval \
	${output}/fold${f} \
	${modelPath}/fold${f}/stage1.extTreebank.params.obj.gz \
	${dmvPath}/fold${f} \
	${ngramModelPath}/kylm-abs-winHelpRL-docs-fold${f}-3-gram.model.arpa \
	${kBest} \
	${interpolationFactor} \
	${lengthPredictionModelFolder}/winHelpFold${f}Train.lengthPrediction.${lengthPredictionFeatureType}.linear-reg.model \
	${lengthPredictionFeatureType} \
	${treebankRules}/recordTreebankRulesRightBinarize${suffix}${f}
done
