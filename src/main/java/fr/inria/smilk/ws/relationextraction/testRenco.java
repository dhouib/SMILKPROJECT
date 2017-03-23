package fr.inria.smilk.ws.relationextraction;

/**
 * Created by dhouib on 09/03/2017.
 */
public class testRenco {
    public static void main(String[] args) throws Exception {
          String text = "Le baume_Fermeté_sculputure, associant Skinfibrine et élastopeptides, vise à rendre la peau plus ferme et élastique.";
        //String text = "La ligne s'anime également en mai avec une édition limitée Summer, rafraîchie d'ananas, création d'Ann Filpo et Catlos Benam";
        Renco renco = new Renco();
        String output= renco.rencoByWebService(text);
        System.out.println(output);
              //  renco.rencoByWebService(text));


    }
}
