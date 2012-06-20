package induction.runtime.server;

import induction.Options.JsonFormat;
import induction.problem.event3.Event3Model;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 *
 * @author sinantie
 */
public class MultiServerThread extends Thread
{

    private Socket socket = null;
    private JsonFormat format;
    private Event3Model model; 
    private String client;
    
    public MultiServerThread(Event3Model model, JsonFormat format, Socket socket)
    {
        super("MultiServerThread");
        this.model = model;
        this.socket = socket;
        this.format = format;
        this.client = socket.getRemoteSocketAddress().toString();
        MultiServer.message("Established connection with client " + client);
    }

    @Override
    public void run()
    {
        try {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                    socket.getInputStream()));

            String inputLine, outputLine;
            Protocol p;
            switch(format)
            {
                case lowjet : 
                    p = new LowJetProtocol(model, client); break;
                case wunderground : default : 
                    p = new WundergroundProtocol(model, client); break;
            }                        
            inputLine = in.readLine(); // read single request
            outputLine = p.processInput(inputLine);
            out.println(outputLine); // return output
            
            out.close();
            in.close();
            socket.close();

        }
        catch (IOException e) {
            MultiServer.error(e.getMessage());
        }
    }
}
