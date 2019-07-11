package user_tweet;

import gnu.trove.map.TIntLongMap;
import it.stilo.g.structures.WeightedDirectedGraph;

public class MappedWeightedGraph {

    private WeightedDirectedGraph g;
    private TIntLongMap map;

    public MappedWeightedGraph(WeightedDirectedGraph g, TIntLongMap map) {
        this.g = g;
        this.map = map;
    }

    public WeightedDirectedGraph getWeightedGraph() {
        return this.g;
    }

    public TIntLongMap getMap() {
        return this.map;
    }

    public void setWeightedGraph(WeightedDirectedGraph g) {
        this.g = g;
    }

    public void setMap(TIntLongMap map) {
        this.map = map;
    }
}
