#!/bin/bash

exec=robocup_dmvTrain.sh
output_path=results/output/robocup/dmv/train/robocup_uniformZ_initNoise_POS_100
numIters=100
numThreads=2

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
#results/output/robocup/dmv/train/all ${numIters} ${numThreads}
