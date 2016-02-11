package induction.utils;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.trees.Tree;
import fig.basic.LogInfo;
import fig.exec.Execution;
import induction.LearnOptions;
import induction.Options;
import induction.Utils;
import induction.problem.AParams;
import induction.problem.Vec;
import induction.problem.event3.CatField;
import induction.problem.event3.EventType;
import induction.problem.event3.Field;
import induction.problem.event3.StrField;
import induction.problem.event3.generative.GenerativeEvent3Model;
import induction.problem.event3.params.CatFieldParams;
import induction.problem.event3.params.EventTypeParams;
import induction.problem.event3.params.Params;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author sinantie
 */
public class ComputeAmrEstimates
{
    
    ComputeAmrEstimatesOptions opts;
    LearnOptions lopts;
    GenerativeEvent3Model model;
    private final Pattern sensePattern = Pattern.compile(".-[0-9]+$");
    enum Type {CONCEPT, ROLE, CONSTANT};
    
    private final Map<String, Dictionary> conceptMap, roleMap, constantMap;
            
    public ComputeAmrEstimates(ComputeAmrEstimatesOptions opts)
    {
        this.opts = opts;    
        this.lopts = opts.modelOpts.stage1;
        conceptMap = new HashMap<>();
        roleMap = new HashMap<>();
        constantMap = new HashMap<>();
    }
    
    public void execute()
    {
        readAlignments();
        
        model = new GenerativeEvent3Model(opts.modelOpts);
        model.readExamples();
        model.init(Options.InitType.random, opts.modelOpts.initRandom, "");
        Params params = (Params)model.getParams();
        LogInfo.track("bootstrap params");
        bootstrapIgnoreFields(params);
        LogInfo.end_track();
        LogInfo.track("Optimise params");
        if (lopts.useVarUpdates)
        {
            params.optimiseVar(lopts.smoothing);
        }
        else
        {
            params.optimise(lopts.smoothing);
        }
        LogInfo.end_track();
        String name = "bootstrap";
        model.saveParams(name);
        if(!model.getOpts().dontOutputParams)
            params.outputNonZero(Execution.getFile(name+".params.gz"), AParams.ParamsType.PROBS);
    }
    
    /**
     * 
     * Initialise counts for event3 parameter vectors based on alignments. More specifically,
     * 1. For each categorical field, and for each value that maps to a concept or constant, 
     * there exists a vector of size W, which is either artifical set to a number or to the size
     * of the training vocabulary. We then find the corresponding entry in the concept/constant maps
     * acquired in a previous step, and for every aligned word we add to the counts vector the number of times
     * it has been observed in the training set.
     * 2. Likewise for the (none) field of each eventType we iterate over the corresponding concept values and perform step 1.
     * 
     * Note that we do not take into account the role each concept/constant corresponds to. For example, words for
     * the concept go-01, will be added both to the values of fields @destination and @source for all the event types they may belong to. 
     * This will probably add some noise, but will increase the counts for more rarely-seen concepts.
     */
    private void bootstrapIgnoreFields(Params params)
    {
        for(EventType eventType : model.getEventTypes())
        {
            EventTypeParams eventTypeParams = params.eventTypeParams[eventType.getEventTypeIndex()];
            bootstrapCatFieldEmissions(eventType, eventTypeParams);
            bootstrapNoneFieldEmissions(eventType, eventTypeParams);            
        }
        
    }
    
    private void bootstrapCatFieldEmissions(EventType eventType, EventTypeParams eventTypeParams)
    {
        int f = 0;
        for(Field field : eventType.getFields())
        {
            if(field instanceof CatField)
            {
                CatField catField = (CatField)field;
                for(int v = 0; v < field.getV(); v++)
                {                    
                    Vec vecToUpdate = ((CatFieldParams)eventTypeParams.fieldParams[f]).emissions[v];
                    bootstrapVecWithWords(vecToUpdate, stripSense(catField.valueToString(v)), conceptMap);
                    bootstrapVecWithWords(vecToUpdate, stripSense(catField.valueToString(v)), constantMap);
                }
            }
            f++;
        }
    }
    
    private void bootstrapNoneFieldEmissions(EventType eventType, EventTypeParams eventTypeParams)
    {
        Vec vecToUpdate = eventTypeParams.noneFieldEmissions;
        String eventTypeName = eventType.getName();        
        bootstrapVecWithWords(vecToUpdate, eventTypeName, conceptMap);
        int conceptFieldIndex = eventType.getFieldIndex("concept");
        if(conceptFieldIndex != -1)
        {
            Field conceptField = eventType.getFields()[conceptFieldIndex];
            String[] conceptNames = null;
            if(conceptField instanceof StrField)
                conceptNames = ((StrField)conceptField).valuesToStringArray();
            else if(conceptField instanceof CatField)
                conceptNames = ((CatField)conceptField).valuesToStringArray();
            if(conceptNames != null)
            {
                for(String concept : conceptNames)
                {
                    bootstrapVecWithWords(vecToUpdate, stripSense(concept), conceptMap);
                }
            }
        }        
    }
    
    private void bootstrapVecWithWords(Vec vec, String key, Map<String, Dictionary> map)
    {
         Dictionary words = map.get(key);
        if(words != null)
        {
            for(Entry<String, Integer> word : words.vocabulary.getEntriesFreqs())
            {
                int wordIndex = getWordIndexSafe(word.getKey());
                if(wordIndex != -1)
                    vec.addCount(wordIndex, word.getValue());                
            }
        }        
    }
    
    private void readAlignments()
    {
        if(new File(opts.sentencesFile).exists() && new File(opts.GHKMTreeFile).exists() && new File(opts.alignmentsFile).exists())
        {
            String[] sentences = Utils.readLines(opts.sentencesFile);
            String[] trees = Utils.readLines(opts.GHKMTreeFile);
            String[] alignments = Utils.readLines(opts.alignmentsFile);
            int numOfNull = 0;
            for(int i = 0; i < sentences.length; i++)
            {
                String[] words = sentences[i].split(" ");
                try
                {
                    Tree tree = Tree.valueOf(trees[i]);
                    if(tree != null)
                    {
                        List<CoreLabel> yield = new ArrayList<>();
                        tree.yield(yield);
                        String[] alignment = alignments[i].split(" ");
                        for(String token : alignment)
                        {
                            String[] yieldWord = token.split("-");
                            int yieldIndex = Integer.valueOf(yieldWord[0]);
                            int wordIndex = Integer.valueOf(yieldWord[1]);
                            if(yieldIndex < yield.size() && wordIndex < words.length)
                            {
                                String key = yield.get(yieldIndex).word();
                                updateVocabulary(key, words[wordIndex]);

                                //System.out.println(key + " " + words[wordIndex]);
                            }
                        } // for
                    } // if
                    else
                    {
                        numOfNull++;
                    }
                }
                catch(Exception ioe)
                {
                    LogInfo.error("Error reading tree " + trees[i]);
                }
            } // for
            Utils.logs("Num of null trees: " + numOfNull);
        } // if
    }
    
    private void updateVocabulary(String key, String word)
    {
        Map<String, Dictionary> map;
        switch(determineType(key))
        {
            case CONCEPT : default: map = conceptMap; key = normaliseConcept(opts.stripConceptSense, key); break;
            case CONSTANT : map = constantMap; key = normaliseConstant(key); break;
            case ROLE : map = roleMap; key = normaliseRole(key); break;
        }
        Dictionary dict = map.get(key);
        if(dict == null)
        {
            dict = new Dictionary(key);
            map.put(key, dict);
        }
        dict.addWord(word);
    }
    
    private Type determineType(String word)
    {
        char c = word.charAt(0);
        switch(c)
        {
            case '/' : return Type.CONCEPT;
            case ':' : return Type.ROLE;
            default  : return Type.CONSTANT;
        }
    }
    
    private String normaliseConcept(boolean stripSense, String str)
    {        
        if(str.charAt(0) == '/')
            str = str.substring(1);
        if (stripSense)
        {
            str = stripSense(str);
        }
        return str;
    }
    
    private String normaliseRole(String str)
    {        
        if(str.charAt(0) == ':')
            str = str.substring(1);
        int index = str.indexOf("-of");
        return index > 0 ? str.substring(0, index) : str;        
    }
    
    private String normaliseConstant(String str)
    {                
        return str.replaceAll("`", "");
    }
    
    private String stripSense(String word)
    {
        Matcher m = sensePattern.matcher(word);
        if(m.find())
        {
            return word.substring(0, m.start() + 1);
        }
        return word;
    }
    
    private int getWordIndexSafe(String word)
    {
        return model.getWordIndexSafe(word);
    }
    
    public void testExecute()
    {
        execute();
    }
    
    class Dictionary
    {
        String key;
        HistMap<String> vocabulary;

        public Dictionary(String key)
        {
            this.key = key;
            this.vocabulary = new HistMap<>();
        }
        
        void addWord(String word)
        {
            vocabulary.add(word);
        }
        
        
    }
}
