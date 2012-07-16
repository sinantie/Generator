#!/bin/bash

input=$1
execDir=$2
stagedParamsFile=$3
dmvModelParamsFile=$4
ngramModelFile=$5
kBest=$6
interpolationFactor=$7
numThreads=$8
averageTextLength=$9

java -Xmx3000m -cp dist/Generator.jar:dist/lib/kylm.jar:dist/lib/meteor.jar:dist/lib/tercom.jar:dist/lib/srilmWrapper.jar \
-Djava.library.path=lib/wrappers induction.runtime.Generation \
-outputFullPred \
-create \
-overwrite \
-forceOutputOrder \
-inputFormat zmert \
-examplesInSingleFile \
-modelType generate \
-testInputLists ${input} \
-inputFileExt events \
-execDir ${execDir} \
-stagedParamsFile ${stagedParamsFile}/stage1.params.obj.gz \
-dmvModelParamsFile ${dmvModelParamsFile}/stage1.dmv.params.obj.gz \
-disallowConsecutiveRepeatFields \
-allowConsecutiveEvents \
-kBest ${kBest} \
-ngramModelFile ${ngramModelFile} \
-ngramWrapper srilm \
-outputExampleFreq 100  \
-reorderType eventType \
-maxPhraseLength 5 \
-averageTextLength ${averageTextLength} \
-ngramSize 3 \
-binariseAtWordLevel \
-useStopNode \
-interpolationFactor ${interpolationFactor} \
-useDependencies \
-posAtSurfaceLevel \
-tagDelimiter "_"

#-allowNoneEvent

#-lengthPredictionModelFile data/winHelpHLA/lengthPrediction.counts.linear-reg.model \
#-lengthPredictionFeatureType COUNTS \
#-lengthPredictionStartIndex 2 \
#-lengthCompensation 0 \
#-interpolationFactor ${interpolationFactor} \
#-useDependencies \
#-useGoldStandardOnly
#-posAtSurfaceLevel \
