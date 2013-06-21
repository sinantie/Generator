#!/bin/bash
threads=4
#gaborLists/genDevListPathsGabor, trainListPathsGabor, genEvalListPathsGabor
#data/weatherGov/weatherGovGenDevGaborRecordTreebank.gz, weatherGovTrainGaborRecordTreebank.gz
input=data/weatherGov/weatherGovTrainGaborRecordTreebankRightBinarizeGoldRst.gz
output=results/output/weatherGov/alignments/pcfg/rst/model_3_gabor_record_pcfg_treebank_gold_treebank_rst_20iter
#data/weatherGov/treebanks/recordTreebankRulesGenDevRightBinarize recordTreebankRulesTrainRightBinarize
#treebankRules=data/weatherGov/treebanks/recordTreebankRulesTrainRightBinarizeUnaryRules
treebankRules=data/weatherGov/treebanks/torontoRST/recordTreebankRulesRightBinarizeGoldRst
memory=-Xmx25000m

mkdir -p $output

java $memory -XX:+UseConcMarkSweepGC -cp dist/Generator.jar:dist/lib/Helper.jar:dist/lib/kylm.jar:dist/lib/meteor.jar:dist/lib/tercom.jar:\dist/lib/srilmWrapper:\
dist/stanford-postagger-2010-05-26.jar -ea -Djava.library.path=lib/wrappers induction.runtime.Induction \
-create \
-overwriteExecDir \
-modeltype event3pcfg \
-examplesInSingleFile \
-inputLists $input \
-execDir $output \
-Options.stage1.numIters 20 \
-inputFileExt events \
-numThreads $threads \
-treebankRules $treebankRules \
-indepFields 0,5 \
-indepWords 0,5 \
-newEventTypeFieldPerWord 0,5 \
-newFieldPerWord 0,5 \
-Options.stage1.smoothing 0.1 \
-disallowConsecutiveRepeatFields \
-noneFieldSmoothing 0 \
-outputFullPred \
-modelUnkWord \
-outputExampleFreq 1000 \
-initNoise 1e-3 \
-initSmoothing 0.01 \
-initType artificial \
-fixRecordSelection \
-wordsPerRootRule \
-maxDocLength 90 \
-docLengthBinSize 5
#-binarizedAtSentenceLevel
#-inputPosTagged

# Record PCFG - Treebank Input
#-treebankRules $treebankRules \
#-fixRecordSelection \
#-initType artificial \
#-indepFields 0,5 \
#-indepWords 0,5 \
#-newEventTypeFieldPerWord 0,5 \
#-newFieldPerWord 0,5 \
#-Options.stage1.smoothing 0.1 \
#-wordsPerRootRule


# Record HMM alignment
# --------------------
#-modeltype event3 \
#-Options.stage1.numIters 15 \
#-indepEventTypes 0,10 \
#-indepFields 0,5 \
#-indepWords 0,5 \
#-newEventTypeFieldPerWord 0,5 \
#-dontCrossPunctuation \
#-conditionNoneEvent \
#-allowNoneEvent \


# Viterbi EM params
# -----------------
#-Options.stage1.smoothing 0.1 \
#-initType uniformz \
#-initNoise 0
#-Options.stage1.hardUpdate

#-inputPosTagged

# Misc
# ----
#-useStopNode \
#-useGoldStandardOnly
#-excludedEventTypes airline airport booking_class city entity fare_basis_code location transport
#-excludedFields flight.stop
#-excludedFields temperature.time windChill.time windSpeed.time windDir.time gust.time precipPotential.time \
#thunderChance.time snowChance.time freezingRainChance.time sleetChance.time 
