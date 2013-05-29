#!/usr/bin/octave3.2 -qf

arg_list = argv ();
data = dlmread(arg_list{1});
bleu = wilcoxon_test(data(:,1), data(:,3));
printf ("BLEU-4: p=%3.5f\n", bleu);
meteor = wilcoxon_test(data(:,2), data(:,4));
printf ("METEOR: p=%3.5f", meteor);

