#!/bin/bash

#Fold 1
./robocup_model_3.sh robocupLists/randomFolds/robocupFold1PathsTrain \
results/output/robocup/randomFolds/model_3_percy_NO_NULL_semPar_values_oneEvent_unk_no_generic_newField_random/fold1 10 2

#Fold 2
./robocup_model_3.sh robocupLists/randomFolds/robocupFold2PathsTrain \
 results/output/robocup/randomFolds/model_3_percy_NO_NULL_semPar_values_oneEvent_unk_no_generic_newField_random/fold2 10 2
#Fold 3
./robocup_model_3.sh robocupLists/randomFolds/robocupFold3PathsTrain \
 results/output/robocup/randomFolds/model_3_percy_NO_NULL_semPar_values_oneEvent_unk_no_generic_newField_random/fold3 10 2
#Fold 4
./robocup_model_3.sh robocupLists/randomFolds/robocupFold4PathsTrain \
 results/output/robocup/randomFolds/model_3_percy_NO_NULL_semPar_values_oneEvent_unk_no_generic_newField_random/fold4 10 2

#All
#./robocup_model_3.sh robocupLists/robocupAllPathsTrain \
#results/output/robocup/model_3_percy_NO_NULL_semPar_values_unk_no_generic_newField/all 10 2
