package fr.inria.smilk.ws.relationextraction.bean;

/**
 * Created by dhouib on 05/10/2016.
 */
public class BonsaiToken {
    private int id;
    private String lema;
    private String form;
    private String antecedent;
    private String edge_type;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLemmaByID(int id){
        return lema;
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
