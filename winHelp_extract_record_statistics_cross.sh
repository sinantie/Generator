#!/bin/bash


#predInput=results/output/weatherGov/alignments/model_3_gabor_cond_null_bigrams_correct/1.exec/stage1.train.pred.14.sorted

#!/bin/bash

exec=winHelp_extract_record_statistics.sh
inputPath=data/branavan/winHelpHLA/winHelpRL.cleaned.objType.norm.docs.all
#inputPath=data/branavan/winHelpHLA/folds/docs.cleaned
execDir=data/branavan/winHelpHLA/
#execDir=data/branavan/winHelpHLA/folds/treebanks
stagedParamsFile=results/output/winHelp/alignments/model_3_docs_no_null_cleaned_objType_norm
suffix=CleanedObjTypeNormMarkov0

folds=10

mkdir -p $execDir

#for (( f=1; f<=folds; f++ ))
#do
#	./${exec} ${inputPath}/winHelpFold${f}Train ${stagedParamsFile}/fold${f} ${execDir} ${suffix}Fold${f}
#done

#All
./${exec} ${inputPath} ${stagedParamsFile}/all ${execDir} ${suffix}
