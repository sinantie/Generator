#!/bin/bash

exec=winHelp_gen.sh
kBest=$1
interpolationFactor=$2
inputLists=data/branavan/winHelpHLA/folds
output=results/output/winHelp/generation/generative/no_pos/no_null/model_3_no_null_${kBest}-best_inter${interpolationFactor}_goldLength
#modelPath=results/output/winHelp/alignments/model_3
modelPath=results/output/winHelp/alignments/model_3_NO_NULL

dmvPath=results/output/winHelp/dmv/train/winHelp_uniformZ_initNoise_POS_100
numThreads=2
folds=10

rm -fr $output
mkdir -p $output

for (( f=1; f<=folds; f++ ))
do	
	./${exec} ${inputLists}/winHelpFold${f}PathsEval \
	${output}/fold${f} \
	${modelPath}/fold${f} \
	${dmv_path}/fold${f} \
	winHelpLM/srilm-abs-winHelpRL-split-fold${f}-3-gram.model.arpa \
	${kBest} \
	${interpolationFactor} \
	${numThreads} \
	12
done



