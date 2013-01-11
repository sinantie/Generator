#!/bin/bash

destDir=/media/volkswagen/eddie_dir/Generator
expDir=/media/volkswagen/eddie_expDir
origDir=/home/sinantie/EDI/Generator

distDir=dist
resultsDir=results/output/weatherGov/generation/pcfg
inputDir=data/weatherGov/weatherGovTrainGabor.gz
treebanksDir=data/weatherGov/treebanks
treebanks=(recordTreebankRulesTrainRightBinarizeMarkov0 recordTreebankRulesTrainRightBinarizeMarkov1 recordTreebankRulesTrainRightBinarizeAlignments recordTreebankRulesTrainRightBinarizeAlignmentsMarkov0 recordTreebankRulesTrainRightBinarizeAlignmentsMarkov1)
lmDir=weatherGovLM/gabor-srilm-abs-3-gram.model.arpa
modelsDir=results/output/weatherGov/alignments/pcfg
models=(model_3_gabor_record_pcfg_treebank_markov0_externalTreebank model_3_gabor_record_pcfg_treebank_markov1_externalTreebank model_3_gabor_record_pcfg_treebank_alignments_externalTreebank model_3_gabor_record_pcfg_treebank_alignments_markov0_externalTreebank model_3_gabor_record_pcfg_treebank_alignments_markov1_externalTreebank)


# copy jar file and libraries. Comment out if necessary (usually you will want to run it on the first time)
#mkdir -p $destDir/$distDir
cp $origDir/$distDir/Generator.jar $destDir/$distDir
#mkdir -p $destDir/$distDir/lib
#cp $origDir/$distDir/lib/*.jar $destDir/$distDir/lib

# create output directory
mkdir -p $expDir/$resultsDir

# copy input files
mkdir -p $destDir/$inputDir
cp $origDir/$inputDir $destDir/$inputDir

# copy treebank files
mkdir -p $destDir/$treebanksDir
for f in ${treebanks[@]}
do
	cp -R $origDir/$treebanksDir/${f} $destDir/$treebanksDir/${f}
done

# copy LM file
cp $origDir/$lmDir $destDir/$lmDir

# copy models
mkdir -p $destDir/$modelsDir
for f in ${models[@]}
do
	cp -R $origDir/$modelsDir/${f} $destDir/$modelsDir/${f}
done

