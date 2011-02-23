#!/bin/bash
java -Xmx1800m -cp dist/ContentSelectionJava.jar:dist/lib/kylm.jar:dist/lib/meteor.jar:dist/lib/tercom.jar:dist/lib/srilmWrapper.jar \
-Djava.library.path=lib/wrappers/srilm induction.Generation -outputFullPred -create -modelType generate \
-testInputLists $1 -inputFileExt events -execPoolDir $2 -stagedParamsFile ${3}/stage1.params.obj \
-disallowConsecutiveRepeatFields -kBest $4 -ngramModelFile $5 -ngramWrapper kylm \
-outputExampleFreq 100  -averageTextLength $6 -ngramSize $7 -allowConsecutiveEvents -useGoldStandardOnly
