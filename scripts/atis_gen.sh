#!/bin/bash

inputLists=datasets/atis-test.txt
numThreads=4
stagedParamsFile=results/atis/alignments/model_3/0.exec/stage1.params.obj.gz
dmvModelParamsFile=results/atis/dmv/train/atis_raw5000_full_indexers_001_POS_50/stage1.dmv.params.obj.gz
kBest=120
interpolationFactor=0.3
execDir=results/atis/generation/

CUR_DIR=`pwd`
cd ..

java -Xmx16000m -cp lib/jung/collections-generic-4.01.jar:lib/commons-math-2.2.jar:lib/jung/colt-1.2.0.jar:lib/jung/concurrent-1.3.4.jar:lib/jackson-annotations-2.0.2.jar:lib/jackson-core-2.0.2.jar:lib/jackson-databind-2.0.2.jar:lib/jung/jung-algorithms-2.0.1.jar:lib/jung/jung-graph-impl-2.0.1.jar:lib/jung/jung-hypergraph-visualization-1.0.jar:lib/jung/jung-api-2.0.1.jar:lib/jung/jung-io-2.0.1.jar:lib/jung/jung-jai-2.0.1.jar:lib/jung/jung-visualization-2.0.1.jar:lib/stanford-corenlp-3.5.1-models.jar:lib/stanford-corenlp-3.5.1.jar:lib/jung/stax-api-1.0.1.jar:lib/jung/vecmath-1.3.1.jar:lib/weka.jar:lib/jung/wstx-asl-3.2.6.jar:lib/kylm.jar:lib/Helper.jar:lib/meteor.jar:lib/srilmWrapper.jar:lib/tercom.jar:lib/RoarkWrapper.jar:dist/Generator.jar \
induction.runtime.Generation \
-numThreads $numThreads \
-outputFullPred -create -overwriteExecDir \
-examplesInSingleFile \
-modelType generate \
-inputFileExt events \
-disallowConsecutiveRepeatFields \
-ngramWrapper kylm \
-outputExampleFreq 100 \
-allowConsecutiveEvents \
-reorderType eventType \
-maxPhraseLength 5 \
-binariseAtWordLevel \
-kBest ${kBest} \
-testInputLists ${inputLists} \
-execDir ${execDir} \
-stagedParamsFile  ${stagedParamsFile} \
-ngramModelFile output_file.arpa \
-lengthCompensation 0 \
-useStopNode 
#-dmvModelParamsFile ${dmvModelParamsFile} \
#-lengthPredictionModelFile ../datasets/atis/train/lengthPrediction.counts.linear-reg.model \
#-lengthPredictionFeatureType counts \
#-lengthPredictionStartIndex 2 \
#-posAtSurfaceLevel \
#-interpolationFactor ${interpolationFactor} \
#-useDependencies
#-oracleReranker
#
#-secondaryNgramModelFile atisLM/atis-all-train-3-gram-tagged.CDnumbers.tags_only.model.arpa

#-excludedEventTypes airline airport booking_class city entity fare_basis_code location transport
#-excludedFields flight.stop

cd ${CUR_DIR}
