package induction.utils;

import induction.Utils;
import induction.problem.event3.generative.GenerativeEvent3Model;

/**
 * Partition a dataset in a single file, to multiple individual counterparts,
 * i,e. .events, .text and .align files. Each individual file will get a unique
 * number in ascending order.
 *
 * @author konstas
 */
public class PartitionDataset
{
    private String outputPath, inputFilename;
    private int startIndex;

    public PartitionDataset(String outputPath, String inputFilename, int startIndex)
    {
        this.outputPath = outputPath;
        this.inputFilename = inputFilename;
        this.startIndex = startIndex;
    }
    public void execute()
    {
        String key = null;
        StringBuilder str = new StringBuilder();
        for(String line : Utils.readLines(inputFilename))
        {
            if(line.startsWith("Example_"))
            {
                if(key != null) // only for the first example
                {
                    writeFiles(str.toString(), startIndex++);
                    str = new StringBuilder();
                }
                key = line;
            } // if
            str.append(line).append("\n");
        }  // for
        writeFiles(str.toString(), startIndex++); // don't forget last example
    }

    private void writeFiles(String input, int id)
    {
        String[] parts = GenerativeEvent3Model.extractExampleFromString(input);
        Utils.write(String.format("%s%s.%s", outputPath, id, "text"), parts[1]);
        Utils.write(String.format("%s%s.%s", outputPath, id, "events"), parts[2]);
//        Utils.write(String.format("%s%s.%s", outputPath, id, "align"), parts[3]);
        // heurestic: alignments on gabor's-percy's system is measured by line number rathern than id
        // simple to solve on atis, where the alignment is over all events present.
        StringBuilder str = new StringBuilder("0"); // begin with line number; we only have a single line
        for(int i = 0; i < parts[2].split("\n").length; i++)
            str.append(" ").append(i);
        Utils.write(String.format("%s%s.%s", outputPath, id, "align"), str.toString());
    }

    public static void main(String[] args)
    {
        // 509 505 506 515 508 510 505 484 500 471
        int fold = 10;
        String outputPath = "../Gabor/generation/data/winHelp.sents/fold"+fold+"/";
//        String inputFilename = "../atis/lambda/percy/train/atis5000.sents.full";
        String inputFilename = "data/branavan/winHelpHLA/folds/winHelpFold"+fold+"PathsEval";
//        int startIndex = 0; // the initial number to use as filename
        int startIndex = 471; // the initial number to use as filename
        PartitionDataset pd = new PartitionDataset(outputPath, inputFilename, startIndex);
        pd.execute();
    }
}
