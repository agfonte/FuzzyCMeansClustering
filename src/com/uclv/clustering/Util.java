package com.uclv.clustering;

import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Scanner;

public class Util {

    public static void showError(String s, Component component) {
        JOptionPane.showMessageDialog(component, s, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private static String[] stopWords(File f) throws FileNotFoundException {
        Scanner sr = new Scanner(f);
        ArrayList<String> l = new ArrayList<>();
        sr = sr.reset();
        while (sr.hasNext()) {
            l.add(sr.nextLine());
        }
        String[] arr = new String[l.size()];
        System.arraycopy(l.toArray(), 0, arr, 0, l.size());
        sr.close();
        return arr;
    }

    public static String[][] stopWordsFile(File f) throws Exception {
        String[][] res;
        if (f != null && f.canRead()) {
            if (f.isDirectory()) {
                File[] files = f.listFiles((dir, name) -> name.endsWith(".txt") && name.contains("stopwords"));
                ArrayList<String[]> stopwords = new ArrayList<>();
                if (files.length == 0) {
                    throw new IllegalArgumentException("No stop words files!!!.");
                }
                for (File file : files) {
                    stopwords.add(stopWords(file));
                }
                res = new String[stopwords.size()][];
                for (int i = 0; i < res.length; i++) {
                    res[i] = stopwords.get(i);
                }
                return res;
            } else {
                return new String[][]{stopWords(f)};
            }
        } else {
            throw new Error("No stop words!!!");
        }
    }

    public static int detectLanguage(Document doc, String[][] stops) throws IOException {
        int cant = 0;
        int inx = 0;
        int max = 0;
        ArrayList<String> to_detect = tokens(doc);
        for (int i = 0; i < stops.length; i++) {
            for (int j = 0; j < stops[i].length; j++) {
                for (String aTo_detect : to_detect) {
                    if (aTo_detect.equalsIgnoreCase(stops[i][j])) {
                        cant++;
                    }
                }
            }
            if (cant > max) {
                max = cant;
                inx = i;
            }
            cant = 0;
        }
        return inx;
    }

    public static ArrayList<String> tokens(Document docs, char an, String[] s) throws IOException {
        Analyzer analyzer = getAnalyzer(an, s);
        Field field = docs.getField("content");
        String doc = field == null ? "" : field.stringValue();
        StringReader sr = new StringReader(doc);
        TokenStream stream = analyzer.tokenStream(null, sr);
        LowerCaseFilter a = new LowerCaseFilter(stream);
        TokenStream t1 = new PorterStemFilter(a);
        ArrayList<String> tokenList = new ArrayList<>();
        Token token = t1.next();
        while (token != null) {
            tokenList.add(token.term());
            token = stream.next();
        }
        return tokenList;
    }

    public static Analyzer getAnalyzer(char a, String[] stop) {
        switch (a) {
            case 's':
                return stop != null ? new StandardAnalyzer(stop) : new StandardAnalyzer();
            case 't':
                return stop != null ? new StopAnalyzer(stop) : new StopAnalyzer();
            case 'w':
                return new WhitespaceAnalyzer();
            case 'a':
                return new SimpleAnalyzer();
            default:
                return stop != null ? new StandardAnalyzer(stop) : new StandardAnalyzer();
        }

    }

    private static ArrayList<String> tokens(Document documento) throws IOException {
        Analyzer analyzer = new StandardAnalyzer();
        Field field = documento.getField("content");
        String doc = field == null ? "" : field.stringValue();
        StringReader sr = new StringReader(doc);
        TokenStream stream = analyzer.tokenStream(null, sr);
        LowerCaseFilter a = new LowerCaseFilter(stream);
        TokenStream t1 = new PorterStemFilter(a);
        ArrayList<String> tokenList = new ArrayList<>();
        Token token = t1.next();
        while (token != null) {
            tokenList.add(token.term());
            token = t1.next();
        }
        return tokenList;
    }

    public static int findLargest(double[] A) {
        double largestValue = A[0];
        int largestIndex = 0;
        for (int i = 1; i < A.length; ++i) {
            if (A[i] > largestValue) {
                largestValue = A[i];
                largestIndex = i;
            }
        }
        return largestIndex;
    }

    public static int findLargest(double[] A, int boundary) {
        double largestValue = A[0];
        int largestIndex = 0;

        for (int i = 1; i <= boundary; ++i) {
            if (A[i] > largestValue) {
                largestValue = A[i];
                largestIndex = i;
            }
        }

        return largestIndex;
    }

    public static int findSmallest(double[] A) {
        double smallestValue = A[0];
        int smallestIndex = 0;

        for (int i = 1; i < A.length; ++i) {
            if (A[i] < smallestValue) {
                smallestValue = A[i];
                smallestIndex = i;
            }
        }

        return smallestIndex;
    }

    public static int findSmallest(double[] A, int boundary) {
        double smallestValue = A[0];
        int smallestIndex = 0;

        for (int i = 1; i < boundary; ++i) {
            if (A[i] < smallestValue) {
                smallestValue = A[i];
                smallestIndex = i;
            }
        }

        return smallestIndex;
    }

    public static double[] Sort(double[] unsorted) {
        double[] sorted = new double[unsorted.length];

        for (int boundary = unsorted.length - 1; boundary >= 0; --boundary) {
            int largest = findLargest(unsorted, boundary);
            sorted[boundary] = unsorted[largest];
            unsorted[largest] = unsorted[boundary];
        }

        return sorted;
    }

    public static double findMax(double[] nums) {
        double curMax = nums[0];

        for (int i = 1; i < nums.length; ++i) {
            if (nums[i] > curMax) {
                curMax = nums[i];
            }
        }

        return curMax;
    }

    public static String findMax(String[] strs) {
        String curMax = strs[0];

        for (int i = 1; i < strs.length; ++i) {
            if (strs[i].compareTo(curMax) > 0) {
                curMax = strs[i];
            }
        }

        return curMax;
    }

    public static int findMin(int[] nums) {
        int curMin = nums[0];

        for (int i = 1; i < nums.length; ++i) {
            if (nums[i] < curMin) {
                curMin = nums[i];
            }
        }

        return curMin;
    }

    public static String findMin(String[] strs) {
        String curMin = strs[0];

        for (int i = 1; i < strs.length; ++i) {
            if (strs[i].compareTo(curMin) < 0) {
                curMin = strs[i];
            }
        }

        return curMin;
    }

    public static int findIndex(double[] nums, double val) {
        for (int i = 0; i < nums.length; ++i) {
            if (nums[i] == val) {
                return i;
            }
        }

        return -1;
    }

    public static int findIndexBoundary(double[] nums, double val, int bound) {
        for (int i = bound; i < nums.length; ++i) {
            if (nums[i] == val) {
                return i;
            }
        }
        return -1;
    }

    public static int findVal(String[] strs, String str) {
        for (int i = 0; i < strs.length; ++i) {
            if (strs[i].equals(str)) {
                return i;
            }
        }

        return -1;
    }

    public static int sum(int[] nums) {
        int sum = 0;

        for (int i = 0; i < nums.length; ++i) {
            sum += nums[i];
        }

        return sum;
    }

    public static int product(int[] nums) {
        int product = 1;

        for (int i = 0; i < nums.length; ++i) {
            product *= nums[i];
        }

        return product;
    }

    public static void remove(int[] nums, int index) {
        for (int i = index; i < nums.length - 1; ++i) {
            nums[i] = nums[i + 1];
        }

    }

    public static void insert(int[] nums, int index, int val) {
        for (int i = nums.length - 1; i > index; --i) {
            nums[i + 1] = nums[i];
        }

        nums[index] = val;
    }

    public static void swapElements(int[] nums, int i, int j) {
        int temp = nums[i];
        nums[i] = nums[j];
        nums[j] = temp;
    }

    public static int[] copy(int[] nums) {
        int[] newNums = new int[nums.length];

        for (int i = 0; i < nums.length; ++i) {
            newNums[i] = nums[i];
        }

        return newNums;
    }

    public static double calcSquaredDistance(double[] a1, double[] a2) {
        double distance = 0f;
        for (int e = 0; e < a1.length; e++)
            distance += (a1[e] - a2[e]) * (a1[e] - a2[e]);
        return distance;
    }

    public static double calcDistance(double[] a1, double[] a2) {
        double distance = 0f;
        for (int e = 0; e < a1.length; e++)
            distance += (a1[e] - a2[e]) * (a1[e] - a2[e]);
        return Math.sqrt(distance);
    }

    public static double[] calcDistanceCJ(double[][] indat, double[][] membership, double fuzziness, int N, int c) {
        double[] cj = new double[indat.length];
        double bottom = 0;
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < indat[i].length; j++) {
                cj[i] = indat[i][j] * Math.pow(membership[i][c], fuzziness);
            }
            bottom += Math.pow(membership[i][c], fuzziness);
        }
        for (int i = 0; i < cj.length; i++) {
            cj[i] /= bottom;
        }
        return cj;
    }
}

