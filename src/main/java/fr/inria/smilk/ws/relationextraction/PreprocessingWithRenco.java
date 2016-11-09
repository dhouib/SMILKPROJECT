package fr.inria.smilk.ws.relationextraction;

import fr.inria.smilk.ws.relationextraction.bean.Token;
import fr.inria.smilk.ws.relationextraction.util.ListFilesUtil;
import fr.inria.smilk.ws.relationextraction.util.openNLP;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import static fr.inria.smilk.ws.relationextraction.ExtractionHelper.elementToToken;

/**
 * Created by dhouib on 13/10/2016.
 */
public class PreprocessingWithRenco {

    static File folder = new File("C:/Users/dhouib/Desktop/SMILK_project_devpt/RelationExtractionSMILK/src/resources/input/test/");
    static String folder_name = "C:/Users/dhouib/Desktop/SMILK_project_devpt/RelationExtractionSMILK/src/resources/input/test/";

    public static void main(String[] args) throws Exception {
       /* Renco renco = new Renco();
       String line="Alors qu'il a perdu sa place de premier parfum de la division Luxe de L'Oréal au profit de La Vie est Belle de Lancôme, Amor Amor de Cacharel revient sur le devant de la scène en avril avec une édition Amor Amor In a flash.";
       String input= renco.rencoByWebService(line);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        StringBuilder xmlStringBuilder = new StringBuilder();
        xmlStringBuilder.append(input);
        System.out.println(input);
        ByteArrayInputStream in = new ByteArrayInputStream(xmlStringBuilder.toString().getBytes("UTF-8"));//"UTF-8"
        Document doc = builder.parse(in);
        doc.getDocumentElement().normalize();
        NodeList nSentenceList = doc.getElementsByTagName("sentence");
        for (int sent_temp = 0; sent_temp < nSentenceList.getLength(); sent_temp++) {
            Node nSentNode = nSentenceList.item(sent_temp);
            //StringBuilder builder = new StringBuilder();
            String sentence = line;
            NodeList nTokensList = nSentNode.getChildNodes();
            //parcourir l'arbre Renco
            for (int token_temp = 0; token_temp < nTokensList.getLength(); token_temp++) {
                Node nTokenNode = nTokensList.item(token_temp);
                if (nTokenNode instanceof org.w3c.dom.Element) {
                    NodeList nList = nTokenNode.getChildNodes();
                    Node nNode = nList.item(token_temp);
                    int y = 0;
                    for (int x = 0; x < nList.getLength(); x++) {
                        Node xNode = nList.item(x);
                        if (xNode instanceof org.w3c.dom.Element) {
                            org.w3c.dom.Element xElement = (org.w3c.dom.Element) xNode;
                            //Si XElement a un type (product, range, brand, division, group
                            if (xElement.hasAttribute("type") && !xElement.getAttribute("type").equalsIgnoreCase("not_identified"))
                                if (xElement.getAttribute("type").equalsIgnoreCase("product")||xElement.getAttribute("type").equalsIgnoreCase("division")
                                        ||xElement.getAttribute("type").equalsIgnoreCase("group")||xElement.getAttribute("type").equalsIgnoreCase("brand")
                                        ||xElement.getAttribute("type").equalsIgnoreCase("range")) {
                                    Token subjectToken = elementToToken(xElement);
                                    String form=subjectToken.getForm();
                                    String form_with_underscore=form.replaceAll("\\s", "_");
                                    System.out.println("form: "+ form_with_underscore);
                                    subjectToken.setForm(form_with_underscore);
                                    System.out.println("product: " + subjectToken.getForm());
                                }
                        }
                    }
                }
            }
        }*/


        try {
            readCorpus(folder_name, folder);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    //Sort Files Corpus
    private static void sortFiles(String[] filenames) {
        Arrays.sort(filenames, new Comparator<String>() {
            public int compare(String f1, String f2) {
                try {
                    int i1 = Integer.parseInt(f1.substring(0, f1.indexOf("L")).concat(f1.substring(f1.indexOf("l") + 1, f1.indexOf("."))));// = Integer.parseInt(f1.substring(0, f1_repalace.indexOf(".")));
                    int i2 = Integer.parseInt(f2.substring(0, f2.indexOf("L")).concat(f2.substring(f2.indexOf("l") + 1, f2.indexOf("."))));// = Integer.parseInt(f2.substring(0, f2_replace.indexOf(".")));

                    return i1 - i2;
                } catch (NumberFormatException e) {
                    throw new AssertionError(e);
                }
            }
        });
    }
    // fileContentCorpus = readFile(listOfFiles[i].getAbsolutePath(), "windows-1252");
    /*public static void readFiles() throws Exception {
        ListFilesUtil listFileUtil = new ListFilesUtil();
        listFileUtil.listFilesFromDirector(folder_name);
        File[] listOfFiles = folder.listFiles();
        List<String> files = listFileUtil.files;
        List<String> lines = new LinkedList<>();
         String fileContentCorpus;
        int j = 0;
        String[] filesArray = files.toArray(new String[]{});
        sortFiles(filesArray);
        StringBuilder fileContentBuilder = new StringBuilder();
        for (String file : filesArray) {
            System.out.println("Processing file #: " + file + ": " + j);
            j++;
            try {

                File fXmlFile = new File(folder_name + "/" + file);

              //  readXML(fXmlFile, file);
                int nbfile=0;
                  String toWrite="test";
                nbfile++;
                FileWriter writer = new FileWriter("C:/Users/dhouib/Desktop/SMILK_project_devpt/RelationExtractionSMILK/src/resources/output/out_renco/" + file, false);
                writer.write(toWrite);
                writer.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }*/


    public static void readCorpus(String folderName, File folder1) throws Exception {
        ListFilesUtil listFileUtil = new ListFilesUtil();
        listFileUtil.listFilesFromDirector(folderName);

        File[] listOfFiles = folder1.listFiles();
        List<String> files = listFileUtil.files;
        List<String> lines = new LinkedList<>();
        openNLP opennlp;
        opennlp = new openNLP();

        int i = 0;
        String[] filesArray = files.toArray(new String[]{});
        sortFiles(filesArray);
        //   sortByNumber(listOfFiles);
        for (String file : filesArray) {

            System.out.println("Processing file #: " + file + ": " + i);
            i++;
            BufferedReader fileReader = null;
            String line = "";  String newline=line;
            //Create the file reader
            FileInputStream is = new FileInputStream(folderName + "/" + file);

            InputStreamReader isr = new InputStreamReader(is, Charset.forName("utf-8"));

            fileReader = new BufferedReader(isr);

            while ((line = fileReader.readLine()) != null) {
               /* if (line.trim().length() > 1) {
                    String[] sentences = opennlp.senenceSegmentation(line);
                    if (sentences != null) {
                        for (String sent : sentences) {
                            if (sent.length() > 0) {
                                lines.add(sent);
                            }
                        }
                    }*/
                Renco renco = new Renco();
                String input= renco.rencoByWebService(line);
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                StringBuilder xmlStringBuilder = new StringBuilder();
                xmlStringBuilder.append(input);
                System.out.println(input);
                ByteArrayInputStream in = new ByteArrayInputStream(xmlStringBuilder.toString().getBytes("UTF-8"));//"UTF-8"
                Document doc = builder.parse(in);
                doc.getDocumentElement().normalize();
                NodeList nSentenceList = doc.getElementsByTagName("sentence");
                for (int sent_temp = 0; sent_temp < nSentenceList.getLength(); sent_temp++) {
                    Node nSentNode = nSentenceList.item(sent_temp);
                    //StringBuilder builder = new StringBuilder();
                    String sentence = line;
                    NodeList nTokensList = nSentNode.getChildNodes();
                    //parcourir l'arbre Renco
                    for (int token_temp = 0; token_temp < nTokensList.getLength(); token_temp++) {
                        Node nTokenNode = nTokensList.item(token_temp);
                        if (nTokenNode instanceof org.w3c.dom.Element) {
                            NodeList nList = nTokenNode.getChildNodes();
                            Node nNode = nList.item(token_temp);
                            int y = 0;
                            for (int x = 0; x < nList.getLength(); x++) {
                                Node xNode = nList.item(x);
                                if (xNode instanceof org.w3c.dom.Element) {
                                    org.w3c.dom.Element xElement = (org.w3c.dom.Element) xNode;
                                    //Si XElement a un type (product, range, brand, division, group
                                    if (xElement.hasAttribute("type") && !xElement.getAttribute("type").equalsIgnoreCase("not_identified"))
                                        if (xElement.getAttribute("type").equalsIgnoreCase("product")||xElement.getAttribute("type").equalsIgnoreCase("division")
                                                ||xElement.getAttribute("type").equalsIgnoreCase("group")||xElement.getAttribute("type").equalsIgnoreCase("brand")
                                                ||xElement.getAttribute("type").equalsIgnoreCase("range")) {
                                            Token subjectToken = elementToToken(xElement);
                                            String form=subjectToken.getForm();
                                            String form_with_underscore=form.replaceAll("\\s", "_");
                                            System.out.println("form: "+ form + " with underscor: "+ form_with_underscore);

newline=line;
                                            newline=newline.replace(form,form_with_underscore);


                                        }
                                }
                            }
                        }
                    }
                }

                System.out.println(newline);
            }
            FileWriter writer = new FileWriter("C:/Users/dhouib/Desktop/SMILK_project_devpt/RelationExtractionSMILK/src/resources/output/out_renco/" + file, false);
            writer.write(newline);
            writer.close();
            }
        }



}
