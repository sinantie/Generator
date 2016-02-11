package induction.problem.event3;

import fig.basic.Indexer;
import fig.basic.IntPair;
import induction.Utils;
import induction.problem.AParams;
import induction.problem.VecFactory;
import induction.problem.event3.generative.GenerativeEvent3Model;
import induction.problem.event3.params.StrFieldParams;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * String ($) - sequence of words
 * Example: description in the NFL domain
 * indexer = possible sequences of words
 *
 * @author konstas
 */
public class StrField extends Field implements Serializable
{
    static final long serialVersionUID = -4500849348695568802L;

    public Indexer<WordLabel> indexer = new Indexer<WordLabel>();
    private Indexer<String> wordIndexer;
    private boolean stemAll, lemmatiseAll;
    
    public StrField() {}
    public StrField(Event3Model model, String fieldName)
    {
        this.wordIndexer = model.getWordIndexer();
        stemAll = model.getOpts().stemAll;
        lemmatiseAll = model.getOpts().lemmatiseAll;
        name = fieldName;
        maxLength = Integer.MAX_VALUE;
    }

    public Indexer<WordLabel> getIndexer()
    {
        return indexer;
    }

    public void setIndexer(Indexer<WordLabel> indexer)
    {
        this.indexer = indexer;
    }

    @Override
    public String valueToString(int v)
    {
        return indexer.getObject(v).toString();
    }

    public String[] valuesToStringArray()
    {
        String[] out = new String[indexer.getObjects().size()];
        for(int i = 0; i < out.length; i++)
        {
            StringBuilder str = new StringBuilder();
            for(String token : indexer.getObject(i).getWords(false, true))
                str.append(token).append(" ");
            out[i] = str.toString().trim();
        }
        return out;
    }
    
    public int valueNumOfWords(int v)
    {
        return indexer.getObject(v).toString().split(" ").length;
    }
    
    public WordLabel getWordLabel(int v)
    {
        return indexer.getObject(v);
    }
        
    @Override
    public int parseValue(int role, String str)
    {
        String[] tokens = str.split(" ");
        WordLabel wl = new WordLabel(wordIndexer);
        for(int i = 0; i < tokens.length; i++)       
            if (role == -1 || role == GenerativeEvent3Model.getWordRole(tokens[i]))            
                wl.addWordLabel(Event3Model.getWordIndex(wordIndexer, tokens[i], stemAll, lemmatiseAll), GenerativeEvent3Model.getLabelIndex(Arrays.asList(tokens), i));
        return indexer.getIndex(wl);
    }

    @Override
    public AParams newParams(Event3Model model, int numOfWords, VecFactory.Type vectorType, String prefix)
    {
        return new StrFieldParams(model, vectorType, GenerativeEvent3Model.LB(), prefix);
    }

    @Override
    public int getV()
    {
        return indexer.size(); // number of values
    }

    @Override
    public String toString()
    {
        return Utils.fmts("$%s(%s)", name, getV());
    }
}
