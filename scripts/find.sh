#!/bin/bash

input=$1
what=$2
extension=$3

for file in `cat $input`
do
	if [[ -n ${extension} ]];
		then file="${file%%.*}".$extension;			
	fi
	if grep "${what}" $file 
		then echo $file
	fi
done

