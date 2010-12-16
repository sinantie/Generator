package induction.problem.event3;

import fig.basic.Indexer;
import induction.Utils;
import induction.problem.AParams;
import java.io.Serializable;

/**
 * Category (@) - we have to learn the possibly ambiguous correspondance
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

    protected String[] valuesToStringArray()
    {
        String[] out = new String[indexer.getObjects().size()];
        return indexer.getObjects().toArray(out);
    }

    @Override
    protected int parseValue(int role, String str)
    {
        return indexer.getIndex(str);
    }

    @Override
    protected AParams newParams(String prefix)
    {
        return new CatFieldParams(Event3Model.W(), prefix, this);
    }

    @Override
    protected int V()
    {
        return indexer.size();
    }

    @Override
    public String toString()
    {
        return Utils.fmts("@%s(%s)", name, V());
    }
}
