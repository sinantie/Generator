package induction.utils;

import fig.basic.Option;
import fig.basic.OptionSet;
import induction.Options;

/**
 *
 * @author sinantie
 */
public class CrossFoldPartitionDatasetOptions
{
    public enum Type {raw, event3};
    
    @OptionSet(name="modelOpts") public Options modelOpts = new Options();
    
    @Option(required=true) public Type inputType = Type.raw;    
    @Option(gloss="Prefix string to append to resulting fold files") public String prefix;
    @Option(gloss="Number of folds") public int folds;
    @Option(gloss="Shuffle input list before splitting to folds") public boolean shuffle = false;
}