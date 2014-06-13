set terminal postscript eps enhanced color
#set output 'test.pdf'


set title 'Root node rule log frequencies'
set xlabel 'Rank'
set ylabel 'Frequency'
set yrange [0:4]
f(x) = a*x + b

fit f(x) 'root_node_rules_counts_logs' using 1:(log10($2)) via a,b
rms_title= sprintf("RMS=%1.4f",FIT_STDFIT)

plot 'root_node_rules_counts_logs' using 1:(log10($2)) notitle, f(x) title rms_title
