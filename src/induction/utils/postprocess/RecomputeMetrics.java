package induction.utils.postprocess;

import edu.berkeley.nlp.mt.BatchBleuModifiedScorer;
import edu.berkeley.nlp.mt.BatchBleuScorer;
import edu.cmu.meteor.Meteor;
import edu.cmu.meteor.scorer.MeteorScorer;
import edu.cmu.meteor.scorer.MeteorStats;
import fig.basic.IOUtils;
import induction.Utils;
import induction.problem.event3.TERMetric;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
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
 * Takes a file in SGML format and re-runs all text-related performance metrics,
 * against the original files. Optionally creates an SGML with the reference text.
 *
 * @author konstas
 */
public class RecomputeMetrics
{
    final static int NUM_OF_METRICS = 4;
    final static String[] METRICS = {"bleu", "bleu_modified", "meteor", "ter"};
    int modifiedBleuScoreSize = 4;
    BatchBleuScorer bleuScorer, bleuModifiedScorer;    
    MeteorScorer meteorScorer;
    MeteorStats meteorAggStats;
    TERMetric terScorer;
    double TERTotalEdits = 0.0, TERTotalWords = 0.0;

    Map<String, String> referenceExamples = new HashMap<String, String>();
    String referenceExamplesPath, fileExtension, inputFilePath, refFilePath;

    public RecomputeMetrics(String referenceExamplesPath, String fileExtension,
                            String inputFilePath)
    {
        this.referenceExamplesPath = referenceExamplesPath;
        this.fileExtension = fileExtension;
        this.inputFilePath = inputFilePath;        
        bleuScorer = new BatchBleuScorer();
        bleuModifiedScorer = new BatchBleuModifiedScorer(modifiedBleuScoreSize);
        meteorScorer = Meteor.MeteorScorerFactory();
        meteorAggStats = new MeteorStats();
        terScorer = new TERMetric();
    }

     /**
     * Parses a file that contains a list of the files to be processed
     * @param path
     */
    private void addList(String path)
    {
        for(String line : Utils.readLines(path))
        {
            addPath(line);
        } // for
    }

    /**
     * Recursively processes files under the <code>path</code> directory
     * @param path
     */
    private void addPath(String path)
    {
        File file = new File(path);
        if(file.isDirectory())
        {
            for(String fileStr : Utils.sortWithEmbeddedInt(file.list()))
            {
                addPath(path + "/" + fileStr);
            } // for
        } // if
        else
        {
            if(!file.getName().endsWith(fileExtension))
            {
                path = IOUtils.stripFileExt(path) + "." + fileExtension;
            }
            try
            {
                referenceExamples.put(path, readFile(path));
            }
            catch(IOException ioe)
            {
                System.out.println("Error opening file "+ path + " " + ioe.getMessage());

            }
        }
    }

    /**
     * Read a text file into a single line in lowercase
     * @param path the file path to read
     * @return a single line in lowercase
     * @throws IOException
     */
    private String readFile(String path) throws IOException
    {
        BufferedReader fin = new BufferedReader(new FileReader(path));
        String line = "", out = "";
        while((line = fin.readLine()) != null)
        {
            out +=  line + " ";
        }
        fin.close();
        return out.toLowerCase().trim();
    }

    private void parseSGMLFile(FileOutputStream fos, FileOutputStream fosRef) throws Exception
    {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(inputFilePath);
        doc.getDocumentElement().normalize();

        // Read doc level first to get the docid, i.e. the filename of the predicted example
        NodeList nodeDocList = doc.getElementsByTagName("doc");
        String keyPath = "", predStr = "", trueStr = "";

        for (int d = 0; d < nodeDocList.getLength(); d++)
        {            
            Node docNode = nodeDocList.item(d);
            if (docNode.getNodeType() == Node.ELEMENT_NODE)
            {
                keyPath = ((Element)docNode).getAttribute("docid");
            }

            // Parse the predicted text from the 'seg' element
            Node segNode = docNode.getChildNodes().item(1);
            if (segNode.getNodeType() == Node.ELEMENT_NODE)
            {
                Element segElement = (Element) segNode;
                // raw text is the predicted string
                predStr = segElement.getTextContent().trim();
                // match the reference string based on the docid, i.e. path
                trueStr = referenceExamples.get(keyPath);

                // write SGML entry with computed metrics (text-related only) to FileOutputStream
                if(trueStr !=null)
                {
                    fos.write(computeSGMLEntry(keyPath, predStr, trueStr).getBytes());
                    if(refFilePath != null)
                        fosRef.write(referenceSGMLEntry(keyPath, trueStr).getBytes());
                }
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

    private String referenceSGMLEntry(String docid, String trueStr)
    {
        return "<doc docid=\"" + docid  + "\" genre=\"nw\">\n" +
                     "<p>\n<seg id=\"1\">" +
                     trueStr +
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

    public void execute()
    {
        // get all reference examples in the map
        System.out.print("Reading files from " + referenceExamplesPath + "...");
        addList(referenceExamplesPath);
//        referenceExamples.put("data/weather-data-full/data/texas/gatesville/2009-02-09-0.text",
//                              "partly cloudy , with a low around 56 . south southwest wind around 10 mph .");
        System.out.print("done\nRecomputing metrics...");

        // open output stream(s) and write header data
        try
        {
            FileOutputStream fosRecomputed = new FileOutputStream(inputFilePath + ".recomputed");
            fosRecomputed.write(("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<mteval>\n" +
                    "<tstset setid=\"recomputed\" srclang=\"English\" " +
                    "trglang=\"English\" sysid=\"sample_system\">\n").getBytes());

            FileOutputStream fosRef = null;
            if(refFilePath != null)
            {
                fosRef = new FileOutputStream(refFilePath);
                fosRef.write(("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<mteval>\n" +
                    "<refset setid=\"reference\" srclang=\"English\" " +
                    "trglang=\"English\" refid=\"ref\">\n").getBytes());
            }
            // parse SGML file
            parseSGMLFile(fosRecomputed, fosRef);
            
            fosRecomputed.write("</tstset>\n</mteval>".getBytes());
            fosRecomputed.close();
            if(refFilePath != null)
            {
                fosRef.write("</refset>\n</mteval>".getBytes());
                fosRef.close();
            }
            System.out.println("done\n\n" + output());
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }       
    }

    public void setRefFilePath(String refFilePath)
    {
        this.refFilePath = refFilePath;
    }

    public void setModifiedBleuScoreSize(int modifiedBleuScoreSize)
    {
        this.modifiedBleuScoreSize = modifiedBleuScoreSize;
    }

    public static void main(String[] args)
    {
        String referenceExamplesPath = "", inputFile = "", refFile = null;
        if(args.length < 1)
        {
            referenceExamplesPath = "gaborLists/genEvalListPathsGabor";
            inputFile = "results/output/weatherGov/generation/15-best_reordered_eventTypes_exact_wordLength_hyp_recomb/stage1.tst.xml";
            refFile = "results/output/weatherGov/generation/15-best_reordered_eventTypes_exact_wordLength_hyp_recomb/stage1.ref.xml";
        }
        else
        {
            referenceExamplesPath = args[0];
            inputFile = args[1];
            refFile = args[2];
        }
        String fileExtension = "text";

        RecomputeMetrics rm = new RecomputeMetrics(referenceExamplesPath,
                                                   fileExtension, inputFile);
        if(refFile != null)
            rm.setRefFilePath(refFile);
        rm.setModifiedBleuScoreSize(3);
        rm.execute();
    }
}
