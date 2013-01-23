#!/bin/bash
threads=2
#gaborLists/genDevListPathsGabor, trainListPathsGabor, genEvalListPathsGabor
#data/weatherGov/weatherGovGenDevGaborRecordTreebank.gz, weatherGovTrainGaborRecordTreebank.gz
input=data/weatherGov/weatherGovTrainGabor.gz
stagedParamsFile=/home/sinantie/EDI/Generator/results/output/weatherGov/alignments/model_3_gabor_split
output=results/output/weatherGov/alignments/model_3_gabor_staged
numIters=2
memory=-Xmx1800m


java $memory -cp dist/Generator.jar:dist/lib/Helper.jar:dist/lib/kylm.jar:dist/lib/meteor.jar:dist/lib/tercom.jar:\dist/lib/srilmWrapper:\
dist/stanford-postagger-2010-05-26.jar -ea -Djava.library.path=lib/wrappers induction.runtime.Induction \
-create \
-overwriteExecDir \
-modeltype event3 \
-examplesInSingleFile \
-inputLists $input \
-execDir $output \
-inputFileExt events \
-Options.stage1.numIters $numIters \
-numThreads $threads \
-dontCrossPunctuation \
-Options.stage1.smoothing 0.01 \
-disallowConsecutiveRepeatFields \
-noneFieldSmoothing 0 \
-outputFullPred \
-modelUnkWord \
-outputExampleFreq 1000 \
-initType staged \
-initNoise 0 \
-Options.stage1.smoothing 0.01 \
-fixedGenericProb 0 \
-conditionNoneEvent \
-allowNoneEvent \
-dontOutputParams \
-stagedParamsFile ${stagedParamsFile}/stage1.params.obj.gz \
-useStopNode

#-indepEventTypes 0,10 \
#-indepFields 0,5 \
#-indepWords 0,5 \
#-newEventTypeFieldPerWord 0,5 \
#-newFieldPerWord 0,5 \
