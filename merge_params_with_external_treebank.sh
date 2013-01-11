#!/bin/bash

input=$1
stagedParamsFile=$2
externalTreebankFile=$3
treebankRules=$4
execDir=$5
maxDocLength=$6
docLengthBinSize=$7
initSmoothing=$8

java -Xmx2g -cp dist/Generator.jar:dist/lib/Helper.jar:dist/lib/kylm.jar:dist/lib/meteor.jar:dist/lib/tercom.jar:dist/lib/srilmWrapper:\
dist/stanford-postagger-2010-05-26.jar \
-Djava.library.path=lib/wrappers induction.utils.MergeParamsWithExternalTreebankExecutor \
-modelType event3 \
-create \
-overwriteExecDir \
-examplesInSingleFile \
-inputLists ${input} \
-execDir ${execDir} \
-stagedParamsFile ${stagedParamsFile} \
-externalTreebankFile ${externalTreebankFile} \
-treebankRules ${treebankRules} \
-dontOutputParams \
-maxDocLength ${maxDocLength} \
-docLengthBinSize ${docLengthBinSize}


#-excludedEventTypes sleetChance windChill
