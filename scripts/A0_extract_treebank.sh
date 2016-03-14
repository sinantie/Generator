#!/bin/bash
inputPath=datasets/GoldLogo
inputFile=Records.train
input=${inputPath}/${inputFile}
stagedParamsPath=results/GoldLogo/alignments/0.exec
suffix=Aligned
binarize=right
inputPosTagged=false
execDir=results/GoldLogo/treebanks/

CUR_DIR=`pwd`
cd ..

# 1. Reorder Alignment Predictions (if the process has run with multiple threads then the order may be wrong)
prefix=--
pred_file=${stagedParamsPath}/stage1.train.pred.14
ref_file=${stagedParamsPath}/stage1.train.full-pred.14
sorted_align_file=${stagedParamsPath}/stage1.train.pred.14.sorted
java -cp dist/Generator.jar:dist/lib/Helper.jar induction.utils.ReorderAlignmentPredictions ${prefix} ${input} ${pred_file} ${ref_file}

# 2. Export examples to EDUs, and predicted alignments files
#options: aligned, goldStandard
exportExamplesToEdusType=aligned
predictedAlignments=${execDir}/${inputFile}.${exportExamplesToEdusType}Type.align
${CUR_DIR}/export_examples_to_edus.sh ${exportExamplesToEdusType} ${input} ${sorted_align_file} ${execDir}/${inputFile}.${exportExamplesToEdusType}.edus ${predictedAlignments}


# 3. Extract Record Treebank

java -cp dist/Generator.jar:dist/lib/Helper.jar:dist/lib/kylm.jar:dist/lib/meteor.jar:dist/lib/tercom.jar:dist/lib/srilmWrapper:\
dist/stanford-postagger-2010-05-26.jar \
-Djava.library.path=lib/wrappers induction.utils.ExtractRecordsStatisticsExecutor \
-exportType recordType \
-create \
-overwriteExecDir \
-modelType event3 \
-initType staged \
-inputFileExt events \
-examplesInSingleFile \
-outputExampleFreq 500 \
-inputLists ${input} \
-execDir ${execDir} \
-stagedParamsFile ${stagedParamsPath}/stage1.params.obj.gz \
-suffix ${suffix} \
-binarize ${binarize} \
-markovOrder 0 \
-useEventTypeNames \
-modifiedBinarization \
-ruleCountThreshold 0 \
-extractRecordTrees \
-predInput ${predictedAlignments}

# For RST Only
#-externalTreesInput ${externalTreesInput} \
#-externalTreesInputType rst \
#-overrideCleaningHeuristics

# Various Parameters
#-exportEvent3 \
#-modifiedBinarization
#-useEventTypeNames
#-extractRecordTrees
#-countRepeatedRecords
#-countSentenceNgrams
#-countDocumentNgrams
#-writePermutations
#-delimitSentences


# 4. Create new dataset file with embedded trees
binarizedCamelCase="$(tr '[:lower:]' '[:upper:]' <<< ${binarize:0:1})${binarize:1}"
java -cp dist/Generator.jar:dist/lib/Helper.jar induction.utils.ExportExamplesToSingleFile \
${input} ${execDir}/recordTreebank${binarizedCamelCase}Binarize${suffix} ${input}.${suffix}.trees ${inputPosTagged}


cd ${CUR_DIR}