/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package induction.utils.humanevaluation;

/**
 *
 * @author konstas
 */
public class GaborEntry implements CompetitorEntry
{

    private String predText, goldText, predEvents;

    public GaborEntry(String guess, String gold, String guessEvents)
    {
        this.predText = stripLine(guess, "guess").toUpperCase();
        this.goldText = stripLine(gold, "gold").toUpperCase();
        this.predEvents = stripLine(guessEvents, "guess_events");
    }

    private String stripLine(String line, String token)
    {
        return line.replace("<" + token + ">", "").replace("</" + token + ">", "").trim();
    }

    @Override
    public String getPredText()
    {
        return predText;
    }

    @Override
    public String getGoldText()
    {
        return goldText;
    }

    @Override
    public String getPredEvents()
    {
        return predEvents;
    }
}