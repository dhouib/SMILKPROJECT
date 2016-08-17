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
public class CoreferenceAlgo {

    //static LinkedHashMap<String, LinkedHashMap<String, Integer>> antecedant = new LinkedHashMap<String, LinkedHashMap<String, Integer>>();
    static LinkedHashMap<Span, Span> antecedant = new LinkedHashMap<Span, Span>();


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
        constructNounPhrasesSpan(span, input, tags, text);
    }

    //Identification des expressions référentielles
    private static void constructNounPhrasesSpan(Span[] span, String input, String[] tags, String[] text) {
        LinkedHashMap<Span, String> nounPhrases = new LinkedHashMap<Span, String>();
        LinkedHashMap<Span, Integer> nounPhrases_stat = new LinkedHashMap<Span, Integer>();
        //List<Span> list_noun = new ArrayList<>();
        List<Span> list_np = new ArrayList<>();

        list_np = Arrays.asList(span).stream().filter(p -> p.getType().equalsIgnoreCase("NP")).collect(Collectors.toList());
        System.out.println("Filter: " + list_np);
        constructAntecedent(list_np, input, text, tags);
    }

    //Generation des antecedents candidats
    private static void constructAntecedent(List<Span> list_np, String input, String[] text, String[] tags) {
        LinkedHashMap<Span, List<SpanWrapper>> antecedent = new LinkedHashMap<>();

        for (Span l : list_np) {
            List<SpanWrapper> antecedent_list = new ArrayList<>();
            for (int i = 0; i < list_np.indexOf(l); i++) {
                SpanWrapper spanWrapper = new SpanWrapper();
                spanWrapper.setSpan(list_np.get(i));
                antecedent_list.add(spanWrapper);
            }
            antecedent.put(l, antecedent_list);
        }
        setRankingbyPOS(antecedent, input, text, tags);
        setRankingbyDistance(antecedent, input, text, tags);

    }

    // Attribution de score
    private static void setRankingbyPOS(LinkedHashMap<Span, List<SpanWrapper>> antecedent, String input, String[] text, String[] tags) {
        int rank = 0, rank_pos;
        for (Map.Entry<Span, List<SpanWrapper>> entry : antecedent.entrySet()) {
            Object key = entry.getKey();
            List<SpanWrapper> values = (List<SpanWrapper>) entry.getValue();
            for (SpanWrapper s : values) {
                int start = s.getSpan().getStart();
                int end = s.getSpan().getEnd();
                int chek = end - start;
                rank_pos = 0;
                if (chek == 1) {
                    rank_pos++;
                    s.setRanking_pos(rank_pos);
                    System.out.println("Key Pooos: " + key + " s: " + s.getSpan() + " rank: " + s.getRanking_pos());
                } else if (chek > 1) {
                    rank_pos = 0;
                    s.setRanking_pos(rank_pos);
                    System.out.println("Key Pooos: " + key + " s: " + s.getSpan() + " rank: " + s.getRanking_pos());
                }
            }
        }

        setRaning(antecedent, input, text, tags);
    }

    private static void setRankingbyDistance(LinkedHashMap<Span, List<SpanWrapper>> antecedent, String input, String[] text, String[] tags) {
        for (Map.Entry<Span, List<SpanWrapper>> entry : antecedent.entrySet()) {
            Object key = entry.getKey();
            List<SpanWrapper> values = (List<SpanWrapper>) entry.getValue();
            int rank_dist = 0;
            for (SpanWrapper s : values) {
                rank_dist = rank_dist + 10;
                s.setRanking_dist(rank_dist);
                System.out.println("Key Dist: " + key + " s: " + s.getSpan() + " rank: " + s.getRanking_dist());
            }
        }


        setRaning(antecedent, input, text, tags);
    }



    private static void setRaning (LinkedHashMap<Span, List<SpanWrapper>> antecedent, String input, String[] text, String[] tags) {
        for (Map.Entry<Span, List<SpanWrapper>> entry : antecedent.entrySet()) {
            Object key = entry.getKey();
            List<SpanWrapper> values = (List<SpanWrapper>) entry.getValue();
            for (int j = 0; j < values.size(); j++) {
                values.get(j).setRank(values.get(j).getRanking_dist() + values.get(j).getRanking_pos());
                System.out.println("key antecedent: " + key + "antecedent: " + values.get(j).getSpan() + " " + values.get(j).getRanking_pos() + " " + values.get(j).getRanking_dist() + " " + values.get(j).getRank());
            }
        }

        choiceCandidat( antecedent,  input,  text, tags);
    }


    private static void choiceCandidat(LinkedHashMap<Span, List<SpanWrapper>> antecedent, String input, String[] text, String[] tags) {
        for (Map.Entry<Span, List<SpanWrapper>> entry : antecedent.entrySet()) {
            Object key = entry.getKey();
            Span span_token= (Span) key;
            List<SpanWrapper> values = (List<SpanWrapper>) entry.getValue();

            SpanWrapper spanWrapper_choisie = new SpanWrapper();
            //System.out.println("values size:"+values.size());
            for (int j = 0; j < values.size(); j++) {
                spanWrapper_choisie = values.get(j);
                for (int i = 0; i < values.size()-1; i++){
                    if (values.get(j).getRank()>values.get(i).getRank()){
                        spanWrapper_choisie= values.get(j);
                    }
                    else {
                        spanWrapper_choisie= values.get(i);
                    }
                }
                System.out.println(span_token + " "+ spanWrapper_choisie.getSpan());

                replaceWithCoreference ( span_token,  spanWrapper_choisie,  input, text);
            }

        }
    }

    private static void replaceWithCoreference ( Span span_token, SpanWrapper spanWrapper_choisie, String input, String [] text){
        int start_token= span_token.getStart();
        int end_token= span_token.getEnd();
        System.out.println("span: "+span_token+ " start: "+start_token + end_token);
        int start_token_choisie= spanWrapper_choisie.getSpan().getStart();
        int end_token_choisie=spanWrapper_choisie.getSpan().getEnd();
        System.out.println("span choisie: "+spanWrapper_choisie.getSpan()+ " start: "+start_token_choisie + end_token_choisie);

        String token = new String();
        String token_choisie = new String();
        if (end_token - start_token == 1) {
            token = text[start_token];
        } else {
            token = text[start_token] + " " + text[end_token - 1];
        }
        if (end_token_choisie - start_token_choisie == 1) {
            token_choisie = text[start_token_choisie];
        } else {
            token_choisie = text[start_token_choisie] + " " + text[end_token_choisie - 1];
        }

        replaceAnaphore(token, token_choisie, input);
    }

    private static String replaceAnaphore(String token, String token_choisie, String input) {
        input = input.replace(token, token_choisie);
        System.out.println("input: " + input);
        return input;
    }
}