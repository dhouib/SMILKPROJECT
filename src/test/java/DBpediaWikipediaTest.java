import com.google.gson.*;

import com.google.gson.stream.JsonReader;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Literal;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import sun.rmi.runtime.Log;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by dhouib on 01/08/2016.
 */
public class DBpediaWikipediaTest {

    static HashMap<String, String> hmap = new HashMap<String, String>();

    static Set<String> list_brand = new HashSet<>();

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


    public static void extractionFromDBpedia() throws Exception {
        List<String> listSentence = new ArrayList<>();
        String sentenceabstract = new String();
        String product = new String();
        String brand = new String(), id;
        String sentencePatternsBelongsToBrand = new String();
        String queryString =
                "prefix p: <http://dbpedia.org/property/>" +
                "prefix dbpedia: <http://dbpedia.org/resource/>" +
                "prefix category: <http://dbpedia.org/resource/Category:>" +
                "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
                "prefix skos: <http://www.w3.org/2004/02/skos/core#>" +
                "prefix geo: <http://www.georss.org/georss/>" +
                "prefix dbpedia-owl: <http://dbpedia.org/ontology/>" +
                "prefix prop-fr: <http://fr.dbpedia.org/property/>" +
                "select  ?brand_name ?id\n" +
                "where {\n" +
                "?parfum_list rdfs:label \"Liste de parfums\" @fr.\n" +
                "?parfum_list dbpedia-owl:wikiPageWikiLink ?parfum.\n" +
                "?parfum prop-fr:marque ?brand.\n" +
                "?brand rdfs:label ?brand_name.\n" +
                "?brand dbpedia-owl:wikiPageID ?id. \n"+
                "FILTER ( LANG(?brand_name) = \"fr\" )\n" +
                "} ";

        Query query = QueryFactory.create(queryString);
        QueryExecution qexec = QueryExecutionFactory.sparqlService("http://fr.dbpedia.org/sparql", query);
        try {
            ResultSet results = qexec.execSelect();
            while (results.hasNext()) {
                QuerySolution soln = results.nextSolution();
                Literal idDBpedia = soln.getLiteral("id");
                id = idDBpedia.toString();
                int indexbrand = 0;
                Literal brandDBpedia = soln.getLiteral("brand_name");
                brand = brandDBpedia.toString();
                 if (id.contains("^")) {
                    indexbrand = id.indexOf("^");
                }
                id = id.substring(0, indexbrand);
                list_brand.add(id);
            }
        } finally {
            qexec.close();
        }
    }

    public static void main(String[] args) throws Exception {

       extractionFromDBpedia();
       /* list_brand.add("1585168");
        list_brand.add("4223948");
        list_brand.add("1392765");*/
       /* for(String brand:list_brand) {

            System.out.println("brand: " + brand + "\n");*/

            String var = "2466528";
            String link = "https://fr.wikipedia.org/w/api.php?action=query&pageids=" + var + "&prop=revisions&rvprop=content&format=json";
            JsonObject json = readJsonFromUrl(link);
           // System.out.println(json.get("query"));
            readJson(json, var);
       // }
    }

    public static void readJson ( JsonObject json, String var) throws ParseException {
        //System.out.println(json.get("query"));
         HashMap<String, String> hmap1 = new HashMap<String, String>();

        String result=json.toString();
        JSONParser parser = new JSONParser();
        Object obj = parser.parse(String.valueOf(json.get("query")));
        JSONObject jsonObject = (JSONObject) obj;

        JSONObject pages= (JSONObject) ((JSONObject) obj).get("pages");
        //System.out.println("pages: "+pages);
        JSONObject id= (JSONObject) ((JSONObject) pages).get(var);
        //System.out.println("id: "+id);

        JSONObject jsonObject1 = (JSONObject) id;
        String title=  jsonObject1.get("title").toString();
        JSONArray revision = (JSONArray) jsonObject1.get("revisions");

        Iterator iterator1 = revision.iterator();
        while (iterator1.hasNext()) {
            JSONObject innerObj = (JSONObject) iterator1.next();
            String content=innerObj.get("*").toString();
         //   System.out.println("content:" +content);
            int index_section= content.indexOf("== Parfums ==");
           String parfumsection=content.substring(index_section);
            System.out.println("parfumsection: "+ parfumsection);
            String[] lines = parfumsection.split("\n");

            for(String line:lines){
               // System.out.println("line:"+line);
                Pattern p = Pattern.compile("''(.*)''") ;
                Matcher m = p.matcher(line) ;
                boolean b = m.find() ;
                System.out.println(b);
                if(b) {
                    System.out.println(m.group(1));
                    hmap1.put(title, m.group(1));
                    Set set = hmap1.entrySet();
                    Iterator iteratorhmap = set.iterator();
                    while (iteratorhmap.hasNext()) {
                        Map.Entry mentry = (Map.Entry) iteratorhmap.next();
                        Object key = mentry.getKey();
                        String value = (String) mentry.getValue();
                        System.out.println("Key: " + key + "Value: " + mentry.getValue());
                    }
                }
            }
            System.out.println(lines.length);
        }
    }




}
