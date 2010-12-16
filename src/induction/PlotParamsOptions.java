package induction;

import java.util.*;
import fig.basic.*;

public class PlotParamsOptions {
  @Option(gloss="Each directory contains a path of EM") public ArrayList<String> dirs = new ArrayList();
  @Option(gloss="Permute the tags") public boolean allowPermutation = true;
  @Option(gloss="Dimensionality to reduce to") public int dim = 2;
  //@Option(gloss="Sample the points on each trail (1 is take every one, 2 is every other, ...)") public int takePointFreq = 1;
  //@Option(gloss="Fraction of points to take") public double fracPointsPerTrail = 1;
  @Option public int numPointsPerTrail = 100;
  @Option(gloss="Output results here") public String outPath;
}
