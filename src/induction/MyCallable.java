package induction;

import java.util.concurrent.Callable;

/**
 *
 * @author sinantie
 */
public abstract class MyCallable implements Callable
{
    protected boolean log;

    public void setLog(boolean log)
    {
        this.log = log;
    }

    public boolean isLog()
    {
        return log;
    }
}
