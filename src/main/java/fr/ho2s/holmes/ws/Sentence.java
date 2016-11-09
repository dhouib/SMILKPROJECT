
package fr.ho2s.holmes.ws;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour Sentence complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="Sentence"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="tokens" minOccurs="0"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element name="token" type="{http://ws.holmes.ho2s.fr/}Token" maxOccurs="unbounded" minOccurs="0"/&gt;
 *                 &lt;/sequence&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="semgraphs" minOccurs="0"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element name="semgraph" type="{http://ws.holmes.ho2s.fr/}SemanticGraph" maxOccurs="unbounded" minOccurs="0"/&gt;
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
@XmlType(name = "Sentence", propOrder = {
    "tokens",
    "semgraphs"
})
public class Sentence {

    protected Sentence.Tokens tokens;
    protected Sentence.Semgraphs semgraphs;

    /**
     * Obtient la valeur de la propriété tokens.
     * 
     * @return
     *     possible object is
     *     {@link Sentence.Tokens }
     *     
     */
    public Sentence.Tokens getTokens() {
        return tokens;
    }

    /**
     * Définit la valeur de la propriété tokens.
     * 
     * @param value
     *     allowed object is
     *     {@link Sentence.Tokens }
     *     
     */
    public void setTokens(Sentence.Tokens value) {
        this.tokens = value;
    }

    /**
     * Obtient la valeur de la propriété semgraphs.
     * 
     * @return
     *     possible object is
     *     {@link Sentence.Semgraphs }
     *     
     */
    public Sentence.Semgraphs getSemgraphs() {
        return semgraphs;
    }

    /**
     * Définit la valeur de la propriété semgraphs.
     * 
     * @param value
     *     allowed object is
     *     {@link Sentence.Semgraphs }
     *     
     */
    public void setSemgraphs(Sentence.Semgraphs value) {
        this.semgraphs = value;
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
     *         &lt;element name="semgraph" type="{http://ws.holmes.ho2s.fr/}SemanticGraph" maxOccurs="unbounded" minOccurs="0"/&gt;
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
        "semgraph"
    })
    public static class Semgraphs {

        protected List<SemanticGraph> semgraph;

        /**
         * Gets the value of the semgraph property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the semgraph property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getSemgraph().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link SemanticGraph }
         * 
         * 
         */
        public List<SemanticGraph> getSemgraph() {
            if (semgraph == null) {
                semgraph = new ArrayList<SemanticGraph>();
            }
            return this.semgraph;
        }

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
     *         &lt;element name="token" type="{http://ws.holmes.ho2s.fr/}Token" maxOccurs="unbounded" minOccurs="0"/&gt;
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
        "token"
    })
    public static class Tokens {

        protected List<Token> token;

        /**
         * Gets the value of the token property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the token property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getToken().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link Token }
         * 
         * 
         */
        public List<Token> getToken() {
            if (token == null) {
                token = new ArrayList<Token>();
            }
            return this.token;
        }

    }

}
