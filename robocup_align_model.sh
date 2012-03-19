#!/bin/bash

inputLists=$1
output=$2
numIters=$3
numThreads=$4
java -Xmx1800m -cp dist/Generator.jar:dist/lib/kylm.jar:dist/lib/meteor.jar:dist/lib/tercom.jar:dist/lib/srilmWrapper:dist/stanford-postagger-2010-05-26.jar induction.runtime.Induction \
-create \
-modeltype event3 \
-inputLists ${inputLists} \
-execDir ${output} \
-Options.stage1.numIters ${numIters} \
-inputFileExt events \
-numThreads ${numThreads} \
-disallowConsecutiveRepeatFields \
-initNoise 0 \
-Options.stage1.smoothing 0.1 \
-outputFullPred \
-modelUnkWord \
-fixedGenericProb 0 \
-oneEventPerExample 0,-1 \
-binariseAtWordLevel \
-allowConsecutiveEvents \
-indepWords 0,5 \
-allowNoneEvent
