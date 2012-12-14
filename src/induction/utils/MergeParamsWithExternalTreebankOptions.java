package induction.utils;

import fig.basic.Option;
import fig.basic.OptionSet;
import induction.Options;

/**
 *
 * @author sinantie
 */
public class MergeParamsWithExternalTreebankOptions
{
    @OptionSet(name="modelOpts") public Options modelOpts = new Options();    
    @Option(gloss="external treebank file") public String externalTreebankFile;

}
