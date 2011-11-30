package induction.problem.event3.params;

public class Parameters
{
  // How to generate a word
  public static final int  G_FIELD_NAME = 0, // Talk about the field name (for field temperature.max, use word "high")
                           G_FIELD_VALUE = 1, // Talk about the field value (for field temperature.max, use word "24")
                           G_FIELD_GENERIC = 2, // Back off to generic words - shared globally (hopefully this becomes function words)
                           G = 3;
  public static final String[] generateToString = {"name", "value", "generic"};
  public static final String[] short_gstr = {"n", "v", "g"};

  // Methods of generate a number (for NumField)
  public static final int  M_IDENTITY = 0,
                           M_NOISEUP = 1,
                           M_NOISEDOWN = 2,
                           M_ROUNDUP = 3,
                           M_ROUNDDOWN = 4,
                           M_ROUNDCLOSE = 5,
                           M = 6,
                           ROUND_SPACING = 5;
  public static final String[] numMethodsToString = {"identity", "noiseup", "noisedown",
                                       "roundup", "rounddown", "roundclose"};
//  public static final String[] numMethodsToString = {"identity",
//                                       "roundup", "rounddown", "roundclose"};
  public static final String[] short_mstr = {"1", "*", "*", ">", "<", "~"};
//  public static final String[] short_mstr = {"1", ">", "<", "~"};

  // Numeric noise: model as geometric distribution
  public static final int S_CONTINUE = 0,
                          S_STOP = 1,
                          S = 2;
  public static final String[] noiseToString = {"continue", "stop"};

  public static final int NUMBER_OF_METRICS_GEN = 7,
                          NUMBER_OF_METRICS_SEM_PAR = 12,
                          
                          PRECISION_METRIC = 0,
                          RECALL_METRIC = 1,
                          F_MEASURE_METRIC = 2,
                          BLEU_METRIC = 3,
                          METEOR_METRIC = 4,
                          TER_METRIC = 5,
                          BLEU_METRIC_MODIFIED = 6,
                          
                          EVENT_PRECISION_METRIC = 3,
                          EVENT_RECALL_METRIC = 4,
                          EVENT_F_MEASURE_METRIC = 5,
                          FIELD_PRECISION_METRIC = 6,
                          FIELD_RECALL_METRIC = 7,
                          FIELD_F_MEASURE_METRIC = 8,
                          VALUE_PRECISION_METRIC = 9,
                          VALUE_RECALL_METRIC = 10,
                          VALUE_F_MEASURE_METRIC = 11;

  // Histogram bins over numeric values
  public static final int H_MIN = 0,
                          H_MAX = 1,
                          H_OTHER = 2,
                          H = 3;
  public static final String[] histToString = {"min", "max", "other"};

  // Booleans
  public static final int B_FALSE = 0,
                          B_TRUE = 1,
                          B = 2;

  public static final String[] booleanToString = {"false", "true"};

  public static final int none_e = -1;
  // Sometimes the true event is not in our set, so we automatically get it wrong
  public static final int unreachable_e = -2;
  
  public static boolean isRealEvent(int e)
  {
      return !(e == none_e || e == unreachable_e);
  }
}
