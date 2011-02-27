package induction.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author konstas
 */
public class RobocupToEventsConverter
{
    private String trainPath, goldPath, prefix;
    private Map<Integer, List<Integer>> examplesMap = new HashMap<Integer, List<Integer>>();
    private Map<Integer, String> mrlMap = new HashMap<Integer, String>();

    public RobocupToEventsConverter(String trainPath, String goldPath, String outputPath)
    {
        this.trainPath = trainPath;
        this.goldPath = goldPath;
        assert(new File(outputPath).exists());
        prefix = outputPath + trainPath.substring(trainPath.lastIndexOf("/")) + "-";
    }

    public void execute()
    {
        try
        {
            // parse train file first
            parseTrainFile(parseXmlFile(trainPath));
            System.out.println("Converting MRLs to Events...");
            long start = System.currentTimeMillis();
            parseGoldFile(parseXmlFile(goldPath));
            System.out.println("\n\nDone in " + (System.currentTimeMillis() - start) + "ms");
        }
        catch(Exception ioe)
        {
            ioe.printStackTrace();
        }
    }

    private Document parseXmlFile(String path) throws Exception
    {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(path);
        doc.getDocumentElement().normalize();
        return doc;
    }

    /**
     * Parse robocup training xml file. At the top of the file are
     * the examples with an id and a list of semids (id's of mrls).
     * Lower in the file are the actual mrls with an id and the actual content.
     * We first parse all the examples in a map and then all the mrls in another map.
     * @param doc
     */
    private void parseTrainFile(Document doc)
    {
         // Read example level first to get the example ids and mrl ids into the map
        NodeList nodeList = doc.getElementsByTagName("example");
        String id = "";
        for(int e = 0; e < nodeList.getLength(); e++)
        {
            Node exampleNode = nodeList.item(e);
            // snatch the example id
            if (exampleNode.getNodeType() == Node.ELEMENT_NODE)
            {
                id = ((Element)exampleNode).getAttribute("id");
            }

            // Parse the list of mrl ids into an arraylist of integers
            Node semIdNode = exampleNode.getChildNodes().item(3);
            if(semIdNode.getNodeType() == Node.ELEMENT_NODE)
            {
                String ar[] = semIdNode.getTextContent().trim().split(" ");
                List<Integer> list = new ArrayList(ar.length);
                for(String tok : ar)
                    list.add(Integer.valueOf(tok));
                examplesMap.put(Integer.valueOf(id), list);
            }
        } // for all examples

        // Read the mrls into an indexer
        nodeList = doc.getElementsByTagName("sem");
        for(int s = 0; s < nodeList.getLength(); s++)
        {
            Node semNode = nodeList.item(s);            
            if (semNode.getNodeType() == Node.ELEMENT_NODE)
            {
                // snatch the mrl id
                id = ((Element)semNode).getAttribute("id");
                // pick the mrl
                String mrl = ((Element)semNode).getTextContent().trim();
                mrlMap.put(Integer.valueOf(id), mrl);
            }
        }
    }

    /**
     * Parse robocup gold alignments xml file. This file contains example elements 
     * with an id, corresponding to the ids in the example map, an nl phrase and
     * an mrl corresponding to the nl, with an id that should match those in the mrlMap.
     * As we parse each example, we create the corresponding .text file with the nl/
     * Then we create the .events file with the corresponding events in the format
     * defined by Percy, captured from the exampleMap. Finally we naively create
     * a .align file matching the single line in the .text with the manually aligned
     * mrl in the xml file. We get it's id from the mrlMap.
     * @param doc
     */
    private void parseGoldFile(Document doc)
    {
         // Everything is in the example level
        NodeList nodeList = doc.getElementsByTagName("example");
        String id = "", nl = "", mrl = "";
        for(int e = 0; e < nodeList.getLength(); e++)
        {
            Node exampleNode = nodeList.item(e);
            // snatch the example id
            if (exampleNode.getNodeType() == Node.ELEMENT_NODE)
            {
                id = ((Element)exampleNode).getAttribute("id");
            }
            // Parse the nl
            Node nlIdNode = exampleNode.getChildNodes().item(1);
            if(nlIdNode.getNodeType() == Node.ELEMENT_NODE)
            {
                nl = nlIdNode.getTextContent().trim();
            }
            // Parse the ml
            Node mrlIdNode = exampleNode.getChildNodes().item(3);
            if(mrlIdNode.getNodeType() == Node.ELEMENT_NODE)
            {
                mrl = mrlIdNode.getTextContent().trim();
            }
            writeToFiles(id, nl, mrl);
        }
    }

    private void writeToFiles(String id, String nl, String mrl)
    {        
        List<Integer> mrlsIdList = examplesMap.get(Integer.valueOf(id));
        String out = "";
        if(mrlsIdList == null) // if gold standard not found in the original list
        {
            mrlsIdList = new ArrayList<Integer>(1);
            System.out.print("WARNING: Example " + id + " not found in the " +
                               "training set. Using gold standard only instead...");            
            // linearly search for the mrl's id in mrlMap
            for(Entry<Integer, String> entry : mrlMap.entrySet())
            {
                if(entry.getValue().equals(mrl))
                {
                    mrlsIdList.add(entry.getKey());
                    System.out.println("done!");
                    break;
                }                
            }
            if(mrlsIdList.isEmpty()) // if not found in mrlMap
            {
                out += String.format(".id:0\t.mrlId:UNKNOWN\t%s\n", processMrl(mrl));
                System.out.println("not found in the mrl list. Unknown mrl id!");
            }
        }
        List<String> listOfMrls = new ArrayList(mrlsIdList.size());
        int i = 0; 
        for(Integer mrlId : mrlsIdList)
        {
            // convert mrl to percy's style
            String currentMrl = mrlMap.get(mrlId);
            out += String.format(".id:%d\t.mrlId:%d\t%s\n", i++, mrlId, processMrl(currentMrl));
            listOfMrls.add(currentMrl); // store for easy matching in the .align procress
        }
        
        // Write .events file
        writeToFile(prefix + id + ".events", out);

        // Write .text file
        writeToFile(prefix + id + ".text", nl);

        // Write .align file
        out = "0 "; // Cheating: robocup has only one line per example
        
        out += mrlsIdList.isEmpty() ? "0" : listOfMrls.indexOf(mrl);
        writeToFile(prefix + id + ".align", out);
    }

    /**
     * Map the mrl from the robocup notation to Percy's standard
     * @param mrl
     * @return
     */
    private String processMrl(String mrl)
    {
        String out = "";
        String tokensArray[] = mrl.split("[ (,)]+");
        // first token is the mrl predicate, mapped to percy's eventType
        out += String.format(".type:%s", tokensArray[0]);
        // next tokens (if they exist) are the predicate arguments. We map
        // them to percy's categorical (@) fields assigning arbitrary names
        for(int i = 1; i < tokensArray.length; i++)
        {
            out += String.format("\t@arg%d:%s", i, tokensArray[i]);
        }
        return out;
    }
    private void writeToFile(String filename, String str)
    {
        try
        {
            FileOutputStream fos = new FileOutputStream(filename);
            fos.write(str.getBytes());
            fos.close();
        }
        catch(IOException ioe)
        {
            ioe.printStackTrace();
        }
    }

    public static void main(String[] args)
    {
        String trainPath = "../robocup-data/training/2004final-train";
        String goldPath = "../robocup-data/gold/2004final-gold";
        String outputPath = "../robocup-data/output/2004final-percy";
        RobocupToEventsConverter rtec = new RobocupToEventsConverter(
                trainPath, goldPath, outputPath);
        rtec.execute();
    }
}