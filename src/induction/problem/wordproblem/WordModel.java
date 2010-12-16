package induction.problem.wordproblem;

import fig.basic.Indexer;
import fig.exec.Execution;
import induction.Options;
import induction.problem.AInferState;
import induction.problem.AModel;
import induction.problem.AParams;
import induction.problem.APerformance;
import induction.problem.AWidget;

/**
 *
 * @author konstas
 */
public abstract class WordModel<Widget extends AWidget,
                                Params extends AParams,
                                Performance extends APerformance<Widget>,
                                Example extends WordExample<Widget>,
                                InferState extends AInferState<Widget, Example, Params> >
                      extends AModel<Widget, Params, Performance, Example, InferState>
{

    protected static Indexer<String> wordIndexer = new Indexer<String>();
    protected int[] wordFreqs = null; // Word frequencies
   
    public WordModel(Options opts)
    {
        super(opts);
    }

    /**
     *
     * @return number of words
     */
    public static int W()
    {
        return wordIndexer.size();
    }

    public static String wordToString(int w)
    {
        if(w > -1)
            return wordIndexer.getObject(w);
        else
            return "N/A";
    }

    public static String[] wordsToStringArray()
    {
        String[] out = new String[wordIndexer.size()];
        return wordIndexer.getObjects().toArray(out);
    }

    public Indexer<String> getWordIndexer()
    {
        return wordIndexer;
    }
    
    @Override
    public void logStats()
    {
        super.logStats();
        Execution.putLogRec("numWords", W());
    }

    @Override
    public void readExamples()
    {
        super.readExamples();
//        wordFreqs = new int[W()];
////      examples.foreach { ex => ex.words.foreach(wordFreqs(_) += 1) }
//        for(Example ex : examples)
//        {
//            for(int i = 0; i < ex.words.length; i++)
//            {
//                // I assume we want to capture the global word frequencies,
//                // so I add the word frequencies of each example, provided
//                // that the words array corresponds to all the words in the corpus
//                // (bag of words).
//                wordFreqs[i] += ex.words[i];
//            }
//        } // for
    }
}
