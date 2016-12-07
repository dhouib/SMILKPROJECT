package fr.inria.smilk.ws.relationextraction;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

/**
 * Created by dhouib on 30/11/2016.
 */
public class RencoIngredient {
    public String rencoIngredientByWebService(String in) throws Exception {
        String sortie = "";
        try {

            Client client = Client.create();
            String url = "https://demo-innovation-projets-groupe.viseo.net/semantic-rest/rest/semantic/entity/annotate";
            WebResource webResource = client.resource(url);

            ClientResponse response = webResource.type("text/plain").post(ClientResponse.class, in);
            sortie = response.getEntity(String.class);

        } catch (Exception e) {
            System.out.println("ERROR in the text: " + in);
            e.printStackTrace();

        }
        return sortie;
    }

}
