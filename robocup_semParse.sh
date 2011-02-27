#!/bin/bash
java -Xmx1800m -cp dist/Generator.jar:dist/lib/kylm.jar:dist/lib/meteor.jar:dist/lib/tercom.jar:dist/lib/srilmWrapper.jar \
-Djava.library.path=lib/wrappers induction.Generation -outputFullPred -create -modelType semParse \
-testInputLists $1 -inputFileExt events -execPoolDir $2 -stagedParamsFile ${3}/stage1.params.obj \
-disallowConsecutiveRepeatFields -kBest $4 -ngramModelFile $5 -ngramWrapper kylm \
-outputExampleFreq 100 -ngramSize 2 -reorderType eventType \
-maxPhraseLength 5 -useGoldStandardOnly -newFieldPerWord 0,-1
