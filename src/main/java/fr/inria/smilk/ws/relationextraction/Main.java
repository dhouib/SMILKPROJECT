package fr.inria.smilk.ws.relationextraction;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Literal;
import fr.inria.smilk.ws.relationextraction.renco.renco_simple.RENCO;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by dhouib on 25/05/2016.
 */
public class Main {

    //fichier du résultat
    static FileWriter outtputWriter;

    //stocker les (product, brand)
    static HashMap<String, String> hmap = new HashMap<String, String>();
    //stocker les patterns
    static Set<String> patterns = new HashSet<>();

    public static void main(String[] args) throws Exception {

        //le dataset
        Scanner sc = new Scanner(new File("C:/Users/dhouib/Desktop/SMILK_project_devpt/RelationExtractionSMILK/src/main/resources/input/HealthImpactText.txt"));
        String line;
        String s = "";
        while (sc.hasNextLine())
        {
            s += sc.nextLine();
        }
        sc.close();


        //output File
        String outputFile = "C:/Users/dhouib/Desktop/SMILK_project_devpt/RelationExtractionSMILK/src/main/resources/output/HealthImpactRelation.txt";
        outtputWriter=new FileWriter(outputFile);

        //outil d'analyse syntaxique
        RENCO renco = new RENCO();

        //requete SPARQL pour récuperer les abstracts, brand et product
        belongsToBrandSPARQLBrand();
        //extraire à partir du dataSet
        extractFromText (s,renco );

        outtputWriter.flush();
        outtputWriter.close();
    }


    // méthode qui permet d'extraire les patterns de la relation belongsToBrand à partir
    // de l'abstract de DBpedia ainsi que les parfums et les marques
    private static void belongsToBrandSPARQLBrand() {
        List<String> listSentence= new ArrayList<>();
        String sentenceabstract=new String();
        String product=new String();
        String brand= new String();
        String sentencePatterns =new String();
        String queryString =
                "PREFIX p: <http://dbpedia.org/property/>"+
                "PREFIX dbpedia: <http://dbpedia.org/resource/>"+
                "PREFIX category: <http://dbpedia.org/resource/Category:>"+
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"+
                "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>"+
                "PREFIX geo: <http://www.georss.org/georss/>"+
                "PREFIX dbpedia-owl: <http://dbpedia.org/ontology/>"+
                "PREFIX prop-fr: <http://fr.dbpedia.org/property/>"+
                "select  ?parfum_name ?brand_name ?abstract\n" +
                        "where {\n" +
                        "?parfum_list rdfs:label \"Liste de parfums\" @fr.\n" +
                        "?parfum_list dbpedia-owl:wikiPageWikiLink ?parfum.\n" +
                        "?parfum rdfs:label ?parfum_name.\n"+
                        "?parfum prop-fr:marque ?brand.\n" +
                        "?brand rdfs:label ?brand_name.\n"+
                        "?parfum dbpedia-owl:abstract ?abstract.\n" +
                        "FILTER ( LANG(?abstract) = \"fr\" )\n" +
                        "FILTER ( LANG(?parfum_name) = \"fr\" )\n" +
                        "FILTER ( LANG(?brand_name) = \"fr\" )\n" +
                        "}LIMIT 10 ";

        Query query = QueryFactory.create(queryString);
        QueryExecution qexec = QueryExecutionFactory.sparqlService("http://fr.dbpedia.org/sparql", query);
        try {
            ResultSet results = qexec.execSelect();
            while (results.hasNext() ) {
                QuerySolution soln = results.nextSolution();
                Literal abstractDBpedia = soln.getLiteral("abstract");
                sentenceabstract = abstractDBpedia.toString();
                listSentence.add(sentenceabstract);
                int indexproduct, indexbrand;
                Literal productDBpedia=soln.getLiteral("parfum_name");
                product=productDBpedia.toString();
                Literal brandDBpedia=soln.getLiteral("brand_name");
                brand=brandDBpedia.toString();
                if ((product.contains("(")) || (brand.contains("("))) {
                    indexproduct = product.indexOf("(");
                    indexbrand = brand.indexOf("(");
                }
                else
                    indexproduct = product.indexOf("@");
                    indexbrand = brand.indexOf("@");
                    product = product.substring(0, indexproduct);
                    brand = brand.substring(0, indexbrand);
                hmap.put(product,brand);
                int debut = sentenceabstract.indexOf(product) + product.length();
                sentencePatterns=sentenceabstract.substring(debut,sentenceabstract.indexOf(brand));
                patterns.add(sentencePatterns.trim());
            }
         //   patterns.add(sentencePatterns.trim());
        } finally {
            qexec.close();
        }

    }


    private static void extractFromText (String s, RENCO renco ) throws Exception {
        String product, brand;

        String sentencePatterns =new String();
        for (String i : patterns) {
            if (s.contains(i)) {
                System.out.println("Pattern trouvé:" + i);
                extractDOMparser(renco.rencoByWebService(s));
            }
            else {

                System.out.println("Pattern non trouvé");
                Set set = hmap.entrySet();
                Iterator iterator = set.iterator();
                while (iterator.hasNext()) {
                    Map.Entry mentry = (Map.Entry) iterator.next();
                    product= (String) mentry.getKey();
                    brand= (String) mentry.getValue();
                    if((s.contains(product)&&(s.contains(brand)))){
                        System.out.println("brand: "+brand);
                        int debut = s.indexOf(product) + product.length();
                        sentencePatterns=s.substring(debut,s.indexOf(brand));
                        System.out.println("extraaaaaaact pattern: "+sentencePatterns);
                    }
                }
            }
        }

        patterns.add(sentencePatterns.trim());
        for (String i : patterns) {
            System.out.println ("patterne est: "+ i);
        }


        Set set = hmap.entrySet();
        Iterator iterator = set.iterator();
        while (iterator.hasNext()) {
            Map.Entry mentry = (Map.Entry) iterator.next();
            System.out.print("key is: " + mentry.getKey() + " & Value is: ");
            System.out.println(mentry.getValue());
        }

    }

    public static void extractDOMparser(String input) {
        List<String> sentences = new ArrayList<>();
        String inputRenco = null;
        String brand = null,product = null;

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
            NodeList nList = doc.getElementsByTagName("token");
            for (int temp = 0; temp < nList.getLength(); temp++) {
                Node nNode = nList.item(temp);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    org.w3c.dom.Element eElement = (org.w3c.dom.Element) nNode;
                    if (eElement.hasAttribute("type")) {
                        if (eElement.getAttribute("type").toString().contains("brand")){
                            brand=eElement.getAttribute("form");
                        }
                        else if (eElement.getAttribute("type").toString().contains("product")){
                          product= eElement.getAttribute("form");
                        }
                    }
                }
            }
            hmap.put(product,brand);
        }catch(IOException ex){
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
    }

    public static void extractDOMparser2(String input) {
        List<String> sentences = new ArrayList<>();
        String inputRenco = null;
        String brand = null,product = null;

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
            NodeList nList = doc.getElementsByTagName("token");
            for (int temp = 0; temp < nList.getLength(); temp++) {
                Node nNode = nList.item(temp);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    org.w3c.dom.Element eElement = (org.w3c.dom.Element) nNode;
                    if (eElement.hasAttribute("type")) {
                        if (eElement.getAttribute("type").toString().contains("brand")){
                            brand=eElement.getAttribute("form");
                        }
                        else if (eElement.getAttribute("type").toString().contains("product")){
                            product= eElement.getAttribute("form");
                        }
                    }
                }
            }
            hmap.put(product,brand);
        }catch(IOException ex){
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
    }
        private static List<String> hasAmbassadorSPARQL() {
        List<String> listSentence= new ArrayList<>();
        String sentence=new String();
        String queryString =
                "PREFIX p: <http://dbpedia.org/property/>"+
                        "PREFIX dbpedia: <http://dbpedia.org/resource/>"+
                        "PREFIX category: <http://dbpedia.org/resource/Category:>"+
                        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"+
                        "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>"+
                        "PREFIX geo: <http://www.georss.org/georss/>"+
                        "PREFIX dbpedia-owl: <http://dbpedia.org/ontology/>"+
                        "PREFIX prop-fr: <http://fr.dbpedia.org/property/>"+
                        "select  ?parfum ?person\n" +
                        "where {\n" +
                        "?parfum_list rdfs:label \"Liste de parfums\" @fr.\n" +
                        "?parfum_list dbpedia-owl:wikiPageWikiLink ?parfum.\n" +
                        "?parfum dbpedia-owl:wikiPageWikiLink ?person.\n" +
                        "?person rdf:type foaf:Person.\n" +
                        "FILTER ( LANG(?abstract) = \"fr\" )\n" +
                        "}LIMIT 5 ";

        Query query = QueryFactory.create(queryString);
        QueryExecution qexec = QueryExecutionFactory.sparqlService("http://fr.dbpedia.org/sparql", query);
        try {
            ResultSet results = qexec.execSelect();
            while (results.hasNext() ) {
                QuerySolution soln = results.nextSolution();
                Literal name = soln.getLiteral("abstract");
                sentence = name.toString();
                listSentence.add(sentence);
                System.out.println(sentence);
            }

        } finally {
            qexec.close();
        }
        return listSentence;
    }


    private static List<String> hasComponentSPARQL() {
        List<String> listSentence= new ArrayList<>();
        String sentence=new String();
        String queryString =
                "PREFIX p: <http://dbpedia.org/property/>"+
                        "PREFIX dbpedia: <http://dbpedia.org/resource/>"+
                        "PREFIX category: <http://dbpedia.org/resource/Category:>"+
                        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"+
                        "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>"+
                        "PREFIX geo: <http://www.georss.org/georss/>"+
                        "PREFIX dbpedia-owl: <http://dbpedia.org/ontology/>"+
                        "PREFIX prop-fr: <http://fr.dbpedia.org/property/>"+
                        "select  ?Component_name\n" +
                        "where {\n" +
                        "?plant_list rdfs:label \"Liste des plantes à cosmétique et à parfum\" @fr..\n" +
                        "?plant_list dbpedia-owl:wikiPageWikiLink  ?Component.\n" +
                        "?Component rdfs:label ?Component_name.\n" +
                        "FILTER ( LANG(?Component_name) = \"fr\" )\n" +
                        "}LIMIT 5 ";

        Query query = QueryFactory.create(queryString);
        QueryExecution qexec = QueryExecutionFactory.sparqlService("http://fr.dbpedia.org/sparql", query);
        try {
            ResultSet results = qexec.execSelect();
            while (results.hasNext() ) {
                QuerySolution soln = results.nextSolution();
                Literal name = soln.getLiteral("Component_name");
                sentence = name.toString();
                listSentence.add(sentence);
                System.out.println(sentence);
            }

        } finally {
            qexec.close();
        }
        return listSentence;
    }



    private static List<String> hasCreatorSPARQL() {
        List<String> listSentence= new ArrayList<>();
        String sentence=new String();
        String queryString =
                "PREFIX p: <http://dbpedia.org/property/>"+
                        "PREFIX dbpedia: <http://dbpedia.org/resource/>"+
                        "PREFIX category: <http://dbpedia.org/resource/Category:>"+
                        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"+
                        "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>"+
                        "PREFIX geo: <http://www.georss.org/georss/>"+
                        "PREFIX dbpedia-owl: <http://dbpedia.org/ontology/>"+
                        "PREFIX prop-fr: <http://fr.dbpedia.org/property/>"+
                        "select  ?parfum ?creator ?abstract\n" +
                        "where {\n" +
                        "?parfum_list rdfs:label \"Liste de parfums\" @fr.\n" +
                        "?parfum_list dbpedia-owl:wikiPageWikiLink ?parfum.\n" +
                        "?parfum prop-fr:créateur ?creator.\n" +
                        "?parfum dbpedia-owl:abstract ?abstract.\n" +
                        "FILTER ( LANG(?abstract) = \"fr\" )\n" +
                        "}LIMIT 5 ";

        Query query = QueryFactory.create(queryString);
        QueryExecution qexec = QueryExecutionFactory.sparqlService("http://fr.dbpedia.org/sparql", query);
        try {
            ResultSet results = qexec.execSelect();
            while (results.hasNext() ) {
                QuerySolution soln = results.nextSolution();
                Literal name = soln.getLiteral("Component_name");
                sentence = name.toString();
                listSentence.add(sentence);
                System.out.println(sentence);
            }

        } finally {
            qexec.close();
        }
        return listSentence;
    }


    private static List<String> hasFounderSPARQL() {
        List<String> listSentence= new ArrayList<>();
        String sentence=new String();
        String queryString =
                "PREFIX p: <http://dbpedia.org/property/>"+
                        "PREFIX dbpedia: <http://dbpedia.org/resource/>"+
                        "PREFIX category: <http://dbpedia.org/resource/Category:>"+
                        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"+
                        "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>"+
                        "PREFIX geo: <http://www.georss.org/georss/>"+
                        "PREFIX dbpedia-owl: <http://dbpedia.org/ontology/>"+
                        "PREFIX prop-fr: <http://fr.dbpedia.org/property/>"+
                        "select  ?Groupe ?founder ?founder_name\n" +
                        "where {\n" +
                        "?Groupe rdf:type dbpedia-owl:Company.\n" +
                        "?Groupe dbpedia-owl:foundedBy ?founder.\n" +
                        "?founder rdfs:label ?founder_name.\n" +
                        "FILTER ( LANG(?founder_name) = \"fr\" )\n" +
                        "}LIMIT 5 ";

        Query query = QueryFactory.create(queryString);
        QueryExecution qexec = QueryExecutionFactory.sparqlService("http://fr.dbpedia.org/sparql", query);
        try {
            ResultSet results = qexec.execSelect();
            while (results.hasNext() ) {
                QuerySolution soln = results.nextSolution();
                Literal name = soln.getLiteral("Component_name");
                sentence = name.toString();
                listSentence.add(sentence);
                System.out.println(sentence);
            }

        } finally {
            qexec.close();
        }
        return listSentence;
    }


    private static List<String> formationYearSPARQL() {
        List<String> listSentence= new ArrayList<>();
        String sentence=new String();
        String queryString =
                "PREFIX p: <http://dbpedia.org/property/>"+
                        "PREFIX dbpedia: <http://dbpedia.org/resource/>"+
                        "PREFIX category: <http://dbpedia.org/resource/Category:>"+
                        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"+
                        "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>"+
                        "PREFIX geo: <http://www.georss.org/georss/>"+
                        "PREFIX dbpedia-owl: <http://dbpedia.org/ontology/>"+
                        "PREFIX prop-fr: <http://fr.dbpedia.org/property/>"+
                        "select  ?parfum  ?date  ?abstract\n" +
                        "where {\n" +
                        "?parfum_list rdfs:label \"Liste de parfums\" @fr.\n" +
                        "?parfum_list dbpedia-owl:wikiPageWikiLink ?parfum.\n" +
                        "?parfum prop-fr:lancement ?date.\n" +
                        "?parfum dbpedia-owl:abstract ?abstract.\n" +
                        "FILTER ( LANG(?abstract) = \"fr\" )\n" +
                        "}LIMIT 5 ";

        Query query = QueryFactory.create(queryString);
        QueryExecution qexec = QueryExecutionFactory.sparqlService("http://fr.dbpedia.org/sparql", query);
        try {
            ResultSet results = qexec.execSelect();
            while (results.hasNext() ) {
                QuerySolution soln = results.nextSolution();
                Literal name = soln.getLiteral("abstract");
                sentence = name.toString();
                listSentence.add(sentence);
                System.out.println(sentence);
            }

        } finally {
            qexec.close();
        }
        return listSentence;
    }


    private static List<String> healthImpactRelation(List<String> composantCosmetiqueFile,List<String> troubleFile, String SentenceHealthImpact) throws IOException {

        System.out.print("*************healthImpactRealtion \n");
        String entity1 = null,entity2 = null,relationphrase = null;
        List<String> listPatternrelation = null;
        for (String i : composantCosmetiqueFile){
            for (String j : troubleFile) {
                //System.out.print(i);
                if (SentenceHealthImpact.contains(i) && SentenceHealthImpact.contains(j)) {
                    System.out.println(SentenceHealthImpact + "\n" + i + "\n" + j + "\n");
                    int debut = SentenceHealthImpact.indexOf(i) + i.length();
                    System.out.print("debut: "+debut);
                    SentenceHealthImpact.indexOf(i);System.out.print("index of "+i +"= "+SentenceHealthImpact.indexOf(i)+"\n");
                    SentenceHealthImpact.indexOf(j);System.out.print("index of "+j +"= "+SentenceHealthImpact.indexOf(j)+"\n");

                    relationphrase=SentenceHealthImpact.substring(debut,SentenceHealthImpact.indexOf(j));

                    System.out.print("relationphrase:"+ relationphrase+"\n");
                    System.out.print(i+ ",healthImpact,"+ j+"\n");
                    entity1=i;
                    entity2=j;
                    outtputWriter.append( entity1+",healthImpact, "+entity2+'\n');
                }
            }
        }

       // listPatternrelation.add(relationphrase);
        return listPatternrelation;
    }




}
