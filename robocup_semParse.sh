#!/bin/bash
java -Xmx1800m -cp dist/Generator.jar:dist/lib/Helper.jar:dist/lib/kylm.jar:dist/lib/meteor.jar:dist/lib/tercom.jar:dist/lib/srilmWrapper.jar \
-Djava.library.path=lib/wrappers induction.Generation -outputFullPred -create -modelType semParse \
-testInputLists $1 -inputFileExt events -execDir $2 -stagedParamsFile ${3}/stage1.params.obj \
-disallowConsecutiveRepeatFields -kBest $4 -ngramModelFile $5 -ngramWrapper srilm \
-outputExampleFreq 100 -ngramSize 2 -reorderType eventTypeAndField -modelUnkWord -allowConsecutiveEvents \
-maxPhraseLength 5 -newFieldPerWord 0,-1 -oneEventPerExample 0,-1 -excludeLists robocupLists/robocupAllUnreachable
