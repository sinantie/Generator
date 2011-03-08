#!/bin/bash

#Fold 1
./robocup_model_3.sh robocupLists/robocupFold1PathsTrain \
results/output/robocup/model_3_percy_NO_NULL_semPar_values_unk_no_generic_newField_gold/fold1 10 2

#Fold 2
./robocup_model_3.sh robocupLists/robocupFold2PathsTrain \
 results/output/robocup/model_3_percy_NO_NULL_semPar_values_unk_no_generic_newField_gold/fold2 10 2
#Fold 3
./robocup_model_3.sh robocupLists/robocupFold3PathsTrain \
 results/output/robocup/model_3_percy_NO_NULL_semPar_values_unk_no_generic_newField_gold/fold3 10 2
#Fold 4
./robocup_model_3.sh robocupLists/robocupFold4PathsTrain \
 results/output/robocup/model_3_percy_NO_NULL_semPar_values_unk_no_generic_newField_gold/fold4 10 2

#All
#./robocup_model_3.sh robocupLists/robocupAllPathsTrain \
#results/output/robocup/model_3_percy_NO_NULL_semPar_values_unk_no_generic_newField/all 10 2
