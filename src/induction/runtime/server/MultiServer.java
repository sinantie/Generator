package induction.runtime.server;

import fig.exec.Execution;
import induction.LearnOptions;
import induction.Options;
import induction.Options.InitType;
import induction.Utils;
import induction.problem.event3.Event3Model;
import induction.problem.event3.generative.GenerativeEvent3Model;
import induction.problem.event3.discriminative.DiscriminativeEvent3Model;
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
    Options opts;
    
    public MultiServer(String[] args)
    {
        setUp(args);
    }
    
    private void setUp(String[] args) 
    {
        /*initialisation procedure from Generation class*/
        opts = new Options();
        Execution.init(args, new Object[] {opts}); // parse input params
        model = new GenerativeEvent3Model(opts);
//        model = new DiscriminativeEvent3Model(opts);        
        model.init(InitType.staged, opts.initRandom, "");   
        model.getWordIndexer().add("(boundary)"); // from readExamples
        opts.outputIterFreq = opts.stage1.numIters;
        lopts = opts.stage1;
        opts.alignmentModel = lopts.alignmentModel;
        name = "stage1";     
        Utils.logs("\nFinished loading. Generator running in server mode using "
                + "%s threads and listening on port %s", opts.numThreads, opts.port);
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
            serverSocket = new ServerSocket(opts.port);
        }
        catch (IOException e) {
            error("Could not listen on port: " + opts.port);
            
        }
        try {
            while (listening)             
                new MultiServerThread(model, opts.jsonFormat, serverSocket.accept()).start();            
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
        MultiServer ms = new MultiServer(args);
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
