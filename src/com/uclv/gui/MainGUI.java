package com.uclv.gui;

import com.bulenkov.darcula.DarculaLookAndFeelInfo;
import com.uclv.clustering.DataHolder;
import com.uclv.clustering.DataPoint;
import com.uclv.clustering.FCM;
import com.uclv.exceptions.WrongParameter;
import com.uclv.lucene.LuceneIndexer;
import org.apache.lucene.document.Document;
import org.apache.tika.Tika;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.*;

import static com.uclv.clustering.Util.showError;

public class MainGUI extends JFrame {
    private final PropertyChangeSupport prt;
    private JPanel root;
    private JTabbedPane tabbedPane1;
    private JTextField address;
    private JButton docsAddress;
    private JTextField clusters;
    private JTextField fuzziness;
    private JTextField iterations;
    private JTextField epsilon;
    private JTextField probability;
    private JButton runButton;
    private JButton runBestButton;
    private JProgressBar progressBar;
    private JTextField stopsworsTexts;
    private JButton stopwords;
    private JLabel amountOfDocs;
    private JLabel numberStop;
    private JComboBox comboBox;
    private JCheckBox box;
    private JButton saveIndexButton;
    private JTextField thresh;

    private FCM fcm;
    private LuceneIndexer lucene;
    private File dir;
    private File stopwds;
    private int status = 0;
    private Document[] docs = null;
    private ArrayList[] words = null;
    private int maxiterations = 1000;
    private int numberOfcluster = 3;
    private double fuzz = 1.7;
    private double eps = 0.001;
    private double prob = 0.2;
    private DataPoint[] points;
    private char analyzer;
    private ExecutorService exe;
    private int threshold;

    public MainGUI() {
        prt = new PropertyChangeSupport(this);
        this.setContentPane(root);
        this.pack();
        this.setResizable(false);
        initListeners();
        initComponents();
        //runT();
    }

    public static void main(String[] args) {

        try {
            UIManager.setLookAndFeel(DarculaLookAndFeelInfo.CLASS_NAME);
        } catch (ClassNotFoundException | InstantiationException | UnsupportedLookAndFeelException | IllegalAccessException e) {
            e.printStackTrace();
        }
        EventQueue.invokeLater(() -> {
            JFrame frame = new JFrame("Fuzzy C-Means Clustering");
            frame.setContentPane(new MainGUI().root);
            frame.setMinimumSize(new Dimension(1000, 400));
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            frame.pack();
            frame.setLocationRelativeTo(frame);
            frame.setVisible(true);

        });
    }

    private void updateTabbedPane() {
//        JTable tab0 = new JTable(new TableModelDocs(centers));
//        JScrollPane s = new JScrollPane(tab0);
//        tabbedPane1.setTitleAt(0, "Centers");
        //tabbedPane1.setTitleAt(0, "Centers");
        DataHolder x = fcm.getDataHolder();
        for (int i = 0; i < numberOfcluster; i++) {
            ArrayList<DataPoint> a = new ArrayList<>();
            for (DataPoint point : points) {
                if (point.getClusterNumber() == i) {
                    a.add(point);
                }
            }
            DataPoint[] b = new DataPoint[a.size()];
            for (int i1 = 0; i1 < a.size(); i1++) {
                b[i1] = a.get(i1);
            }
            JTable tab1 = new JTable(new TableModelDocs(b));
            JScrollPane s1 = new JScrollPane(tab1);
            tabbedPane1.addTab("Cluster " + i + ", Total of docs : " + b.length, s1);
        }
        JTable tab1 = new JTable(new TableModelMFs(x.getArray(), fcm.getMembeship()));
        JScrollPane s1 = new JScrollPane(tab1);
        tabbedPane1.addTab("Membership values", s1);
    }

    private void initComponents() {
        progressBar.setVisible(false);
        LinkedBlockingQueue<Runnable> l = new LinkedBlockingQueue<>();
        exe = Executors.newSingleThreadExecutor();
        ThreadPoolExecutor th = new ThreadPoolExecutor(1, 1, 50000, TimeUnit.MILLISECONDS, l);
        th.prestartAllCoreThreads();
    }

    private void initListeners() {
        clusters.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                clusters.selectAll();
            }

            @Override
            public void focusLost(FocusEvent e) {
                clusters.select(0, 0);
            }
        });
        fuzziness.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                fuzziness.selectAll();
            }

            @Override
            public void focusLost(FocusEvent e) {
                fuzziness.select(0, 0);
            }
        });
        iterations.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                iterations.selectAll();
            }

            @Override
            public void focusLost(FocusEvent e) {
                iterations.select(0, 0);
            }
        });
        epsilon.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                epsilon.selectAll();
            }

            @Override
            public void focusLost(FocusEvent e) {
                epsilon.select(0, 0);
            }
        });
        probability.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                probability.selectAll();
            }

            @Override
            public void focusLost(FocusEvent e) {
                probability.select(0, 0);
            }
        });
        address.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                address.selectAll();
            }

            @Override
            public void focusLost(FocusEvent e) {
                address.select(0, 0);
            }
        });
        thresh.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                thresh.selectAll();
            }

            @Override
            public void focusLost(FocusEvent e) {
                thresh.select(0, 0);
            }
        });

        PropertyChangeListener number_docs_listener = evt -> {
            if (evt.getPropertyName().equals("DOC")) {
                amountOfDocs.setText("Number of docs : " + String.valueOf(evt.getNewValue()));
            }
        };
        PropertyChangeListener number_stop_listener = evt -> {
            if (evt.getPropertyName().equals("STOP")) {
                numberStop.setText("Number of stop words languages : " + String.valueOf(evt.getNewValue()));
            }
        };

        amountOfDocs.addPropertyChangeListener(number_docs_listener);
        numberStop.addPropertyChangeListener(number_stop_listener);

        prt.addPropertyChangeListener(number_docs_listener);
        prt.addPropertyChangeListener(number_stop_listener);

        //Accions
        docsAddress.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                putDirs(address);
            }
        });
        stopwords.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                putDirs(stopsworsTexts);
            }
        });
        runButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                progressBar.setVisible(true);
                Runnable t = () -> {
                    try {
                        restartUI();
                        progressBar.setVisible(true);
                        initOptions();
                        runFCM();
                        progressBar.setVisible(false);
                        updateGUI();
                    } catch (NumberFormatException err) {
                        try {
                            optionChecks(status);
                        } catch (WrongParameter ignored) {

                        }
                        restartUI();
                    } catch (IOException e1) {
                        showError("The directory [" + stopsworsTexts.getText() + "] do not contain any valid stopwords file.", root);
                        restartUI();
                    } catch (IllegalArgumentException e1) {
                        e1.printStackTrace();
                        showError(e1.getMessage(), root);
                        restartUI();
                    } catch (WrongParameter ignored) {

                    } catch (Exception e1) {
                        e1.printStackTrace();
                        showError("The directory [" + address.getText() + "] do not contain any valid document to cluster o do not exist.", root);
                        restartUI();
                    }
                };
                exe.submit(t);
            }
        });
        runBestButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                progressBar.setVisible(true);
            }
        });
        saveIndexButton.addActionListener(e -> {
            if (lucene != null) {
                try {
                    lucene.storeDocs(dir);
                } catch (IOException e1) {
                    showError(e1.getMessage(), root);
                }
            }
        });
    }

    private void runBestFCM() throws IOException {
        fcm = new FCM(docs, words, prt);
        fcm.setOptions(maxiterations, eps, fuzz, prob, numberOfcluster);
        fcm.setAMOUNT_TERMS(threshold);
        fcm.runBest();
        numberOfcluster = fcm.getClusters();
        points = fcm.getDataHolder().getArray();
        updateTextFields();
    }

    private void updateTextFields() {
        clusters.setText(String.valueOf(numberOfcluster));
    }

    private void clearTabbedPane() {
        tabbedPane1.removeAll();
    }

    private void putDirs(JTextField ads) {
        JFileChooser fch = new JFileChooser();

        fch.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        int res = fch.showDialog(null, "Choose docs dir");
        if (res == JFileChooser.APPROVE_OPTION) {
            ads.setText(fch.getSelectedFile().getAbsolutePath());
        }
    }

    private void runFCM() throws IOException {
        fcm = new FCM(docs, words, prt);
        fcm.setAMOUNT_TERMS(threshold);
        fcm.setOptions(maxiterations, eps, fuzz, prob, numberOfcluster);
        fcm.run();
        points = fcm.getDataHolder().getArray();
    }

    private void initOptionsDefault() throws Exception {
        maxiterations = 1000;
        iterations.setText(String.valueOf(maxiterations));
        numberOfcluster = 3;
        clusters.setText(String.valueOf(numberOfcluster));
        fuzz = 1.7;
        fuzziness.setText(String.valueOf(fuzz));
        eps = 0.001;
        epsilon.setText(String.valueOf(eps));
        prob = 0.2;
        probability.setText(String.valueOf(prob));
        threshold = 600;
        thresh.setText(String.valueOf(threshold));
        analyzer = 's';
        dir = new File(address.getText());
        stopwds = new File(stopsworsTexts.getText());
        lucene = new LuceneIndexer(new Tika(), dir, stopwds, analyzer);
        lucene.setPropertyChange(prt);
        lucene.readStopWords();
        docs = lucene.readDocs();
        words = lucene.words();
    }

    private void updateGUI() {
        amountOfDocs.setText("Number of docs : " + docs.length);
        numberStop.setText("Number of stopwords : " + Objects.requireNonNull(stopwds.listFiles((dir, name) -> name.endsWith(".txt") && name.contains("stopwords"))).length);
        updateTabbedPane();
    }

    private void initOptions() throws Exception {
        analyzer = detectAnalyzer();
        dir = new File(address.getText());
        stopwds = new File(stopsworsTexts.getText());
        lucene = new LuceneIndexer(new Tika(), null, stopwds, analyzer);
        lucene.setPropertyChange(prt);
        if (box.isSelected()) {
            lucene.readFromIndex(dir);
        } else {
            lucene.setDocsDir(dir);
        }
        lucene.readStopWords();
        status = 0;
        docs = lucene.getDocs();
        words = lucene.words();
        maxiterations = Integer.parseInt(iterations.getText());
        checkIterations(maxiterations);
        status = 1;
        numberOfcluster = Integer.parseInt(clusters.getText());
        checkClusters(numberOfcluster, docs.length);
        status = 2;
        fuzz = Double.parseDouble(fuzziness.getText());
        checkFuzz(fuzz);
        status = 3;
        eps = Double.parseDouble(epsilon.getText());
        checkEps(eps);
        status = 4;
        prob = Double.parseDouble(probability.getText());
        checkProbability(prob);
        status = 5;
        threshold = Integer.parseInt(thresh.getText());
        checkThresh(threshold);
        status = 6;
    }


    private char detectAnalyzer() {
        Object item = comboBox.getSelectedItem();
        switch (Objects.requireNonNull(item).toString()) {
            case "Standard Analyzer":
                return 's';
            case "StopWord Analyzer":
                return 't';
            case "Simple Analyzer":
                return 'a';
            case "Whitespace Analyzer":
                return 'w';
        }
        return 's';
    }

    private void optionChecks(int status) throws WrongParameter {
        progressBar.setVisible(false);
        String error = "";
        switch (status) {
            case 0:
                error = "Max iterations must be between 1 and " + Integer.MAX_VALUE;
                break;
            case 1:
                error = "The number of clusters must be less than the amount of documents to clustering [" + docs.length + "]\n and must be a value between 1 and " + Integer.MAX_VALUE;
                break;
            case 2:
                error = "The fuzziness value must be a real value grater than 1.";
                break;
            case 3:
                error = "The epsilon value must be a real value greater than 0.";
                break;
            case 4:
                error = "The probability must be between 0 and 1.";
                break;
            case 5:
                error = "The threshold of number of words must be between 1 and " + Integer.MAX_VALUE;
                break;
        }
        showError(error, root);
        throw new WrongParameter();
    }

    private void checkProbability(double prob) throws IllegalArgumentException, WrongParameter {
        if (prob < 0 || prob > 1) {
            optionChecks(4);
        }
    }

    private void checkClusters(int numberOfcluster, int N) throws WrongParameter {
        if (numberOfcluster >= N || numberOfcluster < 1) {
            optionChecks(1);
        }
    }

    private void checkIterations(int maxiterations) throws WrongParameter {
        if (maxiterations < 1) {
            optionChecks(0);
        }
    }

    private void checkFuzz(double fuzz) throws WrongParameter {
        if (fuzz < 1) {
            optionChecks(2);
        }
    }

    private void checkThresh(int threshold) throws WrongParameter {
        if (threshold < 0) {
            optionChecks(5);
        }
    }

    private void checkEps(double eps) throws WrongParameter {
        if (eps < 0) {
            optionChecks(3);
        }
    }

    private void restartUI() {
        progressBar.setVisible(false);
        clearTabbedPane();
        amountOfDocs.setText("Numbers of docs : 0");
        numberStop.setText("Number of stop words languages : 0");
    }

    @Override
    protected void processWindowEvent(WindowEvent e) {
        super.processWindowEvent(e);
        this.pack();
    }
}
