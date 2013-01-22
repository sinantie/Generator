#!/bin/bash

exec=winHelp_extract_record_statistics.sh
stagedParamsFile=results/output/winHelp/alignments/model_3_docs_no_null_newAnnotation
suffix=NewAnnotationAlignmentsMarkov1
markovOrder=1

#FOLDS
inputPath=data/branavan/winHelpHLA/folds/docs.newAnnotation
execDir=data/branavan/winHelpHLA/folds/treebanks
predInput=results/output/winHelp/alignments/model_3_docs_no_null_newAnnotation
folds=10
mkdir -p $execDir

for (( f=1; f<=folds; f++ ))
do
	./${exec} ${inputPath}/winHelpFold${f}Train ${stagedParamsFile}/fold${f} ${execDir} ${suffix}Fold${f} ${predInput}/fold${f}/stage1.train.pred.1.sorted ${markovOrder}
done

#ALL
#inputPath=data/branavan/winHelpHLA/winHelpRL.cleaned.objType.norm.docs.all.newAnnotation
#execDir=data/branavan/winHelpHLA/
#predInput=results/output/winHelp/alignments/model_3_docs_no_null_newAnnotation

#./${exec} ${inputPath} ${stagedParamsFile}/all ${execDir} ${suffix} ${predInput}/all/stage1.train.pred.1.sorted ${markovOrder}


#./${exec} ${inputPath} ${stagedParamsFile} ${execDir} ${suffix} 
