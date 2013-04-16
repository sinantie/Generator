#!/bin/bash

exec=winHelp_align_model_staged.sh
outputPath=results/output/winHelp/alignments/model_3_docs_no_null_newAnnotation
stagedParamsFile=results/output/winHelp/alignments/pos/model_3_sents_no_null_newAnnotation
numIters=2
numThreads=2

mkdir -p $outputPath

# FOLDS
inputPath=data/branavan/winHelpHLA/folds/docs.newAnnotation
folds=1

for (( f=1; f<=folds; f++ ))
do
	./${exec} ${inputPath}/winHelpFold${f}Train.tagged ${outputPath}/fold${f} ${numIters} ${numThreads} ${stagedParamsFile}/fold${f}
done

# ALL
#inputPath=data/branavan/winHelpHLA/winHelpRL.cleaned.objType.norm.docs.all.newAnnotation
#./${exec} ${inputPath} ${outputPath}/all ${numIters} ${numThreads} ${stagedParamsFile}/all
