package induction.utils;

import fig.basic.IOUtils;
import fig.basic.LogInfo;
import fig.exec.Execution;
import induction.Options;
import induction.Options.InitType;
import induction.problem.AExample;
import induction.problem.event3.Event3Model;
import induction.problem.event3.Example;
import induction.problem.event3.Widget;
import induction.problem.event3.generative.GenerativeEvent3Model;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author konstas
 */
public class ExtractRecordsStatistics
{
    Options opts;    
    Event3Model model;
    List<Permutation> examples;

    public ExtractRecordsStatistics(Options opts)
    {
        this.opts = opts;
    }                
    
    public void execute()
    {
        model = new GenerativeEvent3Model(opts);
        model.init(InitType.staged, opts.initRandom, "");
        model.readExamples();
        examples = new ArrayList<Permutation>(model.getExamples().size());
        convertToEventTypeStrings(false);
        LogInfo.logs("Writing permutations...");
        writePermutations();
    }
    
    public void convertToEventTypeStrings(boolean splitClauses)
    {       
        for(AExample ex: model.getExamples())
        {
            Permutation p = new Permutation();
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
//                        int eventType = e.events.get(eventId).getEventTypeIndex();
                        int eventType = eventId;
                        // collapse consecutive records having the same type                        
//                        if(eventTypes.isEmpty() || eventType != eventTypes.get(eventTypes.size() - 1))
                        // we don't allow repetitions of record types in the same sentence
                        if(eventTypes.isEmpty() || !eventTypes.contains(eventType))
                            eventTypes.add(eventType);
                    }
                }
                // default input is each clause (splitted at punctuation) goes to a seperate line
                if(splitClauses || endOfSentence(model.wordToString(text[startIndices[i]-1])))
                {
                    p.addSentence(new ArrayList(eventTypes));
                    eventTypes.clear();
                } // if
            } // for
            examples.add(p);
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
            for(Permutation p : examples)
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
    
    public void testExecute()
    {
        model = new GenerativeEvent3Model(opts);
        model.init(InitType.staged, opts.initRandom, "");
        model.readExamples();
        examples = new ArrayList<Permutation>(model.getExamples().size());
        convertToEventTypeStrings(false);
        for(Permutation p : examples)
            System.out.println(p);
    }
    
    class Permutation
    {
        List<List<Integer>> sentences;

        public Permutation()
        {
            sentences = new ArrayList<List<Integer>>();
        }
                
        void addSentence(List<Integer> types)
        {
            sentences.add(types);
        }

        @Override
        public String toString()
        {
            StringBuilder str = new StringBuilder();
            for(List<Integer> sentence : sentences)
            {
                for(Integer i : sentence)
                    str.append(i).append(" ");
            }
            return str.toString().trim();
        }        
    }
}
