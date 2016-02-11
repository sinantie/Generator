#!/bin/bash

threads=2
input=../hackathon/data/ldc/split/training/training-thres-5-catFields.event3
output=results/output/amr/ldc/alignments/bootstrap_word_estimates/ingoreFields-catFields-noSmooth
sentencesFile=../hackathon/data/ldc/split/training/training-sentences.s.tok.lc
GHKMTreeFile=../hackathon/data/ldc/split/training/training-sentences.t-tree-clean
alignmentsFile=../hackathon/data/ldc/split/training/training-sentences.align.t-s

smooth=0
java -Xmx16000m -cp dist/Generator.jar:dist/lib/Helper.jar:dist/lib/kylm.jar:dist/lib/meteor.jar:dist/lib/tercom.jar:\dist/lib/srilmWrapper:\
dist/lib/stanford-corenlp-3.5.1.jar:dist/lib/stanford-corenlp-3.5.1-models.jar: induction.utils.ComputeAmrEstimatesExecutor \
-create \
-modeltype event3 \
-examplesInSingleFile \
-inputLists $input \
-execDir $output \
-overwriteExecDir \
-numThreads $threads \
-ComputeAmrEstimatesOptions.modelOpts.stage1.smoothing $smooth \
-sentencesFile $sentencesFile \
-GHKMTreeFile $GHKMTreeFile \
-alignmentsFile $alignmentsFile \
-useStringLabels false \
-stripConceptSense \
-indepWords 0,-1 \
-dontOutputParams

#-artNumWords 100 \
#-dontOutputParams \

