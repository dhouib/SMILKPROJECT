import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Literal;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by dhouib on 01/08/2016.
 */
public class test_wikipedia_parfum {
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




    public static void main(String[] args) throws Exception {
        String var = "Liste%20de%20parfums";
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
        JSONObject id = (JSONObject) (pages).get("689876");
        JSONObject jsonObject1 = id;
        JSONArray revision = (JSONArray) jsonObject1.get("revisions");
        Iterator iterator1 = revision.iterator();
        String content=new String();
        String text="''[[Amor Amor]]'' de Cacharel, créé par Dominique Ropion et [[Laurent Bruyère]], 2003<ref>Le Guérer, ''Le Parfum'', {{p.|313.}}</ref>.\n## ''[[Amor Amor Catch Me]]'' de Cacharel, créé par Dominique Ropion, 2012<ref>{{en}} {{Lien web|url=http://www.trbusiness.com/index.php?option=com_content&view=article&id=11716:lstr-scoops-global-exclusive-with-cacharel&catid=8:international&Itemid=13|auteur=Kevin Rozario|titre=LSTR scoops global exclusive with Cachare|éditeur=|date=24 juillet 2012|site=trbusiness.com|consulté le=}}.</ref>.\n## ''[[Amor Amor Elixir]]'' de [[Cacharel (parfums)|Cacharel]]. Flacon bleu nuit. Notes : fleurs blanches sur fond de bois exotiques et de benjoin.\n## ";
        while (iterator1.hasNext()) {
            JSONObject innerObj = (JSONObject) iterator1.next();
             content = innerObj.get("*").toString();
        }
            String[] lines = content.split("\n");
            for (String line : lines) {
                String subLine = line;
                if(line.indexOf(',')>=0) {
//                    System.out.println("line: " + line.substring(','));
                    subLine = line.substring(0,line.indexOf(','));

                }else  if(line.indexOf('.')>=0) {
                    //System.out.println("line indexof: " + line.indexOf('.')+ " ." +line);
                    //System.out.println("line: " + line + " "+line.substring(line.indexOf('.')));
                    subLine = line.substring(0,line.indexOf('.'));
                }
                //System.out.println("subLine: "+ subLine);
               // Pattern p = Pattern.compile("''(.*)''");
                //site http://www.regexr.com/
                String subsubLine=subLine;
                //pattern pour extraire le nom du parfum
            Pattern p=Pattern.compile("''\\[\\[.*\\]\\]'' ");
                Matcher m = p.matcher(subLine);
                boolean b = m.find();
               // System.out.println(b);
                if (b) {
                   // System.out.println(m.group(0));
                    int index =m.group(0).indexOf("''[[");
                    int index_end =m.group(0).indexOf("]]''");
                    String parfum=m.group(0).substring(index+4,index_end);
                     subsubLine=subLine.substring(subLine.indexOf("]]''")+4);
                   //System.out.println("subLine:" +subsubLine );
                    if(parfum.contains("|")){
                       int index_parfum=parfum.indexOf("|")+1;
                        parfum=parfum.substring(index_parfum);
                    }

                    String brand=new String();

                    //pattern pour extriare le nom de la marque sous la forme "de [[GA]]
                    Pattern p_brand=Pattern.compile("de (\\[\\[)?.*(\\]\\])?");
                    Matcher m_brand = p_brand.matcher(subsubLine);
                    boolean b_brand = m_brand.find();
                    // System.out.println(b);
                    if (b_brand) {
                        if((m_brand.group(0).indexOf("[[")>=0)&&(m_brand.group(0).indexOf("]]")>=0)) {
                             int index_start_brand = m_brand.group(0).indexOf("[[");
                            int index_end_brand = m_brand.group(0).indexOf("]]");
                            brand = m_brand.group(0).substring(index_start_brand + 2, index_end_brand);
                           if (m_brand.group(0).indexOf("|")>=0) {
                                int index_brand = brand.indexOf("|") + 1;
                                brand = brand.substring(index_brand);
                            }
                        }
                        else
                        {
                            int index_brand1 = m_brand.group(0).indexOf("de");
                            brand = m_brand.group(0).substring(index + 2);
                        }
                    }

                    //pattern pour extriare le nom de la marque sous la forme "d'[[GA]]
                    Pattern p_brand1=Pattern.compile("d'(\\[\\[)?.*(\\]\\])?");
                    Matcher m_brand1 = p_brand1.matcher(subsubLine);
                    boolean b_brand1 = m_brand1.find();
                    if (b_brand1) {
                        if((m_brand1.group(0).indexOf("[[")>=0)&&(m_brand1.group(0).indexOf("]]")>=0)) {
                        int index_brand1 = m_brand1.group(0).indexOf("[[");
                        int index_end_brand2 = m_brand1.group(0).indexOf("]]");
                         brand = m_brand1.group(0).substring(index_brand1 + 2, index_end_brand2);
                         if (m_brand1.group(0).indexOf("|")>=0) {
                            int index_brand = brand.indexOf("|") + 1;
                            brand = brand.substring(index_brand);
                        }
                        }
                        else{
                            int index_brand1 = m_brand1.group(0).indexOf("d'");
                            brand = m_brand1.group(0).substring(index + 2);
                        }
                    }

                    //pattern pour extriare le nom de la marque sous la forme "d'[[GA]]
                    Pattern p_brand2=Pattern.compile("créé par (\\[\\[)?.*(\\]\\])?");
                    Matcher m_brand2 = p_brand2.matcher(subsubLine);
                    boolean b_brand2 = m_brand2.find();


                    if (b_brand2) {
                        //System.out.println("");
                        if((m_brand2.group(0).indexOf("[[")>=0)&&(m_brand2.group(0).indexOf("]]")>=0)) {
                            int index_brand2 = m_brand2.group(0).indexOf("[[");
                            int index_end_brand2 = m_brand2.group(0).indexOf("]]");
                            brand = m_brand2.group(0).substring(index_brand2 + 2, index_end_brand2);
                            if (m_brand2.group(0).indexOf("|")>=0) {
                                int index_brand = brand.indexOf("|") + 1;
                                brand = brand.substring(index_brand);
                            }
                        }
                        else{
                            int index_brand2 = m_brand2.group(0).indexOf("crée par ");
                            brand = m_brand2.group(0).substring(index + 9);
                        }
                    }

                    if(brand.indexOf(".")>0){
                        brand=brand.substring(0,brand.indexOf("."));
                    }
                    if(brand.indexOf("]")>0){
                        brand=brand.substring(0,brand.indexOf("]"));
                    }

                    if(brand.indexOf("[")>0){
                        brand=brand.substring(brand.indexOf("[")+2);
                    }

                    if(brand.indexOf("(")>0){
                        brand=brand.substring(0,brand.indexOf("("));
                    }

                    if(brand.indexOf("<")>0){
                        brand=brand.substring(0,brand.indexOf("<"));
                    }

                    if (parfum.equals("{{numéro avec majuscule|5}}"))
                    {
                        parfum= "N°5";
                    }

                    if (parfum.equals("{{numéro avec majuscule|5}} Eau Première"))
                    {
                        parfum= "N°5 Eau Première";
                    }

                    if (parfum.equals("{{numéro avec majuscule|19}}"))
                    {
                        parfum="N°19";
                    }

                    if (brand.equals(" Bond {{n°|9}}"))
                    {
                        brand="Bond n°9";
                    }

                    if (brand.equals("{{Lien|fr=Ermenegildo Zegna|lang=en}}"))
                    {
                        brand="Ermenegildo Zegna";
                    }

                    if(brand.equals("{{Lien|fr=Etro|lang=en}}"))
                    {
                        brand="Etro";
                    }
                    hmap.put(brand.trim(), parfum);
                    System.out.println("brand: "+brand+" parfum: "+parfum);

                    Set set = hmap.entrySet();
                    Iterator iteratorhmap = set.iterator();
                    while (iteratorhmap.hasNext()) {
                        Map.Entry mentry = (Map.Entry) iteratorhmap.next();
                        Object key = mentry.getKey();
                        String value = (String) mentry.getValue();
                        if(value.contains("Amor Amor")) {
                            System.out.println("Key: " + key + " Value: " + value);
                        }
                    }

                }
            }



    }
}
