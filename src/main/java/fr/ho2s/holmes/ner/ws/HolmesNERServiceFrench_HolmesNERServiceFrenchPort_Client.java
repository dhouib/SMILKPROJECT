
package fr.ho2s.holmes.ner.ws;

/**
 * Please modify this class to meet your needs
 * This class is not complete
 */

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.ws.Action;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

/**
 * This class was generated by Apache CXF 3.0.11
 * 2016-10-26T21:35:45.244+02:00
 * Generated source version: 3.0.11
 * 
 */
public final class HolmesNERServiceFrench_HolmesNERServiceFrenchPort_Client {

    private static final QName SERVICE_NAME = new QName("http://ws.ner.holmes.ho2s.fr/", "HolmesNERServiceFrenchService");

    private HolmesNERServiceFrench_HolmesNERServiceFrenchPort_Client() {
    }

    public static void main(String args[]) throws java.lang.Exception {
        URL wsdlURL = HolmesNERServiceFrenchService.WSDL_LOCATION;
        if (args.length > 0 && args[0] != null && !"".equals(args[0])) { 
            File wsdlFile = new File(args[0]);
            try {
                if (wsdlFile.exists()) {
                    wsdlURL = wsdlFile.toURI().toURL();
                } else {
                    wsdlURL = new URL(args[0]);
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
      
        HolmesNERServiceFrenchService ss = new HolmesNERServiceFrenchService(wsdlURL, SERVICE_NAME);
        HolmesNERServiceFrench port = ss.getHolmesNERServiceFrenchPort();  
        
        {
        System.out.println("Invoking parse...");
        java.lang.String _parse_text = "";
        java.util.List<fr.ho2s.holmes.ner.ws.CompactNamedEntity> _parse__return = port.parse(_parse_text);
        System.out.println("parse.result=" + _parse__return);


        }

        System.exit(0);
    }

}