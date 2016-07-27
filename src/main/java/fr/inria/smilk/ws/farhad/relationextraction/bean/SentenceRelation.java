package fr.inria.smilk.ws.farhad.relationextraction.bean;

import fr.inria.smilk.ws.relationextraction.bean.SentenceRelationMethod;

/**
 * Created by dhouib on 02/07/2016.
 */
public class SentenceRelation {

    private SentenceRelationId sentenceRelationId;

    private SentenceRelationMethod method;

    public SentenceRelationId getSentenceRelationId() {
        return sentenceRelationId;
    }

    public void setSentenceRelationId(SentenceRelationId sentenceRelationId) {
        this.sentenceRelationId = sentenceRelationId;
    }

    public SentenceRelationMethod getMethod() {
        return method;
    }

    public void setMethod(SentenceRelationMethod method) {
        this.method = method;
    }

    public String toString(){
        return  sentenceRelationId +",method: "+method;
    }



}

