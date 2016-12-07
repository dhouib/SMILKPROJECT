package fr.inria.smilk.ws.relationextraction.tools_test;


import com.hp.hpl.jena.rdf.model.Model;
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
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static fr.inria.smilk.ws.relationextraction.ExtractionHelper.*;

/**
 * Created by dhouib on 10/10/2016.
 */
public class BonsaiRules extends AbstractRelationExtraction {
    static HashMap<Integer, TalismaneToken> id_token = new HashMap<Integer, TalismaneToken>();


    private void startTools(String line, String input) throws ParserConfigurationException, IOException, SAXException, InvalidBabelSynsetIDException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        StringBuilder xmlStringBuilder = new StringBuilder();
        xmlStringBuilder.append(input);
        System.out.println(input);
        ByteArrayInputStream in = new ByteArrayInputStream(xmlStringBuilder.toString().getBytes("UTF-8"));
        Document doc = builder.parse(in);
        doc.getDocumentElement().normalize();
        NodeList nSentenceList = doc.getElementsByTagName("sentence");
        annotatedByTalismane(line, nSentenceList);
    }


    public void annotatedByTalismane(String line, NodeList nSentenceList) throws IOException {
        BufferedReader br = null;
        String sCurrentLine;
        br = new BufferedReader(new FileReader("C:/Users/dhouib/Desktop/SMILK_project_devpt/RelationExtractionSMILK/src/resources/input/bonsai/test.txt"));
        while ((sCurrentLine = br.readLine()) != null) {
            if (!(sCurrentLine.isEmpty())) {
                //Do stuff
                String values[] = sCurrentLine.trim().split("\\t");
                System.out.println(sCurrentLine);
                //System.out.println("id: " + values[0] + " form: " + values[1] + " lemme: " + values[2] + " pos: " + values[3] + " pos2: " + values[4] + " genre: " + values[5] + " antec: " + values[6] + " edge: " + values[7]);
                TalismaneToken talisman_token = new TalismaneToken();
                talisman_token.setId(Integer.parseInt(values[0]));
                talisman_token.setForm(values[1]);
                talisman_token.setLema(values[2]);
                talisman_token.setPos(values[4]);
                talisman_token.setAntecedent(values[6]);
                talisman_token.setEdge_type(values[7]);
                //System.out.println(talisman_token.getId() + " " + talisman_token.getForm() + " " + talisman_token.getLema() + " " + talisman_token.getAntecedent() + " " + talisman_token.getEdge_type());
                id_token.put(talisman_token.getId(), talisman_token);
            }
        }

        HashMap<TalismaneToken, List<Integer> > previous=  search_previous(id_token);

        for (Map.Entry<TalismaneToken, List<Integer> > e : previous.entrySet()) {
            TalismaneToken key = e.getKey();
            List<Integer> previousToken = e.getValue();
            //  key.setPrevious(previousToken);
            for (int i : previousToken) {
                TalismaneToken currentTalismaneToken = id_token.get(i);
                key.getPrevious().add(currentTalismaneToken);
            }
            id_token.get(key.getId()).setPrevious(key.getPrevious());
        }


        for (Map.Entry<TalismaneToken, List<Integer> > e : previous.entrySet()) {
            TalismaneToken key = e.getKey();

            if(key.getEdge_type().equalsIgnoreCase("mod")) {
                for (TalismaneToken i : key.getPrevious()) {
                    if (i.getEdge_type().equalsIgnoreCase("dep_coord") || i.getEdge_type().equalsIgnoreCase("obj")) {
                        for (TalismaneToken j : i.getPrevious()) {
                            //   System.out.println(i.getForm());
                            if (j.getLema().equalsIgnoreCase("composer") || j.getLema().equalsIgnoreCase("créer") || j.getLema().equalsIgnoreCase("création")) {
                                System.out.println("key: " + key.getForm() + " antec: " + j.getForm());
                                String object = key.getForm().concat(" " + i.getForm());
                                extractRelations(line, object, nSentenceList, "creator");
                            }
                        }
                    }
                }
            }

            if(key.getEdge_type().equalsIgnoreCase("mod") ){
              //  System.out.println("key: "+key.getForm()+ " "+ key.getPrevious());
               for (TalismaneToken i:key.getPrevious()){
                  /*   if(i.getEdge_type().equalsIgnoreCase("dep_coord") || i.getEdge_type().equalsIgnoreCase("suj") ||i.getEdge_type().equalsIgnoreCase("mod") && i.getPos().equalsIgnoreCase("NPP")){
                        for(TalismaneToken j:i.getPrevious()){
                            //   System.out.println(i.getForm());
                            if(j.getLema().equalsIgnoreCase("composer") || j.getLema().equalsIgnoreCase("créer") || j.getLema().equalsIgnoreCase("création")||j.getLema().equalsIgnoreCase("orchestrer")){
                                System.out.println("key: " + key.getForm() + " antec: " + j.getForm());
                                String object=key.getForm().concat(" "+i.getForm());
                                extractRelations(line, object, nSentenceList, "creator");
                            }

                            else  if(j.getLema().equalsIgnoreCase("incarner") ||j.getForm().equalsIgnoreCase("incerna")){
                                System.out.println("key: " + key.getForm() + " antec: " + j.getForm());
                                String object=key.getForm().concat(" "+i.getForm());
                                extractRelations(line, object, nSentenceList, "ambasador");
                            }
                        }
                    }
                   else  if(i.getEdge_type().equalsIgnoreCase("prep")){
                      //  System.out.println("key: "+ key.getForm()+" antec: "+i.getForm()+ i.getLema()+ i.getPrevious()+i.getEdge_type());
                       // i.getPrevious();
                        for(TalismaneToken j:i.getPrevious()){
                         //   System.out.println(i.getForm());
                            if(j.getLema().equalsIgnoreCase("composer") || j.getLema().equalsIgnoreCase("créer") || j.getLema().equalsIgnoreCase("création")){
                                System.out.println("key: " + key.getForm() + " antec: " + j.getForm());
                                String object=key.getForm().concat(" "+i.getForm());
                                extractRelations(line, object, nSentenceList, "creator");
                            }
                        }
                    }*/

                      if(i.getEdge_type().equalsIgnoreCase("suj")){
                        //  System.out.println("key: "+ key.getForm()+" antec: "+i.getForm()+ i.getLema()+ i.getPrevious()+i.getEdge_type());
                        // i.getPrevious();
                        for(TalismaneToken j:i.getPrevious()){
                            //   System.out.println(i.getForm());
                            if(j.getLema().equalsIgnoreCase("incarner")){
                                System.out.println("key: " + key.getForm() + " antec: " + j.getForm());
                                String object=key.getForm().concat(" "+i.getForm());
                                extractRelations(line, object, nSentenceList, "ambasador");
                            }
                        }
                    }
                }
            }


           if (key.getEdge_type().equalsIgnoreCase("obj")/*&&key.getPos().equalsIgnoreCase("NC")*/) {
               System.out.println("key: "+key.getForm());
                for (TalismaneToken i : key.getPrevious()) {
                    System.out.println(i.getLema());
                    if ((i.getLema().equalsIgnoreCase("bouquet")) || i.getLema().equalsIgnoreCase("construit") || i.getLema().equalsIgnoreCase("coeur") || i.getLema().equalsIgnoreCase("rafraîchir")
                          || i.getLema().equalsIgnoreCase("contenir")  || i.getLema().equalsIgnoreCase("extrait" )
                           || i.getLema().equalsIgnoreCase("élaborer")) {
                         System.out.println("finalkey: " + key.getForm() + " previous: " + i.getId() + " " + i.getForm());
                        extractRelations(line, key.getForm(), nSentenceList, "component");
                    }
                }
            }

            if (key.getEdge_type().equalsIgnoreCase("dep") &&key.getPos().equalsIgnoreCase("ET")) {
                System.out.println("key: " + key.getForm());
                for (TalismaneToken i : key.getPrevious()) {
                    System.out.println(i.getLema());
                    if ((i.getEdge_type().equalsIgnoreCase("obj"))) {
                        for (TalismaneToken j : i.getPrevious()) {
                            //   System.out.println(i.getForm());
                            if (j.getLema().equalsIgnoreCase("reposer")) {
                                System.out.println("key: " + key.getForm() + " antec: " + j.getForm());
                                String object = i.getForm().concat(" " + key.getForm());
                                extractRelations(line, object, nSentenceList, "creator");
                            }
                        }
                    }

                }
            }

            if ((key.getEdge_type().equalsIgnoreCase("obj")||key.getEdge_type().equalsIgnoreCase("dep_coord"))&&key.getPos().equalsIgnoreCase("NC")) {
                System.out.println("key: "+key.getForm());
                for (TalismaneToken i : key.getPrevious()) {
                    System.out.println(i.getLema());
                    if ((i.getLema().equalsIgnoreCase("associer")) ||i.getLema().equalsIgnoreCase("extrait" )) {
                        System.out.println("finalkey: " + key.getForm() + " previous: " + i.getId() + " " + i.getForm());
                        extractRelations(line, key.getForm(), nSentenceList, "component");
                    }
                }
            }

            if (key.getEdge_type().equalsIgnoreCase("mod")) {
                System.out.println("key: " + key.getForm());
                for (TalismaneToken i : key.getPrevious()) {
                    System.out.println(i.getLema());
                    if ((i.getLema().equalsIgnoreCase("riche"))) {
                        System.out.println("finalkey: " + key.getForm() + " previous: " + i.getId() + " " + i.getForm());
                        extractRelations(line, key.getForm(), nSentenceList, "component");
                    } else if (i.getEdge_type().equalsIgnoreCase("obj")) {
                        for (TalismaneToken j : key.getPrevious()) {
                            System.out.println(i.getLema());
                            if ((j.getLema().equalsIgnoreCase("association"))) {
                                System.out.println("finalkey: " + key.getForm() + " previous: " + i.getId() + " " + i.getForm());
                                String object = i.getForm().concat(" " + key.getForm());
                                extractRelations(line, object, nSentenceList, "component");
                            }
                        }
                    }
                }
            }

            if ((key.getEdge_type().equalsIgnoreCase("obj")||key.getEdge_type().equalsIgnoreCase("coord") || key.getEdge_type().equalsIgnoreCase("dep_coord"))&&key.getPos().equalsIgnoreCase("NC")/*|| key.getEdge_type().equalsIgnoreCase()*/) {
                System.out.println("key: "+key.getForm());
                for (TalismaneToken i : key.getPrevious()) {
                    System.out.println(i.getLema());
                    if ((i.getLema().equalsIgnoreCase("orchestrer"))  ) {
                        System.out.println("finalkey: " + key.getForm() + " previous: " + i.getId() + " " + i.getForm());
                        extractRelations(line, key.getForm(), nSentenceList, "component");
                    }
                }
            }
        }
    }
    public static HashMap<TalismaneToken, List<Integer> >  search_previous(HashMap<Integer, TalismaneToken>  id_token){
        HashMap<TalismaneToken, List<Integer> >  token_prevous_map = new HashMap<TalismaneToken, List<Integer> >();
        for (Map.Entry<Integer, TalismaneToken> e : id_token.entrySet()) {
            Integer key = e.getKey();
            TalismaneToken previousToken = e.getValue();
            List<Integer> antec = new ArrayList<>();

            int previous = Integer.parseInt(previousToken.getAntecedent());
            System.out.println(previousToken.getForm());
            while (previous != 0) {
                System.out.println("prev: " + previous);
                antec.add(previous);
                previous = Integer.parseInt(id_token.get(previous).getAntecedent());
                token_prevous_map.put(previousToken,antec);

            }
            System.out.println("Antecedent de " + previousToken.getForm()/*;e.getKey() */+ ": " + antec);
        }
        return token_prevous_map;
    }


    private void extractRelations(String line,  String object, NodeList nSentenceList, String type_relation){
        List<Token> list_EN = new ArrayList<>();
        Token token_renco=new Token();
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
                            if (xElement.hasAttribute("type") /*&& !xElement.getAttribute("type").equalsIgnoreCase("not_identified")*/)
                                if (xElement.getAttribute("type").equalsIgnoreCase("product") || xElement.getAttribute("type").equalsIgnoreCase("not_identified")) {
                                    Token subjectToken = elementToToken(xElement);
                                    Token objectToken = new Token();
                                    objectToken.setForm(object);
                                        System.out.println("Subject: "+ subjectToken.getForm()+ " Object: "+ objectToken.getForm());
                                    SentenceRelation sentenceRelation = new SentenceRelation();
                                    SentenceRelationId sentenceRelationId = new SentenceRelationId();
                                    sentenceRelationId.setSubject(subjectToken);
                                    sentenceRelationId.setObject(objectToken);
                                    sentenceRelationId.setRelation("coeur");
                                    sentenceRelationId.setSentence_text(sentence);
                                    if(type_relation.equalsIgnoreCase("component")) {
                                        sentenceRelationId.setType(SentenceRelationType.hasComponent);
                                    }
                                    else if(type_relation.equalsIgnoreCase("creator")){
                                        sentenceRelationId.setType(SentenceRelationType.hasFragranceCreator);
                                    }

                                    else if(type_relation.equalsIgnoreCase("ambasador")){
                                        sentenceRelationId.setType(SentenceRelationType.hasRepresentative );
                                    }
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
