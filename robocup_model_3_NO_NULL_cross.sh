#!/bin/bash

#Fold 1
./robocup_model_3.sh robocupLists/robocupAllPathsTrain \
results/output/robocup/model_3_percy_NO_NULL_semPar_values_unk_no_generic/all 10 2

#Fold 2
./robocup_model_3.sh robocupLists/robocupFold2PathsTrain \
 results/output/robocup/model_3_percy_NO_NULL_semPar_values_unk_no_generic_newField/fold2 10 2
#Fold 3
./robocup_model_3.sh robocupLists/robocupFold3PathsTrain \
 results/output/robocup/model_3_percy_NO_NULL_semPar_values_unk_no_generic_newField/fold3 10 2
#Fold 4
./robocup_model_3.sh robocupLists/robocupFold4PathsTrain \
 results/output/robocup/model_3_percy_NO_NULL_semPar_values_unk_no_generic_newField/fold4 10 2
