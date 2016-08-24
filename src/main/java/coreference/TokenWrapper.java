package coreference;

import fr.inria.smilk.ws.relationextraction.bean.Token;

/**
 * Created by dhouib on 17/08/2016.
 */
public class TokenWrapper {
    Token token;
    int ranking_pos;
    int ranking_dep_rel;
    int ranking_type;
    int ranking_dist;
int ranking_type_form;
    int rank;

    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = token;
    }

    public int getRanking_pos() {
        return ranking_pos;
    }

    public void setRanking_pos(int ranking_pos) {
        this.ranking_pos = ranking_pos;
    }

    public int getRanking_dist() {
        return ranking_dist;
    }

    public void setRanking_dist(int ranking_dist) {
        this.ranking_dist = ranking_dist;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public int getRanking_type_form() {
        return ranking_type_form;
    }

    public void setRanking_type_form(int ranking_type_form) {
        this.ranking_type_form = ranking_type_form;
    }

    public int getRanking_type() {
        return ranking_type;
    }

    public void setRanking_type(int ranking_type) {
        this.ranking_type = ranking_type;
    }

    public int getRanking_dep_rel() {
        return ranking_dep_rel;
    }

    public void setRanking_dep_rel(int ranking_dep_rel) {
        this.ranking_dep_rel = ranking_dep_rel;
    }
}
