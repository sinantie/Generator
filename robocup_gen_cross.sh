#!/bin/bash

exec=robocup_gen.sh
model_path=model_3_percy_NO_NULL_semPar_values_oneEvent_unk_no_generic_newField_NEW_3
numThreads=2

#Fold 1
./${exec} robocupLists/robocupFold1PathsEval results/output/robocup/generation/all/fold1 \
results/output/robocup/alignments/${model_path}/fold1 ${numThreads} \
robocupLM/srilm-abs-robocup-fold1-3-gram.model.arpa 5

#Fold 2
./${exec} robocupLists/robocupFold2PathsEval results/output/robocup/generation/all/fold2 \
results/output/robocup/alignments/${model_path}/fold2 ${numThreads} \
robocupLM/srilm-abs-robocup-fold2-3-gram.model.arpa 5

#Fold 3
./${exec} robocupLists/robocupFold3PathsEval results/output/robocup/generation/all/fold3 \
results/output/robocup/alignments/${model_path}/fold3 ${numThreads} \
robocupLM/srilm-abs-robocup-fold3-3-gram.model.arpa 6

#Fold 4
./${exec} robocupLists/robocupFold4PathsEval results/output/robocup/generation/all/fold4 \
results/output/robocup/alignments/${model_path}/fold4 ${numThreads} \
robocupLM/srilm-abs-robocup-fold4-3-gram.model.arpa 6

