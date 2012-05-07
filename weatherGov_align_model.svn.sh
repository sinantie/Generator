#!/bin/bash
threads=2
#genDevListPathsGabor, trainListPathsGabor, genEvalListPathsGabor
input=gaborLists/genDevListPathsGabor
output=results/output/weatherGov/alignments/dev/model_3_gabor_cond_null_bigrams_correct
memory=-Xmx2g
java $memory -cp dist/Generator.jar:dist/lib/Helper.jar:dist/lib/kylm.jar:dist/lib/meteor.jar:dist/lib/tercom.jar:\dist/lib/srilmWrapper:\
dist/stanford-postagger-2010-05-26.jar -ea -Djava.library.path=lib/wrappers induction.Induction \
-create \
-modeltype event3 \
-inputLists $input \
-execPoolDir $output \
-Options.stage1.numIters 15 \
-inputFileExt events \
-numThreads $threads \
-initNoise 0 \
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
-outputExampleFreq 10000 \
-conditionNoneEvent \
-allowNoneEvent
#-useGoldStandardOnly
#-excludedEventTypes airline airport booking_class city entity fare_basis_code location transport
#-excludedFields flight.stop
#-excludedFields temperature.time windChill.time windSpeed.time windDir.time gust.time precipPotential.time \
#thunderChance.time snowChance.time freezingRainChance.time sleetChance.time 
