#!/bin/bash
threads=2
#gaborLists/genDevListPathsGabor, trainListPathsGabor, genEvalListPathsGabor, test/testWeatherGovEvents
#input=data/branavan/winHelpHLA/winHelpRL.sents.all
input=$1
memory=-Xmx500m
#posDictionary=gaborLists/trainListPathsGabor_vocabulary_manual
#posDictionary=data/branavan/winHelpHLA/winHelpRL.sents.all.vocabulary

java $memory -cp dist/Generator.jar:dist/lib/Helper.jar:dist/stanford-postagger-2010-05-26.jar -ea induction.utils.postagger.PosTaggerExecutor \
-inputPath ${input} \
-typeOfPath file \
-typeOfInput events \
-outputExampleFreq 100 \
-tagDelimiter "_"
#-forceTagger
#-posDictionaryPath ${posDictionary} \
#-extension text \
#-forceTagger 
#-replaceNumbers \
