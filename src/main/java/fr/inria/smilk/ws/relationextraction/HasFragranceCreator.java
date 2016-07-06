package fr.inria.smilk.ws.relationextraction;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Literal;
import fr.inria.smilk.ws.relationextraction.renco.renco_simple.RENCO;
import fr.inria.smilk.ws.relationextraction.util.ListFilesUtil;
import fr.inria.smilk.ws.relationextraction.util.openNLP;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Created by dhouib on 01/07/2016.
 */
public class HasFragranceCreator {
    //stocker les patterns
    static Set<String> listFragranceCreator = new HashSet<>();
    static Set<String> patternshasFragranceCreator=new HashSet<>();

    public static void main(String[] args) throws Exception {
        String folder = "C:/Users/dhouib/Desktop/SMILK_project_devpt/smilk_relation_extraction/src/main/resources/input/test1";
        constructSentence(folder);
    }

    public static void constructSentence(String folder) throws Exception {
        RENCO renco = new RENCO();
        List<String> lines = readCorpus(folder);
        int i = 0;
        for (String line : lines) {
            i++;
            if (line.trim().length() > 1) {
                System.out.println("\n line: "+i+" " + line);
                hasCreatorRelation(line);
            }
        }
    }

    private static void hasCreatorRelation(String input) throws Exception {
        List<String> listSentence= new ArrayList<>();
        String sentenceabstract=new String();
        String sentencePatternshasFragranceCreator=new String();
        String creator, product;
        String queryString =
                "PREFIX p: <http://dbpedia.org/property/>"+
                        "PREFIX dbpedia: <http://dbpedia.org/resource/>"+
                        "PREFIX category: <http://dbpedia.org/resource/Category:>"+
                        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"+
                        "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>"+
                        "PREFIX geo: <http://www.georss.org/georss/>"+
                        "PREFIX dbpedia-owl: <http://dbpedia.org/ontology/>"+
                        "PREFIX prop-fr: <http://fr.dbpedia.org/property/>"+
                        "select  ?parfum_name ?creator_name ?abstract\n" +
                        "where {\n" +
                        "?parfum_list rdfs:label \"Liste de parfums\" @fr.\n" +
                        "?parfum_list dbpedia-owl:wikiPageWikiLink ?parfum.\n" +
                        "?parfum rdfs:label ?parfum_name.\n"+
                        "?parfum prop-fr:créateur ?creator.\n" +
                        "?creator rdfs:label ?creator_name.\n"+
                        "?parfum dbpedia-owl:abstract ?abstract.\n" +
                        "FILTER ( LANG(?abstract) = \"fr\" )\n" +
                        "FILTER ( LANG(?creator_name) = \"fr\" )\n" +
                        "}";

        Query query = QueryFactory.create(queryString);
        QueryExecution qexec = QueryExecutionFactory.sparqlService("http://fr.dbpedia.org/sparql", query);
        try {
            ResultSet results = qexec.execSelect();
            while (results.hasNext() ) {
                QuerySolution soln = results.nextSolution();
                Literal creator_name_DBpedia = soln.getLiteral("?creator_name");
                creator = creator_name_DBpedia.toString();


                Literal productDBpedia=soln.getLiteral("parfum_name");
                product=productDBpedia.toString();
                int indexproduct = 0, indexbrand = 0,indexcreator=0;
                if ((product.contains("("))) {
                    indexproduct = product.indexOf("(");
                }
                else if(product.contains("@")) {
                    indexproduct = product.indexOf("@");
                }
                if ((creator.contains("("))) {
                    indexcreator = creator.indexOf("(");
                }
                else if(creator.contains("@")) {
                    indexcreator = creator.indexOf("@");
                }

                product = product.substring(0,indexproduct);
                creator=creator.substring(0,indexcreator);
                listFragranceCreator.add(creator.trim());

                Literal abstractDBpedia = soln.getLiteral("abstract");
                sentenceabstract = abstractDBpedia.toString();
                listSentence.add(sentenceabstract);
                int debut = sentenceabstract.indexOf(product) + product.length();
                if(sentenceabstract.contains(creator)) {
                    sentencePatternshasFragranceCreator=sentenceabstract.substring(debut,sentenceabstract.indexOf(creator));
                    patternshasFragranceCreator.add(sentencePatternshasFragranceCreator.trim());
                }
            }

        } finally {
            qexec.close();
        }

        try {
            affichage (patternshasFragranceCreator,listFragranceCreator);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void affichage (Set<String> patternsFragranceCreator,Set<String> listFragranceCreator) throws Exception {

        for (String i : patternsFragranceCreator) {
            System.out.println("Patterns:" + i);
        }

        for (String i : listFragranceCreator) {
            System.out.println("EN:" + i);
        }
    }

    private static void verifyPatternsDBpedia (String input,Set<String> patterns) {
        for (String i : patterns) {
            if (input.contains(i)) {
                System.out.println("Pattern trouvé:" + i);
            }
        }
    }

    //lire les fichiers d'input
    public static List<String> readCorpus(String folderName) throws IOException {
        ListFilesUtil listFileUtil = new ListFilesUtil();
        listFileUtil.listFilesFromDirector(folderName);
        List<String> files = listFileUtil.files;
        List<String> lines = new LinkedList<>();
        openNLP opennlp;
        opennlp = new openNLP();

        int i = 0;
        for (String file : files) {

            System.out.println("Processing file #: "+ file +": "+ i);
            i++;
            BufferedReader fileReader = null;
            String line = "";
            //Create the file reader
            fileReader = new BufferedReader(new FileReader(folderName + "/" + file));
            while ((line = fileReader.readLine()) != null) {
                if (line.trim().length() > 1) {
                    String[] sentences = opennlp.senenceSegmentation(line);
                    if (sentences != null) {
                        for (String sent : sentences) {
                            if (sent.length() > 0) {
                                lines.add(sent);
                            }
                        }
                    }

                }
            }
        }

        return lines;
    }
}
