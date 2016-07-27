import fr.inria.smilk.ws.relationextraction.AbstractRelationExtraction;
import fr.inria.smilk.ws.relationextraction.util.ListFilesUtil;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.AbstractCollection;
import java.util.List;

/**
 * Created by dhouib on 12/07/2016.
 */
public class UtfTest {


    public static void main(String[] args) throws IOException {
        //String marque = "Lanc�me";

        /*byte[] encoded = Files.readAllBytes(Paths.get("src/main/resources/input/test/1"));
        System.out.println(new String(encoded, Charset.forName("windows-1252")));
*/

        ListFilesUtil listFileUtil = new ListFilesUtil();
        listFileUtil.listFilesFromDirector("src/main/resources/input/test");
        List<String> files = listFileUtil.files;

        for(String filename: files){
            System.out.println("file:"+filename);
            FileInputStream is = new FileInputStream("src/main/resources/input/test/"+filename);

            //windows Ansi encodding
            InputStreamReader isr = new InputStreamReader(is, Charset.forName(filename.equals("10")?"utf-8":"windows-1252"));


            BufferedReader fileReader = new BufferedReader(isr);

            // InputStreamReader isr1 = new InputStreamReader(is,  Charset.forName("UTF-8"));
            //fileReader = new BufferedReader(isr1);
            String line = null;
            //fileReader = new BufferedReader(in);
            while ((line = fileReader.readLine()) != null) {
                System.out.println("line"+line);
            }
        }



/*
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
*/

    }
}
