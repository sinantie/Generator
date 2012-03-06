#!/bin/bash

threads=2
input=data/atis/train/atis5000.sents.full.tagged.CDnumbers
output=results/output/atis/alignments/model_3
smooth=0.01
java -Xmx3800m -cp dist/Generator.jar:dist/lib/Helper.jar:dist/lib/kylm.jar:dist/lib/meteor.jar:dist/lib/tercom.jar:\dist/lib/srilmWrapper:\
dist/stanford-postagger-2010-05-26.jar induction.runtime.Induction \
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
-indepFields 0,5 \
-indepWords 0,5 \
-newEventTypeFieldPerWord 0,5 \
-newFieldPerWord 0,5 \
-disallowConsecutiveRepeatFields \
-dontCrossPunctuation \
-Options.stage1.smoothing $smooth \
-noneFieldSmoothing 0 \
-outputFullPred \
-Options.stage1.useVarUpdates \
-posAtSurfaceLevel
#-modelUnkWord \

#-useGoldStandardOnly
#-excludedEventTypes airline airport booking_class city entity fare_basis_code location transport
#-excludedFields flight.stop

