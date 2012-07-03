#!/bin/bash

exec=winHelp_align_model.sh
inputPath=data/branavan/winHelpHLA/folds
outputPath=results/output/winHelp/alignments/model_3
numIters=15
numThreads=2
folds=10

mkdir -p $outputPath

for (( f=1; f<=folds; f++ ))
do
	./${exec} ${inputPath}/winHelpFold${f}PathsTrain ${outputPath}/fold${f} ${numIters} ${numThreads}
done

#All
#./${exec} robocupLists/robocupAllPathsTrain \
#results/output/robocup/model_3_percy_NO_NULL_semPar_values_unk_no_generic_newField/all 10 2
