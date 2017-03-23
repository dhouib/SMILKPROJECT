package fr.inria.smilk.ws.relationextraction.Evaluation;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Created by dhouib on 29/11/2016.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name="Sentences")
public class Sentences {
    @XmlElement
    private List<Sentence> sentence;

    public List<Sentence> getSentence() {
        return sentence;
    }

    public void setSentence(List<Sentence> sentence) {
        this.sentence = sentence;
    }
}
