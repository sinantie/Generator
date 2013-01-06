#!/bin/bash

exec=merge_params_with_external_treebank.sh
execDir=results/output/winHelp/alignments/model_3_docs_no_null_newAnnotation_markov1_externalTreebank
stagedParamsFile=results/output/winHelp/alignments/model_3_docs_no_null_newAnnotation
maxDocLength=100
docLengthBinSize=15
initSmoothing=0.0001
# FOLDS

#input=data/branavan/winHelpHLA/folds/docs.newAnnotation
#externalTreebankFile=data/branavan/winHelpHLA/folds/treebanks
#treebankRules=data/branavan/winHelpHLA/folds/treebanks
#suffix=NewAnnotationFold
#folds=10

mkdir -p $execDir

#for (( f=1; f<=folds; f++ ))
#do	
#	./${exec} ${input}/winHelpFold${f}Train \
#	${stagedParamsFile}/fold${f}/stage1.params.obj.gz \
#	${externalTreebankFile}/recordTreebankRightBinarize${suffix}${f} \
#	${treebankRules}/recordTreebankRulesRightBinarize${suffix}${f} \
#	${execDir}/fold${f} \
#	${maxDocLength} \
#	${docLengthBinSize} \
#	${initSmoothing}
#	
#done

# ALL

input=data/branavan/winHelpHLA/winHelpRL.cleaned.objType.norm.docs.all.newAnnotation
externalTreebankFile=data/branavan/winHelpHLA/
treebankRules=data/branavan/winHelpHLA/
suffix=NewAnnotationMarkov1
./${exec} ${input} \
${stagedParamsFile}/all/stage1.params.obj.gz \
${externalTreebankFile}/recordTreebankRightBinarize${suffix} \
${treebankRules}/recordTreebankRulesRightBinarize${suffix} \
${execDir}/all \
${maxDocLength} \
${docLengthBinSize} \
${initSmoothing}

