#!/bin/bash

#genDevListPathsGabor, genEvalListPathsGabor
inputLists=data/weatherGov/weatherGovGenEvalGaborRecordTreebankUnaryRules.gz
numThreads=2
#stagedParamsFile=results/output/weatherGov/alignments/pos/model_3_cond_null_POS_CDNumbers/stage1.params.obj.gz
#stagedParamsFile=results/output/weatherGov/alignments/model_3_15_NO_STOP_NEW/stage1.params.obj.gz
stagedParamsFile=results/output/weatherGov/alignments/pcfg/model_3_gabor_record_pcfg_treebank_unaryRules_30iter/stage1.params.obj.gz
dmvModelParamsFile=results/output/weatherGov/dmv/train/weatherGov_uniformZ_initNoise_POS_100/stage1.dmv.params.obj.gz
kBest=15
interpolationFactor=1
#execDir=results/output/weatherGov/generation/dependencies/model_3_${kBest}-best_0.01_NO_STOP_inter${interpolationFactor}_condLM_hypRecomb_lmLEX_NO_STOP
#execDir=results/output/weatherGov/generation/dev/model_3_${kBest}-best_0.01_NO_STOP
execDir=results/output/weatherGov/generation/dev/pcfg/model_3_${kBest}-best_0.01_treebank_unaryRules_test_predLength
treebankRules=data/weatherGov/treebanks/recordTreebankRulesTrainRightBinarizeUnaryRules

java -Xmx6g -cp dist/Generator.jar:dist/lib/Helper.jar:dist/lib/kylm.jar:dist/lib/meteor.jar:dist/lib/tercom.jar:dist/lib/srilmWrapper:\
dist/lib/stanford-postagger-2010-05-26.jar \
-Djava.library.path=lib/wrappers induction.runtime.Generation \
-numThreads $numThreads \
-create \
-overwriteExecDir \
-modelType generatePcfg \
-examplesInSingleFile \
-treebankRules $treebankRules \
-maxPhraseLength 10  \
-reorderType ignore \
-outputPcfgTrees \
-inputFileExt events \
-disallowConsecutiveRepeatFields \
-ngramWrapper srilm \
-outputExampleFreq 250 \
-allowConsecutiveEvents \
-kBest ${kBest} \
-testInputLists ${inputLists} \
-execDir ${execDir} \
-stagedParamsFile ${stagedParamsFile} \
-dmvModelParamsFile ${dmvModelParamsFile} \
-lengthPredictionModelFile gaborLists/lengthPrediction.values.linear-reg.model \
-ngramModelFile weatherGovLM/gabor-srilm-abs-3-gram.model.arpa \
-lengthPredictionFeatureType values \
-lengthPredictionStartIndex 4 \
-lengthCompensation 0 \
-numAsSymbol \
-allowNoneEvent \
-binariseAtWordLevel \
-outputFullPred

# Record PCFG - Grammar/Treebank Input
#-modelType generatePcfg \
#-examplesInSingleFile \
#-treebankRules $treebankRules \
#-maxPhraseLength 10  \
#-reorderType ignore \
#-outputPcfgTrees \
#-fixRecordSelection


# Record HMM 
#-modelType generate \
#-examplesInSingleFile \
#-reorderType eventType \
#-maxPhraseLength 5 \

# Misc
# ----
#-useStopNode \
#-binariseAtWordLevel \
#-posAtSurfaceLevel \
#-interpolationFactor ${interpolationFactor} \
#-useDependencies

#-lengthPredictionModelFile gaborLists/lengthPrediction.values.linear-reg.model \
#-excludedEventTypes airline airport booking_class city entity fare_basis_code location transport
#-excludedFields flight.stop
