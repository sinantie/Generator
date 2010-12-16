#!/bin/bash

pathsFile=$1
linesFile=$2

for line in `cat $linesFile`
do
	`echo awk "NR==$line{print;exit}" $pathsFile`
done

