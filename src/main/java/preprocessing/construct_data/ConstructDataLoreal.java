package preprocessing.construct_data;

import fr.inria.smilk.ws.relationextraction.util.ListFilesUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by dhouib on 26/08/2016.
 */
public class ConstructDataLoreal {
    static File folder = new File("C:/Users/dhouib/Desktop/SMILK_project_devpt/RelationExtractionSMILK/src/resources/input/preprocessing_data/L'Oreal/");
    static String folder_name = "C:/Users/dhouib/Desktop/SMILK_project_devpt/RelationExtractionSMILK/src/resources/input/preprocessing_data/L'Oreal/";

    public static void main(String[] args) {
        try {
            readFiles();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    //Sort Files Corpus
    private static void sortFiles(String[] filenames) {
        Arrays.sort(filenames, new Comparator<String>() {
            public int compare(String f1, String f2) {
                try {
                    int i1 = Integer.parseInt(f1.substring(6, f1.indexOf(".")));//6 pour ingorer LOreal
                    int i2 = Integer.parseInt(f2.substring(6, f2.indexOf(".")));
                    return i1 - i2;
                } catch (NumberFormatException e) {
                    throw new AssertionError(e);
                }
            }
        });
    }
   // fileContentCorpus = readFile(listOfFiles[i].getAbsolutePath(), "windows-1252");
    public static void readFiles() throws Exception {
        ListFilesUtil listFileUtil = new ListFilesUtil();
        listFileUtil.listFilesFromDirector(folder_name);
        File[] listOfFiles = folder.listFiles();
        List<String> files = listFileUtil.files;
        List<String> lines = new LinkedList<>();
        int j = 0;
        String[] filesArray = files.toArray(new String[]{});
        sortFiles(filesArray);
        StringBuilder fileContentBuilder = new StringBuilder();
        for (String file : filesArray) {
            System.out.println("Processing file #: " + file + ": " + j);
            j++;
            try {

            File fXmlFile = new File(folder_name + "/" + file);

                readXML(fXmlFile, file);
                int nbfile=0;
              //  String toWrite=readXML(fXmlFile, file);
                nbfile++;
               /* FileWriter writer = new FileWriter("C:/Users/dhouib/Desktop/SMILK_project_devpt/RelationExtractionSMILK/src/main/resources/out_L'oreal/" +nbfile+ file, false);
                writer.write(toWrite);
                writer.close();*/

        } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }



    public static void readXML(File fXmlFile, String file) throws IOException {
        //String normalText= new String();

        try {
            org.jsoup.nodes.Document doc = Jsoup.parse(fXmlFile, "UTF-8", "http://example.com/");
            String title = doc.title();
            System.out.println("title : " + title);

            int size_div=doc.select("div[class=article frArticle]").size();

            for (int i=0; i<size_div; i++) {
                String normalText = new String();

                Element div = doc.select("div[class=article frArticle]").get(i);
                Element head = div.select("div[id=hd]").first();
                normalText = "Titre: " + head.text() + ". ";
                int size = div.select("p[class=articleParagraph frarticleParagraph]").size();
                for (int j = 0; j < size; j++) {
                    Element meta = div.select("p[class=articleParagraph frarticleParagraph]").get(j);
                    if (!meta.text().contains(" Tous droits ")) {
                        normalText = normalText.concat(meta.text());
                    }
                   String new_file= modify(file);
                   // System.out.println("new_file:"+ new_file);
                    FileWriter writer = new FileWriter("C:/Users/dhouib/Desktop/SMILK_project_devpt/RelationExtractionSMILK/src/resources/output/preprocessing_data/out_L'oreal/" + i + new_file, false);
                    writer.write(normalText);
                    writer.close();
                }
            }
    } catch (Exception e) {
        e.printStackTrace();
    }
        //return normalText ;
    }

    public static String modify(String file)
    {
        String new_file=new String();
        int index = file.indexOf(".");
        //print filename
        //System.out.println(file.getName().substring(0, index));
        //print extension
        //System.out.println(file.getName().substring(index));
        String ext = file.replace (".html",".txt");
        //use file.renameTo() to rename the file
       // file.concat().renameTo(new File("Newname"+ext));
        new_file=ext;
        return new_file;
    }

}
