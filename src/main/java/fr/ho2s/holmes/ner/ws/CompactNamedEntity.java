
package fr.ho2s.holmes.ner.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour compactNamedEntity complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="compactNamedEntity"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="entityString" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="entityType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="score" type="{http://www.w3.org/2001/XMLSchema}double"/&gt;
 *         &lt;element name="sentenceNdx" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/&gt;
 *         &lt;element name="spanFrom" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="spanTo" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "compactNamedEntity", propOrder = {
    "entityString",
    "entityType",
    "score",
    "sentenceNdx",
    "spanFrom",
    "spanTo"
})
public class CompactNamedEntity {

    protected String entityString;
    protected String entityType;
    protected double score;
    protected Integer sentenceNdx;
    protected int spanFrom;
    protected int spanTo;

    /**
     * Obtient la valeur de la propriété entityString.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEntityString() {
        return entityString;
    }

    /**
     * Définit la valeur de la propriété entityString.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEntityString(String value) {
        this.entityString = value;
    }

    /**
     * Obtient la valeur de la propriété entityType.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEntityType() {
        return entityType;
    }

    /**
     * Définit la valeur de la propriété entityType.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEntityType(String value) {
        this.entityType = value;
    }

    /**
     * Obtient la valeur de la propriété score.
     * 
     */
    public double getScore() {
        return score;
    }

    /**
     * Définit la valeur de la propriété score.
     * 
     */
    public void setScore(double value) {
        this.score = value;
    }

    /**
     * Obtient la valeur de la propriété sentenceNdx.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getSentenceNdx() {
        return sentenceNdx;
    }

    /**
     * Définit la valeur de la propriété sentenceNdx.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setSentenceNdx(Integer value) {
        this.sentenceNdx = value;
    }

    /**
     * Obtient la valeur de la propriété spanFrom.
     * 
     */
    public int getSpanFrom() {
        return spanFrom;
    }

    /**
     * Définit la valeur de la propriété spanFrom.
     * 
     */
    public void setSpanFrom(int value) {
        this.spanFrom = value;
    }

    /**
     * Obtient la valeur de la propriété spanTo.
     * 
     */
    public int getSpanTo() {
        return spanTo;
    }

    /**
     * Définit la valeur de la propriété spanTo.
     * 
     */
    public void setSpanTo(int value) {
        this.spanTo = value;
    }

}
