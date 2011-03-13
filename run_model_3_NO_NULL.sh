#export JAVA_HOME=/usr/local/opt/java/x86_64/jdk1.6.0_01
#export PATH=$PATH:/usr/local/opt/java/x86_64/jdk1.6.0_01/bin

#. /etc/profile.d/modules.sh
#module load java

java -Xmx5800m -Xms5800m -cp dist/ContentSelectionJava.jar:dist/lib/kylm.jar:dist/lib/meteor.jar:dist/lib/tercom.jar:dist/lib/srilmWrapper:dist/stanford-postagger-2010-05-26.jar induction.Induction -initType random -create -modeltype event3 -inputLists $1 -execPoolDir $2 -Options.stage1.numIters $3 -inputFileExt events -numThreads $4 -indepEventTypes 0,10 -indepFields 0,5 -newEventTypeFieldPerWord 0,5 -newFieldPerWord 0,5 -disallowConsecutiveRepeatFields -dontCrossPunctuation -Options.stage1.smoothing 0.1 -posAtSurfaceLevel
