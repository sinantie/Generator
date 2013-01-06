package induction.utils;

import edu.berkeley.nlp.ling.Tree;
import edu.berkeley.nlp.ling.Trees.PennTreeReader;
import fig.basic.LogInfo;
import fig.exec.Execution;
import induction.Options.InitType;
import induction.Utils;
import induction.problem.AExample;
import induction.problem.AParams.ParamsType;
import induction.problem.Vec;
import induction.problem.event3.CFGRule;
import induction.problem.event3.Example;
import induction.problem.event3.generative.GenerativeEvent3Model;
import induction.problem.event3.params.CFGParams;
import induction.problem.event3.params.Params;
import induction.problem.event3.params.TrackParams;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
/**
 *
 * @author sinantie
 */
public class MergeParamsWithExternalTreebank
{
    MergeParamsWithExternalTreebankOptions opts;
    GenerativeEvent3Model model;
    Map<String, Example> examplesMap;
    Map<String, String> treebankMap;
    public MergeParamsWithExternalTreebank(MergeParamsWithExternalTreebankOptions opts)
    {
        this.opts = opts;
        this.treebankMap = new HashMap<String, String>();
    }
    
    public void execute()
    {
        model = new GenerativeEvent3Model(opts.modelOpts);
        // load initial parameters
        model.init(InitType.staged, opts.modelOpts.initRandom, "");
        model.readExamples();
        // read examples and put them in a map. The treebank file might have less
        // examples, so we need to crossref.
        examplesMap = new HashMap<String, Example>();
        for(AExample ex : model.getExamples())
            examplesMap.put(ex.getName(), (Example)ex);
        ExportExamplesToSingleFile.readTreebankFile(opts.externalTreebankFile, treebankMap);
        updateParams();
        String name = "stage1.extTreebank";
        model.saveParams(name);
        if(!opts.modelOpts.dontOutputParams)
        {
            model.getParams().outputNonZero(Execution.getFile(name+".params.gz"), ParamsType.PROBS);
        }
    }

    public void updateParams()
    {        
        // restore estimates from corpus and replace what is already in the params file
        CFGParams cfgParams = ((Params)model.getParams()).cfgParams; 
        cfgParams.setUniform(0); // reset the cfg rules vectors to 0
        Map<Integer, Vec> cfgRulesChoices = cfgParams.getCfgRulesChoices();
        // we need to update the eventType distribution as well
        TrackParams cparams = ((Params)model.getParams()).trackParams[0];
        cparams.getEventTypeChoices()[cparams.boundary_t].set(0);
        for(Entry<String, String> treebankEntry : treebankMap.entrySet())
        {
            Tree<String> tree = new PennTreeReader(new StringReader(treebankEntry.getValue())).next();
            if(tree == null)
            {
                LogInfo.error("Input file does not contain parse trees!");
                Execution.finish();
            }
            // add 1 count to the corresponding document length bin of the root rule
            Example ex = examplesMap.get(treebankEntry.getKey());
            int N = ex.N();
//            int docLengthBin = N >= opts.modelOpts.maxDocLength ? cfgParams.getNumOfBins() - 1 : N / opts.modelOpts.docLengthBinSize;   
            int docLengthBin = N >= opts.modelOpts.maxDocLength ? cfgParams.getNumOfBins() - 1 : N / opts.modelOpts.docLengthBinSize;   
            int indexOfRule = model.getCfgRuleIndex(new CFGRule(tree, model.getRulesIndexer()));
            cfgParams.getWordsPerRootRule()[indexOfRule].addCount(docLengthBin, 1.0);
            
            // add 1 count to each cfg rule in each subtree of the parse tree
            for(Iterator<Tree> it = tree.iterator(); it.hasNext(); )
            {
                Tree<String> subtree = it.next();
//                if(Utils.countableRule(subtree)) // count only the binary rules
                if(subtree.getChildren().size() > 1) // count only the binary rules
                {
                    CFGRule rule = new CFGRule(subtree, model.getRulesIndexer());
                    cfgRulesChoices.get(rule.getLhs()).addCount(model.getCfgRuleIndex(rule), 1.0);
                }
                else // leaf or preterminal rule
                {                    
                    String label = tree.getLabel();
                    int t0 = label.equals("none") ? cfgParams.none_t : model.getEventTypeNameIndexer().getIndex(label);
                    if(t0 == cfgParams.none_t)
                        cparams.getEventTypeChoices()[cparams.boundary_t].addCount(cparams.none_t, 1.0);
                    else
                        cparams.getEventTypeChoices()[cparams.boundary_t].addCount(t0, 1.0d/(double)ex.getEventTypeCounts()[t0]);
                }
            }
        }
        cfgParams.optimise(opts.modelOpts.initSmoothing);
        for(Vec v : cfgParams.getVecs().values())
            v.setProbSortedIndices();
    }
    
    void testExecute()
    {
        model = new GenerativeEvent3Model(opts.modelOpts);
        // load initial parameters
        model.init(InitType.staged, opts.modelOpts.initRandom, "");
        model.readExamples();
        // read examples and put them in a map. The treebank file might have less
        // examples, so we need to crossref.
        examplesMap = new HashMap<String, Example>();
        for(AExample ex : model.getExamples())
            examplesMap.put(ex.getName(), (Example)ex);
        ExportExamplesToSingleFile.readTreebankFile(opts.externalTreebankFile, treebankMap);
        updateParams();
        System.out.println(((Params)model.getParams()).cfgParams.outputNonZero(ParamsType.PROBS));
    }
}
