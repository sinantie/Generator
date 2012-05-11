#!/bin/bash

inputLists=data/atis/test/atis-test.txt
#inputLists=test/testAtisExamples2
execDir=results/output/atis/generation/dependencies_uniformZ/grid/model_3_${1}-best_0.01_STOP_inter${2}_condLM_hypRecomb_lmLEX_POS_predLength
numThreads=1
stagedParamsFile=results/output/atis/alignments/model_3/prior_0.01_POS_again/stage1.params.obj.gz
dmvModelParamsFile=results/output/atis/dmv/train/atis_raw5000_full_indexers_uniformZ_initNoise_POS_100/stage1.dmv.params.obj.gz
kBest=$1
interpolationFactor=$2
java -Xmx3000m -cp dist/Generator.jar:dist/lib/Helper.jar:dist/lib/kylm.jar:dist/lib/meteor.jar:dist/lib/tercom.jar:dist/lib/srilmWrapper:\
dist/stanford-postagger-2010-05-26.jar \
-Djava.library.path=lib/wrappers induction.runtime.Generation \
-create \
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
-kBest ${kBest} \
-testInputLists ${inputLists} \
-execDir ${execDir} \
-stagedParamsFile  ${stagedParamsFile} \
-dmvModelParamsFile ${dmvModelParamsFile} \
-ngramModelFile atisLM/atis-all-train-3-gram.model.arpa \
-ngramSize 3 \
-lengthPredictionModelFile data/atis/train/lengthPrediction.counts.linear-reg.model \
-lengthPredictionFeatureType COUNTS \
-lengthPredictionStartIndex 2 \
-lengthCompensation 0 \
-posAtSurfaceLevel \
-interpolationFactor ${interpolationFactor} \
-useDependencies \
-useStopNode \
-outputFullPred

#-oracleReranker
#-secondaryNgramModelFile atisLM/atis-all-train-3-gram-tagged.CDnumbers.tags_only.model.arpa

#-excludedEventTypes airline airport booking_class city entity fare_basis_code location transport
#-excludedFields flight.stop
