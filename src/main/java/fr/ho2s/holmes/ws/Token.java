
package fr.ho2s.holmes.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour Token complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="Token"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="features" type="{http://ws.holmes.ho2s.fr/}features" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}int" /&gt;
 *       &lt;attribute name="lemma" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="form" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="start" type="{http://www.w3.org/2001/XMLSchema}int" /&gt;
 *       &lt;attribute name="end" type="{http://www.w3.org/2001/XMLSchema}int" /&gt;
 *       &lt;attribute name="pos" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="depRel" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="head" type="{http://www.w3.org/2001/XMLSchema}int" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Token", propOrder = {
    "features"
})
public class Token {

    protected Features features;
    @XmlAttribute(name = "id")
    protected Integer id;
    @XmlAttribute(name = "lemma")
    protected String lemma;
    @XmlAttribute(name = "form")
    protected String form;
    @XmlAttribute(name = "start")
    protected Integer start;
    @XmlAttribute(name = "end")
    protected Integer end;
    @XmlAttribute(name = "pos")
    protected String pos;
    @XmlAttribute(name = "depRel")
    protected String depRel;
    @XmlAttribute(name = "head")
    protected Integer head;

    /**
     * Obtient la valeur de la propriété features.
     * 
     * @return
     *     possible object is
     *     {@link Features }
     *     
     */
    public Features getFeatures() {
        return features;
    }

    /**
     * Définit la valeur de la propriété features.
     * 
     * @param value
     *     allowed object is
     *     {@link Features }
     *     
     */
    public void setFeatures(Features value) {
        this.features = value;
    }

    /**
     * Obtient la valeur de la propriété id.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getId() {
        return id;
    }

    /**
     * Définit la valeur de la propriété id.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setId(Integer value) {
        this.id = value;
    }

    /**
     * Obtient la valeur de la propriété lemma.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLemma() {
        return lemma;
    }

    /**
     * Définit la valeur de la propriété lemma.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLemma(String value) {
        this.lemma = value;
    }

    /**
     * Obtient la valeur de la propriété form.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getForm() {
        return form;
    }

    /**
     * Définit la valeur de la propriété form.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setForm(String value) {
        this.form = value;
    }

    /**
     * Obtient la valeur de la propriété start.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getStart() {
        return start;
    }

    /**
     * Définit la valeur de la propriété start.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setStart(Integer value) {
        this.start = value;
    }

    /**
     * Obtient la valeur de la propriété end.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getEnd() {
        return end;
    }

    /**
     * Définit la valeur de la propriété end.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setEnd(Integer value) {
        this.end = value;
    }

    /**
     * Obtient la valeur de la propriété pos.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPos() {
        return pos;
    }

    /**
     * Définit la valeur de la propriété pos.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPos(String value) {
        this.pos = value;
    }

    /**
     * Obtient la valeur de la propriété depRel.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDepRel() {
        return depRel;
    }

    /**
     * Définit la valeur de la propriété depRel.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDepRel(String value) {
        this.depRel = value;
    }

    /**
     * Obtient la valeur de la propriété head.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getHead() {
        return head;
    }

    /**
     * Définit la valeur de la propriété head.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setHead(Integer value) {
        this.head = value;
    }

}
