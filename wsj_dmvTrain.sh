#!/bin/bash

numIters=5
numThreads=2
#inputLists=../wsj/3.0/parsed/mrg/wsj
#inputLists=../wsj/3.0/conll/wsj-10-noP.deps
inputLists=../childes/english/english_childes_train.unlabelled
execDir=results/output/wsj/dmv/train/wsj_conll_POS/childes_uniformZ_${numIters}
# mrg, events, deps
ext=deps
# mrg, raw, conll
format=conll

java -Xmx3500m -ea -cp dist/Generator.jar:dist/lib/Helper.jar induction.runtime.Induction \
-modelType dmv \
-Options.stage1.numIters ${numIters} \
-numThreads ${numThreads} \
-inputLists ${inputLists} \
-inputFileExt ${ext} \
-inputFormat ${format} \
-create \
-execDir ${execDir} \
-dontOutputParams \
-outputFullPred \
-outputExampleFreq 1000 \
-examplesInSingleFile \
-initType uniformz \
-initSmoothing 0.01 \
-initNoise 1e-3 \
-useTagsAsWords \
-connlTagPos 5 \
-connlHeadPos 7

#-inputPosTagged \
#-maxExampleLength 10 \
#-removePunctuation \
#-inputPaths ${inputLists} \

#-outputFullPred \
#-Options.stage1.useVarUpdates \
#-Options.stage1.smoothing 0.0001 \

#-posAtSurfaceLevel

