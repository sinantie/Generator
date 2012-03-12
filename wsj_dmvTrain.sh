#!/bin/bash

inputLists=../wsj/3.0/parsed/mrg/wsj
execDir=results/output/wsj/dmv/train/wsj_mrg_POS_50
numIters=50
numThreads=2
# mrg, events
ext=mrg
# mrg, raw
format=mrg

java -Xmx3500m -ea -Djava.library.path=lib/wrappers -cp dist/Generator.jar:dist/lib/Helper.jar:dist/lib/kylm.jar:dist/lib/meteor.jar:\
dist/lib/tercom.jar:dist/lib/srilmWrapper:\
dist/stanford-postagger-2010-05-26.jar induction.runtime.Induction \
-modelType dmv \
-Options.stage1.numIters ${numIters} \
-numThreads ${numThreads} \
-inputPaths ${inputLists} \
-create \
-execPoolDir ${execDir} \
-inputFileExt ${ext} \
-inputFormat ${format} \
-initType bait \
-useTagsAsWords \
-maxExampleLength 10 \
-removePunctuation

#-outputFullPred \
#-Options.stage1.useVarUpdates \
#-Options.stage1.smoothing 0.0001 \

#-posAtSurfaceLevel

