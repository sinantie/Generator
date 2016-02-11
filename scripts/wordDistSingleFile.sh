#!/bin/bash

while read line
do
   echo $line | wc -w
done < $1
