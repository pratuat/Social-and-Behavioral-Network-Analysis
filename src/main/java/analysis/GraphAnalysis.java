package analysis;

import com.AppConfigs;
import com.google.common.graph.Graph;
import com.google.common.primitives.Ints;
import io.ReadFile;
import io.TxtUtils;
import com.google.common.util.concurrent.AtomicDouble;
import gnu.trove.iterator.TLongIntIterator;
import gnu.trove.map.TIntLongMap;
import index.IndexSearcher;
import static io.TxtUtils.txtToList;
import it.stilo.g.algo.ConnectedComponents;
import it.stilo.g.algo.CoreDecomposition;
import it.stilo.g.algo.GraphInfo;
import it.stilo.g.algo.HubnessAuthority;
import it.stilo.g.algo.KppNeg;
import it.stilo.g.algo.SubGraphByEdgesWeight;
import it.stilo.g.structures.Core;
import it.stilo.g.util.NodesMapper;
import java.io.IOException;
import java.util.*;

import it.stilo.g.algo.SubGraph;
import it.stilo.g.algo.UnionDisjoint;
import it.stilo.g.structures.DoubleValues;
import it.stilo.g.structures.LongIntDict;
import it.stilo.g.structures.WeightedDirectedGraph;
import it.stilo.g.structures.WeightedGraph;
import it.stilo.g.structures.WeightedUndirectedGraph;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import static java.lang.Integer.min;
import java.lang.reflect.InvocationTargetException;

import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.commons.lang3.tuple.ImmutablePair;
import structure.MappedWeightedGraph;
import structure.ReadTxtException;
import user_tweet.FileUtility;
import utils.GraphUtils;

public abstract class GraphAnalysis {

    public static int runner = (int) (Runtime.getRuntime().availableProcessors());

    static IndexSearcher searcher;

    static {
        try {
            searcher = new IndexSearcher(AppConfigs.ALL_TWEET_INDEX);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static int getNumberClusters(String graph) throws IOException {
        //String graph = "src/main/resources/yes_graph.txt";
        ReadFile rf = new ReadFile();
        String[] lines = rf.readLines(graph);

        Set<Double> clusters = new HashSet<>();

        int n = lines.length;
        for (int i = 0; i < n; i++) {
            String[] line = lines[i].split(" ");
            Double c = Double.parseDouble(line[3]);
            clusters.add(c);
        }
        return clusters.size();
    }

    private static List<Integer> getNumberNodes(String graph, int c) throws IOException {
        ReadFile rf = new ReadFile();
        String[] lines = rf.readLines(graph);
        List<Integer> numberNodes = new ArrayList<>();
        int n = lines.length;
        for (int cIter = 0; cIter < c; cIter++) {
            Set<String> words = new HashSet<String>();
            for (int i = 0; i < n; i++) {
                String[] line = lines[i].split(" ");
                int cc = Integer.parseInt(line[3]);
                if (cc == cIter) {
                    words.add(line[0]);
                    words.add(line[1]);
                }
            }
            numberNodes.add(words.size());
        }
        return numberNodes;
    }

    private static WeightedUndirectedGraph addNodesGraph(WeightedUndirectedGraph g, int k, String graph, NodesMapper<String> mapper) throws IOException {
        // add the nodes from the a file created with coocurrencegraph.java, and returns the graph
        ReadFile rf = new ReadFile();
        String[] lines = rf.readLines(graph);

        // map the words into id for g stilo
        //NodesMapper<String> mapper = new NodesMapper<String>();
        // creathe the graph
        // keep in mind that the id of a word is mapper.getId(s1) - 1 (important the -1)
        int n = lines.length;
        for (int i = 0; i < n; i++) {
            // split the line in 3 parts: node1, node2, and weight
            String[] line = lines[i].split(" ");
            if (Integer.parseInt(line[3]) == k) {
                String node1 = line[0];
                String node2 = line[1];
                Double w = Double.parseDouble(line[2]);
                // the graph is directed, add links in both ways
                g.add(mapper.getId(node1) - 1, mapper.getId(node2) - 1, w);
                //g.add(mapper.getId(node2) - 1, mapper.getId(node1) - 1, w);
            }

        }
        return g;
    }

    private static WeightedUndirectedGraph normalizeGraph(WeightedUndirectedGraph g) {
        // normalize the weights of the edges
        // Normalize the weights
        double suma = 0;
        // go in each node

        for (int i = 0; i < g.size - 1; i++) {
            suma = 0;

            // calculate the sum of the weights of this node with the neighbours
            for (int j = 0; j < g.weights[i].length; j++) {
                suma = suma + g.weights[i][j];
            }

            // update the weights by dividing by the total weight sum
            for (int j = 0; j < g.weights[i].length; j++) {
                g.weights[i][j] = g.weights[i][j] / suma;
            }

        }

        return g;
    }

    private static WeightedUndirectedGraph kcore(WeightedUndirectedGraph g) throws InterruptedException {
        // calculates the kcore and returns a graph. Now its not working who knows why
        WeightedUndirectedGraph g1 = UnionDisjoint.copy(g, runner);
        Core cc = CoreDecomposition.getInnerMostCore(g1, runner);
        System.out.println("Kcore");
        System.out.println("Minimum degree: " + cc.minDegree);
        System.out.println("Vertices: " + cc.seq.length);
        System.out.println("Seq: " + cc.seq);

        g1 = UnionDisjoint.copy(g, 2);
        WeightedUndirectedGraph s = SubGraph.extract(g1, cc.seq, runner);
        return s;
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

    private static WeightedUndirectedGraph getLargestCC(WeightedUndirectedGraph g) throws InterruptedException {
        // this get the largest component of the graph and returns a graph too
        //System.out.println(Arrays.deepToString(g.weights));
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

    /*
    iterate through all the edges, recovering the terms.
    'edges' is a matrix, in which each row is a termID1, and in each column is
    another termID2 that has an edge with termID1.
    Ex:
    [0] = [1, 5, 6]
    [1] = [0, 8]
    ...
    Map back each termID to the term string and save to the edges in the following
    format:
    term1 term2 clusterID
     */
    private static void saveGraphToFile(PrintWriter pw, NodesMapper<String> mapper, int[][] edges, int clusterID) throws IOException {
        String term1 = "", term2 = "";

        for (int i = 0; i < edges.length; i++) {
            if (edges[i] != null) {
                term1 = mapper.getNode(i + 1);
                for (int j = 0; j < edges[i].length; j++) {
                    term2 = mapper.getNode(edges[i][j] + 1);
                    pw.println(term1 + " " + term2 + " " + clusterID);
                }
            }
        }
    }

    public static void identifyTopKPlayers(WeightedDirectedGraph graph, LongIntDict longIntDict, int[] userIds, String fileName, HashMap<Long, String> userIntIdScreenNameHashMap) throws InterruptedException, IOException {
        WeightedDirectedGraph subGrpah = SubGraph.extract(graph, userIds, runner);
        TIntLongMap intLongDict = longIntDict.getInverted();

        // FILTER NODES WITH DEGREES LESSER THAN THRESHOLD //

        int threshold = 15;
        ArrayList<Integer> subGraphNodes = new ArrayList<>();

        for (int i = 1; i < subGrpah.out.length; i++) {
            if ((subGrpah.out[i] != null && subGrpah.out[i].length >= threshold) || (subGrpah.in[i] != null && subGrpah.in[i].length >= threshold)) {
                subGraphNodes.add(i);
            }
        }

        System.out.println(">>> Original user length: " + userIds.length);
        System.out.println(">>> Filtered user length: " + subGraphNodes.toArray().length);

        List<DoubleValues> brokers = KppNeg.searchBroker(subGrpah, subGraphNodes.stream().mapToInt((i) -> i).toArray(), runner);
        brokers.sort((new HubAuthorityComparator()).reversed());

        DoubleValues[] brokersArray = brokers.subList(0, 500).stream().toArray(DoubleValues[]::new);

        Long brokerId;
        String brokerScreenName;
        List<String> kpps = new ArrayList<>();

        for (DoubleValues broker: brokersArray) {
            brokerId = intLongDict.get(broker.index);
            brokerScreenName = userIntIdScreenNameHashMap.get(brokerId);
            kpps.add(String.format("%f, %s, %d", brokerId, brokerScreenName, broker.value));
        }

        FileUtility.writeToFile(fileName, kpps.toArray());
    }

    public static MappedWeightedGraph extractLargestCCofM(WeightedDirectedGraph g, int[] usersIDs, LongIntDict mapLong2Int, boolean saveToFile) throws InterruptedException, IOException {
        // extract the subgraph induced by the users that mentioned the politicians
        System.out.println("Extracting the subgraph induced by M");
        g = SubGraph.extract(g, usersIDs, runner);

        // The SubGraph.extract() creates a graph of the same size as the old graph
        // and it raises an exception due to insufficient memory.
        // We had to resize the graph.
        LongIntDict dictResize = new LongIntDict();
        g = GraphUtils.resizeGraph(g, dictResize, usersIDs.length);
        // map the id of the old big graph to the new ones
        usersIDs = new int[usersIDs.length];
        int i = 0;
        TLongIntIterator iterator = dictResize.getIterator();
        while (iterator.hasNext()) {
            iterator.advance();
            usersIDs[i] = iterator.value();
            i++;
        }

        // extract the largest connected component
        System.out.println("Extracting the largest connected component of the subgraph induced by M");
        Set<Integer> setMaxCC = getMaxSet(ConnectedComponents.rootedConnectedComponents(g, usersIDs, runner));
        g = SubGraph.extract(g, Ints.toArray(setMaxCC), runner);

        // save the largest CC of M
        System.out.println("Saving the graph");
        TIntLongMap revDictResize = dictResize.getInverted();

        if (saveToFile)
            GraphUtils.saveDirectGraph2Mappings(g, AppConfigs.USER_GRAPH_LCC_PATH, revDictResize, mapLong2Int.getInverted());

        return new MappedWeightedGraph(g, revDictResize);
    }

    public static void saveTopKAuthorities(MappedWeightedGraph graph, LongIntDict mapLong2Int, int topk, String fileName, HashMap<Long, String> userIntIdScreenNameHashMap) throws InterruptedException, IOException {
        TIntLongMap mapInt2LongSuper = mapLong2Int.getInverted();
        TIntLongMap mapInt2Long = graph.getMap();

        // get the authorities
        ArrayList<ArrayList<DoubleValues>> hubAuthorities = HubnessAuthority.compute(graph.getWeightedGraph(), 0.00001, runner);

        // sort users by authority and hub score
        hubAuthorities.get(0).sort((new HubAuthorityComparator()).reversed());
        hubAuthorities.get(1).sort((new HubAuthorityComparator()).reversed());

        DoubleValues[] authorityScores = hubAuthorities.get(0).subList(0, topk).stream().toArray(DoubleValues[]::new);
        DoubleValues[] hubScores = hubAuthorities.get(1).subList(0, topk).stream().toArray(DoubleValues[]::new);

        List<String> authorityScoreList = new ArrayList<>();
        List<String> hubScoreList = new ArrayList<>();

        long authorityIndex, hubIndex;
        String authorityScreenName, hubScreenName;

        for (int i = 0; i < authorityScores.length; i++) {
            authorityIndex = mapInt2LongSuper.get((int)mapInt2Long.get(authorityScores[i].index));
            hubIndex = mapInt2LongSuper.get((int)mapInt2Long.get(hubScores[i].index));
            authorityScreenName = userIntIdScreenNameHashMap.get(authorityIndex);
            hubScreenName = userIntIdScreenNameHashMap.get(hubIndex);

            authorityScoreList.add(String.format("%d, %s, %f", authorityIndex, authorityScreenName, authorityScores[i].value));
            hubScoreList.add(String.format("%d, %s, %f", hubIndex, hubScreenName, hubScores[i].value));
        }

        FileUtility.writeToFile(fileName + "_authorities.csv", authorityScoreList.toArray());
        FileUtility.writeToFile(fileName + "_hubs.csv", hubScoreList.toArray());
    }
}

class HubAuthorityComparator implements Comparator<DoubleValues> {
    @Override
    public int compare(DoubleValues x, DoubleValues y) {
        return (new Double(x.value)).compareTo(new Double(y.value));
    }
}
