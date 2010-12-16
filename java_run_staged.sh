#!/bin/bash
java -cp build/classes -Xmx3800m -Xms3800m -XX:+AggressiveHeap induction.Induction -create -modeltype event3 -inputPaths data/$1 -execPoolDir $2 -Options.stage1.numIters 0  -Options.stage2.numIters $3 -inputFileExt events -numThreads $4 -paramsDir results/output/model_3_percy/0.exec -dontCrossPunctuation -disallowConsecutiveRepeatFields -outputFullPred
