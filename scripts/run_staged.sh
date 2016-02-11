#!/bin/bash
 
input=data/atis/test/atis-test.txt
output=results/output/atis/alignments/test_staged/no_null_smooth_0001_STOP
stagedFile=results/output/atis/alignments/model_3/15_iter_no_null_smooth_0001_STOP/stage1.params.obj
threads=2

java -cp dist/Generator.jar:dist/lib/Helper.jar:dist/lib/kylm.jar:dist/lib/meteor.jar:dist/lib/tercom.jar:dist/lib/srilmWrapper:dist/stanford-postagger-2010-05-26.jar induction.Induction -create -modeltype event3 -testInputLists $input -Options.stage1.numIters 1 -execDir $output \
-inputFileExt events -numThreads $threads -initNoise 0 -initType staged -stagedParamsFile $stagedFile \
-disallowConsecutiveRepeatFields -dontCrossPunctuation -outputFullPred -modelUnkWord -examplesInSingleFile #-useGoldStandardOnly \
#-excludedFields skyCover.time temperature.time windChill.time windSpeed.time windDir.time gust.time precipPotential.time thunderChance.time \
#snowChance.time freezingRainChance.time sleetChance.time

#-create -modeltype event3 -testInputLists gaborLists/genEvalListPathsGabor -Options.stage1.numIters 1 -execPoolDir results/output/weatherGov/alignments/gold_staged -inputFileExt events -numThreads 2 -initNoise 0 -initType staged -stagedParamsFile results/output/weatherGov/alignments/model_3_gabor/1.exec/stage1.params.obj -disallowConsecutiveRepeatFields -dontCrossPunctuation -allowNoneEvent -outputFullPred -modelUnkWord -useGoldStandardOnly
