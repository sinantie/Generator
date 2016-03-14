#!/bin/bash

PREFIX=$1
GOLD_FILE=$2
PRED_FILE=$3
REF_FILE=$4
echo /usr/bin/java -cp dist/Generator.jar:dist/lib/Helper.jar induction.utils.ReorderAlignmentPredictions ${PREFIX} ${GOLD_FILE} ${PRED_FILE} ${REF_FILE}
