#!/bin/bash

exec=merge_params_with_external_treebank.sh
input=data/weatherGov/weatherGovTrainGabor.gz
stagedParamsFile=results/output/weatherGov/alignments/model_3_gabor_cond_null_correct/2.exec/stage1.params.obj
externalTreebankFile=data/weatherGov/treebanks/recordTreebankTrainRightBinarizeUnaryRulesFilteredAlignments
treebankRules=data/weatherGov/treebanks/recordTreebankRulesTrainRightBinarizeUnaryRulesFilteredAlignments                 
execDir=results/output/weatherGov/alignments/pcfg/model_3_gabor_record_pcfg_treebank_alignments_unaryRules_wordsPerRootRule_externalTreebank
maxDocLength=90
docLengthBinSize=5

mkdir -p $output
./${exec} ${input} ${stagedParamsFile} ${externalTreebankFile} ${treebankRules} ${execDir} ${maxDocLength} ${docLengthBinSize}
