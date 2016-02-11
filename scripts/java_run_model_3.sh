#export JAVA_HOME=/usr/local/opt/java/x86_64/jdk1.6.0_01
#export PATH=$PATH:/usr/local/opt/java/x86_64/jdk1.6.0_01/bin

#. /etc/profile.d/modules.sh
#module load java

java -cp dist/Generator.jar:dist/lib/kylm.jar:dist/lib/meteor.jar:dist/lib/tercom.jar:dist/lib/srilmWrapper:dist/stanford-postagger-2010-05-26.jar:\
dist/lib/collections-generic-4.01.jar:dist/lib/colt-1.2.0.jar:dist/lib/concurrent-1.3.4.jar:dist/lib/jung-algorithms-2.0.1.jar:\
dist/lib/jung-api-2.0.1.jar:dist/lib/jung-graph-impl-2.0.1.jar:dist/lib/jung-hypergraph-visualization-1.0.jar:dist/lib/jung-io-2.0.1.jar:\
dist/lib/jung-jai-2.0.1.jar:dist/lib/jung-visualization-2.0.1.jar:dist/lib/stax-api-1.0.1.jar:dist/lib/vecmath-1.3.1.jar:\
dist/lib/wstx-asl-3.2.6.jar induction.Induction -initType random -create \
-modeltype event3 -inputLists $2 \
-execPoolDir $3 -Options.stage1.numIters $4 -inputFileExt \
events \ -numThreads $5 -initNoise 0 -indepEventTypes 0,10 -indepFields 0,5 -newEventTypeFieldPerWord 0,5 -newFieldPerWord 0,5 \
-disallowConsecutiveRepeatFields \
-dontCrossPunctuation -Options.stage1.smoothing 0.1 -initNoise 0 -outputExampleFreq 10000 $6 -allowNoneEvent -outputFullPred
