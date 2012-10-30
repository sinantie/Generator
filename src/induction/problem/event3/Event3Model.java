package induction.problem.event3;

import edu.berkeley.nlp.ling.Tree;
import edu.berkeley.nlp.ling.Trees.PennTreeReader;
import induction.problem.event3.json.JsonWrapper;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import fig.basic.FullStatFig;
import induction.problem.event3.generative.generation.SemParseWidget;
import induction.problem.event3.generative.generation.GenWidget;
import induction.problem.event3.params.Parameters;
import fig.basic.Indexer;
import fig.basic.LogInfo;
import fig.exec.Execution;
import induction.LearnOptions;
import induction.Options;
import induction.Options.InitType;
import induction.Options.InputFormat;
import induction.Options.JsonFormat;
import induction.Options.ModelType;
import induction.Options.NgramWrapper;
import util.Stemmer;
import induction.Utils;
import induction.utils.linearregression.LinearRegressionWekaWrapper;
import induction.ngrams.KylmNgramWrapper;
import induction.ngrams.NgramModel;
import induction.ngrams.RoarkNgramWrapper;
import induction.ngrams.SrilmNgramWrapper;
import induction.problem.AExample;
import induction.problem.AInferState;
import induction.problem.AParams;
import induction.problem.APerformance;
import induction.problem.AWidget;
import induction.problem.InductionUtils;
import induction.problem.dmv.generative.GenerativeDMVModel;
import induction.problem.event3.json.JsonResult;
import induction.problem.wordproblem.WordModel;
import induction.utils.linearregression.LinearRegressionOptions;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * A model of events and their text summaries (ACL 2009).
 * Model:
 *  - Select events to talk about
 *  - Generate text of those events
 * An event type has a fixed set of fields.
 * Each field has a type (numeric, categorical, symbol, string).
 * Each event has a fixed event type and values for each of the fields.
 * The model is essentially a hierarchical labelled segmentation process.
 *
 * Change log:
 *  - 03/02/10: complete rewrite in Java
 *
 *  - 02/08/09: make field type explicit
 *  - 02/08/09: make the non-field type a possible field
 *  - 02/10/09: geometric distribution on numeric noise
 *  - 02/11/09: multiple trueEvents
 *  - 02/13/09: redo event model
 *  - 02/16/09: add tracks
 *  - 02/17/09: add word roles
 *  - 02/18/09: add labels, field set
 *
 * @author konstas
 */
public abstract class Event3Model extends WordModel
//                                            <Widget, Params, 
////                                           Performance,
////                                           Example, Event3InferState> implements Serializable
//                                           Example> 
                                  implements Serializable
{

    protected EventType[] eventTypes = null;  // Filled in later
    protected Indexer<String> eventTypeNameIndexer = new Indexer<String>(); // Temporary
    protected ArrayList<EventType> eventTypesBuffer = new ArrayList<EventType>();

    // Stuff for tracks
    protected int C = opts.eventTypeTracks.length;
    protected int PC = 1 << C; // Number of choices for the top level
    public HashSet<Integer>[] eventTypeAllowedOnTrack = null;

    // Stuff for labels (each word has a label)
    protected static Indexer<String> labelIndexer = new Indexer<String>();
    protected static int none_lb, misc_lb;
    // Word roles
    private Indexer<String> wordRoleNameIndexer = new Indexer<String>();
    private static HashMap<String, Integer> wordRoleMap = new HashMap<String,Integer>();
    private int eventTypeIndex = -1;
    private static int other_role = -1;
    // map of fields names and indices - used for semantic parsing. It is filled in
    // at stagedInitParams
    protected HashMap<Integer, HashMap<String, Integer>> fieldsMap;
    protected Indexer<String> testSetWordIndexer = new Indexer<String>();
    protected Map<Integer, Integer> depsCrossWordMap;
    protected GenerativeDMVModel depsModel;
    protected NgramModel secondaryNgramModel;
    // map of pcfg rules read from input file, indexed on the lhs non-terminal. 
    // The internal hashmap maps the cfg rule to the position in the associated parameter vector.
    protected Map<Integer, HashMap<CFGRule, Integer>> cfgRules;
    protected Map<Integer, Tree<String>> grammarTrees;
    protected Indexer<String> rulesIndexer = new Indexer<String>();
    
    public Event3Model(Options opts)
    {
        super(opts);
        none_lb = labelIndexer.getIndex("-");
        misc_lb = labelIndexer.getIndex("*");
    }

    public NgramModel getSecondaryNgramModel()
    {
        return secondaryNgramModel;
    }

    public Indexer<String> getRulesIndexer()
    {
        return rulesIndexer;
    }
    
    ////////////////////////////////////////////////////////////
    // Generic routines
    private int[] newIntArray(int n, int x)
    {
        return Utils.set(new int[n], x); // Helper
    }

    public static String processWord(String word)
    {
        if(Options.stemAll)
        {
            return Stemmer.stem(word);
        }
        return word;
    }

    public int getWordIndex(String str)
    {
        return wordIndexer.getIndex(processWord(str));
    }

    public int getTestSetWordIndex(String str)
    {
        return testSetWordIndexer.getIndex(processWord(str));
    }

    public Indexer<String> getTestSetWordIndexer()
    {
        return testSetWordIndexer;
    }
        
    public int getT()
    {
        return eventTypes.length;
    }

    public int getPC()
    {
        return PC;
    }

    public int getC()
    {
        return C;
    }

    public static int getNone_lb()
    {
        return none_lb;
    }

    public HashMap<Integer, HashMap<String, Integer>> getFieldsMap()
    {
        return fieldsMap;
    }    

    /**
     * Return the none record
     * @return
     */
    public int none_t()
    {
        return getT();
    }
    /**
     * START and END are regarded as boundary records.
     * @return
     */
    public int boundary_t()
    {
        return none_t() + 1;
    }
    public String eventTypeToString(int eventTypeIndex)
    {
        if (eventTypeIndex == none_t())
        {
            return "(none)";
        }
        else if(eventTypeIndex == boundary_t())
        {
            return "(boundary)";
        }
        else 
        {
            return eventTypes[eventTypeIndex].name;
        }
    }
    public String[] eventTypeStrArray()
    {
        String[] out = new String[eventTypes.length + 2];
        for(int i = 0; i < out.length; i++)
        {
            out[i] = eventTypeToString(i);
        }        
        return out;
    }
    public String[] cfgRulesRhsStrArray(Map<CFGRule, Integer> map)
    {
        String[] out = new String[map.size()];
        for(Entry<CFGRule, Integer> e : map.entrySet())
        {
            out[e.getValue()] = e.getKey().getRhsToString();
        }
        return out;
    }
    
    public int getCfgRuleIndex(CFGRule rule)
    {        
        return cfgRules.get(rule.getLhs()).get(rule);        
    }
    
    public HashMap<CFGRule, Integer> getCfgCandidateRules(int lhs)
    {
        return cfgRules.get(lhs);
    }
    
    public EventType[] getEventTypes()
    {
        return eventTypes;
    }

    public int[] getEventTypeIndices()
    {
        int[] out = new int[eventTypes.length];
        for(int i = 0; i < out.length; i++)
        {
            out[i] = eventTypes[i].getEventTypeIndex();
        }
        return out;
    }

    public Indexer<String> getEventTypeNameIndexer()
    {
        return eventTypeNameIndexer;
    }
    
    // Stuff for tracks
    public String cstr(int c)
    {
        return opts.eventTypeTracks[c];
    }
    public String pcstr(int pc)
    {
        return Constants.setstr(C, pc);
    }
    public String[] pcstrArray()
    {
        String[] out = new String[PC];
        for(int i = 0; i < PC; i++)
        {
            out[i] = pcstr(i);
        }
        return out;
    }
    // Stuff for labels (each word has a label)
    public static int LB()
    {
        return labelIndexer.size();
    }
    
    public static String labelToString(int lb)
    {
        return labelIndexer.getObject(lb);
    }
    public static String[] labelsToStringArray()
    {
        String[] out = new String[labelIndexer.size()];
        return labelIndexer.getObjects().toArray(out);
    }
    public static boolean isAlpha(String s)
    {
        return s.matches("\\p{Alpha}+");
    }
    public static int getLabelIndex(List<String> textStr, int i)
    {
        // For now, let the label of a word be the word after it if it's a number
        if (Constants.str2num(textStr.get(i)) == Constants.NaN)
        {
            return none_lb;
        }
        else if (i+1 == textStr.size() || !isAlpha(textStr.get(i+1)))
        {
            return misc_lb;
        }
        else 
        {
            return labelIndexer.getIndex(textStr.get(i+1));
        }
    }

    // Word roles
    public boolean useWordRoles()
    {
        return opts.useWordRolesOnFields.length > 0;
    }
    public int numWordRoles()
    {
        return wordRoleNameIndexer.size();
    }
    public static int getWordRole(String str)
    {
        Integer role = wordRoleMap.get(str);
        if(role == null)
        {
            role = other_role;
        }
        return role;
    }

    public void readWordRoles()
    {
        Utils.begin_track("readWordRoles: %s", opts.wordRolesPath);
        other_role = wordRoleNameIndexer.getIndex("other");
        for(String line : Utils.readLines(opts.wordRolesPath))
        {
            String[] wordRoleArray = line.split(" "); // [0] = word, [1] = role
            wordRoleMap.put(wordRoleArray[0].toLowerCase(),
                    wordRoleNameIndexer.getIndex(wordRoleArray[1]));
        }
        Utils.logs("%s words, %s roles", wordRoleMap.size(), numWordRoles());

    }

    protected void loadDMVModel()
    {
        depsModel = new GenerativeDMVModel(opts);
        depsCrossWordMap = new HashMap<Integer, Integer>();
        depsModel.stagedInitParams(opts.dmvModelParamsFile, wordIndexer, depsCrossWordMap);
    }

    public Map<Integer, Integer> getDepsCrossWordMap()
    {
        return depsCrossWordMap;
    }

    public GenerativeDMVModel getDepsModel()
    {
        return depsModel;
    }

    public Map<Integer, HashMap<CFGRule, Integer>> getCfgRules()
    {
        return cfgRules;
    }
    
    @Override
    public void logStats()
    {
        super.logStats();
//        Execution.putLogRec("numWords", W());
        Execution.putLogRec("numLabels", LB());
        Execution.putLogRec("numEventTypes", getT());
        String[] ar = new String[getT()];
        for(int i = 0; i < ar.length; i++)
        {
            ar[i] = String.valueOf(eventTypes[i].F);
        }
        Execution.putLogRec("numFields", Utils.mkString(ar, " "));
        for(int i = 0; i < ar.length; i++)
        {
            ar[i] = "[" + eventTypes[i].name + "] " +
                    Utils.mkString(eventTypes[i].fields, " ");
        }
        Execution.putLogRec("numFieldValues", Utils.mkString(ar, " | "));
    }

    @Override
    protected Integer[] widgetToIntSeq(AWidget widget)
    {
        return Utils.int2Integer(((Widget)widget).events[0]); // WARNING: only use first track
    }

    @Override
    protected String widgetToSGMLOutput(AExample ex, AWidget widget)
    {
        return ((Example)ex).genWidgetToSGMLOutput((GenWidget)widget);
    }
    @Override
    protected String widgetToFullString(AExample aex, AWidget widget)
    {
        Example ex = (Example)aex;
        if (opts.fullPredForEval)
        {
            return ex.widgetToEvalFullString((Widget)widget);
        }
        else
        {
            if(opts.modelType == ModelType.generate || opts.modelType == ModelType.discriminativeTrain)
            {
                if(opts.inputFormat == InputFormat.zmert)
                    return ex.genWidgetToMertFullString((GenWidget)widget);
                else
                    return ex.genWidgetToNiceFullString((GenWidget)widget);
            }
            else if(opts.modelType == ModelType.semParse)
            {
                return ex.semParseWidgetToNiceFullString((GenWidget)widget);
            }
            return ex.widgetToNiceFullString((Widget)widget);
        }
    }   
    
    @Override
    protected String exampleToString(AExample aex)
    {
        Example ex = (Example)aex;
        return ex.name + ": " + ex.events.get(0) + "..." + " ||| " +
                Utils.mkString(InductionUtils.getObject(wordIndexer,
                                                        ex.text), " ");
    }

    private Token[] readTokens(String line, HashSet<String> excludedEventTypes,
                               HashSet<String> excludedFields)
    {
        ArrayList<Token> tokens = new ArrayList();
        char tchar; String fieldName, value; Token tokenId = null;
        for(String token : line.split("\\t")) // token @
        {
            int i = token.indexOf(':', 1);
            if(i == -1)
            {
                throw new IllegalArgumentException("Bad token: " + token);

            }
            tchar = token.charAt(0); // denotes type of event
            fieldName = token.substring(1, i);
            value = token.substring(i + 1);
            if(fieldName.equals("id"))
            {
                tokenId = new Token('i', "id", -1, value);
            }
            else if (fieldName.equals("type"))
            {
                if (!excludedEventTypes.contains(value))
                {
                    eventTypeIndex = eventTypeNameIndexer.getIndex(value);
                    if (opts.includeEventTypeAsSymbol)
                    {
                        tokens.add(new Token(':', "type", -1, value));
                    }
                } // if
                else
                    return null;
            } // if (fieldName.equals("type"))
            else if (tchar != '.' &&  // Ignore hidden fields
                     eventTypeIndex != -1) // Consider only fields that come after type
            {
                String fullFieldName = eventTypeNameIndexer.getObject(eventTypeIndex)+ "." + fieldName; //stupid
                if (!excludedFields.contains(fullFieldName)) // Ignore excluded fields
                {
                    if (tchar == '$' && // Need to expand into the roles
                        Arrays.asList(opts.useWordRolesOnFields).contains(fullFieldName))
                    {
                        for(int role = 0; role < numWordRoles(); role++)
                        {
                            tokens.add(new Token(tchar, fieldName+"-" +
                                    wordRoleNameIndexer.getObject(role),
                                    role, value));
                        }  // for
                    } // if
                    else
                    {
                        tokens.add(new Token(tchar, fieldName, -1, value));
                    }
                } // if
            } // else if
        } // for each token
        tokens.add(tokenId);
        Token[] out = new Token[tokens.size()];
        return tokens.toArray(out);
    }

    public Map<Integer, Event> readEvents(String[] eventLines, HashSet<String> excludedEventTypes,
                               HashSet<String> excludedFields)
    {
        final HashSet<Integer> seenEventTypes = new HashSet<Integer>();
//        ArrayList<Event> events = new ArrayList<Event>(eventLines.length);
        Map<Integer, Event> events = new HashMap<Integer, Event>(eventLines.length);
        ArrayList<Field> fields;
        ArrayList<Integer> values;
        // Format: <fieldtype><field>:<value>\t...
        for(String line : eventLines)
        {           
            // parse tokens
            final Token[] tokens = readTokens(line, excludedEventTypes,
                                              excludedFields);
            if(tokens == null) // excludedEventType
                continue;
            values = new ArrayList<Integer>(tokens.length);
            if (eventTypeIndex != -1 &&
                (!opts.takeOneOfEventType ||
                !seenEventTypes.contains(eventTypeIndex)))
            {
                seenEventTypes.add(eventTypeIndex);
                // Set up event type
                EventType currentEventType = null;
                int id = -1;
                if (eventTypeIndex >= eventTypesBuffer.size()) // New event type (CAREFUL, changed from == to >=)
                {
                    // parse fields
                    fields = new ArrayList<Field>(tokens.length);

                    for(Token token: tokens)
                    {
                       Field field = null;
                       switch(token.tchar)
                       {
                           case 'i' : id = Integer.valueOf(token.value); break;
                           case '#' : field = new NumField(token.fieldName); break;
                           case '@' : if (opts.treatCatAsSym)
                                            field = new SymField(this, token.fieldName);
                                        else
                                            field = new CatField(token.fieldName);
                                        break;
                           case ':' : field = new SymField(this, token.fieldName); break;
                           case '$' : field = new StrField(this, token.fieldName); break;
                           default : throw new IllegalArgumentException("Bad field name: " + token.fieldName);
                       } // switch
                       if(field != null) // in case it's id
                       {
                           fields.add(field);
                           values.add(field.parseValue(token.role, token.value));
                       }
                    } // for
                    Field[] f = new Field[fields.size()];
                    currentEventType = new EventType(opts.useFieldSetsOnEventTypes, eventTypeIndex,
                                         eventTypeNameIndexer.getObject(eventTypeIndex),
                                         fields.toArray(f));
                    eventTypesBuffer.add(currentEventType);
                } // if
                else
                { // Check that the fields are the same
                    currentEventType = eventTypesBuffer.get(eventTypeIndex);
                    id = Integer.valueOf(tokens[tokens.length -1].value);
                    int same = Utils.same(currentEventType.F, tokens.length - 1); // id is an extra token
                    for(int f = 0; f < same; f++)
                    {
                        if (!currentEventType.fieldToString(f).equals(tokens[f].fieldName))
                        {                            
                            throw new IllegalArgumentException(
                                Utils.fmts("Field names don't match for event type" +
                                           " %s: %s versus %s",
                                           currentEventType, currentEventType.fieldToString(f),
                                           tokens[f].fieldName));
                        } // if
                        else
                        {
                            values.add(currentEventType.fields[f].parseValue(
                                    tokens[f].role, tokens[f].value));
                        }
                    } // for
                } // else

                // Create the event with its values
//                events.add(new Event(id, currentEventType, values));
                Event newEvent = new Event(id, currentEventType, values);
                if(opts.omitEmptyEvents)
                {
                    if(!newEvent.containsEmptyValues())
                        events.put(id, newEvent);
                }
                else
                    events.put(id, newEvent);

            } // if
        } // for
//        Event[] e = new Event[events.size()];
//        return events.toArray(e);
        return events;
    }

    /**
     * Method for reading Gold Meaning Representations from flat file. The
     * representation in the input is the same as in a normal .events file, but
     * in this case there is no consistency as far as eventTypes are concerned.
     * We allow events of the same eventType to have different number of fields.
     * @param eventLines
     * @param excludedEventTypes
     * @param excludedFields
     * @return
     */
    public Collection<MRToken> readMrTokens(String[] eventLines, Map<Integer, Event> events,
                               HashSet<String> excludedEventTypes,
                               HashSet<String> excludedFields)
    {
        ArrayList<MRToken> mrList = new ArrayList<MRToken>(eventLines.length);
        ArrayList<Integer> goldEvents = new ArrayList<Integer>(eventLines.length);
        // Format: <fieldtype><field>:<value>\t...
        for(String line : eventLines)
        {
            // parse tokens
            final Token[] tokens = readTokens(line, excludedEventTypes,
                                              excludedFields);
            if(tokens == null) // excludedEventType
                continue;
            // eventTypeIndex is already defined from readTokens(...). It's
            // guaranteed to have a value, since readMRTokens(...) is called
            // after readEvents(...)
            MRToken mr = new MRToken(eventTypeIndex);
            HashMap<String, Integer> fields = fieldsMap.get(eventTypeIndex);
            EventType currentEventType = eventTypesBuffer.get(eventTypeIndex);      
            for(Token token: tokens)
            {
                if(token.tchar != 'i') // not id
                {
                    int fieldIndex = fields.get(token.fieldName);
                    // if the field is of numeric type, then parse the value as integer,
                    // else get the correct value index from the corresponding field
                    mr.parseMrToken(fieldIndex,
                            token.tchar == '#' ? MRToken.Type.num : MRToken.Type.cat, 
                            token.tchar == '#' ? Integer.parseInt(token.value) :
                            currentEventType.getFields()[fieldIndex].
                            parseValue(token.role, token.value.toLowerCase()));
                }
                else
                {
                    goldEvents.add(Integer.valueOf(token.value));
                }
            } // for                   
            mrList.add(mr);
        } // for
        if(opts.useGoldStandardOnly)
        {
            Iterator<Event> it = events.values().iterator();
            while(it.hasNext())
            {
                Event e = it.next();
                if(!goldEvents.contains(e.id))
                    it.remove();
            }
        }
        return mrList;
    }

    private int[][] readTrueEvents(String[] alignLines, int N, Map<Integer, Event> events,
                                   ArrayList<Integer> lineToStartText)
    {
        int maxTracks = 0, eventId = 0, lineIndex = 0;
        // contains events aligned to each word of a sentence
        final ArrayList<Integer>[] trueEventsBuffer = new ArrayList[N];

        HashSet<Integer> goldEvents = null;
        if(opts.useGoldStandardOnly)
        {
             goldEvents = new HashSet<Integer>();
        }
        ArrayList<Integer> alignedEvents = new ArrayList(); // List of events
        for(String line : alignLines)
        {
            // Format: <line index> <event id1> ... <event idn>
            alignedEvents.clear();
            String[] lineEvents = line.split(" ");
            lineIndex = Integer.parseInt(lineEvents[0]);
            for(int i = 1; i < lineEvents.length; i++)
            {
                eventId = Integer.parseInt(lineEvents[i]);
                // -1 means that this line corresponds to an event
                // that's not in the candidate set, so we automatically get it wrong
                if(eventId == -1)
                {
                    eventId = Parameters.unreachable_e;
                }
                // we need to allow arbitrary ids, so no need for this assertion
//                assert ((eventId >= 0 && eventId < events.size())
//                        || eventId == Parameters.unreachable_e);
                alignedEvents.add(eventId);
                if(opts.useGoldStandardOnly)
                    goldEvents.add(eventId);
            } // for
            // This line goes from positions (char positions in text) i to j
            final int i = lineToStartText.get(lineIndex);
            final int j = lineToStartText.get(lineIndex + 1);
            maxTracks = Math.max(maxTracks, alignedEvents.size());
            for(int k = i; k < j; k++)
            {
                trueEventsBuffer[k] = new ArrayList<Integer>(alignedEvents); // test
            }
        } // for
        final int[][] trueEvents = new int[maxTracks][N];
        for(int c = 0; c < maxTracks; c++)
        {
            for(int i = 0; i < N; i++)
            {
                trueEvents[c][i] = (trueEventsBuffer[i] != null && c < trueEventsBuffer[i].size()) ?
                    trueEventsBuffer[i].get(c) : Parameters.none_e;
            } // for
        } // for

        /*ugly way to use gold standard events only as input to the model*/
        if(opts.useGoldStandardOnly)
        {
            Iterator<Event> it = events.values().iterator();
            while(it.hasNext())
            {
                Event e = it.next();
                if(!goldEvents.contains(e.id))
                    it.remove();
            }
        }
        return trueEvents;
    }

    @Override
    protected void readExamples(String input, int maxExamples)
    {
        String eventInput = "", textInput = "", name = "", alignInput = "";
        Tree<String> recordTree = null;
        boolean alignInputExists = false, textInputExists = false;
        if(opts.examplesInSingleFile)
        {
            String[] res = extractExampleFromString(input);
            name = res[0];
            textInput = res[1];
            eventInput = res[2];
            alignInput = res[3];
            if(res[4] != null)
            {                
                recordTree = new PennTreeReader(new StringReader(res[4])).next();
            }
            alignInputExists = alignInput != null;
            textInputExists = textInput != null;
        }
        else
        {
            eventInput = input;
            String filename = Utils.stripExtension(input);
            if(opts.inputPosTagged)
            {
                if(new File(filename + ".text.tagged").exists())
                    textInput = filename + ".text.tagged";
                else
                    textInput = filename + ".text";
                
            }
            else                
                textInput = filename + ".text";
//                textInput = input.replaceAll("\\."+ opts.inputFileExt, 
//                                         opts.posAtSurfaceLevel ? ".text.tagged" : ".text");            
            name = textInput;
            alignInput = opts.modelType != ModelType.semParse ?
            input.replaceAll("\\."+ opts.inputFileExt, ".align") :
            input.replaceAll("\\."+ opts.inputFileExt, ".salign");
            alignInputExists = new File(alignInput).exists();
            textInputExists = new File(textInput).exists();
        }
//        System.out.println(textPath);
        if (!opts.useOnlyLabeledExamples || alignInputExists)
        {
            final HashSet<String> excludedFields = new HashSet<String>();
            excludedFields.addAll(Arrays.asList(opts.excludedFields));
            final HashSet<String> excludedEventTypes = new HashSet<String>();
            excludedEventTypes.addAll(Arrays.asList(opts.excludedEventTypes));

            //Read events
            Map<Integer, Event> events  = null;
            try{
                events = readEvents(opts.examplesInSingleFile ?
                                            eventInput.split("\n") :
                                            Utils.readLines(eventInput),
                                        excludedEventTypes, excludedFields);
            }
            catch(Exception e) {System.out.println("Error in:"+name); e.printStackTrace(); System.exit(0);}
            wordIndexer.add("(boundary)");
            int textLength = opts.averageTextLength;
            if(opts.modelType == Options.ModelType.generate && lengthPredictor != null)
            {
                try
                {
                    textLength = Integer.valueOf(opts.lengthCompensation.replaceAll("_", "-")) +
                            (int) lengthPredictor.predict(
                            opts.examplesInSingleFile ? eventInput :
                            Utils.readFileAsString(eventInput));
                }
                catch(Exception e)
                {
                    Utils.log(e);
                }                
            }
            // Read text
            if(textInputExists)
            {
                final ArrayList<Integer> lineToStartText = new ArrayList<Integer>();
                int lineIndex = 0, textIndex = 0;
                ArrayList<String> textStr = new ArrayList();
                String fullText = "";
                for(String line : opts.examplesInSingleFile ?
                    textInput.split("\n") : Utils.readLines(textInput))
                {
                    lineToStartText.add(textIndex);
                    for(String s : (opts.inputPosTagged ? line : line.toLowerCase()).split(" "))
                    {
                        textStr.add(s);
                        textIndex++;
                    } // for
                    lineIndex++;
                    fullText += line + " ";
                } // for
                lineToStartText.add(textIndex);

                // POS tag example. Fill vocabulary with words plus their tags.
                // Doesn't work with labels at the moment
                String[] taggedTextArray = null;
                if(posTagger != null)
                    taggedTextArray = posTagger.tagString(fullText).toLowerCase().split(" ");

                final int[] lineStartIndices = Utils.integer2Int(
                      lineToStartText.toArray(new Integer[lineToStartText.size()]));
                final int[] labels = new int[textStr.size()];
                final int[] text  = new int[textStr.size()];
                for(int i = 0; i < text.length; i++)
                {
                    labels[i] = getLabelIndex(textStr, i);
                    String word = posTagger == null ? textStr.get(i) : taggedTextArray[i];
                    if(opts.modelType == ModelType.semParse && opts.modelUnkWord)
                    {                        
                        text[i] = wordIndexer.contains(word) ?
                            getWordIndex(word) : getWordIndex("<unk>");
                    }
                    else
                    {
                        text[i] = !opts.testInputPaths.isEmpty() || !opts.testInputLists.isEmpty() ? 
                                getTestSetWordIndex(word) : getWordIndex(word);
                    }
                }
                // set text length
                if(lengthPredictor == null)
                    textLength = text.length; // use gold value                
                // Read alignments
                if (alignInputExists)
                {
                    // Decide on type of alignments. Correspondences and Generation,
                    // assume alignment on event level only. Semantic Parsing
                    // assumes alignment per MR token (event, fields and values)
                    int[][] trueEvents = null;
                    Collection<MRToken> trueMrTokens = null;
//                    List<Event> eventsAsList = new ArrayList<Event>(events.length);
//                    eventsAsList.addAll(Arrays.asList(events));

                    if(opts.modelType != ModelType.semParse)
                    {
                        try{
                        trueEvents = readTrueEvents(opts.examplesInSingleFile ?
                                            alignInput.split("\n") :
                                            Utils.readLines(alignInput),
                                            text.length, events, lineToStartText);
                        }
                        catch(Exception e) {System.out.println("Error in:"+name); e.printStackTrace(); System.exit(0);}

                    }
                    else
                    {
                        trueMrTokens = readMrTokens(opts.examplesInSingleFile ?
                                            alignInput.split("\n") :
                                            Utils.readLines(alignInput), events,
                                        excludedEventTypes, excludedFields);
                    }
                    // lightweight map with id's of events and eventy type indices
                    HashMap<Integer, Integer> eventTypeIndices =
                            new HashMap<Integer, Integer>(events.size());
                    for(Event e : events.values())
                    {
                        eventTypeIndices.put(e.id, e.getEventTypeIndex());
                    }
//                    if(opts.useGoldStandardOnly)
//                    {
//
//                        events = (Event[]) eventsAsList.toArray(new Event[eventsAsList.size()]);
//                    }                                        
                    if(opts.modelType == Options.ModelType.generate)
                    {                                            
                        examples.add(new Example(this, name, events,
                            null, null, null, textLength,
//                            null, null, null, text.length,
//                            null, null, null, opts.averageTextLength,
//                            null, null, null, events.size()*opts.maxPhraseLength,
                            new GenWidget(trueEvents, text)));
                    } // if (generation WITH gold-standard)
                    else if(opts.modelType == Options.ModelType.semParse)
                    {
                        examples.add(new Example(this, name, events,
                            text, null, null, text.length,
                            new SemParseWidget(trueMrTokens)));
                    }
                    else if(opts.modelType == Options.ModelType.discriminativeTrain)
                    {
                        examples.add(new Example(this, name, events, text,
                                                       labels, lineStartIndices,
                                                       text.length,
//                                            new GenWidget(trueEvents, null, null, null,
//                                                      lineStartIndices,
//                                                      eventTypeAllowedOnTrack,
//                                                      eventTypeIndices)));
                                           new GenWidget(trueEvents, text)));
                    }
                    else
                    {
                        examples.add(new Example(this, name, events, text,
                                                       labels, lineStartIndices,
                                                       text.length,
                                           new Widget(trueEvents, null, null, null,
                                                      lineStartIndices,
                                                      eventTypeAllowedOnTrack,
                                                      recordTree,
                                                      eventTypeIndices)));
                    } // else (normal alignment WITH gold-standard)
                } // if(alignPathExists)
                else
                {
                    if(opts.modelType == Options.ModelType.generate) // for generation only
                    { 
                        examples.add(new Example(this, name, events,
                            null, null, null, textLength,
                            new GenWidget(text)));
                    } // else (generation WITH gold-standard)
                    else if(opts.modelType == Options.ModelType.semParse)
                    {
                        examples.add(new Example(this, name, events,
                            text, null, null, text.length, null));
                    }
                    else if(opts.modelType == Options.ModelType.discriminativeTrain)
                    {
                        examples.add(new Example(this, name, events, text,
                                                       labels, lineStartIndices,
                                                       text.length,
                                           new GenWidget(text)));
                    }
                    else
                    {
                        examples.add(new Example(this, name, events, text,
                                                       labels, lineStartIndices, 
                                                       text.length, null));
                    } // else (alignment - NO gold-standard)
                } // else
            } // if(textPathExists)
            else // for generation only without gold-standard
            {
                examples.add(new Example(this, name, events,
                        null, null, null, textLength, null));
            }
        } // if
    }

    /**
     * Input String has the following format:
     * Example_xxx (name) \n text (optional) \n events \n record_tree (optional) \n align (optional)
     * @param input
     * @return an array of Strings with name, text, events and align data in
     * each position
     */
    public static String[] extractExampleFromString(String input)
    {
        String[] res = new String[5];
        String ar[] = input.split("\n");
        StringBuilder str = new StringBuilder();
        if(ar[0].equals("$NAME")) // event3 v.2 format
        {
            res[0] = ar[1]; // name
            // parse text
            int i = 3; // 2nd line is the $TEXT tag
            while(i< ar.length && !ar[i].equals("$EVENTS")) 
            {
                str.append(ar[i++]).append("\n");                
            }            
            res[1] = str.deleteCharAt(str.length()-1).toString(); // delete last \n
            str = new StringBuilder();
            i++; // move past $EVENTS tag
            while(i< ar.length && !(ar[i].equals("$ALIGN") || ar[i].equals("$RECORD_TREE"))) 
            {
                str.append(ar[i++]).append("\n");                
            }            
            res[2] = str.deleteCharAt(str.length()-1).toString(); // delete last \n
            if(ar[i].equals("$RECORD_TREE"))
            {
                i++; // move past $RECORD_TREE tag
                str = new StringBuilder();
                while(i< ar.length && !ar[i].equals("$ALIGN")) 
                {
                    str.append(ar[i++]).append("\n");                
                }            
                res[4] = str.deleteCharAt(str.length()-1).toString(); // delete last \n
            }
            if(i < ar.length) // didn't reach the end of input, so there is align data
            {
                i++; // move past $ALIGN tag
                str = new StringBuilder();
                while(i< ar.length) 
                {
                    str.append(ar[i++]).append("\n");                
                }            
                res[3] = str.deleteCharAt(str.length()-1).toString(); // delete last \n
            }
        }
        else // event3 v.1 format
        {
            res[0] = ar[0]; // name
            if(!ar[1].startsWith(".id")) // text was found
                res[1] = ar[1];
            int i;            
            for(i = res[1] == null ? 1 : 2; i < ar.length; i++)
            {
                if(ar[i].startsWith(".id")) // event line
                    str.append(ar[i]).append("\n");
                else
                    break;
            } // for
            res[2] = str.deleteCharAt(str.length()-1).toString(); // delete last \n
            if(i < ar.length) // didn't reach the end of input, so there is align data
            {
                str = new StringBuilder();
                for(int j = i; j < ar.length; j++)
                    str.append(ar[j]).append("\n");
                res[3] = str.deleteCharAt(str.length()-1).toString(); // delete last \n
            }
        }                
        return res;
    }        
    @Override
    public void readExamples()
    {
        if(useWordRoles())
        {
            readWordRoles();
        }
        try
        {
        super.readExamples();
        }catch(Exception e){e.printStackTrace();}
        postSetupReadExamples(examples);
    }
    
    private void postSetupReadExamples(List<AExample> exAr)
    {
        if(opts.initType != InitType.staged)
        {
            eventTypes = new EventType[eventTypesBuffer.size()];
            eventTypesBuffer.toArray(eventTypes);
        }
        // if InitType == staged ignore eventTypesBuffer, everything is already loaded
        
        // NOTE commented out, as it interfered with readExamples() being called many times
        // and not just once, in the case of corpora with variable number of records (e.g. ATIS)
        //eventTypesBuffer.clear();
        eventTypeAllowedOnTrack = new HashSet[C];

        if(opts.modelUnkWord)
        {
            wordIndexer.add("<unk>");
        }

        Utils.begin_track("C=%s tracks", C);
        for(int c = 0; c < C; c++)
        {
            // Format of this string: <positive type name>,...,
            // <positive type name>/<negative type name>,...,<negative type name>
            // type name = * means all types
            final String[] posNegStr = opts.eventTypeTracks[c].split("/");
            final HashSet<Integer> posSet = getSet(posNegStr[0]);
            final HashSet<Integer> negSet = getSet((posNegStr.length > 1) ? posNegStr[1] : "");
            for(Integer t : negSet)
            {
                posSet.remove(t);
            }
            eventTypeAllowedOnTrack[c] = posSet;
            Utils.logs("Track %s (%s event types)", cstr(c), eventTypeAllowedOnTrack[c].size());
        } // for
        LogInfo.end_track();
        Utils.begin_track("Setting up examples");
        for(AExample ex: exAr)
        {
            ((Example)ex).computeEventTypeCounts();
//            ex.computeTrackEvents();
        }
        LogInfo.end_track();
        if(opts.treebankRules != null && cfgRules == null) // if we haven't read the rules yet (staged init)
        {
            readTreebankRules();
//            if(opts.modelType == ModelType.event3pcfg && !opts.fixRecordSelection)
//            {
//                // produce all possible trees from the grammar, sorted by number of sentences they span
////                grammarTrees = new Hash
//            }
        }        
    }
    
    protected void readTreebankRules()
    {
        Utils.begin_track("Read treebank rules...");
        cfgRules = new HashMap<Integer, HashMap<CFGRule, Integer>>();
        for(String line : Utils.readLines(opts.treebankRules))
        {
            if(!line.startsWith("#")) // comments
            {
                CFGRule rule = new CFGRule(line, rulesIndexer);
                HashMap<CFGRule, Integer> map = cfgRules.get(rule.getLhs());
                if(map == null)
                {
                    map = new HashMap<CFGRule, Integer>();
                    cfgRules.put(rule.getLhs(), map);
                }
                map.put(rule, map.size());
            }                
        }
        LogInfo.end_track();
    }
    
    private HashSet<Integer> getSet(String str)
    {
        HashSet<Integer> set = new HashSet<Integer>();
        if (str.length() > 0)
        {
            for(String s : str.split(","))
            {
                if (s.equals("ALL")) // Everything
                for(int t = 0; t < getT(); t++)
                {
                    set.add(t);
                }
                else
                {
                    final int t = eventTypeNameIndexer.indexOf(s);
                    if (t == -1)
                    {
                        LogInfo.warnings("Invalid event type specified in " +
                                         "eventTypeTracks: '%s'", s);
                    }
                    else
                    {
                        set.add(t);
                    }
                } // else
            } // for
        }
        return set;
    }       

    @Override
    protected Example genExample(int index)
    {
       return null;
    }

    protected void loadLengthPredictionModel()
    {
        if(opts.lengthPredictionModelFile != null)
        {
            Utils.begin_track("Loading Length Prediction Model...");
            lengthPredictor = new LinearRegressionWekaWrapper(
                    opts.generativeModelParamsFile == null ?
                            opts.stagedParamsFile : 
                            opts.generativeModelParamsFile,
                    opts.lengthPredictionModelFile,
                    opts.lengthPredictionStartIndex,
                    opts.lengthPredictionFeatureType, LinearRegressionOptions.Mode.test);
            LogInfo.end_track();
        }
        
    }
    
    protected void loadPosTagger()
    {
        if(opts.usePosTagger)
        {
            try
            {
                posTagger = new MaxentTagger("lib/models/bidirectional-distsim-wsj-0-18.tagger");
            }
            catch(Exception e)
            {
                Execution.finish();
            }
        }
    }
    
    protected void loadLanguageModel()
    {
        if(!opts.fullPredRandomBaseline && opts.ngramModelFile != null)
        {
            Utils.begin_track("Loading Language Model...");
            if(opts.ngramWrapper == NgramWrapper.kylm)
            {
                ngramModel = new KylmNgramWrapper(opts.ngramModelFile);
                if(opts.secondaryNgramModelFile != null) // pos tags LM
                    secondaryNgramModel = new KylmNgramWrapper(opts.secondaryNgramModelFile);
            }
            else if(opts.ngramWrapper == NgramWrapper.srilm)
            {
                ngramModel = new SrilmNgramWrapper(opts.ngramModelFile, opts.ngramSize);
                if(opts.secondaryNgramModelFile != null) // pos tags LM
                    secondaryNgramModel = new SrilmNgramWrapper(opts.secondaryNgramModelFile, opts.ngramSize);
            }
            else if(opts.ngramWrapper == NgramWrapper.roark)
            {
                ngramModel = new RoarkNgramWrapper(opts.ngramModelFile);
                if(opts.secondaryNgramModelFile != null)
                    secondaryNgramModel = new RoarkNgramWrapper(opts.secondaryNgramModelFile);
            }
                
            LogInfo.end_track();
        }
    }
    
    public Options getOpts()
    {
        return opts;
    }

    /**
     * compute total number of elements: ~|eventTypes|*|fields_per_eventType|
     * @return
     */
    protected int getTotalNumberOfFields()
    {
        int total = 0;
        for(EventType eventType: eventTypes)
        {            
            
            total += eventType.getF();
        }
        return total;
    }
    
    /**
     * helper method for testing the generation output. Simulates generate(...) method
     * for a single example without the thread mechanism
     * @return a String with the generated SGML text output (contains results as well)
     */
    @Override
    public String testGenerate(String name, LearnOptions lopts)
    {
        opts.alignmentModel = lopts.alignmentModel;
        FullStatFig complexity = new FullStatFig();
        double temperature = lopts.initTemperature;
        testPerformance = newPerformance();
//        AParams counts = newParams();
        List<String> outList = new ArrayList<String>();
        AInferState inferState = null;
        for(AExample ex : examples)
        {
            inferState =  createInferState(ex, 1, null, temperature, lopts, 0, complexity);
            testPerformance.add(ex, inferState.bestWidget);
            System.out.println(widgetToFullString(ex, inferState.bestWidget));
            outList.add(widgetToSGMLOutput(ex, inferState.bestWidget));
        }
        return outList.get(0);
//        AExample ex = examples.get(0);
//        AInferState inferState =  createInferState(ex, 1, null, temperature,
//                lopts, 0, complexity);
//        testPerformance.add(ex, inferState.bestWidget);
//        System.out.println(widgetToFullString(ex, inferState.bestWidget));
//        return widgetToSGMLOutput(ex, inferState.bestWidget);        
    }
    
    /**
     * Process single example in JSON Format - for client-server use. 
     * The method goes through the whole pipeline: convert JSON to event3 format,
     * read events, create inferState and generate.
     * @param queryLink
     * @return 
     */
    public String processExamplesJson(JsonFormat format, 
            String queryLink, LearnOptions lopts, String... args)
    {
        // convert json to events
        JsonWrapper wrapper = new JsonWrapper(queryLink, format, testSetWordIndexer, args);
        
        final HashSet<String> excludedFields = new HashSet<String>();
        excludedFields.addAll(Arrays.asList(opts.excludedFields));
        final HashSet<String> excludedEventTypes = new HashSet<String>();
        excludedEventTypes.addAll(Arrays.asList(opts.excludedEventTypes));
        List<AExample> examplesList = new ArrayList<AExample>(wrapper.getNumberOfOutputs());
        for(int i = 0; i < wrapper.getNumberOfOutputs(); i++)
        {
            //Read events
            String eventInput = "";
            Map<Integer, Event> events  = null;
            try
            {
                eventInput = wrapper.getEventsString()[i];
                events = readEvents(eventInput.split("\n"), excludedEventTypes, excludedFields);
            }
            catch(Exception e) 
            {
                Utils.log("Error in reading events!");                 
                LogInfo.error(e);
                e.printStackTrace();
                return encodeToJson(new JsonResult[] {JsonWrapper.ERROR_EVENTS});
            }
            // set text length
            int textLength = opts.averageTextLength;
            if(lengthPredictor != null)
            {
                try
                {
                    textLength = Integer.valueOf(opts.lengthCompensation.replaceAll("_", "-")) +
                                                 (int) lengthPredictor.predict(eventInput);
                }
                catch(Exception e)
                {Utils.log(e);}                
            }
            // create example
            examplesList.add((new Example(this, wrapper.getName()[i], events, null, null, null, textLength, 
                    wrapper.hasText() ? new GenWidget(wrapper.getText().get(i)) : null)));
        } // for
        postSetupReadExamples(examplesList);
        // generate text !
        APerformance performance = newPerformance();
        FullStatFig complexity = new FullStatFig();
        Collection<JsonExampleProcessor> list = new ArrayList(examplesList.size());
//        List<JsonResult> results = Collections.synchronizedList(new ArrayList<JsonResult>());
        // get dictionary that may contain translation of terms
        Properties dictionary = new Properties();
        if(args.length > 0 && args[0].endsWith("properties"))
        {
            try
            {
                dictionary.load(wrapper.getClass().getResourceAsStream(args[0]));
            }
            catch(IOException ioe)
            {
                LogInfo.error(ioe);
            }
        }
        JsonResult[] results = new JsonResult[examplesList.size()];        
        for(int i = 0; i < examplesList.size(); i++)
        {
            list.add(addExampleJson(i, results, examplesList.get(i), null, lopts.initTemperature, 
                                    lopts, 0, complexity, performance, dictionary));
        }                       
        Utils.parallelForeach(opts.numThreads, list);
        // return results in correct order        
//        Collections.sort(results);
        String out = encodeToJson(results);        
        Utils.logs("Finished generating " + queryLink);
        return out;
    }        
    
    private String encodeToJson(JsonResult[] results)
    {
        try
        {
            return JsonWrapper.mapper.writeValueAsString(results);
        }
        catch(Exception ioe)
        {
            return JsonWrapper.ERROR_EXPORT_JSON.toString();
        }
    }
    
    protected JsonExampleProcessor addExampleJson(int i, JsonResult[] results, AExample ex, 
                AParams counts, double temperature, LearnOptions lopts, int iter, 
                FullStatFig complexity, APerformance performance, Properties dictionary)
    {
        return new JsonExampleProcessor(i, results, ex, counts, temperature, 
                                        lopts, iter, complexity, performance, dictionary);
    }
    
    protected class JsonExampleProcessor extends BatchEM
    {
        protected final APerformance performance;
        protected final JsonResult[] results;      
        protected final Properties dictionary;
        public JsonExampleProcessor(int i, JsonResult[] results, AExample ex, 
                AParams counts, double temperature, LearnOptions lopts, int iter, 
                FullStatFig complexity, APerformance performance, Properties dictionary)
        {
            super(i, ex, counts, temperature, lopts, iter, complexity);
            this.results = results;
            this.performance = performance;            
            this.dictionary = dictionary;
        }

        @Override
        public JsonResult call() throws Exception
        {
            AInferState inferState =  createInferState(ex, i, null, temperature, lopts, 0, complexity);
            synchronized(performance)
            {
                performance.add(ex, inferState.bestWidget);
            }
            synchronized(results)
            {
                results[i] = widgetToJson(i, ex, inferState.bestWidget, dictionary);
            }
            return null;
//            return widgetToJson(i, ex, inferState.bestWidget);
        }                
    }  
    
    protected JsonResult widgetToJson(int i, AExample aex, AWidget widget, Properties dictionary)
    {
        return ((Example)aex).genWidgetToJson(i, (GenWidget)widget, dictionary);
    }
    
}
