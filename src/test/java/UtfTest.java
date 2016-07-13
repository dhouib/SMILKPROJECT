import fr.inria.smilk.ws.relationextraction.AbstractRelationExtraction;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.AbstractCollection;

/**
 * Created by dhouib on 12/07/2016.
 */
public class UtfTest {


    public static void main(String[] args) throws IOException {
        String marque = "Lancôme";

        byte[] encoded = Files.readAllBytes(Paths.get("src/main/resources/testAnas"));
        System.out.println(new String(encoded, Charset.forName("windows-1252")));


        System.out.println(marque.getBytes());
        String sr=new String(marque.getBytes("UTF-8"), Charset.forName("windows-1252"));
        System.out.println(sr);



        String wrongMarque = "Lanc\\'f4me";
        System.out.println(wrongMarque.getBytes());

        String result = new String(wrongMarque.getBytes("UTF-8"), "ISO-8859-1");
        System.out.println(result);
        String wrongMarqueEnc =new String(wrongMarque.getBytes("UTF-8"), Charset.forName("ISO-8859-1"));
        System.out.println(wrongMarqueEnc);

        wrongMarqueEnc =new String(wrongMarque.getBytes("ISO-8859-1"), Charset.forName("UTF-8"));
        System.out.println(wrongMarqueEnc);




    }
}
