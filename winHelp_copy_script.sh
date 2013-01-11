#!/bin/bash

destDir=/media/afs/EDI/Generator
origDir=/home/sinantie/EDI/Generator

distDir=dist
resultsDir=results/output/winHelp/generation/generative/no_pos/no_null/pcfg
inputDir=data/branavan/winHelpHLA/folds/docs.newAnnotation
treebanksDir=data/branavan/winHelpHLA/folds/treebanks
lmDir=data/branavan/winHelpHLA/folds/winHelpLM/docs.newAnnotation
modelsDir=results/output/winHelp/alignments

models=(model_3_docs_no_null_newAnnotation_externalTreebank model_3_docs_no_null_newAnnotation_markov0_externalTreebank model_3_docs_no_null_newAnnotation_markov1_externalTreebank)


# copy jar file and libraries. Comment out if necessary (usually you will want to run it on the first time)
mkdir -p $destDir/$distDir
cp $origDir/$distDir/Generator.jar $destDir/$distDir
mkdir -p $destDir/$distDir/lib
cp $origDir/$distDir/lib/*.jar $destDir/$distDir/lib

# create output directory
mkdir -p $destDir/$resultsDir

# copy input files
mkdir -p $destDir/$inputDir
cp $origDir/$inputDir/* $destDir/$inputDir

# copy treebank files
mkdir -p $destDir/$treebanksDir
cp $origDir/$treebanksDir/* $destDir/$treebanksDir

# copy kylm only LM files
mkdir -p $destDir/$lmDir
cp $origDir/$lmDir/kylm* $destDir/$lmDir

# copy models
mkdir -p $destDir/$modelsDir
for f in ${models[@]}
do
	cp -R $origDir/$modelsDir/${f} $destDir/$modelsDir/${f}
done

