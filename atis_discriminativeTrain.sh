#!/bin/bash

inputLists=data/atis/train/atis5000.sents.full
execDir=results/output/atis/generation/discriminative/calculate_baseline_weight_norm
numIters=15
numThreads=2
baselineModel=results/output/atis/alignments/model_3/prior_0.01/stage1.params.obj
#baselineModel=results/output/atis/alignments/model_3/15_iter_no_null_smooth_0001_STOP/stage1.params.obj

java -Xmx6g -ea -cp dist/Generator.jar:dist/lib/Helper.jar:dist/lib/kylm.jar:dist/lib/meteor.jar:dist/lib/tercom.jar:\dist/lib/srilmWrapper:\
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
-outputExampleFreq 100 \
-ngramModelFile atisLM/atis-all-train-3-gram.model.arpa \
-ngramWrapper srilm \
-allowConsecutiveEvents \
-reorderType eventType \
-maxPhraseLength 5 \
-binariseAtWordLevel
#-allowNoneEvent \
#-conditionNoneEvent
