package induction.utils;

import induction.Utils;
import induction.problem.event3.Event3Example;
import java.io.File;
import java.lang.String;
import java.util.ArrayList;
import java.util.List;

/**
 * Partition a dataset in a single file, to multiple individual counterparts,
 * i,e. .events, .text and .align files. Each individual file will get a unique
 * number in ascending order.
 *
 * @author konstas
 */
public class ExportExamplesToFiles
{
    private String outputPath, inputFilename;
    private int startIndex, processedExamples;

    public ExportExamplesToFiles(String outputPath, String inputFilename, int startIndex)
    {
        this.outputPath = outputPath;
        this.inputFilename = inputFilename;
        this.startIndex = startIndex;
    }
    public int execute()
    {
//        String key = null;
//        StringBuilder str = new StringBuilder();
//        for(String line : Utils.readLines(inputFilename))
//        {
//            if(line.startsWith("Example_"))
//            {
//                if(key != null) // only for the first example
//                {
//                    writeFiles(str.toString(), startIndex++);
//                    str = new StringBuilder();
//                }
//                key = line;
//            } // if
//            str.append(line).append("\n");
//        }  // for
//        writeFiles(str.toString(), startIndex++); // don't forget last example
        
        for(Event3Example ex : Utils.readEvent3Examples(inputFilename, true))
        {
            writeFiles(ex, startIndex++);
        }
        return startIndex;
    }

    private void writeFiles(Event3Example example, int id)
    {
        Utils.write(String.format("%s%s.%s", outputPath, id, "text"), example.getText());
        Utils.write(String.format("%s%s.%s", outputPath, id, "events"), example.getEvents());
        // heurestic: alignments on gabor's-percy's system is measured by line number rathern than id
        // simple to solve on atis, where the alignment is over all events present.
        StringBuilder str = new StringBuilder("0"); // begin with line number; we only have a single line
        for(int i = 0; i < example.getAlignments().split("\n").length; i++)
            str.append(" ").append(i);
        Utils.write(String.format("%s%s.%s", outputPath, id, "align"), str.toString());
        processedExamples++;
    }

    public int getProcessedExamples()
    {
        return processedExamples;
    }
        
    private void writeFiles(String input, int id)
    {
        String[] parts = Utils.extractExampleFromString(input);
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
        List<Integer> trainIndices = new ArrayList<Integer>();
        List<Integer> testIndices = new ArrayList<Integer>();
        int folds = 10;
        int startIndex = 0;
//        String inputPath = "data/branavan/winHelpHLA/folds/docs.newAnnotation.removedOutliers/";
//        String outputPath = "../Gabor/generation/data/winHelp.docs.newAnnotation.removedOutliers/";
        String inputPath = "data/branavan/winHelpHLA/folds/sents.newAnnotation/";
        String outputPath = "../Gabor/generation/data/winHelp.sents.newAnnotation/";
        for(int fold = 1; fold <= folds; fold++)
        {
            startIndex = 0;            
            new File(outputPath+"fold"+fold+"/").mkdir();
            // write train files
            String inputFilename = inputPath + "winHelpFold"+fold+"Train";
            ExportExamplesToFiles ex = new ExportExamplesToFiles(outputPath, inputFilename, startIndex);
            startIndex = ex.execute();
            trainIndices.add(ex.processedExamples);
            // write test files
            inputFilename = inputPath + "winHelpFold"+fold+"Eval";
            ex = new ExportExamplesToFiles(outputPath, inputFilename, startIndex);
            ex.execute();
            testIndices.add(ex.processedExamples);
        } 
        // output train / test indices (copy to Gabor scripts)
        System.out.println(trainIndices);
        System.out.println(testIndices);
    }
}
