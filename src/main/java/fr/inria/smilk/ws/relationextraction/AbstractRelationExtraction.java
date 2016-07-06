package fr.inria.smilk.ws.relationextraction;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import fr.inria.smilk.ws.relationextraction.bean.SentenceRelation;
import fr.inria.smilk.ws.relationextraction.renco.renco_simple.RENCO;
import fr.inria.smilk.ws.relationextraction.util.ListFilesUtil;
import fr.inria.smilk.ws.relationextraction.util.openNLP;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static org.apache.jena.atlas.io.IO.close;

/**
 * Created by dhouib on 03/07/2016.
 */
public abstract class AbstractRelationExtraction {

     String folder = "C:/Users/dhouib/Desktop/SMILK_project_devpt/smilk_relation_extraction/src/main/resources/input/test1";
     protected List<SentenceRelation> list_result = new ArrayList<>();



     public void process() throws Exception {
        constructSentence(folder);
        annotationData(list_result);
    }




    public abstract void annotationData(List<SentenceRelation> list_result) throws IOException;
    public abstract void processExtraction(String line) throws Exception;

    public void constructSentence(String folder) throws Exception {

        List<String> lines = readCorpus(folder);
        //System.out.println("Size of data: " + lines.size());
        // System.out.print("data: "+lines);
        int i = 0;
        for (String line : lines) {
            i++;
            if (line.trim().length() > 1) {
                System.out.println("\n line: " + i + " " + line);
                processExtraction(line);

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
