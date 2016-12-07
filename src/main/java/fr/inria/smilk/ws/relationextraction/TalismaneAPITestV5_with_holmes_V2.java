package fr.inria.smilk.ws.relationextraction;


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
public class TalismaneAPITestV5_with_holmes_V2 extends AbstractRelationExtraction {


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
                if(form.contains("(")){form = form.substring(0,form.indexOf("(")).trim();}
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
                                && (ptt_test1.getComment().equalsIgnoreCase("product") || ptt_test1.getComment().equalsIgnoreCase("brand")
                                || ptt_test1.getComment().equalsIgnoreCase("division") || ptt_test1.getComment().equalsIgnoreCase("range")
                                || ptt_test1.getComment().equalsIgnoreCase("group"))) {
                            subject2.setForm(ptt_test1.getToken().toString());
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
                                                                        && (ptt_test6.getComment().equalsIgnoreCase("product") || ptt_test6.getComment().equalsIgnoreCase("brand")
                                                                        || ptt_test6.getComment().equalsIgnoreCase("range") || ptt_test6.getComment().equalsIgnoreCase("division")
                                                                        || ptt_test6.getComment().equalsIgnoreCase("group"))) {
                                                                    subject2.setForm(ptt_test6.getToken().toString());

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
                                                                && (ptt_test6.getComment().equalsIgnoreCase("product") || ptt_test6.getComment().equalsIgnoreCase("brand")
                                                                || ptt_test6.getComment().equalsIgnoreCase("range") || ptt_test6.getComment().equalsIgnoreCase("division")
                                                                || ptt_test6.getComment().equalsIgnoreCase("group"))) {
                                                            subject2.setForm(ptt_test6.getToken().toString());
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
                                               &&(ptt_3.getComment().equalsIgnoreCase("product") || ptt_3.getComment().equalsIgnoreCase("brand")
                                                        || ptt_3.getComment().equalsIgnoreCase("range") || ptt_3.getComment().equalsIgnoreCase("division")
                                                        || ptt_3.getComment().equalsIgnoreCase("group"))) {
                                                    subject2.setForm(ptt_3.getToken().toString());
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
                                && (ptt.getComment().equalsIgnoreCase("product") || ptt.getComment().equalsIgnoreCase("brand")
                                || ptt.getComment().equalsIgnoreCase("range") || ptt.getComment().equalsIgnoreCase("division")
                                || ptt.getComment().equalsIgnoreCase("group"))) {

                            subject2.setForm(ptt.getToken().toString());
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
                                        && (ptt_test2.getComment().equalsIgnoreCase("product") || ptt_test2.getComment().equalsIgnoreCase("brand")
                                        || ptt_test2.getComment().equalsIgnoreCase("range") || ptt_test2.getComment().equalsIgnoreCase("division")
                                        || ptt_test2.getComment().equalsIgnoreCase("group"))) {
                                    subject2.setForm(ptt_test2.getToken().toString());
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
                                                            && (ptt_test4.getComment().equalsIgnoreCase("product") || ptt_test4.getComment().equalsIgnoreCase("brand")
                                                            || ptt_test4.getComment().equalsIgnoreCase("range") || ptt_test4.getComment().equalsIgnoreCase("division")
                                                            || ptt_test4.getComment().equalsIgnoreCase("group"))) {
                                                        subject2.setForm(ptt_test4.getToken().toString());
                                                    }

                                                    if (ptt_test4.getTag().toString().equalsIgnoreCase("P") && parseConfiguration.getGoverningDependency(ptt_test4).getLabel().equalsIgnoreCase("dep")
                                                            ) {
                                                        List<PosTaggedToken> pos_tagged_token_test_5 = parseConfiguration.getDependents(ptt_test4);
                                                        for (PosTaggedToken ptt_test5 : pos_tagged_token_test_5) {
                                                            if (ptt_test5.getTag().toString().equalsIgnoreCase("NPP") && parseConfiguration.getGoverningDependency(ptt_test5).getLabel().equalsIgnoreCase("prep")
                                                                    && (ptt_test5.getComment().equalsIgnoreCase("product") || ptt_test5.getComment().equalsIgnoreCase("brand")
                                                                    || ptt_test5.getComment().equalsIgnoreCase("range") || ptt_test5.getComment().equalsIgnoreCase("division")
                                                                    || ptt_test5.getComment().equalsIgnoreCase("group"))) {

                                                                subject2.setForm(ptt_test5.getToken().toString());
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
                                && (ptt.getComment().equalsIgnoreCase("product") || ptt.getComment().equalsIgnoreCase("brand")
                                || ptt.getComment().equalsIgnoreCase("range") || ptt.getComment().equalsIgnoreCase("division")
                                || ptt.getComment().equalsIgnoreCase("group"))) {
                            subject2.setForm(ptt.getToken().toString());
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
                                        && (!ptt.getComment().equalsIgnoreCase("product") || !ptt.getComment().equalsIgnoreCase("brand")
                                        || !ptt.getComment().equalsIgnoreCase("range") || !ptt.getComment().equalsIgnoreCase("division")
                                        || !ptt.getComment().equalsIgnoreCase("group"))) {
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
                                                                                && (ptt_test6.getComment().equalsIgnoreCase("product") || ptt_test6.getComment().equalsIgnoreCase("brand")
                                                                                || ptt_test6.getComment().equalsIgnoreCase("range") || ptt_test6.getComment().equalsIgnoreCase("division")
                                                                                || ptt_test6.getComment().equalsIgnoreCase("group"))) {
                                                                            subject2.setForm(ptt_test6.getToken().toString());
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
                                                                                                && (ptt_test8.getComment().equalsIgnoreCase("product") || ptt_test8.getComment().equalsIgnoreCase("brand")
                                                                                                || ptt_test8.getComment().equalsIgnoreCase("range") || ptt_test6.getComment().equalsIgnoreCase("division")
                                                                                                || ptt_test6.getComment().equalsIgnoreCase("group"))) {
                                                                                            subject2.setForm(ptt_test8.getToken().toString());
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
                                                && (ptt_test3.getComment().equalsIgnoreCase("product") || ptt_test3.getComment().equalsIgnoreCase("brand")
                                                || ptt_test3.getComment().equalsIgnoreCase("range") || ptt_test3.getComment().equalsIgnoreCase("division")
                                                || ptt_test3.getComment().equalsIgnoreCase("group"))) {
                                            subject2.setForm(ptt_test3.getToken().toString());
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
                                                                && (ptt_test5.getComment().equalsIgnoreCase("product") || ptt_test5.getComment().equalsIgnoreCase("brand")
                                                                || ptt_test5.getComment().equalsIgnoreCase("range") || ptt_test5.getComment().equalsIgnoreCase("division")
                                                                || ptt_test5.getComment().equalsIgnoreCase("group"))) {
                                                            subject2.setForm(ptt_test5.getToken().toString());
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
                                                && (ptt_test2.getComment().equalsIgnoreCase("product") || ptt_test2.getComment().equalsIgnoreCase("brand")
                                                || ptt_test2.getComment().equalsIgnoreCase("range") || ptt_test2.getComment().equalsIgnoreCase("division")
                                                || ptt_test2.getComment().equalsIgnoreCase("group"))) {
                                            subject2.setForm(ptt_test2.getToken().toString());
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
                                                && (ptt_test3.getComment().equalsIgnoreCase("product") || ptt_test3.getComment().equalsIgnoreCase("brand")
                                                || ptt_test3.getComment().equalsIgnoreCase("range") || ptt_test3.getComment().equalsIgnoreCase("division")
                                                || ptt_test3.getComment().equalsIgnoreCase("group"))) {
                                            subject2.setForm(ptt_test3.getToken().toString());
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
                                                        &&( ptt_test4.getComment().equalsIgnoreCase("product") || ptt_test4.getComment().equalsIgnoreCase("brand")
                                                        || ptt_test4.getComment().equalsIgnoreCase("range") || ptt_test4.getComment().equalsIgnoreCase("division")
                                                        || ptt_test4.getComment().equalsIgnoreCase("group"))) {
                                                    subject2.setForm(ptt_test4.getToken().toString());
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
                    if (ptt.getTag().toString().equalsIgnoreCase("P") && parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("mod") ) {
                        List<PosTaggedToken> pos_tagged_token_test_2 = parseConfiguration.getDependents(ptt);
                        for (PosTaggedToken ptt_test2 : pos_tagged_token_test_2) {
                            if ((ptt_test2.getTag().toString().equalsIgnoreCase("NPP") || ptt_test2.getTag().toString().equalsIgnoreCase("NC"))
                                    && parseConfiguration.getGoverningDependency(ptt_test2).getLabel().equalsIgnoreCase("prep")
                                    &&(ptt_test2.getComment().equalsIgnoreCase("product") || ptt_test2.getComment().equalsIgnoreCase("brand")
                                    || ptt_test2.getComment().equalsIgnoreCase("range") || ptt_test2.getComment().equalsIgnoreCase("division")
                                    || ptt_test2.getComment().equalsIgnoreCase("group"))) {
                                subject2.setForm(ptt_test2.getToken().toString());
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
                            && (ptt.getComment().equalsIgnoreCase("product") || ptt.getComment().equalsIgnoreCase("brand")
                            || ptt.getComment().equalsIgnoreCase("range") || ptt.getComment().equalsIgnoreCase("division")
                            || ptt.getComment().equalsIgnoreCase("group"))) {
                        subject2.setForm(ptt.getToken().toString());
                    }
                }
            }

            //(VPP,mod) +(NPP,obj) + (V,mod_rel) +(NC,obj) + (P,dep) + (NPP,prep)
            if (posTagSequence.get(i).getTag().toString().equalsIgnoreCase("VPP") && parseConfiguration.getGoverningDependency(posTagSequence.get(i)).getLabel().equalsIgnoreCase("mod")) {
                List<PosTaggedToken> pos_tagged_token = parseConfiguration.getDependents(posTagSequence.get(i));
                for (PosTaggedToken ptt : pos_tagged_token) {
                    if (ptt.getTag().toString().equalsIgnoreCase("NPP") && parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("obj") ) {
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
                                                            && (ptt_child3.getComment().equalsIgnoreCase("product") || ptt_child3.getComment().equalsIgnoreCase("brand")
                                                            || ptt_child3.getComment().equalsIgnoreCase("range") || ptt_child3.getComment().equalsIgnoreCase("division")
                                                            || ptt_child3.getComment().equalsIgnoreCase("group"))) {
                                                        subject2.setForm(ptt_child3.getToken().toString());
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
                            && (ptt.getComment().equalsIgnoreCase("product") || ptt.getComment().equalsIgnoreCase("brand")
                            || ptt.getComment().equalsIgnoreCase("range") || ptt.getComment().equalsIgnoreCase("division")
                            || ptt.getComment().equalsIgnoreCase("group"))) {

                        subject2.setForm(ptt.getToken().toString());
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
                                    && (ptt_child.getComment().equalsIgnoreCase("product") || ptt_child.getComment().equalsIgnoreCase("brand")
                                    || ptt_child.getComment().equalsIgnoreCase("range") || ptt_child.getComment().equalsIgnoreCase("division")
                                    || ptt_child.getComment().equalsIgnoreCase("group"))) {

                                subject2.setForm(ptt_child.getToken().toString());
                            }
                        }
                    }
                }
            }


        }

        return subject2;

    }

    public void relationRules(ParseConfiguration parseConfiguration, PosTagSequence posTagSequence, String line/*,  Token subject*/) {

        for (int i = 0; i < posTagSequence.size(); i++) {
                /*******************************Extraction de hasAmbassador Relation (product/brand/range/division/group--> Ambassador) *****************************/
                /*-----------(VINF,prep) + (NC,obj) + (NPP,mod)------------*/
                if (posTagSequence.get(i).getTag().toString().equalsIgnoreCase("VINF") && parseConfiguration.getGoverningDependency(posTagSequence.get(i)).getLabel().equalsIgnoreCase("prep")) {
                    List<PosTaggedToken> pos_tagged_token = parseConfiguration.getDependents(posTagSequence.get(i));
                    for (PosTaggedToken ptt : pos_tagged_token) {
                        if (ptt.getTag().toString().equalsIgnoreCase("NC") && parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("obj")) {
                            List<PosTaggedToken> pos_tagged_token_test_2 = parseConfiguration.getDependents(ptt);
                            for (PosTaggedToken ptt_test2 : pos_tagged_token_test_2) {
                                if (ptt_test2.getTag().toString().equalsIgnoreCase("NPP") && parseConfiguration.getGoverningDependency(ptt_test2).getLabel().equalsIgnoreCase("mod")
                                        && ptt_test2.getComment().equalsIgnoreCase("PER")) {
                                    Token objectToken = new Token();
                                    Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                    objectToken.setForm(ptt_test2.getToken().toString());
                                    SentenceRelation sentenceRelation = new SentenceRelation();
                                    SentenceRelationId sentenceRelationId = new SentenceRelationId();
                                    sentenceRelationId.setSubject(subject2);
                                    sentenceRelationId.setObject(objectToken);
                                    sentenceRelationId.setRelation("(VINF,prep) + (NC,obj) + (NPP,mod)");
                                    sentenceRelationId.setSentence_text(line);
                                    sentenceRelationId.setType(SentenceRelationType.hasRepresentative);
                                    sentenceRelationId.setConfidence(1);
                                    sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                    sentenceRelation.setMethod(SentenceRelationMethod.rules);
                                    list_result.add(sentenceRelation);
                                }
                            }
                        }
                    }
                }

                /*---------------------(V,root)+(P+D,mod)+(NC,prep)+(P,dep)+ (NPP,prep)+(NPP,mod)------------------*/
                /* ---------------------(V,root)+(P+D,mod)+(NC,prep)+(P,dep)+ (NPP,prep)+(cc,coord)+(NPP,dep_coord)+(NPP,mod)-------------------------------*/
                if (posTagSequence.get(i).getTag().toString().equalsIgnoreCase("V") && parseConfiguration.getGoverningDependency(posTagSequence.get(i)).getLabel().equalsIgnoreCase("root")) {
                    List<PosTaggedToken> pos_tagged_token = parseConfiguration.getDependents(posTagSequence.get(i));
                    for (PosTaggedToken ptt : pos_tagged_token) {
                        if (ptt.getTag().toString().equalsIgnoreCase("P+D") && parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("mod")) {
                            List<PosTaggedToken> pos_tagged_token_test_2 = parseConfiguration.getDependents(ptt);
                            for (PosTaggedToken ptt_test2 : pos_tagged_token_test_2) {
                                if (ptt_test2.getTag().toString().equalsIgnoreCase("NC") && parseConfiguration.getGoverningDependency(ptt_test2).getLabel().equalsIgnoreCase("prep")
                                        && (ptt_test2.getComment()==null||ptt_test2.getComment().isEmpty())) {
                                    List<PosTaggedToken> pos_tagged_token_test_3 = parseConfiguration.getDependents(ptt_test2);
                                    for (PosTaggedToken ptt_test3 : pos_tagged_token_test_3) {
                                        if (ptt_test3.getTag().toString().equalsIgnoreCase("P") && parseConfiguration.getGoverningDependency(ptt_test3).getLabel().equalsIgnoreCase("dep")) {
                                            List<PosTaggedToken> pos_tagged_token_test_4 = parseConfiguration.getDependents(ptt_test3);
                                            for (PosTaggedToken ptt_test4 : pos_tagged_token_test_4) {
                                                if (ptt_test4.getTag().toString().equalsIgnoreCase("NPP") && parseConfiguration.getGoverningDependency(ptt_test4).getLabel().equalsIgnoreCase("prep")
                                                        && ptt_test4.getComment().equalsIgnoreCase("PER")) {
                                                    Token objectToken = new Token();
                                                    Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                                    objectToken.setForm(ptt_test4.getToken().toString());
                                                    SentenceRelation sentenceRelation = new SentenceRelation();
                                                    SentenceRelationId sentenceRelationId = new SentenceRelationId();
                                                    sentenceRelationId.setSubject(subject2);
                                                    sentenceRelationId.setObject(objectToken);
                                                    sentenceRelationId.setRelation("(V,root)+(P+D,mod)+(NC,prep)+(P,dep)+ (NPP,prep)+(NPP,mod)");
                                                    sentenceRelationId.setSentence_text(line);
                                                    sentenceRelationId.setType(SentenceRelationType.hasRepresentative);
                                                    sentenceRelationId.setConfidence(1);
                                                    sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                                    sentenceRelation.setMethod(SentenceRelationMethod.rules);
                                                    list_result.add(sentenceRelation);

                                                    List<PosTaggedToken> pos_tagged_token_test_5 = parseConfiguration.getDependents(ptt_test4);
                                                    for (PosTaggedToken ptt_test5 : pos_tagged_token_test_5) {
                                                        if (ptt_test5.getTag().toString().equalsIgnoreCase("CC") && parseConfiguration.getGoverningDependency(ptt_test5).getLabel().equalsIgnoreCase("coord")
                                                                ) {
                                                            List<PosTaggedToken> pos_tagged_token_test_6 = parseConfiguration.getDependents(ptt_test5);

                                                            for (PosTaggedToken ptt_test6 : pos_tagged_token_test_6) {
                                                                if (ptt_test6.getTag().toString().equalsIgnoreCase("NPP") && parseConfiguration.getGoverningDependency(ptt_test6).getLabel().equalsIgnoreCase("dep_coord")
                                                                        && ptt_test6.getComment().equalsIgnoreCase("PER")) {
                                                                             objectToken = new Token();
                                                                             subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                                                            objectToken.setForm(ptt_test6.getToken().toString());
                                                                             sentenceRelation = new SentenceRelation();
                                                                             sentenceRelationId = new SentenceRelationId();
                                                                            sentenceRelationId.setSubject(subject2);
                                                                            sentenceRelationId.setObject(objectToken);
                                                                            sentenceRelationId.setRelation("(V,root)+(P+D,mod)+(NC,prep)+(P,dep)+ (NPP,prep)+(cc,coord)+(NPP,dep_coord)+(NPP,mod)");
                                                                            sentenceRelationId.setSentence_text(line);
                                                                            sentenceRelationId.setType(SentenceRelationType.hasRepresentative);
                                                                            sentenceRelationId.setConfidence(1);
                                                                            sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                                                            sentenceRelation.setMethod(SentenceRelationMethod.rules);
                                                                            list_result.add(sentenceRelation);


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

                /*--------------------------(V,root)+(P,mod)+(NPP,prep)-------------------------*/
                if (posTagSequence.get(i).getTag().toString().equalsIgnoreCase("V") && parseConfiguration.getGoverningDependency(posTagSequence.get(i)).getLabel().equalsIgnoreCase("root")) {
                    List<PosTaggedToken> pos_tagged_token = parseConfiguration.getDependents(posTagSequence.get(i));
                    for (PosTaggedToken ptt : pos_tagged_token) {
                        if (ptt.getTag().toString().equalsIgnoreCase("P") && parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("mod")) {
                            List<PosTaggedToken> pos_tagged_token_test_2 = parseConfiguration.getDependents(ptt);
                            for (PosTaggedToken ptt_test2 : pos_tagged_token_test_2) {
                                if (ptt_test2.getTag().toString().equalsIgnoreCase("NPP") && parseConfiguration.getGoverningDependency(ptt_test2).getLabel().equalsIgnoreCase("prep")
                                        && ptt_test2.getComment().equalsIgnoreCase("PER")) {
                                            Token objectToken = new Token();
                                            Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                            objectToken.setForm(ptt_test2.getToken().toString());
                                            SentenceRelation sentenceRelation = new SentenceRelation();
                                            SentenceRelationId sentenceRelationId = new SentenceRelationId();
                                            sentenceRelationId.setSubject(subject2);
                                            sentenceRelationId.setObject(objectToken);
                                            sentenceRelationId.setRelation("(V,root)+(P,mod)+(NPP,prep)");
                                            sentenceRelationId.setSentence_text(line);
                                            sentenceRelationId.setType(SentenceRelationType.hasRepresentative);
                                            sentenceRelationId.setConfidence(1);
                                            sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                            sentenceRelation.setMethod(SentenceRelationMethod.rules);
                                            list_result.add(sentenceRelation);
                                }
                            }
                        }
                    }
                }

            /*--------------------------(V,root)+(P,mod)+(NPP,prep)-------------------------*/
            if (posTagSequence.get(i).getTag().toString().equalsIgnoreCase("V") && parseConfiguration.getGoverningDependency(posTagSequence.get(i)).getLabel().equalsIgnoreCase("root")) {
                List<PosTaggedToken> pos_tagged_token = parseConfiguration.getDependents(posTagSequence.get(i));
                for (PosTaggedToken ptt : pos_tagged_token) {
                    if (ptt.getTag().toString().equalsIgnoreCase("P") && parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("mod")) {
                        List<PosTaggedToken> pos_tagged_token_test_2 = parseConfiguration.getDependents(ptt);
                        for (PosTaggedToken ptt_test2 : pos_tagged_token_test_2) {
                            if (ptt_test2.getTag().toString().equalsIgnoreCase("NPP") && parseConfiguration.getGoverningDependency(ptt_test2).getLabel().equalsIgnoreCase("prep")) {
                                List<PosTaggedToken> pos_tagged_token_test_3 = parseConfiguration.getDependents(ptt_test2);
                                for (PosTaggedToken ptt_test3 : pos_tagged_token_test_3) {
                                    if (ptt_test3.getTag().toString().equalsIgnoreCase("NPP") && parseConfiguration.getGoverningDependency(ptt_test3).getLabel().equalsIgnoreCase("coord")
                                            && ptt_test3.getComment().toString().equalsIgnoreCase("PER")) {
                                        Token objectToken = new Token();
                                        Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                        objectToken.setForm(ptt_test3.getToken().toString());
                                        SentenceRelation sentenceRelation = new SentenceRelation();
                                        SentenceRelationId sentenceRelationId = new SentenceRelationId();
                                        sentenceRelationId.setSubject(subject2);
                                        sentenceRelationId.setObject(objectToken);
                                        sentenceRelationId.setRelation("(V,root)+(P,mod)+(NPP,prep)");
                                        sentenceRelationId.setSentence_text(line);
                                        sentenceRelationId.setType(SentenceRelationType.hasRepresentative);
                                        sentenceRelationId.setConfidence(1);
                                        sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                        sentenceRelation.setMethod(SentenceRelationMethod.rules);
                                        list_result.add(sentenceRelation);
                                        List<PosTaggedToken> pos_tagged_token_test_4 = parseConfiguration.getDependents(ptt_test3);
                                        for (PosTaggedToken ptt_test4 : pos_tagged_token_test_4) {
                                            if (ptt_test4.getTag().toString().equalsIgnoreCase("NPP") && parseConfiguration.getGoverningDependency(ptt_test4).getLabel().equalsIgnoreCase("coord")) {
                                                 objectToken = new Token();
                                                 subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                                objectToken.setForm(ptt_test4.getToken().toString());
                                                 sentenceRelation = new SentenceRelation();
                                                 sentenceRelationId = new SentenceRelationId();
                                                sentenceRelationId.setSubject(subject2);
                                                sentenceRelationId.setObject(objectToken);
                                                sentenceRelationId.setRelation("(V,root)+(P,mod)+(NPP,prep)");
                                                sentenceRelationId.setSentence_text(line);
                                                sentenceRelationId.setType(SentenceRelationType.hasRepresentative);
                                                sentenceRelationId.setConfidence(1);
                                                sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                                sentenceRelation.setMethod(SentenceRelationMethod.rules);
                                                list_result.add(sentenceRelation);

                                                List<PosTaggedToken> pos_tagged_token_test_5 = parseConfiguration.getDependents(ptt_test4);
                                                for (PosTaggedToken ptt_test5 : pos_tagged_token_test_5) {
                                                    if (ptt_test5.getTag().toString().equalsIgnoreCase("CC") && parseConfiguration.getGoverningDependency(ptt_test5).getLabel().equalsIgnoreCase("coord")) {
                                                        List<PosTaggedToken> pos_tagged_token_test_6 = parseConfiguration.getDependents(ptt_test5);
                                                        for (PosTaggedToken ptt_test6 : pos_tagged_token_test_6) {
                                                            if (ptt_test6.getTag().toString().equalsIgnoreCase("NPP") && parseConfiguration.getGoverningDependency(ptt_test6).getLabel().equalsIgnoreCase("dep_coord")) {
                                                                objectToken = new Token();
                                                                subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                                                objectToken.setForm(ptt_test6.getToken().toString());
                                                                sentenceRelation = new SentenceRelation();
                                                                sentenceRelationId = new SentenceRelationId();
                                                                sentenceRelationId.setSubject(subject2);
                                                                sentenceRelationId.setObject(objectToken);
                                                                sentenceRelationId.setRelation("(V,root)+(P,mod)+(NPP,prep)");
                                                                sentenceRelationId.setSentence_text(line);
                                                                sentenceRelationId.setType(SentenceRelationType.hasRepresentative);
                                                                sentenceRelationId.setConfidence(1);
                                                                sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                                                sentenceRelation.setMethod(SentenceRelationMethod.rules);
                                                                list_result.add(sentenceRelation);
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
            /*--------------------------(VPP,root)+(P,mod)+(NPP,prep)-------------------------*/
            if (posTagSequence.get(i).getTag().toString().equalsIgnoreCase("VPP") && parseConfiguration.getGoverningDependency(posTagSequence.get(i)).getLabel().equalsIgnoreCase("root")) {
                List<PosTaggedToken> pos_tagged_token = parseConfiguration.getDependents(posTagSequence.get(i));
                System.out.println("search hasAmbassador");
                for (PosTaggedToken ptt : pos_tagged_token) {
                    if (ptt.getTag().toString().equalsIgnoreCase("P") && parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("mod")) {
                        List<PosTaggedToken> pos_tagged_token_test_2 = parseConfiguration.getDependents(ptt);
                        for (PosTaggedToken ptt_test2 : pos_tagged_token_test_2) {
                            if (ptt_test2.getTag().toString().equalsIgnoreCase("NPP") && parseConfiguration.getGoverningDependency(ptt_test2).getLabel().equalsIgnoreCase("prep")
                                    && ptt_test2.getComment().equalsIgnoreCase("PER")) {
                                Token objectToken = new Token();
                                Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                objectToken.setForm(ptt_test2.getToken().toString());
                                SentenceRelation sentenceRelation = new SentenceRelation();
                                SentenceRelationId sentenceRelationId = new SentenceRelationId();
                                sentenceRelationId.setSubject(subject2);
                                sentenceRelationId.setObject(objectToken);
                                sentenceRelationId.setRelation("(VPP,root)+(P,mod)+(NPP,prep)");
                                sentenceRelationId.setSentence_text(line);
                                sentenceRelationId.setType(SentenceRelationType.hasRepresentative);
                                sentenceRelationId.setConfidence(1);
                                sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                sentenceRelation.setMethod(SentenceRelationMethod.rules);
                                list_result.add(sentenceRelation);
                            }
                        }
                    }
                }
            }


                /***********************(V,root) +(NC,suj)+(NPP,mod) (NPP,mod)*******************/
                if (posTagSequence.get(i).getTag().toString().equalsIgnoreCase("V") && parseConfiguration.getGoverningDependency(posTagSequence.get(i)).getLabel().equalsIgnoreCase("root")) {
                    List<PosTaggedToken> pos_tagged_token = parseConfiguration.getDependents(posTagSequence.get(i));
                    for (PosTaggedToken ptt : pos_tagged_token) {
                        if (ptt.getTag().toString().equalsIgnoreCase("NC") && parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("suj")) {
                            List<PosTaggedToken> pos_tagged_token_child = parseConfiguration.getDependents(ptt);
                            for (PosTaggedToken ptt_child : pos_tagged_token_child) {
                                if (ptt_child.getTag().toString().equalsIgnoreCase("NPP")
                                        && parseConfiguration.getGoverningDependency(ptt_child).getLabel().equalsIgnoreCase("mod")
                                        && (ptt_child.getComment().equalsIgnoreCase("PER")|| ptt_child.getComment().equalsIgnoreCase("person"))) {
                                    Token objectToken = new Token();
                                    Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                    objectToken.setForm(ptt_child.getToken().toString());
                                    SentenceRelation sentenceRelation = new SentenceRelation();
                                    SentenceRelationId sentenceRelationId = new SentenceRelationId();
                                    sentenceRelationId.setSubject(subject2);
                                    sentenceRelationId.setObject(objectToken);
                                    sentenceRelationId.setRelation("(V,root) +(NC,suj)+(NPP,mod)");
                                    sentenceRelationId.setSentence_text(line);
                                    sentenceRelationId.setType(SentenceRelationType.hasRepresentative);
                                    sentenceRelationId.setConfidence(1);
                                    sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                    sentenceRelation.setMethod(SentenceRelationMethod.rules);
                                    list_result.add(sentenceRelation);
                                }
                            }
                        }
                    }
                }


                /*----------------------------(V,root) +(NPP,suj)+((NPP||NC),mod)---------------------*/
                if (posTagSequence.get(i).getTag().toString().equalsIgnoreCase("V") && parseConfiguration.getGoverningDependency(posTagSequence.get(i)).getLabel().equalsIgnoreCase("root")) {
                    List<PosTaggedToken> pos_tagged_token = parseConfiguration.getDependents(posTagSequence.get(i));
                    for (PosTaggedToken ptt : pos_tagged_token) {
                        if (ptt.getTag().toString().equalsIgnoreCase("NPP") && parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("suj")
                                && ptt.getComment().equalsIgnoreCase("PER")) {
                                    Token objectToken = new Token();
                                    Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                    objectToken.setForm(ptt.getToken().toString());
                                    SentenceRelation sentenceRelation = new SentenceRelation();
                                    SentenceRelationId sentenceRelationId = new SentenceRelationId();
                                    sentenceRelationId.setSubject(subject2);
                                    sentenceRelationId.setObject(objectToken);
                                    sentenceRelationId.setRelation("(V,root) +(NPP,suj)");
                                    sentenceRelationId.setSentence_text(line);
                                    sentenceRelationId.setType(SentenceRelationType.hasRepresentative);
                                    sentenceRelationId.setConfidence(1);
                                    sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                    sentenceRelation.setMethod(SentenceRelationMethod.rules);
                                    list_result.add(sentenceRelation);
                        }
                    }
                }

            if (posTagSequence.get(i).getTag().toString().equalsIgnoreCase("V") && parseConfiguration.getGoverningDependency(posTagSequence.get(i)).getLabel().equalsIgnoreCase("root")) {
                List<PosTaggedToken> pos_tagged_token = parseConfiguration.getDependents(posTagSequence.get(i));
                for (PosTaggedToken ptt : pos_tagged_token) {
                    if ((ptt.getTag().toString().equalsIgnoreCase("NC")
                            && (parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("obj"))
                          )) {
                        List<PosTaggedToken> pos_tagged_token2 = parseConfiguration.getDependents(ptt);
                        for (PosTaggedToken ptt2 : pos_tagged_token2) {
                            if ((ptt2.getTag().toString().equalsIgnoreCase("NPP")
                                    && (parseConfiguration.getGoverningDependency(ptt2).getLabel().equalsIgnoreCase("mod")
                            && ptt2.getComment().equalsIgnoreCase("PER"))
                            )) {
                                Token objectToken = new Token();
                                Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                objectToken.setForm(ptt2.getToken().toString());
                                SentenceRelation sentenceRelation = new SentenceRelation();
                                SentenceRelationId sentenceRelationId = new SentenceRelationId();
                                sentenceRelationId.setSubject(subject2);
                                sentenceRelationId.setObject(objectToken);
                                sentenceRelationId.setRelation("(V,root)+(NPP,obj)+(NPP,mod)");
                                sentenceRelationId.setSentence_text(line);
                                sentenceRelationId.setType(SentenceRelationType.hasRepresentative);
                                sentenceRelationId.setConfidence(1);
                                sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                sentenceRelation.setMethod(SentenceRelationMethod.rules);
                                list_result.add(sentenceRelation);
                            }
                            }


                    }

                }
            }
                /*---------------------------------(V,root)+(NPP,obj)+(NPP,mod)--------------------------------*/
                 /*---------------------------------(V,root)+(NPP,obj)+(NPP,coord)+(NPP,mod)--------------------------------*/
                 /*---------------------------------(V,root)+(NPP,obj)+(NPP,coord)+(CC,coord)+(V_dep_coord)+(NPP,obj)--------------------------------*/
                if (posTagSequence.get(i).getTag().toString().equalsIgnoreCase("V") && parseConfiguration.getGoverningDependency(posTagSequence.get(i)).getLabel().equalsIgnoreCase("root")) {
                    List<PosTaggedToken> pos_tagged_token = parseConfiguration.getDependents(posTagSequence.get(i));
                    for (PosTaggedToken ptt : pos_tagged_token) {
                if ((ptt.getTag().toString().equalsIgnoreCase("NPP")
                        && (parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("obj")||parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("ats"))
                        && ptt.getComment().equalsIgnoreCase("PER"))) {
                    boolean annotated = false;
                    List<PosTaggedToken> pos_tagged_token_child = parseConfiguration.getDependents(ptt);
                    Token objectToken = new Token();
                    Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                    objectToken.setForm(ptt.getToken().toString());
                    SentenceRelation sentenceRelation = new SentenceRelation();
                    SentenceRelationId sentenceRelationId = new SentenceRelationId();
                    sentenceRelationId.setSubject(subject2);
                    sentenceRelationId.setObject(objectToken);
                    sentenceRelationId.setRelation("(V,root)+(NPP,obj)+(NPP,mod)");
                    sentenceRelationId.setSentence_text(line);
                    sentenceRelationId.setType(SentenceRelationType.hasRepresentative);
                    sentenceRelationId.setConfidence(1);
                    sentenceRelation.setSentenceRelationId(sentenceRelationId);
                    sentenceRelation.setMethod(SentenceRelationMethod.rules);
                    list_result.add(sentenceRelation);
                    for (PosTaggedToken ptt_child : pos_tagged_token_child) {
                        if ((ptt_child.getTag().toString().equalsIgnoreCase("NPP")
                                && (parseConfiguration.getGoverningDependency(ptt_child).getLabel().equalsIgnoreCase("coord")
                                && ptt_child.getComment().equalsIgnoreCase("PER")))) {
                                     List<PosTaggedToken> pos_tagged_token_child2 = parseConfiguration.getDependents(ptt_child);
                                     objectToken = new Token();
                                     subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                    objectToken.setForm(ptt_child.getToken().toString());
                                     sentenceRelation = new SentenceRelation();
                                     sentenceRelationId = new SentenceRelationId();
                                    sentenceRelationId.setSubject(subject2);
                                    sentenceRelationId.setObject(objectToken);
                                    sentenceRelationId.setRelation("(V,root)+(NPP,obj)+(NPP,coord)+(NPP,mod)");
                                    sentenceRelationId.setSentence_text(line);
                                    sentenceRelationId.setType(SentenceRelationType.hasRepresentative);
                                    sentenceRelationId.setConfidence(1);
                                    sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                    sentenceRelation.setMethod(SentenceRelationMethod.rules);
                                    list_result.add(sentenceRelation);

                            for (PosTaggedToken ptt_child2 : pos_tagged_token_child2) {
                                if ((ptt_child2.getTag().toString().equalsIgnoreCase("CC")
                                        && (parseConfiguration.getGoverningDependency(ptt_child2).getLabel().equalsIgnoreCase("coord")))) {
                                    List<PosTaggedToken> pos_tagged_token_child3 = parseConfiguration.getDependents(ptt_child2);
                                    for (PosTaggedToken ptt_child3 : pos_tagged_token_child3) {
                                        if ((ptt_child3.getTag().toString().equalsIgnoreCase("NPP")
                                                && (parseConfiguration.getGoverningDependency(ptt_child3).getLabel().equalsIgnoreCase("dep_coord")
                                                && ptt_child3.getComment().equalsIgnoreCase("PER")))) {
                                            Token objectToken2 = new Token();
                                            Token subject3 = extractSubject2(parseConfiguration, posTagSequence, line);
                                            objectToken2.setForm(ptt_child3.getToken().toString());
                                            SentenceRelation sentenceRelation2 = new SentenceRelation();
                                            SentenceRelationId sentenceRelationId2 = new SentenceRelationId();
                                            sentenceRelationId2.setSubject(subject3);
                                            sentenceRelationId2.setObject(objectToken2);
                                            sentenceRelationId2.setRelation("(V,root)+(NPP,obj)+(NPP,coord)+(CC,coord)+(V,dep_coord)+(NPP,obj)");
                                            sentenceRelationId2.setSentence_text(line);
                                            sentenceRelationId2.setType(SentenceRelationType.hasRepresentative);
                                            sentenceRelationId2.setConfidence(1);
                                            sentenceRelation2.setSentenceRelationId(sentenceRelationId2);
                                            sentenceRelation2.setMethod(SentenceRelationMethod.rules);
                                            list_result.add(sentenceRelation2);


                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

                /*--------------------------------(VPP,(mod||dep))+(NPP,obj)+((NC||NPP),mod)--------------------------------*/
                if (posTagSequence.get(i).getTag().toString().equalsIgnoreCase("VPP") && (parseConfiguration.getGoverningDependency(posTagSequence.get(i)).getLabel().equalsIgnoreCase("mod")
                        || parseConfiguration.getGoverningDependency(posTagSequence.get(i)).getLabel().equalsIgnoreCase("dep"))) {
                    List<PosTaggedToken> pos_tagged_token = parseConfiguration.getDependents(posTagSequence.get(i));
                    for (PosTaggedToken ptt : pos_tagged_token) {
                        if (ptt.getTag().toString().equalsIgnoreCase("NPP") && parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("obj")
                                && ptt.getComment().equalsIgnoreCase("PER")) {
                                    Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                    SentenceRelation sentenceRelation = new SentenceRelation();
                                    SentenceRelationId sentenceRelationId = new SentenceRelationId();
                                    Token objectToken = new Token();
                                    objectToken.setForm(ptt.getToken().toString());
                                    sentenceRelationId.setSubject(subject2);
                                    sentenceRelationId.setObject(objectToken);
                                    sentenceRelationId.setRelation("(VPP,mod)+(NPP,obj)+((NC||NPP),mod)");
                                    sentenceRelationId.setSentence_text(line);
                                    sentenceRelationId.setConfidence(1);
                                    sentenceRelationId.setType(SentenceRelationType.hasRepresentative);
                                    sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                    sentenceRelation.setMethod(SentenceRelationMethod.rules);
                                    list_result.add(sentenceRelation);
                        }
                    }
                }

                /***************************************** Extraction hasFragranceCreator Relation *****************************/
                /*---------------------------------(V,root) +(P,mod) +(NC,prep)+(NPP,mod)-------------------------------*/
                if (posTagSequence.get(i).getTag().toString().equalsIgnoreCase("V") && parseConfiguration.getGoverningDependency(posTagSequence.get(i)).getLabel().equalsIgnoreCase("root")) {
                    List<PosTaggedToken> pos_tagged_token = parseConfiguration.getDependents(posTagSequence.get(i));
                    for (PosTaggedToken ptt : pos_tagged_token) {
                        if (ptt.getTag().toString().equalsIgnoreCase("P") && parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("mod")) {
                            List<PosTaggedToken> pos_tagged_token_test_0 = parseConfiguration.getDependents(ptt);
                            for (PosTaggedToken ptt_test0 : pos_tagged_token_test_0) {
                                if (ptt_test0.getTag().toString().equalsIgnoreCase("NC") && parseConfiguration.getGoverningDependency(ptt_test0).getLabel().equalsIgnoreCase("prep")
                                        &&  ptt_test0.getComment().equalsIgnoreCase("PER")) {
                                            Token objectToken = new Token();
                                            objectToken.setForm(ptt_test0.getToken().toString());
                                            SentenceRelation sentenceRelation = new SentenceRelation();
                                            SentenceRelationId sentenceRelationId = new SentenceRelationId();
                                            Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                            sentenceRelationId.setSubject(subject2);
                                            sentenceRelationId.setObject(objectToken);
                                            sentenceRelationId.setRelation("(V,root) +(P,mod) +(NC,prep)+(NPP,mod)");
                                            sentenceRelationId.setSentence_text(line);
                                            sentenceRelationId.setType(SentenceRelationType.hasFragranceCreator);
                                            sentenceRelationId.setConfidence(1);
                                            sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                            sentenceRelation.setMethod(SentenceRelationMethod.rules);
                                            list_result.add(sentenceRelation);
                                }
                            }
                        }
                    }
                    }

                /*-----------------------------(V,root) +(NC,suj)+(NPP,mod)----------------------------------*/
                if (posTagSequence.get(i).getTag().toString().equalsIgnoreCase("V") && parseConfiguration.getGoverningDependency(posTagSequence.get(i)).getLabel().equalsIgnoreCase("root")) {
                    //Liste des dependences de i
                    List<PosTaggedToken> pos_tagged_token = parseConfiguration.getDependents(posTagSequence.get(i));
                    for (PosTaggedToken ptt : pos_tagged_token) {
                        if (ptt.getTag().toString().equalsIgnoreCase("NC") &&
                                (parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("suj")||parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("obj"))
                                && ptt.getComment().equalsIgnoreCase("PER")) {
                            List<PosTaggedToken> pos_tagged_token_child2 = parseConfiguration.getDependents(ptt);
                                    Token objectToken = new Token();
                                    objectToken.setForm(ptt.getToken().toString());
                                    SentenceRelation sentenceRelation = new SentenceRelation();
                                    SentenceRelationId sentenceRelationId = new SentenceRelationId();
                                    Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                    sentenceRelationId.setSubject(subject2);
                                    sentenceRelationId.setObject(objectToken);
                                    sentenceRelationId.setRelation("(V,root) +(NC,suj)+(NPP,mod)");
                                    sentenceRelationId.setSentence_text(line);
                                    sentenceRelationId.setType(SentenceRelationType.hasFragranceCreator);
                                    sentenceRelationId.setConfidence(1);
                                    sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                    sentenceRelation.setMethod(SentenceRelationMethod.rules);
                                    list_result.add(sentenceRelation);
                        }
                    }
                }

 /*---------------------------------(NC,(mod||suj||prepp) +((P||P+D),dep) +(NPP,prep)+(NPP,mod)-------------------------------*/
           /*---------------------------------      (NC,(mod||suj||prepp) +((P||P+D),dep) +(NPP,prep)+(CC,coord)+(NPP,dep_coord)+(NPP,mod)-------------------------------*/
              /*---------------------------------      (NC,(mod||suj||prepp) +((P||P+D),dep)+ (CC,coord)+(P,dep_coord)+(NPP,prep) +(NPP,mod)-------------------------------*/

            if(posTagSequence.get(i).getTag().toString().equalsIgnoreCase("VPP")&&parseConfiguration.getGoverningDependency(posTagSequence.get(i)).getLabel().equalsIgnoreCase("mod")) {
                List<PosTaggedToken> pos_tagged_token0 = parseConfiguration.getDependents(posTagSequence.get(i));
                for (PosTaggedToken ptt0 : pos_tagged_token0) {
                    if (ptt0.getTag().toString().equalsIgnoreCase("NC")
                            && (parseConfiguration.getGoverningDependency(ptt0).getLabel().equalsIgnoreCase("mod")
                            || parseConfiguration.getGoverningDependency(ptt0).getLabel().equalsIgnoreCase("suj")
                            || parseConfiguration.getGoverningDependency(ptt0).getLabel().equalsIgnoreCase("prep"))
                            && (ptt0.getComment() == null || ptt0.getComment().isEmpty())) {
                        List<PosTaggedToken> pos_tagged_token = parseConfiguration.getDependents(ptt0);
                        for (PosTaggedToken ptt : pos_tagged_token) {
                            if ((ptt.getTag().toString().equalsIgnoreCase("P") || ptt.getTag().toString().equalsIgnoreCase("P+D"))
                                    && parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("dep")) {
                                List<PosTaggedToken> pos_tagged_token_test = parseConfiguration.getDependents(ptt);
                                for (PosTaggedToken ptt_test : pos_tagged_token_test) {
                                    if (ptt_test.getTag().toString().equalsIgnoreCase("NPP") && parseConfiguration.getGoverningDependency(ptt_test).getLabel().equalsIgnoreCase("prep")
                                            && ptt_test.getComment().equalsIgnoreCase("PER")) {
                                        Token objectToken = new Token();
                                        objectToken.setForm(ptt_test.getToken().toString());
                                        Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                        SentenceRelation sentenceRelation = new SentenceRelation();
                                        SentenceRelationId sentenceRelationId = new SentenceRelationId();
                                        sentenceRelationId.setSubject(subject2);
                                        sentenceRelationId.setObject(objectToken);
                                        sentenceRelationId.setRelation("(NC,(mod||suj||prepp) +((P||P+D),dep) +(NPP,prep)+(NPP,mod)");
                                        sentenceRelationId.setSentence_text(line);
                                        sentenceRelationId.setConfidence(1);
                                        sentenceRelationId.setType(SentenceRelationType.hasFragranceCreator);
                                        sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                        sentenceRelation.setMethod(SentenceRelationMethod.rules);
                                        list_result.add(sentenceRelation);
                                    }
                                    if (ptt_test.getTag().toString().equalsIgnoreCase("CC") && parseConfiguration.getGoverningDependency(ptt_test).getLabel().equalsIgnoreCase("coord")) {
                                        List<PosTaggedToken> pos_tagged_token_test2 = parseConfiguration.getDependents(ptt_test);
                                        for (PosTaggedToken ptt_test2 : pos_tagged_token_test2) {
                                            if (ptt_test2.getTag().toString().equalsIgnoreCase("P") &&
                                                    parseConfiguration.getGoverningDependency(ptt_test2).getLabel().equalsIgnoreCase("dep_coord")) {
                                                List<PosTaggedToken> pos_tagged_token_test3 = parseConfiguration.getDependents(ptt_test2);
                                                for (PosTaggedToken ptt_test3 : pos_tagged_token_test3) {
                                                    if (ptt_test3.getTag().toString().equalsIgnoreCase("NPP")
                                                            && parseConfiguration.getGoverningDependency(ptt_test3).getLabel().equalsIgnoreCase("prep")
                                                            && ptt_test3.getComment().equalsIgnoreCase("PER")) {
                                                        Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                                        Token objectToken2 = new Token();
                                                        objectToken2.setForm(ptt_test3.getToken().toString());
                                                        SentenceRelation sentenceRelation2 = new SentenceRelation();
                                                        SentenceRelationId sentenceRelationId2 = new SentenceRelationId();
                                                        sentenceRelationId2.setSubject(subject2);
                                                        sentenceRelationId2.setObject(objectToken2);
                                                        sentenceRelationId2.setRelation("(NC,(mod||suj||prepp) +((P||P+D),dep) +(CC,coord)+(NPP,dep_coord)+(NPP,mod)");
                                                        sentenceRelationId2.setConfidence(1);
                                                        sentenceRelationId2.setSentence_text(line);
                                                        sentenceRelationId2.setType(SentenceRelationType.hasFragranceCreator);
                                                        sentenceRelation2.setSentenceRelationId(sentenceRelationId2);
                                                        sentenceRelation2.setMethod(SentenceRelationMethod.rules);
                                                        list_result.add(sentenceRelation2);
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
                               /* if (ptt_test.getTag().toString().equalsIgnoreCase("CC") && parseConfiguration.getGoverningDependency(ptt_test).getLabel().equalsIgnoreCase("coord")) {
                                    List<PosTaggedToken> pos_tagged_token_test1 = parseConfiguration.getDependents(ptt_test);
                                    for (PosTaggedToken ptt_test1 : pos_tagged_token_test1) {
                                        if (ptt_test1.getTag().toString().equalsIgnoreCase("P") && parseConfiguration.getGoverningDependency(ptt_test1).getLabel().equalsIgnoreCase("dep_coord")) {
                                            List<PosTaggedToken> pos_tagged_token_test2 = parseConfiguration.getDependents(ptt_test1);
                                            for (PosTaggedToken ptt_test2 : pos_tagged_token_test2) {
                                                if (ptt_test2.getTag().toString().equalsIgnoreCase("NPP") &&
                                                        parseConfiguration.getGoverningDependency(ptt_test2).getLabel().equalsIgnoreCase("prep")
                                                        &&   ptt_test2.getComment().equalsIgnoreCase("PER")
                                                        ) {
                                                    List<PosTaggedToken> pos_tagged_token_test3 = parseConfiguration.getDependents(ptt_test2);
                                                    for (PosTaggedToken ptt_test3 : pos_tagged_token_test3) {
                                                        if (ptt_test3.getTag().toString().equalsIgnoreCase("NPP") && parseConfiguration.getGoverningDependency(ptt_test3).getLabel().equalsIgnoreCase("mod")
                                                                && ptt_test3.getComment().equalsIgnoreCase("PER")) {

                                                            Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                                            if (subject2 != null || !subject2.getForm().equalsIgnoreCase("null")) {
                                                                Token objectToken2 = new Token();
                                                                objectToken2.setForm(ptt_test2.getToken().toString() + " " + ptt_test3.getToken().toString());
                                                                SentenceRelation sentenceRelation2 = new SentenceRelation();
                                                                SentenceRelationId sentenceRelationId2 = new SentenceRelationId();
                                                                sentenceRelationId2.setSubject(subject2);
                                                                sentenceRelationId2.setObject(objectToken2);
                                                                sentenceRelationId2.setRelation("  (NC,(mod||suj||prepp) +((P||P+D),dep)+ (CC,coord)+(P,dep_coord)+(NPP,prep) +(NPP,mod)");
                                                                sentenceRelationId2.setConfidence(1);
                                                                sentenceRelationId2.setSentence_text(line);
                                                                sentenceRelationId2.setType(SentenceRelationType.hasFragranceCreator);
                                                                sentenceRelation2.setSentenceRelationId(sentenceRelationId2);
                                                                sentenceRelation2.setMethod(SentenceRelationMethod.rules);
                                                                list_result.add(sentenceRelation2);
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }*/


                             //:   }
                           // }
                        //}
                   // }
                //}


                /*-------------------------------------(VPP,(mod||dep)+(P,p_obj)+(NC,NPP)+ ((NC||NPP),mod)----------------------------------*/
                if (posTagSequence.get(i).getTag().toString().equalsIgnoreCase("VPP") && (parseConfiguration.getGoverningDependency(posTagSequence.get(i)).getLabel().equalsIgnoreCase("mod")
                        || parseConfiguration.getGoverningDependency(posTagSequence.get(i)).getLabel().equalsIgnoreCase("dep"))) {

                    List<PosTaggedToken> pos_tagged_token = parseConfiguration.getDependents(posTagSequence.get(i));
                    for (PosTaggedToken ptt : pos_tagged_token) {
                        if (ptt.getTag().toString().equalsIgnoreCase("P") && parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("p_obj")) {
                            List<PosTaggedToken> pos_tagged_token_test = parseConfiguration.getDependents(ptt);
                            for (PosTaggedToken ptt_test : pos_tagged_token_test) {
                                if ((ptt_test.getTag().toString().equalsIgnoreCase("NC") || ptt_test.getTag().toString().equalsIgnoreCase("NPP"))
                                        && parseConfiguration.getGoverningDependency(ptt_test).getLabel().equalsIgnoreCase("prep")
                                        && ptt_test.getComment().equalsIgnoreCase("PER")) {
                                    {
                                            Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                            SentenceRelation sentenceRelation = new SentenceRelation();
                                            SentenceRelationId sentenceRelationId = new SentenceRelationId();
                                            Token objectToken = new Token();
                                            objectToken.setForm(ptt_test.getToken().toString());
                                            sentenceRelationId.setSubject(subject2);
                                            sentenceRelationId.setObject(objectToken);
                                            sentenceRelationId.setRelation("(VPP,(mod||dep))+(P,p_obj)+((NC||NPP),prep)");
                                            sentenceRelationId.setSentence_text(line);
                                            sentenceRelationId.setConfidence(1);
                                            sentenceRelationId.setType(SentenceRelationType.hasFragranceCreator);
                                            sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                            sentenceRelation.setMethod(SentenceRelationMethod.rules);
                                            list_result.add(sentenceRelation);
                                    }
                                }
                            }
                        }
                    }
                }

                /*--------------------------------------(VPP,root)+(NPP,suj)+(NPP,mod)-----------------------------*/
                /*--------------------------------------(VPP,root)+(NPP,suj)+(CC,coord)+(NPP,dep_coord)+(NPP,mod)-----------------------------*/
                if (posTagSequence.get(i).getTag().toString().equalsIgnoreCase("VPP") && parseConfiguration.getGoverningDependency(posTagSequence.get(i)).getLabel().equalsIgnoreCase("root")) {
                    List<PosTaggedToken> pos_tagged_token = parseConfiguration.getDependents(posTagSequence.get(i));
                    System.out.println(pos_tagged_token);
                    for (PosTaggedToken ptt : pos_tagged_token) {
                        if (ptt.getTag().toString().equalsIgnoreCase("NPP") && parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("suj")
                                && ptt.getComment().equalsIgnoreCase("PER")) {
                            Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                            SentenceRelation sentenceRelation = new SentenceRelation();
                            SentenceRelationId sentenceRelationId = new SentenceRelationId();
                            Token objectToken = new Token();
                            objectToken.setForm(ptt.getToken().toString());
                            sentenceRelationId.setSubject(subject2);
                            sentenceRelationId.setObject(objectToken);
                            sentenceRelationId.setRelation("(VPP,root)+(NPP,suj)");
                            sentenceRelationId.setSentence_text(line);
                            sentenceRelationId.setConfidence(1);
                            sentenceRelationId.setType(SentenceRelationType.hasFragranceCreator);
                            sentenceRelation.setSentenceRelationId(sentenceRelationId);
                            sentenceRelation.setMethod(SentenceRelationMethod.rules);
                            list_result.add(sentenceRelation);
                            List<PosTaggedToken> pos_tagged_token_test = parseConfiguration.getDependents(ptt);
                            for (PosTaggedToken ptt_test : pos_tagged_token_test) {
                                if (ptt_test.getTag().toString().equalsIgnoreCase("CC") && parseConfiguration.getGoverningDependency(ptt_test).getLabel().equalsIgnoreCase("coord")) {
                                    List<PosTaggedToken> pos_tagged_token_test1 = parseConfiguration.getDependents(ptt_test);
                                    for (PosTaggedToken ptt_test1 : pos_tagged_token_test1) {
                                        if (ptt_test1.getTag().toString().equalsIgnoreCase("NPP") && parseConfiguration.getGoverningDependency(ptt_test1).getLabel().equalsIgnoreCase("dep_coord")
                                                && ptt_test1.getComment().equalsIgnoreCase("PER")) {
                                                     subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                                     sentenceRelation = new SentenceRelation();
                                                     sentenceRelationId = new SentenceRelationId();
                                                     objectToken = new Token();
                                                    objectToken.setForm(ptt_test1.getToken().toString());
                                                    sentenceRelationId.setSubject(subject2);
                                                    sentenceRelationId.setObject(objectToken);
                                                    sentenceRelationId.setRelation("(VPP,root)+(NPP,suj)+(CC,coord)+(NPP,dep_coord)+(NPP,mod)");
                                                    sentenceRelationId.setSentence_text(line);
                                                    sentenceRelationId.setConfidence(1);
                                                    sentenceRelationId.setType(SentenceRelationType.hasFragranceCreator);
                                                    sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                                    sentenceRelation.setMethod(SentenceRelationMethod.rules);
                                                    list_result.add(sentenceRelation);


                                        }
                                    }

                                }

                            }
                        }
                    }
                }

            //TODO (à verifier)
                /*-------------------------(VPP,(mod||dep)+(NC,mod)+(P,dep)+(NPP,dep)----------------------------*/
                if (posTagSequence.get(i).getTag().toString().equalsIgnoreCase("VPP")
                        && (parseConfiguration.getGoverningDependency(posTagSequence.get(i)).getLabel().equalsIgnoreCase("mod")
                        || parseConfiguration.getGoverningDependency(posTagSequence.get(i)).getLabel().equalsIgnoreCase("dep"))) {

                    List<PosTaggedToken> pos_tagged_token = parseConfiguration.getDependents(posTagSequence.get(i));
                    for (PosTaggedToken ptt : pos_tagged_token) {
                        if (ptt.getTag().toString().equalsIgnoreCase("NC")
                                && (parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("mod"))
                                && ptt.getComment().equalsIgnoreCase("PER")) {
                            // liste de dependence
                            List<PosTaggedToken> pos_tagged_token_test = parseConfiguration.getDependents(ptt);
                            for (PosTaggedToken ptt_test : pos_tagged_token_test) {
                                if (ptt_test.getTag().toString().equalsIgnoreCase("P")
                                        && (parseConfiguration.getGoverningDependency(ptt_test).getLabel().equalsIgnoreCase("dep"))) {
                                    List<PosTaggedToken> pos_tagged_token_test1 = parseConfiguration.getDependents(ptt_test);
                                    for (PosTaggedToken ptt_test1 : pos_tagged_token_test1) {
                                        if (ptt_test1.getTag().toString().equalsIgnoreCase("NPP") &&
                                                (parseConfiguration.getGoverningDependency(ptt_test1).getLabel().equalsIgnoreCase("dep"))
                                                && ptt_test1.getComment().equalsIgnoreCase("PER")) {
                                            Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                            SentenceRelation sentenceRelation = new SentenceRelation();
                                            SentenceRelationId sentenceRelationId = new SentenceRelationId();
                                            Token objectToken = new Token();
                                            objectToken.setForm(ptt_test.getToken().toString() + " " + ptt_test1.getToken().toString());
                                            sentenceRelationId.setSubject(subject2);
                                            sentenceRelationId.setObject(objectToken);
                                            sentenceRelationId.setRelation("(VPP,(mod||dep)+(NC,mod)+(P,dep)+(NPP,dep)");
                                            sentenceRelationId.setSentence_text(line);
                                            sentenceRelationId.setConfidence(1);
                                            sentenceRelationId.setType(SentenceRelationType.hasFragranceCreator);
                                            sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                            sentenceRelation.setMethod(SentenceRelationMethod.rules);
                                            list_result.add(sentenceRelation);

                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                /************************************************Extraction hasComponent***********************************************/
                /*-------------------------------(V,root)+(NPP,obj)+(NC,mod)+(P,dep)+(NC,prep)+(ADJ,mod)---------------------------*/
                /*-------------------------------(V,root)+(NPP,obj)+(NC,mod)+(P,dep)+(NC,prep)---------------------------*/
                if (posTagSequence.get(i).getTag().toString().equalsIgnoreCase("V") && parseConfiguration.getGoverningDependency(posTagSequence.get(i)).getLabel().equalsIgnoreCase("root")) {
                    //Liste des dependences de i

                    List<PosTaggedToken> pos_tagged_token = parseConfiguration.getDependents(posTagSequence.get(i));
                    for (PosTaggedToken ptt : pos_tagged_token) {
                        if (ptt.getTag().toString().equalsIgnoreCase("NPP") && parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("obj") ) {
                            List<PosTaggedToken> pos_tagged_token_child = parseConfiguration.getDependents(ptt);
                            for (PosTaggedToken ptt_child : pos_tagged_token_child) {
                                if (ptt_child.getTag().toString().equalsIgnoreCase("NC")
                                        && parseConfiguration.getGoverningDependency(ptt_child).getLabel().equalsIgnoreCase("mod")
                                        && (ptt_child.getComment()==null||ptt_child.getComment().isEmpty())) {
                                    List<PosTaggedToken> pos_tagged_token_child1 = parseConfiguration.getDependents(ptt_child);
                                    for (PosTaggedToken ptt_child1 : pos_tagged_token_child1) {
                                        if (ptt_child1.getTag().toString().equalsIgnoreCase("P")
                                                && parseConfiguration.getGoverningDependency(ptt_child1).getLabel().equalsIgnoreCase("dep")) {

                                            List<PosTaggedToken> pos_tagged_token_child2 = parseConfiguration.getDependents(ptt_child1);
                                            for (PosTaggedToken ptt_child2 : pos_tagged_token_child2) {
                                                if (ptt_child2.getTag().toString().equalsIgnoreCase("NC")
                                                        && parseConfiguration.getGoverningDependency(ptt_child2).getLabel().equalsIgnoreCase("prep")
                                                        &&(ptt_child2.getComment()==null||ptt_child2.getComment().isEmpty())) {
                                                    boolean annotated = false;
                                                    List<PosTaggedToken> pos_tagged_token_child3 = parseConfiguration.getDependents(ptt_child2);
                                                    for (PosTaggedToken ptt_child3 : pos_tagged_token_child3) {
                                                        if (ptt_child3.getTag().toString().equalsIgnoreCase("ADJ")
                                                                && parseConfiguration.getGoverningDependency(ptt_child3).getLabel().equalsIgnoreCase("mod")) {
                                                            annotated = true;
                                                            Token objectToken = new Token();
                                                            Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                                            objectToken.setForm(ptt_child2.getToken().toString() + " " + ptt_child3.getToken().toString());
                                                            SentenceRelation sentenceRelation = new SentenceRelation();
                                                            SentenceRelationId sentenceRelationId = new SentenceRelationId();
                                                            sentenceRelationId.setSubject(subject2);
                                                            sentenceRelationId.setObject(objectToken);
                                                            sentenceRelationId.setRelation("(V,root)+(NPP,obj)+(NC,mod)+(P,dep)+(NC,prep)+(ADJ,mod)");
                                                            sentenceRelationId.setSentence_text(line);
                                                            sentenceRelationId.setType(SentenceRelationType.hasComponent);
                                                            sentenceRelationId.setConfidence(1);
                                                            sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                                            sentenceRelation.setMethod(SentenceRelationMethod.rules);
                                                            list_result.add(sentenceRelation);
                                                        }
                                                    }

                                                        if (!annotated) {
                                                            Token objectToken = new Token();
                                                            Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                                            objectToken.setForm(ptt_child2.getToken().toString());
                                                            SentenceRelation sentenceRelation = new SentenceRelation();
                                                            SentenceRelationId sentenceRelationId = new SentenceRelationId();
                                                            sentenceRelationId.setSubject(subject2);
                                                            sentenceRelationId.setObject(objectToken);
                                                            sentenceRelationId.setRelation("(V,root) +(NPP,obj)+(NC,mod)+(P,dep)+(NC,prep)");
                                                            sentenceRelationId.setSentence_text(line);
                                                            sentenceRelationId.setType(SentenceRelationType.hasComponent);
                                                            sentenceRelationId.setConfidence(1);
                                                            sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                                            sentenceRelation.setMethod(SentenceRelationMethod.rules);
                                                            list_result.add(sentenceRelation);
                                                        }

                                                }
                                            }
                                        }
                                       /* if (ptt_child1.getTag().toString().equalsIgnoreCase("NPP")
                                                && parseConfiguration.getGoverningDependency(ptt_child1).getLabel().equalsIgnoreCase("mod")
                                                && !ptt_child1.getComment().equalsIgnoreCase("product") && !ptt_child1.getComment().equalsIgnoreCase("brand")
                                                || !ptt_child1.getComment().equalsIgnoreCase("range") || !ptt_child1.getComment().equalsIgnoreCase("division")
                                                || !ptt_child1.getComment().equalsIgnoreCase("group")) {
                                            boolean annotated = false;
                                            List<PosTaggedToken> pos_tagged_token_child3 = parseConfiguration.getDependents(ptt_child1);
                                            for (PosTaggedToken ptt_child3 : pos_tagged_token_child3) {
                                                if (ptt_child3.getTag().toString().equalsIgnoreCase("ADJ")
                                                        && parseConfiguration.getGoverningDependency(ptt_child3).getLabel().equalsIgnoreCase("mod")) {
                                                    annotated = true;
                                                    Token objectToken = new Token();
                                                    Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                                    objectToken.setForm(ptt_child1.getToken().toString() + " " + ptt_child3.getToken().toString());
                                                    SentenceRelation sentenceRelation = new SentenceRelation();
                                                    SentenceRelationId sentenceRelationId = new SentenceRelationId();
                                                    sentenceRelationId.setSubject(subject2);
                                                    sentenceRelationId.setObject(objectToken);
                                                    sentenceRelationId.setRelation("(V,root) +(NPP,obj)+(NPP,mod)+ (ADJ,mod)");
                                                    sentenceRelationId.setSentence_text(line);
                                                    sentenceRelationId.setType(SentenceRelationType.hasComponent);
                                                    sentenceRelationId.setConfidence(1);
                                                    sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                                    sentenceRelation.setMethod(SentenceRelationMethod.rules);
                                                    list_result.add(sentenceRelation);

                                                }
                                            }
                                            if (!annotated) {
                                                Token objectToken = new Token();
                                                System.out.println("test rules: " + ptt_child1.getToken().toString() + " tag: " + ptt_child1.getTag());
                                                Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                                objectToken.setForm(ptt_child1.getToken().toString());
                                                SentenceRelation sentenceRelation = new SentenceRelation();
                                                SentenceRelationId sentenceRelationId = new SentenceRelationId();
                                                sentenceRelationId.setSubject(subject2);
                                                sentenceRelationId.setObject(objectToken);
                                                sentenceRelationId.setRelation("(V,root) +(NPP,obj)+(NPP,mod)");
                                                sentenceRelationId.setSentence_text(line);
                                                sentenceRelationId.setType(SentenceRelationType.hasComponent);
                                                sentenceRelationId.setConfidence(1);
                                                sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                                sentenceRelation.setMethod(SentenceRelationMethod.rules);
                                                list_result.add(sentenceRelation);
                                            }
                                        }*/

                                    }

                                }
                                if (ptt_child.getTag().toString().equalsIgnoreCase("NPP")
                                        && parseConfiguration.getGoverningDependency(ptt_child).getLabel().equalsIgnoreCase("coord")
                                        && (ptt_child.getComment()==null||ptt_child.getComment().isEmpty())) {
                                    Token objectToken = new Token();
                                    Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                    objectToken.setForm(ptt_child.getToken().toString());
                                    SentenceRelation sentenceRelation = new SentenceRelation();
                                    SentenceRelationId sentenceRelationId = new SentenceRelationId();
                                    sentenceRelationId.setSubject(subject2);
                                    sentenceRelationId.setObject(objectToken);
                                    sentenceRelationId.setRelation("(V,root) +(NPP,obj)+(NPP,mod)");
                                    sentenceRelationId.setSentence_text(line);
                                    sentenceRelationId.setType(SentenceRelationType.hasComponent);
                                    sentenceRelationId.setConfidence(1);
                                    sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                    sentenceRelation.setMethod(SentenceRelationMethod.rules);
                                    list_result.add(sentenceRelation);

                                    List<PosTaggedToken> pos_tagged_token_child1 = parseConfiguration.getDependents(ptt_child);
                                    for (PosTaggedToken ptt_child1 : pos_tagged_token_child1) {
                                        if (ptt_child1.getTag().toString().equalsIgnoreCase("CC")
                                                && parseConfiguration.getGoverningDependency(ptt_child1).getLabel().equalsIgnoreCase("coord")) {
                                            List<PosTaggedToken> pos_tagged_token_child2 = parseConfiguration.getDependents(ptt_child1);
                                            for (PosTaggedToken ptt_child2 : pos_tagged_token_child2) {
                                                if (ptt_child2.getTag().toString().equalsIgnoreCase("NPP")
                                                        && parseConfiguration.getGoverningDependency(ptt_child2).getLabel().equalsIgnoreCase("dep_coord")
                                                        && (ptt_child2.getComment() == null || ptt_child2.getComment().isEmpty())) {
                                                    objectToken = new Token();
                                                    subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                                    objectToken.setForm(ptt_child2.getToken().toString());
                                                    sentenceRelation = new SentenceRelation();
                                                    sentenceRelationId = new SentenceRelationId();
                                                    sentenceRelationId.setSubject(subject2);
                                                    sentenceRelationId.setObject(objectToken);
                                                    sentenceRelationId.setRelation("(V,root) +(NPP,obj)+(NPP,mod)");
                                                    sentenceRelationId.setSentence_text(line);
                                                    sentenceRelationId.setType(SentenceRelationType.hasComponent);
                                                    sentenceRelationId.setConfidence(1);
                                                    sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                                    sentenceRelation.setMethod(SentenceRelationMethod.rules);
                                                    list_result.add(sentenceRelation);

                                                }
                                            }
                                        }

                                        }
                                }
                            }



                        }
                    }
                }

                /*-----------------------(V,root)+(NC,suj)+(P,dep)+(NC,prep)+(ADJ,mod)---------------------*/
                  /*-----------------------(V,root)+(NC,suj)+(P,dep)+(NC,prep)---------------------*/
                /*-----------------------(V,root)+(NC,suj)+(P,dep)+(CC,coord)+(P,dep_coord)+(NC,prep)+(ADJ,mod)---------------------*/
                /*-----------------------(V,root)+(NC,suj)+(P,dep)+(CC,coord)+(P,dep_coord)+(NC,prep)---------------------*/
                if (posTagSequence.get(i).getTag().toString().equalsIgnoreCase("V") && parseConfiguration.getGoverningDependency(posTagSequence.get(i)).getLabel().equalsIgnoreCase("root")) {
                    //Liste des dependences de i
                    List<PosTaggedToken> pos_tagged_token = parseConfiguration.getDependents(posTagSequence.get(i));
                    for (PosTaggedToken ptt : pos_tagged_token) {
                        if (ptt.getTag().toString().equalsIgnoreCase("NC") && parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("suj")
                                ) {
                            List<PosTaggedToken> pos_tagged_token_child2 = parseConfiguration.getDependents(ptt);
                            for (PosTaggedToken ptt_child2 : pos_tagged_token_child2) {
                                if (ptt_child2.getTag().toString().equalsIgnoreCase("P") && parseConfiguration.getGoverningDependency(ptt_child2).getLabel().equalsIgnoreCase("dep")) {
                                    List<PosTaggedToken> pos_tagged_token_child3 = parseConfiguration.getDependents(ptt_child2);
                                    for (PosTaggedToken ptt_child3 : pos_tagged_token_child3) {
                                        if ((ptt_child3.getTag().toString().equalsIgnoreCase("NC")
                                                && (parseConfiguration.getGoverningDependency(ptt_child3).getLabel().equalsIgnoreCase("prep")
                                                && (ptt_child3.getComment()==null||ptt_child3.getComment().isEmpty())))) {
                                            boolean annotated = false;
                                            List<PosTaggedToken> pos_tagged_token_child4 = parseConfiguration.getDependents(ptt_child3);
                                            for (PosTaggedToken ptt_child4 : pos_tagged_token_child4) {
                                                if (ptt_child4.getTag().toString().equalsIgnoreCase("ADJ")
                                                        && parseConfiguration.getGoverningDependency(ptt_child4).getLabel().equalsIgnoreCase("mod")) {
                                                    annotated = true;
                                                    Token objectToken = new Token();
                                                    Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                                    objectToken.setForm(ptt_child3.getToken().toString() + " " + ptt_child4.getToken().toString());
                                                    SentenceRelation sentenceRelation = new SentenceRelation();
                                                    SentenceRelationId sentenceRelationId = new SentenceRelationId();
                                                    sentenceRelationId.setSubject(subject2);
                                                    sentenceRelationId.setObject(objectToken);
                                                    sentenceRelationId.setRelation("(V,root)+(NC,suj)+(P,dep)+(NC,prep)+(ADJ,mod)");
                                                    sentenceRelationId.setSentence_text(line);
                                                    sentenceRelationId.setType(SentenceRelationType.hasComponent);
                                                    sentenceRelationId.setConfidence(1);
                                                    sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                                    sentenceRelation.setMethod(SentenceRelationMethod.rules);
                                                    list_result.add(sentenceRelation);
                                                }
                                            }
                                            if (!annotated) {
                                                Token objectToken = new Token();
                                                objectToken.setForm(ptt_child3.getToken().toString());
                                                SentenceRelation sentenceRelation = new SentenceRelation();
                                                SentenceRelationId sentenceRelationId = new SentenceRelationId();
                                                Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                                sentenceRelationId.setSubject(subject2);
                                                sentenceRelationId.setObject(objectToken);
                                                sentenceRelationId.setRelation("(V,root)+(NC,suj)+(P,dep)+(NC,prep)");
                                                sentenceRelationId.setSentence_text(line);
                                                sentenceRelationId.setType(SentenceRelationType.hasComponent);
                                                sentenceRelationId.setConfidence(1);
                                                sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                                sentenceRelation.setMethod(SentenceRelationMethod.rules);
                                                list_result.add(sentenceRelation);
                                            }
                                        }

                                        if ((ptt_child3.getTag().toString().equalsIgnoreCase("CC")
                                                && (parseConfiguration.getGoverningDependency(ptt_child3).getLabel().equalsIgnoreCase("coord")))) {
                                            List<PosTaggedToken> pos_tagged_token_child4 = parseConfiguration.getDependents(ptt_child3);
                                            for (PosTaggedToken ptt_child4 : pos_tagged_token_child4) {
                                                if ((ptt_child4.getTag().toString().equalsIgnoreCase("P")
                                                        && (parseConfiguration.getGoverningDependency(ptt_child4).getLabel().equalsIgnoreCase("dep_coord")
                                                ))) {
                                                    List<PosTaggedToken> pos_tagged_token_child5 = parseConfiguration.getDependents(ptt_child4);
                                                    for (PosTaggedToken ptt_child5 : pos_tagged_token_child5) {
                                                        if (ptt_child5.getTag().toString().equalsIgnoreCase("NC")
                                                                && parseConfiguration.getGoverningDependency(ptt_child5).getLabel().equalsIgnoreCase("prep")
                                                                && (ptt_child5.getComment()==null||ptt_child5.getComment().isEmpty())) {
                                                            boolean annotated=false;
                                                            List<PosTaggedToken> pos_tagged_token_child6 = parseConfiguration.getDependents(ptt_child5);
                                                            for (PosTaggedToken ptt_child6 : pos_tagged_token_child6) {
                                                                if (ptt_child6.getTag().toString().equalsIgnoreCase("ADJ")
                                                                        && parseConfiguration.getGoverningDependency(ptt_child6).getLabel().equalsIgnoreCase("mod")) {
                                                                    annotated = true;
                                                                    Token objectToken = new Token();
                                                                    Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                                                    objectToken.setForm(ptt_child2.getToken().toString() + " " + ptt_child3.getToken().toString());
                                                                    SentenceRelation sentenceRelation = new SentenceRelation();
                                                                    SentenceRelationId sentenceRelationId = new SentenceRelationId();
                                                                    sentenceRelationId.setSubject(subject2);
                                                                    sentenceRelationId.setObject(objectToken);
                                                                    sentenceRelationId.setRelation("(V,root)+(NC,suj)+(P,dep)+(CC,coord)+(P,dep_coord)+(NC,prep)+(ADJ,mod)");
                                                                    sentenceRelationId.setSentence_text(line);
                                                                    sentenceRelationId.setType(SentenceRelationType.hasComponent);
                                                                    sentenceRelationId.setConfidence(1);
                                                                    sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                                                    sentenceRelation.setMethod(SentenceRelationMethod.rules);
                                                                    list_result.add(sentenceRelation);
                                                                }
                                                            }
                                                            if (!annotated) {
                                                                Token objectToken = new Token();
                                                                objectToken.setForm(ptt_child5.getToken().toString());
                                                                SentenceRelation sentenceRelation = new SentenceRelation();
                                                                SentenceRelationId sentenceRelationId = new SentenceRelationId();
                                                                Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                                                sentenceRelationId.setSubject(subject2);
                                                                sentenceRelationId.setObject(objectToken);
                                                                sentenceRelationId.setRelation("(V,root)+(NC,suj)+(P,dep)+(CC,coord)+(P,dep_coord)+(NC,prep)");
                                                                sentenceRelationId.setSentence_text(line);
                                                                sentenceRelationId.setType(SentenceRelationType.hasComponent);
                                                                sentenceRelationId.setConfidence(1);
                                                                sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                                                sentenceRelation.setMethod(SentenceRelationMethod.rules);
                                                                list_result.add(sentenceRelation);

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

                /*----------------------------------(V,root)+(P,(P_obj||de_obj))+((NC||NPP),prep)+(ADJ,mod)----------------------------------*/
                /*----------------------------------(V,root)+(P,(P_obj||de_obj))+((NC||NPP),prep)----------------------------------*/
                /*---------------------------------- (V,root)+(P,(P_obj||de_obj))+(CC,coord)+(P+D,dep_coord)+(NC,prep)+(ADJ,mod)----------------------------------*/
                 /*----------------------------------(V,root)+(P,(P_obj||de_obj))+(CC,coord)+(P+D,dep_coord)+(NC,prep)+(ADJ,mod)----------------------------------*/
                if (posTagSequence.get(i).getTag().toString().equalsIgnoreCase("V") && parseConfiguration.getGoverningDependency(posTagSequence.get(i)).getLabel().equalsIgnoreCase("root")) {
                    //Liste des dependences de i
                    List<PosTaggedToken> pos_tagged_token = parseConfiguration.getDependents(posTagSequence.get(i));
                    for (PosTaggedToken ptt : pos_tagged_token) {
                        if ((ptt.getTag().toString().equalsIgnoreCase("P") || ptt.getTag().toString().equalsIgnoreCase("P+D"))
                             && (parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("P_obj")
                                 || parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("de_obj"))) {
                            List<PosTaggedToken> pos_tagged_token_test_0 = parseConfiguration.getDependents(ptt);
                            for (PosTaggedToken ptt_test0 : pos_tagged_token_test_0) {
                                if ((ptt_test0.getTag().toString().equalsIgnoreCase("NC") || (ptt_test0.getTag().toString().equalsIgnoreCase("NPP"))
                                        && parseConfiguration.getGoverningDependency(ptt_test0).getLabel().equalsIgnoreCase("prep"))
                                        && (ptt_test0.getComment().equalsIgnoreCase("LOC")||ptt_test0.getComment()==null||ptt_test0.getComment().isEmpty())) {
                                    boolean annotated = false;
                                    List<PosTaggedToken> pos_tagged_token_child3 = parseConfiguration.getDependents(ptt_test0);
                                    for (PosTaggedToken ptt_child3 : pos_tagged_token_child3) {
                                        if (ptt_child3.getTag().toString().equalsIgnoreCase("ADJ")
                                                && parseConfiguration.getGoverningDependency(ptt_child3).getLabel().equalsIgnoreCase("mod")) {
                                            annotated = true;
                                            Token objectToken = new Token();
                                            Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                            if (subject2.getForm() != null && !subject2.getForm().isEmpty()) {
                                                objectToken.setForm(ptt_test0.getToken().toString() + " " + ptt_child3.getToken().toString());
                                                SentenceRelation sentenceRelation = new SentenceRelation();
                                                SentenceRelationId sentenceRelationId = new SentenceRelationId();
                                                sentenceRelationId.setSubject(subject2);
                                                sentenceRelationId.setObject(objectToken);
                                                sentenceRelationId.setRelation("(V,root)+(P,(P_obj||de_obj))+((NC||NPP),prep)+(ADJ,mod)");
                                                sentenceRelationId.setSentence_text(line);
                                                sentenceRelationId.setType(SentenceRelationType.hasComponent);
                                                sentenceRelationId.setConfidence(1);
                                                sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                                sentenceRelation.setMethod(SentenceRelationMethod.rules);
                                                list_result.add(sentenceRelation);
                                            }
                                        }
                                        if (ptt_child3.getTag().toString().equalsIgnoreCase("NC")
                                                && parseConfiguration.getGoverningDependency(ptt_child3).getLabel().equalsIgnoreCase("mod")) {
                                            Token objectToken = new Token();
                                            Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                            objectToken.setForm(ptt_child3.getToken().toString());
                                            SentenceRelation sentenceRelation = new SentenceRelation();
                                            SentenceRelationId sentenceRelationId = new SentenceRelationId();
                                            sentenceRelationId.setSubject(subject2);
                                            sentenceRelationId.setObject(objectToken);
                                            sentenceRelationId.setRelation("(V,root)+(P,(P_obj||de_obj))+((NC||NPP),prep)+(ADJ,mod)");
                                            sentenceRelationId.setSentence_text(line);
                                            sentenceRelationId.setType(SentenceRelationType.hasComponent);
                                            sentenceRelationId.setConfidence(1);
                                            sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                            sentenceRelation.setMethod(SentenceRelationMethod.rules);
                                            list_result.add(sentenceRelation);

                                            List<PosTaggedToken> pos_tagged_token_child4 = parseConfiguration.getDependents(ptt_child3);
                                            for (PosTaggedToken ptt_child4 : pos_tagged_token_child4) {
                                                if (ptt_child4.getTag().toString().equalsIgnoreCase("CC")
                                                        && parseConfiguration.getGoverningDependency(ptt_child4).getLabel().equalsIgnoreCase("coord")
                                                      ) {  List<PosTaggedToken> pos_tagged_token_child5 = parseConfiguration.getDependents(ptt_child4);
                                                    for (PosTaggedToken ptt_child5 : pos_tagged_token_child5) {
                                                        if ((ptt_child5.getTag().toString().equalsIgnoreCase("NC")||ptt_child5.getTag().toString().equalsIgnoreCase("NPP"))
                                                                && parseConfiguration.getGoverningDependency(ptt_child5).getLabel().equalsIgnoreCase("dep_coord")
                                                                && (ptt_child5.getComment()==null|| ptt_child5.getComment().isEmpty())) {
                                                             objectToken = new Token();
                                                             subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                                            objectToken.setForm(ptt_child5.getToken().toString());
                                                             sentenceRelation = new SentenceRelation();
                                                             sentenceRelationId = new SentenceRelationId();
                                                            sentenceRelationId.setSubject(subject2);
                                                            sentenceRelationId.setObject(objectToken);
                                                            sentenceRelationId.setRelation("(V,root)+(P,(P_obj||de_obj))+((NC||NPP),prep)+(ADJ,mod)");
                                                            sentenceRelationId.setSentence_text(line);
                                                            sentenceRelationId.setType(SentenceRelationType.hasComponent);
                                                            sentenceRelationId.setConfidence(1);
                                                            sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                                            sentenceRelation.setMethod(SentenceRelationMethod.rules);
                                                            list_result.add(sentenceRelation);
                                                        }
                                                        }

                                                }
                                            }
                                        }
                                        if (ptt_child3.getTag().toString().equalsIgnoreCase("CC")
                                                && parseConfiguration.getGoverningDependency(ptt_child3).getLabel().equalsIgnoreCase("coord")) {
                                            List<PosTaggedToken> pos_tagged_token_child4 = parseConfiguration.getDependents(ptt_child3);
                                            for (PosTaggedToken ptt_child4 : pos_tagged_token_child4) {
                                                if ((ptt_child4.getTag().toString().equalsIgnoreCase("NC")||ptt_child4.getTag().toString().equalsIgnoreCase("NPP"))
                                                        && parseConfiguration.getGoverningDependency(ptt_child4).getLabel().equalsIgnoreCase("dep_coord")
                                                        && (ptt_child4.getComment().equalsIgnoreCase("LOC")||ptt_child4.getComment()==null|| ptt_child4.getComment().isEmpty())) {
                                                    Token objectToken = new Token();
                                                    Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                                        objectToken.setForm(ptt_child4.getToken().toString());
                                                        SentenceRelation sentenceRelation = new SentenceRelation();
                                                        SentenceRelationId sentenceRelationId = new SentenceRelationId();
                                                        sentenceRelationId.setSubject(subject2);
                                                        sentenceRelationId.setObject(objectToken);
                                                        sentenceRelationId.setRelation("(V,root)+(P,(P_obj||de_obj))+((NC||NPP),prep)+(ADJ,mod)");
                                                        sentenceRelationId.setSentence_text(line);
                                                        sentenceRelationId.setType(SentenceRelationType.hasComponent);
                                                        sentenceRelationId.setConfidence(1);
                                                        sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                                        sentenceRelation.setMethod(SentenceRelationMethod.rules);
                                                        list_result.add(sentenceRelation);
                                                }
                                            }

                                        }
                                    }
                                    if (!annotated) {
                                        Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                        if (subject2.getForm() != null && !subject2.getForm().isEmpty()) {
                                            Token objectToken = new Token();
                                            objectToken.setForm(ptt_test0.getToken().toString());
                                            SentenceRelation sentenceRelation = new SentenceRelation();
                                            SentenceRelationId sentenceRelationId = new SentenceRelationId();
                                            sentenceRelationId.setSubject(subject2);
                                            sentenceRelationId.setObject(objectToken);
                                            sentenceRelationId.setRelation("(V,root)+(P,(P_obj||de_obj))+((NC||NPP),prep)");
                                            sentenceRelationId.setSentence_text(line);
                                            sentenceRelationId.setConfidence(1);
                                            sentenceRelationId.setType(SentenceRelationType.hasComponent);
                                            sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                            sentenceRelation.setMethod(SentenceRelationMethod.rules);
                                            list_result.add(sentenceRelation);

                                   }
                                    }
                                }
                                if ((ptt_test0.getTag().toString().equalsIgnoreCase("P")||ptt_test0.getTag().toString().equalsIgnoreCase("P+D"))
                                        && parseConfiguration.getGoverningDependency(ptt_test0).getLabel().equalsIgnoreCase("coord")) {
                                    List<PosTaggedToken> pos_tagged_token_test_1 = parseConfiguration.getDependents(ptt_test0);
                                    for (PosTaggedToken ptt_test1 : pos_tagged_token_test_1) {
                                        if ((ptt_test1.getTag().toString().equalsIgnoreCase("NC")||ptt_test1.getTag().toString().equalsIgnoreCase("NPP"))
                                                && parseConfiguration.getGoverningDependency(ptt_test1).getLabel().equalsIgnoreCase("prep")
                                                && (ptt_test1.getComment()==null||ptt_test1.getComment().isEmpty())) {
                                            Token objectToken = new Token();
                                            Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                                objectToken.setForm(ptt_test1.getToken().toString());
                                                SentenceRelation sentenceRelation = new SentenceRelation();
                                                SentenceRelationId sentenceRelationId = new SentenceRelationId();
                                                sentenceRelationId.setSubject(subject2);
                                                sentenceRelationId.setObject(objectToken);
                                                sentenceRelationId.setRelation("(V,root)+(P,(P_obj||de_obj))+(CC,coord)+(P+D,dep_coord)+(NC,prep)+(ADJ,mod)");
                                                sentenceRelationId.setSentence_text(line);
                                                sentenceRelationId.setType(SentenceRelationType.hasComponent);
                                                sentenceRelationId.setConfidence(1);
                                                sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                                sentenceRelation.setMethod(SentenceRelationMethod.rules);
                                                list_result.add(sentenceRelation);
                                        }

                                        if((ptt_test1.getTag().toString().equalsIgnoreCase("CC") && parseConfiguration.getGoverningDependency(ptt_test1).getLabel().equalsIgnoreCase("coord"))){
                                            List<PosTaggedToken> pos_tagged_token_test_2 = parseConfiguration.getDependents(ptt_test1);
                                            for (PosTaggedToken ptt_test2 : pos_tagged_token_test_2) {
                                                if ((ptt_test2.getTag().toString().equalsIgnoreCase("P+D")||ptt_test2.getTag().toString().equalsIgnoreCase("P"))
                                                        && parseConfiguration.getGoverningDependency(ptt_test2).getLabel().equalsIgnoreCase("dep_coord")) {
                                                    List<PosTaggedToken> pos_tagged_token_test_3 = parseConfiguration.getDependents(ptt_test2);
                                                    for (PosTaggedToken ptt_test3 : pos_tagged_token_test_3) {
                                                        if ((ptt_test3.getTag().toString().equalsIgnoreCase("NC") || ptt_test3.getTag().toString().equalsIgnoreCase("NPP"))
                                                                && parseConfiguration.getGoverningDependency(ptt_test3).getLabel().equalsIgnoreCase("prep")
                                                                && (ptt_test3.getComment() == null || ptt_test2.getComment().isEmpty())) {
                                                            Token objectToken = new Token();
                                                            Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                                            objectToken.setForm(ptt_test3.getToken().toString());
                                                            SentenceRelation sentenceRelation = new SentenceRelation();
                                                            SentenceRelationId sentenceRelationId = new SentenceRelationId();
                                                            sentenceRelationId.setSubject(subject2);
                                                            sentenceRelationId.setObject(objectToken);
                                                            sentenceRelationId.setRelation("(V,root)+(P,(P_obj||de_obj))+(CC,coord)+(P+D,dep_coord)+(NC,prep)+(ADJ,mod)");
                                                            sentenceRelationId.setSentence_text(line);
                                                            sentenceRelationId.setType(SentenceRelationType.hasComponent);
                                                            sentenceRelationId.setConfidence(1);
                                                            sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                                            sentenceRelation.setMethod(SentenceRelationMethod.rules);
                                                            list_result.add(sentenceRelation);
                                                        }
                                                    }
                                                }
                                                        }
                                        }
                                    }

                                }

                                //Le lait Fermeté sculpturale contient de la céramide et du collagène et promet une double action anti-dessèchement et fermeté remodelante.
                                if ((ptt_test0.getTag().toString().equalsIgnoreCase("CC") && parseConfiguration.getGoverningDependency(ptt_test0).getLabel().equalsIgnoreCase("coord"))) {
                                    List<PosTaggedToken> pos_tagged_token_test_1 = parseConfiguration.getDependents(ptt_test0);
                                    for (PosTaggedToken ptt_test1 : pos_tagged_token_test_1) {
                                        if ((ptt_test1.getTag().toString().equalsIgnoreCase("P+D")||ptt_test1.getTag().toString().equalsIgnoreCase("P"))
                                                && parseConfiguration.getGoverningDependency(ptt_test1).getLabel().equalsIgnoreCase("dep_coord")) {
                                            List<PosTaggedToken> pos_tagged_token_test_2 = parseConfiguration.getDependents(ptt_test1);
                                            for (PosTaggedToken ptt_test2 : pos_tagged_token_test_2) {
                                                if ((ptt_test2.getTag().toString().equalsIgnoreCase("NC")||ptt_test2.getTag().toString().equalsIgnoreCase("NPP"))
                                                        && parseConfiguration.getGoverningDependency(ptt_test2).getLabel().equalsIgnoreCase("prep")
                                                        && (ptt_test2.getComment()==null||ptt_test2.getComment().isEmpty())) {
                                                    boolean annotated = false;
                                                    List<PosTaggedToken> pos_tagged_token_child3 = parseConfiguration.getDependents(ptt_test2);
                                                    for (PosTaggedToken ptt_child3 : pos_tagged_token_child3) {
                                                       if (ptt_child3.getTag().toString().equalsIgnoreCase("ADJ")
                                                                && parseConfiguration.getGoverningDependency(ptt_child3).getLabel().equalsIgnoreCase("mod")) {
                                                            annotated = true;
                                                            Token objectToken = new Token();
                                                            Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                                            if( subject2.getForm()!=null && !subject2.getForm().isEmpty()) {
                                                                objectToken.setForm(ptt_test2.getToken().toString() + " " + ptt_child3.getToken().toString());
                                                                SentenceRelation sentenceRelation = new SentenceRelation();
                                                                SentenceRelationId sentenceRelationId = new SentenceRelationId();
                                                                sentenceRelationId.setSubject(subject2);
                                                                sentenceRelationId.setObject(objectToken);
                                                                sentenceRelationId.setRelation("(V,root)+(P,(P_obj||de_obj))+(CC,coord)+(P+D,dep_coord)+(NC,prep)+(ADJ,mod)");
                                                                sentenceRelationId.setSentence_text(line);
                                                                sentenceRelationId.setType(SentenceRelationType.hasComponent);
                                                                sentenceRelationId.setConfidence(1);
                                                                sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                                                sentenceRelation.setMethod(SentenceRelationMethod.rules);
                                                                list_result.add(sentenceRelation);
                                                            }
                                                        }
                                                    }
                                                    if (!annotated) {
                                                        Token objectToken = new Token();
                                                        objectToken.setForm(ptt_test2.getToken().toString());
                                                        SentenceRelation sentenceRelation = new SentenceRelation();
                                                        SentenceRelationId sentenceRelationId = new SentenceRelationId();
                                                        Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                                        if (subject2.getForm() != null && !subject2.getForm().isEmpty()) {
                                                            sentenceRelationId.setSubject(subject2);
                                                            sentenceRelationId.setObject(objectToken);
                                                            sentenceRelationId.setRelation("(V,root)+(P,(P_obj||de_obj))+(CC,coord)+(P+D,dep_coord)+(NC,prep)");
                                                            sentenceRelationId.setSentence_text(line);
                                                            sentenceRelationId.setConfidence(1);
                                                            sentenceRelationId.setType(SentenceRelationType.hasComponent);
                                                            sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                                            sentenceRelation.setMethod(SentenceRelationMethod.rules);
                                                            list_result.add(sentenceRelation);
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

                /*-----------------------(V,root)+(NC,obj)+(V,mod_rel)+(NC,obj)+(CC,coord)+(NPP,dep_coord)+(ADJ,mod)---------------------------*/
                /*-----------------------(V,root)+(NC,obj)+(V,mod_rel)+(NC,obj)+(CC,coord)+(NPP,dep_coord)---------------------------*/
                if (posTagSequence.get(i).getTag().toString().equalsIgnoreCase("V") && parseConfiguration.getGoverningDependency(posTagSequence.get(i)).getLabel().equalsIgnoreCase("root")) {
                    //Liste des dependences de i
                    List<PosTaggedToken> pos_tagged_token = parseConfiguration.getDependents(posTagSequence.get(i));
                    for (PosTaggedToken ptt : pos_tagged_token) {
                        if (ptt.getTag().toString().equalsIgnoreCase("NC") && parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("obj")
                                && (ptt.getComment()==null||ptt.getComment().isEmpty())) {
                            List<PosTaggedToken> pos_tagged_token_ptt = parseConfiguration.getDependents(ptt);
                            for (PosTaggedToken ptt_child : pos_tagged_token_ptt) {
                                if (ptt_child.getTag().toString().equalsIgnoreCase("V") && parseConfiguration.getGoverningDependency(ptt_child).getLabel().equalsIgnoreCase("mod_rel")) {
                                    List<PosTaggedToken> pos_tagged_token_ptt1 = parseConfiguration.getDependents(ptt_child);
                                    for (PosTaggedToken ptt_child1 : pos_tagged_token_ptt1) {
                                        if (ptt_child1.getTag().toString().equalsIgnoreCase("NC") && parseConfiguration.getGoverningDependency(ptt_child1).getLabel().equalsIgnoreCase("obj")
                                                && (ptt_child1.getComment()==null||ptt_child1.getComment().isEmpty())) {
                                            List<PosTaggedToken> pos_tagged_token_ptt2 = parseConfiguration.getDependents(ptt_child1);
                                            for (PosTaggedToken ptt_child2 : pos_tagged_token_ptt2) {
                                                if (ptt_child2.getTag().toString().equalsIgnoreCase("CC") && parseConfiguration.getGoverningDependency(ptt_child2).getLabel().equalsIgnoreCase("coord")) {
                                                    List<PosTaggedToken> pos_tagged_token_ptt3 = parseConfiguration.getDependents(ptt_child2);
                                                    for (PosTaggedToken ptt_child3 : pos_tagged_token_ptt3) {
                                                        if (ptt_child3.getTag().toString().equalsIgnoreCase("NPP") && parseConfiguration.getGoverningDependency(ptt_child3).getLabel().equalsIgnoreCase("dep_coord")
                                                                && (ptt_child3.getComment()==null||ptt_child3.getComment().isEmpty())) {
                                                            boolean annotated=false;
                                                            List<PosTaggedToken> pos_tagged_token_child_31 = parseConfiguration.getDependents(ptt_child3);
                                                            for (PosTaggedToken ptt_child31 : pos_tagged_token_child_31) {
                                                                if (ptt_child31.getTag().toString().equalsIgnoreCase("ADJ")
                                                                        && parseConfiguration.getGoverningDependency(ptt_child31).getLabel().equalsIgnoreCase("mod")) {
                                                                    annotated = true;
                                                                    Token objectToken = new Token();
                                                                    Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                                                    if (subject2.getForm() != null && !subject2.getForm().isEmpty()) {
                                                                        objectToken.setForm(ptt_child3.getToken().toString() + " " + ptt_child31.getToken().toString());
                                                                        SentenceRelation sentenceRelation = new SentenceRelation();
                                                                        SentenceRelationId sentenceRelationId = new SentenceRelationId();
                                                                        sentenceRelationId.setSubject(subject2);
                                                                        sentenceRelationId.setObject(objectToken);
                                                                        sentenceRelationId.setRelation("(V,root)+(NC,obj)+(V,mod_rel)+(NC,obj)+(CC,coord)+(NPP,dep_coord)+(ADJ,mod)");
                                                                        sentenceRelationId.setSentence_text(line);
                                                                        sentenceRelationId.setType(SentenceRelationType.hasComponent);
                                                                        sentenceRelationId.setConfidence(1);
                                                                        sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                                                        sentenceRelation.setMethod(SentenceRelationMethod.rules);
                                                                        list_result.add(sentenceRelation);
                                                                    }
                                                                }
                                                            }

                                                            if (!annotated) {
                                                                Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                                                if (subject2.getForm() != null && !subject2.getForm().isEmpty()) {
                                                                    Token objectToken = new Token();
                                                                    objectToken.setForm(ptt_child3.getToken().toString());
                                                                    SentenceRelation sentenceRelation = new SentenceRelation();
                                                                    SentenceRelationId sentenceRelationId = new SentenceRelationId();
                                                                    sentenceRelationId.setSubject(subject2);
                                                                    sentenceRelationId.setObject(objectToken);
                                                                    sentenceRelationId.setRelation("(V,root)+(NC,obj)+(V,mod_rel)+(NC,obj)+(CC,coord)+(NPP,dep_coord)");
                                                                    sentenceRelationId.setSentence_text(line);
                                                                    sentenceRelationId.setConfidence(1);
                                                                    sentenceRelationId.setType(SentenceRelationType.hasComponent);
                                                                    sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                                                    sentenceRelation.setMethod(SentenceRelationMethod.rules);
                                                                    list_result.add(sentenceRelation);

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


                /*-----------------------(V,root)+(NC,obj)+(NC,mod)+(ADJ,mod)---------------------------*/
               /*----------------------- (V,root)+(NC,obj)+(NC,mod)+(P,dep)+(NC,prep)+(CC,coord)+(NPP,dep_coord)---------------------------*/
                /*-----------------------(V,root)+(NC,obj)+(NC,mod)---------------------------*/
                if (posTagSequence.get(i).getTag().toString().equalsIgnoreCase("V") && parseConfiguration.getGoverningDependency(posTagSequence.get(i)).getLabel().equalsIgnoreCase("root")) {
                    //Liste des dependences de i
                    List<PosTaggedToken> pos_tagged_token = parseConfiguration.getDependents(posTagSequence.get(i));
                    for (PosTaggedToken ptt : pos_tagged_token) {
                        if (ptt.getTag().toString().equalsIgnoreCase("NC") && parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("obj")
                                && (ptt.getComment()==null||ptt.getComment().isEmpty())) {
                            List<PosTaggedToken> pos_tagged_token_ptt = parseConfiguration.getDependents(ptt);
                            for (PosTaggedToken ptt_child : pos_tagged_token_ptt) {
                                if (ptt_child.getTag().toString().equalsIgnoreCase("NC")
                                        && parseConfiguration.getGoverningDependency(ptt_child).getLabel().equalsIgnoreCase("mod")
                                        &&(ptt_child.getComment()==null||ptt_child.getComment().isEmpty()) ) {
                                    boolean annotated = false;
                                    List<PosTaggedToken> pos_tagged_token_ptt1 = parseConfiguration.getDependents(ptt_child);
                                    for (PosTaggedToken ptt_child1 : pos_tagged_token_ptt1) {
                                        if (ptt_child1.getTag().toString().equalsIgnoreCase("ADJ")
                                                && parseConfiguration.getGoverningDependency(ptt_child1).getLabel().equalsIgnoreCase("mod")) {
                                            annotated = true;
                                            Token objectToken = new Token();
                                            objectToken.setForm(ptt_child.getToken().toString() + " " + ptt_child1.getToken().toString());
                                            SentenceRelation sentenceRelation = new SentenceRelation();
                                            SentenceRelationId sentenceRelationId = new SentenceRelationId();
                                            Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                            System.out.println("subject2: enter condition" + subject2);
                                            sentenceRelationId.setSubject(subject2);
                                            sentenceRelationId.setObject(objectToken);
                                            sentenceRelationId.setRelation("(V,root)+(NC,obj)+(NC,mod)+(ADJ,mod)");
                                            sentenceRelationId.setSentence_text(line);
                                            sentenceRelationId.setConfidence(1);
                                            sentenceRelationId.setType(SentenceRelationType.hasComponent);
                                            sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                            sentenceRelation.setMethod(SentenceRelationMethod.rules);
                                            list_result.add(sentenceRelation);
                                        }

                                        if (ptt_child1.getTag().toString().equalsIgnoreCase("NC")
                                                && parseConfiguration.getGoverningDependency(ptt_child1).getLabel().equalsIgnoreCase("coord")) {

                                            Token objectToken = new Token();
                                            objectToken.setForm(ptt_child1.getToken().toString());
                                            SentenceRelation sentenceRelation = new SentenceRelation();
                                            SentenceRelationId sentenceRelationId = new SentenceRelationId();
                                            Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                            System.out.println("subject2: enter condition" + subject2);
                                            sentenceRelationId.setSubject(subject2);
                                            sentenceRelationId.setObject(objectToken);
                                            sentenceRelationId.setRelation("(V,root)+(NC,obj)+(NC,mod)+(ADJ,mod)");
                                            sentenceRelationId.setSentence_text(line);
                                            sentenceRelationId.setConfidence(1);
                                            sentenceRelationId.setType(SentenceRelationType.hasComponent);
                                            sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                            sentenceRelation.setMethod(SentenceRelationMethod.rules);
                                            list_result.add(sentenceRelation);
                                            List<PosTaggedToken> pos_tagged_token_ptt2 = parseConfiguration.getDependents(ptt_child1);
                                            for (PosTaggedToken ptt_child2 : pos_tagged_token_ptt2) {
                                                if (ptt_child2.getTag().toString().equalsIgnoreCase("NC")
                                                        && parseConfiguration.getGoverningDependency(ptt_child2).getLabel().equalsIgnoreCase("coord")) {
                                                    objectToken = new Token();
                                                    objectToken.setForm(ptt_child2.getToken().toString());
                                                    sentenceRelation = new SentenceRelation();
                                                    sentenceRelationId = new SentenceRelationId();
                                                    subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                                    System.out.println("subject2: enter condition" + subject2);
                                                    sentenceRelationId.setSubject(subject2);
                                                    sentenceRelationId.setObject(objectToken);
                                                    sentenceRelationId.setRelation("(V,root)+(NC,obj)+(NC,mod)+(ADJ,mod)");
                                                    sentenceRelationId.setSentence_text(line);
                                                    sentenceRelationId.setConfidence(1);
                                                    sentenceRelationId.setType(SentenceRelationType.hasComponent);
                                                    sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                                    sentenceRelation.setMethod(SentenceRelationMethod.rules);
                                                    list_result.add(sentenceRelation);
                                                    List<PosTaggedToken> pos_tagged_token_ptt3 = parseConfiguration.getDependents(ptt_child2);
                                                    for (PosTaggedToken ptt_child3 : pos_tagged_token_ptt3) {
                                                        if (ptt_child3.getTag().toString().equalsIgnoreCase("CC")
                                                                && parseConfiguration.getGoverningDependency(ptt_child3).getLabel().equalsIgnoreCase("coord")) {
                                                            List<PosTaggedToken> pos_tagged_token_ptt4 = parseConfiguration.getDependents(ptt_child3);
                                                            for (PosTaggedToken ptt_child4 : pos_tagged_token_ptt4) {
                                                                if (ptt_child4.getTag().toString().equalsIgnoreCase("NC")
                                                                        && parseConfiguration.getGoverningDependency(ptt_child4).getLabel().equalsIgnoreCase("dep_coord")) {
                                                                    objectToken = new Token();
                                                                    objectToken.setForm(ptt_child4.getToken().toString());
                                                                    sentenceRelation = new SentenceRelation();
                                                                    sentenceRelationId = new SentenceRelationId();
                                                                    subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                                                    System.out.println("subject2: enter condition" + subject2);
                                                                    sentenceRelationId.setSubject(subject2);
                                                                    sentenceRelationId.setObject(objectToken);
                                                                    sentenceRelationId.setRelation("(V,root)+(NC,obj)+(NC,mod)+(ADJ,mod)");
                                                                    sentenceRelationId.setSentence_text(line);
                                                                    sentenceRelationId.setConfidence(1);
                                                                    sentenceRelationId.setType(SentenceRelationType.hasComponent);
                                                                    sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                                                    sentenceRelation.setMethod(SentenceRelationMethod.rules);
                                                                    list_result.add(sentenceRelation);
                                                                }
                                                            }

                                                        }

                                                        }

                                                }

                                                }
                                        }
                                        //TODO (à verifier)
                                        if (ptt_child1.getTag().toString().equalsIgnoreCase("P") && parseConfiguration.getGoverningDependency(ptt_child1).getLabel().equalsIgnoreCase("dep")) {
                                            List<PosTaggedToken> pos_tagged_token_ptt2 = parseConfiguration.getDependents(ptt_child1);
                                            for (PosTaggedToken ptt_child2 : pos_tagged_token_ptt2) {
                                                if (ptt_child2.getTag().toString().equalsIgnoreCase("NC") && parseConfiguration.getGoverningDependency(ptt_child2).getLabel().equalsIgnoreCase("prep")
                                                        && (ptt_child2.getComment() == null || ptt_child2.getComment().isEmpty())) {
                                                    List<PosTaggedToken> pos_tagged_token_ptt3 = parseConfiguration.getDependents(ptt_child2);
                                                    for (PosTaggedToken ptt_child3 : pos_tagged_token_ptt3) {
                                                        if (ptt_child3.getTag().toString().equalsIgnoreCase("CC") && parseConfiguration.getGoverningDependency(ptt_child3).getLabel().equalsIgnoreCase("coord")) {
                                                            List<PosTaggedToken> pos_tagged_token_ptt4 = parseConfiguration.getDependents(ptt_child3);
                                                            for (PosTaggedToken ptt_child4 : pos_tagged_token_ptt4) {
                                                                if (ptt_child4.getTag().toString().equalsIgnoreCase("NPP") && parseConfiguration.getGoverningDependency(ptt_child4).getLabel().equalsIgnoreCase("dep_coord")
                                                                        && (ptt_child4.getComment() == null || ptt_child4.getComment().isEmpty())) {
                                                                    annotated = true;
                                                                    Token objectToken = new Token();
                                                                    objectToken.setForm(ptt_child4.getToken().toString());
                                                                    SentenceRelation sentenceRelation = new SentenceRelation();
                                                                    SentenceRelationId sentenceRelationId = new SentenceRelationId();
                                                                    Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                                                    sentenceRelationId.setSubject(subject2);
                                                                    sentenceRelationId.setObject(objectToken);
                                                                    sentenceRelationId.setRelation("(V,root)+(NC,obj)+(NC,mod)+(P,dep)+(NC,prep)+(CC,coord)+(NPP,dep_coord)");
                                                                    sentenceRelationId.setSentence_text(line);
                                                                    sentenceRelationId.setConfidence(1);
                                                                    sentenceRelationId.setType(SentenceRelationType.hasComponent);
                                                                    sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                                                    sentenceRelation.setMethod(SentenceRelationMethod.rules);
                                                                    list_result.add(sentenceRelation);
                                                                }

                                                            }
                                                        }
                                                    }
                                                }

                                            }
                                        }
                                    }
                                        if (!annotated) {
                                            Token objectToken = new Token();
                                            objectToken.setForm(ptt_child.getToken().toString());
                                            SentenceRelation sentenceRelation = new SentenceRelation();
                                            SentenceRelationId sentenceRelationId = new SentenceRelationId();
                                            Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                            sentenceRelationId.setSubject(subject2);
                                            sentenceRelationId.setObject(objectToken);
                                            sentenceRelationId.setRelation("(V,root)+(NC,obj)+(NC,mod)");
                                            sentenceRelationId.setSentence_text(line);
                                            sentenceRelationId.setConfidence(1);
                                            sentenceRelationId.setType(SentenceRelationType.hasComponent);
                                            sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                            sentenceRelation.setMethod(SentenceRelationMethod.rules);
                                            list_result.add(sentenceRelation);

                                    }
                                }
                            }
                        }

                    }
                }


                /*-----------------------------(V,sub)+(P,mod)+(NC,prep)+ (P+D,dep)+(NPP,prep)--------------------------*/
                /*------------------------------ (V,sub)+(P,mod)+(NC,prep)+ (P+D,dep)+(CC,coord)+(P,dep_coord)+(NC,prep)+(ADJ,mod)*/
                if (posTagSequence.get(i).getTag().toString().equalsIgnoreCase("V") &&
                        parseConfiguration.getGoverningDependency(posTagSequence.get(i)).getLabel().equalsIgnoreCase("sub")) {
                    List<PosTaggedToken> pos_tagged_token = parseConfiguration.getDependents(posTagSequence.get(i));
                    for (PosTaggedToken ptt : pos_tagged_token) {
                        if (ptt.getTag().toString().equalsIgnoreCase("P") &&
                                (parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("mod"))) {
                            List<PosTaggedToken> pos_tagged_token_child = parseConfiguration.getDependents(ptt);
                            for (PosTaggedToken ptt_child : pos_tagged_token_child) {
                                if (ptt_child.getTag().toString().equalsIgnoreCase("NC")
                                        && (parseConfiguration.getGoverningDependency(ptt_child).getLabel().equalsIgnoreCase("prep"))
                                        && (ptt_child.getComment()==null||ptt_child.getComment().isEmpty())) {
                                    List<PosTaggedToken> pos_tagged_token_child1 = parseConfiguration.getDependents(ptt_child);
                                    for (PosTaggedToken ptt_child1 : pos_tagged_token_child1) {
                                        if (ptt_child1.getTag().toString().equalsIgnoreCase("P+D")
                                                && (parseConfiguration.getGoverningDependency(ptt_child1).getLabel().equalsIgnoreCase("dep"))) {
                                            List<PosTaggedToken> pos_tagged_token_child2 = parseConfiguration.getDependents(ptt_child1);
                                            for (PosTaggedToken ptt_child2 : pos_tagged_token_child2) {
                                                if (ptt_child2.getTag().toString().equalsIgnoreCase("NPP")
                                                        && (parseConfiguration.getGoverningDependency(ptt_child2).getLabel().equalsIgnoreCase("prep"))
                                                        && (ptt_child2.getComment()==null||ptt_child2.getComment().isEmpty())) {
                                                    Token objectToken = new Token();
                                                    objectToken.setForm(ptt_child2.getToken().toString());
                                                    SentenceRelation sentenceRelation = new SentenceRelation();
                                                    SentenceRelationId sentenceRelationId = new SentenceRelationId();
                                                    Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                                    sentenceRelationId.setSubject(subject2);
                                                    sentenceRelationId.setObject(objectToken);
                                                    sentenceRelationId.setRelation("(V,sub)+(P,mod)+(NC,prep)+ (P+D,dep)+(NPP,prep)");
                                                    sentenceRelationId.setSentence_text(line);
                                                    sentenceRelationId.setConfidence(1);
                                                    sentenceRelationId.setType(SentenceRelationType.hasComponent);
                                                    sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                                    sentenceRelation.setMethod(SentenceRelationMethod.rules);
                                                    list_result.add(sentenceRelation);
                                                }
//TODO (à verifier)
                                                if (ptt_child2.getTag().toString().equalsIgnoreCase("CC")
                                                        && (parseConfiguration.getGoverningDependency(ptt_child2).getLabel().equalsIgnoreCase("coord"))) {
                                                    List<PosTaggedToken> pos_tagged_token_child3 = parseConfiguration.getDependents(ptt_child2);
                                                    for (PosTaggedToken ptt_child3 : pos_tagged_token_child3) {
                                                        if (ptt_child3.getTag().toString().equalsIgnoreCase("P")
                                                                && (parseConfiguration.getGoverningDependency(ptt_child3).getLabel().equalsIgnoreCase("dep_coord"))) {
                                                            List<PosTaggedToken> pos_tagged_token_child4 = parseConfiguration.getDependents(ptt_child3);
                                                            for (PosTaggedToken ptt_child4 : pos_tagged_token_child4) {
                                                                if (ptt_child4.getTag().toString().equalsIgnoreCase("NC")
                                                                        && (parseConfiguration.getGoverningDependency(ptt_child4).getLabel().equalsIgnoreCase("prep"))
                                                                        && (ptt_child4.getComment()==null||ptt_child4.getComment().isEmpty())) {
                                                                    List<PosTaggedToken> pos_tagged_token_child5 = parseConfiguration.getDependents(ptt_child4);
                                                                    for (PosTaggedToken ptt_child5 : pos_tagged_token_child5) {
                                                                        if (ptt_child5.getTag().toString().equalsIgnoreCase("ADJ")
                                                                                && (parseConfiguration.getGoverningDependency(ptt_child5).getLabel().equalsIgnoreCase("mod"))) {
                                                                            Token objectToken = new Token();
                                                                            objectToken.setForm(ptt_child4.getToken().toString() + " " + ptt_child5.getToken().toString());
                                                                            SentenceRelation sentenceRelation = new SentenceRelation();
                                                                            SentenceRelationId sentenceRelationId = new SentenceRelationId();
                                                                            Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                                                            sentenceRelationId.setSubject(subject2);
                                                                            sentenceRelationId.setObject(objectToken);
                                                                            sentenceRelationId.setRelation("(V,sub)+(P,mod)+(NC,prep)+ (P+D,dep)+(CC,coord)+(P,dep_coord)+(NC,prep)+(ADJ,mod)");
                                                                            sentenceRelationId.setSentence_text(line);
                                                                            sentenceRelationId.setConfidence(1);
                                                                            sentenceRelationId.setType(SentenceRelationType.hasComponent);
                                                                            sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                                                            sentenceRelation.setMethod(SentenceRelationMethod.rules);
                                                                            list_result.add(sentenceRelation);
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

                /*---------------------(VPR,mod)+(NPP,obj)+(ADJ,mod)----------------------*/
               /* (VPR,mod)+(NPP,obj)+(CC,coord)+(NPP,dep_coord)+(ADJ,mod)*/
              /*  (VPR,mod)+(NPP,obj)+(CC,coord)+(NPP,dep_coord)*/
                if (posTagSequence.get(i).getTag().toString().equalsIgnoreCase("VPR")
                        && parseConfiguration.getGoverningDependency(posTagSequence.get(i)).getLabel().equalsIgnoreCase("mod")) {
                    List<PosTaggedToken> pos_tagged_token = parseConfiguration.getDependents(posTagSequence.get(i));
                    for (PosTaggedToken ptt : pos_tagged_token) {
                        // règle pour la relation hasComponent
                        //Le baume Fermeté sculputure, associant Skinfibrine et élastopeptides, vise à rendre la peau plus ferme et élastique.
                        if (ptt.getTag().toString().equalsIgnoreCase("NPP")
                                && (parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("obj"))
                                && (ptt.getComment()==null||ptt.getComment().isEmpty())) {
                            boolean annotated = false;
                            List<PosTaggedToken> pos_tagged_token_test = parseConfiguration.getDependents(ptt);
                            for (PosTaggedToken ptt_test : pos_tagged_token_test) {
                                if (ptt_test.getTag().toString().equalsIgnoreCase("ADJ")
                                        && parseConfiguration.getGoverningDependency(ptt_test).getLabel().equalsIgnoreCase("mod")
                                        && ptt_test.getComment().isEmpty()) {
                                    annotated = true;
                                    Token objectToken = new Token();
                                    objectToken.setForm(ptt.getToken().toString() + " " + ptt_test.getToken().toString());
                                    SentenceRelation sentenceRelation = new SentenceRelation();
                                    SentenceRelationId sentenceRelationId = new SentenceRelationId();
                                    Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                    System.out.println("subject2: enter condition" + subject2);
                                    sentenceRelationId.setSubject(subject2);
                                    sentenceRelationId.setObject(objectToken);
                                    sentenceRelationId.setRelation("(VPR,mod)+(NPP,obj)+(ADJ,mod)");
                                    sentenceRelationId.setSentence_text(line);
                                    sentenceRelationId.setConfidence(1);
                                    sentenceRelationId.setType(SentenceRelationType.hasComponent);
                                    sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                    sentenceRelation.setMethod(SentenceRelationMethod.rules);
                                    list_result.add(sentenceRelation);

                                }
                                //TODO (à verifier)
                                if (!annotated) {
                                    Token objectToken = new Token();
                                    objectToken.setForm(ptt.getToken().toString());
                                    SentenceRelation sentenceRelation = new SentenceRelation();
                                    SentenceRelationId sentenceRelationId = new SentenceRelationId();
                                    Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                    System.out.println("subject2: enter condition" + subject2);
                                    sentenceRelationId.setSubject(subject2);
                                    sentenceRelationId.setObject(objectToken);
                                    sentenceRelationId.setRelation("(VPR,mod)+(NPP,obj)");
                                    sentenceRelationId.setSentence_text(line);
                                    sentenceRelationId.setConfidence(1);
                                    sentenceRelationId.setType(SentenceRelationType.hasComponent);
                                    sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                    sentenceRelation.setMethod(SentenceRelationMethod.rules);
                                    list_result.add(sentenceRelation);
                                }

                                if (ptt_test.getTag().toString().equalsIgnoreCase("CC")
                                        && (parseConfiguration.getGoverningDependency(ptt_test).getLabel().equalsIgnoreCase("coord"))
                                        && (ptt_test.getComment()==null||ptt_test.getComment().isEmpty())) {
                                    List<PosTaggedToken> pos_tagged_token_test1 = parseConfiguration.getDependents(ptt_test);
                                    for (PosTaggedToken ptt_test1 : pos_tagged_token_test1) {
                                        if (ptt_test1.getTag().toString().equalsIgnoreCase("NPP")
                                                && (parseConfiguration.getGoverningDependency(ptt_test1).getLabel().equalsIgnoreCase("dep_coord"))
                                                && (ptt_test1.getComment()==null||ptt_test1.getComment().isEmpty())) {
                                            List<PosTaggedToken> pos_tagged_token_test_2 = parseConfiguration.getDependents(ptt_test1);
                                            for (PosTaggedToken ptt_test_2 : pos_tagged_token_test_2) {
                                                if (ptt_test_2.getTag().toString().equalsIgnoreCase("ADJ")
                                                        && parseConfiguration.getGoverningDependency(ptt_test_2).getLabel().equalsIgnoreCase("mod")) {
                                                    annotated = true;
                                                    Token objectToken2 = new Token();
                                                    objectToken2.setForm(ptt_test1.getToken().toString() + " " + ptt_test_2.getToken().toString());
                                                    SentenceRelation sentenceRelation2 = new SentenceRelation();
                                                    SentenceRelationId sentenceRelationId2 = new SentenceRelationId();
                                                    Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                                    System.out.println("subject2: enter condition" + subject2);
                                                    sentenceRelationId2.setSubject(subject2);
                                                    sentenceRelationId2.setObject(objectToken2);
                                                    sentenceRelationId2.setRelation("(VPR,mod)+(NPP,obj)+(CC,coord)+(NPP,dep_coord)+(ADJ,mod)");
                                                    sentenceRelationId2.setSentence_text(line);
                                                    sentenceRelationId2.setConfidence(1);
                                                    sentenceRelationId2.setType(SentenceRelationType.hasComponent);
                                                    sentenceRelation2.setSentenceRelationId(sentenceRelationId2);
                                                    sentenceRelation2.setMethod(SentenceRelationMethod.rules);
                                                    list_result.add(sentenceRelation2);
                                                }
                                                if (!annotated) {
                                                    Token objectToken2 = new Token();
                                                    objectToken2.setForm(ptt_test1.getToken().toString());
                                                    SentenceRelation sentenceRelation2 = new SentenceRelation();
                                                    SentenceRelationId sentenceRelationId2 = new SentenceRelationId();
                                                    Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                                    System.out.println("subject2: enter condition" + subject2);
                                                    sentenceRelationId2.setSubject(subject2);
                                                    sentenceRelationId2.setObject(objectToken2);
                                                    sentenceRelationId2.setRelation("(VPR,mod)+(NPP,obj)+(CC,coord)+(NPP,dep_coord)");
                                                    sentenceRelationId2.setSentence_text(line);
                                                    sentenceRelationId2.setConfidence(1);
                                                    sentenceRelationId2.setType(SentenceRelationType.hasComponent);
                                                    sentenceRelation2.setSentenceRelationId(sentenceRelationId2);
                                                    sentenceRelation2.setMethod(SentenceRelationMethod.rules);
                                                    list_result.add(sentenceRelation2);
                                                }
                                            }
                                        }
                                    }
                                }
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
                                                && (ptt_test1.getComment()==null||ptt_test1.getComment().isEmpty())) {
                                            boolean annotated = false;
                                            List<PosTaggedToken> pos_tagged_token_test2 = parseConfiguration.getDependents(ptt_test1);
                                            for (PosTaggedToken ptt_test2 : pos_tagged_token_test2) {
                                                if (ptt_test2.getTag().toString().equalsIgnoreCase("ADJ")
                                                        && (parseConfiguration.getGoverningDependency(ptt_test2).getLabel().equalsIgnoreCase("mod")) ) {
                                                    annotated = true;
                                                    Token objectToken = new Token();
                                                    objectToken.setForm(ptt_test1.getToken().toString() + " " + ptt_test2.getToken().toString());
                                                    Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                                        SentenceRelation sentenceRelation = new SentenceRelation();
                                                        SentenceRelationId sentenceRelationId = new SentenceRelationId();
                                                        sentenceRelationId.setSubject(subject2);
                                                        sentenceRelationId.setObject(objectToken);
                                                        sentenceRelationId.setRelation("(VPP,(mod||dep))+ (NC,mod)+(P+D,dep)+(NC,prep)+(ADJ,mod)");
                                                        sentenceRelationId.setSentence_text(line);
                                                        sentenceRelationId.setConfidence(1);
                                                        sentenceRelationId.setType(SentenceRelationType.hasComponent);
                                                        sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                                        sentenceRelation.setMethod(SentenceRelationMethod.rules);
                                                        list_result.add(sentenceRelation);
                                                }

                                                if (!annotated) {
                                                    Token objectToken = new Token();
                                                    objectToken.setForm(ptt_test1.getToken().toString());
                                                    Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                                    if (!subject2.getForm().isEmpty() && subject2.getForm() != null) {
                                                        SentenceRelation sentenceRelation = new SentenceRelation();
                                                        SentenceRelationId sentenceRelationId = new SentenceRelationId();
                                                        sentenceRelationId.setSubject(subject2);
                                                        sentenceRelationId.setObject(objectToken);
                                                        sentenceRelationId.setRelation("(VPP,(mod||dep))+ (NC,mod)+(P+D,dep)+(NC,prep)");
                                                        sentenceRelationId.setSentence_text(line);
                                                        sentenceRelationId.setConfidence(1);
                                                        sentenceRelationId.setType(SentenceRelationType.hasComponent);
                                                        sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                                        sentenceRelation.setMethod(SentenceRelationMethod.rules);
                                                        list_result.add(sentenceRelation);
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

            /*----------------------(VPP,(mod||dep))+(P+D,dep)+(NC,prep)+(ADJ,mod)-------------------*/
                  /*----------------------(VPP,(mod||dep))+(P+D,dep)+(NC,prep)-------------------*/
                  /*---------------------- (VPP,(mod||dep))+(P+D,dep)+(CC,coord)+(P,dep_coord)+(NC,prep)-------------------*/
            if (posTagSequence.get(i).getTag().toString().equalsIgnoreCase("VPP")
                    && (parseConfiguration.getGoverningDependency(posTagSequence.get(i)).getLabel().equalsIgnoreCase("mod")
                    || parseConfiguration.getGoverningDependency(posTagSequence.get(i)).getLabel().equalsIgnoreCase("dep"))) {
                List<PosTaggedToken> pos_tagged_token = parseConfiguration.getDependents(posTagSequence.get(i));
                for (PosTaggedToken ptt : pos_tagged_token) {
                    if (ptt.getTag().toString().equalsIgnoreCase("P+D")
                            && (parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("mod"))) {
                        List<PosTaggedToken> pos_tagged_token_test = parseConfiguration.getDependents(ptt);
                        for (PosTaggedToken ptt_test : pos_tagged_token_test) {
                            if (ptt_test.getTag().toString().equalsIgnoreCase("NC")
                                    && (parseConfiguration.getGoverningDependency(ptt_test).getLabel().equalsIgnoreCase("prep"))
                                    && (ptt_test.getComment()==null||ptt_test.getComment().isEmpty())) {
                                boolean annotated = false;
                                List<PosTaggedToken> pos_tagged_token_test2 = parseConfiguration.getDependents(ptt_test);
                                for (PosTaggedToken ptt_test2 : pos_tagged_token_test2) {
                                    if (ptt_test2.getTag().toString().equalsIgnoreCase("ADJ")
                                            && (parseConfiguration.getGoverningDependency(ptt_test2).getLabel().equalsIgnoreCase("mod")) ) {
                                        annotated = true;
                                        Token objectToken = new Token();
                                        objectToken.setForm(ptt_test.getToken().toString() + " " + ptt_test2.getToken().toString());
                                        SentenceRelation sentenceRelation = new SentenceRelation();
                                        SentenceRelationId sentenceRelationId = new SentenceRelationId();
                                        Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                        if (!subject2.getForm().isEmpty() && subject2.getForm() != null) {
                                            sentenceRelationId.setSubject(subject2);
                                            sentenceRelationId.setObject(objectToken);
                                            sentenceRelationId.setRelation(" (VPP,(mod||dep))+(P+D,dep)+(NC,prep)+(ADJ,mod)");
                                            sentenceRelationId.setSentence_text(line);
                                            sentenceRelationId.setConfidence(1);
                                            sentenceRelationId.setType(SentenceRelationType.hasComponent);
                                            sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                            sentenceRelation.setMethod(SentenceRelationMethod.rules);
                                            list_result.add(sentenceRelation);
                                        }

                                    }
                                }
                                if (!annotated) {
                                    Token objectToken = new Token();
                                    objectToken.setForm(ptt_test.getToken().toString());
                                    Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                    // if (!subject2.getForm().isEmpty() && subject2.getForm() != null) {
                                    SentenceRelation sentenceRelation = new SentenceRelation();
                                    SentenceRelationId sentenceRelationId = new SentenceRelationId();
                                    System.out.println("subject2: enter condition" + subject2);
                                    sentenceRelationId.setSubject(subject2);
                                    sentenceRelationId.setObject(objectToken);
                                    sentenceRelationId.setRelation("(VPP, mod)(P+D,dep)+(NC,prep)");
                                    sentenceRelationId.setSentence_text(line);
                                    sentenceRelationId.setConfidence(1);
                                    sentenceRelationId.setType(SentenceRelationType.hasComponent);
                                    sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                    sentenceRelation.setMethod(SentenceRelationMethod.rules);
                                    list_result.add(sentenceRelation);
                                    // }
                                }
                            }

                            if (ptt_test.getTag().toString().equalsIgnoreCase("CC")
                                    && (parseConfiguration.getGoverningDependency(ptt_test).getLabel().equalsIgnoreCase("coord"))) {
                                List<PosTaggedToken> pos_tagged_token_test4 = parseConfiguration.getDependents(ptt_test);
                                for (PosTaggedToken ptt_test4 : pos_tagged_token_test4) {
                                    if (ptt_test4.getTag().toString().equalsIgnoreCase("P")
                                            && parseConfiguration.getGoverningDependency(ptt_test4).getLabel().equalsIgnoreCase("dep_coord")) {
                                        List<PosTaggedToken> pos_tagged_token_test5 = parseConfiguration.getDependents(ptt_test4);
                                        for (PosTaggedToken ptt_test5 : pos_tagged_token_test5) {
                                            if (ptt_test5.getTag().toString().equalsIgnoreCase("NC")
                                                    && parseConfiguration.getGoverningDependency(ptt_test5).getLabel().equalsIgnoreCase("prep")
                                                    && (ptt_test5.getComment()==null||ptt_test5.getComment().isEmpty())) {
                                                Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                                if (!subject2.getForm().isEmpty() && subject2.getForm() != null) {
                                                    SentenceRelation sentenceRelation = new SentenceRelation();
                                                    SentenceRelationId sentenceRelationId = new SentenceRelationId();
                                                    Token objectToken = new Token();
                                                    objectToken.setForm(ptt_test5.getToken().toString());
                                                    sentenceRelationId.setSubject(subject2);
                                                    sentenceRelationId.setObject(objectToken);
                                                    sentenceRelationId.setRelation("(VPP,(mod||dep))+(P+D,dep)+(CC,coord)+(P,dep_coord)+(NC,prep)");
                                                    sentenceRelationId.setSentence_text(line);
                                                    sentenceRelationId.setConfidence(1);
                                                    sentenceRelationId.setType(SentenceRelationType.hasComponent);
                                                    sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                                    sentenceRelation.setMethod(SentenceRelationMethod.rules);
                                                    list_result.add(sentenceRelation);
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                        }
                    }

                    if (ptt.getTag().toString().equalsIgnoreCase("P")
                            && (parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("de_obj"))) {
                        // liste de dependence
                        List<PosTaggedToken> pos_tagged_token_test = parseConfiguration.getDependents(ptt);
                        for (PosTaggedToken ptt_test : pos_tagged_token_test) {
                            if (ptt_test.getTag().toString().equalsIgnoreCase("NC")
                                    && (parseConfiguration.getGoverningDependency(ptt_test).getLabel().equalsIgnoreCase("prep"))
                                    && (ptt_test.getComment()==null||ptt_test.getComment().isEmpty())) {
                                List<PosTaggedToken> pos_tagged_token_test1 = parseConfiguration.getDependents(ptt_test);
                                boolean annotated = false;
                                for (PosTaggedToken ptt_test1 : pos_tagged_token_test1) {
                                    if (ptt_test1.getTag().toString().equalsIgnoreCase("ADJ")
                                            && (parseConfiguration.getGoverningDependency(ptt_test1).getLabel().equalsIgnoreCase("mod"))) {
                                        annotated = true;
                                        Token objectToken = new Token();
                                        objectToken.setForm(ptt_test.getToken().toString());
                                        SentenceRelation sentenceRelation = new SentenceRelation();
                                        SentenceRelationId sentenceRelationId = new SentenceRelationId();
                                        Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                        sentenceRelationId.setSubject(subject2);
                                        sentenceRelationId.setObject(objectToken);
                                        sentenceRelationId.setRelation("(VPP,mod)+(P,de_obj)+(NC,prep)+(ADJ,mod)");
                                        sentenceRelationId.setSentence_text(line);
                                        sentenceRelationId.setConfidence(1);
                                        sentenceRelationId.setType(SentenceRelationType.hasComponent);
                                        sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                        sentenceRelation.setMethod(SentenceRelationMethod.rules);
                                        list_result.add(sentenceRelation);

                                    }
                                }
                                    if (!annotated) {
                                        Token objectToken = new Token();
                                        objectToken.setForm(ptt_test.getToken().toString());
                                        SentenceRelation sentenceRelation = new SentenceRelation();
                                        SentenceRelationId sentenceRelationId = new SentenceRelationId();
                                        Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                        sentenceRelationId.setSubject(subject2);
                                        sentenceRelationId.setObject(objectToken);
                                        sentenceRelationId.setRelation("(VPP,mod)+(P,de_obj)+(NC,prep)");
                                        sentenceRelationId.setSentence_text(line);
                                        sentenceRelationId.setConfidence(1);
                                        sentenceRelationId.setType(SentenceRelationType.hasComponent);
                                        sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                        sentenceRelation.setMethod(SentenceRelationMethod.rules);
                                        list_result.add(sentenceRelation);
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
                            if (ptt_test.getTag().toString().equalsIgnoreCase("NC")
                                    && (parseConfiguration.getGoverningDependency(ptt_test).getLabel().equalsIgnoreCase("prep"))
                                    && (ptt_test.getComment()==null||ptt_test.getComment().isEmpty())) {
                                boolean annotated = false;
                                List<PosTaggedToken> pos_tagged_token_test1 = parseConfiguration.getDependents(ptt_test);
                                for (PosTaggedToken ptt_test1 : pos_tagged_token_test1) {
                                    if (ptt_test1.getTag().toString().equalsIgnoreCase("ADJ")
                                            && (parseConfiguration.getGoverningDependency(ptt_test1).getLabel().equalsIgnoreCase("mod"))) {
                                        annotated = true;
                                        Token objectToken = new Token();
                                        objectToken.setForm(ptt_test.getToken().toString());
                                        SentenceRelation sentenceRelation = new SentenceRelation();
                                        SentenceRelationId sentenceRelationId = new SentenceRelationId();
                                        Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                        sentenceRelationId.setSubject(subject2);
                                        sentenceRelationId.setObject(objectToken);
                                        sentenceRelationId.setRelation("(VPP,mod)+(P,de_obj)+(NC,prep)+(ADJ,mod)");
                                        sentenceRelationId.setSentence_text(line);
                                        sentenceRelationId.setConfidence(1);
                                        sentenceRelationId.setType(SentenceRelationType.hasComponent);
                                        sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                        sentenceRelation.setMethod(SentenceRelationMethod.rules);
                                        list_result.add(sentenceRelation);

                                    }
                                }
                                    if (!annotated) {
                                        Token objectToken = new Token();
                                        objectToken.setForm(ptt_test.getToken().toString());
                                        SentenceRelation sentenceRelation = new SentenceRelation();
                                        SentenceRelationId sentenceRelationId = new SentenceRelationId();
                                        Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                        sentenceRelationId.setSubject(subject2);
                                        sentenceRelationId.setObject(objectToken);
                                        sentenceRelationId.setRelation("(VPP,mod)+(P,de_obj)+(NC,prep)");
                                        sentenceRelationId.setSentence_text(line);
                                        sentenceRelationId.setConfidence(1);
                                        sentenceRelationId.setType(SentenceRelationType.hasComponent);
                                        sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                        sentenceRelation.setMethod(SentenceRelationMethod.rules);
                                        list_result.add(sentenceRelation);
                                }
                            }
                        }
                    }
                }
            }
            /*---------------------------------(VPP,(mod||dep))+(P,p_obj)(NC,prep)(NC,mod)(CC,coord)(NC,dep_coord)+(P,dep)+(NC,prep)+(ADJ,mod)----------------------*/
            /*---------------------------------(VPP,(mod||dep))+(P,p_obj)(NC,prep)(NC,mod)(CC,coord)(NC,dep_coord)+(P,dep)+(NC,prep)----------------------*/
            if (posTagSequence.get(i).getTag().toString().equalsIgnoreCase("VPP")
                    && (parseConfiguration.getGoverningDependency(posTagSequence.get(i)).getLabel().equalsIgnoreCase("mod")
                    || parseConfiguration.getGoverningDependency(posTagSequence.get(i)).getLabel().equalsIgnoreCase("dep"))) {
                List<PosTaggedToken> pos_tagged_token = parseConfiguration.getDependents(posTagSequence.get(i));
                for (PosTaggedToken ptt : pos_tagged_token) {
                    if (ptt.getTag().toString().equalsIgnoreCase("P")
                            && (parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("p_obj"))) {
                        // liste de dependence
                        List<PosTaggedToken> pos_tagged_token_test = parseConfiguration.getDependents(ptt);
                        for (PosTaggedToken ptt_test : pos_tagged_token_test) {
                            if (ptt_test.getTag().toString().equalsIgnoreCase("NC")
                                    && (parseConfiguration.getGoverningDependency(ptt_test).getLabel().equalsIgnoreCase("prep"))
                                    && (ptt_test.getComment()==null||ptt_test.getComment().isEmpty())) {
                                List<PosTaggedToken> pos_tagged_token_test_0 = parseConfiguration.getDependents(ptt_test);
                                for (PosTaggedToken ptt_test_0 : pos_tagged_token_test_0) {
                                    if (ptt_test_0.getTag().toString().equalsIgnoreCase("NC")
                                            && (parseConfiguration.getGoverningDependency(ptt_test_0).getLabel().equalsIgnoreCase("mod"))
                                            && (ptt_test_0.getComment()==null||ptt_test_0.getComment().isEmpty())) {
                                        List<PosTaggedToken> pos_tagged_token_test_1 = parseConfiguration.getDependents(ptt_test_0);
                                        for (PosTaggedToken ptt_test_1 : pos_tagged_token_test_1) {
                                            if (ptt_test_1.getTag().toString().equalsIgnoreCase("CC")
                                                    && (parseConfiguration.getGoverningDependency(ptt_test_1).getLabel().equalsIgnoreCase("coord"))
                                                    && ptt_test_1.getComment().isEmpty()) {
                                                List<PosTaggedToken> pos_tagged_token_test_2 = parseConfiguration.getDependents(ptt_test_1);
                                                for (PosTaggedToken ptt_test_2 : pos_tagged_token_test_2) {
                                                    if (ptt_test_2.getTag().toString().equalsIgnoreCase("NC")
                                                            && (parseConfiguration.getGoverningDependency(ptt_test_2).getLabel().equalsIgnoreCase("dep_coord"))
                                                            &&(ptt_test_2.getComment()==null||ptt_test_2.getComment().isEmpty())) {
                                                        List<PosTaggedToken> pos_tagged_token_test_3 = parseConfiguration.getDependents(ptt_test_2);
                                                        for (PosTaggedToken ptt_test_3 : pos_tagged_token_test_3) {
                                                            if (ptt_test_3.getTag().toString().equalsIgnoreCase("P")
                                                                    && (parseConfiguration.getGoverningDependency(ptt_test_3).getLabel().equalsIgnoreCase("dep"))) {
                                                                List<PosTaggedToken> pos_tagged_token_test_4 = parseConfiguration.getDependents(ptt_test_3);
                                                                for (PosTaggedToken ptt_test_4 : pos_tagged_token_test_4) {
                                                                    if (ptt_test_4.getTag().toString().equalsIgnoreCase("NC")
                                                                            && (parseConfiguration.getGoverningDependency(ptt_test_4).getLabel().equalsIgnoreCase("prep"))
                                                                            && (ptt_test_4.getComment()==null||ptt_test_4.getComment().isEmpty())) {
                                                                        boolean annotated=false;
                                                                        List<PosTaggedToken> pos_tagged_token_test_5 = parseConfiguration.getDependents(ptt_test_4);
                                                                        for (PosTaggedToken ptt_test_5 : pos_tagged_token_test_5) {
                                                                            if (ptt_test_5.getTag().toString().equalsIgnoreCase("ADJ") && (parseConfiguration.getGoverningDependency(ptt_test_5).getLabel().equalsIgnoreCase("mod"))) {
                                                                                annotated = true;
                                                                                Token objectToken = new Token();
                                                                                objectToken.setForm(ptt_test_2.getToken().toString() + " " + ptt_test_3.getToken().toString() + " " + ptt_test_4.getToken().toString() + " " + ptt_test_5.getToken().toString());
                                                                                SentenceRelation sentenceRelation = new SentenceRelation();
                                                                                SentenceRelationId sentenceRelationId = new SentenceRelationId();
                                                                                Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                                                                sentenceRelationId.setSubject(subject2);
                                                                                sentenceRelationId.setObject(objectToken);
                                                                                sentenceRelationId.setRelation("(VPP,(mod||dep))+(P,p_obj)(NC,prep)(NC,mod)(CC,coord)(NC,dep_coord)+(P,dep)+(NC,prep)+(ADJ,mod)");
                                                                                sentenceRelationId.setSentence_text(line);
                                                                                sentenceRelationId.setConfidence(1);
                                                                                sentenceRelationId.setType(SentenceRelationType.hasComponent);
                                                                                sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                                                                sentenceRelation.setMethod(SentenceRelationMethod.rules);
                                                                                list_result.add(sentenceRelation);
                                                                            }
                                                                        }
                                                                            if(!annotated){
                                                                                Token objectToken = new Token();
                                                                                objectToken.setForm(ptt_test_0.getToken().toString());
                                                                                SentenceRelation sentenceRelation = new SentenceRelation();
                                                                                SentenceRelationId sentenceRelationId = new SentenceRelationId();
                                                                                Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                                                                System.out.println("subject2: enter condition" + subject2);
                                                                                sentenceRelationId.setSubject(subject2);
                                                                                sentenceRelationId.setObject(objectToken);
                                                                                sentenceRelationId.setRelation("(VPP,(mod||dep))+(P,p_obj)(NC,prep)(NC,mod)(CC,coord)(NC,dep_coord)+(P,dep)+(NC,prep))");
                                                                                sentenceRelationId.setSentence_text(line);
                                                                                sentenceRelationId.setConfidence(1);
                                                                                sentenceRelationId.setType(SentenceRelationType.hasComponent);
                                                                                sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                                                                sentenceRelation.setMethod(SentenceRelationMethod.rules);
                                                                                list_result.add(sentenceRelation);

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
                                    if (ptt_test.getTag().toString().equalsIgnoreCase("P")
                                            && parseConfiguration.getGoverningDependency(ptt_test).getLabel().equalsIgnoreCase("dep")) {

                                        List<PosTaggedToken> pos_tagged_token_test_1 = parseConfiguration.getDependents(ptt_test);
                                        for (PosTaggedToken ptt_test_1 : pos_tagged_token_test_1) {
                                            if (ptt_test_1.getTag().toString().equalsIgnoreCase("NC")
                                                    && (parseConfiguration.getGoverningDependency(ptt_test_1).getLabel().equalsIgnoreCase("prep"))
                                                    && (ptt_test_1.getComment()==null||ptt_test_1.getComment().isEmpty())) {
                                                Token objectToken = new Token();
                                                objectToken.setForm(ptt_test_1.getToken().toString());
                                                SentenceRelation sentenceRelation = new SentenceRelation();
                                                SentenceRelationId sentenceRelationId = new SentenceRelationId();
                                                Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                                sentenceRelationId.setSubject(subject2);
                                                sentenceRelationId.setObject(objectToken);
                                                sentenceRelationId.setRelation("(P,dep)(NC,prep)");
                                                sentenceRelationId.setSentence_text(line);
                                                sentenceRelationId.setConfidence(1);
                                                sentenceRelationId.setType(SentenceRelationType.hasComponent);
                                                sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                                sentenceRelation.setMethod(SentenceRelationMethod.rules);
                                                list_result.add(sentenceRelation);
                                            }

                                        }
                                    }

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
                                            if (ptt_test_1.getTag().toString().equalsIgnoreCase("P")
                                                    && (parseConfiguration.getGoverningDependency(ptt_test_1).getLabel().equalsIgnoreCase("dep"))
                                                    && (ptt_test_1.getComment() == null || ptt_test_1.getComment().isEmpty())) {
                                                List<PosTaggedToken> pos_tagged_token_test_2 = parseConfiguration.getDependents(ptt_test_1);
                                                for (PosTaggedToken ptt_test_2 : pos_tagged_token_test_2) {
                                                    if (ptt_test_2.getTag().toString().equalsIgnoreCase("NC")
                                                            && (parseConfiguration.getGoverningDependency(ptt_test_2).getLabel().equalsIgnoreCase("prep"))
                                                            && (ptt_test_2.getComment() == null || ptt_test_2.getComment().isEmpty())) {
                                                        Token objectToken = new Token();
                                                        objectToken.setForm(ptt_test_2.getToken().toString());
                                                        SentenceRelation sentenceRelation = new SentenceRelation();
                                                        SentenceRelationId sentenceRelationId = new SentenceRelationId();
                                                        Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                                        sentenceRelationId.setSubject(subject2);
                                                        sentenceRelationId.setObject(objectToken);
                                                        sentenceRelationId.setRelation("(VPP,mod||dep)+(P,p_obj)+(P,dep)(NC,prep)");
                                                        sentenceRelationId.setSentence_text(line);
                                                        sentenceRelationId.setConfidence(1);
                                                        sentenceRelationId.setType(SentenceRelationType.hasComponent);
                                                        sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                                        sentenceRelation.setMethod(SentenceRelationMethod.rules);
                                                        list_result.add(sentenceRelation);
                                                    }
                                                }
                                            }

                                            if (ptt_test_1.getTag().toString().equalsIgnoreCase("NC")
                                                    && (parseConfiguration.getGoverningDependency(ptt_test_1).getLabel().equalsIgnoreCase("mod"))
                                                    && (ptt_test_1.getComment() == null || ptt_test_1.getComment().isEmpty())) {
                                                boolean annotated=false;
                                                List<PosTaggedToken> pos_tagged_token_test_2 = parseConfiguration.getDependents(ptt_test_1);
                                                for (PosTaggedToken ptt_test_2 : pos_tagged_token_test_2) {
                                                    if (ptt_test_2.getTag().toString().equalsIgnoreCase("NC")
                                                            && (parseConfiguration.getGoverningDependency(ptt_test_2).getLabel().equalsIgnoreCase("prep"))
                                                            && (ptt_test_2.getComment() == null || ptt_test_2.getComment().isEmpty())) {
                                                        annotated=true;
                                                        Token objectToken = new Token();
                                                        objectToken.setForm(ptt_test_1.getToken().toString()+" "+ptt_test_2.getToken().toString());
                                                        SentenceRelation sentenceRelation = new SentenceRelation();
                                                        SentenceRelationId sentenceRelationId = new SentenceRelationId();
                                                        Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                                        sentenceRelationId.setSubject(subject2);
                                                        sentenceRelationId.setObject(objectToken);
                                                        sentenceRelationId.setRelation("(VPP,mod||dep)+(P,p_obj)+(P,dep)(NC,prep)");
                                                        sentenceRelationId.setSentence_text(line);
                                                        sentenceRelationId.setConfidence(1);
                                                        sentenceRelationId.setType(SentenceRelationType.hasComponent);
                                                        sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                                        sentenceRelation.setMethod(SentenceRelationMethod.rules);
                                                        list_result.add(sentenceRelation);
                                                    }
                                        }
                                                if(!annotated){
                                                    Token objectToken = new Token();
                                                    objectToken.setForm(ptt_test_1.getToken().toString());
                                                    SentenceRelation sentenceRelation = new SentenceRelation();
                                                    SentenceRelationId sentenceRelationId = new SentenceRelationId();
                                                    Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                                    sentenceRelationId.setSubject(subject2);
                                                    sentenceRelationId.setObject(objectToken);
                                                    sentenceRelationId.setRelation("(VPP,mod||dep)+(P,p_obj)+(P,dep)(NC,prep)");
                                                    sentenceRelationId.setSentence_text(line);
                                                    sentenceRelationId.setConfidence(1);
                                                    sentenceRelationId.setType(SentenceRelationType.hasComponent);
                                                    sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                                    sentenceRelation.setMethod(SentenceRelationMethod.rules);
                                                    list_result.add(sentenceRelation);
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
                                                        Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                                        SentenceRelation sentenceRelation = new SentenceRelation();
                                                        SentenceRelationId sentenceRelationId = new SentenceRelationId();
                                                        Token objectToken = new Token();
                                                        objectToken.setForm(ptt_test1.getToken().toString() + " " + ptt_test2.getToken().toString() + " " + ptt_test3.getToken().toString());
                                                        sentenceRelationId.setSubject(subject2);
                                                        sentenceRelationId.setObject(objectToken);
                                                        sentenceRelationId.setRelation("(VPP,root)+(NC,obj)+(P,dep)+(NC,prep)+(P,dep)+(VInf,prep)");
                                                        sentenceRelationId.setSentence_text(line);
                                                        sentenceRelationId.setConfidence(1);
                                                        sentenceRelationId.setType(SentenceRelationType.hasComponent);
                                                        sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                                        sentenceRelation.setMethod(SentenceRelationMethod.rules);
                                                        list_result.add(sentenceRelation);

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
                                                                                        Token subject3 = extractSubject2(parseConfiguration, posTagSequence, line);
                                                                                        SentenceRelation sentenceRelation3 = new SentenceRelation();
                                                                                        SentenceRelationId sentenceRelationId3 = new SentenceRelationId();
                                                                                        Token objectToken3 = new Token();
                                                                                        objectToken3.setForm(ptt_test5.getToken().toString() + " " + ptt_test6.getToken().toString() + " " + ptt_test7.getToken().toString());
                                                                                        sentenceRelationId3.setSubject(subject3);
                                                                                        sentenceRelationId3.setObject(objectToken3);
                                                                                        sentenceRelationId3.setRelation("(VPP,root)+(NC,obj)+(P,dep)+(NC,prep)+(P,dep)+(VInf,prep)+(P+D,a_obj)+(NC,prep)+(P,dep)+(NC,prep)");
                                                                                        sentenceRelationId3.setSentence_text(line);
                                                                                        sentenceRelationId3.setConfidence(1);
                                                                                        sentenceRelationId3.setType(SentenceRelationType.hasComponent);
                                                                                        sentenceRelation3.setSentenceRelationId(sentenceRelationId3);
                                                                                        sentenceRelation3.setMethod(SentenceRelationMethod.rules);
                                                                                        list_result.add(sentenceRelation3);
                                                                                    }
                                                                                    if (ptt_test7.getTag().toString().equalsIgnoreCase("CC")
                                                                                            && parseConfiguration.getGoverningDependency(ptt_test7).getLabel().equalsIgnoreCase("coord")) {
                                                                                        List<PosTaggedToken> pos_tagged_token_test8 = parseConfiguration.getDependents(ptt_test7);
                                                                                        for (PosTaggedToken ptt_test8 : pos_tagged_token_test8) {
                                                                                            if (ptt_test8.getTag().toString().equalsIgnoreCase("P")
                                                                                                    && parseConfiguration.getGoverningDependency(ptt_test8).getLabel().equalsIgnoreCase("dep_coord")) {
                                                                                                List<PosTaggedToken> pos_tagged_token_test9 = parseConfiguration.getDependents(ptt_test8);
                                                                                                for (PosTaggedToken ptt_test9 : pos_tagged_token_test9) {
                                                                                                    if (ptt_test9.getTag().toString().equalsIgnoreCase("NC") && parseConfiguration.getGoverningDependency(ptt_test9).getLabel().equalsIgnoreCase("prep")
                                                                                                            && (ptt_test9.getComment() == null || ptt_test9.getComment().isEmpty())) {
                                                                                                        Token subject3 = extractSubject2(parseConfiguration, posTagSequence, line);
                                                                                                        SentenceRelation sentenceRelation3 = new SentenceRelation();
                                                                                                        SentenceRelationId sentenceRelationId3 = new SentenceRelationId();
                                                                                                        Token objectToken3 = new Token();
                                                                                                        objectToken3.setForm(ptt_test9.getToken().toString());
                                                                                                        sentenceRelationId3.setSubject(subject3);
                                                                                                        sentenceRelationId3.setObject(objectToken3);
                                                                                                        sentenceRelationId3.setRelation("(VPP,root)+(NC,obj)+(P,dep)+(NC,prep)+(P,dep)+(VInf,prep)+(P+D,a_obj)+(NC,prep)+(P,dep)+(CC,coord)+(P,dep_coord)+(NC,prep)");
                                                                                                        sentenceRelationId3.setSentence_text(line);
                                                                                                        sentenceRelationId3.setConfidence(1);
                                                                                                        sentenceRelationId3.setType(SentenceRelationType.hasComponent);
                                                                                                        sentenceRelation3.setSentenceRelationId(sentenceRelationId3);
                                                                                                        sentenceRelation3.setMethod(SentenceRelationMethod.rules);
                                                                                                        list_result.add(sentenceRelation3);
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
                                                            if (ptt_test3.getTag().toString().equalsIgnoreCase("ADJ") &&
                                                                    parseConfiguration.getGoverningDependency(ptt_test3).getLabel().equalsIgnoreCase("mod")) {
                                                                annotated = true;
                                                                Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                                                SentenceRelation sentenceRelation = new SentenceRelation();
                                                                SentenceRelationId sentenceRelationId = new SentenceRelationId();
                                                                Token objectToken = new Token();
                                                                objectToken.setForm(ptt_test2.getToken().toString() + " " + ptt_test3.getToken().toString());
                                                                sentenceRelationId.setSubject(subject2);
                                                                sentenceRelationId.setObject(objectToken);
                                                                sentenceRelationId.setRelation("(VPP,root)+(P+D,de_obj)+(NC,prep)+(P,dep)+(NC,prep)+(ADJ,mod)");
                                                                sentenceRelationId.setSentence_text(line);
                                                                sentenceRelationId.setConfidence(1);
                                                                sentenceRelationId.setType(SentenceRelationType.hasComponent);
                                                                sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                                                sentenceRelation.setMethod(SentenceRelationMethod.rules);
                                                                list_result.add(sentenceRelation);
                                                            }
                                                            //TODO (à verifier)
                                                            if (!annotated) {
                                                                SentenceRelation sentenceRelation = new SentenceRelation();
                                                                SentenceRelationId sentenceRelationId = new SentenceRelationId();
                                                                Token objectToken = new Token();
                                                                Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                                                objectToken.setForm(ptt_test2.getToken().toString());
                                                                sentenceRelationId.setSubject(subject2);
                                                                sentenceRelationId.setObject(objectToken);
                                                                sentenceRelationId.setRelation("(VPP,root)+(P+D,de_obj)+(NC,prep)+(P,dep)+(NC,prep)");
                                                                sentenceRelationId.setSentence_text(line);
                                                                sentenceRelationId.setConfidence(1);
                                                                sentenceRelationId.setType(SentenceRelationType.hasComponent);
                                                                sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                                                sentenceRelation.setMethod(SentenceRelationMethod.rules);
                                                                list_result.add(sentenceRelation);
                                                            }

                                                            if (ptt_test3.getTag().toString().equalsIgnoreCase("CC") &&
                                                                    parseConfiguration.getGoverningDependency(ptt_test3).getLabel().equalsIgnoreCase("coord")) {

                                                                List<PosTaggedToken> pos_tagged_token_test4 = parseConfiguration.getDependents(ptt_test3);
                                                                for (PosTaggedToken ptt_test4 : pos_tagged_token_test4) {
                                                                    if (ptt_test4.getTag().toString().equalsIgnoreCase("NC") &&
                                                                            parseConfiguration.getGoverningDependency(ptt_test4).getLabel().equalsIgnoreCase("dep_coord")
                                                                            && (ptt_test4.getComment() == null || ptt_test4.getComment().isEmpty())) {
                                                                        List<PosTaggedToken> pos_tagged_token_test5 = parseConfiguration.getDependents(ptt_test4);

                                                                        for (PosTaggedToken ptt_test5 : pos_tagged_token_test5) {
                                                                            if (ptt_test5.getTag().toString().equalsIgnoreCase("P") &&
                                                                                    parseConfiguration.getGoverningDependency(ptt_test5).getLabel().equalsIgnoreCase("dep")) {
                                                                                List<PosTaggedToken> pos_tagged_token_test6 = parseConfiguration.getDependents(ptt_test5);

                                                                                for (PosTaggedToken ptt_test6 : pos_tagged_token_test6) {
                                                                                    if (ptt_test6.getTag().toString().equalsIgnoreCase("NPP") &&
                                                                                            parseConfiguration.getGoverningDependency(ptt_test6).getLabel().equalsIgnoreCase("Prep")
                                                                                            && (ptt_test6.getComment() == null || ptt_test6.getComment().isEmpty())) {
                                                                                        List<PosTaggedToken> pos_tagged_token_test7 = parseConfiguration.getDependents(ptt_test6);
                                                                                        for (PosTaggedToken ptt_test7 : pos_tagged_token_test7) {
                                                                                            if (ptt_test7.getTag().toString().equalsIgnoreCase("NPP") &&
                                                                                                    parseConfiguration.getGoverningDependency(ptt_test7).getLabel().equalsIgnoreCase("mod")
                                                                                                    && (ptt_test7.getComment() == null || ptt_test7.getComment().isEmpty())) {
                                                                                                Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                                                                                SentenceRelation sentenceRelation = new SentenceRelation();
                                                                                                SentenceRelationId sentenceRelationId = new SentenceRelationId();
                                                                                                Token objectToken = new Token();
                                                                                                objectToken.setForm(ptt_test6.getToken().toString() + " " + ptt_test7.getToken().toString());
                                                                                                sentenceRelationId.setSubject(subject2);
                                                                                                sentenceRelationId.setObject(objectToken);
                                                                                                sentenceRelationId.setRelation("(VPP,root)+(P+D,de_obj)+(NC,prep)+(P,dep)+(NC,prep)(CC,coord)+(NC,dep_coord)+(P,dep)+(NPP,prep)+(NPP,mod)");
                                                                                                sentenceRelationId.setSentence_text(line);
                                                                                                sentenceRelationId.setConfidence(1);
                                                                                                sentenceRelationId.setType(SentenceRelationType.hasComponent);
                                                                                                sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                                                                                sentenceRelation.setMethod(SentenceRelationMethod.rules);
                                                                                                list_result.add(sentenceRelation);

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
                    }

                            if (posTagSequence.get(i).getTag().toString().equalsIgnoreCase("VPP")
                                    && parseConfiguration.getGoverningDependency(posTagSequence.get(i)).getLabel().equalsIgnoreCase("root")) {
                                List<PosTaggedToken> pos_tagged_token = parseConfiguration.getDependents(posTagSequence.get(i));
                                for (PosTaggedToken ptt : pos_tagged_token) {

                    if (ptt.getTag().toString().equalsIgnoreCase("CC") && parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("coord")) {
                        List<PosTaggedToken> pos_tagged_token_test = parseConfiguration.getDependents(ptt);
                        for (PosTaggedToken ptt_test : pos_tagged_token_test) {
                            if (ptt_test.getTag().toString().equalsIgnoreCase("NC") && parseConfiguration.getGoverningDependency(ptt_test).getLabel().equalsIgnoreCase("dep_coord")
                                    &&( ptt_test.getComment()==null||ptt_test.getComment().isEmpty())) {
                                boolean annotated = false;
                                List<PosTaggedToken> pos_tagged_token_test3 = parseConfiguration.getDependents(ptt_test);
                                for (PosTaggedToken ptt_test3 : pos_tagged_token_test3) {
                                    if (ptt_test3.getTag().toString().equalsIgnoreCase("ADJ") &&
                                            parseConfiguration.getGoverningDependency(ptt_test3).getLabel().equalsIgnoreCase("mod")) {
                                        annotated = true;
                                        Token objectToken = new Token();
                                        objectToken.setForm(ptt_test.getToken().toString() + " " + ptt_test3.getToken().toString());
                                        SentenceRelation sentenceRelation = new SentenceRelation();
                                        SentenceRelationId sentenceRelationId = new SentenceRelationId();
                                        Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                        sentenceRelationId.setSubject(subject2);
                                        sentenceRelationId.setObject(objectToken);
                                        sentenceRelationId.setRelation("(VPP,root)+(CC,coord)+ (NC,dep_coord)+(ADJ,mod)");
                                        sentenceRelationId.setSentence_text(line);
                                        sentenceRelationId.setType(SentenceRelationType.hasComponent);
                                        sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                        sentenceRelation.setMethod(SentenceRelationMethod.rules);
                                        list_result.add(sentenceRelation);
                                   }
                                }
                                    if (!annotated) {
                                        Token objectToken = new Token();
                                        objectToken.setForm(ptt_test.getToken().toString());
                                        SentenceRelation sentenceRelation = new SentenceRelation();
                                        SentenceRelationId sentenceRelationId = new SentenceRelationId();
                                        Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                        sentenceRelationId.setSubject(subject2);
                                        sentenceRelationId.setObject(objectToken);
                                        sentenceRelationId.setRelation("(VPP,root)+(CC,coord)+ (NC,dep_coord)");
                                        sentenceRelationId.setSentence_text(line);
                                        sentenceRelationId.setType(SentenceRelationType.hasComponent);
                                        sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                        sentenceRelation.setMethod(SentenceRelationMethod.rules);
                                        list_result.add(sentenceRelation);


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
                System.out.println(pos_tagged_token);
                for (PosTaggedToken ptt : pos_tagged_token) {
                    if (ptt.getTag().toString().equalsIgnoreCase("P")
                            && parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("mod")) {
                        List<PosTaggedToken> pos_tagged_token_test = parseConfiguration.getDependents(ptt);
                        for (PosTaggedToken ptt_test : pos_tagged_token_test) {
                            if (ptt_test.getTag().toString().equalsIgnoreCase("NPP")
                                    && parseConfiguration.getGoverningDependency(ptt_test).getLabel().equalsIgnoreCase("prep")
                                    && (ptt_test.getComment()==null||ptt_test.getComment().isEmpty())) {
                                boolean annotated=false;
                                List<PosTaggedToken> pos_tagged_token_test1 = parseConfiguration.getDependents(ptt_test);
                                for (PosTaggedToken ptt_test1 : pos_tagged_token_test1) {
                                   if (ptt_test1.getTag().toString().equalsIgnoreCase("NPP")
                                            && parseConfiguration.getGoverningDependency(ptt_test1).getLabel().equalsIgnoreCase("mod")
                                            && (ptt_test1.getComment()==null||ptt_test1.getComment().isEmpty())) {
                                        annotated=true;
                                        Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                        SentenceRelation sentenceRelation = new SentenceRelation();
                                        SentenceRelationId sentenceRelationId = new SentenceRelationId();
                                        Token objectToken = new Token();
                                        objectToken.setForm(ptt_test.getToken().toString() + " " + ptt_test1.getToken().toString());
                                        sentenceRelationId.setSubject(subject2);
                                        sentenceRelationId.setObject(objectToken);
                                        sentenceRelationId.setRelation("(VPP,root)+(P,mod)+(NPP,prep)+(NPP,mod)");
                                        sentenceRelationId.setSentence_text(line);
                                        sentenceRelationId.setConfidence(1);
                                        sentenceRelationId.setType(SentenceRelationType.hasComponent);
                                        sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                        sentenceRelation.setMethod(SentenceRelationMethod.rules);
                                        list_result.add(sentenceRelation);
                                    }
                                }
                                if(!annotated){
                                    Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                    SentenceRelation sentenceRelation = new SentenceRelation();
                                    SentenceRelationId sentenceRelationId = new SentenceRelationId();
                                    Token objectToken = new Token();
                                    objectToken.setForm(ptt_test.getToken().toString());
                                    sentenceRelationId.setSubject(subject2);
                                    sentenceRelationId.setObject(objectToken);
                                    sentenceRelationId.setRelation("(VPP,root)+(P,mod)+(NPP,prep)");
                                    sentenceRelationId.setSentence_text(line);
                                    sentenceRelationId.setConfidence(1);
                                    sentenceRelationId.setType(SentenceRelationType.hasComponent);
                                    sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                    sentenceRelation.setMethod(SentenceRelationMethod.rules);
                                    list_result.add(sentenceRelation);
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
