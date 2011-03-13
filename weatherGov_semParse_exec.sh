#!/bin/bash

#Fold 1
./robocup_semParse.sh gaborLists/genEvalListPathsGabor results/output/weatherGov/semParse/semParse_no_LM \
results/output/weatherGov/alignments/model_3_gabor/1.exec 15 \
weatherGovLM/srilm-abs-weather-semantic-noisy-3-gram.model.arpa

