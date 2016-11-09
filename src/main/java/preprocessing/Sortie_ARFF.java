package preprocessing;

import com.hp.hpl.jena.rdf.model.Model;
import fr.ho2s.holmes.ner.ws.CompactNamedEntity;
import fr.ho2s.holmes.ner.ws.HolmesNERServiceFrench;
import fr.ho2s.holmes.ner.ws.HolmesNERServiceFrenchService;
import fr.inria.smilk.ws.relationextraction.AbstractRelationExtraction;
import fr.inria.smilk.ws.relationextraction.bean.SentenceRelation;
import fr.inria.smilk.ws.relationextraction.bean.SentenceRelationId;
import fr.inria.smilk.ws.relationextraction.bean.SentenceRelationMethod;
import fr.inria.smilk.ws.relationextraction.bean.Token;
import fr.inria.smilk.ws.relationextraction.util.ListFilesUtil;
import it.uniroma1.lcl.babelnet.InvalidBabelSynsetIDException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import preprocessing.coreference.Renco;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

import static fr.inria.smilk.ws.relationextraction.ExtractionHelper.constructModel;
import static fr.inria.smilk.ws.relationextraction.ExtractionHelper.elementToToken;
import static fr.inria.smilk.ws.relationextraction.ExtractionHelper.writeRdf;

/**
 * Created by dhouib on 08/11/2016.
 */
public class Sortie_ARFF extends AbstractRelationExtraction {

    String folder = "C:/Users/dhouib/Desktop/SMILK_project_devpt/RelationExtractionSMILK/src/resources/input/test";
    File folder1 = new File("C:/Users/dhouib/Desktop/SMILK_project_devpt/RelationExtractionSMILK/src/resources/input/test");
    static String toWrite=" ";

    //vider le contenu au début

   /* static String line="Crée par Pierre Negrin (Firmenich) Amor Amor In a flash est un oriental gourmand caractérisé par un coeur de vanille, caramel et pomme d'amour sucrée.\n" +
                        "Jessica Chastain incerna Manifesto, le nouveau parfum féminin de la griffe, qui sort à la rentrée, construit autour du jasmin et de la vanille.";

    public static void main(String[] args) throws Exception {
        fr.inria.smilk.ws.relationextraction.Renco renco = new fr.inria.smilk.ws.relationextraction.Renco();

        startTools(line, renco.rencoByWebService(line));
    }*/


    private static void startTools(String line, String input) throws ParserConfigurationException, IOException, SAXException, InvalidBabelSynsetIDException, ClassNotFoundException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        StringBuilder xmlStringBuilder = new StringBuilder();
        xmlStringBuilder.append(input);
        System.out.println(input);
        ByteArrayInputStream in = new ByteArrayInputStream(xmlStringBuilder.toString().getBytes("UTF-8"));
        Document doc = builder.parse(in);
        doc.getDocumentElement().normalize();
        NodeList nSentenceList = doc.getElementsByTagName("sentence");
        readCorpus(line, nSentenceList);

    }

    private static void readCorpus(String line, NodeList nSentenceList) throws IOException, ClassNotFoundException {
        String newline = line;
        for (int sent_temp = 0; sent_temp < nSentenceList.getLength(); sent_temp++) {
            Node nSentNode = nSentenceList.item(sent_temp);
            //StringBuilder builder = new StringBuilder();
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
                                if (xElement.getAttribute("type").equalsIgnoreCase("product") || xElement.getAttribute("type").equalsIgnoreCase("division")
                                        || xElement.getAttribute("type").equalsIgnoreCase("group") || xElement.getAttribute("type").equalsIgnoreCase("brand")
                                        || xElement.getAttribute("type").equalsIgnoreCase("range")) {
                                    Token subjectToken = elementToToken(xElement);
                                    String form = subjectToken.getForm();
                                    String form_with_underscore = form.replaceAll("\\s", "_");
                                    System.out.println("form: " + form + " with underscor: " + form_with_underscore);
                                    newline = newline;
                                    newline = newline.replace(form, form_with_underscore);

                                }
                            }
                            else {
                                Token subjectToken = elementToToken(xElement);
                                String form = subjectToken.getForm();

                                newline=newline.replace(form,subjectToken.getLema());
                            }
                        }
                    }
                }
            }
        }

        HolmesNERServiceFrenchService hs = new HolmesNERServiceFrenchService();
        HolmesNERServiceFrench holmes = hs.getHolmesNERServiceFrenchPort();
        List<CompactNamedEntity> holmesOutput = holmes.parse(line);
        for(CompactNamedEntity cne : holmesOutput) {
            System.out.println(cne.getEntityString() + " type: "+ cne.getEntityType());
            if(cne.getEntityType().equalsIgnoreCase("PER")){
                String form = cne.getEntityString();
                String form_with_underscore = form.replaceAll("\\s", "_");
                System.out.println("form: " + form + " with underscor: " + form_with_underscore);
                newline = newline;
                newline = newline.replace(form, form_with_underscore);
            }
            // System.out.println(cne.getEntityString() +" "+cne.getEntityType()+ " "+ cne.getSpanFrom()+ " "+ cne.getSpanTo()+" "+ cne.getScore());

        }
        System.out.println("newline: " + newline);
        createFileARFF(newline, line, nSentenceList);
        // return newline;
    }

    private static void createFileARFF(String newline, String line, NodeList nSentenceList) throws IOException {

        toWrite= "@relation train \n @attribute Text string \n @attribute class-hasComponent {0,1}\n '"+ newline +"',?\n";
        FileWriter writer = new FileWriter("C:/Users/dhouib/Desktop/SMILK_project_devpt/RelationExtractionSMILK/src/resources/arff/" + "train.arff", true);
        writer.write(toWrite);
        writer.close();

        File file_data = new File("src/resources/arff/test.arff");
        FileWriter out_data = null;
        //vider le contenu au début
        out_data = new FileWriter(file_data);
        out_data.append(' ');
        out_data.append(toWrite);
        out_data.flush();
        out_data.close();

    }

    @Override
    public void annotationData(List<SentenceRelation> list_result) throws IOException {

       /* Map<SentenceRelationId, List<SentenceRelationMethod>> relationMap = new HashMap<>();
        //construction de list_result contenant les méthodes appliquées pour la même sentence
        for (SentenceRelation sentence_relation : list_result) {
            System.out.println("sentence_relation:" + sentence_relation + sentence_relation.getSentenceRelationId());
            if (!relationMap.containsKey(sentence_relation.getSentenceRelationId())) {
                ArrayList<SentenceRelationMethod> methodlist = new ArrayList<>();
                methodlist.add(sentence_relation.getMethod());
                relationMap.put(sentence_relation.getSentenceRelationId(), methodlist);
            } else {
                relationMap.get(sentence_relation.getSentenceRelationId()).add(sentence_relation.getMethod());
            }
        }
        // on parcours le map, et on applique la méthode d'extraction selon cette ordre: dbpedia_patterns,
        // dbpedia_namedEntity, rulesbelongsToBrand
        for (SentenceRelationId sentenceRelationId : relationMap.keySet()) {
            List<SentenceRelationMethod> relationMethods = relationMap.get(sentenceRelationId);
            if (relationMethods.contains(SentenceRelationMethod.dbpedia_chimical_component)) {
                System.out.println("Selected" + sentenceRelationId + "" + SentenceRelationMethod.dbpedia_chimical_component);
                Model model = constructModel(sentenceRelationId);
                writeRdf(model);
            } else if (relationMethods.contains(SentenceRelationMethod.dbpedia_component)) {
                System.out.println("Selected" + sentenceRelationId + "" + SentenceRelationMethod.dbpedia_component);
                Model model = constructModel(sentenceRelationId);
                writeRdf(model);
            } else if (relationMethods.contains(SentenceRelationMethod.dbpedia_parfum_component)) {
                System.out.println("Selected" + sentenceRelationId + "" + SentenceRelationMethod.dbpedia_parfum_component);
                Model model = constructModel(sentenceRelationId);
                writeRdf(model);

            }
        }*/
    }

    @Override
    public void processExtraction(String line) throws Exception {
        fr.inria.smilk.ws.relationextraction.Renco renco = new fr.inria.smilk.ws.relationextraction.Renco();
        startTools(line, renco.rencoByWebService(line));
    }

    @Override
    public void init() throws Exception {

    }

}
