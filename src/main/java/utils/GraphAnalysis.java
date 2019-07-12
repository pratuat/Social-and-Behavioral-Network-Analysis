package utils;

import com.google.common.primitives.Ints;
import gnu.trove.iterator.TLongIntIterator;
import gnu.trove.map.TIntLongMap;
import it.stilo.g.algo.ConnectedComponents;
import it.stilo.g.algo.HubnessAuthority;
import it.stilo.g.algo.KppNeg;
import java.io.IOException;
import java.util.*;
import it.stilo.g.algo.SubGraph;
import it.stilo.g.structures.DoubleValues;
import it.stilo.g.structures.LongIntDict;
import it.stilo.g.structures.WeightedDirectedGraph;

public abstract class GraphAnalysis {

    public static int runner = (int) (Runtime.getRuntime().availableProcessors());

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

    public static void runKPPNEGAnalysis(WeightedDirectedGraph graph, LongIntDict longIntDict, int[] userIds, String fileName, HashMap<Long, String> userIntIdScreenNameHashMap) throws InterruptedException, IOException {
        WeightedDirectedGraph subGraph = SubGraph.extract(graph, userIds, runner);
        TIntLongMap intLongDict = longIntDict.getInverted();

        // FILTER NODES WITH DEGREES LESSER THAN THRESHOLD //

        int threshold = 70;
        ArrayList<Integer> subGraphNodes = new ArrayList<>();

        for (int i = 1; i < subGraph.out.length; i++) {
            if ((subGraph.out[i] != null && subGraph.out[i].length >= threshold) || (subGraph.in[i] != null && subGraph.in[i].length >= threshold)) {
                subGraphNodes.add(i);
            }
        }

        System.out.println(">>> Original user length: " + userIds.length);
        System.out.println(">>> Filtered user length: " + subGraphNodes.toArray().length);

        subGraph = SubGraph.extract(subGraph, subGraphNodes.stream().mapToInt(i -> i).toArray(), runner);

        List<DoubleValues> brokers = KppNeg.searchBroker(subGraph, subGraph.getVertex(), runner);

        brokers.sort((new HubAuthorityComparator()).reversed());

        DoubleValues[] brokersArray = brokers.subList(0, 500).stream().toArray(DoubleValues[]::new);

        Long brokerId;
        String brokerScreenName;
        List<String> kpps = new ArrayList<>();

        for (DoubleValues broker: brokersArray) {
            brokerId = intLongDict.get(broker.index);
            brokerScreenName = userIntIdScreenNameHashMap.get(brokerId);
            kpps.add(brokerId + "," + brokerScreenName + "," + broker.value);
        }

        FileUtility.writeToFile(fileName, kpps.toArray());
    }

    public static MappedWeightedGraph extractLargestCC(WeightedDirectedGraph g, int[] usersIDs, LongIntDict mapLong2Int, boolean saveToFile) throws InterruptedException, IOException {
        // extract the subgraph induced by the users that mentioned the politicians
        System.out.println("Extracting the subgraph induced by M");
        g = SubGraph.extract(g, usersIDs, runner);

        // The SubGraph.extract() creates a graph of the same size as the old graph
        // and it raises an exception due to insufficient memory.
        // We had to resize the graph.
        LongIntDict dictResize = new LongIntDict();
        g = GraphUtility.resizeGraph(g, dictResize, usersIDs.length);
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
            GraphUtility.saveDirectGraph2Mappings(g, AppConfigs.USER_GRAPH_LCC_PATH, revDictResize, mapLong2Int.getInverted());

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
