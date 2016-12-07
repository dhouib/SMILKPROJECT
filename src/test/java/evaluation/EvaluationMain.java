package evaluation;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;

/**
 * Created by dhouib on 29/11/2016.
 */
public class EvaluationMain {
    private static int tpComponent=0;
    private static int nbAutomatiqueComponent=0;
    private static int tpFragranceCreator=0;
    private static int nbAutomatiqueFragranceCreator=0;
    private static int tpRepresentative=0;
    private static int nbAutomatiqueAmbassador=0;
    private static int nbManuelComponent=0;
    private static int nbManuelFragranceCreator=0;
    private static int nbManuelAmbassador=0;
    public static void main(String[] args) {
        JAXBContext jaxbContext = null;
        try {
            jaxbContext = JAXBContext.newInstance(Sentences.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            Sentences sentences = (Sentences) jaxbUnmarshaller.unmarshal(new File("src/resources/output/Evaluation/automatic_annotation.xml"));
            Sentences sentences_manu = (Sentences) jaxbUnmarshaller.unmarshal(new File("src/resources/output/Evaluation/manuel_annotation.xml"));

            for(Sentence sent_aut:sentences.getSentence()){
                for(Relation relation_aut:sent_aut.getRelations().getRelation()){
                    if (relation_aut.getPredicate().equalsIgnoreCase("hasComponent")) {
                        System.out.println("test:" + sent_aut.getRelations().getRelation().contains(relation_aut));
                        nbAutomatiqueComponent++;
                    }
                    if (relation_aut.getPredicate().equalsIgnoreCase("hasFragranceCreator")) {
                        System.out.println("test:" + sent_aut.getRelations().getRelation().contains(relation_aut));
                        nbAutomatiqueFragranceCreator++;
                    }
                    if (relation_aut.getPredicate().equalsIgnoreCase("hasRepresentative")) {
                        System.out.println("test:" + sent_aut.getRelations().getRelation().contains(relation_aut));
                        nbAutomatiqueAmbassador++;
                    }
                }
            }

            for (Sentence sent_man:sentences_manu.getSentence()){
            Sentence sentence=sentences.getSentence().get(sentences.getSentence().indexOf(sent_man));
                System.out.println(sentence.getText());
                for(Relation relation_man:sent_man.getRelations().getRelation()){
                        if (relation_man.getPredicate().equalsIgnoreCase("hasComponent")) {
                            System.out.println("test:" + sentence.getRelations().getRelation().contains(relation_man));
                            nbManuelComponent++;
                        }
                        if (relation_man.getPredicate().equalsIgnoreCase("hasFragranceCreator")) {
                            System.out.println("test:" + sentence.getRelations().getRelation().contains(relation_man));
                            nbManuelFragranceCreator++;
                        }
                        if (relation_man.getPredicate().equalsIgnoreCase("hasRepresentative")) {
                            System.out.println("test:" + sentence.getRelations().getRelation().contains(relation_man));
                            nbManuelAmbassador++;
                        }
                }

                for(Relation relation_man:sent_man.getRelations().getRelation()){
                    if(sentence.getRelations().getRelation().contains(relation_man)) {
                        if (relation_man.getPredicate().equalsIgnoreCase("hasComponent")) {
                            System.out.println("test:" + sentence.getRelations().getRelation().contains(relation_man));
                            tpComponent++;
                        }
                        if (relation_man.getPredicate().equalsIgnoreCase("hasFragranceCreator")) {
                            System.out.println("test:" + sentence.getRelations().getRelation().contains(relation_man));
                            tpFragranceCreator++;
                        }
                        if (relation_man.getPredicate().equalsIgnoreCase("hasRepresentative")) {
                            System.out.println("test:" + sentence.getRelations().getRelation().contains(relation_man));
                            tpRepresentative++;
                        }
                    }
                }

            }
        } catch (JAXBException e) {
            e.printStackTrace();
        }

        System.out.println("tpComponent: "+tpComponent);
        System.out.println("nbAuCompo: "+nbAutomatiqueComponent + " manuComp: "+ nbManuelComponent);
        System.out.println("tpFragranceCreator: "+tpFragranceCreator);
        System.out.println("nbAuFrag: "+nbAutomatiqueFragranceCreator+ " manuFrag: "+ nbManuelFragranceCreator);
        System.out.println("tpAmbassador: "+tpRepresentative);
        System.out.println("nbAuAmb: "+nbAutomatiqueAmbassador+ " manuAmb: "+ nbManuelAmbassador);

        double precisionComponent=computePrecision(tpComponent,nbAutomatiqueComponent-tpComponent);
        System.out.println("precisionComponent: "+precisionComponent);
        double recallComponent=computeRecall(tpComponent,nbManuelComponent-tpComponent);
        System.out.println("recallComponent: "+recallComponent);

        double precisionFragranceCreator=computePrecision(tpFragranceCreator,nbAutomatiqueFragranceCreator-tpFragranceCreator);
        System.out.println("precisionFragranceCreator: "+precisionFragranceCreator);
        double recallFragranceCreator=computeRecall(tpFragranceCreator,nbManuelFragranceCreator-tpFragranceCreator);
        System.out.println("recallFragranceCreator: "+recallFragranceCreator);

        double precisionAmbassador=computePrecision(tpRepresentative,nbAutomatiqueAmbassador-tpRepresentative);
        System.out.println("precisionAmbassador: "+precisionAmbassador);
        double recallAmbassador=computeRecall(tpRepresentative,nbManuelAmbassador-tpRepresentative);
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
