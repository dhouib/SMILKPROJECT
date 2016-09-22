/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package preprocessing.coreference;

/**
 *
 * @author fnoorala
 */
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class Renco {

    public String rencoByWebService(String in) throws Exception {
        String sortie = "";
        try {

            Client client = Client.create();
            String url = "https://demo-innovation-projets-groupe.viseo.net/renco-rest/rest/renco/getRenco";
            String tempURL = "https://172.42.1.166/renco-rest/rest/renco/getRenco";
            String fullUrl = "https://demo-innovation-projets-groupe.viseo.net/renco-rest/rest/rencoFull/getFullAnalyze";
            WebResource webResource = client.resource(url);

            ClientResponse response = webResource.type("text/plain").post(ClientResponse.class, in);

            sortie = response.getEntity(String.class);

            // System.out_copy.println(sortie);


        } catch (Exception e) {
            System.out.println("ERROR in the text: " + in);
            e.printStackTrace();

        }
        return sortie;
    }


}
