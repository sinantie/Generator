#!/bin/bash

inputLists=datasets/A0/Dev.data
numThreads=6
stagedParamsFile=results/A0/alignments/model_3/1.exec/stage1.params.obj.gz
kBest=120
interpolationFactor=0.3
execDir=results/A0/generationNone_new/

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
-binariseAtWordLevel \
-ngramSize 3 \
-kBest ${kBest} \
-testInputLists ${inputLists} \
-execDir ${execDir} \
-stagedParamsFile ${stagedParamsFile} \
-ngramModelFile results/A0/lang_file.arpa \
-lengthCompensation 0 \
-useStopNode \
-lengthPredictionMode gold \
-allowNoneEvent \
-allowConsecutiveEvents

#-lengthPredictionModelFile A0/Dev.lengths \
#-averageTextLength 12 \
#-allowConsecutiveEvents \

cd ${CUR_DIR}
