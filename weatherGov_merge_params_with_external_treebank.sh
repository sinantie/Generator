#!/bin/bash

exec=merge_params_with_external_treebank.sh
input=data/weatherGov/weatherGovTrainGabor.gz
#stagedParamsFile=results/output/weatherGov/alignments/model_3_gabor_cond_null_correct/2.exec/stage1.params.obj
stagedParamsFile=results/output/weatherGov/alignments/model_3_gabor_no_sleet_windChill_15iter/stage1.params.obj.gz
externalTreebankFile=data/weatherGov/treebanks/recordTreebankTrainRightBinarizeNoSleetWindChillFilteredAligments
treebankRules=data/weatherGov/treebanks/recordTreebankRulesTrainRightBinarizeNoSleetWindChillFilteredAligments
execDir=results/output/weatherGov/alignments/pcfg/model_3_gabor_record_pcfg_treebank_alignments_no_windChill_sleet_externalTreebank
maxDocLength=90
docLengthBinSize=5

mkdir -p $execDir
./${exec} ${input} ${stagedParamsFile} ${externalTreebankFile} ${treebankRules} ${execDir} ${maxDocLength} ${docLengthBinSize}
