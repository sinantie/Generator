package induction.problem.event3;

import fig.basic.Indexer;
import induction.Utils;
import induction.problem.AParams;
import induction.problem.event3.params.StrFieldParams;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

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

//    int LB;
    public Indexer<ArrayPair> indexer = new Indexer<ArrayPair>();

    public StrField() {}
    public StrField(String fieldName)
    {
        name = fieldName;
//        this.LB = LB;
        maxLength = Integer.MAX_VALUE;
    }

    public Indexer<ArrayPair> getIndexer()
    {
        return indexer;
    }

    public void setIndexer(Indexer<ArrayPair> indexer)
    {
        this.indexer = indexer;
    }

    @Override
    public String valueToString(int v)
    {
        ArrayPair ap = indexer.getObject(v);
        String[] out = new String[ap.same()];
        for(int i = 0; i < out.length; i++)
        {
            out[i] = (ap.labels.get(i) == Event3Model.getNone_lb()) ? "" :
                     "/"+ Event3Model.labelToString(ap.labels.get(i));
        }
        return Utils.mkString(out, " ");
    }

    @Override
    protected int parseValue(int role, String str)
    {
        String[] tokens = str.split(" ");
        ArrayList<Integer> words = new ArrayList<Integer>();
        ArrayList<Integer> labels = new ArrayList<Integer>();
        for(int i = 0; i < tokens.length; i++)
        {
            if (role == -1 || role == Event3Model.getWordRole(tokens[i]))
            {
                words.add(Event3Model.getWordIndex(tokens[i]));
                labels.add(Event3Model.getLabelIndex(Arrays.asList(tokens), i));
            }
        }
        return indexer.getIndex( new ArrayPair(words, labels));
    }

    @Override
    public AParams newParams(String prefix)
    {
        return new StrFieldParams(Event3Model.LB(), prefix);
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


    public class ArrayPair implements Serializable
    {
        ArrayList<Integer> words, labels;

        private ArrayPair(ArrayList<Integer> words, ArrayList<Integer> labels)
        {
            this.words = words;
            this.labels = labels;
        }

        public ArrayList<Integer> getWords()
        {
            return words;
        }

        public ArrayList<Integer> getLabels()
        {
            return labels;
        }

        int same()
        {
            return Utils.same(words.size(), labels.size());
        }
    }
}
