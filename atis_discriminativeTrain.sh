#!/bin/bash

inputLists=data/atis/train/atis5000.sents.full
#inputLists=test/trainAtisExamples
execDir=results/output/atis/generation/discriminative/calculate_baseline_ngrams_negative_staged_ignore
numIters=15
numThreads=1
baselineModel=results/output/atis/alignments/model_3/prior_0.01/stage1.params.obj
stagedModel=results/output/atis/generation/discriminative/calculate_baseline_weight_norm/stage1.discriminative.params.obj
#baselineModel=results/output/atis/alignments/model_3/15_iter_no_null_smooth_0001_STOP/stage1.params.obj

java -Xmx3g -ea -Djava.library.path=lib/wrappers -cp dist/Generator.jar:dist/lib/Helper.jar:dist/lib/kylm.jar:dist/lib/meteor.jar:dist/lib/tercom.jar:\dist/lib/srilmWrapper:\
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
-dontCrossPunctuation \
-generativeModelParamsFile ${baselineModel} \
-stagedParamsFile ${stagedModel} \
-outputExampleFreq 100 \
-ngramModelFile atisLM/atis-all-train-3-gram.model.arpa \
-ngramWrapper srilm \
-allowConsecutiveEvents \
-reorderType ignore \
-maxPhraseLength 5 \
-binariseAtWordLevel \
-kBest 40 \
-initType staged
#-allowNoneEvent \
#-conditionNoneEvent
