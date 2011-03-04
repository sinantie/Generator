/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * VisualiseHypergraph.java
 *
 * Created on 01-Mar-2011, 23:28:23
 */

package induction.utils;

import edu.uci.ics.jung.algorithms.layout.FRLayout2;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.SpringLayout2;
import edu.uci.ics.jung.graph.DelegateTree;
import edu.uci.ics.jung.graph.Forest;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.PickingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.PluggableGraphMouse;
import edu.uci.ics.jung.visualization.control.ScalingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.TranslatingGraphMousePlugin;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position;
import fig.exec.Execution;
import induction.LearnOptions;
import induction.Options;
import induction.Options.InitType;
import induction.problem.event3.Event3Model;
import induction.problem.event3.nodes.EventsNode;
import induction.problem.event3.nodes.Node;
import induction.problem.event3.nodes.TrackNode;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseEvent;

/**
 *
 * @author sinantie
 */
public class VisualiseHypergraph extends javax.swing.JFrame {

    private VisualizationViewer vv;
    private Layout layout;
    LearnOptions lopts;
    String name;
    Event3Model model;

    /** Creates new form VisualiseHypergraph */
    public VisualiseHypergraph() {
        initComponents();
        setSize(1000, 1000);
        setUp();
        setUpView(model.testSemParseVisualise(name, lopts));
    }

    private void setUp()
    {
         String args = "-modelType semParse -testInputLists test/testRobocupEvents "
//         String args = "-modelType semParse -testInputLists robocupLists/robocupFold1PathsEval "
                    + "-excludeLists robocupLists/robocupAllUnreachable "
                    + "-inputFileExt events -stagedParamsFile "
                    + "results/output/robocup/model_3_percy_NO_NULL_semPar_values_unk_no_generic/fold1/stage1.params.obj "
                    + "-disallowConsecutiveRepeatFields -kBest 2 "
                    + "-ngramModelFile robocupLM/srilm-abs-robocup-fold1-3-gram.model.arpa "
                    + "-ngramWrapper kylm -reorderType eventTypeAndField "
                    + "-maxPhraseLength 5 -useGoldStandardOnly "
                    + "-modelUnkWord -newFieldPerWord 0,-1 -allowConsecutiveEvents";
        /*initialisation procedure from Generation class*/
        Options opts = new Options();
        Execution.init(args.split(" "), new Object[] {opts}); // parse input params
        model = new Event3Model(opts);
        model.init(InitType.staged, opts.initRandom, "");
        model.readExamples();
        model.logStats();
        opts.outputIterFreq = opts.stage1.numIters;
        lopts = opts.stage1;
        name = "stage1";
    }

    private void setUpView(Graph graph)
    {                 
        layout = new SpringLayout2(graph);
        //layout.setSize(new Dimension(700,700));
        vv = new VisualizationViewer(layout);
        vv.setPreferredSize(new Dimension(900,900));
        vv.setBackground( Color.white );
        // Tell the renderer to use our own customized label rendering
        vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller());
        vv.getRenderContext().setEdgeLabelTransformer(new ToStringLabeller());
        vv.getRenderer().getVertexLabelRenderer().setPosition(Position.CNTR);
        getContentPane().add(new GraphZoomScrollPane(vv), BorderLayout.CENTER);

        /*Mouse controller plugins*/
        PluggableGraphMouse gm = new PluggableGraphMouse();
        gm.add(new TranslatingGraphMousePlugin(MouseEvent.BUTTON3_MASK));
        gm.add(new ScalingGraphMousePlugin(new CrossoverScalingControl(), 0, 1.1f, 0.9f));
        gm.add(new PickingGraphMousePlugin());
        vv.setGraphMouse(gm);
    }

    private Forest generateGraph()
    {
        /*position vertices in graph*/
//        DirectedSparseGraph<Node, HyperEdge> graph  =
//                new DirectedSparseGraph<Node, HyperEdge>();
        DelegateTree graph  =
                new DelegateTree<HyperEdge, Node>();
        EventsNode ev0 = new EventsNode(0, 5);
        TrackNode tr1 = new TrackNode(0, 1, 0, 0, true, true);
        EventsNode ev1 = new EventsNode(1, 0);
        graph.addVertex(ev0);
        graph.addChild(new HyperEdge(1.0), ev0, ev1);
        graph.addChild(new HyperEdge(1.0), ev0, tr1);
        
//        graph.addEdge(ev0, Arrays.asList(ev1, tr1));
//        graph.addEdge(new HyperEdge(1.0), ev0, tr1);

        return graph;
    }
     
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new java.awt.FlowLayout());

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new VisualiseHypergraph().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables

    class HyperEdge {
        private double weight;

        public HyperEdge(double weight)
        {
            this.weight = weight;
        }

        public void setWeight(double weight)
        {
            this.weight = weight;
        }

        public double getWeight()
        {
            return weight;
        }

        @Override
        public String toString()
        {
            return String.valueOf(weight);
        }

    }
}
