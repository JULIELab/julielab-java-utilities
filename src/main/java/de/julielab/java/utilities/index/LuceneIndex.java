package de.julielab.java.utilities.index;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.NIOFSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Stream;

public class LuceneIndex implements StringIndex {
    private final static Logger log = LoggerFactory.getLogger(LuceneIndex.class);
    private final FSDirectory directory;
    private final String indexDirectory;
    private IndexWriter iw;
    private IndexSearcher searcher;
    private boolean retrieveAllKeys = true;
    private int firstRetrievalPage = 10;

    public LuceneIndex(String indexDirectory) {
        this.indexDirectory = indexDirectory;
        try {
            Path lucene = Path.of(indexDirectory);
            File directoryFile = lucene.toFile();
            boolean indexExists = directoryFile.exists() && directoryFile.isDirectory() && directoryFile.list().length != 0;
            directory = NIOFSDirectory.open(lucene);
            // Do not open a writer to an existing index. This causes locking issues when starting multiple
            // pipelines in parallel.
            // Of course, the first pipeline still needs to create the index, so this must be a one-time effort
            // that has to be completed before the other pipelines are started.
            if (!indexExists) {
                log.debug("Creating index writer for index directory {}.", indexDirectory);
                IndexWriterConfig iwc = new IndexWriterConfig();
                iw = new IndexWriter(directory, iwc);
            } else {
                log.debug("Index directory {} already exists.", indexDirectory);
            }
        } catch (IOException e) {
            log.error("could not initialize Lucene index", e);
            throw new IllegalStateException(e);
        }
    }

    public String getIndexDirectory() {
        return indexDirectory;
    }

    public boolean isRetrieveAllKeys() {
        return retrieveAllKeys;
    }

    public void setRetrieveAllKeys(boolean retrieveAllKeys) {
        this.retrieveAllKeys = retrieveAllKeys;
    }

    public int getFirstRetrievalPage() {
        return firstRetrievalPage;
    }

    public void setFirstRetrievalPage(int firstRetrievalPage) {
        this.firstRetrievalPage = firstRetrievalPage;
    }

    @Override
    public String get(String key) {
        TermQuery tq = new TermQuery(new Term("key", key));
        BooleanQuery.Builder b = new BooleanQuery.Builder();
        b.add(tq, BooleanClause.Occur.FILTER);
        BooleanQuery q = b.build();
        try {
            if (searcher == null)
                throw new IllegalStateException("Call 'open()' on the index object before trying to access its contents.");
            TopDocs topDocs = searcher.search(q, 1);
            if (topDocs.scoreDocs.length > 0) {
                Document doc = searcher.getIndexReader().document(topDocs.scoreDocs[0].doc);
                return doc.getField("value").stringValue();
            }
        } catch (IOException e) {
            log.error("Could not retrieve results for '{}' in Lucene index.", key, e);
            throw new IllegalStateException(e);
        }
        return null;
    }

    @Override
    public String[] getArray(String key) {
        TermQuery tq = new TermQuery(new Term("key", key));
        BooleanQuery.Builder b = new BooleanQuery.Builder();
        b.add(tq, BooleanClause.Occur.FILTER);
        BooleanQuery q = b.build();
        try {
            if (searcher == null)
                throw new IllegalStateException("Call 'open()' on the index object before trying to access its contents.");
            TopDocs topDocs = searcher.search(q, firstRetrievalPage);
            Stream<String> values = Stream.empty();
            if (topDocs.scoreDocs.length > 0) {
                for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                    Document doc = searcher.getIndexReader().document(scoreDoc.doc);
                    values = Stream.concat(values, Arrays.stream(doc.getFields("value")).map(IndexableField::stringValue));
                }
            }
            if (retrieveAllKeys && topDocs.totalHits.value > firstRetrievalPage) {
                log.debug("There are more hits for key {} than were retrieved in the first page of size {}. The retrieval of all values is enabled and the remaining values are obtained now.", key, firstRetrievalPage);
                TopDocs remainingValues = searcher.searchAfter(topDocs.scoreDocs[topDocs.scoreDocs.length - 1], q, (int) (topDocs.totalHits.value - firstRetrievalPage));
                for (ScoreDoc scoreDoc : remainingValues.scoreDocs) {
                    Document doc = searcher.getIndexReader().document(scoreDoc.doc);
                    values = Stream.concat(values, Arrays.stream(doc.getFields("value")).map(IndexableField::stringValue));
                }
            }
            final String[] ret = values.toArray(String[]::new);
            return ret.length > 0 ? ret : null;
        } catch (IOException e) {
            log.error("Could not retrieve results for '{}' in Lucene index.", key, e);
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void put(String key, String value) {
        Field keyField = new StringField("key", key, Field.Store.NO);
        Field valueField = new StoredField("value", value);
        Document doc = new Document();
        doc.add(keyField);
        doc.add(valueField);
        try {
            iw.addDocument(doc);
        } catch (IOException e) {
            log.error("Could not index key-value pair {}:{} with Lucene", key, value, e);
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void put(String key, String[] value) {
        Field keyField = new StringField("key", key, Field.Store.NO);
        Document doc = new Document();
        doc.add(keyField);
        for (var v : value)
            doc.add(new StoredField("value", v));
        try {
            iw.addDocument(doc);
        } catch (IOException e) {
            log.error("Could not index key-value pair {}:{} with Lucene", key, value, e);
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void commit() {
        try {
            iw.commit();
        } catch (IOException e) {
            log.error("Could not commit Lucene index", e);
            throw new IllegalStateException(e);
        }
    }

    @Override
    public boolean requiresExplicitCommit() {
        return true;
    }

    @Override
    public void close() {
        try {
            if (searcher != null) {
                searcher.getIndexReader().close();
                searcher = null;
            }
            if (iw != null) {
                iw.close();
                iw = null;
            }
        } catch (IOException e) {
            log.error("Could not close Lucene index reader.", e);
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void open() {
        try {
            searcher = new IndexSearcher(DirectoryReader.open(directory));
        } catch (IOException e) {
            log.error("Could not open Lucene index searcher.", e);
            if (e.getMessage() != null && e.getMessage().contains("no segments* file"))
                throw new IllegalStateException("No index files found in directory " + directory.getDirectory() + ". Before the index can be used it needs to be committed via the 'commit()' method.", e);
            throw new IllegalStateException(e);
        }
    }

    @Override
    public int size() {
        if (iw != null && iw.isOpen())
            return iw.getDocStats().numDocs;
        else if (searcher != null)
            return searcher.getIndexReader().numDocs();
        return 0;
    }
}
