#!/bin/bash
threads=6
input=datasets/GoldLogo/Records.train.Aligned.trees
output=results/GoldLogo/alignments/pcfg/
treebankRules=results/GoldLogo/treebanks/recordTreebankRulesRightBinarizeAligned

CUR_DIR=`pwd`
cd ..

java -Xmx16000m -cp lib/jung/collections-generic-4.01.jar:lib/commons-math-2.2.jar:lib/jung/colt-1.2.0.jar:lib/jung/concurrent-1.3.4.jar:lib/jackson-annotations-2.0.2.jar:lib/jackson-core-2.0.2.jar:lib/jackson-databind-2.0.2.jar:lib/jung/jung-algorithms-2.0.1.jar:lib/jung/jung-graph-impl-2.0.1.jar:lib/jung/jung-hypergraph-visualization-1.0.jar:lib/jung/jung-api-2.0.1.jar:lib/jung/jung-io-2.0.1.jar:lib/jung/jung-jai-2.0.1.jar:lib/jung/jung-visualization-2.0.1.jar:lib/stanford-corenlp-3.5.1-models.jar:lib/stanford-corenlp-3.6.0.jar:lib/jung/stax-api-1.0.1.jar:lib/jung/vecmath-1.3.1.jar:lib/weka.jar:lib/jung/wstx-asl-3.2.6.jar:lib/kylm.jar:lib/Helper.jar:lib/meteor.jar:lib/srilmWrapper.jar:lib/tercom.jar:lib/RoarkWrapper.jar:dist/Generator.jar \
induction.runtime.Induction \
-create \
-overwriteExecDir \
-modeltype event3pcfg \
-examplesInSingleFile \
-inputLists $input \
-execPoolDir $output \
-Options.stage1.numIters 20 \
-inputFileExt events \
-numThreads $threads \
-treebankRules $treebankRules \
-indepFields 0,5 \
-indepWords 0,5 \
-newEventTypeFieldPerWord 0,5 \
-newFieldPerWord 0,5 \
-Options.stage1.smoothing 0.01 \
-disallowConsecutiveRepeatFields \
-noneFieldSmoothing 0.01 \
-outputFullPred \
-outputExampleFreq 100 \
-initNoise 1e-3 \
-initSmoothing 0.01 \
-initType artificial \
-fixRecordSelection \
-binarizedAtSentenceLevel \
-wordsPerRootRule \
-maxDocLength 20 \
-docLengthBinSize 5 \
-useStopNode
	
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

# Misc
# ----
#-inputPosTagged \
#-modelUnkWord \
#-useStopNode \
#-useGoldStandardOnly
#-excludedEventTypes airline airport booking_class city entity fare_basis_code location transport
#-excludedFields flight.stop
#-excludedFields temperature.time windChill.time windSpeed.time windDir.time gust.time precipPotential.time \
#thunderChance.time snowChance.time freezingRainChance.time sleetChance.time 

cd ${CUR_DIR}

