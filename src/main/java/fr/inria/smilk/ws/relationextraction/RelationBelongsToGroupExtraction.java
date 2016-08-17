package fr.inria.smilk.ws.relationextraction;

import com.hp.hpl.jena.rdf.model.Model;
import fr.inria.smilk.ws.relationextraction.bean.*;
import org.apache.commons.lang3.StringUtils;
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
 * Created by dhouib on 04/07/2016.
 */
public class RelationBelongsToGroupExtraction extends AbstractRelationExtraction {


    @Override
    public void annotationData(List<SentenceRelation> list_result) throws IOException {
        // on construit un map (SentenceRelationId,List<SentenceRelationMethod>)
        Map<SentenceRelationId, List<SentenceRelationMethod>> relationMap = new HashMap<>();
        //construction de list_result contenant les méthodes appliquées pour la même sentence
        for (SentenceRelation sentence_relation : list_result) {
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
            if (relationMethods.contains(SentenceRelationMethod.rulesBelongsToGroup)) {
                System.out.println("Selected" + sentenceRelationId + "" + SentenceRelationMethod.rulesBelongsToGroup);
                Model model = constructModel(sentenceRelationId);
                writeRdf(model);
            }
        }
    }

    @Override
    public void processExtraction(String line) throws Exception {
        Renco renco = new Renco();
        rulesBelongsToGroup(line, renco.rencoByWebService(line));


    }

    //recherche de la relation belongsToGroup
    private void rulesBelongsToGroup(String line, String input) throws ParserConfigurationException, IOException, SAXException {
        Map<String, String> divisionGroupMap = new HashMap<>();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        StringBuilder xmlStringBuilder = new StringBuilder();
        xmlStringBuilder.append(input);
        ByteArrayInputStream in = new ByteArrayInputStream(xmlStringBuilder.toString().getBytes("UTF-8"));//"UTF-8"
        Document doc = builder.parse(in);
        doc.getDocumentElement().normalize();
        NodeList nSentenceList = doc.getElementsByTagName("sentence");
        VerifyTypeBasedOnFarhadAproach(line, nSentenceList, "Division", "Group");
        divisionGroupMap = findByTypes(line, nSentenceList, "Division", "Group");
    }

    private void VerifyTypeBasedOnFarhadAproach(String line, NodeList nSentenceList, String firstType, String secondType) {
        List<Spot> list_spot;
        list_spot = readFileJson();
        Map<String, String> productBrandMap = new HashMap<>();
        for (int sent_temp = 0; sent_temp < nSentenceList.getLength(); sent_temp++) {
            Node nSentNode = nSentenceList.item(sent_temp);
            StringBuilder builder = new StringBuilder();
            String sentence = line;
            System.out.println("sentence:" + builder.toString());
            NodeList nTokensList = nSentNode.getChildNodes();
            //parcourir l'arbre Renco
            for (int token_temp = 0; token_temp < nTokensList.getLength(); token_temp++) {
                Node nTokenNode = nTokensList.item(token_temp);
                NodeList nList = nTokenNode.getChildNodes();
                Node nNode = nList.item(token_temp);
                int x = 0, y = 0;
                for (int j = 0; j < nList.getLength(); j++) {
                    Node xNode = nList.item(j);
                    if (xNode instanceof Element) {
                        Element xElement = (Element) xNode;

                        if (!xElement.hasAttribute("type") || xElement.getAttribute("type").equalsIgnoreCase("not_identified")) {
                            System.out.println("not_type: " + xElement.getAttribute("form") + " type: " + xElement.getAttribute("type"));
                            Token token = elementToToken(xElement);
                            Spot spot = new Spot();


                            spot = searchSpotByForm(list_spot, token.getForm());
                            System.out.println("nex Type: " + spot.getSpot() + " type: " + spot.getType());
                            xElement.setAttribute("type", spot.getType());
                            token.setLink(spot.getLink());
                            token.setType(spot.getType());

                        }
                    }
                }
            }
        }
        // productBrandMap = findByTypes(line,nSentenceList,"product","brand");
    }

    //recherche de la relation en se basant sur la règle belongsToBrand=product->brand
    private Map<String, String> findByTypes(String line, NodeList nSentenceList, String firstType, String secondType) {
        Map<String, String> firstTypeSecondTypeMap = new HashMap<>();
        List<Spot> list_spot;
        list_spot = readFileJson();
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
                        if (xElement.hasAttribute("type") && !xElement.getAttribute("type").equalsIgnoreCase("not_identified") &&
                                (xElement.getAttribute("type").equalsIgnoreCase(firstType) ||
                                        xElement.getAttribute("type").equalsIgnoreCase(secondType))) {

                            Token subjectToken = elementToToken(xElement);
                            Spot spot = searchSpotByForm(list_spot, subjectToken.getForm());
                            subjectToken.setLink(spot.getLink());
                            y = x + 1;
                            LinkedList<Token> relationTokens = new LinkedList<>();
                            for (int j = y; j < nList.getLength(); j++) {
                                Node yNode = nList.item(j);
                                if (yNode instanceof Element) {
                                    Element yElement = (Element) yNode;
                                    if ((!yElement.hasAttribute("type") || StringUtils.isBlank(yElement.getAttribute("type")) || yElement.getAttribute("type").equalsIgnoreCase("not_identified")))
                                    {
                                        Token relationToken = elementToToken(yElement);
                                        relationTokens.add(relationToken);

                                    } else {
                                        Token objectToken = elementToToken(yElement);
                                        spot = searchSpotByForm(list_spot, objectToken.getForm());
                                        objectToken.setLink(spot.getLink());
                                        if ((xElement.getAttribute("type").equalsIgnoreCase(firstType) &&
                                                yElement.getAttribute("type").equalsIgnoreCase(secondType))) {
                                            StringBuilder relation = new StringBuilder();

                                            for (Token t : relationTokens) {
                                                relation.append(t.getForm()).append(" ");
                                            }
                                            SentenceRelation sentenceRelation = new SentenceRelation();
                                            SentenceRelationId sentenceRelationId = new SentenceRelationId();
                                            sentenceRelationId.setSubject(subjectToken);
                                            sentenceRelationId.setObject(objectToken);
                                            sentenceRelationId.setRelation(relation.toString());
                                            sentenceRelationId.setSentence_text(sentence);
                                            sentenceRelationId.setType(SentenceRelationType.belongsToGroup);
                                            sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                            sentenceRelation.setMethod(SentenceRelationMethod.rulesBelongsToGroup);
                                            list_result.add(sentenceRelation);
                                        } else if (xElement.getAttribute("type").equalsIgnoreCase(secondType) &&
                                                yElement.getAttribute("type").equalsIgnoreCase(firstType)) {
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
                                            sentenceRelationId.setType(SentenceRelationType.belongsToGroup);
                                            sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                            sentenceRelation.setMethod(SentenceRelationMethod.rulesBelongsToGroup);
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
    public void init() throws Exception {

    }

}
