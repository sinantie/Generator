#!/bin/bash

#Fold 1
./robocup_semParse.sh robocupLists/robocupFold1PathsEval results/output/robocup/semParse_LM_2_noisy/fold1 \
results/output/robocup/model_3_percy_NO_NULL_semPar_values_unk_no_generic_newField/fold1 15 \
robocupLM/srilm-abs-robocup-semantic-fold1-noisy-3-gram.model.arpa

#Fold 2
./robocup_semParse.sh robocupLists/robocupFold2PathsEval results/output/robocup/semParse_LM_2_noisy/fold2 \
results/output/robocup/model_3_percy_NO_NULL_semPar_values_unk_no_generic_newField/fold2 15 \
robocupLM/srilm-abs-robocup-semantic-fold2-noisy-3-gram.model.arpa

#Fold 3
./robocup_semParse.sh robocupLists/robocupFold3PathsEval results/output/robocup/semParse_LM_2_noisy/fold3 \
results/output/robocup/model_3_percy_NO_NULL_semPar_values_unk_no_generic_newField/fold3 15 \
robocupLM/srilm-abs-robocup-semantic-fold3-noisy-3-gram.model.arpa

#Fold 4
./robocup_semParse.sh robocupLists/robocupFold4PathsEval results/output/robocup/semParse_LM_2_noisy/fold4 \
results/output/robocup/model_3_percy_NO_NULL_semPar_values_unk_no_generic_newField/fold4 15 \
robocupLM/srilm-abs-robocup-semantic-fold4-noisy-3-gram.model.arpa
