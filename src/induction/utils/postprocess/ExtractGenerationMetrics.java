package induction.utils.postprocess;

import induction.utils.postprocess.ExtractGenerationMetricsOptions.TypeOfInput;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import static induction.utils.postprocess.ExtractGenerationMetricsOptions.METRICS;
/**
 *
 * @author sinantie
 */
public class ExtractGenerationMetrics
{
    ExtractGenerationMetricsOptions opts;
    Map<String, double[]> results;
    
    public ExtractGenerationMetrics(ExtractGenerationMetricsOptions opts)
    {
        this.opts = opts;
        results = new HashMap<String, double[]>();
    }

    public void execute()
    {
        try
        {
            double[][] scores1 = processFile(opts.inputFile1, opts.inputFile1Type);
            double[][] scores2 = processFile(opts.inputFile2, opts.inputFile2Type);
            if(scores1 == null || scores2 == null)
            {
                System.err.println("Error processing files!");
            }
            else
            {
                FileOutputStream fos = new FileOutputStream(opts.outputFile);
                writeToFile(scores1, fos);
                writeToFile(scores2, fos);
                fos.close();
            }            
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    private double[][] processFile(String file, TypeOfInput typeOfInput) throws Exception
    {
        File f = new File(file);
        if(!f.exists())
            throw new FileNotFoundException();
        double[][] scores = parseScores(f, typeOfInput);
        if (sanityCheck(scores))
            return scores;
        else
            return null;
    }        

    private double[][] parseScores(File file, TypeOfInput typeOfInput) throws Exception
    {
        if(typeOfInput == TypeOfInput.percy)
            return parseScoresPercy(file);
        else
            return parseScoresGabor(file);
    }
    
    /**
     * Parse the scores from a standard mt_eval xml file, as it is being output by our systems.
     * The structure goes like <doc docid="..."><p><seg feature_1=... feature_2=...>text</seg></p></doc>
     * @param file
     * @return
     * @throws Exception 
     */
    private double[][] parseScoresPercy(File file) throws Exception
    {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(file);
        doc.getDocumentElement().normalize();        
        NodeList nodeList = doc.getElementsByTagName("doc");
        double[][] scores = new double[METRICS.length][nodeList.getLength()];
        for(int i = 0; i < scores.length; i++) // fill everything with -1 for sanity check
        {
            Arrays.fill(scores[i], -1);
        }
        for (int d = 0; d < nodeList.getLength(); d++)
        {
            Node docNode = nodeList.item(d);                        
            if (docNode.getNodeType() == Node.ELEMENT_NODE)
            {
                Element docElement = (Element) docNode; 
                // get id from doc tag
                String id = docElement.getAttribute("docid");
                // get seg node
                Element segElement = (Element) docElement.getElementsByTagName("seg").item(0);
                for(int i = 0; i < METRICS.length; i++)
                {
                    scores[i][d] = Double.valueOf(
                            segElement.getAttribute(METRICS[i]));
                }
            } // if
            
        } // for
        return scores;
    }

    private double[][] parseScoresGabor(File file) throws Exception
    {
        return null;
    }
    
    private boolean  sanityCheck(double[][] scores) throws Exception
    { // very inefficient!!
        for(int i = 0; i < scores.length; i++)
        {
            for(int j = 0; j < scores[i].length; j++)
            {
                if(scores[i][j] == -1)
                    return false;
            }
        }
        return true;
    }
    
    private void writeToFile(double[][] scores, FileOutputStream fos) throws IOException
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
    
    class Example {
        String id;
        double[] scores;

        public Example(String id, double[] scores)
        {
            this.id = id;
            this.scores = scores;
        }

        @Override
        public boolean equals(Object obj)
        {
            assert obj instanceof Example;
            return (this.id == null ? ((Example)obj).id == null : this.id.equals(((Example)obj).id));
        }

        @Override
        public int hashCode()
        {
            int hash = 5;
            hash = 19 * hash + (this.id != null ? this.id.hashCode() : 0);
            return hash;
        }
        
        
    }
}
