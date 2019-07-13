/** 
 * Class for LPA clustering of a G graph
 * May. 2019
 * @author Durand Azimedem
*/

package spread_of_influence;

import io.TxtUtils;
import it.stilo.g.structures.LongIntDict;
import it.stilo.g.structures.WeightedDirectedGraph;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import org.apache.commons.lang3.ArrayUtils;
import utils.AppConfigs;

public class LPA {

    public Map<Integer, Integer> nodesLabels;
    
    public LPA() {
    	
    }

    public static boolean contains(final int[] array, final int key) {
        return ArrayUtils.contains(array, key);
    }

    public static void shuffleLists(List<Integer> l1, List<Integer> l2) {
        // shuffles randomly two lists in the same order
        long seed = System.nanoTime();
        Collections.shuffle(l1, new Random(seed));
        Collections.shuffle(l2, new Random(seed));
    }

    public static void shuffleArray(int[] array) {
        // shuffles randomly one array (from stackoverflow)
        int index;
        Random random = new Random();
        for (int i = array.length - 1; i > 0; i--) {
            index = random.nextInt(i + 1);
            if (index != i) {
                array[index] ^= array[i];
                array[i] ^= array[index];
                array[index] ^= array[i];
            }
        }
    }

    public static int getRandom(int[] array) {
        int rnd = new Random().nextInt(array.length);
        return array[rnd];
    }

    public static void promptEnterKey() {
        System.out.println("Press \"ENTER\" to continue...");
        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();
        scanner.close();
    }

    public static int getPopularElement(int[] a) {
        // shuffle the labels so if there are 2 most popular nodes
        // the winner is selected randomly
        shuffleArray(a);
        // from stackoverflow
        int count = 0, tempCount;
        // add some randomness
        int popular = getRandom(a);
        int temp = getRandom(a);
        for (int i = 0; i < (a.length - 1); i++) {
            temp = a[i];
            tempCount = 0;
            for (int j = 1; j < a.length; j++) {
                if (temp == a[j]) {
                    tempCount++;
                }
            }
            if (tempCount > count) {
                popular = temp;
                count = tempCount;
            }
        }
        return popular;
    }

    public void lpaAlgorithm(WeightedDirectedGraph g, int[] seedsYes, int[] seedsNo, int killAt) throws IOException {
    	
    	SimpleDateFormat formatter= new SimpleDateFormat("dd-MM-yyyy 'at' HH:mm:ss");
        // all the non seed nodes will be labeled with unique labels
        ArrayList<Integer> tmp = new ArrayList<Integer>();
        for (int i = 0; i < g.size; i++) {
            if ((contains(seedsYes, i) == false) && (contains(seedsNo, i) == false)) {
                tmp.add(i);
            }
        }
        // Create list of nodes that don't have yes or no label at the begining
        int[] seedsUnknown = tmp.stream().mapToInt(i -> i).toArray();

        // Create an array for the labels, the position i will contain the label of the element i of the graph
        int[] labels = new int[g.size];

        //-1 means yes, -2 means no, and 0 to n are reserved for the initialization of the unknown labels
        for (int i = 0; i < seedsYes.length; i++) {
            labels[seedsYes[i]] = -1;
        }
        for (int i = 0; i < seedsNo.length; i++) {
            labels[seedsNo[i]] = -2;
        }
        for (int i = 0; i < seedsUnknown.length; i++) {
            labels[seedsUnknown[i]] = i;
        }

        // create list of nodes
        int[] nodes = new int[labels.length];
        for (int i = 0; i < g.size; i++) {
            nodes[i] = i; 
        }

        // create a dict for the labels
        nodesLabels = new HashMap<>();
        for (int i = 0; i < nodes.length; i++) {
            //System.out.println(nodes[i] + " " + labels[i]);
            int nodesTmp = nodes[i];
            int labelsTmp = labels[i];
            nodesLabels.put(nodesTmp, labelsTmp);
        }

        // go to each node and find the new labels
        // if this counter reaches the number of nodes, then all nodes
        // didn't change label at iteration k and we can stop iterating
        int counterConvergence = 0;

        // put a limit of iterations and keep track of the iterations for convergence
        int counter = 0;

        // Store here the temporal labels
        ArrayList<Integer> yesCounterOverTime = new ArrayList<Integer>();
        ArrayList<Integer> noCounterOverTime = new ArrayList<Integer>();
        ArrayList<Integer> unknownCounterOverTime = new ArrayList<Integer>();
        yesCounterOverTime.add(seedsYes.length);
        noCounterOverTime.add(seedsNo.length);
        unknownCounterOverTime.add(seedsUnknown.length);

        // change this when convergence is done or killAt < counter
        boolean stop = false;
        int newLabelNode;
        while (stop == false) {
            counter++;
            counterConvergence = 0;

            // shuffle first
            shuffleArray(nodes);

            // check iteration counter to save results
            if (counter % 5 == 0) {
                // j is an exact multiple of 4
            	Date date = new Date(System.currentTimeMillis());
        		System.out.println(formatter.format(date)+" INFO: Saving temporal counters");
                TxtUtils.iterableToTxt(AppConfigs.YES_LABELS_TEMPORAL_COUNTER, yesCounterOverTime);
                TxtUtils.iterableToTxt(AppConfigs.NO_LABELS_TEMPORAL_COUNTER, noCounterOverTime);
                TxtUtils.iterableToTxt(AppConfigs.UNKNOWN_LABELS_TEMPORAL_COUNTER, unknownCounterOverTime);
            }

            // print counters for the current iteration
            Date date = new Date(System.currentTimeMillis());
    		System.out.println(formatter.format(date)+" INFO: Iteration " + counter + "...");
            int lenthis = yesCounterOverTime.size() - 1;
            date = new Date(System.currentTimeMillis());
            System.out.println(formatter.format(date)+" INFO: "+yesCounterOverTime.get(lenthis)+" nodes labeled with 'Yes'.");
            lenthis = noCounterOverTime.size() - 1;
            date = new Date(System.currentTimeMillis());
            System.out.println(formatter.format(date)+" INFO: "+noCounterOverTime.get(lenthis)+" nodes labeled with 'No'.");
            lenthis = unknownCounterOverTime.size() - 1;
            date = new Date(System.currentTimeMillis());
            System.out.println(formatter.format(date)+" INFO: "+unknownCounterOverTime.get(lenthis)+" nodes with unknown labels.");

            // start the counters for the new iteration
            yesCounterOverTime.add(0);
            noCounterOverTime.add(0);
            unknownCounterOverTime.add(0);

            // for each node in the graph
            for (int k = 0; k < nodes.length; k++) {
                //current node
                int node = nodes[k];
                // neighbors of the current node
                int[] neighbours = g.in[node];
                newLabelNode = nodesLabels.get(node);
                
                // get an array of neighbors labels
                if (neighbours != null) {
                    int[] neighbourLabels = new int[neighbours.length];
                    for (int j = 0; j < neighbours.length; j++) {
                        int nj = neighbours[j];
                        int tmpLabel = nodesLabels.get(nj);
                        neighbourLabels[j] = tmpLabel;
                    }
                    // keep only -1 (yes) or -2(no) labels
                    ArrayList<Integer> neighbourLabelsYesNo = new ArrayList<Integer>();
                    for (int nl = 0; nl < neighbourLabels.length; nl++) {
                        int neighbourLabelsnl = neighbourLabels[nl];
                        if (neighbourLabelsnl == -1 || neighbourLabelsnl == -2) {
                            neighbourLabelsYesNo.add(neighbourLabelsnl);
                        }
                    }
                    
                    // if there is at least 1 neighbor get the most popular(frequent) label among the neighbor labels
                    // if not, keep current label
                    if (neighbourLabelsYesNo.size() > 0) {
                        int[] neighbourLabelsYesNoArray = neighbourLabelsYesNo.stream().mapToInt(i -> i).toArray();
                        newLabelNode = getPopularElement(neighbourLabelsYesNoArray);
                    } else {
                        newLabelNode = nodesLabels.get(node);
                    }
                    
                }
                
                // if the label didn't change increment the convergence flag
                if (nodesLabels.get(node).equals(newLabelNode)) {
                    counterConvergence++;
                }

                // Update the label
                nodesLabels.replace(node, newLabelNode);

                // update temporal counters
                if (newLabelNode == -1) { // if new label is a "yes" label
                    int len = yesCounterOverTime.size() - 1;
                    int counterLast = yesCounterOverTime.get(len);
                    yesCounterOverTime.remove(len);
                    yesCounterOverTime.add(counterLast + 1);
                } else if (newLabelNode == -2) {
                    int len = noCounterOverTime.size() - 1;
                    int counterLast = noCounterOverTime.get(len);
                    noCounterOverTime.remove(len);
                    noCounterOverTime.add(counterLast + 1);
                } else {
                    int len = unknownCounterOverTime.size() - 1;
                    int counterLast = unknownCounterOverTime.get(len);
                    unknownCounterOverTime.remove(len);
                    unknownCounterOverTime.add(counterLast + 1);
                }
            }
            
            // check convergence
            if (counterConvergence == nodes.length) {
            	date = new Date(System.currentTimeMillis());
                System.out.println(formatter.format(date)+" INFO: Iteration "+counter+" completed. The algorithm converged.");
                stop = true;
            }
            if (counter >= killAt) {
            	date = new Date(System.currentTimeMillis());
            	System.out.println(formatter.format(date)+" INFO: Iteration "+counter+" completed. The algorithm didn't converge yet.");
                stop = true;
            }

        }

        int counterYes = 0;
        int counterNo = 0;
        int counterUnknown = 0;
        for (int i = 0; i < nodes.length; i++) {
            if (nodesLabels.get(nodes[i]).equals(-1)) {
                counterYes++;
            } else if (nodesLabels.get(nodes[i]).equals(-2)) {
                counterNo++;
            } else {
                counterUnknown++;
            }
        }

        // print final results
        Date date = new Date(System.currentTimeMillis());
    	System.out.println(formatter.format(date)+" INFO: Algoritm completed.");
    	date = new Date(System.currentTimeMillis());
    	System.out.println(formatter.format(date)+" INFO: "+counterYes+" 'Yes' labels.");
    	date = new Date(System.currentTimeMillis());
    	System.out.println(formatter.format(date)+" INFO: "+counterNo+" 'No' labels.");
    	date = new Date(System.currentTimeMillis());
        System.out.println(formatter.format(date)+" INFO: "+counterUnknown+" 'Unknown' labels.");

        // save to files the temporal counters
        TxtUtils.iterableToTxt(AppConfigs.YES_LABELS_TEMPORAL_COUNTER, yesCounterOverTime);
        TxtUtils.iterableToTxt(AppConfigs.NO_LABELS_TEMPORAL_COUNTER, noCounterOverTime);
        TxtUtils.iterableToTxt(AppConfigs.UNKNOWN_LABELS_TEMPORAL_COUNTER, unknownCounterOverTime);
        
    }

    public int[] loadSeed(LongIntDict mapLong2Int, String seeds_filename) throws IOException {
    	// Load IDs from file
        List<String> seeds = TxtUtils.txtToList(seeds_filename, 0);

        // List that will contain the seeds mapped from Long to Integer
        int[] seedsAsInt = new int[seeds.size()];

        // Map the TwitterIDs from Long to integer
        for (int i = 0; i < seeds.size(); i++) {
            seedsAsInt[i] = mapLong2Int.get(Long.parseLong(seeds.get(i)));
        }
        return seedsAsInt;
    }

}
