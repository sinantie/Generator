#!/bin/bash

exec=winHelp_align_model_staged.sh
#inputPath=data/branavan/winHelpHLA/folds
#inputPath=data/branavan/winHelpHLA/winHelpRL.sents.all.tagged

#inputPath=data/branavan/winHelpHLA/winHelpRL.cleaned.objType.norm.docs.all
inputPath=data/branavan/winHelpHLA/folds/docs.cleaned.norm
outputPath=results/output/winHelp/alignments/model_3_docs_no_null_cleaned_objType_norm
stagedParamsFile=results/output/winHelp/alignments/model_3_sents_no_null_cleaned_objType_norm

numIters=2
numThreads=2
folds=10

mkdir -p $outputPath

#Folds
for (( f=1; f<=folds; f++ ))
do
	./${exec} ${inputPath}/winHelpFold${f}Train ${outputPath}/fold${f} ${numIters} ${numThreads} ${stagedParamsFile}/fold${f}
done

#All
#./${exec} ${inputPath} ${outputPath}/all ${numIters} ${numThreads} ${stagedParamsFile}/all
