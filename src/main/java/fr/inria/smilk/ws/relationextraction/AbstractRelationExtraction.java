package fr.inria.smilk.ws.relationextraction;

import fr.inria.smilk.ws.relationextraction.bean.SentenceRelation;
import fr.inria.smilk.ws.relationextraction.util.ListFilesUtil;
import fr.inria.smilk.ws.relationextraction.util.openNLP;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static org.apache.jena.atlas.io.IO.close;

/**
 * Created by dhouib on 03/07/2016.
 */
public abstract class AbstractRelationExtraction {

    protected List<SentenceRelation> list_result = new ArrayList<>();

    public void process(List<String> lines) throws Exception {
    }

    public void processGlobal() throws Exception {
        annotationData(list_result);
    }

    public abstract void annotationData(List<SentenceRelation> list_result) throws IOException;

    public abstract void processExtraction(String line) throws Exception;

    public abstract void init() throws Exception;


    //transform data in 1 File
    public static void constructSentence(List<String> lines) throws Exception {
        File file = new File("src/main/resources/manuel_annotation_Data.txt");
        //  List<String> lines = readCorpus(folder);
        //System.out_copy.println("Size of data: " + lines.size());
        // System.out_copy.print("data: "+lines);
        int i = 0;
        for (String line : lines) {
            i++;
            if (line.trim().length() > 1) {

                    System.out.println("\n line: " + i + " " + line);

                    FileWriter out = null;

                    try {

                        out = new FileWriter(file, true);
                        try {
                            out.append(line + "\n");

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
            }
    }


    // read corpus Files
    public static List<String> readCorpus(String folderName, File folder1) throws IOException {
        ListFilesUtil listFileUtil = new ListFilesUtil();
        listFileUtil.listFilesFromDirector(folderName);

        File[] listOfFiles = folder1.listFiles();
        List<String> files = listFileUtil.files;
        List<String> lines = new LinkedList<>();
        openNLP opennlp;
        opennlp = new openNLP();

        int i = 0;
        String[] filesArray = files.toArray(new String[]{});
       // sortFiles(filesArray);
        sortByNumber(listOfFiles);
        for (String file : filesArray) {

            System.out.println("Processing file #: " + file + ": " + i);
            i++;
            BufferedReader fileReader = null;
            String line = "";
            //Create the file reader
            FileInputStream is = new FileInputStream(folderName + "/" + file);

            InputStreamReader isr = new InputStreamReader(is, Charset.forName("utf-8"));

            fileReader = new BufferedReader(isr);

            // InputStreamReader isr1 = new InputStreamReader(is,  Charset.forName("UTF-8"));
            //fileReader = new BufferedReader(isr1);

            //fileReader = new BufferedReader(in);
            while ((line = fileReader.readLine()) != null) {
                if (line.trim().length() > 1) {
                    String[] sentences = opennlp.senenceSegmentation(line);
                    if (sentences != null) {
                        for (String sent : sentences) {
                            if (sent.length() > 0) {
                               /* String[] linessplit = sent.split(",");
                                for (String linesplit : linessplit) {
                                    if (!(linesplit.indexOf("et") >= 0)) {
                                        lines.add(linesplit);
                                    }
                                    if (linesplit.indexOf("et") >= 0) {
                                        String[] sublines = linesplit.split("et");
                                        for (String subline : sublines) {
                                            lines.add(subline);
                                            // System.out.println("subline: " + subline);
                                        }

                                    }
                                }*/

                                lines.add(sent);
                        }
                        }
                    }

                }
            }


        }

        return lines;
    }


    //Sort Files Corpus
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

    //Sort Files Corpus
    public static void sortByNumber(File[] listOfFiles) {
        Arrays.sort(listOfFiles, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                int n1 = extractNumber(o1.getName());
                int n2 = extractNumber(o2.getName());
                return n1 - n2;
            }

            private int extractNumber(String name) {
                int i = 0;
                try {
                    //int s = name.indexOf('_')+1;
                    int e = name.lastIndexOf('.');
                    String number = name.substring(e);
                    i = Integer.parseInt(number);
                } catch(Exception e) {
                    i = 0; // if filename does not match the format
                    // then default to 0
                }
                return i;
            }
        });

       /* for(File f : files) {
            System.out_copy.println(f.getName());
        }*/
    }


}
