package fr.inria.smilk.ws.relationextraction.bean;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.List;

/**
 * Created by dhouib on 02/07/2016.
 */
public class SentenceRelations {

    private Node sentenceNode;

    private List<SentenceRelation> relations;


    public Node getSentenceNode() {
        return sentenceNode;
    }

    public void setSentenceNode(Node sentenceNode) {
        this.sentenceNode = sentenceNode;
    }


}
