#!/bin/bash

t=''
for file in `cat $1`
do
   ${../$file/events/text} | cat | wc -l
done
