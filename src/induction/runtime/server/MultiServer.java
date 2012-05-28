package induction.runtime.server;

import fig.exec.Execution;
import induction.LearnOptions;
import induction.Options;
import induction.Options.InitType;
import induction.Options.JsonFormat;
import induction.Utils;
import induction.problem.event3.Event3Model;
import induction.problem.event3.generative.GenerativeEvent3Model;
import java.io.IOException;
import java.net.ServerSocket;

/**
 *
 * @author sinantie
 */
public class MultiServer
{
    static LearnOptions lopts;
    String name;
    Event3Model model;    
    JsonFormat format;
    int port;
    
    public MultiServer(JsonFormat format, int port)
    {
        this.format = format;
        this.port = port;
        setUp();
    }
    
    private void setUp() 
    {
         String args = "-modelType generate -testInputLists test/testWeatherGovEvents "
                    + "-inputFileExt events "
                    + "-stagedParamsFile "
                    + "results/output/weatherGov/alignments/"
                    + "model_3_gabor_cond_null_correct/2.exec/stage1.params.obj "
//                    + "pos/model_3_cond_null_POS_CDNumbers/stage1.params.obj.gz "
                    + "-disallowConsecutiveRepeatFields"
                    + " -kBest 20 "
                    + "-ngramModelFile weatherGovLM/gabor-srilm-abs-3-gram.model.arpa "
//                    + "-ngramModelFile weatherGovLM/dev/gabor-srilm-abs-weather-dev-3-gram.model.arpa "
                    + "-ngramWrapper srilm "
//                    + "-allowConsecutiveEvents "
                    + "-reorderType eventType "
                    + "-allowNoneEvent "
//                    + "-conditionNoneEvent "
                    + "-maxPhraseLength 5 "
                    + "-binariseAtWordLevel "
                    + "-ngramSize 3 "
                    + "-numAsSymbol "
                    + "-lengthPredictionModelFile gaborLists/lengthPrediction.values.linear-reg.model "
                    + "-lengthPredictionFeatureType VALUES "
                    + "-lengthPredictionStartIndex 2 " // IMPORTANT!!!
                    + "-numAsSymbol ";
//                    + "-useDependencies "
//                    + "-interpolationFactor 0.5 "
//                    + "-posAtSurfaceLevel "                    
//                    + "-dmvModelParamsFile results/output/weatherGov/dmv/train/"
//                    + "weatherGov_uniformZ_initNoise_POS_100/stage1.dmv.params.obj.gz"; 
//                 + "-oracleReranker";
//                    + "-omitEmptyEvents";
//                    + "-useGoldStandardOnly";
        /*initialisation procedure from Generation class*/
        Options opts = new Options();
        Execution.init(args.split(" "), new Object[] {opts}); // parse input params
        model = new GenerativeEvent3Model(opts);
        model.init(InitType.staged, opts.initRandom, "");   
        model.getWordIndexer().add("(boundary)"); // from readExamples
        opts.outputIterFreq = opts.stage1.numIters;
        lopts = opts.stage1;
        opts.alignmentModel = lopts.alignmentModel;
        name = "stage1";     
        Utils.logs("\nFinished loading. Generator running in server mode using " + opts.numThreads + " threads");
    }

    public static LearnOptions getLopts()
    {
        return lopts;
    }
    
    public void execute()
    {
        ServerSocket serverSocket = null;
        boolean listening = true;        
            
        try {
            serverSocket = new ServerSocket(port);
        }
        catch (IOException e) {
            error("Could not listen on port: " + port);
            
        }
        try {
            while (listening)             
                new MultiServerThread(model, format, serverSocket.accept()).start();            
        }
        catch(IOException ioe) {
            message("Could not establish connection!");
        }
        try {
            serverSocket.close();
        }
        catch(IOException ioe) {
            error("Error closing socket");
        }
    }
    public static void main(String[] args) throws IOException
    {
        // default to wunderground protocol
        JsonFormat format = JsonFormat.wunderground;
        int port = 4444;
        
        if(args.length > 1)
        {            
            String f = args[0].trim();
            if(f.equals("wunderground"))
                format = JsonFormat.wunderground;
            else
                error("Only 'wunderground' format is supported at the moment");
            port = Integer.valueOf(args[1]);            
        }
        System.out.println("Listening on port: "+port);
        MultiServer ms = new MultiServer(format, port);
        ms.execute();
    }
    
    public static void error(String msg)
    {
        System.err.println(msg);
        System.exit(-1);
    }
    
    public static void message(String msg)
    {
        System.out.println(msg);        
    }    
}
