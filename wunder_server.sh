#!/bin/bash
format=wunderground
port=4444
stagedParamsFile=results/output/weatherGov/alignments/model_3_gabor_cond_null_correct/2.exec/stage1.params.obj
numThreads=2
kBest=20

java -Xmx2g -cp dist/Generator.jar:dist/lib/Helper.jar:dist/lib/kylm.jar:dist/lib/meteor.jar:dist/lib/tercom.jar:dist/lib/srilmWrapper:\
dist/stanford-postagger-2010-05-26.jar \
-Djava.library.path=lib/wrappers induction.runtime.server.MultiServer \
-numThreads ${numThreads} \
-jsonFormat ${format} \
-port ${port} \
-modelType generate \
-testInputLists test/testWeatherGovEvents \
-inputFileExt events \
-stagedParamsFile ${stagedParamsFile} \
-disallowConsecutiveRepeatFields \
-kBest ${kBest} \
-ngramModelFile weatherGovLM/gabor-srilm-abs-3-gram.model.arpa \
-ngramWrapper srilm \
-reorderType eventType \
-allowNoneEvent \
-maxPhraseLength 5 \
-binariseAtWordLevel \
-ngramSize 3 \
-numAsSymbol \
-lengthPredictionModelFile gaborLists/lengthPrediction.values.linear-reg.model \
-lengthPredictionFeatureType VALUES \
-lengthPredictionStartIndex 2 \
-numAsSymbol
#-allowConsecutiveEvents
#-conditionNoneEvent

#-useDependencies
#-interpolationFactor 0.5
#-posAtSurfaceLevel
#-dmvModelParamsFile results/output/weatherGov/dmv/train/
#weatherGov_uniformZ_initNoise_POS_100/stage1.dmv.params.obj.gz
#-oracleReranker
#-omitEmptyEvents
#-useGoldStandardOnly

