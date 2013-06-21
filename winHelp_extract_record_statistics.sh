#!/bin/bash

input=$1
stagedParamsFile=$2
execDir=$3
suffix=$4
predInput=$5
markovOrder=$6
externalTreesInput=$7
externalTreesInputType=$8

java -Xmx500m -cp dist/Generator.jar:dist/lib/Helper.jar:dist/lib/kylm.jar:dist/lib/meteor.jar:dist/lib/tercom.jar:dist/lib/srilmWrapper:\
dist/stanford-postagger-2010-05-26.jar \
-Djava.library.path=lib/wrappers induction.utils.ExtractRecordsStatisticsExecutor \
-exportType recordType \
-create \
-overwriteExecDir \
-modelType event3 \
-inputFileExt events \
-examplesInSingleFile \
-outputExampleFreq 500 \
-inputLists ${input} \
-execDir ${execDir} \
-stagedParamsFile ${stagedParamsFile}/stage1.params.obj.gz \
-binarize right \
-markovOrder ${markovOrder} \
-extractRecordTrees \
-suffix ${suffix} \
-initType staged \
-useEventTypeNames \
-ruleCountThreshold 0 \
-externalTreesInput ${externalTreesInput} \
-externalTreesInputType rst \
-predInput ${predInput} \
-parentAnnotation \
-overrideCleaningHeuristics

#-overrideCleaningHeuristics # USEFUL for gold-standard
#-extractNoneEvent \
#-ruleCountThreshold 5
#-examplesInSingleFile \
#-exportEvent3 \
#-modifiedBinarization
#-useEventTypeNames\
#-extractRecordTrees
#-countRepeatedRecords
#-countSentenceNgrams
#-countDocumentNgrams
#-writePermutations
#-delimitSentences
