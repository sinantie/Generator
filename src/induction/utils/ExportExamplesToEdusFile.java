package induction.utils;


import fig.basic.IOUtils;
import induction.Utils;
import induction.problem.event3.Event3Example;
import induction.problem.event3.Event3Example.Alignment;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * Create dataset file that contains text and elementary discourse units (EDUs). 
 * The boundaries for the segmentation of the text are taken from alignment data,
 * extracted either manually or automatically. 
 * 
 * Accepts dataset in single file only.
 * 
 * Write examples following the event3 ver.2 format, which contains headers 
 * denoting the start of text, and EDUs.
 * @author konstas
 */
public class ExportExamplesToEdusFile
{    
    private String inputPath, recordAlignmentsPath, outputFile, outputFileAlignments;        
    public enum InputType {aligned, goldStandard};
    public enum Dataset {weatherGov, winHelp};
    private InputType type;
    private Dataset dataset;    
    private Map<String, String[]> manualAnnotation, textCorrections;
    private boolean multipleReferences;
    
    public ExportExamplesToEdusFile(InputType type, Dataset dataset, String path, String recordAlignmentsPath, 
            String outputFile, String outputFileAlignments, boolean multipleReferences)
    {
        this.type = type;
        this.dataset = dataset;
        this.inputPath = path;               
        this.recordAlignmentsPath = recordAlignmentsPath;
        this.outputFile = outputFile;        
        this.outputFileAlignments = outputFileAlignments;
        this.multipleReferences = multipleReferences;        
    }
    
    public ExportExamplesToEdusFile(InputType type, Dataset dataset, String path, String recordAlignmentsPath, 
            String manualAnnotationPath, String textCorrections, String outputFile, String outputFileAlignments, boolean multipleReferences)
    {
        this(type, dataset, path, recordAlignmentsPath, outputFile, outputFileAlignments, multipleReferences);
        this.manualAnnotation = readManualAnnotation(manualAnnotationPath);
        this.textCorrections = readTextCorrections(textCorrections);
    }

    public void execute()
    {        
        try
        {                        
            PrintWriter out = IOUtils.openOutEasy(outputFile);
            PrintWriter outAlignments = IOUtils.openOutEasy(outputFileAlignments);
            List<Event3Example> examples = Utils.readEvent3Examples(inputPath, true);             
            String[] recordAlignments = type == InputType.aligned ? Utils.readLines(recordAlignmentsPath) : null;
            int i = 0;
            for(Event3Example example : examples)
            {               
//                System.out.println(example.getName());
                // correct any text anomalies that will cause the parser to crash
                if(textCorrections != null)
                {
                    String[] textCorrectionsArray =  textCorrections.get(example.getName());
                    if(textCorrectionsArray != null)
                    {
                        example.setText(example.getText().replace(textCorrectionsArray[0], textCorrectionsArray[1]));
                    }
                }       
                example.setText(example.getText().replaceAll("\\(", "{").replaceAll("\\)", "}"));
                String[] ar;
                switch(type)
                {                    
                    case aligned : 
                        ar = cleanRecordAlignments(recordAlignments[i++].split(" "), multipleReferences ? example.getFirstLineOfTextInOneLine() : example.getTextInOneLine()); break;
                    case goldStandard : default : 
                        ar = mapGoldStandardAlignments(dataset, example.getName(), example.getAlignmentsPerLineArray(), example.getTextArray());                    
//                    case goldStandard : mapGoldStandardAlignments(dataset, example.getName(), example.ogetAlignmentsPerLineArray(), example.getTextArray());
                }
                out.print(example.exportToEdusFormat(ar, multipleReferences));
                outAlignments.println(Utils.arrayToString(ar));
            }
            out.close();
            outAlignments.close();
        } catch (Exception ioe)
        {
            System.err.println(ioe.getMessage());
            ioe.printStackTrace(System.err);
        }
    }
    
    /**
     * Record alignments cleansing. 
     * 1. Remove records aligning to one word.
     * 2. If a record alignment spans phrases from two adjacent sentences (separated with full-stop)
     * then keep the alignment up to the full-stop and assign the rest words of the phrase
     * to the next record alignment.
     * @param alignments
     * @param words
     * @return 
     */
    public static String[] cleanRecordAlignments(String[] alignments, String[] words)
    {
        return fixSplitSentenceAlignments(removeRecordsWithOneWord(alignments, words), words);                
    }
    
    /**
     * We assume that a record alignment should span up to the end of a sentence.
     * If a record alignment spans phrases from two adjacent sentences (separated with full-stop)
     * then keep the alignment up to the full-stop and assign the rest words of the phrase
     * to the next record alignment.
     * For examples, if we have the record alignment, 11 11 11 11 11 5 5, and the
     * third word is a full-stop then change the alignments to, 11 11 11 5 5 5 5.
     * 
     * @param alignments
     * @param words
     * @return 
     */
    public static String[] fixSplitSentenceAlignments(String[] alignments, String[] words)
    {
//        String[] out = new String[alignments.length];
        String[] out = Arrays.copyOf(alignments, alignments.length);
        int startIndex = 0;
        // restart the process for each delimiter found. In this case we avoid nested
        // splitting points.
        int timeOut = 0;
        while(startIndex < out.length && timeOut < words.length)
        {
            startIndex = fixSplitSentenceAlignment(out, words, startIndex);
            timeOut++;
        }
        return out;
    }
    
    private static int fixSplitSentenceAlignment(String[] alignments, String[] words, int startIndex)
    {
        int i;
        for(i = startIndex; i < words.length; i++)
        {
            if(Utils.isSentencePunctuation(words[i])) // found a sentence delimiter
            {
                if(i < words.length - 1 && (alignments[i].equals(alignments[i + 1]))) // the alignment span crosses the delimiter
                {            
                    // very rare: if the record-delimiter is the beginning of a span, i.e., different
                    // from the previous record span (obviously a wrong alignment)
                    // then assign it to the previous record span, as it is more likely to belog there.
                    if(!alignments[i].equals(alignments[i - 1]))
                    {
                        alignments[i] = alignments[i - 1];
                        break;
                    }
                    // find the next record alignment
                    int j;
                    for(j = i + 1; j < words.length - 1; j++)
                    {
                        if(!alignments[j].equals(alignments[i]))
                            break;
                    }
                    for(int k = i + 1; k < j; k++) // replace the crossing alignments with the next record
                    {
                        alignments[k] = alignments[j];
                    }
                    break;
//                    i = j; // continue with the rest of the string
                } // if
            } // if            
        }
        return i;
    }
    
    /**
     * Remove records aligning to one word. 
     * TODO: If the single word is punctuation, then always get the record alignment
     * of the previous word
     * @param alignments
     * @return 
     */
    private static String[] removeRecordsWithOneWord(String[] alignments, String[] words)
    {
        String[] out = new String[alignments.length];
        // First tackle single alignments on punctuation tokens. 
        for(int i = 0; i < alignments.length; i++)
        {
            // get the record alignment of the previous word. No need to check bounaries, 
            // as it is very unlikely to begin a sentence with punctuation.
            if(recordWithOneWord(alignments, alignments[i], i - 1, i + 1) && Utils.isPunctuation(words[i]))                            
                out[i] = alignments[i - 1];             
            else
                out[i] = alignments[i];
        }
        // Tackle remaining single word alignments
        for(int i = 0; i < out.length; i++)
        {
            if(recordWithOneWord(out, out[i], i - 1, i + 1))           
                out[i] = i == 0 ? out[i + 1] : out[i - 1];
        }
        return out;
    }
    
    private static boolean recordWithOneWord(String[] records, String current, int from, int to)
    {
        if(records.length == 1)
            return true;
        if(from < 0)
            return !current.equals(records[to]);
        else 
        {
            if(to >= records.length)
                return !current.equals(records[from]);
            else
                return !(current.equals(records[from]) || current.equals(records[to]));
        }        
    }
    
    private String[] mapGoldStandardAlignments(Dataset dataset, String name, Alignment[] alignments, String[] text)
    {
        switch(dataset)
        {
            case weatherGov : return mapGoldStandardAlignmentsWeatherGov(name, alignments, text);
            case winHelp : default: return mapGoldStandardAlignmentsWinHelp(name, alignments, text);                
        }
    }
    
    /**
     * Takes a set of record alignments per line of text and returns an array of alignments per word.
     * We automatically segment lines of text that correspond to more than one records, using
     * domain-based heuristics (based on Percy Liang's original alignment code).
     * @param name
     * @param alignments
     * @param text
     * @return 
     */
    private String[] mapGoldStandardAlignmentsWeatherGov(String name, Alignment[] alignments, String[] text)
    {        
        List<String> out = new ArrayList<>();
        for(int i = 0; i < alignments.length; i++)
        {
            Alignment alignment = alignments[i];
            if(alignment.size() == 1)
            {
                // treat none event lines: (wrongly) assign them the alignments of 
                // the next (or previous) non-none line.
                if(alignment.getElements()[0].equals("-1"))
                {
                    out.addAll(Arrays.asList(fillWithOneRecord(findNextNonNullAlignment(alignments, i), text[i])));
                }
                else
                {
                    out.addAll(Arrays.asList(fillWithOneRecord(alignment, text[i])));
                }                
            }
            else
            {                
                out.addAll(Arrays.asList(segmentUsingPatternsWeatherGov(alignment, text[i])));                                
            }
        }
        return out.toArray(new String[0]);
    }
    
    private String findNextNonNullAlignment(Alignment[] alignments, int currentLine)
    {
        // search first in forward direction
        for(int i = currentLine + 1; i < alignments.length; i++)
        {
            String nextRecordAlignment = alignments[i].getElements()[0];
            if(!nextRecordAlignment.equals("-1"))
                return nextRecordAlignment;
        }
        // search backwards
        for(int i = currentLine - 1; i >= 0; i--)
        {
            String prevRecordAlignment = alignments[i].getElements()[0];
            if(!prevRecordAlignment.equals("-1"))
                return prevRecordAlignment;
        }
        return null;
    }
    
    private String[] segmentUsingPatternsWeatherGov(Alignment alignment, String text)
    {     
        String[] words = text.split(" ");
        // deal with windDir and windSpeed
        if(text.contains("wind"))
        {
            if(text.contains("becoming"))                
            { 
//                return segmentWeatherGovWindDirWindSpeed(alignment, text);
                return segmentWeatherGovWindDirWindSpeedNormalise(alignment, text);
            }
            if(text.contains("increasing"))
            {
                return segmentUsingPatternWeatherGov(alignment, words, "wind", true);
            }
            if(text.contains("decreasing"))
            {
                return segmentUsingPatternWeatherGov(alignment, words, "wind", true);
            }            
            return segmentUsingPatternWeatherGov(alignment, words, "wind", true);
        }
        if(text.contains("chill")) // wrongly annotated in original dataset
        {
            String[] out = new String[text.split(" ").length];
            Arrays.fill(out, "1"); // HACK: we know that windChill is always the record with id=1
            return out;
        }
        // deal with precipPotential, rainChance, thunderChance, snowChance, sleetChance, and freezingRainChance
        if(text.contains("percent"))
        {
            if(text.contains("thunderstorms"))
            {
                if(alignment.size() == 2) // the gold-standard is wrong. It is most likely missing the precipPotential record
                {
                    String[] temp = new String[3];
                    temp[0] = "10";
                    System.arraycopy(alignment.getElements(), 0, temp, 1, alignment.size());
                    alignment.setElements(temp);
                }
                if(text.contains("showers"))
                {
                    return segmentUsingPatternWeatherGov(alignment, words, "of", true, "showers", true);
                }
                if(text.contains("rain"))
                {
                    return segmentUsingPatternWeatherGov(alignment, words, "of", true, "rain", true);
                }                
            }
            if(text.contains("snow"))
            {
                // the gold-standard is wrong. There is no rainChance record in the text, so we delete it.
                if(alignment.size() > 2)
                {
                    alignment.getElements()[1] = alignment.getElements()[2];
                }
                return segmentUsingPatternWeatherGov(alignment, words, "of", true);
            }
            if(text.contains("rain") || text.contains("showers"))
            {
                return segmentUsingPatternWeatherGov(alignment, words, "of", true);
            }
        }
        if(text.contains("snow showers") || text.contains("Snow showers")) // the gold-standard is wrong. There is no rainChance record in the text, so we delete it.
        {
            if(alignment.size() > 1)
            {
                alignment.getElements()[0] = alignment.getElements()[1];
            }
            String[] out = new String[text.split(" ").length];
            Arrays.fill(out, alignment.getElements()[0]);
            return out;
        }
        if(text.contains("chance"))
        {            
            if(text.contains("and"))
            {
                return segmentUsingPatternWeatherGov(alignment, words, "and", false);
            }
            if(text.contains("or"))
            {
                return segmentUsingPatternWeatherGov(alignment, words, "or", false);
            }
        }
        if(text.contains("and")) // deal with the rest conjunctions
        {
            return segmentUsingPatternWeatherGov(alignment, words, "and", false);
        }
        if(text.contains("or"))
        {
            return segmentUsingPatternWeatherGov(alignment, words, "or", false);
        }
        if(text.contains("mainly")) // the gold-standard is ambiguous. Just use the first record, which is wrong, but they are few!
        {
            String[] out = new String[text.split(" ").length];
            Arrays.fill(out, alignment.getElements()[0]);
            return out;
        }        
        System.out.println(text);        
        return new String[0];
    }
    
    private String[] segmentUsingPatternWeatherGov(Alignment alignment, 
            String[] words, String boundaryWord, boolean inclusive)
    {        
        String[] out = new String[words.length];
        String[] records = alignment.getElements();
        for(int i = 0; i < words.length; i++)
        {
            out[i] = records[0];
            if(words[i].equals(boundaryWord))
            {
                if(!inclusive) // the boundary word aligns to the next record
                {
                    out[i] = records[1];
                }
                for(int j = i + 1; j < words.length; j++)
                {
                    out[j] = records[1];
                }
                break;
            } // if
        } // for
        return out;
    }
    
    private String[] segmentUsingPatternWeatherGov(Alignment alignment, String[] words, 
            String boundaryWord1, boolean inclusive1, String boundaryWord2, boolean inclusive2)
    {
        String[] out = new String[words.length];
        String[] records = alignment.getElements();
            for(int i = 0; i < words.length; i++)
            {
                out[i] = records[0];
                if(words[i].equals(boundaryWord1))
                {
                    if(!inclusive1) // the boundary word aligns to the next record
                    {
                        out[i] = records[1];
                    }
                    for(int j = i + 1; j < words.length; j++)
                    {
                        out[j] = records[1];
                        if(words[j].equals(boundaryWord2))
                        {
                            if(!inclusive2) // the boundary word aligns to the previous record
                            {// alternate between first and second record or go to the 3rd record
                                out[j] = records[records.length == 2 ? 0 : 2]; 
                            }
                            for(int k = j + 1; k < words.length; k++)
                            {
                                out[k] = records[records.length == 2 ? 0 : 2];
                            }
                            break;
                        }
                    }
                    break;
                } // if
            } // for        
        return out;
    }
    
    /**
     * Segment windDir and windSpeed using word-matching heuristics.
     * @param alignment
     * @param text
     * @return 
     */
    private String[] segmentWeatherGovWindDirWindSpeed(Alignment alignment, String text)
    {
        String[] words = text.split(" ");
        String[] out = new String[words.length];
//        String[] records = alignment.getElements();
        if(countNumberOfOccurences("mph", words) == 1)
        {
            if(atEndOfSentence("mph", words))
            {
                if(text.contains("between"))
                {
                    return segmentUsingPatternWeatherGov(alignment, words, "between", false);
                }
                if(text.contains("around"))
                {
                    return segmentUsingPatternWeatherGov(alignment, words, "around", false);
                }
                if(text.contains("west"))
                {
                    return segmentUsingPatternWeatherGov(alignment, words, "west", true);
                }
                if(text.contains("southwest"))
                {
                    return segmentUsingPatternWeatherGov(alignment, words, "southwest", true);
                }
                if(text.contains("southeast"))
                {
                    return segmentUsingPatternWeatherGov(alignment, words, "southeast", true);
                }
                if(text.contains("northwest"))
                {
                    return segmentUsingPatternWeatherGov(alignment, words, "northwest", true);
                }
                
            } // if      
            else // we have an extra windDir at the end
            {
                if(text.contains("between"))
                {
                    return segmentUsingPatternWeatherGov(alignment, words, "between", false, "becoming", false);
                }
                if(text.contains("around"))
                {
                    return segmentUsingPatternWeatherGov(alignment, words, "around", false, "becoming", false);
                }
                if(text.contains("at"))
                {
                    return segmentUsingPatternWeatherGov(alignment, words, "at", false, "becoming", false);
                }
                
                return segmentUsingPatternWeatherGov(alignment, words, "wind", true, "becoming", false);
            }
        }
        else
        {
            int index = text.indexOf("becoming");
            String[] part1 = segmentUsingPatternsWeatherGov(alignment, text.substring(0, index));
            String[] part2 = segmentWeatherGovWindDirWindSpeed(alignment, text.substring(index));
            System.arraycopy(part1, 0, out, 0, part1.length);
            System.arraycopy(part2, 0, out, part1.length, part2.length);
            return out;
        }
        System.out.println(text);
        return out;
    }
    
    /**
     * Segment windDir and windSpeed using word-matching heuristics, but only
     * split at the first boundary, i.e., don't introduce extra record alignments.
     * @param alignment
     * @param text
     * @return 
     */
    private String[] segmentWeatherGovWindDirWindSpeedNormalise(Alignment alignment, String text)
    {
        String[] words = text.split(" ");
        if(text.contains("between"))
        {
            return segmentUsingPatternWeatherGov(alignment, words, "between", false);
        }
        if(text.contains("around"))
        {
            return segmentUsingPatternWeatherGov(alignment, words, "around", false);
        }
        return segmentUsingPatternWeatherGov(alignment, words, "wind", true);
    }
    
    private int countNumberOfOccurences(String word, String[] text)
    {
        int count = 0;
        for(String t : text)
        {
            if(t.equals(word))
                count++;
        }
        return count;
    }
    
    private boolean atEndOfSentence(String word, String[] text)
    {
        int lastIndex = text.length -1;
        return text[lastIndex].equals(word) || 
                (text[lastIndex - 1].equals(word) &&
                (text[lastIndex].equals(".") || text[lastIndex].equals(",") ));       
    }
    
    /**
     * Takes a set of record alignments per line of text and returns an array of alignments per word.
     * We assume that records are segmented at punctuation.
     * 
     * @param name
     * @param alignments
     * @param text
     * @return 
     */
    private String[] mapGoldStandardAlignmentsWinHelp(String name, Alignment[] alignments, String[] text)
    {        
        List<String> out = new ArrayList<>();
        for(int i = 0; i < alignments.length; i++)
        {
            Alignment alignment = alignments[i];
            if(alignment.size() == 1)
            {                
                out.addAll(Arrays.asList(fillWithOneRecord(alignment, text[i])));
            }        
            else
            {
                String manualAnnotationAr[] = manualAnnotation.get(name + "^" + i);
                if(manualAnnotationAr != null) // check first if there is a manually annotated entry
                {
                    out.addAll(Arrays.asList(manualAnnotationAr));
                }
                else // default to segmenting on punctuation
                {
                    out.addAll(Arrays.asList(segmentAtPunctuation(alignment, text[i])));
//                    String[] temp = segmentAtPunctuation(alignment, text[i]);
//                    if(temp == null)
//                        System.out.println(String.format("%s^%s:\n%s\n%s", name, i, text[i], alignment)); // Export lines requiring manual annotation                    
                }                
            }
        }
        return out.toArray(new String[0]);
    }
    
    private String[] segmentAtPunctuation(Alignment alignment, String text)
    {        
        String[] words = text.split(" ");
        String[] out = new String[words.length];
        int numOfSegments = text.split("[,\\.]").length;
        if(numOfSegments == alignment.size())
        {
            Queue<String> q = alignment.getElementsAsQueue();
            String align = q.poll();
            for(int i = 0; i < words.length; i++)
            {
                String word = words[i];                
                out[i] = align;
                if(i < words.length - 1 && Utils.isPunctuation(word))
                {
                    align = q.poll();
                }
            }
            return out;
        }        
        return null;
    }
    
    private String[] fillWithOneRecord(Alignment alignment, String text)
    {
        String[] words = text.split(" ");
        String[] out = new String[words.length];
        String record = alignment.getElements()[0];
        for(int j = 0; j < words.length; j++)
        {
            out[j] = record;
        }
        return out;
    }
    
    private String[] fillWithOneRecord(String alignment, String text)
    {
        String[] words = text.split(" ");
        String[] out = new String[words.length];        
        for(int j = 0; j < words.length; j++)
        {
            out[j] = alignment;
        }
        return out;
    }
    
    /**
     * Read file containing manual annotation in the following format key-value:
     * name^line:entry
     * @param manualAnnotationPath
     * @return 
     */
    private Map<String, String[]> readManualAnnotation(String manualAnnotationPath)
    {       
        Map<String, String[]> map = new HashMap<>();
        for(String line : Utils.readLines(manualAnnotationPath))
        {
            String[] temp = line.split(":");
            map.put(temp[0], temp[1].split(" "));
        }
        return map;
    }
    
    /**
     * Read file containing text corrections in the following format key-value:
     * name^line^find^replace
     * @param manualAnnotationPath
     * @return 
     */
    private Map<String, String[]> readTextCorrections(String textCorrectionsPath)
    {       
        Map<String, String[]> map = new HashMap<>();
        for(String line : Utils.readLines(textCorrectionsPath))
        {
            String[] temp = line.split("\\^");
            map.put(temp[0], new String[] {temp[1], temp[2]});
        }
        return map;
    }
    
    public static void main(String[] args)
    {
        
        // WEATHERGOV
//        Dataset dataset = Dataset.weatherGov;
//        InputType type = InputType.goldStandard;
//        // trainListPathsGabor, genDevListPathsGabor, genEvalListPathsGabor
//        String inputPath = "data/weatherGov/weatherGovTrainGabor.gz";
////        String inputPath = "data/weatherGov/weatherGovGenDevGaborRecordTreebankUnaryRules_modified2";
//        String inputPathRecordAlignments = "results/output/weatherGov/alignments/model_3_gabor_no_sleet_windChill_15iter/stage1.train.pred.14.sorted";
////        String inputPathRecordAlignments = "data/weatherGov/weatherGovGenDevGaborRecordTreebankUnaryRulesPredAlign_modified2";
////        String outputFile = "data/weatherGov/weatherGovTrainGaborEdusAligned.gz";
////        String outputFile = "data/weatherGov/weatherGovGenDevGaborRecordTreebankUnaryRules_modified2_EdusAligned";
//        String outputFile = "data/weatherGov/weatherGovTrainGaborEdusGoldNormal";
//        String outputFileAlignments = "data/weatherGov/weatherGovTrainGaborEdusGoldNormal.align";   
//        boolean multipleReferences = false;
        
        // BLOCKSWORLD
        Dataset dataset = Dataset.weatherGov;
        InputType type = InputType.aligned;        
        String inputPath = "datasets/GoldDigit20/Records.train";
        String inputPathRecordAlignments = "results/GoldDigit20/alignments/4.exec/stage1.train.pred.19.sorted";
        String outputFile = "results/GoldDigit20/treebanks/Records.train.aligned_edus";
        String outputFileAlignments = "results/GoldDigit20/treebanks/Records.train.align.edus.align";   
        boolean multipleReferences = true;
        if(args.length == 6)
        {
            type = Enum.valueOf(InputType.class, args[0]);
            inputPath = args[1];
            inputPathRecordAlignments = args[2];
            outputFile = args[3];
            outputFileAlignments = args[4];
            multipleReferences = Boolean.valueOf(args[5]);
        }
        new ExportExamplesToEdusFile(type, dataset, inputPath, inputPathRecordAlignments, outputFile, outputFileAlignments, multipleReferences).execute();        
        
        // WINHELP - ALL
//        Dataset dataset = Dataset.winHelp;
//        String inputPath = "data/branavan/winHelpHLA/winHelpRL.cleaned.objType.norm.docs.all.newAnnotation";
//        String manualAnnotation = "data/branavan/winHelpHLA/winHelpRL.cleaned.objType.norm.docs.all.newAnnotation.gold.manual";
//        String textCorrections = "data/branavan/winHelpHLA/winHelpRL.cleaned.objType.norm.docs.all.newAnnotation.textCorrections";
////        String inputPath = "data/branavan/winHelpHLA/winHelpRL.cleaned.objType.norm.docs.single.newAnnotation";
//        String inputPathRecordAlignments = "results/output/winHelp/alignments/model_3_docs_no_null_newAnnotation/all/stage1.train.pred.1.sorted";
////        String inputPathRecordAlignments = "data/branavan/winHelpHLA/winHelpRL.cleaned.objType.norm.docs.single.newAnnotation.align";
////        // ALIGNED
////        InputType type = InputType.aligned;
////        String outputFile = "data/branavan/winHelpHLA/winHelpRL.cleaned.objType.norm.docs.all.newAnnotation.aligned.edus";
////        String outputFileAlignments = "data/branavan/winHelpHLA/winHelpRL.cleaned.objType.norm.docs.all.newAnnotation.aligned.align";        
////        // GOLD
//        InputType type = InputType.goldStandard;
//        String outputFile = "data/branavan/winHelpHLA/winHelpRL.cleaned.objType.norm.docs.all.newAnnotation.gold.edus";        
//        String outputFileAlignments = "data/branavan/winHelpHLA/winHelpRL.cleaned.objType.norm.docs.all.newAnnotation.gold.align";        
//        System.out.println("Creating " + outputFile);
//        new ExportExamplesToEdusFile(type, dataset, inputPath, inputPathRecordAlignments, manualAnnotation, textCorrections, outputFile, outputFileAlignments).execute();        
        
        // WINHELP - FOLDS
//        Dataset dataset = Dataset.winHelp;   
//        String manualAnnotation = "data/branavan/winHelpHLA/winHelpRL.cleaned.objType.norm.docs.all.newAnnotation.gold.manual";
//        String textCorrections = "data/branavan/winHelpHLA/winHelpRL.cleaned.objType.norm.docs.all.newAnnotation.textCorrections";
//        int folds = 10;
//        for (int fold = 1; fold <= folds; fold++)
//        {
//            String inputPath = "data/branavan/winHelpHLA/folds/docs.newAnnotation/winHelpFold"+fold+"Train";    
//            String inputPathRecordAlignments = "results/output/winHelp/alignments/model_3_docs_no_null_newAnnotation/fold"+fold+"/stage1.train.pred.1.sorted";
//            // ALIGNED
////            InputType type = InputType.aligned;
////            String outputFile = "data/branavan/winHelpHLA/folds/docs.newAnnotation/winHelpFold"+fold+"Train.aligned.edus";
////            String outputFileAlignments = "data/branavan/winHelpHLA/folds/docs.newAnnotation/winHelpFold"+fold+"Train.aligned.align";
////            // GOLD
//            InputType type = InputType.goldStandard;
//            String outputFile = "data/branavan/winHelpHLA/folds/docs.newAnnotation/winHelpFold"+fold+"Train.gold.edus";
//            String outputFileAlignments = "data/branavan/winHelpHLA/folds/docs.newAnnotation/winHelpFold"+fold+"Train.gold.align";
//            System.out.println("Creating " + outputFile);
//            new ExportExamplesToEdusFile(type, dataset, inputPath, inputPathRecordAlignments, manualAnnotation, textCorrections, outputFile, outputFileAlignments).execute();       
//        }
    }
}
