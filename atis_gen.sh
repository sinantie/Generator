#!/bin/bash

inputLists=data/atis/test/atis-test.txt
execDir=results/output/atis/generation/dependencies/noRecursiveWeight/model_3_120-best_0.01_STOP_inter0.3_condLM_hypRecomb_lmLEX_POS_predLength
numThreads=2
stagedParamsFile=results/output/atis/alignments/model_3/prior_0.01_POS/stage1.params.obj.gz
dmvModelParamsFile=results/output/atis/dmv/train/atis_raw5000_full_indexers_001_POS_50/stage1.dmv.params.obj.gz
kBest=120
interpolationFactor=0.3
java -Xmx2000m -cp dist/Generator.jar:dist/lib/Helper.jar:dist/lib/kylm.jar:dist/lib/meteor.jar:dist/lib/tercom.jar:dist/lib/srilmWrapper:\
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
-kBest ${kBest} \
-testInputLists ${inputLists} \
-execDir ${execDir} \
-stagedParamsFile  ${stagedParamsFile} \
-dmvModelParamsFile ${dmvModelParamsFile} \
-ngramModelFile atisLM/atis-all-train-3-gram.model.arpa \
-lengthPredictionModelFile data/atis/train/lengthPrediction.counts.linear-reg.model \
-lengthPredictionFeatureType COUNTS \
-lengthPredictionStartIndex 2 \
-lengthCompensation 0 \
-posAtSurfaceLevel \
-interpolationFactor ${interpolationFactor} \
-useDependencies
#-oracleReranker
#
#-secondaryNgramModelFile atisLM/atis-all-train-3-gram-tagged.CDnumbers.tags_only.model.arpa

#-excludedEventTypes airline airport booking_class city entity fare_basis_code location transport
#-excludedFields flight.stop
