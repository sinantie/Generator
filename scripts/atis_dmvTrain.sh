#!/bin/bash

#inputLists=../wsj/3.0/parsed/mrg/atis/atis3_clean_pos_cut.mrg
inputLists=data/atis/train/atis5000.sents.full.tagged.CDnumbers
execDir=results/output/atis/dmv/train/atis_raw5000_full_indexers_uniformZ_initNoise_POS_100
numIters=100
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
-create \
-execDir ${execDir} \
-inputFileExt ${ext} \
-inputFormat ${format} \
-initType uniformz \
-outputFullPred \
-posAtSurfaceLevel \
-inputPosTagged \
-useTagsAsWords \
-initSmoothing 0.01 \
-initNoise 1e-3

#-Options.stage1.smoothing 0.001 \
#-Options.stage1.useVarUpdates \


