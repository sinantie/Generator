#!/bin/bash

list=bikesLists/allListText

for f in `cat $list` 
do
	`cat $f | sed -e 's/all mountain/all-mountain/' > $f`
done
