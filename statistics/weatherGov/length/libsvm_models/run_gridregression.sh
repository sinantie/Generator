#!/bin/bash

trainFile=dev7.5KListPathsGaborScaled.values.features.libsvm

python ~/libsvm-3.14/tools/gridregression.py \
-svmtrain ~/libsvm-3.14/svm-train \
-log2c 1,16,1 \
-log2g -5,5,1 \
${trainFile}

#-log2p -8,-1,1
