#!/bin/bash

inputFile=$1
paramsFile=$2
modelFile=$3
featureType=$4
startIndex=$5

java -cp dist/Generator.jar induction.utils.linearregression.LinearRegressionExecutor \
-mode test \
-inputFile ${inputFile} \
-examplesInSingleFile \
-paramsFile ${paramsFile} \
-modelFile ${modelFile} \
-type ${featureType} \
-startIndex ${startIndex}

#-examplesInSingleFile \
