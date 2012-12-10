#!/bin/bash

exec=winHelp_align_model.sh
#inputPath=data/branavan/winHelpHLA/folds
#inputPath=data/branavan/winHelpHLA/winHelpRL.sents.all.tagged
inputPath=data/branavan/winHelpHLA/winHelpRL.cleaned.objType.sents.all
outputPath=results/output/winHelp/alignments/model_3_sents_no_null_cleaned_objType
numIters=15
numThreads=2
folds=10

mkdir -p $outputPath

#Folds
#for (( f=1; f<=folds; f++ ))
#do
#	./${exec} ${inputPath}/winHelpFold${f}Train.tagged ${outputPath}/fold${f} ${numIters} ${numThreads}
#done

#All
./${exec} ${inputPath} ${outputPath}/all ${numIters} ${numThreads}
