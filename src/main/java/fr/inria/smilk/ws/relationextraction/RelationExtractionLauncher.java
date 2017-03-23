package fr.inria.smilk.ws.relationextraction;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

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
        String folder = "C:/Users/dhouib/Desktop/SMILK_project_devpt/RelationExtractionSMILK/src/resources/corpus_test_loreal_files_1_5";
        File folder1 = new File("C:/Users/dhouib/Desktop/SMILK_project_devpt/RelationExtractionSMILK/src/resources/corpus_test_loreal_files_1_5");

       RelationBelongsToBrandExtraction relationBelongsToBrandExtraction = new RelationBelongsToBrandExtraction();
       RelationBelongsToDivisionExtraction relationBelongsToDivisionExtraction = new RelationBelongsToDivisionExtraction();
        RelationBelongsToGroupExtraction relationBelongsToGroupExtraction = new RelationBelongsToGroupExtraction();
        RelationHasFragranceCreatorBasedOnRenco_Holmes relationHasFragranceCreator = new RelationHasFragranceCreatorBasedOnRenco_Holmes();
        RelationHasRespresentativeBasedOnRenco_Holmes relationHasAmbassador = new RelationHasRespresentativeBasedOnRenco_Holmes();
        RelationHasComponentBasedOnRenco_RencoIngredient relationComponent= new RelationHasComponentBasedOnRenco_RencoIngredient();

       relationBelongsToBrandExtraction.init();
        relationBelongsToDivisionExtraction.init();
        relationBelongsToGroupExtraction.init();
        relationHasFragranceCreator.init();
       relationHasAmbassador.init();
        relationComponent.init();

        List<String> lines = AbstractRelationExtraction.readCorpus(folder, folder1);
        for (String line : lines) {
          relationBelongsToBrandExtraction.processExtraction(line);
           relationBelongsToDivisionExtraction.processExtraction(line);
            relationBelongsToGroupExtraction.processExtraction(line);
          relationHasFragranceCreator.processExtraction(line);
          relationHasAmbassador.processExtraction(line);
          relationComponent.processExtraction(line);
        }
      relationBelongsToBrandExtraction.processGlobal();
        relationBelongsToDivisionExtraction.processGlobal();
        relationBelongsToGroupExtraction.processGlobal();
        relationHasFragranceCreator.processGlobal();
       relationHasAmbassador.processGlobal();
       relationComponent.processGlobal();

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
