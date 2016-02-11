#!/bin/bash

inputLists=data/atis/test/atis-test.txt
execDir=results/output/atis/alignments/test_staged/no_null_smooth_0001_STOP_sanity
numIters=1
numThreads=2
stagedFile=results/output/atis/alignments/model_3/15_iter_no_null_smooth_0001_STOP/stage1.params.obj
#stagedFile=results/output/atis/alignments/model_3/prior_0.01/stage1.params.obj

java -Xmx6g -ea -cp dist/Generator.jar:dist/lib/Helper.jar:dist/lib/kylm.jar:dist/lib/meteor.jar:dist/lib/tercom.jar:\dist/lib/srilmWrapper:\
dist/stanford-postagger-2010-05-26.jar induction.runtime.Induction \
-create \
-examplesInSingleFile \
-modeltype event3 \
-testInputLists ${inputLists} \
-execDir ${execDir} \
-Options.stage1.numIters ${numIters} \
-inputFileExt events \
-numThreads ${numThreads} \
-disallowConsecutiveRepeatFields \
-dontCrossPunctuation \
-stagedParamsFile $stagedFile \
-outputExampleFreq 100 \
-initType staged
#-initNoise 0
#-allowNoneEvent \
#-conditionNoneEvent
