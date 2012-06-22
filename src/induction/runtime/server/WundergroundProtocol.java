package induction.runtime.server;

import fig.basic.SysInfoUtils;
import induction.Options.JsonFormat;
import induction.problem.event3.Event3Model;

/**
 * Simple protocol for php demo hook. The data exchange entails receiving
 * a string of the form 'system@link', where system corresponds to the Metric System
 * being used (i.e. english or metric) and link is the url snippet required by
 * the model. The server replies with an HTML formatted output for showing in
 * the php frontend.
 * @author sinantie
 */
public class WundergroundProtocol implements Protocol
{
    Event3Model model;
    String client;
    
    public WundergroundProtocol(Event3Model model, String client)
    {
        this.model = model;
        this.client = client;
    }    
    
    @Override
    public String processInput(String input)
    {
        String[] in = input.split("@"); // in[0] = system, in[1] = link     
        String system = in[0];
        String link = in[1];
        MultiServer.message(SysInfoUtils.getCurrentDateStr() + ": Client " + client + " requested " + link + " in the " + system + " system");
        String output = model.processExamplesJson(JsonFormat.wunderground, link, MultiServer.getLopts(), "", system);
        return output;
    }
}
