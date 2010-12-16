#!/bin/bash
java -Xmx1800m -Xms1800m -cp build/classes:dist/lib/kylm.jar:dist/lib/meteor.jar:dist/lib/tercom.jar:dist/lib/srilmWrapper.jar \
-Djava.library.path=lib/wrappers/srilm induction.Generation -outputFullPred -create -modelType generate \
-testInputPaths $1 -inputFileExt events -execPoolDir $2 -stagedParamsFile results/output/model_3_percy/1.exec/stage1.params.obj \
-disallowConsecutiveRepeatFields -kBest $3 -ngramModelFile $4 -allowNoneEvent -ngramWrapper $5 -outputExampleFreq 100
