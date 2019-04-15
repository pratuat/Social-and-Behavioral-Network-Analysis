import com.sbn.twitter.*;
import it.stilo.g.example.ZacharyExample;
import it.stilo.g.structures.WeightedDirectedGraph;
import it.stilo.g.structures.WeightedUndirectedGraph;
import it.stilo.g.util.GraphReader;
import it.stilo.g.algo.ComunityLPA;
import it.stilo.g.algo.PageRankPI;
import it.stilo.g.algo.PageRankRW;
import it.stilo.g.structures.DoubleValues;
import it.stilo.g.structures.WeightedDirectedGraph;
import it.stilo.g.util.MemInfo;
import it.stilo.g.util.ZacharyNetwork;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
//import utils.csv.*;



public class Main {

    public static void main(String[] args){

        System.out.println("Hello World !!!");

        //MyNewClass object = new MyNewClass("Blah Blah Blah...");

        Tweet tweet = new Tweet("Matteo Salvini", "123", "blah blah");
        tweet.show();

        //g.out;
        /*
        Logger logger = LogManager.getLogger(ZacharyExample.class);

        int worker = (int) (Runtime.getRuntime().availableProcessors());

        WeightedDirectedGraph g = new WeightedDirectedGraph(ZacharyNetwork.VERTEX);
        ZacharyNetwork.generate(g, worker);
        logger.info(Arrays.deepToString(g.out));

        ArrayList<DoubleValues> list;

        list = PageRankPI.compute(g,0.99,0.5, worker);
        for (int i = 0; i < ZacharyNetwork.VERTEX; i++) {
            logger.info(list.get(i).value + ":\t\t" + list.get(i).index);        }

        list = PageRankRW.compute(g,0.99, worker);
        for (int i = 0; i < ZacharyNetwork.VERTEX; i++) {
            logger.info(list.get(i).value + ":\t\t" + list.get(i).index);
        }


        int[] labels = ComunityLPA.compute(g,1.0d, worker);
        logger.info(Arrays.toString(labels));

        MemInfo.info();*/
    }
}
