#!/bin/bash
# options are: GoldDigit, GoldSplitLogo, GoldLogo, GoldLogo20, GoldLogoAll
DATASET=GoldLogoAll
inputLists=datasets/${DATASET}/Records.dev
#ngramModelFile=datasets/GoldDigit/Language.arpa
ngramModelFile=datasets/GoldSplitLogo/Language.arpa
numThreads=6
stagedParamsFile=results/${DATASET}/alignments/12.exec/stage1.params.obj.gz
kBest=120
# option are: gold, fixed, file, linearRegression
lengthPredictionMode=fixed
execDir=results/${DATASET}/generation/generation_kBest-${kBest}-${lengthPredictionMode}Length-withNone-noBinarise-1x-multiReferences--tied-solo.RP-noNoneEvent/

CUR_DIR=`pwd`
cd ..

java -Xmx16000m -cp lib/jung/collections-generic-4.01.jar:lib/commons-math-2.2.jar:lib/jung/colt-1.2.0.jar:lib/jung/concurrent-1.3.4.jar:lib/jackson-annotations-2.0.2.jar:lib/jackson-core-2.0.2.jar:lib/jackson-databind-2.0.2.jar:lib/jung/jung-algorithms-2.0.1.jar:lib/jung/jung-graph-impl-2.0.1.jar:lib/jung/jung-hypergraph-visualization-1.0.jar:lib/jung/jung-api-2.0.1.jar:lib/jung/jung-io-2.0.1.jar:lib/jung/jung-jai-2.0.1.jar:lib/jung/jung-visualization-2.0.1.jar:lib/stanford-corenlp-3.5.1-models.jar:lib/stanford-corenlp-3.6.0.jar:lib/jung/stax-api-1.0.1.jar:lib/jung/vecmath-1.3.1.jar:lib/weka.jar:lib/jung/wstx-asl-3.2.6.jar:lib/kylm.jar:lib/Helper.jar:lib/meteor.jar:lib/srilmWrapper.jar:lib/tercom.jar:lib/RoarkWrapper.jar:dist/Generator.jar \
induction.runtime.Generation \
-numThreads $numThreads \
-outputFullPred -create -overwriteExecDir \
-examplesInSingleFile \
-modelType generate \
-inputFileExt events \
-disallowConsecutiveRepeatFields \
-ngramWrapper kylm \
-outputExampleFreq 100 \
-reorderType eventType \
-maxPhraseLength 5 \
-ngramSize 3 \
-kBest ${kBest} \
-testInputLists ${inputLists} \
-execDir ${execDir} \
-stagedParamsFile ${stagedParamsFile} \
-ngramModelFile ${ngramModelFile} \
-lengthCompensation 0 \
-useStopNode \
-lengthPredictionMode ${lengthPredictionMode} \
-fixedTextLength 12 \
-allowConsecutiveEvents \
-useMultipleReferences

#-allowNoneEvent \
#-lengthPredictionModelFile gaborLists/genEvalGaborScaledPredLength_c6_g1.1.svr_round.length \
#-binariseAtWordLevel \
#-lengthPredictionModelFile A0/Dev.lengths \
#-averageTextLength 12 \
#-allowConsecutiveEvents \

cd ${CUR_DIR}
