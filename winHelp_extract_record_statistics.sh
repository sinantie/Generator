#!/bin/bash

input=../branavan/winHelpHLA/winHelpRL.cleaned.objType.docs.all
stagedParamsFile=results/output/winHelp/alignments/model_3_sents_no_null_cleaned_objType/all/stage1.params.obj.gz
#predInput=results/output/weatherGov/alignments/model_3_gabor_cond_null_bigrams_correct/1.exec/stage1.train.pred.14.sorted

execDir=statistics/weatherGov/recordStatistics/aligned
java -Xmx1g -cp dist/Generator.jar:dist/lib/Helper.jar:dist/lib/kylm.jar:dist/lib/meteor.jar:dist/lib/tercom.jar:dist/lib/srilmWrapper:\
dist/stanford-postagger-2010-05-26.jar \
-Djava.library.path=lib/wrappers induction.utils.ExtractRecordsStatisticsExecutor \
-exportType recordType \
-create \
-overwriteExecDir \
-modelType event3 \
-inputFileExt events \
-examplesInSingleFile \
-outputExampleFreq 500 \
-inputLists ${input} \
-execDir ${execDir} \
-stagedParamsFile ${stagedParamsFile} \
-extractNoneEvent \
-binarize right \
-markovOrder 0 \
-modifiedBinarization \
-delimitSentences \
-useEventTypeNames \
-extractRecordTrees

#-predInput ${predInput} \
#-ruleCountThreshold 5
#-examplesInSingleFile \
#-exportEvent3 \
#-modifiedBinarization
#-useEventTypeNames
#-extractRecordTrees
#-countRepeatedRecords
#-countSentenceNgrams
#-countDocumentNgrams
#-writePermutations
#-delimitSentences
