package fr.inria.smilk.ws.relationextraction.bean;/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * Created by dhouib on 30/06/2016.
 */
public class Token {

    private String lema;

    private String form;
    private String pos;
    private String type;
    private int id;
    private String depRel;
    private int start;
    private int end;
    private int head;
    private String link;

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
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

    public String getPos() {
        return pos;
    }

    public void setPos(String pos) {
        this.pos = pos;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDepRel() {
        return depRel;
    }

    public void setDepRel(String depRel) {
        this.depRel = depRel;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public int getHead() {
        return head;
    }

    public void setHead(int head) {
        this.head = head;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Token token = (Token) o;

        if (id != token.id) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
