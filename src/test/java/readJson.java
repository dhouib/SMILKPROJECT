import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import fr.inria.smilk.ws.farhad.relationextraction.bean.SentenceRelation;
import fr.inria.smilk.ws.farhad.relationextraction.bean.SentenceRelationId;
import fr.inria.smilk.ws.farhad.relationextraction.bean.SentenceRelationType;
import fr.inria.smilk.ws.farhad.relationextraction.bean.Spot;
import fr.inria.smilk.ws.relationextraction.bean.SentenceRelationMethod;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import static fr.inria.smilk.ws.farhad.relationextraction.RdfHelper.constructModelFarhad;
import static fr.inria.smilk.ws.farhad.relationextraction.RdfHelper.writeRdf;

/**
 * Created by dhouib on 25/07/2016.
 * https://examples.javacodegeeks.com/core-java/json/java-json-parser-example/
 * http://crunchify.com/how-to-read-json-object-from-file-in-java/
 */
public class readJson {

     static HashMap<Integer, Spot> hmap = new HashMap<>();
    static List <Spot> list = new ArrayList<Spot>();

    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws Exception {
        initRdfFile();
        readFilesJson();
    }

    private static void readFilesJson() throws Exception {
        File folder = new File("C:/Users/dhouib/Desktop/SMILK_project_devpt/RelationExtractionSMILK/src/main/resources/json/");
        File[] listOfFiles = folder.listFiles();

        JSONParser parser = new JSONParser();
        for (int f = 0; f < listOfFiles.length; f++) {
            if (listOfFiles[f].isFile()) {
                System.out.println("File " + listOfFiles[f].getName());

                try {
                    hmap=new HashMap<>();
                    list=new ArrayList<>();
                    Object obj = parser.parse(new FileReader(listOfFiles[f]));
                    // "C:/Users/dhouib/Desktop/SMILK_project_devpt/RelationExtractionSMILK/src/main/resources/json/1_result.txt"));
                    JSONObject jsonObject = (JSONObject) obj;
                    //String text = (String) jsonObject.get("text");System.out_copy.println("text: " + text);

                    JSONArray annotatedSpot = (JSONArray) jsonObject.get("annotatedSpot");
                    System.out.println("\nannotatedSpot:");
                    for (int i = 0; i <annotatedSpot.size(); i++) {
                        //  System.out_copy.println("the "+ i+"element of the array: "+annotatedSpot.get(i));

                    }
                    Iterator i = annotatedSpot.iterator();
                    while (i.hasNext()) {
                        JSONObject innerObj = (JSONObject) i.next();
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
                    Iterator iterator = set.iterator();
                    while (iterator.hasNext()) {
                        Map.Entry mentry = (Map.Entry) iterator.next();
                        Object key = mentry.getKey();
                        Spot value = (Spot) mentry.getValue();
                        //System.out_copy.println("Key: " + key + "Value: " + value.getSpot());
                        list.add(value);
                    }
                    Collections.sort(list);
                    constructSpotFile(list);
                    modifyType(list);
                    modifyLink(list);
                    rulesBelongsToBrand(list);
                    rulesBelongsToDivision(list);
                    rulesBelongsToGroup(list);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private static void modifyType (List<Spot> list) {
        for (int j = 0; j < list.size(); j++) {
            if((list.get(j).getType().equals("NULL"))&& !(list.get(j).getLink().equals("NIL"))){
                verifyLink (list.get(j));
            }

        }
    }

    private static void modifyLink (List<Spot> list) {
        for (int j = 0; j < list.size(); j++) {
            if((list.get(j).getType().equals("PRODUCT"))&& !(list.get(j).getLink().equals("NIL"))){
                verifyLink_2 (list.get(j));
            }

        }
    }


    private static void rulesBelongsToBrand(List<Spot> list) throws IOException {
        int x = 0, y = 0;

        while (x < list.size()) {
            if ((list.get(x).getType().equals("PRODUCT")) || (list.get(x).getType().equals("RANGE"))) {
                y = x + 1;

                for (int j = y; j < list.size(); j++) {
                    if ((list.get(j).getType().equals("BRAND"))) {

                        // System.out_copy.println(list.get(x).getSpot() + " belongsToBrand " + list.get(j).getSpot())
                        //  System.out_copy.println(list.get(j).getSpot());
                        Spot subjectSpot, objectSpot;
                        subjectSpot = list.get(x);
                        objectSpot = list.get(j);
                        SentenceRelation sentenceRelation = new SentenceRelation();
                        SentenceRelationId sentenceRelationId = new SentenceRelationId();
                        sentenceRelationId.setSubject(subjectSpot);
                        sentenceRelationId.setObject(objectSpot);
                        sentenceRelationId.setType(SentenceRelationType.belongsToBrand);
                        sentenceRelation.setSentenceRelationId(sentenceRelationId);
                        sentenceRelation.setMethod(SentenceRelationMethod.rulesbelongsToBrand);
                        Model model = constructModelFarhad(sentenceRelationId);
                        writeRdf(model);
                    }
                    y = j;
                    break;
                }
                x = y;
            } else if ((list.get(x).getType().equals("BRAND"))) {

                y = x + 1;
                for (int j = y; j < list.size(); j++) {
                    if ((list.get(j).getType().equals("PRODUCT")) || (list.get(j).getType().equals("RANGE"))) {

                        // System.out_copy.println(list.get(j).getSpot() + " belongsToBrand " + list.get(x).getSpot());
                        Spot subjectSpot, objectSpot;
                        subjectSpot = list.get(j);
                        objectSpot = list.get(x);
                        SentenceRelation sentenceRelation = new SentenceRelation();
                        SentenceRelationId sentenceRelationId = new SentenceRelationId();
                        sentenceRelationId.setSubject(subjectSpot);
                        sentenceRelationId.setObject(objectSpot);
                        sentenceRelationId.setType(SentenceRelationType.belongsToBrand);
                        sentenceRelation.setSentenceRelationId(sentenceRelationId);
                        sentenceRelation.setMethod(SentenceRelationMethod.rulesbelongsToBrand);
                        Model model = constructModelFarhad(sentenceRelationId);
                        writeRdf(model);
                    }
                    y = j;
                    break;
                }
                x = y;
            } else {
                x += 1;
            }

        }
    }

    private static void rulesBelongsToDivision(List<Spot> list) throws IOException {
        int x = 0, y = 0;

        while (x < list.size()) {
            if (list.get(x).getType().equals("BRAND")) {
                y = x + 1;
                for (int j = y; j < list.size(); j++) {
                    if (list.get(j).getType().equals("DIVISION")) {
                        //System.out_copy.println(list.get(x).getSpot() + " belongsToDivision " + list.get(j).getSpot());
                        Spot subjectSpot, objectSpot;
                        subjectSpot=list.get(x);
                        objectSpot=list.get(j);
                        SentenceRelation sentenceRelation=new SentenceRelation();
                        SentenceRelationId sentenceRelationId = new SentenceRelationId();
                        sentenceRelationId.setSubject(subjectSpot);
                        sentenceRelationId.setObject(objectSpot);
                        sentenceRelationId.setType(SentenceRelationType.belongsToDivision);
                        sentenceRelation.setSentenceRelationId(sentenceRelationId);
                        sentenceRelation.setMethod(SentenceRelationMethod.rulesBelongsToDivision);
                        Model model=constructModelFarhad(sentenceRelationId);
                        writeRdf(model);

                    }
                    y = j;
                    break;
                }
                x = y;
            } else if (list.get(x).getType().equals("DIVISION")) {
                y = x + 1;
                for (int j = y; j < list.size(); j++) {
                    if (list.get(j).getType().equals("BRAND")) {
                        //System.out_copy.println(list.get(j).getSpot() + " belongsToBrand " + list.get(x).getSpot());
                        Spot subjectSpot, objectSpot;
                        subjectSpot=list.get(j);
                        objectSpot=list.get(x);
                        SentenceRelation sentenceRelation=new SentenceRelation();
                        SentenceRelationId sentenceRelationId = new SentenceRelationId();
                        sentenceRelationId.setSubject(subjectSpot);
                        sentenceRelationId.setObject(objectSpot);
                        sentenceRelationId.setType(SentenceRelationType.belongsToDivision);
                        sentenceRelation.setSentenceRelationId(sentenceRelationId);
                        sentenceRelation.setMethod(SentenceRelationMethod.rulesBelongsToDivision);
                        Model model=constructModelFarhad(sentenceRelationId);
                        writeRdf(model);
                    }
                    y = j;
                    break;
                }
                x = y;
            } else {
                x += 1;
            }

        }
    }

    private static void rulesBelongsToGroup(List<Spot> list) throws IOException {
        int x = 0, y = 0;

        while (x < list.size()) {
            if (list.get(x).getType().equals("DIVISION")) {
                y = x + 1;
                for (int j = y; j < list.size(); j++) {
                    if (list.get(j).getType().equals("GROUP")) {
                       // System.out_copy.println(list.get(x).getSpot() + " belongsToGroup " + list.get(j).getSpot());
                        Spot subjectSpot, objectSpot;
                        subjectSpot=list.get(x);
                        objectSpot=list.get(j);
                        SentenceRelation sentenceRelation=new SentenceRelation();
                        SentenceRelationId sentenceRelationId = new SentenceRelationId();
                        sentenceRelationId.setSubject(subjectSpot);
                        sentenceRelationId.setObject(objectSpot);
                        sentenceRelationId.setType(SentenceRelationType.belongsToGroup);
                        sentenceRelation.setSentenceRelationId(sentenceRelationId);
                        sentenceRelation.setMethod(SentenceRelationMethod.rulesBelongsToGroup);
                        Model model=constructModelFarhad(sentenceRelationId);
                        writeRdf(model);
                    }
                    y = j;
                    break;
                }
                x = y;
            } else if (list.get(x).getType().equals("GROUP")) {
                y = x + 1;
                for (int j = y; j < list.size(); j++) {
                    if (list.get(j).getType().equals("DIVISION")) {
                      //  System.out_copy.println(list.get(j).getSpot() + " belongsToGroup " + list.get(x).getSpot());
                        Spot subjectSpot, objectSpot;
                        subjectSpot=list.get(j);
                        objectSpot=list.get(x);
                        SentenceRelation sentenceRelation=new SentenceRelation();
                        SentenceRelationId sentenceRelationId = new SentenceRelationId();
                        sentenceRelationId.setSubject(subjectSpot);
                        sentenceRelationId.setObject(objectSpot);
                        sentenceRelationId.setType(SentenceRelationType.belongsToGroup);
                        sentenceRelation.setSentenceRelationId(sentenceRelationId);
                        sentenceRelation.setMethod(SentenceRelationMethod.rulesBelongsToGroup);
                        Model model=constructModelFarhad(sentenceRelationId);
                        writeRdf(model);
                    }
                    y = j;
                    break;
                }
                x = y;
            } else {
                x += 1;
            }

        }
    }

    private static void rulesBelongsToProductOrRange(List<Spot> list) throws IOException {
        int x = 0, y = 0;

        while (x < list.size()) {
            if (list.get(x).getType().equals("PRODUCT")) {
                y = x + 1;
                for (int j = y; j < list.size(); j++) {
                    if (list.get(j).getType().equals("RANGE")) {
                        //System.out_copy.println(list.get(x).getSpot() + " belongsToProductOrRange " + list.get(j).getSpot());
                        Spot subjectSpot, objectSpot;
                        subjectSpot=list.get(x);
                        objectSpot=list.get(j);
                        SentenceRelation sentenceRelation=new SentenceRelation();
                        SentenceRelationId sentenceRelationId = new SentenceRelationId();
                        sentenceRelationId.setSubject(subjectSpot);
                        sentenceRelationId.setObject(objectSpot);
                        sentenceRelationId.setType(SentenceRelationType.belongsToProductOrServiceRange);
                        sentenceRelation.setSentenceRelationId(sentenceRelationId);
                        sentenceRelation.setMethod(SentenceRelationMethod.rulesBelongsToProductOrServiceRange);
                        Model model=constructModelFarhad(sentenceRelationId);
                        writeRdf(model);
                    }
                    y = j;
                    break;
                }
                x = y;
            } else if (list.get(x).getType().equals("RANGE")) {
                y = x + 1;
                for (int j = y; j < list.size(); j++) {
                    if (list.get(j).getType().equals("PRODUCT")) {
                       // System.out_copy.println(list.get(j).getSpot() + " belongsToProductOrRange " + list.get(x).getSpot());
                        Spot subjectSpot, objectSpot;
                        subjectSpot=list.get(j);
                        objectSpot=list.get(x);
                        SentenceRelation sentenceRelation=new SentenceRelation();
                        SentenceRelationId sentenceRelationId = new SentenceRelationId();
                        sentenceRelationId.setSubject(subjectSpot);
                        sentenceRelationId.setObject(objectSpot);
                        sentenceRelationId.setType(SentenceRelationType.belongsToProductOrServiceRange);
                        sentenceRelation.setSentenceRelationId(sentenceRelationId);
                        sentenceRelation.setMethod(SentenceRelationMethod.rulesBelongsToProductOrServiceRange);
                        Model model=constructModelFarhad(sentenceRelationId);
                        writeRdf(model);
                    }
                    y = j;
                    break;
                }
                x = y;
            } else {
                x += 1;
            }

        }
    }



    private static void initRdfFile() throws IOException {
        File file=new File("extractedrelationFarhadApproch.ttl");
        FileWriter out = null;
        //vider le contenu au début
        out = new FileWriter(file);
        out.append(' ');
        out.flush();
        out.close();

        File file_data=new File("src/main/resources/spots.txt");
        FileWriter out_data = null;
        //vider le contenu au début
        out_data = new FileWriter(file_data);
        out_data.append(' ');
        out_data.flush();
        out_data.close();
    }


    public static void constructSpotFile( List<Spot> list) throws Exception {
        File file=new File("src/main/resources/spots.txt");
        FileWriter out = null;
        try {
            out = new FileWriter(file,true);
            int x=0;
            try {
                for (int j = 0; j < list.size(); j++) {
                    out.append("spot: " +list.get(j).getSpot()+ " type: "+ list.get(j).getType()+ " link:"+ list.get(j).getLink()
                            + "wikiname: "+list.get(j).getWikiname()+"\n");
                }

            } finally {
                try {
                    out.flush();
                    out.close();
                } catch (IOException closeException) {
                    // ignore
                }
            }
        } finally {
            try {
                out.flush();
                out.close();
            } catch (IOException ex) {
                // ignore
            }
        }
    }


    public static void verifyLink (Spot s){
        String link=s.getLink().replace("page","resource");
        System.out.println ("link: "+link +"\n");
        ParameterizedSparqlString qs = new ParameterizedSparqlString("" +
                "PREFIX p: <http://dbpedia.org/property/>" +
                        "PREFIX dbpedia: <http://dbpedia.org/resource/>" +
                        "PREFIX category: <http://dbpedia.org/resource/Category:>" +
                        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
                        "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>" +
                        "PREFIX geo: <http://www.georss.org/georss/>" +
                        "PREFIX dbpedia-owl: <http://dbpedia.org/ontology/>" +
                        "PREFIX prop-fr: <http://fr.dbpedia.org/property/>" +
                        "PREFIX dcterms: <http://purl.org/dc/terms/> "+
                        "select ?subject ?subject_name where {\n" +
                        "?link dcterms:subject ?subject.\n" +
                        "?subject rdfs:label ?subject_name.\n " +
                        "FILTER ( LANG(?subject_name) = \"fr\" )\n" +
                        "} ");
        Model model = ModelFactory.createDefaultModel();
     Resource resource= model.createResource(link);
        System.out.println ("Resource: "+ resource);
        qs.setParam("link", resource);

        //Query query = QueryFactory.create(qs);
        QueryExecution qexec = QueryExecutionFactory.sparqlService("http://fr.dbpedia.org/sparql", qs.asQuery());

        String subject_string="NULL";
        try {
            ResultSet results = qexec.execSelect();
            while (results.hasNext()) {
                QuerySolution soln = results.nextSolution();
                Resource subject = soln.getResource("subject");
                Literal subject_name = soln.getLiteral("subject_name");
                subject_string = subject_name.toString();
                //   subject_string=subject.toString();
                System.out.print("subject: " + subject_string + "\n");


                if (subject_string.contains("Maison de parfum")) {
                    s.setType("BRAND");
                    System.out.println("Spot: " + s.getSpot()+ " "+ s.getType());
                }


            }
        } finally {
            qexec.close();
        }


    }

    public static void verifyLink_2 (Spot s){
        String link=s.getLink().replace("page","resource");
        System.out.println ("link: "+link +"\n");
        ParameterizedSparqlString qs = new ParameterizedSparqlString("" +
                "PREFIX p: <http://dbpedia.org/property/>" +
                "PREFIX dbpedia: <http://dbpedia.org/resource/>" +
                "PREFIX category: <http://dbpedia.org/resource/Category:>" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
                "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>" +
                "PREFIX geo: <http://www.georss.org/georss/>" +
                "PREFIX dbpedia-owl: <http://dbpedia.org/ontology/>" +
                "PREFIX prop-fr: <http://fr.dbpedia.org/property/>" +
                "PREFIX dcterms: <http://purl.org/dc/terms/> "+
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "+
                "select ?type ?type_name where {\n" +
                "?link rdf:type  ?type.\n" +
                "?type rdfs:label ?type_name.\n " +
                "FILTER ( LANG(?type_name) = \"fr\" )\n" +
                "} ");
        Model model = ModelFactory.createDefaultModel();
        Resource resource= model.createResource(link);
        System.out.println ("Resource: "+ resource);
        qs.setParam("link", resource);

        //Query query = QueryFactory.create(qs);
        QueryExecution qexec = QueryExecutionFactory.sparqlService("http://fr.dbpedia.org/sparql", qs.asQuery());

        String subject_string="NULL";
        try {
            ResultSet results = qexec.execSelect();
            while (results.hasNext()) {
                QuerySolution soln = results.nextSolution();
                Resource subject = soln.getResource("type");
                Literal subject_name = soln.getLiteral("type_name");
                subject_string = subject_name.toString();
                //   subject_string=subject.toString();
                System.out.print("type: " + subject_string + "\n");


                if ((subject_string.contains("film")|| (subject_string.contains("personne")))) {
                    s.setLink("NIL");
                    System.out.println("Spot: " + s.getSpot()+ " "+ s.getLink());
                }


            }
        } finally {
            qexec.close();
        }


    }



}