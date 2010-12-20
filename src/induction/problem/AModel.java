package induction.problem;

import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import fig.basic.FullStatFig;
import fig.basic.IOUtils;
import fig.basic.LogInfo;
import fig.exec.Execution;
import fig.record.Record;
import induction.KylmNgramWrapper;
import induction.LearnOptions;
import induction.MyCallable;
import induction.NgramModel;
import induction.Options;
import induction.Options.InitType;
import induction.RoarkNgramWrapper;
import induction.SrilmNgramWrapper;
import induction.Utils;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
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
                             Performance extends APerformance<Widget>,
                             Example extends AExample<Widget>,
                             InferState extends AInferState<Widget, Example, Params> >
                             implements ModelInterface
{
    protected Options opts;
    protected Params params;
    protected Performance performance;
    protected InferState inferState;
    protected List<Example> examples = new ArrayList<Example>();
    private int numExamples, maxExamples;
    private PrintWriter trainPredOut, testPredOut, trainFullPredOut, testFullPredOut;
    private Performance trainPerformance, testPerformance;
    protected NgramModel ngramModel;
    int currExample;

    protected MaxentTagger posTagger;

    public AModel(Options opts)
    {
        this.opts = opts;
        maxExamples = opts.maxExamples;
    }

    protected abstract Params newParams();
    protected abstract Performance newPerformance();

    protected Example tokensToExample(String[] tokens)
    {
        throw new UnsupportedOperationException("Not supported");
    }

    protected abstract InferState newInferState(Example ex, Params params,
                                                Params counts, InferSpec ispec);

    public void logStats()
    {
        Execution.putLogRec("numExamples", examples.size());
    }

    protected int genSample(ProbVec v)
    {
        return v.sample(opts.genRandom);
    }

    protected abstract Example genExample(int index);

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
        boolean setTrainTest = opts.testInputPaths.size() > 0 ||
                opts.testInputLists.size() > 0;

        if (setTrainTest)
        {
            opts.trainStart = numExamples;
        }
        read(opts.inputPaths, opts.inputLists);
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
        read(opts.testInputPaths, opts.testInputLists);
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
        return Utils.isEmpty(opts.inputFileExt) ||
               path.endsWith(opts.inputFileExt);
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

    private void read(ArrayList<String> inputPaths, ArrayList<String> inputLists)
    {
        for(String path : inputPaths)
        {
            addPath(path);
        }
        for(String path : inputLists) // file that contains paths
        {
            for(String line : Utils.readLines(path))
            {
                if(needMoreExamples())
                {
//                    addPath(pathName(path, line));
                    addPath(line);
                }
            } // for
        } // for
    }

    private String pathName(String path, String f)
    {
        if(f.charAt(0) == '/')
        {
            return f;
        }
        return new File(path).getParent() + "/" + f;
    }
    public void preInit() {}

    public abstract void stagedInitParams();
    
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
          params.output(Execution.getFile("init.params"));
      }
     
      LogInfo.end_track();
    }

    private boolean isTrain(int i)
    {
        return opts.trainStart <= i && i < opts.trainEnd;
    }
    private boolean isTest(int i)
    {
        return opts.testStart <= i && i < opts.testEnd;
    }

    // ext specifies the iteration or example number
    // Use the given params (which are actually counts so we can evaluate even in batch EM)
    private void record(String ext, String name, FullStatFig complexity,
                        boolean output)
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

    private InferState createInferState(Example ex, double stepSize,
            Params counts, double temperature, LearnOptions lopts, int iter,
            FullStatFig complexity)
    {
        InferState currentInferState = newInferState(ex, params, counts,
        new InferSpec(temperature, !lopts.hardUpdate, true, lopts.hardUpdate,
                      false, lopts.mixParamsCounts, lopts.useVarUpdates,
                      stepSize, iter));
        synchronized(complexity)
        {
            complexity.add(currentInferState.getComplexity());
        }
        return currentInferState;
    }

    private void processInferState(InferState inferState, int i, Example ex)
    {
        if (isTrain(i))
        {
            inferState.updateCounts();
            synchronized(trainPerformance)
            {
                trainPerformance.add(inferState.stats());
                trainPerformance.add(ex.getTrueWidget(), inferState.bestWidget);
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
                testPerformance.add(ex.getTrueWidget(), inferState.bestWidget);
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

    public void learn(String name, LearnOptions lopts)
    {
        opts.alignmentModel = lopts.alignmentModel; // HACK        
        Record.begin(name);
        Utils.begin_track("Train: " + name);
        boolean existsTrain = false, existsTest = false, output, fullOutput;
        for(int i = 0; i < examples.size(); i++)
        {
            if(isTrain(i))
            {
                existsTrain = true; break;
            }
        }
        for(int i = 0; i < examples.size(); i++)
        {
            if(isTest(i))
            {
                existsTest = true; break;
            }
        }
        
        int iter = 0;
        while (iter < lopts.numIters)
        {
            FullStatFig complexity = new FullStatFig(); // Complexity inference
            // Gradually reduce temperature
            double temperature = (lopts.numIters == 1) ? lopts.initTemperature :
                lopts.initTemperature +
                (lopts.finalTemperature- lopts.initTemperature) *
                iter / (lopts.numIters - 1);

            Utils.begin_track("Iteration %s/%s: temperature = %s",
                    Utils.fmt(iter+1), Utils.fmt(lopts.numIters),
                    Utils.fmt(temperature));
            Record.begin("iteration", iter+1);
            Execution.putOutput("currIter", iter+1);

            trainPerformance = existsTrain ? newPerformance() : null;
            testPerformance = existsTest ? newPerformance() : null;

//            output = opts.outputIterFreq != 0 && iter % opts.outputIterFreq == 0;
            output = (iter+1) % lopts.numIters == 0; // output only at the last iteration
            fullOutput = output && opts.outputFullPred;
            try
            {
                trainPredOut = (output && existsTrain) ?
                    IOUtils.openOut(Execution.getFile(
                    name+".train.pred."+iter)) : null;
                testPredOut = (output && existsTest) ?
                    IOUtils.openOut(Execution.getFile(
                    name+".test.pred."+iter)) : null;
                trainFullPredOut = (fullOutput && existsTrain) ?
                    IOUtils.openOut(Execution.getFile(
                    name+".train.full-pred."+iter)) : null;
                testFullPredOut = (fullOutput && existsTest) ?
                    IOUtils.openOut(Execution.getFile(
                    name+".test.full-pred."+iter)) : null;
            }
            catch(IOException ioe)
            {
                Utils.begin_track("Error opening file");
                LogInfo.end_track();
            }

            // Batch EM only
            Params counts = newParams();

            // E-step
            Utils.begin_track("E-step");
            Collection<BatchEM> list = new ArrayList(examples.size());
            for(int i = 0; i < examples.size(); i++)
            {
                list.add(new BatchEM(i, examples.get(i), counts, temperature,
                        lopts, iter, complexity));
            }
            Utils.parallelForeach(opts.numThreads, list);
            LogInfo.end_track();
            list.clear();
            // M-step
            params = counts;
            params.saveSum(); // 02/07/09: for printing out posterior mass (see AParams.foreachProb)
            if (lopts.useVarUpdates)
            {
                params.optimiseVar(lopts.smoothing);
            }
            else
            {
                params.optimise(lopts.smoothing);
            }
            
            record(String.valueOf(iter), name, complexity, output);
            if(trainPredOut != null) trainPredOut.close();
            if(testPredOut != null) testPredOut.close();
            if(trainFullPredOut != null) trainFullPredOut.close();
            if(testFullPredOut != null) testFullPredOut.close();

            Execution.putOutput("currExample", examples.size());
            LogInfo.end_track();
            Record.end();
            // Final
            if (iter == lopts.numIters - 1)
            {
                LogInfo.track("Final", true);
                if(trainPerformance != null)
                    trainPerformance.record("train");
                if(testPerformance != null)
                    testPerformance.record("test");
            }
            iter++;
            if (Execution.shouldBail() || existsTest) // it makes sense to perform one iteration at test time
                lopts.numIters = iter;
        } // while (iter < lopts.numIters)
        saveParams(name);
        params.output(Execution.getFile(name+".params"));
        LogInfo.end_track();
        LogInfo.end_track();
        Record.end();
        Record.end();
        Execution.putOutput("currIter", lopts.numIters);
    }

    public void generate(String name, LearnOptions lopts)
    {
        opts.alignmentModel = lopts.alignmentModel; // HACK
//        saveParams("stage1");
//        System.exit(1);
        if(!opts.fullPredRandomBaseline)
        {
            Utils.begin_track("Loading Language Model: " + name);
            if(opts.ngramWrapper == opts.ngramWrapper.kylm)
                ngramModel = new KylmNgramWrapper(opts.ngramModelFile);
            else if(opts.ngramWrapper == opts.ngramWrapper.srilm)
                ngramModel = new SrilmNgramWrapper(opts.ngramModelFile, opts.ngramSize);
            else if(opts.ngramWrapper == opts.ngramWrapper.roark)
                ngramModel = new RoarkNgramWrapper(opts.ngramModelFile);
            LogInfo.end_track();
        }
        FullStatFig complexity = new FullStatFig(); // Complexity inference (number of hypergraph nodes)
        double temperature = lopts.initTemperature;
        testPerformance = newPerformance();       
        try
        {
            testFullPredOut = (opts.outputFullPred) ?
                IOUtils.openOut(Execution.getFile(
                name+".test.full-pred-gen")) : null;
            testPredOut = IOUtils.openOut(Execution.getFile(name+".tst.xml"));
            // write prediction file header, conforming to SGML NIST standard
            testPredOut.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                "<mteval>\n" +
                                "<tstset setid=\"" + name + "\" srclang=\"English\" " +
                                "trglang=\"English\" sysid=\"sample_system\">");
        }
        catch(IOException ioe)
        {
            Utils.begin_track("Error opening file");
            LogInfo.end_track();
        }                
        // E-step
        Utils.begin_track("Generation-step " + name);
        Params counts = newParams();
        Collection<BatchEM> list = new ArrayList(examples.size());
        for(int i = 0; i < examples.size(); i++)
        {
            list.add(new BatchEM(i, examples.get(i), counts, temperature,
                    lopts, 0, complexity));                
        }
        Utils.parallelForeach(opts.numThreads, list);
        LogInfo.end_track();
        list.clear();

        if(testFullPredOut != null) testFullPredOut.close();
        testPredOut.println("</tstset>\n</mteval>");
        // write prediction file footer, conforming to SGML NIST standard
        testPredOut.close();
        Execution.putOutput("currExample", examples.size());

        // Final
        testPerformance.output(Execution.getFile(name+".test.performance"));
        LogInfo.end_track();
        Record.end();
    }

    class InitParams extends MyCallable
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

        public Object call() throws Exception
        {
            if(isLog()) Utils.begin_track("Example %s/%s", Utils.fmt(i), Utils.fmt(examples.size()));
            newInferState(ex, params, counts, new InferSpec(1, false, false,
                    false, true, false, false, 1, 0)).updateCounts();   // don't we need to save somehow??
            if(isLog()) LogInfo.end_track();
            return null;
        }
    }

    class BatchEM extends MyCallable
    {
        private Example ex;
        private int i, iter;
        private Params counts;
        private double temperature;
        private LearnOptions lopts;
        private FullStatFig complexity;

        BatchEM(int i, Example ex, Params counts, double temperature,
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
        public Object call() throws Exception
        {
//            if (isLog()) Utils.begin_track("Example %s/%s: %s", Utils.fmt(i+1),
            if (opts.outputExampleFreq != 0 && i % opts.outputExampleFreq == 0)
                Utils.begin_track("Example %s/%s: %s", Utils.fmt(i+1),
                         Utils.fmt(examples.size()), summary(i));
            if (opts.outputExampleFreq != 0 && i % opts.outputExampleFreq == 0)
                Execution.putOutput("currExample", i);
            processExample(i, ex, 1, counts, temperature, lopts, iter, complexity);
            if (opts.outputExampleFreq != 0 && i % opts.outputExampleFreq == 0)
                LogInfo.end_track();
            return null;
        }
    }
}
