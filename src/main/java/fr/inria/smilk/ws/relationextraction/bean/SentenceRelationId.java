package fr.inria.smilk.ws.relationextraction.bean;

/**
 * Created by dhouib on 02/07/2016.
 */
public class SentenceRelationId {

    private Token subject;

    private Token object;

    private String relation;

    private SentenceRelationType type;

    private double confidence;

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public String getSentence_text() {
        return sentence_text;
    }

    public void setSentence_text(String sentence_text) {
        this.sentence_text = sentence_text;
    }

    private String sentence_text;

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


    public String toString() {
        return "Subject:" + subject.getForm() + ",Object:" + object.getForm() + ",relation:" + relation + ",type:" + type + "\nsentenceText:" + sentence_text + ",confidence:"+confidence;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SentenceRelationId that = (SentenceRelationId) o;

        if (!object.getForm().equals(that.object.getForm())) return false;
        if (!relation.equals(that.relation)) return false;
        if (subject != null && that.subject != null && subject.getForm() !=null && !subject.getForm().equals(that.subject.getForm())) return false;
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

