#!/bin/bash

#genDevListPathsGabor, trainListPathsGabor, genEvalListPathsGabor
#input=gaborLists/trainListPathsGabor
input=data/weatherGov/weatherGovTrainGaborRecordTreebankUnaryRules.gz
#data/weatherGov/weatherGovGenDevGaborRecordTreebank.gz, weatherGovTrainGaborRecordTreebank.gz, weatherGovGenEvalGaborRecordTreebank
#input=data/weatherGov/weatherGovGenDevGaborRecordTreebank.gz
#stagedParamsFile=results/output/weatherGov/alignments/pos/model_3_cond_null_POS_CDNumbers/stage1.params.obj.gz
stagedParamsFile=results/output/weatherGov/alignments/model_3_gabor/1.exec/stage1.params.obj
predInput=results/output/weatherGov/alignments/model_3_gabor_cond_null_bigrams_correct/1.exec/stage1.train.pred.14.sorted

execDir=data/weatherGov/treebanks
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
-writePermutations \
-useEventTypeNames \
-exportEvent3 \
-extractRecordTrees \
-predInput ${predInput}

#-examplesInSingleFile \

#-modifiedBinarization
#-useEventTypeNames
#-extractRecordTrees
#-countRepeatedRecords
#-countSentenceNgrams
#-countDocumentNgrams
#-writePermutations
#-delimitSentences
