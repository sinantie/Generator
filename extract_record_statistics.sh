#!/bin/bash

#genDevListPathsGabor, trainListPathsGabor, genEvalListPathsGabor
input=gaborLists/genEvalListPathsGabor
#data/weatherGov/weatherGovGenDevGaborRecordTreebank.gz, weatherGovTrainGaborRecordTreebank.gz, weatherGovGenEvalGaborRecordTreebank
#input=data/weatherGov/weatherGovGenDevGaborRecordTreebank.gz
#stagedParamsFile=results/output/weatherGov/alignments/pos/model_3_cond_null_POS_CDNumbers/stage1.params.obj.gz
stagedParamsFile=results/output/weatherGov/alignments/model_3_gabor/1.exec/stage1.params.obj

execDir=data/weatherGov/treebanks
java -Xmx1g -cp dist/Generator.jar:dist/lib/Helper.jar:dist/lib/kylm.jar:dist/lib/meteor.jar:dist/lib/tercom.jar:dist/lib/srilmWrapper:\
dist/stanford-postagger-2010-05-26.jar \
-Djava.library.path=lib/wrappers induction.utils.ExtractRecordsStatisticsExecutor \
-exportType recordType \
-create \
-overwriteExecDir \
-modelType event3 \
-inputFileExt events \
-outputExampleFreq 500 \
-inputLists ${input} \
-execDir ${execDir} \
-stagedParamsFile ${stagedParamsFile} \
-extractNoneEvent \
-delimitSentences \
-extractRecordTrees \
-binarize right \
-useEventTypeNames \
-markovOrder 0 \
-modifiedBinarization

#-examplesInSingleFile \

#-modifiedBinarization
#-useEventTypeNames
#-extractRecordTrees
#-countRepeatedRecords
#-countSentenceNgrams
#-countDocumentNgrams
#-writePermutations
#-delimitSentences
