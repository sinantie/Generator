#!/bin/bash
java -Xmx3g -cp dist/Generator.jar:dist/lib/Helper.jar:dist/lib/kylm.jar:dist/lib/meteor.jar:dist/lib/tercom.jar:dist/lib/srilmWrapper:\
dist/stanford-postagger-2010-05-26.jar \
-Djava.library.path=lib/wrappers induction.runtime.Generation \
-outputFullPred -create \
-examplesInSingleFile \
-modelType generate \
-inputFileExt events \
-disallowConsecutiveRepeatFields \
-ngramWrapper srilm \
-outputExampleFreq 100 \
-allowConsecutiveEvents \
-reorderType eventType \
-maxPhraseLength 5 \
-binariseAtWordLevel \
-kBest 40 \
-testInputLists data/atis/test/atis-test.txt \
-execDir results/output/atis/generation/model_3_40-best_prior_0.01_STOP_predLength_all_oracle_again \
-stagedParamsFile results/output/atis/alignments/model_3/prior_0.01/stage1.params.obj \
-ngramModelFile atisLM/atis-all-train-3-gram.model.arpa \
-lengthPredictionModelFile data/atis/train/lengthPrediction.counts.linear-reg.model \
-lengthPredictionFeatureType COUNTS \
-lengthPredictionStartIndex 2 \
-lengthCompensation 0 \
-oracleReranker

#-excludedEventTypes airline airport booking_class city entity fare_basis_code location transport
#-excludedFields flight.stop
