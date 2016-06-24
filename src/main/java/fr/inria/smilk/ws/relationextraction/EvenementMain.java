package fr.inria.smilk.ws.relationextraction;

import fr.inria.smilk.ws.relationextraction.renco.renco_simple.RENCO;
import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by dhouib on 22/06/2016.
 */
public class EvenementMain {
    static SAXBuilder saxBuilder = new SAXBuilder();
    static List<String> listVerbLemma = new ArrayList<>();
    static List<String> listNounLemma = new ArrayList<>();
    //liste des verbes et de leurs nominalisations décrivant des actions
    static File xmlFile = new File("C:/Users/dhouib/Desktop/SMILK_project_devpt/RelationExtractionSMILK/src/main/resources/input/Verbaction-1.0.xml");

    public static void main(String[] args) throws Exception {
        String s = null;
        RENCO renco = new RENCO();
        //dataSet
        Scanner sc = new Scanner(new File("C:/Users/dhouib/Desktop/SMILK_project_devpt/RelationExtractionSMILK/src/main/resources/input/RachatEntrepriseMarqueText.txt"));
        while (sc.hasNextLine()) {
            s += sc.nextLine();
        }
        sc.close();
        extractVerbAction();
        extractDOMparser(renco.rencoByWebService(s), listVerbLemma,listNounLemma );
        extractMontant (s);
    }

    //lire le vebAction.xml
    public static void extractVerbAction() throws JDOMException, IOException {
        List<String> sentences = new ArrayList<>();

        org.jdom2.Document document = (org.jdom2.Document) saxBuilder.build(xmlFile);
        //Définir l'element root du fichier XML
        Element rootElement = document.getRootElement();
        List<Element> listCouples = rootElement.getChildren("couple");

        for (Element element : listCouples) {
            listVerbLemma.add(element.getChild("verb").getChild("lemma").getValue());
            listNounLemma.add(element.getChild("noun").getChild("lemma").getValue());
        }

   }

    public static void extractEN (String input){


    }

    public static void extractMontant(String input){
        String regex = "((montant).*(euros))";
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher matcher = pattern.matcher("un montant de 3,2 millions d'euros. La transaction");
        if (matcher.find()) {
            System.out.println ("\n montant: "+ matcher.group(0));
        }
    }
    //lire la sortie de renco
    public static void extractDOMparser(String input, List<String> listVerbLemma, List<String> listNounLemma) throws JDOMException, IOException {
        //lire la sortie de renco
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            StringBuilder xmlStringBuilder = new StringBuilder();
            xmlStringBuilder.append(input);
            System.out.println("****************** " + input);

///Exemple http://www.tutorialspoint.com/java_xml/java_dom_parse_document.htm
            ByteArrayInputStream in = new ByteArrayInputStream(xmlStringBuilder.toString().getBytes("UTF-8"));
            Document doc = builder.parse(in);
            doc.getDocumentElement().normalize();
            System.out.println("Root element :"
                    + doc.getDocumentElement().getNodeName());

            NodeList nList = doc.getElementsByTagName("token");
            System.out.println("----------------------------");
            for (int temp = 0; temp < nList.getLength(); temp++) {
                Node nNode = nList.item(temp);
                System.out.println("\nCurrent Element :" + nNode.getNodeName());
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    org.w3c.dom.Element eElement = (org.w3c.dom.Element) nNode;
                    if (eElement.hasAttribute("pos")) {
                        System.out.println("pos: " + eElement.getAttribute("pos"));

                        if ((eElement.getAttribute("pos").toString().contains("NC")) || (eElement.getAttribute("type").toString().contains("VINF"))) {

                            System.out.println("pos : " + eElement.getAttribute("pos"));
                            String lemma = (eElement.getAttribute("lemma"));
                            System.out.println("lemma: " + lemma);

                            if ((listVerbLemma.contains(lemma))||(listNounLemma.contains(lemma))){
                                System.out.println (" ***************événement");
                            }
                            else
                            {
                                System.out.print("pas de detection d'evenement");
                            }
                        }


                    }
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }


    }

}

