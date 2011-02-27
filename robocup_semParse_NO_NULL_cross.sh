#!/bin/bash

#Fold 1
./robocup_semParse.sh robocupLists/robocupFold1PathsEval results/output/robocup/semParse/fold1 \
results/output/robocup/model_3_percy_NO_NULL_semPar_values/fold1 5 \
robocupLM/srilm-abs-robocup-fold1-3-gram.model.arpa

#Fold 2
./robocup_semParse.sh robocupLists/robocupFold2PathsEval results/output/robocup/semParse/fold2 \
results/output/robocup/model_3_percy_NO_NULL_semPar_values/fold2 5 \
robocupLM/srilm-abs-robocup-fold2-3-gram.model.arpa

#Fold 3
./robocup_semParse.sh robocupLists/robocupFold3PathsEval results/output/robocup/semParse/fold3 \
results/output/robocup/model_3_percy_NO_NULL_semPar_values/fold3 5 \
robocupLM/srilm-abs-robocup-fold3-3-gram.model.arpa

#Fold 4
./robocup_semParse.sh robocupLists/robocupFold4PathsEval results/output/robocup/semParse/fold4 \
results/output/robocup/model_3_percy_NO_NULL_semPar_values/fold4 5 \
robocupLM/srilm-abs-robocup-fold4-3-gram.model.arpa

