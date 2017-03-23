import com.hp.hpl.jena.rdf.model.Model;
import com.joliciel.talismane.TalismaneConfig;
import com.joliciel.talismane.TalismaneSession;
import com.joliciel.talismane.parser.ParseConfiguration;
import com.joliciel.talismane.parser.Parser;
import com.joliciel.talismane.posTagger.PosTagSequence;
import com.joliciel.talismane.posTagger.PosTaggedToken;
import com.joliciel.talismane.posTagger.PosTagger;
import com.joliciel.talismane.tokeniser.TokenSequence;
import com.joliciel.talismane.tokeniser.Tokeniser;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import fr.ho2s.holmes.ner.ws.CompactNamedEntity;
import fr.ho2s.holmes.ner.ws.HolmesNERServiceFrench;
import fr.ho2s.holmes.ner.ws.HolmesNERServiceFrenchService;
import fr.inria.smilk.ws.relationextraction.AbstractRelationExtraction;
import fr.inria.smilk.ws.relationextraction.Renco;
import fr.inria.smilk.ws.relationextraction.bean.*;
import it.uniroma1.lcl.babelnet.InvalidBabelSynsetIDException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static fr.inria.smilk.ws.relationextraction.ExtractionHelper.*;

/**
 * Created by dhouib on 14/10/2016.
 */
public class TalismaneAPITestV5_with_holmes_V3_hasComponent extends AbstractRelationExtraction {


    private void startTools(String line, String input) throws ParserConfigurationException, IOException, SAXException, InvalidBabelSynsetIDException, ClassNotFoundException {
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

    private void readCorpus(String line, NodeList nSentenceList) throws IOException, ClassNotFoundException {
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
                            if (xElement.hasAttribute("type") && !xElement.getAttribute("type").equalsIgnoreCase("not_identified"))
                                if (xElement.getAttribute("type").equalsIgnoreCase("product") || xElement.getAttribute("type").equalsIgnoreCase("division")
                                        || xElement.getAttribute("type").equalsIgnoreCase("group") || xElement.getAttribute("type").equalsIgnoreCase("brand")
                                        || xElement.getAttribute("type").equalsIgnoreCase("range")) {
                                    Token subjectToken = elementToToken(xElement);
                                    String form = subjectToken.getForm();
                                    String form_with_underscore = form.replaceAll("\\s", "_");
                                    System.out.println("form: " + form + " with underscor: " + form_with_underscore);
                                    //newline = newline;
                                    newline = newline.replace(form, form_with_underscore);

                                }
                        }
                    }
                }
            }
        }
        HolmesNERServiceFrenchService hs = new HolmesNERServiceFrenchService();
        HolmesNERServiceFrench holmes = hs.getHolmesNERServiceFrenchPort();
        List<CompactNamedEntity> holmesOutput = holmes.parse(line);
        for (CompactNamedEntity cne : holmesOutput) {
            System.out.println(cne.getEntityString() + " type: " + cne.getEntityType());
            if (cne.getEntityType().equalsIgnoreCase("PER")) {
                String form = cne.getEntityString();
                if (form.contains("(")) {
                    form = form.substring(0, form.indexOf("(")).trim();
                }
                String form_with_underscore = form.replaceAll("\\s", "_");
                System.out.println("form: " + form + " with underscor: " + form_with_underscore);
                //newline = newline;
                newline = newline.replace(form, form_with_underscore);
            }
            // System.out.println(cne.getEntityString() +" "+cne.getEntityType()+ " "+ cne.getSpanFrom()+ " "+ cne.getSpanTo()+" "+ cne.getScore());

        }
        System.out.println("newline: " + newline);
        annotatedByTalismane(newline, line, nSentenceList);
        // return newline;
    }

    private String setType(String line, NodeList nSentenceList, String token) {
        String type = new String();
        HolmesNERServiceFrenchService hs = new HolmesNERServiceFrenchService();
        HolmesNERServiceFrench holmes = hs.getHolmesNERServiceFrenchPort();
        List<CompactNamedEntity> holmesOutput = holmes.parse(line);

        for (CompactNamedEntity cne : holmesOutput) {

            // System.out.println(cne.getEntityString() +" "+cne.getEntityType()+ " "+ cne.getSpanFrom()+ " "+ cne.getSpanTo()+" "+ cne.getScore());
            String token2 = token.replace("_", " ");
            if ((cne.getEntityString().contains(token2))) {
                type = cne.getEntityType();
            }
        }
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
                            if (xElement.hasAttribute("type") && !xElement.getAttribute("type").equalsIgnoreCase("not_identified")
                             /* && (xElement.getAttribute("type").equalsIgnoreCase("product")  )*/) {
                                Token subjectToken = elementToToken(xElement);
                                String token2 = token.replace("_", " ");
                                if ((subjectToken.getForm().equalsIgnoreCase(token2))) {
                                    type = subjectToken.getType();

                                }

                            }
                        }
                    }
                }
            }

        }

        return type;
    }

    public void annotatedByTalismane(String new_line, String line, NodeList nSentenceList) throws IOException, ClassNotFoundException {
        String sessionId = "";
        // load the Talismane configuration
        TalismaneSession talismaneSession;
        talismaneSession = TalismaneSession.getInstance(sessionId);
      /*  Config conf = ConfigFactory.load();
        TalismaneConfig talismaneConfig = new TalismaneConfig(conf, talismaneSession);*/
        Config parsedConfig = ConfigFactory.parseFile(new File("src/main/resources/talismane-fr-3.0.0b.conf"));
        Config conf = ConfigFactory.load(parsedConfig);
        TalismaneConfig talismaneConfig = new TalismaneConfig(conf, talismaneSession);

        // tokenise the text
        Tokeniser tokeniser = talismaneConfig.getTokeniser();
        TokenSequence tokenSequence_line = tokeniser.tokeniseText(line);
        TokenSequence tokenSequence = tokeniser.tokeniseText(new_line);

        // pos-tag the token sequence
        PosTagger posTagger = talismaneConfig.getPosTagger();
        PosTagSequence posTagSequence = posTagger.tagSentence(tokenSequence);
        PosTagSequence posTagSequence_line = posTagger.tagSentence(tokenSequence_line);
        Parser parser = talismaneConfig.getParser();
        ParseConfiguration parseConfiguration = parser.parseSentence(posTagSequence);

        for (int i = 0; i < posTagSequence.size(); i++) {
            posTagSequence.get(i).setComment(setType(line, nSentenceList, posTagSequence.get(i).getToken().toString()));
            System.out.println(posTagSequence.get(i).getToken() + " " + posTagSequence.get(i).getComment());
        }

        System.out.println("test: " + parseConfiguration.getDependencies());

        // extractSubject(parseConfiguration, posTagSequence, line);
        relationRules(parseConfiguration, posTagSequence, line);
    }

    public Token extractSubject2(ParseConfiguration parseConfiguration, PosTagSequence posTagSequence, String line) {
        Token subject2 = new Token();

        for (int i = 0; i < posTagSequence.size(); i++) {
            if (posTagSequence.get(i).getLexicalEntry() != null) {

                //(V,sub)+(NPP,suj)
                if (posTagSequence.get(i).getTag().toString().equalsIgnoreCase("V") && parseConfiguration.getGoverningDependency(posTagSequence.get(i)).getLabel().equalsIgnoreCase("sub")) {
                    List<PosTaggedToken> pos_tagged_token_test_1 = parseConfiguration.getDependents(posTagSequence.get(i));
                    for (PosTaggedToken ptt_test1 : pos_tagged_token_test_1) {
                        if (ptt_test1.getTag().toString().equalsIgnoreCase("NPP") && parseConfiguration.getGoverningDependency(ptt_test1).getLabel().equalsIgnoreCase("suj")
                                && (ptt_test1.getComment().equalsIgnoreCase("product"))) {
                            subject2.setForm(ptt_test1.getToken().toString());
                            subject2.setType(ptt_test1.getComment());
                        }
                    }
                }

                //(VINF,prep)+(NC,obj)+((P+D),dep)+(NC,prep)+(P,dep)+(NC,prep)+(NPP,mod)
                if (posTagSequence.get(i).getTag().toString().equalsIgnoreCase("VINF") && parseConfiguration.getGoverningDependency(posTagSequence.get(i)).getLabel().equalsIgnoreCase("prep")) {
                    List<PosTaggedToken> pos_tagged_token_test_1 = parseConfiguration.getDependents(posTagSequence.get(i));
                    for (PosTaggedToken ptt_test1 : pos_tagged_token_test_1) {
                        if (ptt_test1.getTag().toString().equalsIgnoreCase("NC") && parseConfiguration.getGoverningDependency(ptt_test1).getLabel().equalsIgnoreCase("obj")) {
                            List<PosTaggedToken> pos_tagged_token_test_2 = parseConfiguration.getDependents(ptt_test1);
                            for (PosTaggedToken ptt_test2 : pos_tagged_token_test_2) {
                                if (ptt_test2.getTag().toString().equalsIgnoreCase("P+D") && parseConfiguration.getGoverningDependency(ptt_test2).getLabel().equalsIgnoreCase("dep")) {
                                    List<PosTaggedToken> pos_tagged_token_test_3 = parseConfiguration.getDependents(ptt_test2);
                                    for (PosTaggedToken ptt_test3 : pos_tagged_token_test_3) {
                                        if (ptt_test3.getTag().toString().equalsIgnoreCase("NC") && parseConfiguration.getGoverningDependency(ptt_test3).getLabel().equalsIgnoreCase("prep")
                                                ) {
                                            List<PosTaggedToken> pos_tagged_token_test_4 = parseConfiguration.getDependents(ptt_test3);
                                            for (PosTaggedToken ptt_test4 : pos_tagged_token_test_4) {
                                                if (ptt_test4.getTag().toString().equalsIgnoreCase("P") && parseConfiguration.getGoverningDependency(ptt_test4).getLabel().equalsIgnoreCase("dep")
                                                        ) {
                                                    List<PosTaggedToken> pos_tagged_token_test_5 = parseConfiguration.getDependents(ptt_test4);
                                                    for (PosTaggedToken ptt_test5 : pos_tagged_token_test_5) {
                                                        if (ptt_test5.getTag().toString().equalsIgnoreCase("NC") && parseConfiguration.getGoverningDependency(ptt_test5).getLabel().equalsIgnoreCase("prep")) {
                                                            List<PosTaggedToken> pos_tagged_token_test_6 = parseConfiguration.getDependents(ptt_test5);
                                                            for (PosTaggedToken ptt_test6 : pos_tagged_token_test_6) {
                                                                if (ptt_test6.getTag().toString().equalsIgnoreCase("NPP") && parseConfiguration.getGoverningDependency(ptt_test6).getLabel().equalsIgnoreCase("mod")
                                                                        && (ptt_test6.getComment().equalsIgnoreCase("product"))) {
                                                                    subject2.setForm(ptt_test6.getToken().toString());
                                                                    subject2.setType(ptt_test6.getComment());

                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                }
                            }

                        }
                    }

                }

                //(V+dep_coord)+(P,mod)+(NC,prep)+((P+D),dep)+(NC,prep)+(NPP,mod)
                if (posTagSequence.get(i).getTag().toString().equalsIgnoreCase("V") && parseConfiguration.getGoverningDependency(posTagSequence.get(i)).getLabel().equalsIgnoreCase("dep_coord")) {

                    List<PosTaggedToken> pos_tagged_token_test_2 = parseConfiguration.getDependents(posTagSequence.get(i));
                    for (PosTaggedToken ptt_test2 : pos_tagged_token_test_2) {
                        if (ptt_test2.getTag().toString().equalsIgnoreCase("P") && parseConfiguration.getGoverningDependency(ptt_test2).getLabel().equalsIgnoreCase("mod")
                                ) {
                            List<PosTaggedToken> pos_tagged_token_test_3 = parseConfiguration.getDependents(ptt_test2);
                            for (PosTaggedToken ptt_test3 : pos_tagged_token_test_3) {
                                if (ptt_test3.getTag().toString().equalsIgnoreCase("NC") && parseConfiguration.getGoverningDependency(ptt_test3).getLabel().equalsIgnoreCase("prep")
                                        ) {
                                    List<PosTaggedToken> pos_tagged_token_test_4 = parseConfiguration.getDependents(ptt_test3);
                                    for (PosTaggedToken ptt_test4 : pos_tagged_token_test_4) {
                                        if (ptt_test4.getTag().toString().equalsIgnoreCase("P+D") && parseConfiguration.getGoverningDependency(ptt_test4).getLabel().equalsIgnoreCase("dep")
                                                ) {
                                            List<PosTaggedToken> pos_tagged_token_test_5 = parseConfiguration.getDependents(ptt_test4);
                                            for (PosTaggedToken ptt_test5 : pos_tagged_token_test_5) {
                                                if (ptt_test5.getTag().toString().equalsIgnoreCase("NC") && parseConfiguration.getGoverningDependency(ptt_test5).getLabel().equalsIgnoreCase("prep")
                                                        ) {
                                                    List<PosTaggedToken> pos_tagged_token_test_6 = parseConfiguration.getDependents(ptt_test5);
                                                    for (PosTaggedToken ptt_test6 : pos_tagged_token_test_6) {
                                                        if (ptt_test6.getTag().toString().equalsIgnoreCase("NPP") && parseConfiguration.getGoverningDependency(ptt_test6).getLabel().equalsIgnoreCase("mod")
                                                                && (ptt_test6.getComment().equalsIgnoreCase("product"))) {
                                                            subject2.setForm(ptt_test6.getToken().toString());
                                                            subject2.setType(ptt_test6.getComment());
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                            }
                        }

                    }
                }
                //(V,root)+(NC,mod)+(P,dep)+(NC,prep)+(ADV,mod)
                if (posTagSequence.get(i).getTag().toString().equalsIgnoreCase("V") && parseConfiguration.getGoverningDependency(posTagSequence.get(i)).getLabel().equalsIgnoreCase("root")) {
                    //Liste des dependences de i
                    List<PosTaggedToken> pos_tagged_token = parseConfiguration.getDependents(posTagSequence.get(i));
                    for (PosTaggedToken ptt : pos_tagged_token) {

                        if (ptt.getTag().toString().equalsIgnoreCase("NC") && (parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("mod"))) {
                            List<PosTaggedToken> pos_tagged_token1 = parseConfiguration.getDependents(ptt);
                            for (PosTaggedToken ptt_1 : pos_tagged_token1) {
                                if (ptt_1.getTag().toString().equalsIgnoreCase("P") && (parseConfiguration.getGoverningDependency(ptt_1).getLabel().equalsIgnoreCase("dep"))) {
                                    List<PosTaggedToken> pos_tagged_token2 = parseConfiguration.getDependents(ptt_1);
                                    for (PosTaggedToken ptt_2 : pos_tagged_token2) {
                                        if (ptt_2.getTag().toString().equalsIgnoreCase("NC") && (parseConfiguration.getGoverningDependency(ptt_2).getLabel().equalsIgnoreCase("prep"))) {
                                            List<PosTaggedToken> pos_tagged_token3 = parseConfiguration.getDependents(ptt_2);
                                            for (PosTaggedToken ptt_3 : pos_tagged_token3) {
                                                if (ptt_3.getTag().toString().equalsIgnoreCase("ADV") && (parseConfiguration.getGoverningDependency(ptt_3).getLabel().equalsIgnoreCase("mod"))
                                                        && (ptt_3.getComment().equalsIgnoreCase("product"))) {
                                                    subject2.setForm(ptt_3.getToken().toString());
                                                    subject2.setType(ptt_3.getComment());
                                                }
                                            }
                                        }
                                    }
                                }

                            }
                        }
                    }
                }

                //(V,root)+(NPP,(suj||obj)
                if (posTagSequence.get(i).getTag().toString().equalsIgnoreCase("V") && parseConfiguration.getGoverningDependency(posTagSequence.get(i)).getLabel().equalsIgnoreCase("root")) {
                    //Liste des dependences de i
                    List<PosTaggedToken> pos_tagged_token = parseConfiguration.getDependents(posTagSequence.get(i));
                    for (PosTaggedToken ptt : pos_tagged_token) {

                        if (ptt.getTag().toString().equalsIgnoreCase("NPP")
                                && (parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("suj")
                                || parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("obj"))
                                && (ptt.getComment().equalsIgnoreCase("product"))) {

                            subject2.setForm(ptt.getToken().toString());
                            subject2.setType(ptt.getComment());
                        }
                    }
                }
                //(V,root)+ (NC,suj)+(NPP,mod)
                if (posTagSequence.get(i).getTag().toString().equalsIgnoreCase("V") && parseConfiguration.getGoverningDependency(posTagSequence.get(i)).getLabel().equalsIgnoreCase("root")) {
                    //Liste des dependences de i
                    List<PosTaggedToken> pos_tagged_token = parseConfiguration.getDependents(posTagSequence.get(i));
                    for (PosTaggedToken ptt : pos_tagged_token) {
                        if ((ptt.getTag().toString().equalsIgnoreCase("NC")) && parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("suj")) {
                            List<PosTaggedToken> pos_tagged_token_test_2 = parseConfiguration.getDependents(ptt);
                            for (PosTaggedToken ptt_test2 : pos_tagged_token_test_2) {
                                if (ptt_test2.getTag().toString().equalsIgnoreCase("NPP") && parseConfiguration.getGoverningDependency(ptt_test2).getLabel().equalsIgnoreCase("mod")
                                        && (ptt_test2.getComment().equalsIgnoreCase("product"))) {
                                    subject2.setForm(ptt_test2.getToken().toString());
                                    subject2.setType(ptt_test2.getComment());
                                }
                            }
                        }

                    }
                }

                //(V,root)+(NC,ats)+((P+D),dep)+ (NC,prep)  +(NPP,mod)
                //(V,root)+(NC,ats)+((P+D),dep)+ (NC,prep)+(P,dep)+(NPP,prep)
                if (posTagSequence.get(i).getTag().toString().equalsIgnoreCase("V") && parseConfiguration.getGoverningDependency(posTagSequence.get(i)).getLabel().equalsIgnoreCase("root")) {
                    //Liste des dependences de i
                    List<PosTaggedToken> pos_tagged_token = parseConfiguration.getDependents(posTagSequence.get(i));
                    for (PosTaggedToken ptt : pos_tagged_token) {
                        if ((ptt.getTag().toString().equalsIgnoreCase("NC")) && parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("ats")) {
                            {
                                List<PosTaggedToken> pos_tagged_token_test_3 = parseConfiguration.getDependents(ptt);
                                for (PosTaggedToken ptt_test3 : pos_tagged_token_test_3) {
                                    if (ptt_test3.getTag().toString().equalsIgnoreCase("P+D") && parseConfiguration.getGoverningDependency(ptt_test3).getLabel().equalsIgnoreCase("dep")) {
                                        List<PosTaggedToken> pos_tagged_token_test_2 = parseConfiguration.getDependents(ptt_test3);
                                        for (PosTaggedToken ptt_test2 : pos_tagged_token_test_2) {
                                            if (ptt_test2.getTag().toString().equalsIgnoreCase("NC")
                                                    && parseConfiguration.getGoverningDependency(ptt_test2).getLabel().equalsIgnoreCase("prep")) {
                                                List<PosTaggedToken> pos_tagged_token_test_4 = parseConfiguration.getDependents(ptt_test2);
                                                for (PosTaggedToken ptt_test4 : pos_tagged_token_test_4) {

                                                    if (ptt_test4.getTag().toString().equalsIgnoreCase("NPP") && parseConfiguration.getGoverningDependency(ptt_test4).getLabel().equalsIgnoreCase("mod")
                                                            && (ptt_test4.getComment().equalsIgnoreCase("product"))) {
                                                        subject2.setForm(ptt_test4.getToken().toString());
                                                        subject2.setType(ptt_test4.getComment());
                                                    }

                                                    if (ptt_test4.getTag().toString().equalsIgnoreCase("P") && parseConfiguration.getGoverningDependency(ptt_test4).getLabel().equalsIgnoreCase("dep")
                                                            ) {
                                                        List<PosTaggedToken> pos_tagged_token_test_5 = parseConfiguration.getDependents(ptt_test4);
                                                        for (PosTaggedToken ptt_test5 : pos_tagged_token_test_5) {
                                                            if (ptt_test5.getTag().toString().equalsIgnoreCase("NPP") && parseConfiguration.getGoverningDependency(ptt_test5).getLabel().equalsIgnoreCase("prep")
                                                                    && (ptt_test5.getComment().equalsIgnoreCase("product") )) {

                                                                subject2.setForm(ptt_test5.getToken().toString());
                                                                subject2.setType(ptt_test5.getComment());
                                                            }
                                                        }
                                                    }
                                                }
                                            }

                                        }
                                    }
                                }
                            }

                        }

                    }
                }

                //(V,root) +(NC,obj)
                if (posTagSequence.get(i).getTag().toString().equalsIgnoreCase("V") && parseConfiguration.getGoverningDependency(posTagSequence.get(i)).getLabel().equalsIgnoreCase("root")) {
                    //Liste des dependences de i
                    List<PosTaggedToken> pos_tagged_token = parseConfiguration.getDependents(posTagSequence.get(i));
                    for (PosTaggedToken ptt : pos_tagged_token) {
                        if (ptt.getTag().toString().equalsIgnoreCase("NC") && parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("obj")
                                && (ptt.getComment().equalsIgnoreCase("product") )) {
                            subject2.setForm(ptt.getToken().toString());
                            subject2.setType(ptt.getComment());
                        }
                    }
                }
                //(V,root) +(NC,obj) + (P,dep) +(NC,prep)+ (P,dep) +(NC,prep)
                //(V,root) +(NC,obj) +(P,dep) +(NC,prep) + (P,dep) +(CC,coord) + (P,dep_coord) + (NC,prep)
                if (posTagSequence.get(i).getTag().toString().equalsIgnoreCase("V") && parseConfiguration.getGoverningDependency(posTagSequence.get(i)).getLabel().equalsIgnoreCase("root")) {
                    //Liste des dependences de i
                    List<PosTaggedToken> pos_tagged_token = parseConfiguration.getDependents(posTagSequence.get(i));
                    for (PosTaggedToken ptt : pos_tagged_token) {
                        if (ptt.getTag().toString().equalsIgnoreCase("NC") && parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("obj")
                                && (!ptt.getComment().equalsIgnoreCase("product"))) {
                            List<PosTaggedToken> pos_tagged_token_test_2 = parseConfiguration.getDependents(ptt);
                            for (PosTaggedToken ptt_test2 : pos_tagged_token_test_2) {
                                if (ptt_test2.getTag().toString().equalsIgnoreCase("P") && parseConfiguration.getGoverningDependency(ptt_test2).getLabel().equalsIgnoreCase("dep")) {
                                    List<PosTaggedToken> pos_tagged_token_test_3 = parseConfiguration.getDependents(ptt_test2);
                                    for (PosTaggedToken ptt_test3 : pos_tagged_token_test_3) {
                                        if (ptt_test3.getTag().toString().equalsIgnoreCase("NC") && parseConfiguration.getGoverningDependency(ptt_test3).getLabel().equalsIgnoreCase("prep")) {
                                            List<PosTaggedToken> pos_tagged_token_test_4 = parseConfiguration.getDependents(ptt_test3);
                                            for (PosTaggedToken ptt_test4 : pos_tagged_token_test_4) {
                                                if (ptt_test4.getTag().toString().equalsIgnoreCase("P") && parseConfiguration.getGoverningDependency(ptt_test4).getLabel().equalsIgnoreCase("dep")) {
                                                    List<PosTaggedToken> pos_tagged_token_test_5 = parseConfiguration.getDependents(ptt_test4);
                                                    for (PosTaggedToken ptt_test5 : pos_tagged_token_test_5) {
                                                        if (ptt_test5.getTag().toString().equalsIgnoreCase("NC") && parseConfiguration.getGoverningDependency(ptt_test5).getLabel().equalsIgnoreCase("prep")) {
                                                            List<PosTaggedToken> pos_tagged_token_test_6 = parseConfiguration.getDependents(ptt_test5);
                                                            for (PosTaggedToken ptt_test6 : pos_tagged_token_test_6) {
                                                                if (ptt_test6.getTag().toString().equalsIgnoreCase("NPP") && parseConfiguration.getGoverningDependency(ptt_test6).getLabel().equalsIgnoreCase("mod")
                                                                        && (ptt_test6.getComment().equalsIgnoreCase("product"))) {
                                                                    subject2.setForm(ptt_test6.getToken().toString());
                                                                    subject2.setType(ptt_test6.getComment());
                                                                }
                                                            }
                                                        }

                                                        if (ptt_test5.getTag().toString().equalsIgnoreCase("CC") && parseConfiguration.getGoverningDependency(ptt_test5).getLabel().equalsIgnoreCase("coord")) {
                                                            List<PosTaggedToken> pos_tagged_token_test_6 = parseConfiguration.getDependents(ptt_test5);
                                                            for (PosTaggedToken ptt_test6 : pos_tagged_token_test_6) {
                                                                if (ptt_test6.getTag().toString().equalsIgnoreCase("P") && parseConfiguration.getGoverningDependency(ptt_test6).getLabel().equalsIgnoreCase("dep_coord")
                                                                        ) {
                                                                    List<PosTaggedToken> pos_tagged_token_test_7 = parseConfiguration.getDependents(ptt_test6);
                                                                    for (PosTaggedToken ptt_test7 : pos_tagged_token_test_7) {
                                                                        if (ptt_test7.getTag().toString().equalsIgnoreCase("NC") && parseConfiguration.getGoverningDependency(ptt_test7).getLabel().equalsIgnoreCase("prep")
                                                                                ) {
                                                                            List<PosTaggedToken> pos_tagged_token_test_8 = parseConfiguration.getDependents(ptt_test7);
                                                                            for (PosTaggedToken ptt_test8 : pos_tagged_token_test_8) {
                                                                                if (ptt_test8.getTag().toString().equalsIgnoreCase("NPP") && parseConfiguration.getGoverningDependency(ptt_test8).getLabel().equalsIgnoreCase("mod")
                                                                                        && (ptt_test8.getComment().equalsIgnoreCase("product"))) {
                                                                                    subject2.setForm(ptt_test8.getToken().toString());
                                                                                    subject2.setType(ptt_test8.getComment());
                                                                                }
                                                                            }

                                                                        }
                                                                    }

                                                                }
                                                            }
                                                        }

                                                    }
                                                }
                                            }
                                        }

                                    }


                                }
                            }
                        }

                    }
                }

                //(V,root) + (P,mod) + ((NPP||NC) prep)
                //(V,root) + (P,mod) +(NC,(prep, obj))+ (NPP,mod)
                if (posTagSequence.get(i).getTag().toString().equalsIgnoreCase("V") && parseConfiguration.getGoverningDependency(posTagSequence.get(i)).getLabel().equalsIgnoreCase("root")) {
                    //Liste des dependences de i
                    List<PosTaggedToken> pos_tagged_token = parseConfiguration.getDependents(posTagSequence.get(i));
                    for (PosTaggedToken ptt : pos_tagged_token) {

                        if (ptt.getTag().toString().equalsIgnoreCase("P") && parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("mod")) {
                            List<PosTaggedToken> pos_tagged_token_test_2 = parseConfiguration.getDependents(ptt);
                            for (PosTaggedToken ptt_test2 : pos_tagged_token_test_2) {
                                if ((ptt_test2.getTag().toString().equalsIgnoreCase("NPP") || ptt_test2.getTag().toString().equalsIgnoreCase("NC"))
                                        && parseConfiguration.getGoverningDependency(ptt_test2).getLabel().equalsIgnoreCase("prep")
                                        && ptt_test2.getComment().equalsIgnoreCase("product") || ptt_test2.getComment().equalsIgnoreCase("brand")) {
                                    subject2.setForm(ptt_test2.getToken().toString());
                                }
                                if ((ptt_test2.getTag().toString().equalsIgnoreCase("NC")) && (parseConfiguration.getGoverningDependency(ptt_test2).getLabel().equalsIgnoreCase("prep")
                                        || parseConfiguration.getGoverningDependency(ptt_test2).getLabel().equalsIgnoreCase("obj"))) {
                                    List<PosTaggedToken> pos_tagged_token_test_3 = parseConfiguration.getDependents(ptt_test2);
                                    for (PosTaggedToken ptt_test3 : pos_tagged_token_test_3) {
                                        //TODO (à verifier)
                                        if ((ptt_test3.getTag().toString().equalsIgnoreCase("NPP") || ptt_test3.getTag().toString().equalsIgnoreCase("mod"))
                                                && parseConfiguration.getGoverningDependency(ptt_test3).getLabel().equalsIgnoreCase("mod")
                                                && (ptt_test3.getComment().equalsIgnoreCase("product") )) {
                                            subject2.setForm(ptt_test3.getToken().toString());
                                            subject2.setType(ptt_test3.getComment());
                                        }
                                    }
                                }
                            }

                        }

                        if (ptt.getTag().toString().equalsIgnoreCase("P") && parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("mod")) {
                            List<PosTaggedToken> pos_tagged_token_test_2 = parseConfiguration.getDependents(ptt);
                            for (PosTaggedToken ptt_test2 : pos_tagged_token_test_2) {
                                if ((ptt_test2.getTag().toString().equalsIgnoreCase("NC")) && (parseConfiguration.getGoverningDependency(ptt_test2).getLabel().equalsIgnoreCase("prep"))) {
                                    List<PosTaggedToken> pos_tagged_token_test_3 = parseConfiguration.getDependents(ptt_test2);
                                    for (PosTaggedToken ptt_test3 : pos_tagged_token_test_3) {
                                        if ((ptt_test3.getTag().toString().equalsIgnoreCase("P+D"))
                                                && parseConfiguration.getGoverningDependency(ptt_test3).getLabel().equalsIgnoreCase("dep")) {
                                            List<PosTaggedToken> pos_tagged_token_test_4 = parseConfiguration.getDependents(ptt_test3);
                                            for (PosTaggedToken ptt_test4 : pos_tagged_token_test_4) {
                                                if ((ptt_test4.getTag().toString().equalsIgnoreCase("NC"))
                                                        && parseConfiguration.getGoverningDependency(ptt_test4).getLabel().equalsIgnoreCase("prep")) {
                                                    List<PosTaggedToken> pos_tagged_token_test_5 = parseConfiguration.getDependents(ptt_test4);
                                                    for (PosTaggedToken ptt_test5 : pos_tagged_token_test_5) {
                                                        if ((ptt_test5.getTag().toString().equalsIgnoreCase("NPP"))
                                                                && parseConfiguration.getGoverningDependency(ptt_test5).getLabel().equalsIgnoreCase("mod")
                                                                && (ptt_test5.getComment().equalsIgnoreCase("product"))) {
                                                            subject2.setForm(ptt_test5.getToken().toString());
                                                            subject2.setType(ptt_test5.getComment());
                                                        }

                                                    }
                                                }

                                            }


                                        }
                                    }
                                }
                            }

                        }
                    }
                }
                //(V,root) +(V,mod) +(NPP,suj)
                if (posTagSequence.get(i).getTag().toString().equalsIgnoreCase("V") && parseConfiguration.getGoverningDependency(posTagSequence.get(i)).getLabel().equalsIgnoreCase("root")) {
                    //Liste des dependences de i
                    List<PosTaggedToken> pos_tagged_token = parseConfiguration.getDependents(posTagSequence.get(i));
                    for (PosTaggedToken ptt : pos_tagged_token) {
                        if (ptt.getTag().toString().equalsIgnoreCase("V") && parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("mod")) {
                            List<PosTaggedToken> pos_tagged_token_test_2 = parseConfiguration.getDependents(ptt);
                            for (PosTaggedToken ptt_test2 : pos_tagged_token_test_2) {
                                if (ptt_test2.getTag().toString().equalsIgnoreCase("NPP") && parseConfiguration.getGoverningDependency(ptt_test2).getLabel().equalsIgnoreCase("suj")
                                        && (ptt_test2.getComment().equalsIgnoreCase("product"))) {
                                    subject2.setForm(ptt_test2.getToken().toString());
                                    subject2.setType(ptt_test2.getComment());
                                }
                            }

                        }

                    }
                }
                //(v,root) +(NC,ats) +(P,dep) +(NC,prep)

                if (posTagSequence.get(i).getTag().toString().equalsIgnoreCase("V") && parseConfiguration.getGoverningDependency(posTagSequence.get(i)).getLabel().equalsIgnoreCase("root")) {
                    //Liste des dependences de i
                    List<PosTaggedToken> pos_tagged_token = parseConfiguration.getDependents(posTagSequence.get(i));
                    for (PosTaggedToken ptt : pos_tagged_token) {

                        if (ptt.getTag().toString().equalsIgnoreCase("NC") && parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("ats")) {
                            List<PosTaggedToken> pos_tagged_token_test_2 = parseConfiguration.getDependents(ptt);
                            for (PosTaggedToken ptt_test2 : pos_tagged_token_test_2) {
                                if (ptt_test2.getTag().toString().equalsIgnoreCase("P") && parseConfiguration.getGoverningDependency(ptt_test2).getLabel().equalsIgnoreCase("dep")) {
                                    List<PosTaggedToken> pos_tagged_token_test_3 = parseConfiguration.getDependents(ptt_test2);
                                    for (PosTaggedToken ptt_test3 : pos_tagged_token_test_3) {
                                        if (ptt_test3.getTag().toString().equalsIgnoreCase("NC") && parseConfiguration.getGoverningDependency(ptt_test3).getLabel().equalsIgnoreCase("prep")
                                                && (ptt_test3.getComment().equalsIgnoreCase("product") )) {
                                            subject2.setForm(ptt_test3.getToken().toString());
                                            subject2.setType(ptt_test3.getComment());
                                        }
                                    }
                                }
                            }

                        }
                    }
                }

                //(V,root)+(NC,obj)+(P,dep)+(NC,prep)+(NC,mod)
                if (posTagSequence.get(i).getTag().toString().equalsIgnoreCase("V") && parseConfiguration.getGoverningDependency(posTagSequence.get(i)).getLabel().equalsIgnoreCase("root")) {
                    //Liste des dependences de i
                    List<PosTaggedToken> pos_tagged_token = parseConfiguration.getDependents(posTagSequence.get(i));
                    for (PosTaggedToken ptt : pos_tagged_token) {
                        if (ptt.getTag().toString().equalsIgnoreCase("NC") && parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("obj")) {
                            List<PosTaggedToken> pos_tagged_token_test_2 = parseConfiguration.getDependents(ptt);
                            for (PosTaggedToken ptt_test2 : pos_tagged_token_test_2) {
                                if (ptt_test2.getTag().toString().equalsIgnoreCase("P") && parseConfiguration.getGoverningDependency(ptt_test2).getLabel().equalsIgnoreCase("dep")) {
                                    List<PosTaggedToken> pos_tagged_token_test_3 = parseConfiguration.getDependents(ptt_test2);
                                    for (PosTaggedToken ptt_test3 : pos_tagged_token_test_3) {
                                        if (ptt_test3.getTag().toString().equalsIgnoreCase("NC") && parseConfiguration.getGoverningDependency(ptt_test3).getLabel().equalsIgnoreCase("prep")) {
                                            List<PosTaggedToken> pos_tagged_token_test_4 = parseConfiguration.getDependents(ptt_test3);
                                            for (PosTaggedToken ptt_test4 : pos_tagged_token_test_4) {
                                                if (ptt_test4.getTag().toString().equalsIgnoreCase("NC") && parseConfiguration.getGoverningDependency(ptt_test4).getLabel().equalsIgnoreCase("mod")
                                                        && (ptt_test4.getComment().equalsIgnoreCase("product"))) {
                                                    subject2.setForm(ptt_test4.getToken().toString());
                                                    subject2.setType(ptt_test4.getComment());
                                                }
                                            }
                                        }

                                    }
                                }

                            }
                        }
                    }
                }
            }


            //(VPP,root) +(P,mod) +((NPP||NC),prep)
            if (posTagSequence.get(i).getTag().toString().equalsIgnoreCase("VPP") && parseConfiguration.getGoverningDependency(posTagSequence.get(i)).getLabel().equalsIgnoreCase("root")) {
                List<PosTaggedToken> pos_tagged_token = parseConfiguration.getDependents(posTagSequence.get(i));
                for (PosTaggedToken ptt : pos_tagged_token) {
                    if (ptt.getTag().toString().equalsIgnoreCase("P") && parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("mod")) {
                        List<PosTaggedToken> pos_tagged_token_test_2 = parseConfiguration.getDependents(ptt);
                        for (PosTaggedToken ptt_test2 : pos_tagged_token_test_2) {
                            if ((ptt_test2.getTag().toString().equalsIgnoreCase("NPP") || ptt_test2.getTag().toString().equalsIgnoreCase("NC"))
                                    && parseConfiguration.getGoverningDependency(ptt_test2).getLabel().equalsIgnoreCase("prep")
                                    && (ptt_test2.getComment().equalsIgnoreCase("product") )) {
                                subject2.setForm(ptt_test2.getToken().toString());
                                subject2.setType(ptt_test2.getComment());
                            }
                        }
                    }
                }
            }

            //(VPP,mod) +(NPP,mod)
            if (posTagSequence.get(i).getTag().toString().equalsIgnoreCase("VPP") && parseConfiguration.getGoverningDependency(posTagSequence.get(i)).getLabel().equalsIgnoreCase("mod")) {
                List<PosTaggedToken> pos_tagged_token = parseConfiguration.getDependents(posTagSequence.get(i));
                for (PosTaggedToken ptt : pos_tagged_token) {
                    if (ptt.getTag().toString().equalsIgnoreCase("NPP")
                            && parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("mod")
                            && (ptt.getComment().equalsIgnoreCase("product"))) {
                        subject2.setForm(ptt.getToken().toString());
                        subject2.setType(ptt.getComment());
                    }
                }
            }

            //(VPP,mod) +(NPP,obj) + (V,mod_rel) +(NC,obj) + (P,dep) + (NPP,prep)
            if (posTagSequence.get(i).getTag().toString().equalsIgnoreCase("VPP") && parseConfiguration.getGoverningDependency(posTagSequence.get(i)).getLabel().equalsIgnoreCase("mod")) {
                List<PosTaggedToken> pos_tagged_token = parseConfiguration.getDependents(posTagSequence.get(i));
                for (PosTaggedToken ptt : pos_tagged_token) {
                    if (ptt.getTag().toString().equalsIgnoreCase("NPP") && parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("obj")) {
                        List<PosTaggedToken> pos_tagged_token_child = parseConfiguration.getDependents(ptt);
                        for (PosTaggedToken ptt_child : pos_tagged_token_child) {
                            if (ptt_child.getTag().toString().equalsIgnoreCase("V") && parseConfiguration.getGoverningDependency(ptt_child).getLabel().equalsIgnoreCase("mod_rel")) {
                                List<PosTaggedToken> pos_tagged_token_child1 = parseConfiguration.getDependents(ptt_child);
                                for (PosTaggedToken ptt_child1 : pos_tagged_token_child1) {
                                    if (ptt_child1.getTag().toString().equalsIgnoreCase("NC") && parseConfiguration.getGoverningDependency(ptt_child1).getLabel().equalsIgnoreCase("obj")) {
                                        List<PosTaggedToken> pos_tagged_token_child2 = parseConfiguration.getDependents(ptt_child1);
                                        for (PosTaggedToken ptt_child2 : pos_tagged_token_child2) {
                                            if (ptt_child2.getTag().toString().equalsIgnoreCase("P") && parseConfiguration.getGoverningDependency(ptt_child2).getLabel().equalsIgnoreCase("dep")) {
                                                List<PosTaggedToken> pos_tagged_token_child3 = parseConfiguration.getDependents(ptt_child2);
                                                for (PosTaggedToken ptt_child3 : pos_tagged_token_child3) {
                                                    if (ptt_child3.getTag().toString().equalsIgnoreCase("NPP")
                                                            && parseConfiguration.getGoverningDependency(ptt_child3).getLabel().equalsIgnoreCase("prep")
                                                            && (ptt_child3.getComment().equalsIgnoreCase("product") )) {
                                                        subject2.setForm(ptt_child3.getToken().toString());
                                                        subject2.setType(ptt_child3.getComment());
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                            }

                        }

                    }
                }
            }

            //(VPP,mod)+(ADV,mod)

            if (posTagSequence.get(i).getTag().toString().equalsIgnoreCase("VPP") && parseConfiguration.getGoverningDependency(posTagSequence.get(i)).getLabel().equalsIgnoreCase("mod")) {
                List<PosTaggedToken> pos_tagged_token = parseConfiguration.getDependents(posTagSequence.get(i));
                for (PosTaggedToken ptt : pos_tagged_token) {

                    if (ptt.getTag().toString().equalsIgnoreCase("ADV")
                            && parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("mod")
                            && (ptt.getComment().equalsIgnoreCase("product") )) {

                        subject2.setForm(ptt.getToken().toString());
                        subject2.setType(ptt.getComment());
                    }
                }
            }

            //(VPP,mod) +(P,(mod||dep))
            if (posTagSequence.get(i).getTag().toString().equalsIgnoreCase("VPP") && parseConfiguration.getGoverningDependency(posTagSequence.get(i)).getLabel().equalsIgnoreCase("mod")) {
                List<PosTaggedToken> pos_tagged_token = parseConfiguration.getDependents(posTagSequence.get(i));
                for (PosTaggedToken ptt : pos_tagged_token) {

                    if (ptt.getTag().toString().equalsIgnoreCase("P")
                            && (parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("mod")
                            || parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("dep"))) {
                        List<PosTaggedToken> pos_tagged_token_child = parseConfiguration.getDependents(ptt);
                        for (PosTaggedToken ptt_child : pos_tagged_token_child) {
                            if (ptt_child.getTag().toString().equalsIgnoreCase("NPP")
                                    && parseConfiguration.getGoverningDependency(ptt_child).getLabel().equalsIgnoreCase("prep")
                                    && (ptt_child.getComment().equalsIgnoreCase("product") )) {

                                subject2.setForm(ptt_child.getToken().toString());
                                subject2.setType(ptt_child.getComment());
                            }
                        }
                    }
                }
            }


        }

        return subject2;

    }

    public void constructRelations(String objectForm, Token subject, String Relation, SentenceRelationType SRT, SentenceRelationMethod SRM, String line, double confidence) {
        SentenceRelation sentenceRelation = new SentenceRelation();
        SentenceRelationId sentenceRelationId = new SentenceRelationId();
        Token objectToken = new Token();
        Token subject2 = subject;
        objectToken.setForm(objectForm);
        sentenceRelationId.setSubject(subject2);
        sentenceRelationId.setObject(objectToken);
        sentenceRelationId.setRelation(Relation);
        sentenceRelationId.setSentence_text(line);
        sentenceRelationId.setType(SRT);
        sentenceRelationId.setConfidence(confidence);
        sentenceRelation.setSentenceRelationId(sentenceRelationId);
        sentenceRelation.setMethod(SRM);
        list_result.add(sentenceRelation);
        constructXML ( sentenceRelationId,  line);

    }

    public void constructXML (SentenceRelationId sentenceRelationId, String line){


    }

    public boolean composedNames(ParseConfiguration parseConfiguration, PosTaggedToken ptt) {
        boolean iscomposed = false;
        if (((ptt.getTag().toString().equalsIgnoreCase("ADJ") && parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("mod")) ||
                (ptt.getTag().toString().equalsIgnoreCase("NPP") && parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("mod")) ||
                (ptt.getTag().toString().equalsIgnoreCase("NC") && parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("prep"))||
                (ptt.getTag().toString().equalsIgnoreCase("NC") && parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("NC"))
                && (ptt.getComment() == null || ptt.getComment().isEmpty()))) {

            iscomposed = true;
        }
        return iscomposed;
    }




    public void isCoordination(ParseConfiguration parseConfiguration, PosTaggedToken ptt,  PosTagSequence posTagSequence, String line,String old_relation ) {
        if (ptt.getTag().toString().equalsIgnoreCase("CC")
                && (parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("coord"))) {
            List<PosTaggedToken> pos_tagged_token_child = parseConfiguration.getDependents(ptt);
            for (PosTaggedToken ptt_child : pos_tagged_token_child) {
                if ((ptt_child.getTag().toString().equalsIgnoreCase("P") || ptt_child.getTag().toString().equalsIgnoreCase("P+D"))
                        && parseConfiguration.getGoverningDependency(ptt_child).getLabel().equalsIgnoreCase("dep_coord")) {
                    List<PosTaggedToken> pos_tagged_token_child1 = parseConfiguration.getDependents(ptt_child);
                    for (PosTaggedToken ptt_child1 : pos_tagged_token_child1) {
                        if ((ptt_child1.getTag().toString().equalsIgnoreCase("NC")||ptt_child1.getTag().toString().equalsIgnoreCase("NPP"))
                                && parseConfiguration.getGoverningDependency(ptt_child1).getLabel().equalsIgnoreCase("prep")
                                && (ptt_child1.getComment() == null || ptt_child1.getComment().isEmpty())) {
                            boolean annotated1 = false;
                            List<PosTaggedToken> pos_tagged_token_child2 = parseConfiguration.getDependents(ptt_child1);
                            for (PosTaggedToken ptt_child2 : pos_tagged_token_child2) {
                                if (composedNames(parseConfiguration, ptt_child2) == true) {
                                    annotated1 = true;
                                    String objectForm = ptt_child1.getToken().toString() + " " + ptt_child2.getToken().toString();
                                    Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                    String Relation = old_relation + "+(CC,coord)+((P||P+D),dep_coord)+((NPP||NC),prep)+(ADJ||NPP)";
                                    SentenceRelationType SRT = SentenceRelationType.hasComponent;
                                    SentenceRelationMethod SRM = SentenceRelationMethod.rules;
                                    double confidence = 1;
                                    constructRelations(objectForm, subject2, Relation, SRT, SRM, line, confidence);
                                }
                            }
                            if (!annotated1) {
                                String objectForm = ptt_child1.getToken().toString();
                                Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                String Relation = old_relation + "+(CC,coord)+((P||P+D),dep_coord)+((NPP||NC),prep))";
                                SentenceRelationType SRT = SentenceRelationType.hasComponent;
                                SentenceRelationMethod SRM = SentenceRelationMethod.rules;
                                double confidence = 1;
                                constructRelations(objectForm, subject2, Relation, SRT, SRM, line, confidence);
                            }
                        }
                    }
                }


                if ((ptt_child.getTag().toString().equalsIgnoreCase("NPP")||ptt_child.getTag().toString().equalsIgnoreCase("NC"))
                        && parseConfiguration.getGoverningDependency(ptt_child).getLabel().equalsIgnoreCase("dep_coord")
                        && (ptt_child.getComment() == null || ptt_child.getComment().isEmpty())) {
                    boolean annotated1 = false;
                    List<PosTaggedToken> pos_tagged_token_child1 = parseConfiguration.getDependents(ptt_child);
                    for (PosTaggedToken ptt_child1 : pos_tagged_token_child1) {
                        if (composedNames(parseConfiguration, ptt_child1) == true) {
                            annotated1 = true;
                            String objectForm = ptt_child.getToken().toString() + " " + ptt_child1.getToken().toString();
                            Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                            String Relation = old_relation + "+(CC,coord)+((NPP||NC),dep_coord)+(ADJ||NPP)";
                            SentenceRelationType SRT = SentenceRelationType.hasComponent;
                            SentenceRelationMethod SRM = SentenceRelationMethod.rules;
                            double confidence = 1;
                            constructRelations(objectForm, subject2, Relation, SRT, SRM, line, confidence);
                        }

                        if (ptt_child1.getTag().toString().equalsIgnoreCase("P")
                                && (parseConfiguration.getGoverningDependency(ptt_child1).getLabel().equalsIgnoreCase("dep"))) {
                            List<PosTaggedToken> pos_tagged_token_child2 = parseConfiguration.getDependents(ptt_child1);
                            for (PosTaggedToken ptt_child2 : pos_tagged_token_child2) {
                                if (ptt_child2.getTag().toString().equalsIgnoreCase("NC")
                                        && (parseConfiguration.getGoverningDependency(ptt_child2).getLabel().equalsIgnoreCase("prep"))
                                        && (ptt_child2.getComment() == null || ptt_child2.getComment().isEmpty())) {
                                    boolean annotated = false;
                                    List<PosTaggedToken> pos_tagged_token_child3 = parseConfiguration.getDependents(ptt_child2);
                                    for (PosTaggedToken ptt_child3 : pos_tagged_token_child3) {
                                        if (composedNames(parseConfiguration, ptt_child3) == true) {
                                            annotated = true;
                                            String objectForm = ptt_child.getToken().toString() + " " + ptt_child1.getToken().toString() + " " + ptt_child2.getToken().toString() + " " + ptt_child3.getToken().toString();
                                            Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                            String Relation = old_relation + "+(CC,coord)+((NPP||NC),dep_coord)+(P,dep)+(NC,prep)+(ADJ||NC)";
                                            SentenceRelationType SRT = SentenceRelationType.hasComponent;
                                            SentenceRelationMethod SRM = SentenceRelationMethod.rules;
                                            double confidence = 1;
                                            constructRelations(objectForm, subject2, Relation, SRT, SRM, line, confidence);
                                        }
                                    }
                                    if (!annotated) {
                                        String objectForm =ptt_child.getToken().toString() + " " + ptt_child1.getToken().toString() + " " + ptt_child2.getToken().toString();
                                        Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                        String Relation = old_relation +"+(CC,coord)+((NPP||NC),dep_coord)+(P,dep)+(NC,prep)";
                                        SentenceRelationType SRT = SentenceRelationType.hasComponent;
                                        SentenceRelationMethod SRM = SentenceRelationMethod.rules;
                                        double confidence = 1;
                                        constructRelations(objectForm, subject2, Relation, SRT, SRM, line, confidence);
                                    }
                                }
                            }
                            annotated1=true;
                        }
                    }


                    if (!annotated1) {
                        String objectForm = ptt_child.getToken().toString();
                        Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                        String Relation = old_relation +"+(CC,coord)+((NPP||NC),dep_coord)";
                        SentenceRelationType SRT = SentenceRelationType.hasComponent;
                        SentenceRelationMethod SRM = SentenceRelationMethod.rules;
                        double confidence = 1;
                        constructRelations(objectForm, subject2, Relation, SRT, SRM, line, confidence);
                    }
                }
            }
        }
    }



    public void relationRules(ParseConfiguration parseConfiguration, PosTagSequence posTagSequence, String line) {

        for (int i = 0; i < posTagSequence.size(); i++) {

            /************************************************Extraction hasComponent***********************************************/
                /*-------------------------------(V,root)+(NPP,obj)+(NC,mod)+(P,dep)+(NC,prep)+(ADJ,mod)*
(V,root)+(NPP,obj)+((NPP||NC),coord)+(ADJ,mod)*
(V,root)+(NPP,obj)+((NPP||NC),coord)+ isCoordination*/
            if (posTagSequence.get(i).getTag().toString().equalsIgnoreCase("V")
                    && parseConfiguration.getGoverningDependency(posTagSequence.get(i)).getLabel().equalsIgnoreCase("root")) {
                List<PosTaggedToken> pos_tagged_token = parseConfiguration.getDependents(posTagSequence.get(i));
                for (PosTaggedToken ptt : pos_tagged_token) {
                    if (ptt.getTag().toString().equalsIgnoreCase("NPP")
                            && parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("obj")
                            && (ptt.getComment() == null || ptt.getComment().isEmpty())) {
                        List<PosTaggedToken> pos_tagged_token_child = parseConfiguration.getDependents(ptt);
                        for (PosTaggedToken ptt_child : pos_tagged_token_child) {
                            if (ptt_child.getTag().toString().equalsIgnoreCase("NC")
                                    && parseConfiguration.getGoverningDependency(ptt_child).getLabel().equalsIgnoreCase("mod")
                                    && (ptt_child.getComment() == null || ptt_child.getComment().isEmpty())) {
                                List<PosTaggedToken> pos_tagged_token_child1 = parseConfiguration.getDependents(ptt_child);
                                for (PosTaggedToken ptt_child1 : pos_tagged_token_child1) {
                                    if (ptt_child1.getTag().toString().equalsIgnoreCase("P")
                                            && parseConfiguration.getGoverningDependency(ptt_child1).getLabel().equalsIgnoreCase("dep")) {
                                        List<PosTaggedToken> pos_tagged_token_child2 = parseConfiguration.getDependents(ptt_child1);
                                        for (PosTaggedToken ptt_child2 : pos_tagged_token_child2) {
                                            if (ptt_child2.getTag().toString().equalsIgnoreCase("NC")
                                                    && parseConfiguration.getGoverningDependency(ptt_child2).getLabel().equalsIgnoreCase("prep")
                                                    && (ptt_child2.getComment() == null || ptt_child2.getComment().isEmpty())) {
                                                boolean annotated = false;
                                                List<PosTaggedToken> pos_tagged_token_child3 = parseConfiguration.getDependents(ptt_child2);
                                                for (PosTaggedToken ptt_child3 : pos_tagged_token_child3) {
                                                    if (composedNames(parseConfiguration, ptt_child3) == true) {
                                                        annotated = true;
                                                        String objectForm = ptt_child2.getToken().toString() + " " + ptt_child3.getToken().toString();
                                                        Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                                        String Relation = "(V,root)+(NPP,obj)+(NC,mod)+(P,dep)+(NC,prep)+(ADJ,mod)";
                                                        SentenceRelationType SRT = SentenceRelationType.hasComponent;
                                                        SentenceRelationMethod SRM = SentenceRelationMethod.rules;
                                                        double confidence = 1;
                                                        constructRelations(objectForm, subject2, Relation, SRT, SRM, line, confidence);
                                                    }
                                                }

                                                if (!annotated) {
                                                    String objectForm = "ptt_child2.getToken().toString()";
                                                    Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                                    String Relation = "(V,root) +(NPP,obj)+(NC,mod)+(P,dep)+(NC,prep)";
                                                    SentenceRelationType SRT = SentenceRelationType.hasComponent;
                                                    SentenceRelationMethod SRM = SentenceRelationMethod.rules;
                                                    double confidence = 1;
                                                    constructRelations(objectForm, subject2, Relation, SRT, SRM, line, confidence);
                                                }

                                            }
                                        }
                                    }
                                }
                            }

                            if ((ptt_child.getTag().toString().equalsIgnoreCase("NPP") ||ptt_child.getTag().toString().equalsIgnoreCase("NC"))
                                    && parseConfiguration.getGoverningDependency(ptt_child).getLabel().equalsIgnoreCase("coord")
                                    && (ptt_child.getComment() == null || ptt_child.getComment().isEmpty())) {
                                boolean annotated = false;
                                List<PosTaggedToken> pos_tagged_token_child1 = parseConfiguration.getDependents(ptt_child);
                                for (PosTaggedToken ptt_child1 : pos_tagged_token_child1) {
                                    if (composedNames(parseConfiguration, ptt_child1) == true) {
                                        annotated = true;
                                        String objectForm = ptt_child.getToken().toString() + " " + ptt_child1.getToken().toString();
                                        Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                        String Relation = "(V,root)+(NPP,obj)+((NPP||NC),coord)+(ADJ,mod)";
                                        SentenceRelationType SRT = SentenceRelationType.hasComponent;
                                        SentenceRelationMethod SRM = SentenceRelationMethod.rules;
                                        double confidence = 1;
                                        constructRelations(objectForm, subject2, Relation, SRT, SRM, line, confidence);
                                    }
                                    String old_relation="(V,root)+((NPP||NC),obj)+(NPP,coord)";
                                    isCoordination(parseConfiguration, ptt_child1,posTagSequence, line,old_relation);
                                }

                                if (!annotated) {
                                    String objectForm = ptt_child.getToken().toString();
                                    Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                    String Relation = "(V,root)+(NPP,obj)+((NPP||NC),coord)";
                                    SentenceRelationType SRT = SentenceRelationType.hasComponent;
                                    SentenceRelationMethod SRM = SentenceRelationMethod.rules;
                                    double confidence = 1;
                                    constructRelations(objectForm, subject2, Relation, SRT, SRM, line, confidence);
                                }
                            }

                            String old_relation="(V,root)+(NPP,obj)";
                            isCoordination(parseConfiguration, ptt_child,posTagSequence, line,old_relation);
                        }

                        String objectForm = ptt.getToken().toString();
                        Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                        String Relation = "(V,root) +(NPP,obj)";
                        SentenceRelationType SRT = SentenceRelationType.hasComponent;
                        SentenceRelationMethod SRM = SentenceRelationMethod.rules;
                        double confidence = 1;
                        constructRelations(objectForm, subject2, Relation, SRT, SRM, line, confidence);
                    }
                }
            }

                /*-----------------------
(V,root)+(NC,suj)+((P||P+D),dep)+(NC,prep)+(ADJ,mod)*
(V,root) +(NC,suj)+((P||P+D),dep+(NC,prep)+isCoordination*/
            if (posTagSequence.get(i).getTag().toString().equalsIgnoreCase("V")
                    && parseConfiguration.getGoverningDependency(posTagSequence.get(i)).getLabel().equalsIgnoreCase("root")) {
                //Liste des dependences de i
                List<PosTaggedToken> pos_tagged_token = parseConfiguration.getDependents(posTagSequence.get(i));
                for (PosTaggedToken ptt : pos_tagged_token) {
                    if (ptt.getTag().toString().equalsIgnoreCase("NC")
                            && parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("suj") ) {
                        List<PosTaggedToken> pos_tagged_token_child2 = parseConfiguration.getDependents(ptt);
                        for (PosTaggedToken ptt_child2 : pos_tagged_token_child2) {
                            if ((ptt_child2.getTag().toString().equalsIgnoreCase("P")||ptt_child2.getTag().toString().equalsIgnoreCase("P+D"))
                                && parseConfiguration.getGoverningDependency(ptt_child2).getLabel().equalsIgnoreCase("dep")) {
                                List<PosTaggedToken> pos_tagged_token_child3 = parseConfiguration.getDependents(ptt_child2);
                                for (PosTaggedToken ptt_child3 : pos_tagged_token_child3) {
                                    if ((ptt_child3.getTag().toString().equalsIgnoreCase("NC")
                                            && (parseConfiguration.getGoverningDependency(ptt_child3).getLabel().equalsIgnoreCase("prep")
                                            && (ptt_child3.getComment() == null || ptt_child3.getComment().isEmpty())))) {
                                        boolean annotated = false;
                                        List<PosTaggedToken> pos_tagged_token_child4 = parseConfiguration.getDependents(ptt_child3);
                                        for (PosTaggedToken ptt_child4 : pos_tagged_token_child4) {
                                            if (composedNames(parseConfiguration, ptt_child4) == true) {
                                                annotated = true;
                                                String objectForm = ptt_child3.getToken().toString() + " " + ptt_child4.getToken().toString();
                                                Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                                String Relation = "(V,root)+(NC,suj)+((P||P+D),dep)+(NC,prep)+(ADJ,mod)";
                                                SentenceRelationType SRT = SentenceRelationType.hasComponent;
                                                SentenceRelationMethod SRM = SentenceRelationMethod.rules;
                                                double confidence = 1;
                                                constructRelations(objectForm, subject2, Relation, SRT, SRM, line, confidence);
                                            }
                                        }
                                        if (!annotated) {
                                            String objectForm = ptt_child3.getToken().toString();
                                            Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                            String Relation = "(V,root)+(NC,suj)+((P||P+D),dep)+(NC,prep)";
                                            SentenceRelationType SRT = SentenceRelationType.hasComponent;
                                            SentenceRelationMethod SRM = SentenceRelationMethod.rules;
                                            double confidence = 1;
                                            constructRelations(objectForm, subject2, Relation, SRT, SRM, line, confidence);

                                        }
                                    }
                                    String old_relation="(V,root)+(NC,suj)+((P||P+D),dep)+(NC,prep)";
                                    isCoordination(parseConfiguration, ptt_child3,posTagSequence, line,old_relation);
                                }
                            }
                        }
                    }
                }
            }

            /*(V,root)+(P,(P_obj||de_obj))+((NC||NPP),prep)+(ADJ,mod)*
            (V,root)+(P,(P_obj||de_obj))+((NC||NPP),prep)+(NC,mod)+(ADJ,mod)*+(CC,coord)+ isCoordination
                    (V,root)+(P,(P_obj||de_obj))+((NC||NPP),prep)+isCoordination
                    (V,root)+(P,(P_obj||de_obj))+((P||P+D),coord) +((NC||NPP),prep)+(ADJ,mod)* */
            if (posTagSequence.get(i).getTag().toString().equalsIgnoreCase("V")
                    && (parseConfiguration.getGoverningDependency(posTagSequence.get(i)).getLabel().equalsIgnoreCase("root")
           /* ||parseConfiguration.getGoverningDependency(posTagSequence.get(i)).getLabel().equalsIgnoreCase("mod_rel")*/)) {
                //Liste des dependences de i
                List<PosTaggedToken> pos_tagged_token = parseConfiguration.getDependents(posTagSequence.get(i));
                for (PosTaggedToken ptt : pos_tagged_token) {
                    if ((ptt.getTag().toString().equalsIgnoreCase("P")||ptt.getTag().toString().equalsIgnoreCase("P+D"))
                            && (parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("P_obj")
                            || parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("de_obj")
                    ||parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("mod"))) {
                        List<PosTaggedToken> pos_tagged_token_test_0 = parseConfiguration.getDependents(ptt);
                        for (PosTaggedToken ptt_test0 : pos_tagged_token_test_0) {
                            if ((ptt_test0.getTag().toString().equalsIgnoreCase("NC") || (ptt_test0.getTag().toString().equalsIgnoreCase("NPP"))
                                    && parseConfiguration.getGoverningDependency(ptt_test0).getLabel().equalsIgnoreCase("prep"))
                                    && (/*ptt_test0.getComment().equalsIgnoreCase("LOC") ||*/ ptt_test0.getComment() == null || ptt_test0.getComment().isEmpty())) {
                                boolean annotated = false;
                                List<PosTaggedToken> pos_tagged_token_child3 = parseConfiguration.getDependents(ptt_test0);
                                for (PosTaggedToken ptt_child3 : pos_tagged_token_child3) {
                                    if (composedNames(parseConfiguration, ptt_child3) == true) {
                                        annotated = true;
                                        String objectForm = ptt_test0.getToken().toString() + " " + ptt_child3.getToken().toString();
                                        Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                        String Relation = "(V,root)+(P,(P_obj||de_obj))+((NC||NPP),prep)+(ADJ,mod)";
                                        SentenceRelationType SRT = SentenceRelationType.hasComponent;
                                        SentenceRelationMethod SRM = SentenceRelationMethod.rules;
                                        double confidence = 1;
                                        constructRelations(objectForm, subject2, Relation, SRT, SRM, line, confidence);
                                    }

                                    if (ptt_child3.getTag().toString().equalsIgnoreCase("NC")
                                            && parseConfiguration.getGoverningDependency(ptt_child3).getLabel().equalsIgnoreCase("mod")) {
                                        List<PosTaggedToken> pos_tagged_token_child4 = parseConfiguration.getDependents(ptt_child3);
                                        boolean annotated1=false;
                                        for (PosTaggedToken ptt_child4 : pos_tagged_token_child4) {
                                                if (composedNames(parseConfiguration, ptt_child4) == true) {
                                                    annotated1=true;
                                                    String objectForm =ptt_child3.getToken().toString()+" "+ ptt_child4.getToken().toString();
                                                    Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                                    String Relation = "(V,root)+(P,(P_obj||de_obj))+((NC||NPP),prep)+(NC,mod)+(ADJ,mod)*+(CC,coord)";
                                                    SentenceRelationType SRT = SentenceRelationType.hasComponent;
                                                    SentenceRelationMethod SRM = SentenceRelationMethod.rules;
                                                    double confidence = 1;
                                                    constructRelations(objectForm, subject2, Relation, SRT, SRM, line, confidence);
                                                }
                                            if (ptt_child4.getTag().toString().equalsIgnoreCase("CC")
                                                    && parseConfiguration.getGoverningDependency(ptt_child4).getLabel().equalsIgnoreCase("coord")) {
                                                List<PosTaggedToken> pos_tagged_token_child5 = parseConfiguration.getDependents(ptt_child4);
                                                for (PosTaggedToken ptt_child5 : pos_tagged_token_child5) {
                                                    String old_relation="(V,root)+(P,(P_obj||de_obj))+((NC||NPP),prep)+(NC,mod)+(CC,coord)";
                                                    isCoordination(parseConfiguration, ptt_child5,posTagSequence, line,old_relation);
                                                }

                                            }
                                        }

                                        if(!annotated1){

                                            String objectForm = ptt_child3.getToken().toString();
                                            Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                            String Relation = "(V,root)+(P,(P_obj||de_obj))+((NC||NPP),prep)+(ADJ,mod)";
                                            SentenceRelationType SRT = SentenceRelationType.hasComponent;
                                            SentenceRelationMethod SRM = SentenceRelationMethod.rules;
                                            double confidence = 1;
                                            constructRelations(objectForm, subject2, Relation, SRT, SRM, line, confidence);
                                        }
                                    }
                                    if (ptt_child3.getTag().toString().equalsIgnoreCase("CC")
                                            && parseConfiguration.getGoverningDependency(ptt_child3).getLabel().equalsIgnoreCase("coord")) {
                                        List<PosTaggedToken> pos_tagged_token_child4 = parseConfiguration.getDependents(ptt_child3);
                                        for (PosTaggedToken ptt_child4 : pos_tagged_token_child4) {
                                            String old_relation="(V,root)+(P,(P_obj||de_obj))+((NC||NPP),prep)+(CC,coord)";
                                            isCoordination(parseConfiguration, ptt_child4,posTagSequence, line,old_relation);
                                        }

                                    }
                                }
                                if (!annotated) {
                                    Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);

                                        String objectForm = ptt_test0.getToken().toString();
                                        subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                        String Relation = "(V,root)+(P,(P_obj||de_obj))+((NC||NPP),prep";
                                        SentenceRelationType SRT = SentenceRelationType.hasComponent;
                                        SentenceRelationMethod SRM = SentenceRelationMethod.rules;
                                        double confidence = 1;
                                        constructRelations(objectForm, subject2, Relation, SRT, SRM, line, confidence);

                                }
                            }

                            if ((ptt_test0.getTag().toString().equalsIgnoreCase("P") || ptt_test0.getTag().toString().equalsIgnoreCase("P+D"))
                                    && parseConfiguration.getGoverningDependency(ptt_test0).getLabel().equalsIgnoreCase("coord")) {
                                List<PosTaggedToken> pos_tagged_token_test_1 = parseConfiguration.getDependents(ptt_test0);
                                for (PosTaggedToken ptt_test1 : pos_tagged_token_test_1) {
                                    if ((ptt_test1.getTag().toString().equalsIgnoreCase("NC") || ptt_test1.getTag().toString().equalsIgnoreCase("NPP"))
                                            && parseConfiguration.getGoverningDependency(ptt_test1).getLabel().equalsIgnoreCase("prep")
                                            && (ptt_test1.getComment() == null || ptt_test1.getComment().isEmpty())) {
                                        boolean annotated1 = false;
                                        List<PosTaggedToken> pos_tagged_token_child3 = parseConfiguration.getDependents(ptt_test1);
                                        for (PosTaggedToken ptt_child3 : pos_tagged_token_child3) {
                                            if (composedNames(parseConfiguration, ptt_child3) == true) {
                                                annotated1=true;
                                                String objectForm =ptt_test1.getToken().toString()+" "+ ptt_child3.getToken().toString();
                                                Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                                String Relation = "(V,root)+(P,(P_obj||de_obj))+((P||P+D),coord) +((NC||NPP),prep)+(ADJ,mod)*";
                                                SentenceRelationType SRT = SentenceRelationType.hasComponent;
                                                SentenceRelationMethod SRM = SentenceRelationMethod.rules;
                                                double confidence = 1;
                                                constructRelations(objectForm, subject2, Relation, SRT, SRM, line, confidence);
                                            }
                                        }

                                        if(!annotated1) {
                                            String objectForm = ptt_test1.getToken().toString();
                                            Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                            String Relation = "(V,root)+(P,(P_obj||de_obj))+((P||P+D),coord) +((NC||NPP),prep)";
                                            SentenceRelationType SRT = SentenceRelationType.hasComponent;
                                            SentenceRelationMethod SRM = SentenceRelationMethod.rules;
                                            double confidence = 1;
                                            constructRelations(objectForm, subject2, Relation, SRT, SRM, line, confidence);
                                        }
                                    }

                                    String old_relation="(V,root)+(P,(P_obj||de_obj))+((P||P+D),coord) +((NC||NPP),prep)";
                                    isCoordination(parseConfiguration, ptt_test1,posTagSequence, line,old_relation);
                                }

                            }
                            String old_relation="(V,root)+(P,(P_obj||de_obj))+((P||P+D),coord)";
                            isCoordination(parseConfiguration, ptt_test0,posTagSequence, line,old_relation);
                        }

                    }
                }
            }


                /*-----------------------(V,root)+(NC,obj)+(V,mod_rel)+(NC,obj)+(CC,coord)+(NPP,dep_coord)+(ADJ,mod)---------------------------*/
                /*-----------------------(V,root)+(NC,obj)+(V,mod_rel)+(NC,obj)+(CC,coord)+(NPP,dep_coord)---------------------------*/
            if (posTagSequence.get(i).getTag().toString().equalsIgnoreCase("V")
                    && parseConfiguration.getGoverningDependency(posTagSequence.get(i)).getLabel().equalsIgnoreCase("root")) {
                //Liste des dependences de i
                List<PosTaggedToken> pos_tagged_token = parseConfiguration.getDependents(posTagSequence.get(i));
                for (PosTaggedToken ptt : pos_tagged_token) {
                    if (ptt.getTag().toString().equalsIgnoreCase("NC") && parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("obj")
                            && (ptt.getComment() == null || ptt.getComment().isEmpty())) {
                        List<PosTaggedToken> pos_tagged_token_ptt = parseConfiguration.getDependents(ptt);
                        for (PosTaggedToken ptt_child : pos_tagged_token_ptt) {
                            if (ptt_child.getTag().toString().equalsIgnoreCase("V")
                                    && parseConfiguration.getGoverningDependency(ptt_child).getLabel().equalsIgnoreCase("mod_rel")) {
                                List<PosTaggedToken> pos_tagged_token_ptt1 = parseConfiguration.getDependents(ptt_child);
                                for (PosTaggedToken ptt_child1 : pos_tagged_token_ptt1) {
                                    if (ptt_child1.getTag().toString().equalsIgnoreCase("NC")
                                            && parseConfiguration.getGoverningDependency(ptt_child1).getLabel().equalsIgnoreCase("obj")
                                            && (ptt_child1.getComment() == null || ptt_child1.getComment().isEmpty())) {
                                        List<PosTaggedToken> pos_tagged_token_ptt2 = parseConfiguration.getDependents(ptt_child1);
                                        for (PosTaggedToken ptt_child2 : pos_tagged_token_ptt2) {
                                            String old_relation="(V,root)+(NC,obj)+(V,mod_rel)+(NC,obj)";
                                            isCoordination(parseConfiguration, ptt_child2,posTagSequence, line,old_relation);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

            }



                /*-----------------------(V,root)+(NC,obj)+(NC,mod)+(ADJ,mod)---------------------------*/
               /*----------------------- (V,root)+(NC,obj)+(NC,mod)+(P,dep)+(NC,prep)+(CC,coord)+(NPP,dep_coord)---------------------------*/
                /*-----------------------(V,root)+(NC,obj)+(NC,mod)---------------------------*/
            if (posTagSequence.get(i).getTag().toString().equalsIgnoreCase("V") && parseConfiguration.getGoverningDependency(posTagSequence.get(i)).getLabel().equalsIgnoreCase("root")) {
                //Liste des dependences de i
                List<PosTaggedToken> pos_tagged_token = parseConfiguration.getDependents(posTagSequence.get(i));
                for (PosTaggedToken ptt : pos_tagged_token) {
                    if (ptt.getTag().toString().equalsIgnoreCase("NC") && parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("obj")
                            && (ptt.getComment() == null || ptt.getComment().isEmpty())) {
                        List<PosTaggedToken> pos_tagged_token_ptt = parseConfiguration.getDependents(ptt);
                        for (PosTaggedToken ptt_child : pos_tagged_token_ptt) {
                            if ((ptt_child.getTag().toString().equalsIgnoreCase("NC")||ptt_child.getTag().toString().equalsIgnoreCase("NPP"))
                                    && parseConfiguration.getGoverningDependency(ptt_child).getLabel().equalsIgnoreCase("mod")
                                    && (ptt_child.getComment() == null || ptt_child.getComment().isEmpty())) {
                                boolean annotated = false;
                                List<PosTaggedToken> pos_tagged_token_ptt1 = parseConfiguration.getDependents(ptt_child);
                                for (PosTaggedToken ptt_child1 : pos_tagged_token_ptt1) {
                                    if (composedNames(parseConfiguration, ptt_child1) == true) {
                                        annotated = true;
                                        String objectForm = ptt_child.getToken().toString() + " " + ptt_child1.getToken().toString();
                                        Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                        String Relation = "(V,root)+(NC,obj)+(NC,mod)+(ADJ,mod)";
                                        SentenceRelationType SRT = SentenceRelationType.hasComponent;
                                        SentenceRelationMethod SRM = SentenceRelationMethod.rules;
                                        double confidence = 1;
                                        constructRelations(objectForm, subject2, Relation, SRT, SRM, line, confidence);

                                    }

                                    if (ptt_child1.getTag().toString().equalsIgnoreCase("NC")
                                            && parseConfiguration.getGoverningDependency(ptt_child1).getLabel().equalsIgnoreCase("coord")) {
                                        boolean annotated1=false;
                                        List<PosTaggedToken> pos_tagged_token_ptt2 = parseConfiguration.getDependents(ptt_child1);
                                        for (PosTaggedToken ptt_child2 : pos_tagged_token_ptt2) {
                                                if (composedNames(parseConfiguration, ptt_child2) == true) {
                                                    annotated1=true;
                                                    String objectForm =ptt_child1.getToken().toString()+" "+ ptt_child2.getToken().toString();
                                                    Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                                    String Relation = "(V,root)+(NC,obj)+(NC,mod)+(NC,coord)+(ADJ,mod)*";
                                                    SentenceRelationType SRT = SentenceRelationType.hasComponent;
                                                    SentenceRelationMethod SRM = SentenceRelationMethod.rules;
                                                    double confidence = 1;
                                                    constructRelations(objectForm, subject2, Relation, SRT, SRM, line, confidence);
                                                }


                                            if (ptt_child2.getTag().toString().equalsIgnoreCase("NC")
                                                    && parseConfiguration.getGoverningDependency(ptt_child2).getLabel().equalsIgnoreCase("coord")) {
                                                boolean annotated2=false;
                                                List<PosTaggedToken> pos_tagged_token_ptt3 = parseConfiguration.getDependents(ptt_child2);
                                                for (PosTaggedToken ptt_child3 : pos_tagged_token_ptt3) {
                                                        if (composedNames(parseConfiguration, ptt_child3) == true) {
                                                            annotated2=true;
                                                            String objectForm =ptt_child2.getToken().toString()+" "+ ptt_child3.getToken().toString();
                                                            Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                                            String Relation = "(V,root)+(NC,obj)+(NC,mod)+(NC,coord)+(NC,coord)+(ADJ,mod)*";
                                                            SentenceRelationType SRT = SentenceRelationType.hasComponent;
                                                            SentenceRelationMethod SRM = SentenceRelationMethod.rules;
                                                            double confidence = 1;
                                                            constructRelations(objectForm, subject2, Relation, SRT, SRM, line, confidence);
                                                        }
                                                    String old_relation="(V,root)+(NC,obj)+(NC,mod)+(NC,coord)+(NC,coord)";
                                                    isCoordination(parseConfiguration, ptt_child3,posTagSequence, line,old_relation);
                                                }

                                                if(!annotated2){
                                                    String objectForm = ptt_child2.getToken().toString();
                                                    Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                                   String Relation = "(V,root)+(NC,obj)+(NC,mod)+(NC,coord)+(NC,coord)";
                                                  SentenceRelationType  SRT = SentenceRelationType.hasComponent;
                                                  SentenceRelationMethod  SRM = SentenceRelationMethod.rules;
                                                   double confidence = 1;
                                                    constructRelations(objectForm, subject2, Relation, SRT, SRM, line, confidence);
                                                }

                                            }

                                        }

                                        if(!annotated1){
                                            String objectForm = ptt_child1.getToken().toString();
                                            Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                            String Relation = "(V,root)+(NC,obj)+(NC,mod)+(NC,coord)";
                                            SentenceRelationType SRT = SentenceRelationType.hasComponent;
                                            SentenceRelationMethod SRM = SentenceRelationMethod.rules;
                                            double confidence = 1;
                                            constructRelations(objectForm, subject2, Relation, SRT, SRM, line, confidence);
                                        }


                                    }
                                    if (ptt_child1.getTag().toString().equalsIgnoreCase("P") && parseConfiguration.getGoverningDependency(ptt_child1).getLabel().equalsIgnoreCase("dep")) {
                                        List<PosTaggedToken> pos_tagged_token_ptt2 = parseConfiguration.getDependents(ptt_child1);
                                        for (PosTaggedToken ptt_child2 : pos_tagged_token_ptt2) {
                                            if (ptt_child2.getTag().toString().equalsIgnoreCase("NC") && parseConfiguration.getGoverningDependency(ptt_child2).getLabel().equalsIgnoreCase("prep")
                                                    && (ptt_child2.getComment() == null || ptt_child2.getComment().isEmpty())) {
                                                List<PosTaggedToken> pos_tagged_token_ptt3 = parseConfiguration.getDependents(ptt_child2);
                                                for (PosTaggedToken ptt_child3 : pos_tagged_token_ptt3) {
                                                    String old_relation="(V,root)+(NC,obj)+(NC,mod)+(P,dep)+(NC,prep)";
                                                    isCoordination(parseConfiguration, ptt_child3,posTagSequence, line,old_relation);
                                                }
                                            }

                                        }
                                    }
                                }
                                if (!annotated) {
                                    String objectForm = ptt_child.getToken().toString();
                                    Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                    String Relation = "(V,root)+(NC,obj)+(NC,mod)";
                                    SentenceRelationType SRT = SentenceRelationType.hasComponent;
                                    SentenceRelationMethod SRM = SentenceRelationMethod.rules;
                                    double confidence = 1;
                                    constructRelations(objectForm, subject2, Relation, SRT, SRM, line, confidence);

                                }
                            }
                        }
                    }

                }
            }

                /*-----------------------(V,root)+(NC,obj)+(NC,mod)+(ADJ,mod)---------------------------*/
               /*----------------------- (V,root)+(NC,obj)+(NC,mod)+(P,dep)+(NC,prep)+(CC,coord)+(NPP,dep_coord)---------------------------*/
                /*-----------------------(V,root)+(NC,obj)+(NC,mod)---------------------------*/
            if (posTagSequence.get(i).getTag().toString().equalsIgnoreCase("V") && parseConfiguration.getGoverningDependency(posTagSequence.get(i)).getLabel().equalsIgnoreCase("root")) {
                //Liste des dependences de i
                List<PosTaggedToken> pos_tagged_token = parseConfiguration.getDependents(posTagSequence.get(i));
                for (PosTaggedToken ptt : pos_tagged_token) {
                    if (ptt.getTag().toString().equalsIgnoreCase("NC") && parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("obj")
                            && (ptt.getComment() == null || ptt.getComment().isEmpty())) {
                        List<PosTaggedToken> pos_tagged_token_ptt = parseConfiguration.getDependents(ptt);
                        for (PosTaggedToken ptt_child0 : pos_tagged_token_ptt) {
                            if ((ptt_child0.getTag().toString().equalsIgnoreCase("P") || (ptt_child0.getTag().toString().equalsIgnoreCase("P+D"))
                                    && parseConfiguration.getGoverningDependency(ptt_child0).getLabel().equalsIgnoreCase("dep"))) {

                                List<PosTaggedToken> pos_tagged_token_ptt_child = parseConfiguration.getDependents(ptt_child0);
                                for (PosTaggedToken ptt_child : pos_tagged_token_ptt_child) {

                                    if ((ptt_child.getTag().toString().equalsIgnoreCase("NC") || ptt_child.getTag().toString().equalsIgnoreCase("NPP"))
                                            && parseConfiguration.getGoverningDependency(ptt_child).getLabel().equalsIgnoreCase("prep")
                                            && (ptt_child.getComment() == null || ptt_child.getComment().isEmpty())) {
                                        boolean annotated = false;
                                        List<PosTaggedToken> pos_tagged_token_ptt1 = parseConfiguration.getDependents(ptt_child);
                                        for (PosTaggedToken ptt_child1 : pos_tagged_token_ptt1) {
                                            if (composedNames(parseConfiguration, ptt_child1) == true) {
                                                annotated = true;
                                                String objectForm = ptt_child.getToken().toString() + " " + ptt_child1.getToken().toString();
                                                Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                                String Relation = "(V,root)+(NC,obj)+(NC,mod)+(ADJ,mod)";
                                                SentenceRelationType SRT = SentenceRelationType.hasComponent;
                                                SentenceRelationMethod SRM = SentenceRelationMethod.rules;
                                                double confidence = 1;
                                                constructRelations(objectForm, subject2, Relation, SRT, SRM, line, confidence);

                                            }

                                            if (ptt_child1.getTag().toString().equalsIgnoreCase("NC")
                                                    && parseConfiguration.getGoverningDependency(ptt_child1).getLabel().equalsIgnoreCase("coord")) {
                                                boolean annotated1 = false;
                                                List<PosTaggedToken> pos_tagged_token_ptt2 = parseConfiguration.getDependents(ptt_child1);
                                                for (PosTaggedToken ptt_child2 : pos_tagged_token_ptt2) {
                                                    if (composedNames(parseConfiguration, ptt_child2) == true) {
                                                        annotated1 = true;
                                                        String objectForm = ptt_child1.getToken().toString() + " " + ptt_child2.getToken().toString();
                                                        Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                                        String Relation = "(V,root)+(NC,obj)+(NC,mod)+(NC,coord)+(ADJ,mod)*";
                                                        SentenceRelationType SRT = SentenceRelationType.hasComponent;
                                                        SentenceRelationMethod SRM = SentenceRelationMethod.rules;
                                                        double confidence = 1;
                                                        constructRelations(objectForm, subject2, Relation, SRT, SRM, line, confidence);
                                                    }


                                                    if (ptt_child2.getTag().toString().equalsIgnoreCase("NC")
                                                            && parseConfiguration.getGoverningDependency(ptt_child2).getLabel().equalsIgnoreCase("coord")) {
                                                        boolean annotated2 = false;
                                                        List<PosTaggedToken> pos_tagged_token_ptt3 = parseConfiguration.getDependents(ptt_child2);
                                                        for (PosTaggedToken ptt_child3 : pos_tagged_token_ptt3) {
                                                            if (composedNames(parseConfiguration, ptt_child3) == true) {
                                                                annotated2 = true;
                                                                String objectForm = ptt_child2.getToken().toString() + " " + ptt_child3.getToken().toString();
                                                                Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                                                String Relation = "(V,root)+(NC,obj)+(NC,mod)+(NC,coord)+(NC,coord)+(ADJ,mod)*";
                                                                SentenceRelationType SRT = SentenceRelationType.hasComponent;
                                                                SentenceRelationMethod SRM = SentenceRelationMethod.rules;
                                                                double confidence = 1;
                                                                constructRelations(objectForm, subject2, Relation, SRT, SRM, line, confidence);
                                                            }
                                                            String old_relation = "(V,root)+(NC,obj)+(NC,mod)+(NC,coord)+(NC,coord)";
                                                            isCoordination(parseConfiguration, ptt_child3, posTagSequence, line, old_relation);
                                                        }

                                                        if (!annotated2) {
                                                            String objectForm = ptt_child2.getToken().toString();
                                                            Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                                            String Relation = "(V,root)+(NC,obj)+(NC,mod)+(NC,coord)+(NC,coord)";
                                                            SentenceRelationType SRT = SentenceRelationType.hasComponent;
                                                            SentenceRelationMethod SRM = SentenceRelationMethod.rules;
                                                            double confidence = 1;
                                                            constructRelations(objectForm, subject2, Relation, SRT, SRM, line, confidence);
                                                        }

                                                    }

                                                }

                                                if (!annotated1) {
                                                    String objectForm = ptt_child1.getToken().toString();
                                                    Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                                    String Relation = "(V,root)+(NC,obj)+(NC,mod)+(NC,coord)";
                                                    SentenceRelationType SRT = SentenceRelationType.hasComponent;
                                                    SentenceRelationMethod SRM = SentenceRelationMethod.rules;
                                                    double confidence = 1;
                                                    constructRelations(objectForm, subject2, Relation, SRT, SRM, line, confidence);
                                                }
                                            }
                                            if (ptt_child1.getTag().toString().equalsIgnoreCase("P") && parseConfiguration.getGoverningDependency(ptt_child1).getLabel().equalsIgnoreCase("coord")) {
                                                List<PosTaggedToken> pos_tagged_token_ptt2 = parseConfiguration.getDependents(ptt_child1);
                                                for (PosTaggedToken ptt_child2 : pos_tagged_token_ptt2) {
                                                    if (ptt_child2.getTag().toString().equalsIgnoreCase("NC") && parseConfiguration.getGoverningDependency(ptt_child2).getLabel().equalsIgnoreCase("prep")
                                                            && (ptt_child2.getComment() == null || ptt_child2.getComment().isEmpty())) {
                                                        List<PosTaggedToken> pos_tagged_token_ptt3 = parseConfiguration.getDependents(ptt_child2);
                                                        for (PosTaggedToken ptt_child3 : pos_tagged_token_ptt3) {
                                                            String old_relation = "(V,root)+(NC,obj)+(NC,mod)+(P,dep)+(NC,prep)";
                                                            isCoordination(parseConfiguration, ptt_child3, posTagSequence, line, old_relation);
                                                        }
                                                    }
                                                    String old_relation = "(V,root)+(NC,obj)+(NC,mod)+(P,dep)+(NC,prep)";
                                                    isCoordination(parseConfiguration, ptt_child2, posTagSequence, line, old_relation);
                                                }
                                            }
                                        }
                                        if (!annotated) {
                                            String objectForm = ptt_child.getToken().toString();
                                            Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                            String Relation = "(V,root)+(NC,obj)+(NC,mod)";
                                            SentenceRelationType SRT = SentenceRelationType.hasComponent;
                                            SentenceRelationMethod SRM = SentenceRelationMethod.rules;
                                            double confidence = 1;
                                            constructRelations(objectForm, subject2, Relation, SRT, SRM, line, confidence);

                                        }
                                    }

                                    if (ptt_child.getTag().toString().equalsIgnoreCase("P") && parseConfiguration.getGoverningDependency(ptt_child).getLabel().equalsIgnoreCase("coord")) {
                                        List<PosTaggedToken> pos_tagged_token_ptt2 = parseConfiguration.getDependents(ptt_child);
                                        for (PosTaggedToken ptt_child2 : pos_tagged_token_ptt2) {
                                            if (ptt_child2.getTag().toString().equalsIgnoreCase("NC") && parseConfiguration.getGoverningDependency(ptt_child2).getLabel().equalsIgnoreCase("prep")
                                                    && (ptt_child2.getComment() == null || ptt_child2.getComment().isEmpty())) {
                                                String objectForm = ptt_child2.getToken().toString();
                                                Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                                String Relation = "(V,root)+(NC,obj)+(NC,mod)";
                                                SentenceRelationType SRT = SentenceRelationType.hasComponent;
                                                SentenceRelationMethod SRM = SentenceRelationMethod.rules;
                                                double confidence = 1;
                                                constructRelations(objectForm, subject2, Relation, SRT, SRM, line, confidence);
                                            }
                                            String old_relation="(V,root)+(NC,obj)";
                                            isCoordination(parseConfiguration, ptt_child2, posTagSequence, line, old_relation);
                                        }
                                    }
                                }
                            }

                            String old_relation="(V,root)+(NC,obj)";
                            isCoordination(parseConfiguration, ptt_child0, posTagSequence, line, old_relation);
                        }

                        }

                }
            }

                /*-----------------------------(V,sub)+(P,mod)+(NC,prep)+ (P+D,dep)+(NPP,prep)--------------------------*/
                /*------------------------------ (V,sub)+(P,mod)+(NC,prep)+ (P+D,dep)+(CC,coord)+(P,dep_coord)+(NC,prep)+(ADJ,mod)*/
            if (posTagSequence.get(i).getTag().toString().equalsIgnoreCase("V") &&
                    ( parseConfiguration.getGoverningDependency(posTagSequence.get(i)).getLabel().equalsIgnoreCase("sub"))) {
                List<PosTaggedToken> pos_tagged_token = parseConfiguration.getDependents(posTagSequence.get(i));
                for (PosTaggedToken ptt : pos_tagged_token) {
                    if (ptt.getTag().toString().equalsIgnoreCase("P") &&
                            (parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("mod"))) {
                        List<PosTaggedToken> pos_tagged_token_child = parseConfiguration.getDependents(ptt);
                        for (PosTaggedToken ptt_child : pos_tagged_token_child) {
                            if (ptt_child.getTag().toString().equalsIgnoreCase("NC")
                                    && (parseConfiguration.getGoverningDependency(ptt_child).getLabel().equalsIgnoreCase("prep"))
                                    && (ptt_child.getComment() == null || ptt_child.getComment().isEmpty())) {
                                List<PosTaggedToken> pos_tagged_token_child1 = parseConfiguration.getDependents(ptt_child);
                                for (PosTaggedToken ptt_child1 : pos_tagged_token_child1) {
                                    if ((ptt_child1.getTag().toString().equalsIgnoreCase("P+D")||ptt_child1.getTag().toString().equalsIgnoreCase("P"))
                                            && (parseConfiguration.getGoverningDependency(ptt_child1).getLabel().equalsIgnoreCase("dep"))) {
                                        List<PosTaggedToken> pos_tagged_token_child2 = parseConfiguration.getDependents(ptt_child1);
                                        for (PosTaggedToken ptt_child2 : pos_tagged_token_child2) {
                                            if ((ptt_child2.getTag().toString().equalsIgnoreCase("NPP")||ptt_child2.getTag().toString().equalsIgnoreCase("NC"))
                                                    && (parseConfiguration.getGoverningDependency(ptt_child2).getLabel().equalsIgnoreCase("prep"))
                                                    && (ptt_child2.getComment() == null || ptt_child2.getComment().isEmpty())) {
                                                boolean annotated1 = false;
                                                List<PosTaggedToken> pos_tagged_token_child3 = parseConfiguration.getDependents(ptt_child2);
                                                for (PosTaggedToken ptt_child3 : pos_tagged_token_child3) {
                                                    if (composedNames(parseConfiguration, ptt_child3) == true) {
                                                        annotated1=true;
                                                        String objectForm =ptt_child2.getToken().toString()+" "+ ptt_child3.getToken().toString();
                                                        Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                                        String Relation = "(V,sub) +(P,mod)+(NC,prep)+(P+D,dep)+(NPP,prep)+(ADJ,mod)*";
                                                        SentenceRelationType SRT = SentenceRelationType.hasComponent;
                                                        SentenceRelationMethod SRM = SentenceRelationMethod.rules;
                                                        double confidence = 1;
                                                        constructRelations(objectForm, subject2, Relation, SRT, SRM, line, confidence);
                                                    }
                                                }
                                                if(!annotated1) {
                                                    String objectForm = ptt_child2.getToken().toString();
                                                    Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                                    String Relation = "(V,sub) +(P,mod)+(NC,prep)+(P+D,dep)+(NPP,prep)";
                                                    SentenceRelationType SRT = SentenceRelationType.hasComponent;
                                                    SentenceRelationMethod SRM = SentenceRelationMethod.rules;
                                                    double confidence = 1;
                                                    constructRelations(objectForm, subject2, Relation, SRT, SRM, line, confidence);
                                                }
                                            }
                                            String oldRelation="(V,sub) +(P,mod)+(NC,prep)+(P+D,dep)+(NPP,prep)";
                                            isCoordination(parseConfiguration, ptt_child2,posTagSequence, line,oldRelation);
                                        }
                                    }

                                }
                            }
                        }


                    }

                }
            }


                /*-----------------------------(V,sub)+(P,mod)+(NC,prep)+ (P+D,dep)+(NPP,prep)--------------------------*/
                /*------------------------------ (V,sub)+(P,mod)+(NC,prep)+ (P+D,dep)+(CC,coord)+(P,dep_coord)+(NC,prep)+(ADJ,mod)*/
            if (posTagSequence.get(i).getTag().toString().equalsIgnoreCase("V") &&
                    ( parseConfiguration.getGoverningDependency(posTagSequence.get(i)).getLabel().equalsIgnoreCase("root"))) {
                List<PosTaggedToken> pos_tagged_token = parseConfiguration.getDependents(posTagSequence.get(i));
                for (PosTaggedToken ptt : pos_tagged_token) {
                    if (ptt.getTag().toString().equalsIgnoreCase("P") &&
                            (parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("mod"))) {
                        List<PosTaggedToken> pos_tagged_token_child = parseConfiguration.getDependents(ptt);
                        for (PosTaggedToken ptt_child : pos_tagged_token_child) {
                            if (ptt_child.getTag().toString().equalsIgnoreCase("NC")
                                    && (parseConfiguration.getGoverningDependency(ptt_child).getLabel().equalsIgnoreCase("prep"))
                                    && (ptt_child.getComment() == null || ptt_child.getComment().isEmpty())) {
                                List<PosTaggedToken> pos_tagged_token_child1 = parseConfiguration.getDependents(ptt_child);
                                for (PosTaggedToken ptt_child1 : pos_tagged_token_child1) {
                                    if ((ptt_child1.getTag().toString().equalsIgnoreCase("P+D")||ptt_child1.getTag().toString().equalsIgnoreCase("P"))
                                            && (parseConfiguration.getGoverningDependency(ptt_child1).getLabel().equalsIgnoreCase("dep"))) {
                                        List<PosTaggedToken> pos_tagged_token_child2 = parseConfiguration.getDependents(ptt_child1);
                                        for (PosTaggedToken ptt_child2 : pos_tagged_token_child2) {
                                            if ((ptt_child2.getTag().toString().equalsIgnoreCase("NPP")||ptt_child2.getTag().toString().equalsIgnoreCase("NC"))
                                                    && (parseConfiguration.getGoverningDependency(ptt_child2).getLabel().equalsIgnoreCase("prep"))
                                                    && (ptt_child2.getComment() == null || ptt_child2.getComment().isEmpty())) {
                                                boolean annotated1 = false;
                                                List<PosTaggedToken> pos_tagged_token_child3 = parseConfiguration.getDependents(ptt_child2);
                                                for (PosTaggedToken ptt_child3 : pos_tagged_token_child3) {
                                                    if (composedNames(parseConfiguration, ptt_child3) == true) {
                                                        annotated1=true;
                                                        String objectForm =ptt_child2.getToken().toString()+" "+ ptt_child3.getToken().toString();
                                                        Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                                        String Relation = "(V,sub) +(P,mod)+(NC,prep)+(P+D,dep)+(NPP,prep)+(ADJ,mod)*";
                                                        SentenceRelationType SRT = SentenceRelationType.hasComponent;
                                                        SentenceRelationMethod SRM = SentenceRelationMethod.rules;
                                                        double confidence = 1;
                                                        constructRelations(objectForm, subject2, Relation, SRT, SRM, line, confidence);
                                                    }

                                                }
                                                if(!annotated1) {
                                                    String objectForm = ptt_child2.getToken().toString();
                                                    Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                                    String Relation = "(V,sub) +(P,mod)+(NC,prep)+(P+D,dep)+(NPP,prep)";
                                                    SentenceRelationType SRT = SentenceRelationType.hasComponent;
                                                    SentenceRelationMethod SRM = SentenceRelationMethod.rules;
                                                    double confidence = 1;
                                                    constructRelations(objectForm, subject2, Relation, SRT, SRM, line, confidence);
                                                }
                                            }
                                            String oldRelation="(V,sub) +(P,mod)+(NC,prep)+(P+D,dep)+(NPP,prep)";
                                            isCoordination(parseConfiguration, ptt_child2,posTagSequence, line,oldRelation);
                                        }
                                    }

                                }
                            }
                        }


                    }

                }
            }
                /*---------------------(VPR,mod)+(NPP,obj)+(ADJ,mod)----------------------*/
               /* (VPR,mod)+(NPP,obj)+(CC,coord)+(NPP,dep_coord)+(ADJ,mod)*/
              /*  (VPR,mod)+(NPP,obj)+(CC,coord)+(NPP,dep_coord)*/
            if (posTagSequence.get(i).getTag().toString().equalsIgnoreCase("VPR")
                    && parseConfiguration.getGoverningDependency(posTagSequence.get(i)).getLabel().equalsIgnoreCase("mod")) {
                List<PosTaggedToken> pos_tagged_token = parseConfiguration.getDependents(posTagSequence.get(i));
                for (PosTaggedToken ptt : pos_tagged_token) {
                    // règle pour la relation hasComponent
                    //Le baume Fermeté sculputure, associant Skinfibrine et élastopeptides, vise à rendre la peau plus ferme et élastique.
                    if ((ptt.getTag().toString().equalsIgnoreCase("NPP")||ptt.getTag().toString().equalsIgnoreCase("NC"))
                            && (parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("obj"))
                            && (ptt.getComment() == null || ptt.getComment().isEmpty())) {
                        boolean annotated = false;
                        List<PosTaggedToken> pos_tagged_token_test = parseConfiguration.getDependents(ptt);
                        for (PosTaggedToken ptt_test : pos_tagged_token_test) {
                            if (composedNames(parseConfiguration, ptt_test) == true) {
                                annotated = true;
                                String objectForm = ptt.getToken().toString() + " " + ptt_test.getToken().toString();
                                Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                String Relation = "(VPR,mod)+((NPP||NC),obj)+(ADJ,mod)*";
                                SentenceRelationType SRT = SentenceRelationType.hasComponent;
                                SentenceRelationMethod SRM = SentenceRelationMethod.rules;
                                double confidence = 1;
                                constructRelations(objectForm, subject2, Relation, SRT, SRM, line, confidence);
                            }

                            if((ptt_test.getTag().toString().equalsIgnoreCase("NPP")||ptt_test.getTag().toString().equalsIgnoreCase("NC"))
                                    && (parseConfiguration.getGoverningDependency(ptt_test).getLabel().equalsIgnoreCase("coord"))
                                    && (ptt_test.getComment() == null || ptt_test.getComment().isEmpty())) {
                                boolean annotated1 = false;
                                List<PosTaggedToken> pos_tagged_token_test1 = parseConfiguration.getDependents(ptt_test);
                                for (PosTaggedToken ptt_test1 : pos_tagged_token_test1) {
                                    if (composedNames(parseConfiguration, ptt_test1) == true) {
                                        annotated1 = true;
                                        String objectForm = ptt_test.getToken().toString() + " " + ptt_test1.getToken().toString();
                                        Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                        String Relation = "(VPR,mod)+((NPP||NC),obj)+((NC||NPP),coord)+(ADJ,mod)*";
                                        SentenceRelationType SRT = SentenceRelationType.hasComponent;
                                        SentenceRelationMethod SRM = SentenceRelationMethod.rules;
                                        double confidence = 1;
                                        constructRelations(objectForm, subject2, Relation, SRT, SRM, line, confidence);
                                    }
                                    String old_relation="(VPR,mod)+((NPP||NC),obj)+((NC||NPP),coord)";
                                    isCoordination(parseConfiguration, ptt_test1, posTagSequence, line,old_relation);
                                }
                                if (!annotated1) {
                                    String objectForm = ptt_test.getToken().toString();
                                    Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                    String Relation = "(VPR,mod)+((NPP||NC),obj)+((NC||NPP),coord)";
                                    SentenceRelationType SRT = SentenceRelationType.hasComponent;
                                    SentenceRelationMethod SRM = SentenceRelationMethod.rules;
                                    double confidence = 1;
                                    constructRelations(objectForm, subject2, Relation, SRT, SRM, line, confidence);
                                }
                            }

                            String old_relation="(VPR,mod)+((NPP||NC),obj)";
                            isCoordination(parseConfiguration, ptt_test,posTagSequence, line,old_relation);
                        }
                        if (!annotated) {
                            String objectForm = ptt.getToken().toString();
                            Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                            String Relation = "(VPR,mod)+((NPP||NC),obj)";
                            SentenceRelationType SRT = SentenceRelationType.hasComponent;
                            SentenceRelationMethod SRM = SentenceRelationMethod.rules;
                            double confidence = 1;
                            constructRelations(objectForm, subject2, Relation, SRT, SRM, line, confidence);
                        }


                    }
                }
            }


                /*----------------------(VPP,(mod||dep))+ (NC,mod)+(P+D,dep)+(NC,prep)+(ADJ,mod)-------------------*/
                   /*----------------------(VPP,(mod||dep))+ (NC,mod)+(P+D,dep)+(NC,prep)-------------------*/
            if (posTagSequence.get(i).getTag().toString().equalsIgnoreCase("VPP")
                    && (parseConfiguration.getGoverningDependency(posTagSequence.get(i)).getLabel().equalsIgnoreCase("mod")
                    || parseConfiguration.getGoverningDependency(posTagSequence.get(i)).getLabel().equalsIgnoreCase("dep"))) {

                List<PosTaggedToken> pos_tagged_token = parseConfiguration.getDependents(posTagSequence.get(i));
                for (PosTaggedToken ptt : pos_tagged_token) {
                    if (ptt.getTag().toString().equalsIgnoreCase("NC")
                            && (parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("mod"))) {
                        // liste de dependence
                        List<PosTaggedToken> pos_tagged_token_test = parseConfiguration.getDependents(ptt);
                        for (PosTaggedToken ptt_test : pos_tagged_token_test) {
                            if (ptt_test.getTag().toString().equalsIgnoreCase("P+D")
                                    && (parseConfiguration.getGoverningDependency(ptt_test).getLabel().equalsIgnoreCase("dep"))) {
                                List<PosTaggedToken> pos_tagged_token_test1 = parseConfiguration.getDependents(ptt_test);
                                for (PosTaggedToken ptt_test1 : pos_tagged_token_test1) {
                                    if (ptt_test1.getTag().toString().equalsIgnoreCase("NC")
                                            && (parseConfiguration.getGoverningDependency(ptt_test1).getLabel().equalsIgnoreCase("prep"))
                                            && (ptt_test1.getComment() == null || ptt_test1.getComment().isEmpty())) {
                                        boolean annotated = false;
                                        List<PosTaggedToken> pos_tagged_token_test2 = parseConfiguration.getDependents(ptt_test1);
                                        for (PosTaggedToken ptt_test2 : pos_tagged_token_test2) {
                                            if (composedNames(parseConfiguration, ptt_test2) == true) {
                                                annotated = true;
                                                String objectForm = ptt_test1.getToken().toString() + " " + ptt_test2.getToken().toString();
                                                Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                                String Relation = "(VPP,(mod||dep))+ (NC,mod)+(P+D,dep)+(NC,prep)+(ADJ,mod)";
                                                SentenceRelationType SRT = SentenceRelationType.hasComponent;
                                                SentenceRelationMethod SRM = SentenceRelationMethod.rules;
                                                double confidence = 1;
                                                constructRelations(objectForm, subject2, Relation, SRT, SRM, line, confidence);

                                            }

                                            if (!annotated) {
                                                String objectForm = ptt_test1.getToken().toString();
                                                Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                                String Relation = "(VPP,(mod||dep))+ (NC,mod)+(P+D,dep)+(NC,prep)";
                                                SentenceRelationType SRT = SentenceRelationType.hasComponent;
                                                SentenceRelationMethod SRM = SentenceRelationMethod.rules;
                                                double confidence = 1;
                                                constructRelations(objectForm, subject2, Relation, SRT, SRM, line, confidence);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }


               /*---------------------- (VPP,mod)+(P,de_obj)+(NC,prep)+(ADJ,mod)-------------------*/
                /*---------------------- (P,p_obj)(NC,prep)(NC,mod)(CC,coord)(NC,dep_coord)+(P,dep)+(NC,prep)+(ADJ,mod)-------------------*/
            if (posTagSequence.get(i).getTag().toString().equalsIgnoreCase("VPP")
                    && (parseConfiguration.getGoverningDependency(posTagSequence.get(i)).getLabel().equalsIgnoreCase("mod")
                    || parseConfiguration.getGoverningDependency(posTagSequence.get(i)).getLabel().equalsIgnoreCase("dep"))) {
                List<PosTaggedToken> pos_tagged_token = parseConfiguration.getDependents(posTagSequence.get(i));
                for (PosTaggedToken ptt : pos_tagged_token) {
                    if (ptt.getTag().toString().equalsIgnoreCase("P")
                            && (parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("de_obj"))) {
                        List<PosTaggedToken> pos_tagged_token_test = parseConfiguration.getDependents(ptt);
                        for (PosTaggedToken ptt_test : pos_tagged_token_test) {
                            String old_relation="(VPP,(mod||dep)),(P,de_obj)";
                            isCoordination(parseConfiguration, ptt_test,posTagSequence, line,old_relation);
                            if (ptt_test.getTag().toString().equalsIgnoreCase("NC")
                                    && (parseConfiguration.getGoverningDependency(ptt_test).getLabel().equalsIgnoreCase("prep"))
                                    && (ptt_test.getComment() == null || ptt_test.getComment().isEmpty())) {
                                boolean annotated = false;
                                List<PosTaggedToken> pos_tagged_token_test1 = parseConfiguration.getDependents(ptt_test);
                                for (PosTaggedToken ptt_test1 : pos_tagged_token_test1) {
                                    if (composedNames(parseConfiguration, ptt_test1) == true) {
                                        annotated = true;
                                        String objectForm = ptt_test.getToken().toString() + " " + ptt_test1.getToken().toString();
                                        Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                        String Relation = "(VPP,mod)+(P,de_obj)+(NC,prep)+(ADJ,mod)";
                                        SentenceRelationType SRT = SentenceRelationType.hasComponent;
                                        SentenceRelationMethod SRM = SentenceRelationMethod.rules;
                                        double confidence = 1;
                                        constructRelations(objectForm, subject2, Relation, SRT, SRM, line, confidence);


                                    }
                                }
                                if (!annotated) {
                                    String objectForm = ptt_test.getToken().toString();
                                    Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                    String Relation = "(VPP,mod)+(P,de_obj)+(NC,prep)";
                                    SentenceRelationType SRT = SentenceRelationType.hasComponent;
                                    SentenceRelationMethod SRM = SentenceRelationMethod.rules;
                                    double confidence = 1;
                                    constructRelations(objectForm, subject2, Relation, SRT, SRM, line, confidence);

                                }
                            }
                        }
                    }
                }
            }


            /*----------------------(VPP,(mod||dep))+(P+D,dep)+(NC,prep)+(ADJ,mod)-------------------*/
                  /*----------------------(VPP,(mod||dep))+(P+D,dep)+(NC,prep)-------------------*/
                  /*---------------------- (VPP,(mod||dep))+(P+D,dep)+(CC,coord)+(P,dep_coord)+(NC,prep)-------------------*/
            if (posTagSequence.get(i).getTag().toString().equalsIgnoreCase("VPP")
                    && (parseConfiguration.getGoverningDependency(posTagSequence.get(i)).getLabel().equalsIgnoreCase("mod")
                    || parseConfiguration.getGoverningDependency(posTagSequence.get(i)).getLabel().equalsIgnoreCase("dep"))) {
                List<PosTaggedToken> pos_tagged_token = parseConfiguration.getDependents(posTagSequence.get(i));
                for (PosTaggedToken ptt : pos_tagged_token) {
                    if ((ptt.getTag().toString().equalsIgnoreCase("P+D"))
                            && (parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("mod"))) {
                        List<PosTaggedToken> pos_tagged_token_test = parseConfiguration.getDependents(ptt);
                        for (PosTaggedToken ptt_test : pos_tagged_token_test) {
                            String old_relation="(VPP,(mod||dep))+(P+D,mod)";
                            isCoordination(parseConfiguration, ptt_test,posTagSequence, line,old_relation);
                            if (ptt_test.getTag().toString().equalsIgnoreCase("NC")
                                    && (parseConfiguration.getGoverningDependency(ptt_test).getLabel().equalsIgnoreCase("prep"))
                                    && (ptt_test.getComment() == null || ptt_test.getComment().isEmpty())) {
                                boolean annotated = false;
                                List<PosTaggedToken> pos_tagged_token_test2 = parseConfiguration.getDependents(ptt_test);
                                for (PosTaggedToken ptt_test2 : pos_tagged_token_test2) {
                                    if (composedNames(parseConfiguration, ptt_test2) == true) {
                                        annotated = true;
                                        String objectForm = ptt_test.getToken().toString() + " " + ptt_test2.getToken().toString();
                                        Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                        String Relation = "(VPP,(mod||dep))+(P+D,dep)+(NC,prep)+(ADJ,mod)";
                                        SentenceRelationType SRT = SentenceRelationType.hasComponent;
                                        SentenceRelationMethod SRM = SentenceRelationMethod.rules;
                                        double confidence = 1;
                                        constructRelations(objectForm, subject2, Relation, SRT, SRM, line, confidence);
                                    }
                                    if ((ptt_test2.getTag().toString().equalsIgnoreCase("P+D") || ptt_test2.getTag().toString().equalsIgnoreCase("P"))
                                            && (parseConfiguration.getGoverningDependency(ptt_test2).getLabel().equalsIgnoreCase("dep"))) {
                                      List<PosTaggedToken> pos_tagged_token_test3 = parseConfiguration.getDependents(ptt_test2);
                                        for (PosTaggedToken ptt_test3 : pos_tagged_token_test3) {
                                            if (ptt_test3.getTag().toString().equalsIgnoreCase("NC")
                                                    && (parseConfiguration.getGoverningDependency(ptt_test3).getLabel().equalsIgnoreCase("prep"))
                                                    && (ptt_test3.getComment() == null || ptt_test3.getComment().isEmpty())) {
                                                boolean annotated1=false;
                                                List<PosTaggedToken> pos_tagged_token_test4 = parseConfiguration.getDependents(ptt_test3);
                                                for (PosTaggedToken ptt_test4 : pos_tagged_token_test4) {
                                                    if (composedNames(parseConfiguration, ptt_test4) == true) {
                                                        annotated1 = true;
                                                        String objectForm = ptt_test3.getToken().toString() + " " + ptt_test4.getToken().toString();
                                                        Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                                        String Relation = "(VPP+(mod||dep))+(P+D,mod)+(NC,prep)+((P+D||P),dep)+(NC,prep)+(ADJ,mod)*";
                                                        SentenceRelationType SRT = SentenceRelationType.hasComponent;
                                                        SentenceRelationMethod SRM = SentenceRelationMethod.rules;
                                                        double confidence = 1;
                                                        constructRelations(objectForm, subject2, Relation, SRT, SRM, line, confidence);
                                                    }

                                                }
                                                if(!annotated1) {
                                                    String objectForm = ptt_test3.getToken().toString();
                                                    Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                                    String Relation = "(VPP+(mod||dep))+(P+D,mod)+(NC,prep)+((P+D||P),dep)+(NC,prep)";
                                                    SentenceRelationType SRT = SentenceRelationType.hasComponent;
                                                    SentenceRelationMethod SRM = SentenceRelationMethod.rules;
                                                    double confidence = 1;
                                                    constructRelations(objectForm, subject2, Relation, SRT, SRM, line, confidence);
                                                }
                                            }
                                             old_relation="(VPP+(mod||dep))+(P+D,mod)+(NC,prep)+((P+D||P),dep)";
                                            isCoordination(parseConfiguration, ptt_test3,posTagSequence, line,old_relation);
                                        }
                                    }
                                }
                                if (!annotated) {
                                    String objectForm = ptt_test.getToken().toString();
                                    Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                    String Relation = "(VPP, mod)(P+D,dep)+(NC,prep)";
                                    SentenceRelationType SRT = SentenceRelationType.hasComponent;
                                    SentenceRelationMethod SRM = SentenceRelationMethod.rules;
                                    double confidence = 1;
                                    constructRelations(objectForm, subject2, Relation, SRT, SRM, line, confidence);
                                }
                            }
                        }
                    }
                }
            }

             /*----------------------(VPP,(mod||dep))+(P+D,dep)+(NC,prep)+(ADJ,mod)-------------------*/
                  /*----------------------(VPP,(mod||dep))+(P+D,dep)+(NC,prep)-------------------*/
                  /*---------------------- (VPP,(mod||dep))+(P+D,dep)+(CC,coord)+(P,dep_coord)+(NC,prep)-------------------*/
            if (posTagSequence.get(i).getTag().toString().equalsIgnoreCase("VPP")
                    && (parseConfiguration.getGoverningDependency(posTagSequence.get(i)).getLabel().equalsIgnoreCase("mod")
                    || parseConfiguration.getGoverningDependency(posTagSequence.get(i)).getLabel().equalsIgnoreCase("dep"))) {
                List<PosTaggedToken> pos_tagged_token = parseConfiguration.getDependents(posTagSequence.get(i));
                for (PosTaggedToken ptt : pos_tagged_token) {
                    if ((ptt.getTag().toString().equalsIgnoreCase("P")||ptt.getTag().toString().equalsIgnoreCase("P+D"))
                            && (parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("de_obj"))) {
                        List<PosTaggedToken> pos_tagged_token_test = parseConfiguration.getDependents(ptt);
                        for (PosTaggedToken ptt_test : pos_tagged_token_test) {
                            String old_relation="(VPP,(mod||dep))+(P,de_obj)";
                            isCoordination(parseConfiguration, ptt_test,posTagSequence, line,old_relation);
                            if (ptt_test.getTag().toString().equalsIgnoreCase("NPP")
                                    && (parseConfiguration.getGoverningDependency(ptt_test).getLabel().equalsIgnoreCase("prep"))
                                    && (ptt_test.getComment() == null || ptt_test.getComment().isEmpty())) {
                                boolean annotated = false;
                                List<PosTaggedToken> pos_tagged_token_test2 = parseConfiguration.getDependents(ptt_test);
                                for (PosTaggedToken ptt_test2 : pos_tagged_token_test2) {
                                    if (composedNames(parseConfiguration, ptt_test2) == true) {
                                        annotated = true;
                                        String objectForm = ptt_test.getToken().toString() + " " + ptt_test2.getToken().toString();
                                        Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                        String Relation = "(VPP,(mod||dep))+(P+D,dep)+(NC,prep)+(ADJ,mod)";
                                        SentenceRelationType SRT = SentenceRelationType.hasComponent;
                                        SentenceRelationMethod SRM = SentenceRelationMethod.rules;
                                        double confidence = 1;
                                        constructRelations(objectForm, subject2, Relation, SRT, SRM, line, confidence);
                                    }
                                    if ((ptt_test2.getTag().toString().equalsIgnoreCase("P+D") || ptt_test2.getTag().toString().equalsIgnoreCase("P"))
                                            && (parseConfiguration.getGoverningDependency(ptt_test2).getLabel().equalsIgnoreCase("dep"))) {
                                        List<PosTaggedToken> pos_tagged_token_test3 = parseConfiguration.getDependents(ptt_test2);
                                        for (PosTaggedToken ptt_test3 : pos_tagged_token_test3) {
                                            if (ptt_test3.getTag().toString().equalsIgnoreCase("NC")
                                                    && (parseConfiguration.getGoverningDependency(ptt_test3).getLabel().equalsIgnoreCase("prep"))
                                                    && (ptt_test3.getComment() == null || ptt_test3.getComment().isEmpty())) {
                                                boolean annotated1=false;
                                                List<PosTaggedToken> pos_tagged_token_test4 = parseConfiguration.getDependents(ptt_test3);
                                                for (PosTaggedToken ptt_test4 : pos_tagged_token_test4) {
                                                    if (composedNames(parseConfiguration, ptt_test4) == true) {
                                                        annotated1 = true;
                                                        String objectForm = ptt_test3.getToken().toString() + " " + ptt_test4.getToken().toString();
                                                        Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                                        String Relation = "(VPP+(mod||dep))+(P+D,mod)+(NC,prep)+((P+D||P),dep)+(NC,prep)+(ADJ,mod)*";
                                                        SentenceRelationType SRT = SentenceRelationType.hasComponent;
                                                        SentenceRelationMethod SRM = SentenceRelationMethod.rules;
                                                        double confidence = 1;
                                                        constructRelations(objectForm, subject2, Relation, SRT, SRM, line, confidence);
                                                    }

                                                }
                                                if(!annotated1) {
                                                    String objectForm = ptt_test3.getToken().toString();
                                                    Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                                    String Relation = "(VPP+(mod||dep))+(P+D,mod)+(NC,prep)+((P+D||P),dep)+(NC,prep)";
                                                    SentenceRelationType SRT = SentenceRelationType.hasComponent;
                                                    SentenceRelationMethod SRM = SentenceRelationMethod.rules;
                                                    double confidence = 1;
                                                    constructRelations(objectForm, subject2, Relation, SRT, SRM, line, confidence);
                                                }
                                            }
                                            old_relation="(VPP+(mod||dep))+(P+D,mod)+(NC,prep)+((P+D||P),dep)";
                                            isCoordination(parseConfiguration, ptt_test3,posTagSequence, line,old_relation);
                                        }
                                    }
                                }
                                if (!annotated) {
                                    String objectForm = ptt_test.getToken().toString();
                                    Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                    String Relation = "(VPP, mod)(P+D,dep)+(NC,prep)";
                                    SentenceRelationType SRT = SentenceRelationType.hasComponent;
                                    SentenceRelationMethod SRM = SentenceRelationMethod.rules;
                                    double confidence = 1;
                                    constructRelations(objectForm, subject2, Relation, SRT, SRM, line, confidence);
                                }
                            }
                        }
                    }
                }
            }
            /*--------------------------(VPP,mod||dep)+(P,p_obj)+(P,dep)(NC,prep)---(à verifier)-----------------------*/
            if (posTagSequence.get(i).getTag().toString().equalsIgnoreCase("VPP")
                    && (parseConfiguration.getGoverningDependency(posTagSequence.get(i)).getLabel().equalsIgnoreCase("mod")
                    || parseConfiguration.getGoverningDependency(posTagSequence.get(i)).getLabel().equalsIgnoreCase("dep"))) {
                List<PosTaggedToken> pos_tagged_token = parseConfiguration.getDependents(posTagSequence.get(i));
                for (PosTaggedToken ptt : pos_tagged_token) {
                    if (ptt.getTag().toString().equalsIgnoreCase("P")
                            && (parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("p_obj"))) {
                        List<PosTaggedToken> pos_tagged_token_test = parseConfiguration.getDependents(ptt);
                        for (PosTaggedToken ptt_test : pos_tagged_token_test) {

                            if (ptt_test.getTag().toString().equalsIgnoreCase("NC")
                                    && parseConfiguration.getGoverningDependency(ptt_test).getLabel().equalsIgnoreCase("prep")) {
                                List<PosTaggedToken> pos_tagged_token_test_1 = parseConfiguration.getDependents(ptt_test);
                                for (PosTaggedToken ptt_test_1 : pos_tagged_token_test_1) {
                                    if ((ptt_test_1.getTag().toString().equalsIgnoreCase("P")||ptt_test_1.getTag().toString().equalsIgnoreCase("P+D"))
                                            && (parseConfiguration.getGoverningDependency(ptt_test_1).getLabel().equalsIgnoreCase("dep"))
                                            && (ptt_test_1.getComment() == null || ptt_test_1.getComment().isEmpty())) {
                                        List<PosTaggedToken> pos_tagged_token_test_2 = parseConfiguration.getDependents(ptt_test_1);
                                        for (PosTaggedToken ptt_test_2 : pos_tagged_token_test_2) {
                                            if (ptt_test_2.getTag().toString().equalsIgnoreCase("NC")
                                                    && (parseConfiguration.getGoverningDependency(ptt_test_2).getLabel().equalsIgnoreCase("prep"))
                                                    && (ptt_test_2.getComment() == null || ptt_test_2.getComment().isEmpty())) {
                                                boolean annotated1 = false;
                                                List<PosTaggedToken> pos_tagged_token_child3 = parseConfiguration.getDependents(ptt_test_2);
                                                for (PosTaggedToken ptt_child3 : pos_tagged_token_child3) {
                                                    if (composedNames(parseConfiguration, ptt_child3) == true) {
                                                        annotated1 = true;
                                                        String objectForm = ptt_test_2.getToken().toString() + " " + ptt_child3.getToken().toString();
                                                        Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                                        String Relation = "(VPP,(mod||dep)) +(P,p_obj)+(NC,prep)+((P||P+D),dep)+(NC,prep)+(ADJ,mod)*";
                                                        SentenceRelationType SRT = SentenceRelationType.hasComponent;
                                                        SentenceRelationMethod SRM = SentenceRelationMethod.rules;
                                                        double confidence = 1;
                                                        constructRelations(objectForm, subject2, Relation, SRT, SRM, line, confidence);
                                                    }
                                                }

                                                if (!annotated1) {
                                                    String objectForm = ptt_test_2.getToken().toString();
                                                    Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                                    String Relation = "(VPP,(mod||dep)) +(P,p_obj)+(NC,prep)+((P||P+D),dep)+(NC,prep)";
                                                    SentenceRelationType SRT = SentenceRelationType.hasComponent;
                                                    SentenceRelationMethod SRM = SentenceRelationMethod.rules;
                                                    double confidence = 1;
                                                    constructRelations(objectForm, subject2, Relation, SRT, SRM, line, confidence);
                                                }
                                            }
                                        }
                                    }

                                    String old_relation="(VPP,(mod||dep)) +(P,p_obj)+(NC,prep)+((P||P+D),dep)";
                                    isCoordination(parseConfiguration, ptt_test_1,posTagSequence, line,old_relation);

                                    if (ptt_test_1.getTag().toString().equalsIgnoreCase("NC")
                                            && (parseConfiguration.getGoverningDependency(ptt_test_1).getLabel().equalsIgnoreCase("mod"))
                                            && (ptt_test_1.getComment() == null || ptt_test_1.getComment().isEmpty())) {
                                        boolean annotated = false;
                                        List<PosTaggedToken> pos_tagged_token_test_2 = parseConfiguration.getDependents(ptt_test_1);
                                        for (PosTaggedToken ptt_test_2 : pos_tagged_token_test_2) {
                                            if (composedNames(parseConfiguration, ptt_test_2) == true) {
                                                annotated = true;
                                                String objectForm = ptt_test_1.getToken().toString() + " " + ptt_test_2.getToken().toString();
                                                Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                                String Relation = "(VPP,(mod||dep)) +(P,p_obj)+(NC,prep)+(NC,mod)+(ADJ,mod)*";
                                                SentenceRelationType SRT = SentenceRelationType.hasComponent;
                                                SentenceRelationMethod SRM = SentenceRelationMethod.rules;
                                                double confidence = 1;
                                                constructRelations(objectForm, subject2, Relation, SRT, SRM, line, confidence);

                                            }
                                             old_relation="(VPP,(mod||dep)) +(P,p_obj)+(NC,prep)+(NC,mod)";
                                            isCoordination(parseConfiguration, ptt_test_2,posTagSequence, line,old_relation);
                                        }
                                        if (!annotated) {
                                            String objectForm = ptt_test_1.getToken().toString();
                                            Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                            String Relation = "(VPP,(mod||dep)) +(P,p_obj)+(NC,prep)+(NC,mod)";
                                            SentenceRelationType SRT = SentenceRelationType.hasComponent;
                                            SentenceRelationMethod SRM = SentenceRelationMethod.rules;
                                            double confidence = 1;
                                            constructRelations(objectForm, subject2, Relation, SRT, SRM, line, confidence);
                                        }
                                    }

                                }


                            }

                        }
                    }
                }
            }



            /*--------------------------(VPP,root)+(NC,obj)+(P,dep)+(NC,prep)+(P,dep)+(VInf,prep)--------------------------*/
             /*-------------------------- (VPP,root)+(CC,coord)+ (NC,dep_coord)+(ADJ,mod)----------------------------*/
                /*-------------------------- (VPP,root)+(CC,coord)+ (NC,dep_coord)--------------------*/
            /*----------------------------(VPP,root)+(NC,obj)+(P,dep)+(NC,prep)+(P,dep)+(VInf,prep)+(P+D,a_obj)+(NC,prep)+(P,dep)+(NC,prep)-------------*/
            /*--------------------------(VPP,root)+(NC,obj)+(P,dep)+(NC,prep)+(P,dep)+(VInf,prep)+(P+D,a_obj)+(NC,prep)+(P,dep)+(CC,coord)+(P,dep_coord)+(NC,prep)------------*/

            /*--------------------------- (VPP,root)+(P+D,de_obj)+(NC,prep)+(P,dep)+(NC,prep)+(ADJ,mod)--------------------*/
            /*---------------------------- (VPP,root)+(P+D,de_obj)+(NC,prep)+(P,dep)+(NC,prep)----------------------*/
            /*----------------------------- (VPP,root)+(P+D,de_obj)+(NC,prep)+(P,dep)+(NC,prep)(CC,coord)+(NC,dep_coord)+(P,dep)+(NPP,prep)+(NPP,mod)-------------*/
            if (posTagSequence.get(i).getTag().toString().equalsIgnoreCase("VPP")
                    && parseConfiguration.getGoverningDependency(posTagSequence.get(i)).getLabel().equalsIgnoreCase("root")) {
                List<PosTaggedToken> pos_tagged_token = parseConfiguration.getDependents(posTagSequence.get(i));
                for (PosTaggedToken ptt : pos_tagged_token) {
                    if (ptt.getTag().toString().equalsIgnoreCase("NC")
                            && parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("obj")
                            && (ptt.getComment() == null || ptt.getComment().isEmpty())) {
                        List<PosTaggedToken> pos_tagged_token_test = parseConfiguration.getDependents(ptt);
                        for (PosTaggedToken ptt_test : pos_tagged_token_test) {
                            if (ptt_test.getTag().toString().equalsIgnoreCase("P")
                                    && parseConfiguration.getGoverningDependency(ptt_test).getLabel().equalsIgnoreCase("dep")) {
                                List<PosTaggedToken> pos_tagged_token_test1 = parseConfiguration.getDependents(ptt_test);
                                for (PosTaggedToken ptt_test1 : pos_tagged_token_test1) {
                                    if (ptt_test1.getTag().toString().equalsIgnoreCase("NC")
                                            && (parseConfiguration.getGoverningDependency(ptt_test1).getLabel().equalsIgnoreCase("prep"))
                                            && (ptt_test1.getComment() == null || ptt_test1.getComment().isEmpty())) {
                                        List<PosTaggedToken> pos_tagged_token_test2 = parseConfiguration.getDependents(ptt_test1);
                                        for (PosTaggedToken ptt_test2 : pos_tagged_token_test2) {
                                            if (ptt_test2.getTag().toString().equalsIgnoreCase("P")
                                                    && parseConfiguration.getGoverningDependency(ptt_test2).getLabel().equalsIgnoreCase("dep")) {
                                                List<PosTaggedToken> pos_tagged_token_test3 = parseConfiguration.getDependents(ptt_test2);
                                                for (PosTaggedToken ptt_test3 : pos_tagged_token_test3) {
                                                    if (ptt_test3.getTag().toString().equalsIgnoreCase("VInf")
                                                            && parseConfiguration.getGoverningDependency(ptt_test3).getLabel().equalsIgnoreCase("prep")) {
                                                        List<PosTaggedToken> pos_tagged_token_test4 = parseConfiguration.getDependents(ptt_test3);
                                                        String objectForm = ptt_test1.getToken().toString() + " " + ptt_test2.getToken().toString() + " " + ptt_test3.getToken().toString();
                                                        Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                                        String Relation = "(VPP,root)+(NC,obj)+(P,dep)+(NC,prep)+(P,dep)+(VInf,prep)";
                                                        SentenceRelationType SRT = SentenceRelationType.hasComponent;
                                                        SentenceRelationMethod SRM = SentenceRelationMethod.rules;
                                                        double confidence = 1;
                                                        constructRelations(objectForm, subject2, Relation, SRT, SRM, line, confidence);
                                                        for (PosTaggedToken ptt_test4 : pos_tagged_token_test4) {
                                                            if (ptt_test4.getTag().toString().equalsIgnoreCase("P+D")
                                                                    && parseConfiguration.getGoverningDependency(ptt_test4).getLabel().equalsIgnoreCase("a_obj")) {
                                                                List<PosTaggedToken> pos_tagged_token_test5 = parseConfiguration.getDependents(ptt_test4);
                                                                for (PosTaggedToken ptt_test5 : pos_tagged_token_test5) {
                                                                    if (ptt_test5.getTag().toString().equalsIgnoreCase("NC")
                                                                            && parseConfiguration.getGoverningDependency(ptt_test5).getLabel().equalsIgnoreCase("prep")
                                                                            && (ptt_test5.getComment() == null || ptt_test5.getComment().isEmpty())) {
                                                                        List<PosTaggedToken> pos_tagged_token_test6 = parseConfiguration.getDependents(ptt_test5);
                                                                        for (PosTaggedToken ptt_test6 : pos_tagged_token_test6) {
                                                                            if (ptt_test6.getTag().toString().equalsIgnoreCase("P")
                                                                                    && parseConfiguration.getGoverningDependency(ptt_test6).getLabel().equalsIgnoreCase("dep")) {
                                                                                List<PosTaggedToken> pos_tagged_token_test7 = parseConfiguration.getDependents(ptt_test6);
                                                                                for (PosTaggedToken ptt_test7 : pos_tagged_token_test7) {
                                                                                    if (ptt_test7.getTag().toString().equalsIgnoreCase("NC")
                                                                                            && parseConfiguration.getGoverningDependency(ptt_test7).getLabel().equalsIgnoreCase("prep")
                                                                                            && (ptt_test7.getComment() == null || ptt_test7.getComment().isEmpty())) {
                                                                                        boolean annotated1 = false;
                                                                                        List<PosTaggedToken> pos_tagged_token_child8 = parseConfiguration.getDependents(ptt_test7);
                                                                                        for (PosTaggedToken ptt_child8 : pos_tagged_token_child8) {
                                                                                            if (composedNames(parseConfiguration, ptt_child8) == true) {
                                                                                                annotated1=true;
                                                                                                 objectForm =ptt_test7.getToken().toString()+" "+ ptt_child8.getToken().toString();
                                                                                                 subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                                                                                 Relation = "(VPP,root) +(NC,obj)+(P,dep)+(NC,prep)+(P,dep)+(VINF,prep)+(P+D,a_obj)+(NC,prep)+(P,dep)+(NC,prep)+(ADJ,mod)";
                                                                                                 SRT = SentenceRelationType.hasComponent;
                                                                                                 SRM = SentenceRelationMethod.rules;
                                                                                                 confidence = 1;
                                                                                                constructRelations(objectForm, subject2, Relation, SRT, SRM, line, confidence);
                                                                                            }
                                                                                        }

                                                                                        if(!annotated1) {
                                                                                            objectForm = ptt_test5.getToken().toString() + " " + ptt_test6.getToken().toString() + " " + ptt_test7.getToken().toString();
                                                                                            subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                                                                            Relation = "(VPP,root)+(NC,obj)+(P,dep)+(NC,prep)+(P,dep)+(VInf,prep)+(P+D,a_obj)+(NC,prep)+(P,dep)+(NC,prep)";
                                                                                            SRT = SentenceRelationType.hasComponent;
                                                                                            SRM = SentenceRelationMethod.rules;
                                                                                            confidence = 1;
                                                                                            constructRelations(objectForm, subject2, Relation, SRT, SRM, line, confidence);
                                                                                        }
                                                                                    }
                                                                                    String old_relation="(VPP,root) +(NC,obj)+(P,dep)+(NC,prep)+(P,dep)+(VINF,prep)+(P+D,a_obj)+(NC,prep)+(P,dep)+(NC,prep)";
                                                                                    isCoordination(parseConfiguration, ptt_test7,posTagSequence, line,old_relation);
                                                                                 }
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (posTagSequence.get(i).getTag().toString().equalsIgnoreCase("VPP")
                    && parseConfiguration.getGoverningDependency(posTagSequence.get(i)).getLabel().equalsIgnoreCase("root")) {
                List<PosTaggedToken> pos_tagged_token = parseConfiguration.getDependents(posTagSequence.get(i));
                for (PosTaggedToken ptt : pos_tagged_token) {
                    if (ptt.getTag().toString().equalsIgnoreCase("P+D")
                            && parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("de_obj")) {
                        List<PosTaggedToken> pos_tagged_token_test = parseConfiguration.getDependents(ptt);
                        for (PosTaggedToken ptt_test : pos_tagged_token_test) {
                            if (ptt_test.getTag().toString().equalsIgnoreCase("NC")
                                    && parseConfiguration.getGoverningDependency(ptt_test).getLabel().equalsIgnoreCase("prep")
                                    && (ptt_test.getComment() == null || ptt_test.getComment().isEmpty())) {
                                List<PosTaggedToken> pos_tagged_token_test1 = parseConfiguration.getDependents(ptt_test);
                                for (PosTaggedToken ptt_test1 : pos_tagged_token_test1) {
                                    if (ptt_test1.getTag().toString().equalsIgnoreCase("P")
                                            && parseConfiguration.getGoverningDependency(ptt_test1).getLabel().equalsIgnoreCase("dep")) {
                                        List<PosTaggedToken> pos_tagged_token_test2 = parseConfiguration.getDependents(ptt_test1);
                                        for (PosTaggedToken ptt_test2 : pos_tagged_token_test2) {
                                            if (ptt_test2.getTag().toString().equalsIgnoreCase("NC")
                                                    && parseConfiguration.getGoverningDependency(ptt_test2).getLabel().equalsIgnoreCase("prep")
                                                    && (ptt_test2.getComment() == null || ptt_test.getComment().isEmpty())) {
                                                List<PosTaggedToken> pos_tagged_token_test3 = parseConfiguration.getDependents(ptt_test2);
                                                boolean annotated = false;
                                                for (PosTaggedToken ptt_test3 : pos_tagged_token_test3) {
                                                    if (composedNames(parseConfiguration, ptt_test3) == true) {
                                                        annotated = true;
                                                        String objectForm = ptt_test2.getToken().toString() + " " + ptt_test3.getToken().toString();
                                                        Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                                        String Relation = "(VPP,root)+(P+D,de_obj)+(NC,prep)+(P,dep)+(NC,prep)+(ADJ,mod)";
                                                        SentenceRelationType SRT = SentenceRelationType.hasComponent;
                                                        SentenceRelationMethod SRM = SentenceRelationMethod.rules;
                                                        double confidence = 1;
                                                        constructRelations(objectForm, subject2, Relation, SRT, SRM, line, confidence);
                                                    }
                                                    String old_relation="(VPP,root)+(P+D,de_obj)+(NC,prep)+(P,dep)+(NC,prep)";
                                                    isCoordination(parseConfiguration, ptt_test3,posTagSequence, line,old_relation);

                                                }
                                                if (!annotated) {
                                                    String objectForm = ptt_test2.getToken().toString();
                                                    Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                                    String Relation = "(VPP,root)+(P+D,de_obj)+(NC,prep)+(P,dep)+(NC,prep)";
                                                    SentenceRelationType SRT = SentenceRelationType.hasComponent;
                                                    SentenceRelationMethod SRM = SentenceRelationMethod.rules;
                                                    double confidence = 1;
                                                    constructRelations(objectForm, subject2, Relation, SRT, SRM, line, confidence);
                                                }

                                            }
                                        }
                                    }
                                }

                            }
                        }
                    }
                }
            }

            if (posTagSequence.get(i).getTag().toString().equalsIgnoreCase("VPP")
                    && parseConfiguration.getGoverningDependency(posTagSequence.get(i)).getLabel().equalsIgnoreCase("root")) {
                List<PosTaggedToken> pos_tagged_token = parseConfiguration.getDependents(posTagSequence.get(i));
                for (PosTaggedToken ptt : pos_tagged_token) {
                    String old_relation="(VPP,root)";
                    isCoordination(parseConfiguration, ptt,posTagSequence, line,old_relation);
                    if (ptt.getTag().toString().equalsIgnoreCase("P")
                            && parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("a_obj")) {
                        List<PosTaggedToken> pos_tagged_token_test = parseConfiguration.getDependents(ptt);
                        for (PosTaggedToken ptt_test : pos_tagged_token_test) {
                            if (ptt_test.getTag().toString().equalsIgnoreCase("VINF")
                                    && parseConfiguration.getGoverningDependency(ptt_test).getLabel().equalsIgnoreCase("prep")
                               ) {
                                List<PosTaggedToken> pos_tagged_token_test2 = parseConfiguration.getDependents(ptt_test);
                                for (PosTaggedToken ptt_test2 : pos_tagged_token_test2) {
                                    if (ptt_test2.getTag().toString().equalsIgnoreCase("P")
                                            && parseConfiguration.getGoverningDependency(ptt_test2).getLabel().equalsIgnoreCase("de_obj")
                                            ) {
                                        List<PosTaggedToken> pos_tagged_token_test3 = parseConfiguration.getDependents(ptt_test2);
                                        for (PosTaggedToken ptt_test3 : pos_tagged_token_test3) {
                                            if (ptt_test3.getTag().toString().equalsIgnoreCase("NC")
                                                    && parseConfiguration.getGoverningDependency(ptt_test3).getLabel().equalsIgnoreCase("prep")
                                                    ) {
                                                boolean annotated = false;
                                                List<PosTaggedToken> pos_tagged_token_test4 = parseConfiguration.getDependents(ptt_test3);
                                                for (PosTaggedToken ptt_test4 : pos_tagged_token_test4) {
                                                    if (composedNames(parseConfiguration, ptt_test4) == true) {
                                                        annotated = true;
                                                        String objectForm = ptt_test3.getToken().toString() + " " + ptt_test4.getToken().toString();
                                                        Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                                        String Relation = "(VPP,root)+(P,a_obj)+(VINF,prep)+(P,de_obj)+(NC,prep)+(ADJ,mod)";
                                                        SentenceRelationType SRT = SentenceRelationType.hasComponent;
                                                        SentenceRelationMethod SRM = SentenceRelationMethod.rules;
                                                        double confidence = 1;
                                                        constructRelations(objectForm, subject2, Relation, SRT, SRM, line, confidence);
                                                    }
                                                     old_relation="(VPP,root)+(P,a_obj)+(VINF,prep)+(P,de_obj)+(NC,prep)+(ADJ,mod))";
                                                    isCoordination(parseConfiguration, ptt_test3,posTagSequence, line,old_relation);
                                                }
                                                if (!annotated) {
                                                    String objectForm = ptt_test2.getToken().toString();
                                                    Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                                    String Relation = "(VPP,root)+(P,a_obj)+(VINF,prep)+(P,de_obj)+(NC,prep))";
                                                    SentenceRelationType SRT = SentenceRelationType.hasComponent;
                                                    SentenceRelationMethod SRM = SentenceRelationMethod.rules;
                                                    double confidence = 1;
                                                    constructRelations(objectForm, subject2, Relation, SRT, SRM, line, confidence);
                                                }

                                            }
                                            }
                                    }


                                    }

                            }
                            }
                    }

                }
            }

 /*---------------------------(VPP,root)+(P,mod)+(NPP,prep)+(NPP,mod)--------------------------------*/
            if (posTagSequence.get(i).getTag().toString().equalsIgnoreCase("VPP")
                    && parseConfiguration.getGoverningDependency(posTagSequence.get(i)).getLabel().equalsIgnoreCase("root")) {
                List<PosTaggedToken> pos_tagged_token = parseConfiguration.getDependents(posTagSequence.get(i));
                for (PosTaggedToken ptt : pos_tagged_token) {
                    if (ptt.getTag().toString().equalsIgnoreCase("P")
                            && parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("mod")) {
                        List<PosTaggedToken> pos_tagged_token_test = parseConfiguration.getDependents(ptt);
                        for (PosTaggedToken ptt_test : pos_tagged_token_test) {
                            if (ptt_test.getTag().toString().equalsIgnoreCase("NPP")
                                    && parseConfiguration.getGoverningDependency(ptt_test).getLabel().equalsIgnoreCase("prep")
                                    && (ptt_test.getComment() == null || ptt_test.getComment().isEmpty())) {
                                boolean annotated = false;
                                List<PosTaggedToken> pos_tagged_token_test1 = parseConfiguration.getDependents(ptt_test);
                                for (PosTaggedToken ptt_test1 : pos_tagged_token_test1) {
                                    if (composedNames(parseConfiguration, ptt_test1) == true) {
                                        annotated = true;
                                        String objectForm = ptt_test.getToken().toString() + " " + ptt_test1.getToken().toString();
                                        Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                        String Relation = "(VPP,root)+(P,mod)+(NPP,prep)+(ADJ,mod)";
                                        SentenceRelationType SRT = SentenceRelationType.hasComponent;
                                        SentenceRelationMethod SRM = SentenceRelationMethod.rules;
                                        double confidence = 1;
                                        constructRelations(objectForm, subject2, Relation, SRT, SRM, line, confidence);
                                    }
                                }
                                if (!annotated) {
                                    String objectForm = ptt_test.getToken().toString();
                                    Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                    String Relation = "(VPP,root)+(P,mod)+(NPP,prep)";
                                    SentenceRelationType SRT = SentenceRelationType.hasComponent;
                                    SentenceRelationMethod SRM = SentenceRelationMethod.rules;
                                    double confidence = 1;
                                    constructRelations(objectForm, subject2, Relation, SRT, SRM, line, confidence);
                                }
                            }


                        }

                    }
                }
            }
        }
    }



    @Override
    public void annotationData(List<SentenceRelation> list_result) throws IOException {

        Map<SentenceRelationId, List<SentenceRelationMethod>> relationMap = new HashMap<>();
        //construction de list_result contenant les méthodes appliquées pour la même sentence
        for (SentenceRelation sentence_relation : list_result) {
          //  xml_Model (sentence_relation);
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
        // dbpedia_namedEntity, rulesbelongsToBrandi
        for (SentenceRelationId sentenceRelationId : relationMap.keySet()) {
            List<SentenceRelationMethod> relationMethods = relationMap.get(sentenceRelationId);
            if (relationMethods.contains(SentenceRelationMethod.rules)) {
                System.out.println("Selected" + sentenceRelationId + "" + SentenceRelationMethod.rules);
                Model model = constructModel(sentenceRelationId);
                writeRdf(model);
            } else if (relationMethods.contains(SentenceRelationMethod.rules)) {
                System.out.println("Selected" + sentenceRelationId + "" + SentenceRelationMethod.rules);
                Model model = constructModel(sentenceRelationId);
                writeRdf(model);
            } else if (relationMethods.contains(SentenceRelationMethod.rules)) {
                System.out.println("Selected" + sentenceRelationId + "" + SentenceRelationMethod.rules);
                Model model = constructModel(sentenceRelationId);
                writeRdf(model);

            }
        }
    }

    @Override
    public void processExtraction(String line) throws Exception {
        Renco renco = new Renco();
        startTools(line, renco.rencoByWebService(line));
    }

    @Override
    public void init() throws Exception {

    }
}
