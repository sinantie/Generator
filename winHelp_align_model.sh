#!/bin/bash

input=$1
output=$2
numIters=$3
numThreads=$4
java -Xmx1800m -cp dist/Generator.jar:dist/lib/Helper.jar:dist/lib/kylm.jar:dist/lib/meteor.jar:dist/lib/tercom.jar:\dist/lib/srilmWrapper:\
dist/stanford-postagger-2010-05-26.jar induction.runtime.Induction \
-create \
-modeltype event3 \
-inputLists ${input} \
-examplesInSingleFile \
-execDir ${output} \
-Options.stage1.numIters ${numIters} \
-inputFileExt events \
-numThreads ${numThreads} \
-initType random \
-disallowConsecutiveRepeatFields \
-dontCrossPunctuation \
-initNoise 0 \
-Options.stage1.smoothing 0.01 \
-fixedGenericProb 0 \
-indepEventTypes 0,10 \
-indepFields 0,5 \
-newEventTypeFieldPerWord 0,5 \
-newFieldPerWord 0,5 \
-indepWords 0,5 \
-useStopNode \
-allowNoneEvent \
-outputFullPred \
-posAtSurfaceLevel \
-inputPosTagged

#-allowConsecutiveEvents \
#-allowNoneEvent \
#-modelUnkWord \
