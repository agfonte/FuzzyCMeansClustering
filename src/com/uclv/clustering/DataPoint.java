package com.uclv.clustering;

import org.apache.lucene.document.Document;

import java.io.Serializable;

public class DataPoint implements Serializable {

    private Document doc;
    private double a[];
    private int dimen;
    private int clusterNumber;

    public DataPoint(double[] a, int dim, Document doc) {
        this.a = a;
        this.dimen = dim;
        this.clusterNumber = 0;
        this.doc = doc;
    }

    public DataPoint(double xx[], int dim) {
        a = xx;
        this.dimen = dim;
        this.clusterNumber = 0;
    }

    public DataPoint(double xx[]) {
        int dim = xx.length;
        a = xx;
        this.dimen = dim;
        this.clusterNumber = 0;
    }

    public DataPoint(double[] xx, Document doc) {
        int dim = xx.length;
        a = xx;
        this.dimen = dim;
        this.clusterNumber = 0;
        this.doc = doc;
    }

    public static double distance(DataPoint dp1, DataPoint dp2) {
        double result = 0;
        for (int i = 0; i < dp1.dimen; i++) {
            double x1 = dp1.getAttribute(i);
            double x2 = dp2.getAttribute(i);
            result = result + (x1 - x2) * (x1 - x2);
        }
        result = Math.sqrt(result);
        return result;
    }

    public static double distanceSqrt(DataPoint dp1, DataPoint dp2) {
        double result = 0;
        for (int i = 0; i < dp1.dimen; i++) {
            double x1 = dp1.getAttribute(i);
            double x2 = dp2.getAttribute(i);
            result += (x1 - x2) * (x1 - x2);
        }
        return result;
    }

    public Document getDoc() {
        return doc;
    }

    public void setDoc(Document doc) {
        this.doc = doc;
    }

    public int getDimension() {

        return this.dimen;
    }

    public void assignToCluster(int clusterNumber) {
        this.clusterNumber = clusterNumber;
    }

    public int getClusterNumber() {
        return this.clusterNumber;
    }

    public double getAttribute(int index) {
        if (index < this.dimen) {
            return this.a[index];
        } else {
            return -999999.;
        }
    }

    public void showAttributes() {
        String s = "(";
        for (int i = 0; i < this.dimen; i++) {
            if (i < this.dimen - 1)
                s = s + this.getAttribute(i) + ",";
            if (i == this.dimen - 1)
                s = s + this.getAttribute(i);
        }
        s = s + ")";
        System.out.println(s);
    }

    public String toString() {
        String s = "(";
        for (int i = 0; i < this.dimen; i++) {
            if (i < this.dimen - 1)
                s = s + this.getAttribute(i) + ",";
            if (i == this.dimen - 1)
                s = s + this.getAttribute(i);
        }
        return doc.getField("address").stringValue() + s + ")[" + this.clusterNumber + "]";
    }

    public double[] getArray() {
        return a;
    }

    public void setArray(double[] array) {
        this.a = array;
    }
}
