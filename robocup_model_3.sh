#!/bin/bash
java -Xmx1800m -cp dist/ContentSelectionJava.jar:dist/lib/kylm.jar:dist/lib/meteor.jar:dist/lib/tercom.jar:dist/lib/srilmWrapper:dist/stanford-postagger-2010-05-26.jar induction.Induction -create -modeltype event3 -inputLists $1 -execDir $2 -Options.stage1.numIters $3 -inputFileExt events -numThreads $4 -disallowConsecutiveRepeatFields -initNoise 0 -Options.stage1.smoothing 0.1 $5 $6
