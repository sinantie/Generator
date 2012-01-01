#!/bin/bash

inputLists=data/atis/train/atis5000.sents.full
#inputLists=test/trainAtisExamples
execDir=results/output/atis/generation/discriminative/baseline_ignore_ngrams_bigrams_numWordsField_hasCons_numFieldsEvent_seg5
numIters=15
numThreads=1
baselineModel=results/output/atis/alignments/model_3/prior_0.01/stage1.params.obj
stagedModel=results/output/atis/generation/discriminative/baseline_ignore/stage1.discriminative.params.obj.gz
#baselineModel=results/output/atis/alignments/model_3/15_iter_no_null_smooth_0001_STOP/stage1.params.obj

java -Xmx2300m -ea -Djava.library.path=lib/wrappers -cp dist/Generator.jar:dist/lib/Helper.jar:dist/lib/kylm.jar:dist/lib/meteor.jar:dist/lib/tercom.jar:\dist/lib/srilmWrapper:\
dist/stanford-postagger-2010-05-26.jar induction.runtime.DiscriminativeInduction \
-create \
-examplesInSingleFile \
-modeltype discriminativeTrain \
-inputLists ${inputLists} \
-execDir ${execDir} \
-Options.stage1.numIters ${numIters} \
-Options.stage1.learningScheme incremental \
-inputFileExt events \
-numThreads ${numThreads} \
-disallowConsecutiveRepeatFields \
-generativeModelParamsFile ${baselineModel} \
-stagedParamsFile ${stagedModel} \
-outputExampleFreq 100 \
-ngramModelFile atisLM/atis-all-train-3-gram.model.arpa \
-ngramWrapper srilm \
-allowConsecutiveEvents \
-reorderType eventType \
-maxPhraseLength 5 \
-binariseAtWordLevel \
-kBest 40 \
-initType supervised \
-includeHasConsecutiveWordsFeature \
-includeBigramsFeature \
-includeNumFieldsPerEventTypeFeature
#-includeHasEmptyValuePerFieldFeature
#-includeHasConsecutiveTrigramsFeature
#-includeFieldNgramsPerEventTypeFeature
#-includeHasConsecutiveBigramsFeature \
#-includeHasConsecutiveTrigramsFeature
#-includeHasEmptyValueFeature
#-allowNoneEvent \
#-conditionNoneEvent
#-dontCrossPunctuation \ !!!!!!!!!!!!!!!!!!!!!!
