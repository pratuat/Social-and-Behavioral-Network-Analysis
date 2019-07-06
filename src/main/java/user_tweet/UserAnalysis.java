package user_tweet;

import analysis.GraphAnalysis;
import com.AppConfigs;
import io.ReadFile;
import io.TxtUtils;
import it.stilo.g.structures.LongIntDict;
import it.stilo.g.structures.WeightedDirectedGraph;
import it.stilo.g.util.GraphReader;
import org.apache.commons.lang3.tuple.ImmutablePair;
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
import structure.MappedWeightedGraph;
import twitter4j.JSONException;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.zip.GZIPInputStream;

import static io.TxtUtils.txtToList;
import static user_tweet.IndexUtility.*;

public class UserAnalysis {

    public static IndexSearcher allTweetIndexSearcher;
    public static IndexSearcher userTweetIndexSearcher;

    public static String[] allUsers;

    static {
        try {
            allTweetIndexSearcher = getIndexSearcher(AppConfigs.ALL_TWEET_INDEX);
            userTweetIndexSearcher = getIndexSearcher(AppConfigs.USER_TWEET_INDEX);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args)  throws IOException, JSONException, ParseException, Exception {

        if (false) {
            buildUserTweetIndex();
            buildUserPoliticianMapIndex();
        }

        if (true) {
            generateUserPoliticianMap();
        }
    }

    public static void mainn(String[] args)  throws IOException, JSONException, ParseException, Exception {

        boolean build_user_tweet_index = false;
        boolean loadGraph = true;
        boolean calculateTopAuthorities = true;
        boolean printAuthorities = true;
        boolean calculateKplayers = true;
        boolean useCache = false;


        // Build tweet index with mentions of listed politicians
        if (build_user_tweet_index) {
            buildUserTweetIndex();
        }

        ArrayList<String> usersList;
        int[] nodes;
        Document[] docs;
        long id;
        int i;

        String[] prefixYesNo = {"yes", "no"};

        WeightedDirectedGraph g = null;
        LongIntDict mapLong2Int = new LongIntDict();
        int graphSize = 0;
        String graphFilename = "";

        // loadGraph = false;
        if (loadGraph) {
            System.out.println(">> Start: Load graph.");
            graphSize = 16815933;
            g = new WeightedDirectedGraph(graphSize + 1);
            GraphReader.readGraphLong2IntRemap(g, AppConfigs.RESOURCES_DIR + "Official_SBN-ITA-2016-Net.gz", mapLong2Int, false);
            System.out.println(">> Success: Load graph.");
        }

        if (calculateTopAuthorities) {
            System.out.println(">> Start: Calculate Top Authorities.");
            LinkedHashSet<Integer> users = GraphAnalysis.getUsersMentionedPolitician(useCache, mapLong2Int);

            System.out.println("User size: " + users.size());

           // convert the set to array of int, needed by the method "SubGraph.extract"
            int[] usersIDs = new int[users.size()];
            i = 0;

            for (Integer userId : users) {
                usersIDs[i] = userId;
                i++;
            }

            MappedWeightedGraph gmap = GraphAnalysis.extractLargestCCofM(g, usersIDs, mapLong2Int);
            GraphAnalysis.saveTopKAuthorities(gmap, users, mapLong2Int, 1000, useCache);
            TweetsOpinion.saveTop500HubnessAuthorities(gmap, users, mapLong2Int, 3);
            TweetsOpinion.hubnessGraph13();
            System.out.println(">> Success: Calculate Top Authorities.");
        }

        if (printAuthorities) {
            System.out.println(">> Start: Print top authorities.");
            GraphAnalysis.printAuthorities(mapLong2Int.getInverted());
            System.out.println(">> Success: Print top authorities.");
        }

        if (calculateKplayers) {
            System.out.println(">> Start: Calculate K playes.");
            graphFilename = AppConfigs.RESOURCES_DIR + "graph_largest_cc_of_M.gz";
            GZIPInputStream gzipIS = new GZIPInputStream(new FileInputStream(graphFilename));
            graphSize = (int) ReadFile.getLineCount(gzipIS);
            g = new WeightedDirectedGraph(graphSize + 1);
            mapLong2Int = new LongIntDict();

            for (String supportType : prefixYesNo) {
                usersList = txtToList(AppConfigs.RESOURCES_DIR + supportType + "_M.txt", String.class); // retrieve the users names
                nodes = new int[usersList.size()];
                i = 0;
                // from the users names to their Twitter ID, then to their respective position in the graph (int)
                // name -> twitterID -> position in the graph
//                for (String username : usersList) {
//                    docs = allTweetIndexSearcher.searchByField("name", username, 1);
//
//                    if (docs != null) {
//                        id = Long.parseLong(docs[0].get("userId"));  //read just the first resulting doc
//                        nodes[i] = mapLong2Int.get(id);  // retrieve the twitter ID (long) and covert to int (the position in the graph)
//                    }
//                    i++;
//                }

                List<ImmutablePair> brokersUsername = GraphAnalysis.getTopKPlayers(g, nodes, mapLong2Int, 500, 1);
                // save the first topk authorities
                TxtUtils.iterableToTxt(AppConfigs.RESOURCES_DIR + supportType + "_top_k_players.txt", brokersUsername);
            }

            System.out.println(">> Success: Calculate K playes.");
        }

    }

    public static void generateUserPoliticianMap() throws Exception {

        System.out.println("==================================");
        System.out.println("Generating user-politician map ...");

        Document document;
        TopDocs topDocs;
        Query query;
        PrintWriter writer;
        StandardAnalyzer analyzer;
        QueryParser queryParser;
        String screenName;
        String id;

        IndexSearcher userPoliticianIndexSearcher = IndexUtility.getIndexSearcher(AppConfigs.USER_POLITICIAN_INDEX);

        // load all user screen names from file.
        Object[] allUserIds = FileUtility.loadColumnsFromCSV(AppConfigs.ALL_USER, 0).toArray();
        Object[] allUserScreenNames = FileUtility.loadColumnsFromCSV(AppConfigs.ALL_USER, 1).toArray();

        analyzer = new StandardAnalyzer(Version.LUCENE_41);
        queryParser = new QueryParser(Version.LUCENE_41, "", analyzer);

        List<String> userTweetCount = new ArrayList<>();
        List<String> userPoliticianMention = new ArrayList<>();

        int len = allUserScreenNames.length;

        for (int i=0; i < len; i++) {

            System.out.println("Processing user: " + i + "/" + len);

            id = (String)allUserIds[i];
            screenName = (String)allUserScreenNames[i];
            query = queryParser.parse("userScreenName: " + screenName);

            topDocs = userPoliticianIndexSearcher.search(query, Integer.MAX_VALUE);

            userTweetCount.add(id + ", " + screenName + ", " + topDocs.scoreDocs.length);

            for (ScoreDoc scoreDoc: topDocs.scoreDocs) {
                document = userPoliticianIndexSearcher.doc(scoreDoc.doc);
                userPoliticianMention.add(String.format("%s, %s, %s", id, screenName, document.get("politicianScreenName")));
            }
        }

        String[] userPoliticianMap = userPoliticianMention.stream().distinct().toArray(String[]::new);

        // System.out.println("No. of total user_politician mention: " + userPoliticianMention.toArray().length);
        // System.out.println("No. of unique user_politician map: " + userPoliticianMap.length);

        FileUtility.writeToFile(AppConfigs.USER_TWEET_COUNT, userTweetCount.toArray());
        FileUtility.writeToFile(AppConfigs.USER_POLITICIAN_MAP, userPoliticianMap);

        System.out.println("----------------------------------");
    }
    
    public static void buildUserPoliticianMapIndex() throws Exception {

        System.out.println("================================");
        System.out.println("Building user-politician map ...");

        TopDocs topDocs = userTweetIndexSearcher.search(new MatchAllDocsQuery(), Integer.MAX_VALUE);

        Document newDocument;

        Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_41);
        IndexWriter userPoliticianMapIndexWriter = IndexUtility.getIndexWriter(AppConfigs.USER_POLITICIAN_INDEX, analyzer);
        Document document;
        int counter = 0;

        // ... CREATE USER-MAP INDEX ... //

        for (ScoreDoc scoreDoc: topDocs.scoreDocs) {
            document = userTweetIndexSearcher.doc(scoreDoc.doc);
            String user = document.get("screenName");

            for (String politician: document.get("mentioned").split(" ")) {
                newDocument = new Document();
                newDocument.add(new TextField("userScreenName", user, Field.Store.YES));
                newDocument.add(new TextField("politicianScreenName", politician, Field.Store.YES));
                userPoliticianMapIndexWriter.addDocument(newDocument);
                counter++;
            }

        }

        System.out.println("No. of user-politician map: " + counter);

        userPoliticianMapIndexWriter.commit();
        userPoliticianMapIndexWriter.close();

        System.out.println("--------------------------------");
    }

    public static void buildUserTweetIndex() throws Exception {

        System.out.println("=============================");
        System.out.println("Building user tweet index ...");

        List<String> all_politicians = FileUtility.listPoliticianIds(AppConfigs.ALL_POLITICIANS_LIST);
        System.out.println("No. of Politicians: " + all_politicians.toArray().length);

        Query query = UserAnalysisUtility.getPoliticiansQuery(all_politicians);

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
        IndexWriter user_tweet_index_writer = IndexUtility.getIndexWriter(AppConfigs.USER_TWEET_INDEX, analyzer);
        user_tweet_index_writer.addDocuments(documents);
        user_tweet_index_writer.commit();
        user_tweet_index_writer.close();

        // ... WRITE UNIQUE USER IDENTIFIERS TO FILE ... //

        PrintWriter printWriter = FileUtility.getPrintWriter(AppConfigs.ALL_USER);

        for (String user: unique_users) {
            printWriter.write(user + "\n");
        }

        printWriter.close();

        System.out.println("-----------------------------");

    }

//    public static void


}

class UserAnalysisUtility {

    public static Query getPoliticiansQuery(List<String> politicians) {

        Query query = null;

        try {
            StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_41);

            List<String> query_tags = new ArrayList<String>();

            for(String name : politicians){
                query_tags.add("mentioned:\"" + name + "\"");
            }

            String query_string = String.join(" OR ", query_tags);
            System.out.println(query_string);

            query = new QueryParser(Version.LUCENE_41, "title", analyzer).parse(query_string);

        } catch (Exception e){
            System.out.print(e.getStackTrace());
        }

        return query;
    }

    public static List<String> loadUsers() {

        return FileUtility.loadColumnsFromCSV(AppConfigs.ALL_USER, 0);
    }

}