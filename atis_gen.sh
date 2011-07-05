#!/bin/bash
./java_run_gen.sh data/atis/test/atis-test.txt \
results/output/atis/generation/model_3_40-best_no_smooth \
results/output/atis/alignments/model_3/15_iter_no_smooth 40 \
atisLM/atis-all-train-3-gram.model.arpa srilm -examplesInSingleFile
