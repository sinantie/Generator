#!/bin/bash

#Fold 1
./robocup_model_3.sh robocupLists/robocupFold1PathsTrain results/output/robocup/model_3_percy/fold1 10 2 -allowNoneEvent
#Fold 2
./robocup_model_3.sh robocupLists/robocupFold2PathsTrain results/output/robocup/model_3_percy/fold2 10 2 -allowNoneEvent
#Fold 3
./robocup_model_3.sh robocupLists/robocupFold3PathsTrain results/output/robocup/model_3_percy/fold3 10 2 -allowNoneEvent
#Fold 4
./robocup_model_3.sh robocupLists/robocupFold4PathsTrain results/output/robocup/model_3_percy/fold4 10 2 -allowNoneEvent
