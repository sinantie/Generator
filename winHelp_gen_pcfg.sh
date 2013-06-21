#!/bin/bash

input=$1
execDir=$2
stagedParamsFile=$3
dmvModelParamsFile=$4
ngramModelFile=$5
kBest=$6
interpolationFactor=$7
lengthPredictionFile=$8
lengthPredictionFeatureType=$9
shift 1
treebankRules=$9

java -Xmx28000m -cp dist/Generator.jar:dist/lib/kylm.jar:dist/lib/meteor.jar:dist/lib/tercom.jar:dist/lib/srilmWrapper.jar \
-Djava.library.path=lib/wrappers induction.runtime.Generation \
-numThreads 1 \
-outputFullPred \
-create \
-overwriteExecDir \
-examplesInSingleFile \
-testInputLists ${input} \
-inputFileExt events \
-execDir ${execDir} \
-stagedParamsFile ${stagedParamsFile} \
-dmvModelParamsFile ${dmvModelParamsFile}/stage1.dmv.params.obj.gz \
-disallowConsecutiveRepeatFields \
-examplesInSingleFile \
-kBest ${kBest} \
-ngramModelFile ${ngramModelFile} \
-ngramWrapper kylm \
-outputExampleFreq 5  \
-lengthPredictionMode gold \
-ngramSize 3 \
-binariseAtWordLevel \
-useStopNode \
-modelType generatePcfg \
-maxPhraseLength 12 \
-reorderType ignore \
-outputPcfgTrees \
-treebankRules ${treebankRules} \
-wordsPerRootRule \
-maxDocLength 100 \
-docLengthBinSize 15 \
-allowConsecutiveEvents \
-Options.stage1.cfgThreshold 0.54

#-allowNoneEvent
#-forceOutputOrder \
#-lengthPredictionModelFile ${lengthPredictionFile} \
#-lengthPredictionFeatureType ${lengthPredictionFeatureType} \
#-lengthPredictionStartIndex 2 \
#-interpolationFactor ${interpolationFactor} \
#-useDependencies \
#-posAtSurfaceLevel \
#-tagDelimiter "_" \
#-allowNoneEvent

# Record PCFG - Grammar/Treebank Input
#-modelType generatePcfg \
#-examplesInSingleFile \
#-treebankRules $treebankRules \
#-maxPhraseLength 12  \
#-reorderType ignore \
#-outputPcfgTrees \
#-fixRecordSelection 
#-wordsPerRootRule
#-Options.stage1.cfgThreshold 0.008 


# Record HMM
#-modelType generate \
#-examplesInSingleFile \
#-reorderType eventType \
#-maxPhraseLength 5 \
#-allowConsecutiveEvents \

#-averageTextLength ${averageTextLength} \
#-lengthPredictionModelFile data/winHelpHLA/lengthPrediction.counts.linear-reg.model \
#-lengthPredictionFeatureType COUNTS \
#-lengthPredictionStartIndex 2 \
#-lengthCompensation 0 \
#-interpolationFactor ${interpolationFactor} \
#-useDependencies \
#-useGoldStandardOnly
#-posAtSurfaceLevel \
