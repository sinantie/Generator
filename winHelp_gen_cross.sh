#!/bin/bash

exec=winHelp_gen.sh
kBest=$1
interpolationFactor=$2
inputLists=data/branavan/winHelpHLA/folds
#output=results/output/winHelp/generation/generative/pos/no_null/model_3_no_null_pos_auto_${kBest}-best_inter${interpolationFactor}_goldLength
output=results/output/winHelp/generation/generative/pos/no_null/model_3_no_null_pos_auto_${kBest}-best_inter${interpolationFactor}_goldLength
#modelPath=results/output/winHelp/alignments/model_3
modelPath=results/output/winHelp/alignments/model_3_no_null_pos_auto

dmvPath=results/output/winHelp/dmv/train/winHelp_uniformZ_initNoise_POS_auto_100
numThreads=2
folds=10

rm -fr $output
mkdir -p $output

for (( f=1; f<=folds; f++ ))
do	
	./${exec} ${inputLists}/winHelpFold${f}Eval \
	${output}/fold${f} \
	${modelPath}/fold${f} \
	${dmvPath}/fold${f} \
	winHelpLM/srilm-abs-winHelpRL-split-3-gram.model.arpa \
	${kBest} \
	${interpolationFactor} \
	${numThreads} \
	12
done



