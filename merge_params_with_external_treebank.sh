#!/bin/bash

input=data/weatherGov/weatherGovTrainGabor.gz
stagedParamsFile=results/output/weatherGov/alignments/model_3_gabor_cond_null_correct/2.exec/stage1.params.obj
externalTreebankFile=data/weatherGov/treebanks/recordTreebankTrainRightBinarizeUnaryRulesFilteredAlignments
treebankRules=data/weatherGov/treebanks/recordTreebankRulesTrainRightBinarizeUnaryRulesFilteredAlignments                 
execDir=results/output/weatherGov/alignments/pcfg/model_3_gabor_record_pcfg_treebank_alignments_unaryRules_wordsPerRootRule_externalTreebank

java -Xmx2g -cp dist/Generator.jar:dist/lib/Helper.jar:dist/lib/kylm.jar:dist/lib/meteor.jar:dist/lib/tercom.jar:dist/lib/srilmWrapper:\
dist/stanford-postagger-2010-05-26.jar \
-Djava.library.path=lib/wrappers induction.utils.MergeParamsWithExternalTreebankExecutor \
-modelType event3 \
-create \
-overwriteExecDir \
-examplesInSingleFile \
-inputLists ${input} \
-execDir ${execDir} \
-stagedParamsFile ${stagedParamsFile} \
-externalTreebankFile ${externalTreebankFile} \
-treebankRules ${treebankRules}

#-dontOutputParams
