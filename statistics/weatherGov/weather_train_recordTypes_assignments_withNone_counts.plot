set terminal postscript eps enhanced color

binwidth=1
set boxwidth binwidth
#bin(x,width)=width*floor(x/width)
bin(x,width)=width*floor(x/width) + binwidth/2
set title "weatherGov train set - records per document histogram"
plot 'weather_train_recordTypes_assignments_withNone.counts' using (bin($2,binwidth)):(1.0) smooth freq with boxes notitle
