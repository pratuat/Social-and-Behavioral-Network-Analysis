package utils;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.it.ItalianAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class IndexUtility {

    public static final CharArraySet STOPWORDS = CharArraySet.copy(Version.LUCENE_41, ItalianAnalyzer.getDefaultStopSet());

    public static IndexSearcher getIndexSearcher(String index_path) throws Exception {

        File path = new File(index_path);
        Directory directory = FSDirectory.open(path);
        IndexReader indexReader = DirectoryReader.open(directory);
        IndexSearcher searcher = new IndexSearcher(indexReader);

        return searcher;
    }

    public static IndexWriter getIndexWriter(String index_path, Analyzer analyzer) throws Exception{

        Directory directory = new SimpleFSDirectory(new File(index_path));

        IndexWriterConfig writerConfig = new IndexWriterConfig(Version.LUCENE_41, analyzer);

        IndexWriter writer = new IndexWriter(directory,  writerConfig);

        return writer;
    }

    public static IndexReader getIndexReader(String index_path) throws Exception{
        Directory directory = FSDirectory.open(new File(index_path));
        IndexReader indexReader = DirectoryReader.open(directory);
        return indexReader;
    }


    public static List<Document> write_to_user_tweet_index(IndexSearcher searcher, TopDocs top_docs, IndexWriter user_tweet_writer) throws Exception{

        List<Document> documents = new ArrayList<>();

        for (ScoreDoc scoreDoc : top_docs.scoreDocs) {
            Document document = searcher.doc(scoreDoc.doc);
            documents.add(document);
        }

        user_tweet_writer.addDocuments(documents);
        user_tweet_writer.commit();
        user_tweet_writer.close();
        return documents;
    }
}
