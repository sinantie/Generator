package induction;

import edu.berkeley.nlp.ling.CollinsHeadFinder;
import edu.berkeley.nlp.ling.HeadFinder;
import edu.berkeley.nlp.ling.Tree;
import fig.basic.ListUtils;
import java.util.ArrayList;
import java.util.List;



public class TreeUtils {
  // (Markov) order = size of history to remember
  // Create new labels such as @S-NP
  // If order == -1, then don't even put @
  public static Tree<String> binarize(Tree<String> tree, int order) {
    String rootLabel = tree.getLabel();

    if(tree.getChildren().size() == 0)
      return tree;
    if(tree.getChildren().size() == 1)
      return new Tree<String>(rootLabel,
        tree.isIntermediateNode(),
        ListUtils.newList(binarize(tree.getChildren().get(0), order)));

    // Binarize all children
    List<Tree<String>> newChildren = new ArrayList();
    for(Tree<String> child : tree.getChildren())
      newChildren.add(binarize(child, order));

    // Construct the labels of the intermediate nodes
    List<String> intermediateLabels = new ArrayList();
    for(Tree<String> child : tree.getChildren())
      intermediateLabels.add(child.getLabel());
    for(int i = intermediateLabels.size()-1; i >= 0; i--) {
      StringBuilder buf = new StringBuilder((order == -1 ? "" : "@")+tree.getLabel());
      for(int k = 0; k < order && i-k >= 0; k++)
        buf.append("-"+intermediateLabels.get(i-k));
      intermediateLabels.set(i, buf.toString());
    }

    // Build tree with intermediate nodes
    Tree<String> newTree = newChildren.get(intermediateLabels.size()-1);
    for(int i = intermediateLabels.size()-2; i >= 0; i--) {
      String intermediateLabel =
        (i == 0 ? rootLabel : intermediateLabels.get(i-1));
      newTree = new Tree<String>(intermediateLabel, i > 0,
                              ListUtils.newList(newChildren.get(i), newTree));
    }
    return newTree;
  }

  private static final HeadFinder headFinder = new CollinsHeadFinder();
  // Binarize direction based on heads
  // First binarize right arguments and then binarize left arguments
  // In (X A B C D E), if C is the head, then the result is
  // (X A (Xl B (Xr (Xr C D) E)))
  public static Tree<String> headBinarize(Tree<String> tree, int order) {
    String rootLabel = tree.getLabel();

    if(tree.getChildren().size() == 0)
      return tree;
    if(tree.getChildren().size() == 1)
      return new Tree<String>(rootLabel,
        tree.isIntermediateNode(),
        ListUtils.newList(headBinarize(tree.getChildren().get(0), order)));

    // Recursively binarize all children
    List<Tree<String>> newChildren = new ArrayList();
    for(Tree<String> child : tree.getChildren())
      newChildren.add(headBinarize(child, order));

    int head = tree.getChildren().indexOf(headFinder.determineHead(tree));
    Tree<String> newTree = newChildren.get(head);
    int numLeftArgs = head;
    int numRightArgs = newChildren.size()-head-1;
    int numArgs = numLeftArgs + numRightArgs;

    for(int i = 0; i < numRightArgs; i++) { // Right arguments
      boolean isRoot = (--numArgs == 0);
      String intermediateLabel = (isRoot || order < 0 ? rootLabel : rootLabel+"-R");
      newTree = new Tree<String>(intermediateLabel, !isRoot,
        ListUtils.newList(newTree, newChildren.get(head+i+1)));
    }

    for(int i = 0; i < numLeftArgs; i++) { // Left arguments
      boolean isRoot = (--numArgs == 0);
      String intermediateLabel = (isRoot || order < 0 ? rootLabel : rootLabel+"-L");
      newTree = new Tree<String>(intermediateLabel, !isRoot,
        ListUtils.newList(newChildren.get(head-i-1), newTree));
    }

    return newTree;
  }

  // For chain of unaries A -> B -> C, replace with C
  public static <T> Tree<T> removeUnaries(Tree<T> tree) {
    if(tree.isLeaf() || tree.isPreTerminal())
      return tree;
    else {
      List<Tree<T>> children = new ArrayList();
      // Find unique descendent (could be self) with more than one node
      while(tree.getChildren().size() == 1 && !tree.isPreTerminal())
        tree = tree.getChildren().get(0);
      for(Tree<T> child : tree.getChildren())
        children.add(removeUnaries(child));
      return new Tree<T>(tree.getLabel(), tree.isIntermediateNode(), children);
    }
  }

  public static <T> Tree<T> replaceTerminalsWithPreterminals(Tree<T> tree) {
    if(tree.isPreTerminal()) {
      return new Tree<T>(tree.getLabel(), tree.isIntermediateNode(),
          ListUtils.newList(new Tree<T>(tree.getLabel(), tree.isIntermediateNode())));
    }
    List<Tree<T>> children = new ArrayList();
    for(Tree<T> child : tree.getChildren())
      children.add(replaceTerminalsWithPreterminals(child));
    return new Tree<T>(tree.getLabel(), tree.isIntermediateNode(), children);
  }

  public interface NodeFunc<T> {
    T apply(T label);
  }
  // Apply the function to all non-terminal labels.
  // If return null, then that whole subtree is killed (and possibly it's
  // parent if it's a unary).
  public static <T> Tree<T> transformNonterminals(Tree<T> tree, NodeFunc<T> func) {
    if(tree.isLeaf()) return tree;
    T label = func.apply(tree.getLabel());
    if(label == null) return null;
    List<Tree<T>> newChildren = new ArrayList();
    for(Tree<T> child : tree.getChildren()) {
      Tree<T> newChild = transformNonterminals(child, func);
      if(newChild != null)
        newChildren.add(newChild);
    }
    if(newChildren.size() == 0) return null;
    return new Tree<T>(label, tree.isIntermediateNode(), newChildren);
  }

  public static String[] punctuationTags = 
    { ".", ",", ":", "``", "''", "-LRB-", "-RRB-", "LS", "#", "$" };
  public static boolean isPunctuationTag(String tag) {
    return ListUtils.indexOf(punctuationTags, tag) != -1;
  }
  public static Tree<String> removePunctuation(Tree<String> tree) {
    return TreeUtils.transformNonterminals(tree, new TreeUtils.NodeFunc<String>() {
      public String apply(String tag) { return isPunctuationTag(tag) ? null : tag; }
    });
  }
}
