package fr.inria.smilk.ws.relationextraction;

import com.google.gson.JsonObject;
import com.hp.hpl.jena.rdf.model.Model;
import fr.inria.smilk.ws.relationextraction.bean.*;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static fr.inria.smilk.ws.relationextraction.ExtractionHelper.*;

/**
 * Created by dhouib on 04/07/2016.
 */
public class RelationBelongsToDivisionExtraction extends AbstractRelationExtraction {

    static HashMap<String,  Set<String>> hmap = new HashMap<String,  Set<String>>();

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
            if (relationMethods.contains(SentenceRelationMethod.rulesBelongsToDivision)) {
                System.out.println("Selected" + sentenceRelationId + "" + SentenceRelationMethod.rulesBelongsToDivision);
                Model model = constructModel(sentenceRelationId);
                writeRdf(model);
            }
        }
    }

    @Override
    public void processExtraction(String line) throws Exception {
        Renco renco = new Renco();
        rulesBelongsToDivision(line, renco.rencoByWebService(line));
    }

    //recherche de la relation belongsTodivision
    private void rulesBelongsToDivision(String line, String input) throws ParserConfigurationException, IOException, SAXException {
        Map<String, String> divisionGroupMap = new HashMap<>();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        StringBuilder xmlStringBuilder = new StringBuilder();
        xmlStringBuilder.append(input);
        ByteArrayInputStream in = new ByteArrayInputStream(xmlStringBuilder.toString().getBytes("UTF-8"));//"UTF-8"
        Document doc = builder.parse(in);
        doc.getDocumentElement().normalize();
        NodeList nSentenceList = doc.getElementsByTagName("sentence");
        String link = "https://fr.wikipedia.org/w/api.php?action=query&titles=L%27Oréal&prop=revisions&rvprop=content&format=json";
        JsonObject json = readJsonFromUrl(link);
        //   System.out.println(json.get("query"));
        try {
            readJson(json,line, nSentenceList);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        VerifyTypeBasedOnFarhadAproach(line, nSentenceList, "brand", "Division");
        divisionGroupMap = findByTypes(line, nSentenceList, "brand", "Division");
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




    public void readJson(JsonObject json,String line, NodeList nSentenceList) throws ParseException {
        JSONParser parser = new JSONParser();
        Object obj = parser.parse(String.valueOf(json.get("query")));
        JSONObject pages = (JSONObject) ((JSONObject) obj).get("pages");
        JSONObject id = (JSONObject) (pages).get("49477");
        JSONObject jsonObject1 = id;
        JSONArray revision = (JSONArray) jsonObject1.get("revisions");
        Iterator iterator1 = revision.iterator();
        String content = new String();
        String text = "\\n\\n===== Cosmétique active =====\\nCette division, regroupant des marques des soins dermocosmétiques sur conseils et prescriptions, propose des produits de haute technicité :\\n* '''''[[Vichy (marque)|Vichy]]''''', fondée en [[1931]] par le docteur Haller et [[Georges Guérin]] à la suite de sa visite la même année des thermes de la ville de [[Vichy]] (le docteur Haller étant à cette époque directeur des centres thermaux de [[Vichy]] et [[Georges Guérin]] à la tête d'une unité de production mécanisée concentrée sur la parfumerie) et acquise en [[1955]].\\n** Parmi les gammes de Vichy, on trouve les marques Dermablend, Dercos, Basic Homme, [[Capital soleil|Capital Soleil]]…\\n* ";
        while (iterator1.hasNext()) {
            JSONObject innerObj = (JSONObject) iterator1.next();
            content = innerObj.get("*").toString();
        }
        // System.out.println(content);
        String div = new String();   String brand = new String();
        String [] sections=content.split("\n=====");
        for (String section : sections) {
            // System.out.println("section: " + section + "  \n ennnnnnnnnnnnnnnd");
            if(section.indexOf("Cette division")>=0) {
                if (section.indexOf("=====") >= 0) {
                    div = section.substring(0, section.indexOf("====="));
                    // System.out.println("div: " + div);

                }
                if (section.indexOf("''''") >= 0) {
                    String Sectionbrand = section.substring(section.indexOf("'''''"));
                    String [] lines_brands=Sectionbrand.split(",");
                    for (String lin: lines_brands) {
                        Pattern p_brand = Pattern.compile("'''''(.)*");
                        // Pattern p_brand =Pattern.compile("'''''\\[\\[.*\\]\\]'''''");
                        Matcher m_brand = p_brand.matcher(lin);
                        boolean b_brand = m_brand.find();
                        if (b_brand) {
                            System.out.println(m_brand.group(0));

                            if ((m_brand.group(0).indexOf("'''''[[") >= 0) && (m_brand.group(0).indexOf("]]'''''") >= 0)) {
                                int index_start_brand = m_brand.group(0).indexOf("'''''[[");
                                int index_end_brand = m_brand.group(0).indexOf("]]");
                                brand = m_brand.group(0).substring(index_start_brand + 7, index_end_brand);
                                if (m_brand.group(0).indexOf("|") >= 0) {
                                    int index_brand = brand.indexOf("|") + 1;
                                    brand = brand.substring(index_brand);
                                }
                            } else if ((m_brand.group(0).indexOf("''") >= 0)) {
                                //m_brand.group(0).replace("''", "''''' ");
                                int index_brand1 = m_brand.group(0).indexOf("''") + 3;
                                brand = m_brand.group(0).substring(index_brand1);
                                brand = brand.replace("'''''", " ");
                                brand = brand.replace("''", " ");
                            }


                            if (brand.indexOf("(") > 0) {
                                brand = brand.substring(0, brand.indexOf("("));
                            }

                            if (brand.indexOf("<") > 0) {
                                brand = brand.substring(0, brand.indexOf("<"));
                            }

                        }
                        addToMap(div.trim(), brand.trim());
                    }
                    // System.out.println("div: " + div+"brand: " + brand);
                }

                // System.out.println("brand: " + brand);
            }
        }
       /* Set set = hmap.entrySet();
        Iterator iteratorhmap = set.iterator();
        while (iteratorhmap.hasNext()) {
            Map.Entry mentry = (Map.Entry) iteratorhmap.next();
            Object key = mentry.getKey();
            List<String> values = (List<String>) mentry.getValue();
            System.out.println("Key: " + key + " Values: " + values);*/


            for (int sent_temp = 0; sent_temp < nSentenceList.getLength(); sent_temp++) {
                Node nSentNode = nSentenceList.item(sent_temp);
                StringBuilder builder = new StringBuilder();
                String sentence = line;
                // System.out.println("sentence:" + builder.toString());
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
                                //    System.out.println("xElement: "+xElement.getAttribute("form")+ "   "+ xElement.getAttribute("type"));
                                Set set = hmap.entrySet();
                                Iterator iteratorhmap = set.iterator();
                                while (iteratorhmap.hasNext()) {
                                    Map.Entry mentry = (Map.Entry) iteratorhmap.next();
                                    Object key = mentry.getKey();
                                    Set<String> values = (Set<String>) mentry.getValue();
                                    System.out.println("Key: " + key + " Values: " + values);
                                    if(key.equals(xElement.getAttribute("form"))){
                                        // System.out.println("wikipedia: "+key+ "  "+ xElement.getAttribute("form"));
                                        Token token = elementToToken(xElement);
                                        xElement.setAttribute("type", "division");
                                        token.setType("division");
                                    }
                                    for (String value:values) {
                                        if (value.equals(xElement.getAttribute("form"))) {
                                            // System.out.println("wikipedia: " + values + "  " + xElement.getAttribute("form"));
                                            Token token = elementToToken(xElement);
                                            xElement.setAttribute("type", "brand");
                                            token.setType("brand");
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

        }




    private static void addToMap(String div, String brand) {
        if(!hmap.containsKey(div)){
            hmap.put(div, new HashSet<String>());
        }
        hmap.get(div).add(brand);
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
            System.out.println("sentence:" + builder.toString());
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
                                        System.out.println("spooooot: " + spot.getLink());
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
                                            sentenceRelationId.setType(SentenceRelationType.belongsToDivision);
                                            sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                            sentenceRelation.setMethod(SentenceRelationMethod.rulesBelongsToDivision);
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
                                            sentenceRelationId.setType(SentenceRelationType.belongsToDivision);
                                            sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                            sentenceRelation.setMethod(SentenceRelationMethod.rulesBelongsToDivision);
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
