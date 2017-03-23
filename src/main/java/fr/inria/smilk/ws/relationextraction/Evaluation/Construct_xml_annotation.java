package fr.inria.smilk.ws.relationextraction.Evaluation;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import fr.inria.smilk.ws.relationextraction.Renco;
import fr.inria.smilk.ws.relationextraction.bean.Token;
import fr.inria.smilk.ws.relationextraction.util.ListFilesUtil;
import fr.inria.smilk.ws.relationextraction.util.openNLP;
import it.uniroma1.lcl.babelnet.InvalidBabelSynsetIDException;
import org.apache.xpath.SourceTree;
import org.jdom.*;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.*;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import static fr.inria.smilk.ws.relationextraction.ExtractionHelper.elementToToken;

/**
 * Created by dhouib on 23/11/2016.
 */
public class Construct_xml_annotation {

    static String i="s0";
    static String folder = "C:/Users/dhouib/Desktop/SMILK_project_devpt/RelationExtractionSMILK/src/resources/corpus_test2";
    static File folder1 = new File("C:/Users/dhouib/Desktop/SMILK_project_devpt/RelationExtractionSMILK/src/resources/corpus_test2");

    static Element racine = new Element("Sentences");
    static org.jdom.Document document = new Document(racine);

    // read corpus Files
    public static List<String> readCorpus(String folderName, File folder1) throws IOException {
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
            String line = "";
            //Create the file reader
            FileInputStream is = new FileInputStream(folderName + "/" + file);

            InputStreamReader isr = new InputStreamReader(is, Charset.forName("utf-8"));

            fileReader = new BufferedReader(isr);

            while ((line = fileReader.readLine()) != null) {
                if (line.trim().length() > 1) {
                    String[] sentences = opennlp.senenceSegmentation(line);
                    if (sentences != null) {
                        for (String sent : sentences) {
                            if (sent.length() > 0) {
                                lines.add(sent);
                            }
                        }
                    }
                }
            }
        }
        return lines;
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

    private static void startTools(String line, String input) throws ParserConfigurationException, IOException, SAXException, InvalidBabelSynsetIDException, ClassNotFoundException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        StringBuilder xmlStringBuilder = new StringBuilder();
        xmlStringBuilder.append(input);
        System.out.println(input);
        ByteArrayInputStream in = new ByteArrayInputStream(xmlStringBuilder.toString().getBytes("UTF-8"));
        org.w3c.dom.Document doc = builder.parse(in);
        doc.getDocumentElement().normalize();
        NodeList nSentenceList = doc.getElementsByTagName("sentence");
        //searchEN(line, nSentenceList);
        writeXML(line);
    }

    private static void searchEN(String line, NodeList nSentenceList) throws IOException, ClassNotFoundException {
        String newline = line;
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
                                if (xElement.getAttribute("type").equalsIgnoreCase("product") /*|| xElement.getAttribute("type").equalsIgnoreCase("division")
                                        || xElement.getAttribute("type").equalsIgnoreCase("group") || xElement.getAttribute("type").equalsIgnoreCase("brand")
                                        || xElement.getAttribute("type").equalsIgnoreCase("range")*/) {
                                    Token subjectToken = elementToToken(xElement);
                                    String form = subjectToken.getForm();


                                    String newVersion = "s" + (Integer.parseInt(i.substring(1,i.length()))+1);
                                    //writeXML(line,form,newVersion);
                                    i=newVersion;
                                    System.out.println("i: "+i);
                                }
                        }
                    }
                }
            }
        }
    }

    private static void writeXML(String line/*, String form, String i*/) {
        Element sentence = new Element("sentence");
        racine.addContent(sentence);
        String id = i;
        String newVersion_sentence = "s" + (Integer.parseInt(id.substring(1,id.length()))+1);

        Attribute identifiant = new Attribute("id", id);
        i=newVersion_sentence;
        sentence.setAttribute(identifiant);

        Element text=new Element("text");
        text.setText(line);
        sentence.addContent(text);

        Element Relations = new Element("Relations");
        sentence.addContent(Relations);
        Element relation = new Element("Relation");
        Relations.addContent(relation);
        String id_relation="r0";
        String newVersion = "r" + (Integer.parseInt(id_relation.substring(1,id_relation.length()))+1);
        Attribute identifiant_relation = new Attribute("id", newVersion);
        id_relation=newVersion;
        relation.setAttribute(identifiant_relation);


        Element subject = new Element("subject");
        subject.setText("test");
        relation.addContent(subject);

        Element predicate = new Element("predicate");
        predicate.setText("hasFragranceCreator");
        relation.addContent(predicate);

        Element object = new Element("object");
        object.setText("test");
        relation.addContent(object);

        affiche();
        enregistre("manuel_annotation.xml");
    }


    public static void main(String[] args) throws Exception {
        List<String> Lines=  readCorpus( folder,  folder1);

        for (String line:Lines) {
            Renco renco = new Renco();
            startTools(line, renco.rencoByWebService(line));
        }



    }

    static void affiche()
    {
        try
        {
            XMLOutputter sortie = new XMLOutputter(Format.getPrettyFormat());
            sortie.output(document, System.out);
        }
        catch (java.io.IOException e){}
    }

    static void enregistre(String fichier)
    {
        try
        {
            XMLOutputter sortie = new XMLOutputter(Format.getPrettyFormat());
            sortie.output(document, new FileOutputStream(fichier));
        }
        catch (java.io.IOException e){}
    }
}
