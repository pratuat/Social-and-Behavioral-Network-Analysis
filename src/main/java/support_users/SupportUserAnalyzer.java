package support_users;

import com.AppConfigs;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.CharStream;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.QueryParserBase;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;


public class SupportUserAnalyzer {
    public static final String indexPath = AppConfigs.TWEET_INDEX_PATH;

    public static void main(String[] args){

        String[] politician_ids = {"406869976", "52352494", "2256593570", "48062712"};

        try {
            IndexSearcher searcher = get_tweet_index_searcher();
            Query query = build_query(politician_ids);

            TopDocs topDocs = searcher.search(query, 30);
            List<Document> documents = new ArrayList<>();

            System.out.println(topDocs.totalHits);

            for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                Document document = searcher.doc(scoreDoc.doc);
                documents.add(document);

                System.out.println(String.format("%s, %s, %s, %s , %s | %s",
                        document.get("userId"),
                        document.get("date"),
                        document.get("name"),
                        document.get("screenName"),
                        document.get("mentioned"),
                        document.get("tweetText")
                ));
            }
        } catch (Exception e){
            System.out.println(e.getStackTrace());
        }
    }

    private static Query build_query(String[] politician_ids){
        Query query = null;

        try {

//        Term term = new Term("tweetText", "bastiendemir");
//        Query query = new TermQuery(term);

            StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_41);
//            query = new QueryParser(Version.LUCENE_41, "title", analyzer).parse("mentioned:gdrsophelia");
            query = new QueryParser(Version.LUCENE_41, "title", analyzer).parse("tweetText:\"Derek in Grey's\" OR tweetText:\"CiakGeneration\"");



        } catch (Exception e){
            System.out.print(e.getStackTrace());
        }

        return query;

//        return new MatchAllDocsQuery();
    }

    private static IndexSearcher get_tweet_index_searcher() throws Exception {
        File path = new File(indexPath);

//        Path path = FileSystems.getDefault().getPath(indexPath);
        Directory directory = FSDirectory.open(path);
        IndexReader indexReader = DirectoryReader.open(directory);


        IndexSearcher searcher = new IndexSearcher(indexReader);

        return searcher;
    }
}
