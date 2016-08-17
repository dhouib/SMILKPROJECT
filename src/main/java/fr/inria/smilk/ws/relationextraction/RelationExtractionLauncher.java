package fr.inria.smilk.ws.relationextraction;

import com.google.gson.JsonObject;
import fr.inria.smilk.ws.relationextraction.bean.Spot;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static fr.inria.smilk.ws.relationextraction.ExtractionHelper.readFileJson;

/**
 * Created by dhouib on 03/07/2016.
 */
public class RelationExtractionLauncher {    public static void main(String[] args) throws Exception {
        initRdfFile();
        String folder = "C:/Users/dhouib/Desktop/SMILK_project_devpt/RelationExtractionSMILK/src/main/resources/input/out/";
        File folder1 = new File("C:/Users/dhouib/Desktop/SMILK_project_devpt/RelationExtractionSMILK/src/main/resources/input/out/");

        RelationBelongsToBrandExtraction relationBelongsToBrandExtraction = new RelationBelongsToBrandExtraction();
      //  RelationBelongsToDivisionExtraction relationBelongsToDivisionExtraction = new RelationBelongsToDivisionExtraction();
     //   RelationBelongsToGroupExtraction relationBelongsToGroupExtraction = new RelationBelongsToGroupExtraction();
        //RelationBelongsToProductOrServiceRange relationBelongsToProductOrServiceRange=new RelationBelongsToProductOrServiceRange();
        //RelationHasComponentExtraction relationHasComponentExtraction =new RelationHasComponentExtraction();
       /*  RelationHasTargetExtraction relationHasTargetExtraction=new RelationHasTargetExtraction();
        RelationHasFragranceCreatorExtraction relationHasFragranceCreatorExtraction=new RelationHasFragranceCreatorExtraction();
        RelationHasFounderExtraction  relationHasFounderExtraction=new  RelationHasFounderExtraction();
        RelationHasAmbassadorExtraction relationHasAmbassadorExtraction=new RelationHasAmbassadorExtraction();
        RelationHasModelExtraction relationHasModelExtraction=new RelationHasModelExtraction();*/

        relationBelongsToBrandExtraction.init();
        //relationBelongsToDivisionExtraction.init();
       // relationBelongsToGroupExtraction.init();
        ///relationBelongsToProductOrServiceRange.init();
        //  relationHasComponentExtraction.init();
      /*  relationHasTargetExtraction.init();
        relationHasFragranceCreatorExtraction.init();
        relationHasFounderExtraction.init();
        relationHasAmbassadorExtraction.init();
        relationHasModelExtraction.init();*/


        List<String> lines = AbstractRelationExtraction.readCorpus(folder,folder1);
        for (String line : lines) {
           relationBelongsToBrandExtraction.processExtraction(line);
       //    relationBelongsToDivisionExtraction.processExtraction(line);
        //   relationBelongsToGroupExtraction.processExtraction(line);
            //   relationBelongsToProductOrServiceRange.processExtraction(line);
            //   relationHasComponentExtraction.processExtraction(line);
            /*relationHasTargetExtraction.processExtraction(line);
            relationHasFragranceCreatorExtraction.processExtraction(line);
            relationHasFounderExtraction.processExtraction(line);
            relationHasAmbassadorExtraction.processExtraction(line);
            relationHasModelExtraction.processExtraction(line);*/

        }
       relationBelongsToBrandExtraction.processGlobal();
      // relationBelongsToDivisionExtraction.processGlobal();
      // relationBelongsToGroupExtraction.processGlobal();
        // relationBelongsToProductOrServiceRange.processGlobal();
        //relationHasComponentExtraction.processGlobal();
        /*relationHasTargetExtraction.processGlobal();
        relationHasFragranceCreatorExtraction.processGlobal();
        relationHasFounderExtraction.processGlobal();
        relationHasAmbassadorExtraction.processGlobal();
        relationHasModelExtraction.processGlobal();*/

        AbstractRelationExtraction.constructSentence(lines);
    }


    private static void initRdfFile() throws IOException {
        File file = new File("src/main/resources/extractedrelation.ttl");
        FileWriter out = null;
        //vider le contenu au début
        out = new FileWriter(file);
        out.append(' ');
        out.flush();
        out.close();

        File file_data = new File("src/main/resources/manuel_annotation_Data.txt");
        FileWriter out_data = null;
        //vider le contenu au début
        out_data = new FileWriter(file_data);
        out_data.append(' ');
        out_data.flush();
        out_data.close();
    }
}
