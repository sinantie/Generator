#!/bin/bash


inputLists=$1
execDir=$2
numIters=$3
numThreads=$4
# mrg, events
ext=events
# mrg, raw
format=raw

java -Xmx3000m -ea -Djava.library.path=lib/wrappers -cp dist/Generator.jar:dist/lib/Helper.jar:dist/lib/kylm.jar:dist/lib/meteor.jar:\
dist/lib/tercom.jar:dist/lib/srilmWrapper:\
dist/stanford-postagger-2010-05-26.jar induction.runtime.Induction \
-modelType dmv \
-Options.stage1.numIters ${numIters} \
-numThreads ${numThreads} \
-inputLists ${inputLists} \
-create \
-execDir ${execDir} \
-inputFileExt ${ext} \
-inputFormat ${format} \
-initType uniformz \
-outputFullPred \
-posAtSurfaceLevel \
-useTagsAsWords \
-inputPosTagged \
-initSmoothing 0.01 \
-initNoise 1e-3

#-Options.stage1.smoothing 0.001 \
#-Options.stage1.useVarUpdates \


