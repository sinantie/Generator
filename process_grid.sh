#!/bin/bash

#RoboCup
#input=results/output/robocup/generation/dependencies/NO_POS/
#formattedString=model_3_\$param1\$-best_inter\$param2\$_new4_full_joint
#output=results/output/robocup/generation/dependencies/NO_POS/grid.weighted.full_joint.results
#crossValidate=true
#weights=513#365#214#311

#WeatherGov
input=results/output/weatherGov/generation/dev/pcfg/
#formattedString=model_3_\$param1\$-best_0.01_STOP_inter\$param2\$_condLM_hypRecomb_lmLEX_POS_predLength
formattedString=model_3_\$param1\$-best_0.01_treebank_unaryRules_\$param2\$_wordsPerRootRule_predLength
output=results/output/weatherGov/generation/dev/pcfg/dev_treebank_unaryRules_0.04_wordsPerRootRule_predLength.grid.results
crossValidate=false
#COMMENT weights parameter

#Atis
#input=results/output/atis/generation/dependencies_uniformZ_all/
#formattedString=model_3_\$param1\$-best_0.01_STOP_inter\$param2\$_condLM_hypRecomb_lmLEX_POS_predLength
#output=results/output/atis/generation/dependencies_uniformZ_all/grid.results
#crossValidate=false
#COMMENT weights parameter

#WinHelp
#input=results/output/winHelp/generation/generative/pos/no_null
#formattedString=model_3_no_null_pos_auto_\$param1\$-best_inter\$param2\$_goldLength
#output=${input}/grid_no_null.results
#crossValidate=true
#weights=509#505#506#515#508#510#505#484#500#471

java -cp dist/Generator.jar induction.utils.postprocess.ProcessGridSearchFiles \
${input} \
${formattedString} \
${output} \
${crossValidate}
#${weights}
