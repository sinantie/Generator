package induction.utils;

import fig.basic.Option;
import fig.basic.OptionSet;
import induction.Options;

/**
 *
 * @author sinantie
 */
public class ComputeAmrEstimatesOptions
{
    @OptionSet(name="modelOpts") public Options modelOpts = new Options();    
    @Option(gloss="external sentences file") public String sentencesFile;
    @Option(gloss="external GHKM tree file") public String GHKMTreeFile;
    @Option(gloss="external tree-sentences alignment file") public String alignmentsFile;
    @Option(gloss="strip concept sense") public boolean stripConceptSense;

}
