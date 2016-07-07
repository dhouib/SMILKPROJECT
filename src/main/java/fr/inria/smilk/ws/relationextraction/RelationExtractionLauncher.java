package fr.inria.smilk.ws.relationextraction;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by dhouib on 03/07/2016.
 */
public class RelationExtractionLauncher {
    public static void main(String[] args) throws Exception {
        initRdfFile();

        RelationBelongsToBrandExtraction relationBelongsToBrandExtraction =new RelationBelongsToBrandExtraction();
        RelationBelongsToDivisionExtraction relationBelongsToDivisionExtraction =new RelationBelongsToDivisionExtraction();
        RelationBelongsToGroupExtraction relationBelongsToGroupExtraction =new RelationBelongsToGroupExtraction();
        RelationhasComponentExtraction relationhasComponentExtraction=new RelationhasComponentExtraction();
        //relationhasComponentExtraction.process();
       relationBelongsToBrandExtraction.process();
        relationBelongsToDivisionExtraction.process();
        relationBelongsToGroupExtraction.process();
    }

    private static void initRdfFile() throws IOException {
        File file=new File("src/main/resources/extractedrelation.ttl");
        FileWriter out = null;
        //vider le contenu au début
        out = new FileWriter(file);
        out.append(' ');
        out.flush();
        out.close();
    }
}
