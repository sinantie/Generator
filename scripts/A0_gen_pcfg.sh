#!/bin/bash
DATASET=GoldLogo
inputLists=datasets/GoldLogo/Records.dev
numThreads=6
stagedParamsFile=results/${DATASET}/alignments/pcfg/3.exec/stage1.params.obj.gz
kBest=120
# option are: gold, fixed, file
lengthPredictionMode=fixed
execDir=results/${DATASET}/generation/generation_pcfg_kBest-${kBest}-${lengthPredictionMode}Length-2/
treebankRules=results/${DATASET}/treebanks/recordTreebankRulesRightBinarizeAligned

CUR_DIR=`pwd`
cd ..

java -Xmx16000m -cp lib/jung/collections-generic-4.01.jar:lib/commons-math-2.2.jar:lib/jung/colt-1.2.0.jar:lib/jung/concurrent-1.3.4.jar:lib/jackson-annotations-2.0.2.jar:lib/jackson-core-2.0.2.jar:lib/jackson-databind-2.0.2.jar:lib/jung/jung-algorithms-2.0.1.jar:lib/jung/jung-graph-impl-2.0.1.jar:lib/jung/jung-hypergraph-visualization-1.0.jar:lib/jung/jung-api-2.0.1.jar:lib/jung/jung-io-2.0.1.jar:lib/jung/jung-jai-2.0.1.jar:lib/jung/jung-visualization-2.0.1.jar:lib/stanford-corenlp-3.5.1-models.jar:lib/stanford-corenlp-3.6.0.jar:lib/jung/stax-api-1.0.1.jar:lib/jung/vecmath-1.3.1.jar:lib/weka.jar:lib/jung/wstx-asl-3.2.6.jar:lib/kylm.jar:lib/Helper.jar:lib/meteor.jar:lib/srilmWrapper.jar:lib/tercom.jar:lib/RoarkWrapper.jar:dist/Generator.jar \
induction.runtime.Generation \
-numThreads $numThreads \
-create \
-overwriteExecDir \
-modelType generatePcfg \
-examplesInSingleFile \
-treebankRules ${treebankRules} \
-maxPhraseLength 5 \
-reorderType ignore \
-outputPcfgTrees \
-wordsPerRootRule \
-Options.stage1.cfgThreshold 0 \
-inputFileExt events \
-disallowConsecutiveRepeatFields \
-ngramModelFile datasets/GoldSplitLogo/Language.arpa \
-ngramWrapper kylm \
-outputExampleFreq 100 \
-allowConsecutiveEvents \
-kBest ${kBest} \
-testInputLists ${inputLists} \
-execDir ${execDir} \
-stagedParamsFile ${stagedParamsFile} \
-lengthPredictionMode ${lengthPredictionMode} \
-fixedTextLength 12 \
-outputFullPred \
-maxDocLength 20 \
-useStopNode \
-docLengthBinSize 2

#-allowNoneEvent \

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

cd ${CUR_DIR}