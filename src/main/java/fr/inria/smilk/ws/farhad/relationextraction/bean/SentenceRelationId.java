package fr.inria.smilk.ws.farhad.relationextraction.bean;

/**
 * Created by dhouib on 02/07/2016.
 */
public class SentenceRelationId {

    private Spot subject;

    private Spot object;

    private SentenceRelationType type;




    public Spot getSubject() {
        return subject;
    }

    public void setSubject(Spot subject) {
        this.subject = subject;
    }

    public Spot getObject() {
        return object;
    }

    public void setObject(Spot object) {
        this.object = object;
    }


    public SentenceRelationType getType() {
        return type;
    }

    public void setType(SentenceRelationType type) {
        this.type = type;
    }


    public String toString(){
        return  "Subject:"+subject.getSpot()+",Object:"+object.getSpot();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SentenceRelationId that = (SentenceRelationId) o;

        if (!object.equals(that.object)) return false;
        if (!subject.equals(that.subject)) return false;
        if (type != that.type) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = subject.hashCode();
        result = 31 * result + object.hashCode();
        result = 31 * result + type.hashCode();
        return result;
    }
}

