package induction.problem.event3;

import fig.basic.Indexer;
import fig.basic.IntPair;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import util.Stemmer;

/**
 *
 * @author sinantie
 */
public class WordLabel implements Serializable
{

    static final long serialVersionUID = -5666200934367151841L;
    List<IntPair> list;
//    Event3Model model;
    Indexer<String> wordIndexer;
    
    public WordLabel()
    {
    }
    
//    public WordLabel(Event3Model model) 
    public WordLabel(Indexer<String> wordIndexer) 
    {
        list = new ArrayList<IntPair>();
//        this.model = model;        
        this.wordIndexer = wordIndexer;
    }
    
    public void addWordLabel(int word, int label)
    {
        list.add(new IntPair(word, label));
    }    
    
    public void getWordsLabels(ArrayList<Integer> valueWords, ArrayList<Integer> valueLabels)
    {
        for(IntPair pair : list)
        {
            valueWords.add(pair.first);
            if(valueLabels != null)
                valueLabels.add(pair.second);
        }
    }

    public List<String> getWords(boolean stemAll, boolean lemmatiseAll)
    {
        List<String> out = new ArrayList<>();
        for(IntPair pair :  list)
        {
            if(lemmatiseAll) // we assume that values are already lemmatised (partially true for AMR input)
                out.add(Event3Model.wordToString(wordIndexer, pair.first, false, null));
            else if(stemAll)
                out.add(Stemmer.stem(Event3Model.wordToString(wordIndexer, pair.first, false, null)));
        }
        return out;
    }
    
    @Override
    public String toString()
    {
        StringBuilder str = new StringBuilder();
        for (IntPair pair : list) {
            str.append(Event3Model.wordToString(wordIndexer, pair.first, false, null));
            if (pair.second != Event3Model.getNone_lb())             
                str.append("/").append(Event3Model.labelToString(pair.second));
            str.append(" ");
        }
        return str.deleteCharAt(str.length() - 1).toString();
    }

    @Override
    public boolean equals(Object obj)
    {
        assert obj instanceof WordLabel;
        WordLabel wl = (WordLabel) obj;
        return list.equals(wl.list);        
    }

    @Override
    public int hashCode()
    {
        int hash = 3;
        hash = 41 * hash + (this.list != null ? this.list.hashCode() : 0);
        return hash;
    }    
}
