package fr.inria.smilk.ws.relationextraction;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import fr.inria.smilk.ws.relationextraction.bean.SentenceRelation;
import fr.inria.smilk.ws.relationextraction.bean.SentenceRelationId;
import fr.inria.smilk.ws.relationextraction.util.ListFilesUtil;
import fr.inria.smilk.ws.relationextraction.util.openNLP;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by dhouib on 03/07/2016.
 */
public class ExtractionHelper {

    public static Model constructModel (SentenceRelationId sentenceRelationId){
        Model model = ModelFactory.createDefaultModel();
        String smilkprefix="http://ns.inria.fr/smilk/elements/1.0/";
        String rdfsprefix="http://www.w3.org/2000/01/rdf-schema#";
        Resource subject = model.createResource(smilkprefix + sentenceRelationId.getSubject().getForm());
        Property belongs_to_group = model.createProperty(smilkprefix + sentenceRelationId.getType().name());
        Property  rdfs_type = model.createProperty(rdfsprefix + "a");
        Resource object=model.createResource(smilkprefix + sentenceRelationId.getObject().getForm());
        //Resource type=m.createResource(sentenceRelationId.getSubject().getType());
        //  m.add(subject,rdfs_type,type).
        model.add(subject, belongs_to_group, object);
        model.write(System.out, "N-TRIPLE");

        return model;
    }

    public static void writeRdf (Model model) throws IOException {
        File file=new File("src/main/resources/extractedrelation.ttl");

        FileWriter out = null;

        try {

            out = new FileWriter(file,true);
            try {
                model.write(out, "N3");

            } finally {
                try {
                    out.flush();
                    out.close();
                } catch (IOException closeException) {
                    // ignore
                }
            }

        }finally {

            try {
                out.flush();
                out.close();
            } catch (IOException ex) {

                // ignore
            }
        }

    }
    public static Token elementToToken (Element element){
        Token token=new Token();
        token.setId(Integer.parseInt(element.getAttribute("id")));
        token.setForm(element.getAttribute("form"));
        token.setStart(Integer.parseInt(element.getAttribute("start")));
        token.setEnd(Integer.parseInt(element.getAttribute("end")));
        token.setLema(element.getAttribute("lemma"));
        token.setPos(element.getAttribute("pos"));
        token.setDepRel(element.getAttribute("depRel"));
        token.setHead(Integer.parseInt(element.getAttribute("head")));
        token.setType(element.getAttribute("type"));
        return token;
    }

    public static Element searchToken(NodeList nTokensList, int index_pattern) {
        Element before_current_element = null;
        for (int currentTokensIndex = 0; currentTokensIndex < nTokensList.getLength(); currentTokensIndex++) {
            Node nTokensNode = nTokensList.item(currentTokensIndex);
            if (nTokensNode instanceof Element) {
                for (int currentChildIndex = 0; currentChildIndex < nTokensNode.getChildNodes().getLength(); currentChildIndex++) {
                    Node nTokenNode = nTokensNode.getChildNodes().item(currentChildIndex);
                    if (nTokenNode instanceof Element) {
                        Element current_element = (Element) nTokenNode;
                        String index_start = current_element.getAttribute("start");
                        System.out.println("index_start: "+index_start);
                        int index_token = Integer.parseInt(index_start);
                        if (index_token < index_pattern) {
                            before_current_element = current_element;
                            System.out.println("if <" + before_current_element.getNodeValue());
                        } else {
                            System.out.println("else:"+before_current_element);
                            return before_current_element;

                        }
                    }
                }
            }
        }
        return before_current_element;
    }

    public static Element searchTokenByType (NodeList nTokensList, String product ) {
        System.out.println ("entrer");
        Element before_current_element = null;
        for (int currentTokensIndex = 0; currentTokensIndex < nTokensList.getLength(); currentTokensIndex++) {
            Node nTokensNode = nTokensList.item(currentTokensIndex);
            if (nTokensNode instanceof Element) {
                for (int currentChildIndex = 0; currentChildIndex < nTokensNode.getChildNodes().getLength(); currentChildIndex++) {
                    Node nTokenNode = nTokensNode.getChildNodes().item(currentChildIndex);
                    if (nTokenNode instanceof Element) {

                        Element current_element = (Element) nTokenNode;
                        String index_start = current_element.getAttribute("start");
                        //System.out.println("index_start: " + index_start + " type: " + current_element.getAttribute("type"));

                        //before_current_element = current_element;
                        if (current_element.hasAttribute("type")){
                            if(current_element.getAttribute("type").equalsIgnoreCase(product)) {
                                before_current_element = current_element;
                                System.out.println("before_current: " + before_current_element.getAttribute("form"));
                        }

                        }
                    } else {
                        return before_current_element;
                    }



                }
            }
        }
        return before_current_element;
    }

    public static Element searchTokenAfterPattern(NodeList nTokensList, int indexRelation){
        Element after_current_element=null;
        for (int currentTokensIndex = 0; currentTokensIndex < nTokensList.getLength(); currentTokensIndex++) {
            Node nTokensNode = nTokensList.item(currentTokensIndex);
            if (nTokensNode instanceof Element) {
                for (int currentChildIndex = 0; currentChildIndex < nTokensNode.getChildNodes().getLength(); currentChildIndex++) {
                    Node nTokenNode = nTokensNode.getChildNodes().item(currentChildIndex);
                    if (nTokenNode instanceof Element) {
                        Element current_element = (Element) nTokenNode;
                        String index_start = current_element.getAttribute("start");
                        int index_token = Integer.parseInt(index_start);
                        if (index_token > indexRelation) {
                            after_current_element = current_element;
                        } else {
                            return after_current_element;
                        }
                    }
                }
            }
        }

        return after_current_element;
    }

    public static String sentenceToString(Node sentenceNode){
        StringBuilder builder = new StringBuilder();
        sentenceToString(sentenceNode,builder);
        return builder.toString();
    }

    private static void sentenceToString(Node node,StringBuilder builder) {
        // do something with the current node instead of System.out
        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node currentNode = nodeList.item(i);
            if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
                Element currentElement = (Element) currentNode;
                if(StringUtils.isNotBlank(currentElement.getAttribute("form"))){
                    int startIndex = Integer.parseInt(currentElement.getAttribute("start"));
                    if(builder.length() < startIndex){
                        builder.append(StringUtils.repeat(' ', startIndex - builder.length()));
                    }
                    builder.append(currentElement.getAttribute("form"));
                }
                //calls this method for all the children which is Element
                sentenceToString(currentNode,builder);
            }
        }
    }



}
