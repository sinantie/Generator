#!/bin/bash

#genDevListPathsGabor, genEvalListPathsGabor
inputLists=data/weatherGov/weatherGovGenDevGaborRecordTreebankUnaryRules_modified2
numThreads=1
#stagedParamsFile=results/output/weatherGov/alignments/pos/model_3_cond_null_POS_CDNumbers/stage1.params.obj.gz
#stagedParamsFile=results/output/weatherGov/alignments/model_3_15_NO_STOP_NEW/stage1.params.obj.gz
#stagedParamsFile=results/output/weatherGov/alignments/pcfg/model_3_gabor_record_pcfg_treebank_unaryRules_30iter/stage1.params.obj.gz
stagedParamsFile=results/output/weatherGov/alignments/pcfg/model_3_gabor_record_pcfg_treebank_alignments_thres10_externalTreebank/stage1.extTreebank.params.obj.gz
dmvModelParamsFile=results/output/weatherGov/dmv/train/weatherGov_uniformZ_initNoise_POS_100/stage1.dmv.params.obj.gz
kBest=60
interpolationFactor=1
#execDir=results/output/weatherGov/generation/dependencies/model_3_${kBest}-best_0.01_NO_STOP_inter${interpolationFactor}_condLM_hypRecomb_lmLEX_NO_STOP
#execDir=results/output/weatherGov/generation/dev/model_3_${kBest}-best_0.01_NO_STOP
execDir=results/output/weatherGov/generation/pcfg/model_3_${kBest}-best_TEST
treebankRules=data/weatherGov/treebanks/recordTreebankRulesTrainRightBinarizeAlignmentsThres10

java -Xmx3000m -cp dist/Generator.jar:dist/lib/Helper.jar:dist/lib/kylm.jar:dist/lib/meteor.jar:dist/lib/tercom.jar:dist/lib/srilmWrapper:\
dist/lib/stanford-postagger-2010-05-26.jar \
-Djava.library.path=lib/wrappers induction.runtime.Generation \
-numThreads $numThreads \
-create \
-overwriteExecDir \
-modelType generatePcfg \
-examplesInSingleFile \
-treebankRules ${treebankRules} \
-maxPhraseLength 10 \
-reorderType ignore \
-outputPcfgTrees \
-wordsPerRootRule \
-Options.stage1.cfgThreshold 0.04 \
-inputFileExt events \
-disallowConsecutiveRepeatFields \
-ngramModelFile weatherGovLM/gabor-srilm-abs-3-gram.model.arpa \
-ngramWrapper srilm \
-outputExampleFreq 500 \
-allowConsecutiveEvents \
-kBest ${kBest} \
-testInputLists ${inputLists} \
-execDir ${execDir} \
-stagedParamsFile ${stagedParamsFile} \
-dmvModelParamsFile ${dmvModelParamsFile} \
-lengthPredictionMode file \
-lengthPredictionModelFile gaborLists/genEvalGaborScaledPredLength_c6_g1.1.svr_round.length \
-lengthPredictionFeatureType values \
-lengthPredictionStartIndex 4 \
-lengthCompensation 0 \
-numAsSymbol \
-binariseAtWordLevel \
-outputFullPred \
-maxDocLength 90 \
-docLengthBinSize 5

#-allowNoneEvent \
#-excludedEventTypes sleetChance windChill

# Record PCFG - Grammar/Treebank Input
#-modelType generatePcfg \
#-examplesInSingleFile \
#-treebankRules $treebankRules \
#-maxPhraseLength 10  \
#-reorderType ignore \
#-outputPcfgTrees \
#-fixRecordSelection 
#-wordsPerRootRule


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
