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
        String folder = "C:/Users/dhouib/Desktop/SMILK_project_devpt/RelationExtractionSMILK/src/resources/input/test/";
        File folder1 = new File("C:/Users/dhouib/Desktop/SMILK_project_devpt/RelationExtractionSMILK/src/resources/input/test/");

        RelationBelongsToBrandExtraction relationBelongsToBrandExtraction = new RelationBelongsToBrandExtraction();
        RelationBelongsToDivisionExtraction relationBelongsToDivisionExtraction = new RelationBelongsToDivisionExtraction();
        RelationBelongsToGroupExtraction relationBelongsToGroupExtraction = new RelationBelongsToGroupExtraction();
        RelationHasComponentExtraction relationHasComponentExtraction = new RelationHasComponentExtraction();

        relationBelongsToBrandExtraction.init();
        relationBelongsToDivisionExtraction.init();
        relationBelongsToGroupExtraction.init();
        relationHasComponentExtraction.init();

        List<String> lines = AbstractRelationExtraction.readCorpus(folder, folder1);
        for (String line : lines) {
            relationBelongsToBrandExtraction.processExtraction(line);
            relationBelongsToDivisionExtraction.processExtraction(line);
            relationBelongsToGroupExtraction.processExtraction(line);
            relationHasComponentExtraction.processExtraction(line);
        }
        relationBelongsToBrandExtraction.processGlobal();
        relationBelongsToDivisionExtraction.processGlobal();
        relationBelongsToGroupExtraction.processGlobal();
        relationHasComponentExtraction.processGlobal();

        AbstractRelationExtraction.constructSentence(lines);
    }


    private static void initRdfFile() throws IOException {
        File file = new File("src/resources/output/relation_extraction/text.ttl");
        FileWriter out = null;
        //vider le contenu au début
        out = new FileWriter(file);
        out.append(' ');
        out.flush();
        out.close();

        File file_data = new File("src/resources/output/relation_extraction/text_Data.txt");
        FileWriter out_data = null;
        //vider le contenu au début
        out_data = new FileWriter(file_data);
        out_data.append(' ');
        out_data.flush();
        out_data.close();
    }
}
