#!/bin/bash

inputLists=../wsj/3.0/parsed/mrg/wsj
execDir=results/output/wsj/dmv/train/wsj_mrg_POS
numIters=100
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
-initType uniformz \
-useTagsAsWords \
-inputPosTagged \
-maxExampleLength 10 \
-removePunctuation \
-initSmoothing 0.01 \
-initNoise 1e-3

#-outputFullPred \
#-Options.stage1.useVarUpdates \
#-Options.stage1.smoothing 0.0001 \

#-posAtSurfaceLevel

