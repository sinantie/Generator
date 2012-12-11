#!/bin/bash


#predInput=results/output/weatherGov/alignments/model_3_gabor_cond_null_bigrams_correct/1.exec/stage1.train.pred.14.sorted

#!/bin/bash

exec=winHelp_extract_record_statistics.sh
#inputPath=../branavan/winHelpHLA/winHelpRL.cleaned.objType.docs.all
inputPath=data/branavan/winHelpHLA/folds/docs.cleaned
execDir=data/branavan/winHelpHLA/folds/treebanks
stagedParamsFile=results/output/winHelp/alignments/model_3_docs_staged_no_null_cleaned_objType
prefix=winHelp
suffix=CleanedObjTypeFold

folds=10

mkdir -p $execDir

#Folds
for (( f=1; f<=folds; f++ ))
do
	./${exec} ${inputPath}/winHelpFold${f}Train ${stagedParamsFile}/fold${f} ${execDir} ${suffix}${f}
done

#All
#./${exec} ${inputPath} ${stagedParamsFile}/all ${execDir} ${suffix}
