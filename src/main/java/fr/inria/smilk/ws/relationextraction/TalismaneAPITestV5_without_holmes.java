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
public class TalismaneAPITestV5_without_holmes extends AbstractRelationExtraction {


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
                                    newline = newline;
                                    newline = newline.replace(form, form_with_underscore);

                                }
                        }
                    }
                }
            }
        }
        System.out.println("newline: " + newline);
        annotatedByTalismane(newline, line, nSentenceList);
        // return newline;
    }

    private String setType(String line, NodeList nSentenceList, String token) {
        String type = new String();
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
                             /* && (xElement.getAttribute("type").equalsIgnoreCase("product")  )*/)
                            {
                                Token subjectToken = elementToToken(xElement);
                                String token2=token.replace("_"," ");
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
            System.out.println( posTagSequence.get(i).getToken()+ " "+ posTagSequence.get(i).getComment());
        }

        System.out.println("test: "+parseConfiguration.getDependencies());

        extractSubject(parseConfiguration, posTagSequence, line);


    }

    public void extractSubject(ParseConfiguration parseConfiguration, PosTagSequence posTagSequence, String line) {
        Token subject = new Token();

        for (int i = 0; i < posTagSequence.size(); i++) {
            if (((posTagSequence.get(i).getTag().toString().equalsIgnoreCase("NPP")/*&&
                    (parseConfiguration.getGoverningDependency(posTagSequence.get(i)).getLabel().equalsIgnoreCase("suj")
                            || parseConfiguration.getGoverningDependency(posTagSequence.get(i)).getLabel().equalsIgnoreCase("prep")
                            || parseConfiguration.getGoverningDependency(posTagSequence.get(i)).getLabel().equalsIgnoreCase("mod"))*/)
                    || (posTagSequence.get(i).getTag().toString().equalsIgnoreCase("NC") /*&&
                    (parseConfiguration.getGoverningDependency(posTagSequence.get(i)).getLabel().equalsIgnoreCase("obj")
                            || parseConfiguration.getGoverningDependency(posTagSequence.get(i)).getLabel().equalsIgnoreCase("mod"))*/)
                    || (posTagSequence.get(i).getTag().toString().equalsIgnoreCase("ADV") &&
                    parseConfiguration.getGoverningDependency(posTagSequence.get(i)).getLabel().equalsIgnoreCase("mod")))
                    && (posTagSequence.get(i).getComment().equalsIgnoreCase("product")|| (posTagSequence.get(i).getComment().equalsIgnoreCase("brand")))) {
                subject.setForm(posTagSequence.get(i).getToken().toString());


            }
        }
        System.out.println("Subject: "+ subject.getForm());
        if(subject.getForm()!=null)
        { relationRules(parseConfiguration, posTagSequence, line, subject);}
        else
        {
            System.out.println("subject not found");
        }

    }
    public Token extractSubject2(ParseConfiguration parseConfiguration, PosTagSequence posTagSequence, String line) {
        Token subject2= new Token();

        for (int i = 0; i < posTagSequence.size(); i++) {
            if (posTagSequence.get(i).getLexicalEntry() != null) {
                if (posTagSequence.get(i).getTag().toString().equalsIgnoreCase("V") && parseConfiguration.getGoverningDependency(posTagSequence.get(i)).getLabel().equalsIgnoreCase("dep_coord") ) {

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
                                                               && ptt_test6.getComment().equalsIgnoreCase("product") || ptt_test6.getComment().equalsIgnoreCase("brand")) {
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
                //Rules with (V,roo)
                if (posTagSequence.get(i).getTag().toString().equalsIgnoreCase("V") && parseConfiguration.getGoverningDependency(posTagSequence.get(i)).getLabel().equalsIgnoreCase("root")) {
                    //Liste des dependences de i
                    List<PosTaggedToken> pos_tagged_token = parseConfiguration.getDependents(posTagSequence.get(i));
                    for (PosTaggedToken ptt : pos_tagged_token) {
                        if (ptt.getTag().toString().equalsIgnoreCase("NPP") && parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("suj")
                                && ptt.getComment().equalsIgnoreCase("product") || ptt.getComment().equalsIgnoreCase("brand")) {
                            subject2.setForm(ptt.getToken().toString());
                        }
                        if (ptt.getTag().toString().equalsIgnoreCase("NC") && parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("obj")
                                && ptt.getComment().equalsIgnoreCase("product") || ptt.getComment().equalsIgnoreCase("brand")) {
                            subject2.setForm(ptt.getToken().toString());
                        }

                        if (ptt.getTag().toString().equalsIgnoreCase("P") && parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("mod") ) {
                            List<PosTaggedToken> pos_tagged_token_test_2 = parseConfiguration.getDependents(ptt);
                            for (PosTaggedToken ptt_test2 : pos_tagged_token_test_2) {
                                if ((ptt_test2.getTag().toString().equalsIgnoreCase("NPP")|| ptt_test2.getTag().toString().equalsIgnoreCase("NC")) && parseConfiguration.getGoverningDependency(ptt_test2).getLabel().equalsIgnoreCase("prep")
                                        && ptt_test2.getComment().equalsIgnoreCase("product")|| ptt_test2.getComment().equalsIgnoreCase("brand")) {
                                    subject2.setForm(ptt_test2.getToken().toString());
                                }
                            }

                        }

                        if (ptt.getTag().toString().equalsIgnoreCase("V") && parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("mod") ) {
                            List<PosTaggedToken> pos_tagged_token_test_2 = parseConfiguration.getDependents(ptt);
                            for (PosTaggedToken ptt_test2 : pos_tagged_token_test_2) {
                                if (ptt_test2.getTag().toString().equalsIgnoreCase("NPP") && parseConfiguration.getGoverningDependency(ptt_test2).getLabel().equalsIgnoreCase("suj")
                                        && ptt_test2.getComment().equalsIgnoreCase("product")|| ptt_test2.getComment().equalsIgnoreCase("brand")) {
                                    subject2.setForm(ptt_test2.getToken().toString());
                                }
                            }

                        }

                        if (ptt.getTag().toString().equalsIgnoreCase("NC") && parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("suj") ) {
                            List<PosTaggedToken> pos_tagged_token_test_2 = parseConfiguration.getDependents(ptt);
                            for (PosTaggedToken ptt_test2 : pos_tagged_token_test_2) {
                                if (ptt_test2.getTag().toString().equalsIgnoreCase("NPP") && parseConfiguration.getGoverningDependency(ptt_test2).getLabel().equalsIgnoreCase("mod")
                                        && ptt_test2.getComment().equalsIgnoreCase("product")|| ptt_test2.getComment().equalsIgnoreCase("brand")) {
                                    subject2.setForm(ptt_test2.getToken().toString());
                                }
                            }

                        }

                        if (ptt.getTag().toString().equalsIgnoreCase("NC") && parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("obj") ) {
                            List<PosTaggedToken> pos_tagged_token_test_2 = parseConfiguration.getDependents(ptt);
                            for (PosTaggedToken ptt_test2 : pos_tagged_token_test_2) {
                                if (ptt_test2.getTag().toString().equalsIgnoreCase("P") && parseConfiguration.getGoverningDependency(ptt_test2).getLabel().equalsIgnoreCase("dep")
                                      ) {
                                    List<PosTaggedToken> pos_tagged_token_test_3 = parseConfiguration.getDependents(ptt_test2);
                                    for (PosTaggedToken ptt_test3 : pos_tagged_token_test_3) {
                                        if (ptt_test3.getTag().toString().equalsIgnoreCase("NC") && parseConfiguration.getGoverningDependency(ptt_test3).getLabel().equalsIgnoreCase("prep")
                                                ) {
                                            List<PosTaggedToken> pos_tagged_token_test_4 = parseConfiguration.getDependents(ptt_test3);
                                            for (PosTaggedToken ptt_test4 : pos_tagged_token_test_4) {
                                                if (ptt_test4.getTag().toString().equalsIgnoreCase("NC") && parseConfiguration.getGoverningDependency(ptt_test4).getLabel().equalsIgnoreCase("mod")
                                                        && ptt_test4.getComment().equalsIgnoreCase("product")|| ptt_test4.getComment().equalsIgnoreCase("brand")) {
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

                if (posTagSequence.get(i).getTag().toString().equalsIgnoreCase("VPP") && parseConfiguration.getGoverningDependency(posTagSequence.get(i)).getLabel().equalsIgnoreCase("root")) {
                    List<PosTaggedToken> pos_tagged_token = parseConfiguration.getDependents(posTagSequence.get(i));
                    for (PosTaggedToken ptt : pos_tagged_token) {
                        if (ptt.getTag().toString().equalsIgnoreCase("P") && parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("mod")
                                ) {
                            List<PosTaggedToken> pos_tagged_token_test_2 = parseConfiguration.getDependents(ptt);
                            for (PosTaggedToken ptt_test2 : pos_tagged_token_test_2) {
                                if (ptt_test2.getTag().toString().equalsIgnoreCase("NPP") && parseConfiguration.getGoverningDependency(ptt_test2).getLabel().equalsIgnoreCase("prep")
                                        && ptt_test2.getComment().equalsIgnoreCase("product") || ptt_test2.getComment().equalsIgnoreCase("brand")) {
                                    subject2.setForm(ptt_test2.getToken().toString());
                                }
                            }
                        }
                    }
                }

                if (posTagSequence.get(i).getTag().toString().equalsIgnoreCase("VPP") && parseConfiguration.getGoverningDependency(posTagSequence.get(i)).getLabel().equalsIgnoreCase("mod")) {
                    List<PosTaggedToken> pos_tagged_token = parseConfiguration.getDependents(posTagSequence.get(i));
                    for (PosTaggedToken ptt : pos_tagged_token) {
                        if (ptt.getTag().toString().equalsIgnoreCase("NPP") && parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("mod")
                               && ptt.getComment().equalsIgnoreCase("product") || ptt.getComment().equalsIgnoreCase("brand") ) {
                                    subject2.setForm(ptt.getToken().toString());
                        }
                    }
                }
            }

        return subject2;

    }

    public void relationRules(ParseConfiguration parseConfiguration, PosTagSequence posTagSequence, String line,  Token subject) {

        for (int i = 0; i < posTagSequence.size(); i++) {
            if (posTagSequence.get(i).getLexicalEntry() != null) {
                //Rules with (V,roo)
                if (posTagSequence.get(i).getTag().toString().equalsIgnoreCase("V") && parseConfiguration.getGoverningDependency(posTagSequence.get(i)).getLabel().equalsIgnoreCase("root")) {
                    //Liste des dependences de i

                    List<PosTaggedToken> pos_tagged_token = parseConfiguration.getDependents(posTagSequence.get(i));

                    for (PosTaggedToken ptt : pos_tagged_token) {
                        // règle pour la relation hasAmbassador (V,root) +(NPP,suj)+(NPP,mod)
                        if (ptt.getTag().toString().equalsIgnoreCase("P") && parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("mod")) {
                            List<PosTaggedToken> pos_tagged_token_test_2 = parseConfiguration.getDependents(ptt);
                            for (PosTaggedToken ptt_test2 : pos_tagged_token_test_2) {
                                if (ptt_test2.getTag().toString().equalsIgnoreCase("NPP") && parseConfiguration.getGoverningDependency(ptt_test2).getLabel().equalsIgnoreCase("prep")
                                    && !ptt_test2.getComment().equalsIgnoreCase("product") && !ptt_test2.getComment().equalsIgnoreCase("brand") ) {
                                    List<PosTaggedToken> pos_tagged_token_test_3 = parseConfiguration.getDependents(ptt_test2);
                                    for (PosTaggedToken ptt_test3 : pos_tagged_token_test_3) {
                                        if (ptt_test3.getTag().toString().equalsIgnoreCase("NPP") && parseConfiguration.getGoverningDependency(ptt_test3).getLabel().equalsIgnoreCase("mod")
                                                && !ptt_test3.getComment().equalsIgnoreCase("product") && !ptt_test3.getComment().equalsIgnoreCase("brand")) {
                                            Token objectToken = new Token();
                                            Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                            System.out.println("subject2: enter condition" + subject2);
                                            objectToken.setForm(ptt_test2.getToken().toString() + "" + ptt_test3.getToken().toString());
                                            SentenceRelation sentenceRelation = new SentenceRelation();
                                            SentenceRelationId sentenceRelationId = new SentenceRelationId();
                                            sentenceRelationId.setSubject(subject2);
                                            sentenceRelationId.setObject(objectToken);
                                            sentenceRelationId.setRelation("(V,root)+(P,mod)+(NPP,suj)+(NPP,mod)");
                                            sentenceRelationId.setSentence_text(line);
                                            sentenceRelationId.setType(SentenceRelationType.hasAmbasador);
                                            sentenceRelationId.setConfidence(1);
                                            sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                            sentenceRelation.setMethod(SentenceRelationMethod.rules);
                                            list_result.add(sentenceRelation);
                                        }
                                    }
                                    }
                                }
                            }

                        if (ptt.getTag().toString().equalsIgnoreCase("NPP") && parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("suj")
                        && !ptt.getComment().equalsIgnoreCase("product") && !ptt.getComment().equalsIgnoreCase("brand")  ) {
                            List<PosTaggedToken> pos_tagged_token_child = parseConfiguration.getDependents(ptt);
                            for (PosTaggedToken ptt_child : pos_tagged_token_child) {
                                if (ptt_child.getTag().toString().equalsIgnoreCase("NPP")
                                        && parseConfiguration.getGoverningDependency(ptt_child).getLabel().equalsIgnoreCase("mod")) {
                                    System.out.println("pppppppp: " + ptt.getTag() + " " + ptt.getToken().toString());
                                    Token objectToken = new Token();
                                    Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                    System.out.println("subject2: enter condition" + subject2);
                                    objectToken.setForm(ptt.getToken().toString() + " " + ptt_child.getToken().toString());
                                    SentenceRelation sentenceRelation = new SentenceRelation();
                                    SentenceRelationId sentenceRelationId = new SentenceRelationId();
                                    sentenceRelationId.setSubject(subject2);
                                    sentenceRelationId.setObject(objectToken);
                                    sentenceRelationId.setRelation("(V,root) +(NPP,suj)+(NPP,mod)+ " + ptt.getComment());
                                    sentenceRelationId.setSentence_text(line);
                                    sentenceRelationId.setType(SentenceRelationType.hasAmbasador);
                                    sentenceRelationId.setConfidence(1);
                                    sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                    sentenceRelation.setMethod(SentenceRelationMethod.rules);
                                    list_result.add(sentenceRelation);
                                }
                            }
                        }

                        if (ptt.getTag().toString().equalsIgnoreCase("NPP") && parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("obj")
                        && !ptt.getComment().equalsIgnoreCase("product") && !ptt.getComment().equalsIgnoreCase("brand")  ) {
                            boolean annotated = false;
                            List<PosTaggedToken> pos_tagged_token_child = parseConfiguration.getDependents(ptt);
                            for (PosTaggedToken ptt_child : pos_tagged_token_child) {
                                if(ptt_child.getTag().toString().equalsIgnoreCase("NPP")
                                        && parseConfiguration.getGoverningDependency(ptt_child).getLabel().equalsIgnoreCase("mod")){
                                    System.out.println("pppppppp: "+ ptt.getTag()+ " "+ ptt.getToken().toString());
                                    Token objectToken = new Token();
                                    Token subject2=extractSubject2( parseConfiguration,  posTagSequence, line);
                                    System.out.println("subject2: enter condition"+ subject2);
                                    objectToken.setForm(ptt.getToken().toString()+ " "+ ptt_child.getToken().toString());
                                    SentenceRelation sentenceRelation = new SentenceRelation();
                                    SentenceRelationId sentenceRelationId = new SentenceRelationId();
                                    sentenceRelationId.setSubject(subject2);
                                    sentenceRelationId.setObject(objectToken);
                                    sentenceRelationId.setRelation("(V,root) +(NPP,suj)+(NPP,mod)+ " + ptt.getComment());
                                    sentenceRelationId.setSentence_text(line);
                                    sentenceRelationId.setType(SentenceRelationType.hasAmbasador);
                                    sentenceRelationId.setConfidence(1);
                                    sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                    sentenceRelation.setMethod(SentenceRelationMethod.rules);
                                    list_result.add(sentenceRelation);
                                }
                                if ((ptt_child.getTag().toString().equalsIgnoreCase("NPP")
                                        && (parseConfiguration.getGoverningDependency(ptt_child).getLabel().equalsIgnoreCase("coord") && ptt_child.getComment().equalsIgnoreCase("product") && !ptt_child.getComment().equalsIgnoreCase("brand") )
                                        )) {
                                    List<PosTaggedToken> pos_tagged_token_child2 = parseConfiguration.getDependents(ptt_child);

                                    for (PosTaggedToken ptt_child2 : pos_tagged_token_child2) {
                                        if ((ptt_child2.getTag().toString().equalsIgnoreCase("NPP")
                                                && (parseConfiguration.getGoverningDependency(ptt_child2).getLabel().equalsIgnoreCase("mod"))
                                        )) {

                                            System.out.println("pppppppp: "+ ptt.getTag()+ " "+ ptt.getToken().toString());
                                            Token objectToken = new Token();
                                            Token subject2=extractSubject2( parseConfiguration,  posTagSequence, line);
                                            System.out.println("subject2: enter condition"+ subject2);
                                            objectToken.setForm(ptt_child.getToken().toString() + " "+ ptt_child2.getToken().toString());
                                            SentenceRelation sentenceRelation = new SentenceRelation();
                                            SentenceRelationId sentenceRelationId = new SentenceRelationId();
                                            sentenceRelationId.setSubject(subject2);
                                            sentenceRelationId.setObject(objectToken);
                                            sentenceRelationId.setRelation("(V,root) +(NPP,suj)+(NPP,mod)+ " + ptt.getComment());
                                            sentenceRelationId.setSentence_text(line);
                                            sentenceRelationId.setType(SentenceRelationType.hasAmbasador);
                                            sentenceRelationId.setConfidence(1);
                                            sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                            sentenceRelation.setMethod(SentenceRelationMethod.rules);
                                            list_result.add(sentenceRelation);
                                        }

                                        if ((ptt_child2.getTag().toString().equalsIgnoreCase("CC")
                                                && (parseConfiguration.getGoverningDependency(ptt_child2).getLabel().equalsIgnoreCase("coord"))
                                        )) {
                                            List<PosTaggedToken> pos_tagged_token_child3 = parseConfiguration.getDependents(ptt_child2);

                                            for (PosTaggedToken ptt_child3 : pos_tagged_token_child3) {
                                                if ((ptt_child3.getTag().toString().equalsIgnoreCase("V")
                                                        && (parseConfiguration.getGoverningDependency(ptt_child3).getLabel().equalsIgnoreCase("dep_coord") && !ptt_child3.getComment().equalsIgnoreCase("product")
                                                && !ptt_child3.getComment().equalsIgnoreCase("brand"))
                                                )) {
                                                    List<PosTaggedToken> pos_tagged_token_child4 = parseConfiguration.getDependents(ptt_child3);
                                                    for (PosTaggedToken ptt_child4 : pos_tagged_token_child4) {
                                                        if (ptt_child4.getTag().toString().equalsIgnoreCase("NPP")
                                                                && parseConfiguration.getGoverningDependency(ptt_child4).getLabel().equalsIgnoreCase("obj")) {
                                                            System.out.println("pppppppp: " + ptt.getTag() + " " + ptt.getToken().toString());
                                                            Token objectToken = new Token();
                                                            Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                                            System.out.println("subject2: enter condition" + subject2);
                                                            objectToken.setForm(ptt_child3.getToken().toString() + " " + ptt_child4.getToken());
                                                            SentenceRelation sentenceRelation = new SentenceRelation();
                                                            SentenceRelationId sentenceRelationId = new SentenceRelationId();
                                                            sentenceRelationId.setSubject(subject2);
                                                            sentenceRelationId.setObject(objectToken);
                                                            sentenceRelationId.setRelation("(V,root) +(NPP,suj)+(NPP,mod)+ " + ptt.getComment());
                                                            sentenceRelationId.setSentence_text(line);
                                                            sentenceRelationId.setType(SentenceRelationType.hasAmbasador);
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

                        // règle pour la relation hasAmbassador (V,root) +(NC,suj)+(NPP,mod)
                        if (ptt.getTag().toString().equalsIgnoreCase("NC") && parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("suj") ) {
                            List<PosTaggedToken> pos_tagged_token_child2 = parseConfiguration.getDependents(ptt);
                            for (PosTaggedToken ptt_child2 : pos_tagged_token_child2) {
                                if ((ptt_child2.getTag().toString().equalsIgnoreCase("NPP")
                                        && (parseConfiguration.getGoverningDependency(ptt_child2).getLabel().equalsIgnoreCase("mod"))
                                )) {
                                    Token objectToken = new Token();
                                    objectToken.setForm(ptt.getToken().toString() + " "+ ptt_child2.getToken().toString());
                                    SentenceRelation sentenceRelation = new SentenceRelation();
                                    SentenceRelationId sentenceRelationId = new SentenceRelationId();
                                    Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                    System.out.println("subject2: enter condition" + subject2);
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

                        // règle pour la relation hasAmbassador (V,root) +(NC,suj)+(NPP,mod)
                        if (ptt.getTag().toString().equalsIgnoreCase("P") && parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("mod")) {
                            List<PosTaggedToken> pos_tagged_token_test_0 = parseConfiguration.getDependents(ptt);
                            for (PosTaggedToken ptt_test0 : pos_tagged_token_test_0) {
                                if (ptt_test0.getTag().toString().equalsIgnoreCase("NC") && parseConfiguration.getGoverningDependency(ptt_test0).getLabel().equalsIgnoreCase("prep")
                                        && !ptt_test0.getComment().equalsIgnoreCase("product")   ) {
                                    List<PosTaggedToken> pos_tagged_token_child2 = parseConfiguration.getDependents(ptt_test0);
                                    for (PosTaggedToken ptt_child2 : pos_tagged_token_child2) {
                                        if ((ptt_child2.getTag().toString().equalsIgnoreCase("NPP")
                                                && (parseConfiguration.getGoverningDependency(ptt_child2).getLabel().equalsIgnoreCase("mod"))
                                        )) {
                                            Token objectToken = new Token();
                                            objectToken.setForm(ptt_test0.getToken().toString() + " " + ptt_child2.getToken().toString());
                                            SentenceRelation sentenceRelation = new SentenceRelation();
                                            SentenceRelationId sentenceRelationId = new SentenceRelationId();
                                            Token subject2 = extractSubject2(parseConfiguration, posTagSequence, line);
                                            System.out.println("subject2: enter condition" + subject2);
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

                        //règle pour la relation hasComponent (v+root) + [(P,P_obj)||(de_obj)] + [[(NPP,prep)||(NC,prep)]||((cc,coord)+(P+D,dep_coord)+(NC,prep)]]
                        if (ptt.getTag().toString().equalsIgnoreCase("P") &&
                           (parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("P_obj") || parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("de_obj")
                                        )) {
                            List<PosTaggedToken> pos_tagged_token_test_0 = parseConfiguration.getDependents(ptt);
                            for (PosTaggedToken ptt_test0 : pos_tagged_token_test_0) {
                                if ((ptt_test0.getTag().toString().equalsIgnoreCase("NC") || (ptt_test0.getTag().toString().equalsIgnoreCase("NPP"))
                                        && parseConfiguration.getGoverningDependency(ptt_test0).getLabel().equalsIgnoreCase("prep"))
                                        && ptt_test0.getComment().isEmpty()) {
                                    Token subject2=extractSubject2( parseConfiguration,  posTagSequence, line);
                                    System.out.println("subject2: enter condition"+ subject2);
                                        Token objectToken = new Token();
                                        objectToken.setForm(ptt_test0.getToken().toString());
                                        SentenceRelation sentenceRelation = new SentenceRelation();
                                        SentenceRelationId sentenceRelationId = new SentenceRelationId();
                                        sentenceRelationId.setSubject(subject2);
                                        sentenceRelationId.setObject(objectToken);
                                        sentenceRelationId.setRelation("(v+root) + [(P,P_obj)||(de_obj)] + [[(NPP,prep)||(NC,prep)]||((cc,coord)+(P+D,dep_coord)+(NC,prep)]]");
                                        sentenceRelationId.setSentence_text(line);
                                        sentenceRelationId.setConfidence(1);
                                        sentenceRelationId.setType(SentenceRelationType.hasComponent);
                                        sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                        sentenceRelation.setMethod(SentenceRelationMethod.rules);
                                        list_result.add(sentenceRelation);

                                }

                                //Le lait Fermeté sculpturale contient de la céramide et du collagène et promet une double action anti-dessèchement et fermeté remodelante.
                                if ((ptt_test0.getTag().toString().equalsIgnoreCase("CC") && parseConfiguration.getGoverningDependency(ptt_test0).getLabel().equalsIgnoreCase("coord"))) {
                                    List<PosTaggedToken> pos_tagged_token_test_1 = parseConfiguration.getDependents(ptt_test0);
                                    for (PosTaggedToken ptt_test1 : pos_tagged_token_test_1) {
                                        if ((ptt_test1.getTag().toString().equalsIgnoreCase("P+D")) && parseConfiguration.getGoverningDependency(ptt_test1).getLabel().equalsIgnoreCase("dep_coord")) {
                                            List<PosTaggedToken> pos_tagged_token_test_2 = parseConfiguration.getDependents(ptt_test1);
                                            for (PosTaggedToken ptt_test2 : pos_tagged_token_test_2) {
                                                if ((ptt_test2.getTag().toString().equalsIgnoreCase("NC")) && parseConfiguration.getGoverningDependency(ptt_test2).getLabel().equalsIgnoreCase("prep")
                                                        && ptt_test2.getComment().isEmpty()) {
                                                        Token objectToken = new Token();
                                                        objectToken.setForm(ptt_test2.getToken().toString());
                                                        SentenceRelation sentenceRelation = new SentenceRelation();
                                                        SentenceRelationId sentenceRelationId = new SentenceRelationId();
                                                    Token subject2=extractSubject2( parseConfiguration,  posTagSequence, line);
                                                    System.out.println("subject2: enter condition"+ subject2);
                                                        sentenceRelationId.setSubject(subject2);
                                                        sentenceRelationId.setObject(objectToken);
                                                        sentenceRelationId.setRelation("(v+root) + [(P,P_obj)||(de_obj)] + [[(NPP,prep)||(NC,prep)]||((cc,coord)+(P+D,dep_coord)+(NC,prep)]]");
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


                //Rules with (NC, mod||suj ||prep)
                if (posTagSequence.get(i).getTag().toString().equalsIgnoreCase("NC")
                        && (parseConfiguration.getGoverningDependency(posTagSequence.get(i)).getLabel().equalsIgnoreCase("mod")
                        || parseConfiguration.getGoverningDependency(posTagSequence.get(i)).getLabel().equalsIgnoreCase("suj")
                ||parseConfiguration.getGoverningDependency(posTagSequence.get(i)).getLabel().equalsIgnoreCase("prep")) ) {
                    List<PosTaggedToken> pos_tagged_token = parseConfiguration.getDependents(posTagSequence.get(i));
                    for (PosTaggedToken ptt : pos_tagged_token) {
                        if ((ptt.getTag().toString().equalsIgnoreCase("P")||ptt.getTag().toString().equalsIgnoreCase("P+D"))
                                && parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("dep")) {

                            List<PosTaggedToken> pos_tagged_token_test = parseConfiguration.getDependents(ptt);
                          //  if (!pos_tagged_token_test.isEmpty()) {
                                for (PosTaggedToken ptt_test : pos_tagged_token_test) {
                                    //hasFragranceCreator (NC,mod) + (P,dep) + [[(NPP,prep)+ (NPP,mod)] || [ (NPP,prep]+(CC,coord)+(NPP,dep_coord)+(NPP,mod)]]
                                    if (ptt_test.getTag().toString().equalsIgnoreCase("NPP") && parseConfiguration.getGoverningDependency(ptt_test).getLabel().equalsIgnoreCase("prep")
                                        ) {
                                           Token objectToken = new Token();
                                            objectToken.setForm(ptt_test.getToken().toString());
                                            SentenceRelation sentenceRelation = new SentenceRelation();
                                            SentenceRelationId sentenceRelationId = new SentenceRelationId();
                                        Token subject2=extractSubject2( parseConfiguration,  posTagSequence, line);
                                        System.out.println("subject2: enter condition"+ subject2);
                                            sentenceRelationId.setSubject(subject2);
                                            sentenceRelationId.setObject(objectToken);
                                            sentenceRelationId.setRelation("(NC,(suj||mod||prep)) + ((P||(P+D)),dep) + [[(NPP,prep)+ (NPP,mod)] || [(NPP,prep)+(CC,coord)+(NPP,dep_coord)+(NPP,mod)]]");
                                            sentenceRelationId.setSentence_text(line);
                                            sentenceRelationId.setConfidence(1);
                                            sentenceRelationId.setType(SentenceRelationType.hasFragranceCreator);
                                            sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                            sentenceRelation.setMethod(SentenceRelationMethod.rules);
                                            list_result.add(sentenceRelation);
                                          List<PosTaggedToken> pos_tagged_token_test1 = parseConfiguration.getDependents(ptt_test);
                                        for (PosTaggedToken ptt_test1 : pos_tagged_token_test1) {
                                            if (ptt_test1.getTag().toString().equalsIgnoreCase("CC") && parseConfiguration.getGoverningDependency(ptt_test1).getLabel().equalsIgnoreCase("coord")) {
                                                List<PosTaggedToken> pos_tagged_token_test2 = parseConfiguration.getDependents(ptt_test1);
                                                for (PosTaggedToken ptt_test2 : pos_tagged_token_test2) {
                                                    if (ptt_test2.getTag().toString().equalsIgnoreCase("NPP") &&
                                                            parseConfiguration.getGoverningDependency(ptt_test2).getLabel().equalsIgnoreCase("dep_coord")
                                                          ) {
                                                            Token objectToken2 = new Token();
                                                            objectToken2.setForm(ptt_test2.getToken().toString());
                                                            SentenceRelation sentenceRelation2 = new SentenceRelation();
                                                            SentenceRelationId sentenceRelationId2 = new SentenceRelationId();

                                                            sentenceRelationId2.setSubject(subject2);
                                                            sentenceRelationId2.setObject(objectToken2);
                                                            sentenceRelationId2.setRelation("(NC,(suj||mod||prep)) + ((P||(P+D)),dep) + [[(NPP,prep)+ (NPP,mod)] || [(NPP,prep)+(CC,coord)+(NPP,dep_coord)+(NPP,mod)]]");
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

                //Rules with (NC, mod||suj ||prep)
                if (posTagSequence.get(i).getTag().toString().equalsIgnoreCase("VPR") && parseConfiguration.getGoverningDependency(posTagSequence.get(i)).getLabel().equalsIgnoreCase("mod") ) {
                    List<PosTaggedToken> pos_tagged_token = parseConfiguration.getDependents(posTagSequence.get(i));
                    for (PosTaggedToken ptt : pos_tagged_token) {
                        // règle pour la relation hasComponent
                        //Le baume Fermeté sculputure, associant Skinfibrine et élastopeptides, vise à rendre la peau plus ferme et élastique.
                        if (ptt.getTag().toString().equalsIgnoreCase("NPP") && (parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("obj"))
                                && ptt.getComment().isEmpty()) {
                            boolean annotated = false;
                            List<PosTaggedToken> pos_tagged_token_test = parseConfiguration.getDependents(ptt);
                            for (PosTaggedToken ptt_test : pos_tagged_token_test) {
                                if (ptt_test.getTag().toString().equalsIgnoreCase("ADJ") && parseConfiguration.getGoverningDependency(ptt_test).getLabel().equalsIgnoreCase("mod")
                                        && ptt_test.getComment().isEmpty()) {
                                        annotated = true;
                                        Token objectToken = new Token();
                                        objectToken.setForm(ptt.getToken().toString() + " " + ptt_test.getToken().toString());
                                        SentenceRelation sentenceRelation = new SentenceRelation();
                                        SentenceRelationId sentenceRelationId = new SentenceRelationId();
                                    Token subject2=extractSubject2( parseConfiguration,  posTagSequence, line);
                                    System.out.println("subject2: enter condition"+ subject2);
                                        sentenceRelationId.setSubject(subject2);
                                        sentenceRelationId.setObject(objectToken);
                                        sentenceRelationId.setRelation("(VPR,mod) + [(NPP,obj) ||[(NPP,obj)+(Adj,mod)* +(CC,coord) + (NPP,dep_coord)]");
                                        sentenceRelationId.setSentence_text(line);
                                        sentenceRelationId.setConfidence(1);
                                        sentenceRelationId.setType(SentenceRelationType.hasComponent);
                                        sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                        sentenceRelation.setMethod(SentenceRelationMethod.rules);
                                        list_result.add(sentenceRelation);

                                }
                                if (!annotated) {
                                        Token objectToken = new Token();
                                        objectToken.setForm(ptt.getToken().toString());
                                        SentenceRelation sentenceRelation = new SentenceRelation();
                                        SentenceRelationId sentenceRelationId = new SentenceRelationId();
                                    Token subject2=extractSubject2( parseConfiguration,  posTagSequence, line);
                                    System.out.println("subject2: enter condition"+ subject2);
                                        sentenceRelationId.setSubject(subject2);
                                        sentenceRelationId.setObject(objectToken);
                                        sentenceRelationId.setRelation("(VPR,mod) + [(NPP,obj)+(Adj,mod)* ||[(NPP,obj) +(CC,coord) + (NPP,dep_coord)]");
                                        sentenceRelationId.setSentence_text(line);
                                        sentenceRelationId.setConfidence(1);
                                        sentenceRelationId.setType(SentenceRelationType.hasComponent);
                                        sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                        sentenceRelation.setMethod(SentenceRelationMethod.rules);
                                        list_result.add(sentenceRelation);
                                }

                                if (ptt_test.getTag().toString().equalsIgnoreCase("CC") && (parseConfiguration.getGoverningDependency(ptt_test).getLabel().equalsIgnoreCase("coord"))
                                        && !ptt_test.getComment().equalsIgnoreCase("product")) {
                                    List<PosTaggedToken> pos_tagged_token_test1 = parseConfiguration.getDependents(ptt_test);
                                    for (PosTaggedToken ptt_test1 : pos_tagged_token_test1) {
                                        if (ptt_test1.getTag().toString().equalsIgnoreCase("NPP") && (parseConfiguration.getGoverningDependency(ptt_test1).getLabel().equalsIgnoreCase("dep_coord"))
                                                && ptt_test1.getComment().isEmpty()) {
                                            //test ADJ
                                            List<PosTaggedToken> pos_tagged_token_test_2 = parseConfiguration.getDependents(ptt_test1);
                                            for (PosTaggedToken ptt_test_2 : pos_tagged_token_test_2) {
                                                if (ptt_test_2.getTag().toString().equalsIgnoreCase("ADJ") && parseConfiguration.getGoverningDependency(ptt_test_2).getLabel().equalsIgnoreCase("mod")
                                                        ) {
                                                    annotated = true;
                                                    Token objectToken2 = new Token();
                                                        objectToken2.setForm(ptt_test1.getToken().toString() + " " + ptt_test_2.getToken().toString());
                                                        SentenceRelation sentenceRelation2 = new SentenceRelation();
                                                        SentenceRelationId sentenceRelationId2 = new SentenceRelationId();
                                                    Token subject2=extractSubject2( parseConfiguration,  posTagSequence, line);
                                                    System.out.println("subject2: enter condition"+ subject2);
                                                        sentenceRelationId2.setSubject(subject2);
                                                        sentenceRelationId2.setObject(objectToken2);
                                                        sentenceRelationId2.setRelation("(VPR,mod) + [(NPP,obj)+(Adj,mod)* ||[(NPP,obj) +(CC,coord) + (NPP,dep_coord)+(Adj,mod)*]");
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
                                                    Token subject2=extractSubject2( parseConfiguration,  posTagSequence, line);
                                                    System.out.println("subject2: enter condition"+ subject2);
                                                        sentenceRelationId2.setSubject(subject2);
                                                        sentenceRelationId2.setObject(objectToken2);
                                                        sentenceRelationId2.setRelation("(VPR,mod) + [(NPP,obj)+(Adj,mod)* ||[(NPP,obj) +(CC,coord) + (NPP,dep_coord)(Adj,mod)*]");
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

                // rules with (VPP, mod)
                if (posTagSequence.get(i).getTag().toString().equalsIgnoreCase("VPP") && parseConfiguration.getGoverningDependency(posTagSequence.get(i)).getLabel().equalsIgnoreCase("mod") ) {
                    List<PosTaggedToken> pos_tagged_token = parseConfiguration.getDependents(posTagSequence.get(i));
                    for (PosTaggedToken ptt : pos_tagged_token) {
                        //rafraîchir // caractériser // composé
                        // règle pour la relation hasComponent
                        //	Avec sa peau diaphane et ses cheveux blond vénitien, Jessica Chastain incarnera le nouveau parfum féminin de la griffe, Manifesto, construit au-tour du jasmin.
                        if (ptt.getTag().toString().equalsIgnoreCase("NC") && (parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("mod"))) {
                            // liste de dependence
                            List<PosTaggedToken> pos_tagged_token_test = parseConfiguration.getDependents(ptt);
                            for (PosTaggedToken ptt_test : pos_tagged_token_test) {
                                boolean annotated = false;
                                if (ptt_test.getTag().toString().equalsIgnoreCase("P+D") && (parseConfiguration.getGoverningDependency(ptt_test).getLabel().equalsIgnoreCase("dep"))) {
                                    List<PosTaggedToken> pos_tagged_token_test1 = parseConfiguration.getDependents(ptt_test);
                                    for (PosTaggedToken ptt_test1 : pos_tagged_token_test1) {
                                        if (ptt_test1.getTag().toString().equalsIgnoreCase("NC") && (parseConfiguration.getGoverningDependency(ptt_test1).getLabel().equalsIgnoreCase("prep"))
                                                && ptt_test1.getComment().isEmpty()) {

                                            List<PosTaggedToken> pos_tagged_token_test2 = parseConfiguration.getDependents(ptt_test1);

                                            for (PosTaggedToken ptt_test2 : pos_tagged_token_test2) {
                                                if (ptt_test2.getTag().toString().equalsIgnoreCase("ADJ") && (parseConfiguration.getGoverningDependency(ptt_test2).getLabel().equalsIgnoreCase("mod"))
                                                        ) {
                                                        annotated = true;
                                                        Token objectToken = new Token();
                                                        objectToken.setForm(ptt_test1.getToken().toString() + " " + ptt_test2.getToken().toString());
                                                        SentenceRelation sentenceRelation = new SentenceRelation();
                                                        SentenceRelationId sentenceRelationId = new SentenceRelationId();
                                                    Token subject2=extractSubject2( parseConfiguration,  posTagSequence, line);
                                                    System.out.println("subject2: enter condition"+ subject2);
                                                        sentenceRelationId.setSubject(subject2);

                                                        sentenceRelationId.setObject(objectToken);
                                                        sentenceRelationId.setRelation("(VPP, mod)+ (NC,mod)+(P+D,dep)+(NC,prep)+(ADj,mod)*");
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
                                                        SentenceRelation sentenceRelation = new SentenceRelation();
                                                        SentenceRelationId sentenceRelationId = new SentenceRelationId();
                                                    Token subject2=extractSubject2( parseConfiguration,  posTagSequence, line);
                                                    System.out.println("subject2: enter condition"+ subject2);
                                                        sentenceRelationId.setSubject(subject2);
                                                        sentenceRelationId.setObject(objectToken);
                                                        sentenceRelationId.setRelation("(VPP, mod)+ (NC,mod)+(P+D,dep)+(NC,prep)+(ADj,mod)*");
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

                        if (ptt.getTag().toString().equalsIgnoreCase("P+D") && (parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("mod"))) {
                            // liste de dependence
                            List<PosTaggedToken> pos_tagged_token_test = parseConfiguration.getDependents(ptt);
                            for (PosTaggedToken ptt_test : pos_tagged_token_test) {
                                if (ptt_test.getTag().toString().equalsIgnoreCase("NC") && (parseConfiguration.getGoverningDependency(ptt_test).getLabel().equalsIgnoreCase("prep"))
                                        && ptt_test.getComment().isEmpty()) {
                                    List<PosTaggedToken> pos_tagged_token_test2 = parseConfiguration.getDependents(ptt_test);
                                    boolean annotated = false;
                                    for (PosTaggedToken ptt_test2 : pos_tagged_token_test2) {
                                        if (ptt_test2.getTag().toString().equalsIgnoreCase("ADJ") && (parseConfiguration.getGoverningDependency(ptt_test2).getLabel().equalsIgnoreCase("mod"))
                                                ) {
                                            annotated = true;
                                                Token objectToken = new Token();
                                                objectToken.setForm(ptt_test.getToken().toString() + " " + ptt_test2.getToken().toString());
                                                SentenceRelation sentenceRelation = new SentenceRelation();
                                                SentenceRelationId sentenceRelationId = new SentenceRelationId();
                                            Token subject2=extractSubject2( parseConfiguration,  posTagSequence, line);
                                            System.out.println("subject2: enter condition"+ subject2);
                                                sentenceRelationId.setSubject(subject2);
                                                sentenceRelationId.setObject(objectToken);
                                                sentenceRelationId.setRelation("(VPP, mod)+ (NC,mod)+(P+D,dep)+(NC,prep)+(ADj,mod)*");
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
                                        Token subject2=extractSubject2( parseConfiguration,  posTagSequence, line);
                                        System.out.println("subject2: enter condition"+ subject2);
                                            sentenceRelationId.setSubject(subject2);
                                            sentenceRelationId.setObject(objectToken);
                                            sentenceRelationId.setRelation("(VPP, mod)+ (NC,mod)+(P+D,dep)+(NC,prep)+(ADj,mod)*");
                                            sentenceRelationId.setSentence_text(line);
                                            sentenceRelationId.setConfidence(1);
                                            sentenceRelationId.setType(SentenceRelationType.hasComponent);
                                            sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                            sentenceRelation.setMethod(SentenceRelationMethod.rules);
                                            list_result.add(sentenceRelation);

                                    }
                                }

                                if (ptt_test.getTag().toString().equalsIgnoreCase("CC") && (parseConfiguration.getGoverningDependency(ptt_test).getLabel().equalsIgnoreCase("coord"))) {
                                    List<PosTaggedToken> pos_tagged_token_test4 = parseConfiguration.getDependents(ptt_test);
                                    for (PosTaggedToken ptt_test4 : pos_tagged_token_test4) {
                                        if (ptt_test4.getTag().toString().equalsIgnoreCase("P") && parseConfiguration.getGoverningDependency(ptt_test4).getLabel().equalsIgnoreCase("dep_coord")) {
                                            List<PosTaggedToken> pos_tagged_token_test5 = parseConfiguration.getDependents(ptt_test4);
                                            for (PosTaggedToken ptt_test5 : pos_tagged_token_test5) {
                                                if (ptt_test5.getTag().toString().equalsIgnoreCase("NC") &&
                                                        parseConfiguration.getGoverningDependency(ptt_test5).getLabel().equalsIgnoreCase("prep")
                                                        && ptt_test5.getComment().isEmpty()) {
                                                    ///revoir
                                                    SentenceRelation sentenceRelation = new SentenceRelation();
                                                    SentenceRelationId sentenceRelationId = new SentenceRelationId();
                                                    Token objectToken = new Token();
                                                        objectToken.setForm(ptt_test5.getToken().toString());
                                                    Token subject2=extractSubject2( parseConfiguration,  posTagSequence, line);
                                                    System.out.println("subject2: enter condition"+ subject2);
                                                        sentenceRelationId.setSubject(subject2);
                                                        sentenceRelationId.setObject(objectToken);
                                                        sentenceRelationId.setRelation("(VPP, root) + * + [[(NC,prep)+(ADJ,mod)]|| [(NPP,prep) +(NPP,mod)] ||(NC, dep_coord)]");
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

                        //
                        if (ptt.getTag().toString().equalsIgnoreCase("P") && (parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("de_obj"))) {
                            // liste de dependence
                            List<PosTaggedToken> pos_tagged_token_test = parseConfiguration.getDependents(ptt);
                            for (PosTaggedToken ptt_test : pos_tagged_token_test) {
                                if (ptt_test.getTag().toString().equalsIgnoreCase("NC") && (parseConfiguration.getGoverningDependency(ptt_test).getLabel().equalsIgnoreCase("prep"))
                                        && ptt_test.getComment().isEmpty()/* !ptt_test.getComment().equalsIgnoreCase("product") && !ptt_test.getComment().equalsIgnoreCase("range")  && !ptt_test.getComment().equalsIgnoreCase("PER")*/) {
                                    boolean annotated = false;
                                    System.out.println("tttttttoken ananas: "+ ptt_test.getToken() + "comments: "+ ptt_test.getComment());
                                    List<PosTaggedToken> pos_tagged_token_test1 = parseConfiguration.getDependents(ptt_test);
                                    for (PosTaggedToken ptt_test1 : pos_tagged_token_test1) {
                                        if (ptt_test1.getTag().toString().equalsIgnoreCase("ADJ") && (parseConfiguration.getGoverningDependency(ptt_test1).getLabel().equalsIgnoreCase("mod"))
                                                ) {
                                            annotated = true;
                                            Token objectToken = new Token();
                                            objectToken.setForm(ptt_test.getToken().toString());
                                            SentenceRelation sentenceRelation = new SentenceRelation();
                                            SentenceRelationId sentenceRelationId = new SentenceRelationId();
                                            Token subject2=extractSubject2( parseConfiguration,  posTagSequence, line);
                                            System.out.println("subject2: enter condition"+ subject2);
                                            sentenceRelationId.setSubject(subject2);
                                            sentenceRelationId.setObject(objectToken);
                                            sentenceRelationId.setRelation("(VPP,mod)+(P,de_obj)+(NC,prep)+(ADJ,mod)*");
                                            sentenceRelationId.setSentence_text(line);
                                            sentenceRelationId.setConfidence(1);
                                            sentenceRelationId.setType(SentenceRelationType.hasComponent);
                                            sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                            sentenceRelation.setMethod(SentenceRelationMethod.rules);
                                            list_result.add(sentenceRelation);

                                        }
                                        if (!annotated) {
                                                Token objectToken = new Token();
                                                objectToken.setForm(ptt_test.getToken().toString());
                                                SentenceRelation sentenceRelation = new SentenceRelation();
                                                SentenceRelationId sentenceRelationId = new SentenceRelationId();
                                            Token subject2=extractSubject2( parseConfiguration,  posTagSequence, line);
                                            System.out.println("subject2: enter condition"+ subject2);
                                                sentenceRelationId.setSubject(subject2);
                                                sentenceRelationId.setObject(objectToken);
                                                sentenceRelationId.setRelation("(VPP,mod)+(P,de_obj)+(NC,prep)+(ADJ,mod)*");
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

                        if (ptt.getTag().toString().equalsIgnoreCase("P") && (parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("p_obj"))) {
                            // liste de dependence
                            List<PosTaggedToken> pos_tagged_token_test = parseConfiguration.getDependents(ptt);
                            for (PosTaggedToken ptt_test : pos_tagged_token_test) {
                                if (ptt_test.getTag().toString().equalsIgnoreCase("NC") && (parseConfiguration.getGoverningDependency(ptt_test).getLabel().equalsIgnoreCase("prep"))) {
                                    List<PosTaggedToken> pos_tagged_token_test_0 = parseConfiguration.getDependents(ptt_test);
                                    for (PosTaggedToken ptt_test_0 : pos_tagged_token_test_0) {
                                        if (ptt_test_0.getTag().toString().equalsIgnoreCase("NPP") && (parseConfiguration.getGoverningDependency(ptt_test_0).getLabel().equalsIgnoreCase("mod"))
                                                && ptt_test_0.getComment().isEmpty()) {
//à revoir
                                                Token objectToken = new Token();
                                                objectToken.setForm(ptt_test.getToken().toString() + " " + ptt_test_0.getToken().toString());
                                                SentenceRelation sentenceRelation = new SentenceRelation();
                                                SentenceRelationId sentenceRelationId = new SentenceRelationId();
                                            Token subject2=extractSubject2( parseConfiguration,  posTagSequence, line);
                                            System.out.println("subject2: enter condition"+ subject2);
                                                sentenceRelationId.setSubject(subject2);
                                                sentenceRelationId.setObject(objectToken);
                                                sentenceRelationId.setRelation(ptt_test.getTag().toString());
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

                //hasComponent (VPP, root)

                if (posTagSequence.get(i).getTag().toString().equalsIgnoreCase("VPP") && parseConfiguration.getGoverningDependency(posTagSequence.get(i)).getLabel().equalsIgnoreCase("root")) {
                    //boolean annotated=false;
                    //Liste des dependences de i
                    List<PosTaggedToken> pos_tagged_token = parseConfiguration.getDependents(posTagSequence.get(i));
                    System.out.println(pos_tagged_token);
                    for (PosTaggedToken ptt : pos_tagged_token) {
                        if (ptt.getTag().toString().equalsIgnoreCase("P+D") && parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("de_obj")) {
                            List<PosTaggedToken> pos_tagged_token_test = parseConfiguration.getDependents(ptt);
                            for (PosTaggedToken ptt_test : pos_tagged_token_test) {
                                if (ptt_test.getTag().toString().equalsIgnoreCase("NC") && parseConfiguration.getGoverningDependency(ptt_test).getLabel().equalsIgnoreCase("prep")) {
                                    List<PosTaggedToken> pos_tagged_token_test1 = parseConfiguration.getDependents(ptt_test);
                                    for (PosTaggedToken ptt_test1 : pos_tagged_token_test1) {
                                        if (ptt_test1.getTag().toString().equalsIgnoreCase("P") && parseConfiguration.getGoverningDependency(ptt_test1).getLabel().equalsIgnoreCase("dep")) {
                                            List<PosTaggedToken> pos_tagged_token_test2 = parseConfiguration.getDependents(ptt_test1);
                                            for (PosTaggedToken ptt_test2 : pos_tagged_token_test2) {
                                                if (ptt_test2.getTag().toString().equalsIgnoreCase("NC") && parseConfiguration.getGoverningDependency(ptt_test2).getLabel().equalsIgnoreCase("prep")) {
                                                    List<PosTaggedToken> pos_tagged_token_test3 = parseConfiguration.getDependents(ptt_test2);
                                                    boolean annotated=false;
                                                    for (PosTaggedToken ptt_test3 : pos_tagged_token_test3) {
                                                        if (ptt_test3.getTag().toString().equalsIgnoreCase("ADJ") &&
                                                                parseConfiguration.getGoverningDependency(ptt_test3).getLabel().equalsIgnoreCase("mod")
                                                                 && ptt_test3.getComment().isEmpty()) {
                                                                annotated = true;
                                                            Token subject2=extractSubject2( parseConfiguration,  posTagSequence, line);
                                                            System.out.println("subject2: enter condition"+ subject2);
                                                                SentenceRelation sentenceRelation = new SentenceRelation();
                                                                SentenceRelationId sentenceRelationId = new SentenceRelationId();
                                                                Token objectToken = new Token();
                                                                objectToken.setForm(ptt_test2.getToken().toString() + " " + ptt_test3.getToken().toString());
                                                                sentenceRelationId.setSubject(subject2);
                                                                sentenceRelationId.setObject(objectToken);
                                                                sentenceRelationId.setRelation("(VPP, root) + * + [[(NC,prep)+(ADJ,mod)]|| [(NPP,prep) +(NPP,mod)] ||(NC, dep_coord)]");
                                                                sentenceRelationId.setSentence_text(line);
                                                                sentenceRelationId.setConfidence(1);
                                                                sentenceRelationId.setType(SentenceRelationType.hasComponent);
                                                                sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                                                sentenceRelation.setMethod(SentenceRelationMethod.rules);
                                                                list_result.add(sentenceRelation);

                                                        }
                                                        if(!annotated) {
                                                            SentenceRelation sentenceRelation = new SentenceRelation();
                                                            SentenceRelationId sentenceRelationId = new SentenceRelationId();
                                                            Token objectToken = new Token();
                                                            Token subject2=extractSubject2( parseConfiguration,  posTagSequence, line);
                                                            System.out.println("subject2: enter condition"+ subject2);
                                                            objectToken.setForm(ptt_test2.getToken().toString());
                                                                sentenceRelationId.setSubject(subject2);
                                                                sentenceRelationId.setObject(objectToken);
                                                                sentenceRelationId.setRelation("(VPP, root) + * + [[(NC,prep)+(ADJ,mod)]|| [(NPP,prep) +(NPP,mod)] ||(NC, dep_coord)]");
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
                                                                        parseConfiguration.getGoverningDependency(ptt_test4).getLabel().equalsIgnoreCase("dep_coord")) {
                                                                    List<PosTaggedToken> pos_tagged_token_test5 = parseConfiguration.getDependents(ptt_test4);
                                                                    for (PosTaggedToken ptt_test5 : pos_tagged_token_test5) {
                                                                        if (ptt_test5.getTag().toString().equalsIgnoreCase("P") &&
                                                                                parseConfiguration.getGoverningDependency(ptt_test5).getLabel().equalsIgnoreCase("dep")) {
                                                                            List<PosTaggedToken> pos_tagged_token_test6 = parseConfiguration.getDependents(ptt_test5);
                                                                            for (PosTaggedToken ptt_test6 : pos_tagged_token_test6) {
                                                                                if (ptt_test6.getTag().toString().equalsIgnoreCase("NPP") &&
                                                                                        parseConfiguration.getGoverningDependency(ptt_test6).getLabel().equalsIgnoreCase("Prep")) {
                                                                                    List<PosTaggedToken> pos_tagged_token_test7 = parseConfiguration.getDependents(ptt_test6);
                                                                                    for (PosTaggedToken ptt_test7 : pos_tagged_token_test7) {
                                                                                        if (ptt_test7.getTag().toString().equalsIgnoreCase("NPP") &&
                                                                                                parseConfiguration.getGoverningDependency(ptt_test7).getLabel().equalsIgnoreCase("mod")
                                                                                                && !ptt_test7.getComment().isEmpty()) {
                                                                                            ///revoir
                                                                                            Token subject2=extractSubject2( parseConfiguration,  posTagSequence, line);
                                                                                            System.out.println("subject2: enter condition"+ subject2);
                                                                                            SentenceRelation sentenceRelation = new SentenceRelation();
                                                                                            SentenceRelationId sentenceRelationId = new SentenceRelationId();
                                                                                            Token objectToken = new Token();
                                                                                                objectToken.setForm(ptt_test6.getToken().toString() + " " + ptt_test7.getToken().toString());
                                                                                                sentenceRelationId.setSubject(subject2);
                                                                                                sentenceRelationId.setObject(objectToken);
                                                                                                sentenceRelationId.setRelation("(VPP, root) + * + [[(NC,prep)+(ADJ,mod)]|| [(NPP,prep) +(NPP,mod)] ||(NC, dep_coord)]");
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

                        if (ptt.getTag().toString().equalsIgnoreCase("CC") && parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("coord")) {
                            List<PosTaggedToken> pos_tagged_token_test = parseConfiguration.getDependents(ptt);
                            for (PosTaggedToken ptt_test : pos_tagged_token_test) {
                                if (ptt_test.getTag().toString().equalsIgnoreCase("NC") && parseConfiguration.getGoverningDependency(ptt_test).getLabel().equalsIgnoreCase("dep_coord")
                                        && ptt_test.getComment().isEmpty()) {
                                    boolean annotated = false;
                                    List<PosTaggedToken> pos_tagged_token_test3 = parseConfiguration.getDependents(ptt_test);
                                    for (PosTaggedToken ptt_test3 : pos_tagged_token_test3) {
                                        if (ptt_test3.getTag().toString().equalsIgnoreCase("ADJ") &&
                                                parseConfiguration.getGoverningDependency(ptt_test3).getLabel().equalsIgnoreCase("mod")
                                                &&  ptt_test3.getComment().isEmpty()) {
                                            annotated = true;
                                            Token objectToken = new Token();
                                                objectToken.setForm(ptt_test.getToken().toString() + " " + ptt_test3.getToken().toString());
                                                SentenceRelation sentenceRelation = new SentenceRelation();
                                                SentenceRelationId sentenceRelationId = new SentenceRelationId();
                                            Token subject2=extractSubject2( parseConfiguration,  posTagSequence, line);
                                            System.out.println("subject2: enter condition"+ subject2);
                                                sentenceRelationId.setSubject(subject2);
                                                sentenceRelationId.setObject(objectToken);
                                                sentenceRelationId.setRelation("(VPP, root) + * + [[(NC,prep)+(ADJ,mod)]|| [(NPP,prep) +(NPP,mod)] ||(NC, dep_coord)]");
                                                sentenceRelationId.setSentence_text(line);
                                                sentenceRelationId.setType(SentenceRelationType.hasComponent);
                                                sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                                sentenceRelation.setMethod(SentenceRelationMethod.rules);
                                                list_result.add(sentenceRelation);

                                        }
                                        if (!annotated) {
                                            Token objectToken = new Token();
                                                objectToken.setForm(ptt_test.getToken().toString());
                                                SentenceRelation sentenceRelation = new SentenceRelation();
                                                SentenceRelationId sentenceRelationId = new SentenceRelationId();
                                            Token subject2=extractSubject2( parseConfiguration,  posTagSequence, line);
                                            System.out.println("subject2: enter condition"+ subject2);
                                                sentenceRelationId.setSubject(subject2);
                                                sentenceRelationId.setObject(objectToken);
                                                sentenceRelationId.setRelation("(VPP, root) + * + [[(NC,prep)+(ADJ,mod)]|| [(NPP,prep) +(NPP,mod)] ||(NC, dep_coord)]");
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
