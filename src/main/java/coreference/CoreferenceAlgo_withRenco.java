package coreference;

import fr.inria.smilk.ws.relationextraction.bean.Token;
import fr.inria.smilk.ws.relationextraction.util.ListFilesUtil;
import opennlp.tools.util.Span;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static fr.inria.smilk.ws.relationextraction.ExtractionHelper.elementToToken;

/**
 * Created by dhouib on 09/08/2016.
 * http://www.programcreek.com/2012/05/opennlp-tutorial/
 */
public class CoreferenceAlgo_withRenco {

    static LinkedHashMap<Span, Span> antecedant = new LinkedHashMap<Span, Span>();
    static File folder = new File("C:/Users/dhouib/Desktop/SMILK_project_devpt/RelationExtractionSMILK/src/main/resources/input/test1/");
    static String folder_name = "C:/Users/dhouib/Desktop/SMILK_project_devpt/RelationExtractionSMILK/src/main/resources/input/test1/";


    static LinkedHashMap<String, String> replace = new LinkedHashMap<String, String>();

    static LinkedHashMap<Token, String> replacebyID = new LinkedHashMap<Token, String>();

    public static void main(String[] args) throws Exception {

        readFiles();
    }

    //lecture et écriture des fichiers
    public static void readFiles() throws Exception {
        ListFilesUtil listFileUtil = new ListFilesUtil();
        listFileUtil.listFilesFromDirector(folder_name);
        File[] listOfFiles = folder.listFiles();
        List<String> files = listFileUtil.files;
        List<String> lines = new LinkedList<>();
        int j = 0;
        String[] filesArray = files.toArray(new String[]{});
        // sortFiles(filesArray);
        sortFiles(filesArray);

        for (String file : filesArray) {

            System.out.println("Processing file #: " + file + ": " + j);
            replace.clear();
            replacebyID.clear();
            j++;
            BufferedReader fileReader = null;

            //Create the file reader
            FileInputStream is = new FileInputStream(folder_name + "/" + file);

            InputStreamReader isr = new InputStreamReader(is, Charset.forName("utf-8"));
            String line1 = "";
            fileReader = new BufferedReader(isr);
            StringBuilder fileContentBuilder = new StringBuilder();
            while ((line1 = fileReader.readLine()) != null) {
                if (line1.trim().length() > 1) {
                    System.out.println("line1: " + line1);
                    Renco renco = new Renco();
                    rulesBelongsToGroup(line1, renco.rencoByWebService(line1));
                }
                fileContentBuilder.append(line1);
            }

            String toWrite=replaceAllFilesById(replacebyID,fileContentBuilder.toString());
           // String toWrite = replaceAllFiles(replace, fileContentBuilder.toString());

            FileWriter writer = new FileWriter("C:/Users/dhouib/Desktop/SMILK_project_devpt/RelationExtractionSMILK/src/main/resources/out_coreference/" + file, false);

            writer.write(toWrite);
            writer.close();
        }
    }

    //Sort Files Corpus
    private static void sortFiles(String[] filenames) {
        Arrays.sort(filenames, new Comparator<String>() {
            public int compare(String f1, String f2) {
                try {
                    int i1 = Integer.parseInt(f1.substring(0,f1.indexOf(".")));
                    int i2 = Integer.parseInt(f2.substring(0,f2.indexOf(".")));
                    return i1 - i2;
                } catch (NumberFormatException e) {
                    throw new AssertionError(e);
                }
            }
        });
    }


    //Analyse la sortie de renco
    private static void rulesBelongsToGroup(String line, String input) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        StringBuilder xmlStringBuilder = new StringBuilder();
        xmlStringBuilder.append(input);
        //System.out.println(input);
        ByteArrayInputStream in = new ByteArrayInputStream(xmlStringBuilder.toString().getBytes("UTF-8"));//"UTF-8"
        Document doc = builder.parse(in);
        doc.getDocumentElement().normalize();
        NodeList nSentenceList = doc.getElementsByTagName("sentence");
        getPN(line, nSentenceList);
    }

    //pour chaque Element de la sortie renco il renvoie sa morphologie
    private static String getMorphologie(Element xElement) {
        String morphologie = new String();

        NodeList morphoAnnotation = xElement.getElementsByTagName("feature");
        for (int j = 0; j < morphoAnnotation.getLength(); j++) {
            final Element morpholo = (Element) morphoAnnotation.item(j);
            morphologie = morpholo.getAttribute("value");
        }

        return morphologie;
    }

    //génére la liste des expressions référentielles
    private static void getPN(String line, NodeList nSentenceList) throws IOException {
        List<Token> list_EN = new ArrayList<>();
        for (int sent_temp = 0; sent_temp < nSentenceList.getLength(); sent_temp++) {
            Node nSentNode = nSentenceList.item(sent_temp);
            StringBuilder builder = new StringBuilder();
            String sentence = line;
            // System.out.println("sentence:" + sentence);
            NodeList nTokensList = nSentNode.getChildNodes();
            //parcourir l'arbre Renco

            for (int token_temp = 0; token_temp < nTokensList.getLength(); token_temp++) {
                Node nTokenNode = nTokensList.item(token_temp);
                if (nTokenNode instanceof Element) {
                    NodeList nList = nTokenNode.getChildNodes();
                    Node nNode = nList.item(token_temp);
                    int y = 0;
                    for (int x = 0; x < nList.getLength(); x++) {

                        Node xNode = nList.item(x);

                        if (xNode instanceof Element) {
                            Element xElement = (Element) xNode;
                            if (xElement.hasAttribute("type") && !xElement.getAttribute("type").equalsIgnoreCase("not_identified")) {
                                Token token_NP = new Token();
                                token_NP = elementToToken(xElement);
                                list_EN.add(token_NP);
                                String morph = getMorphologie(xElement);
                                //System.out.println("morpho: " + xElement.getAttribute("form") + ":" + morph);
                                token_NP.setMorpho(morph);


                            } else if (xElement.getAttribute("depRel").equalsIgnoreCase("root") && (xElement.getAttribute("pos").equalsIgnoreCase("NPP")/*||xElement.getAttribute("pos").equalsIgnoreCase("NC")*/)) {
                                Token token_NP = new Token();
                                token_NP = elementToToken(xElement);
                                list_EN.add(token_NP);

                                String morph = getMorphologie(xElement);
                                //System.out.println("morpho: " + xElement.getAttribute("form") + ":" + morph);
                                token_NP.setMorpho(morph);

                            } else if (xElement.getAttribute("depRel").equalsIgnoreCase("suj") && xElement.getAttribute("pos").equalsIgnoreCase("CLS")) {
                                Token token_NP = new Token();
                                token_NP = elementToToken(xElement);
                                list_EN.add(token_NP);

                                String morph = getMorphologie(xElement);
                                //System.out.println("morpho: " + xElement.getAttribute("form") + ":" + morph);
                                token_NP.setMorpho(morph);

                            } else if (!xElement.hasAttribute("type") || xElement.getAttribute("type").equalsIgnoreCase("not_identified")) {
                                if (xElement.getAttribute("depRel").equalsIgnoreCase("det") && xElement.getAttribute("pos").equalsIgnoreCase("DET")) {
                                    y = x + 1;
                                    for (int j = y; j < nList.getLength(); j++) {
                                        Node yNode = nList.item(j);
                                        if (yNode instanceof Element) {
                                            Element yElement = (Element) yNode;
                                            if (yElement.getAttribute("depRel").equalsIgnoreCase("suj") && yElement.getAttribute("pos").equalsIgnoreCase("NC")) {

                                                Token token_NP = new Token();
                                                token_NP = elementToToken(xElement, yElement);
                                                list_EN.add(token_NP);

                                                String morph = getMorphologie(xElement);
                                                //System.out.println("morpho: " + xElement.getAttribute("form") + ":" + morph);

                                                String morph2 = getMorphologie(yElement);
                                                //System.out.println("morpho2: " + yElement.getAttribute("form") + ":" + morph2);
                                                token_NP.setMorpho(morph);

                                            } else {
                                                y = j;
                                                break;
                                            }

                                        }
                                    }
                                    x = y;
                                } else {
                                    x += 1;
                                }

                            } else {
                                x += 1;
                            }

                        }
                        //   }

                    }
                }
            }
        }
        //for (Token l : list_EN) {
        //    System.out.println("EN: " + l.getForm());
        //}
        constructAntecedent(line, list_EN);
    }

    //Identification des expressions référentielles
    public static void getEN(String line, NodeList nSentenceList) throws IOException {
        List<Token> list_EN = new ArrayList<>();
        for (int sent_temp = 0; sent_temp < nSentenceList.getLength(); sent_temp++) {
            Node nSentNode = nSentenceList.item(sent_temp);
            StringBuilder builder = new StringBuilder();
            String sentence = line;
            //System.out.println("sentence:" + sentence);
            NodeList nTokensList = nSentNode.getChildNodes();
            //parcourir l'arbre Renco

            for (int token_temp = 0; token_temp < nTokensList.getLength(); token_temp++) {
                Node nTokenNode = nTokensList.item(token_temp);
                NodeList nList = nTokenNode.getChildNodes();
                Node nNode = nList.item(token_temp);
                for (int j = 0; j < nList.getLength(); j++) {
                    Node xNode = nList.item(j);
                    if (xNode instanceof Element) {
                        Element xElement = (Element) xNode;

                        if (xElement.hasAttribute("type") && !xElement.getAttribute("type").equalsIgnoreCase("not_identified")) {
                            Token token_NP = new Token();
                            token_NP = elementToToken(xElement);
                            list_EN.add(token_NP);

                        } else if (!xElement.hasAttribute("type") || xElement.getAttribute("type").equalsIgnoreCase("not_identified")) {
                            if (xElement.getAttribute("depRel").equalsIgnoreCase("suj") && xElement.getAttribute("pos").equalsIgnoreCase("NC")
                                    ) {

                                Token token_NP = new Token();
                                token_NP = elementToToken(xElement);
                                list_EN.add(token_NP);
                            }
                          /*  else if (xElement.getAttribute("depRel").equalsIgnoreCase("obj")&& xElement.getAttribute("pos").equalsIgnoreCase("NC")) {
                                Token token_NP=new Token();
                                token_NP= elementToToken(xElement);
                                list_EN.add(token_NP);
                            }*/
                            else if (xElement.getAttribute("depRel").equalsIgnoreCase("root") && (xElement.getAttribute("pos").equalsIgnoreCase("NPP")/*||xElement.getAttribute("pos").equalsIgnoreCase("NC")*/)) {
                                Token token_NP = new Token();
                                token_NP = elementToToken(xElement);
                                list_EN.add(token_NP);
                            }
                        }
                    }
                }

            }

        }
        //for (Token l : list_EN) {
        //    System.out.println("EN: " + l.getForm());
        //}
        constructAntecedent(line, list_EN);
    }


    //Generation des antecedents candidats
    private static void constructAntecedent(String line, List<Token> list_EN) throws IOException {
        LinkedHashMap<Token, List<TokenWrapper>> antecedent = new LinkedHashMap<>();

        for (Token l : list_EN) {
            if (!l.getType().equalsIgnoreCase("product") && !l.getType().equalsIgnoreCase("range") && !l.getType().equalsIgnoreCase("brand") && !l.getType().equalsIgnoreCase("division")
                    && !l.getType().equalsIgnoreCase("group") && !l.getType().equalsIgnoreCase("not_identified") && !l.getDepRel().equalsIgnoreCase("root")) {
                //System.out.println("test: " + l.getForm());

                List<TokenWrapper> antecedent_list = new ArrayList<>();
                for (int i = 0; i < list_EN.indexOf(l); i++) {
                    TokenWrapper tokenWrapper = new TokenWrapper();
                    tokenWrapper.setToken(list_EN.get(i));
                  if (l.getMorpho().equalsIgnoreCase(list_EN.get(i).getMorpho()) || (list_EN.get(i).getMorpho().isEmpty() && !list_EN.get(i).getPos().equalsIgnoreCase("CLS"))) {
                        //System.out.println("l: " + l.getForm() + " " + l.getMorpho() + " i: " + list_EN.get(i).getForm() + " " + list_EN.get(i).getMorpho()+ " pos: "+ list_EN.get(i).getPos());
                        antecedent_list.add(tokenWrapper);
                    }
                }
                antecedent.put(l, antecedent_list);
            }
        }

        setRank(line, antecedent);
    }

    private static void setRank(String line, LinkedHashMap<Token, List<TokenWrapper>> antecedentFiltred) throws IOException {
        for (Map.Entry<Token, List<TokenWrapper>> entry : antecedentFiltred.entrySet()) {
            Object key = entry.getKey();
            Token token = (Token) key;
            List<TokenWrapper> values = (List<TokenWrapper>) entry.getValue();
            //for (TokenWrapper s : values) {
             //   System.out.println("Key antecedent: " + token.getForm() + "type: " + token.getMorpho() + " s: " + s.getToken().getForm() + " " + s.getToken().getMorpho());
            //}
        }
        setRankingbytype(line, antecedentFiltred);
        setRankingbyDepRel(line, antecedentFiltred);
        setRankingByTypeAndForm(line, antecedentFiltred);
        setRankingbyDistance(line, antecedentFiltred);
        setRanking(line, antecedentFiltred);
    }

    // Attribution de score
    private static void setRankingbytype(String line, LinkedHashMap<Token, List<TokenWrapper>> antecedent) throws IOException {
        int rank_type = 0;
        for (Map.Entry<Token, List<TokenWrapper>> entry : antecedent.entrySet()) {
            Object key = entry.getKey();
            Token token = (Token) key;
            List<TokenWrapper> values = (List<TokenWrapper>) entry.getValue();
            for (TokenWrapper s : values) {
                if (s.getToken().getType().equalsIgnoreCase("product") || s.getToken().getType().equalsIgnoreCase("brand") || s.getToken().getType().equalsIgnoreCase("division") || s.getToken().getType().equalsIgnoreCase("group")) {
                    rank_type = 100;
                    s.setRanking_type(rank_type);
                    //System.out.println("Key Pooos: " + token.getForm() + " s: " + s.getToken().getForm() + " rank: " + s.getRanking_type());
                } else {
                    rank_type = 0;
                    s.setRanking_type(rank_type);
                    //System.out.println("Key Pooos: " + token.getForm() + " s: " + s.getToken().getForm() + " rank: " + s.getRanking_type());
                }
            }
        }
    }

    private static void setRankingbyDepRel(String line, LinkedHashMap<Token, List<TokenWrapper>> antecedent) throws IOException {
        int rank_DepRel = 0;
        for (Map.Entry<Token, List<TokenWrapper>> entry : antecedent.entrySet()) {
            Object key = entry.getKey();
            Token token = (Token) key;
            List<TokenWrapper> values = (List<TokenWrapper>) entry.getValue();
            for (TokenWrapper s : values) {
                if ((s.getToken().getDepRel().equalsIgnoreCase("root") && s.getToken().getPos().equalsIgnoreCase("NPP"))) {
                    rank_DepRel = 120;
                    s.setRanking_dep_rel(rank_DepRel);
                    // System.out.println("Key Pooos: " + token.getForm()+ " s: " + s.getToken().getForm()+ " rank: " + s.getRanking_pos());
                }
                if (s.getToken().getDepRel().equalsIgnoreCase("suj")) {
                    rank_DepRel = 80;
                    s.setRanking_dep_rel(rank_DepRel);
                    // System.out.println("Key Pooos: " + token.getForm()+ " s: " + s.getToken().getForm()+ " rank: " + s.getRanking_pos());
                }
                if (s.getToken().getDepRel().equalsIgnoreCase("obj") || (s.getToken().getDepRel().equalsIgnoreCase("mod"))) {
                    rank_DepRel = 50;
                    s.setRanking_dep_rel(rank_DepRel);
                    //  System.out.println("Key Pooos: " + token.getForm()+ " s: " + s.getToken().getForm()+ " rank: " + s.getRanking_pos());
                }
            }
        }

    }

    private static void setRankingByTypeAndForm(String line, LinkedHashMap<Token, List<TokenWrapper>> antecedent) throws IOException {
        int rank_type_form = 0;
        for (Map.Entry<Token, List<TokenWrapper>> entry : antecedent.entrySet()) {
            Object key = entry.getKey();
            Token token = (Token) key;
            List<TokenWrapper> values = (List<TokenWrapper>) entry.getValue();
            for (TokenWrapper s : values) {
                if (token.getForm().equals("division")) {
                    if (s.getToken().getType().equalsIgnoreCase("division")) {
                        rank_type_form = rank_type_form + 100;
                        s.setRanking_type_form(rank_type_form);
                        // System.out.println("Key Pooos: " + token.getForm()+ " s: " + s.getToken().getForm()+ " rank: " + s.getRanking_pos());
                    } else {
                        rank_type_form = 0;
                        s.setRanking_type_form(rank_type_form);
                        //  System.out.println("Key Pooos: " + token.getForm()+ " s: " + s.getToken().getForm()+ " rank: " + s.getRanking_pos());
                    }
                }


                if (token.getForm().equals("jus")) {
                    if (s.getToken().getType().equalsIgnoreCase("product")) {
                        rank_type_form = rank_type_form + 100;
                        s.setRanking_type_form(rank_type_form);
                        // System.out.println("Key Pooos: " + token.getForm()+ " s: " + s.getToken().getForm()+ " rank: " + s.getRanking_pos());
                    } else {
                        rank_type_form = 0;
                        s.setRanking_type_form(rank_type_form);
                        //  System.out.println("Key Pooos: " + token.getForm()+ " s: " + s.getToken().getForm()+ " rank: " + s.getRanking_pos());
                    }
                }
            }
        }

    }

    private static void setRankingbyDistance(String line, LinkedHashMap<Token, List<TokenWrapper>> antecedent) throws IOException {
        for (Map.Entry<Token, List<TokenWrapper>> entry : antecedent.entrySet()) {
            Object key = entry.getKey();
            Token token = (Token) key;
            List<TokenWrapper> values = (List<TokenWrapper>) entry.getValue();
            int rank_dist = 0;
            for (TokenWrapper s : values) {
                rank_dist = rank_dist + 10;
                s.setRanking_dist(rank_dist);
                // System.out.println("Key Pooos: " + token.getForm()+ " s: " + s.getToken().getForm()+ " rank: " + s.getRanking_dist());
            }
        }
    }

    private static void setRanking(String line, LinkedHashMap<Token, List<TokenWrapper>> antecedent) throws IOException {
        for (Map.Entry<Token, List<TokenWrapper>> entry : antecedent.entrySet()) {
            Object key = entry.getKey();
            Token token = (Token) key;
            List<TokenWrapper> values = (List<TokenWrapper>) entry.getValue();
            for (int j = 0; j < values.size(); j++) {
                values.get(j).setRank(values.get(j).getRanking_dist() + values.get(j).getRanking_type_form() + values.get(j).getRanking_type() + values.get(j).getRanking_dep_rel());
                System.out.println("key: " + token.getForm() + " antecedent: " + values.get(j).getToken().getForm() + " type: " + values.get(j).getRanking_type() + " dist: " + values.get(j).getRanking_dist() +
                        " dep_rel: " + values.get(j).getRanking_dep_rel() + " type_form: " + values.get(j).getRanking_type_form() + "rank: " + values.get(j).getRank());
            }
        }
        choiceCandidat(line, antecedent);
    }

    private static void choiceCandidat(String line, LinkedHashMap<Token, List<TokenWrapper>> antecedent) throws IOException {
        for (Map.Entry<Token, List<TokenWrapper>> entry : antecedent.entrySet()) {
            Object key = entry.getKey();
            Token token = (Token) key;
            List<TokenWrapper> values = (List<TokenWrapper>) entry.getValue();
            //System.out.println(max(values));
            if (!values.isEmpty()) {
                TokenWrapper tokenWrapper = Collections.max(values, new TokenWrapperComparator());
                System.out.println("Token: " + token.getForm() + "maximum: " + tokenWrapper.getToken().getForm());
                replace.put(token.getForm(), tokenWrapper.getToken().getForm());
                replacebyID.put(token,tokenWrapper.getToken().getForm());

            }
        }
    }

private static String replaceAllFilesById(LinkedHashMap<Token, String> replacebyID,String fileContentCorpus){
    StringBuilder builder = new StringBuilder();

    builder = new StringBuilder();
    builder.append(fileContentCorpus);
    fileContentCorpus = builder.toString();


    String toWrite = builder.toString();
    String toWriteold = toWrite;//builder.toString();
    System.out.println("toWritebuilde:"+toWrite);
    StringBuilder result = new StringBuilder();
    // System.out.println("toWrite: "+ toWrite);
    int index_prev_end =0;
    for (Map.Entry<Token, String> entry : replacebyID.entrySet()) {
        int index_start= entry.getKey().getStart();
        int index_end=entry.getKey().getEnd();
        System.out.println("index: "+index_start + ": "+ index_end);
        //System.out.println("length: "+entry.getValue().length());
        result.append(toWriteold.substring(index_prev_end,entry.getKey().getStart())).append(entry.getValue());
        index_prev_end=index_end;
        //toWrite=toWriteold.substring(0,entry.getKey().getStart())+entry.getValue()+ toWriteold.substring(index_start+entry.getKey().getForm().length());
        //toWrite = toWrite.replaceAll(entry.getKey(), entry.getValue());
       // System.out.println("replacebyID: " + entry.getKey() + "  " + entry.getValue());
    }
    result.append(toWriteold.substring(index_prev_end));
    System.out.println("toWritebyID: + " + result.toString());


    return result.toString();
}

    private static String replaceAllFiles(LinkedHashMap<String, String> replace, String fileContentCorpus) throws IOException {
        StringBuilder builder = new StringBuilder();

        builder = new StringBuilder();
        builder.append(fileContentCorpus);
        fileContentCorpus = builder.toString();


        String toWrite = builder.toString();
        // System.out.println("toWrite: "+ toWrite);
        for (Map.Entry<String, String> entry : replace.entrySet()) {
            toWrite = toWrite.replaceAll(entry.getKey(), entry.getValue());
            System.out.println("replace: " + entry.getKey() + "  " + entry.getValue());
        }
        System.out.println("toWrite: + " + toWrite);


        return toWrite;
    }



}

