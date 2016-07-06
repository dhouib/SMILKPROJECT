package fr.inria.smilk.ws.relationextraction;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Literal;
import fr.inria.smilk.ws.relationextraction.renco.renco_simple.RENCO;
import fr.inria.smilk.ws.relationextraction.util.ListFilesUtil;
import fr.inria.smilk.ws.relationextraction.util.openNLP;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Created by dhouib on 01/07/2016.
 */
public class HasTargetRelationExtraction {

    //stocker les (product, brand)
    static HashMap<String, String> hmap = new HashMap<String, String>();
    static HashMap<String, String> hmap2 = new HashMap<String, String>();
    //stocker les patterns
    static Set<String> patterns = new HashSet<>();
    static Set<String> patternshasFragranceCreator=new HashSet<>();

    public static void main(String[] args) throws Exception {
        String folder = "C:/Users/dhouib/Desktop/SMILK_project_devpt/smilk_relation_extraction/src/main/resources/input/hasTargetTest";
        constructSentence(folder);
    }

    public static void constructSentence(String folder) throws Exception {
        RENCO renco = new RENCO();
        List<String> lines = readCorpus(folder);
        //System.out.println("Size of data: " + lines.size());
        // System.out.print("data: "+lines);
        int i = 0;
        for (String line : lines) {
            i++;
            if (line.trim().length() > 1) {
                System.out.println("\n line: "+i+" " + line);
                hasTargetRelation(line,renco.rencoByWebService(line));
            }
        }
    }
    // méthode qui permet d'extraire les patterns de la relation belongsToBrand à partir
    // de l'abstract de DBpedia ainsi que les parfums et les marques
    private static void hasTargetRelation(String line,String input) throws Exception {
        RENCO renco = new RENCO();
        String cible=new String();
        String queryString =
                "PREFIX p: <http://dbpedia.org/property/>"+
                        "PREFIX dbpedia: <http://dbpedia.org/resource/>"+
                        "PREFIX category: <http://dbpedia.org/resource/Category:>"+
                        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"+
                        "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>"+
                        "PREFIX geo: <http://www.georss.org/georss/>"+
                        "PREFIX dbpedia-owl: <http://dbpedia.org/ontology/>"+
                        "PREFIX prop-fr: <http://fr.dbpedia.org/property/>"+
                        "select  ?parfum_name ?cible\n" +
                        "where {\n" +
                        "?parfum_list rdfs:label \"Liste de parfums\" @fr.\n" +
                        "?parfum_list dbpedia-owl:wikiPageWikiLink ?parfum.\n" +
                        "?parfum rdfs:label ?parfum_name.\n"+
                        "?parfum prop-fr:cible  ?cible.\n" +
                        "FILTER ( LANG(?cible) = \"fr\" )\n" +
                        "} ";

        Query query = QueryFactory.create(queryString);
        QueryExecution qexec = QueryExecutionFactory.sparqlService("http://fr.dbpedia.org/sparql", query);
        try {
            ResultSet results = qexec.execSelect();
            while (results.hasNext() ) {
                QuerySolution soln = results.nextSolution();
                Literal cibleDBpedia = soln.getLiteral("cible");
                cible = cibleDBpedia.toString();

                int indexcible = 0,indexend = 0;
                if ((cible.contains("("))) {
                    indexcible = cible.indexOf("(");
                    cible = cible.substring(0,indexcible);
                }
                else if(cible.contains("@")) {
                    indexcible = cible.indexOf("@");
                    cible = cible.substring(0,indexcible);
                }
//corriger ça
                if(cible.contains("Public")) {

                    indexend = "Public".length();
                    cible = cible.substring(indexend,cible.length());
                }
                patterns.add(cible.trim());

            }
        } finally {
            qexec.close();
        }
        //affichage(patterns);
        verifyENPatternsBpedia(line, renco.rencoByWebService(line), patterns);

    }


    private static void affichage (Set<String> patterns ) throws Exception {

        for (String i : patterns) {
            System.out.println("Patterns:" + i);
        }
    }

    private static void verifyPatternsDBpedia (String input,Set<String> patterns) {
        for (String i : patterns) {
            if (input.contains(i)) {
                //System.out.println("Pattern trouvé:" + i);
            }
        }
    }

    private static void verifyENPatternsBpedia(String line, String input,Set<String> patterns ) throws ParserConfigurationException, IOException, SAXException {

        List<String> sentences = new ArrayList<>();
        String inputRenco = null;
        String brand = null, product = null;
        String index_product = null;

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        StringBuilder xmlStringBuilder = new StringBuilder();
        xmlStringBuilder.append(input);
///Exemple http://www.tutorialspoint.com/java_xml/java_dom_parse_document.htm
        ByteArrayInputStream in = new ByteArrayInputStream(xmlStringBuilder.toString().getBytes("UTF-8"));
        Document doc = builder.parse(in);
        doc.getDocumentElement().normalize();
        NodeList nSentenceList = doc.getElementsByTagName("sentence");
        for (int sent_temp = 0; sent_temp < nSentenceList.getLength(); sent_temp++) {
            Node nSentNode = nSentenceList.item(sent_temp);
            NodeList nTokensList = nSentNode.getChildNodes();
            //walk on the tokens node
            for (int token_temp = 0; token_temp < nTokensList.getLength(); token_temp++) {
                Node nTokenNode = nTokensList.item(token_temp);
                NodeList nList = nTokenNode.getChildNodes();
                Node nNode = nList.item(token_temp);
                int x = 0, y = 0;
                while (x < nList.getLength()) {
//System.out.println("x: "+x);
                    Node xNode = nList.item(x);

                    if (xNode instanceof Element) {

                        Element xElement = (Element) xNode;
                        Set set = hmap.entrySet();
                        Iterator iterator = set.iterator();

                        // if (xElement.hasAttribute("type") || xElement.getAttribute("pos").equalsIgnoreCase("NPP")) {
                        if ((xElement.hasAttribute("type")) && (xElement.getAttribute("type") == "product")) {

                            Token subjectToken = new Token();

                            subjectToken.setId(Integer.parseInt(xElement.getAttribute("id")));
                            subjectToken.setForm(xElement.getAttribute("form"));
                            subjectToken.setStart(Integer.parseInt(xElement.getAttribute("start")));
                            subjectToken.setEnd(Integer.parseInt(xElement.getAttribute("end")));
                            subjectToken.setLema(xElement.getAttribute("lemma"));
                            subjectToken.setPos(xElement.getAttribute("pos"));
                            subjectToken.setDepRel(xElement.getAttribute("depRel"));
                            subjectToken.setHead(Integer.parseInt(xElement.getAttribute("head")));
                            subjectToken.setType(xElement.getAttribute("type"));
                            String subtype = (xElement.getAttribute("type") != null && !xElement.getAttribute("type").equalsIgnoreCase("") && xElement.getAttribute("type") != "not_identified") ? xElement.getAttribute("type") : xElement.getAttribute("pos");
                            System.out.println("test1:" + subtype + "    " + xElement.getAttribute("form"));

                            for (String i : patterns) {
                                if (line.contains(i)) {
                                    System.out.println("hasTarget" + i);
                                }

                                //    y = x + 1;
                                /*LinkedList<Token> relationTokens = new LinkedList<>();
                                for (int j = y; j < nList.getLength(); j++) {
                                    System.out.println("y: " + y);
                                    Node yNode = nList.item(j);
                                    if (yNode instanceof Element) {
                                        Element yElement = (Element) yNode;
                                        // if (!yElement.hasAttribute("type") && !yElement.getAttribute("pos").equalsIgnoreCase("NPP")) {
                                        if (!yElement.hasAttribute("type")) {
                                            Token relationToken = new Token();
                                            relationToken.setId(Integer.parseInt(yElement.getAttribute("id")));
                                            relationToken.setForm(yElement.getAttribute("form"));
                                            relationToken.setStart(Integer.parseInt(yElement.getAttribute("start")));
                                            relationToken.setEnd(Integer.parseInt(yElement.getAttribute("end")));
                                            relationToken.setLema(yElement.getAttribute("lemma"));
                                            relationToken.setPos(yElement.getAttribute("pos"));
                                            relationToken.setDepRel(yElement.getAttribute("depRel"));
                                            relationToken.setHead(Integer.parseInt(yElement.getAttribute("head")));
                                            relationTokens.add(relationToken);

                                        } else {

                                            Token objectyToken = new Token();
                                            objectyToken.setId(Integer.parseInt(yElement.getAttribute("id")));
                                            objectyToken.setForm(yElement.getAttribute("form"));
                                            objectyToken.setStart(Integer.parseInt(yElement.getAttribute("start")));
                                            objectyToken.setEnd(Integer.parseInt(yElement.getAttribute("end")));
                                            objectyToken.setLema(yElement.getAttribute("lemma"));
                                            objectyToken.setPos(yElement.getAttribute("pos"));
                                            objectyToken.setDepRel(yElement.getAttribute("depRel"));
                                            objectyToken.setHead(Integer.parseInt(yElement.getAttribute("head")));
                                            objectyToken.setType(yElement.getAttribute("type"));
                                            String objtype = (yElement.getAttribute("type") != null && !yElement.getAttribute("type").equalsIgnoreCase("") && !yElement.getAttribute("type").contains("not_identified")) ? yElement.getAttribute("type") : yElement.getAttribute("pos");
                                            System.out.println("test2:" + objtype + "    " + xElement.getAttribute("form"));

                                            StringBuilder relation = new StringBuilder();

                                            for (Token t : relationTokens) {
                                                relation.append(t.getForm()).append(" ");
                                            }
                                            System.out.println(subjectToken.getForm() + " " + relation.toString() + " " + objectyToken.getForm());

                                            relationTokens = new LinkedList<>();

                                            y = j;
                                            break;
                                        }
                                    }

                                }
                                x = y;

                            } else {

                                x += 1;
                            }

                        }
                    else{

                        x += 1;

                    }*/
                            }
                        }
                    }
                }
            }
        }
    }



    private static void rulesBelongsToBrand(String input) throws ParserConfigurationException, IOException, SAXException {
        List<String> sentences = new ArrayList<>();
        String inputRenco = null;
        String brand = null, product = null;
        String index_product = null;

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        StringBuilder xmlStringBuilder = new StringBuilder();
        xmlStringBuilder.append(input);
///Exemple http://www.tutorialspoint.com/java_xml/java_dom_parse_document.htm
        ByteArrayInputStream in = new ByteArrayInputStream(xmlStringBuilder.toString().getBytes("UTF-8"));
        Document doc = builder.parse(in);
        doc.getDocumentElement().normalize();
        NodeList nSentenceList = doc.getElementsByTagName("sentence");
        for (int sent_temp = 0; sent_temp < nSentenceList.getLength(); sent_temp++) {
            Node nSentNode = nSentenceList.item(sent_temp);
            NodeList nTokensList = nSentNode.getChildNodes();
            //walk on the tokens node
            for (int token_temp = 0; token_temp < nTokensList.getLength(); token_temp++) {
                Node nTokenNode = nTokensList.item(token_temp);
                NodeList nList = nTokenNode.getChildNodes();
                Node nNode = nList.item(token_temp);
                int x = 0, y = 0;
                while (x < nList.getLength()) {
//System.out.println("x: "+x);
                    Node xNode = nList.item(x);

                    if (xNode instanceof Element) {

                        Element xElement = (Element) xNode;

                        // if (xElement.hasAttribute("type") || xElement.getAttribute("pos").equalsIgnoreCase("NPP")) {
                        if (xElement.hasAttribute("type")) {

                            Token subjectToken = new Token();

                            subjectToken.setId(Integer.parseInt(xElement.getAttribute("id")));
                            subjectToken.setForm(xElement.getAttribute("form"));
                            subjectToken.setStart(Integer.parseInt(xElement.getAttribute("start")));
                            subjectToken.setEnd(Integer.parseInt(xElement.getAttribute("end")));
                            subjectToken.setLema(xElement.getAttribute("lemma"));
                            subjectToken.setPos(xElement.getAttribute("pos"));
                            subjectToken.setDepRel(xElement.getAttribute("depRel"));
                            subjectToken.setHead(Integer.parseInt(xElement.getAttribute("head")));
                            subjectToken.setType(xElement.getAttribute("type"));
                            String subtype = (xElement.getAttribute("type") != null && !xElement.getAttribute("type").equalsIgnoreCase("")&& xElement.getAttribute("type")!="not_identified") ? xElement.getAttribute("type") : xElement.getAttribute("pos");
                            System.out.println("test1:"  +subtype + "    "+ xElement.getAttribute("form"));
                            //    y = x + 1;
                            LinkedList<Token> relationTokens = new LinkedList<>();
                            for (int j = y; j < nList.getLength(); j++) {
                                System.out.println("y: "+y);
                                Node yNode = nList.item(j);
                                if (yNode instanceof Element) {
                                    Element yElement = (Element) yNode;
                                    // if (!yElement.hasAttribute("type") && !yElement.getAttribute("pos").equalsIgnoreCase("NPP")) {
                                    if (!yElement.hasAttribute("type")) {
                                        Token relationToken = new Token();
                                        relationToken.setId(Integer.parseInt(yElement.getAttribute("id")));
                                        relationToken.setForm(yElement.getAttribute("form"));
                                        relationToken.setStart(Integer.parseInt(yElement.getAttribute("start")));
                                        relationToken.setEnd(Integer.parseInt(yElement.getAttribute("end")));
                                        relationToken.setLema(yElement.getAttribute("lemma"));
                                        relationToken.setPos(yElement.getAttribute("pos"));
                                        relationToken.setDepRel(yElement.getAttribute("depRel"));
                                        relationToken.setHead(Integer.parseInt(yElement.getAttribute("head")));
                                        relationTokens.add(relationToken);

                                    } else {

                                        Token objectyToken = new Token();
                                        objectyToken.setId(Integer.parseInt(yElement.getAttribute("id")));
                                        objectyToken.setForm(yElement.getAttribute("form"));
                                        objectyToken.setStart(Integer.parseInt(yElement.getAttribute("start")));
                                        objectyToken.setEnd(Integer.parseInt(yElement.getAttribute("end")));
                                        objectyToken.setLema(yElement.getAttribute("lemma"));
                                        objectyToken.setPos(yElement.getAttribute("pos"));
                                        objectyToken.setDepRel(yElement.getAttribute("depRel"));
                                        objectyToken.setHead(Integer.parseInt(yElement.getAttribute("head")));
                                        objectyToken.setType(yElement.getAttribute("type"));
                                        String objtype = (yElement.getAttribute("type") != null && !yElement.getAttribute("type").equalsIgnoreCase("")&&!yElement.getAttribute("type").contains("not_identified")) ? yElement.getAttribute("type") : yElement.getAttribute("pos");
                                        System.out.println("test2:"  +objtype + "    "+ xElement.getAttribute("form"));

                                        StringBuilder relation = new StringBuilder();

                                        for (Token t : relationTokens) {
                                            relation.append(t.getForm()).append(" ");
                                        }
                                        System.out.println( subjectToken.getForm() + " " +relation.toString()+" "+ objectyToken.getForm());

                                        relationTokens = new LinkedList<>();

                                        y = j;
                                        break;
                                    }
                                }

                            }
                            x = y;

                        } else {

                            x += 1;
                        }

                    } else {

                        x += 1;

                    }
                }
            }
        }

    }


    //lire les fichiers d'input
    public static List<String> readCorpus(String folderName) throws IOException {
        ListFilesUtil listFileUtil = new ListFilesUtil();
        listFileUtil.listFilesFromDirector(folderName);
        List<String> files = listFileUtil.files;
        List<String> lines = new LinkedList<>();
        openNLP opennlp;
        opennlp = new openNLP();

        int i = 0;
        for (String file : files) {

            System.out.println("Processing file #: "+ file +": "+ i);
            i++;
            BufferedReader fileReader = null;
            String line = "";
            //Create the file reader
            fileReader = new BufferedReader(new FileReader(folderName + "/" + file));
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
}
