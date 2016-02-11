#!/bin/bash

threads=2
input=../datasets/mathWorld/revisedFullRecords.event3
output=results/output/mathWorld/alignments/model_3
smooth=0.01

mkdir -p ${output}

CUR_DIR=`pwd`
cd ..
java -Xmx16000m -cp dist/Generator.jar:dist/lib/Helper.jar:dist/lib/kylm.jar:dist/lib/meteor.jar:dist/lib/tercom.jar:\dist/lib/srilmWrapper:\
dist/lib/stanford-corenlp-3.5.1.jar:dist/lib/stanford-corenlp-3.5.1-models.jar: induction.runtime.Induction \
-create \
-modeltype event3 \
-examplesInSingleFile \
-inputLists $input \
-execPoolDir $output \
-Options.stage1.numIters 15 \
-numThreads $threads \
-initType random \
-initNoise 0 \
-indepEventTypes 0,10 \
-indepWords 0,5 \
-useFieldSets 0,-1 \
-indepFields 0,5 \
-newEventTypeFieldPerWord 0,5 \
-newFieldPerWord 0,5 \
-disallowConsecutiveRepeatFields \
-dontCrossPunctuation \
-useStringLabels false \
-Options.stage1.smoothing $smooth \
-noneFieldSmoothing 0 \
-outputFullPred \
-outputExampleFreq 1

#-lemmatiseAll \
#-dontOutputParams \
#-useStopNode \
#-Options.stage1.useVarUpdates

#-posAtSurfaceLevel \
#-inputPosTagged

#-modelUnkWord \

#-useGoldStandardOnly
#-excludedEventTypes airline airport booking_class city entity fare_basis_code location transport
#-excludedFields flight.stop

cd ${CUR_DIR}