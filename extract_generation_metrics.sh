# USE in GNU Octave: 
# data = dlmread("file"); 
# wilcoxon_test(data(:,1), data(:,3)) (BLEU-4) 
# wilcoxon_test(data(:,2), data(:,4)) (METEOR)

#!/bin/bash

#RoboCup
# Baseline
#input1=results/output/robocup/generation/dependencies/NO_POS/model_3_1-best_inter1_new4_results_folds_filenames
# LM 
#input1=results/output/robocup/generation/dependencies/grid/model_3_25-best_inter1_new4_results_folds_filenames
# LM-DMV
#input1=results/output/robocup/generation/dependencies/POS/model_3_85-best_inter0.9_new4_results_folds_filenames
# Gabor
#input2=../Gabor/gaborFiles/2010emnlp-generation/results-robocup_results_folds_filenames
#output=results/output/robocup/generation/stat_significance/lmDmv_vs_gabor

#WeatherGov
# Baseline
#input1=results/output/weatherGov/generation/1-best_reordered_eventTypes_linear_reg_cond_null/stage1.tst.xml
# LM
#input1=results/output/weatherGov/generation/dependencies/model_3_15-best_0.01_NO_STOP_inter1_hypRecomb_lmLEX_allowNone_NO_STOP/stage1.tst.xml
# LM-DMV
#input1=results/output/weatherGov/generation/dependencies/final/model_3_65-best_0.01_NO_STOP_inter0.3_hypRecomb_lmLEX_allowNone_POS_NO_STOP/stage1.tst.xml
# DP-UNSUP
#input1=results/output/weatherGov/generation/pcfg/model_3_75-best_0.01_treebank_unaryRules_wordsPerRootRule_0.04_predLength/stage1.tst.xml
# DP-AUTO
#input2=results/output/weatherGov/generation/pcfg/model_3_45-best_0.01_treebank_unaryRules_0.04/stage1.tst.xml
# Gabor
#input2=../Gabor/gaborFiles/2010emnlp-generation/results-weather.xml.recomputed
#output=results/output/weatherGov/generation/pcfg/stat_significance/dpunsup_vs_dpauto

#Atis
# Baseline
#input1=results/output/atis/generation/model_3_1-best_no_null_no_smooth_STOP_predLength/stage1.tst.xml
# LM
#input1=results/output/atis/generation/model_3_40-best_prior_0.01_STOP_predLength_all/stage1.tst.xml
# LM-DMV
#input1=results/output/atis/generation/dependencies_uniformZ_all/model_3_40-best_0.01_STOP_inter0.6_condLM_hypRecomb_lmLEX_POS_predLength/stage1.tst.xml
# Gabor
#input2=../Gabor/generation/outs/atis/1.exec/results-test.xml.recomputed
#output=results/output/atis/generation/stat_significance/lmDmv_vs_gabor

#WinHelp
# Baseline
#input1=results/output/weatherGov/generation/1-best_reordered_eventTypes_linear_reg_cond_null/stage1.tst.xml
# LM
#input1=results/output/weatherGov/generation/dependencies/model_3_15-best_0.01_NO_STOP_inter1_hypRecomb_lmLEX_allowNone_NO_STOP/stage1.tst.xml
# LM-DMV
#input1=results/output/weatherGov/generation/dependencies/final/model_3_65-best_0.01_NO_STOP_inter0.3_hypRecomb_lmLEX_allowNone_POS_NO_STOP/stage1.tst.xml
# DP-UNSUP
input1=results/output/winHelp/generation/generative/no_pos/no_null/pcfg/model_3_docs_newAnnotation_markov1_80-best_iter1_max12_newFolds_gold/all.tst.xml
# DP-AUTO
input2=results/output/winHelp/generation/generative/no_pos/no_null/pcfg/model_3_docs_newAnnotation_markov1_120-best_iter1_max12_newFolds_gold/all.tst.xml
# Gabor
#input2=../Gabor/gaborFiles/2010emnlp-generation/results-weather.xml.recomputed
output=results/output/winHelp/generation/generative/no_pos/no_null/pcfg/stat_significance/dpunsup_vs_dpauto

java -cp dist/Generator.jar induction.utils.postprocess.ExtractGenerationMetricsExecutor \
-inputFile1 ${input1} \
-inputFile1Type percy \
-inputFile2 ${input2} \
-inputFile2Type percy \
-outputFile ${output} \
-trimSize \
-calculateStatSig \
-inputFile1TypeOfPath file \
-inputFile2TypeOfPath file
# useful for cross-fold experiments
#-inputFile1TypeOfPath list \
#-inputFile2TypeOfPath list

