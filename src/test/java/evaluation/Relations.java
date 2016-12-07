package evaluation;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import java.util.List;

/**
 * Created by dhouib on 29/11/2016.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Relations {
    @XmlElement(name="Relation")
    private List<Relation> relation;

    public List<Relation> getRelation() {
        return relation;
    }

    public void setRelation(List<Relation> relation) {
        this.relation = relation;
    }
}
