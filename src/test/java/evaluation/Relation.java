package evaluation;

/**
 * Created by dhouib on 29/11/2016.
 */
public class Relation {
    String subject;
    String object;
    String predicate;

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }

    public String getPredicate() {
        return predicate;
    }

    public void setPredicate(String predicate) {
        this.predicate = predicate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Relation relation = (Relation) o;

        if (object != null ? !object.equals(relation.object) : relation.object != null) return false;
        if (predicate != null ? !predicate.equals(relation.predicate) : relation.predicate != null) return false;
        if (subject != null ? !subject.contains(relation.subject)&&!subject.contains("_:s1"): relation.subject != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result= subject != null ? subject.hashCode() : 0;
        result = 31 * result + (object != null ? object.hashCode() : 0);
        result = 31 * result + (predicate != null ? predicate.hashCode() : 0);
        return result;
    }
}
