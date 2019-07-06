package user_tweet;

import com.AppConfigs;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.util.Version;

import java.util.ArrayList;
import java.util.List;

public class TestMain {
    public static void main(String[] args) {
        IndexSearcher searcher;

        try {
            searcher = IndexUtility.getIndexSearcher(AppConfigs.USER_POLITICIAN_INDEX);



            StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_41);
            QueryParser queryParser = new QueryParser(Version.LUCENE_41, "", analyzer);
            Query query = queryParser.parse("politicianScreenName: \"nurse24it\"");

            TopDocs topDocs = searcher.search(query, Integer.MAX_VALUE);
            // TopDocs topDocs = searcher.search(new MatchAllDocsQuery(), Integer.MAX_VALUE);

            System.out.println(topDocs.scoreDocs.length);

            Document document;

            List<String> iterator = new ArrayList();

            for(ScoreDoc scoreDoc: topDocs.scoreDocs) {
                document = searcher.doc(scoreDoc.doc);
                // System.out.println(document.get("mentioned").split(" "));
                // System.out.println(document.get("userScreenName"));
                System.out.println(String.format("'%s'", document.get("politicianScreenName")));

                //iterator.add(String.format("%s, %s", document.get("userScreenName"), document.get("politicianScreenName")));
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
