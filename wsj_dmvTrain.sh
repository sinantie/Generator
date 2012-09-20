#!/bin/bash

numIters=100
numThreads=2
inputLists=../wsj/3.0/parsed/mrg/wsj
#inputLists=../wsj/3.0/conll/wsj-10-noP.deps
#inputLists=../childes/english/english_childes_train.unlabelled
#execDir=results/output/wsj/dmv/train/wsj_conll_POS/childes_uniformZ_forceOrder_${numIters}
execDir=results/output/wsj/dmv/train/wsj_mrg_POS/uniformZ_initNoise001_noStarts_viterbiEM_100
# mrg, events, deps
ext=mrg
# mrg, raw, conll
format=mrg

java -Xmx3500m -ea -cp dist/Generator.jar:dist/lib/Helper.jar induction.runtime.Induction \
-modelType dmv \
-Options.stage1.numIters ${numIters} \
-numThreads ${numThreads} \
-inputPaths ${inputLists} \
-inputFileExt ${ext} \
-inputFormat ${format} \
-create \
-execDir ${execDir} \
-outputFullPred \
-outputExampleFreq 1000 \
-initType uniformz \
-initSmoothing 0.01 \
-initNoise 1e-3 \
-useTagsAsWords \
-forceOutputOrder \
-maxExampleLength 10 \
-removePunctuation \
-inputPosTagged \
-Options.stage1.hardUpdate

#-dontOutputParams \
#-connlTagPos 5 \
#-connlHeadPos 7 \
#-examplesInSingleFile \

#-inputPosTagged \
#-maxExampleLength 10 \
#-removePunctuation \
#-inputPaths ${inputLists} \

#-outputFullPred \
#-Options.stage1.useVarUpdates \
#-Options.stage1.smoothing 0.0001 \

#-posAtSurfaceLevel

