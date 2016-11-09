package preprocessing;

import edu.stanford.nlp.ling.CoreAnnotations.*;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.PropertiesUtils;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Created by dhouib on 08/08/2016.
 *http://stanfordnlp.github.io/CoreNLP/api.html
 * https://mailman.stanford.edu/pipermail/java-nlp-user/2016-May/007588.html
 * http://stackoverflow.com/questions/36634101/dependency-parsing-for-french-with-corenlp
 */
public class test_stanfordnlp_relation {


    public static void main(String[] args) throws IOException {
        // creates a StanfordCoreNLP object, with POS tagging, lemmatization, NER, parsing, and preprocessing.coreference resolution
        /*Properties props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse, depparse");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);*/

        StanfordCoreNLP pipeline = new StanfordCoreNLP(
                PropertiesUtils.asProperties(
                        "annotators", "tokenize, ssplit, pos, depparse, parse",
                        "tokenize.language", "fr",
                        //"ssplit.isOneSentence", "true",
                        "pos.model","edu/stanford/nlp/models/pos-tagger/french/french.tagger",
                        "parse.model", "edu/stanford/nlp/models/lexparser/frenchFactored.ser.gz",
                        "depparse.model","edu/stanford/nlp/models/parser/nndep/UD_French.gz"));

        // read some text from the file..
       // File inputFile = new File("src/test/resources/sample-content.txt");
       /* String text = "Les chats mangent la souris. Des chats mangent des souries. Le chat mange des grandes souries. L'enfant mange la pomme verte." +
                "Un enfant mange une pomme."    ;*/
        String text="La ligne s'anime également en mai avec une édition limitée Summer, rafraîchie d'ananas, création d'Ann Filpo et Catlos Benam."+
                    "Crée par Pierre Negrin (Firmenich) Amor Amor In a flach est un oriental gourmand caractérisé par un coeur de vanille, caramel et pomme d'amour sucrée.";
               /* "Le baume Fermeté sculputure, associant Skinfibrine et élastopeptides, vise à rendre la peau plus ferme et élastiqur. Le lait Fermeté sculpturale contient de la céramide et du collagène et promet une double action anti-dessèchement et fermeté remodelante.";
                "Prodigy Night repose sur la Bio-Sève Moléculaire, actif vedette de la gamme Prodigy. Pour Prodigy Night on été ajoutés des extraits de lotus blanc et de feuille de Senna alata ainsi que le pro-Xylane maison."+
                "TriAcnéal rassemble les deux actifs principaux de la précédente version - l'acide glycolique pour son effet peeling et le Rétinaldéhyde qui limite la formation des imperfections - et leur ajoute l'Efectiose, un anti-irritant."+
                "La jeune femme incerna Manifesto, le nouveau parfum féminin de la griffe, qui sort à la rentrée, construit autour du jasmin et de la vanille."+
                "Avec sa peau diaphane et ses cheveux blond vénitien, elle incarnera le nouveau parfum féminin de la griffe, Manifesto, construit au-tour du jasmin.";*/



                        Annotation document = new Annotation(text);

        // run all Annotators on this textn
        pipeline.annotate(document);

        // these are all the sentences in this document
        // a CoreMap is essentially a Map that uses class objects as keys and has values with custom types
        List<CoreMap> sentences = document.get(SentencesAnnotation.class);

        for(CoreMap sentence: sentences) {
         // this is the parse tree of the current sentence
            Tree tree = sentence.get(TreeAnnotation.class);
            System.out.println("parse tree:\n" + tree);
            ///System.out.println("first child:\n" + tree.parent());
            // this is the Stanford dependency graph of the current sentence
            SemanticGraph dependencies = sentence.get(CollapsedCCProcessedDependenciesAnnotation.class);
            System.out.println("dependency graph:\n" + dependencies);
            IndexedWord firstRoot = dependencies.getFirstRoot();
           // if (firstRoot.lemma().contains("animer")) {
                System.out.println("firstRoot: " + firstRoot);
           // }
           System.out.println("vertew: "+dependencies.vertexSet());
            Set<IndexedWord> vetex_set=dependencies.vertexSet();
            for (IndexedWord v:vetex_set){
                if(v.originalText().contains("Summer")){
                    v.setNER("PRODUCT");

                    System.out.println( "vvvvvvv"+v.originalText());
                }


v.setLemma("rafraîchir");
                if(v.lemma().contains("rafraîchir") ||v.originalText().contains("coeur")
                     ||v.originalText().contains("contient") || v.originalText().contains("associant")||
                        v.originalText().contains("repose")){
                    System.out.println("v: " + v + " " + v.originalText()+ " "+ v.lemma());

                    List<SemanticGraphEdge> incomingEdgesSorted =
                            dependencies.getOutEdgesSorted(v);

                    for(SemanticGraphEdge edge : incomingEdgesSorted) {
                        GrammaticalRelation relation = edge.getRelation();
                        if (relation.getShortName().contentEquals("nmod")||relation.getShortName().contentEquals("dobj")) {
                            System.out.println("----->Relation=" + relation.getShortName());
                            IndexedWord dep = edge.getDependent();
                            System.out.println("Dependent_target=" + dep);
                            // Getting the source node with attached edges
                            IndexedWord gov = edge.getGovernor();
                            System.out.println("Governor_source=" + gov);
                        }
                    }

                }
        }


            List<SemanticGraphEdge> incomingEdgesSorted =
                    dependencies.getIncomingEdgesSorted(firstRoot);

            for(SemanticGraphEdge edge : incomingEdgesSorted)
            {
                // Getting the target node with attached edges
                IndexedWord dep = edge.getDependent();
                System.out.println("Dependent_target=" + dep);
                // Getting the source node with attached edges
                IndexedWord gov = edge.getGovernor();
                System.out.println("Governor_source=" + gov);
                // Get the relation name between them
                GrammaticalRelation relation = edge.getRelation();
                System.out.println("Relation=" + relation.getShortName());
            }

            // this section is same as above just we retrieve the OutEdges
            List<SemanticGraphEdge> outEdgesSorted = dependencies.getOutEdgesSorted(firstRoot);
           /* for(SemanticGraphEdge edge : outEdgesSorted)
            {
                IndexedWord dep = edge.getDependent();
                System.out.println("Dependent=" + dep);
                IndexedWord gov = edge.getGovernor();

                    System.out.println("rules ok Governor=" + gov);


                GrammaticalRelation relation = edge.getRelation();
               if(relation.getShortName().toString().contentEquals("nsubj"))
                System.out.println("Relation=" + relation.getShortName());
            }*/
        }

    }

}

