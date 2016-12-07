package fr.inria.smilk.ws.relationextraction.Evaluation;

import org.jdom2.Content;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by dhouib on 29/11/2016.
 */
public class NombreExtractionParRelation {
    static int nb_component_man=0, nb_component_aut=0;
    static int nb_ambassador_man=0, nb_ambassador_aut=0;
    static int nb_fragranceCreator_man=0, nb_fragranceCreator_aut=0;

    static String manuelFile = "src/resources/output/Evaluation/manuel_annotation.xml";
    static String automaticFile = "src/resources/output/Evaluation/automatic_annotation.xml";

    public static void main(String[] args) throws JDOMException, IOException {

        SAXBuilder saxBuilder = new SAXBuilder();
        File xmlmanuelFile = new File(manuelFile);
        File xmlautomaticFile = new File(automaticFile);

        Document document = (Document) saxBuilder.build(xmlmanuelFile);
        Document document2 = (Document) saxBuilder.build(xmlautomaticFile);

        //Définir l'element root du fichier XML
        Element rootElement = document.getRootElement();
        Element rootElementAutomatic = document2.getRootElement();

        Iterator<Content> processDescendants = document.getDescendants();
        List<Element> elements = new ArrayList<Element>();

        Iterator<Content> processDescendantsAutomatic = document2.getDescendants();
        List<Element> elementsAutomatic = new ArrayList<Element>();

        while (processDescendants.hasNext()) {
            Content next = processDescendants.next();
            if (next instanceof Element) {
                Element element = (Element) next;

                if ((element.getName().equals("predicate"))&&(element.getValue().equalsIgnoreCase("hasFragranceCreator"))) {
                    nb_fragranceCreator_man++;
                }
                if ((element.getName().equals("predicate"))&&(element.getValue().equalsIgnoreCase("hasComponent"))) {
                    nb_component_man++;
                }
                if ((element.getName().equals("predicate"))&&(element.getValue().equalsIgnoreCase("hasAmbassador"))) {
                    nb_ambassador_man++;
                }
            }
        }


        while (processDescendantsAutomatic.hasNext()) {
            Content next = processDescendantsAutomatic.next();
            if (next instanceof Element) {
                Element element = (Element) next;

                if ((element.getName().equals("predicate"))&&(element.getValue().equalsIgnoreCase("hasFragranceCreator"))) {
                    nb_fragranceCreator_aut++;
                }
                if ((element.getName().equals("predicate"))&&(element.getValue().equalsIgnoreCase("hasComponent"))) {
                    nb_component_aut++;
                }
                if ((element.getName().equals("predicate"))&&(element.getValue().equalsIgnoreCase("hasAmbassador"))) {
                    nb_ambassador_aut++;
                }
            }
        }
        int nbTotalManuel=nb_ambassador_man+nb_component_man+nb_fragranceCreator_man;
        int nbTotalAutomatic=nb_ambassador_aut+nb_component_aut+nb_fragranceCreator_aut;
        System.out.println("Total_Manuel: "+nbTotalManuel + "vs nbTotalAutomatic: "+ nbTotalAutomatic);
        System.out.println("Manuel Ambassador: "+nb_ambassador_man +"vs automatic Ambassador: "+nb_ambassador_aut);
        System.out.println("Manuel Component: "+nb_component_man +"vs automatic Component: "+nb_component_aut);
        System.out.println("Manuel Fragrance: "+nb_fragranceCreator_man +"vs automatic Fragrance: "+nb_fragranceCreator_aut);
    }


}
