package user_tweet;

import com.AppConfigs;
import it.stilo.g.structures.LongIntDict;
import it.stilo.g.structures.WeightedUndirectedGraph;
import it.stilo.g.util.GraphReader;
import org.apache.lucene.index.CheckIndex;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.File;

public class test_main {
    public static final String indexPath = AppConfigs.USER_TWEET_INDEX;

    public static void main(String[] args){

        try {
            int graphSize = 16815933;
            WeightedUndirectedGraph g = new WeightedUndirectedGraph(graphSize + 1);

            LongIntDict mapLong2Int = new LongIntDict();
            GraphReader.readGraphLong2IntRemap(g, AppConfigs.USER_GRAPH_PATH, mapLong2Int, false);

            System.out.println("Original graph size: " + String.valueOf(g.size));

        } catch (Exception  e){
            System.out.print(e.getStackTrace());
        }
    }
}