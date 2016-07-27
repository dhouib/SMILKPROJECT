package fr.inria.smilk.ws.farhad.relationextraction.bean;

/**
 * Created by dhouib on 25/07/2016.
 */
public class Spot  implements Comparable <Spot>{
    private String spot;
    private String type;
    private int start;
    private String end;
    private String link;

    public String getWikiname() {
        return wikiname;
    }

    public void setWikiname(String wikiname) {
        this.wikiname = wikiname;
    }

    private String wikiname;

    public String getSpot() {
        return spot;
    }

    public void setSpot(String spot) {
        this.spot = spot;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    @Override
    public int compareTo(Spot o) {

        return Integer.compare(this.start,o.start);

    }

}
