#!/bin/bash
threads=2
#gaborLists/genDevListPathsGabor, trainListPathsGabor, genEvalListPathsGabor
#data/weatherGov/weatherGovGenDevGaborRecordTreebank.gz, weatherGovTrainGaborRecordTreebank.gz
input=data/weatherGov/weatherGovTrainGaborRecordTreebank.gz
output=results/output/weatherGov/alignments/model_3_gabor_bigrams_again
numIters=15
memory=-Xmx3000m


java $memory -cp dist/Generator.jar:dist/lib/Helper.jar:dist/lib/kylm.jar:dist/lib/meteor.jar:dist/lib/tercom.jar:\dist/lib/srilmWrapper:\
dist/stanford-postagger-2010-05-26.jar -ea -Djava.library.path=lib/wrappers induction.runtime.Induction \
-create \
-overwriteExecDir \
-modeltype event3 \
-examplesInSingleFile \
-inputLists $input \
-execDir $output \
-inputFileExt events \
-Options.stage1.numIters $numIters \
-numThreads $threads \
-indepEventTypes 0,10 \
-indepFields 0,5 \
-indepWords 0,5 \
-newEventTypeFieldPerWord 0,5 \
-newFieldPerWord 0,5 \
-dontCrossPunctuation \
-Options.stage1.smoothing 0.1 \
-disallowConsecutiveRepeatFields \
-noneFieldSmoothing 0 \
-outputFullPred \
-modelUnkWord \
-outputExampleFreq 1000 \
-initType random \
-initNoise 1e-3 \
-initSmoothing 0.01 \
-conditionNoneEvent \
-allowNoneEvent \
-dontOutputParams

#-initType staged \
#-stagedParamsFile results/output/weatherGov/alignments/model_3_gabor_no_sleet_windChill_15iter/stage1.params.obj.gz

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
#-excludedEventTypes sleetChance windChill \
#-excludedEventTypes airline airport booking_class city entity fare_basis_code location transport
#-excludedFields flight.stop
#-excludedFields temperature.time windChill.time windSpeed.time windDir.time gust.time precipPotential.time \
#thunderChance.time snowChance.time freezingRainChance.time sleetChance.time 
