#!/bin/bash

#RoboCup
#input=results/output/robocup/generation/dependencies/NO_POS/
#formattedString=model_3_\$param1\$-best_inter\$param2\$_new4_full_joint
#output=results/output/robocup/generation/dependencies/NO_POS/grid.weighted.full_joint.results
#crossValidate=true
#weights=513#365#214#311

#WeatherGov
#input=results/output/weatherGov/generation/dependencies/
#formattedString=model_3_\$param1\$-best_0.01_STOP_inter\$param2\$_condLM_hypRecomb_lmLEX_POS_predLength
#output=results/output/weatherGov/generation/dependencies/grid.results
#crossValidate=false
#COMMENT weights parameter

#Atis
input=results/output/atis/generation/dependencies_uniformZ_all/
formattedString=model_3_\$param1\$-best_0.01_STOP_inter\$param2\$_condLM_hypRecomb_lmLEX_POS_predLength
output=results/output/atis/generation/dependencies_uniformZ_all/grid.results
crossValidate=false
#COMMENT weights parameter

java -cp dist/Generator.jar induction.utils.ProcessGridSearchFiles \
${input} \
${formattedString} \
${output} \
${crossValidate} \
${weights}
