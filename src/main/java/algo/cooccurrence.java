package algo;

import com.google.common.util.concurrent.AtomicDouble;
import it.stilo.g.algo.*;
import it.stilo.g.structures.Core;
import it.stilo.g.structures.WeightedUndirectedGraph;
import it.stilo.g.util.GraphWriter;
import it.stilo.g.util.NodesMapper;

import java.io.*;
import java.util.*;
import index.indexTweets;
//import jdk.internal.org.objectweb.asm.util.Printer;
import org.apache.lucene.queryparser.classic.ParseException;
//import sun.security.pkcs.ParsingException;

public class cooccurrence {

    private int clusterNumber;
    private String clusterLocation;

    public cooccurrence(int k, String loc){
        this.clusterNumber = k;
        this.clusterLocation = loc;

    }

    /**
     * Get the terms within a particular cluster
     * @param cNumber cluster number
     * @return a list of terms belonging to a particular cluster
     * @throws IOException
     * @throws FileNotFoundException
     */
    public ArrayList loadCluster (int cNumber) throws IOException, FileNotFoundException {
        // Map<Integer, String> clusterGraph = new HashMap<Integer, String>;
        ArrayList terms = new ArrayList();
        FileInputStream fstream = new FileInputStream(clusterLocation + cNumber+".txt");
        System.out.println(clusterLocation + cNumber+".txt");
        InputStreamReader clusterReader = new InputStreamReader(fstream);
        //for (int i = 0; i < clusterReader.; i++)
        try (BufferedReader br = new BufferedReader(clusterReader)) {
            String line;
            int n = 0;
            while ((line = br.readLine()) != null) {
                String[] r = line.split("\n");
                terms.add(r[0]);
                n++;
            }
        }
        clusterReader.close();
        return terms;

    }

    /**
     * Saves the cluster number along with its corresponding terms
     * @param cNumbers
     * @return a hash map with the cluster number as key and the term name as value
     * @throws IOException
     */
    public Map<Integer,ArrayList> getClusterTerms (int cNumbers) throws IOException{

        Map<Integer, ArrayList> clusterTerms = new HashMap<>();

        for (int i=0; i < cNumbers; i++){
            clusterTerms.put(i, loadCluster(i));
        }

       // ConnectedComponents.rootedConnectedComponents();
        return clusterTerms;
    }

    /**
     * create a Graph based on the term within a particular cluster
     * @param clusterTerms
     * @return Weighted undirected graph that maps our the relationship of the terms within a cluster
     * @throws IOException
     * @throws ParseException
     */
    public WeightedUndirectedGraph createGraph(ArrayList<String> clusterTerms) throws IOException, ParseException {
        String indexLocation = "./index/indexTweets";
        String sourceTweets = "./src/util/data/stream";
        WeightedUndirectedGraph graph = new WeightedUndirectedGraph(clusterTerms.size());
        indexTweets index = new indexTweets(sourceTweets, indexLocation);
        double p = 0.1;
        for (int t1 = 0; t1 < clusterTerms.size(); t1++) {
            System.out.println(t1 + " out of "+ clusterTerms.size());
            for (int t2 = t1 + 1; t2 < clusterTerms.size(); t2++) {
                String term1 = clusterTerms.get(t1).split(" ", 2)[0];
                String term2 = clusterTerms.get(t2).split(" ", 2)[0];
                //System.out.println(term1 + " " + term2);
                int weight = index.search(term1,term2, "tweetText");
                //System.out.println(weight);
                //int weight = taNO.queries(term1, term2, "text", false);
                // threshold
                int t1TF = index.termFrequency(term1,"tweetText");
                int t2TF = index.termFrequency(term2,"tweetText");
                int flag;
                if (t1TF < t2TF) {
                    flag = t1TF;
                } else {
                    flag = t2TF;
                }
                double threshold = flag * p;
                //System.out.println(minFreq+" - t: "+t);

                if (weight > threshold) {
                    //graph.add(t1, t2, weight);
                    graph.add(t1, t2, 1);
                }
                //System.out.println(term1+" "+term2+": "+taYES.queries(term1,term2));
            }

        }
        AtomicDouble[] info = GraphInfo.getGraphInfo(graph, 1);
        System.out.println("Nodes:" + info[0]);
        System.out.println("Edges:" + info[1]);
        System.out.println("Density:" + info[2]);

        return graph;
    }

    /**
     * get the largest connected component within a cluster
     * @param g graph to be analysez
     * @param worker
     * @return the largest connected component
     * @throws InterruptedException
     */

    public WeightedUndirectedGraph getLargestConnectedComponent(WeightedUndirectedGraph g, int worker) throws InterruptedException{
        // this get the largest component of the graph and returns a graph too
        //System.out.println(Arrays.deepToString(g.weights));
        int[] all = new int[g.size];
        for (int i = 0; i < g.size; i++) {
            all[i] = i;
        }
        System.out.println("CC");
        Set<Set<Integer>> comps = ConnectedComponents.rootedConnectedComponents(g, all, worker);

        int m = 0;
        Set<Integer> max_set = null;
        // get largest component
        for (Set<Integer> innerSet : comps) {
            if (innerSet.size() > m) {
                max_set = innerSet;
                m = innerSet.size();
            }
        }


        int[] subnodes = new int[max_set.size()];
        Iterator<Integer> iterator = max_set.iterator();
        for (int j = 0; j < subnodes.length; j++) {
            subnodes[j] = iterator.next();
        }

        WeightedUndirectedGraph s = SubGraph.extract(g, subnodes, worker);
        return s;
    }

    /**
     * method to extract the K cores components within a graph
     * @param g
     * @param worker
     * @return
     * @throws InterruptedException
     */
    public WeightedUndirectedGraph extractKCores(WeightedUndirectedGraph g, int worker) throws InterruptedException{
        WeightedUndirectedGraph g1 = UnionDisjoint.copy(g, worker);
        Core cc = CoreDecomposition.getInnerMostCore(g1, worker);

        System.out.println("Kcore");
        System.out.println("Minimum degree: " + cc.minDegree);
        System.out.println("Vertices: " + cc.seq.length);
        System.out.println("Seq: " + cc.seq);

        g1 = UnionDisjoint.copy(g, 2);
        WeightedUndirectedGraph s = SubGraph.extract(g1, cc.seq, worker);
        return s;
    }

    /**
     * method to save the graph
     * @param graph
     * @param saveLocation
     * @throws IOException
     */
    public static void saveGraphToFile(WeightedUndirectedGraph graph, String saveLocation) throws IOException {
        GraphWriter w = new GraphWriter(graph, saveLocation);
        w.save();
    }


//    public WeightedUndirectedGraph extractClusterGraph(Map<String, String> clusters){
//
//    }
//
//    public static void extractKCoreAndConnectedComponent(double threshold) throws IOException, ParseException, Exception {
//
//        // do the same analysis for the yes-group and no-group
//        String[] prefixYesNo = {"yes", "no"};
//        for (String prefix : prefixYesNo) {
//
//            // Get the number of clusters
//            int c = getNumberClusters(RESOURCES_LOCATION + prefix + "_graph.txt");
//
//            // Get the number of nodes inside each cluster
//            List<Integer> numberNodes = getNumberNodes(RESOURCES_LOCATION + prefix + "_graph.txt", c);
//
//            PrintWriter pw_cc = new PrintWriter(new FileWriter(RESOURCES_LOCATION + prefix + "_largestcc.txt")); //open the file where the largest connected component will be written to
//            PrintWriter pw_kcore = new PrintWriter(new FileWriter(RESOURCES_LOCATION + prefix + "_kcore.txt")); //open the file where the kcore will be written to
//
//            // create the array of graphs
//            WeightedUndirectedGraph[] gArray = new WeightedUndirectedGraph[c];
//            for (int i = 0; i < c; i++) {
//                System.out.println();
//                System.out.println("Cluster " + i);
//
//                gArray[i] = new WeightedUndirectedGraph(numberNodes.get(i) + 1);
//
//                // Put the nodes,
//                NodesMapper<String> mapper = new NodesMapper<String>();
//                gArray[i] = addNodesGraph(gArray[i], i, RESOURCES_LOCATION + prefix + "_graph.txt", mapper);
//
//                //normalize the weights
//                gArray[i] = normalizeGraph(gArray[i]);
//
//                AtomicDouble[] info = GraphInfo.getGraphInfo(gArray[i], 1);
//                System.out.println("Nodes:" + info[0]);
//                System.out.println("Edges:" + info[1]);
//                System.out.println("Density:" + info[2]);
//
//                // extract remove the edges with w<t
//                gArray[i] = SubGraphByEdgesWeight.extract(gArray[i], threshold, 1);
//
//                // get the largest CC and save to a file
//                WeightedUndirectedGraph largestCC = getLargestCC(gArray[i]);
//                saveGraphToFile(pw_cc, mapper, largestCC.in, i);
//
//                // Get the inner core and save to a file
//                WeightedUndirectedGraph kcore = kcore(gArray[i]);
//                saveGraphToFile(pw_kcore, mapper, kcore.in, i);
//            }
//
//            pw_cc.close();
//            pw_kcore.close();
//        }
//    }

    public static void main(String[] args) throws UnsupportedOperationException, IOException, org.apache.lucene.queryparser.classic.ParseException, InterruptedException{
        //WeightedUndirectedGraph g = new UnionDisjoint();
        String locationYes = "./yesClusters/yes_";
        String locationNo = "./noClusters/no_";
        String co_lcc = "./cooc/lcc/no/lcc_";
        String co_kcore = "./cooc/kcore/no/kcore_";
        String sourceTweets = "./src/util/data/stream";

        cooccurrence c = new cooccurrence(20, locationNo);

        Map<Integer, ArrayList>clusters = c.getClusterTerms(20);
        System.out.println(clusters);
       // indexTweets ind = new indexTweets(sourceTweets, locationYes);
        //System.out.println(ind.search("fare", "cosa", "tweetText", false));
        for (int i = 0; i<20; i++){
            try{

            System.out.println("Working with clusters #"+i);
            System.out.println("Creating graph for cluster #"+i+" ...");

            WeightedUndirectedGraph graph = c.createGraph(clusters.get(i));
            System.out.println("Extracting largest connected component for clusters #"+i);
            WeightedUndirectedGraph largestCC= c.getLargestConnectedComponent(graph,2);
            // saveLCC = new PrintWriter(new FileWriter(co_lcc+i+".txt"));
            saveGraphToFile(largestCC, co_lcc+i+".txt");
            //saveLCC.close();
            System.out.println("Extracting K-cores component for clusters #"+i);
            WeightedUndirectedGraph kcores= c.extractKCores(graph,2);
            //PrintWriter saveKcores = new PrintWriter((new FileWriter(co_kcore+i+".txt")));
            saveGraphToFile(kcores,co_kcore+i+".txt" );
            //saveKcores.close();
            //c.getLargestConnectedComponent(graph,2);
            }
            catch(Exception e){
                continue;
            }
        }

//        ArrayList<String> arList = new ArrayList<String>();
//        arList.addAll(Arrays.asList("fare", "de", "ultim", "casa", "govern", "nuovo", "via", "stato", "piazz", "cosa", "sindac", "mondo", "roma", "ecco", "passo", "ital"));
//        //[fare, de, ultim, casa, govern, nuovo, via, stato, piazz, cosa, sindac, mondo, roma, ecco, passo, ital, ogni, cambiament, bella, important, paese, incontr]
//
//        WeightedUndirectedGraph graphTest = c.createGraph(arList);
//        WeightedUndirectedGraph kcores= c.extractKCores(graphTest,2);
//        c.getLargestConnectedComponent(graphTest,2);
//
//        GraphWriter w = new GraphWriter(graphTest, "test.txt");
//        w.save();
//
    }
}
