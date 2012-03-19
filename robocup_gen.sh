#!/bin/bash

input=$1
numThreads=$2
stagedParamsFile=$3
dmvModelParamsFile=$4
kBest=$5
interpolationFactor=0.3
execDir=results/output/atis/generation/dependencies/noRecursiveWeight/model_3_${kBest}-best_0.01_STOP_inter${interpolationFactor}_condLM_hypRecomb_lmLEX_POS_predLength
ngramModelFile=$5
averageTextLength=$6

java -Xmx1800m -cp dist/Generator.jar:dist/lib/kylm.jar:dist/lib/meteor.jar:dist/lib/tercom.jar:dist/lib/srilmWrapper.jar \
-Djava.library.path=lib/wrappers induction.runtime.Generation 
-outputFullPred \
-create \
-modelType generate \
-testInputLists ${input} \
-inputFileExt events \
-execDir ${execDir} \
-stagedParamsFile ${stagedParamsFile}/stage1.params.obj \
-dmvModelParamsFile ${dmvModelParamsFile} \
-disallowConsecutiveRepeatFields \
-kBest ${kBest} \
-ngramModelFile ${ngramModelFile} \
-ngramWrapper srilm \
-outputExampleFreq 100  \
-averageTextLength ${averageTextLength} \
-ngramSize 2 \
-binariseAtWordLevel
