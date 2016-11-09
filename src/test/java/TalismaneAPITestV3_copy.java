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
import java.util.List;

import static fr.inria.smilk.ws.relationextraction.ExtractionHelper.elementToToken;

/**
 * Created by dhouib on 14/10/2016.
 */
public class TalismaneAPITestV3_copy extends AbstractRelationExtraction {


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
        //constructRelation();
        //annotatedByTalismane(line, nSentenceList);
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

                                    newline = line;
                                    newline = newline.replace(form, form_with_underscore);


                                }
                        }
                    }
                }
            }
        }
        System.out.println("newline: " + newline);
        annotatedByTalismane(newline, nSentenceList);
        // return newline;
    }

    private String setType(String line, NodeList nSentenceList, int token) {
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
                            if (xElement.hasAttribute("type") && !xElement.getAttribute("type").equalsIgnoreCase("not_identified"))
                            //   if (xElement.getAttribute("type").equalsIgnoreCase("product")  )
                            {
                                Token subjectToken = elementToToken(xElement);
                                if (token == (subjectToken.getHead())) {
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

    public void annotatedByTalismane(String line, NodeList nSentenceList) throws IOException, ClassNotFoundException {
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
        TokenSequence tokenSequence = tokeniser.tokeniseText(line);

        // pos-tag the token sequence
        PosTagger posTagger = talismaneConfig.getPosTagger();
        PosTagSequence posTagSequence = posTagger.tagSentence(tokenSequence);
        System.out.println("posTag: " + posTagSequence);
        Parser parser = talismaneConfig.getParser();
        ParseConfiguration parseConfiguration = parser.parseSentence(posTagSequence);
        for (int i = 0; i < posTagSequence.size(); i++) {
            posTagSequence.get(i).setComment(setType(line, nSentenceList, posTagSequence.get(i).getIndex()));
        }

        relationRules(parseConfiguration, posTagSequence);

    }

    public void relationRules(ParseConfiguration parseConfiguration, PosTagSequence posTagSequence) {

        for (int i = 0; i < posTagSequence.size(); i++) {
            if (posTagSequence.get(i).getLexicalEntry() != null) {
                if (posTagSequence.get(i).getTag().toString().equalsIgnoreCase("VPP") && parseConfiguration.getGoverningDependency(posTagSequence.get(i)).getLabel().equalsIgnoreCase("mod")) {

                    SentenceRelation sentenceRelation = new SentenceRelation();
                    SentenceRelationId sentenceRelationId = new SentenceRelationId();
                    List<PosTaggedToken> pos_tagged_token = parseConfiguration.getDependents(posTagSequence.get(i));
                    for (PosTaggedToken ptt : pos_tagged_token) {
                        //rafraîchir // caractériser // composé
                        if (ptt.getTag().toString().equalsIgnoreCase("P") && (parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("de_obj")
                                || parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("p_obj"))) {
                            // liste de dependence
                            List<PosTaggedToken> pos_tagged_token_test = parseConfiguration.getDependents(ptt);
                            System.out.println(pos_tagged_token_test);
                            if (!pos_tagged_token_test.isEmpty()) {
                                System.out.println(pos_tagged_token_test);
                                for (PosTaggedToken ptt_test : pos_tagged_token_test) {
                                    if (ptt_test.getTag().toString().equalsIgnoreCase("NC")) {
                                        System.out.println("hasComponent: " + ptt_test.getToken());
                                        Token objectToken=new Token();
                                        objectToken.setForm(ptt_test.getToken().toString());
                                        sentenceRelationId.setObject(objectToken);
                                        sentenceRelationId.setRelation(ptt_test.getTag().toString());
                                        sentenceRelationId.setSentence_text("blablabla");
                                        sentenceRelationId.setType(SentenceRelationType.hasComponent);
                                        sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                        sentenceRelation.setMethod(SentenceRelationMethod.dbpedia_chimical_component);
                                        list_result.add(sentenceRelation);
                                        List<PosTaggedToken> pos_tagged_token_test1 = parseConfiguration.getDependents(ptt_test);
                                        System.out.println(pos_tagged_token_test1);
                                        for (PosTaggedToken ptt_test1 : pos_tagged_token_test1) {
                                            if (ptt_test1.getTag().toString().equalsIgnoreCase("NC")) {
                                                System.out.println("hasComponent: " + ptt_test1.getToken());

                                                objectToken.setForm(ptt_test1.getToken().toString());
                                                sentenceRelationId.setObject(objectToken);
                                                sentenceRelationId.setRelation(ptt_test1.getTag().toString());
                                                sentenceRelationId.setSentence_text("blablabla");
                                                sentenceRelationId.setType(SentenceRelationType.hasComponent);
                                                sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                                sentenceRelation.setMethod(SentenceRelationMethod.dbpedia_chimical_component);
                                                list_result.add(sentenceRelation);
                                            }
                                            if (ptt_test1.getTag().toString().equalsIgnoreCase("NPP") && parseConfiguration.getGoverningDependency(ptt_test1).getLabel().equalsIgnoreCase("mod")&& !ptt_test1.getComment().equalsIgnoreCase("product")) {
                                                System.out.println("hasFragranceCreator: " + ptt_test.getToken() + " " + ptt_test1.getToken());

                                                objectToken.setForm(ptt_test1.getToken().toString());
                                                sentenceRelationId.setObject(objectToken);
                                                sentenceRelationId.setRelation(ptt_test1.getTag().toString());
                                                sentenceRelationId.setSentence_text("blablabla");
                                                sentenceRelationId.setType(SentenceRelationType.hasFragranceCreator);
                                                sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                                sentenceRelation.setMethod(SentenceRelationMethod.dbpedia_chimical_component);
                                                list_result.add(sentenceRelation);
                                            }
                                        }

                                    }
                                }
                            }
                        }

                        //construire
                        if (ptt.getTag().toString().equalsIgnoreCase("NC") && parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("mod")) {
                            List<PosTaggedToken> pos_tagged_token1 = parseConfiguration.getDependents(ptt);
                            for (PosTaggedToken ptt1 : pos_tagged_token1) {
                                if (ptt1.getTag().toString().equalsIgnoreCase("P+D") && (parseConfiguration.getGoverningDependency(ptt1).getLabel().equalsIgnoreCase("dep")
                                        || parseConfiguration.getGoverningDependency(ptt1).getLabel().equalsIgnoreCase("mod"))) {
                                    List<PosTaggedToken> pos_tagged_token2 = parseConfiguration.getDependents(ptt1);
                                    for (PosTaggedToken ptt2 : pos_tagged_token2) {
                                        if (ptt2.getTag().toString().equalsIgnoreCase("NC") && parseConfiguration.getGoverningDependency(ptt2).getLabel().equalsIgnoreCase("prep")) {
                                            System.out.println("hasComponent: " + ptt2.getToken());
                                            Token objectToken=new Token();
                                            objectToken.setForm(ptt2.getToken().toString());
                                            sentenceRelationId.setObject(objectToken);
                                            sentenceRelationId.setRelation(ptt2.getTag().toString());
                                            sentenceRelationId.setSentence_text("blablabla");
                                            sentenceRelationId.setType(SentenceRelationType.hasComponent);
                                            sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                            sentenceRelation.setMethod(SentenceRelationMethod.dbpedia_chimical_component);
                                            list_result.add(sentenceRelation);
                                        }
                                    }
                                }
                                if (ptt1.getTag().toString().equalsIgnoreCase("CC") && parseConfiguration.getGoverningDependency(ptt1).getLabel().equalsIgnoreCase("coord")) {
                                    List<PosTaggedToken> pos_tagged_token2 = parseConfiguration.getDependents(ptt1);
                                    for (PosTaggedToken ptt2 : pos_tagged_token2) {
                                        if (ptt2.getTag().toString().equalsIgnoreCase("P") && parseConfiguration.getGoverningDependency(ptt2).getLabel().equalsIgnoreCase("dep_coord")) {
                                            List<PosTaggedToken> pos_tagged_token3 = parseConfiguration.getDependents(ptt2);
                                            for (PosTaggedToken ptt3 : pos_tagged_token3) {
                                                if (ptt3.getTag().toString().equalsIgnoreCase("NC") && parseConfiguration.getGoverningDependency(ptt3).getLabel().equalsIgnoreCase("prep")) {
                                                    System.out.println("hasComponent: " + ptt3.getToken());
                                                    Token objectToken=new Token();
                                                    objectToken.setForm(ptt3.getToken().toString());
                                                    sentenceRelationId.setObject(objectToken);
                                                    sentenceRelationId.setRelation(ptt3.getTag().toString());
                                                    sentenceRelationId.setSentence_text("blablabla");
                                                    sentenceRelationId.setType(SentenceRelationType.hasComponent);
                                                    sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                                    sentenceRelation.setMethod(SentenceRelationMethod.dbpedia_chimical_component);
                                                    list_result.add(sentenceRelation);
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                        }

                        if (ptt.getTag().toString().equalsIgnoreCase("P+D") && parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("mod")) {
                            List<PosTaggedToken> pos_tagged_token1 = parseConfiguration.getDependents(ptt);
                            for (PosTaggedToken ptt1 : pos_tagged_token1) {

                                if (ptt1.getTag().toString().equalsIgnoreCase("NC") && parseConfiguration.getGoverningDependency(ptt1).getLabel().equalsIgnoreCase("prep")) {
                                    System.out.println("hasComponent: " + ptt1.getToken());
                                    Token objectToken=new Token();
                                    objectToken.setForm(ptt1.getToken().toString());
                                    sentenceRelationId.setObject(objectToken);
                                    sentenceRelationId.setRelation(ptt1.getTag().toString());
                                    sentenceRelationId.setSentence_text("blablabla");
                                    sentenceRelationId.setType(SentenceRelationType.hasComponent);
                                    sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                    sentenceRelation.setMethod(SentenceRelationMethod.dbpedia_chimical_component);
                                    list_result.add(sentenceRelation);
                                }

                                if (ptt1.getTag().toString().equalsIgnoreCase("CC") && parseConfiguration.getGoverningDependency(ptt1).getLabel().equalsIgnoreCase("coord")) {
                                    List<PosTaggedToken> pos_tagged_token2 = parseConfiguration.getDependents(ptt1);
                                    for (PosTaggedToken ptt2 : pos_tagged_token2) {
                                        if (ptt2.getTag().toString().equalsIgnoreCase("P") && parseConfiguration.getGoverningDependency(ptt2).getLabel().equalsIgnoreCase("dep_coord")) {
                                            List<PosTaggedToken> pos_tagged_token3 = parseConfiguration.getDependents(ptt2);
                                            for (PosTaggedToken ptt3 : pos_tagged_token3) {
                                                if (ptt3.getTag().toString().equalsIgnoreCase("NC") && parseConfiguration.getGoverningDependency(ptt3).getLabel().equalsIgnoreCase("prep")) {
                                                    System.out.println("hasComponent: " + ptt3.getToken());
                                                    Token objectToken=new Token();
                                                    objectToken.setForm(ptt3.getToken().toString());
                                                    sentenceRelationId.setObject(objectToken);
                                                    sentenceRelationId.setRelation(ptt3.getTag().toString());
                                                    sentenceRelationId.setSentence_text("blablabla");
                                                    sentenceRelationId.setType(SentenceRelationType.hasComponent);
                                                    sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                                    sentenceRelation.setMethod(SentenceRelationMethod.dbpedia_chimical_component);
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

                //bouquet //creation
                if (posTagSequence.get(i).getTag().toString().equalsIgnoreCase("NC") && parseConfiguration.getGoverningDependency(posTagSequence.get(i)).getLabel().equalsIgnoreCase("mod")) {

                    SentenceRelation sentenceRelation = new SentenceRelation();
                    SentenceRelationId sentenceRelationId = new SentenceRelationId();
                    List<PosTaggedToken> pos_tagged_token = parseConfiguration.getDependents(posTagSequence.get(i));
                    for (PosTaggedToken ptt : pos_tagged_token) {
                        if (ptt.getTag().toString().equalsIgnoreCase("P") && parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("dep")) {
                            List<PosTaggedToken> pos_tagged_token_test = parseConfiguration.getDependents(ptt);
                            System.out.println(pos_tagged_token_test);
                            if (!pos_tagged_token_test.isEmpty()) {
                                System.out.println(pos_tagged_token_test);
                                for (PosTaggedToken ptt_test : pos_tagged_token_test) {
                                    if (ptt_test.getTag().toString().equalsIgnoreCase("NC") && parseConfiguration.getGoverningDependency(ptt_test).getLabel().equalsIgnoreCase("prep")) {
                                        System.out.println("hasComponent: " + ptt_test.getToken());
                                        Token objectToken=new Token();
                                        objectToken.setForm(ptt_test.getToken().toString());
                                        sentenceRelationId.setObject(objectToken);
                                        sentenceRelationId.setRelation(ptt_test.getTag().toString());
                                        sentenceRelationId.setSentence_text("blablabla");
                                        sentenceRelationId.setType(SentenceRelationType.hasComponent);
                                        sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                        sentenceRelation.setMethod(SentenceRelationMethod.dbpedia_chimical_component);
                                        list_result.add(sentenceRelation);
                                    }
                                    if (ptt_test.getTag().toString().equalsIgnoreCase("CC") && parseConfiguration.getGoverningDependency(ptt_test).getLabel().equalsIgnoreCase("coord")) {
                                        List<PosTaggedToken> pos_tagged_token_test1 = parseConfiguration.getDependents(ptt_test);
                                        for (PosTaggedToken ptt_test1 : pos_tagged_token_test1) {
                                            if (ptt_test1.getTag().toString().equalsIgnoreCase("P") && parseConfiguration.getGoverningDependency(ptt_test1).getLabel().equalsIgnoreCase("dep_coord")) {
                                                List<PosTaggedToken> pos_tagged_token_test2 = parseConfiguration.getDependents(ptt_test1);
                                                for (PosTaggedToken ptt_test2 : pos_tagged_token_test2) {
                                                    if (ptt_test2.getTag().toString().equalsIgnoreCase("NC") && parseConfiguration.getGoverningDependency(ptt_test2).getLabel().equalsIgnoreCase("prep")) {
                                                        System.out.println("hasComponent: " + ptt_test2.getToken());
                                                        Token objectToken=new Token();
                                                        objectToken.setForm(ptt_test2.getToken().toString());
                                                        sentenceRelationId.setObject(objectToken);
                                                        sentenceRelationId.setRelation(ptt_test2.getTag().toString());
                                                        sentenceRelationId.setSentence_text("blablabla");
                                                        sentenceRelationId.setType(SentenceRelationType.hasComponent);
                                                        sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                                        sentenceRelation.setMethod(SentenceRelationMethod.dbpedia_chimical_component);
                                                        list_result.add(sentenceRelation);
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    if (ptt_test.getTag().toString().equalsIgnoreCase("NPP") && parseConfiguration.getGoverningDependency(ptt_test).getLabel().equalsIgnoreCase("prep")&& !ptt_test.getComment().equalsIgnoreCase("product")) {
                                        List<PosTaggedToken> pos_tagged_token_test1 = parseConfiguration.getDependents(ptt_test);
                                        for (PosTaggedToken ptt_test1 : pos_tagged_token_test1) {
                                            if (ptt_test1.getTag().toString().equalsIgnoreCase("CC") && parseConfiguration.getGoverningDependency(ptt_test1).getLabel().equalsIgnoreCase("coord")) {
                                                List<PosTaggedToken> pos_tagged_token_test2 = parseConfiguration.getDependents(ptt_test1);
                                                for (PosTaggedToken ptt_test2 : pos_tagged_token_test2) {
                                                    if (ptt_test2.getTag().toString().equalsIgnoreCase("NPP") && parseConfiguration.getGoverningDependency(ptt_test2).getLabel().equalsIgnoreCase("dep_coord")&& !ptt_test2.getComment().equalsIgnoreCase("product")) {
                                                        List<PosTaggedToken> pos_tagged_token_test3 = parseConfiguration.getDependents(ptt_test2);
                                                        for (PosTaggedToken ptt_test3 : pos_tagged_token_test3) {
                                                            if (ptt_test3.getTag().toString().equalsIgnoreCase("NPP") && parseConfiguration.getGoverningDependency(ptt_test3).getLabel().equalsIgnoreCase("mod")&& !ptt_test3.getComment().equalsIgnoreCase("product")) {

                                                                System.out.println("hasFragranceCreator: " + ptt_test2.getToken() + " " + ptt_test3.getToken());
                                                                Token objectToken=new Token();
                                                                objectToken.setForm(ptt_test3.getToken().toString());
                                                                sentenceRelationId.setObject(objectToken);
                                                                sentenceRelationId.setRelation(ptt_test3.getTag().toString());
                                                                sentenceRelationId.setSentence_text("blablabla");
                                                                sentenceRelationId.setType(SentenceRelationType.hasFragranceCreator);
                                                                sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                                                sentenceRelation.setMethod(SentenceRelationMethod.dbpedia_chimical_component);
                                                                list_result.add(sentenceRelation);
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                            if (ptt_test1.getTag().toString().equalsIgnoreCase("NPP") && parseConfiguration.getGoverningDependency(ptt_test1).getLabel().equalsIgnoreCase("mod")&& !ptt_test1.getComment().equalsIgnoreCase("product")) {
                                                System.out.println("hasFragranceCreator: " + ptt_test.getToken() + " " + ptt_test1.getToken());
                                                Token objectToken=new Token();
                                                objectToken.setForm(ptt_test1.getToken().toString());
                                                sentenceRelationId.setObject(objectToken);
                                                sentenceRelationId.setRelation(ptt_test1.getTag().toString());
                                                sentenceRelationId.setSentence_text("blablabla");
                                                sentenceRelationId.setType(SentenceRelationType.hasFragranceCreator);
                                                sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                                sentenceRelation.setMethod(SentenceRelationMethod.dbpedia_chimical_component);
                                                list_result.add(sentenceRelation);
                                            }


                                        }

                                    }
                                }
                            }
                        }
                    }
                }

                //contient // incarner // crée
                if (posTagSequence.get(i).getTag().toString().equalsIgnoreCase("V") && parseConfiguration.getGoverningDependency(posTagSequence.get(i)).getLabel().equalsIgnoreCase("root")) {

                    SentenceRelation sentenceRelation = new SentenceRelation();
                    SentenceRelationId sentenceRelationId = new SentenceRelationId();
                    //Liste des dependences de i
                    List<PosTaggedToken> pos_tagged_token = parseConfiguration.getDependents(posTagSequence.get(i));
                    System.out.println(pos_tagged_token);
                    for (PosTaggedToken ptt : pos_tagged_token) {
                        if (ptt.getTag().toString().equalsIgnoreCase("NPP") && parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("suj")/*&&ptt.getComment().equalsIgnoreCase("product")*/) {
                            System.out.println("product subject " + ptt.getToken());
                            Token subjectToken=new Token();
                            subjectToken.setForm(ptt.getToken().toString());
                            sentenceRelationId.setSubject(subjectToken);
                        }
                        if (ptt.getTag().toString().equalsIgnoreCase("V") && parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("mod")/*&&ptt.getComment().equalsIgnoreCase("product")*/) {
                            List<PosTaggedToken> pos_tagged_token_test = parseConfiguration.getDependents(ptt);
                            for (PosTaggedToken ptt_test : pos_tagged_token_test) {
                                if (ptt_test.getTag().toString().equalsIgnoreCase("NPP") && parseConfiguration.getGoverningDependency(ptt_test).getLabel().equalsIgnoreCase("suj")&&ptt.getComment().equalsIgnoreCase("product")) {
                                    System.out.println("product subject " + ptt_test.getToken());
                                    Token subjectToken = new Token();
                                    subjectToken.setForm(ptt_test.getToken().toString());
                                    sentenceRelationId.setSubject(subjectToken);
                                }
                            }

                        }
                        if (ptt.getTag().toString().equalsIgnoreCase("P") && parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("mod")) {
                            // liste de dependence
                            List<PosTaggedToken> pos_tagged_token_test = parseConfiguration.getDependents(ptt);
                            System.out.println(pos_tagged_token_test);
                            if (!pos_tagged_token_test.isEmpty()) {
                                System.out.println(pos_tagged_token_test);
                                for (PosTaggedToken ptt_test : pos_tagged_token_test) {
                                    if (ptt_test.getTag().toString().equalsIgnoreCase("NC") && parseConfiguration.getGoverningDependency(ptt_test).getLabel().equalsIgnoreCase("prep") ) {
                                        List<PosTaggedToken> pos_tagged_token_test1 = parseConfiguration.getDependents(ptt_test);
                                        System.out.println(pos_tagged_token_test1);
                                        for (PosTaggedToken ptt_test1 : pos_tagged_token_test1) {
                                            if (ptt_test1.getTag().toString().equalsIgnoreCase("NPP") && parseConfiguration.getGoverningDependency(ptt_test1).getLabel().equalsIgnoreCase("mod")&& !ptt_test1.getComment().equalsIgnoreCase("product")) {
                                                System.out.println("hasFragranceCreator: " + ptt_test.getToken() + " " + ptt_test1.getToken());
                                                Token objectToken=new Token();
                                                objectToken.setForm(ptt_test1.getToken().toString());
                                                sentenceRelationId.setObject(objectToken);
                                                sentenceRelationId.setRelation(ptt_test1.getTag().toString());
                                                sentenceRelationId.setSentence_text("blablabla");
                                                sentenceRelationId.setType(SentenceRelationType.hasFragranceCreator);
                                                sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                                sentenceRelation.setMethod(SentenceRelationMethod.dbpedia_chimical_component);
                                                list_result.add(sentenceRelation);
                                            }
                                        }

                                        for (PosTaggedToken ptt_test1 : pos_tagged_token_test1) {
                                            if (ptt_test1.getTag().toString().equalsIgnoreCase("NPP") && parseConfiguration.getGoverningDependency(ptt_test1).getLabel().equalsIgnoreCase("mod")&& ptt_test1.getComment().equalsIgnoreCase("product")) {
                                                System.out.println("productSubject: " + ptt_test1.getToken());
                                                Token subjectToken=new Token();
                                                subjectToken.setForm(ptt.getToken().toString());
                                                sentenceRelationId.setSubject(subjectToken);
                                            }
                                        }

                                    }
                                }
                            }
                        }
                        if (ptt.getTag().toString().equalsIgnoreCase("NPP") && parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("suj")&&!ptt.getComment().equalsIgnoreCase("product")) {
                            List<PosTaggedToken> pos_tagged_token1 = parseConfiguration.getDependents(ptt);
                            for (PosTaggedToken ptt1 : pos_tagged_token1) {
                                if (ptt1.getTag().toString().equalsIgnoreCase("NPP") && parseConfiguration.getGoverningDependency(ptt1).getLabel().equalsIgnoreCase("mod")&&!ptt1.getComment().equalsIgnoreCase("product")) {
                                    System.out.println("hasAmbassador: " + ptt.getToken() + " " + ptt1.getToken());
                                    Token objectToken=new Token();
                                    objectToken.setForm(ptt1.getToken().toString());
                                    sentenceRelationId.setObject(objectToken);
                                    sentenceRelationId.setRelation(ptt1.getTag().toString());
                                    sentenceRelationId.setSentence_text("blablabla");
                                    sentenceRelationId.setType(SentenceRelationType.hasAmbasador);
                                    sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                    sentenceRelation.setMethod(SentenceRelationMethod.dbpedia_chimical_component);
                                    list_result.add(sentenceRelation);
                                }
                            }
                        }
                       else if (parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("obj")&&ptt.getComment().equalsIgnoreCase("product")) {
                            System.out.println("product subject " + ptt.getToken());
                            Token subjectToken=new Token();
                            subjectToken.setForm(ptt.getToken().toString());
                            sentenceRelationId.setSubject(subjectToken);

                        }
                        //reposer
                        // verifier si la liste de dépendence conitient un token sont le pos est NPP et le lien entre i et la dependance est de type "obj"
                        if (ptt.getTag().toString().equalsIgnoreCase("P") && parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("p_obj")) {
                            List<PosTaggedToken> pos_tagged_token_test = parseConfiguration.getDependents(ptt);
                            System.out.println(pos_tagged_token_test);
                            if (!pos_tagged_token_test.isEmpty()) {
                                System.out.println(pos_tagged_token_test);
                                for (PosTaggedToken ptt_test : pos_tagged_token_test) {
                                    if (ptt_test.getTag().toString().equalsIgnoreCase("NC") && parseConfiguration.getGoverningDependency(ptt_test).getLabel().equalsIgnoreCase("prep")) {
                                        System.out.println("hasComponent: " + ptt_test.getToken());
                                        Token objectToken=new Token();
                                        objectToken.setForm(ptt_test.getToken().toString());
                                        sentenceRelationId.setObject(objectToken);
                                        sentenceRelationId.setRelation(ptt_test.getTag().toString());
                                        sentenceRelationId.setSentence_text("blablabla");
                                        sentenceRelationId.setType(SentenceRelationType.hasComponent);
                                        sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                        sentenceRelation.setMethod(SentenceRelationMethod.dbpedia_chimical_component);
                                        list_result.add(sentenceRelation);
                                    }
                                }
                            }
                        }

                        // verifier si la liste de dépendence conitient un token sont le pos est NPP et le lien entre i et la dependance est de type "obj"
                        if ((ptt.getTag().toString().equalsIgnoreCase("P") || ptt.getTag().toString().equalsIgnoreCase("P+D")) && parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("de_obj")) {
                            List<PosTaggedToken> pos_tagged_token_test = parseConfiguration.getDependents(ptt);
                            System.out.println(pos_tagged_token_test);
                            boolean is_annotated = false;
                            if (!pos_tagged_token_test.isEmpty()) {
                                System.out.println(pos_tagged_token_test);
                                for (PosTaggedToken ptt_test : pos_tagged_token_test) {
                                    if ((ptt_test.getTag().toString().equalsIgnoreCase("NPP") || ptt_test.getTag().toString().equalsIgnoreCase("NC")) && parseConfiguration.getGoverningDependency(ptt_test).getLabel().equalsIgnoreCase("prep")&& !ptt_test.getComment().equalsIgnoreCase("product")) {
                                        List<PosTaggedToken> pos_tagged_token_test_2 = parseConfiguration.getDependents(ptt_test);
                                        if (!pos_tagged_token_test_2.isEmpty()) {
                                            for (PosTaggedToken ptt_test2 : pos_tagged_token_test_2) {
                                                if ((ptt_test2.getTag().toString().equalsIgnoreCase("ADJ")) && parseConfiguration.getGoverningDependency(ptt_test2).getLabel().equalsIgnoreCase("mod")) {
                                                    System.out.println("hasComponent: " + ptt_test.getToken() + " " + ptt_test2.getToken());
                                                    Token objectToken=new Token();
                                                    objectToken.setForm(ptt_test2.getToken().toString());
                                                    sentenceRelationId.setObject(objectToken);
                                                    sentenceRelationId.setRelation(ptt_test2.getTag().toString());
                                                    sentenceRelationId.setSentence_text("blablabla");
                                                    sentenceRelationId.setType(SentenceRelationType.hasComponent);
                                                    sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                                    sentenceRelation.setMethod(SentenceRelationMethod.dbpedia_chimical_component);
                                                    list_result.add(sentenceRelation);
                                                    is_annotated = true;
                                                }
                                            }
                                            if (!is_annotated) {
                                                System.out.println("hasComponent: " + ptt_test.getToken());
                                                Token objectToken=new Token();
                                                objectToken.setForm(ptt_test.getToken().toString());
                                                sentenceRelationId.setObject(objectToken);
                                                sentenceRelationId.setRelation(ptt_test.getTag().toString());
                                                sentenceRelationId.setSentence_text("blablabla");
                                                sentenceRelationId.setType(SentenceRelationType.hasComponent);
                                                sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                                sentenceRelation.setMethod(SentenceRelationMethod.dbpedia_chimical_component);
                                                list_result.add(sentenceRelation);
                                            }
                                        } else {
                                            System.out.println("hasComponent: " + ptt_test.getToken());
                                            Token objectToken=new Token();
                                            objectToken.setForm(ptt_test.getToken().toString());
                                            sentenceRelationId.setObject(objectToken);
                                            sentenceRelationId.setRelation(ptt_test.getTag().toString());
                                            sentenceRelationId.setSentence_text("blablabla");
                                            sentenceRelationId.setType(SentenceRelationType.hasComponent);
                                            sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                            sentenceRelation.setMethod(SentenceRelationMethod.dbpedia_chimical_component);
                                            list_result.add(sentenceRelation);
                                        }

                                    }
                                    if (ptt_test.getTag().toString().equalsIgnoreCase("CC") && parseConfiguration.getGoverningDependency(ptt_test).getLabel().equalsIgnoreCase("coord")) {
                                        List<PosTaggedToken> pos_tagged_token_test1 = parseConfiguration.getDependents(ptt_test);
                                        for (PosTaggedToken ptt_test1 : pos_tagged_token_test1) {
                                            if (ptt_test1.getTag().toString().equalsIgnoreCase("P+D") && parseConfiguration.getGoverningDependency(ptt_test1).getLabel().equalsIgnoreCase("dep_coord")) {
                                                List<PosTaggedToken> pos_tagged_token_test2 = parseConfiguration.getDependents(ptt_test1);
                                                for (PosTaggedToken ptt_test2 : pos_tagged_token_test2) {
                                                    if (ptt_test2.getTag().toString().equalsIgnoreCase("NC") && parseConfiguration.getGoverningDependency(ptt_test2).getLabel().equalsIgnoreCase("prep")) {
                                                        List<PosTaggedToken> pos_tagged_token_test_3 = parseConfiguration.getDependents(ptt_test2);
                                                        if (!pos_tagged_token_test_3.isEmpty()) {
                                                            for (PosTaggedToken ptt_test3 : pos_tagged_token_test_3) {
                                                                if ((ptt_test3.getTag().toString().equalsIgnoreCase("ADJ")) && parseConfiguration.getGoverningDependency(ptt_test3).getLabel().equalsIgnoreCase("mod")) {
                                                                    System.out.println("hasComponent: " + ptt_test2.getToken() + " " + ptt_test3.getToken());
                                                                    Token objectToken=new Token();
                                                                    objectToken.setForm(ptt_test2.getToken().toString());
                                                                    sentenceRelationId.setObject(objectToken);
                                                                    sentenceRelationId.setRelation(ptt_test2.getTag().toString());
                                                                    sentenceRelationId.setSentence_text("blablabla");
                                                                    sentenceRelationId.setType(SentenceRelationType.hasComponent);
                                                                    sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                                                    sentenceRelation.setMethod(SentenceRelationMethod.dbpedia_chimical_component);
                                                                    list_result.add(sentenceRelation);
                                                                    is_annotated = true;
                                                                }
                                                            }
                                                            if (!is_annotated) {
                                                                System.out.println("hasComponent: " + ptt_test2.getToken());
                                                                Token objectToken=new Token();
                                                                objectToken.setForm(ptt_test2.getToken().toString());
                                                                sentenceRelationId.setObject(objectToken);
                                                                sentenceRelationId.setRelation(ptt_test2.getTag().toString());
                                                                sentenceRelationId.setSentence_text("blablabla");
                                                                sentenceRelationId.setType(SentenceRelationType.hasComponent);
                                                                sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                                                sentenceRelation.setMethod(SentenceRelationMethod.dbpedia_chimical_component);
                                                                list_result.add(sentenceRelation);
                                                            }
                                                        } else {
                                                            System.out.println("hasComponent: " + ptt_test2.getToken());
                                                            Token objectToken=new Token();
                                                            objectToken.setForm(ptt_test2.getToken().toString());
                                                            sentenceRelationId.setObject(objectToken);
                                                            sentenceRelationId.setRelation(ptt_test2.getTag().toString());
                                                            sentenceRelationId.setSentence_text("blablabla");
                                                            sentenceRelationId.setType(SentenceRelationType.hasComponent);
                                                            sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                                            sentenceRelation.setMethod(SentenceRelationMethod.dbpedia_chimical_component);
                                                            list_result.add(sentenceRelation);
                                                        }

                                                    }
                                                }
                                            }

                                            if (ptt_test1.getTag().toString().equalsIgnoreCase("P") && parseConfiguration.getGoverningDependency(ptt_test1).getLabel().equalsIgnoreCase("dep_coord")) {
                                                List<PosTaggedToken> pos_tagged_token_test2 = parseConfiguration.getDependents(ptt_test1);
                                                for (PosTaggedToken ptt_test2 : pos_tagged_token_test2) {
                                                    if (ptt_test2.getTag().toString().equalsIgnoreCase("NC") && parseConfiguration.getGoverningDependency(ptt_test2).getLabel().equalsIgnoreCase("prep")) {
                                                        List<PosTaggedToken> pos_tagged_token_test_3 = parseConfiguration.getDependents(ptt_test2);
                                                        if (!pos_tagged_token_test_3.isEmpty()) {
                                                            for (PosTaggedToken ptt_test3 : pos_tagged_token_test_3) {
                                                                if ((ptt_test3.getTag().toString().equalsIgnoreCase("ADJ")) && parseConfiguration.getGoverningDependency(ptt_test3).getLabel().equalsIgnoreCase("mod")) {
                                                                    System.out.println("hasComponent: " + ptt_test2.getToken() + " " + ptt_test3.getToken());
                                                                    Token objectToken=new Token();
                                                                    objectToken.setForm(ptt_test2.getToken().toString());
                                                                    sentenceRelationId.setObject(objectToken);
                                                                    sentenceRelationId.setRelation(ptt_test2.getTag().toString());
                                                                    sentenceRelationId.setSentence_text("blablabla");
                                                                    sentenceRelationId.setType(SentenceRelationType.hasComponent);
                                                                    sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                                                    sentenceRelation.setMethod(SentenceRelationMethod.dbpedia_chimical_component);
                                                                    list_result.add(sentenceRelation);
                                                                    is_annotated = true;
                                                                }
                                                            }
                                                            if (!is_annotated) {
                                                                System.out.println("hasComponent: " + ptt_test2.getToken());
                                                                Token objectToken=new Token();
                                                                objectToken.setForm(ptt_test2.getToken().toString());
                                                                sentenceRelationId.setObject(objectToken);
                                                                sentenceRelationId.setRelation(ptt_test2.getTag().toString());
                                                                sentenceRelationId.setSentence_text("blablabla");
                                                                sentenceRelationId.setType(SentenceRelationType.hasComponent);
                                                                sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                                                sentenceRelation.setMethod(SentenceRelationMethod.dbpedia_chimical_component);
                                                                list_result.add(sentenceRelation);
                                                            }
                                                        } else {
                                                            System.out.println("hasComponent: " + ptt_test2.getToken());
                                                            Token objectToken=new Token();
                                                            objectToken.setForm(ptt_test2.getToken().toString());
                                                            sentenceRelationId.setObject(objectToken);
                                                            sentenceRelationId.setRelation(ptt_test2.getTag().toString());
                                                            sentenceRelationId.setSentence_text("blablabla");
                                                            sentenceRelationId.setType(SentenceRelationType.hasComponent);
                                                            sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                                            sentenceRelation.setMethod(SentenceRelationMethod.dbpedia_chimical_component);
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


                if (posTagSequence.get(i).getTag().toString().equalsIgnoreCase("VPP") && parseConfiguration.getGoverningDependency(posTagSequence.get(i)).getLabel().equalsIgnoreCase("root")) {

                    SentenceRelation sentenceRelation = new SentenceRelation();
                    SentenceRelationId sentenceRelationId = new SentenceRelationId();
                    //Liste des dependences de i
                    List<PosTaggedToken> pos_tagged_token = parseConfiguration.getDependents(posTagSequence.get(i));
                    System.out.println(pos_tagged_token);
                    for (PosTaggedToken ptt : pos_tagged_token) {
                        // verifier si la liste de dépendence conitient un token sont le pos est NPP et le lien entre i et la dependance est de type "obj"
                        if (ptt.getTag().toString().equalsIgnoreCase("P")) {
                            List<PosTaggedToken> pos_tagged_token_test = parseConfiguration.getDependents(ptt);
                            System.out.println(pos_tagged_token_test);
                            for (PosTaggedToken ptt_test : pos_tagged_token_test) {
                                if (ptt_test.getTag().toString().equalsIgnoreCase("NPP") && parseConfiguration.getGoverningDependency(ptt_test).getLabel().equalsIgnoreCase("prep")&& ptt_test.getComment().equalsIgnoreCase("product")) {
                                    System.out.println("subject relation: " + ptt_test.getToken());
                                    Token subjectToken=new Token();
                                    subjectToken.setForm(ptt.getToken().toString());
                                    sentenceRelationId.setSubject(subjectToken);
                                }
                            }
                        }
                        if (ptt.getTag().toString().equalsIgnoreCase("CC") && parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("coord")) {
                            List<PosTaggedToken> pos_tagged_token_test = parseConfiguration.getDependents(ptt);
                            for (PosTaggedToken ptt_test : pos_tagged_token_test) {
                                if (ptt_test.getTag().toString().equalsIgnoreCase("NC") && parseConfiguration.getGoverningDependency(ptt_test).getLabel().equalsIgnoreCase("dep_coord")) {
                                    System.out.println("hasComponent: " + ptt_test.getToken());
                                    Token objectToken=new Token();
                                    objectToken.setForm(ptt_test.getToken().toString());
                                    sentenceRelationId.setObject(objectToken);
                                    sentenceRelationId.setRelation(ptt_test.getTag().toString());
                                    sentenceRelationId.setSentence_text("blablabla");
                                    sentenceRelationId.setType(SentenceRelationType.hasComponent);
                                    sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                    sentenceRelation.setMethod(SentenceRelationMethod.dbpedia_chimical_component);
                                    list_result.add(sentenceRelation);
                                }
                            }
                        }
                        if (ptt.getTag().toString().equalsIgnoreCase("P+D") && parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("de_obj")) {
                            List<PosTaggedToken> pos_tagged_token_test = parseConfiguration.getDependents(ptt);
                            for (PosTaggedToken ptt_test : pos_tagged_token_test) {
                                if (ptt_test.getTag().toString().equalsIgnoreCase("NC") && parseConfiguration.getGoverningDependency(ptt_test).getLabel().equalsIgnoreCase("prep")) {
                                    List<PosTaggedToken> pos_tagged_token_test1 = parseConfiguration.getDependents(ptt_test);
                                    for (PosTaggedToken ptt_test1 : pos_tagged_token_test1) {
                                        if (ptt_test1.getTag().toString().equalsIgnoreCase("P") && parseConfiguration.getGoverningDependency(ptt_test1).getLabel().equalsIgnoreCase("dep")) {
                                            System.out.println("hasComponent: " + ptt_test1.getToken());
                                            Token objectToken=new Token();
                                            objectToken.setForm(ptt_test1.getToken().toString());
                                            sentenceRelationId.setObject(objectToken);
                                            sentenceRelationId.setRelation(ptt_test1.getTag().toString());
                                            sentenceRelationId.setSentence_text("blablabla");
                                            sentenceRelationId.setType(SentenceRelationType.hasComponent);
                                            sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                            sentenceRelation.setMethod(SentenceRelationMethod.dbpedia_chimical_component);
                                            list_result.add(sentenceRelation);
                                            List<PosTaggedToken> pos_tagged_token_test2 = parseConfiguration.getDependents(ptt_test1);
                                            for (PosTaggedToken ptt_test2 : pos_tagged_token_test2) {
                                                if (ptt_test2.getTag().toString().equalsIgnoreCase("NC") && parseConfiguration.getGoverningDependency(ptt_test2).getLabel().equalsIgnoreCase("prep")) {
                                                    List<PosTaggedToken> pos_tagged_token_test3 = parseConfiguration.getDependents(ptt_test2);
                                                    for (PosTaggedToken ptt_test3 : pos_tagged_token_test3) {
                                                        if (ptt_test3.getTag().toString().equalsIgnoreCase("ADJ") && parseConfiguration.getGoverningDependency(ptt_test3).getLabel().equalsIgnoreCase("mod")) {
                                                            System.out.println("hasComponent: " + ptt_test2.getToken() + " " + ptt_test3.getToken());

                                                            objectToken.setForm(ptt_test2.getToken().toString());
                                                            sentenceRelationId.setObject(objectToken);
                                                            sentenceRelationId.setRelation(ptt_test2.getTag().toString());
                                                            sentenceRelationId.setSentence_text("blablabla");
                                                            sentenceRelationId.setType(SentenceRelationType.hasComponent);
                                                            sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                                            sentenceRelation.setMethod(SentenceRelationMethod.dbpedia_chimical_component);
                                                            list_result.add(sentenceRelation);
                                                        }
                                                        if (ptt_test3.getTag().toString().equalsIgnoreCase("CC") && parseConfiguration.getGoverningDependency(ptt_test3).getLabel().equalsIgnoreCase("coord")) {
                                                            List<PosTaggedToken> pos_tagged_token_test4 = parseConfiguration.getDependents(ptt_test3);
                                                            for (PosTaggedToken ptt_test4 : pos_tagged_token_test4) {
                                                                if (ptt_test4.getTag().toString().equalsIgnoreCase("NC") && parseConfiguration.getGoverningDependency(ptt_test4).getLabel().equalsIgnoreCase("dep_coord")) {
                                                                    List<PosTaggedToken> pos_tagged_token_test5 = parseConfiguration.getDependents(ptt_test4);
                                                                    for (PosTaggedToken ptt_test5 : pos_tagged_token_test5) {
                                                                        if (ptt_test5.getTag().toString().equalsIgnoreCase("P") && parseConfiguration.getGoverningDependency(ptt_test5).getLabel().equalsIgnoreCase("dep")) {
                                                                            List<PosTaggedToken> pos_tagged_token_test6 = parseConfiguration.getDependents(ptt_test5);
                                                                            for (PosTaggedToken ptt_test6 : pos_tagged_token_test6) {
                                                                                if (ptt_test6.getTag().toString().equalsIgnoreCase("NPP") && parseConfiguration.getGoverningDependency(ptt_test6).getLabel().equalsIgnoreCase("Prep")) {
                                                                                    List<PosTaggedToken> pos_tagged_token_test7 = parseConfiguration.getDependents(ptt_test6);
                                                                                    for (PosTaggedToken ptt_test7 : pos_tagged_token_test7) {
                                                                                        if (ptt_test7.getTag().toString().equalsIgnoreCase("NPP") && parseConfiguration.getGoverningDependency(ptt_test7).getLabel().equalsIgnoreCase("mod")&& !ptt_test7.getComment().equalsIgnoreCase("product")) {
                                                                                            System.out.println("hasComponent: " + ptt_test6.getToken() + " " + ptt_test7.getToken());

                                                                                            objectToken.setForm(ptt_test6.getToken().toString());
                                                                                            sentenceRelationId.setObject(objectToken);
                                                                                            sentenceRelationId.setRelation(ptt_test6.getTag().toString());
                                                                                            sentenceRelationId.setSentence_text("blablabla");
                                                                                            sentenceRelationId.setType(SentenceRelationType.hasComponent);
                                                                                            sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                                                                            sentenceRelation.setMethod(SentenceRelationMethod.dbpedia_chimical_component);
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

                //associant
                if (posTagSequence.get(i).getTag().toString().equalsIgnoreCase("VPR") && parseConfiguration.getGoverningDependency(posTagSequence.get(i)).getLabel().equalsIgnoreCase("mod")) {
                    //Liste des dependences de i
                    List<PosTaggedToken> pos_tagged_token = parseConfiguration.getDependents(posTagSequence.get(i));
                    System.out.println(pos_tagged_token);

                    SentenceRelation sentenceRelation = new SentenceRelation();
                    SentenceRelationId sentenceRelationId = new SentenceRelationId();
                    for (PosTaggedToken ptt : pos_tagged_token) {
                        // verifier si la liste de dépendence conitient un token sont le pos est NPP et le lien entre i et la dependance est de type "obj"
                        if (ptt.getTag().toString().equalsIgnoreCase("NPP") && parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("obj")&& !ptt.getComment().equalsIgnoreCase("product")) {
                            System.out.println("hasComponent: " + ptt.getToken());
                            Token objectToken=new Token();
                            objectToken.setForm(ptt.getToken().toString());
                            sentenceRelationId.setObject(objectToken);
                            sentenceRelationId.setRelation(ptt.getTag().toString());
                            sentenceRelationId.setSentence_text("blablabla");
                            sentenceRelationId.setType(SentenceRelationType.hasComponent);
                            sentenceRelation.setSentenceRelationId(sentenceRelationId);
                            sentenceRelation.setMethod(SentenceRelationMethod.dbpedia_chimical_component);
                            list_result.add(sentenceRelation);
                            // liste de dependence
                            List<PosTaggedToken> pos_tagged_token_test = parseConfiguration.getDependents(ptt);
                            if (!pos_tagged_token_test.isEmpty()) {
                                System.out.println(pos_tagged_token_test);
                                for (PosTaggedToken ptt_test : pos_tagged_token_test) {
                                    if (ptt_test.getTag().toString().equalsIgnoreCase("CC")) {
                                        List<PosTaggedToken> pos_tagged_token_test1 = parseConfiguration.getDependents(ptt_test);
                                        for (PosTaggedToken ptt_test1 : pos_tagged_token_test1) {
                                            System.out.println("hasComponent: " + ptt_test1.getToken());

                                            objectToken.setForm(ptt_test1.getToken().toString());
                                            sentenceRelationId.setObject(objectToken);
                                            sentenceRelationId.setRelation(ptt_test1.getTag().toString());
                                            sentenceRelationId.setSentence_text("blablabla");
                                            sentenceRelationId.setType(SentenceRelationType.hasComponent);
                                            sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                            sentenceRelation.setMethod(SentenceRelationMethod.dbpedia_chimical_component);
                                            list_result.add(sentenceRelation);
                                        }
                                    } else if (ptt_test.getTag().toString().equalsIgnoreCase("NPP")&& !ptt_test.getComment().equalsIgnoreCase("product")) {
                                        System.out.println("hasComponent: " + ptt_test.getToken());

                                        objectToken.setForm(ptt_test.getToken().toString());
                                        sentenceRelationId.setObject(objectToken);
                                        sentenceRelationId.setRelation(ptt_test.getTag().toString());
                                        sentenceRelationId.setSentence_text("blablabla");
                                        sentenceRelationId.setType(SentenceRelationType.hasComponent);
                                        sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                        sentenceRelation.setMethod(SentenceRelationMethod.dbpedia_chimical_component);
                                        list_result.add(sentenceRelation);
                                        List<PosTaggedToken> pos_tagged_token_test1 = parseConfiguration.getDependents(ptt_test);
                                        if (!pos_tagged_token_test1.isEmpty()) {
                                            for (PosTaggedToken ptt_test1 : pos_tagged_token_test1) {
                                                if (ptt_test1.getTag().toString().equalsIgnoreCase("CC")) {
                                                    List<PosTaggedToken> pos_tagged_token_test2 = parseConfiguration.getDependents(ptt_test1);
                                                    for (PosTaggedToken ptt_test2 : pos_tagged_token_test2) {
                                                        System.out.println("hasComponent: " + ptt_test2.getToken());

                                                        objectToken.setForm(ptt_test2.getToken().toString());
                                                        sentenceRelationId.setObject(objectToken);
                                                        sentenceRelationId.setRelation(ptt_test2.getTag().toString());
                                                        sentenceRelationId.setSentence_text("blablabla");
                                                        sentenceRelationId.setType(SentenceRelationType.hasComponent);
                                                        sentenceRelation.setSentenceRelationId(sentenceRelationId);
                                                        sentenceRelation.setMethod(SentenceRelationMethod.dbpedia_chimical_component);
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

    public void constructRelation(){
       /* SentenceRelation sentenceRelation = new SentenceRelation();
        SentenceRelationId sentenceRelationId = new SentenceRelationId();
        Token objectToken=new Token();
        Token subjectToken=new Token();
        subjectToken.setForm("subject");
        objectToken.setForm("test");
        sentenceRelationId.setSubject(subjectToken);
        sentenceRelationId.setObject(objectToken);
        sentenceRelationId.setRelation("relation");
        sentenceRelationId.setSentence_text("blablabla");
        sentenceRelationId.setType(SentenceRelationType.hasComponent);
        sentenceRelation.setSentenceRelationId(sentenceRelationId);
        sentenceRelation.setMethod(SentenceRelationMethod.dbpedia_chimical_component);
        list_result.add(sentenceRelation);*/

    }

    @Override
    public void annotationData(List<SentenceRelation> list_result) throws IOException {

     /*  Map<SentenceRelationId, List<SentenceRelationMethod>> relationMap = new HashMap<>();
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
        Renco renco = new Renco();
        startTools(line, renco.rencoByWebService(line));
    }

    @Override
    public void init() throws Exception {

    }
}
