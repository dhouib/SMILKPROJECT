package fr.inria.smilk.ws.relationextraction;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import fr.inria.smilk.ws.relationextraction.bean.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;

import static fr.inria.smilk.ws.relationextraction.ExtractionHelper.*;

/**
 * Created by dhouib on 01/07/2016.
 */

/*Cherche les relations hasComponent dans le texte */
public class RelationHasComponentExtraction extends AbstractRelationExtraction {
    Set<String> listComponent = new HashSet<>();
    Set<String> listChimicalComponent = new HashSet<>();
    Set<String> listhuile = new HashSet<>();


    //SPARQL queries to extract chimical component from DBpedia
    public void extractionComponentFromDBpedia() {
        String Component_name;
        String queryString = "PREFIX p: <http://dbpedia.org/property/>" +
                "PREFIX dbpedia: <http://dbpedia.org/resource/>" +
                "PREFIX category: <http://dbpedia.org/resource/Category:>" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
                "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>" +
                "PREFIX geo: <http://www.georss.org/georss/>" +
                "PREFIX dbpedia-owl: <http://dbpedia.org/ontology/>" +
                "PREFIX prop-fr: <http://fr.dbpedia.org/property/>" +
                "select ?concept ?concept_name\n" +
                " where\n" +
                " {?concept rdf:type dbpedia-owl:ChemicalSubstance.\n" +
                " ?concept rdfs:label ?concept_name.\n" +
                " FILTER ( LANG(?concept_name) = \"fr\")\n" +
                " }\n";
        Query query = QueryFactory.create(queryString);
        QueryExecution qexec = QueryExecutionFactory.sparqlService("http://fr.dbpedia.org/sparql", query);
        try {
            ResultSet results = qexec.execSelect();
            while (results.hasNext()) {
                QuerySolution soln = results.nextSolution();
                Literal Component_name_DBpedia = soln.getLiteral("?concept_name");
                Component_name = Component_name_DBpedia.toString();
                int indexproduct = 0;
                if ((Component_name.contains("("))) {
                    indexproduct = Component_name.indexOf("(");
                } else if (Component_name.contains("@")) {
                    indexproduct = Component_name.indexOf("@");
                }
                Component_name = Component_name.substring(0, indexproduct);
                listChimicalComponent.add(Component_name.toLowerCase());
            }
        } finally {
            qexec.close();
        }
    }

    //SPARQL queries to extract component (plante, arôme) from DBpedia
    private Set<String> hasComponentDbpedia() throws Exception {
        String Component_name;
        String queryString = "PREFIX p: <http://dbpedia.org/property/>" +
                "PREFIX dbpedia: <http://dbpedia.org/resource/>" +
                "PREFIX category: <http://dbpedia.org/resource/Category:>" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
                "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>" +
                "PREFIX geo: <http://www.georss.org/georss/>" +
                "PREFIX dbpedia-owl: <http://dbpedia.org/ontology/>" +
                "PREFIX prop-fr: <http://fr.dbpedia.org/property/>" +
                "select  ?Component_name\n" +
                "where { \n" +
                "{?concept rdfs:label \"Liste des plantes à cosmétique et à parfum\" @fr.\n" +
                "?concept dbpedia-owl:wikiPageWikiLink  ?Component}\n" +
                "Union\n" +
                "{?concept rdfs:label  \"Élément synthétique\" @fr.\n" +
                "?concept dbpedia-owl:wikiPageWikiLink  ?Component\n" +
                "}\n" +
                "Union\n" +
                "{?concept rdfs:label \"Arôme\" @fr.\n" +
                " ?concept dbpedia-owl:wikiPageWikiLink  ?Component\n" +
                "}\n" +
                "\n" +
                "?Component rdfs:label ?Component_name.\n" +
                " FILTER ( LANG(?Component_name) = \"fr\" )\n" +
                "  }\n";

        Query query = QueryFactory.create(queryString);
        QueryExecution qexec = QueryExecutionFactory.sparqlService("http://fr.dbpedia.org/sparql", query);
        try {
            ResultSet results = qexec.execSelect();
            while (results.hasNext()) {
                QuerySolution soln = results.nextSolution();
                Literal Component_name_DBpedia = soln.getLiteral("?Component_name");
                Component_name = Component_name_DBpedia.toString();

                int indexproduct = 0;
                if ((Component_name.contains("("))) {
                    indexproduct = Component_name.indexOf("(");
                } else if (Component_name.contains("@")) {
                    indexproduct = Component_name.indexOf("@");
                }
                Component_name = Component_name.substring(0, indexproduct);
                listComponent.add(Component_name.toLowerCase());
            }

        } finally {
            qexec.close();
        }
        return listComponent;
    }


    //SPARQL queries to extract component (huiles essentielles) from DBpedia
    private Set<String> hashuileDbpedia() throws Exception {
        String Component_name;
        String queryString =
                "PREFIX p: <http://dbpedia.org/property/>" +
                        "PREFIX dbpedia: <http://dbpedia.org/resource/>" +
                        "PREFIX category: <http://dbpedia.org/resource/Category:>" +
                        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
                        "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>" +
                        "PREFIX geo: <http://www.georss.org/georss/>" +
                        "PREFIX dbpedia-owl: <http://dbpedia.org/ontology/>" +
                        "PREFIX prop-fr: <http://fr.dbpedia.org/property/>" +
                        "PREFIX dcterms:<http://purl.org/dc/terms/>" +
                        "PREFIX dbpedia-fr:<http://fr.dbpedia.org/resource/>" +
                        "select ?huile ?huile_name\n" +
                        "where { \n" +
                        "{?huile dcterms:subject ?x.\n" +
                        "?x skos:subject  dbpedia-fr:Huile_essentielle.\n" +
                        "?huile rdfs:label ?huile_name\n}" +
                        "Union" +
                        "{?huile dcterms:subject ?x.\n" +
                        "?x skos:prefLabel  \"Composant de parfum\"@fr.\n" +
                        "?huile rdfs:label ?huile_name}\n" +
                        "Union\n" +
                        "{?huile dcterms:subject ?x.\n" +
                        "?x skos:prefLabel  \"Benzoate d'alkyle\"@fr.\n" +
                        "?huile rdfs:label ?huile_name\n" +
                        "}" +
                        "FILTER ( LANG(?huile_name) = \"fr\" )\n" +
                        " }\n";

        Query query = QueryFactory.create(queryString);
        QueryExecution qexec = QueryExecutionFactory.sparqlService("http://fr.dbpedia.org/sparql", query);
        try {
            ResultSet results = qexec.execSelect();
            while (results.hasNext()) {
                QuerySolution soln = results.nextSolution();
                Literal Component_name_DBpedia = soln.getLiteral("?huile_name");
                Component_name = Component_name_DBpedia.toString();
                int indexproduct = 0;
                if ((Component_name.contains("("))) {
                    indexproduct = Component_name.indexOf("(");
                } else if (Component_name.contains("@")) {
                    indexproduct = Component_name.indexOf("@");
                }
                Component_name = Component_name.substring(0, indexproduct);
                listhuile.add(Component_name.toLowerCase());
            }
        } finally {
            qexec.close();
        }
        return listhuile;
    }

    //recherche de la relation en se basant sur la règle belongsToBrand=product->brand
    private Map<String, String> findByTypes(String line, NodeList nSentenceList, String firstType, String secondType) {
        Map<String, String> firstTypeSecondTypeMap = new HashMap<>();

        for (int sent_temp = 0; sent_temp < nSentenceList.getLength(); sent_temp++) {
            Node nSentNode = nSentenceList.item(sent_temp);
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
                                    if ((!yElement.getAttribute("form").equalsIgnoreCase(secondType)) &&
                                            (!yElement.getAttribute("type").equalsIgnoreCase(firstType))) {
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
                                            sentenceRelationId.setType(SentenceRelationType.hasComponent);
                                            sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                            sentenceRelation.setMethod(SentenceRelationMethod.dbpedia_component);
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
                                            sentenceRelationId.setType(SentenceRelationType.hasComponent);
                                            sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                            sentenceRelation.setMethod(SentenceRelationMethod.dbpedia_component);
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


    private void ruleshasComponent(String line, String input) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        StringBuilder xmlStringBuilder = new StringBuilder();
        xmlStringBuilder.append(input);
        System.out.println(input);
        ByteArrayInputStream in = new ByteArrayInputStream(xmlStringBuilder.toString().getBytes("UTF-8"));
        Document doc = builder.parse(in);
        doc.getDocumentElement().normalize();
        NodeList nSentenceList = doc.getElementsByTagName("sentence");
        listComponent.remove("cosmétique");
        listComponent.remove("acide");
        listComponent.remove("parfum");
        for (String component : listComponent) {
            findByTypes(line, nSentenceList, "product", component);
        }

        for (String component : listChimicalComponent) {
            findByTypes(line, nSentenceList, "product", component);
        }

        for (String component : listhuile) {
            findByTypes(line, nSentenceList, "product", component);
        }
    }

    @Override
    public void annotationData(List<SentenceRelation> list_result) throws IOException {
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
            if (relationMethods.contains(SentenceRelationMethod.dbpedia_chimical_component)) {
                System.out.println("Selected" + sentenceRelationId + "" + SentenceRelationMethod.dbpedia_chimical_component);
                Model model = constructModel(sentenceRelationId);
                writeRdf(model);
            } else if (relationMethods.contains(SentenceRelationMethod.dbpedia_component)) {
                System.out.println("Selected" + sentenceRelationId + "" + SentenceRelationMethod.dbpedia_component);
                Model model = constructModel(sentenceRelationId);
                writeRdf(model);
            } else if (relationMethods.contains(SentenceRelationMethod.dbpedia_parfum_component)) {
                System.out.println("Selected" + sentenceRelationId + "" + SentenceRelationMethod.dbpedia_parfum_component);
                Model model = constructModel(sentenceRelationId);
                writeRdf(model);
            }
        }
    }

    @Override
    public void processExtraction(String line) throws Exception {
        Renco renco = new Renco();
        ruleshasComponent(line, renco.rencoByWebService(line));
    }


    @Override
    public void init() throws Exception {
        extractionComponentFromDBpedia();
        hasComponentDbpedia();
        hashuileDbpedia();
    }

}


