#!/bin/bash

#inputLists=gaborLists/trainListPathsGabor
#inputLists=data/weatherGov/weatherGovTrainGaborRecordTreebankUnaryRules.gz
inputLists=data/weatherGov/induction/weatherGovTrainGaborPermutations_eventTypes
numIters=50
execDir=results/output/weatherGov/dmv/train/recordRules/weatherGov_recordRules_uniformZ_initNoise_${numIters}
numThreads=2
# mrg, events
ext=events
# mrg, raw
format=raw

java -Xmx1000m -ea -Djava.library.path=lib/wrappers -cp dist/Generator.jar:dist/lib/Helper.jar:dist/lib/kylm.jar:dist/lib/meteor.jar:\
dist/lib/tercom.jar:dist/lib/srilmWrapper:\
dist/stanford-postagger-2010-05-26.jar induction.runtime.Induction \
-modelType dmv \
-Options.stage1.numIters ${numIters} \
-numThreads ${numThreads} \
-examplesInSingleFile \
-inputLists ${inputLists} \
-overwriteExecDir \
-create \
-execDir ${execDir} \
-inputFileExt ${ext} \
-inputFormat ${format} \
-initType uniformz \
-outputFullPred \
-initSmoothing 0.01 \
-initNoise 1e-3

#-posAtSurfaceLevel \
#-useTagsAsWords \

#-Options.stage1.smoothing 0.001 \
#-Options.stage1.useVarUpdates \


