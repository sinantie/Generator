#!/bin/bash
java -Xmx1800m -cp dist/Generator.jar:dist/lib/kylm.jar:dist/lib/meteor.jar:dist/lib/tercom.jar:dist/lib/srilmWrapper.jar \
-Djava.library.path=lib/wrappers induction.Generation -outputFullPred -create -modelType semParse \
-testInputLists $1 -inputFileExt events -execDir $2 -stagedParamsFile ${3}/stage1.params.obj \
-disallowConsecutiveRepeatFields -kBest $4 -ngramModelFile $5 -ngramWrapper srilm \
-outputExampleFreq 100 -ngramSize 3 -reorderType eventTypeAndField -modelUnkWord \
-maxPhraseLength 5 -newFieldPerWord 0,-1 -allowNoneEvent
#-excludedFields skyCover.time temperature.time windChill.time windSpeed.time windDir.time gust.time precipPotential.time \
# thunderChance.time snowChance.time freezingRainChance.time sleetChance.time
