#propFile=robocup_humanEval.properties
propFile=winHelp_humanEval.properties
#propFile=atis_humanEval.properties

java -cp dist/Generator.jar:dist/lib/Helper.jar \
induction.utils.humanevaluation.ExportScenarios \
${propFile}

