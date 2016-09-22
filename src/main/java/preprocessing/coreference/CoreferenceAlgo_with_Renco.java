package preprocessing.coreference;

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
import java.util.*;

import static fr.inria.smilk.ws.relationextraction.ExtractionHelper.elementToToken;

/**
 * Created by dhouib on 09/08/2016.
 * http://www.programcreek.com/2012/05/opennlp-tutorial/
 */
public class CoreferenceAlgo_with_Renco {

    static LinkedHashMap<Span, Span> antecedant = new LinkedHashMap<Span, Span>();
    static File folder = new File("C:/Users/dhouib/Desktop/SMILK_project_devpt/RelationExtractionSMILK/src/main/resources/input/out");
    static String folder_name = "C:/Users/dhouib/Desktop/SMILK_project_devpt/RelationExtractionSMILK/src/main/resources/input/out";

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
                    // System.out.println("line1: " + line1);
                    Renco renco = new Renco();
                    rulesBelongsToGroup(line1, renco.rencoByWebService(line1));
                }
                fileContentBuilder.append(line1);
            }

            String toWrite = replaceAllFilesById(replacebyID, fileContentBuilder.toString());
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
                    int i1 = Integer.parseInt(f1.substring(0, f1.indexOf("L")).concat(f1.substring(f1.indexOf("l") + 1, f1.indexOf("."))));// = Integer.parseInt(f1.substring(0, f1_repalace.indexOf(".")));
                    int i2 = Integer.parseInt(f2.substring(0, f2.indexOf("L")).concat(f2.substring(f2.indexOf("l") + 1, f2.indexOf("."))));// = Integer.parseInt(f2.substring(0, f2_replace.indexOf(".")));
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
        System.out.println(input);
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

    //Identification des expressions référentielles
    private static void getPN(String line, NodeList nSentenceList) throws IOException {
        List<Token> list_EN = new ArrayList<>();
        for (int sent_temp = 0; sent_temp < nSentenceList.getLength(); sent_temp++) {
            Node nSentNode = nSentenceList.item(sent_temp);
            StringBuilder builder = new StringBuilder();
            String sentence = line;
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
                            //Si XElement a un type (product, range, brand, division, group
                            if (xElement.hasAttribute("type") && !xElement.getAttribute("type").equalsIgnoreCase("not_identified")) {
                                Token token_NP = new Token();
                                token_NP = elementToToken(xElement);
                                list_EN.add(token_NP);
                                String morph = getMorphologie(xElement);
                                token_NP.setMorpho(morph);
                            }
                            // si xElement à depRel=root && pos=NPP
                            else if (xElement.getAttribute("depRel").equalsIgnoreCase("root") && (xElement.getAttribute("pos").equalsIgnoreCase("NPP")/*||xElement.getAttribute("pos").equalsIgnoreCase("NC")*/)) {
                                Token token_NP = new Token();
                                token_NP = elementToToken(xElement);
                                list_EN.add(token_NP);
                                String morph = getMorphologie(xElement);
                                token_NP.setMorpho(morph);
                            }
                            // si xElement à depRel=suj && pos=CLS
                            else if (xElement.getAttribute("depRel").equalsIgnoreCase("suj") && xElement.getAttribute("pos").equalsIgnoreCase("CLS")) {
                                Token token_NP = new Token();
                                token_NP = elementToToken(xElement);
                                list_EN.add(token_NP);
                                String morph = getMorphologie(xElement);
                                token_NP.setMorpho(morph);
                            }
                            // traite le cas où on a "ce jus" si
                            else if (!xElement.hasAttribute("type") || xElement.getAttribute("type").equalsIgnoreCase("not_identified")) {
                                if (xElement.getAttribute("depRel").equalsIgnoreCase("det") && xElement.getAttribute("pos").equalsIgnoreCase("DET")) {
                                    System.out.println(xElement.getAttribute("form"));
                                    y = x + 1;
                                    for (int j = y; j < nList.getLength(); j++) {
                                        Node yNode = nList.item(j);
                                        if (yNode instanceof Element) {
                                            Element yElement = (Element) yNode;
                                            if (yElement.getAttribute("depRel").equalsIgnoreCase("suj") && yElement.getAttribute("pos").equalsIgnoreCase("NC")) {
                                                System.out.println(yElement.getAttribute("form"));
                                                Token token_NP = new Token();
                                                token_NP = elementToToken(xElement, yElement);
                                                list_EN.add(token_NP);
                                                String morph = getMorphologie(xElement);
                                                String morph2 = getMorphologie(yElement);
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
                    }
                }
            }
        }
        constructAntecedent(line, list_EN);
    }

    //Filtrage des expression référentielles && construction des candidats
    private static void constructAntecedent(String line, List<Token> list_EN) throws IOException {
        LinkedHashMap<Token, List<TokenWrapper>> antecedent = new LinkedHashMap<>();

        for (Token l : list_EN) {

            // on ne considére pas les entités nommes comme des expressions de coréférence
            if (!l.getType().equalsIgnoreCase("product") && !l.getType().equalsIgnoreCase("range") && !l.getType().equalsIgnoreCase("brand") && !l.getType().equalsIgnoreCase("division")
                    && !l.getType().equalsIgnoreCase("group") && !l.getType().equalsIgnoreCase("not_identified") && !l.getDepRel().equalsIgnoreCase("root")) {
                System.out.println("token: " + l.getForm());
                List<TokenWrapper> antecedent_list = new ArrayList<>();
                for (int i = 0; i < list_EN.indexOf(l); i++) {
                    TokenWrapper tokenWrapper = new TokenWrapper();
                    tokenWrapper.setToken(list_EN.get(i));
                    // si il ya une différence entre la morphologie de l'expression de coréférence et un antécédent on ne considére pas l'antécédents comme candidat
                    if (l.getMorpho().equalsIgnoreCase(list_EN.get(i).getMorpho()) || (list_EN.get(i).getMorpho().isEmpty() && !list_EN.get(i).getPos().equalsIgnoreCase("CLS"))) {
                        antecedent_list.add(tokenWrapper);
                    }
                }
                // construction des candidats
                antecedent.put(l, antecedent_list);
            }
        }
        setRank(line, antecedent);
    }

    //  Calcul du score des candidats
    private static void setRank(String line, LinkedHashMap<Token, List<TokenWrapper>> antecedent) throws IOException {
        for (Map.Entry<Token, List<TokenWrapper>> entry : antecedent.entrySet()) {
            Object key = entry.getKey();
            Token token = (Token) key;
            List<TokenWrapper> values = (List<TokenWrapper>) entry.getValue();
        }
        setRankingbytype(line, antecedent);
        setRankingbyDepRel(line, antecedent);
        setRankingByTypeAndForm(line, antecedent);
        setRankingbyDistance(line, antecedent);
        setRanking(line, antecedent);
    }

    // Attribution de score par type
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
                } else {
                    rank_type = 0;
                    s.setRanking_type(rank_type);
                }
            }
        }
    }

    // Attribution de score par depRel
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
                }
                if (s.getToken().getDepRel().equalsIgnoreCase("suj")) {
                    rank_DepRel = 80;
                    s.setRanking_dep_rel(rank_DepRel);
                }
                if (s.getToken().getDepRel().equalsIgnoreCase("obj") || (s.getToken().getDepRel().equalsIgnoreCase("mod"))) {
                    rank_DepRel = 50;
                    s.setRanking_dep_rel(rank_DepRel);
                }
            }
        }

    }

    // Attribution de score par type et forme
    private static void setRankingByTypeAndForm(String line, LinkedHashMap<Token, List<TokenWrapper>> antecedent) throws IOException {
        int rank_type_form = 0;
        for (Map.Entry<Token, List<TokenWrapper>> entry : antecedent.entrySet()) {
            Object key = entry.getKey();
            Token token = (Token) key;
            List<TokenWrapper> values = (List<TokenWrapper>) entry.getValue();
            for (TokenWrapper s : values) {
                if (token.getForm().contains("division")) {
                    if (s.getToken().getType().equalsIgnoreCase("division")) {
                        rank_type_form = rank_type_form + 100;
                        s.setRanking_type_form(rank_type_form);
                    } else {
                        rank_type_form = 0;
                        s.setRanking_type_form(rank_type_form);
                    }
                }
                if (token.getForm().contains("jus") || token.getForm().contains("fragrance")) {
                    if (s.getToken().getType().equalsIgnoreCase("product")) {
                        rank_type_form = rank_type_form + 100;
                        s.setRanking_type_form(rank_type_form);
                    } else {
                        rank_type_form = 0;
                        s.setRanking_type_form(rank_type_form);
                    }
                }
                if (token.getForm().contains("marque")) {
                    if (s.getToken().getType().equalsIgnoreCase("brand")) {
                        rank_type_form = rank_type_form + 100;
                        s.setRanking_type_form(rank_type_form);
                    } else {
                        rank_type_form = 0;
                        s.setRanking_type_form(rank_type_form);
                    }

                    if (token.getForm().contains("gamme") || token.getForm().contains("ligne")) {
                        if (s.getToken().getType().equalsIgnoreCase("range")) {
                            rank_type_form = rank_type_form + 100;
                            s.setRanking_type_form(rank_type_form);
                        } else {
                            rank_type_form = 0;
                            s.setRanking_type_form(rank_type_form);
                        }
                    }

                    if (token.getForm().contains("Groupe")) {
                        if (s.getToken().getType().equalsIgnoreCase("group")) {
                            rank_type_form = rank_type_form + 100;
                            s.setRanking_type_form(rank_type_form);
                        } else {
                            rank_type_form = 0;
                            s.setRanking_type_form(rank_type_form);
                        }
                    }

                }
            }
        }

    }

    // Attribution de score par distance
    private static void setRankingbyDistance(String line, LinkedHashMap<Token, List<TokenWrapper>> antecedent) throws IOException {
        for (Map.Entry<Token, List<TokenWrapper>> entry : antecedent.entrySet()) {
            Object key = entry.getKey();
            Token token = (Token) key;
            List<TokenWrapper> values = (List<TokenWrapper>) entry.getValue();
            int rank_dist = 0;
            for (TokenWrapper s : values) {
                rank_dist = rank_dist + 10;
                s.setRanking_dist(rank_dist);
            }
        }
    }

    // Attribution de score Total
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

    // choix du meilleur candidat
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
                replacebyID.put(token, tokenWrapper.getToken().getForm());

            }
        }
    }

    //remplacement dans le texte
    private static String replaceAllFilesById(LinkedHashMap<Token, String> replacebyID, String fileContentCorpus) {
        StringBuilder builder = new StringBuilder();
        builder = new StringBuilder();
        builder.append(fileContentCorpus);
        fileContentCorpus = builder.toString();
        String toWrite = builder.toString();
        String toWriteold = toWrite;//builder.toString();
        System.out.println("toWritebuilde:" + toWrite);
        StringBuilder result = new StringBuilder();
        // System.out.println("toWrite: "+ toWrite);
        int index_prev_end = 0;
        for (Map.Entry<Token, String> entry : replacebyID.entrySet()) {
            int index_start = entry.getKey().getStart();
            int index_end = entry.getKey().getEnd();
            System.out.println("index: " + index_start + ": " + index_end);
            result.append(toWriteold.substring(index_prev_end, entry.getKey().getStart())).append(entry.getValue());
            index_prev_end = index_end;
        }
        result.append(toWriteold.substring(index_prev_end));
        System.out.println("toWritebyID: + " + result.toString());
        return result.toString();
    }


}

