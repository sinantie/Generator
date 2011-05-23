#!/bin/bash
java -Xmx4g -cp dist/Generator.jar:dist/lib/Helper.jar:dist/lib/kylm.jar:dist/lib/meteor.jar:dist/lib/tercom.jar:dist/lib/srilmWrapper:dist/stanford-postagger-2010-05-26.jar \
-Djava.library.path=lib/wrappers induction.Generation -outputFullPred -create -modelType generate \
-testInputLists $1 -inputFileExt events -execDir $2 -stagedParamsFile ${3}/stage1.params.obj \
-disallowConsecutiveRepeatFields -kBest $4 -ngramModelFile $5 -ngramWrapper $6 -outputExampleFreq 100 \
-allowConsecutiveEvents -reorderType eventType -allowNoneEvent -maxPhraseLength 5
