package fr.inria.smilk.ws.relationextraction.tools_test;

import com.hp.hpl.jena.rdf.model.Model;
import fr.inria.smilk.ws.relationextraction.AbstractRelationExtraction;
import fr.inria.smilk.ws.relationextraction.Renco;
import fr.inria.smilk.ws.relationextraction.bean.*;
import it.uniroma1.lcl.babelfy.commons.BabelfyConstraints;
import it.uniroma1.lcl.babelfy.commons.BabelfyParameters;
import it.uniroma1.lcl.babelfy.commons.BabelfyToken;
import it.uniroma1.lcl.babelfy.commons.annotation.SemanticAnnotation;
import it.uniroma1.lcl.babelfy.commons.annotation.TokenOffsetFragment;
import it.uniroma1.lcl.babelfy.core.Babelfy;
import it.uniroma1.lcl.babelnet.BabelNet;
import it.uniroma1.lcl.babelnet.BabelSynset;
import it.uniroma1.lcl.babelnet.BabelSynsetID;
import it.uniroma1.lcl.babelnet.InvalidBabelSynsetIDException;
import it.uniroma1.lcl.babelnet.data.BabelCategory;
import it.uniroma1.lcl.jlt.util.Language;
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
import java.net.URL;
import java.util.*;

import static fr.inria.smilk.ws.relationextraction.ExtractionHelper.constructModel;
import static fr.inria.smilk.ws.relationextraction.ExtractionHelper.elementToToken;
import static fr.inria.smilk.ws.relationextraction.ExtractionHelper.writeRdf;

/**
 * Created by dhouib on 04/10/2016.
 */
public class BabelfyeTest extends AbstractRelationExtraction {

    Set<String> component = new HashSet<>();
    Set<String> creator = new HashSet<>();
    Set<String> ambassador = new HashSet<>();

    private void startTools(String line, String input) throws ParserConfigurationException, IOException, SAXException, InvalidBabelSynsetIDException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        StringBuilder xmlStringBuilder = new StringBuilder();
        xmlStringBuilder.append(input);
          System.out.println(input);
        ByteArrayInputStream in = new ByteArrayInputStream(xmlStringBuilder.toString().getBytes("UTF-8"));
        Document doc = builder.parse(in);
        doc.getDocumentElement().normalize();
        NodeList nSentenceList = doc.getElementsByTagName("sentence");
        annotatedByBabelFy(line,  nSentenceList);



    }

    public  void annotatedByBabelFy(String line, NodeList nSentenceList) throws InvalidBabelSynsetIDException, IOException {

        BabelNet bn = BabelNet.getInstance();
        Babelfy bfy = new Babelfy();
        List<SemanticAnnotation> bfyAnnotations = bfy.babelfy(line, Language.FR);
        for (SemanticAnnotation annotation : bfyAnnotations) {
            //splitting the input text using the CharOffsetFragment start and end anchors
            String frag = line.substring(annotation.getCharOffsetFragment().getStart(),
                    annotation.getCharOffsetFragment().getEnd() + 1);

            BabelSynset by = bn.getSynset(new BabelSynsetID(annotation.getBabelSynsetID()));
             // System.out.println(by);
            List<BabelCategory> cats = by.getCategories();
          //  System.out.println(cats);
            for (BabelCategory category : cats) {
               /* if (category.getCategory().equalsIgnoreCase("Living_people")) {
                    System.out.println("hasAmbassador: " + frag);
                    extractRelations(line, frag, nSentenceList,"ambasador");
                } else if (category.getCategory().equalsIgnoreCase("Essential_oils") || category.getCategory().equalsIgnoreCase("Spices")
                        || category.getCategory().equalsIgnoreCase("Non-timber_forest_products")
                        || category.getCategory().equalsIgnoreCase("IARC_Group_3_carcinogens") || category.getCategory().equalsIgnoreCase("Substance_intoxication")
                        || category.getCategory().equalsIgnoreCase("Plant_toxin_insecticides") || category.getCategory().equalsIgnoreCase("Flavors")
                        || category.getCategory().equalsIgnoreCase("Dermatologic_drugs") || category.getCategory().equalsIgnoreCase("Confectionery")
                        || category.getCategory().equalsIgnoreCase("Medicinal_plants") ||/* category.getCategory().equalsIgnoreCase("Nymphaea")
                        || category.getCategory().equalsIgnoreCase("Senna") || *//*category.getCategory().equalsIgnoreCase("Plants_described_in_1753")
                        || category.getCategory().equalsIgnoreCase("Anti-aging_substances") || category.getCategory().equalsIgnoreCase("Acid–base_chemistry")
                        || category.getCategory().equalsIgnoreCase("Iridaceae_genera") || category.getCategory().equalsIgnoreCase("Flowers")
                        || category.getCategory().equalsIgnoreCase("Plants_used_in_traditional_Chinese_medicine") || category.getCategory().equalsIgnoreCase("IARC_Group_2B_carcinogens")
                        || category.getCategory().equalsIgnoreCase("Tropical_agriculture")) {
                    System.out.println("hasCompount: " + frag);
                    extractRelations(line, frag, nSentenceList,"component");*/

              if (category.getCategory().equalsIgnoreCase("Actrice_américaine")) {
                    System.out.println("hasAmbassador: " + frag);
                  ambassador.add(frag);
                    //extractRelations(line, frag, nSentenceList,"ambasador");
                } else if (category.getCategory().equalsIgnoreCase("Épice") || category.getCategory().equalsIgnoreCase("Confiserie")
                        || category.getCategory().equalsIgnoreCase("Flore_(nom_vernaculaire)")
                        || category.getCategory().equalsIgnoreCase("Flore_(nom_scientifique)") || category.getCategory().equalsIgnoreCase("Plante_médicinale")
                        || category.getCategory().equalsIgnoreCase("Cancérogène_du_groupe_3_du_CIRC") || category.getCategory().equalsIgnoreCase("Plante_à_parfum")
                        || category.getCategory().equalsIgnoreCase("Caféine") || category.getCategory().equalsIgnoreCase("Édulcorant")
                        || category.getCategory().equalsIgnoreCase("Arôme") ||category.getCategory().equalsIgnoreCase("Cétone")
                        || category.getCategory().equalsIgnoreCase("Amide") || category.getCategory().equalsIgnoreCase("Lipide")
                        || category.getCategory().equalsIgnoreCase("Protéine") || category.getCategory().equalsIgnoreCase("Peptide")
                        || category.getCategory().equalsIgnoreCase("Diholoside") || category.getCategory().equalsIgnoreCase("Vitamine")
                        || category.getCategory().equalsIgnoreCase("Pigment") || category.getCategory().equalsIgnoreCase("Cuir")
                        || category.getCategory().equalsIgnoreCase("Médicament") || category.getCategory().equalsIgnoreCase("Plante_médicinale_utilisée_pour_ses_fleurs")
                        ) {
                    System.out.println("hasCompount: " + frag);
                    component.add(frag.trim());
                    //extractRelations(line, frag, nSentenceList, "component");
                }

                else if(category.getCategory().equalsIgnoreCase("Parfumeur_français") || category.getCategory().equalsIgnoreCase("Parfumeur_espagnol")){
                    System.out.println("hasCompount: " + frag);
                    creator.add(frag.trim());
                    //extractRelations(line, frag, nSentenceList, "creator");
                }
            }
        }
component.remove("parfum");
component.remove("parfums");
        for (String cpt : component) {

            findByTypes(line, nSentenceList, "product", cpt, "component");
        }

        for (String crt : creator) {

            findByTypes(line, nSentenceList, "product", crt, "creator");
        }

        for (String crt : ambassador) {

            findByTypes(line, nSentenceList, "product", crt, "ambassador");
        }

        }

   /* private void extractRelations(String line,  String object, NodeList nSentenceList, String type_relation){
        for (String pattern : component) {
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
                                        if (type_relation.equalsIgnoreCase("component")) {
                                            sentenceRelationId.setType(SentenceRelationType.hasComponent);
                                        } else if (type_relation.equalsIgnoreCase("creator")) {
                                            sentenceRelationId.setType(SentenceRelationType.hasFragranceCreator);
                                        } else if (type_relation.equalsIgnoreCase("ambasador")) {
                                            sentenceRelationId.setType(SentenceRelationType.hasRepresentative);
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
    }*/

    //recherche de la relation en se basant sur la règle belongsToBrand=product->brand
    private Map<String, String> findByTypes(String line, NodeList nSentenceList, String firstType, String secondType, String relationType) {
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
                                            if(relationType.equalsIgnoreCase("component")){
                                                sentenceRelationId.setType(SentenceRelationType.hasComponent);
                                            }
                                            else if (relationType.equalsIgnoreCase("creator")){
                                                sentenceRelationId.setType(SentenceRelationType.hasRepresentative);
                                            }
                                           else if(relationType.equalsIgnoreCase("ambassador")){
                                                sentenceRelationId.setType(SentenceRelationType.hasRepresentative);
                                            }
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
                                            if(relationType.equalsIgnoreCase("component")){
                                                sentenceRelationId.setType(SentenceRelationType.hasComponent);
                                            }
                                            else if (relationType.equalsIgnoreCase("creator")){
                                                sentenceRelationId.setType(SentenceRelationType.hasRepresentative);
                                            }
                                            else if(relationType.equalsIgnoreCase("ambassador")){
                                                sentenceRelationId.setType(SentenceRelationType.hasRepresentative);
                                            }
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

    @Override
    public void annotationData(List<SentenceRelation> list_result) throws IOException {

        Map<SentenceRelationId, List<SentenceRelationMethod>> relationMap = new HashMap<>();
        //construction de list_result contenant les méthodes appliquées pour la même sentence
        for (SentenceRelation sentence_relation : list_result) {
           // System.out.println("sentence_relation:" + sentence_relation + sentence_relation.getSentenceRelationId());
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
