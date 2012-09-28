package induction.utils;

import fig.basic.IOUtils;
import fig.basic.LogInfo;
import fig.exec.Execution;
import induction.Options.InitType;
import induction.problem.AExample;
import induction.problem.event3.Event3Model;
import induction.problem.event3.Example;
import induction.problem.event3.Widget;
import induction.problem.event3.generative.GenerativeEvent3Model;
import induction.utils.HistMap.Counter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

/**
 *
 * @author konstas
 */
public class ExtractRecordsStatistics
{
    ExtractRecordsStatisticsOptions opts;    
    Event3Model model;
    List<ExampleRecords> examples;
    HistMap<Integer> repeatedRecords;
    HistMap<String> sentenceNgrams, documentNgrams;
    
    public ExtractRecordsStatistics(ExtractRecordsStatisticsOptions opts)
    {
        this.opts = opts;
    }                
    
    public void execute()
    {
        model = new GenerativeEvent3Model(opts.modelOpts);
        model.init(InitType.staged, opts.modelOpts.initRandom, "");
        model.readExamples();
        examples = new ArrayList<ExampleRecords>(model.getExamples().size());
        parseExamples();
        if(opts.writePermutations)
        {
            LogInfo.logs("Writing permutations...");
            writePermutations();
        }
        if(opts.countRepeatedRecords)
        {
            LogInfo.logs("Count repeated records in a permutation...");
            countRepeatedRecords();
            writeObject(repeatedRecords, "repeatedRecords");
        }
        if(opts.countSentenceNgrams)
        {
            LogInfo.logs("Count ngrams in each sentence...");
            countSentenceNgrams();
            writeObject(sentenceNgrams, "sentenceNgrams");
        }
        if(opts.countDocumentNgrams)
        {
            LogInfo.logs("Count ngrams in each document...");
            countDocumentNgrams();
            writeObject(documentNgrams, "documentNgrams");
        }
    }
    
    public void parseExamples()
    {       
        for(AExample ex: model.getExamples())
        {
            ExampleRecords er = new ExampleRecords();
            Example e = (Example)ex;
            int[] text = e.getText();
            int[] startIndices = e.getStartIndices();
            Widget w = e.getTrueWidget();
            List<Integer> eventTypes = new ArrayList<Integer>();
            for(int i = 1; i < startIndices.length; i++)
            {                
                for(int[] events : w.getEvents())
                {
                    int eventId = events[startIndices[i-1]];
                    if(eventId != -1)
                    {                        
                        int element = -1;
                        switch(opts.exportType)
                        {
                            case record : element = eventId; break;
                            default: case recordType : element = e.events.get(eventId).getEventTypeIndex(); break;
                        }
                        // collapse consecutive records having the same type                        
//                        if(eventTypes.isEmpty() || eventType != eventTypes.get(eventTypes.size() - 1))
                        // we don't allow repetitions of record types in the same sentence
                        if(eventTypes.isEmpty() || !eventTypes.contains(element))
                            eventTypes.add(element);
                    }
                }
                // default input is each clause (splitted at punctuation) goes to a seperate line
                if(!eventTypes.isEmpty() && (opts.splitClauses || endOfSentence(model.wordToString(text[startIndices[i]-1]))))
                {
                    er.addSentence(new ArrayList(eventTypes));
                    eventTypes.clear();
                } // if
            } // for
            examples.add(er);
        } // for
    }
    
    private boolean endOfSentence(String token)
    {
        return token.equals(".") || token.equals("./.") || token.equals(":") || token.equals("--/:");
    }
      
    private void writePermutations()
    {
        try
        {
            PrintWriter out = IOUtils.openOut(Execution.getFile("permutations"));
            for(ExampleRecords p : examples)
                out.println(p);
            out.close();
        } catch (IOException ex)
        {
            LogInfo.error(ex);
        } catch (NullPointerException ex)
        {
            LogInfo.error(ex);
        }        
    }
    
    private void countRepeatedRecords()
    {
        repeatedRecords = new HistMap<Integer>();
        for(ExampleRecords er : examples)
        {
            HistMap<Integer> repRecsInSent = new HistMap<Integer>();
            for(Integer i : er.getPermutation())
                repRecsInSent.add(i);            
            for(Entry e : repRecsInSent.getEntries())
            {
                if(((Counter)e.getValue()).getValue() > 1)
                    repeatedRecords.add(((Integer)e.getKey()));
            }
        } // for
    }
    
    private void countSentenceNgrams()
    {
        sentenceNgrams = new HistMap<String>();
        for(ExampleRecords er : examples)
        {
            for(int i = 0; i < er.numberOfSentences(); i++)
                sentenceNgrams.add(er.sentenceToString(i));
        } // for
    }
    
    private void countDocumentNgrams()
    {        
        documentNgrams = new HistMap<String>();        
        for(ExampleRecords er : examples)
        {
            documentNgrams.add(er.toString());            
        } // for
    }
    
    private void writeObject(Object obj, String filename)
    {
        try
        {
            PrintWriter out = IOUtils.openOut(Execution.getFile(filename));
            out.print(obj);
            out.close();
        }
        catch(Exception e)
        {
            LogInfo.error(e);
        }
    }
    public void testExecute()
    {
        model = new GenerativeEvent3Model(opts.modelOpts);
        model.init(InitType.staged, opts.modelOpts.initRandom, "");
        model.readExamples();
        examples = new ArrayList<ExampleRecords>(model.getExamples().size());
        parseExamples();
        if(opts.writePermutations)
            for(ExampleRecords p : examples)
            {
                System.out.println(p);
            }
        if(opts.countRepeatedRecords)
        {
            countRepeatedRecords();
            System.out.println(repeatedRecords);
        }
        if(opts.countSentenceNgrams)
        {
            countSentenceNgrams();
            System.out.println(sentenceNgrams);
        }
        if(opts.countDocumentNgrams)
        {
            countDocumentNgrams();
            System.out.println(documentNgrams);
        }
    }
    
    class ExampleRecords
    {
        private List<List<Integer>> sentences;

        public ExampleRecords()
        {
            sentences = new ArrayList<List<Integer>>();
        }
                
        void addSentence(List<Integer> types)
        {
            sentences.add(types);
        }

        List<Integer> getPermutation()
        {
            List<Integer> permutation = new ArrayList<Integer>();
            for(List<Integer> sentence : sentences)
            {
                for(Integer i : sentence)
                    permutation.add(i);
            }
            return permutation;
        }
        
        int numberOfSentences()
        {
            return sentences.size();
        }
        String sentenceToString(int i)
        {
            assert i < sentences.size();
            StringBuilder str = new StringBuilder();
            for(Integer r : sentences.get(i))
                str.append(r).append(" ");
            return str.toString();
        }
        
        @Override
        public String toString()
        {
            StringBuilder str = new StringBuilder();
            for(List<Integer> sentence : sentences)
            {
                for(Integer i : sentence)
                    str.append(i).append(" ");
                if(opts.delimitSentences)
                    str.append("| ");
            }
            return str.toString().trim();
        }        
    }
}
