#!/bin/bash

exec=robocup_gen.sh
kBest=25
interpolationFactor=0.5
output=results/output/robocup/generation/dependencies/model_3_${kBest}-best_inter${interpolationFactor}_new4
model_path=results/output/robocup/alignments/model_3_percy_NO_NULL_semPar_values_oneEvent_unk_no_generic_newField_NEW_4
#model_path=results/output/robocup/alignments/model_3_percy_oneEvent_unk_no_generic_newField_POS
dmv_path=results/output/robocup/dmv/train/robocup_uniformZ_initNoise_POS_100
numThreads=2

rm -fr $output
mkdir -p $output

#Fold 1
./${exec} robocupLists/robocupFold1PathsEval \
${output}/fold1 \
${model_path}/fold1 \
${dmv_path}/fold1 \
robocupLM/srilm-abs-robocup-fold1-3-gram.model.arpa \
${kBest} \
${interpolationFactor} \
${numThreads} \
5

#Fold 2
./${exec} robocupLists/robocupFold2PathsEval \
${output}/fold2 \
${model_path}/fold2 \
${dmv_path}/fold2 \
robocupLM/srilm-abs-robocup-fold2-3-gram.model.arpa \
${kBest} \
${interpolationFactor} \
${numThreads} \
5

#Fold 3
./${exec} robocupLists/robocupFold3PathsEval \
${output}/fold3 \
${model_path}/fold3 \
${dmv_path}/fold3 \
robocupLM/srilm-abs-robocup-fold3-3-gram.model.arpa \
${kBest} \
${interpolationFactor} \
${numThreads} \
6

#Fold 4
./${exec} robocupLists/robocupFold4PathsEval \
${output}/fold4 \
${model_path}/fold4 \
${dmv_path}/fold4 \
robocupLM/srilm-abs-robocup-fold4-3-gram.model.arpa \
${kBest} \
${interpolationFactor} \
${numThreads} \
6

