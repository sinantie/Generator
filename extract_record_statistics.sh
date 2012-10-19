#!/bin/bash

#genDevListPathsGabor, trainListPathsGabor, genEvalListPathsGabor
#inputLists=gaborLists/genDevListPathsGabor
#data/weatherGov/weatherGovGenDevGaborRecordTreebank.gz, weatherGovTrainGaborRecordTreebank.gz, weatherGovGenEvalGaborRecordTreebank
input=data/weatherGov/weatherGovGenEvalGaborRecordTreebank.gz
#stagedParamsFile=results/output/weatherGov/alignments/pos/model_3_cond_null_POS_CDNumbers/stage1.params.obj.gz
stagedParamsFile=results/output/weatherGov/alignments/model_3_15_NO_STOP_NEW/stage1.params.obj.gz

execDir=data/weatherGov/treebanks/standardBinarization
java -Xmx4g -cp dist/Generator.jar:dist/lib/Helper.jar:dist/lib/kylm.jar:dist/lib/meteor.jar:dist/lib/tercom.jar:dist/lib/srilmWrapper:\
dist/stanford-postagger-2010-05-26.jar \
-Djava.library.path=lib/wrappers induction.utils.ExtractRecordsStatisticsExecutor \
-exportType recordType \
-create \
-overwriteExecDir \
-examplesInSingleFile \
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
-useEventTypeNames

#-modifiedBinarization
#-useEventTypeNames
#-extractRecordTrees
#-countRepeatedRecords
#-countSentenceNgrams
#-countDocumentNgrams
#-writePermutations
#-delimitSentences
