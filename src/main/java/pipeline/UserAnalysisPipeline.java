package pipeline;

import it.stilo.g.structures.LongIntDict;
import it.stilo.g.structures.WeightedDirectedGraph;
import it.stilo.g.util.GraphReader;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryparser.classic.ParseException;
import java.io.*;
import java.util.ArrayList;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.util.Version;
import twitter4j.JSONException;
import utils.*;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import static utils.IndexUtility.*;

public class UserAnalysisPipeline {

    public static IndexSearcher allTweetIndexSearcher;
    public static IndexSearcher userTweetIndexSearcher;
    public static IndexSearcher userPoliticianIndexSearcher;
    public static HashMap<Long, String> userIntIdScreenNameHashMap= new HashMap<>();

    public static void runUserAnalysisPipeline()  throws IOException, JSONException, ParseException, Exception {

        buildUserTweetIndex();
        buildUserPoliticianIndex();
        buildYesNoUserPoliticianIndex();
        generateUserPoliticianMap();
        generateUserInducedSubGraphs();

    }

    /**
     * Generates Largest Connected Component of user sub-graphs, compute HITS score and KPP-NEG score on user sub-graphs
     * and save them to file
     * @param topUsers List of user sets to perform graph analysis.
     * @throws IOException
     * @throws InterruptedException
     */
    public static void generateUserInducedSubGraphs() throws IOException, InterruptedException {
        System.out.println("=====================================");
        System.out.println("Generating user induced subgraph ...");

        List[] topUsers = shortListUsers();
        List<String> topUsersByTweetCount = topUsers[0];
        List<String> topYesUsersByTweetCount = topUsers[1];
        List<String> topNoUsersByTweetCount = topUsers[2];

        // ... LOAD USER_ID-SCREEN_NAME HASH-MAP ... //
        /**
         * load user_id - user_screen_name hash-map
         */
        String[] screenNames = FileUtility.loadColumnsFromCSV(AppConfigs.USER_TWEET_COUNT, 1);
        String[] longIds = FileUtility.loadColumnsFromCSV(AppConfigs.USER_TWEET_COUNT, 0);

        for (int i = 0; i < longIds.length; i++) {
            userIntIdScreenNameHashMap.put(Long.parseLong(longIds[i]), screenNames[i]);
        }

        /**
         * load original user graph
         */
        System.out.println("Loading graph ...");

        int graphSize = 16815933;
        LongIntDict mapLong2Int = new LongIntDict();
        WeightedDirectedGraph graph = new WeightedDirectedGraph(graphSize + 1);
        GraphReader.readGraphLong2IntRemap(graph, AppConfigs.USER_GRAPH_PATH, mapLong2Int, false);

        System.out.println("Loading graph completed ...");

        System.out.println("-------------------------------------");
        System.out.println("Computing all user authorities ...");

        // load users M, M_Y, M_N
        int[] topUsersIds = UserAnalysisUtility.getIntMappedUserIds(topUsersByTweetCount.stream().toArray(String[]::new), mapLong2Int);

        /**
         * Retrieve largest connected component S(M) of sub-graph induced by user M
         */
        MappedWeightedGraph lccGraph = GraphAnalysis.extractLargestCC(graph, topUsersIds, mapLong2Int, true);

        /**
         * Compute top 2000 hub and authority users from S(M) sub-graph
         */
        GraphAnalysis.saveTopKAuthorities(lccGraph, mapLong2Int, 2000, AppConfigs.ALL_USERS_TOP_AUTHORITIES, userIntIdScreenNameHashMap);

        System.out.println("Computing all user authorities completed ...");

        // ... EXTRACT LARGEST CONNECTED COMPONENT AND RETRIEVE TOP AUTHORITY [ YES USERS ]... //

        System.out.println("-------------------------------------");
        System.out.println("Computing yes user authorities ...");

        int[] topYesUsersIds = UserAnalysisUtility.getIntMappedUserIds(topYesUsersByTweetCount.stream().toArray(String[]::new), mapLong2Int);
        /**
         * Retrieve largest connected component of sub-graph induced by YES user
         */
        MappedWeightedGraph yesLccGraph = GraphAnalysis.extractLargestCC(graph, topYesUsersIds, mapLong2Int, false);

        /**
         * Compute top 1000 hub and authority users from YES sub-graph
         */
        GraphAnalysis.saveTopKAuthorities(yesLccGraph, mapLong2Int, 1000, AppConfigs.YES_USERS_TOP_AUTHORITIES, userIntIdScreenNameHashMap);

        System.out.println("Computing yes user authorities completed ...");

        // ... EXTRACT LARGEST CONNECTED COMPONENT AND RETRIEVE TOP AUTHORITY [ NO USERS ]... //

        System.out.println("-------------------------------------");
        System.out.println("Computing no user authorities ...");

        int[] topNoUsersIds = UserAnalysisUtility.getIntMappedUserIds(topNoUsersByTweetCount.stream().toArray(String[]::new), mapLong2Int);
        /**
         * Retrieve largest connected component of sub-graph induced by NO user
         */
        MappedWeightedGraph noLccGraph = GraphAnalysis.extractLargestCC(graph, topNoUsersIds, mapLong2Int, false);

        /**
         * Compute top 1000 hub and authority users from NO sub-graph
         */
        GraphAnalysis.saveTopKAuthorities(noLccGraph, mapLong2Int, 1000, AppConfigs.NO_USERS_TOP_AUTHORITIES, userIntIdScreenNameHashMap);

        System.out.println("Computing no user authorities completed ...");

        System.out.println("----------------------------------");

        System.out.println("Computing KPP-NEG");

        GraphAnalysis.runKPPNEGAnalysis(graph, mapLong2Int, topYesUsersIds, AppConfigs.YES_USERS_500KPP, userIntIdScreenNameHashMap);
        GraphAnalysis.runKPPNEGAnalysis(graph, mapLong2Int, topNoUsersIds, AppConfigs.NO_USERS_500KPP, userIntIdScreenNameHashMap);

        System.out.println("Computing KPP-NEG completed ...");
    }

    // ... CREATE USER TWEET COUNT (ALL , YES/NO POLITICIANS) LIST ... //

    /**
     * Counts number of mentions for yes/no politicians and classify users to yes/no group
     * @throws Exception
     */
    public static void generateUserPoliticianMap() throws Exception {

        System.out.println("==================================");
        System.out.println("Generating user-politician map ...");

        int yesTopDocs;
        int noTopDocs;

        Query query;
        StandardAnalyzer analyzer;
        QueryParser queryParser;
        String screenName;
        String id;


        IndexSearcher userYesPoliticianIndexSearcher = IndexUtility.getIndexSearcher(AppConfigs.USER_YES_POLITICIAN_INDEX);
        IndexSearcher userNoPoliticianIndexSearcher = IndexUtility.getIndexSearcher(AppConfigs.USER_NO_POLITICIAN_INDEX);

        // load all user screen names from file.
        String[] allUserIds = FileUtility.loadColumnsFromCSV(AppConfigs.ALL_USER, 0);
        String[] allUserScreenNames = FileUtility.loadColumnsFromCSV(AppConfigs.ALL_USER, 1);

        analyzer = new StandardAnalyzer(Version.LUCENE_41);
        queryParser = new QueryParser(Version.LUCENE_41, "", analyzer);

        List<String> userTweetCount = new ArrayList<>();
        List<String> yesUserTweetCount = new ArrayList<>();
        List<String> noUserTweetCount = new ArrayList<>();

        int len = allUserScreenNames.length;
        String row;

        for (int i=0; i < len; i++) {

            System.out.println("Processing user: " + i + "/" + len);

            id = allUserIds[i];
            screenName = allUserScreenNames[i];
            query = queryParser.parse("userScreenName: " + screenName);

            // count user's mention of yes and no politicians
            yesTopDocs = userYesPoliticianIndexSearcher.search(query, Integer.MAX_VALUE).scoreDocs.length;
            noTopDocs = userNoPoliticianIndexSearcher.search(query, Integer.MAX_VALUE).scoreDocs.length;

            if (yesTopDocs > noTopDocs) {
                row = id + ", " + screenName + ", " + (yesTopDocs + noTopDocs) + ", " + yesTopDocs + ", " + noTopDocs + ", yes";
                yesUserTweetCount.add(row);
                userTweetCount.add(row);
            }
            else if (yesTopDocs < noTopDocs) {
                row = id + ", " + screenName + ", " + (yesTopDocs + noTopDocs) + ", " + yesTopDocs + ", " + noTopDocs  + ", no";
                noUserTweetCount.add(row);
                userTweetCount.add(row);
            }
        }

        FileUtility.writeToFile(AppConfigs.USER_TWEET_COUNT, userTweetCount.toArray());
        FileUtility.writeToFile(AppConfigs.M_YES, yesUserTweetCount.toArray());
        FileUtility.writeToFile(AppConfigs.M_NO, noUserTweetCount.toArray());

        System.out.println("----------------------------------");
    }

    // ... CREATE USER-YES_POLITICIAN MAP INDEX ... //
    // ... CREATE USER-NO_POLITICIAN MAP INDEX ... //

    /**
     * Creates separate user_politician map indexes for yes and no politicians
     * @throws Exception
     */
    public static void buildYesNoUserPoliticianIndex() throws Exception {

        StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_41);
        List<Document> documents;

        // ... CREATE USER-YES_POLITICIAN MAP INDEX ... //

        String[] yesPoliticians = FileUtility.loadColumnsFromCSV(AppConfigs.YES_POLITICIANS, 0);
        Query query = UserAnalysisUtility.getPoliticiansQuery("politicianScreenName", yesPoliticians);
        TopDocs topDocs = userPoliticianIndexSearcher.search(query, Integer.MAX_VALUE);
        documents = new ArrayList<>();

        System.out.println("no. of yes politicians: " + yesPoliticians.length);
        System.out.println("yes politicians mentions: " + topDocs.scoreDocs.length);

        for (ScoreDoc scoreDoc: topDocs.scoreDocs) {
            documents.add(userPoliticianIndexSearcher.doc(scoreDoc.doc));
        }

        IndexWriter userYesPoliticianIndexWriter = IndexUtility.getIndexWriter(AppConfigs.USER_YES_POLITICIAN_INDEX, analyzer);
        userYesPoliticianIndexWriter.addDocuments(documents);
        userYesPoliticianIndexWriter.commit();
        userYesPoliticianIndexWriter.close();

        // ... CREATE USER-NO_POLITICIAN MAP INDEX ... //

        String[] noPoliticians = FileUtility.loadColumnsFromCSV(AppConfigs.NO_POLITICIANS, 0);
        query = UserAnalysisUtility.getPoliticiansQuery("politicianScreenName", noPoliticians);
        topDocs = userPoliticianIndexSearcher.search(query, Integer.MAX_VALUE);
        documents = new ArrayList<>();

        System.out.println("no. of no politicians: " + noPoliticians.length);
        System.out.println("no politicians mentions: " + topDocs.scoreDocs.length);

        for (ScoreDoc scoreDoc: topDocs.scoreDocs) {
            documents.add(userPoliticianIndexSearcher.doc(scoreDoc.doc));
        }

        IndexWriter userNoPoliticianIndexWriter = IndexUtility.getIndexWriter(AppConfigs.USER_NO_POLITICIAN_INDEX, analyzer);
        userNoPoliticianIndexWriter.addDocuments(documents);
        userNoPoliticianIndexWriter.commit();
        userNoPoliticianIndexWriter.close();

    }

    // ... CREATE USER-POLITICIAN INDEX ... //
    /**
     * Creates user_politician map indexes all politicians
     * @throws Exception
     */
    public static void buildUserPoliticianIndex() throws Exception {

        System.out.println("================================");
        System.out.println("Building user-politician map ...");

        TopDocs topDocs = userTweetIndexSearcher.search(new MatchAllDocsQuery(), Integer.MAX_VALUE);

        Document newDocument;

        Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_41);
        IndexWriter userPoliticianMapIndexWriter = IndexUtility.getIndexWriter(AppConfigs.USER_POLITICIAN_INDEX, analyzer);
        Document document;
        int counter = 0;

        // ... CREATE USER-POLITICIAN INDEX ... //

        for (ScoreDoc scoreDoc: topDocs.scoreDocs) {
            document = userTweetIndexSearcher.doc(scoreDoc.doc);
            String user = document.get("screenName");

            for (String politician: document.get("mentioned").split(" ")) {
                newDocument = new Document();
                newDocument.add(new TextField("userScreenName", user.trim(), Field.Store.YES));
                newDocument.add(new TextField("politicianScreenName", politician.trim(), Field.Store.YES));
                userPoliticianMapIndexWriter.addDocument(newDocument);
                counter++;
            }

        }

        System.out.println("No. of user-politician map: " + counter);

        userPoliticianMapIndexWriter.commit();
        userPoliticianMapIndexWriter.close();

        userPoliticianIndexSearcher = IndexUtility.getIndexSearcher(AppConfigs.USER_POLITICIAN_INDEX);

        System.out.println("--------------------------------");
    }

    // ... WRITE USER TWEETS TO INDEX ... //
    // ... WRITE UNIQUE USER IDENTIFIERS TO FILE ... //

    /**
     * Build tweet indexes for tweets which mention all politicians, also short-list those users and saves to csv file
     * @throws Exception
     */
    public static void buildUserTweetIndex() throws Exception {

        System.out.println("=============================");
        System.out.println("Building user tweet index ...");

        allTweetIndexSearcher = getIndexSearcher(AppConfigs.ALL_TWEET_INDEX);

        String[] allPoliticians = FileUtility.listPoliticianIds(AppConfigs.ALL_POLITICIANS).stream().toArray(String[]::new);
        System.out.println("No. of Politicians: " + allPoliticians.length);

        Query query = UserAnalysisUtility.getPoliticiansQuery("mentioned", allPoliticians);

        TopDocs topDocs = allTweetIndexSearcher.search(query, Integer.MAX_VALUE);
        System.out.println("No. of tweet documents: " + topDocs.scoreDocs.length);

        List<Document> documents = new ArrayList<>();
        List<String> users = new ArrayList();

        Document document;

        // ... WRITE USER TWEETS TO INDEX ... //

        for (ScoreDoc scoreDoc: topDocs.scoreDocs) {
            document = allTweetIndexSearcher.doc(scoreDoc.doc);
            users.add(String.format("%s, %s", document.get("userId"), document.get("screenName")));
            documents.add(document);
        }

        String[] unique_users = users.stream().distinct().toArray(String[]::new);
        System.out.println("No. of users: " + unique_users.length);

        Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_41);
        IndexWriter userTweetIndexWriter = IndexUtility.getIndexWriter(AppConfigs.USER_TWEET_INDEX, analyzer);
        userTweetIndexWriter.addDocuments(documents);
        userTweetIndexWriter.commit();
        userTweetIndexWriter.close();

        // ... WRITE UNIQUE USER IDENTIFIERS TO FILE ... //

        FileUtility.writeToFile(AppConfigs.ALL_USER, unique_users);

        userTweetIndexSearcher = getIndexSearcher(AppConfigs.USER_TWEET_INDEX);

        System.out.println("-----------------------------");
    }

    public static List[] shortListUsers() {

        List<String> topUsersByTweetCount = FileUtility.loadSortCSV(AppConfigs.USER_TWEET_COUNT, 2).
                stream().
                map((row) -> row[0]).
                collect(Collectors.toList());

        List<String> topYesUsersByTweetCount = FileUtility.loadSortFilterCSV(AppConfigs.USER_TWEET_COUNT, 2, 5, "yes").
                stream().
                map((row) -> row[0]).
                collect(Collectors.toList());

        List<String> topNoUsersByTweetCount = FileUtility.loadSortFilterCSV(AppConfigs.USER_TWEET_COUNT, 2, 5, "no").
                stream().
                map((row) -> row[0]).
                collect(Collectors.toList());

        List[] list = {topUsersByTweetCount, topYesUsersByTweetCount, topNoUsersByTweetCount};

        return list;
    }
}

class UserAnalysisUtility {

    public static Query getPoliticiansQuery(String keyword, String[] politicians) {

        Query query = null;

        try {
            StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_41);

            List<String> query_tags = new ArrayList<String>();

            for(String name : politicians){
                query_tags.add(keyword + ":\"" + name + "\"");
            }

            String query_string = String.join(" OR ", query_tags);
            // System.out.println(query_string);

            query = new QueryParser(Version.LUCENE_41, "title", analyzer).parse(query_string);

        } catch (Exception e){
            System.out.print(e.getStackTrace());
        }

        return query;
    }

    public static int[] getIntMappedUserIds(String[] userIds, LongIntDict longIntDict) {

        long id;
        int[] intIds = new int[userIds.length];

        for (int i=0; i<userIds.length; i++) {
            id = Long.parseLong(userIds[i]);
            intIds[i] = longIntDict.get(id);
        }

        return intIds;
    }
}


