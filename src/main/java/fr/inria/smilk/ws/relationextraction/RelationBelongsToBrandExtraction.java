package fr.inria.smilk.ws.relationextraction;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.*;
import fr.inria.smilk.ws.relationextraction.bean.*;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.*;

import static fr.inria.smilk.ws.relationextraction.ExtractionHelper.*;

/**
 * Created by dhouib on 30/06/2016.
 * Extraction of the relation belongsToBrand
 */
public class RelationBelongsToBrandExtraction extends AbstractRelationExtraction {
    //store the NE DBpedia (product, brand)
     HashMap<String, String> hmap = new HashMap<String, String>();
    //store patterns extracted from DBpedia
     Set<String> patterns = new HashSet<>();

    // méthode qui permet d'extraire les patterns de la relation belongsToBrand à partir
    // de l'abstract de DBpedia ainsi que les parfums et les marques

    public void extractionFromDBpedia() throws Exception {
        List<String> listSentence = new ArrayList<>();
        String sentenceabstract = new String();
        String product = new String();
        String brand = new String();
        String sentencePatternsBelongsToBrand = new String();
        String queryString =
                "PREFIX p: <http://dbpedia.org/property/>" +
                        "PREFIX dbpedia: <http://dbpedia.org/resource/>" +
                        "PREFIX category: <http://dbpedia.org/resource/Category:>" +
                        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
                        "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>" +
                        "PREFIX geo: <http://www.georss.org/georss/>" +
                        "PREFIX dbpedia-owl: <http://dbpedia.org/ontology/>" +
                        "PREFIX prop-fr: <http://fr.dbpedia.org/property/>" +
                        "select  ?parfum_name ?brand_name ?abstract \n" +
                        "where {\n" +
                        "?parfum_list rdfs:label \"Liste de parfums\" @fr.\n" +
                        "?parfum_list dbpedia-owl:wikiPageWikiLink ?parfum.\n" +
                        "?parfum rdfs:label ?parfum_name.\n" +
                        "?parfum prop-fr:marque ?brand.\n" +
                        "?brand rdfs:label ?brand_name.\n" +
                        "?parfum dbpedia-owl:abstract ?abstract.\n" +
                        "FILTER ( LANG(?abstract) = \"fr\" )\n" +
                        "FILTER ( LANG(?parfum_name) = \"fr\" )\n" +
                        "FILTER ( LANG(?brand_name) = \"fr\" )\n" +
                        "} ";

        Query query = QueryFactory.create(queryString);
        QueryExecution qexec = QueryExecutionFactory.sparqlService("http://fr.dbpedia.org/sparql", query);
        try {
            ResultSet results = qexec.execSelect();
            while (results.hasNext()) {
                QuerySolution soln = results.nextSolution();
                Literal abstractDBpedia = soln.getLiteral("abstract");
                sentenceabstract = abstractDBpedia.toString();
                listSentence.add(sentenceabstract);
                int indexproduct = 0, indexbrand = 0;
                Literal productDBpedia = soln.getLiteral("parfum_name");
                product = productDBpedia.toString();
                Literal brandDBpedia = soln.getLiteral("brand_name");
                brand = brandDBpedia.toString();

                if ((product.contains("("))) {
                    indexproduct = product.indexOf("(");
                } else if (product.contains("@")) {
                    indexproduct = product.indexOf("@");
                }

                if ((brand.contains("("))) {
                    indexbrand = brand.indexOf("(");
                } else if (brand.contains("@")) {
                    indexbrand = brand.indexOf("@");
                }
                product = product.substring(0, indexproduct);
                brand = brand.substring(0, indexbrand);
                hmap.put(product, brand);

                int debut = sentenceabstract.indexOf(product) + product.length();
                if (sentenceabstract.contains(brand)) {
                    sentencePatternsBelongsToBrand = sentenceabstract.substring(debut, sentenceabstract.indexOf(brand));
                    patterns.add(sentencePatternsBelongsToBrand.trim());
                }
            }
        } finally {
            qexec.close();
        }
    }


    // recherche du pattern DBpedoa dans le data
    private  void verifyPatternsDBpedia(String line,NodeList nSentenceList, Set<String> patterns) {
        for (String pattern : patterns) {
            //parcourir la sortie de Renco
            for (int sent_temp = 0; sent_temp < nSentenceList.getLength(); sent_temp++) {
                Node nSentNode = nSentenceList.item(sent_temp);
                String sentence = sentenceToString(nSentNode);
                //si la phrase contient le pattern
                if (sentence.contains(pattern)) {
                    //construction de la SentenceRelation (id, subject, object, relation phrase, méthode, type)
                    SentenceRelation sentenceRelation = new SentenceRelation();
                    Token subject = new Token();
                    Token object = new Token();
                    SentenceRelationId sentenceRelationId = new SentenceRelationId();
                    sentenceRelationId.setRelation(pattern);
                    sentenceRelationId.setType(SentenceRelationType.belongsToBrand);
                    // trouver l'index de l'objet
                    int index_pattern = sentence.indexOf(pattern);
                    String objet= sentence.substring(0, sentence.indexOf(pattern));
                    object.setForm(objet);
                    sentenceRelationId.setObject(object);
                    //trouver le sujet de la relation
                    NodeList nTokensList = nSentNode.getChildNodes();
                    Element token=searchToken(nTokensList, index_pattern);
                    subject= elementToToken(token);
                    sentenceRelationId.setSubject(subject);
                    sentenceRelationId.setSentence_text(sentence);
                    sentenceRelation.setSentenceRelationId(sentenceRelationId);
                    sentenceRelation.setMethod(SentenceRelationMethod.dbpedia_patterns);
                    list_result.add(sentenceRelation);
                }
            }
        }
    }


    // recherche des EN de DBpedia dans le data
    private  void verifyENDBpedia(String line,NodeList nSentenceList, Map<String, String> hmap) throws
            ParserConfigurationException, IOException, SAXException {
        //parcourir le hmap
        Set set = hmap.entrySet();
        Iterator iterator = set.iterator();
        while (iterator.hasNext()) {
            Map.Entry mentry = (Map.Entry) iterator.next();
            SentenceRelation sentenceRelation=new SentenceRelation();
            //parcourir la sortie de fr.inria.smilk.ws.relationextraction.renco
            for (int sent_temp = 0; sent_temp < nSentenceList.getLength(); sent_temp++) {
                Node nSentNode = nSentenceList.item(sent_temp);
                StringBuilder builder = new StringBuilder();
                String sentence=line;
                NodeList nTokensList = nSentNode.getChildNodes();
                for (int token_temp = 0; token_temp < nTokensList.getLength(); token_temp++) {
                    Node nTokenNode = nTokensList.item(token_temp);
                    NodeList nList = nTokenNode.getChildNodes();
                    Node nNode = nList.item(token_temp);
                    int x = 0, y = 0;
                    while (x < nList.getLength()) {
                        Node xNode = nList.item(x);
                        if (xNode instanceof Element) {
                            Element xElement = (Element) xNode;
                            //tester si l'element se trouve dans le key du map
                                if((xElement.getAttribute("form").equalsIgnoreCase(mentry.getKey().toString())) || (xElement.getAttribute("lemma").equalsIgnoreCase(mentry.getKey().toString())))
                                {
                                Token subjectToken= elementToToken (xElement);
                                y = x + 1;
                                LinkedList<Token> relationTokens = new LinkedList<>();
                                for (int j = y; j < nList.getLength(); j++) {
                                    Node yNode = nList.item(j);
                                    if (yNode instanceof Element) {
                                        Element yElement = (Element) yNode;
                                        Token objectToken = new Token();
                                            if ((!yElement.getAttribute("form").equalsIgnoreCase(mentry.getValue().toString()))||(!yElement.getAttribute("lemma").equalsIgnoreCase(mentry.getValue().toString()))){
                                                Token relationToken = new Token();
                                                relationToken=elementToToken(yElement);
                                                relationTokens.add(relationToken);
                                            }
                                         //recherche du l'object
                                            else if ((yElement.getAttribute("form").equalsIgnoreCase(mentry.getValue().toString()))||(yElement.getAttribute("lemma").equalsIgnoreCase(mentry.getValue().toString()))) {
                                                objectToken = elementToToken(yElement);
                                                StringBuilder relation = new StringBuilder();
                                                for (Token t : relationTokens) {
                                                    relation.append(t.getForm()).append(" ");
                                                }
                                                //construction de sentence relation
                                                SentenceRelationId sentenceRelationId = new SentenceRelationId();
                                                sentenceRelationId.setSubject(subjectToken);
                                                sentenceRelationId.setObject(objectToken);
                                                sentenceRelationId.setRelation(relation.toString());
                                                sentenceRelationId.setSentence_text(sentence);
                                                sentenceRelationId.setType(SentenceRelationType.belongsToBrand);
                                                sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                                sentenceRelation.setMethod(SentenceRelationMethod.dbpedia_namedEntity);
                                                list_result.add(sentenceRelation);
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
    }

    //recherche de la relation belongsToBrand
    private  void rulesBelongsToBrand(String line,String input) throws ParserConfigurationException, IOException, SAXException {
        Map<String,String> productBrandMap = new HashMap<>();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        StringBuilder xmlStringBuilder = new StringBuilder();
        xmlStringBuilder.append(input);
        ByteArrayInputStream in = new ByteArrayInputStream(xmlStringBuilder.toString().getBytes("UTF-8"));//"UTF-8"
        Document doc = builder.parse(in);
        doc.getDocumentElement().normalize();
        NodeList nSentenceList = doc.getElementsByTagName("sentence");
        verifyPatternsDBpedia(line,nSentenceList, patterns);
        verifyENDBpedia(line,nSentenceList,productBrandMap);
        productBrandMap = findByTypes(line,nSentenceList,"product","brand");
        productBrandMap = findByTypes(line,nSentenceList,"range","brand");
    }

    //recherche de la relation en se basant sur la règle belongsToBrand=product->brand
    private  Map<String,String> findByTypes(String line,NodeList nSentenceList,String firstType,String secondType) {
        Map<String, String> firstTypeSecondTypeMap = new HashMap<>();
        for (int sent_temp = 0; sent_temp < nSentenceList.getLength(); sent_temp++) {
            Node nSentNode = nSentenceList.item(sent_temp);
            StringBuilder builder = new StringBuilder();
            String sentence =line;
            System.out.println("sentence:"+builder.toString());
            NodeList nTokensList = nSentNode.getChildNodes();
            //parcourir l'arbre Renco
            for (int token_temp = 0; token_temp < nTokensList.getLength(); token_temp++) {
                Node nTokenNode = nTokensList.item(token_temp);
                NodeList nList = nTokenNode.getChildNodes();
                Node nNode = nList.item(token_temp);
                int x = 0, y = 0;
                while (x < nList.getLength()) {
                    Node xNode = nList.item(x);
                    if (xNode instanceof Element) {
                        Element xElement = (Element) xNode;
                        if (xElement.hasAttribute("type") && !xElement.getAttribute("type").equalsIgnoreCase("not_identified") &&
                                (xElement.getAttribute("type").equalsIgnoreCase(firstType) ||
                                        xElement.getAttribute("type").equalsIgnoreCase(secondType))) {

                            Token subjectToken = elementToToken(xElement);
                            y = x + 1;
                            LinkedList<Token> relationTokens = new LinkedList<>();
                            for (int j = y; j < nList.getLength(); j++) {
                                Node yNode = nList.item(j);
                                if (yNode instanceof Element) {
                                    Element yElement = (Element) yNode;

                                    if ((!yElement.hasAttribute("type") || yElement.getAttribute("type").equalsIgnoreCase("not_identified")))

                                    {
                                        Token relationToken = elementToToken(yElement);
                                        relationTokens.add(relationToken);

                                    } else {
                                        Token objectToken = elementToToken(yElement);
                                        if ((xElement.getAttribute("type").equalsIgnoreCase(firstType) &&
                                                yElement.getAttribute("type").equalsIgnoreCase(secondType)))
                                        {
                                            StringBuilder relation = new StringBuilder();

                                            for (Token t : relationTokens) {
                                                relation.append(t.getForm()).append(" ");
                                            }
                                            SentenceRelation sentenceRelation=new SentenceRelation();
                                            SentenceRelationId sentenceRelationId = new SentenceRelationId();
                                            sentenceRelationId.setSubject(subjectToken);
                                            sentenceRelationId.setObject(objectToken);
                                            sentenceRelationId.setRelation(relation.toString());
                                            sentenceRelationId.setSentence_text(sentence);
                                            sentenceRelationId.setType(SentenceRelationType.belongsToBrand);
                                            sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                            sentenceRelation.setMethod(SentenceRelationMethod.rulesbelongsToBrand);
                                            list_result.add(sentenceRelation);
                                        } else if (xElement.getAttribute("type").equalsIgnoreCase(secondType) &&
                                                yElement.getAttribute("type").equalsIgnoreCase(firstType)) {
                                            StringBuilder relation = new StringBuilder();

                                            for (Token t : relationTokens) {
                                                relation.append(t.getForm()).append(" ");
                                            }

                                            SentenceRelation sentenceRelation=new SentenceRelation();
                                            SentenceRelationId sentenceRelationId = new SentenceRelationId();
                                            sentenceRelationId.setSubject(objectToken);
                                            sentenceRelationId.setObject(subjectToken);
                                            sentenceRelationId.setRelation(relation.toString());
                                            sentenceRelationId.setSentence_text(sentence);
                                            sentenceRelationId.setType(SentenceRelationType.belongsToBrand);
                                            sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                            sentenceRelation.setMethod(SentenceRelationMethod.rulesbelongsToBrand);
                                            list_result.add(sentenceRelation);
                                        }
                                        relationTokens = new LinkedList<>();
                                        y = j;
                                        break;
                                    }
                                }

                            }
                            x = y;

                        }
                        else {

                            x += 1;
                        }

                    } else {

                        x += 1;

                    }
                }

            }
        }
        return firstTypeSecondTypeMap;
    }

    //méthode qui permet de choisir quelle méthode utilisé pour l'annotattion sachant qu'on favorise le pattern DBpedia
    //puis l'EN DBpedia puis l'application de règle.
    public  void annotationData(List<SentenceRelation> list_result) throws IOException {
        // on construit un map (SentenceRelationId,List<SentenceRelationMethod>)
        Map<SentenceRelationId,List<SentenceRelationMethod>> relationMap = new HashMap<>();
        //construction de list_result contenant les méthodes appliquées pour la même sentence
        for (SentenceRelation sentence_relation : list_result) {
            System.out.println("sentence_relation:" + sentence_relation);
            if(!relationMap.containsKey(sentence_relation.getSentenceRelationId())){
                ArrayList<SentenceRelationMethod> methodlist = new ArrayList<>();
                methodlist.add(sentence_relation.getMethod());
               relationMap.put(sentence_relation.getSentenceRelationId(), methodlist) ;
            }else {
                relationMap.get(sentence_relation.getSentenceRelationId()).add(sentence_relation.getMethod());
            }
        }
        // on parcours le map, et on applique la méthode d'extraction selon cette ordre: dbpedia_patterns,
        // dbpedia_namedEntity, rulesbelongsToBrand
        for(SentenceRelationId sentenceRelationId : relationMap.keySet()){
            List<SentenceRelationMethod> relationMethods = relationMap.get(sentenceRelationId);
            if(relationMethods.contains(SentenceRelationMethod.dbpedia_patterns)){
                System.out.println("Selected"+sentenceRelationId + "" + SentenceRelationMethod.dbpedia_patterns);
                Model model=constructModel (sentenceRelationId);
                writeRdf(model);
            } else if(relationMethods.contains(SentenceRelationMethod.dbpedia_namedEntity)){
                System.out.println("Selected"+sentenceRelationId + "" + SentenceRelationMethod.dbpedia_namedEntity);
                Model model=constructModel (sentenceRelationId);
                writeRdf(model);
            } else {
                if (relationMethods.contains(SentenceRelationMethod.rulesbelongsToBrand)) {
                    System.out.println("Selected" + sentenceRelationId + "" + SentenceRelationMethod.rulesbelongsToBrand);
                    Model model=constructModel (sentenceRelationId);
                    writeRdf(model);
                }
            }
        }
    }

    @Override
    public void processExtraction(String line) throws Exception {
        Renco renco = new Renco();
        rulesBelongsToBrand(line,renco.rencoByWebService(line));
    }

    @Override
    public void init() throws Exception {
        extractionFromDBpedia();
    }


}

