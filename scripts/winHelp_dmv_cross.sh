#!/bin/bash

exec=winHelp_dmvTrain.sh
inputLists=data/branavan/winHelpHLA/folds/sents.newAnnotation
#inputLists=data/branavan/winHelpHLA/winHelpRL.sents.all.tagged
outputPath=results/output/winHelp/dmv/train/winHelp_uniformZ_initNoise_POS_auto_100
numIters=100
numThreads=2
folds=10

mkdir -p $outputPath

#Folds
for (( f=1; f<=folds; f++ ))
do	
	./${exec} ${inputLists}/winHelpFold${f}Train.tagged \
	${outputPath}/fold${f} ${numIters} ${numThreads}	
done

#All
#./${exec} ${inputLists} ${outputPath}/all ${numIters} ${numThreads}
