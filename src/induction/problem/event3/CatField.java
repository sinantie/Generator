package induction.problem.event3;

import induction.problem.event3.params.CatFieldParams;
import fig.basic.Indexer;
import induction.Utils;
import induction.problem.AParams;
import induction.problem.VecFactory;
import java.io.Serializable;

/**
 * Category (@) - we have to learn the possibly ambiguous correspondence
 * between category values and words
 * Example: windDir in the weather domain
 * indexer = possible values (e.g., N, W, ...)
 * @author konstas
 */
public class CatField extends Field implements Serializable
{
    static final long serialVersionUID = -1743566941068800509L;
    private Indexer<String> indexer = new Indexer<String>();
//    private int W;

    public CatField() {}
    public CatField(String fieldName)
    {
//        this.W = W;
        name = fieldName;
        maxLength = Integer.MAX_VALUE;
    }

    public void setIndexer(Indexer<String> indexer)
    {
        this.indexer = indexer;
    }

    public Indexer<String> getIndexer()
    {
        return indexer;
    }

    @Override
    public String valueToString(int v)
    {
        return indexer.getObject(v);
    }

    public String[] valuesToStringArray()
    {
        String[] out = new String[indexer.getObjects().size()];
        return indexer.getObjects().toArray(out);
    }

    @Override
    public int parseValue(int role, String str)
    {
        return indexer.getIndex(str);
    }

    public boolean containsValue(String str)
    {
        return indexer.contains(str);
    }
    
    @Override
    public AParams newParams(Event3Model model, int numOfWords, VecFactory.Type vectorType, String prefix)
    {
        return new CatFieldParams(model, numOfWords, vectorType, prefix, this);
    }

    @Override
    public int getV()
    {
        return indexer.size();
    }

    @Override
    public String toString()
    {
        return Utils.fmts("@%s(%s)=>%s", name, getV(), indexer);
    }
}
