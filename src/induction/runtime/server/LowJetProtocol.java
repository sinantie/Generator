package induction.runtime.server;

import fig.basic.SysInfoUtils;
import induction.Options.JsonFormat;
import induction.problem.event3.Event3Model;

/**
 * Simple protocol for php demo hook. The data exchange entails receiving
 * a string in json format. The server replies with an HTML formatted output for 
 * showing in the php frontend.
 * @author sinantie
 */
public class LowJetProtocol implements Protocol
{
    Event3Model model;
    String client;
    
    public LowJetProtocol(Event3Model model, String client)
    {
        this.model = model;
        this.client = client;
    }    
    
    @Override
    public String processInput(String input)
    {
        MultiServer.message(SysInfoUtils.getCurrentDateStr() + ": Client " + client + " made a request");
        String output = model.processExamplesJson(JsonFormat.lowjet, input, MultiServer.getLopts());
        return output;
    }
}
