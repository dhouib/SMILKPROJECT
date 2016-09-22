package preprocessing.construct_data;

import fr.inria.smilk.ws.relationextraction.util.ListFilesUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by dhouib on 26/08/2016.
 */
public class ConstructDataExpressBeaute {
    static File folder = new File("C:/Users/dhouib/Desktop/SMILK_project_devpt/RelationExtractionSMILK/src/resources/input/preprocessing_data/express_Beauté/");
    static String folder_name = "C:/Users/dhouib/Desktop/SMILK_project_devpt/RelationExtractionSMILK/src/resources/input/preprocessing_data/express_Beauté/";

    public static void main(String[] args) {
        try {
            readFiles();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //Sort Files Corpus
    private static void sortFiles(String[] filenames) {
        Arrays.sort(filenames, new Comparator<String>() {
            public int compare(String f1, String f2) {
                try {
                    int i1 = Integer.parseInt(f1.substring(0, f1.indexOf(".")));
                    int i2 = Integer.parseInt(f2.substring(0, f2.indexOf(".")));
                    return i1 - i2;
                } catch (NumberFormatException e) {
                    throw new AssertionError(e);
                }
            }
        });
    }

    public static void readFiles() throws Exception {
        ListFilesUtil listFileUtil = new ListFilesUtil();
        listFileUtil.listFilesFromDirector(folder_name);
        List<String> files = listFileUtil.files;
        int j = 0;
        String[] filesArray = files.toArray(new String[]{});
        sortFiles(filesArray);
        StringBuilder fileContentBuilder = new StringBuilder();
        for (String file : filesArray) {
            System.out.println("Processing file #: " + file + ": " + j);
            j++;
            try {

            File fXmlFile = new File(folder_name + "/" + file);
                readXML(fXmlFile);
                String toWrite=readXML(fXmlFile);
                String new_file= modify(file);
                FileWriter writer = new FileWriter("C:/Users/dhouib/Desktop/SMILK_project_devpt/RelationExtractionSMILK/src/resources/output/preprocessing_data/out_express_Beauty/" + new_file, false);
                writer.write(toWrite);
                writer.close();

        } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    public static String readXML(File fXmlFile){
        String normalText= new String();

        try {

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(fXmlFile);
        doc.getDocumentElement().normalize();
        System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
        NodeList nList = doc.getElementsByTagName("CONTENT");
        for (int temp = 0; temp < nList.getLength(); temp++) {

            Node nNode = nList.item(temp);

            System.out.println("\nCurrent Element :" + nNode.getNodeName());

            if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                Element eElement = (Element) nNode;
                int eElements_length= eElement.getElementsByTagName("TEXTE").getLength();
                for(int i=0; i<eElements_length; i++) {
                    String Text = eElement.getElementsByTagName("TEXTE").item(i).getTextContent();
                     normalText = normalText.concat(Text);
                    normalText = normalText.replaceAll("[\r\n]+", "");
                }
            }

        }
    } catch (Exception e) {
        e.printStackTrace();
    }
        return normalText ;
    }

    public static String modify(String file)
    {
        String new_file=new String();
        String ext = file.replace (".xml",".txt");
        new_file=ext;
        return new_file;
    }


}
