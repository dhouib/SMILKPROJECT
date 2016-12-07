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
public class EvaluationParRelation {
    static double recall = 0;
    static double precision = 0;
    static double fmesure = 0;

    static String manuelFile = "src/resources/output/Evaluation/manuel_annotation.xml";
    static String automaticFile = "src/resources/output/Evaluation/automatic_annotation.xml";
    private static int tpComponent=0;
    private static int nbAutomatiqueComponent=0;
    private static int tpFragranceCreator=0;
    private static int nbAutomatiqueFragranceCreator=0;
    private static int tpAmbassador=0;
    private static int nbAutomatiqueAmbassador=0;
    private static int fpComponent=0;
    private static int nbManuelComponent=0;
    private static int nbManuelFragranceCreator=0;
    private static int nbManuelAmbassador=0;

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
        while (processDescendants.hasNext() && processDescendantsAutomatic.hasNext()) {
            Content next = processDescendants.next();
            Content nextAutomatic = processDescendantsAutomatic.next();
            boolean found_relation = false;

           /* while (!found_relation && processDescendants.hasNext()) {
                if (next instanceof Element) {
                    Element element = (Element) next;
                    if (element.getName().equals("relation")) {
                        found_relation = true;
                    }
                }
                if (!found_relation) {
                    next = processDescendants.next();
                }
            }

            boolean found_relation_auto = false;
            while (!found_relation_auto && processDescendantsAutomatic.hasNext()) {
                if (nextAutomatic instanceof Element) {
                    Element elementAutomatic = (Element) nextAutomatic;
                    if (elementAutomatic.getName().equals("relation")) {
                        found_relation_auto = true;
                    }
                }
                if (!found_relation_auto) {
                    nextAutomatic = processDescendantsAutomatic.next();
                }
            }*/
            if (next instanceof Element && nextAutomatic instanceof Element) {
                Element element = (Element) next;
                Element elementAutomatic = (Element) nextAutomatic;
                System.out.println(element.getName() +"vs"+ elementAutomatic.getName());
                if((element.getName().equals("sentence")) && (elementAutomatic.getName().equals("sentence"))){
                    if(element.getChild("text").getValue().equalsIgnoreCase(elementAutomatic.getChild("text").getValue())){
                        System.out.println(element.getChild("text").getValue() +"vs "+ elementAutomatic.getChild("text").getValue());
                        Element child=element.getChild("Relations");
                        List<Element> child_relation = child.getChildren();
                        Element child_auto=elementAutomatic.getChild("Relations");
                        List<Element> child_relation_aut = child_auto.getChildren();

                        for (Element child_child_auto : child_relation_aut) {
                            if (child_child_auto.getChild("predicate").getValue().equalsIgnoreCase("hasComponent")) {
                                nbAutomatiqueComponent++;
                            }
                            if (child_child_auto.getChild("predicate").getValue().equalsIgnoreCase("hasFragranceCreator")) {
                                nbAutomatiqueFragranceCreator++;
                            }
                            if (child_child_auto.getChild("predicate").getValue().equalsIgnoreCase("hasRepresentative")) {
                                nbAutomatiqueAmbassador++;
                            }
                        }

                        for(Element child_child :child_relation) {
                            if(child_child.getChild("predicate").getValue().equalsIgnoreCase("hasComponent")) {
                                nbManuelComponent++;
                            }
                            if(child_child.getChild("predicate").getValue().equalsIgnoreCase("hasFragranceCreator")) {
                                nbManuelFragranceCreator++;
                            }
                            if(child_child.getChild("predicate").getValue().equalsIgnoreCase("hasRepresentative")) {
                                nbManuelAmbassador++;
                            }

                            for (Element child_child_auto : child_relation_aut) {
                                if(child_child.getChild("subject").getValue().equalsIgnoreCase(child_child_auto.getChild("subject").getValue())) {
                                    System.out.println(child_child.getChild("subject").getValue() + "vsss: " + child_child_auto.getChild("subject").getValue());
                                    if(child_child.getChild("predicate").getValue().equalsIgnoreCase(child_child_auto.getChild("predicate").getValue())) {
                                        if(child_child.getChild("predicate").getValue().equalsIgnoreCase("hasComponent")) {
                                            if (child_child.getChild("object").getValue().equalsIgnoreCase(child_child_auto.getChild("object").getValue())) {
                                                System.out.println(child_child.getChild("object").getValue() + "vsss: " + child_child_auto.getChild("object").getValue());
                                                tpComponent++;
                                            }
                                        }
                                        if(child_child.getChild("predicate").getValue().equalsIgnoreCase("hasFragranceCreator")) {
                                            if (child_child.getChild("object").getValue().equalsIgnoreCase(child_child_auto.getChild("object").getValue())) {
                                                System.out.println(child_child.getChild("object").getValue() + "vsss: " + child_child_auto.getChild("object").getValue());
                                                tpFragranceCreator++;
                                            }
                                            nbAutomatiqueFragranceCreator++;
                                        }
                                        if(child_child.getChild("predicate").getValue().equalsIgnoreCase("hasRepresentative")) {
                                            if (child_child.getChild("object").getValue().equalsIgnoreCase(child_child_auto.getChild("object").getValue())) {
                                                System.out.println(child_child.getChild("object").getValue() + "vsss: " + child_child_auto.getChild("object").getValue());
                                                tpAmbassador++;
                                            }
                                            nbAutomatiqueAmbassador++;
                                        }
                                    }
                                }
                            }
                        }
                    }
                   else{
                        System.out.println("ce n'est pas le même texte");
                    }

                }
           }
        }

        System.out.println("tpComponent: "+tpComponent);
        System.out.println("nbAu: "+nbAutomatiqueComponent + " manu: "+ nbManuelComponent);
        System.out.println("tpFragranceCreator: "+tpFragranceCreator);
        System.out.println("nbAu: "+nbAutomatiqueFragranceCreator+ " manu: "+ nbManuelFragranceCreator);
        System.out.println("tpAmbassador: "+tpAmbassador);
        System.out.println("nbAu: "+nbAutomatiqueAmbassador+ " manu: "+ nbManuelAmbassador);
        double precisionComponent=computePrecision(tpComponent,nbAutomatiqueComponent);
        System.out.println("precisionComponent: "+precisionComponent);
        double recallComponent=computeRecall(tpComponent,nbManuelComponent-tpComponent);
        System.out.println("recallComponent: "+recallComponent);

        double precisionFragranceCreator=computePrecision(tpFragranceCreator,nbAutomatiqueFragranceCreator);
        System.out.println("precisionFragranceCreator: "+precisionFragranceCreator);
        double recallFragranceCreator=computeRecall(tpFragranceCreator,nbManuelFragranceCreator-tpFragranceCreator);
        System.out.println("recallFragranceCreator: "+recallFragranceCreator);

        double precisionAmbassador=computePrecision(tpAmbassador,nbAutomatiqueAmbassador);
        System.out.println("precisionAmbassador: "+precisionAmbassador);
        double recallAmbassador=computeRecall(tpAmbassador,nbManuelAmbassador-tpAmbassador);
        System.out.println("recallAmbassador: "+recallAmbassador);
    }



    public static double computeRecall(double tp, double fn) {
        return (double) tp / (tp + fn);
    }

    public static double computePrecision(double tp, double fp) {

        return (double) tp / (tp + fp);
    }

    public static double computeFMesure(double recall, double precision) {

        return (double) 2 * recall * precision / (recall + precision);
    }
}
