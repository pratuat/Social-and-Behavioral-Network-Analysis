package user_tweet;

import com.AppConfigs;
import index.IndexSearcher;
import io.ReadFile;
import io.TxtUtils;
import it.stilo.g.structures.LongIntDict;
import it.stilo.g.structures.WeightedDirectedGraph;
import it.stilo.g.util.GraphReader;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.zip.GZIPInputStream;
import analysis.GraphAnalysis;
import structure.MappedWeightedGraph;
import twitter4j.JSONException;
import java.util.List;

import static io.TxtUtils.txtToList;

public class UserAnalysis {
    public static final String indexPath = AppConfigs.USER_TWEET_INDEX;

    public static IndexSearcher all_tweet_index_searcher;

    static {
        try {
            all_tweet_index_searcher = new IndexSearcher(AppConfigs.ALL_TWEET_INDEX);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args)  throws IOException, JSONException, ParseException, Exception {

        boolean loadGraph = true;
        boolean calculateTopAuthorities = true;
        boolean printAuthorities = true;
        boolean calculateKplayers = true;
        boolean useCache = false;



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
                for (String username : usersList) {
                    docs = all_tweet_index_searcher.searchByField("name", username, 1);

                    if (docs != null) {
                        id = Long.parseLong(docs[0].get("userId"));  //read just the first resulting doc
                        nodes[i] = mapLong2Int.get(id);  // retrieve the twitter ID (long) and covert to int (the position in the graph)
                    }
                    i++;
                }

                List<ImmutablePair> brokersUsername = GraphAnalysis.getTopKPlayers(g, nodes, mapLong2Int, 500, 1);
                // save the first topk authorities
                TxtUtils.iterableToTxt(AppConfigs.RESOURCES_DIR + supportType + "_top_k_players.txt", brokersUsername);
            }

            System.out.println(">> Success: Calculate K playes.");
        }
    }
}