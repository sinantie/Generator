#!/bin/bash
java -cp dist/Generator.jar:dist/lib/Helper.jar:dist/lib/kylm.jar:dist/lib/meteor.jar:dist/lib/tercom.jar:dist/lib/srilmWrapper:dist/stanford-postagger-2010-05-26.jar utils.SemanticAnnotation \
results/output/weatherGov/alignments/gold_staged/evalGabor/f1-pred.0
