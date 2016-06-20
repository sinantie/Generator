#!/bin/bash
# options are: GoldDigit, GoldSplitLogo, GoldLogo, GoldLogo20, GoldLogoRTSPAll
DATASET=GoldLogoRTSPAll
#Records.dev, Records.dev.down, Records.test, Records.test.down, AllWorlds.dev.records, AllWorlds.test.records
inputLists=datasets/${DATASET}/Train.records
#ngramModelFile=datasets/GoldDigit/Language.arpa
ngramModelFile=datasets/GoldSplitLogo/Language.arpa
numThreads=4
stagedParamsFile=results/${DATASET}/alignments/1.exec/stage1.params.obj.gz
kBest=120
lengthDeviation=2
length=12
lengthLambda=100
# option are: gold, fixed, file, linearRegression, multipleCandidates
lengthPredictionMode=fixed
execDir=results/${DATASET}/generation-dev/generation_kBest-${kBest}-${lengthPredictionMode}Length-${length}-withNone-noBinarise-1x-multiReferences-lengthDeviation-${lengthDeviation}-lengthLambda-${lengthLambda}-noDups-GOLD/

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
-fixedTextLength ${length} \
-allowConsecutiveEvents \
-allowNoneEvent \
-useMultipleReferences \
-lengthDeviation ${lengthDeviation} \
-lengthLambda ${lengthLambda}

#-tieCatFieldParameters pos.RP solo.RP \
#-lengthPredictionModelFile gaborLists/genEvalGaborScaledPredLength_c6_g1.1.svr_round.length \
#-binariseAtWordLevel \
#-lengthPredictionModelFile A0/Dev.lengths \
#-averageTextLength 12 \
#-allowConsecutiveEvents \

cd ${CUR_DIR}
