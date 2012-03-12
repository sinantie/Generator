#!/bin/bash
threads=2
#gaborLists/genDevListPathsGabor, trainListPathsGabor, genEvalListPathsGabor, test/testWeatherGovEvents
input=gaborLists/trainListPathsGabor
memory=-Xmx500m
posDictionary=gaborLists/trainListPathsGabor_vocabulary_manual

java $memory -cp dist/Generator.jar:dist/lib/Helper.jar:dist/stanford-postagger-2010-05-26.jar -ea induction.utils.PosTaggerExecutor \
-inputPath ${input} \
-typeOfPath list \
-typeOfInput raw \
-posDictionaryPath ${posDictionary} \
-extension text \
-replaceNumbers \
-forceTagger
