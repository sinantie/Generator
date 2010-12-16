#!/bin/bash

#Fold 1
./robocup_gen.sh robocupLists/robocupFold1PathsEval results/output/robocup/generation/all/fold1 \
results/output/robocup/model_3_percy/fold1 15 robocupLM/srilm-abs-robocup-fold1-3-gram.model.arpa 5 2

#Fold 2
./robocup_gen.sh robocupLists/robocupFold2PathsEval results/output/robocup/generation/all/fold2 \
results/output/robocup/model_3_percy/fold2 15 robocupLM/srilm-abs-robocup-fold2-3-gram.model.arpa 5 2

#Fold 3
./robocup_gen.sh robocupLists/robocupFold3PathsEval results/output/robocup/generation/all/fold3 \
results/output/robocup/model_3_percy/fold3 15 robocupLM/srilm-abs-robocup-fold3-3-gram.model.arpa 6 2

#Fold 4
./robocup_gen.sh robocupLists/robocupFold4PathsEval results/output/robocup/generation/all/fold4 \
results/output/robocup/model_3_percy/fold4 15 robocupLM/srilm-abs-robocup-fold4-3-gram.model.arpa 6 2

