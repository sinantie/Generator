#!/bin/bash

exec=merge_params_with_external_treebank.sh
input=data/branavan/winHelpHLA/folds/docs.cleaned.norm
stagedParamsFile=results/output/winHelp/alignments/model_3_docs_no_null_cleaned_objType_norm
externalTreebankFile=data/branavan/winHelpHLA/folds/treebanks
treebankRules=data/branavan/winHelpHLA/folds/treebanks
suffix=CleanedObjTypeNormMarkov0Fold
execDir=results/output/winHelp/alignments/model_3_docs_no_null_cleaned_objType_norm_markov0_externalTreebank
maxDocLength=150
docLengthBinSize=10
folds=10

mkdir -p $execDir

for (( f=1; f<=folds; f++ ))
do	
	./${exec} ${input}/winHelpFold${f}Train \
	${stagedParamsFile}/fold${f}/stage1.params.obj.gz \
	${externalTreebankFile}/recordTreebankRightBinarize${suffix}${f} \
	${treebankRules}/recordTreebankRulesRightBinarize${suffix}${f} \
	${execDir}/fold${f} \
	${maxDocLength} \
	${docLengthBinSize}
	
done