import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import fr.inria.smilk.ws.relationextraction.AbstractRelationExtraction;
import fr.inria.smilk.ws.relationextraction.Renco;
import fr.inria.smilk.ws.relationextraction.bean.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;

import static fr.inria.smilk.ws.relationextraction.ExtractionHelper.*;

/**
 * Created by dhouib on 13/07/2016.
 */
public class RelationHasAmbassadorExtraction extends AbstractRelationExtraction {
    Set<String> listFragranceCreator = new HashSet<>();
    static Set<String> patterns = new HashSet<>();

    /**
     * méthode qui permet d'extraire les patterns de la relation belongsToBrand à partir
     * de l'abstract de DBpedia ainsi que les parfums et les marques
     */
    private void extractionFromDBpedia() throws Exception {
        List<String> listSentence = new ArrayList<>();
        String sentenceabstract = new String();
        String sentencePatternshasFragranceCreator = new String();
        String creator, product;
        String queryString =
                "PREFIX p: <http://dbpedia.org/property/>" +
                        "PREFIX dbpedia: <http://dbpedia.org/resource/>" +
                        "PREFIX category: <http://dbpedia.org/resource/Category:>" +
                        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
                        "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>" +
                        "PREFIX geo: <http://www.georss.org/georss/>" +
                        "PREFIX dbpedia-owl: <http://dbpedia.org/ontology/>" +
                        "PREFIX prop-fr: <http://fr.dbpedia.org/property/>" +
                        "prefix rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
                        "select ?ambassador ?ambassador_name \n" +
                        "where {\n" +
                        "{?ambassador rdf:type dbpedia-owl:Artist.\n" +
                        "?ambassador rdfs:label ?ambassador_name.\n" +
                        "}\n" +
                        "Union\n" +
                        "{\n" +
                        "?ambassador rdf:type dbpedia-owl:Singer.\n" +
                        "?ambassador rdfs:label ?ambassador_name.\n" +
                        "}\n" +
                        "Union\n" +
                        "{\n" +
                        "?ambassador rdf:type dbpedia-owl:Athlete.\n" +
                        "?ambassador rdfs:label ?ambassador_name.\n" +
                        "}\n" +
                        "FILTER ( LANG(?ambassador_name) = \"fr\" )\n" +
                        "} ";

        Query query = QueryFactory.create(queryString);
        QueryExecution qexec = QueryExecutionFactory.sparqlService("http://fr.dbpedia.org/sparql", query);
        try {
            ResultSet results = qexec.execSelect();
            while (results.hasNext()) {
                QuerySolution soln = results.nextSolution();
                Literal creator_name_DBpedia = soln.getLiteral("?ambassador_name");
                creator = creator_name_DBpedia.toString();


                int indexcreator = 0;

                if ((creator.contains("("))) {
                    indexcreator = creator.indexOf("(");
                } else if (creator.contains("@")) {
                    indexcreator = creator.indexOf("@");
                }

                creator = creator.substring(0, indexcreator);
                listFragranceCreator.add(creator.trim());
            }

        } finally {
            qexec.close();
        }

        try {
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //recherche de la relation en se basant sur la règle belongsToBrand=product->brand
    private Map<String, String> findByTypes(String line, NodeList nSentenceList, String firstType, String secondType) {
        Map<String, String> firstTypeSecondTypeMap = new HashMap<>();

        for (int sent_temp = 0; sent_temp < nSentenceList.getLength(); sent_temp++) {
            Node nSentNode = nSentenceList.item(sent_temp);
            StringBuilder builder = new StringBuilder();
            String sentence = line;
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
                        if ((xElement.hasAttribute("type") && !xElement.getAttribute("type").equalsIgnoreCase("not_identified") &&
                                (xElement.getAttribute("type").equalsIgnoreCase(firstType))) || (xElement.getAttribute("form").equalsIgnoreCase(secondType))) {


                            y = x + 1;
                            LinkedList<Token> relationTokens = new LinkedList<>();
                            for (int j = y; j < nList.getLength(); j++) {
                                Node yNode = nList.item(j);

                                if (yNode instanceof Element) {
                                    Element yElement = (Element) yNode;
                                    //System.out_copy.println("objectToken: "+objectToken.getForm());
                                    // if (!yElement.getAttribute("form").equalsIgnoreCase(secondType))// ||
                                    if ((!yElement.getAttribute("form").equalsIgnoreCase(secondType)) && (!yElement.getAttribute("type").equalsIgnoreCase(firstType))) {

                                        Token relationToken = elementToToken(yElement);
                                        relationTokens.add(relationToken);
                                    } else {


                                        if ((xElement.getAttribute("type").equalsIgnoreCase(firstType) &&
                                                yElement.getAttribute("form").equalsIgnoreCase(secondType))) {
                                            StringBuilder relation = new StringBuilder();

                                            for (Token t : relationTokens) {
                                                relation.append(t.getForm()).append(" ");
                                            }
                                            Token subjectToken = elementToToken(xElement);
                                            Token objectToken = elementToToken(yElement);
                                            objectToken.setForm(secondType);
                                            SentenceRelation sentenceRelation = new SentenceRelation();
                                            SentenceRelationId sentenceRelationId = new SentenceRelationId();
                                            sentenceRelationId.setSubject(subjectToken);
                                            sentenceRelationId.setObject(objectToken);
                                            sentenceRelationId.setRelation(relation.toString());
                                            sentenceRelationId.setSentence_text(sentence);
                                            sentenceRelationId.setType(SentenceRelationType.hasAmbasador);
                                            sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                            sentenceRelation.setMethod(SentenceRelationMethod.dbpedia_cible);
                                            list_result.add(sentenceRelation);
                                        } else if (xElement.getAttribute("form").equalsIgnoreCase(secondType) &&
                                                yElement.getAttribute("type").equalsIgnoreCase(firstType)) {
                                            Token subjectToken = elementToToken(xElement);
                                            subjectToken.setForm(secondType);
                                            Token objectToken = elementToToken(yElement);
                                            StringBuilder relation = new StringBuilder();

                                            for (Token t : relationTokens) {
                                                relation.append(t.getForm()).append(" ");
                                            }

                                            SentenceRelation sentenceRelation = new SentenceRelation();
                                            SentenceRelationId sentenceRelationId = new SentenceRelationId();
                                            sentenceRelationId.setSubject(objectToken);
                                            sentenceRelationId.setObject(subjectToken);
                                            sentenceRelationId.setRelation(relation.toString());
                                            sentenceRelationId.setSentence_text(sentence);
                                            sentenceRelationId.setType(SentenceRelationType.hasAmbasador);
                                            sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                            sentenceRelation.setMethod(SentenceRelationMethod.dbpedia_cible);
                                            list_result.add(sentenceRelation);
                                        }
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

        return firstTypeSecondTypeMap;
    }

    //recherche de la relation belongsToBrand
    private void rulesBelongsToBrand(String line, String input) throws ParserConfigurationException, IOException, SAXException {
        Map<String, String> productBrandMap = new HashMap<>();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        StringBuilder xmlStringBuilder = new StringBuilder();
        xmlStringBuilder.append(input);
        ByteArrayInputStream in = new ByteArrayInputStream(xmlStringBuilder.toString().getBytes("UTF-8"));
        Document doc = builder.parse(in);
        doc.getDocumentElement().normalize();
        NodeList nSentenceList = doc.getElementsByTagName("sentence");
        for (String component : listFragranceCreator) {
            findByTypes(line, nSentenceList, "product", component);
            findByTypes(line, nSentenceList, "range", component);
            findByTypes(line, nSentenceList, "brand", component);
            findByTypes(line, nSentenceList, "division", component);
            findByTypes(line, nSentenceList, "group", component);
        }
    }


    //méthode qui permet de choisir quelle méthode utilisé pour l'annotattion sachant qu'on favorise le pattern DBpedia
    //puis l'EN DBpedia puis l'application de règle.
    public void annotationData(List<SentenceRelation> list_result) throws IOException {
        // on construit un map (SentenceRelationId,List<SentenceRelationMethod>)
        Map<SentenceRelationId, List<SentenceRelationMethod>> relationMap = new HashMap<>();
        //construction de list_result contenant les méthodes appliquées pour la même sentence
        for (SentenceRelation sentence_relation : list_result) {
            System.out.println("sentence_relation:" + sentence_relation);
            if (!relationMap.containsKey(sentence_relation.getSentenceRelationId())) {
                ArrayList<SentenceRelationMethod> methodlist = new ArrayList<>();
                methodlist.add(sentence_relation.getMethod());
                relationMap.put(sentence_relation.getSentenceRelationId(), methodlist);
            } else {
                relationMap.get(sentence_relation.getSentenceRelationId()).add(sentence_relation.getMethod());
            }
        }
        // on parcours le map, et on applique la méthode d'extraction selon cette ordre: dbpedia_patterns,
        // dbpedia_namedEntity, rulesbelongsToBrand
        for (SentenceRelationId sentenceRelationId : relationMap.keySet()) {
            List<SentenceRelationMethod> relationMethods = relationMap.get(sentenceRelationId);
            if (relationMethods.contains(SentenceRelationMethod.dbpedia_cible)) {
                System.out.println("Selected" + sentenceRelationId + "" + SentenceRelationMethod.dbpedia_cible);
                Model model = constructModel(sentenceRelationId);
                writeRdf(model);

            }
        }
    }

    @Override
    public void processExtraction(String line) throws Exception {
        Renco renco = new Renco();
        rulesBelongsToBrand(line, renco.rencoByWebService(line));
    }

    @Override
    public void init() throws Exception {
        extractionFromDBpedia();
    }

}
