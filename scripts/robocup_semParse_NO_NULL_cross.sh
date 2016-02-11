#!/bin/bash

#Fold 1
./robocup_semParse.sh robocupLists/randomFolds/robocupFold1PathsEval results/output/robocup/randomFolds/semParse_LM_2_noisy_oneEvent_random/fold1 \
results/output/robocup/randomFolds/model_3_percy_NO_NULL_semPar_values_oneEvent_unk_no_generic_newField_random/fold1 20 \
robocupLM/randomFolds/srilm-abs-robocup-semantic-fold1-noisy-random-3-gram.model.arpa

#Fold 2
./robocup_semParse.sh robocupLists/randomFolds/robocupFold2PathsEval results/output/robocup/randomFolds/semParse_LM_2_noisy_oneEvent_random/fold2 \
results/output/robocup/randomFolds/model_3_percy_NO_NULL_semPar_values_oneEvent_unk_no_generic_newField_random/fold2 20 \
robocupLM/randomFolds/srilm-abs-robocup-semantic-fold2-noisy-random-3-gram.model.arpa

#Fold 3
./robocup_semParse.sh robocupLists/randomFolds/robocupFold3PathsEval results/output/robocup/randomFolds/semParse_LM_2_noisy_oneEvent_random/fold3 \
results/output/robocup/randomFolds/model_3_percy_NO_NULL_semPar_values_oneEvent_unk_no_generic_newField_random/fold3 20 \
robocupLM/randomFolds/srilm-abs-robocup-semantic-fold3-noisy-random-3-gram.model.arpa

#Fold 4
./robocup_semParse.sh robocupLists/randomFolds/robocupFold4PathsEval results/output/robocup/randomFolds/semParse_LM_2_noisy_oneEvent_random/fold4 \
results/output/robocup/randomFolds/model_3_percy_NO_NULL_semPar_values_oneEvent_unk_no_generic_newField_random/fold4 20 \
robocupLM/randomFolds/srilm-abs-robocup-semantic-fold4-noisy-random-3-gram.model.arpa
