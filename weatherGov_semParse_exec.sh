#!/bin/bash

#Fold 1
./weatherGov_semParse.sh gaborLists/semParseWeatherEval200 results/output/weatherGov/semParse/semParse_no_n_no_t_LM_3_all \
results/output/weatherGov/alignments/model_3_gabor_mapVecs/0.exec 10 \
weatherGovLM/gabor-srilm-abs-weather-semantic-full-no-times-no-none-3-gram.model.arpa

