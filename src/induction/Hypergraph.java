package induction;

import induction.problem.event3.generative.generation.GenInferState;
import induction.problem.event3.nodes.StopNode;
import induction.problem.event3.nodes.TrackNode;
import induction.problem.event3.nodes.FieldNode;
import induction.problem.event3.discriminative.DiscriminativeInferState;
import induction.problem.AInferState;
import induction.ngrams.NgramModel;
import induction.problem.event3.generative.generation.GenerationPerformance;
import induction.problem.event3.generative.generation.GenWidget;
import edu.berkeley.nlp.mt.BatchBleuScorer;
import edu.uci.ics.jung.graph.Graph;
import induction.problem.event3.nodes.WordNode;
import induction.Options.ModelType;
import java.util.*;
import fig.basic.*;
import induction.Options.ReorderType;
import induction.problem.event3.Example;
import induction.problem.event3.nodes.EventsNode;
import induction.problem.event3.nodes.FieldsNode;
import java.util.PriorityQueue;
import static fig.basic.LogInfo.*;
import org.apache.commons.collections15.map.LRUMap;

/**
 * Provides a module for doing inference over discrete structures
 * such as sequences and parse trees.  A distribution over possibly
 * exponentially many structures (widgets) is encoded compactly using a
 * directed hypergraph.
 *
 * Each hyperedge corresponds to a discrete decision which describes part of
 * the structure.  At sum nodes, exactly one hyperedge is chosen; at prod
 * nodes, all hyperedges are chosen.  The product of all chosen weights on the
 * hyperedges determines the probability of the widget (after appropriate
 * normalization).
 *
 * Notes:
 *  - We don't check for cycles.  You will get stack overflow if there are cycles.
 *  - The children of a product node or the two children of a hyperedge should be disjoint.
 *    Otherwise, you will get an assertion failed with invalid posterior probability > 1
 *    due to double counting.
 *    NOTE: this is unnecessarily restrictive.  We should get rid of this, but have to be careful.
 *    Updating posteriors with prob > 1 is fine, but when fetching the widget, user must do it right.
 *
 * To construct the graph:
 *   if(!addSumNode(node))
 *     addEdge(node, child1, child2, new HyperedgeInfo<Widget>() {
 *       // Implement getWeight(), setPosterior(), choose() here
 *     });
 *
 * To do inference and get back results:
 *   computePosteriors(): perform inference to compute posterior distribution over hyperpaths
 *   computeELogZEntropy(): compute some statistics about this inference
 *   fetchPosteriors(): call setPosterior on each hyperedge
 *   fetchBestHyperpath(widget): call choose on each hyperedge in the best one
 *   fetchSampleHyperpath(widget): call choose on each hyperedge in the best one
 *   fetchPosteriorHyperpath(widget): call choose on each hyperedge with a weight (TODO: combine it with fetchPosteriors)
 */
public class Hypergraph<Widget> {
  public enum NodeType { prod, sum }; // Each node represents either a product or a sum over its children

  public interface AHyperedgeInfo<Widget> {
    public void setPosterior(double prob);
    public Widget choose(Widget widget); // Return the updated widget
    //public Widget choose(Widget widget, double v); // Return the updated widget
  }
  public interface HyperedgeInfo<Widget> extends AHyperedgeInfo<Widget> {
    public double getWeight();
  }
  public interface HyperedgeInfoLM<Widget> extends HyperedgeInfo<Widget> {
    public induction.problem.Pair getWeightAtRank(int rank);
    public Widget chooseWord(Widget widget, int word);
  }
  public interface HyperedgeInfoDepLM<Widget> extends HyperedgeInfoLM<Widget> {
    public induction.problem.Pair getDepWeight(int word);    
  }  
  /**
   * Interface that supports calculation of edge weights during viterbi search
   * @param <Widget> 
   */  
  public interface HyperedgeInfoOnline<Widget> extends HyperedgeInfo<Widget> {
    public double getOnlineWeight(List<List<Integer>> ngrams);
  }
  public interface HyperedgeInfoOnlineFields<Widget> extends HyperedgeInfoOnline<Widget> {
    public double getOnlineWeightFields(List<List<Integer>> fieldNgrams, int numOfFields);
  }
  public interface LogHyperedgeInfo<Widget> extends AHyperedgeInfo<Widget> {
    public double getLogWeight();
  }

  private class NullHyperedgeInfo<Widget> implements HyperedgeInfo<Widget> {
    public double getWeight() { 
        return 1; }
    public void setPosterior(double prob) { }
    public Widget choose(Widget widget) { return widget; }
    //public Widget choose(Widget widget, double v) { return widget; }
  }
  private final NullHyperedgeInfo<Widget> nullHyperedgeInfo = new NullHyperedgeInfo<Widget>();

  private class NodeInfo {
    NodeInfo(Object node, NodeType nodeType) { this.node = node; this.nodeType = nodeType; }
    final Object node; // Just for visualizing/debugging
    NodeType nodeType; // Can be changed
    int bestEdge;
    ArrayList<Derivation> derivations;
    final List<Hyperedge> edges = new ArrayList(); // Children
    BigDouble insideScore, outsideScore, maxScore; // Things we compute during inference
    double logMaxScore = Double.NEGATIVE_INFINITY; // For discriminative training we store log scores
        @Override
    public String toString() { return node.toString(); }
  }
  private class Hyperedge {
    final ArrayList<NodeInfo> dest = new ArrayList(2); // Child nodes
//    final NodeInfo dest1, dest2; // Child nodes
    final AHyperedgeInfo info; // Specifies I/O for the hyperedge
    final BigDouble weight;
    final double logWeight; // For discriminative training we store log scores
    
    Hyperedge(NodeInfo dest1, NodeInfo dest2, AHyperedgeInfo info) {
      this.dest.add(dest1);
      this.dest.add(dest2);
//      this.dest1 = dest1;
//      this.dest2 = dest2;
      this.info = info;
      if(info instanceof HyperedgeInfo)
      {
          // in the discriminative model, we check the same hypergraph twice:
          // - Once using the Perceptron model, thus we need to tackle log scores.
          // - Then, using the gold standard text, where we just use the baseline
          // model's parameters, which are probabilities, hence no log manipulation is necessary.
          // So, in the latter case along with all other instances stick to using
          // probabilities only.
          if(inferState instanceof DiscriminativeInferState && 
                  !((DiscriminativeInferState)inferState).isCalculateOracle())
          {
              this.weight = null;
              logWeight = ((HyperedgeInfo)info).getWeight();
          }
          else
          {
              this.weight = BigDouble.fromDouble(((HyperedgeInfo)info).getWeight());
              logWeight = 0;
          }
      }
      else if(info instanceof LogHyperedgeInfo)
      {   // convert from log to probability
          this.weight = BigDouble.fromLogDouble(((LogHyperedgeInfo)info).getLogWeight());
          logWeight = 0;
      }
      else
        throw new RuntimeException("Unknown type of info");
      // Avoid zeros so everything has some positive probability      
      if(!(inferState instanceof DiscriminativeInferState) && weight.isZero()) 
          weight.setToVerySmall(); 
    }
    Hyperedge(ArrayList<NodeInfo> dest, AHyperedgeInfo info)
    {
        this.dest.addAll(dest);
        this.info = info;
        if(info instanceof HyperedgeInfo)
        {
          // in the discriminative model, we check the same hypergraph twice:
          // - Once using the Perceptron model, thus we need to tackle log scores.
          // - Then, using the gold standard text, where we just use the baseline
          // model's parameters, which are probabilities, hence no log manipulation is necessary.
          // So, in the latter case along with all other instances stick to using
          // probabilities only.
          if(inferState instanceof DiscriminativeInferState && !((DiscriminativeInferState)inferState).isCalculateOracle())
          {
              this.weight = null;
              logWeight = ((HyperedgeInfo)info).getWeight();
          }
          else
          {
              this.weight = BigDouble.fromDouble(((HyperedgeInfo)info).getWeight());
              logWeight = 0;
          }
      }
      else if(info instanceof LogHyperedgeInfo)
      {   // convert from log to probability
          this.weight = BigDouble.fromLogDouble(((LogHyperedgeInfo)info).getLogWeight());
          logWeight = 0;
      }
      else
        throw new RuntimeException("Unknown type of info");
      // Avoid zeros so everything has some positive probability      
      if(!(inferState instanceof DiscriminativeInferState) && weight.isZero()) 
          weight.setToVerySmall();
    }
    @Override
    public String toString()
    {        
        return dest + "(" + weight +")";
    }
  }
    
    private class Derivation implements Comparable
    {
        final Hyperedge edge;
        ArrayList<Derivation> derArray;
        ArrayList<Integer> words, fields;
        BigDouble weight;
        double logWeight;
        int[] mask;
        DepHead head;
        Collection<Integer> eventTypeSet, fieldSet;

        public Derivation(Hyperedge edge, int[] mask, Collection<Integer> eventTypeSet,
                Collection<Integer> fieldSet)
        {
            words = new ArrayList<Integer>(2*M - 1);
            if(enableFieldFeatures && 
              (edge.dest.get(0).node instanceof FieldNode || 
               edge.dest.get(0).node instanceof FieldsNode || 
               edge.dest.get(0).node instanceof TrackNode))
            {
                fields = new ArrayList<Integer>(2*M - 1);
            }
            this.edge = edge;
            derArray = new ArrayList<Derivation>(edge.dest.size());            
            this.mask = mask;
            this.eventTypeSet = eventTypeSet;
            this.fieldSet = fieldSet;
            if(discriminative)
                getSuccDiscriminative(mask);
            else
                getSucc(mask);
        }       
        
        private void getSucc(int[] kBestMask)
        {
            /* leaf derivation - axiom. Get the best-first value from the
               underlying multinomial distribution*/
            if(edge.dest.get(0) == endNodeInfo && edge.dest.get(1) == endNodeInfo)
            {
                derArray = null;
                induction.problem.Pair p = ((HyperedgeInfoLM)edge.info).getWeightAtRank(kBestMask[0]);
                this.weight = BigDouble.fromDouble(p.value);
                if(p.label != null)
                {
                    this.words.add(new Integer((Integer)p.label));
                    if(useDependencies && edge.info instanceof HyperedgeInfoDepLM)
                    {
                        induction.problem.Pair<DepHead> headPair = 
                                ((HyperedgeInfoDepLM)edge.info).getDepWeight(new Integer((Integer)p.label));
                        head = headPair.label;
//                        this.weight.mult(head.getWeight());
                    }
                }
            } // if
            else
            {
                Derivation d;
                ArrayList<Integer> input = new ArrayList();
                BigDouble[] weightArray = new BigDouble[!useDependencies ? kBestMask.length + 2 : 
                                                        kBestMask.length + 3];
                if(useDependencies) weightArray[weightArray.length - 1] = BigDouble.one();
                if(modelType == ModelType.semParse && edge.info instanceof HyperedgeInfoLM)
                {
                    input.add(new Integer((Integer)((HyperedgeInfoLM)edge.info).getWeightAtRank(0).label));
                }            
                for(int i = 0; i < kBestMask.length; i++)
                {
                    if( edge.dest.get(i).derivations.isEmpty())
                        System.out.println("Empty edge: " + edge);
                            
                    d = edge.dest.get(i).derivations.get(kBestMask[i]);
                    derArray.add(d);
                    weightArray[i] = d.weight;
                    input.addAll(d.words);
                }
                weightArray[!useDependencies ? weightArray.length - 2 : 
                        weightArray.length - 3] = edge.weight;     // edge weight            
                weightArray[!useDependencies ? weightArray.length - 1 : 
                        weightArray.length - 2] = BigDouble.one(); // LM weight (see 'sf' example, Table 1, Chiang 2007)
                // compute P_LM and +LM item (store in words list) (see Chiang, 2007)
                if(input.size() >= M)
                {
                    
                    for(int i = M - 1; i < input.size(); i++) // function p in Chiang 2007
                    {
                        if(!input.subList(i - M + 1, i + 1).contains(ELIDED_SYMBOL))
                        {
//                            weightArray[!useDependencies ? weightArray.length - 1 : 
//                                        weightArray.length - 2].mult(
//                                    getLMProb(input.subList(i - M + 1, i + 1))); // subList returns [i, j), j exclusive
                            weightArray[!useDependencies ? weightArray.length - 1 : 
                                        weightArray.length - 2].mult(
                                    getLMProb(input.subList(i - M + 1, i + 1))
                                    
                                    ); // subList returns [i, j), j exclusive
                        }
                    } // for
                    for(int i = 0; i < M - 1; i++) // function q in Chiang 2007
                    {
                        words.add(input.get(i));
                    }
                    words.add(ELIDED_SYMBOL);
                    for(int i = input.size() - M + 1; i < input.size(); i++)
                    {
                        words.add(input.get(i));
                    }
                } // if
                else
                {                    
                    words = input; // 2nd branch of function q in Chiang 2007
                } // LM
                if(useDependencies && kBestMask.length < 3) // DMV only handles binary and unary rules
                {
                    if(kBestMask.length < 2) // unary rule, just carry the head as is
                        head = derArray.get(0).head;
                    else
                    {                        
                        DepHead leftHead = derArray.get(0).head;
                        DepHead rightHead = derArray.get(1).head;
                        if(leftHead == null) // special cases, of nodes that don't emit words (e.g. StopNode)
                            head = rightHead;
                        else if(rightHead == null)
                            head = leftHead;
                        else
                        {
                            // choose whether to attach the right head as argument to the left head
                            // or vice versa
                            BigDouble weights[] = {
                            ((GenInferState)inferState).getDepDerivationWeight(
                                    leftHead, rightHead, induction.problem.dmv.Constants.D_RIGHT), 
                            ((GenInferState)inferState).getDepDerivationWeight(
                                    rightHead, leftHead, induction.problem.dmv.Constants.D_LEFT)};
                            int argmax = BigDouble.argmax(weights);
                            // in case both derivations are equiprobable choose to attach right-first
                            if(argmax == -1) argmax = 0;
                            head = argmax == 0 ?
                                new DepHead(leftHead.getHead(), leftHead.getPos(), weights[0]) :
                                new DepHead(rightHead.getHead(), rightHead.getPos(), weights[1]);
//                            weightArray[weightArray.length - 1] = weights[argmax];
//                            if(calculateDependencies)
                            {
                                if(weightArray[weightArray.length - 2].getE() > -1)
                                {
//                                    System.out.println(weightArray[weightArray.length - 2] + " " + weights[argmax]);
                                    weightArray[weightArray.length - 2].mult(interpolationFactor);
                                    weights[argmax].mult(1.0 - interpolationFactor);
                                    weightArray[weightArray.length - 2].incr(weights[argmax]);
                                }
                                
                            }                            
                        }                        
                    }                    
                } // deps
                this.weight = BigDouble.one();
                this.weight.mult_multN(weightArray);
            } // else
        }

        private void getSuccDiscriminative(int[] kBestMask)
        {
            if(enableFieldFeatures && edge.dest.get(0).node instanceof FieldNode)
            {
                this.fields.add(new Integer(((FieldNode)edge.dest.get(0).node).getField()));
            }
            /* leaf derivation - axiom. Get the best-first value from the
               underlying multinomial distribution*/
            if(edge.dest.get(0) == endNodeInfo && edge.dest.get(1) == endNodeInfo)
            {
                derArray = null;
                induction.problem.Pair p = ((HyperedgeInfoLM)edge.info).getWeightAtRank(kBestMask[0]);
                this.logWeight = p.value;
                if(p.label != null)
                {
                    this.words.add(new Integer((Integer)p.label));
                    if(useDependencies && edge.info instanceof HyperedgeInfoDepLM)
                    {
                        induction.problem.Pair<DepHead> headPair = 
                                ((HyperedgeInfoDepLM)edge.info).getDepWeight(new Integer((Integer)p.label));
                        head = headPair.label;
                    }
                }
            }//            
            else
            {
                Derivation d;
                ArrayList<Integer> wordInput = new ArrayList();
                ArrayList<Integer> fieldInput = new ArrayList();
                if(modelType == ModelType.semParse && edge.info instanceof HyperedgeInfoLM)
                {
                    wordInput.add(new Integer((Integer)((HyperedgeInfoLM)edge.info).getWeightAtRank(0).label));
                }
                for(int i = 0; i < kBestMask.length; i++)
                {
                    d = edge.dest.get(i).derivations.get(kBestMask[i]);
                    derArray.add(d);
                    this.logWeight += d.logWeight; // add weight of children
                    wordInput.addAll(d.words);
                    if(enableFieldFeatures && d.fields != null)
                        fieldInput.addAll(d.fields);
                }
                this.logWeight += edge.logWeight;  // add local features
                // local + non-local features log weight (see 'sf' example, Table 1, Chiang 2007 
                // and EVAL(e,J) function in pseudocode 3, Huang 2008)
                // compute EVAL(e,J) and +LM item (store in words list) (see Chiang, 2007)
                List<List<Integer>> wordNgrams = new ArrayList<List<Integer>>();
                if(wordInput.size() >= M)
                {
                    // add non-local features                    
                    for(int i = M - 1; i < wordInput.size(); i++) // function p in Chiang 2007
                    {
                        if(!wordInput.subList(i - M + 1, i + 1).contains(ELIDED_SYMBOL))
                        {
//                          // subList returns [i, j), j exclusive
                            wordNgrams.add(wordInput.subList(i - M + 1, i + 1));  
//                            this.logWeight += Math.log(getLMProb(input.subList(i - M + 1, i + 1)));
                        }
                    } // for                    
                    this.logWeight += ((HyperedgeInfoOnline)edge.info).getOnlineWeight(wordNgrams);
                    for(int i = 0; i < M - 1; i++) // function q in Chiang 2007
                    {
                        words.add(wordInput.get(i));
                    }
                    words.add(ELIDED_SYMBOL);
                    for(int i = wordInput.size() - M + 1; i < wordInput.size(); i++)
                    {
                        words.add(wordInput.get(i));
                    }
                } // if
                else
                {
                    // add non-local features for input < M
                    wordNgrams.add(wordInput);
                    if(edge.info instanceof HyperedgeInfoOnline)
                        this.logWeight += ((HyperedgeInfoOnline)edge.info).getOnlineWeight(wordNgrams);                    
                    words = wordInput; // 2nd branch of function q in Chiang 2007
                } // LM
                if(enableFieldFeatures && fields != null)
                {
                    List<List<Integer>> fieldNgrams = new ArrayList<List<Integer>>();                                    
                    if(fieldInput.size() >= M)
                    {
                        // add non-local features                    
                        for(int i = M - 1; i < fieldInput.size(); i++) // function p in Chiang 2007
                        {
                            if(!fieldInput.subList(i - M + 1, i + 1).contains(ELIDED_SYMBOL))
                            {
    //                          // subList returns [i, j), j exclusive
                                fieldNgrams.add(fieldInput.subList(i - M + 1, i + 1));  
                            }
                        } // for                    
                        if(edge.info instanceof HyperedgeInfoOnlineFields)
                            this.logWeight += ((HyperedgeInfoOnlineFields)edge.info).getOnlineWeightFields(fieldNgrams, getNumOfFieldsInSubGeneration());
                        for(int i = 0; i < M - 1; i++) // function q in Chiang 2007
                        {
                            fields.add(fieldInput.get(i));
                        }
                        fields.add(ELIDED_SYMBOL);
                        for(int i = fieldInput.size() - M + 1; i < fieldInput.size(); i++)
                        {
                            fields.add(fieldInput.get(i));
                        }
                    } // if
                    else
                    {
                        // add non-local features for input < M
                        fieldNgrams.add(fieldInput);
                        if(edge.info instanceof HyperedgeInfoOnlineFields)
                            this.logWeight += ((HyperedgeInfoOnlineFields)edge.info).getOnlineWeightFields(fieldNgrams, getNumOfFieldsInSubGeneration());
                        if(!(edge.dest.get(0).node instanceof FieldNode))
                            fields = fieldInput; // 2nd branch of function q in Chiang 2007
                    } 
                } // fields          
                if(useDependencies && kBestMask.length < 3) // DMV only handles binary and unary rules
                {
                    if(kBestMask.length < 2) // unary rule, just carry the head as is
                        head = derArray.get(0).head;
                    else
                    {                        
                        DepHead leftHead = derArray.get(0).head;
                        DepHead rightHead = derArray.get(1).head;
                        if(leftHead == null) // special cases, of nodes that don't emit words (e.g. StopNode)
                            head = rightHead;
                        else if(rightHead == null)
                            head = leftHead;
                        else
                        {
                            // choose whether to attach the right head as argument to the left head
                            // or vice versa
                            double weights[] = {
                            ((DiscriminativeInferState)inferState).getDepDerivationWeight(
                                    leftHead, rightHead, induction.problem.dmv.Constants.D_RIGHT), 
                            ((DiscriminativeInferState)inferState).getDepDerivationWeight(
                                    rightHead, leftHead, induction.problem.dmv.Constants.D_LEFT)};
                            int argmax = Utils.argmax(weights);
                            // in case both derivations are equiprobable choose to attach right-first
                            if(argmax == -1) argmax = 0;
                            head = argmax == 0 ?
                                new DepHead(leftHead.getHead(), leftHead.getPos(), weights[0]) :
                                new DepHead(rightHead.getHead(), rightHead.getPos(), weights[1]);
                            if(calculateDependencies) // apply DMV locally on field level
                            {
                                this.logWeight += weights[argmax];
                            }                            
                        }                        
                    }                    
                } // deps
            } // else
        }
        
        /**
         * Compare two derivations. Note, if the current derivation has a larger weight 
         * than the other, return -1. I.e. we sort in descending order.
         * @param o
         * @return 
         */
        @Override
        public int compareTo(Object o)
        {
            Derivation d = (Derivation)o;            
            return -(discriminative ? Double.compare(this.logWeight, d.logWeight): 
                                           this.weight.compareTo(d.weight));
        }
      
        /**
         * Checks whether two derivations are equivalent, i.e. they have the
         * same result (sub-generation part) but are produced by (potentially) 
         * different combinations
         * @param d
         * @return if the derivations are equivalent
         */
        public boolean isEquivalentTo(Derivation d)
        {
            return this.getSubGeneration().equals(d.getSubGeneration());
        }

        private String getSubGeneration()
        {
            StringBuilder out = new StringBuilder();
            if(derArray == null) // choose terminal nodes
            {
                if(!words.isEmpty())
                {
                    out.append(words.get(0)).append(" "); 
                    if(useDependencies && head != null)
                        out.append("[").append(head.getHead()).append("]").append(" ");
                    return out.toString();                     
                }
            }
            for(Derivation d : derArray)
            {
              out.append(d.getSubGeneration());
            }
            return out.toString();
        }

        private int getNumOfFieldsInSubGeneration()
        {
            int out = 0;
            if(edge.dest.get(0).node instanceof FieldNode && !fields.isEmpty())            
            {                
                return ++out;                
            }
            else if(edge.dest.get(0).node instanceof StopNode)
                return 0;
            for(Derivation d : derArray)
            {
                out += d.getNumOfFieldsInSubGeneration();
            }
            return out;
        }
        
        private double getLMProb(List<Integer> ngram)
        {
            String[] posNgramStr = secondaryNgramModel != null ? new String[ngram.size()] : null;
            Double prob = cache.get(ngram); // check if it is in the cache first
            if(prob == null)
            {
//            if(modelType == ModelType.semParse)
//                return 1.0; // we currently don't support LM for semantic parsing
                String[] ngramStr = new String[ngram.size()];
                
                String temp = "";
                for(int i = 0; i < ngram.size(); i++)
                {
                    String token = vocabulary.getObject(ngram.get(i));
                    temp = Utils.stripTag(token, tagDelimiter);
                    // ngram inferState needs to convert numbers to symbol <num>
                    // syntax parser can process numbers
                    ngramStr[i] = numbersAsSymbol ? Utils.replaceNumber(temp, posAtSurfaceLevel, tagDelimiter) : temp;
                    if(secondaryNgramModel != null)
                        posNgramStr[i] = Utils.stripWord(token, false, tagDelimiter);
                }
                prob = ngramModel.getProb(ngramStr);
                cache.put(ngram, prob); // update cache
            }
            return secondaryNgramModel == null ? prob : 
                    prob * interpolationFactor * secondaryNgramModel.getProb(posNgramStr) * (1-interpolationFactor);
        }

        @Override
        public String toString()
        {
            StringBuilder out = new StringBuilder();
            for(int i = 0; i < words.size(); i++)
            {
                out.append(vocabulary.getObject(words.get(i))).append(" ");
            }
            out.append("(").append((discriminative ? logWeight : weight)).append(")");
            if(eventTypeSet == null)
                return out.toString();
            out.append(" [");
            for(Integer e : eventTypeSet)
                out.append(e).append(",");
            return eventTypeSet.size() > 0 ? out.substring(0, out.length() - 1) + "]" : out.append("]").toString();
        }
    }   
    
  // Specifies the hypergraph and stores the computations
  public boolean debug = false;
  public boolean allowEmptyNodes = false; // Do we allow nodes with no children?
  private HashMap<Object,NodeInfo> nodes = new HashMap();
  private ArrayList<NodeInfo> topologicalOrdering;

  // kBest stuff
  public  int K, M, NUM, ELIDED_SYMBOL;
  public Options.ReorderType reorderType;
  private enum Reorder {eventType, event, field, ignore};
  public NgramModel ngramModel, secondaryNgramModel;
  public Indexer<String> vocabulary;
  public boolean numbersAsSymbol = true, allowConsecutiveEvents, oracleReranker, 
                 enableFieldFeatures, posAtSurfaceLevel;
  private String tagDelimiter;
  private static final int UNKNOWN_EVENT = Integer.MAX_VALUE, IGNORE_REORDERING = -1;
  public Example ex;
  private Options.ModelType modelType;
  public Graph graph;
  // discriminative stuff
  private AInferState inferState;
  private boolean discriminative;
  // dependencies stuff
  private boolean useDependencies, calculateDependencies;
  private double interpolationFactor;
  // Start and end nodes
  private final Object startNode = addNodeAndReturnIt("START", NodeType.sum); // use sum or prod versions
  public final Object endNode = addNodeAndReturnIt("END", NodeType.sum);
  public final Object invalidNode = "INVALID";
  private final NodeInfo startNodeInfo = getNodeInfoOrFail(startNode);
  private final NodeInfo endNodeInfo = getNodeInfoOrFail(endNode);
  private Hyperedge terminalEdge = new Hyperedge(endNodeInfo, endNodeInfo, nullHyperedgeInfo);
  private Map<List<Integer>, Double> cache;
  
  public  void setup(AInferState inferState, boolean debug, ModelType modelType, boolean allowEmptyNodes,
                                        int K, NgramModel ngramModel, 
                                        NgramModel secondaryNgramModel,
                                        int M, Options.ReorderType reorderType,
                                        boolean allowConsecutiveEvents,
                                        boolean oracleReranker, boolean useDependencies, 
                                        double interpolationFactor, boolean posAtSurfaceLevel, 
                                        String tagDelimiter, int NUM,
                                        int ELIDED_SYMBOL, boolean numbersAsSymbol,
                                        Indexer<String> wordIndexer, Example ex, Graph graph)
  {
        this.inferState = inferState;
        discriminative = this.inferState instanceof DiscriminativeInferState;
        this.debug = debug;
        // Need this because the pc sets might be inconsistent with the types
        this.allowEmptyNodes = allowEmptyNodes;
        this.modelType = modelType;
        this.K = K;
        this.M = M;
        this.ngramModel = ngramModel;
        this.secondaryNgramModel = secondaryNgramModel;
        this.reorderType = reorderType;
        this.allowConsecutiveEvents = allowConsecutiveEvents;
        this.oracleReranker = oracleReranker;
        this.useDependencies = useDependencies;
        this.interpolationFactor = interpolationFactor;
        this.posAtSurfaceLevel = posAtSurfaceLevel;
        this.tagDelimiter = tagDelimiter;
        /*add NUM category and ELIDED_SYMBOL to word vocabulary. Useful for the LM calculations*/
        this.NUM = NUM;
        this.ELIDED_SYMBOL = ELIDED_SYMBOL;
        this.numbersAsSymbol = numbersAsSymbol;
        this.vocabulary = wordIndexer;
        this.ex = ex;
        this.graph = graph;
        if(graph != null)
            graph.addVertex(startNode);
        cache = Collections.synchronizedMap(new LRUMap<List<Integer>, Double>(1000));
  }

    public void setNumbersAsSymbol(boolean numbersAsSymbol)
    {
        this.numbersAsSymbol = numbersAsSymbol;
    }

    public void setInferState(AInferState inferState)
    {
        this.inferState = inferState;
    }

    public void setEnableFieldFeatures(boolean enableFieldFeatures)
    {
        this.enableFieldFeatures = enableFieldFeatures;
    }
    public  void setupForSemParse(boolean debug, ModelType modelType, boolean allowEmptyNodes,
                                        int K, Options.ReorderType reorderType,
                                        boolean allowConsecutiveEvents, 
                                        boolean useDependencies, double interpolationFactor, 
                                        boolean posAtSurfaceLevel, String tagDelimiter, int NUM,
                                        int ELIDED_SYMBOL, boolean numbersAsSymbol,
                                        Indexer<String> wordIndexer, Example ex, Graph graph)
    {
        setup(null, debug, modelType, allowEmptyNodes, K, null, null, 2, reorderType, 
                         allowConsecutiveEvents, false, useDependencies, interpolationFactor, 
                         posAtSurfaceLevel, tagDelimiter, NUM, ELIDED_SYMBOL, numbersAsSymbol, wordIndexer, ex, graph);
    }

  // Things we're going to compute
  private double logZ = Double.NaN; // Normalization constant
  private double elogZ = Double.NaN; // E_q(z|x) log weight(x,z)
  private double entropy = Double.NaN; // Entropy of the posterior q(z|x)

  public double getLogZ() { return logZ; }
  public double getELogZ() { return elogZ; }
  public double getEntropy() { return entropy; }

  // Add nodes: return whether added something
  public boolean addSumNode(Object node) { return addNode(node, NodeType.sum); }  
  public boolean addProdNode(Object node) { return addNode(node, NodeType.prod); }
  public boolean removeNode(Object node) {
    return nodes.remove(node) == null ? false : true;
  }
  
  public Object sumStartNode() { getNodeInfoOrFail(startNode).nodeType = NodeType.sum; return startNode; }
  public Object prodStartNode() { getNodeInfoOrFail(startNode).nodeType = NodeType.prod; return startNode; }

  public int numEdges(Object node) { return getNodeInfoOrFail(node).edges.size(); }
  public int numNodes() { return nodes.size(); }

  public void assertNonEmpty(Object node) {
    assert numEdges(node) > 0 : node + " has no children hyperedges (it's empty)";
  }

  // Use: after add edges to a node, return the compactified version of the
  // node if there is a possibility that the node might have no children.
  // Motivation: sometimes it's hard to check whether a node is going to have
  // children.  We just need to construct as we go.  Later if we've discovered
  // it doesn't have children, then prune it from the hypergraph.
  // DON'T USE THIS: if we return a compactified version, we're going to keep on creating the same node over and over again
  public Object compactify(Object node) {
    // If this node has no children, then remove it and use invalidNode
    if(numEdges(node) == 0) {
      nodes.remove(node);
      return invalidNode;
    }
    return node;
  }

  // Add edges
  public void addEdge(Object source) { addEdge(source, endNode, endNode, nullHyperedgeInfo);}
  public void addEdge(Object source, AHyperedgeInfo<Widget> info) { addEdge(source, endNode, endNode, info); }
  public void addEdge(Object source, Object dest1) { addEdge(source, dest1, endNode, nullHyperedgeInfo); }
  public void addEdge(Object source, Object dest1, AHyperedgeInfo<Widget> info) { addEdge(source, dest1, endNode, info); }
  public void addEdge(Object source, Object dest1, Object dest2) { addEdge(source, dest1, dest2, nullHyperedgeInfo); }
  public void addEdge(Object source, Object dest1, Object dest2, AHyperedgeInfo<Widget> info) {
    assert source != invalidNode;
    if(debug) dbgs("add %s -> %s %s", source, dest1, dest2);
    if(dest1 == invalidNode || dest2 == invalidNode) return;
    assert source != dest1 && source != dest2; // Catch obvious loops
    getNodeInfoOrFail(source).edges.add(new Hyperedge(getNodeInfoOrFail(dest1),
            getNodeInfoOrFail(dest2), info));

    if(graph != null)
    {
        Double weight = new Double(((HyperedgeInfo)info).getWeight());
        if(!graph.containsVertex(source))
            graph.addVertex(source);
        if(!graph.containsVertex(dest1))
            graph.addVertex(dest1);
//        if(dest1 != endNode)
            graph.addEdge(new DummyEdge(weight), source, dest1);

        if(!graph.containsVertex(dest2))
            graph.addVertex(dest2);
        if(dest2 != endNode)
            graph.addEdge(new DummyEdge(weight), source, dest2);
    }

  }
  public void addEdge(Object source, ArrayList dest) {
    addEdge(source, dest, nullHyperedgeInfo);  
  }          
  public void addEdge(Object source, ArrayList dest, AHyperedgeInfo<Widget> info) {
    assert source != invalidNode;
    if(debug)
    {
        String s = "add %s -> ";
        for(int i = 0; i < dest.size(); i++)
        {
            s += "%s ";
        }
        dbgs(s, source, dest.toArray());
    }
    for(Object o : dest)
    {
        if(o == invalidNode) return;
    }
    ArrayList<NodeInfo> list = new ArrayList(dest.size());
    for(Object o : dest)
    {
        assert source != o; // Catch obvious loops
        list.add(getNodeInfoOrFail(o));
    }
    getNodeInfoOrFail(source).edges.add(new Hyperedge(list, info));

    if(graph != null)
//    if(false)
    {
        Double weight = new Double(((HyperedgeInfo)info).getWeight());
        Object dest1 = dest.get(0);
        if(!graph.containsVertex(source))
            graph.addVertex(source);
        if(!graph.containsVertex(dest1))
            graph.addVertex(dest1);
        graph.addEdge(new DummyEdge(weight), source, dest1);
        if(dest.size() > 1)
        {
            Object dest2 = dest.get(1);
            if(!graph.containsVertex(dest2))
                graph.addVertex(dest2);
            graph.addEdge(new DummyEdge(weight), source, dest2);
        }        
    }
  }
  class DummyEdge{
      double weight;
      public DummyEdge(double weight){this.weight = weight;}

        @Override
        public String toString()
        {
//            return String.valueOf(weight);
            return "";
        }

  }
  // Helpers
  private boolean addNode(Object node, NodeType nodeType) { // Return whether a new node was added
    NodeInfo info = nodes.get(node);
    if(info != null) return false;
    nodes.put(node, new NodeInfo(node, nodeType));
    return true;
  }
  private Object addNodeAndReturnIt(Object node, NodeType nodeType) { // Return the node that we added
    if(!addNode(node, nodeType)) throw Exceptions.bad("Can't add node");
    return node;
  }
  private NodeInfo getNodeInfoOrFail(Object node) {
    NodeInfo info = nodes.get(node);
    assert info != null : "Node doesn't exist in hypergraph (need to add nodes before edges containing them): "+node;
    return info;
  }

  private void checkGraph() {
    // Make sure that all nodes have children (except end of course)
    if(!allowEmptyNodes) {
      int numBadNodes = 0;
      for(NodeInfo nodeInfo : nodes.values()) {
        if(nodeInfo.edges.isEmpty() && nodeInfo.node != endNode) {
          errors("Node has no children: "+nodeInfo.node);
          numBadNodes++;
        }
      }
      if(numBadNodes > 0) throw Exceptions.bad(numBadNodes + " bad nodes");
    }

    // FUTURE: check for cycles to be more graceful
    // Now, we just wait for computeTopologicalOrdering() to stack overflow
  }

  private void computeTopologicalOrdering() {
    if(topologicalOrdering != null) return;
    checkGraph();
    topologicalOrdering = new ArrayList<NodeInfo>(nodes.size());
    for(int i = 0; i < nodes.size(); i++) { topologicalOrdering.add(null);}
    HashSet<NodeInfo> hit = new HashSet();
    IntRef i = new IntRef(nodes.size()-1);
    computeReverseTopologicalOrdering(hit, startNodeInfo, i);
    if(i.value != -1)
      throw Exceptions.bad("Not all nodes reachable from startNode");
    assert topologicalOrdering.get(0) == startNodeInfo;
    if(!allowEmptyNodes)
      assert topologicalOrdering.get(topologicalOrdering.size()-1) == endNodeInfo;
  }
  private void computeReverseTopologicalOrdering(HashSet<NodeInfo> hit, NodeInfo nodeInfo, IntRef i) {
    if(hit.contains(nodeInfo)) return;
    for(Hyperedge edge : nodeInfo.edges) {
        for(int d = 0; d < edge.dest.size(); d++)
        {
            computeReverseTopologicalOrdering(hit, edge.dest.get(d), i);
        }
//      computeReverseTopologicalOrdering(hit, edge.dest.get(0), i);
//      computeReverseTopologicalOrdering(hit, edge.dest.get(1), i);        
    }
    topologicalOrdering.set(i.value--, nodeInfo);
    hit.add(nodeInfo);
  }

  //////////////////////////////////////////////////////////// 

  public Hypergraph() { }

  public void computePosteriors(boolean viterbi) {
    computeTopologicalOrdering();
    computeInsideMaxScores(viterbi);
    if(!viterbi) computeOutsideScores();
    if(viterbi) this.logZ = startNodeInfo.maxScore.toLogDouble();
    else        this.logZ = startNodeInfo.insideScore.toLogDouble();
  }

  public HyperpathResult<Widget> oneBestViterbi(Widget widget, Random random)
  {
        computeTopologicalOrdering();
        computeInsideMaxScores(false);
        HyperpathChooser chooser = new HyperpathChooser();
        chooser.viterbi = false; // choose nodes randomly
        chooser.widget = widget;
        chooser.choose = true;
        chooser.random = random;
        chooser.recurse(startNodeInfo);
        return new HyperpathResult(chooser.widget, chooser.logWeight);
  }

    public HyperpathResult<Widget> kBestViterbi(Widget widget)
    {
        computeTopologicalOrdering();
        // 1. Run kBest in reverse topological order on all Nodes BUT those
        // related with the events HSMM
        NodeInfo v;
        for(int i = topologicalOrdering.size()-1; i >= 0; i--)
        {
            v = topologicalOrdering.get(i);
            if(v != endNodeInfo)
            {
                if(useDependencies)
                    calculateDependencies = v.node instanceof EventsNode || v.node instanceof TrackNode;
                if(reorderType == ReorderType.ignore)
                {
                    kBest(v, IGNORE_REORDERING, Reorder.ignore); // don't mind about order
                }
                // don't allow repetition of same event types or events
                else if(v.node instanceof EventsNode) // TrackNode and EventsNode store the eventType directly
                {
                    if(reorderType == ReorderType.eventType ||
                            reorderType == ReorderType.eventTypeAndField)
                    {
                        int eventType = ((EventsNode)v.node).getEventType();
//                        if(eventType != ex.inferState.none_t())
                            kBest(v, eventType, Reorder.eventType);
//                        else
//                            kBest(v, IGNORE_REORDERING, Reorder.ignore);
                    }
                    // event re-ordering
                    else if(reorderType == ReorderType.event)
                        kBest(v, UNKNOWN_EVENT, Reorder.event);
                }
                // don't allow repetition of same fields
                else if(v.node instanceof FieldsNode)
                {
                    if(reorderType == ReorderType.eventTypeAndField)
                    {
                        // we don't want to perform reordering on none_f
                        int field = ((FieldsNode)v.node).getField();
                        int none_f = ex.events.get(((FieldsNode)v.node).getEvent()).getF();
                        kBest(v, field == none_f ? IGNORE_REORDERING : field, Reorder.field);
                    }
                    else
                        kBest(v, IGNORE_REORDERING, Reorder.ignore); // don't mind about field order
                }
                else
                {
                    kBest(v, IGNORE_REORDERING, Reorder.ignore); // don't mind about order
                }
            }            
        } // for
        if(oracleReranker) // Perform oracle reranking, against BLEU-4 score
        {
            BatchBleuScorer bleuScorer = new BatchBleuScorer();
            String trueStr = GenerationPerformance.widgetToString(vocabulary, (GenWidget)ex.getTrueWidget(), tagDelimiter);
            TreeSet<DerivationWithBleu> set = new TreeSet<DerivationWithBleu>();
            for(int k = 0; k < startNodeInfo.derivations.size(); k++)
            {
                set.add(new DerivationWithBleu(widget, k, ex.N(), trueStr, bleuScorer));
                if(inferState instanceof DiscriminativeInferState)
                    ((DiscriminativeInferState)inferState).getFeatures().clear();
            }
            // choose the derivation with the highest score score
            DerivationWithBleu oracle = set.first();
            if(inferState instanceof DiscriminativeInferState)
                oracle.recurseKBest();
    //        System.out.println(set);
//            System.out.println("K with highest score score is: " + oracle.k);
            return new HyperpathResult(oracle.chooser.widget, oracle.chooser.logWeight);
        }
        else // Original 1-best derivation
        {
            HyperpathChooser chooser = new HyperpathChooser();
            chooser.widget = widget;
            chooser.choose = true;
            chooser.recurseKBest((Derivation)startNodeInfo.derivations.get(0), !discriminative);
//            for(Derivation d : startNodeInfo.derivations)
//            {
//                StringBuilder str = new StringBuilder();
//                for(String s : d.getSubGeneration().split(" "))
//                    str.append(vocabulary.getObject(Integer.valueOf(s))).append(" ");
//                System.out.println(str.toString()+"\n");
//            }
            return new HyperpathResult(chooser.widget, chooser.logWeight);
        }
    }
    
  public HyperpathResult<Widget> rerankOneBestViterbi(Widget widget, Random random)
  {
        computeTopologicalOrdering();
        computeLogMaxScores(); // viterbi using log scores
        HyperpathChooser chooser = new HyperpathChooser();
        chooser.viterbi = true;
        chooser.widget = widget;
        chooser.choose = true;
        chooser.random = random;
        chooser.recurseRerank(startNodeInfo, false);
        return new HyperpathResult(chooser.widget, chooser.logWeight);
  }
  
  public HyperpathResult<Widget> rerankKBestViterbi(Widget widget, Random random)
  {
        computeTopologicalOrdering();
        NodeInfo v;
        for(int i = topologicalOrdering.size()-1; i >= 0; i--)
        {
            v = topologicalOrdering.get(i);
            
            if(v != endNodeInfo)
            {                
                kBest(v, IGNORE_REORDERING, Reorder.ignore); // don't mind about order                
            }            
        } // for
        HyperpathChooser chooser = new HyperpathChooser();
        chooser.widget = widget;
        chooser.choose = true;
        chooser.recurseKBest((Derivation)startNodeInfo.derivations.get(0), !discriminative);
        return new HyperpathResult(chooser.widget, chooser.logWeight);
  }
  
  public HyperpathResult<Widget> oracleOneBestViterbi(Widget widget, Random random)
  {
        computeTopologicalOrdering();
//        computeOracleMaxScores(); // viterbi
        computeInsideMaxScores(true);
        HyperpathChooser chooser = new HyperpathChooser();
        chooser.viterbi = true;
        chooser.widget = widget;
        chooser.choose = true;
        chooser.random = random;
        chooser.recurse(startNodeInfo);
        return new HyperpathResult(chooser.widget, chooser.logWeight);
  }
  
    class DerivationWithBleu implements Comparable{
        HyperpathChooser chooser;
        double score;
        int k, N;
        String predStr;

        public DerivationWithBleu(Widget widget, int k, int N, String trueStr, BatchBleuScorer bleuScorer)
        {
            chooser = new HyperpathChooser();
            chooser.widget = (Widget)new GenWidget(newMatrix(1, N), newMatrix(1, N),
                                           newMatrix(1, N), newMatrix(1, N),
                                           newMatrixOne(N), ((GenWidget)widget).getEventTypeAllowedOnTrack(),
                                           ((GenWidget)widget).getEventTypeIndices());
            chooser.choose = true;
            this.k = k;
            // get the k-best derivation
            recurseKBest();
//            chooser.recurseKBest((Derivation)startNodeInfo.derivations.get(k), !discriminative);
            predStr = GenerationPerformance.widgetToString(vocabulary, (GenWidget)chooser.widget, tagDelimiter);
            // score it
            score = bleuScorer.evaluateBleu(predStr, trueStr);
        }

        public void recurseKBest()
        {
            chooser.recurseKBest((Derivation)startNodeInfo.derivations.get(k), !discriminative);
        }
        
        private int[] newMatrixOne(int N)
        {
            int[] out = new int[N];
            Arrays.fill(out, -1);
            return out;
        }

        private int[][] newMatrix(int C, int N)
        {
            int[][] out = new int[C][N];
            for(int i = 0; i < out.length; i++)
            {
                Arrays.fill(out[i], -1);
            }
            return out;
        }

        /**
         * Compares two derivations against their score
         * @param o
         * @return
         */
        @Override
        public int compareTo(Object o)
        {
            DerivationWithBleu dwb = (DerivationWithBleu)o;
            if(dwb.score > this.score)
                return 1;
            else if(dwb.score < this.score)
                return -1;
            return 0;
        }
    
        @Override
        public String toString()
        {
            return String.format("k=%d, BLEU-4=%s: %s\n", k, score, predStr);
        }


    }
    /**
     * Implementation of k-best cube pruning algorithm found in Huang and Chiang, 2005
     * @param v the node for which we will create k-best derivations
     * @param currentType the event id, eventType id or field id of the current node. Used
     * for re-ordering
     * @param reorder the type of reordering to perform - event, eventType or field
     */
    private void kBest(NodeInfo v, int currentType, Reorder reorder)
    {
        Queue<Derivation> cand = new PriorityQueue<Derivation>(); // a priority queue of candidates
        List<Derivation> buf = new ArrayList<Derivation>();
        Derivation item;
        int[] mask;
        boolean emptyEdge = false;
        for(int i = 0; i < v.edges.size(); i++) // for each incoming edge
        {
            Hyperedge edge = v.edges.get(i);
            // check whether the path has been blocked
            // (semParse: in case NumFieldValueNode is not spanning integers, block the whole hyperpath)
//            if(edge.dest.get(0).derivations != null && edge.dest.get(0).derivations.isEmpty())
//                continue;
            for(NodeInfo child : edge.dest)
            {                
//                if(edge.dest.get(j).derivations != null &&  edge.dest.get(j).derivations.isEmpty())
//                if(child.edges.isEmpty())
                if(child.derivations != null && child.derivations.isEmpty())
                {                    
                    emptyEdge = true; break;
                }
            }
            if(emptyEdge)
            {
                emptyEdge = false;
                continue;
            }
            // mask is set to 0s, i.e. get the 1-best derivation of
            // the antedecendants of v
            if(edge.dest.size() == 1 || edge.dest.get(1) == endNodeInfo)
            {
                mask = new int[1]; // for leaf nodes or nodes with one antecedent                
            }
            else
            {
                mask = new int[edge.dest.size()];
            }
            // check whether the current derivation is of an event (type) OR field
            // that has already been included in the antedecendant derivations
            if(currentType != IGNORE_REORDERING)
            {
                if(reorder == Reorder.eventType)
                {
                    Collection<Integer> eventTypeSet = new HashSet<Integer>();
                    if(!hyperpathContainsEventType(currentType,
                            edge.dest, mask, eventTypeSet))
                    {
                        cand.add(new Derivation(edge, mask, eventTypeSet, null));
                    }
                }
                else if(reorder == Reorder.event)
                {
                    Collection<Integer> eventsStack = new Stack<Integer>();
                    if(!hyperpathContainsEvent(edge.dest, mask, eventsStack))
                    {
                        cand.add(new Derivation(v.edges.get(i), mask, eventsStack, null));
                    }
                }
                else if(reorder == Reorder.field)
                {
                    Collection<Integer> fieldSet = new HashSet<Integer>();
                    if(!hyperpathContainsField(currentType,
                            edge.dest, mask, fieldSet))
                    {
                        cand.add(new Derivation(edge, mask, null, fieldSet));
                    }
                }
            }
            else
            {
                cand.add(new Derivation(edge, mask, null, null));
            }
        } // for
        while(cand.size() > 0 && buf.size() < K)
        {
            item = cand.poll();
            buf.add(item);
            pushSucc(item, cand, currentType, reorder);
        }
        // sort buf to D(v) in descending order
//        Collections.sort(buf, Collections.reverseOrder());
        Collections.sort(buf);
        v.derivations = new ArrayList();
        doHypothesisRecombination(buf);
        v.derivations.addAll(buf);
//        this.logZ += ((Derivation)v.derivations.get(0)).weight.toLogDouble();
    }

    private void pushSucc(Derivation item, Queue<Derivation> cand, int currentEventType,
            Reorder reorder)
    {
        int[] mask = Arrays.copyOf(item.mask, item.mask.length);
        for(int i = 0; i < mask.length; i++) // for i in |e| do
        {
            int[] tempMask = Arrays.copyOf(mask, mask.length); // j'
            tempMask[i]++; // j' <- j + b^i
            if(item.derArray == null || 
               item.edge.dest.get(i).derivations.size() > tempMask[i]) // if D(u_i) is defined, Line 12, Figure 6, Chiang 2007
            {
                // check whether the current derivation is of an event type
                // that has already been included in the antedecendant derivations
                if(currentEventType != IGNORE_REORDERING)
                {                    
                    if(reorder == Reorder.eventType)
                    {
                        Collection<Integer> eventTypeSet = new HashSet<Integer>();
                        if(!hyperpathContainsEventType(currentEventType, item.edge.dest,
                                tempMask, eventTypeSet))
                        {
                            try
                            {
                                cand.add(new Derivation(item.edge, tempMask,
                                        eventTypeSet, null));
                            }
                            catch(NullPointerException npe) {}
                        }
                    }
                    else if(reorder == Reorder.event)
                    {
                        Collection<Integer> eventsStack = new Stack<Integer>();
                        if(!hyperpathContainsEvent(item.edge.dest, tempMask,
                                eventsStack))
                        {
                            try
                            {
                                cand.add(new Derivation(item.edge, tempMask,
                                        eventsStack, null));
                            }
                            catch(NullPointerException npe) {}
                        }
                    }
                    else if(reorder == Reorder.field)
                    {
                        Collection<Integer> fieldSet = new HashSet<Integer>();
                        if(!hyperpathContainsField(currentEventType, item.edge.dest,
                                tempMask, fieldSet))
                        {
                            try
                            {
                                cand.add(new Derivation(item.edge, tempMask, null,
                                        fieldSet));
                            }
                            catch(NullPointerException npe) {}
                        }
                    }
                }
                else
                {
                    try
                    {
                        cand.add(new Derivation(item.edge, tempMask, null, null));
                    }
                    catch(NullPointerException npe) {
//                        LogInfo.error("Error in ignore reordering");
                    }
                }
            }
        } // for
    }


    private boolean hyperpathContainsEvent(ArrayList<NodeInfo> dest,
                                      int[] mask, Collection<Integer> eventStack)
    {
        Derivation d;
        int childEvent = -1;
        // in case we have blocked the path from this node and on, due to not allowing
        // repetition of event types, just ignore the derivation
        for(int i = 0; i < mask.length; i++)
        {
            if(dest.get(i).derivations.isEmpty())
                return true;
        }
        // A TrackNode has two children, a FieldsNode and an EventsNode.
        // We implicitly check the path so far by checking whether the EventsNode
        // derivation denoted by mask, has a conflict with the event stored
        // in the sister FieldsNode. Note only FieldsNodes carry events.
        if(dest.get(0).node instanceof FieldsNode)
        {
            childEvent = ((FieldsNode)dest.get(0).node).getEvent();
            if(dest.get(1).node != endNode)
            {
                d = (Derivation) dest.get(1).derivations.get(mask[1]);             
                if (d.eventTypeSet.contains(childEvent))
                {
                    return true;
                }
                addAll(d.eventTypeSet, (Stack<Integer>) eventStack); // copy the events from the EventsNode
            }
        }
        else // EventsNode. Just copy the events from it's only child, i.e. a TrackNode
        {
            addAll(
                    ((Derivation) dest.get(0).derivations.get(mask[0])).eventTypeSet,
                    (Stack<Integer>)eventStack);
        }        
       
        // allow consequent nodes with the same event type
        //TO-DO: FIX it, it's really ugly!!!
        if(allowConsecutiveEvents)
        {
            if(!eventStack.isEmpty())
            {
                if( childEvent > -1 && childEvent != ((Stack<Integer>)eventStack).peek())
                {
                    add((Stack<Integer>) eventStack,childEvent);
                }
            }
            else if(childEvent > -1)
                add((Stack<Integer>) eventStack,childEvent);            
        }
        else
        {
            if(childEvent > -1)
                add((Stack<Integer>) eventStack,childEvent);
        }
        return false;
    }

    private boolean hyperpathContainsEventType(int currentEventType, ArrayList<NodeInfo> dest,
                                      int[] mask, Collection<Integer> eventTypeSet)
    {
        Derivation d;        
        for(int i = 0; i < mask.length; i++)
        {
            // in case we have blocked the path from this node and on, due to not allowing
            // repetition of event types, just ignore the derivation
            try{
            if(dest.get(i).derivations.isEmpty())
                return true;
            }catch(Exception e){System.out.println("error here "+dest.get(i));}
            d = (Derivation) dest.get(i).derivations.get(mask[i]);
            if(d.eventTypeSet != null)
            {
                if (d.eventTypeSet.contains(currentEventType))
                {
                    return true;
                }
                eventTypeSet.addAll(d.eventTypeSet);
            }
        }
        
        int childEventType = -1;
        if(dest.get(0).node instanceof FieldsNode)
        {
            childEventType = ex.events.get(
                    ((FieldsNode)dest.get(0).node).getEvent()).getEventTypeIndex();
        }
        else if (dest.get(0).node instanceof EventsNode) // TrackNode and EventsNode store the eventType directly
        {
            childEventType = ((EventsNode)dest.get(0).node).getEventType();
        }
        // allow consequent nodes with the same event type
        //TO-DO: FIX it, it's really ugly!!!
        if(allowConsecutiveEvents)
        {
            if(childEventType > -1 && childEventType != currentEventType)
                eventTypeSet.add(childEventType);
        }
        else
        {
            if(childEventType > -1)
                eventTypeSet.add(childEventType);
        }
        return false;
    }

    private boolean hyperpathContainsField(int currentField, ArrayList<NodeInfo> dest,
                                      int[] mask, Collection<Integer> fieldSet)
    {

        Derivation d;
        for(int i = 0; i < mask.length; i++)
        {
            // in case we have blocked the path from this node and on, due to not allowing
            // repetition of event types, just ignore the derivation
            if(dest.get(i).derivations.isEmpty())
                return true;
            d = (Derivation) dest.get(i).derivations.get(mask[i]);
            if(d.fieldSet != null)
            {
                if (d.fieldSet.contains(currentField))
                {
                    return true;
                }
                fieldSet.addAll(d.fieldSet);
            }
        }

        int childField = -1;
        if(dest.get(0).node instanceof FieldsNode)
        {
            childField = ((FieldsNode)dest.get(0).node).getField();
        }
        else if (dest.get(0).node instanceof WordNode)
        {
            childField = ((WordNode)dest.get(0).node).getField();
        }
        // allow consequent nodes with the same event type
        //TO-DO: FIX it, it's really ugly!!!
//        if(allowConsecutiveEvents)
//        {
//            if(childField > -1 && childField != currentField)
//                fieldSet.add(childField);
//        }
//        else
        {
            if(childField > -1)
                fieldSet.add(childField);
        }
        return false;
    }

    private void add(Stack<Integer> stack, Integer i)
    {
        if(!stack.contains(i))
            stack.push(i);
    }

    private void addAll(Collection<Integer> source, Stack<Integer> destination)
    {
        if(source == null)
            return;
        for(Integer i : source)
        {
            if(!destination.contains(i))
                destination.push(i);
        }
    }
    Set<String> subGenerations = new HashSet<String>(K);
    /**
     * Perform hypothesis recombination on the sorted {@link List} <code>list</code>
     * of derivations. Checks whether two derivations have the same sub-generation
     * result and removes the one with the lowest score
     * @param list List of sorted derivations
     */
    private void doHypothesisRecombination(List<Derivation> list)
    {
        if(list.size() < 2) return;
        subGenerations.clear();
//        Set<String> subGenerations = new HashSet<String>(list.size());
        for (Iterator<Derivation> iter = list.listIterator(); iter.hasNext(); )
        {
            String subGeneration = iter.next().getSubGeneration();
            if(subGenerations.contains(subGeneration))
                iter.remove();
            else
                subGenerations.add(subGeneration);
        }
        
//        for (Derivation d : list)
//            subGenerations.add(d.getSubGeneration());        
//        for (int i = 0; i < subGenerations.size(); i++)
//        {
//            int j = i + 1;
//            for (Iterator<String> iter = subGenerations.listIterator(i + 1); iter.hasNext(); )
//            {
//                if(subGenerations.get(i).equals(iter.next()))
//                {
//                    iter.remove();
//                    list.remove(j);
//                }
//                j++;
//            }
//        }
    }
//    private void doHypothesisRecombination(List<Derivation> list)
//    {
//        if(list.size() < 2) return;
//
////        for (Iterator<Derivation> i = list.listIterator(); i.hasNext(); )
//        for (int i = 0; i < list.size(); i++)
//        {
//            Derivation ref = list.get(i);
//            for (Iterator<Derivation> j = list.listIterator(i + 1); j.hasNext(); )
//            {
//                if(ref.isEquivalentTo(j.next()))
//                    j.remove();
//            }
//        }
//    }

  private void computeInsideMaxScores(boolean viterbi) {
    if(viterbi && this.startNodeInfo.maxScore != null) return; // Already computed
    if(!viterbi && this.startNodeInfo.insideScore != null) return; // Already computed

    for(int i = topologicalOrdering.size()-1; i >= 0; i--) {
      NodeInfo nodeInfo = topologicalOrdering.get(i);
      BigDouble score;
      if(viterbi) score = nodeInfo.maxScore = BigDouble.invalid();
      else        score = nodeInfo.insideScore = BigDouble.invalid();
      if(nodeInfo == endNodeInfo) { score.setToOne(); continue; }
      switch(nodeInfo.nodeType) {
        case sum:
          score.setToZero();
          int chosenIndex = -1;
//          for(Hyperedge edge : nodeInfo.edges) {
          for(int k = 0; k < nodeInfo.edges.size(); k++)
          {
            Hyperedge edge = nodeInfo.edges.get(k);
            if(viterbi) 
            {             
                if(score.updateMax_mult3(edge.weight, edge.dest.get(0).maxScore, edge.dest.get(1).maxScore))
                {
                    chosenIndex = k;
                }
            }
            else 
                score.incr_mult3(edge.weight, edge.dest.get(0).insideScore, edge.dest.get(1).insideScore);
            nodeInfo.bestEdge = chosenIndex;
            //assert score.M != 0 : score + " " + edge.weight + " " + edge.dest1.insideScore + " " + edge.dest2.insideScore;
          }
          break;
        case prod:
          score.setToOne();
          for(Hyperedge edge : nodeInfo.edges) {
            if(viterbi) score.mult_mult3(edge.weight, edge.dest.get(0).maxScore, edge.dest.get(1).maxScore);
            else        score.mult_mult3(edge.weight, edge.dest.get(0).insideScore, edge.dest.get(1).insideScore);
          }
          break;
      }
      //if(!viterbi) dbgs("insideScore(%s) = %s", nodeInfo.node, nodeInfo.insideScore);
    }
    if(viterbi) assert !startNodeInfo.maxScore.isZero() : "Max score = 0";
    else        assert !startNodeInfo.insideScore.isZero() : "Marginal score = 0";
  }

  private void computeOutsideScores() {
    if(startNodeInfo.outsideScore != null) return; // Already computed

    // Initialize values to zero
    for(NodeInfo nodeInfo : topologicalOrdering)
      nodeInfo.outsideScore = BigDouble.zero();

    startNodeInfo.outsideScore.setToOne();
    for(int i = 0; i < topologicalOrdering.size(); i++) {
      NodeInfo nodeInfo = topologicalOrdering.get(i);
      if(nodeInfo.insideScore.isZero()) 
      {
          continue; // This happens for dead nodes
      }
          
      //dbgs("outsideScore(%s) = %s", nodeInfo.node, nodeInfo.outsideScore);
      switch(nodeInfo.nodeType) {
        case sum:
          for(Hyperedge edge : nodeInfo.edges) {
            if(edge.dest.get(0) != endNodeInfo) edge.dest.get(0).outsideScore.
                    incr_mult3(nodeInfo.outsideScore, edge.weight,
                               edge.dest.get(1).insideScore);
            if(edge.dest.get(1) != endNodeInfo) edge.dest.get(1).outsideScore.
                    incr_mult3(nodeInfo.outsideScore, edge.weight,
                               edge.dest.get(0).insideScore);
          }
          break;
        case prod:
          for(Hyperedge edge : nodeInfo.edges) {
            if(edge.dest.get(0) != endNodeInfo) edge.dest.get(0).outsideScore.
                    incr_mult2div1(nodeInfo.outsideScore, nodeInfo.insideScore,
                                   edge.dest.get(0).insideScore);
            if(edge.dest.get(1) != endNodeInfo) edge.dest.get(1).outsideScore.
                    incr_mult2div1(nodeInfo.outsideScore, nodeInfo.insideScore,
                                   edge.dest.get(1).insideScore);
          }
          break;
      }
    }
  }

  private void computeLogMaxScores() {
    if(this.startNodeInfo.logMaxScore > Double.NEGATIVE_INFINITY) return; // Already computed    
    for(int i = topologicalOrdering.size()-1; i >= 0; i--) 
    {
        NodeInfo nodeInfo = topologicalOrdering.get(i);
        if(nodeInfo == endNodeInfo) { nodeInfo.logMaxScore = 0; continue; }
        double score = Double.NEGATIVE_INFINITY;
        int chosenIndex = -1;
        for(int k = 0; k < nodeInfo.edges.size(); k++)
        {
            Hyperedge edge = nodeInfo.edges.get(k);
            double sum = edge.logWeight;
//            List<Integer> words = new ArrayList();
//            for(NodeInfo node : edge.dest)
//                words.addAll(node.derivations.get(0).words);            
//            double sum = edge.logWeight + 
//                    (edge.info instanceof HyperedgeInfoOnline ? 
//                        ((HyperedgeInfoOnline)edge.info).getOnlineWeight(words) : 0.0);
            for(NodeInfo info : edge.dest)
            {
                sum += info.logMaxScore;
            }
            if(score < sum)
            {
                score = sum;
                chosenIndex = k;
            }          
        } // for
        nodeInfo.logMaxScore = score;
        nodeInfo.bestEdge = chosenIndex;
    } // for
//      System.out.println("start.maxScore: " + startNodeInfo.logMaxScore);
    assert startNodeInfo.logMaxScore > Double.NEGATIVE_INFINITY : "Max score = -Infinity";  
    assert startNodeInfo.bestEdge != -1 : "No best edge attached to start node";
  }      
  
  public void fetchPosteriors(boolean viterbi) {
    if(viterbi) fetchPosteriorsMax(); // Only need to call setPosteriors on the best widget
    else        fetchPosteriorsSum(); // Call setPosteriors on each hyperedge
  }

  public void computeELogZEntropy(boolean viterbi) {
    if(viterbi) { // Easy case: q(z|x) is degenerate
      this.elogZ = this.logZ;
      this.entropy = 0;
      return;
    }

    this.elogZ = 0;
    this.entropy = 0;
    for(NodeInfo nodeInfo : topologicalOrdering) {
      //dbg(startNodeInfo.insideScore);
      double nodeProb = BigDouble.mult2div1(nodeInfo.outsideScore, 
                                            nodeInfo.insideScore,
                                            startNodeInfo.insideScore).toDouble();
      if(nodeProb == 0) continue;
      switch(nodeInfo.nodeType) {
        case sum:
          for(Hyperedge edge : nodeInfo.edges) {
            double edgeProb = BigDouble.mult4div1(nodeInfo.outsideScore, 
                    edge.weight, edge.dest.get(0).insideScore,
                    edge.dest.get(1).insideScore, startNodeInfo.insideScore).toDouble();
            if(edgeProb == 0) continue;
            elogZ += edgeProb * edge.weight.toLogDouble();
            entropy -= edgeProb * Math.log(edgeProb/nodeProb);
          }
          break;
        case prod:
          // No uncertainty, so no contribution to entropy
          for(Hyperedge edge : nodeInfo.edges)
            elogZ += nodeProb * edge.weight.toLogDouble();
          break;
      }
    }
  }

  private void fetchPosteriorsSum() {
    for(NodeInfo nodeInfo : topologicalOrdering) {
      switch(nodeInfo.nodeType) {
        case sum:
          for(Hyperedge edge : nodeInfo.edges) {
            double prob = BigDouble.mult4div1(nodeInfo.outsideScore, edge.weight,
              edge.dest.get(0).insideScore, edge.dest.get(1).insideScore,
              startNodeInfo.insideScore).toDouble();
            assert prob >= 0 && prob <= 1+1e-6 : nodeInfo + " " + edge + " has invalid posterior probability " + prob;
            //if(prob > 0.1) dbgs("setPosterior sum %s %s", edge, Fmt.D(prob));
            edge.info.setPosterior(prob);
          }
          break;
        case prod:
          for(Hyperedge edge : nodeInfo.edges) {
            double prob = BigDouble.mult2div1(nodeInfo.outsideScore, nodeInfo.insideScore, startNodeInfo.insideScore).toDouble();
            assert prob >= 0 && prob <= 1+1e-6 : nodeInfo + " " + edge + " has invalid posterior probability " + prob;
            //if(prob > 0.1) dbgs("setPosterior prod %s %s", edge, Fmt.D(prob));
            edge.info.setPosterior(prob);
          }
          break;
      }
    }
  }

  private void fetchPosteriorsMax() {
    HyperpathChooser chooser = new HyperpathChooser();
    chooser.viterbi = true;
    chooser.setPosterior = true;
    chooser.recurse(startNodeInfo);
  }

  // Return the best or a sampled solution
  public HyperpathResult<Widget> fetchBestHyperpath(Widget widget) {
    computeInsideMaxScores(true);
    HyperpathChooser chooser = new HyperpathChooser();
    chooser.viterbi = true;
    chooser.widget = widget;
    chooser.choose = true;
    chooser.recurse(startNodeInfo);
    return new HyperpathResult(chooser.widget, chooser.logWeight);
  }
  public HyperpathResult<Widget> fetchSampleHyperpath(Random random, Widget widget) {
    computeInsideMaxScores(false);
    HyperpathChooser chooser = new HyperpathChooser();
    chooser.viterbi = false;
    chooser.widget = widget;
    chooser.random = random;
    chooser.choose = true;
    chooser.recurse(startNodeInfo);
    return new HyperpathResult(chooser.widget, chooser.logWeight);
  }

  public class HyperpathResult<Widget> {
    public HyperpathResult(Widget widget, double logWeight) {
      this.widget = widget;
      this.logWeight = logWeight;
    }

    public HyperpathResult(Widget widget, double logWeight, int[] text) {
      this.widget = widget;
      this.logWeight = logWeight;
      this.kBestText = text;
    }

    public final Widget widget;
    public final double logWeight;
    public int[] kBestText;
  }

  private class HyperpathChooser {
    boolean viterbi;
    Widget widget;
    Random random;
    // Which function to call to return what was chosen
    boolean choose;
    boolean setPosterior;
    double logWeight; // Likelihood of the weight of the hyperpath chosen

      private void recurseKBest(Derivation derivation, boolean calculateLogVZ)
      {
          if(derivation.derArray == null) // choose terminal nodes
          {
              if(!derivation.words.isEmpty())
              {
                  widget = (Widget) ((HyperedgeInfoLM)derivation.edge.info).
                          chooseWord(widget, derivation.words.get(0));
    //                  System.out.println(derivation.edge);
              }
              return;
          }

         // choose intermediate non-terminal nodes
          widget = (Widget)derivation.edge.info.choose(widget);
//              System.out.println(derivation.edge + " " + derivation.head);
    //          if(setPosterior) derivation.edge.info.setPosterior(1.0);
          if(calculateLogVZ)
              logWeight += derivation.weight.toLogDouble();
          for(Derivation d : derivation.derArray)
          {
              recurseKBest(d, calculateLogVZ);
          }
      }    
    
    private void recurse(NodeInfo nodeInfo) {
      if(nodeInfo == endNodeInfo) return;

      switch(nodeInfo.nodeType) {
        case sum:
          int n = nodeInfo.edges.size();
          // Compute scores
          BigDouble[] scores = null;
          if(!viterbi)
          {
            scores = new BigDouble[n];
            for(int i = 0; i < n; i++)
            {                
                Hyperedge edge = nodeInfo.edges.get(i);
                scores[i] = BigDouble.mult3(edge.weight, edge.dest.get(0).insideScore,
                                           edge.dest.get(1).insideScore);
            }
          }
          // Choose edge
          int chosenIndex;
//          if(viterbi) chosenIndex = BigDouble.argmax(scores);
          if(viterbi)
          {
              chosenIndex = nodeInfo.bestEdge;
          }
          else
          {
              chosenIndex = BigDouble.normalizeAndSample(random, scores);
          }
          if(chosenIndex == -1)
            throw Exceptions.bad("Unable to choose best child node %s", nodeInfo);
          Hyperedge chosenEdge = nodeInfo.edges.get(chosenIndex);
          if(choose) widget = (Widget)chosenEdge.info.choose(widget);
          //if(choose) dbg("Choose "+widget);
          if(setPosterior) chosenEdge.info.setPosterior(1.0);
          logWeight += chosenEdge.weight.toLogDouble();
          for(NodeInfo node : chosenEdge.dest)
          {
              recurse(node);
          }
//          recurse(chosenEdge.dest.get(0));
//          recurse(chosenEdge.dest.get(1));
          break;
        case prod:
          // Recurse on each edge
          for(Hyperedge edge : nodeInfo.edges) {
            if(choose) widget = (Widget)edge.info.choose(widget);
            if(setPosterior) edge.info.setPosterior(1.0);
            logWeight += edge.weight.toLogDouble();
            for(NodeInfo node : edge.dest)
            {
                recurse(node);
            }
//            recurse(edge.dest.get(0));
//            recurse(edge.dest.get(1));
          }
          break;
      }
    }  
    
    private void recurseRerank(NodeInfo nodeInfo, boolean calculateLogVZ) {
        if(nodeInfo == endNodeInfo) 
            return;                
        // Choose edge
        int chosenIndex = nodeInfo.bestEdge;          
        if(chosenIndex == -1)
            throw Exceptions.bad("No best edge found!");
        Hyperedge chosenEdge = nodeInfo.edges.get(chosenIndex);
        if(choose) 
            widget = (Widget)chosenEdge.info.choose(widget);          
        if(calculateLogVZ)
            logWeight += chosenEdge.logWeight;
        for(NodeInfo node : chosenEdge.dest)
        {
            recurseRerank(node, calculateLogVZ);
        }
    }
  }
}