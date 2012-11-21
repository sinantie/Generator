#!/bin/bash

inputFeaturesFile=$1
outputFeaturesFile=$2
paramsFile=$3
modelFile=$4
featureType=$5
startIndex=$6

java -cp dist/Generator.jar induction.utils.linearregression.LinearRegressionExecutor \
-mode train \
-inputFeaturesFile ${inputFeaturesFile} \
-outputFeaturesFile ${outputFeaturesFile} \
-paramsFile ${paramsFile} \
-modelFile ${modelFile} \
-type ${featureType} \
-startIndex ${startIndex} \
-extractFeatures \
-saveModel

#-examplesInSingleFile \
