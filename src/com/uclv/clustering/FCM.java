package com.uclv.clustering;

import org.apache.lucene.document.Document;

import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class FCM {
    private double[][] indat;
    private int nrow, ncol;
    private int maxIterations, numClusters;
    // The FCM additional parameters and membership function values.
    private double fuzziness; // "m"
    private double[][] membership;
    // A small value, if the difference of the cluster "quality" does not
    // changes beyond this value, we consider the clustering converged.
    private double epsilon;
    // The cluster centers.
    private double[][] clusterCenters;
    // A big array with the output data (cluster indexes).
    private int[] assignment;
    private Random generator;
    private DataHolder data;
    private double probClusters;

    //array of docs
    private Document[] docs;
    //amount of docs
    private int N;
    //matrix of docs and terms
    private ArrayList[] words;
    //points to cluster
    private DataPoint[] dataPoints;
    //all terms
    private String[] terms;
    private int[][] fr;
    private int AMOUNT_TERMS = 600;
    private double[] q0;
    private PropertyChangeSupport propertyChange;

    /**
     * Initialize Fuzzy C-means calculations
     *
     * @param docs input documents
     */
    public FCM(Document[] docs, ArrayList[] words, PropertyChangeSupport propertyChange) throws IOException {
        super();
        this.propertyChange = propertyChange;
        this.docs = docs;
        this.words = words;
        N = docs.length;
        fillTerms();
        System.out.println("Fill terms " + this.terms.length);
        reduceDimension();
        System.out.println("Reduce Dimension " + this.terms.length);
        fillDataPoints();
        System.out.println("Fill Points");
        fillDataHolder();
        System.out.println("Fill DataHolder");
        nrow = this.data.getSize();
        ncol = this.data.getDimention();
        numClusters = 3;
        probClusters = 0.68;
        indat = new double[nrow][ncol]; // hold data
        assignment = new int[nrow];
        clusterCenters = new double[numClusters][ncol];
        membership = new double[nrow][numClusters];
        // Input array, values to be read in successively, float
        for (int i = 0; i < nrow; i++) {
            DataPoint dp = this.data.getRow(i);
            for (int i2 = 0; i2 < ncol; i2++) {
                indat[i][i2] = dp.getAttribute(i2);
            }
        }
    }

    public int getAMOUNT_TERMS() {
        return AMOUNT_TERMS;
    }

    public void setAMOUNT_TERMS(int AMOUNT_TERMS) {
        this.AMOUNT_TERMS = AMOUNT_TERMS;
    }

    public PropertyChangeSupport getPropertyChange() {
        return propertyChange;
    }

    public void setPropertyChange(PropertyChangeSupport propertyChange) {
        this.propertyChange = propertyChange;
    }

    private void fillDataHolder() {
        data = new DataHolder();
        for (DataPoint dataPoint : dataPoints) {
            data.add(dataPoint);
        }
    }

    /**
     * Fill all term that appear in all docs
     */
    private void fillTerms() {
        ArrayList t = new ArrayList<>();
        for (ArrayList docs_word : words) {
            for (Object aDocs_word : docs_word) {
                if (!t.contains(aDocs_word)) {
                    t.add(aDocs_word);
                }
            }
        }
        terms = new String[t.size()];
        System.arraycopy(t.toArray(), 0, terms, 0, t.size());
    }

    /**
     * Transform a document to a vector and fills the DataPoints needed to FCM
     */
    private void fillDataPoints() {
        double[][] weight = new double[N][terms.length];
        double sum;
        dataPoints = new DataPoint[N];
        for (int i = 0; i < words.length; i++) {
            sum = 0;
            for (int j = 0; j < terms.length; j++) {
                weight[i][j] = calculateWeight(i, j);
                sum += weight[i][j];
            }
            sum = Math.sqrt(sum);
            for (int j = 0; j < weight[i].length; j++) {
                weight[i][j] /= sum;
            }
            dataPoints[i] = new DataPoint(weight[i], docs[i]);
            propertyChange.firePropertyChange("DOC", i, i + 1);
        }
    }


    /**
     * Calculate the tf-idf weight vector for a document
     */
    private double calculateWeight(int i, int j) {
        //int freq_i_j = frequencyAbs(i, j);
        int freq_i_j = fr[i][j];
        int max = maxFrequency(j);
        double fij = freq_i_j * 1.0 / max;
        int ni = countDocs(j);
        double idfi;
        idfi = Math.log(N * 1.0 / ni);
        return fij * idfi;
    }

    private void reduceDimension() {
        double first = 0;
        double last = 0;
        q0 = new double[terms.length];
        fr = new int[words.length][terms.length];
        for (int i = 0; i < fr.length; i++) {
            Arrays.fill(fr[i], 0);
        }
        for (int i = 0; i < fr.length; i++) {
            for (int j = 0; j < fr[i].length; j++) {
                //if (j < words[i].size()) {
                fr[i][j] = frequencyAbs(i, j);
                //}
            }
        }
        for (int j = 0; j < terms.length; j++) {
            for (int i = 0; i < words.length; i++) {
                first += fr[i][j] * fr[i][j];
                last += fr[i][j];
            }
            last = Math.pow(last, 2);
            q0[j] = first - 1.0 / N * last;
            last = 0;
            first = 0;
        }
        if (AMOUNT_TERMS < terms.length * 0.8) {
            String[] newTerm = new String[AMOUNT_TERMS];
            int[][] newFreq = new int[words.length][AMOUNT_TERMS];

            IndexValue[] index_val = new IndexValue[q0.length];
            for (int i = 0; i < q0.length; i++) {
                index_val[i] = new IndexValue(q0[i], i);
            }
            Arrays.sort(index_val);
            for (int i = 0; i < AMOUNT_TERMS; i++) {
                newTerm[i] = terms[index_val[i].getIndex()];
                for (int j = 0; j < newFreq.length; j++) {
                    newFreq[j][i] = fr[j][index_val[i].getIndex()];
                }
            }
            terms = newTerm;
            fr = newFreq;
        }
    }

    /**
     * Calculate the number of docs that contain term j
     */
    private int countDocs(int j) {
        String t = terms[j];
        int count = 0;
        for (ArrayList docs_word : words) {
            if (docs_word.contains(t)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Calculate the max frequency of a term in all documents
     */
    private int maxFrequency(int j) {
        int max = 0;
        for (int i = 0; i < words.length; i++) {
            int t = frequencyAbs(i, j);
            if (t > max) {
                max = t;
            }
        }
        return max;
    }

    /**
     * Calculate the frecuency of a term j in a document i
     */
    private int frequencyAbs(int i, int j) {
        int cant = 0;
        String term = terms[j];
        for (j = 0; j < words[i].size(); j++) {
            cant += term.equals(words[i].get(j)) ? 1 : 0;
        }
        return cant;
    }

    /**
     * Get the number of clusters
     *
     * @return number of clusters
     */
    public int getClusters() {
        return this.numClusters;
    }

    /**
     * Set number of clusters for calculations.
     *
     * @param N number of Clusters
     *          Set the desired number of clusters.
     */
    public void setClusters(int N) {
        if (N > this.nrow) {
            System.out.println("Too many clusters! Set to number of points");
            N = this.nrow;
        }
        this.numClusters = N;
        membership = new double[nrow][numClusters];
        clusterCenters = new double[numClusters][ncol];
    }

    /**
     * Return cluster membership
     *
     * @return membership matrix (2D)
     */
    public double[][] getMembeship() {
        return this.membership;
    }

    /**
     * Set initial conditions for clustering.
     *
     * @param maxIterations   the maximum number of iterations.
     * @param epsilon         a small value used to verify if clustering has converged.
     * @param fuzziness       the fuzziness (a.k.a. the "m" value)
     * @param numberOfcluster
     */
    public void setOptions(int maxIterations, double epsilon, double fuzziness, double prob, int numberOfcluster) {
        this.maxIterations = maxIterations;
        this.fuzziness = fuzziness;
        this.epsilon = epsilon;
        this.probClusters = prob;
        this.setClusters(numberOfcluster);
    }

    /**
     * Clear
     */
    public void delete() {
        this.numClusters = 0;
        this.maxIterations = 0;
        this.fuzziness = 00;
        this.epsilon = 0;
        this.nrow = 0;
        this.ncol = 0;
        this.indat = null;
        this.clusterCenters = null;
        this.membership = null;
        this.generator = null;
    }

    /**
     * Set probability of associations with each cluster.
     * The default value is 0.68. Does dot affect calculations.
     *
     * @param probClusters probability association.
     */
    public void setProb(double probClusters) {
        this.probClusters = probClusters;
    }

    /**
     * Returns cluster centers
     *
     * @return DataHolder with cluster centers
     */
    public DataHolder getCenters() {
        DataHolder cMeans = new DataHolder();
        for (int i = 0; i < this.numClusters; i++) {
            double[] a = new double[this.ncol];
            System.arraycopy(clusterCenters[i], 0, a, 0, this.ncol);
            DataPoint c = new DataPoint(a, this.ncol);
            cMeans.add(c);
        }

        return cMeans;

    }

    /**
     * Returns cluster assignments of all points.
     * Affected by the method setProb(), which sets
     * association probability (default is 0.68).
     *
     * @return array with cluster assignments
     */
    public int[] getAssignments() {
        return assignment;
    }

    /**
     * Returns the number of points in each cluster.
     * Affected by the method setProb(), which sets
     * association probability (default is 0.68).
     * Look also at getAssignment() and getMembership()
     *
     * @return number of points in each cluster
     */
    public int[] getNumberPoints() {
        int[] a = new int[numClusters];
        for (int c = 0; c < this.numClusters; c++) {
            int nn = 0;
            for (int h = 0; h < this.nrow; h++) {
                if (membership[h][c] > probClusters) nn++;
            }
            a[c] = nn;
        }
        return a;
    }

    /**
     * Runs for the best estimate
     */
    public void runBest() {
        int iter = 1 + (nrow / 2);
        double[] selec = new double[iter];
        for (int j = 0; j < iter; j++) {
            selec[j] = Double.MAX_VALUE;
        }
        int[] clus = new int[iter];

        int N = 0;
        for (int j = 0; j < iter; j++) {
            int nclus = 2 + j;
            setClusters(nclus);
            run();
            selec[j] = getCompactness();
            clus[j] = nclus;
            // stop if it's increasing
            N++;
            if (j > 5) {
                if (selec[j - 1] < selec[j] && selec[j - 2] < selec[j - 1]) {
                    break;
                }
            }
        }
        int ibest = Util.findSmallest(selec, N);
        this.numClusters = clus[ibest];
        run();
    }

    /**
     * Run classic Fuzzy C-Means clustering algorithm: Calculate the cluster
     * centers. Update the membership function. Calculate statistics and repeat
     * from 1 if needed.
     */
    public void run() {
        // Initialize the membership functions randomly.
        generator = new Random(); // easier to debug if a seed is used
        // For each data point (in the membership function table)
        for (int dataPoint_index = 0; dataPoint_index < this.nrow; dataPoint_index++) {
            // For each cluster's membership assign a random value.
            double sum = 0f;
            for (int cluster_index = 0; cluster_index < this.numClusters; cluster_index++) {
                membership[dataPoint_index][cluster_index] = 0.01f + generator.nextDouble();
                sum += membership[dataPoint_index][cluster_index];
            }
            // Normalize so the sum of MFs for a particular data point will be
            // equal to 1.
            for (int cluster_index = 0; cluster_index < this.numClusters; cluster_index++) {
                membership[dataPoint_index][cluster_index] /= sum;
            }
        }
        double lastJ;

        // Calculate the initial objective function just for kicks.
        lastJ = calculateObjectiveFunction();
        // Do all required iterations (until the clustering converges)
        for (int iteration = 0; iteration < maxIterations; iteration++) {
            // Calculate cluster centers from MFs.
            calculateClusterCentersFromMFs();
            // Then calculate the MFs from the cluster centers !
            calculateMFsFromClusterCenters();
            // Then see how our objective function is going.
            double j = calculateObjectiveFunction();
            if (Math.abs(lastJ - j) < epsilon)
                break;
            lastJ = j;
        } // end of the iterations loop.
        // Means that all calculations are done, too.
        for (int dataPoint_index = 0; dataPoint_index < this.nrow; dataPoint_index++) {
            assignment[dataPoint_index] = -1;
        }
        // assume that a cluster has 68% probability

        for (int dataPoint_index = 0; dataPoint_index < this.nrow; dataPoint_index++) {
            int largest = Util.findLargest(membership[dataPoint_index]);
            if (membership[dataPoint_index][largest] > probClusters) {
                assignment[dataPoint_index] = largest;
            }
        }
        // fill this
        for (int i = 0; i < nrow; i++) {
            DataPoint dp = data.getRow(i);
            dp.assignToCluster(assignment[i]);
        }
    }

    /**
     * This method calculates the cluster centers from the membership functions.
     */
    private void calculateClusterCentersFromMFs() {
        double top, bottom;
        // For each band and cluster
        for (int b = 0; b < this.ncol; b++)
            for (int c = 0; c < this.numClusters; c++) {
                // For all data points calculate the top and bottom parts of the
                // equation.
                top = bottom = 0;
                for (int h = 0; h < this.nrow; h++) {
                    // Index will help locate the pixel data position.
                    top += Math.pow(membership[h][c], fuzziness) * indat[h][b];
                    bottom += Math.pow(membership[h][c], fuzziness);
                }
                // Calculate the cluster center.
                clusterCenters[c][b] = top / bottom;
                // Upgrade the position vector (batch).
                // position += width*height;
            }
    }

    /**
     * This method calculates the membership functions from the cluster centers.
     */
    private void calculateMFsFromClusterCenters() {
        double sumTerms;
        // For each cluster and data point
        for (int c = 0; c < this.numClusters; c++) {
            for (int h = 0; h < this.nrow; h++) {
                // Top is the distance of this data point to the cluster being
                // read
                double top = Util.calcDistance(indat[h], clusterCenters[c]);
                // Bottom is the sum of distances from this data point to all
                // clusters.
                sumTerms = 0f;
                for (int ck = 0; ck < numClusters; ck++) {
                    double thisDistance = Util.calcDistance(this.indat[h], clusterCenters[ck]);
                    sumTerms += Math.pow(top / thisDistance, (2f / (fuzziness - 1f)));
                }
                // Then the MF can be calculated as...
                this.membership[h][c] = 1f / sumTerms;
                // Upgrade the position vector (batch).
            }
        }
    }

    /**
     * This method calculates the objective function ("j") which reflects the
     * quality of the clustering.
     */
    private double calculateObjectiveFunction() {
        double j = 0;
        // For all data values and clusters
        for (int datPoint_index = 0; datPoint_index < nrow; datPoint_index++)
            for (int cluster_index = 0; cluster_index < numClusters; cluster_index++) {
                // Calculate the distance between a pixel and a cluster center.
                double distancePixelToCluster = Util.calcDistance(indat[datPoint_index], clusterCenters[cluster_index]);
                j += distancePixelToCluster * Math.pow(membership[datPoint_index][cluster_index], fuzziness);
            }
        return j;
    }

    /**
     * This method returns the estimated size (steps) for this task. The value
     * is, of course, an approximation, just so we will be able to give the user
     * a feedback on the processing time. In this case, the value is calculated
     * as the number of loops in the run() method.
     */
    public long getSize() {
        return (long) maxIterations;
    }

    /**
     * This method returns the Partition Coefficient measure of cluster validity
     * (see Fuzzy Algorithms With Applications to Image Processing and Pattern
     * Recognition, Zheru Chi, Hong Yan, Tuan Pham, World Scientific, pp. 91)
     */
    public double getPartitionCoefficient() {
        double pc = 0;
        // For all data values and clusters
        for (int h = 0; h < this.nrow; h++)
            for (int c = 0; c < this.numClusters; c++)
                pc += membership[h][c] * membership[h][c];
        pc = pc / this.nrow;
        return pc;
    }

    /**
     * This method returns the Partition Entropy measure of cluster validity
     */
    public double getPartitionEntropy() {
        double pe = 0;
        // For all data values and clusters
        for (int h = 0; h < this.nrow; h++)
            for (int c = 0; c < this.numClusters; c++)
                pe += membership[h][c] * Math.log(membership[h][c]);
        pe = -pe / this.nrow;
        return pe;
    }

    /**
     * This method returns the Compactness and Separation measure of cluster
     * validity
     */
    public double getCompactness() {
        double cs = 0;
        // For all data values and clusters
        for (int h = 0; h < this.nrow; h++) {
            for (int c = 0; c < this.numClusters; c++) {
                // Calculate the distance between a pixel and a cluster center.
                double distancePixelToCluster = Util.calcSquaredDistance(this.indat[h], clusterCenters[c]);
                cs += membership[h][c] * membership[h][c] * distancePixelToCluster * distancePixelToCluster;
            }
        }
        cs /= (nrow);
        // Calculate minimum distance between ALL clusters
        double minDist = Double.MAX_VALUE;
        for (int c1 = 0; c1 < this.numClusters - 1; c1++)
            for (int c2 = c1 + 1; c2 < this.numClusters; c2++) {
                double distance = Util.calcSquaredDistance(clusterCenters[c1], clusterCenters[c2]);
                minDist = Math.min(minDist, distance);
            }
        cs = cs / (minDist * minDist);
        return cs;
    }

    public DataHolder getDataHolder() {
        return data;
    }

    public ArrayList[] getWords() {
        return words;
    }

    public void setWords(ArrayList[] words) {
        this.words = words;
    }
}
