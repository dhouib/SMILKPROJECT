package coreference;

import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;
import opennlp.tools.cmdline.PerformanceMonitor;
import opennlp.tools.cmdline.parser.ParserTool;
import opennlp.tools.cmdline.postag.POSModelLoader;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.parser.Parse;
import opennlp.tools.parser.Parser;
import opennlp.tools.parser.ParserFactory;
import opennlp.tools.parser.ParserModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSSample;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.tokenize.WhitespaceTokenizer;
import opennlp.tools.util.*;

import java.io.*;
import java.util.*;

/**
 * Created by dhouib on 09/08/2016.
 * http://www.programcreek.com/2012/05/opennlp-tutorial/
 */
public class openNLPTest {

    //static LinkedHashMap<String, LinkedHashMap<String, Integer>> antecedant = new LinkedHashMap<String, LinkedHashMap<String, Integer>>();
    static LinkedHashMap<String, String> antecedant = new LinkedHashMap<String, String>();

    static LinkedHashMap<String, Integer> anaphore_rank=new LinkedHashMap<String, Integer>();
    public static void main(String[] args) throws Exception {
        String input = "Tu as invité Pierre, le garçon très charmant." +
                "Tu as invité Pierre, ce garçon très charmant.";


         SentenceDetect();
          Tokenize();
        findName();
       POSTag();
        chunk(input);
    }

    public static void SentenceDetect() throws InvalidFormatException,
            IOException {
        String paragraph = "Tu as invité Pierre. Le garçon très charmant.";

        // always start with a model, a model is learned from training data
        InputStream is = new FileInputStream("C:/Users/dhouib/Desktop/SMILK_project_devpt/RelationExtractionSMILK/src/main/resources/fr-sent.bin");
        SentenceModel model = new SentenceModel(is);
        SentenceDetectorME sdetector = new SentenceDetectorME(model);
        String sentences[] = sdetector.sentDetect(paragraph);
        System.out.println(sentences[0]);
        System.out.println(sentences[1]);
        is.close();
    }

    public static void Tokenize() throws InvalidFormatException, IOException {
        InputStream is = new FileInputStream("C:/Users/dhouib/Desktop/SMILK_project_devpt/RelationExtractionSMILK/src/main/resources/token.bin");

        TokenizerModel model = new TokenizerModel(is);

        Tokenizer tokenizer = new TokenizerME(model);

        String tokens[] = tokenizer.tokenize("Tu as invité Pierre. Le garçon très charmant.");

        for (String a : tokens)
            System.out.println(a);

        is.close();
    }

    public static void findName() throws IOException {
        InputStream is = new FileInputStream("C:/Users/dhouib/Desktop/SMILK_project_devpt/RelationExtractionSMILK/src/main/resources/ner-person.bin");

        TokenNameFinderModel model = new TokenNameFinderModel(is);
        is.close();

        NameFinderME nameFinder = new NameFinderME(model);

        String []sentence = new String[]{
                "françois",
                "hollande",
                "président",
                "du",
                "Sénat"
        };

        opennlp.tools.util.Span[] nameSpans = nameFinder.find(sentence);
        System.out.println("NER");
        for(opennlp.tools.util.Span s: nameSpans)
            System.out.println(s.toString());
    }


    public static void POSTag() throws IOException {
        POSModel model = new POSModelLoader()
                .load(new File("C:/Users/dhouib/Desktop/SMILK_project_devpt/RelationExtractionSMILK/src/main/resources/pos.bin"));
        PerformanceMonitor perfMon = new PerformanceMonitor(System.err, "sent");
        POSTaggerME tagger = new POSTaggerME(model);

        String input = "Tu as invité Pierre. Le garçon très charmant.";
        ObjectStream<String> lineStream = new PlainTextByLineStream(
                new StringReader(input));

        perfMon.start();
        String line;
        while ((line = lineStream.read()) != null) {

            String whitespaceTokenizerLine[] = WhitespaceTokenizer.INSTANCE
                    .tokenize(line);
            String[] tags = tagger.tag(whitespaceTokenizerLine);

            POSSample sample = new POSSample(whitespaceTokenizerLine, tags);
            System.out.println(sample.toString());

            perfMon.incrementCounter();
        }
        perfMon.stopAndPrintFinalResult();
    }


    public static void chunk(String input) throws IOException {
        POSModel model = new POSModelLoader()
                .load(new File("C:/Users/dhouib/Desktop/SMILK_project_devpt/RelationExtractionSMILK/src/main/resources/pos.bin"));
        PerformanceMonitor perfMon = new PerformanceMonitor(System.err, "sent");
        POSTaggerME tagger = new POSTaggerME(model);

        ObjectStream<String> lineStream = new PlainTextByLineStream(
                new StringReader(input));
        perfMon.start();
        String line;
        String whitespaceTokenizerLine[] = null;
        String[] tags = null;
        while ((line = lineStream.read()) != null) {
            whitespaceTokenizerLine = WhitespaceTokenizer.INSTANCE
                    .tokenize(line);
            tags = tagger.tag(whitespaceTokenizerLine);

            POSSample sample = new POSSample(whitespaceTokenizerLine, tags);
            System.out.println(sample.toString());
            perfMon.incrementCounter();
        }

        perfMon.stopAndPrintFinalResult();
        // chunker
        InputStream is = new FileInputStream("C:/Users/dhouib/Desktop/SMILK_project_devpt/RelationExtractionSMILK/src/main/resources/fr-chunk.bin");
        ChunkerModel cModel = new ChunkerModel(is);

        ChunkerME chunkerME = new ChunkerME(cModel);
        String result[] = chunkerME.chunk(whitespaceTokenizerLine, tags);
        for (String s : result) {
            System.out.println(s);
        }
        Span[] span = chunkerME.chunkAsSpans(whitespaceTokenizerLine, tags);
        String[] text = whitespaceTokenizerLine.clone();
        constructNounPhrases(span, text, tags, input);
    }

    //
    private static void constructNounPhrases(Span[] span, String[] text, String[] tags, String input){
        //nounPhrases(token, POS)
        LinkedHashMap<String, String> nounPhrases = new LinkedHashMap<String, String>();
        List<String> list_noun=new ArrayList<>();
       // HashList<String, String> test=new HashList();

        for (Span s : span) {
            System.out.println("Span: " + s.toString());
            if (s.toString().contains("NP")) {
                int start = s.getStart();
                int end = s.getEnd();
                int chek = end - start;
                System.out.println("s: " + s.toString() + " " + " " + s.getType() + " " + start + " " + end);
                if(text[start].equals(text [end-1])){
                    list_noun.add(text[start]);
                    nounPhrases.put(text[start],tags[start]);
                }
                else
                { list_noun.add(text[start]+ " "+ text [end-1]);
                nounPhrases.put(text[start] + " " + text[end - 1], tags[start]+ " "+tags[end-1]);
                }
                for (String n : list_noun){
                    System.out.println("List: "+ n);


                }
               /* if (chek == 1) {
                    if (text[start].indexOf(".") > 0) {
                        String noun = text[start];
                        noun = noun.substring(0, noun.indexOf('.'));
                        nounPhrases.put(noun, tags[start]);
                    }
                    if (text[start].indexOf(",") > 0) {
                        String noun = text[start];
                        noun = noun.substring(0, noun.indexOf(','));
                        nounPhrases.put(noun, tags[start]);
                    } else
                        nounPhrases.put(text[start], tags[start]);
                }
                else if (chek > 1) {
                    nounPhrases.put(text[start] + " " + text[end - 1], tags[start]+ " "+tags[end-1]);
                }*/
            }
        }

        List<String> peopleByAge = new ArrayList<String>(nounPhrases.values());


        filtreAnaphoreExpression(nounPhrases, input);
    }

    private static void filtreAnaphoreExpression(LinkedHashMap<String, String> nounPhrases, String input){
        List<String> anaphore = new ArrayList<String>();
        Set set = nounPhrases.entrySet();
        Iterator iteratorhmap = set.iterator();
        int rank=0;
        while (iteratorhmap.hasNext()) {
            Map.Entry mentry = (Map.Entry) iteratorhmap.next();
            Object key = mentry.getKey();
            String values = (String) mentry.getValue();
            System.out.println("Key: " + key + " Values: " + values);
            if (!values.equals("NC")) {

                if (!values.contains("DET")) {
                    rank++;
                }
                else
                    rank=0;
                anaphore.add(key.toString());
                anaphore_rank.put(key.toString(), rank);
            }
        }
        constructCandidats(anaphore, anaphore_rank, input);
    }


    private static void constructCandidats(List<String> anaphore, HashMap<String, Integer> anaphore_rank, String input){
        for (int a = anaphore.size()-1; a >=0; a--) {
            String antecedentChoisi = null;
            int currentRank = 0;
            for (int j = 0; j < a; j++) {
                //System.out.println("antécédent: " + anaphore.get(a) + " anaphore " + anaphore.get(j));
              //  addToMap(anaphore.get(a), anaphore.get(j));
                String key = anaphore.get(j);
                if(antecedentChoisi == null){
                    antecedentChoisi = key;
                    currentRank = anaphore_rank.get(key);
                } else {
                    if(currentRank < anaphore_rank.get(key)){
                        antecedentChoisi = key;
                    }
                }
/*                for (Map.Entry<String, Integer> entry : anaphore_rank.entrySet()) {
                   // if (anaphore.get(a).equals(entry.getKey())) {
                        if(anaphore.get(j).equals(entry.getKey())){
                        System.out.println("antécédent: " + anaphore.get(a) + "Keyrank : " + entry.getKey() + " Value : " + entry.getValue());
                            anaphore_rank.put(entry.getKey().toString(),entry.getValue());
                            antecedant.put(anaphore.get(a), (LinkedHashMap<String, Integer>) anaphore_rank);
                    }

                }*/
                antecedant.put(anaphore.get(a),antecedentChoisi);
                replaceAnaphore(anaphore.get(a), antecedentChoisi, input);
            }

        }
        System.out.println(antecedant);

       // rankingCandidate(antecedant, input);
    }

    private static String replaceAnaphore(Object key, String replace_token, String input){
        System.out.println("key: "+ key + " replace: "+ replace_token);
        input= input.replace(key.toString(), replace_token);
        System.out.println("input: "+ input);
        return input;
    }

    /*private static void addToMap(String div, String brand) {
        if(!antecedant.containsKey(div)){
            antecedant.put(div, new ArrayList<String>());
        }
        antecedant.get(div).add(brand);
    }*/


}
