
for f in `cat genEvalListPathsRandomEventsOnly`
do
	`ls $f | grep "No such file"`
done
