package fr.inria.smilk.ws.relationextraction;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.*;
import fr.inria.smilk.ws.relationextraction.bean.SentenceRelationId;
import fr.inria.smilk.ws.relationextraction.bean.SentenceRelationMethod;
import fr.inria.smilk.ws.relationextraction.bean.Spot;
import fr.inria.smilk.ws.relationextraction.bean.Token;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.*;

/**
 * Created by dhouib on 03/07/2016.
 */
public class ExtractionHelper {

    //construct the RDF Model
    public static Model constructModel(SentenceRelationId sentenceRelationId) {
        Model model = ModelFactory.createDefaultModel();
        String smilkprefix = "http://ns.inria.fr/smilk/elements/1.0/";
        String rdfsprefix = "http://www.w3.org/2000/01/rdf-schema#";
        Resource subject, object;
        //subject = model.createResource(smilkprefix + sentenceRelationId.getSubject().getForm());
        System.out.println("RDF: " + sentenceRelationId.getSubject().getForm() + "link: " + sentenceRelationId.getSubject().getLink());
        if ((sentenceRelationId.getSubject().getLink() == null) || (sentenceRelationId.getSubject().getLink() == "null")) {
            subject = model.createResource(smilkprefix + sentenceRelationId.getSubject().getForm());
        } else {
            subject = model.createResource(sentenceRelationId.getSubject().getLink());
        }

        if ((sentenceRelationId.getObject().getLink() == null) || (sentenceRelationId.getSubject().getLink() == "null")) {
            object = model.createResource(smilkprefix + sentenceRelationId.getObject().getForm());
        } else {
            object = model.createResource(sentenceRelationId.getObject().getLink());
        }
        Property belongs_to_group = model.createProperty(smilkprefix + sentenceRelationId.getType().name());
        Property rdfs_type = model.createProperty(rdfsprefix + "a");
        Property hasText = model.createProperty("hasText");
        Property hasConfidence = model.createProperty("hasConfidence");
        Property hasRules=model.createProperty("hasRules");
        Resource type_subject = model.createResource("http://provoc/Product");
        Resource type_object = null;
        if(sentenceRelationId.getType().name().equalsIgnoreCase("hasComponent")) {
             type_object = model.createResource("http://provoc/Component");
        }
        if(sentenceRelationId.getType().name().equalsIgnoreCase("hasFragranceCreator")) {
            type_object = model.createResource("http://provoc/FragrranceCreator");
        }
        if(sentenceRelationId.getType().name().equalsIgnoreCase("hasAmbasador")) {
            type_object = model.createResource("http://provoc/Ambassador");
        }
        Resource text_sources = model.createResource(sentenceRelationId.getSentence_text());
        Resource  rules_sources = model.createResource(sentenceRelationId.getRelation());
        Resource confidence=model.createResource(String.valueOf(sentenceRelationId.getConfidence()));
        model.add(subject, rdfs_type, type_subject).add(subject, belongs_to_group, object).add(subject, hasText, text_sources).add(subject,hasRules,rules_sources).add(subject,hasConfidence,confidence);
        model.add(object, rdfs_type, type_object);
          model.write(System.out, "N-TRIPLE");
        return model;
    }

    //write the RDF in the N3 format
    public static void writeRdf(Model model) throws IOException {
        File file = new File("src/resources/output/relation_extraction/text.ttl");
        FileWriter out = null;
        try {
            out = new FileWriter(file, true);
            try {
                model.write(out, "N3");
            } finally {
                try {
                    out.flush();
                    out.close();
                } catch (IOException closeException) {
                }
            }

        } finally {

            try {
                out.flush();
                out.close();
            } catch (IOException ex) {

            }
        }
    }

    // transform element To Token
    public static Token elementToToken(Element element) {
        Token token = new Token();
        token.setId(Integer.parseInt(element.getAttribute("id")));
        token.setForm(element.getAttribute("form"));
        token.setStart(Integer.parseInt(element.getAttribute("start")));
        token.setEnd(Integer.parseInt(element.getAttribute("end")));
        token.setLema(element.getAttribute("lemma"));
        token.setPos(element.getAttribute("pos"));
        token.setDepRel(element.getAttribute("depRel"));
        token.setHead(Integer.parseInt(element.getAttribute("head")));
        token.setType(element.getAttribute("type"));
        token.setLink("null");
        return token;
    }

    // transform element To Token
    public static Token elementToToken(Element element1, Element element2) {
        Token token = new Token();
        token.setId(Integer.parseInt(element1.getAttribute("id")));
        token.setForm(element1.getAttribute("form") + " " + element2.getAttribute("form"));
        token.setStart(Integer.parseInt(element1.getAttribute("start")));
        token.setEnd(Integer.parseInt(element2.getAttribute("end")));
        token.setLema(element1.getAttribute("lemma") + " " + element2.getAttribute("form"));
        token.setPos(element1.getAttribute("pos") + " " + element2.getAttribute("pos"));
        token.setDepRel(element1.getAttribute("depRel"));
        token.setHead(Integer.parseInt(element1.getAttribute("head")));
        token.setType(element1.getAttribute("type"));
        token.setLink("null");
        return token;
    }

    // transform element To Token
    public static Token elementToToken(Element element1, Element element2, Element element3) {
        Token token = new Token();
        token.setId(Integer.parseInt(element1.getAttribute("id")));
        token.setForm(element1.getAttribute("form") + " " + element2.getAttribute("form"));
        token.setStart(Integer.parseInt(element1.getAttribute("start")));
        token.setEnd(Integer.parseInt(element3.getAttribute("end")));
        token.setLema(element1.getAttribute("lemma") + " " + element2.getAttribute("form") + " " + element3.getAttribute("form"));
        token.setPos(element1.getAttribute("pos") + " " + element2.getAttribute("pos"));
        token.setDepRel(element1.getAttribute("depRel"));
        token.setHead(Integer.parseInt(element1.getAttribute("head")));
        token.setType(element1.getAttribute("type"));
        token.setLink("null");
        return token;
    }

    // search token by index
    public static Element searchToken(NodeList nTokensList, int index_pattern) {
        Element before_current_element = null;
        for (int currentTokensIndex = 0; currentTokensIndex < nTokensList.getLength(); currentTokensIndex++) {
            Node nTokensNode = nTokensList.item(currentTokensIndex);
            if (nTokensNode instanceof Element) {
                for (int currentChildIndex = 0; currentChildIndex < nTokensNode.getChildNodes().getLength(); currentChildIndex++) {
                    Node nTokenNode = nTokensNode.getChildNodes().item(currentChildIndex);
                    if (nTokenNode instanceof Element) {
                        Element current_element = (Element) nTokenNode;
                        String index_start = current_element.getAttribute("start");
                        //  System.out.println("index_start: " + index_start);
                        int index_token = Integer.parseInt(index_start);
                        if (index_token < index_pattern) {
                            before_current_element = current_element;
                            //    System.out.println("if <" + before_current_element.getNodeValue());
                        } else {
                            //  System.out.println("else:" + before_current_element);
                            return before_current_element;
                        }
                    }
                }
            }
        }
        return before_current_element;
    }

    public static String sentenceToString(Node sentenceNode) {
        StringBuilder builder = new StringBuilder();
        sentenceToString(sentenceNode, builder);
        return builder.toString();
    }

    private static void sentenceToString(Node node, StringBuilder builder) {
        // do something with the current node instead of System.out_copy
        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node currentNode = nodeList.item(i);
            if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
                Element currentElement = (Element) currentNode;
                if (StringUtils.isNotBlank(currentElement.getAttribute("form"))) {
                    int startIndex = Integer.parseInt(currentElement.getAttribute("start"));
                    if (builder.length() < startIndex) {
                        builder.append(StringUtils.repeat(' ', startIndex - builder.length()));
                    }
                    builder.append(currentElement.getAttribute("form"));
                }
                //calls this method for all the children which is Element
                sentenceToString(currentNode, builder);
            }
        }
    }


    public static List<Spot> readFileJson() {
        File folder = new File("C:/Users/dhouib/Desktop/SMILK_project_devpt/RelationExtractionSMILK/src/resources/input/ENL_farhad/");
        File[] listOfFiles = folder.listFiles();
        HashMap<Integer, Spot> hmap = new HashMap<Integer, Spot>();
        List<Spot> list = new ArrayList<Spot>();
        JSONParser parser = new JSONParser();

        for (int f = 0; f < listOfFiles.length; f++) {
            if (listOfFiles[f].isFile()) {
                try {
                    hmap = new HashMap<Integer, Spot>();
                    list = new ArrayList<>();
                    Object obj = parser.parse(new FileReader(listOfFiles[f]));
                    JSONObject jsonObject = (JSONObject) obj;
                    String text = (String) jsonObject.get("text");
                    JSONArray annotatedSpot = (JSONArray) jsonObject.get("annotatedSpot");
                    Iterator iterator = annotatedSpot.iterator();
                    while (iterator.hasNext()) {
                        JSONObject innerObj = (JSONObject) iterator.next();
                        Spot spot = new Spot();
                        spot.setSpot(innerObj.get("spot").toString());
                        spot.setLink(innerObj.get("dbpedia").toString());
                        spot.setType(innerObj.get("type").toString());
                        spot.setStart(Integer.parseInt(innerObj.get("start").toString()));
                        spot.setEnd(innerObj.get("end").toString());
                        spot.setWikiname(innerObj.get("wikiname").toString());
                        hmap.put(spot.getStart(), spot);
                    }
                    Set set = hmap.entrySet();
                    Iterator it = set.iterator();
                    while (it.hasNext()) {
                        Map.Entry mentry = (Map.Entry) it.next();
                        Object key = mentry.getKey();
                        Spot value = (Spot) mentry.getValue();
                        list.add(value);
                    }
                    Collections.sort(list);
                    modifyType(list);
                    modifyLink(list);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return list;
    }

    private static void modifyType(List<Spot> list) {
        for (int j = 0; j < list.size(); j++) {
            if ((list.get(j).getType().equals("NULL")) && !(list.get(j).getLink().equals("NIL"))) {
                verifyLink(list.get(j));
            }

        }
    }

    private static void modifyLink(List<Spot> list) {
        for (int j = 0; j < list.size(); j++) {
            if ((list.get(j).getType().equals("PRODUCT")) && !(list.get(j).getLink().equals("NIL"))) {
                verifyLink_2(list.get(j));
            }

        }
    }

    public static Spot searchSpotByForm(List<Spot> list, String form) {
        Spot spot = new Spot();
        for (int j = 0; j < list.size(); j++) {
            if ((list.get(j).getWikiname().toUpperCase().contains("COSMÉTIQUE")) || ((list.get(j).getWikiname().toUpperCase().contains("PARFUMS")))
                    || list.get(j).getWikiname().toUpperCase().contains("ENTREPRISE")) {
                int index = list.get(j).getWikiname().indexOf("_");
                String new_wikiname = list.get(j).getWikiname().substring(0, index);
                list.get(j).setWikiname(new_wikiname);
            }
            if (list.get(j).getWikiname().equalsIgnoreCase(form)) {
                if ((list.get(j).getLink().equals("NIL"))) {
                    list.get(j).setLink("null");
                    spot = list.get(j);
                } else {
                    spot = list.get(j);
                }
            }
        }
        return spot;
    }

    public static void verifyLink(Spot s) {
        String link = s.getLink().replace("page", "resource");
        ParameterizedSparqlString qs = new ParameterizedSparqlString("" +
                "PREFIX p: <http://dbpedia.org/property/>" +
                "PREFIX dbpedia: <http://dbpedia.org/resource/>" +
                "PREFIX category: <http://dbpedia.org/resource/Category:>" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
                "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>" +
                "PREFIX geo: <http://www.georss.org/georss/>" +
                "PREFIX dbpedia-owl: <http://dbpedia.org/ontology/>" +
                "PREFIX prop-fr: <http://fr.dbpedia.org/property/>" +
                "PREFIX dcterms: <http://purl.org/dc/terms/> " +
                "select ?subject ?subject_name where {\n" +
                "?link dcterms:subject ?subject.\n" +
                "?subject rdfs:label ?subject_name.\n " +
                "FILTER ( LANG(?subject_name) = \"fr\" )\n" +
                "} ");
        Model model = ModelFactory.createDefaultModel();
        Resource resource = model.createResource(link);
        qs.setParam("link", resource);

        QueryExecution qexec = QueryExecutionFactory.sparqlService("http://fr.dbpedia.org/sparql", qs.asQuery());

        String subject_string = "NULL";
        try {
            ResultSet results = qexec.execSelect();
            while (results.hasNext()) {
                QuerySolution soln = results.nextSolution();
                Resource subject = soln.getResource("subject");
                Literal subject_name = soln.getLiteral("subject_name");
                subject_string = subject_name.toString();
                if (subject_string.contains("Maison de parfum")) {
                    s.setType("BRAND");
                }
            }
        } finally {
            qexec.close();
        }
    }

    public static void verifyLink_2(Spot s) {
        String link = s.getLink().replace("page", "resource");
        ParameterizedSparqlString qs = new ParameterizedSparqlString("" +
                "PREFIX p: <http://dbpedia.org/property/>" +
                "PREFIX dbpedia: <http://dbpedia.org/resource/>" +
                "PREFIX category: <http://dbpedia.org/resource/Category:>" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
                "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>" +
                "PREFIX geo: <http://www.georss.org/georss/>" +
                "PREFIX dbpedia-owl: <http://dbpedia.org/ontology/>" +
                "PREFIX prop-fr: <http://fr.dbpedia.org/property/>" +
                "PREFIX dcterms: <http://purl.org/dc/terms/> " +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                "select ?type ?type_name where {\n" +
                "?link rdf:type  ?type.\n" +
                "?type rdfs:label ?type_name.\n " +
                "FILTER ( LANG(?type_name) = \"fr\" )\n" +
                "} ");
        Model model = ModelFactory.createDefaultModel();
        Resource resource = model.createResource(link);
        qs.setParam("link", resource);
        QueryExecution qexec = QueryExecutionFactory.sparqlService("http://fr.dbpedia.org/sparql", qs.asQuery());

        String subject_string = "NULL";
        try {
            ResultSet results = qexec.execSelect();
            while (results.hasNext()) {
                QuerySolution soln = results.nextSolution();
                Resource subject = soln.getResource("type");
                Literal subject_name = soln.getLiteral("type_name");
                subject_string = subject_name.toString();

                if ((subject_string.contains("film") || (subject_string.contains("personne")))) {
                    s.setLink("null");
                }
            }
        } finally {
            qexec.close();
        }
    }

    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    public static JsonObject readJsonFromUrl(String url) throws IOException {
        InputStream is = new URL(url).openStream();
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = readAll(rd);
            JsonParser parser = new JsonParser();
            JsonObject json = parser.parse(jsonText).getAsJsonObject();
            return json;
        } finally {
            is.close();
        }
    }

}
