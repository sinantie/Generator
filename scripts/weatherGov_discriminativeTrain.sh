#!/bin/bash

inputLists=gaborLists/trainListPathsGabor
execPoolDir=results/output/weatherGov/generation/discriminative/baseline_ignore
numIters=15
numThreads=1
baselineModel=results/output/weatherGov/alignments/model_3_gabor_cond_null_correct/2.exec/stage1.params.obj


java -Xmx3g -ea -Djava.library.path=lib/wrappers -cp dist/Generator.jar:dist/lib/Helper.jar:dist/lib/kylm.jar:dist/lib/meteor.jar:dist/lib/tercom.jar:\dist/lib/srilmWrapper:\
dist/stanford-postagger-2010-05-26.jar induction.runtime.DiscriminativeInduction \
-create \
-modeltype discriminativeTrain \
-inputLists ${inputLists} \
-execDir ${execPoolDir} \
-Options.stage1.numIters ${numIters} \
-Options.stage1.learningScheme incremental \
-inputFileExt events \
-numThreads ${numThreads} \
-disallowConsecutiveRepeatFields \
-dontCrossPunctuation \
-generativeModelParamsFile ${baselineModel} \
-ngramModelFile weatherGovLM/gabor-srilm-abs-3-gram.model.arpa \
-ngramWrapper srilm \
-allowConsecutiveEvents \
-reorderType ignore \
-maxPhraseLength 5 \
-binariseAtWordLevel \
-outputExampleFreq 10000 \
-kBest 1 \
-allowNoneEvent \
-conditionNoneEvent
