import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by dhouib on 01/08/2016.
 */
public class test_wikipedia_loreal {
    static HashMap<String,  Set<String>> hmap = new HashMap<String,  Set<String>>();
    static HashMap<String,  Set<String>> hmap_group_division = new HashMap<String,  Set<String>>();
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


    public static void main(String[] args) throws Exception {
        String var = "L%27Oréal";
        String link = "https://fr.wikipedia.org/w/api.php?action=query&titles=" + var + "&prop=revisions&rvprop=content&format=json";
        JsonObject json = readJsonFromUrl(link);
        System.out.println(json.get("query"));
        readJson(json, var);
        // }
    }

    public static void readJson(JsonObject json, String var) throws ParseException {

        JSONParser parser = new JSONParser();
        Object obj = parser.parse(String.valueOf(json.get("query")));
        JSONObject pages = (JSONObject) ((JSONObject) obj).get("pages");
        JSONObject id = (JSONObject) (pages).get("49477");
        JSONObject jsonObject1 = id;
        String title=  jsonObject1.get("title").toString();
        JSONArray revision = (JSONArray) jsonObject1.get("revisions");
        Iterator iterator1 = revision.iterator();
        String content = new String();
        String text = "\\n\\n===== Cosmétique active =====\\nCette division, regroupant des marques des soins dermocosmétiques sur conseils et prescriptions, propose des produits de haute technicité :\\n* '''''[[Vichy (marque)|Vichy]]''''', fondée en [[1931]] par le docteur Haller et [[Georges Guérin]] à la suite de sa visite la même année des thermes de la ville de [[Vichy]] (le docteur Haller étant à cette époque directeur des centres thermaux de [[Vichy]] et [[Georges Guérin]] à la tête d'une unité de production mécanisée concentrée sur la parfumerie) et acquise en [[1955]].\\n** Parmi les gammes de Vichy, on trouve les marques Dermablend, Dercos, Basic Homme, [[Capital soleil|Capital Soleil]]…\\n* ";
        while (iterator1.hasNext()) {
            JSONObject innerObj = (JSONObject) iterator1.next();
            content = innerObj.get("*").toString();
        }
          //System.out.println(content);
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
                    for (String line: lines_brands) {
                        Pattern p_brand = Pattern.compile("'''''(.)*");
                       // Pattern p_brand =Pattern.compile("'''''\\[\\[.*\\]\\]'''''");
                        Matcher m_brand = p_brand.matcher(line);
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
                        addToMap2(title,div.trim());
                    }
                      // System.out.println("div: " + div+"brand: " + brand);
                    }

                   // System.out.println("brand: " + brand);
            }
        }
        Set set = hmap.entrySet();
        Iterator iteratorhmap = set.iterator();
        while (iteratorhmap.hasNext()) {
            Map.Entry mentry = (Map.Entry) iteratorhmap.next();
            Object key = mentry.getKey();
            Set<String> values = (Set<String>) mentry.getValue();
            System.out.println("Key: " + key + " Values: " + values);
        }

        Set setdiv = hmap_group_division.entrySet();
        Iterator iteratordiv = setdiv.iterator();
        while (iteratordiv.hasNext()) {
            Map.Entry mentry = (Map.Entry) iteratordiv.next();
            Object key = mentry.getKey();
            Set<String> values = (Set<String>) mentry.getValue();
            System.out.println("Group Key: " + key + " Values: " + values);
        }
    }

    private static void addToMap(String div, String brand) {
        if(!hmap.containsKey(div)){
            hmap.put(div, new HashSet<String>());
        }
        hmap.get(div).add(brand);
    }

    private static void addToMap2(String title, String div) {
        if(!hmap_group_division.containsKey(title)){
            hmap_group_division.put(title, new HashSet<String>());
        }
        hmap_group_division.get(title).add(div);
    }

}
