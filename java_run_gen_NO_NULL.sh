#!/bin/bash
java -Xmx6g -cp dist/ContentSelectionJava.jar:dist/lib/kylm.jar:dist/lib/meteor.jar:dist/lib/tercom.jar:dist/lib/srilmWrapper.jar \
-Djava.library.path=lib/wrappers induction.Generation -outputFullPred -create -modelType generate \
-testInputLists $1 -inputFileExt events -execPoolDir $2 -stagedParamsFile results/output/model_3_percy_NO_NULL/1.exec/stage1.params.obj \
-disallowConsecutiveRepeatFields -kBest $3 -ngramModelFile $4 -ngramWrapper $5 -outputExampleFreq 100 $6
