
package fr.ho2s.holmes.ws;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour HolmesOutput complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="HolmesOutput"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="sentences" minOccurs="0"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element name="sentence" type="{http://ws.holmes.ho2s.fr/}Sentence" maxOccurs="unbounded" minOccurs="0"/&gt;
 *                 &lt;/sequence&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "HolmesOutput", propOrder = {
    "sentences"
})
public class HolmesOutput {

    protected HolmesOutput.Sentences sentences;

    /**
     * Obtient la valeur de la propriété sentences.
     * 
     * @return
     *     possible object is
     *     {@link HolmesOutput.Sentences }
     *     
     */
    public HolmesOutput.Sentences getSentences() {
        return sentences;
    }

    /**
     * Définit la valeur de la propriété sentences.
     * 
     * @param value
     *     allowed object is
     *     {@link HolmesOutput.Sentences }
     *     
     */
    public void setSentences(HolmesOutput.Sentences value) {
        this.sentences = value;
    }


    /**
     * <p>Classe Java pour anonymous complex type.
     * 
     * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
     * 
     * <pre>
     * &lt;complexType&gt;
     *   &lt;complexContent&gt;
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *       &lt;sequence&gt;
     *         &lt;element name="sentence" type="{http://ws.holmes.ho2s.fr/}Sentence" maxOccurs="unbounded" minOccurs="0"/&gt;
     *       &lt;/sequence&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "sentence"
    })
    public static class Sentences {

        protected List<Sentence> sentence;

        /**
         * Gets the value of the sentence property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the sentence property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getSentence().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link Sentence }
         * 
         * 
         */
        public List<Sentence> getSentence() {
            if (sentence == null) {
                sentence = new ArrayList<Sentence>();
            }
            return this.sentence;
        }

    }

}
