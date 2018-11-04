package com.uclv.clustering;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

public class DataHolder implements Serializable {

    private static boolean first;
    private ArrayList<DataPoint> points;
    private DataPoint Min;
    private DataPoint Max;
    private int dim;

    public DataHolder() {
        points = new ArrayList<DataPoint>();
        dim = 0;
        first = true;
    }

    public DataHolder(int dim) {
        points = new ArrayList<DataPoint>();
        this.dim = dim;
        first = true;
    }

    public ArrayList getArrayList() {
        return points;
    }

    public DataPoint[] getArray() {
        int nn = points.size();
        DataPoint[] d = new DataPoint[nn];
        for (int m = 0; m < nn; m++) {
            DataPoint dp = getRow(m);
            d[m] = dp;
        }
        return d;
    }

    public void clear() {
        points.clear();
        dim = 0;

    }

    public void add(DataPoint p) {
        points.add(p);
        dim = p.getDimension();
        if (first) {
            first = false;
        }
    }

    public int getDimention() {
        return dim;
    }

    public void setDimention(int Dim) {
        this.dim = Dim;

    }

    public DataPoint getMin() {

        return Min;

    }

    public DataPoint getMax() {

        return Max;

    }

    public int getSize() {
        return points.size();
    }


    public double[] getElement(int n) {
        double[] ele = new double[this.dim];
        int m = 0;
        Iterator i = this.points.iterator();
        while (i.hasNext()) {
            DataPoint dp = (DataPoint) (i.next());
            ele[m] = dp.getAttribute(n);
            m++;
        }
        return ele;
    }

    public DataPoint getRow(int n) {
        DataPoint xx = null;
        if (n < points.size()) {
            xx = points.get(n);
        } else {
            System.out.println("Requested row=" + n + " but data have only the size=" + points.size());
        }
        return xx;
    }

    public void print() {
        if (points != null) {
            Iterator i = points.iterator();
            while (i.hasNext()) {
                DataPoint dp = (DataPoint) (i.next());
                dp.showAttributes();
            }
        }
    }

    public String toString() {
        int kk = 0;
        String tmp = "";
        if (points != null) {
            Iterator i = points.iterator();
            while (i.hasNext()) {
                DataPoint dp = (DataPoint) (i.next());
                tmp = tmp + Integer.toString(kk) + "  " + dp.toString() + "\n";
                kk++;
            }
        }
        return tmp;
    }
}
