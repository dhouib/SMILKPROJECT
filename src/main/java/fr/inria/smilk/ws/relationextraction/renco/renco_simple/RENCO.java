package fr.inria.smilk.ws.relationextraction.renco.renco_simple;

/**
 * Created by dhouib on 25/05/2016.
 */

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;


public class RENCO {



    public String rencoByWebService(String in) throws Exception {
        String sortie = "";

        try {

            Client client = Client.create();
            String url = "https://demo-innovation-projets-groupe.viseo.net/fr.inria.smilk.ws.relationextraction.renco-rest/rest/fr.inria.smilk.ws.relationextraction.renco/getRenco";
            String tempURL = "https://172.42.1.166/fr.inria.smilk.ws.relationextraction.renco-rest/rest/fr.inria.smilk.ws.relationextraction.renco/getRenco";
            WebResource webResource = client.resource(url);

            ClientResponse response = webResource.type("text/plain").post(ClientResponse.class, in);

            sortie = response.getEntity(String.class);

            System.out.println(sortie);


        } catch (Exception e) {
            System.out.println("ERROR in the text: " + in);
            e.printStackTrace();

        }
        return sortie;


    }
}
