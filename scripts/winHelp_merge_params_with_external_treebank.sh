#!/bin/bash

exec=merge_params_with_external_treebank.sh
#execDir=results/output/winHelp/alignments/model_3_docs_no_null_newAnnotation_alignments_markov1_externalTreebank
execDir=results/output/winHelp/alignments/model_3_docs_no_null_newAnnotation_gold_rst_parent_externalTreebank
stagedParamsFile=results/output/winHelp/alignments/model_3_docs_no_null_newAnnotation
maxDocLength=100
docLengthBinSize=15
initSmoothing=0.0001

mkdir -p $execDir

# FOLDS
input=data/branavan/winHelpHLA/folds/docs.newAnnotation
externalTreebankPath=data/branavan/winHelpHLA/folds/treebanks
#suffix=NewAnnotationAlignmentsMarkov1Fold
suffix=GoldRstParentFold
folds=10

for (( f=1; f<=folds; f++ ))
do	
	./${exec} ${input}/winHelpFold${f}Train \
	${stagedParamsFile}/fold${f}/stage1.params.obj.gz \
	${externalTreebankPath}/recordTreebankRightBinarize${suffix}${f} \
	${externalTreebankPath}/recordTreebankRulesRightBinarize${suffix}${f} \
	${execDir}/fold${f} \
	${maxDocLength} \
	${docLengthBinSize} \
	${initSmoothing}	
done

# ALL
#input=data/branavan/winHelpHLA/winHelpRL.cleaned.objType.norm.docs.all.newAnnotation
#externalTreebankPath=data/branavan/winHelpHLA
##suffix=NewAnnotationAlignmentsMarkov1
#suffix=GoldRstParent
#./${exec} ${input} \
#${stagedParamsFile}/all/stage1.params.obj.gz \
#${externalTreebankPath}/recordTreebankRightBinarize${suffix} \
#${externalTreebankPath}/recordTreebankRulesRightBinarize${suffix} \
#${execDir}/all \
#${maxDocLength} \
#${docLengthBinSize} \
#${initSmoothing}
