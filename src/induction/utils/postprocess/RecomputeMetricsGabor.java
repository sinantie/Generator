package induction.utils.postprocess;

import edu.berkeley.nlp.mt.BatchBleuModifiedScorer;
import edu.berkeley.nlp.mt.BatchBleuScorer;
import edu.cmu.meteor.Meteor;
import edu.cmu.meteor.scorer.MeteorScorer;
import edu.cmu.meteor.scorer.MeteorStats;
import induction.problem.event3.TERMetric;
import java.io.FileOutputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import tercom.TERalignment;
import tercom.TERcalc;

/**
 *
 * @author konstas
 */
public class RecomputeMetricsGabor
{
    private final String inputFile;
    BatchBleuScorer bleuScorer, bleuModifiedScorer;
    MeteorScorer meteorScorer;
    MeteorStats meteorAggStats;
    TERMetric terScorer;
    double TERTotalEdits = 0.0, TERTotalWords = 0.0;

    public RecomputeMetricsGabor(String inputFile)
    {
        this.inputFile = inputFile;
        bleuScorer = new BatchBleuScorer();
        bleuModifiedScorer = new BatchBleuModifiedScorer(4);
        meteorScorer = Meteor.MeteorScorerFactory();
        meteorAggStats = new MeteorStats();
        terScorer = new TERMetric();
    }

     private String output()
    {
        meteorScorer.computeMetrics(meteorAggStats);
        String out = "BLEU scores";
        out += "\n-----------\n";
        out += bleuScorer.getScore().toString();
        out += "\n\nModified BLEU scores";
        out += "\n-----------\n";
        out += bleuModifiedScorer.getScore().toString();
        out += "\n\nMETEOR scores";
        out += "\n-------------";
        out += "\nTest words:\t\t" + meteorAggStats.testLength;
        out += "\nReference words:\t" + meteorAggStats.referenceLength;
        out += "\nChunks:\t\t\t" + meteorAggStats.chunks;
        out += "\nPrecision:\t\t" + meteorAggStats.precision;
        out += "\nRecall:\t\t\t" + meteorAggStats.recall;
        out += "\nf1:\t\t\t" + meteorAggStats.f1;
        out += "\nfMean:\t\t\t" + meteorAggStats.fMean;
        out += "\nFragmentation penalty:\t" + meteorAggStats.fragPenalty;
        out += "\n";
        out += "\nFinal score:\t\t" + meteorAggStats.score;

        out += "\n\nTER scores";
        out += "\n---------";
        out += "\nTotal TER: " + (TERTotalEdits / TERTotalWords) + " (" +
			   TERTotalEdits + "/" + TERTotalWords + ")";
	out += "\n\nNumber of calls to beam search: " + TERcalc.numBeamCalls();
	out += "\nNumber of segments scored: " + TERcalc.numSegsScored();
	out += "\nNumber of shifts tried: " + TERcalc.numShiftsTried();

        return out;
    }

    public void parseXmlFile(FileOutputStream fos) throws Exception
    {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(inputFile);
        doc.getDocumentElement().normalize();

        // instance level contain the gold and guessed text
        NodeList nodeInstanceList = doc.getElementsByTagName("instance");
        String keyId = "", predStr = "", trueStr = "";
        for(int i = 0; i < nodeInstanceList.getLength(); i++)
        {
            Node instanceNode = nodeInstanceList.item(i);
            if(instanceNode.getNodeType() == Node.ELEMENT_NODE)
            {
                Element instanceElement = (Element)instanceNode;
                keyId = instanceElement.getAttribute("id");
                // get the guess and gold tags                
                predStr = ((Element)instanceElement.getElementsByTagName("guess").item(0)).
                        getChildNodes().item(0).getNodeValue().trim();
                trueStr = ((Element)instanceElement.getElementsByTagName("gold").item(0)).
                        getChildNodes().item(0).getNodeValue().trim();
//                predStr = instanceNode.getChildNodes().item(1).getTextContent().trim();
//                trueStr = instanceNode.getChildNodes().item(3).getTextContent().trim();
                fos.write(computeSGMLEntry(keyId, predStr, trueStr).getBytes());
            }
        }
    }

     private String computeSGMLEntry(String docid, String predStr, String trueStr)
    {
        // Compute BLEU
        double bleuScore = bleuScorer.evaluateBleu(predStr, trueStr);
        // Compute modified BLEU (Don't penalise number deviations of 5 scalars)
        double bleuModifiedScore = bleuModifiedScorer.evaluateBleu(predStr, trueStr);
        // Compute METEOR
        MeteorStats meteorWidgetStats = meteorScorer.getMeteorStats(
                                  predStr,
                                  trueStr);
        meteorAggStats.addStats(meteorWidgetStats);
        // Compute TER
        TERalignment terWidgetStats = terScorer.getScore(predStr, trueStr);
        TERTotalEdits += terWidgetStats.numEdits;
        TERTotalWords += terWidgetStats.numWords;
        double terScore = terWidgetStats.score();

        return "<doc docid=\"" + docid  + "\" genre=\"nw\">\n" +
                     "<p>\n<seg id=\"1\" " +
                     "bleu=\"" + bleuScore + "\"" +
                     " bleu_modified=\"" + bleuModifiedScore + "\"" +
                     " meteor=\"" + meteorWidgetStats.score + "\"" +
                     " ter=\"" + terScore + "\"" +
                     ">" +
                     predStr +
                     "</seg>\n</p>\n</doc>\n";
    }
     
    public void execute()
    {
        try
        {
            // open output stream(s) and write header data
            FileOutputStream fosRecomputed = new FileOutputStream(inputFile + ".recomputed");
            fosRecomputed.write(("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<mteval>\n" +
                    "<tstset setid=\"recomputed\" srclang=\"English\" " +
                    "trglang=\"English\" sysid=\"sample_system\">\n").getBytes());
            parseXmlFile(fosRecomputed);
            fosRecomputed.write("</tstset>\n</mteval>".getBytes());
            fosRecomputed.close();
            // open output stream(s) and write summary data
            FileOutputStream fos = new FileOutputStream(inputFile + ".performance");
            fos.write(output().getBytes());
            fos.close();

        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void main(String[] args)
    {
        String inputFile = "";
        if(args.length < 1)
            inputFile = "/home/konstas/EDI/Gabor/generation/outs/atis/1.exec/results-test.xml";
        else
            inputFile = args[0];

        RecomputeMetricsGabor rgm = new RecomputeMetricsGabor(inputFile);
        rgm.execute();
    }
}
