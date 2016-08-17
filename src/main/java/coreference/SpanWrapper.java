package coreference;

import opennlp.tools.util.Span;

/**
 * Created by dhouib on 16/08/2016.
 */
public class SpanWrapper implements Comparable <SpanWrapper> {

   Span span;
    int ranking_pos;
    int ranking_dist;

    int rank;
    public Span getSpan() {
        return span;
    }

    public void setSpan(Span span) {
        this.span = span;
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


    @Override
    public int compareTo(SpanWrapper o) {

        return Integer.compare(this.rank,o.rank);

    }



    }
