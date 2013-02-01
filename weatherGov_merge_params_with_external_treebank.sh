#!/bin/bash

exec=merge_params_with_external_treebank.sh
input=data/weatherGov/weatherGovTrainGabor.gz
stagedParamsFile=results/output/weatherGov/alignments/model_3_gabor_cond_null_bigrams_correct/1.exec/stage1.params.obj
#stagedParamsFile=results/output/weatherGov/alignments/model_3_gabor_staged_iter3/stage1.params.obj.gz
externalTreebankFile=data/weatherGov/treebanks/recordTreebankTrainRightBinarizeUnaryRules
treebankRules=data/weatherGov/treebanks/recordTreebankRulesTrainRightBinarizeUnaryRules
execDir=results/output/weatherGov/alignments/pcfg/model_3_gabor_record_pcfg_treebank_gold_externalTreebank
maxDocLength=90
docLengthBinSize=5
initSmoothing=0.0001
mkdir -p $execDir
./${exec} ${input} ${stagedParamsFile} ${externalTreebankFile} ${treebankRules} ${execDir} ${maxDocLength} ${docLengthBinSize} ${initSmoothing}
