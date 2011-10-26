
#for f in `cat genEvalListPathsRandomEventsOnly`
#do
#	`ls $f | grep "No such file"`
#done
iter=1
for i in `ls results/output/atis/generation/discriminative/calculate_baseline_weight_norm/stage1.train.performance.*` 
do
	echo $iter: `cat $i | grep "Averaged Bleu scores"`
	echo $iter: `cat $i | grep "gradient"`
	iter=`expr $iter + 1` 
done

