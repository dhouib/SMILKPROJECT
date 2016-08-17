import java.util.LinkedList;
import java.util.List;

/**
 * Created by dhouib on 05/08/2016.
 */
public class test_enumeration {



    public static void main(String[] args) throws Exception {
        String text = "En parfum, une déferlante d'initiatives entend troubler le jeu des One Million de Paco Rabanne (Puig), Le Male de Jean Paul Gaultier (BPI) et Eau Sauvage de Dior (LVMH), trio de tête français.";
        String[] lines = text.split(",");
        List<String> lines_t = new LinkedList<>();
        for (String line : lines) {
            // System.out.println("line: " + line);
            if (!(line.indexOf("et") >= 0)) {
                lines_t.add(line);
            }
            if (line.indexOf("et") >= 0) {
                String[] sublines = line.split("et");
                for (String subline : sublines) {
                    lines_t.add(subline);
                    // System.out.println("subline: " + subline);
                }
            }
        }

        for (String line : lines_t){
            System.out.println("line: " + line);
        }
    }

}
