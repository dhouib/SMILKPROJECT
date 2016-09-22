package preprocessing.decoding;

import fr.inria.smilk.ws.relationextraction.util.ListFilesUtil;
import fr.inria.smilk.ws.relationextraction.util.openNLP;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by dhouib on 20/07/2016.
 */
public class DecodingFile {
    static StringBuilder builder = new StringBuilder();
    static String folder = "C:/Users/dhouib/Desktop/SMILK_project_devpt/RelationExtractionSMILK/src/main/resources/input/test1";
    static String fileContentCorpus;


    static String readFile(String path, String encoding)
            throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

    public static void main(String[] args) throws IOException {

        File folder = new File("C:/Users/dhouib/Desktop/SMILK_project_devpt/RelationExtractionSMILK/src/main/resources/input/test");
        File[] listOfFiles = folder.listFiles();

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                System.out.println("File " + listOfFiles[i].getName());

                //InputStreamReader isr = new InputStreamReader(is, Charset.forName(file.equals("10") ? "utf-8" : "windows-1252"));
                fileContentCorpus = readFile(listOfFiles[i].getAbsolutePath(), "windows-1252");
                System.out.println("Start annotating file:" + listOfFiles[i].getName());

                builder = new StringBuilder();
                // if (builder.toString().length() > 0) {
                builder.append(fileContentCorpus);
                fileContentCorpus = builder.toString();
                FileWriter writer = new FileWriter("C:/Users/dhouib/Desktop/SMILK_project_devpt/RelationExtractionSMILK/src/main/resources/out_copy/"+listOfFiles[i].getName()+".txt", false);

                writer.write(fileContentCorpus);
                writer.close();
            } else if (listOfFiles[i].isDirectory()) {
                System.out.println("Directory " + listOfFiles[i].getName());
            }
        }




    }
/*
    // read corpus Files
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

            System.out_copy.println("Processing file #: "+ file +": "+ i);
            i++;
            BufferedReader fileReader = null;
            String line = "";
            //Create the file reader
            FileInputStream is = new FileInputStream(folderName + "/" + file);

            //windows Ansi encodding
            InputStreamReader isr = new InputStreamReader(is, Charset.forName(file.equals("10") ? "utf-8" : "windows-1252"));


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
                                lines.add(sent);

                            }
                        }
                    }

                }
            }


        }

        return lines;
    }
*/
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
/*
    //transform data in 1 File
    public static void constructSentence( List<String> lines) throws Exception {
        File file=new File("src/main/resources/text_Data.txt");
        //  List<String> lines = readCorpus(folder);
        //System.out_copy.println("Size of data: " + lines.size());
        // System.out_copy.print("data: "+lines);
        int i = 0;
        for (String line : lines) {
            i++;
            if (line.trim().length() > 1) {

                System.out_copy.println("\n line: " + i + " " + line);

                FileWriter out_copy = null;

                try {

                    out_copy = new FileWriter(file,true);
                    try {
                        out_copy.append(line+"\n");

                    } finally {
                        try {
                            out_copy.flush();
                            out_copy.close();
                        } catch (IOException closeException) {
                            // ignore
                        }
                    }

                } finally {

                    try {
                        out_copy.flush();
                        out_copy.close();
                    } catch (IOException ex) {

                        // ignore
                    }
                }
            }
        }
    }*/

}


