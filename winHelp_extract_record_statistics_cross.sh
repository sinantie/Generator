#!/bin/bash


#predInput=results/output/weatherGov/alignments/model_3_gabor_cond_null_bigrams_correct/1.exec/stage1.train.pred.14.sorted

#!/bin/bash

exec=winHelp_extract_record_statistics.sh
stagedParamsFile=results/output/winHelp/alignments/model_3_docs_no_null_newAnnotation
suffix=NewAnnotationMarkov1

#Folds
#inputPath=data/branavan/winHelpHLA/folds/docs.newAnnotation
#execDir=data/branavan/winHelpHLA/folds/treebanks
#folds=10
#mkdir -p $execDir

#for (( f=1; f<=folds; f++ ))
#do
#	./${exec} ${inputPath}/winHelpFold${f}Train ${stagedParamsFile}/fold${f} ${execDir} ${suffix}Fold${f}
#done

#All
inputPath=data/branavan/winHelpHLA/winHelpRL.cleaned.objType.norm.docs.all.newAnnotation
execDir=data/branavan/winHelpHLA/

./${exec} ${inputPath} ${stagedParamsFile}/all ${execDir} ${suffix}

#./${exec} ${inputPath} ${stagedParamsFile} ${execDir} ${suffix}
