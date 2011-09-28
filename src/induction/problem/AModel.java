package induction.problem;

import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Graph;
import fig.basic.FullStatFig;
import fig.basic.LogInfo;
import fig.exec.Execution;
import induction.ngrams.KylmNgramWrapper;
import induction.LearnOptions;
import induction.MyCallable;
import induction.ngrams.NgramModel;
import induction.Options;
import induction.Options.InitType;
import induction.Options.NgramWrapper;
import induction.ngrams.SrilmNgramWrapper;
import induction.Utils;
import induction.WekaWrapper;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;

/**
 *
 * @author konstas
 */
// A problem is defined by params, performance, example, inferState, model
public abstract class AModel<Widget extends AWidget,
                             Params extends AParams,
//                             Performance extends APerformance<Widget>,
                             Example extends AExample<Widget>>
//                             InferState extends AInferState<Widget, Example, Params> >
                             implements ModelInterface
{
    protected Options opts;
    protected Params params;
    protected List<Example> examples = new ArrayList<Example>();
    private int numExamples, maxExamples;
    protected PrintWriter trainPredOut, testPredOut, trainFullPredOut, testFullPredOut;
    protected APerformance trainPerformance, testPerformance;
    protected NgramModel ngramModel;
    protected WekaWrapper lengthPredictor;

    int currExample;

    protected MaxentTagger posTagger;

    public AModel(Options opts)
    {
        this.opts = opts;
        maxExamples = opts.maxExamples;
    }

    protected abstract Params newParams();
    protected abstract APerformance newPerformance();

    protected Example tokensToExample(String[] tokens)
    {
        throw new UnsupportedOperationException("Not supported");
    }

    protected abstract AInferState newInferState(Example ex, Params params,
                                                Params counts, InferSpec ispec);
    protected abstract AInferState newInferState(Example ex, Params params,
                                                Params counts, InferSpec ispec, Graph graph);

    @Override
    public void logStats()
    {
        Execution.putLogRec("numExamples", examples.size());
    }

    protected int genSample(ProbVec v)
    {
        return v.sample(opts.genRandom);
    }

    protected abstract Example genExample(int index);

    @Override
    public void genExamples()
    {
        params.output(Execution.getFile("gen.params"));
        Utils.begin_track("Generating %s examples", Utils.fmt(opts.genNumExamples));
        examples = new ArrayList(opts.genNumExamples);
        String[] examplesToString = new String[examples.size()];
        for(int i = 0; i < examples.size(); i++)
        {
            examples.add(genExample(i));
            examplesToString[i] = exampleToString(examples.get(i));
        }

        Utils.writeLines(Execution.getFile("gen.examples"), examplesToString);
    }

    protected abstract Integer[] widgetToIntSeq(Widget widget);
    protected abstract String widgetToSGMLOutput(Example ex, Widget widget);

    protected String widgetToFullString(Example ex, Widget widget)
    {
        return exampleToString(ex) + " " + Utils.mkString(widgetToIntSeq(widget), " ");
    }

    protected abstract String exampleToString(Example ex);

    protected void supervisedInitParams()
    {
        Utils.begin_track("supervisedInitParams");
        final Params counts = newParams();
        params.setUniform(1);
        Collection<InitParams> list = new ArrayList(examples.size());
        for(int i = 0; i < examples.size(); i++)
        {
            list.add(new InitParams(i, examples.get(i), counts));
        }
        Utils.parallelForeach(opts.numThreads, list);
        params = counts;
        params.optimise(opts.initSmoothing);
        LogInfo.end_track();
    }

    
    protected abstract void saveParams(String name);
   
    protected void uniformzInitParams()
    {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    protected void artificialInitParams()
    {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    protected void baitInitParams()
    {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    // Default thing to do with a file: assume each example is a line
    // Override if want different behavior (e.g., WSJ)
    protected void readExamples(String path, int maxExamples)
    {
        String[] lines = Utils.readLines(path, maxExamples);
        for(String line : lines)
        {
            examples.add(tokensToExample(line.split("\\s+")));
            numExamples++;
        }
    }

    @Override
    public void readExamples()
    {
        Utils.begin_track("Reading examples");
        numExamples = 0;
        examples.clear();
        if(opts.posAtSurfaceLevel)
        {
            try
            {
                posTagger = new MaxentTagger("lib/models/bidirectional-distsim-wsj-0-18.tagger");
            }
            catch(Exception e)
            {
                Execution.finish();
            }
        }
        if(opts.lengthPredictionModelFile != null)
        {
            Utils.begin_track("Loading Length Prediction Model...");
            lengthPredictor = new WekaWrapper(opts.stagedParamsFile,
                    opts.lengthPredictionModelFile,
                    opts.lengthPredictionStartIndex,
                    opts.lengthPredictionFeatureType, WekaWrapper.Mode.TEST);
            LogInfo.end_track();
        }
        boolean setTrainTest = !opts.testInputPaths.isEmpty() ||
                !opts.testInputLists.isEmpty();

        if (setTrainTest)
        {
            opts.trainStart = numExamples;
        }
        ArrayList<String> excludeLists = new ArrayList();
        if(opts.excludeLists != null)
        {
            String[] temp = Utils.readLines(opts.excludeLists);
            excludeLists.addAll(Arrays.asList(temp));
        }
        if(opts.examplesInSingleFile)
            readFromSingleFile(opts.inputLists);
        else
            read(opts.inputPaths, opts.inputLists, excludeLists);
        if (setTrainTest)
        {
            opts.trainEnd = numExamples;
        }

        // Allow for the test examples
        maxExamples = Utils.safeAdd(maxExamples, opts.testMaxExamples);

        if (setTrainTest)
        {
            opts.testStart = numExamples;
        }
        if(opts.examplesInSingleFile)
            readFromSingleFile(opts.testInputLists);
        else
            read(opts.testInputPaths, opts.testInputLists, excludeLists);
        if (setTrainTest)
        {
            opts.testEnd = numExamples;
        }

        if (setTrainTest)
        {
            Utils.logss("readExamples: train: %s...%s; test: %s...%s",
                        opts.trainStart, opts.trainEnd,
                        opts.testStart, opts.testEnd);
        }
//        examples = (Example[]) new AExample[examplesList.size()];
//        examplesList.toArray(examples);
        LogInfo.end_track();
    }

    private boolean needMoreExamples()
    {
        return numExamples < maxExamples;
    }

    private boolean validName(String path)
    {
        return (Utils.isEmpty(opts.inputFileExt) ||
               path.endsWith(opts.inputFileExt)) &&
               !path.startsWith("#");
    }

    private void addPath(String path)
    {
        File file = new File(path);
        if(file.isDirectory())
        {
            for(String fileStr : Utils.sortWithEmbeddedInt(file.list()))
            {
                if(needMoreExamples())
                {
                    addPath(path + "/" + fileStr);
                }
            } // for
        } // if
        else if(needMoreExamples() && validName(path))
        {
            Utils.begin_track("%s (%s examples so far)", path, ++numExamples);
            readExamples(path, maxExamples - numExamples);
            LogInfo.end_track();
        }
    }

    private void read(ArrayList<String> inputPaths, ArrayList<String> inputLists,
                      ArrayList<String> excludeLists)
    {
        for(String path : inputPaths)
        {
            addPath(path);
        }
        for(String path : inputLists) // file that contains paths
        {
            for(String line : Utils.readLines(path))
            {
                if(needMoreExamples() && !excludeLists.contains(line))
                {
//                    addPath(pathName(path, line));
                    addPath(line);
                }
            } // for
        } // for
    }

    private void readFromSingleFile(ArrayList<String> inputLists)
    {
        for(String file : inputLists)
        {
            if(new File(file).exists())
            {
                String key = null;
                StringBuilder str = new StringBuilder();
                for(String line : Utils.readLines(file))
                {
                    if(line.startsWith("Example_"))
                    {
                        if(key != null) // only for the first example
                        {
                            ++numExamples;
                            readExamples(str.toString(), maxExamples - numExamples);
                            str = new StringBuilder();
                        }
                        key = line;
                    } // if                   
                    str.append(line).append("\n");
                }  // for
                ++numExamples;
                readExamples(str.toString(), maxExamples - numExamples); // don't forget last example
            } // if
        }
    }

    private String pathName(String path, String f)
    {
        if(f.charAt(0) == '/')
        {
            return f;
        }
        return new File(path).getParent() + "/" + f;
    }
    @Override
    public void preInit() {}

    public abstract void stagedInitParams();
    
    @Override
    public void init(InitType initType, Random initRandom, String name)
    {
      Utils.begin_track("Init parameters: %s", initType);
      if(initType == InitType.staged)
      {
           stagedInitParams();
//           params.output(Execution.getFile("init.params"));
      }
      else
      {
          params = newParams();
          switch(initType)
          {
              case random : params.randomise(initRandom, opts.initNoise);
                            params.optimise(opts.initSmoothing); break;
              case bait : baitInitParams(); break;
              case supervised : supervisedInitParams(); break;
              case uniformz : uniformzInitParams(); break;
              case artificial: artificialInitParams(); break;
              default : throw new UnsupportedOperationException("Invalid init type");
          }
          //params.output(Execution.getFile("init.params"));
      }
     
      LogInfo.end_track();
    }

    protected boolean isTrain(int i)
    {
        return opts.trainStart <= i && i < opts.trainEnd;
    }
    protected boolean isTest(int i)
    {
        return opts.testStart <= i && i < opts.testEnd;
    }

    // ext specifies the iteration or example number
    // Use the given params (which are actually counts so we can evaluate even in batch EM)
    protected void record(String ext, String name, FullStatFig complexity)
    {
        Utils.logs("Inference complexity: %s", complexity);
        if (!(trainPerformance == null || trainPerformance.isEmpty()))
        {
            trainPerformance.record("train");
            trainPerformance.output(
                    Execution.getFile(name+".train.performance."+ext));
        }
        if (!(testPerformance == null || testPerformance.isEmpty()))
        {
            testPerformance.record("test");
            testPerformance.output(
                    Execution.getFile(name+".test.performance."+ext));
        }
    }

    private void processExample(int i, Example ex, double stepSize, Params counts,
                                double temperature, LearnOptions lopts,
                                int iter, FullStatFig complexity)
    {
        processInferState(createInferState(ex, stepSize, counts, temperature,
                lopts, iter, complexity), i, ex);
    }

    private AInferState createInferState(Example ex, double stepSize,
            Params counts, double temperature, LearnOptions lopts, int iter,
            FullStatFig complexity, Graph graph)
    {
        AInferState currentInferState = newInferState(ex, params, counts,
        new InferSpec(temperature, !lopts.hardUpdate, true, lopts.hardUpdate,
                      false, lopts.mixParamsCounts, lopts.useVarUpdates,
                      stepSize, iter), graph);
        currentInferState.createHypergraph();
        currentInferState.doInference();
        synchronized(complexity)
        {
            complexity.add(currentInferState.getComplexity());
        }
        return currentInferState;
    }

    protected AInferState createInferState(Example ex, double stepSize,
            Params counts, double temperature, LearnOptions lopts, int iter,
            FullStatFig complexity)
    {
        AInferState currentInferState = newInferState(ex, params, counts,
        new InferSpec(temperature, !lopts.hardUpdate, true, lopts.hardUpdate,
                      false, lopts.mixParamsCounts, lopts.useVarUpdates,
                      stepSize, iter));
        currentInferState.createHypergraph();
        currentInferState.doInference();
        synchronized(complexity)
        {
            complexity.add(currentInferState.getComplexity());
        }
        return currentInferState;
    }

    private void processInferState(AInferState inferState, int i, Example ex)
    {
        if (isTrain(i))
        {
            inferState.updateCounts();
            synchronized(trainPerformance)
            {
                trainPerformance.add(inferState.stats());
//                trainPerformance.add(ex.getTrueWidget(), inferState.bestWidget);
                trainPerformance.add(ex, inferState.bestWidget);
                if(trainPredOut != null)
                {
                    trainPredOut.println(Utils.mkString(widgetToIntSeq(inferState.bestWidget), " "));
                }
                if(trainFullPredOut != null)
                {
                    trainFullPredOut.println(widgetToFullString(ex, inferState.bestWidget));
                }
            }
        }
        if (isTest(i))
        {
            synchronized(testPerformance)
            {
                testPerformance.add(inferState.stats());
//                testPerformance.add(ex.getTrueWidget(), inferState.bestWidget);
                testPerformance.add(ex, inferState.bestWidget);
                if(testPredOut != null)
                {
                    if(opts.modelType == Options.ModelType.generate)
                        testPredOut.println(widgetToSGMLOutput(ex, inferState.bestWidget));
                    else
                        testPredOut.println(Utils.mkString(widgetToIntSeq(inferState.bestWidget), " "));
                }
                if(testFullPredOut != null)
                {
                    testFullPredOut.println(widgetToFullString(ex, inferState.bestWidget));
                }
            }
        }
    }

    private String summary(int i)
    {
        if (isTrain(i))
            return "train: "+trainPerformance.summary();
        else if (isTest(i))
            return "test: "+testPerformance.summary();
        else return "(skip)";
    }
        
    /**
     * helper method for testing the learning output. Simulates learn(...) method
     * for a single example without the thread mechanism
     * @return a String with the aligned events' indices
     */
    public String testStagedLearn(String name, LearnOptions lopts)
    {
        opts.alignmentModel = lopts.alignmentModel;
        FullStatFig complexity = new FullStatFig();
        double temperature = lopts.initTemperature;
        testPerformance = newPerformance();
        Params counts = newParams();
        Example ex = examples.get(0);
        AInferState inferState =  createInferState(ex, 1, counts, temperature,
                lopts, 0, complexity);
//        testPerformance.add(ex.getTrueWidget(), inferState.bestWidget);
        testPerformance.add(ex, inferState.bestWidget);
        System.out.println(widgetToFullString(ex, inferState.bestWidget));
//        int i = 0;
//        for(Example ex: examples)
//        {
//            try
//            {
//            InferState inferState =  createInferState(ex, 1, counts, temperature,
//                    lopts, 0, complexity);
//            testPerformance.add(ex, inferState.bestWidget);
//            System.out.println(widgetToFullString(ex, inferState.bestWidget));
//
//            }
//            catch(Exception e)
//            {
//                System.out.println(i+ " " + e.getMessage());
//                e.printStackTrace();
//            }
//            i++;
//        }
//        return "";
        return Utils.mkString(widgetToIntSeq(inferState.bestWidget), " ");
    }

    /**
     * helper method for testing the learning output. Simulates learn(...) method
     * for a number of examples without the thread mechanism.
     * @return a String with the aligned events' indices of the last example
     */
    public String testInitLearn(String name, LearnOptions lopts)
    {
        opts.alignmentModel = lopts.alignmentModel;
        FullStatFig complexity = new FullStatFig();
        double temperature = lopts.initTemperature;
        int iter = 0;
        AInferState inferState = null;
        while (iter < lopts.numIters)
        {
            trainPerformance = newPerformance();
            Params counts = newParams();
//            Example ex = examples.get(0);
            for(Example ex: examples)
            {
                try
                {
                    inferState =  createInferState(ex, 1, counts, temperature,
                        lopts, iter, complexity);
                    inferState.updateCounts();
                    trainPerformance.add(ex, inferState.bestWidget);
                }
                catch(Exception e)
                {
                    System.out.println(ex.toString());
                    e.printStackTrace();
                    System.exit(0);
                }
            }
            // M step
            params = counts;
            params.optimise(lopts.smoothing);            
            iter++;
        }
//        System.out.println(params.output());
        return Utils.mkString(widgetToIntSeq(inferState.bestWidget), " ");
    }

    /**
     * helper method for testing the discriminative learning scheme. 
     * Simulates learn(...) method from the DiscriminativeEvent3Model class
     * for a number of examples without the thread mechanism.
     * @return the average Viterbi log probability
     */
    public double testDiscriminativeLearn(String name, LearnOptions lopts)
    {
        learn(name, lopts);
        return trainPerformance.getAccuracy();
    }
    
    /**
     * helper method for testing the generation output. Simulates generate(...) method
     * for a single example without the thread mechanism
     * @return a String with the generated SGML text output (contains results as well)
     */
    public String testGenerate(String name, LearnOptions lopts)
    {
        opts.alignmentModel = lopts.alignmentModel;
        ngramModel = new KylmNgramWrapper(opts.ngramModelFile);
        FullStatFig complexity = new FullStatFig();
        double temperature = lopts.initTemperature;
        testPerformance = newPerformance();
        Params counts = newParams();
        Example ex = examples.get(0);
        AInferState inferState =  createInferState(ex, 1, counts, temperature,
                lopts, 0, complexity);
        testPerformance.add(ex, inferState.bestWidget);
        System.out.println(widgetToFullString(ex, inferState.bestWidget));
        return widgetToSGMLOutput(ex, inferState.bestWidget);
    }

    /**
     * helper method for testing the semantic parse output. Simulates generate(...) method
     * for a single example without the thread mechanism
     * @return the accuracy of the semantic parsing
     */
    public String testSemParse(String name, LearnOptions lopts)
    {
        opts.alignmentModel = lopts.alignmentModel;
        if(opts.ngramWrapper == NgramWrapper.kylm)
            ngramModel = new KylmNgramWrapper(opts.ngramModelFile);
        else if(opts.ngramWrapper == NgramWrapper.srilm)
            ngramModel = new SrilmNgramWrapper(opts.ngramModelFile, opts.ngramSize);
        FullStatFig complexity = new FullStatFig();
        double temperature = lopts.initTemperature;
        testPerformance = newPerformance();
        Params counts = newParams(); int i = 0;
        for(Example ex: examples)
        {
//        Example ex = examples.get(0);
            try
            {
                AInferState inferState =  createInferState(ex, 1, counts, temperature,
                        lopts, 0, complexity);
                testPerformance.add(ex, inferState.bestWidget);
                System.out.println(widgetToFullString(ex, inferState.bestWidget));
            }
            catch(Exception e)
            {
                System.out.println(i+ " " + e.getMessage());
                e.printStackTrace();
            }
            i++;
        }

        return testPerformance.output();
    }

    public Graph testSemParseVisualise(String name, LearnOptions lopts)
    {
        opts.alignmentModel = lopts.alignmentModel;
//        ngramModel = new KylmNgramWrapper(opts.ngramModelFile);
        FullStatFig complexity = new FullStatFig();
        double temperature = lopts.initTemperature;
        testPerformance = newPerformance();
        Params counts = newParams();
        Example ex = examples.get(0);
        Graph graph = new DirectedSparseGraph<String, String>();
        AInferState inferState =  createInferState(ex, 1, counts, temperature,
                lopts, 0, complexity, graph);
        testPerformance.add(ex, inferState.bestWidget);
        System.out.println(widgetToFullString(ex, inferState.bestWidget));
                                    
        return graph;
    }

    public Graph testGenerateVisualise(String name, LearnOptions lopts)
    {
        opts.alignmentModel = lopts.alignmentModel;
        ngramModel = new KylmNgramWrapper(opts.ngramModelFile);
        FullStatFig complexity = new FullStatFig();
        double temperature = lopts.initTemperature;
        testPerformance = newPerformance();
        Params counts = newParams();
        Example ex = examples.get(0);
        Graph graph = new DirectedSparseGraph<String, String>();
        AInferState inferState =  createInferState(ex, 1, counts, temperature,
                lopts, 0, complexity, graph);
        testPerformance.add(ex, inferState.bestWidget);
        System.out.println(widgetToFullString(ex, inferState.bestWidget));
        return graph;
    }

    protected class InitParams extends MyCallable
    {
        private Example ex;
        private int i;
        private Params counts;

        InitParams(int i, Example ex, Params counts)
        {
            this.i = i;
            this.ex = ex;
            this.counts = counts;
        }     

        @Override
        public Object call() throws Exception
        {
            if(isLog()) Utils.begin_track("Example %s/%s", Utils.fmt(i), Utils.fmt(examples.size()));
            newInferState(ex, params, counts, new InferSpec(1, false, false,
                    false, true, false, false, 1, 0)).updateCounts();   // don't we need to save somehow??
            if(isLog()) LogInfo.end_track();
            return null;
        }
    }

    protected class BatchEM extends MyCallable
    {
        private Example ex;
        private int i, iter;
        private Params counts;
        private double temperature;
        private LearnOptions lopts;
        private FullStatFig complexity;

        public BatchEM(int i, Example ex, Params counts, double temperature,
                LearnOptions lopts, int iter, FullStatFig complexity)
        {
            this.i = i;
            this.ex = ex;
            this.counts = counts;
            this.temperature = temperature;
            this.lopts = lopts;
            this.iter = iter;
            this.complexity = complexity;
        }
        @Override
        public Object call() throws Exception
        {
            processExample(i, ex, 1, counts, temperature, lopts, iter, complexity);
            if (opts.outputExampleFreq != 0 && i % opts.outputExampleFreq == 0)
                Utils.begin_track("Example %s/%s: %s", Utils.fmt(i+1),
                         Utils.fmt(examples.size()), summary(i));
            if (opts.outputExampleFreq != 0 && i % opts.outputExampleFreq == 0)
                Execution.putOutput("currExample", i);
//            processExample(i, ex, 1, counts, temperature, lopts, iter, complexity);
            if (opts.outputExampleFreq != 0 && i % opts.outputExampleFreq == 0)
                LogInfo.end_track();
            return null;
        }
    }
}
