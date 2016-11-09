
package fr.ho2s.holmes.ws;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour nodeType.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * <p>
 * <pre>
 * &lt;simpleType name="nodeType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="BASIC"/&gt;
 *     &lt;enumeration value="REFERENCE_TIME"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "nodeType")
@XmlEnum
public enum NodeType {

    BASIC,
    REFERENCE_TIME;

    public String value() {
        return name();
    }

    public static NodeType fromValue(String v) {
        return valueOf(v);
    }

}
