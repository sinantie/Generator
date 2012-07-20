package induction.utils.postprocess;

import induction.utils.postprocess.ExtractGenerationMetricsOptions.TypeOfPath;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import induction.Utils;

import java.util.LinkedHashMap;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import induction.utils.postprocess.ExtractGenerationMetricsOptions.TypeOfInput;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import static induction.utils.postprocess.ExtractGenerationMetricsOptions.METRICS;
import static induction.utils.postprocess.ExtractGenerationMetricsOptions.PAIRS;
/**
 *
 * @author sinantie
 */
public class ExtractGenerationMetrics
{
    ExtractGenerationMetricsOptions opts;
    Map<String, Example> results;
    
    public ExtractGenerationMetrics(ExtractGenerationMetricsOptions opts)
    {
        this.opts = opts;
        results = new LinkedHashMap<String, Example>();
    }

    public void execute()
    {
        try
        {
            if(opts.inputFile1TypeOfPath == TypeOfPath.file)
                processFile(opts.inputFile1, opts.inputFile1Type);
            else
            {
                for(String inputFile : Utils.readLines(opts.inputFile1))
                    processFile(inputFile, opts.inputFile1Type);
            }
            if(opts.inputFile2TypeOfPath == TypeOfPath.file)
                processFile(opts.inputFile2, opts.inputFile2Type);
            else
            {
                for(String inputFile : Utils.readLines(opts.inputFile2))
                    processFile(inputFile, opts.inputFile2Type);
            }
//            if(scores1 == null || scores2 == null)
            if(sanityCheck(opts.trimSize))
            {
                FileOutputStream fos = new FileOutputStream(opts.outputFile);
                writeToFile(results, fos);                
                fos.close();                
            }
            else
            {
                System.err.println("Error processing files!");
            }    
            if(opts.calculateStatSig)
            {
                Process exec = Runtime.getRuntime().exec("./wilcoxon_octave.sh " + opts.outputFile);
                BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                    exec.getInputStream()));
                StringBuilder str = new StringBuilder();
                String line = "";
                while((line = in.readLine()) != null)
                {
                    str.append(line).append("\n");
                }
                in.close();
                Utils.write(opts.outputFile + ".stat_sig", str.toString());
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    private void processFile(String file, TypeOfInput typeOfInput) throws Exception
    {
        File f = new File(file);
        if(!f.exists())
            throw new FileNotFoundException(file + " not found!");
        parseScores(f, typeOfInput);        
    }        

    private void parseScores(File file, TypeOfInput typeOfInput) throws Exception
    {
        if(typeOfInput == TypeOfInput.percy)
            parseScoresPercy(file);
        else
            parseScoresGabor(file);
    }
    
    /**
     * Parse the scoresPerModel from a standard mt_eval xml file, as it is being output by our systems.
     * The structure goes like <doc docid="..."><p><seg feature_1=... feature_2=...>text</seg></p></doc>
     * @param file
     * @throws Exception 
     */
    private void parseScoresPercy(File file) throws Exception
    {        
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
        doc.getDocumentElement().normalize();        
        NodeList nodeList = doc.getElementsByTagName("doc");        
//        double[][] scores = new double[METRICS.length][nodeList.getLength()];
//        for(int i = 0; i < scores.length; i++) // fill everything with -1 for sanity check
//        {
//            Arrays.fill(scores[i], -1);
//        }
        for (int d = 0; d < nodeList.getLength(); d++)
        {
            double[] scores = new double[METRICS.length];
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
                    scores[i] = Double.valueOf(segElement.getAttribute(METRICS[i]));
                }
                if(results.containsKey(id))
                    results.get(id).add(scores);
                else
                    results.put(id, new Example(scores));
            } // if            
        } // for        
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
    
    private boolean sanityCheck(boolean trimSize)
    {
        Iterator<Example> it = results.values().iterator();        
        while(it.hasNext())
        {
            Example e = it.next();
            if(e.size() != PAIRS) // we compare two pairs of models each time. TO-DO: extend for more than 2
            {
                if(trimSize)
                    it.remove();
                else
                    return false;
            } // if
        }  // while
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
    
    private void writeToFile(Map<String, Example> results, FileOutputStream fos) throws IOException
    {        
        for(Example e : results.values())        
        {            
            fos.write((e + "\n").getBytes());
        } // for        
    }
    
    class Example {
        List<double[]> scoresPerModel;

        public Example(double[] scores)
        {
            scoresPerModel = new ArrayList();
            scoresPerModel.add(scores);
        }                
        
        public void add(double[] scores)
        {
            scoresPerModel.add(scores);
        }

        public List<double[]> getScoresPerModel()
        {
            return scoresPerModel;
        }
                
        public int size()
        {
            return scoresPerModel.size();
        }

        @Override
        public String toString()
        {
            StringBuilder str = new StringBuilder();
            for(double[] scores : scoresPerModel)
                for(double score : scores)
                    str.append(score).append(" ");
            return str.toString();
        }                
    }
}
