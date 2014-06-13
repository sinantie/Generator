set terminal postscript eps enhanced color

set title 'Root node rule frequencies'
set xlabel 'Rank'
set ylabel 'Frequency'
#set yrange [0:4]

plot 'root_node_rules_counts_logs' using 1:2 notitle
