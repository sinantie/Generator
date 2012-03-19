#!/bin/bash

exec=robocup_align_model.sh
output_path=model_3_percy_NO_NULL_oneEvent_unk_no_generic_newField_POS
numIters=10
numThreads=2

#Fold 1
./${exec} robocupLists/robocupFold1PathsTrain \
results/output/robocup/alignments/${output_path}/fold1 ${numIters} ${numThreads}

#Fold 2
./${exec} robocupLists/robocupFold2PathsTrain \
 results/output/robocup/alignments/${output_path}/fold2 ${numIters} ${numThreads}
#Fold 3
./${exec} robocupLists/robocupFold3PathsTrain \
 results/output/robocup/alignments/${output_path}/fold3 ${numIters} ${numThreads}
#Fold 4
./${exec} robocupLists/robocupFold4PathsTrain \
 results/output/robocup/alignments/${output_path}/fold4 ${numIters} ${numThreads}

#All
#./${exec} robocupLists/robocupAllPathsTrain \
#results/output/robocup/model_3_percy_NO_NULL_semPar_values_unk_no_generic_newField/all 10 2
