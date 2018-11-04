package com.uclv.clustering;

import org.jetbrains.annotations.NotNull;

/**
 * Created by anonymous on 1/8/2018.
 */
public class IndexValue implements Comparable {


    private double value;
    private int index;

    public IndexValue(double value, int index) {
        this.value = value;
        this.index = index;
    }

    @Override
    public int compareTo(@NotNull Object o) {
        double res = ((IndexValue) o).getValue() - value;
        if (res == 0) {
            return 0;
        } else if (res < 0) {
            return -1;
        } else {
            return 1;
        }

    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    @Override
    public String toString() {
        return "value=" + value + ", index=" + index;
    }
}
