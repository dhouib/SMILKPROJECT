package preprocessing.coreference;

import java.util.Comparator;

/**
 * Created by dhouib on 17/08/2016.
 */
public class TokenWrapperComparator implements Comparator<TokenWrapper> {

    @Override
    public int compare(TokenWrapper o1, TokenWrapper o2) {
        // return  new Integer(o1.getRank()).compareTo( new Integer(o2.getRank()));
        if (o1.getRank() > o2.getRank())
            return 1;
        else if (o2.getRank() < o2.getRank())
            return -1;
        return 0;
    }
}
