package fr.inria.smilk.ws.relationextraction;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import fr.inria.smilk.ws.relationextraction.bean.SentenceRelation;
import fr.inria.smilk.ws.relationextraction.renco.renco_simple.RENCO;
import fr.inria.smilk.ws.relationextraction.util.ListFilesUtil;
import fr.inria.smilk.ws.relationextraction.util.openNLP;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static org.apache.jena.atlas.io.IO.close;

/**
 * Created by dhouib on 03/07/2016.
 */
public abstract class AbstractRelationExtraction {

     String folder = "C:/Users/dhouib/Desktop/SMILK_project_devpt/RelationExtractionSMILK/src/main/resources/input/test1";
     protected List<SentenceRelation> list_result = new ArrayList<>();



     public void process(List<String> lines) throws Exception {
       // constructSentence(folder);
        //
        //annotationData(list_result);
    }


    public  void processGlobal() throws Exception{
       annotationData(list_result);
    }
    public abstract void annotationData(List<SentenceRelation> list_result) throws IOException;
    public abstract void processExtraction(String line) throws Exception;

    public static void constructSentence( List<String> lines) throws Exception {
        File file=new File("src/main/resources/manuel_annotation_Data.txt");
      //  List<String> lines = readCorpus(folder);
        //System.out.println("Size of data: " + lines.size());
        // System.out.print("data: "+lines);
        int i = 0;
        for (String line : lines) {
            i++;
            if (line.trim().length() > 1) {
                System.out.println("\n line: " + i + " " + line);
                FileWriter out = null;

                try {

                    out = new FileWriter(file,true);
                    try {
                        out.append(line+"\n");

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

               // processExtraction(line);

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
        String[] filesArray = files.toArray(new String[]{});
        sortFiles(filesArray);
        for (String file : filesArray) {

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

    /**
     *
     * @param path
     * @param encoding
     * @return
     * @throws IOException
     */
    static String readFile(String path, Charset encoding)
            throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }


    private static void sortFiles(String[] filenames) {
        Arrays.sort(filenames, new Comparator<String>() {
            public int compare(String f1, String f2) {
                try {
                    int i1 = Integer.parseInt(f1);
                    int i2 = Integer.parseInt(f2);
                    return i1 - i2;
                } catch (NumberFormatException e) {
                    throw new AssertionError(e);
                }
            }
        });
    }
}
