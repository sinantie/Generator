#!/bin/bash
bn=`basename $1`
dir=`dirname $1`
gnuplot $1
epstopdf $dir/$bn.ps
pdfcrop $dir/$bn.pdf
mv $dir/$bn-crop.pdf $dir/$bn.pdf
