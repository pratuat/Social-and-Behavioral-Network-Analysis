package user_tweet;

import com.AppConfigs;
import com.opencsv.CSVReader;
import it.stilo.g.algo.ConnectedComponents;
import it.stilo.g.algo.HubnessAuthority;
import it.stilo.g.structures.DoubleValues;
import it.stilo.g.structures.LongIntDict;
import it.stilo.g.structures.WeightedDirectedGraph;
import it.stilo.g.structures.WeightedUndirectedGraph;
import it.stilo.g.util.GraphReader;
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
import java.io.*;
import it.stilo.g.algo.SubGraph;

import java.util.*;


public class UserTweetIndexer {
    public static final String TWEET_INDEX_PATH = AppConfigs.TWEET_INDEX;
    public static final String USER_TWEET_INDEX_PATH = AppConfigs.USER_TWEET_INDEX;


    public static int runner = (int) (Runtime.getRuntime().availableProcessors());

    public static void main(String[] args){

        // processUserTweet();

        // processUserGraph();

        // generateUserPoliticianGraph();
    }

    private static WeightedUndirectedGraph getLargestCC(WeightedUndirectedGraph g) throws InterruptedException {
        // this get the largest component of the graph and returns a graph too

        System.out.println(Arrays.deepToString(g.weights));

        int[] all = new int[g.size];
        for (int i = 0; i < g.size; i++) {
            all[i] = i;
        }

        System.out.println("CC");
        Set<Set<Integer>> comps = ConnectedComponents.rootedConnectedComponents(g, all, runner);

        Set<Integer> max_set = getMaxSet(comps);
        int[] subnodes = new int[max_set.size()];
        Iterator<Integer> iterator = max_set.iterator();
        for (int j = 0; j < subnodes.length; j++) {
            subnodes[j] = iterator.next();
        }

        WeightedUndirectedGraph s = SubGraph.extract(g, subnodes, runner);
        return s;
    }

    private static String[] processUserTweet(){

        String[] all_users = null;

        try {
//             List<String> politician_ids = list_politician_ids();

            // Query query = build_user_tweet_query(politician_ids);
            // IndexSearcher tweet_searcher = IndexUtility.get_index_searcher(TWEET_INDEX_PATH);
            // TopDocs topDocs = tweet_searcher.search(query, Integer.MAX_VALUE);

            // IndexWriter user_tweet_writer = IndexUtility.get_index_writer(USER_TWEET_INDEX_PATH);

            // List<Document> documents = IndexUtility.write_to_user_tweet_index(tweet_searcher, topDocs, user_tweet_writer);

            IndexSearcher tweet_searcher = IndexUtility.get_index_searcher(USER_TWEET_INDEX_PATH);
            TopDocs topDocs = tweet_searcher.search(new MatchAllDocsQuery(), Integer.MAX_VALUE);


            List<String> user_ids = new ArrayList<String>();
            List<String> user_names = new ArrayList<String>();
            // List<String> mentions = new ArrayList<String>();


            for (ScoreDoc scoreDoc : topDocs.scoreDocs){
                Document doc = tweet_searcher.doc(scoreDoc.doc);

                user_ids.add(doc.get("userId"));
                user_names.add(doc.get("name"));
                // mentioned.add(doc.get("mentioned"));
                // System.out.println(doc.get("userId") + doc.get("name") + " | " + doc.get("mentioned"));
            }

            all_users = user_ids.stream().distinct().toArray(String[]::new);

            System.out.println("Total number of documents: " + topDocs.scoreDocs.length);
            System.out.println("Total no of tweets: " + user_ids.toArray().length);
            System.out.println("Total no of unique users: " + all_users.length);
            // System.out.println("Total no of users: " + mentions.length);

            FileWriter fr = new FileWriter(AppConfigs.OUTPUT_PATH + "all_user_ids.csv");
            BufferedWriter br = new BufferedWriter(fr);
            PrintWriter pw = new PrintWriter(br);

            for (String user: all_users ){
                pw.write(user);
                pw.write("\n");
            }

            pw.close();

        } catch (Exception e){
            System.out.println(e.getStackTrace());
        }

        return all_users;
    }

    private static void generateUserPoliticianGraph(){

        try {
//            FileWriter fr = new FileWriter(file_name);
//            BufferedWriter br = new BufferedWriter(fr);
//            PrintWriter pw = new PrintWriter(br);

            IndexSearcher tweet_searcher = IndexUtility.get_index_searcher(AppConfigs.USER_TWEET_INDEX);
            TopDocs topDocs = tweet_searcher.search(new MatchAllDocsQuery(), Integer.MAX_VALUE);

//            List<String> userIds = new ArrayList<String>();
//            List<String> politicianIds = new ArrayList<String>();

            for (ScoreDoc scoreDoc : topDocs.scoreDocs){
                Document doc = tweet_searcher.doc(scoreDoc.doc);
                String user_id = doc.get("userId");
                String politician_ids = doc.get("mentioned");

                for (String politician_id: politician_ids.split(" ")){
                    System.out.println(user_id + "\t" + politician_id);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
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

    private static Set<Integer> getMaxSet(Set<Set<Integer>> comps) {
        int m = 0;
        Set<Integer> max_set = null;

        // get largest component
        for (Set<Integer> innerSet : comps) {
            if (innerSet.size() > m) {
                max_set = innerSet;
                m = innerSet.size();
            }
        }
        return max_set;
    }


    private static void computeHITS(WeightedUndirectedGraph g) {
        ArrayList<ArrayList<DoubleValues>> list = HubnessAuthority.compute(g, 0.00001, runner);

        for (int i = 0; i < list.size(); i++) {
            ArrayList<DoubleValues> score = list.get(i);
            String x = "";
            if (i == 0) {
                x = "Auth ";
            } else {
                x = "Hub ";
            }

            for (int j = 0; j < score.size(); j++) {
                System.out.println(x + score.get(j).value + ":\t\t" + score.get(j).index);
            }
        }
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
        // return new MatchAllDocsQuery();
    }


    private static IndexReader get_index_reader(String index_path) throws Exception{
        Directory directory = FSDirectory.open(new File(index_path));
        CheckIndex ci = new CheckIndex(directory);
        IndexReader indexReader = DirectoryReader.open(directory);

        return indexReader;
    }
}
