package fr.inria.smilk.ws.relationextraction;

import com.google.gson.JsonObject;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Literal;
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
 * Created by dhouib on 30/06/2016.
 * Extraction of the relation belongsToBrand
 */
public class RelationBelongsToBrandExtraction extends AbstractRelationExtraction {
    //store the NE DBpedia (product, brand)
    HashMap<String, String> hmap = new HashMap<String, String>();
    //store patterns extracted from DBpedia
    Set<String> patterns = new HashSet<>();

    //store data of wikipedia
    HashMap<String, Set<String>> hmap_wiki = new HashMap<String, Set<String>>();

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
    private void verifyPatternsDBpedia(String line, NodeList nSentenceList, Set<String> patterns) {
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
                    String objet = sentence.substring(0, sentence.indexOf(pattern));
                    object.setForm(objet);
                    sentenceRelationId.setObject(object);
                    //trouver le sujet de la relation
                    NodeList nTokensList = nSentNode.getChildNodes();
                    Element token = searchToken(nTokensList, index_pattern);
                    subject = elementToToken(token);
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
    private void verifyENDBpedia(String line, NodeList nSentenceList, Map<String, String> hmap) throws
            ParserConfigurationException, IOException, SAXException {
        //parcourir le hmap
        Set set = hmap.entrySet();
        Iterator iterator = set.iterator();
        while (iterator.hasNext()) {
            Map.Entry mentry = (Map.Entry) iterator.next();
            SentenceRelation sentenceRelation = new SentenceRelation();
            //parcourir la sortie de fr.inria.smilk.ws.relationextraction.renco
            for (int sent_temp = 0; sent_temp < nSentenceList.getLength(); sent_temp++) {
                Node nSentNode = nSentenceList.item(sent_temp);
                StringBuilder builder = new StringBuilder();
                String sentence = line;
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
                            if ((xElement.getAttribute("form").equalsIgnoreCase(mentry.getKey().toString())) || (xElement.getAttribute("lemma").equalsIgnoreCase(mentry.getKey().toString()))) {
                                Token subjectToken = elementToToken(xElement);
                                y = x + 1;
                                LinkedList<Token> relationTokens = new LinkedList<>();
                                for (int j = y; j < nList.getLength(); j++) {
                                    Node yNode = nList.item(j);
                                    if (yNode instanceof Element) {
                                        Element yElement = (Element) yNode;
                                        Token objectToken = new Token();
                                        if ((!yElement.getAttribute("form").equalsIgnoreCase(mentry.getValue().toString())) || (!yElement.getAttribute("lemma").equalsIgnoreCase(mentry.getValue().toString()))) {
                                            Token relationToken = new Token();
                                            relationToken = elementToToken(yElement);
                                            relationTokens.add(relationToken);
                                        }
                                        //recherche du l'object
                                        else if ((yElement.getAttribute("form").equalsIgnoreCase(mentry.getValue().toString())) || (yElement.getAttribute("lemma").equalsIgnoreCase(mentry.getValue().toString()))) {
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
    private void rulesBelongsToBrand(String line, String input) throws ParserConfigurationException, IOException, SAXException, ParseException {
        Map<String, String> productBrandMap = new HashMap<>();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        StringBuilder xmlStringBuilder = new StringBuilder();
        xmlStringBuilder.append(input);
        System.out.println(input);
        ByteArrayInputStream in = new ByteArrayInputStream(xmlStringBuilder.toString().getBytes("UTF-8"));//"UTF-8"
        Document doc = builder.parse(in);
        doc.getDocumentElement().normalize();
        NodeList nSentenceList = doc.getElementsByTagName("sentence");
        //sercive web media_wiki avec la page weikipedia liste des parfums
        String link = "https://fr.wikipedia.org/w/api.php?action=query&titles=Liste%20de%20parfums&prop=revisions&rvprop=content&format=json";
        JsonObject json = readJsonFromUrl(link);
        //tester les types en se basant sur wikipedia
        try {
            readJson(json, line, nSentenceList);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        //tester les types en se basant sur NED
        VerifyTypeBasedOnFarhadAproach(line, nSentenceList, "product", "brand");
        verifyPatternsDBpedia(line, nSentenceList, patterns);
        verifyENDBpedia(line, nSentenceList, productBrandMap);
        //renco
        productBrandMap = findByTypes(line, nSentenceList, "product", "brand");
        productBrandMap = findByTypes(line, nSentenceList, "range", "brand");
    }

    private void VerifyTypeBasedOnFarhadAproach(String line, NodeList nSentenceList, String firstType, String secondType) {
        List<Spot> list_spot;
        list_spot = readFileJson();
        Map<String, String> productBrandMap = new HashMap<>();
        for (int sent_temp = 0; sent_temp < nSentenceList.getLength(); sent_temp++) {
            Node nSentNode = nSentenceList.item(sent_temp);
            StringBuilder builder = new StringBuilder();
            String sentence = line;
            //System.out.println("sentence:" + builder.toString());
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
                            //  System.out.println("not_type: " + xElement.getAttribute("form") + " type: " + xElement.getAttribute("type"));
                            Token token = elementToToken(xElement);
                            Spot spot = new Spot();
                            spot = searchSpotByForm(list_spot, token.getForm());
                            // System.out.println("nex Type: " + spot.getSpot() + " type: " + spot.getType());
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

    public void readJson(JsonObject json, String line, NodeList nSentenceList) throws ParseException {

        JSONParser parser = new JSONParser();
        Object obj = parser.parse(String.valueOf(json.get("query")));
        JSONObject pages = (JSONObject) ((JSONObject) obj).get("pages");
        JSONObject id = (JSONObject) (pages).get("689876");
        JSONObject jsonObject1 = id;
        JSONArray revision = (JSONArray) jsonObject1.get("revisions");
        Iterator iterator1 = revision.iterator();
        String content = new String();
        String text = "''[[Amor Amor]]'' de Cacharel, créé par Dominique Ropion et [[Laurent Bruyère]], 2003<ref>Le Guérer, ''Le Parfum'', {{p.|313.}}</ref>.\n## ''[[Amor Amor Catch Me]]'' de Cacharel, créé par Dominique Ropion, 2012<ref>{{en}} {{Lien web|url=http://www.trbusiness.com/index.php?option=com_content&view=article&id=11716:lstr-scoops-global-exclusive-with-cacharel&catid=8:international&Itemid=13|auteur=Kevin Rozario|titre=LSTR scoops global exclusive with Cachare|éditeur=|date=24 juillet 2012|site=trbusiness.com|consulté le=}}.</ref>.\n## ''[[Amor Amor Elixir]]'' de [[Cacharel (parfums)|Cacharel]]. Flacon bleu nuit. Notes : fleurs blanches sur fond de bois exotiques et de benjoin.\n## ";
        while (iterator1.hasNext()) {
            JSONObject innerObj = (JSONObject) iterator1.next();
            content = innerObj.get("*").toString();
        }
        String[] lines = content.split("\n");
        for (String linewiki : lines) {
            String subLine = line;
            if (linewiki.indexOf(',') >= 0) {
                //System.out.println("line: " + line.substring(','));
                subLine = linewiki.substring(0, linewiki.indexOf(','));

            } else if (linewiki.indexOf('.') >= 0) {
                //System.out.println("line indexof: " + line.indexOf('.')+ " ." +line);
                //System.out.println("line: " + line + " "+line.substring(line.indexOf('.')));
                subLine = linewiki.substring(0, linewiki.indexOf('.'));
            }
            //System.out.println("subLine: "+ subLine);
            // Pattern p = Pattern.compile("''(.*)''");
            //site http://www.regexr.com/
            String subsubLine = subLine;
            //pattern pour extraire le nom du parfum
            Pattern p = Pattern.compile("''\\[\\[.*\\]\\]'' ");
            Matcher m = p.matcher(subLine);
            String parfum, brand = new String();
            boolean b = m.find();
            // System.out.println(b);
            if (b) {
                // System.out.println(m.group(0));
                int index = m.group(0).indexOf("''[[");
                int index_end = m.group(0).indexOf("]]''");
                parfum = m.group(0).substring(index + 4, index_end);
                subsubLine = subLine.substring(subLine.indexOf("]]''") + 4);
                //System.out.println("subLine:" +subsubLine );
                if (parfum.contains("|")) {
                    int index_parfum = parfum.indexOf("|") + 1;
                    parfum = parfum.substring(index_parfum);
                }


                //pattern pour extriare le nom de la marque sous la forme "de [[GA]]
                Pattern p_brand = Pattern.compile("de (\\[\\[)?.*(\\]\\])?");
                Matcher m_brand = p_brand.matcher(subsubLine);
                boolean b_brand = m_brand.find();
                // System.out.println(b);
                if (b_brand) {
                    if ((m_brand.group(0).indexOf("[[") >= 0) && (m_brand.group(0).indexOf("]]") >= 0)) {
                        int index_start_brand = m_brand.group(0).indexOf("[[");
                        int index_end_brand = m_brand.group(0).indexOf("]]");
                        brand = m_brand.group(0).substring(index_start_brand + 2, index_end_brand);
                        if (m_brand.group(0).indexOf("|") >= 0) {
                            int index_brand = brand.indexOf("|") + 1;
                            brand = brand.substring(index_brand);
                        }
                    } else {
                        int index_brand1 = m_brand.group(0).indexOf("de");
                        brand = m_brand.group(0).substring(index + 2);
                    }
                }

                //pattern pour extriare le nom de la marque sous la forme "d'[[GA]]
                Pattern p_brand1 = Pattern.compile("d'(\\[\\[)?.*(\\]\\])?");
                Matcher m_brand1 = p_brand1.matcher(subsubLine);
                boolean b_brand1 = m_brand1.find();
                if (b_brand1) {
                    if ((m_brand1.group(0).indexOf("[[") >= 0) && (m_brand1.group(0).indexOf("]]") >= 0)) {
                        int index_brand1 = m_brand1.group(0).indexOf("[[");
                        int index_end_brand2 = m_brand1.group(0).indexOf("]]");
                        brand = m_brand1.group(0).substring(index_brand1 + 2, index_end_brand2);
                        if (m_brand1.group(0).indexOf("|") >= 0) {
                            int index_brand = brand.indexOf("|") + 1;
                            brand = brand.substring(index_brand);
                        }
                    } else {
                        int index_brand1 = m_brand1.group(0).indexOf("d'");
                        brand = m_brand1.group(0).substring(index + 2);
                    }
                }

                //pattern pour extriare le nom de la marque sous la forme "d'[[GA]]
                Pattern p_brand2 = Pattern.compile("créé par (\\[\\[)?.*(\\]\\])?");
                Matcher m_brand2 = p_brand2.matcher(subsubLine);
                boolean b_brand2 = m_brand2.find();


                if (b_brand2) {
                    //System.out.println("");
                    if ((m_brand2.group(0).indexOf("[[") >= 0) && (m_brand2.group(0).indexOf("]]") >= 0)) {
                        int index_brand2 = m_brand2.group(0).indexOf("[[");
                        int index_end_brand2 = m_brand2.group(0).indexOf("]]");
                        brand = m_brand2.group(0).substring(index_brand2 + 2, index_end_brand2);
                        if (m_brand2.group(0).indexOf("|") >= 0) {
                            int index_brand = brand.indexOf("|") + 1;
                            brand = brand.substring(index_brand);
                        }
                    } else {
                        int index_brand2 = m_brand2.group(0).indexOf("crée par ");
                        brand = m_brand2.group(0).substring(index + 9);
                    }
                }

                if (brand.indexOf(".") > 0) {
                    brand = brand.substring(0, brand.indexOf("."));
                }
                if (brand.indexOf("]") > 0) {
                    brand = brand.substring(0, brand.indexOf("]"));
                }

                if (brand.indexOf("[") > 0) {
                    brand = brand.substring(brand.indexOf("[") + 2);
                }

                if (brand.indexOf("(") > 0) {
                    brand = brand.substring(0, brand.indexOf("("));
                }

                if (brand.indexOf("<") > 0) {
                    brand = brand.substring(0, brand.indexOf("<"));
                }

                if (parfum.equals("{{numéro avec majuscule|5}}")) {
                    parfum = "N°5";
                }

                if (parfum.equals("{{numéro avec majuscule|5}} Eau Première")) {
                    parfum = "N°5 Eau Première";
                }

                if (parfum.equals("{{numéro avec majuscule|19}}")) {
                    parfum = "N°19";
                }

                if (brand.equals(" Bond {{n°|9}}")) {
                    brand = "Bond n°9";
                }

                if (brand.equals("{{Lien|fr=Ermenegildo Zegna|lang=en}}")) {
                    brand = "Ermenegildo Zegna";
                }

                if (brand.equals("{{Lien|fr=Etro|lang=en}}")) {
                    brand = "Etro";
                }
                //hmap_wiki.put(brand, parfum);
                addToMap(brand.trim(), parfum.trim());
                //System.out.println("parfum: "+parfum+" brand: "+brand);
            }
        }

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
                            Set set = hmap_wiki.entrySet();
                            Iterator iteratorhmap = set.iterator();
                            while (iteratorhmap.hasNext()) {
                                Map.Entry mentry = (Map.Entry) iteratorhmap.next();
                                Object key = mentry.getKey();
                                Set<String> values = (Set<String>) mentry.getValue();
                                //  System.out.println("Key: " + key + " Values: " + values);
                                if (key.equals(xElement.getAttribute("form"))) {
                                    // System.out.println("wikipedia: "+key+ "  "+ xElement.getAttribute("form"));
                                    Token token = elementToToken(xElement);
                                    xElement.setAttribute("type", "brand");
                                    token.setType("brand");
                                }
                                for (String value : values) {
                                    if (value.equalsIgnoreCase(xElement.getAttribute("form"))) {
                                        // System.out.println("wikipedia: " + values + "  " + xElement.getAttribute("form"));
                                        Token token = elementToToken(xElement);
                                        xElement.setAttribute("type", "product");
                                        token.setType("product");
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    private void addToMap(String brand, String parfum) {
        if (!hmap_wiki.containsKey(brand)) {
            hmap_wiki.put(brand, new HashSet<String>());
        }
        hmap_wiki.get(brand).add(parfum);
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
            // System.out.println("sentence:" + builder.toString());
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

                                    if (!yElement.hasAttribute("type") || StringUtils.isBlank(yElement.getAttribute("type")) || yElement.getAttribute("type").equalsIgnoreCase("not_identified")) {
                                        Token relationToken = elementToToken(yElement);
                                        relationTokens.add(relationToken);
                                    } else {
                                        /*System.out.print("xElement: "+ xElement.getAttribute("form") + " valeur x: "+x+ " y: "+ yElement.getAttribute("form")+  " valeur y: "+ y+"\n");
                                        if((y-x)==1) {*/


                                        Token objectToken = elementToToken(yElement);
                                        spot = searchSpotByForm(list_spot, objectToken.getForm());
                                        objectToken.setLink(spot.getLink());

                                        if ((xElement.getAttribute("type").equalsIgnoreCase(firstType) &&
                                                yElement.getAttribute("type").equalsIgnoreCase(secondType))) {
                                           /* Set set = hmap_wiki.entrySet();
                                            Iterator iteratorhmap = set.iterator();
                                            while (iteratorhmap.hasNext()) {
                                                Map.Entry mentry = (Map.Entry) iteratorhmap.next();
                                                Object key = mentry.getKey();
                                                String value = (String) mentry.getValue();
                                                if (key.toString().toUpperCase().equalsIgnoreCase(yElement.getAttribute("form").toUpperCase())&&value.toUpperCase().equalsIgnoreCase(xElement.getAttribute("form").toUpperCase())) {
                                                    System.out.println("Ruleswikipedia: " + key + "  " + yElement.getAttribute("form")+ " "+value +" " + xElement.getAttribute("form"));
*/
                                            StringBuilder relation = new StringBuilder();
                                            for (Token t : relationTokens) {
                                                relation.append(t.getForm()).append(" ");
                                            }

                                            if ((!relation.toString().contains(",")) && (!relation.toString().contains("et"))) {
                                                SentenceRelation sentenceRelation = new SentenceRelation();
                                                SentenceRelationId sentenceRelationId = new SentenceRelationId();
                                                sentenceRelationId.setSubject(subjectToken);
                                                sentenceRelationId.setObject(objectToken);
                                                sentenceRelationId.setRelation(relation.toString());
                                                sentenceRelationId.setSentence_text(sentence);
                                                sentenceRelationId.setType(SentenceRelationType.belongsToBrand);
                                                sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                                sentenceRelation.setMethod(SentenceRelationMethod.rulesbelongsToBrand);
                                                list_result.add(sentenceRelation);
                                                // }
                                                // }
                                            }
                                        }
                                        if (xElement.getAttribute("type").equalsIgnoreCase(secondType) &&
                                                yElement.getAttribute("type").equalsIgnoreCase(firstType)) {
                                            Set set = hmap_wiki.entrySet();
                                            Iterator iteratorhmap = set.iterator();
                                           /* while (iteratorhmap.hasNext()) {
                                                Map.Entry mentry = (Map.Entry) iteratorhmap.next();
                                                Object key = mentry.getKey();
                                                String value = (String) mentry.getValue();
                                                if (key.toString().toUpperCase().equalsIgnoreCase(xElement.getAttribute("form").toUpperCase()) && value.toUpperCase().equalsIgnoreCase(yElement.getAttribute("form").toUpperCase().toUpperCase())) {
                                                    System.out.println("Ruleswikipedia: " + key + "  " + xElement.getAttribute("form") + " " + value + " " + yElement.getAttribute("form"));
*/
                                            StringBuilder relation = new StringBuilder();
                                            for (Token t : relationTokens) {
                                                relation.append(t.getForm()).append(" ");
                                            }
                                            if ((!relation.toString().contains(",") && (!relation.toString().contains("et")))) {
                                                SentenceRelation sentenceRelation = new SentenceRelation();
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
                                        }
                                        // }
                                        //}
                                        // }
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

    //méthode qui permet de choisir quelle méthode utilisé pour l'annotattion sachant qu'on favorise le pattern DBpedia
    //puis l'EN DBpedia puis l'application de règle.
    public void annotationData(List<SentenceRelation> list_result) throws IOException {
        // on construit un map (SentenceRelationId,List<SentenceRelationMethod>)
        Map<SentenceRelationId, List<SentenceRelationMethod>> relationMap = new HashMap<>();
        //construction de list_result contenant les méthodes appliquées pour la même sentence
        for (SentenceRelation sentence_relation : list_result) {
            //   System.out.println("sentence_relation:" + sentence_relation);
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
            if (relationMethods.contains(SentenceRelationMethod.dbpedia_patterns)) {
                System.out.println("Selected" + sentenceRelationId + "" + SentenceRelationMethod.dbpedia_patterns);
                Model model = constructModel(sentenceRelationId);
                writeRdf(model);
            } else if (relationMethods.contains(SentenceRelationMethod.dbpedia_namedEntity)) {
                System.out.println("Selected" + sentenceRelationId + "" + SentenceRelationMethod.dbpedia_namedEntity);
                Model model = constructModel(sentenceRelationId);
                writeRdf(model);
            } else {
                if (relationMethods.contains(SentenceRelationMethod.rulesbelongsToBrand)) {
                    System.out.println("Selected" + sentenceRelationId + "" + SentenceRelationMethod.rulesbelongsToBrand);
                    Model model = constructModel(sentenceRelationId);
                    writeRdf(model);
                }
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

