#!/bin/bash
threads=2
#genDevListPathsGabor, trainListPathsGabor, genEvalListPathsGabor
input=gaborLists/genDevListPathsGabor
output=results/output/weatherGov/alignments/dev/model_3_gabor_cond_null_bigrams_viterbiEM_uniformz
memory=-Xmx2g
java $memory -cp dist/Generator.jar:dist/lib/Helper.jar:dist/lib/kylm.jar:dist/lib/meteor.jar:dist/lib/tercom.jar:\dist/lib/srilmWrapper:\
dist/stanford-postagger-2010-05-26.jar -ea -Djava.library.path=lib/wrappers induction.runtime.Induction \
-create \
-modeltype event3 \
-inputLists $input \
-execDir $output \
-Options.stage1.numIters 15 \
-inputFileExt events \
-numThreads $threads \
-indepEventTypes 0,10 \
-indepFields 0,5 \
-indepWords 0,5 \
-newEventTypeFieldPerWord 0,5 \
-newFieldPerWord 0,5 \
-disallowConsecutiveRepeatFields \
-dontCrossPunctuation \
-Options.stage1.smoothing 0.1 \
-noneFieldSmoothing 0 \
-outputFullPred \
-modelUnkWord \
-outputExampleFreq 500 \
-conditionNoneEvent \
-allowNoneEvent \
-initType uniformz \
-initNoise 0 \
-initSmoothing 0.01 \
-initNoise 1e-3 \
-Options.stage1.hardUpdate

#-inputPosTagged


#-useStopNode \
#-useGoldStandardOnly
#-excludedEventTypes airline airport booking_class city entity fare_basis_code location transport
#-excludedFields flight.stop
#-excludedFields temperature.time windChill.time windSpeed.time windDir.time gust.time precipPotential.time \
#thunderChance.time snowChance.time freezingRainChance.time sleetChance.time 
