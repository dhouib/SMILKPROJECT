package fr.inria.smilk.ws.relationextraction.bean;

import fr.inria.smilk.ws.relationextraction.Token;

/**
 * Created by dhouib on 02/07/2016.
 */
public class SentenceRelationId {

    private Token subject;

    private Token object;

    private String relation;

    private SentenceRelationType type;

    public Token getSubject() {
        return subject;
    }

    public void setSubject(Token subject) {
        this.subject = subject;
    }

    public Token getObject() {
        return object;
    }

    public void setObject(Token object) {
        this.object = object;
    }

    public String getRelation() {
        return relation;
    }

    public void setRelation(String relation) {
        this.relation = relation;
    }

    public SentenceRelationType getType() {
        return type;
    }

    public void setType(SentenceRelationType type) {
        this.type = type;
    }


    public String toString(){
        return  "Subject:"+subject.getForm()+",Object:"+object.getForm()+",relation:"+ relation+ ",type:"+type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SentenceRelationId that = (SentenceRelationId) o;

        if (!object.equals(that.object)) return false;
        if (!relation.equals(that.relation)) return false;
        if (!subject.equals(that.subject)) return false;
        if (type != that.type) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = subject.hashCode();
        result = 31 * result + object.hashCode();
        result = 31 * result + relation.hashCode();
        result = 31 * result + type.hashCode();
        return result;
    }
}

