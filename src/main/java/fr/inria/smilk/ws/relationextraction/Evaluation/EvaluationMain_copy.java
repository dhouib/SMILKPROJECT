package fr.inria.smilk.ws.relationextraction.Evaluation;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;

/**
 * Created by dhouib on 29/11/2016.
 */
public class EvaluationMain_copy {

    private static int tpToBrand=0, tpComponent=0;
    private static int nbAutomatiqueToBrand=0, nbAutomatiqueComponent=0;
    private static int tpFragranceCreator=0;
    private static int nbAutomatiqueFragranceCreator=0;
    private static int tpRepresentative=0;
    private static int nbAutomatiqueAmbassador=0;
    private static int nbManuelToBrand=0,nbManuelComponent=0;
    private static int nbManuelFragranceCreator=0;
    private static int nbManuelAmbassador=0;

    public static void main(String[] args) {
        JAXBContext jaxbContext = null;
        try {
            jaxbContext = JAXBContext.newInstance(Sentences.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            Sentences sentences = (Sentences) jaxbUnmarshaller.unmarshal(new File("src/resources/output/evaluation_loreal_test/articles_journaux.xml"));
            Sentences sentences_manu = (Sentences) jaxbUnmarshaller.unmarshal(new File("src/resources/output/evaluation_loreal_test/manuel_annotation_test_corpus_total.xml"));

      /*      Sentences sentences = (Sentences) jaxbUnmarshaller.unmarshal(new File("src/resources/output/evaluation_loreal_test/par1-2.xml"));
            Sentences sentences_manu = (Sentences) jaxbUnmarshaller.unmarshal(new File("src/resources/output/evaluation_loreal_test/maunelle_p1_p2.xml"));
*/
            for(Sentence sent_aut:sentences.getSentence()) {
                System.out.println(sent_aut.getText());
                Sentence sentence = null;
                try {
                    sentence = sentences_manu.getSentence().get(sentences_manu.getSentence().indexOf(sent_aut));
                }catch(Exception e){
                    e.printStackTrace();
                }
                System.out.println(sentence.getText());
                for (Relation relation_aut : sent_aut.getRelations().getRelation()) {
                    if (relation_aut.getPredicate().equalsIgnoreCase("belongsToBrand")) {
                        //System.out.println("test:" + sentence.getRelations().getRelation().contains(relation_aut));
                        nbAutomatiqueToBrand++;
                    }

                    if (relation_aut.getPredicate().equalsIgnoreCase("hasComponent")) {
                        //System.out.println("test:" + sentence.getRelations().getRelation().contains(relation_aut));
                        nbAutomatiqueComponent++;
                    }
                    if (relation_aut.getPredicate().equalsIgnoreCase("hasFragranceCreator")) {
                   //     System.out.println("test:" + sentence.getRelations().getRelation().contains(relation_aut));
                        nbAutomatiqueFragranceCreator++;
                    }
                    if (relation_aut.getPredicate().equalsIgnoreCase("hasRepresentative")) {
                     //   System.out.println("test:" + sentence.getRelations().getRelation().contains(relation_aut));
                        nbAutomatiqueAmbassador++;
                    }
                }

                for (Relation relation_aut : sent_aut.getRelations().getRelation()) {
                    if (sentence.getRelations() != null && sentence.getRelations().getRelation().contains(relation_aut)) {
                        if (relation_aut.getPredicate().equalsIgnoreCase("belongsToBrand")) {
                            System.out.println("tpppppp:" + sentence.getRelations().getRelation().contains(relation_aut));
                            tpToBrand++;
                        }
                        if (relation_aut.getPredicate().equalsIgnoreCase("hasComponent")) {
                            System.out.println("tpppppp:" + sentence.getRelations().getRelation().contains(relation_aut));
                            tpComponent++;
                        }

                        if (relation_aut.getPredicate().equalsIgnoreCase("hasFragranceCreator")) {
                            System.out.println("test:" + sentence.getRelations().getRelation().contains(relation_aut));
                            tpFragranceCreator++;
                        }
                        if (relation_aut.getPredicate().equalsIgnoreCase("hasRepresentative")) {
                            System.out.println("test:" + sentence.getRelations().getRelation().contains(relation_aut));
                            tpRepresentative++;
                        }
                    }
                }
            }

            for (Sentence sent_man:sentences_manu.getSentence()){
                if(sent_man.getRelations()!=null && sent_man.getRelations().getRelation()!= null){
                for(Relation relation_man:sent_man.getRelations().getRelation()){
                    if (relation_man.getPredicate().equalsIgnoreCase("belongsToBrand")) {
                        System.out.println("test:" + sent_man.getRelations().getRelation().contains(relation_man));
                        nbManuelToBrand++;
                    }
                        if (relation_man.getPredicate().equalsIgnoreCase("hasComponent")) {
                            System.out.println("test:" + sent_man.getRelations().getRelation().contains(relation_man));
                            nbManuelComponent++;
                        }
                        if (relation_man.getPredicate().equalsIgnoreCase("hasFragranceCreator")) {
                            System.out.println("test:" + sent_man.getRelations().getRelation().contains(relation_man));
                            nbManuelFragranceCreator++;
                        }
                        if (relation_man.getPredicate().equalsIgnoreCase("hasRepresentative")) {
                            System.out.println("test:" + sent_man.getRelations().getRelation().contains(relation_man));
                            nbManuelAmbassador++;
                        }
                }

            }}
        } catch (JAXBException e) {
            e.printStackTrace();
        }

        System.out.println("tpToBrand: "+tpToBrand);
        System.out.println("nbAutoToBrand: "+nbAutomatiqueToBrand+ " nbManuToBrand: "+ nbManuelToBrand);
        System.out.println("*********");
        System.out.println("tpComponent: "+tpComponent);
        System.out.println("nbAuCompo: "+nbAutomatiqueComponent + " manuComp: "+ nbManuelComponent);
        System.out.println("*********");
        System.out.println("tpFragranceCreator: "+tpFragranceCreator);
        System.out.println("nbAuFrag: "+nbAutomatiqueFragranceCreator+ " manuFrag: "+ nbManuelFragranceCreator);
        System.out.println("*********");
        System.out.println("tpAmbassador: "+tpRepresentative);
        System.out.println("nbAuAmb: "+nbAutomatiqueAmbassador+ " manuAmb: "+ nbManuelAmbassador);
        System.out.println("*********");
        double precisionToBrand=computePrecision(tpToBrand,nbAutomatiqueToBrand);
        System.out.println("precisionToBrand: "+precisionToBrand);
        double recallToBrand=computeRecall(tpToBrand,nbManuelToBrand);
        System.out.println("recallToBrand: "+recallToBrand);
        System.out.println("*********");
        double precisionComponent=computePrecision(tpComponent,nbAutomatiqueComponent);
        System.out.println("precisionComponent: "+precisionComponent);
        double recallComponent=computeRecall(tpComponent,nbManuelComponent);
        System.out.println("recallComponent: "+recallComponent);
        System.out.println("*********");
       double precisionFragranceCreator=computePrecision(tpFragranceCreator,nbAutomatiqueFragranceCreator);
        System.out.println("precisionFragranceCreator: "+precisionFragranceCreator);
        double recallFragranceCreator=computeRecall(tpFragranceCreator,nbManuelFragranceCreator);
        System.out.println("recallFragranceCreator: "+recallFragranceCreator);
        System.out.println("*********");
        double precisionAmbassador=computePrecision(tpRepresentative,nbAutomatiqueAmbassador);
        System.out.println("precisionAmbassador: "+precisionAmbassador);
        double recallAmbassador=computeRecall(tpRepresentative,nbManuelAmbassador);
        System.out.println("recallAmbassador: "+recallAmbassador);

    }


    public static double computeRecall(int tp, int nb) {
        return (double) tp / nb;
    }

    public static double computePrecision(int tp, int nb) {

        return (double) tp / nb;
    }

    public static double computeFMesure(double recall, double precision) {

        return (double) 2 * recall * precision / (recall + precision);
    }
}
