package user_tweet;

import com.AppConfigs;
import com.opencsv.CSVReader;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.it.ItalianAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;


public class UserTweetIndexer {
    public static final String TWEET_INDEX_PATH = AppConfigs.TWEET_INDEX_PATH;
    public static final String USER_TWEET_INDEX_PATH = AppConfigs.USER_TWEET_INDEX_PATH;
    public static final CharArraySet STOPWORDS = CharArraySet.copy(Version.LUCENE_41, ItalianAnalyzer.getDefaultStopSet());

    public static void main(String[] args){
        procesUserTweet();
    }

    private static void procesUserTweet(){
        try {
            List<String> politician_ids = list_politician_ids();

            Query query = build_user_tweet_query(politician_ids);

            IndexSearcher tweet_searcher = get_index_searcher(TWEET_INDEX_PATH);

            TopDocs topDocs = tweet_searcher.search(query, Integer.MAX_VALUE);

            IndexWriter user_tweet_writer = get_index_writer(USER_TWEET_INDEX_PATH);

            List<Document> documents = write_to_user_tweet_index(tweet_searcher, topDocs, user_tweet_writer);
            System.out.println("Total number of documents: " + documents.toArray().length);

            user_tweet_writer.commit();
            user_tweet_writer.close();

            // IndexSearcher user_tweet_searcher = get_index_searcher(USER_TWEET_INDEX_PATH);
            // TopDocs topDocs = user_tweet_searcher.search(new MatchAllDocsQuery(), Integer.MAX_VALUE);

            List<String> userIds = new ArrayList<String>();
            List<String> mentioned = new ArrayList<String>();


            for (ScoreDoc scoreDoc : topDocs.scoreDocs){
                Document doc = tweet_searcher.doc(scoreDoc.doc);

                userIds.add(doc.get("userId"));
                mentioned.add(doc.get("mentioned"));
            }

            System.out.println("Total no of tweets: " + userIds.toArray().length);
            System.out.println("Total no of users: " + userIds.stream().distinct().toArray().length);
        } catch (Exception e){
            System.out.println(e.getStackTrace());
        }
    }

    private static List<Document> write_to_user_tweet_index(IndexSearcher searcher, TopDocs top_docs, IndexWriter user_tweet_writer) throws Exception{
        List<Document> documents = new ArrayList<>();

        for (ScoreDoc scoreDoc : top_docs.scoreDocs) {
            Document document = searcher.doc(scoreDoc.doc);
            documents.add(document);

//            System.out.println(String.format("%s, %s, %s, %s , %s | %s",
//                    document.get("userId"),
//                    document.get("date"),
//                    document.get("name"),
//                    document.get("screenName"),
//                    document.get("mentioned"),
//                    document.get("tweetText")
//            ));
        }

        user_tweet_writer.addDocuments(documents);

        return documents;
    }

    private static List<String> list_politician_ids(){
        List<String> politician_ids = new ArrayList<String>();

        try {
            try (CSVReader csvReader = new CSVReader(new FileReader(AppConfigs.POLITICIANS_LIST_FILE_PATH));) {
                String[] values = null;
                while ((values = csvReader.readNext()) != null) {
                    politician_ids.add(values[0]);
                }
            }

        } catch (Exception e) {
            System.out.println(e.getStackTrace());
        }

        return politician_ids;
    }

    private static Query build_user_tweet_query(List<String> politician_ids){
        Query query = null;

        try {
            StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_41);

            List<String> query_tags = new ArrayList<String>();

            for(String id : politician_ids){
                query_tags.add("mentioned:\"" + id + "\"");
            }

            String query_string = String.join(" OR ", query_tags);
            System.out.println(query_string);

            query = new QueryParser(Version.LUCENE_41, "title", analyzer).parse(query_string);

        } catch (Exception e){
            System.out.print(e.getStackTrace());
        }

        return query;
//        return new MatchAllDocsQuery();
    }

    private static Query build_distinct_user_query(List<String> politician_ids){
        Query query = null;

        try {
            StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_41);

            List<String> query_tags = new ArrayList<String>();

            for(String id : politician_ids){
                query_tags.add("mentioned:\"" + id + "\"");
            }

            String query_string = String.join(" OR ", query_tags);
            System.out.println(query_string);

            query = new QueryParser(Version.LUCENE_41, "title", analyzer).parse(query_string);

        } catch (Exception e){
            System.out.print(e.getStackTrace());
        }

        return query;
//        return new MatchAllDocsQuery();
    }

    private static IndexSearcher get_index_searcher(String index_path) throws Exception {
        File path = new File(index_path);
        Directory directory = FSDirectory.open(path);
        IndexReader indexReader = DirectoryReader.open(directory);
        IndexSearcher searcher = new IndexSearcher(indexReader);

        return searcher;
    }

    private static IndexWriter get_index_writer(String index_path) throws Exception{
        Directory directory = new SimpleFSDirectory(new File(index_path));

        Analyzer analyzer = new ItalianAnalyzer(Version.LUCENE_41, STOPWORDS);
        IndexWriterConfig writerConfig = new IndexWriterConfig(Version.LUCENE_41, analyzer);

        IndexWriter writer = new IndexWriter(directory, writerConfig);

        return writer;
    }

    private static IndexReader get_index_reader(String index_path) throws Exception{
        Directory directory = FSDirectory.open(new File(index_path));
        CheckIndex ci = new CheckIndex(directory);
        IndexReader indexReader = DirectoryReader.open(directory);

        return indexReader;
    }
}
