#!/bin/bash
# options are: GoldDigit, GoldSplitLogo, GoldLogo, GoldLogo
DATASET=GoldLogo
threads=6
input=datasets/${DATASET}/Records.train.10
output=results/${DATASET}/alignments/

smooth=0.01

CUR_DIR=`pwd`
cd ..
mkdir -p ${output}

java -Xmx16000m -cp lib/jung/collections-generic-4.01.jar:lib/commons-math-2.2.jar:lib/jung/colt-1.2.0.jar:lib/jung/concurrent-1.3.4.jar:lib/jackson-annotations-2.0.2.jar:lib/jackson-core-2.0.2.jar:lib/jackson-databind-2.0.2.jar:lib/jung/jung-algorithms-2.0.1.jar:lib/jung/jung-graph-impl-2.0.1.jar:lib/jung/jung-hypergraph-visualization-1.0.jar:lib/jung/jung-api-2.0.1.jar:lib/jung/jung-io-2.0.1.jar:lib/jung/jung-jai-2.0.1.jar:lib/jung/jung-visualization-2.0.1.jar:lib/stanford-corenlp-3.5.1-models.jar:lib/stanford-corenlp-3.6.0.jar:lib/jung/stax-api-1.0.1.jar:lib/jung/vecmath-1.3.1.jar:lib/weka.jar:lib/jung/wstx-asl-3.2.6.jar:lib/kylm.jar:lib/Helper.jar:lib/meteor.jar:lib/srilmWrapper.jar:lib/tercom.jar:lib/RoarkWrapper.jar:dist/Generator.jar induction.runtime.Induction \
-create \
-modeltype event3 \
-examplesInSingleFile \
-inputLists $input \
-execPoolDir $output \
-Options.stage1.numIters 15 \
-numThreads $threads \
-initType random \
-initNoise 0.0001 \
-indepEventTypes 0,10 \
-indepFields 0,5 \
-indepWords 0,5 \
-newEventTypeFieldPerWord 0,5 \
-newFieldPerWord 0,5 \
-disallowConsecutiveRepeatFields \
-dontCrossPunctuation \
-Options.stage1.smoothing $smooth \
-noneFieldSmoothing 0.01 \
-useStopNode \
-allowNoneEvent \
-outputFullPred 
	
#-Options.stage1.useVarUpdates \
#-posAtSurfaceLevel \
#-inputPosTagged

#-modelUnkWord \

#-useGoldStandardOnly
#-excludedEventTypes airline airport booking_class city entity fare_basis_code location transport
#-excludedFields flight.stop

cd ${CUR_DIR}
