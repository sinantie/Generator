package induction.utils;

import fig.basic.IOUtils;
import fig.basic.Indexer;
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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

/**
 *
 * @author konstas
 */
public class ExtractRecordsStatistics
{
    ExtractRecordsStatisticsOptions opts;    
    Event3Model model;
    List<ExampleRecords> examples;
    HistMap repeatedRecords;
    HistMap<Sentence> sentenceNgrams;
    HistMap<String> documentNgrams;
    Indexer indexer = new Indexer();
    
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
        if(opts.extractRecordTrees)
        {
            LogInfo.logs("Extracting record trees...");
            if(sentenceNgrams == null) // we need to compute sentence ngrams to construct CFG rules that bias toward sentence constituents
                countSentenceNgrams();
            extractRecordTrees();
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
                        Object element = null;
                        switch(opts.exportType)
                        {
                            case record : element = eventId; break;
                            default: case recordType : element = opts.useEventTypeNames ? 
                                    e.events.get(eventId).getEventTypeName() : e.events.get(eventId).getEventTypeIndex(); break;
                        }
                        // collapse consecutive records having the same type                        
//                        if(eventTypes.isEmpty() || eventType != eventTypes.get(eventTypes.size() - 1))
                        int indexOfElement = indexer.getIndex(element);
                        // we don't allow repetitions of record tokens in the same sentence
                        if(eventTypes.isEmpty() || !eventTypes.contains(indexOfElement))
                            eventTypes.add(indexOfElement);
                    } // if                    
                }
                if(opts.extractNoneEvent && w.getEvents()[0][startIndices[i-1]] == -1)
                    eventTypes.add(indexer.getIndex(opts.useEventTypeNames ? "(none)" : -1));
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
            HistMap<Object> repRecsInSent = new HistMap<Object>();
            for(Object i : er.getPermutation())
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
        sentenceNgrams = new HistMap<Sentence>();
        for(ExampleRecords er : examples)
        {
            for(Sentence sentence : er.getSentences())
                sentenceNgrams.add(sentence);
//            for(int i = 0; i < er.numberOfSentences(); i++)
//                sentenceNgrams.add(er.sentenceToString(i));
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
    
    private void extractRecordTrees()
    {
        // make a lexicon of unique sentence ngram ordered by token length
        Map<Integer, Set<Sentence>> ngrams = new TreeMap<Integer, Set<Sentence>>();
        for(Sentence ngram : sentenceNgrams.getKeys())
        {
            int n = ngram.getSize();
            Set<Sentence> list = ngrams.get(n);
            if(list == null)
            {
                list = new HashSet<Sentence>();
                list.add(ngram);                
            }
            else if(!list.contains(ngram))
                list.add(ngram);
            ngrams.put(n, list);
        } // for
        Integer[] ngramsLength = ngrams.keySet().toArray(new Integer[0]);
        for(ExampleRecords p : examples)
        {
            int[][] matrix = createConstituencyMatrix(p, ngrams, ngramsLength);
            for(Sentence sentence : p.getSentences())
            {
                
            }
        }
//        def renderTree(words:Array[Int], matrix:Array[Array[Int]]) = {
//    def create(i:Int, j:Int) : Tree = {
//      val k = matrix(i)(j)
//      if (i == j) {
//        val tag = if (opts.fixPreTags) ptstr(k) else "p"+k
//        new Tree(tag, false, fig.basic.ListUtils.newList(new Tree(wstr(words(i)), false)))
//      }
//      else {
//        val tag = "n"+k
//        foreach(i, j, { k:Int => // Find the split point
//          if (matrix(i)(k) != -1 && matrix(k+1)(j) != -1)
//            return new Tree(tag, false,
//              fig.basic.ListUtils.newList(create(i, k), create(k+1, j)))
//        })
//        throw new RuntimeException("Bad")
//      }
//    }
//    edu.berkeley.nlp.ling.Trees.PennTreeRenderer.render(create(0, words.length-1))
//  }
    }
    
    private int[][] createConstituencyMatrix(ExampleRecords p, Map<Integer, Set<Sentence>> ngrams, Integer[] ngramsLength)
    {
        int N = p.getSize();
        int[][] matrix = constituencyMatrixFactory(N);        
        int pos = 0;
        for(Sentence sentence : p.getSentences())
        {
            int sentLength = sentence.getSize();
            // fill diagonal with terminal symbol
            for(int i = 0; i < sentLength; i++)
                matrix[pos + i][pos + i] = sentence.getTokens().get(i);            
            // find the smallest prefix (leftmost) ngram that spans the sentence
            if(sentLength > 1) // spans of size=1 are already covered
            {
                for(int j = 0 ; j < ngramsLength.length; j++) // we assume that ngramsLength is ordered ascending
                {
                    int n = ngramsLength[j];
                    if(n > 1) // consider ngrams with span greater than 1
                    {
                        Sentence fragment = new Sentence(sentence.getFragment(0, n));
                        if(ngrams.get(n).contains(fragment))
                        {
                            
                        }
                    } // if
                } // for
            } // if
            
            pos += sentLength;
        }
        System.out.println(printConstituencyMatrix(matrix));
        return matrix;
    }
    
    private int[][] constituencyMatrixFactory(int N)
    {
        int[][] matrix = new int[N][N];
        for(int[] row : matrix)
            Arrays.fill(row, -1);
        return matrix;
    }
    
    private String printConstituencyMatrix(int[][] matrix)
    {
        StringBuilder str = new StringBuilder();
        for(int[] row : matrix)
        {
            for(int el : row)
                str.append(el != -1 ? indexer.getObject(el) : "_").append("\t");
            str.append("\n");
        }
        return str.toString();
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
        if(opts.extractRecordTrees)
        {
            LogInfo.logs("Extracting record trees...");
            if(sentenceNgrams == null) // we need to compute sentence ngrams to construct CFG rules that bias toward sentence constituents
                countSentenceNgrams();
            extractRecordTrees();
        }
    }
    
    class ExampleRecords
    {
        private List<Sentence> sentences;

        public ExampleRecords()
        {
            sentences = new ArrayList<Sentence>();
        }
                
        void addSentence(List types)
        {
            sentences.add(new Sentence(types));
        }

        List getPermutation()
        {
            List permutation = new ArrayList();
            for(Sentence sentence : sentences)
            {                
                    permutation.addAll(sentence.getTokens());
            }
            return permutation;
        }
        
        int numberOfSentences()
        {
            return sentences.size();
        }

        int getSize()
        {
            int size = 0;
            for(Sentence sentence : sentences)
                size += sentence.getSize();
            return size;
        }
        
        public List<Sentence> getSentences()
        {
            return sentences;
        }
                
        String sentenceToString(int i)
        {
            assert i < sentences.size();            
            return sentences.get(i).toString();
        }
        
        @Override
        public String toString()
        {
            StringBuilder str = new StringBuilder();
            for(Sentence sentence : sentences)
            {                
                str.append(sentence).append(" ");
                if(opts.delimitSentences)
                    str.append("| ");
            }
            return str.toString().trim();
        }        
    }
    
    class Sentence
    {
        private List<Integer> tokens;
        private int size;
        
        public Sentence(List types)
        {
            this.tokens = types;
            this.size = types.size();
        }

        public int getSize()
        {
            return size;
        }

        public List<Integer> getTokens()
        {
            return tokens;
        }

        public List<Integer> getFragment(int i, int j)
        {
            assert i > 0 && j < size && i < j;
            return tokens.subList(i, j+1);
        }
        
        @Override
        public String toString()
        {
            StringBuilder str = new StringBuilder();
            for(Integer r : tokens)
                str.append(indexer.getObject(r)).append(" ");
            return str.toString();
        }

        @Override
        public boolean equals(Object obj)
        {
            assert obj instanceof Sentence;
            return this.tokens.equals(((Sentence)obj).tokens);
        }

        @Override
        public int hashCode()
        {
            int hash = 3;
            hash = 29 * hash + (this.tokens != null ? this.tokens.hashCode() : 0);
            return hash;
        }                
    }
}
