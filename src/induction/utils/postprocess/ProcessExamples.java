package induction.utils.postprocess;

import fig.basic.IOUtils;
import fig.exec.Execution;
import induction.Options.InitType;
import induction.Utils;
import induction.problem.event3.Event3Example;
import induction.problem.event3.Event3Model;
import induction.problem.event3.generative.GenerativeEvent3Model;
import induction.utils.humanevaluation.ExportScenarios;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author sinantie
 */
public class ProcessExamples
{

    ProcessExamplesOptions opts;
    private Action action;
    List<Event3Example> examples;

    public ProcessExamples(ProcessExamplesOptions opts)
    {
        this.opts = opts;
        examples = new ArrayList<Event3Example>();
    }

    public void execute()
    {
        switch (opts.actionType) {
            case averageAlignmentsPerExample:
                action = new AverageAlignmentsPerExample();
                break;
            case averageFieldsWithNoValuePerRecord:
                action = new AverageFieldsWithNoValuePerRecord(opts.record, opts.totalNumberOfFields);
                break;
            case averageWordsPerSentence:
                action = new AverageWordsPerSentence();
                break;
            case averageWordsPerDocument:
                action = new AverageWordsPerDocument();
                break;
            case averageSentencesPerDocument:
                action = new AverageSentencesPerDocument();
                break;
            case maxDocLength:
                action = new MaxDocumentLength();
                break;
            case maxValueLength:
                action = new MaxValueLength();
                break;
            case splitDocToSentences:
                action = new SplitDocToSentences();
                break;
            case exportExamplesAsSentences:
                action = new ExportExamplesAsSentences(opts.lmOrder, opts.splitSentences);
                break;
            case computePermMetrics:
                Event3Model model = new GenerativeEvent3Model(opts.modelOpts);
                model.init(InitType.staged, opts.modelOpts.initRandom, "");            
                action = new ComputePermMetrics(opts.fullPredOutput, opts.predFileType, model.eventTypeStrArray());
                break;
        }
        examples = Utils.readEvent3Examples(opts.modelOpts.inputPaths, opts.modelOpts.inputLists, opts.modelOpts.examplesInSingleFile);
        // process examples
        for (Event3Example example : examples) {
            action.act(example);
        }
//        System.out.println(action.result());                
        Utils.write("data/weatherGov/weatherGovTrainGaborSplit.gz", (String)action.result());
    }

    public void testExecute()
    {
        execute();
    }

    /**
     * find the average number of gold standard alignments per example
     */
    static class AverageAlignmentsPerExample implements Action<Event3Example>
    {

        int totalAlignedEvents = 0, totalExamples = 0;

        @Override
        public Object act(Event3Example example)
        {
            // 4th entry are the alignments that have the format line_no [event_id]+
            // we just count the number of events
            totalAlignedEvents += example.getAlignments().split(" ").length - 1;
            totalExamples++;
            return null;
        }

        @Override
        public Object result()
        {
            return new Double((double) totalAlignedEvents / (double) totalExamples);
        }
    }

    static class AverageFieldsWithNoValuePerRecord implements Action<Event3Example>
    {

        private String record;
        private int totalNumberOfFields, totalEmpty, totalExamples;

        public AverageFieldsWithNoValuePerRecord(String record, int totalNumberOfFields)
        {
            this.record = record;
            this.totalNumberOfFields = totalNumberOfFields;
        }

        @Override
        public Object act(Event3Example example)
        {
            // 3rd entry are the events
            String events[] = example.getAlignments().split("\n");
            String recordEvent = null;
            // capture the line with the event we are looking for
            for (String event : events) {
                if (event.contains(".type:" + record)) {
                    recordEvent = event;
                }
            }
            if (recordEvent != null) {
                // count the number of non-empty fields. 
                // Then subtract from the total number of fields.
                // Note that some records have a fixed number of fields, whereas others
                // have a variable number, of only the non-empty. That's why
                // it's safer to subtract from the total
                int total = 0;
                for (String fieldValue : recordEvent.split("\t")) {
                    // we need actual fields
                    if (fieldValue.contains("@") || fieldValue.contains("$") || fieldValue.contains("#")) {
                        if (!fieldValue.contains("--")) // capture only the non-empty
                        {
                            total++;
                        }
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
            return new Double((double) totalEmpty / (double) totalExamples);
        }
    }

    private static class AverageWordsPerSentence implements Action<Event3Example>
    {

        private int words, sentences;

        @Override
        public Object act(Event3Example example)
        {
            // 2nd entry is the text
            for (String token : example.getText().split("\\s")) {
                if (Utils.isSentencePunctuation(token)) {
                    sentences++;
                }
                words++;
            }
            return null;
        }

        @Override
        public Object result()
        {
            return new Double((double) words / (double) sentences);
        }
    }

    private static class AverageWordsPerDocument implements Action<Event3Example>
    {

        private int words, docs;

        @Override
        public Object act(Event3Example example)
        {
            int w;
            // 2nd entry is the text
            w = example.getText().split("\\s").length;
            words += w;
            System.out.println(w);
            docs++;
            return null;
        }

        @Override
        public Object result()
        {
            return new Double((double) words / (double) docs);
        }
    }

    private static class MaxDocumentLength implements Action<Event3Example>
    {

        private int max = -1;

        @Override
        public Object act(Event3Example example)
        {
            int w;
            // 2nd entry is the text
            w = example.getText().split("\\s").length;
//            System.out.println(w);

            String lines[] = example.getText().split("\n");
            int no = 11;
            if (lines.length > no) {
                System.out.println(lines[no]);
            }

            if (w > max) {
                max = w;
            }
            return null;
        }

        @Override
        public Object result()
        {
            return max;
        }
    }
    
    private static class MaxValueLength implements Action<Event3Example>
    {

        private int max = -1, avg = 0, count = 0;

        @Override
        public Object act(Event3Example example)
        {
            int v = -1;
            String events = example.getEvents();
            for(String event : events.split("\n"))
            {
                if(event.contains("$"))
                {
                    for(String token : event.split("\t"))
                    {
                        if(token.startsWith("$"))
                        {
                            v = token.split(":")[1].split(" ").length;
                            if (v > max)                         
                                max = v;
                            avg += v;
                            count++;
                        }                        
                    }
                }
            }            
            return null;
        }

        @Override
        public Object result()
        {
            return String.format("Max=%s, Avg=%.3f", max, (double)avg/(double)count);
        }
    }

    private static class AverageSentencesPerDocument implements Action<Event3Example>
    {

        private int docs, sentences;

        @Override
        public Object act(Event3Example example)
        {
            // 2nd entry is the text
            for (String token : example.getText().split("\\s")) {
                if (Utils.isSentencePunctuation(token)) {
                    sentences++;
                }
            }
            docs++;
            return null;
        }

        @Override
        public Object result()
        {
            return new Double((double) sentences / (double) docs);
        }
    }

    private static class ExportExamplesAsSentences implements Action<Event3Example>
    {
        
        private List<String> list = new ArrayList<String>();
        private int lmOrder;
        private boolean splitSentences;
        
        public ExportExamplesAsSentences(int lmOrder, boolean splitSentences)
        {        
            this.lmOrder = lmOrder;
            this.splitSentences = splitSentences;
        }        
        
        @Override
        public Object act(Event3Example example)
        {            
            String text = example.getText();            
            StringBuilder label = new StringBuilder();
            for (int i = 0; i < lmOrder - 1; i++)
                label.append("<s> ");                   
            if(splitSentences)
            {
                String[] sentences = text.split(".");
                for(String sent : sentences)
                {
                    StringBuilder str = new StringBuilder();
                    list.add(str.append(label).append(sent).append(" . </s>").toString());
                }
            }
            else
            {
                StringBuilder str = new StringBuilder();
                list.add(str.append(label).append(text.replace("\n", " ")).append(" </s>").toString());
            }                     
            return null;
        }       

        @Override
        public Object result()
        {
            StringBuilder out = new StringBuilder();
            for (String s : list) {
                out.append(s).append("\n");
            }
            return out.toString();
        }
    }

    public static class SplitDocToSentences implements Action<Event3Example>
    {

        private List<Event3Example> list = new ArrayList<Event3Example>();

        @Override
        public Object act(Event3Example example)
        {
            List<Event3Example> doc = new ArrayList<Event3Example>();
            String[] textLines = example.getText().split("\n");
            int i = 0;
            Map<String, String> eventsMap = example.getEventsMap();
            for (String alignLine : example.getAlignments().split("\n")) {
                String name = String.format("%s_sent_%s", example.getName(), i);
                String text = textLines[i++];
                String[] alignIds = alignLine.split(" ");
                String events = extractEvents(eventsMap, Arrays.copyOfRange(alignIds, 1, alignIds.length));
                String alignments = alignLine(alignIds.length - 1);
                doc.add(new Event3Example(name, text, events, alignments));
            }
            list.addAll(doc);
            return doc;
        }

        private String extractEvents(Map<String, String> events, String[] ids)
        {
            StringBuilder str = new StringBuilder();
            int i = 0;
            for (String id : ids) {
                str.append(Event3Example.packEvents(i++, events.get(id))).append("\n");
            }
            return str.toString();
        }

        private String alignLine(int size)
        {
            StringBuilder str = new StringBuilder("0");
            for (int i = 0; i < size; i++) {
                str.append(" ").append(i);
            }
            return str.toString();
        }

        @Override
        public Object result()
        {
            StringBuilder out = new StringBuilder();
            for (Event3Example e : list) {
                out.append(e);
            }
            return out.toString();
        }
    }

    private static class ComputePermMetrics implements Action<Event3Example>
    {

        private Map<String, Collection<Integer>> predRecordsMap;
        int total;
        float wer;
        
        public ComputePermMetrics(String predPath, ProcessExamplesOptions.PredFileType predFileType, String[] eventTypeNamesAr)
        {
            Set<String> eventTypeNames = new HashSet<String>();
            Collections.addAll(eventTypeNames, eventTypeNamesAr);
            predRecordsMap = new HashMap<String, Collection<Integer>>();
            if(predFileType == ProcessExamplesOptions.PredFileType.alignment)
                readRecordsFromAlignInput(Utils.readLines(predPath), eventTypeNames);
            else
                readRecordsFromGenInput(Utils.readLines(predPath), eventTypeNames);
        }

        private void readRecordsFromGenInput(String[] lines, Set<String> eventTypeNames)
        {
            for(int i = 0; i < lines.length; i++)
            {
                String name = lines[i]; // first line is the name
                i += 3; // skip three lines (name, text and an empty line)
                Collection<Integer> records = ExportScenarios.processEventsLine(lines[i], eventTypeNames, null, false, false);
                i += 2; // skip two lines (scores, and an empty line)
                predRecordsMap.put(name, records);
            }
        }
        
        private void readRecordsFromAlignInput(String[] lines, Set<String> eventTypeNames)
        {
            for(String line : lines) // each line contains a seperate example
            {
                // each piece of information is split with tabs. We need to identify the portion that contains the prediction
                String[] chunks = line.split("\t");
                // first chunk contains the name ending in ':'
                String name = chunks[0].substring(0, chunks[0].lastIndexOf(":"));                 
                // find the end of the prediction. Default is the size of the array, in case the line does not contain the gold alignment.
                int endPos = chunks.length;
                for(int i = 2; i < chunks.length; i++)
                    if(chunks[i].equals("- True:"))
                        endPos = i;
                String pred = Arrays.toString(Arrays.copyOfRange(chunks, 2, endPos));
                Collection<Integer> records = ExportScenarios.processEventsLine(pred, eventTypeNames, null, false, false);
                predRecordsMap.put(name, records);
            }
        }
        
        @Override
        public Object act(Event3Example example)
        {
            if(predRecordsMap.containsKey(example.getName()))
            {
                Integer[] gold = Event3Example.getFlatAlignments(example.getAlignments()).toArray(new Integer[0]);
                Integer[] pred = predRecordsMap.get(example.getName()).toArray(new Integer[0]);
                wer += Utils.computeWER(pred, gold);
                total++;
            }            
            return null;
        }

        @Override
        public Object result()
        {
            return wer / (float) total;
        }
    }

    interface Action<T>
    {

        public Object act(T in);

        public Object result();
    }
}
