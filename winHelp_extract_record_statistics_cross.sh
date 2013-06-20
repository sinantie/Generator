#!/bin/bash

exec=winHelp_extract_record_statistics.sh
stagedParamsFile=results/output/winHelp/alignments/model_3_docs_no_null_newAnnotation
suffix=GoldRst
markovOrder=1
externalTreesInputType=rst
#aligned, gold
type=gold

#FOLDS
inputPath=data/branavan/winHelpHLA/folds/docs.newAnnotation
execDir=data/branavan/winHelpHLA/folds/treebanks
# ALIGN
#predInput=results/output/winHelp/alignments/model_3_docs_no_null_newAnnotation
# GOLD
predInput=data/branavan/winHelpHLA/folds/docs.newAnnotation

folds=10
mkdir -p $execDir

for (( f=1; f<=folds; f++ ))
do
## ALIGN
#	./${exec} ${inputPath}/winHelpFold${f}Train ${stagedParamsFile}/fold${f} ${execDir} ${suffix}Fold${f} ${predInput}/fold${f}/#stage1.train.pred.1.sorted ${markovOrder} ${inputPath}/winHelpFold${f}Train.${type}.edus.tree ${externalTreesInputType}

#GOLD
	./${exec} ${inputPath}/winHelpFold${f}Train ${stagedParamsFile}/fold${f} ${execDir} ${suffix}Fold${f} ${predInput}/winHelpFold${f}Train.${type}.align ${markovOrder} ${inputPath}/winHelpFold${f}Train.${type}.edus.tree ${externalTreesInputType}

done

#ALL
#inputPath=data/branavan/winHelpHLA/winHelpRL.cleaned.objType.norm.docs.all.newAnnotation
#execDir=data/branavan/winHelpHLA/
#predInput=results/output/winHelp/alignments/model_3_docs_no_null_newAnnotation

#ALIGN
#./${exec} ${inputPath} ${stagedParamsFile}/all ${execDir} ${suffix} ${predInput}/all/stage1.train.pred.1.sorted ${markovOrder} ${inputPath}.${type}.edus.tree ${externalTreesInputType}

#GOLD
#./${exec} ${inputPath} ${stagedParamsFile}/all ${execDir} ${suffix} ${inputPath}.${type}.align ${markovOrder} ${inputPath}.${type}.edus.tree ${externalTreesInputType}

#./${exec} ${inputPath} ${stagedParamsFile} ${execDir} ${suffix} 
