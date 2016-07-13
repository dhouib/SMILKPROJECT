package fr.inria.smilk.ws.relationextraction;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Created by dhouib on 03/07/2016.
 */
public class RelationExtractionLauncher {
    public static void main(String[] args) throws Exception {
        initRdfFile();
        String folder = "C:/Users/dhouib/Desktop/SMILK_project_devpt/RelationExtractionSMILK/src/main/resources/input/test1";
        RelationBelongsToBrandExtraction relationBelongsToBrandExtraction =new RelationBelongsToBrandExtraction();
        RelationBelongsToDivisionExtraction relationBelongsToDivisionExtraction =new RelationBelongsToDivisionExtraction();
        RelationBelongsToGroupExtraction relationBelongsToGroupExtraction =new RelationBelongsToGroupExtraction();
        RelationBelongsToProductOrServiceRange relationBelongsToProductOrServiceRange=new RelationBelongsToProductOrServiceRange();
        RelationhasComponentExtraction relationhasComponentExtraction=new RelationhasComponentExtraction();

        relationBelongsToBrandExtraction.init();
        relationBelongsToDivisionExtraction.init();
        relationBelongsToGroupExtraction.init();
        relationBelongsToProductOrServiceRange.init();
        relationhasComponentExtraction.init();


        List<String> lines= AbstractRelationExtraction.readCorpus(folder);
        for (String line : lines) {
            relationBelongsToBrandExtraction.processExtraction(line);
            relationBelongsToDivisionExtraction.processExtraction(line);
            relationBelongsToGroupExtraction.processExtraction(line);
            relationBelongsToProductOrServiceRange.processExtraction(line);
            relationhasComponentExtraction.processExtraction(line);

        }
        relationBelongsToBrandExtraction.processGlobal();
        relationBelongsToDivisionExtraction.processGlobal();
        relationBelongsToGroupExtraction.processGlobal();
        relationBelongsToProductOrServiceRange.processGlobal();
        relationhasComponentExtraction.processGlobal();

        AbstractRelationExtraction.constructSentence(lines);
    }


    private static void initRdfFile() throws IOException {
        File file=new File("src/main/resources/extractedrelation.ttl");
        FileWriter out = null;
        //vider le contenu au début
        out = new FileWriter(file);
        out.append(' ');
        out.flush();
        out.close();

        File file_data=new File("src/main/resources/manuel_annotation_Data.txt");
        FileWriter out_data = null;
        //vider le contenu au début
        out_data = new FileWriter(file_data);
        out_data.append(' ');
        out_data.flush();
        out_data.close();
    }
}
