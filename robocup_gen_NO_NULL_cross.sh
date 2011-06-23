#!/bin/bash

model_path=model_3_percy_NO_NULL_semPar_values_oneEvent_unk_no_generic_newField_NEW_3

#Fold 1
./robocup_gen.sh robocupLists/robocupFold1PathsEval results/output/robocup/generation/all/fold1 \
results/output/robocup/alignments/${model_path}/fold1 1 \
robocupLM/srilm-abs-robocup-fold1-3-gram.model.arpa 5 2

#Fold 2
./robocup_gen.sh robocupLists/robocupFold2PathsEval results/output/robocup/generation/all/fold2 \
results/output/robocup/alignments/${model_path}/fold2 1 \
robocupLM/srilm-abs-robocup-fold2-3-gram.model.arpa 5 2

#Fold 3
./robocup_gen.sh robocupLists/robocupFold3PathsEval results/output/robocup/generation/all/fold3 \
results/output/robocup/alignments/${model_path}/fold3 1 \
robocupLM/srilm-abs-robocup-fold3-3-gram.model.arpa 6 2

#Fold 4
./robocup_gen.sh robocupLists/robocupFold4PathsEval results/output/robocup/generation/all/fold4 \
results/output/robocup/alignments/${model_path}/fold4 1 \
robocupLM/srilm-abs-robocup-fold4-3-gram.model.arpa 6 2

