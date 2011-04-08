package induction.problem.event3;

import edu.uci.ics.jung.graph.Graph;
import induction.problem.event3.params.Params;
import induction.problem.event3.params.Parameters;
import fig.basic.Indexer;
import fig.basic.LogInfo;
import fig.exec.Execution;
import induction.Options;
import induction.Options.InitType;
import induction.Options.ModelType;
import induction.Stemmer;
import induction.Utils;
import induction.problem.InductionUtils;
import induction.problem.InferSpec;
import induction.problem.ProbVec;
import induction.problem.wordproblem.WordModel;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
public class Event3Model extends WordModel<Widget, Params, Performance,
                                           Example, Event3InferState> implements Serializable
{

    private EventType[] eventTypes = null;  // Filled in later
    private Indexer<String> eventTypeNameIndexer = new Indexer<String>(); // Temporary
    private ArrayList<EventType> eventTypesBuffer = new ArrayList<EventType>();

    // Stuff for tracks
    protected int C = opts.eventTypeTracks.length;
    protected int PC = 1 << C; // Number of choices for the top level
    public HashSet<Integer>[] eventTypeAllowedOnTrack = null;

    // Stuff for labels (each word has a label)
    private static Indexer<String> labelIndexer = new Indexer<String>();
    protected static int none_lb, misc_lb;
    // Word roles
    private Indexer<String> wordRoleNameIndexer = new Indexer<String>();
    private static HashMap<String, Integer> wordRoleMap = new HashMap<String,Integer>();
    private int eventTypeIndex = -1;
    private static int other_role = -1;
    // map of fields names and indices - used for semantic parsing. It is filled in
    // at stagedInitParams
    private HashMap<Integer, HashMap<String, Integer>> fieldsMap;

    public Event3Model(Options opts)
    {
        super(opts);
        none_lb = labelIndexer.getIndex("-");
        misc_lb = labelIndexer.getIndex("*");
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

    public static int getWordIndex(String str)
    {
        return wordIndexer.getIndex(processWord(str));
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

    @Override
    public void stagedInitParams()
    {
        Utils.begin_track("stagedInitParams");
        try
        {
            Utils.log("Loading " + opts.stagedParamsFile);
            ObjectInputStream ois = new ObjectInputStream(
                    new FileInputStream(opts.stagedParamsFile));
            wordIndexer = ((Indexer<String>) ois.readObject());
            labelIndexer = ((Indexer<String>) ois.readObject());
            eventTypes = (EventType[]) ois.readObject(); // NEW
            eventTypesBuffer = new ArrayList<EventType>(Arrays.asList(eventTypes));
            // fill in eventTypesNameIndexer
            fieldsMap = new HashMap<Integer, HashMap<String, Integer>>(eventTypes.length);
            for(EventType e: eventTypes)
            {
                eventTypeNameIndexer.add(e.name);
                HashMap<String, Integer> fields = new HashMap<String, Integer>();
                int i = 0;
                for(Field f : e.getFields())
                {
                    fields.put(f.name, i++);
                }
                fields.put("none_f", i++);
                fieldsMap.put(e.getEventTypeIndex(), fields);
            }


//                ois.readObject(); ois.readObject();
//
//                for(EventType e : eventTypes)
//                {
//                    for(Field f : e.fields)
//                    {
//                        if(f instanceof CatField)
//                        {
////                            ((CatField)f).setIndexer((Indexer<String>)ois.readObject());
//                            ois.readObject();
//                        }
//                        else if(f instanceof StrField)
//                        {
////                            ((StrField)f).setIndexer((Indexer<StrField.ArrayPair>)ois.readObject());
//                            ois.readObject();
//                        }
//                    }
//                }
            //eventTypesBuffer = (ArrayList<EventType>) ois.readObject();
            params = newParams();
//            params.setVecs((List<ProbVec>) ois.readObject());
            params.setVecs((Map<String, ProbVec>) ois.readObject());
//            }
            ois.close();
        }
        catch(Exception ioe)
        {
            Utils.log("Error loading "+ opts.stagedParamsFile);
            ioe.printStackTrace();
            Execution.finish();
        }
        LogInfo.end_track();
    }

    @Override
    protected void saveParams(String name)
    {
        try
        {
            ObjectOutputStream oos = new ObjectOutputStream(
                    new FileOutputStream(Execution.getFile(name + ".params.obj")));
            oos.writeObject(wordIndexer);
            oos.writeObject(labelIndexer);
            oos.writeObject(eventTypes); // NEW
//            for(EventType e : eventTypes)
//            {
//                for(Field f : e.fields)
//                {
//                    if(f instanceof CatField)
//                    {
//                        oos.writeObject(((CatField)f).getIndexer());
//                    }
//                    else if(f instanceof StrField)
//                    {
//                        oos.writeObject(((StrField)f).getIndexer());
//                    }
//                }
//            }
//            oos.writeObject(eventTypesBuffer);
            oos.writeObject(params.getVecs());
            oos.close();
        }
        catch (IOException ex)
        {
            Utils.log(ex.getMessage());
            ex.printStackTrace();
        }
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
    protected Integer[] widgetToIntSeq(Widget widget)
    {
        return Utils.int2Integer(widget.events[0]); // WARNING: only use first track
    }

    @Override
    protected String widgetToSGMLOutput(Example ex, Widget widget)
    {
        return ex.genWidgetToSGMLOutput((GenWidget)widget);
    }
    @Override
    protected String widgetToFullString(Example ex, Widget widget)
    {
        if (opts.fullPredForEval)
        {
            return ex.widgetToEvalFullString(widget);
        }
        else
        {
            if(opts.modelType == ModelType.generate)
            {
                return ex.genWidgetToNiceFullString((GenWidget)widget);
            }
            else if(opts.modelType == ModelType.semParse)
            {
                return ex.semParseWidgetToNiceFullString((GenWidget)widget);
            }
            return ex.widgetToNiceFullString(widget);
        }
    }

    @Override
    protected String exampleToString(Example ex)
    {
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
            values = new ArrayList<Integer>(tokens.length);
            if (eventTypeIndex != -1 &&
                (!opts.takeOneOfEventType ||
                !seenEventTypes.contains(eventTypeIndex)))
            {
                seenEventTypes.add(eventTypeIndex);
                // Set up event type
                EventType currentEventType = null;
                int id = -1;
                if (eventTypeIndex == eventTypesBuffer.size()) // New event type
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
                                            field = new SymField(token.fieldName);
                                        else
                                            field = new CatField(token.fieldName);
                                        break;
                           case ':' : field = new SymField(token.fieldName); break;
                           case '$' : field = new StrField(token.fieldName); break;
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
                events.put(id, new Event(id, currentEventType, values));
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

    private int[][] readTrueEvents(String alignPath, int N, Map<Integer, Event> events,
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
        for(String line : Utils.readLines(alignPath))
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
                assert ((eventId >= 0 && eventId < events.size())
                        || eventId == Parameters.unreachable_e);
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
    protected void readExamples(String path, int maxExamples)
    {
        //examples.clear();
        final String textPath = path.replaceAll("\\."+ opts.inputFileExt,
                                                ".text");
        final String alignPath = opts.modelType != ModelType.semParse ? 
            path.replaceAll("\\."+ opts.inputFileExt,".align") :
            path.replaceAll("\\."+ opts.inputFileExt,".salign");
        final boolean alignPathExists = new File(alignPath).exists();
        final boolean textPathExists = new File(textPath).exists();
//        System.out.println(textPath);
        if (!opts.useOnlyLabeledExamples || alignPathExists)
        {
            final HashSet<String> excludedFields = new HashSet<String>();
            excludedFields.addAll(Arrays.asList(opts.excludedFields));
            final HashSet<String> excludedEventTypes = new HashSet<String>();
            excludedEventTypes.addAll(Arrays.asList(opts.excludedEventTypes));

            //Read events                        
            Map<Integer, Event> events = readEvents(Utils.readLines(path),
                                        excludedEventTypes, excludedFields);

            wordIndexer.add("(boundary)");
            // Read text
            if(textPathExists)
            {
                final ArrayList<Integer> lineToStartText = new ArrayList<Integer>();
                int lineIndex = 0, textIndex = 0;
                ArrayList<String> textStr = new ArrayList();
                String fullText = "";
                for(String line : Utils.readLines(textPath))
                {
                    lineToStartText.add(textIndex);
                    for(String s : line.toLowerCase().split(" "))
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
                        text[i] = getWordIndex(word);
                    }
                }

                // Read alignments
                if (alignPathExists)
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
                        trueEvents = readTrueEvents(alignPath, text.length,
                            events, lineToStartText);
                    }
                    else
                    {
                        trueMrTokens = readMrTokens(Utils.readLines(alignPath), events,
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
                    
                    if (opts.oneExamplePerLine) // if (one example per line WITH gold-standard)
                    {
                        int[][] subTrueEvents;
                        for(int l = 0; l < lineStartIndices.length - 1; l++)
                        {
                            int i = lineStartIndices[l];
                            int j = lineStartIndices[l + 1];
                            subTrueEvents = new int[trueEvents.length][j - i];
                            for(int r = 0; r < trueEvents.length; r++)
                            {
                                subTrueEvents[r] = Arrays.copyOfRange(
                                        trueEvents[r], i, j);
                            } // for
                            int[] subStartIndices = {0, j - i};
                            examples.add(new Example(this, textPath+":" + l,
                                               events,
                                               Arrays.copyOfRange(text, i, j),
                                               Arrays.copyOfRange(labels, i, j),
                                               subStartIndices, text.length,
                                               new Widget(subTrueEvents, null, null,
                                               null, subStartIndices,
                                               eventTypeAllowedOnTrack,
                                               eventTypeIndices)));
                        } // for
                    }
                    if(opts.modelType == Options.ModelType.generate)
                    {
//                        examples.add(new Example(this, textPath, events,
//                            null, null, null, opts.averageTextLength,
//                            new GenWidget(trueEvents, text)));
                        examples.add(new Example(this, textPath, events,
                            null, null, null, text.length,
                            new GenWidget(trueEvents, text)));
                    } // if (generation WITH gold-standard)
                    else if(opts.modelType == Options.ModelType.semParse)
                    {
                        examples.add(new Example(this, textPath, events,
                            text, null, null, text.length,
                            new SemParseWidget(trueMrTokens)));
                    }
                    else
                    {
                        examples.add(new Example(this, textPath, events, text,
                                                       labels, lineStartIndices,
                                                       text.length,
                                           new Widget(trueEvents, null, null, null,
                                                      lineStartIndices,
                                                      eventTypeAllowedOnTrack,
                                                      eventTypeIndices)));
                    } // else (normal alignment WITH gold-standard)
                } // if(alignPathExists)
                else
                {
                    if (opts.oneExamplePerLine)
                    { // Originally for NFL data, but now with cooked.pruned, don't need this anymore
                        for(int l = 0; l < lineStartIndices.length - 1; l++)
                        {
                            int i = lineStartIndices[l];
                            int j = lineStartIndices[l + 1];
                            int[] subStartIndices = {0, j - i};
                            examples.add(new Example(this, textPath+":" + l,
                                               events,
                                               Arrays.copyOfRange(text, i, j),
                                               Arrays.copyOfRange(labels, i, j),
                                               subStartIndices, text.length, null));
                        } // for
                    } // if (alignment one example per line - NO gold-standard)
                    else if(opts.modelType == Options.ModelType.generate) // for generation only
                    { 
                        examples.add(new Example(this, textPath, events,
                            null, null, null, opts.averageTextLength,
                            new GenWidget(text)));
//                         examples.add(new Example(this, textPath, events,
//                        null, null, null, opts.averageTextLength, null));
                    } // else (generation WITH gold-standard)
                    else if(opts.modelType == Options.ModelType.semParse)
                    {
                        examples.add(new Example(this, textPath, events,
                            text, null, null, text.length, null));
                    }
                    else
                    {
                        examples.add(new Example(this, textPath, events, text,
                                                       labels, lineStartIndices, 
                                                       text.length, null));
                    } // else (alignment - NO gold-standard)
                } // else
            } // if(textPathExists)
            else // for generation only without gold-standard
            {
                examples.add(new Example(this, textPath, events, 
                        null, null, null, opts.averageTextLength, null));
            }
        } // if
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
        if(opts.initType != InitType.staged)
        {
            eventTypes = new EventType[eventTypesBuffer.size()];
            eventTypesBuffer.toArray(eventTypes);
        }
        // if InitType == staged ignore eventTypesBuffer, everything is already loaded
        eventTypesBuffer.clear();
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
        for(Example ex: examples)
        {
            ex.computeEventTypeCounts();
//            ex.computeTrackEvents();
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
    protected void baitInitParams()
    { // Hard code things
        params = newParams();
        params.setUniform(1);
    }
    @Override
    protected Params newParams()
    {
        return new Params(this, opts);
    }

    @Override
    protected Performance newPerformance()
    {
        switch(opts.modelType)
        {
            case generate : return new GenPerformance(this);
            case semParse : return new SemParsePerformance(this);
            default : return new Performance(this);
        }        
    }

    @Override
    protected Event3InferState newInferState(Example ex, Params params, Params counts,
                                       InferSpec ispec)
    {
        switch(opts.modelType)
        {
            case generate : return new GenInferState(this, ex, params, counts, ispec, ngramModel);
            case semParse : return new SemParseInferState(this, ex, params, counts, ispec, ngramModel);
            default : return new InferState(this, ex, params, counts, ispec);
        }
    }

    protected Event3InferState newInferState(Example ex, Params params, Params counts,
                                       InferSpec ispec, Graph graph)
    {
        return new SemParseInferState(this, ex, params, counts, ispec, graph);
    }

    @Override
    protected Example genExample(int index)
    {
       return null;
    }

    public Options getOpts()
    {
        return opts;
    }     
}
