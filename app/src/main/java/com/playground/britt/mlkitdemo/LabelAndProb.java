package com.playground.britt.mlkitdemo;

import androidx.annotation.NonNull;

class LabelAndProb implements Comparable {
    protected final String label;
    protected final float prob;

    LabelAndProb(String label, float prob) {
        this.label = label;
        this.prob = prob;
    }

    public String getLabel() {
        return label;
    }

    public float getProb() {
        return prob;
    }

    @Override
    public int compareTo(@NonNull Object o) {
        if (o instanceof LabelAndProb) {
            return (int) (this.prob - ((LabelAndProb) o).prob);
        }
        return 0;
    }
}
