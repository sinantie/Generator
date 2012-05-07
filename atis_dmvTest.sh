#!/bin/bash

#inputLists=../wsj/3.0/parsed/mrg/atis/atis3_clean_pos_cut.mrg
inputLists=data/atis/test/atis-test.txt
execDir=results/output/atis/dmv/test/atis_LEX_full_indexers_100
numThreads=2
stagedParamsFile=results/output/atis/dmv/train/atis_raw5000_full_indexers_LEX_100/stage1.dmv.params.obj.gz
# mrg, events
ext=events
# mrg, raw
format=raw


java -Xmx1000m -ea -Djava.library.path=lib/wrappers -cp dist/Generator.jar:dist/lib/Helper.jar:dist/lib/kylm.jar:dist/lib/meteor.jar:\
dist/lib/tercom.jar:dist/lib/srilmWrapper:\
dist/stanford-postagger-2010-05-26.jar induction.runtime.Generation \
-modelType dmv \
-numThreads ${numThreads} \
-stagedParamsFile ${stagedParamsFile} \
-examplesInSingleFile \
-testInputLists ${inputLists} \
-create \
-execDir ${execDir} \
-inputFileExt ${ext} \
-inputFormat ${format} \
-outputFullPred \
-dontOutputParams
#-useTagsAsWords \
#-posAtSurfaceLevel

#-execDir ${execDir} \
