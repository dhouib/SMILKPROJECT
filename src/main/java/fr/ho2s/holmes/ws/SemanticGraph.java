
package fr.ho2s.holmes.ws;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour SemanticGraph complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="SemanticGraph"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="nodes" minOccurs="0"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element name="node" type="{http://ws.holmes.ho2s.fr/}BasicNode" maxOccurs="unbounded" minOccurs="0"/&gt;
 *                 &lt;/sequence&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="edges" minOccurs="0"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element name="edge" type="{http://ws.holmes.ho2s.fr/}Edge" maxOccurs="unbounded" minOccurs="0"/&gt;
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
@XmlType(name = "SemanticGraph", propOrder = {
    "nodes",
    "edges"
})
public class SemanticGraph {

    protected SemanticGraph.Nodes nodes;
    protected SemanticGraph.Edges edges;

    /**
     * Obtient la valeur de la propriété nodes.
     * 
     * @return
     *     possible object is
     *     {@link SemanticGraph.Nodes }
     *     
     */
    public SemanticGraph.Nodes getNodes() {
        return nodes;
    }

    /**
     * Définit la valeur de la propriété nodes.
     * 
     * @param value
     *     allowed object is
     *     {@link SemanticGraph.Nodes }
     *     
     */
    public void setNodes(SemanticGraph.Nodes value) {
        this.nodes = value;
    }

    /**
     * Obtient la valeur de la propriété edges.
     * 
     * @return
     *     possible object is
     *     {@link SemanticGraph.Edges }
     *     
     */
    public SemanticGraph.Edges getEdges() {
        return edges;
    }

    /**
     * Définit la valeur de la propriété edges.
     * 
     * @param value
     *     allowed object is
     *     {@link SemanticGraph.Edges }
     *     
     */
    public void setEdges(SemanticGraph.Edges value) {
        this.edges = value;
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
     *         &lt;element name="edge" type="{http://ws.holmes.ho2s.fr/}Edge" maxOccurs="unbounded" minOccurs="0"/&gt;
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
        "edge"
    })
    public static class Edges {

        protected List<Edge> edge;

        /**
         * Gets the value of the edge property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the edge property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getEdge().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link Edge }
         * 
         * 
         */
        public List<Edge> getEdge() {
            if (edge == null) {
                edge = new ArrayList<Edge>();
            }
            return this.edge;
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
     *         &lt;element name="node" type="{http://ws.holmes.ho2s.fr/}BasicNode" maxOccurs="unbounded" minOccurs="0"/&gt;
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
        "node"
    })
    public static class Nodes {

        protected List<BasicNode> node;

        /**
         * Gets the value of the node property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the node property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getNode().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link BasicNode }
         * 
         * 
         */
        public List<BasicNode> getNode() {
            if (node == null) {
                node = new ArrayList<BasicNode>();
            }
            return this.node;
        }

    }

}
