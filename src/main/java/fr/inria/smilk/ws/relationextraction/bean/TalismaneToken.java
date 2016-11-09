package fr.inria.smilk.ws.relationextraction.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dhouib on 10/10/2016.
 */
public class TalismaneToken {
    private int id;
    private String lema;
    private String form;
    private String pos;
    private String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPos() {
        return pos;
    }

    public void setPos(String pos) {
        this.pos = pos;
    }

    private String antecedent;
    private String edge_type;
    private List<TalismaneToken> previous=new ArrayList<>();

    public List<TalismaneToken> getPrevious() {
        return previous;
    }

    public void setPrevious(List<TalismaneToken> previous) {
        this.previous = previous;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLema() {
        return lema;
    }

    public void setLema(String lema) {
        this.lema = lema;
    }

    public String getForm() {
        return form;
    }

    public void setForm(String form) {
        this.form = form;
    }

    public String getAntecedent() {
        return antecedent;
    }

    public void setAntecedent(String antecedent) {
        this.antecedent = antecedent;
    }

    public String getEdge_type() {
        return edge_type;
    }

    public void setEdge_type(String edge_type) {
        this.edge_type = edge_type;
    }


}
