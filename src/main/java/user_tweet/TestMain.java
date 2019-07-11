package user_tweet;

import com.AppConfigs;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.util.Version;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class TestMain {
    public static void main(String[] args) {
        try {
            testLoadCSV();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void testLoadCSV(){
        List<String[]> userData = FileUtility.loadSortFilterCSV(AppConfigs.USER_TWEET_COUNT, 2, 5, "no");

        System.out.println(userData.toArray().length);
        for (String[] row: userData.subList(0, 15)) {
            System.out.println(row[2] + "\t" + row[5]);
        }
    }

    private static void testUserYesPoliticianIndex() throws Exception {

        IndexSearcher userPoliticianIndexSearcher = IndexUtility.getIndexSearcher(AppConfigs.USER_POLITICIAN_INDEX);



        StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_41);

        //String[] yesPoliticians = FileUtility.loadColumnsFromCSV(AppConfigs.YES_POLITICIANS, 0);
        String[] yesPoliticians = {"BenitaLicata", "antonio_bordin"};
        Query query = UserAnalysisUtility.getPoliticiansQuery("userScreenName", yesPoliticians);
//        QueryParser queryParser = new QueryParser(Version.LUCENE_41, "", analyzer);
//        Query query = queryParser.parse("userScreenName: BenitaLicata");

        TopDocs topDocs = userPoliticianIndexSearcher.search(query, Integer.MAX_VALUE);

        System.out.println("Yes Politicians length: " + topDocs.scoreDocs.length);

//        Document document;
//
//        for(ScoreDoc scoreDoc: topDocs.scoreDocs) {
//            document = userPoliticianIndexSearcher.doc(scoreDoc.doc);
//            System.out.println(String.format("%s", document.get("userScreenName")));
//        }
    }

    private static void testAuthorityPoliticianHashMap(){
        HashMap<String, String> userIntIdScreenNameHashMap= new HashMap<>();

        String[] screenNames = FileUtility.loadColumnsFromCSV(AppConfigs.USER_TWEET_COUNT_TRIMMED, 2);
        String[] screenIntIds = FileUtility.loadColumnsFromCSV(AppConfigs.USER_TWEET_COUNT_TRIMMED, 1);

        for (int i=0; i<screenIntIds.length; i++) {
            System.out.println("UserIntId: " + screenIntIds[i] + "\t" + screenNames[i]);
            userIntIdScreenNameHashMap.put(screenIntIds[i], screenNames[i]);
        }

        String[] topAuthorities = FileUtility.loadColumnsFromCSV(AppConfigs.ALL_USERS_TOP_AUTHORITIES, 0);

        for (String authority: topAuthorities) {
            System.out.println("Authority: " + authority + "\t" + userIntIdScreenNameHashMap.get(authority));
        }
    }

    private static void testIndexes() {
        IndexSearcher searcher;

        try {
            searcher = IndexUtility.getIndexSearcher(AppConfigs.USER_YES_POLITICIAN_INDEX);

            StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_41);
            QueryParser queryParser = new QueryParser(Version.LUCENE_41, "", analyzer);
            Query query = queryParser.parse("politicianScreenName: \"nurse24it\"");
            TopDocs topDocs = searcher.search(query, Integer.MAX_VALUE);

            //TopDocs topDocs = searcher.search(new MatchAllDocsQuery(), Integer.MAX_VALUE);

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
