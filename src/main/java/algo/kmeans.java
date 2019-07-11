package algo;

//import com.sun.jdi.event.ExceptionEvent;

//import com.sun.jdi.event.ExceptionEvent;
//import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;


import java.io.UnsupportedEncodingException;
import java.util.*;
import java.io.*;
import java.util.HashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Kmeans implementation for SAX strings
 * 
 */
public class kmeans {

    private Map<String, Integer> clusters;
    private int k;
    private ArrayList<String> timeserie;
    private String[] terms;
    private HashMap<String, String> sax;


    /**
     * constructor
     * @param sax - hash map containing term as key and as value its sax representation
     * @param k - number of clusters
     */
    public kmeans(HashMap<String, String> sax, int k){
        this.sax = sax;
        this.k = k;
        this.terms=  sax.keySet().toArray(new String[sax.size()]);
        this.clusters = new HashMap<>();
    }

    /**
     * 
     * @return Hashmap which has the term as key and the assigned cluster value as Value
     * @throws Exception
     */
    
    public Map<String, Integer> fit() throws Exception {
        // Initialize the error
        int currentError;
        int updatedError=99;
        // We select our initial centroids randomly from the terms
        String[] centroids = selectInitialCentroids();

        do {
            // Start the error traking
            currentError = updatedError;
            updatedError = 0;
            for (int i = 0; i < terms.length; i++) {
                double minDistance = Integer.MAX_VALUE;
                // get sax representation of current terms
                String saxString = this.sax.get(terms[i]);
                // initialize the distance value
                double distance;
                int updatedCluster = 0;
                for (int cluster = 0; cluster < k; cluster++) {
                    // evaluate the distance between the current SAX string and each centroid
                    //distance=   StringUtils.getLevenshteinDistance(saxString, centroids[clusterID]);
                    distance = StringUtils.getJaroWinklerDistance(saxString, centroids[cluster]);
                    //distance = calculateDistance(saxString, centroids[clusterID]);

                    // if the lowest distance to a centroid will be the cluster the terms belongs to
                    if (distance < minDistance) {
                        minDistance = distance;
                        updatedCluster = cluster;
                    }
                }
                updatedError += minDistance; // update the total error
                // get the total elements within a cluster
                int count = Collections.frequency(this.clusters.values(), this.clusters.get(terms[i]));
                // if the term exist already in a cluster
                if (this.clusters.containsKey(terms[i])) {
                    int previousCluster = this.clusters.get(terms[i]);
                    // and the previous cluster is different than the new one
                    // move the term into a new cluster
                    if (previousCluster != updatedCluster && count>10) {
                        this.clusters.put(terms[i], updatedCluster);
                    }
                }
                else{
                    // else leave the term in the current cluster
                    this.clusters.put(terms[i], updatedCluster);
                }
            }
            // update the clusters centroids
            centroids = updateCentroids(this.clusters);
        } while (currentError != updatedError);

        return this.clusters;
    }

    /**
     *
     * @return Random initial centroids for kmean analysis
     */
    private String[] selectInitialCentroids(){
        String[] centroids = new String[this.k];
        Random random = new Random();
        for (int i = 0; i < this.k; i++){
            int cent = random.nextInt(terms.length);
            centroids[i] = sax.get(terms[cent]);
        }
        return centroids;
    }

    /**
     *
     * @param cluster a hashmap with terms as key and corresponding cluster as value
     * @return an updated list of centroid
     */
    private String[] updateCentroids(Map<String, Integer> cluster) {
        //ArrayList<String> clusterList = new ArrayList<>(cluster);  // convert to list to allow indexed
        String[] centroid = new String[this.k];
        //centroid = new String[]{"best";
        int saxStringSize = sax.get(terms[0]).length();


        // extract all the clusters
        for (int c = 0; c<k ; c++) {
            int cc = c;
            //System.out.println(cc);
            Map<String,Integer> currentCluster = filterByValue(this.clusters, value -> value == cc);
             System.out.println(currentCluster);

            // teking the averache char value for all string in the cluster
            for (int i = 0; i < saxStringSize; i++) {
                char[] chars = new char[currentCluster.size()];
                for (int j = 0; j < currentCluster.size(); j++) {
                    String currentTerm = (String) sax.keySet().toArray()[j];
                    chars[j] = sax.get(currentTerm).charAt(i);

                }
                centroid[cc] += getClusterAverage(chars);
            }

            centroid[cc]= centroid[cc].substring(4);

        }
        //return centroid;
        //System.out.println(centroid[0]);


        return centroid;
    }

    /**
     *
     * @param chars correspond to a list of charater within the cluster
     * @return the average character from the list
     */

    private  char getClusterAverage(char[] chars){
        int average = 0;
        //System.out.println(chars);
        for (int i = 0; i < chars.length; i++) {
            average += (int) chars[i];
        }
        average =  Math.round( (float) average / chars.length);
        //System.out.println( (char) average);
        return (char)average;
    }

    /**
     * Extract the clusters sax strings
     * @param map
     * @param predicate
     * @param <K>
     * @param <V>
     * @return
     */
    static <K, V> Map<K, V> filterByValue(Map<K, V> map, Predicate<V> predicate) {
        return map.entrySet()
                .stream()
                .filter(entry -> predicate.test(entry.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }


    public static void main(String[] args) throws UnsupportedOperationException{
        try{
        HashMap<String, String> hash_map = new HashMap<>();

        hash_map.put("fare", "aaabaaaaabbbbabaaaaaa");
        hash_map.put("ultim", "abababaaabbbbaaaaaaaa");
        hash_map.put("casa", "aaaaaaaaaaaabaaaaaaaa");
        hash_map.put("govern", "baabaaaabbabaaaababaa");
        hash_map.put("nuovo", "baabaababbbbbbaababaa");
        System.out.println( hash_map);

        //String s = "admin";
        byte[] bytes = hash_map.get("fare").getBytes("US-ASCII");
        System.out.println(bytes);
        System.out.println(bytes[3]);
        String[] keys = hash_map.keySet().toArray(new String[hash_map.keySet().size()]);
        System.out.println(keys[0]);
         double dis=   StringUtils.getLevenshteinDistance(hash_map.get("fare"), hash_map.get("fare"));
         System.out.println(dis);



         kmeans s  = new kmeans(hash_map,2);
        // System.out.println();
         s.fit();
         System.out.println(s.clusters);



        }
    catch (Exception e){

        }
    }
}
