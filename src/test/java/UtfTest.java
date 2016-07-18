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
        String marque = "Lanc�me";

        /*byte[] encoded = Files.readAllBytes(Paths.get("src/main/resources/test1/1"));
        System.out.println(new String(encoded, Charset.forName("windows-1252")));
*/

        System.out.println(marque.getBytes());
        String sr=new String(marque.getBytes("UTF-8"), Charset.forName("UTF-16"));
        System.out.println("str:"+sr);

         sr=new String(marque.getBytes("UTF-8"), "windows-1252");
        System.out.println("str:"+sr);


        String wrongMarque = "Lancï¿½me";
        System.out.println(wrongMarque.getBytes());

        String result = new String(wrongMarque.getBytes("windows-1252"), "UTF-8");
        System.out.println(result);

        String wrongMarqueEnc =new String(wrongMarque.getBytes("UTF-8"), Charset.forName("ISO-8859-1"));
        System.out.println(wrongMarqueEnc);

        wrongMarqueEnc =new String(wrongMarque.getBytes("ISO-8859-1"), Charset.forName("UTF-8"));
        System.out.println(wrongMarqueEnc);
        wrongMarqueEnc =new String(wrongMarque.getBytes("ISO-8859-1"), Charset.forName("UTF-16"));
        System.out.println(wrongMarqueEnc);


        String input = "Ã¼";

        final Charset cp1252 = Charset.forName("windows-1252");
        final Charset utf8 = Charset.forName("UTF-8");

        // lets convert it to bytes in windows-1252:
        // this gives you 2 bytes: c3 bc
        // "Ã" ==> c3
        // "¼" ==> bc
        byte[] windows1252Bytes = input.getBytes(cp1252);

        // but in utf-8, c3 bc is "ü"
        String fixed = new String(windows1252Bytes, utf8);

        System.out.println(input);
        System.out.println(fixed);


    }
}
