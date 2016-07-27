package fr.inria.smilk.ws.farhad.relationextraction;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import fr.inria.smilk.ws.farhad.relationextraction.bean.SentenceRelationId;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by dhouib on 26/07/2016.
 */
public class RdfHelper {

    //construct the RDF Model
    public static Model constructModelFarhad (SentenceRelationId sentenceRelationId){
        Model model = ModelFactory.createDefaultModel();
        String smilkprefix="http://ns.inria.fr/smilk/elements/1.0/";
        String rdfsprefix="http://www.w3.org/2000/01/rdf-schema#";
        Resource subject, object;
        if(sentenceRelationId.getSubject().getLink().equals("NIL")){
             subject = model.createResource(smilkprefix + sentenceRelationId.getSubject().getSpot());
        }
        else
        {
            subject = model.createResource(sentenceRelationId.getSubject().getLink());
        }

        if(sentenceRelationId.getObject().getLink().equals("NIL")){
            object=model.createResource(smilkprefix + sentenceRelationId.getObject().getSpot());
        }
        else
        {
            object = model.createResource(sentenceRelationId.getObject().getLink());
        }

        Property belongs_to_group = model.createProperty(smilkprefix + sentenceRelationId.getType().name());
        Property  rdfs_type = model.createProperty(rdfsprefix + "a");
        Resource type_subject=model.createResource(sentenceRelationId.getSubject().getType());
        Resource type_object=model.createResource(sentenceRelationId.getObject().getType());
        model.add(subject, rdfs_type, type_subject).add(subject, belongs_to_group, object);
        model.add(object, rdfs_type,type_object);
        model.write(System.out, "N-TRIPLE");
        return model;
    }

    //write the RDF in the N3 format
    public static void writeRdf (Model model) throws IOException {
        File file=new File("src/main/resources/extractedrelationFarhadApproch.ttl");

        FileWriter out = null;

        try {

            out = new FileWriter(file,true);
            try {
                model.write(out, "N3");

            } finally {
                try {
                    out.flush();
                    out.close();
                } catch (IOException closeException) {
                }
            }

        }finally {

            try {
                out.flush();
                out.close();
            } catch (IOException ex) {

            }
        }

    }
}
