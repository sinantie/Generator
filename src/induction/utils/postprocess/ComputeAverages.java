package induction.utils.postprocess;

import fig.basic.LogInfo;
import induction.Utils;
import induction.problem.event3.generative.GenerativeEvent3Model;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author sinantie
 */
public class ComputeAverages
{   
    ComputeAveragesOptions opts;
    
    private Action action;
    List<String[]> examples;    
    
    public ComputeAverages(ComputeAveragesOptions opts)
    {
        this.opts = opts;        
        examples = new ArrayList<String[]>();
    }
    
    public void execute()
    {
        switch(opts.actionType)
        {
            case averageAlignmentsPerExample : action = new AverageAlignmentsPerExample(); break;
            case averageFieldsWithNoValuePerRecord : action = new AverageFieldsWithNoValuePerRecord(opts.record, opts.totalNumberOfFields); break;
            case averageWordsPerSentence : action = new AverageWordsPerSentence(); break;
            case averageSentencesPerDocument : action = new AverageSentencesPerDocument(); break;
        }
        readExamples();                
        // process examples
        for(String[] example : examples)
        {
            action.act(example);
        }
        System.out.println(action.result());
    }    
    
    public void readExamples()
    {
        try 
        {
            String example[];
            if(opts.modelOpts.examplesInSingleFile)
            {
                String key = null;
                StringBuilder str = new StringBuilder();
                for(String line : Utils.readLines(opts.modelOpts.inputLists.get(0)))
                {
                    if(line.startsWith("Example_") || line.equals("$NAME"))
                    {
                        if(key != null) // only for the first example
                        {
                            example = GenerativeEvent3Model.extractExampleFromString(str.toString());                        
                            action.act(example);
                            str = new StringBuilder();
                        }
                        key = line;
                    } // if
                    str.append(line).append("\n");
                }  // for
                // don't forget last example
                example = GenerativeEvent3Model.extractExampleFromString(str.toString());
                examples.add(example);
            } // if
            else
            {
                for(String line : Utils.readLines(opts.modelOpts.inputPaths.get(0))) // contains list of .events files
                {
    //                    System.out.println(line);
                    String events = Utils.readFileAsString(line);
                    String text = Utils.readFileAsString(Utils.stripExtension(line)+".text");
                    String[] ex = {events, text};
                    examples.add(ex);
                }
            }            
        }
        catch(IOException ioe) {
            LogInfo.error(ioe);
        }
    }
    public void testExecute()
    {
        execute();
    }
    
    /**
     * find the average number of gold standard alignments per example
     */
    static class AverageAlignmentsPerExample implements Action {
        int totalAlignedEvents = 0, totalExamples = 0;
        @Override
        public Object act(Object in)
        {
            String[] example = (String[])in;
            // 4th entry are the alignments that have the format line_no [event_id]+
            // we just count the number of events
            totalAlignedEvents += example[3].split(" ").length - 1;
            totalExamples++;
            return null;
        }

        @Override
        public Object result()
        {
            return new Double((double)totalAlignedEvents / (double)totalExamples);
        }        
    }
    
    static class AverageFieldsWithNoValuePerRecord implements Action {

        private String record;
        private int totalNumberOfFields, totalEmpty, totalExamples;
        
        public AverageFieldsWithNoValuePerRecord(String record, int totalNumberOfFields)
        {
            this.record = record;
            this.totalNumberOfFields = totalNumberOfFields;
        }
        
        @Override
        public Object act(Object in)
        {
            String[] example = (String[])in;
            // 3rd entry are the events
            String events[] = example[2].split("\n");
            String recordEvent = null;
            // capture the line with the event we are looking for
            for(String event : events)
                if(event.contains(".type:"+record))
                    recordEvent = event;
            if(recordEvent != null)
            {
                // count the number of non-empty fields. 
                // Then subtract from the total number of fields.
                // Note that some records have a fixed number of fields, whereas others
                // have a variable number, of only the non-empty. That's why
                // it's safer to subtract from the total
                int total = 0;
                for(String fieldValue : recordEvent.split("\t"))
                {
                    // we need actual fields
                    if(fieldValue.contains("@") || fieldValue.contains("$") || fieldValue.contains("#"))
                    {
                        if(!fieldValue.contains("--")) // capture only the non-empty
                            total++;
                    }
                } // for                
                totalEmpty += totalNumberOfFields - total;
            } // if 
            totalExamples++;
            return null;
        }

        @Override
        public Object result()
        {
            return new Double((double)totalEmpty / (double)totalExamples);
        }
        
    }
    
    private static class AverageWordsPerSentence implements Action
    {      
        private int words, sentences;
        
        @Override
        public Object act(Object in)
        {
            String[] example = (String[])in;
            // 2nd entry is the text
            for(String token : example[1].split("\\s"))
            {
                if(Utils.isSentencePunctuation(token))
                    sentences++;
                words++;
            }
            return null;
        }

        @Override
        public Object result()
        {
            return new Double((double)words / (double)sentences);
        }
    }
    
    private static class AverageSentencesPerDocument implements Action
    {
        private int docs, sentences;
        @Override
        public Object act(Object in)
        {
            String[] example = (String[])in;
            // 2nd entry is the text
            for(String token : example[1].split("\\s"))
            {
                if(Utils.isSentencePunctuation(token))
                    sentences++;
            }
            docs++;
            return null;
        }

        @Override
        public Object result()
        {
            return new Double((double)sentences / (double)docs);
        }
        
    }
    
    interface Action {
        public Object act(Object in);        
        public Object result();
    }
}
