#!/bin/bash

# Discriminative version
format=lowjet
port=4445
kBest=40
stagedParamsFile=results/output/atis/alignments/model_3/prior_0.01/stage1.params.obj
#stagedParamsFile=results/output/atis/generation/discriminative/baseline_ignore_ngrams_bigrams_hasCons_oracle_seg5/stage1.discriminative.params.obj.gz
#stagedParamsFile=results/output/atis/generation/discriminative/baseline_ignore_ngrams_bigrams_hasCons_hasConsBi_hasConsTri_seg5/stage1.discriminative.params.obj.gz
baselineModel=results/output/atis/alignments/model_3/prior_0.01/stage1.params.obj
numThreads=1


java -Xmx2g -cp dist/Generator.jar:dist/lib/Helper.jar:dist/lib/kylm.jar:dist/lib/meteor.jar:dist/lib/tercom.jar:dist/lib/srilmWrapper:\
dist/stanford-postagger-2010-05-26.jar \
-Djava.library.path=lib/wrappers induction.runtime.server.MultiServer \
-initType staged \
-numThreads ${numThreads} \
-jsonFormat ${format} \
-port ${port} \
-modelType generate \
-examplesInSingleFile \
-inputFileExt events \
-generativeModelParamsFile ${baselineModel} \
-stagedParamsFile ${stagedParamsFile} \
-disallowConsecutiveRepeatFields \
-kBest ${kBest} \
-ngramModelFile atisLM/atis-all-train-3-gram.model.arpa \
-ngramWrapper srilm \
-reorderType eventType \
-allowConsecutiveEvents \
-maxPhraseLength 5 \
-binariseAtWordLevel \
-ngramSize 3 \
-lengthPredictionModelFile data/atis/train/lengthPrediction.counts.linear-reg.model \
-lengthPredictionFeatureType COUNTS \
-lengthPredictionStartIndex 2 \
-useStopNode \
-includeBigrams \
-includeHasConsecutiveWordsFeature


##-includeHasEmptyValuePerFieldFeature
##-includeHasEmptyValueFeature
##-includeFieldNgramsPerEventTypeFeature
##-includeHasConsecutiveBigramsFeature \
##-includeHasConsecutiveTrigramsFeature
##-includeHasEmptyValueFeature
