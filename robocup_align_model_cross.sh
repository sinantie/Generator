#!/bin/bash

exec=robocup_align_model.sh
output_path=results/output/robocup/alignments/model_3_percy_oneEvent_unk_no_generic_newField_POS
numIters=10
numThreads=2

mkdir -p $output_path

#Fold 1
./${exec} robocupLists/robocupFold1PathsTrain \
${output_path}/fold1 ${numIters} ${numThreads}

#Fold 2
./${exec} robocupLists/robocupFold2PathsTrain \
${output_path}/fold2 ${numIters} ${numThreads}
#Fold 3
./${exec} robocupLists/robocupFold3PathsTrain \
${output_path}/fold3 ${numIters} ${numThreads}
#Fold 4
./${exec} robocupLists/robocupFold4PathsTrain \
${output_path}/fold4 ${numIters} ${numThreads}

#All
#./${exec} robocupLists/robocupAllPathsTrain \
#results/output/robocup/model_3_percy_NO_NULL_semPar_values_unk_no_generic_newField/all 10 2
