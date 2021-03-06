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
public class RelationHasFragranceCreatorBasedOnRenco_Holmes extends AbstractRelationExtraction {


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
                                    //System.out.println("form: " + form + " with underscor: " + form_with_underscore);
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
           // System.out.println(cne.getEntityString() + " type: " + cne.getEntityType());
            if (cne.getEntityType().equalsIgnoreCase("PER")) {
                String form = cne.getEntityString();
                if (form.contains("(")) {
                    form = form.substring(0, form.indexOf("(")).trim();
                }
                String form_with_underscore = form.replaceAll("\\s", "_");
               // System.out.println("form: " + form + " with underscor: " + form_with_underscore);
                //newline = newline;
                newline = newline.replace(form, form_with_underscore);
            }
            // System.out.println(cne.getEntityString() +" "+cne.getEntityType()+ " "+ cne.getSpanFrom()+ " "+ cne.getSpanTo()+" "+ cne.getScore());

        }
        //System.out.println("newline: " + newline);
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

        //System.out.println("test: " + parseConfiguration.getDependencies());

        // extractSubject(parseConfiguration, posTagSequence, line);
        relationRules(parseConfiguration, posTagSequence, line);
    }

    public List<Token> extractSubject2(ParseConfiguration parseConfiguration, PosTagSequence posTagSequence, String line) {

        List<Token> subject_list=new ArrayList<>();
        for (int i = 0; i < posTagSequence.size(); i++) {
            if (posTagSequence.get(i).getLexicalEntry() != null) {

                //(V,sub)+(NPP,suj)
                if (posTagSequence.get(i).getTag().toString().equalsIgnoreCase("V") && parseConfiguration.getGoverningDependency(posTagSequence.get(i)).getLabel().equalsIgnoreCase("sub")) {
                    List<PosTaggedToken> pos_tagged_token_test_1 = parseConfiguration.getDependents(posTagSequence.get(i));
                    for (PosTaggedToken ptt_test1 : pos_tagged_token_test_1) {
                        if (ptt_test1.getTag().toString().equalsIgnoreCase("NPP") && parseConfiguration.getGoverningDependency(ptt_test1).getLabel().equalsIgnoreCase("suj")
                                && (ptt_test1.getComment().equalsIgnoreCase("product"))) {
                            Token subject2 = new Token();
                            subject2.setForm(ptt_test1.getToken().toString());
                            subject2.setType(ptt_test1.getComment());
                            subject_list.add(subject2);
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
                                                                        &&  (ptt_test6.getComment().equalsIgnoreCase("product"))) {
                                                                    Token subject2 = new Token();
                                                                    subject2.setForm(ptt_test6.getToken().toString());
                                                                    subject2.setType(ptt_test6.getComment());


                                                                    subject_list.add(subject2);
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
                                                            Token subject2 = new Token();
                                                            subject2.setForm(ptt_test6.getToken().toString());
                                                            subject2.setType(ptt_test6.getComment());

                                                            subject_list.add(subject2);                                                        }
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
                                                    Token subject2 = new Token();
                                                    subject2.setForm(ptt_3.getToken().toString());
                                                    subject2.setType(ptt_3.getComment());
                                                    subject_list.add(subject2);
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

                            Token subject2=new Token();
                            subject2.setForm(ptt.getToken().toString());
                            subject2.setType(ptt.getComment());
                           // System.out.println("sujet1: "+subject2.getForm());
                            subject_list.add(subject2);
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
                                    Token subject2 = new Token();
                                    subject2.setForm(ptt_test2.getToken().toString());
                                    subject2.setType(ptt_test2.getComment());
                                    subject_list.add(subject2);
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
                                                        Token subject2 = new Token();
                                                        subject2.setForm(ptt_test4.getToken().toString());
                                                        subject2.setType(ptt_test4.getComment());
                                                        subject_list.add(subject2);
                                                    }

                                                    if (ptt_test4.getTag().toString().equalsIgnoreCase("P") && parseConfiguration.getGoverningDependency(ptt_test4).getLabel().equalsIgnoreCase("dep")
                                                            ) {
                                                        List<PosTaggedToken> pos_tagged_token_test_5 = parseConfiguration.getDependents(ptt_test4);
                                                        for (PosTaggedToken ptt_test5 : pos_tagged_token_test_5) {
                                                            if (ptt_test5.getTag().toString().equalsIgnoreCase("NPP") && parseConfiguration.getGoverningDependency(ptt_test5).getLabel().equalsIgnoreCase("prep")
                                                                    && (ptt_test5.getComment().equalsIgnoreCase("product"))) {
                                                                Token subject2 = new Token();
                                                                subject2.setForm(ptt_test5.getToken().toString());
                                                                subject2.setType(ptt_test5.getComment());
                                                                subject_list.add(subject2);
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
                                && (ptt.getComment().equalsIgnoreCase("product"))) {
                            Token subject2 = new Token();
                            subject2.setForm(ptt.getToken().toString());
                            subject2.setType(ptt.getComment());
                            subject_list.add(subject2);
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
                                                                    Token subject2 = new Token();
                                                                    subject2.setForm(ptt_test6.getToken().toString());
                                                                    subject2.setType(ptt_test6.getComment());
                                                                    subject_list.add(subject2);
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
                                                                                    Token subject2 = new Token();
                                                                                    subject2.setForm(ptt_test8.getToken().toString());
                                                                                    subject2.setType(ptt_test8.getComment());
                                                                                    subject_list.add(subject2);
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
                if ((posTagSequence.get(i).getTag().toString().equalsIgnoreCase("V")||posTagSequence.get(i).getTag().toString().equalsIgnoreCase("VPP"))
                        && parseConfiguration.getGoverningDependency(posTagSequence.get(i)).getLabel().equalsIgnoreCase("root")) {
                    //Liste des dependences de i
                    List<PosTaggedToken> pos_tagged_token = parseConfiguration.getDependents(posTagSequence.get(i));
                    for (PosTaggedToken ptt : pos_tagged_token) {

                        if ((ptt.getTag().toString().equalsIgnoreCase("P") ||ptt.getTag().toString().equalsIgnoreCase("P+D"))
                                && (parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("mod")
                                || parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("p_obj")
                                || parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("a_obj"))) {
                            List<PosTaggedToken> pos_tagged_token_test_2 = parseConfiguration.getDependents(ptt);
                            for (PosTaggedToken ptt_test2 : pos_tagged_token_test_2) {
                                if ((ptt_test2.getTag().toString().equalsIgnoreCase("NPP") || ptt_test2.getTag().toString().equalsIgnoreCase("NC"))
                                        && parseConfiguration.getGoverningDependency(ptt_test2).getLabel().equalsIgnoreCase("prep")
                                        && (ptt_test2.getComment().equalsIgnoreCase("product"))) {
                                    Token subject2=new Token();
                                    subject2.setForm(ptt_test2.getToken().toString());
                                    subject2.setType(ptt_test2.getComment());
                                    subject_list.add(subject2);
                                 //   System.out.println("sujet4: "+subject2.getForm());
                                }
                                if ((ptt_test2.getTag().toString().equalsIgnoreCase("NC"))
                                        && (parseConfiguration.getGoverningDependency(ptt_test2).getLabel().equalsIgnoreCase("prep")
                                        || parseConfiguration.getGoverningDependency(ptt_test2).getLabel().equalsIgnoreCase("obj"))) {
                                    List<PosTaggedToken> pos_tagged_token_test_3 = parseConfiguration.getDependents(ptt_test2);
                                    for (PosTaggedToken ptt_test3 : pos_tagged_token_test_3) {
                                        //TODO (à verifier)
                                        if ((ptt_test3.getTag().toString().equalsIgnoreCase("NPP") || ptt_test3.getTag().toString().equalsIgnoreCase("NC"))
                                                && parseConfiguration.getGoverningDependency(ptt_test3).getLabel().equalsIgnoreCase("mod")
                                                && (ptt_test3.getComment().equalsIgnoreCase("product"))) {
                                            Token subject2 = new Token();
                                            subject2.setForm(ptt_test3.getToken().toString());
                                            subject2.setType(ptt_test3.getComment());
                                            subject_list.add(subject2);
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
                                                            Token subject2 = new Token();
                                                            subject2.setForm(ptt_test5.getToken().toString());
                                                            subject2.setType(ptt_test5.getComment());
                                                            subject_list.add(subject2);
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
                                    Token subject2 = new Token();
                                    subject2.setForm(ptt_test2.getToken().toString());
                                    subject2.setType(ptt_test2.getComment());
                                    subject_list.add(subject2);
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
                                                && (ptt_test3.getComment().equalsIgnoreCase("product"))) {
                                            Token subject2 = new Token();
                                            subject2.setForm(ptt_test3.getToken().toString());
                                            subject2.setType(ptt_test3.getComment());
                                            subject_list.add(subject2);
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
                                                    Token subject2 = new Token();
                                                    subject2.setForm(ptt_test4.getToken().toString());
                                                    subject2.setType(ptt_test4.getComment());
                                                    subject_list.add(subject2);
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
            if ((posTagSequence.get(i).getTag().toString().equalsIgnoreCase("VPP")||posTagSequence.get(i).getTag().toString().equalsIgnoreCase("V"))
                    && parseConfiguration.getGoverningDependency(posTagSequence.get(i)).getLabel().equalsIgnoreCase("root")) {
                List<PosTaggedToken> pos_tagged_token = parseConfiguration.getDependents(posTagSequence.get(i));
                for (PosTaggedToken ptt : pos_tagged_token) {
                    if (ptt.getTag().toString().equalsIgnoreCase("P") && parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("mod")) {
                        List<PosTaggedToken> pos_tagged_token_test_2 = parseConfiguration.getDependents(ptt);
                        for (PosTaggedToken ptt_test2 : pos_tagged_token_test_2) {
                            if ((ptt_test2.getTag().toString().equalsIgnoreCase("NPP") || ptt_test2.getTag().toString().equalsIgnoreCase("NC"))
                                    && parseConfiguration.getGoverningDependency(ptt_test2).getLabel().equalsIgnoreCase("prep")
                                    && (ptt_test2.getComment().equalsIgnoreCase("product"))) {
                                Token subject2 = new Token();
                                subject2.setForm(ptt_test2.getToken().toString());
                                subject2.setType(ptt_test2.getComment());
                                subject_list.add(subject2);
                            }
                        }
                    }
                    if (ptt.getTag().toString().equalsIgnoreCase("NC") && parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("obj")) {
                        List<PosTaggedToken> pos_tagged_token_test_2 = parseConfiguration.getDependents(ptt);
                        for (PosTaggedToken ptt_test2 : pos_tagged_token_test_2) {
                            if ((ptt_test2.getTag().toString().equalsIgnoreCase("NPP") || ptt_test2.getTag().toString().equalsIgnoreCase("NC"))
                                    && parseConfiguration.getGoverningDependency(ptt_test2).getLabel().equalsIgnoreCase("mod")
                                    && (ptt_test2.getComment().equalsIgnoreCase("product"))) {
                                Token subject2 = new Token();
                                subject2.setForm(ptt_test2.getToken().toString());
                                subject2.setType(ptt_test2.getComment());
                                subject_list.add(subject2);
                                List<PosTaggedToken> pos_tagged_token_test_3 = parseConfiguration.getDependents(ptt_test2);
                                for (PosTaggedToken ptt_test3 : pos_tagged_token_test_3) {
                                    if ((ptt_test3.getTag().toString().equalsIgnoreCase("CC"))
                                            && parseConfiguration.getGoverningDependency(ptt_test3).getLabel().equalsIgnoreCase("coord")   ) {
                                        List<PosTaggedToken> pos_tagged_token_test_4 = parseConfiguration.getDependents(ptt_test3);
                                        for (PosTaggedToken ptt_test4 : pos_tagged_token_test_4) {
                                            if ((ptt_test4.getTag().toString().equalsIgnoreCase("NPP") || ptt_test4.getTag().toString().equalsIgnoreCase("NC"))
                                                    && parseConfiguration.getGoverningDependency(ptt_test4).getLabel().equalsIgnoreCase("dep_coord")
                                                    && (ptt_test4.getComment().equalsIgnoreCase("product"))) {
                                                Token subject3 = new Token();
                                                subject3.setForm(ptt_test4.getToken().toString());
                                                subject3.setType(ptt_test4.getComment());
                                                subject_list.add(subject3);
                                            }
                                        }
                                    }


                                }

                            }
                        }
                    }

                    if (ptt.getTag().toString().equalsIgnoreCase("NC") && parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("ats")) {
                        List<PosTaggedToken> pos_tagged_token_test_2 = parseConfiguration.getDependents(ptt);
                        for (PosTaggedToken ptt_test2 : pos_tagged_token_test_2) {
                            if ((ptt_test2.getTag().toString().equalsIgnoreCase("P") ||ptt_test2.getTag().toString().equalsIgnoreCase("P+D"))
                                    && parseConfiguration.getGoverningDependency(ptt_test2).getLabel().equalsIgnoreCase("dep")){
                                List<PosTaggedToken> pos_tagged_token_test_3 = parseConfiguration.getDependents(ptt_test2);
                                for (PosTaggedToken ptt_test3 : pos_tagged_token_test_3) {
                                    if (ptt_test3.getTag().toString().equalsIgnoreCase("NC")
                                            && parseConfiguration.getGoverningDependency(ptt_test3).getLabel().equalsIgnoreCase("prep")){
                                        List<PosTaggedToken> pos_tagged_token_test_4 = parseConfiguration.getDependents(ptt_test3);
                                        for (PosTaggedToken ptt_test4 : pos_tagged_token_test_4) {
                                            if ((ptt_test4.getTag().toString().equalsIgnoreCase("NPP")
                                                    && parseConfiguration.getGoverningDependency(ptt_test4).getLabel().equalsIgnoreCase("mod")
                                                    && (ptt_test4.getComment().equalsIgnoreCase("product") ))) {
                                                Token subject2 = new Token();
                                                subject2.setForm(ptt_test4.getToken().toString());
                                                subject2.setType(ptt_test4.getComment());
                                                subject_list.add(subject2);
                                            }
                                            List<PosTaggedToken> pos_tagged_token_test_5 = parseConfiguration.getDependents(ptt_test4);
                                            for (PosTaggedToken ptt_test5 : pos_tagged_token_test_5) {
                                                if (ptt_test5.getTag().toString().equalsIgnoreCase("CC") && parseConfiguration.getGoverningDependency(ptt_test5).getLabel().equalsIgnoreCase("coord") ) {
                                                    List<PosTaggedToken> pos_tagged_token_child = parseConfiguration.getDependents(ptt_test5);
                                                    for (PosTaggedToken ptt_test : pos_tagged_token_child) {
                                                        if (ptt_test.getTag().toString().equalsIgnoreCase("NPP") && parseConfiguration.getGoverningDependency(ptt_test).getLabel().equalsIgnoreCase("dep_coord")
                                                                && (ptt_test.getComment().equalsIgnoreCase("product") )) {
                                                            Token subject3 = new Token();
                                                            subject3.setForm(ptt_test.getToken().toString());
                                                            subject3.setType(ptt_test.getComment());
                                                            subject_list.add(subject3);


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

                    if (ptt.getTag().toString().equalsIgnoreCase("NC") && parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("ats")) {
                        List<PosTaggedToken> pos_tagged_token_test_2 = parseConfiguration.getDependents(ptt);
                        for (PosTaggedToken ptt_test2 : pos_tagged_token_test_2) {
                            if ((ptt_test2.getTag().toString().equalsIgnoreCase("P") ||ptt_test2.getTag().toString().equalsIgnoreCase("P+D"))
                                    && parseConfiguration.getGoverningDependency(ptt_test2).getLabel().equalsIgnoreCase("dep")){
                                List<PosTaggedToken> pos_tagged_token_test_3 = parseConfiguration.getDependents(ptt_test2);
                                for (PosTaggedToken ptt_test3 : pos_tagged_token_test_3) {
                                    if (ptt_test3.getTag().toString().equalsIgnoreCase("NPP")
                                            && parseConfiguration.getGoverningDependency(ptt_test3).getLabel().equalsIgnoreCase("prep")
                                            && (ptt_test3.getComment().equalsIgnoreCase("product"))){
                                        Token subject2 = new Token();
                                        subject2.setForm(ptt_test3.getToken().toString());
                                        subject2.setType(ptt_test3.getComment());
                                        subject_list.add(subject2);
                                    }
                                    List<PosTaggedToken> pos_tagged_token_test_5 = parseConfiguration.getDependents(ptt_test3);
                                    for (PosTaggedToken ptt_test5 : pos_tagged_token_test_5) {
                                        if (ptt_test5.getTag().toString().equalsIgnoreCase("CC") && parseConfiguration.getGoverningDependency(ptt_test5).getLabel().equalsIgnoreCase("coord")) {
                                            List<PosTaggedToken> pos_tagged_token_child = parseConfiguration.getDependents(ptt_test5);
                                            for (PosTaggedToken ptt_test : pos_tagged_token_child) {
                                                if (ptt_test.getTag().toString().equalsIgnoreCase("NPP") && parseConfiguration.getGoverningDependency(ptt_test).getLabel().equalsIgnoreCase("dep_coord")
                                                        && (ptt_test.getComment().equalsIgnoreCase("product"))) {
                                                    Token subject3 = new Token();
                                                    subject3.setForm(ptt_test.getToken().toString());
                                                    subject3.setType(ptt_test.getComment());
                                                    subject_list.add(subject3);

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

            //(VPP,mod) +(NPP,mod)
            if (posTagSequence.get(i).getTag().toString().equalsIgnoreCase("VPP") && parseConfiguration.getGoverningDependency(posTagSequence.get(i)).getLabel().equalsIgnoreCase("mod")) {
                List<PosTaggedToken> pos_tagged_token = parseConfiguration.getDependents(posTagSequence.get(i));
                for (PosTaggedToken ptt : pos_tagged_token) {
                    if (ptt.getTag().toString().equalsIgnoreCase("NPP")
                            && parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("mod")
                            && (ptt.getComment().equalsIgnoreCase("product"))) {
                        Token subject2 = new Token();
                        subject2.setForm(ptt.getToken().toString());
                        subject2.setType(ptt.getComment());
                        subject_list.add(subject2);
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
                                                            && (ptt_child3.getComment().equalsIgnoreCase("product"))) {
                                                        Token subject2 = new Token();
                                                        subject2.setForm(ptt_child3.getToken().toString());
                                                        subject2.setType(ptt_child3.getComment());
                                                        subject_list.add(subject2);
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
                            && (ptt.getComment().equalsIgnoreCase("product"))) {
                        Token subject2 = new Token();
                        subject2.setForm(ptt.getToken().toString());
                        subject2.setType(ptt.getComment());
                        subject_list.add(subject2);
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
                                    && (ptt_child.getComment().equalsIgnoreCase("product"))) {
                                Token subject2 = new Token();
                                subject2.setForm(ptt_child.getToken().toString());
                                subject2.setType(ptt_child.getComment());
                                subject_list.add(subject2);
                            }
                        }
                    }
                }
            }


        }
        for(Token subject:subject_list) {
          //  System.out.println("subject_list: " + subject.getForm());
        }
        return subject_list;
        //return subject2;
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

    }


    public void isCoordination(ParseConfiguration parseConfiguration, PosTaggedToken ptt,  PosTagSequence posTagSequence, String line,String old_relation ) {
        if (ptt.getTag().toString().equalsIgnoreCase("CC") && parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("coord")) {
            List<PosTaggedToken> pos_tagged_token_child = parseConfiguration.getDependents(ptt);
            for (PosTaggedToken ptt_child : pos_tagged_token_child) {
                if (ptt_child.getTag().toString().equalsIgnoreCase("P") &&
                        parseConfiguration.getGoverningDependency(ptt_child).getLabel().equalsIgnoreCase("dep_coord")) {
                    List<PosTaggedToken> pos_tagged_token_child1 = parseConfiguration.getDependents(ptt_child);
                    for (PosTaggedToken ptt_child1 : pos_tagged_token_child1) {
                        if (ptt_child1.getTag().toString().equalsIgnoreCase("NPP")
                                && parseConfiguration.getGoverningDependency(ptt_child1).getLabel().equalsIgnoreCase("prep")
                                && (ptt_child1.getComment().equalsIgnoreCase("PER")||ptt_child1.getComment().equalsIgnoreCase("person"))) {
                            //  Si depRel(_,CC)=coord et depRel(CC,P)=dep_coord et depRel(P,NPP)=prep et type(NPP)=PER Alors NPP=FragranceCreator
                            List<Token> subject_list = extractSubject2(parseConfiguration, posTagSequence, line);
                            if(subject_list.isEmpty()){
                                String objectForm = ptt_child1.getToken().toString();
                                Token subject2 = new Token();
                                String Relation = old_relation + "(CC,coord)+(P,dep_coord)+(NPP,prep)";
                                SentenceRelationType SRT = SentenceRelationType.hasFragranceCreator;
                                SentenceRelationMethod SRM = SentenceRelationMethod.rules;
                                double confidence = 1;
                                constructRelations(objectForm, subject2, Relation, SRT, SRM, line, confidence);
                            }else{
                            for (Token subject : subject_list) {
                                String objectForm = ptt_child1.getToken().toString();
                                Token subject2 = subject;
                                String Relation = old_relation + "(CC,coord)+(P,dep_coord)+(NPP,prep)";
                                SentenceRelationType SRT = SentenceRelationType.hasFragranceCreator;
                                SentenceRelationMethod SRM = SentenceRelationMethod.rules;
                                double confidence = 1;
                                constructRelations(objectForm, subject2, Relation, SRT, SRM, line, confidence);
                            }}
                        }
                    }
                }

                if (ptt_child.getTag().toString().equalsIgnoreCase("NPP")
                        && parseConfiguration.getGoverningDependency(ptt_child).getLabel().equalsIgnoreCase("dep_coord")
                        && (ptt_child.getComment().equalsIgnoreCase("PER")||ptt_child.getComment().equalsIgnoreCase("person"))) {
                    //  Si depRel(_,CC)=coord et depRel(CC,NPP)=dep_coord type(NPP)=PER Alors NPP=FragranceCreator
                    List<Token> subject_list = extractSubject2(parseConfiguration, posTagSequence, line);
                    if(subject_list.isEmpty()){
                        String objectForm = ptt_child.getToken().toString();
                        Token subject2 = new Token();
                        String Relation = "(CC,coord)+(NPP,dep_coord)";
                        SentenceRelationType SRT = SentenceRelationType.hasFragranceCreator;
                        SentenceRelationMethod SRM = SentenceRelationMethod.rules;
                        double confidence = 1;
                        constructRelations(objectForm, subject2, Relation, SRT, SRM, line, confidence);
                    }else {
                        for (Token subject : subject_list) {
                            String objectForm = ptt_child.getToken().toString();
                            Token subject2 = subject;
                            String Relation = "(CC,coord)+(NPP,dep_coord)";
                            SentenceRelationType SRT = SentenceRelationType.hasFragranceCreator;
                            SentenceRelationMethod SRM = SentenceRelationMethod.rules;
                            double confidence = 1;
                            constructRelations(objectForm, subject2, Relation, SRT, SRM, line, confidence);
                        }
                    }
                }
            }
        }
    }

        public void relationRules(ParseConfiguration parseConfiguration, PosTagSequence posTagSequence, String line) {

        for (int i = 0; i < posTagSequence.size(); i++) {
            /***************************************** Extraction hasFragranceCreator Relation *****************************/
                /*---------------------------------(V,root) +(P,mod) +(NC,prep)+(NPP,mod)-------------------------------*/
            if (posTagSequence.get(i).getTag().toString().equalsIgnoreCase("V") && parseConfiguration.getGoverningDependency(posTagSequence.get(i)).getLabel().equalsIgnoreCase("root")) {
                List<PosTaggedToken> pos_tagged_token = parseConfiguration.getDependents(posTagSequence.get(i));
                for (PosTaggedToken ptt : pos_tagged_token) {
                    if (ptt.getTag().toString().equalsIgnoreCase("P") && parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("mod")) {
                        List<PosTaggedToken> pos_tagged_token_test_0 = parseConfiguration.getDependents(ptt);
                        for (PosTaggedToken ptt_test0 : pos_tagged_token_test_0) {
                            /*String old_relation="(V,root)+(P,mod))";
                            isFragranceCreator( parseConfiguration,  ptt_test0,  posTagSequence, line, old_relation);*/
                            if (ptt_test0.getTag().toString().equalsIgnoreCase("NC") && parseConfiguration.getGoverningDependency(ptt_test0).getLabel().equalsIgnoreCase("prep")
                                    && (ptt_test0.getComment().equalsIgnoreCase("PER")||ptt_test0.getComment().equalsIgnoreCase("person"))) {
                                List<Token> subject_list = extractSubject2(parseConfiguration, posTagSequence, line);
                                if(subject_list.isEmpty()){
                                    String objectForm = ptt_test0.getToken().toString();
                                    Token subject2 = new Token();
                                    String Relation = "(V,root) +(P,mod) +(NC,prep)";
                                    SentenceRelationType SRT = SentenceRelationType.hasFragranceCreator;
                                    SentenceRelationMethod SRM = SentenceRelationMethod.rules;
                                    double confidence = 1;
                                    constructRelations(objectForm, subject2, Relation, SRT, SRM, line, confidence);
                                }else {
                                    for (Token subject : subject_list) {
                                        //  Si depRel(0,V)=root et depRel(V,P)=mod et depRel(P,NC)=prep et type(NC)=PER Alors NC=FragranceCreator
                                        String objectForm = ptt_test0.getToken().toString();
                                        Token subject2 = subject;
                                        String Relation = "(V,root) +(P,mod) +(NC,prep)";
                                        SentenceRelationType SRT = SentenceRelationType.hasFragranceCreator;
                                        SentenceRelationMethod SRM = SentenceRelationMethod.rules;
                                        double confidence = 1;
                                        constructRelations(objectForm, subject2, Relation, SRT, SRM, line, confidence);
                                    }
                                }
                            }
                            if (ptt_test0.getTag().toString().equalsIgnoreCase("NC") && parseConfiguration.getGoverningDependency(ptt_test0).getLabel().equalsIgnoreCase("prep"))
                            {
                                List<PosTaggedToken> pos_tagged_token_test_1 = parseConfiguration.getDependents(ptt_test0);
                                for (PosTaggedToken ptt_test1 : pos_tagged_token_test_1) {
                                    if (ptt_test1.getTag().toString().equalsIgnoreCase("P") && parseConfiguration.getGoverningDependency(ptt_test1).getLabel().equalsIgnoreCase("dep") )
                                    {
                                        List<PosTaggedToken> pos_tagged_token_test_2 = parseConfiguration.getDependents(ptt_test1);
                                        for (PosTaggedToken ptt_test2 : pos_tagged_token_test_2) {
                                            if (parseConfiguration.getGoverningDependency(ptt_test2).getLabel().equalsIgnoreCase("prep")
                                                    && (ptt_test2.getComment().equalsIgnoreCase("PER")||ptt_test2.getComment().equalsIgnoreCase("person"))) {
                                                List<Token> subject_list = extractSubject2(parseConfiguration, posTagSequence, line);
                                                if(subject_list.isEmpty()){
                                                    String objectForm = ptt_test2.getToken().toString();
                                                    Token subject2 = new Token();
                                                    String Relation = "(V,root) +(P,mod) +(NC,prep)+(NC,P)+(P,(NPP||NC))=prep+ type=PER";
                                                    SentenceRelationType SRT = SentenceRelationType.hasFragranceCreator;
                                                    SentenceRelationMethod SRM = SentenceRelationMethod.rules;
                                                    double confidence = 1;
                                                    constructRelations(objectForm, subject2, Relation, SRT, SRM, line, confidence);
                                                }else {
                                                for (Token subject : subject_list) {
                                                    // Si depRel(0,V)=root et depRel(V,P)=mod et depRel(P,NC)=prep et depRel(NC,P)=dep et depRel(P,_)=prep et type(_)=PER Alors _=FragranceCreator
                                                    String objectForm = ptt_test2.getToken().toString();
                                                    Token subject2 = subject;
                                                    String Relation = "(V,root) +(P,mod) +(NC,prep)+(NC,P)+(P,(NPP||NC))=prep+ type=PER";
                                                    SentenceRelationType SRT = SentenceRelationType.hasFragranceCreator;
                                                    SentenceRelationMethod SRM = SentenceRelationMethod.rules;
                                                    double confidence = 1;
                                                    constructRelations(objectForm, subject2, Relation, SRT, SRM, line, confidence);
                                                }}
                                                List<PosTaggedToken> pos_tagged_token_test_3 = parseConfiguration.getDependents(ptt_test2);
                                                for (PosTaggedToken ptt_test3 : pos_tagged_token_test_3) {
                                                    String old_relation="(V,root) +(P,mod) +(NC,prep)+(NC,P)+(P,(NPP||NC))=prep+ type=PER et isCoordination";
                                                    // Si depRel(0,V)=root et depRel(V,P)=mod et depRel(P,NC)=prep et depRel(NC,P)=dep et depRel(P,_)=prep et type(_)=PER Alors _=FragranceCreator + isCoordination
                                                    isCoordination( parseConfiguration, ptt_test3,  posTagSequence, line, old_relation );
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

                /*-----------------------------(V,root) +(NC,suj)+(NPP,mod)----------------------------------*/
            if (posTagSequence.get(i).getTag().toString().equalsIgnoreCase("V") && parseConfiguration.getGoverningDependency(posTagSequence.get(i)).getLabel().equalsIgnoreCase("root")) {
                //Liste des dependences de i
                List<PosTaggedToken> pos_tagged_token = parseConfiguration.getDependents(posTagSequence.get(i));
                for (PosTaggedToken ptt : pos_tagged_token) {
                    if ((ptt.getTag().toString().equalsIgnoreCase("NC")||ptt.getTag().toString().equalsIgnoreCase("NPP"))&&
                            (parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("suj")
                            || parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("obj"))
                            && (ptt.getComment().equalsIgnoreCase("PER")||ptt.getComment().equalsIgnoreCase("person"))) {
                        // Si depRel(0,V)=root et depRel(V,(NC||NPP))=(suj||obj) et type(NC||NPP)=PER alors (NC||NPP)=FragranceCreator
                        List<PosTaggedToken> pos_tagged_token_child2 = parseConfiguration.getDependents(ptt);
                        List<Token> subject_list = extractSubject2(parseConfiguration, posTagSequence, line);
                        if(subject_list.isEmpty()){
                            String objectForm = ptt.getToken().toString();
                            Token subject2 = new Token();
                            String Relation = "(V,root) +(NC,(suj||obj))";
                            SentenceRelationType SRT = SentenceRelationType.hasFragranceCreator;
                            SentenceRelationMethod SRM = SentenceRelationMethod.rules;
                            double confidence = 1;
                            constructRelations(objectForm, subject2, Relation, SRT, SRM, line, confidence);
                        }else {
                        for (Token subject : subject_list) {
                            String objectForm = ptt.getToken().toString();
                            Token subject2 = subject;
                            String Relation = "(V,root) +(NC,(suj||obj))";
                            SentenceRelationType SRT = SentenceRelationType.hasFragranceCreator;
                            SentenceRelationMethod SRM = SentenceRelationMethod.rules;
                            double confidence = 1;
                            constructRelations(objectForm, subject2, Relation, SRT, SRM, line, confidence);
                        }
                    }
                }}
            }


            if (posTagSequence.get(i).getTag().toString().equalsIgnoreCase("V") && parseConfiguration.getGoverningDependency(posTagSequence.get(i)).getLabel().equalsIgnoreCase("root")) {
                //Liste des dependences de i
                List<PosTaggedToken> pos_tagged_token = parseConfiguration.getDependents(posTagSequence.get(i));
                for (PosTaggedToken ptt : pos_tagged_token) {

                    if (ptt.getTag().toString().equalsIgnoreCase("NC")
                            &&(parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("suj")
                            || parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("obj"))) {
                        List<PosTaggedToken> pos_tagged_token_child2 = parseConfiguration.getDependents(ptt);
                        for (PosTaggedToken ptt2 : pos_tagged_token_child2) {
                            if (parseConfiguration.getGoverningDependency(ptt2).getLabel().equalsIgnoreCase("mod")
                                    && (ptt2.getComment().equalsIgnoreCase("PER")||ptt2.getComment().equalsIgnoreCase("person"))) {
                                // Si depRel(0,V)=root et depRel(V,NC)=(suj||obj) et depRel(NC,(NC,NPP))=mod et type(NC,NPP)=PER alors (NC,NPP)=FragranceCreator
                                List<Token> subject_list = extractSubject2(parseConfiguration, posTagSequence, line);
                                if(subject_list.isEmpty()){
                                    String objectForm = ptt.getToken().toString();
                                    Token subject2 = new Token();
                                    String Relation = "(V,root) +(NC,(suj||obj))+((NC||NPP),mod)";
                                    SentenceRelationType SRT = SentenceRelationType.hasFragranceCreator;
                                    SentenceRelationMethod SRM = SentenceRelationMethod.rules;
                                    double confidence = 1;
                                    constructRelations(objectForm, subject2, Relation, SRT, SRM, line, confidence);
                                }else {
                                for (Token subject : subject_list) {
                                    String objectForm = ptt.getToken().toString();
                                    Token subject2 = subject;
                                    String Relation = "(V,root) +(NC,(suj||obj))+((NC||NPP),mod)";
                                    SentenceRelationType SRT = SentenceRelationType.hasFragranceCreator;
                                    SentenceRelationMethod SRM = SentenceRelationMethod.rules;
                                    double confidence = 1;
                                    constructRelations(objectForm, subject2, Relation, SRT, SRM, line, confidence);
                                }}
                            }
                        }
                    }
                }
            }

            if (posTagSequence.get(i).getTag().toString().equalsIgnoreCase("V") && parseConfiguration.getGoverningDependency(posTagSequence.get(i)).getLabel().equalsIgnoreCase("root") ) {
                List<PosTaggedToken> pos_tagged_token0 = parseConfiguration.getDependents(posTagSequence.get(i));
                for (PosTaggedToken ptt0 : pos_tagged_token0) {
                    if (ptt0.getTag().toString().equalsIgnoreCase("NC")
                            && (parseConfiguration.getGoverningDependency(ptt0).getLabel().equalsIgnoreCase("ats"))
                            && (ptt0.getComment() == null || ptt0.getComment().isEmpty())) {
                        List<PosTaggedToken> pos_tagged_token = parseConfiguration.getDependents(ptt0);
                        for (PosTaggedToken ptt : pos_tagged_token) {
                            if ((ptt.getTag().toString().equalsIgnoreCase("P") || ptt.getTag().toString().equalsIgnoreCase("P+D"))
                                    && parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("dep")) {
                                List<PosTaggedToken> pos_tagged_token_test = parseConfiguration.getDependents(ptt);
                                for (PosTaggedToken ptt_test : pos_tagged_token_test) {
                                    if (ptt_test.getTag().toString().equalsIgnoreCase("NPP") && parseConfiguration.getGoverningDependency(ptt_test).getLabel().equalsIgnoreCase("prep")
                                            && (ptt_test.getComment().equalsIgnoreCase("PER")||ptt_test.getComment().equalsIgnoreCase("person"))) {
                                        //Si depRel(0,V)=root et depRel(V,NC)=ats et depRel(NC,(P,P+D))=dep et depRel((P,P+D),NPP)=prep et type (NPP)=PER alors (NPP)=FragranceCreator
                                        List<Token> subject_list = extractSubject2(parseConfiguration, posTagSequence, line);
                                        if(subject_list.isEmpty()){
                                            String objectForm = ptt_test.getToken().toString();
                                            Token subject2 = new Token();
                                            String Relation = "Si depRel(0,V)=root et depRel(V,NC)=ats et depRel(NC,(P,P+D))=dep et depRel((P,P+D),NPP)=prep et type (NPP)=PER alors (NPP)=FragranceCreator";
                                            SentenceRelationType SRT = SentenceRelationType.hasFragranceCreator;
                                            SentenceRelationMethod SRM = SentenceRelationMethod.rules;
                                            double confidence = 1;
                                            constructRelations(objectForm, subject2, Relation, SRT, SRM, line, confidence);
                                        }else {
                                        for (Token subject : subject_list) {
                                            String objectForm = ptt_test.getToken().toString();
                                            Token subject2 = subject;
                                            String Relation = "Si depRel(0,V)=root et depRel(V,NC)=ats et depRel(NC,(P,P+D))=dep et depRel((P,P+D),NPP)=prep et type (NPP)=PER alors (NPP)=FragranceCreator";
                                            SentenceRelationType SRT = SentenceRelationType.hasFragranceCreator;
                                            SentenceRelationMethod SRM = SentenceRelationMethod.rules;
                                            double confidence = 1;
                                            constructRelations(objectForm, subject2, Relation, SRT, SRM, line, confidence);
                                        }
                                    }}
                                    //Si depRel(0,V)=root et depRel(V,NC)=ats et depRel(NC,(P,P+D))=dep et depRel((P,P+D),NPP)=prep et type (NPP)=PER alors (NPP)=FragranceCreator +isCordination
                                    String old_relation="Si depRel(0,V)=root et depRel(V,NC)=ats et depRel(NC,(P,P+D))=dep et depRel((P,P+D),NPP)=prep et type (NPP)=PER alors (NPP)=FragranceCreator";
                                    isCoordination( parseConfiguration, ptt_test,  posTagSequence, line, old_relation );
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
                    if (ptt.getTag().toString().equalsIgnoreCase("NPP")
                            && parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("suj")
                            && (ptt.getComment().equalsIgnoreCase("PER")||ptt.getComment().equalsIgnoreCase("person"))) {
                        //Si depRel(0,VPP)=root et depRel(V,NPP)=suj et type (NPP)=PER alors (NPP)=FragranceCreator
                        List<Token> subject_list = extractSubject2(parseConfiguration, posTagSequence, line);
                        if(subject_list.isEmpty()){
                            Token subject2 = new Token();
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
                        }else {
                        for (Token subject : subject_list) {
                            Token subject2 = subject;
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
                        }}
                        List<PosTaggedToken> pos_tagged_token_test = parseConfiguration.getDependents(ptt);
                        for (PosTaggedToken ptt_test : pos_tagged_token_test) {
                        //Si depRel(0,VPP)=root et depRel(V,NPP)=suj et type (NPP)=PER alors (NPP)=FragranceCreator + isCoordination
                            String old_relation="(VPP,root)+(NPP,suj)";
                            isCoordination( parseConfiguration, ptt_test,  posTagSequence, line, old_relation );
                        }
                    }
                }
            }


            if (posTagSequence.get(i).getTag().toString().equalsIgnoreCase("VPP") && parseConfiguration.getGoverningDependency(posTagSequence.get(i)).getLabel().equalsIgnoreCase("mod") ) {
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
                                           && (ptt_test.getComment().equalsIgnoreCase("PER")||ptt_test.getComment().equalsIgnoreCase("person"))) {
                                       //Si depRel(0,VPP)=mod et depRel(VPP,NC)=(mod||suj||prep) et depRel(NC,(P||P+D))=dep et depRel((P||P+D),NPP)=prep et type (NPP)=PER alors (NPP)=FragranceCreator
                                       List<Token> subject_list = extractSubject2(parseConfiguration, posTagSequence, line);
                                       if(subject_list.isEmpty()){
                                           String objectForm = ptt_test.getToken().toString();
                                           Token subject2 = new Token();
                                           String Relation = "(VPP,mod)+(NC,(mod||suj||prep) +((P||P+D),dep) +(NPP,prep)";
                                           SentenceRelationType SRT = SentenceRelationType.hasFragranceCreator;
                                           SentenceRelationMethod SRM = SentenceRelationMethod.rules;
                                           double confidence = 1;
                                           constructRelations(objectForm, subject2, Relation, SRT, SRM, line, confidence);
                                       }else {
                                       for (Token subject : subject_list) {
                                           String objectForm = ptt_test.getToken().toString();
                                           Token subject2 = subject;
                                           String Relation = "(VPP,mod)+(NC,(mod||suj||prep) +((P||P+D),dep) +(NPP,prep)";
                                           SentenceRelationType SRT = SentenceRelationType.hasFragranceCreator;
                                           SentenceRelationMethod SRM = SentenceRelationMethod.rules;
                                           double confidence = 1;
                                           constructRelations(objectForm, subject2, Relation, SRT, SRM, line, confidence);
                                       }
                                   }}
                                    //Si depRel(0,VPP)=mod et depRel(VPP,NC)=(mod||suj||prep) et depRel(NC,(P,P+D))=dep et depRel((P,P+D),NPP)=prep et type (NPP)=PER alors (NPP)=FragranceCreator +isCoordination
                                    String old_relation="(VPP,mod) +(NC,(mod||suj||prepp) +((P||P+D),dep))";
                                    isCoordination( parseConfiguration, ptt_test,  posTagSequence, line, old_relation );
                                }
                            }
                        }
                    }
                }
            }



            if (posTagSequence.get(i).getTag().toString().equalsIgnoreCase("VPP")
                    && (parseConfiguration.getGoverningDependency(posTagSequence.get(i)).getLabel().equalsIgnoreCase("mod")
                    || parseConfiguration.getGoverningDependency(posTagSequence.get(i)).getLabel().equalsIgnoreCase("dep")
                    ||parseConfiguration.getGoverningDependency(posTagSequence.get(i)).getLabel().equalsIgnoreCase("root")
                    ||parseConfiguration.getGoverningDependency(posTagSequence.get(i)).getLabel().equalsIgnoreCase("obj"))) {

                List<PosTaggedToken> pos_tagged_token = parseConfiguration.getDependents(posTagSequence.get(i));
                for (PosTaggedToken ptt : pos_tagged_token) {
                    if (ptt.getTag().toString().equalsIgnoreCase("P")
                            && parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("p_obj")) {
                        List<PosTaggedToken> pos_tagged_token_test = parseConfiguration.getDependents(ptt);
                        for (PosTaggedToken ptt_test : pos_tagged_token_test) {
                            if ((ptt_test.getTag().toString().equalsIgnoreCase("NC") || ptt_test.getTag().toString().equalsIgnoreCase("NPP"))
                                    && parseConfiguration.getGoverningDependency(ptt_test).getLabel().equalsIgnoreCase("prep")
                                    && ptt_test.getComment().equalsIgnoreCase("PER") || ptt_test.getComment().equalsIgnoreCase("person")) {
                                {
                                    //Si depRel(0,VPP)=(mod||dep||root||obj) et depRel(VPP,P)=(p_obj) et depRel(P,NC)=prep et type (NPP)=(PER||person) alors (NPP)=FragranceCreator
                                    List<Token> subject_list = extractSubject2(parseConfiguration, posTagSequence, line);
                                    if(subject_list.isEmpty()){
                                        String objectForm = ptt_test.getToken().toString();
                                        Token subject2 = new Token();
                                        String Relation = "Si depRel(0,VPP)=(mod||dep||root||obj) et depRel(VPP,P)=(p_obj) et depRel(P,NC)=prep et type (NPP)=(PER||person) alors (NPP)=FragranceCreator";
                                        SentenceRelationType SRT = SentenceRelationType.hasFragranceCreator;
                                        SentenceRelationMethod SRM = SentenceRelationMethod.rules;
                                        double confidence = 1;
                                        constructRelations(objectForm, subject2, Relation, SRT, SRM, line, confidence);
                                    }else {
                                    for (Token subject : subject_list) {
                                        String objectForm = ptt_test.getToken().toString();
                                        Token subject2 = subject;
                                        String Relation = "Si depRel(0,VPP)=(mod||dep||root||obj) et depRel(VPP,P)=(p_obj) et depRel(P,NC)=prep et type (NPP)=(PER||person) alors (NPP)=FragranceCreator";
                                        SentenceRelationType SRT = SentenceRelationType.hasFragranceCreator;
                                        SentenceRelationMethod SRM = SentenceRelationMethod.rules;
                                        double confidence = 1;
                                        constructRelations(objectForm, subject2, Relation, SRT, SRM, line, confidence);
                                    }}
                                }
                                List<PosTaggedToken> pos_tagged_token_test1 = parseConfiguration.getDependents(ptt_test);
                                for (PosTaggedToken ptt_test1 : pos_tagged_token_test1) {
                                    String old_relation="Si depRel(0,VPP)=(mod||dep||root||obj) et depRel(VPP,P)=(p_obj) et depRel(P,NC)=prep et type (NPP)=(PER||person) alors (NPP)=FragranceCreator";
                                    isCoordination( parseConfiguration, ptt_test1,  posTagSequence, line, old_relation );
                                }

                            }
                        }
                    }
                }
            }


            if (posTagSequence.get(i).getTag().toString().equalsIgnoreCase("VPP")
                    && (parseConfiguration.getGoverningDependency(posTagSequence.get(i)).getLabel().equalsIgnoreCase("mod")
                    || parseConfiguration.getGoverningDependency(posTagSequence.get(i)).getLabel().equalsIgnoreCase("dep")
                    ||parseConfiguration.getGoverningDependency(posTagSequence.get(i)).getLabel().equalsIgnoreCase("root")
                    ||parseConfiguration.getGoverningDependency(posTagSequence.get(i)).getLabel().equalsIgnoreCase("obj"))) {
                List<PosTaggedToken> pos_tagged_token = parseConfiguration.getDependents(posTagSequence.get(i));
                for (PosTaggedToken ptt : pos_tagged_token) {
                    if (ptt.getTag().toString().equalsIgnoreCase("P") && parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("p_obj")) {
                        List<PosTaggedToken> pos_tagged_token_test = parseConfiguration.getDependents(ptt);
                        for (PosTaggedToken ptt_test : pos_tagged_token_test) {
                            if ((ptt_test.getTag().toString().equalsIgnoreCase("NC")||ptt_test.getTag().toString().equalsIgnoreCase("NPP"))
                                    && parseConfiguration.getGoverningDependency(ptt_test).getLabel().equalsIgnoreCase("prep")) {
                                List<PosTaggedToken> pos_tagged_token_test2 = parseConfiguration.getDependents(ptt_test);
                                for (PosTaggedToken ptt_test2 : pos_tagged_token_test2) {
                                    if ((ptt_test2.getTag().toString().equalsIgnoreCase("P"))
                                            && parseConfiguration.getGoverningDependency(ptt_test2).getLabel().equalsIgnoreCase("dep")) {
                                        List<PosTaggedToken> pos_tagged_token_test3 = parseConfiguration.getDependents(ptt_test2);
                                        for (PosTaggedToken ptt_test3 : pos_tagged_token_test3) {
                                            if ((ptt_test3.getTag().toString().equalsIgnoreCase("NC"))
                                                    && parseConfiguration.getGoverningDependency(ptt_test3).getLabel().equalsIgnoreCase("prep") ) {
                                                List<PosTaggedToken> pos_tagged_token_test4 = parseConfiguration.getDependents(ptt_test3);
                                                for (PosTaggedToken ptt_test4 : pos_tagged_token_test4) {
                                                    if ((ptt_test4.getTag().toString().equalsIgnoreCase("NPP"))
                                                            && parseConfiguration.getGoverningDependency(ptt_test4).getLabel().equalsIgnoreCase("mod")
                                                            && (ptt_test4.getComment().equalsIgnoreCase("PER") || ptt_test.getComment().equalsIgnoreCase("person"))) {
                                                        //Si depRel(0,VPP)=(mod||dep||root||obj) et depRel(VPP,P)=p_obj et depRel(P,(NC||NPP))=prep et depRel((NC||NPP),P)=dep et depRel((NC||NPP),P)=dep et depRel(P,NC)=prep et depRel(NC,NPP)=mod
                                                        // et type (NPP)=(PER ||person) alors (NPP)=FragranceCreator
                                                        List<Token> subject_list = extractSubject2(parseConfiguration, posTagSequence, line);
                                                        if(subject_list.isEmpty()){
                                                            String objectForm = ptt_test4.getToken().toString();
                                                            Token subject2 = new Token();
                                                            String Relation = "(VPP,(mod||dep))+(P,p_obj)+((NC||NPP),prep)";
                                                            SentenceRelationType SRT = SentenceRelationType.hasFragranceCreator;
                                                            SentenceRelationMethod SRM = SentenceRelationMethod.rules;
                                                            double confidence = 1;
                                                            constructRelations(objectForm, subject2, Relation, SRT, SRM, line, confidence);
                                                        }else {
                                                        for (Token subject : subject_list) {
                                                            String objectForm = ptt_test4.getToken().toString();
                                                            Token subject2 = subject;
                                                            String Relation = "(VPP,(mod||dep))+(P,p_obj)+((NC||NPP),prep)";
                                                            SentenceRelationType SRT = SentenceRelationType.hasFragranceCreator;
                                                            SentenceRelationMethod SRM = SentenceRelationMethod.rules;
                                                            double confidence = 1;
                                                            constructRelations(objectForm, subject2, Relation, SRT, SRM, line, confidence);
                                                        }}
                                                        List<PosTaggedToken> pos_tagged_token_test5 = parseConfiguration.getDependents(ptt_test4);

                                                        for (PosTaggedToken ptt_test5 : pos_tagged_token_test5) {
                                                            //Si depRel(0,VPP)=(mod||dep||root||obj) et depRel(VPP,P)=p_obj et depRel(P,(NC||NPP))=prep et depRel((NC||NPP),P)=dep et depRel((NC||NPP),P)=dep et depRel(P,NC)=prep et depRel(NC,NPP)=mod
                                                            // et type (NPP)=(PER ||person) alors (NPP)=FragranceCreator + isCoordiantion
                                                            String old_relation = "(VPP,(mod||dep))+(P,p_obj)+((NC||NPP),prep)";
                                                            isCoordination(parseConfiguration, ptt_test5, posTagSequence, line, old_relation);
                                                            if ((ptt_test5.getTag().toString().equalsIgnoreCase("NPP")||ptt_test5.getTag().toString().equalsIgnoreCase("NC"))
                                                                    && parseConfiguration.getGoverningDependency(ptt_test5).getLabel().equalsIgnoreCase("coord")
                                                                    && (ptt_test5.getComment().equalsIgnoreCase("PER")|| ptt_test5.getComment().equalsIgnoreCase("person"))) {
                                                                //Si depRel(0,VPP)=(mod||dep||root||obj) et depRel(VPP,P)=p_obj et depRel(P,(NC||NPP))=prep et depRel((NC||NPP),P)=dep et depRel((NC||NPP),P)=dep et depRel(P,NC)=prep et depRel(NC,NPP)=mod
                                                                // et type (NPP)=(PER ||person) + depRel(NPP,(NPP||NC)=coord et type(NPP||NC)=(PER||person) alors ((NPP||NC))=FragranceCreator
                                                               // List<Token> subject_list = extractSubject2(parseConfiguration, posTagSequence, line);
                                                                if(subject_list.isEmpty()){
                                                                    String objectForm = ptt_test5.getToken().toString();
                                                                    Token subject2 = new Token();
                                                                    String Relation = "(VPP,(mod||dep))+(P,p_obj)+((NC||NPP),prep)";
                                                                    SentenceRelationType SRT = SentenceRelationType.hasFragranceCreator;
                                                                    SentenceRelationMethod SRM = SentenceRelationMethod.rules;
                                                                    double confidence = 1;
                                                                    constructRelations(objectForm, subject2, Relation, SRT, SRM, line, confidence);
                                                                }else {
                                                                for (Token subject : subject_list) {
                                                                   String objectForm = ptt_test5.getToken().toString();
                                                                   Token subject2 = subject;
                                                                   String Relation = "(VPP,(mod||dep))+(P,p_obj)+((NC||NPP),prep)";
                                                                   SentenceRelationType SRT = SentenceRelationType.hasFragranceCreator;
                                                                   SentenceRelationMethod SRM = SentenceRelationMethod.rules;
                                                                    double confidence = 1;
                                                                    constructRelations(objectForm, subject2, Relation, SRT, SRM, line, confidence);
                                                                }}
                                                                List<PosTaggedToken> pos_tagged_token_test6 = parseConfiguration.getDependents(ptt_test5);
                                                                for (PosTaggedToken ptt_test6 : pos_tagged_token_test6) {
                                                                    //Si depRel(0,VPP)=(mod||dep||root||obj) et depRel(VPP,P)=p_obj et depRel(P,(NC||NPP))=prep et depRel((NC||NPP),P)=dep et depRel((NC||NPP),P)=dep et depRel(P,NC)=prep et depRel(NC,NPP)=mod
                                                                    // et type (NPP)=(PER ||person) + depRel(NPP,(NPP||NC)=coord et type(NPP||NC)=(PER||person) alors ((NPP||NC))=FragranceCreator + isCoordiantion
                                                                    old_relation = "(VPP,(mod||dep))+(P,p_obj)+((NC||NPP),prep)";
                                                                    isCoordination(parseConfiguration, ptt_test6, posTagSequence, line, old_relation);
                                                                }

                                                            }

                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    if ((ptt_test2.getTag().toString().equalsIgnoreCase("NPP"))
                                            && parseConfiguration.getGoverningDependency(ptt_test2).getLabel().equalsIgnoreCase("mod")
                                            && (ptt_test2.getComment().equalsIgnoreCase("PER")|| ptt_test2.getComment().equalsIgnoreCase("person"))) {
                                        //Si depRel(0,VPP)=(mod||dep||root||obj) et depRel(VPP,P)=p_obj et depRel(P,(NC||NPP))=prep et depRel((NC||NPP),P)=dep et depRel((NC||NPP),NPP)=mod
                                        // et type (NPP)=(PER ||person) alors (NPP)=FragranceCreator
                                        List<Token> subject_list = extractSubject2(parseConfiguration, posTagSequence, line);
                                        if(subject_list.isEmpty()){
                                            String objectForm = ptt_test2.getToken().toString();
                                            Token subject2 = new Token();
                                            String Relation = "(VPP,(mod||dep))+(P,p_obj)+((NC||NPP),prep)";
                                            SentenceRelationType SRT = SentenceRelationType.hasFragranceCreator;
                                            SentenceRelationMethod SRM = SentenceRelationMethod.rules;
                                            double confidence = 1;
                                            constructRelations(objectForm, subject2, Relation, SRT, SRM, line, confidence);
                                        }else {
                                        for (Token subject : subject_list) {
                                            String objectForm = ptt_test2.getToken().toString();
                                            Token subject2 = subject;
                                            String Relation = "(VPP,(mod||dep))+(P,p_obj)+((NC||NPP),prep)";
                                            SentenceRelationType SRT = SentenceRelationType.hasFragranceCreator;
                                            SentenceRelationMethod SRM = SentenceRelationMethod.rules;
                                            double confidence = 1;
                                            constructRelations(objectForm, subject2, Relation, SRT, SRM, line, confidence);
                                        }}
                                        List<PosTaggedToken> pos_tagged_token_test3 = parseConfiguration.getDependents(ptt_test2);
                                        for (PosTaggedToken ptt_test3 : pos_tagged_token_test3) {
                                            //Si depRel(0,VPP)=(mod||dep||root||obj) et depRel(VPP,P)=p_obj et depRel(P,(NC||NPP))=prep et depRel((NC||NPP),P)=dep et depRel((NC||NPP),NPP)=mod
                                            // et type (NPP)=(PER ||person) alors (NPP)=FragranceCreator + isCoordiantion
                                            String old_relation = "(VPP,(mod||dep))+(P,p_obj)+((NC||NPP),prep)";
                                            isCoordination(parseConfiguration, ptt_test3, posTagSequence, line, old_relation);
                                            if ((ptt_test3.getTag().toString().equalsIgnoreCase("NPP")||(ptt_test3.getTag().toString().equalsIgnoreCase("NC")))
                                                    && parseConfiguration.getGoverningDependency(ptt_test3).getLabel().equalsIgnoreCase("coord")
                                                    && (ptt_test3.getComment().equalsIgnoreCase("PER")|| ptt_test3.getComment().equalsIgnoreCase("person"))) {
                                                //Si depRel(0,VPP)=(mod||dep||root||obj) et depRel(VPP,P)=p_obj et depRel(P,(NC||NPP))=prep et depRel((NC||NPP),P)=dep et depRel((NC||NPP),NPP)=mod
                                                // et type (NPP)=(PER ||person) et depRel(NPP,(NPP||NC)=coord et type (NPP||NC)=(PER || person) alors (NPP||NC)=FragranceCreator
                                               // List<Token> subject_list = extractSubject2(parseConfiguration, posTagSequence, line);
                                                if(subject_list.isEmpty()){
                                                    String   objectForm = ptt_test3.getToken().toString();
                                                    Token   subject2 = new Token();
                                                    String   Relation = "(VPP,(mod||dep))+(P,p_obj)+((NC||NPP),prep)";
                                                    SentenceRelationType  SRT = SentenceRelationType.hasFragranceCreator;
                                                    SentenceRelationMethod  SRM = SentenceRelationMethod.rules;
                                                    double  confidence = 1;
                                                    constructRelations(objectForm, subject2, Relation, SRT, SRM, line, confidence);
                                                }else {
                                                for (Token subject : subject_list) {
                                                 String   objectForm = ptt_test3.getToken().toString();
                                                 Token   subject2 = subject;
                                                 String   Relation = "(VPP,(mod||dep))+(P,p_obj)+((NC||NPP),prep)";
                                                  SentenceRelationType  SRT = SentenceRelationType.hasFragranceCreator;
                                                  SentenceRelationMethod  SRM = SentenceRelationMethod.rules;
                                                  double  confidence = 1;
                                                    constructRelations(objectForm, subject2, Relation, SRT, SRM, line, confidence);
                                                }}
                                                List<PosTaggedToken> pos_tagged_token_test4 = parseConfiguration.getDependents(ptt_test3);
                                                for (PosTaggedToken ptt_test4 : pos_tagged_token_test4) {
                                                    //Si depRel(0,VPP)=(mod||dep||root||obj) et depRel(VPP,P)=p_obj et depRel(P,(NC||NPP))=prep et depRel((NC||NPP),P)=dep et depRel((NC||NPP),NPP)=mod
                                                    // et type (NPP)=(PER ||person) et depRel(NPP,(NPP||NC)=coord et type (NPP||NC)=(PER || person) alors (NPP||NC)=FragranceCreator + isCoordiantion
                                                    old_relation = "(VPP,(mod||dep))+(P,p_obj)+((NC||NPP),prep)";
                                                    isCoordination(parseConfiguration, ptt_test4, posTagSequence, line, old_relation);
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
                                           && (ptt_test1.getComment().equalsIgnoreCase("PER")||ptt_test1.getComment().equalsIgnoreCase("person"))) {
                                       //Si depRel(0,VPP)=(mod||dep) et depRel(VPP,NC)=(mod) et type(NC)=PER et depRel(NC,P)=dep et depRel(P,NPP)=dep type (NPP)=(PER) alors (NPP)=FragranceCreator
                                       List<Token> subject_list = extractSubject2(parseConfiguration, posTagSequence, line);
                                       if(subject_list.isEmpty()){
                                           String objectForm = ptt_test.getToken().toString() + " " + ptt_test1.getToken().toString();
                                           Token subject2 = new Token();
                                           String Relation = "(VPP,(mod||dep)+(NC,mod)+(P,dep)+(NPP,dep)";
                                           SentenceRelationType SRT = SentenceRelationType.hasFragranceCreator;
                                           SentenceRelationMethod SRM = SentenceRelationMethod.rules;
                                           double confidence = 1;
                                           constructRelations(objectForm, subject2, Relation, SRT, SRM, line, confidence);
                                       }else {
                                       for (Token subject : subject_list) {
                                           String objectForm = ptt_test.getToken().toString() + " " + ptt_test1.getToken().toString();
                                           Token subject2 = subject;
                                           String Relation = "(VPP,(mod||dep)+(NC,mod)+(P,dep)+(NPP,dep)";
                                           SentenceRelationType SRT = SentenceRelationType.hasFragranceCreator;
                                           SentenceRelationMethod SRM = SentenceRelationMethod.rules;
                                           double confidence = 1;
                                           constructRelations(objectForm, subject2, Relation, SRT, SRM, line, confidence);
                                       }}
                                   }
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
        //List<SentenceRelationMethod> relationMethods = new ArrayList<>();
        for (SentenceRelationId sentenceRelationId : relationMap.keySet()) {
            //relationMethods.addAll(relationMap.get(sentenceRelationId));
            List<SentenceRelationMethod> relationMethods = relationMap.get(sentenceRelationId);
            System.out.println("Current:" + sentenceRelationId );
            if (relationMethods.contains(SentenceRelationMethod.rules)) {
                System.out.println("Selected" + sentenceRelationId + "" + SentenceRelationMethod.rules);
                Model model = constructModel(sentenceRelationId);
                writeRdf(model);
            }/* else if (relationMethods.contains(SentenceRelationMethod.rules)) {
                System.out.println("Selected" + sentenceRelationId + "" + SentenceRelationMethod.rules);
                Model model = constructModel(sentenceRelationId);
                writeRdf(model);
            } else if (relationMethods.contains(SentenceRelationMethod.rules)) {
                System.out.println("Selected" + sentenceRelationId + "" + SentenceRelationMethod.rules);
                Model model = constructModel(sentenceRelationId);
                writeRdf(model);

            }*/
            // constructModel(relationMap.keySet());
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
