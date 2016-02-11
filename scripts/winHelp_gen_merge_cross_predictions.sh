#!/bin/bash

inputPath=results/output/winHelp/generation/generative/no_pos/no_null/model_3_docs_newAnnotation_150-best_iter1_gold
outputFile=results/output/winHelp/generation/generative/no_pos/no_null/model_3_docs_newAnnotation_150-best_iter1_gold/stage1.test.all.full-pred-gen
folds=10

#Folds
for (( f=1; f<=folds; f++ ))
do
	cat ${inputPath}/fold${f}/stage1.test.full-pred-gen >> ${outputFile}
done

