#!/bin/bash

exec=winHelp_align_model.sh
numIters=15
numThreads=2
outputPath=results/output/winHelp/alignments/pos/model_3_sents_no_null_newAnnotation
mkdir -p $outputPath

# FOLDS
inputPath=data/branavan/winHelpHLA/folds/sents.newAnnotation
folds=1

for (( f=1; f<=folds; f++ ))
do
	./${exec} ${inputPath}/winHelpFold${f}Train.tagged ${outputPath}/fold${f} ${numIters} ${numThreads}
done

# ALL
#inputPath=data/branavan/winHelpHLA/winHelpRL.cleaned.objType.norm.sents.all.newAnnotation
#./${exec} ${inputPath} ${outputPath}/all ${numIters} ${numThreads}
