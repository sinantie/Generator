package induction.utils.humanevaluation;

/**
 *
 * @author sinantie
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

    private String stripLine(String line, String token)
    {
        return line.replace("<" + token + ">", "").replace("</" + token + ">", "").trim();
    }
}
