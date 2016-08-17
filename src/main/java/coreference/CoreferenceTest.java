package coreference;

import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;
import opennlp.tools.cmdline.PerformanceMonitor;
import opennlp.tools.cmdline.postag.POSModelLoader;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSSample;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.WhitespaceTokenizer;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.PlainTextByLineStream;
import opennlp.tools.util.Span;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by dhouib on 09/08/2016.
 * http://www.programcreek.com/2012/05/opennlp-tutorial/
 */
public class CoreferenceTest {

    //static LinkedHashMap<String, LinkedHashMap<String, Integer>> antecedant = new LinkedHashMap<String, LinkedHashMap<String, Integer>>();
    static LinkedHashMap<Span, Span> antecedant = new LinkedHashMap<Span, Span>();
    static LinkedHashMap<Span, List<Span>> span_antecedent = new LinkedHashMap<Span, List<Span>>();

    static LinkedHashMap<String, Integer> anaphore_rank = new LinkedHashMap<String, Integer>();

    public static void main(String[] args) throws Exception {
        String input = "Tu as invité Jean, le garçon très charmant." +
                       "Tu as invité Pierre, ce garçon très charmant.";

        chunk(input);
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
            //  System.out.println(sample.toString());
            perfMon.incrementCounter();
        }

        perfMon.stopAndPrintFinalResult();
        // chunker
        InputStream is = new FileInputStream("C:/Users/dhouib/Desktop/SMILK_project_devpt/RelationExtractionSMILK/src/main/resources/fr-chunk.bin");
        ChunkerModel cModel = new ChunkerModel(is);

        ChunkerME chunkerME = new ChunkerME(cModel);
        String result[] = chunkerME.chunk(whitespaceTokenizerLine, tags);
        for (String s : result) {
            //  System.out.println(s);
        }
        Span[] span = chunkerME.chunkAsSpans(whitespaceTokenizerLine, tags);
        String[] text = whitespaceTokenizerLine.clone();
        // constructNounPhrases(span, text, tags, input);
        constructNounPhrasesSpan(span, input, tags, text);
    }

    private static void constructNounPhrasesSpan(Span[] span, String input, String[] tags, String[] text) {
        LinkedHashMap<Span, String> nounPhrases = new LinkedHashMap<Span, String>();
        LinkedHashMap<Span, Integer> nounPhrases_stat = new LinkedHashMap<Span, Integer>();
        List<Span> list_noun = new ArrayList<>();
        List<Span> list_np = new ArrayList<>();
        LinkedHashMap<Span, List<Span>> antecedent = new LinkedHashMap<>();
        list_np = Arrays.asList(span).stream().filter(p -> p.getType().equalsIgnoreCase("NP")).collect(Collectors.toList());
        System.out.println("Filter: "+ list_np);
        System.out.println("test:" + list_np.subList(0, 3));

        for (Span l: list_np){

            antecedent.put(l, list_np.subList(0,list_np.indexOf(l)));
            System.out.println("antecedent" +
                    ": "+ antecedent);
        }
        for (Span s : span) {
            System.out.println("Span: " + s.toString());

            if (s.getType().equals("NP")) {
                list_noun.add(s);
                int start = s.getStart();
                int end = s.getEnd();
                int chek = end - start;
                if (chek == 1) {
                    nounPhrases.put(s, tags[start]);
                    nounPhrases_stat.put(s,s.getStart());
                } else if (chek > 1) {
                    nounPhrases.put(s, tags[start] + " " + tags[end - 1]);
                    nounPhrases_stat.put(s,s.getStart());
                }

            }
        }
        System.out.println(list_noun);
        constructAntecedent (list_noun);
        //System.out.println(nounPhrases);
        //System.out.println(nounPhrases_stat);
        //setRanking(nounPhrases, nounPhrases_stat, input, text);
    }

    private static void constructAntecedent ( List<Span> list_noun) {

        for (int i = 0; i < list_noun.size() - 1; i++)
                if (list_noun.get(i).getStart() < list_noun.get(i+1).getStart()) {
                    addToMap(list_noun.get(i+1), list_noun.get(i));
                }
                System.out.println(" span_antecedent" + span_antecedent);
            }


    private static void addToMap(Span div, Span brand) {
          if (!span_antecedent.containsKey(div)) {
              span_antecedent.put(div, new ArrayList<Span>());
          }
        span_antecedent.get(div).add(brand);
      }

    private static void setRanking(LinkedHashMap<Span, String> nounPhrases, LinkedHashMap<Span, Integer> nounPhrases_stat, String input, String[] text) {
        LinkedHashMap<Span, Integer> span_rank = new LinkedHashMap<>();
        Span span = null;
        List<Span> anaphores = new ArrayList<>();
        int rank = 0, rank_pos=0,rank_dis=0;
        for (Map.Entry<Span, String> entry : nounPhrases.entrySet()) {
            Object key = entry.getKey();
            String values = (String) entry.getValue();
            if (!values.contains("DET")) {
                rank_pos++;
            } else {
                rank_pos = 0;
            }

            span = (Span) key;
            anaphores.add(span);

            rank=rank_pos+rank_dis;
            span_rank.put(span, rank);

            System.out.println(span + " " + values + " : " + rank);

        }


        int[] start_values=new int[nounPhrases_stat.size()];
        for (Map.Entry<Span, Integer> entry : nounPhrases_stat.entrySet()) {
            Object key = entry.getKey();
            int values = (int) entry.getValue();

            for (int i=0; i<nounPhrases_stat.size(); i++){
                start_values[i]=values;
                System.out.println(start_values[i]);
            }
        }

        for (int i=0; i<nounPhrases_stat.size()-1; i++){
            int diff=0;
            for(int j=0; j<nounPhrases_stat.size(); j++){
                 diff =start_values[j]-start_values[i];
            }
            System.out.println("diff:" +diff);
        }
        for (Map.Entry<Span, Integer> entry : span_rank.entrySet()) {
            System.out.println("Keyrank : " + entry.getKey() + " Value : " + entry.getValue());
        }
        //constructCandidatsSpan(anaphores, span_rank, input, text);
    }


    //choix de meilleur candidat
    private static void constructCandidatsSpan(List<Span> anaphores, LinkedHashMap<Span, Integer> span_rank, String input, String[] text) {
        for (int a = anaphores.size() - 1; a >= 0; a--) {
            Span antecedentChoisi = null;
            int currentRank = 0;
            for (int j = 0; j < a; j++) {
                Span key = anaphores.get(j);
                if (antecedentChoisi == null) {
                    antecedentChoisi = key;
                    currentRank = span_rank.get(key);
                } else {
                    if (currentRank < span_rank.get(key)) {
                        antecedentChoisi = key;
                    }
                }

                System.out.println(anaphores.get(a) + " " + antecedentChoisi);
                antecedant.put(anaphores.get(a), antecedentChoisi);
            }

        }
        System.out.println(antecedant);
        getToken(antecedant, input, text);
    }


    private static void getToken(LinkedHashMap<Span, Span> antecedant, String input, String[] text) {
        String token = new String();
        String replace = new String();
        for (Map.Entry<Span, Span> entry : antecedant.entrySet()) {
            Object key = entry.getKey();
            Span span = (Span) key;
            int start_token = span.getStart();
            int end_token = span.getEnd();
            Span values = (Span) entry.getValue();
            int start_replace = 0;
            int end_replace = 0;

            start_replace = values.getStart();
            end_replace = values.getEnd();
            System.out.println("start_token: " + start_token + " end_token: " + end_token + " start: " + start_replace + " end: " + end_replace);
            if (end_token - start_token == 1) {
                token = text[start_token];
            } else {
                token = text[start_token] + " " + text[end_token - 1];
            }
            if (end_replace - start_replace == 1) {
                replace = text[start_replace];
            } else {
                token = text[start_replace] + " " + text[end_replace - 1];
            }

            System.out.println("token: " + token + " replace: " + replace);
            replaceAnaphore(token, replace, input);
        }


    }

    private static String replaceAnaphore(String token, String replace, String input) {
        input = input.replace(token, replace);
        System.out.println("input: " + input);
        return input;
    }
}