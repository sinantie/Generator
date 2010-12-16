package induction.problem;

import induction.LearnOptions;
import induction.Options;
import java.util.Random;

/**
 *
 * @author konstas
 */
public interface ModelInterface
{
    public void readExamples();
    public void logStats();
    public void genExamples();
    public void preInit();
    public void init(Options.InitType initType, Random initRandom, String name);
    public void learn(String name, LearnOptions lopts); 
    public void generate(String name, LearnOptions lopts);    
}
