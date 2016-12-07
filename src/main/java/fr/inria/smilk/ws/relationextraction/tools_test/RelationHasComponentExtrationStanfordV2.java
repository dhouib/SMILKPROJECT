package fr.inria.smilk.ws.relationextraction.tools_test;

import com.hp.hpl.jena.rdf.model.Model;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.PropertiesUtils;
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
 * Created by dhouib on 01/07/2016.
 */

/*Cherche les relations hasComponent dans le texte */
public class RelationHasComponentExtrationStanfordV2 extends AbstractRelationExtraction {
    static HashMap<String, String> form_lemma = new HashMap<String, String>();

    private void startTools(String line, String input) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        StringBuilder xmlStringBuilder = new StringBuilder();
        xmlStringBuilder.append(input);
      //  System.out.println(input);
        ByteArrayInputStream in = new ByteArrayInputStream(xmlStringBuilder.toString().getBytes("UTF-8"));
        Document doc = builder.parse(in);
        doc.getDocumentElement().normalize();
        NodeList nSentenceList = doc.getElementsByTagName("sentence");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(
                PropertiesUtils.asProperties(
                        "annotators", "tokenize, ssplit, pos, depparse, parse, lemma",
                        "tokenize.language", "fr",
                        //"ssplit.isOneSentence", "true",
                        "pos.model", "edu/stanford/nlp/models/pos-tagger/french/french.tagger",
                        "parse.model", "edu/stanford/nlp/models/lexparser/frenchFactored.ser.gz",
                        "depparse.model", "edu/stanford/nlp/models/parser/nndep/UD_French.gz"));
        // create an empty Annotation just with the given text
        Annotation document = new Annotation(line);
        // run all Annotators on this text
        pipeline.annotate(document);
        // these are all the sentences in this document
        // a CoreMap is essentially a Map that uses class objects as keys and has values with custom types
        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
        modifyLemmaStanford(line, nSentenceList, sentences);
    }

    private  void modifyLemmaStanford (String line, NodeList nSentenceList, List<CoreMap> sentences) throws IOException {
        //   System.out.println(token_renco.getLema());
        for (CoreMap sentence : sentences) {
            // this is the Stanford dependency graph of the current sentence
            SemanticGraph dependencies = sentence.get(SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation.class);
           // System.out.println("dependency graph:\n" + dependencies);
           // System.out.println("vertew: " + dependencies.vertexSet());
            Set<IndexedWord> vetex_set = dependencies.vertexSet();
            getNewLemma(line, nSentenceList, sentences);
            //System.out.println(form_lemma);
            Set set = form_lemma.entrySet();
            for (IndexedWord v : vetex_set) {
                Iterator iteratorhmap = set.iterator();
                while (iteratorhmap.hasNext()) {
                    Map.Entry mentry = (Map.Entry) iteratorhmap.next();
                    Object key = mentry.getKey();
                    if (key.equals(v.originalText())) {
                        v.setLemma(form_lemma.get(key));
                    }

                    }
                extractPatterns(line, v,nSentenceList, dependencies );
            }

        }
    }

    private void extractPatterns(String line, IndexedWord v,  NodeList nSentenceList, SemanticGraph dependencies ){

        if( v.lemma().equalsIgnoreCase("contenir") || v.lemma().equalsIgnoreCase("associer")
             ){
            List<SemanticGraphEdge> outcomingEdgesSorted = dependencies.getOutEdgesSorted(v);
            for(SemanticGraphEdge edge : outcomingEdgesSorted) {
                GrammaticalRelation relation = edge.getRelation();
                if (relation.getShortName().contentEquals("nmod")||relation.getShortName().contentEquals("dobj")) {
                  //  System.out.println("-----> Relation=" + relation.getShortName());
                    IndexedWord dep = edge.getDependent();
                  //  System.out.println("Dependent_target=" + dep);
                    extractRelations(line, dep.originalText(),  nSentenceList, "component");
                    // Getting the source node with attached edges
                    IndexedWord gov = edge.getGovernor();
                  //  System.out.println("Governor_source=" + gov);
                }
            }
        }

        if(v.lemma().equalsIgnoreCase("reposer")){
            List<SemanticGraphEdge> outcomingEdgesSorted = dependencies.getOutEdgesSorted(v);
            for(SemanticGraphEdge edge : outcomingEdgesSorted) {
                if (edge.getRelation().getShortName().equalsIgnoreCase("nmod") ) {
                    IndexedWord word = edge.getDependent();
                    //System.out.println("word: "+word.originalText());
                    List<SemanticGraphEdge> outcomingEdgesSorted_word = dependencies.getOutEdgesSorted(word);
                    SemanticGraphEdge edgeWordWithName = null;
                    for(SemanticGraphEdge edge_word : outcomingEdgesSorted_word) {
                        if (edge_word.getRelation().getShortName().equalsIgnoreCase("name")){
                            edgeWordWithName = edge_word;
                        }
                    }
                    if(edgeWordWithName != null){
                //        System.out.println(edgeWordWithName);
                        extractRelations(line, edgeWordWithName.getTarget().originalText(),  nSentenceList, "component");
                    }else{
                        extractRelations(line, word.originalText(),  nSentenceList, "component");
                    }
                }
            }

        }

        if( v.lemma().equalsIgnoreCase("extrait") ){
            List<SemanticGraphEdge> outcomingEdgesSorted = dependencies.getOutEdgesSorted(v);
            for(SemanticGraphEdge edge : outcomingEdgesSorted) {
                if ( edge.getRelation().getShortName().equalsIgnoreCase("nmod") || edge.getRelation().getShortName().equalsIgnoreCase("conj")  ) {
                    IndexedWord word = edge.getDependent();
                    //System.out.println("word: "+word.originalText());
                    List<SemanticGraphEdge> outcomingEdgesSorted_word = dependencies.getOutEdgesSorted(word);
                    SemanticGraphEdge edgeWordWithName = null;
                    for(SemanticGraphEdge edge_word : outcomingEdgesSorted_word) {
                        if (edge_word.getRelation().getShortName().equalsIgnoreCase("amod") || edge_word.getRelation().getShortName().equalsIgnoreCase("nmod")){
                            edgeWordWithName = edge_word;
                        }
                    }
                    if(edgeWordWithName != null){
               //         System.out.println(edgeWordWithName);
                        extractRelations(line, word.originalText().concat(" "+edgeWordWithName.getTarget().originalText()),  nSentenceList, "component");
                    }else{
                        extractRelations(line, word.originalText(),  nSentenceList, "component");
                    }
                }
            }

        }

        if( v.lemma().equalsIgnoreCase("acide") ){
            List<SemanticGraphEdge> outcomingEdgesSorted = dependencies.getOutEdgesSorted(v);
            for(SemanticGraphEdge edge : outcomingEdgesSorted) {
                if ( edge.getRelation().getShortName().equalsIgnoreCase("amod") ) {
                    IndexedWord word = edge.getDependent();
                 //   System.out.println("word: "+word.originalText());
                    List<SemanticGraphEdge> outcomingEdgesSorted_word = dependencies.getOutEdgesSorted(word);
                    SemanticGraphEdge edgeWordWithName = null;
                    for(SemanticGraphEdge edge_word : outcomingEdgesSorted_word) {
                        if (edge_word.getRelation().getShortName().equalsIgnoreCase("conj")){
                            edgeWordWithName = edge_word;
                        }
                    }
                    if(edgeWordWithName != null){
                      //  System.out.println(edgeWordWithName);
                        extractRelations(line, word.originalText().concat(" "+edgeWordWithName.getTarget().originalText()),  nSentenceList, "component");
                    }else{
                        extractRelations(line, word.originalText(),  nSentenceList, "component");
                    }
                }
            }

        }

        if((v.lemma().equalsIgnoreCase("coeur")) || (v.lemma().equalsIgnoreCase("rafraîchir"))){
            List<SemanticGraphEdge> outcomingEdgesSorted = dependencies.getOutEdgesSorted(v);
            for(SemanticGraphEdge edge : outcomingEdgesSorted) {
                if (edge.getRelation().getShortName().equalsIgnoreCase("nmod")) {
                    IndexedWord word = edge.getDependent();
                 //   System.out.println("word: "+word.originalText());
                    List<SemanticGraphEdge> outcomingEdgesSorted_word = dependencies.getOutEdgesSorted(word);
                    SemanticGraphEdge edgeWordWithName = null;
                    for(SemanticGraphEdge edge_word : outcomingEdgesSorted_word) {
                        if (edge_word.getRelation().getShortName().equalsIgnoreCase("nmod")){
                            edgeWordWithName = edge_word;
                        }
                    }
                    if(edgeWordWithName != null){
                        extractRelations(line, word.originalText().concat(" "+edgeWordWithName.getTarget().originalText()),  nSentenceList, "component");
                    }else{
                        extractRelations(line, word.originalText(),  nSentenceList, "component");
                    }
                }
            }

        }

        if(v.lemma().equalsIgnoreCase("orchestrer")) {
            List<SemanticGraphEdge> outcomingEdgesSorted = dependencies.getOutEdgesSorted(v);
            for (SemanticGraphEdge edge : outcomingEdgesSorted) {
                if (edge.getRelation().getShortName().equalsIgnoreCase("dobj")) {
                    IndexedWord word = edge.getDependent();
                //    System.out.println("word: " + word.originalText());

                    List<SemanticGraphEdge> outcomingEdgesSorted_word = dependencies.getOutEdgesSorted(word);
                    for (SemanticGraphEdge edge_word : outcomingEdgesSorted_word) {
                        if (edge_word.getRelation().getShortName().equalsIgnoreCase("appos")) {
                         //   System.out.println(edge_word);
                            String object= edge_word.getTarget().originalText().concat(" "+word.originalText());
                            extractRelations(line, object, nSentenceList,"component");
                        }
                    }
                }
            }
        }

        if(v.lemma().equalsIgnoreCase("doter")) {
            List<SemanticGraphEdge> outcomingEdgesSorted = dependencies.getOutEdgesSorted(v);
            for (SemanticGraphEdge edge : outcomingEdgesSorted) {
                if (edge.getRelation().getShortName().equalsIgnoreCase("nmod")) {
                    IndexedWord word = edge.getDependent();
                  //  System.out.println("word: " + word.originalText());

                    List<SemanticGraphEdge> outcomingEdgesSorted_word = dependencies.getOutEdgesSorted(word);
                    for (SemanticGraphEdge edge_word : outcomingEdgesSorted_word) {
                        if (edge_word.getRelation().getShortName().equalsIgnoreCase("amod")) {
                         //   System.out.println(edge_word);
                            String object= edge_word.getTarget().originalText().concat(" "+word.originalText());
                            extractRelations(line, object, nSentenceList,"component");
                        }
                    }
                }
            }
        }
        if(v.lemma().equalsIgnoreCase("rassembler")) {
            List<SemanticGraphEdge> outcomingEdgesSorted = dependencies.getOutEdgesSorted(v);
            for (SemanticGraphEdge edge : outcomingEdgesSorted) {
                if (edge.getRelation().getShortName().equalsIgnoreCase("dobj")) {
                    IndexedWord word = edge.getDependent();
               //     System.out.println("word: "+word.originalText());
                    List<SemanticGraphEdge> outcomingEdgesSorted_word = dependencies.getOutEdgesSorted(word);
                    SemanticGraphEdge edgeWordWithName = null;
                    for(SemanticGraphEdge edge_word : outcomingEdgesSorted_word) {
                        if (edge_word.getRelation().getShortName().equalsIgnoreCase("conj")){
                  //          System.out.println(edge_word);
                            //extractRelations(line, object, nSentenceList,"component");
                        }
                    }
                }
            }
        }

        if((v.lemma().equalsIgnoreCase("associer"))){
            List<SemanticGraphEdge> outcomingEdgesSorted = dependencies.getOutEdgesSorted(v);
            for(SemanticGraphEdge edge : outcomingEdgesSorted) {
                GrammaticalRelation relation = edge.getRelation();
                if (relation.getShortName().contentEquals("dobj")) {
                 //   System.out.println("-----> Relation=" + relation.getShortName());
                    IndexedWord dep = edge.getDependent();
                  //  System.out.println("Dependent_target=" + dep);
                    extractRelations(line, dep.originalText(),  nSentenceList, "component");
                    // Getting the source node with attached edges
                    IndexedWord gov = edge.getGovernor();
                  //  System.out.println("Governor_source=" + gov);
                }
            }
        }

        if((v.lemma().equalsIgnoreCase("construire"))){
         //   System.out.println("test true");
            List<SemanticGraphEdge> incomingEdgesSorted = dependencies.getIncomingEdgesSorted(v);
            for(SemanticGraphEdge edge : incomingEdgesSorted) {
                GrammaticalRelation relation = edge.getRelation();
                if (relation.getShortName().contentEquals("amod")) {
                  //  System.out.println("-----> Relation=" + relation.getShortName());

                    IndexedWord dep = edge.getDependent();
                  //  System.out.println("Dependent_target=" + dep);

                    // Getting the source node with attached edges
                    IndexedWord gov = edge.getGovernor();
                //    System.out.println("Governor_source=" + gov);
                    extractRelations(line, gov.originalText(),  nSentenceList, "component");
                }
            }
        }

       //hasFragranceCreator
        if(v.lemma().equalsIgnoreCase("créer")) {
            List<SemanticGraphEdge> outcomingEdgesSorted = dependencies.getOutEdgesSorted(v);
            for (SemanticGraphEdge edge : outcomingEdgesSorted) {
                if (edge.getRelation().getShortName().equalsIgnoreCase("nmod")) {
                    IndexedWord word = edge.getDependent();
                //    System.out.println("word: " + word.originalText());

                    List<SemanticGraphEdge> outcomingEdgesSorted_word = dependencies.getOutEdgesSorted(word);
                    for (SemanticGraphEdge edge_word : outcomingEdgesSorted_word) {
                        if (edge_word.getRelation().getShortName().equalsIgnoreCase("name")) {
                      //      System.out.println(edge_word);
                            String object= edge_word.getTarget().originalText().concat(" "+word.originalText());
                            extractRelations(line, object, nSentenceList,"creator");
                        }
                    }
                }
            }
        }

        if(v.lemma().equalsIgnoreCase("création") || v.lemma().equalsIgnoreCase("retravailler")) {
            List<SemanticGraphEdge> outcomingEdgesSorted = dependencies.getOutEdgesSorted(v);
            for (SemanticGraphEdge edge : outcomingEdgesSorted) {
                if (edge.getRelation().getShortName().equalsIgnoreCase("nmod")) {
                    IndexedWord word = edge.getDependent();
               //     System.out.println("word: " + word.originalText());

                    List<SemanticGraphEdge> outcomingEdgesSorted_word = dependencies.getOutEdgesSorted(word);
                    for (SemanticGraphEdge edge_word : outcomingEdgesSorted_word) {
                        if (edge_word.getRelation().getShortName().equalsIgnoreCase("name")) {
                    //        System.out.println(edge_word);
                            String object= edge_word.getTarget().originalText().concat(" "+word.originalText());
                            extractRelations(line, object, nSentenceList,"creator");
                        }
                    }
                }
            }
        }

        if(v.lemma().equalsIgnoreCase("orchestrer")) {
            List<SemanticGraphEdge> outcomingEdgesSorted = dependencies.getOutEdgesSorted(v);
            for (SemanticGraphEdge edge : outcomingEdgesSorted) {
                if (edge.getRelation().getShortName().equalsIgnoreCase("nsubj")) {
                    IndexedWord word = edge.getDependent();
                //    System.out.println("word: " + word.originalText());

                    List<SemanticGraphEdge> outcomingEdgesSorted_word = dependencies.getOutEdgesSorted(word);
                    for (SemanticGraphEdge edge_word : outcomingEdgesSorted_word) {
                        if (edge_word.getRelation().getShortName().equalsIgnoreCase("name")) {
                    //        System.out.println(edge_word);
                            String object= edge_word.getTarget().originalText().concat(" "+word.originalText());
                            extractRelations(line, object, nSentenceList,"creator");
                        }
                    }
                }
            }
        }

        if(v.lemma().equalsIgnoreCase("incerna") || v.lemma().equalsIgnoreCase("incarner")) {
            List<SemanticGraphEdge> outcomingEdgesSorted = dependencies.getOutEdgesSorted(v);
            for (SemanticGraphEdge edge : outcomingEdgesSorted) {
                if (edge.getRelation().getShortName().equalsIgnoreCase("nsubj")) {
                    IndexedWord word = edge.getDependent();
               //     System.out.println("word: " + word.originalText());

                    List<SemanticGraphEdge> outcomingEdgesSorted_word = dependencies.getOutEdgesSorted(word);
                    for (SemanticGraphEdge edge_word : outcomingEdgesSorted_word) {
                        if (edge_word.getRelation().getShortName().equalsIgnoreCase("name")) {
                  //          System.out.println(edge_word);
                            String object= edge_word.getTarget().originalText().concat(" "+word.originalText());
                            extractRelations(line, object, nSentenceList,"ambasador");
                        }
                    }
                }
            }
        }


    }


    private void extractRelations(String line,  String object, NodeList nSentenceList, String type_relation){
        List<Token> list_EN = new ArrayList<>();
        Token token_renco=new Token();
        for (int sent_temp = 0; sent_temp < nSentenceList.getLength(); sent_temp++) {
            Node nSentNode = nSentenceList.item(sent_temp);
            StringBuilder builder = new StringBuilder();
            String sentence = line;
            NodeList nTokensList = nSentNode.getChildNodes();
            //parcourir l'arbre Renco
            for (int token_temp = 0; token_temp < nTokensList.getLength(); token_temp++) {
                Node nTokenNode = nTokensList.item(token_temp);
                if (nTokenNode instanceof Element) {
                    NodeList nList = nTokenNode.getChildNodes();
                    Node nNode = nList.item(token_temp);
                    int y = 0;
                    for (int x = 0; x < nList.getLength(); x++) {
                        Node xNode = nList.item(x);
                        if (xNode instanceof Element) {
                            Element xElement = (Element) xNode;
                            //Si XElement a un type (product, range, brand, division, group
                            if (xElement.hasAttribute("type") && !xElement.getAttribute("type").equalsIgnoreCase("not_identified"))
                                if (xElement.getAttribute("type").equalsIgnoreCase("product")) {
                                    Token subjectToken = elementToToken(xElement);
                                    Token objectToken = new Token();
                                    objectToken.setForm(object);
                            //        System.out.println("Subject: "+ subjectToken.getForm()+ " Object: "+ objectToken.getForm());
                                    SentenceRelation sentenceRelation = new SentenceRelation();
                                    SentenceRelationId sentenceRelationId = new SentenceRelationId();
                                    sentenceRelationId.setSubject(subjectToken);
                                    sentenceRelationId.setObject(objectToken);
                                    sentenceRelationId.setRelation("coeur");
                                    sentenceRelationId.setSentence_text(sentence);
                                    if(type_relation.equalsIgnoreCase("component")) {
                                        sentenceRelationId.setType(SentenceRelationType.hasComponent);
                                    }
                                    else if(type_relation.equalsIgnoreCase("creator")){
                                        sentenceRelationId.setType(SentenceRelationType.hasFragranceCreator);
                                    }

                                    else if(type_relation.equalsIgnoreCase("ambasador")){
                                        sentenceRelationId.setType(SentenceRelationType.hasRepresentative );
                                    }
                                    sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                    sentenceRelation.setMethod(SentenceRelationMethod.dbpedia_chimical_component);
                                    list_result.add(sentenceRelation);
                                }

                        }
                    }
                }
            }
        }

    }
    //Identification des expressions référentielles
    private  HashMap<String, String>  getNewLemma(String line, NodeList nSentenceList, List<CoreMap> sentences) throws IOException {
        Token token_renco=new Token();
        for (int sent_temp = 0; sent_temp < nSentenceList.getLength(); sent_temp++) {
            Node nSentNode = nSentenceList.item(sent_temp);
            StringBuilder builder = new StringBuilder();
            String sentence = line;
            NodeList nTokensList = nSentNode.getChildNodes();
            //parcourir l'arbre Renco
            for (int token_temp = 0; token_temp < nTokensList.getLength(); token_temp++) {
                Node nTokenNode = nTokensList.item(token_temp);
                if (nTokenNode instanceof Element) {
                    NodeList nList = nTokenNode.getChildNodes();
                    Node nNode = nList.item(token_temp);
                    int y = 0;
                    for (int x = 0; x < nList.getLength(); x++) {
                        Node xNode = nList.item(x);
                        if (xNode instanceof Element) {
                            Element xElement = (Element) xNode;
                            token_renco=elementToToken(xElement);
                           // useNLP (token_renco, sentences );
                            form_lemma.put(token_renco.getForm(),token_renco.getLema());
                        }
                    }
                }
            }
        }

        return (form_lemma);

     /*   System.out.println("token_renco: "+ token_renco.getLema());
        return token_renco;*/

    }

    @Override
    public void annotationData(List<SentenceRelation> list_result) throws IOException {

        Map<SentenceRelationId, List<SentenceRelationMethod>> relationMap = new HashMap<>();
        //construction de list_result contenant les méthodes appliquées pour la même sentence
        for (SentenceRelation sentence_relation : list_result) {
            System.out.println("sentence_relation:" + sentence_relation + sentence_relation.getSentenceRelationId());
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
        startTools(line, renco.rencoByWebService(line));
    }


    @Override
    public void init() throws Exception {

    }

}


