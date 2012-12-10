#!/bin/bash
folds=10
input=data/branavan/winHelpHLA/folds

for (( f=1; f<=folds; f++ ))
do
	echo winHelpFold${f}Train
	./posTagger.sh ${input}/winHelpFold${f}Train
	cat ${input}/winHelpFold${f}Train | grep Example_ | wc -l
	cat ${input}/winHelpFold${f}Train.tagged | grep Example_ | wc -l
	
	echo winHelpFold${f}Eval
	./posTagger.sh ${input}/winHelpFold${f}Eval
	cat ${input}/winHelpFold${f}Eval | grep Example_ | wc -l
	cat ${input}/winHelpFold${f}Eval.tagged | grep Example_ | wc -l
done
