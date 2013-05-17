package induction.utils.postprocess;

import edu.berkeley.nlp.mt.BatchBleuModifiedScorer;
import edu.berkeley.nlp.mt.BatchBleuScorer;
import edu.cmu.meteor.Meteor;
import edu.cmu.meteor.scorer.MeteorScorer;
import edu.cmu.meteor.scorer.MeteorStats;
import induction.Utils;
import induction.problem.event3.TERMetric;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
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
    public enum TypeOfId {filename, orderedText, goldText}
    private final String inputFile, arg;
    private String[] listOfIds;
    private Map<String, String> mapOfIds;
    private final TypeOfId typeOfId;
    BatchBleuScorer bleuScorer, bleuModifiedScorer;
    MeteorScorer meteorScorer;
    MeteorStats meteorAggStats;
    TERMetric terScorer;
    double TERTotalEdits = 0.0, TERTotalWords = 0.0;

    public RecomputeMetricsGabor(String inputFile, String arg, TypeOfId typeOfId)
    {
        this.inputFile = inputFile;
        this.arg = arg;
        this.typeOfId = typeOfId;
        bleuScorer = new BatchBleuScorer();
        bleuModifiedScorer = new BatchBleuModifiedScorer(4);
        meteorScorer = Meteor.MeteorScorerFactory();
        meteorAggStats = new MeteorStats();
        terScorer = new TERMetric();
        if(typeOfId == TypeOfId.filename)
            listOfIds = Utils.readLines(arg); // each line of the file is going to be the id of each example
        else if(typeOfId == TypeOfId.goldText)
        {
            // each line is a file with the gold text. Use this to reverse
            // map each example to a filename. Won't get a guaranteed mapping!
            mapOfIds = new HashMap<String, String>();
            for(String filename : Utils.readLines(arg))
            {
                String text;
                try
                {
                    text = Utils.readFileAsString(filename).replaceAll("\n", " ").toLowerCase().trim();
                    mapOfIds.put(text, filename);
                }
                catch (IOException ex)
                {
                    ex.printStackTrace();
                }                
            }
        }
            
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
                // get the guess and gold tags                
                predStr = ((Element)instanceElement.getElementsByTagName("guess").item(0)).
                        getChildNodes().item(0).getNodeValue().trim();
                trueStr = ((Element)instanceElement.getElementsByTagName("gold").item(0)).
                        getChildNodes().item(0).getNodeValue().trim();
                //                keyId = instanceElement.getAttribute("id");
                // use id given from external source, so that it matches to our models' output
                switch(typeOfId)
                {
                    case filename: keyId = listOfIds[i]; break;
                    case orderedText : keyId = arg + (i+1); break;
                    case goldText : keyId = mapOfIds.get(trueStr); break;
                }
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
    
    public static void main(String[] args)
    {
        String inputFile = "";
        String arg = "";
        TypeOfId type = TypeOfId.filename;
        if(args.length < 3)
        {
            // Robocup
//            inputFile = "../Gabor/gaborFiles/2010emnlp-generation/results-robocup2004.xml";
//            arg = "robocupLists/robocupFold4PathsEvalText";
//            type = TypeOfId.goldText;
            // WeatherGov
//            inputFile = "../Gabor/gaborFiles/2010emnlp-generation/results-weather.xml";
//            arg = "gaborLists/genEvalListPathsGaborText";
//            type = TypeOfId.filename;
            // Atis
//            inputFile = "../Gabor/generation/outs/atis/1.exec/results-test.xml";
//            arg = "Example_";
//            type = TypeOfId.orderedText;
            // WinHelp
            inputFile = "../Gabor/generation/outs/winHelp.docs.newAnnotation.removedOutliers/fold5/0.exec/results-test.xml";
//            inputFile = "../Gabor/generation/outs/winHelp.docs.newAnnotation.removedOutliers/all";
            arg = "Example_";
            type = TypeOfId.orderedText;
        }
        else
        {
            inputFile = args[0];
            arg = args[1];
            type = args[2].equals("filename") ? TypeOfId.filename : 
                    (args[2].equals("orderedText") ? TypeOfId.orderedText : TypeOfId.goldText);
        }

        RecomputeMetricsGabor rgm = new RecomputeMetricsGabor(inputFile, arg, type);
        rgm.execute();
    }
}
