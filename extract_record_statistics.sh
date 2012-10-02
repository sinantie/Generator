#!/bin/bash

#genDevListPathsGabor, trainListPathsGabor, genEvalListPathsGabor
inputLists=gaborLists/trainListPathsGabor

#stagedParamsFile=results/output/weatherGov/alignments/pos/model_3_cond_null_POS_CDNumbers/stage1.params.obj.gz
stagedParamsFile=results/output/weatherGov/alignments/model_3_15_NO_STOP_NEW/stage1.params.obj.gz

execDir=weatherGovLM/recordStatistics
java -Xmx4g -cp dist/Generator.jar:dist/lib/Helper.jar:dist/lib/kylm.jar:dist/lib/meteor.jar:dist/lib/tercom.jar:dist/lib/srilmWrapper:\
dist/stanford-postagger-2010-05-26.jar \
-Djava.library.path=lib/wrappers induction.utils.ExtractRecordsStatisticsExecutor \
-exportType recordType \
-create \
-overwriteExecDir \
-modelType event3 \
-inputFileExt events \
-ngramWrapper srilm \
-outputExampleFreq 500 \
-inputLists ${inputLists} \
-execDir ${execDir} \
-stagedParamsFile ${stagedParamsFile} \
-extractNoneEvent \
-delimitSentences \
-writePermutations \
-countSentenceNgrams \
-countDocumentNgrams

#-countRepeatedRecords
#-countSentenceNgrams
#-countDocumentNgrams
#-writePermutations
#-delimitSentences
