package com.uclv.lucene;

import com.uclv.clustering.Util;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;

import java.awt.*;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;


public class LuceneIndexer {

    private char analyzer;
    private Tika tika;
    private File working_directory;
    private String[] suffixs = new String[]{".pdf", ".txt", ".docx", ".ppt", ".pptx", ".PDF"};
    private IndexWriter writer;
    private Document[] docs;
    private ArrayList[] words;
    private File stop;
    private PropertyChangeSupport propertyChange;
    private int[] index_stop;
    private String[][] stopwords;

    public LuceneIndexer(Tika tika, File docs, File stop, char analyzer) {
        this.tika = tika;
        this.stop = stop;
        this.analyzer = analyzer;
        this.working_directory = docs;
    }


    public Document readDoc(File file) throws IOException, TikaException {
        Document document = new Document();
        String read = tika.parseToString(file);
        document.add(new Field("name", file.getName(), Store.YES, Field.Index.ANALYZED));
        document.add(new Field("address", file.getAbsolutePath(), Store.YES, Field.Index.ANALYZED));
        Metadata met = new Metadata();
        InputStream is = new FileInputStream(file);
        try {
            tika.parse(is, met);
        } catch (IOException e) {
            Util.showError("An error reading from file [" + file.getAbsolutePath() + "] has taken place.", new Container());
        }
        try {
            document.add(new Field("author", met.get(Metadata.CREATOR), Store.YES, Field.Index.ANALYZED));
        } catch (NullPointerException ex) {
            document.add(new Field("author", "unknown", Store.YES, Field.Index.ANALYZED));
        }
        try {
            document.add(new Field("title", met.get(Metadata.TITLE), Store.YES, Field.Index.ANALYZED));
        } catch (NullPointerException ex) {
            document.add(new Field("title", "unknown", Store.YES, Field.Index.ANALYZED));
        }
        try {
            document.add(new Field("date", met.get(Metadata.DATE), Store.YES, Field.Index.ANALYZED));
        } catch (NullPointerException ex) {
            document.add(new Field("date", "unknown", Store.YES, Field.Index.ANALYZED));
        }
        document.add(new Field("content", read, Store.YES, Field.Index.ANALYZED));
        return document;
    }

    public Document[] readDocs() throws Exception {
        ArrayList<File> files = new ArrayList<>();
        if (!working_directory.exists()) {
            throw new Exception("Directory [" + working_directory.getAbsolutePath() + "] do not exists.");
        }
        if (working_directory != null) {
            if (working_directory.isDirectory()) {
                for (int i = 0; i < suffixs.length; i++) {
                    int finalI = i;
                    Collections.addAll(files, working_directory.listFiles((dir, name) -> name.endsWith(suffixs[finalI])));
                    this.propertyChange.firePropertyChange("DOCS", 0, files.size());
                }
            } else {
                throw new Exception("Working directory not a file");
            }
        } else {
            throw new Exception("No working directory to index");
        }
        if (files.size() == 0) {
            throw new Exception();
        }
        Document[] docs = new Document[files.size()];

        for (int i = 0; i < docs.length; i++) {
            docs[i] = readDoc(files.get(i));
        }
        this.docs = docs;
        return docs;
    }

    public void storeDocs(File file) throws IOException {
        writer = new IndexWriter(file, Util.getAnalyzer(analyzer, null), true);
        for (int i = 0; i < docs.length; i++) {
            writer.addDocument(docs[i]);
        }
        writer.commit();
        writer.close();
    }

    public void readFromIndex(File f) throws Exception {
        IndexReader reader = IndexReader.open(f);
        if (!IndexReader.indexExists(f)) {
            throw new Exception("No valid stored index");
        }
        docs = new Document[reader.numDocs()];
        for (int i = 0; i < reader.numDocs(); i++) {
            docs[i] = reader.document(i);
            this.propertyChange.firePropertyChange("DOCS", 0, i + 1);
        }
    }

    public void readStopWords() throws Exception {
        this.stopwords = Util.stopWordsFile(stop);
        this.propertyChange.firePropertyChange("STOP", 0, this.stopwords.length);
    }

    public ArrayList[] words() throws IOException {
        words = new ArrayList[docs.length];
        index_stop = new int[docs.length];
        for (int i = 0; i < words.length; i++) {
            index_stop[i] = Util.detectLanguage(docs[i], stopwords);
            words[i] = Util.tokens(docs[i], analyzer, stopwords[index_stop[i]]);
        }
        return words;
    }

    public File getDocsDir() {
        return working_directory;
    }

    public void setDocsDir(File docsDir) {
        this.working_directory = docsDir;
    }

    public Document[] getDocs() throws Exception {
        if (docs == null) {
            return readDocs();
        }
        return docs;
    }

    public PropertyChangeSupport getPropertyChange() {
        return propertyChange;
    }

    public void setPropertyChange(PropertyChangeSupport propertyChange) {
        this.propertyChange = propertyChange;
    }
}
