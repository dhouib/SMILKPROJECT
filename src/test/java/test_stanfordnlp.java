import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.Properties;


import edu.stanford.nlp.dcoref.CorefChain;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations.CorefChainAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.parser.nndep.DependencyParser;
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

/**
 * Created by dhouib on 08/08/2016.
 *http://stanfordnlp.github.io/CoreNLP/api.html
 * https://mailman.stanford.edu/pipermail/java-nlp-user/2016-May/007588.html
 * http://stackoverflow.com/questions/36634101/dependency-parsing-for-french-with-corenlp
 */
public class test_stanfordnlp {

    public static void main(String[] args) throws IOException {
        // creates a StanfordCoreNLP object, with POS tagging, lemmatization, NER, parsing, and preprocessing.coreference resolution
        /*Properties props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse, depparse");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);*/

        StanfordCoreNLP pipeline = new StanfordCoreNLP(
                PropertiesUtils.asProperties(
                        "annotators", "tokenize, ssplit, pos, depparse, parse, lemma",
                        "tokenize.language", "fr",
                        //"ssplit.isOneSentence", "true",
                        "pos.model","edu/stanford/nlp/models/pos-tagger/french/french.tagger",
                        "parse.model", "edu/stanford/nlp/models/lexparser/frenchFactored.ser.gz",
                        "depparse.model","edu/stanford/nlp/models/parser/nndep/UD_French.gz"));

        // read some text from the file..
       // File inputFile = new File("src/test/resources/sample-content.txt");
       /* String text = "Les chats mangent la souris. Des chats mangent des souries. Le chat mange des grandes souries. L'enfant mange la pomme verte." +
                "Un enfant mange une pomme."    ;*/
        String text="La ligne s'anime également en mai avec une édition limitée Summer, rafraîchie d'ananas, création d'Ann Filpo et Catlos Benam." +
                "La jeune femme incarnera Manifesto, le nouveau parfum féminin d la griffe, qui sort à la rentrée, construit autour du jasmin et de la vanille." +
                "Prodigy Night repose sur la Bio-Sève Moléculaire, actif vedette de la gamme Prodigy.";
        // create an empty Annotation just with the given text
        Annotation document = new Annotation(text);

        // run all Annotators on this text
        pipeline.annotate(document);

        // these are all the sentences in this document
        // a CoreMap is essentially a Map that uses class objects as keys and has values with custom types
        List<CoreMap> sentences = document.get(SentencesAnnotation.class);

        for(CoreMap sentence: sentences) {
            // traversing the words in the current sentence
            // a CoreLabel is a CoreMap with additional token-specific methods
            for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
                // this is the text of the token
                String word = token.get(TextAnnotation.class);
                // this is the POS tag of the token
                String pos = token.get(PartOfSpeechAnnotation.class);
                // this is the NER label of the token
                String ne = token.get(NamedEntityTagAnnotation.class);



                System.out.println("word: " + word + " pos: " + pos + " ne:" + ne);
            }

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
            List<SemanticGraphEdge> incomingEdgesSorted =
                    dependencies.getIncomingEdgesSorted(firstRoot);

            for(SemanticGraphEdge edge : incomingEdgesSorted)
            {
                // Getting the target node with attached edges
                IndexedWord dep = edge.getDependent();

                // Getting the source node with attached edges
                IndexedWord gov = edge.getGovernor();

                // Get the relation name between them
                GrammaticalRelation relation = edge.getRelation();
            }

            // this section is same as above just we retrieve the OutEdges
            List<SemanticGraphEdge> outEdgesSorted = dependencies.getOutEdgesSorted(firstRoot);
            for(SemanticGraphEdge edge : outEdgesSorted)
            {
                IndexedWord dep = edge.getDependent();
                System.out.println("Dependent=" + dep);
                IndexedWord gov = edge.getGovernor();
                System.out.println("Governor=" + gov);
                GrammaticalRelation relation = edge.getRelation();
                System.out.println("Relation=" + relation);
            }
        }

    }

}

