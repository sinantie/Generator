#!/bin/bash
java -jar -Xmx3800m -Xms3800m -XX:+AggressiveHeap dist/ContentSelectionJava.jar -create -modeltype event3 -inputPaths data/$1 -execPoolDir $2 -Options.stage1.numIters $3 -inputFileExt events -numThreads $4 -indepEventTypes 0,-1 -indepFields 0,-1 -new EventTypeFieldPerWord 0,5 -newFieldPerWord 0,5 -disallowConsecutiveRepeatFields -dontCrossPunctuation -Options.stage1.smoothing 0.1
