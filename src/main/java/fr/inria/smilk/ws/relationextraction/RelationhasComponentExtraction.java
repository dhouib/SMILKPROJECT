package fr.inria.smilk.ws.relationextraction;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import fr.inria.smilk.ws.relationextraction.bean.SentenceRelation;
import fr.inria.smilk.ws.relationextraction.bean.SentenceRelationId;
import fr.inria.smilk.ws.relationextraction.bean.SentenceRelationMethod;
import fr.inria.smilk.ws.relationextraction.bean.SentenceRelationType;
import fr.inria.smilk.ws.relationextraction.renco.renco_simple.RENCO;
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
 * Created by dhouib on 01/07/2016.
 */
public class RelationhasComponentExtraction extends AbstractRelationExtraction {
    Set<String> listComponent = new HashSet<>();
    Set<String> listChimicalComponent = new HashSet<>();
    Set<String> listChimicalComponent_test = new HashSet<>();
    //SPARQL queries to extract chimical component from DBpedia

    public void extractionComponentFromDBpedia (){
        String Component_name;
        String queryString =  "PREFIX p: <http://dbpedia.org/property/>"+
                "PREFIX dbpedia: <http://dbpedia.org/resource/>"+
                "PREFIX category: <http://dbpedia.org/resource/Category:>"+
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"+
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"+
                "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>"+
                "PREFIX geo: <http://www.georss.org/georss/>"+
                "PREFIX dbpedia-owl: <http://dbpedia.org/ontology/>"+
                "PREFIX prop-fr: <http://fr.dbpedia.org/property/>"+
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
            while (results.hasNext() ) {
                QuerySolution soln = results.nextSolution();
                Literal Component_name_DBpedia = soln.getLiteral("?concept_name");
                Component_name = Component_name_DBpedia.toString();

                int indexproduct = 0;
                if ((Component_name.contains("("))) {
                    indexproduct = Component_name.indexOf("(");
                }
                else if(Component_name.contains("@")) {
                    indexproduct = Component_name.indexOf("@");
                }

                Component_name = Component_name.substring(0,indexproduct);

                listChimicalComponent.add(Component_name.toLowerCase());

            }

        } finally {
            qexec.close();
        }

    }


    //SPARQL queries to extract component (plante, arôme) from DBpedia
    private  Set<String> hasComponentDbpedia() throws Exception {
        String Component_name;
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
            while (results.hasNext() ) {
                QuerySolution soln = results.nextSolution();
                Literal Component_name_DBpedia = soln.getLiteral("?Component_name");
                Component_name = Component_name_DBpedia.toString();

                int indexproduct = 0;
                if ((Component_name.contains("("))) {
                    indexproduct = Component_name.indexOf("(");
                }
                else if(Component_name.contains("@")) {
                    indexproduct = Component_name.indexOf("@");
                }
                Component_name = Component_name.substring(0,indexproduct);
                listComponent.add(Component_name.toLowerCase());
            }

        } finally {
            qexec.close();
        }
        return listComponent;
    }


    private  void verifyChimicalComponentDBpedia(NodeList nSentenceList, Set<String> listChimicalComponent) {
        for (String chimical_component : listChimicalComponent) {
            System.out.println("chimical_component: "+chimical_component );

        }


        for (String chimical_component : listChimicalComponent) {

            //parcourir la sortie de Renco

            for (int sent_temp = 0; sent_temp < nSentenceList.getLength(); sent_temp++) {
                Node nSentNode = nSentenceList.item(sent_temp);
                String sentence = sentenceToString(nSentNode);
                //String sentence_text=line;
             //   System.out.println("sentence_text: "+sentence_text);
                if (sentence.contains(chimical_component)) {
                    System.out.println("\nchimical_component:"+chimical_component+"\n");
                    SentenceRelation sentenceRelation = new SentenceRelation();
                    SentenceRelationId sentenceRelationId = new SentenceRelationId();
                    NodeList nTokensList = nSentNode.getChildNodes();
                    for (int currentTokensIndex = 0; currentTokensIndex < nTokensList.getLength(); currentTokensIndex++) {
                        Node nTokensNode = nTokensList.item(currentTokensIndex);
                        if (nTokensNode instanceof Element) {
                            for (int currentChildIndex = 0; currentChildIndex < nTokensNode.getChildNodes().getLength(); currentChildIndex++) {
                                Node nTokenNode = nTokensNode.getChildNodes().item(currentChildIndex);
                                if (nTokenNode instanceof Element) {

                                    Element current_element = (Element) nTokenNode;
                                    int index_Component, index_subject;
                                    if (current_element.hasAttribute("type")) {
                                        if (current_element.getAttribute("type").equalsIgnoreCase("product")) {
                                            //construction de l'objet
                                            Token object = new Token();
                                            //construction du sujet
                                            Token subject = new Token();
                                            //construction de la relation
                                            index_Component= sentence.indexOf(chimical_component);
                                            System.out.println("indexComponent: " + index_Component);


                                            sentenceRelationId.setType(SentenceRelationType.hasComponent);
                                            subject = elementToToken(current_element);

                                            sentenceRelationId.setSubject(subject);
                                            index_subject=sentence.indexOf(subject.getForm());
                                            System.out.println("index_subject: "+ index_subject);
                                            if(index_Component<index_subject) {
                                                int indexRelation = index_Component + chimical_component.length();
                                                String SentenceRelation = sentence.substring(indexRelation, index_subject);
                                                sentenceRelationId.setRelation(SentenceRelation);
                                               // sentenceRelationId.setSentence_text(sentence_text);
                                                sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                                sentenceRelation.setMethod(SentenceRelationMethod.dbpedia_chimical_component);
                                                object.setForm(chimical_component);
                                                sentenceRelationId.setObject(object);
                                                list_result.add(sentenceRelation);
                                                System.out.println("Extracted: " + sentenceRelationId);
                                            }
                                            else if (index_Component>index_subject){
                                                int indexRelation = index_subject + subject.getForm().length();
                                                String SentenceRelation = sentence.substring(indexRelation, index_Component);
                                                sentenceRelationId.setRelation(SentenceRelation);
                                               // sentenceRelationId.setSentence_text(sentence_text);
                                                sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                                sentenceRelation.setMethod(SentenceRelationMethod.dbpedia_chimical_component);
                                                object.setForm(chimical_component);
                                                sentenceRelationId.setObject(object);
                                                list_result.add(sentenceRelation);
                                                System.out.println("Extracted:index_component>index_subject " + sentenceRelationId);
                                            }
                                        }
                                    }

                                }
                            }
                        }
                    }
                    }

                }
            }
        }


    private  void verifyComponentDBpedia(String line,NodeList nSentenceList, Set<String> listComponent) {
       // listComponent.add("acide hyaluronique");
        for (String component : listComponent) {
            //parcourir la sortie de Renco
            for (int sent_temp = 0; sent_temp < nSentenceList.getLength(); sent_temp++) {
                Node nSentNode = nSentenceList.item(sent_temp);
                String sentence = sentenceToString(nSentNode);
                String sentence_text=line;
                //si la phrase contient le pattern
                if (sentence.contains(component)) {
                    //construction de la SentenceRelation (id, subject, object, relation phrase, méthode, type)
                    SentenceRelation sentenceRelation = new SentenceRelation();
                    SentenceRelationId sentenceRelationId = new SentenceRelationId();
                    NodeList nTokensList = nSentNode.getChildNodes();
                    for (int currentTokensIndex = 0; currentTokensIndex < nTokensList.getLength(); currentTokensIndex++) {
                        Node nTokensNode = nTokensList.item(currentTokensIndex);
                        if (nTokensNode instanceof Element) {
                            for (int currentChildIndex = 0; currentChildIndex < nTokensNode.getChildNodes().getLength(); currentChildIndex++) {
                                Node nTokenNode = nTokensNode.getChildNodes().item(currentChildIndex);
                                if (nTokenNode instanceof Element) {

                                    Element current_element = (Element) nTokenNode;

                                    if (current_element.hasAttribute("type")) {
                                        if (current_element.getAttribute("type").equalsIgnoreCase("product")) {
                                            //construction de l'objet
                                            Token object = new Token();
                                            object.setForm(component);
                                            sentenceRelationId.setObject(object);
                                            //construction du sujet
                                            Token subject = new Token();
                                            //construction de la relation
                                            int index_Component = sentence.indexOf(component);
                                            System.out.println("indexComponent: " + index_Component);

                                            int index_subject;
                                            sentenceRelationId.setType(SentenceRelationType.hasComponent);
                                            subject = elementToToken(current_element);

                                            sentenceRelationId.setSubject(subject);
                                            index_subject=sentence.indexOf(subject.getForm());
                                            System.out.println("index_subject: "+ index_subject);
                                            if(index_Component<index_subject) {
                                                int indexRelation = index_Component + component.length();
                                                String SentenceRelation = sentence.substring(indexRelation, index_subject);
                                                sentenceRelationId.setRelation(SentenceRelation);
                                                sentenceRelationId.setSentence_text(SentenceRelation);
                                                sentenceRelationId.setSentence_text(sentence_text);
                                                sentenceRelation.setMethod(SentenceRelationMethod.dbpedia_component);
                                                list_result.add(sentenceRelation);
                                                System.out.println("Extracted: " + sentenceRelationId);

                                            }
                                            else if (index_Component>index_subject){
                                                int indexRelation = index_subject + subject.getForm().length();
                                                String SentenceRelation = sentence.substring(indexRelation, index_Component);
                                                sentenceRelationId.setRelation(SentenceRelation);
                                                sentenceRelationId.setSentence_text(sentence_text);
                                                sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                                sentenceRelation.setMethod(SentenceRelationMethod.dbpedia_component);
                                                list_result.add(sentenceRelation);
                                                System.out.println("Extracted: " + sentenceRelationId);
                                            }

                                        }
                                    }
                                }
                            }
                        }
                    }
                }

            }
        }
    }

    private  void ruleshasComponent(String input) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        StringBuilder xmlStringBuilder = new StringBuilder();
        xmlStringBuilder.append(input);
        ByteArrayInputStream in = new ByteArrayInputStream(xmlStringBuilder.toString().getBytes("UTF-8"));//"UTF-8"
        Document doc = builder.parse(in);
        doc.getDocumentElement().normalize();
        NodeList nSentenceList = doc.getElementsByTagName("sentence");
        //extractionFromDBpedia();
        //hasComponentDbpedia();


        verifyChimicalComponentDBpedia(nSentenceList, listChimicalComponent);
       // verifyComponentDBpedia (nSentenceList, listComponent);
    }
    @Override
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
            if(relationMethods.contains(SentenceRelationMethod.dbpedia_chimical_component)){
                System.out.println("Selected"+sentenceRelationId + "" + SentenceRelationMethod.dbpedia_chimical_component);
                Model model=constructModel (sentenceRelationId);
                writeRdf(model);
            } else if(relationMethods.contains(SentenceRelationMethod.dbpedia_component)){
                System.out.println("Selected"+sentenceRelationId + "" + SentenceRelationMethod.dbpedia_component);
                Model model=constructModel (sentenceRelationId);
                writeRdf(model);
            }
        }
    }

    @Override
    public void processExtraction(String line) throws Exception {
        RENCO renco = new RENCO();
        ruleshasComponent(renco.rencoByWebService(line));
    }
    @Override
    public void processGlobal() throws Exception {
        extractionComponentFromDBpedia();
        super.processGlobal();
    }


}


