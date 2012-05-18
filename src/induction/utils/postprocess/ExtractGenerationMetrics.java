package induction.utils.postprocess;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Arrays;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author sinantie
 */
public class ExtractGenerationMetrics
{
    final static int NUM_OF_METRICS = 4;
    final static String[] METRICS = {"bleu", "bleu_modified", "meteor", "ter"};
    String inputFile1, inputFile2, outputFile;

    public ExtractGenerationMetrics(String inputFile1, String inputFile2, String outputFile)
    {
        this.inputFile1 = inputFile1;
        this.inputFile2 = inputFile2;
        this.outputFile = outputFile;
    }

    public void execute()
    {
        try
        {
            File f = new File(inputFile1);
            if(!f.exists())
                throw new FileNotFoundException();
            double[][] scores1 = parseScores(f);
            sanityCheck(scores1);

            f = new File(inputFile2);
            if(!f.exists())
                throw new FileNotFoundException();
            double[][] scores2 = parseScores(f);
            sanityCheck(scores2);

            FileOutputStream fos = new FileOutputStream(outputFile);
//            fos.write((inputFile1 + "\n").getBytes());
            writeToFile(scores1, fos);
//            fos.write(("\n" + inputFile2 + "\n").getBytes());
            writeToFile(scores2, fos);
            fos.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    private void writeToFile(double[][] scores, FileOutputStream fos) throws Exception
    {
        for(int i = 0; i < scores.length; i++)
        {
            for(int j = 0; j < scores[i].length; j++)
            {
                fos.write((String.valueOf(scores[i][j]).toString() + " ").getBytes());
            }
            fos.write("\n".getBytes());
        }

    }

    private void sanityCheck(double[][] scores) throws Exception
    { // very inefficient!!
        for(int i = 0; i < scores.length; i++)
        {
            for(int j = 0; j < scores[i].length; j++)
            {
                if(scores[i][j] == -1)
                    throw new IllegalArgumentException();
            }
        }
    }

    private double[][] parseScores(File file) throws Exception
    {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(file);
        doc.getDocumentElement().normalize();
        NodeList nodeList = doc.getElementsByTagName("seg");

        double[][] scores = new double[NUM_OF_METRICS][nodeList.getLength()];
        for(int i = 0; i < scores.length; i++) // fill everything with 1 for sanity check
        {
            Arrays.fill(scores[i], -1);
        }

        for (int s = 0; s < nodeList.getLength(); s++)
        {
            Node segNode = nodeList.item(s);
            if (segNode.getNodeType() == Node.ELEMENT_NODE)
            {
                Element segElement = (Element) segNode;
                for(int i = 0; i < NUM_OF_METRICS; i++)
                {
                    scores[i][s] = Double.valueOf(
                            segElement.getAttribute(METRICS[i]));
                }
            }
        }
        return scores;
    }

    public static void main(String[] args)
    {
//        args = new String[3];
//        args[0] = "results/output/weatherGov/generation/"
//                + "20-best_reordered_eventTypes_exact_wordLength_cond_null/stage1.tst.xml";
//        args[1] =  "results/output/weatherGov/generation/"
//                + "20-best_reordered_eventTypes_exact_wordLength_cond_null_bigrams/stage1.tst.xml";
//        args[2] = "results/output/weatherGov/generation/";
        
        if(args.length < 3)
        {
            System.out.println("Usage: [inputFile1 inputFile2 outputFile]");
            System.exit(1);
        }
        ExtractGenerationMetrics pgf = new ExtractGenerationMetrics(args[0], args[1], args[2]);
        pgf.execute();
    }   
}
