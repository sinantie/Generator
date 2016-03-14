package induction.problem.event3.generative.generation;

import edu.berkeley.nlp.ling.Tree;
import induction.problem.event3.CFGRule;
import induction.problem.event3.Widget;
import induction.problem.event3.params.Parameters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author konstas
 */
public class GenWidget extends Widget
{
    protected int[] text, nums;
    protected String[] multipleReferences;
    protected double[] scores;
    protected Collection<Integer> trueEvents;
    LinkedList<Tree<String>> queue;
    
    public GenWidget(int [][]events, int[][] fields, int[][] gens,
                     int [][] numMethods,
                     int [] text,
                     int[] startIndices,
                     HashSet<Integer>[] eventTypeAllowedOnTrack,
                     Map<Integer, Integer> eventTypeIndices, String[] multipleReferences)
    {
        super(events, fields, gens, numMethods, startIndices,
              eventTypeAllowedOnTrack, eventTypeIndices);
        if(text != null)
        {
            this.text = text;
            nums = new int[text.length];
            Arrays.fill(nums, -1);
        }
        this.multipleReferences = multipleReferences;
        scores = new double[Parameters.NUMBER_OF_METRICS_GEN];
    }

    /**
     * Constructor for gold-standard widget.
     * @param text the gold-standard text, for calculating generation-oriented metrics
     * @param multipleReferences array of multiple gold references
     */
    public GenWidget(int[] text, String[] multipleReferences)
    {
         this(null, null, null, null, text, null, null, null, multipleReferences);
    }

    /**
     * Constructor for gold-standard widget.
     * @param events the true events, for calculating Precision, Recall and F-1
     * @param text the gold-standard text, for calculating generation-oriented metrics
     * @param startIndices the array holding the starting indices of each line in the text, for computing record WER
     * @param multipleReferences array of multiple gold references
     */
    public GenWidget(int[][] events, int[] text, int[] startIndices, String[] multipleReferences)
    {
         this(events, null, null, null, text, startIndices, null, null, multipleReferences);
         trueEvents = new ArrayList<>();
         int prev = -5, cur;
         // consider 1st track first seperately
         for(int j = 0; j < events[0].length; j++)
         {
             cur = new Integer(events[0][j]);
             if(prev != cur)
                 trueEvents.add(cur);
             prev = cur;
         }
         // consider next tracks as supplementary (only >-1)
         for(int i = 1; i < events.length; i++)
         {
             for(int j = 0; j < events[0].length; j++)
             {
                 cur = new Integer(events[i][j]);
                 if(prev != cur && cur > -1)
                     trueEvents.add(cur);
                 prev = cur;
             }
         }
    }

    public GenWidget(int [][]events, int[][] fields, int[][] gens,
                     int [][] numMethods,
                     int [] text,
                     int[] startIndices,
                     HashSet<Integer>[] eventTypeAllowedOnTrack,
                     Map<Integer, Integer> eventTypeIndices, String startSymbol, String[] multipleReferences)
    {
        this(events, fields, gens, numMethods, text, startIndices, eventTypeAllowedOnTrack, eventTypeIndices, multipleReferences);        
        if(startSymbol != null)
        {            
            recordTree = new Tree<>(startSymbol, false);
            queue = new LinkedList<>();            
            queue.push(recordTree);
        }
    }
    
    public GenWidget(int[][] events, int[] text, int[] startIndices, Tree<String> recordTree, String[] multipleReferences)
    {
        this(events, text, startIndices, multipleReferences);
        this.recordTree = recordTree;
    }
    
    void addEdge(CFGRule rule)
    {
        Tree<String> first = queue.poll();
        if(first.getLabelNoSpan().equals(rule.getLhsToString()))
        {
            List<Tree<String>> newChildren = new ArrayList<>(2);
            rule.getRhsListToString().stream()
                    .map(rhs -> newChildren.add(new Tree<>(rhs, false)));            
            first.setChildren(newChildren);
            queue.addAll(0, newChildren);
        }
        else
        {
            addEdge(rule);
        }        
    }
    
    public int[] getNums()
    {
        return nums;
    }

    public int[] getText()
    {
        return text;
    }  
    
    public double[] getScores()
    {
        return scores;
    }
        
    @Override
    public String toString()
    {
        String out = "";
        for(int i = 0; i < text.length; i++)
        {
            out += (nums[i] > -1 ? nums[i] :
                    text[i]) + " ";
        }
        return out.trim();
    }


}
