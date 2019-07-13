package pipeline;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import algo.cooccurrence;
import algo.kmeans;
import it.stilo.g.structures.WeightedUndirectedGraph;
import org.apache.lucene.analysis.it.ItalianAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.util.Version;
import org.jfree.data.time.TimeSeriesCollection;
import twitter4j.TwitterException;
import index.indexTweets;
import index.politicianIndex;
import algo.saxAnalysis;
import utils.AppConfigs;
import utils.plot;

import static algo.saxAnalysis.countTweetTerm;
import static algo.saxAnalysis.extractDateRange;

public class TemporalAnalysisPipeline {
    /**
     * Create the user tweet index
     * @param sourcePath
     */

        public static void createTweetIndex(String sourcePath) {

            System.out.println("Tweets Index Creation!");
            String indexPath = AppConfigs.TWEET_INDEX;
            // Initialize a new TweetsIndexBuilder
            indexTweets indexAllTweets = new indexTweets(sourcePath, indexPath);
            Path dir = Paths.get(AppConfigs.TWEET_INDEX);
            if (!Files.exists(dir)) {
                try {
                    // Build the index
                    indexAllTweets.build();
                } catch (IOException ex) {
                    System.out.println("---> Problems with source files: IOException <---");
                    ex.printStackTrace();
                } catch (TwitterException ex) {
                    System.out.println("---> Problems with Tweets: TwitterException <---");
                    ex.printStackTrace();
                }
            } else {

                System.out.println(dir.toString() + ": Index already created!");
            }
        }

    /**
     * create the politician index
     * @param sourcePath
     */

    public static void createPoliticianIndex(String sourcePath) {

            System.out.println("Politician Index Creation!");
            String indexPath = AppConfigs.POLITICIAN_INDEX;
            // Initialize a new TweetsIndexBuilder
            politicianIndex indexPoliticians = new politicianIndex(sourcePath, indexPath);
            Path dir = Paths.get(AppConfigs.POLITICIAN_INDEX);
            if (!Files.exists(dir)) {
                try {
                    // Building the the index
                    indexPoliticians.build();
                } catch (IOException ex) {
                    System.out.println("---> Problems with source files: IOException <---");
                    ex.printStackTrace();
                }
//                catch (TwitterException ex) {
//                    System.out.println("---> Problems with Tweets: TwitterException <---");
//                    ex.printStackTrace();
//                }
            } else {

                System.out.println(dir.toString() + ": Index already created!");
            }
        }

    /**
     * create the yes/ no politician index
     * @param sourcePath
     */

    public static void dividePoliticians(String sourcePath) {

            System.out.println("Dividing Politicians in Groups Yes/No Index Creation!");
            String indexPath = AppConfigs.POLITICIAN_INDEX;
            // Initialize a new TweetsIndexBuilder
            politicianIndex indexPoliticians = new politicianIndex(sourcePath, indexPath);
            Path dir = Paths.get(AppConfigs.POLITICIAN_INDEX);
            // Divide politicians in YES and NO
            ArrayList<Document> yesPoliticians = indexPoliticians.search("vote", "yes", 10000);
            ArrayList<Document> noPoliticians = indexPoliticians.search("vote", "no", 10000);


//            if (yesPoliticians != null && noPoliticians != null) {
//                System.out.println("YES POLITICIANS: " + yesPoliticians.size());
//                System.out.println("NO POLITICIANS: " + noPoliticians.size());
//                System.out.println("TOT POLITICIANS: " + (yesPoliticians.size() + noPoliticians.size()));
//            }

            // Loading all the tweets
            String indexYes = AppConfigs.POLITICIAN_YES;
            indexTweets indexAllTweets = new indexTweets(sourcePath, indexYes);

            // If the index of all yes tweets doesn't exist
            dir = Paths.get(AppConfigs.POLITICIAN_YES);
            if (!Files.exists(dir)) {
                // Create it collecting all the yes ploticians screen name
                ArrayList<String> yesPoliticiansID = indexPoliticians.filter("vote", "yes", "screenName", 10000);
                try {
                    indexAllTweets.build("screenName", yesPoliticiansID);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {

                System.out.println(dir.toString() + ": Index already created!");
            }

            String indexNo = AppConfigs.POLITICIAN_NO;
            indexTweets indexAllTweetsNo = new indexTweets(sourcePath, indexNo);


            // If the index of all no tweets doesn't exist
            dir = Paths.get(AppConfigs.POLITICIAN_NO);
            if (!Files.exists(dir)) {
                // Create it collecting all the no ploticians screen name
                ArrayList<String> noScreenNames = indexPoliticians.filter("vote", "no", "screenName", 10000);
                try {
                    indexAllTweetsNo.build( "screenName", noScreenNames);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {

                System.out.println(dir.toString() + ": Index already created!");
            }

        }

    /**
     * plots the tweet distribution over time
     * @param indexLocation
     * @throws Exception
     * @throws IOException
     * @throws ParseException
     */
        public static void plotDistributions(String indexLocation) throws Exception, IOException, ParseException {
            saxAnalysis s = new saxAnalysis();
            long step = 43200000;
            long[] date = extractDateRange(indexLocation);
            System.out.println(date[0]);
            System.out.println(date[1]);

            HashMap<Long, Integer> distribution = s.ntweets(indexLocation, date[0], date[1], step);
            TimeSeriesCollection dataset = new TimeSeriesCollection();
            dataset = s.createDataset(dataset, distribution, "tweets distribution");
            plot p_yesno2 = new plot("Tweet Distirbution", "Tweet Distribution over time", dataset, AppConfigs.SAVE_IMAGE +"Tweet-Distro.png", 200);
            p_yesno2.pack();
            p_yesno2.setLocation(800, 20);
            p_yesno2.setVisible(true);
        }

    /**
     * plots the tweet distribution over time
     * @param indexLocation1
     * @param
     * @throws Exception
     * @throws IOException
     * @throws ParseException
     */
    public static void plotDistributionsYesNo(String indexLocation1, String indexLocation2) throws Exception, IOException, ParseException {
        saxAnalysis s = new saxAnalysis();
        long step = 43200000;
        long[] date = extractDateRange(indexLocation1);
        System.out.println(date[0]);
        System.out.println(date[1]);

        HashMap<Long, Integer> distribution = s.ntweets(indexLocation1, date[0], date[1], step);
        HashMap<Long, Integer> distribution2 = s.ntweets(indexLocation2, date[0], date[1], step);


        TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset = s.createDataset(dataset, distribution, "Yes");
        dataset = s.createDataset(dataset, distribution2, "No");
        plot p_yesno2 = new plot("Tweet Distirbution", "Tweet Distribution over time YES/NO", dataset, AppConfigs.SAVE_IMAGE +"Tweet-Distro-Yes-No.png", 200);
        p_yesno2.pack();
        p_yesno2.setLocation(800, 20);
        p_yesno2.setVisible(true);
    }

    /**
     * Performs the 2nd point related to the SAX Analysis on the indexes
     * @param indexLocation
     * @param saveClusterLocation
     * @param step
     * @throws Exception
     */
        public static void performSaxAnalysis(String indexLocation, String saveClusterLocation,long step) throws Exception {
            System.out.println("--- SAX ANALYSIS ---");
            int index = saveClusterLocation.lastIndexOf('/');
            Path dir = Paths.get(saveClusterLocation.substring(0,index));
            if (!Files.exists(dir)) {
                try {
                    saxAnalysis sax = new saxAnalysis();
                    long[] date = extractDateRange(indexLocation);
                    System.out.println("Extracting Top 1000 Terms ....");
                    ArrayList topN = sax.extractTopNTerms(1000, indexLocation);
                    ItalianAnalyzer analyzer = new ItalianAnalyzer(Version.LUCENE_41);
                    System.out.println("Creating Terms Time Series...");
                    HashMap<String, double[]> countOccurenceTs = sax.countOccurenceOverTime(topN, date[0], date[1], step, indexLocation);

                    //System.out.println(createTermsTimeSeries(indexLocation, topN,date[1], date[0], step ));
                    System.out.println("Converting Timeseries to SAX Representation...");
                    HashMap<String, String> saxRep = sax.buildSaxRepresentation(countOccurenceTs, 20);
                    System.out.println("Performing k-means clustering...");
                    kmeans km = new kmeans(saxRep, 20);
                    Map<String, Integer> res;
                    res = km.fit();
                    System.out.println(res);
                    System.out.println("Saving Term clusters to Disk ....");
                    sax.saveAllClusters(20, saveClusterLocation, res, saxRep);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                // Advise the index already exist
                System.out.println(dir.toString() + ": SAX Analysis Already performed");
            }
        }

    /**
     * Perfomr the 3rd point regarding the Coocurence analysis, with the creationg of the LCC and the KCORES
     * @param clusterLocation
     * @param saveLocationKcore
     * @param saveLocationLCC
     * @param k
     * @throws IOException
     */

        public static void performCoocurenceAnalysis(String clusterLocation, String saveLocationKcore,
                                                     String saveLocationLCC, int k) throws IOException {

            Path dir = Paths.get(saveLocationLCC);
            if (!Files.exists(dir)) {
                try {
                    cooccurrence c = new cooccurrence(k, clusterLocation);
                    Map<Integer, ArrayList> clusters = c.getClusterTerms(k);
                    System.out.println(clusters);

                    for (int i = 0; i < 20; i++) {
                        try {
                            System.out.println("Working with clusters #" + i);
                            System.out.println("Creating graph for cluster #" + i + " ...");

                            WeightedUndirectedGraph graph = c.createGraph(clusters.get(i));
                            System.out.println("Extracting largest connected component for clusters #" + i);
                            WeightedUndirectedGraph largestCC = c.getLargestConnectedComponent(graph, 2);
                            // saveLCC = new PrintWriter(new FileWriter(co_lcc+i+".txt"));
                            c.saveGraphToFile(largestCC, saveLocationLCC + i + ".txt");
                            //saveLCC.close();
                            System.out.println("Extracting K-cores component for clusters #" + i);
                            WeightedUndirectedGraph kcores = c.extractKCores(graph, 2);
                            //PrintWriter saveKcores = new PrintWriter((new FileWriter(co_kcore+i+".txt")));
                            c.saveGraphToFile(kcores, saveLocationKcore + i + ".txt");
                            //saveKcores.close();
                            //c.getLargestConnectedComponent(graph,2);
                        } catch (Exception e) {
                            continue;
                        }
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            } else {
                
                System.out.println(dir.toString() + ": Coocurence Analysis Already performed");
            }
        }
    /**
     * performs the 4th point related to ploting the various timeseries and observing the various action reaction present
     * @param indexLocation
     * @param clustersLocation
     * @param cl
     * @param grain
     * @param saveName
     * @throws Exception
     * @throws IOException
     * @throws ParseException
     */
    public static void plotTSComparaison(String indexLocation, String clustersLocation,String cl, long grain, String saveName) throws Exception, IOException, ParseException {
        saxAnalysis s = new saxAnalysis();
        cooccurrence c = new cooccurrence(20, cl);
        String term;
        System.out.println("Extracting Date range");
        //long[] date = s.extractDateRange(indexLocation);
        String currentFile;
        indexTweets it = new indexTweets("", indexLocation);
        System.out.println("Extracting LCC");
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(clustersLocation))) {
            for (Path path : directoryStream) {
                // get the current file within the cluster and extract just the cluster nunmber
                currentFile = path.toString();
                System.out.println("Working with file: "+ currentFile);
                Set<Integer> termToCompare = getTermToCompare(currentFile);
                currentFile= currentFile.replaceAll("[^0-9]", "");
                // extract the cluster terms from the cluster bynber
                ArrayList<String> clusterTerms = c.loadCluster(Integer.parseInt(currentFile));
                TimeSeriesCollection dataset = new TimeSeriesCollection();
                for(Integer i : termToCompare){

                    term = clusterTerms.get(i).split(" ", 2)[0];
                    System.out.println(term);
                    HashMap<Long, Integer> distribution = countTweetTerm(indexLocation,term, 1480170621755L, 1481035172059L, 10800000L);
                    dataset = s.createDataset(dataset, distribution, term);

                }
                System.out.println("Plotting :" +path.toString());
                plot plt = new plot("Tweet Distribution", "Temporal Distribution of related tweet terms", dataset, AppConfigs.SAVE_IMAGE+saveName+currentFile+".png", 200);

            }
        } catch (IOException ex) {System.out.println(ex);}

    }

    /**
     * helper to conver the numbers on a node to a word
     * @param file
     * @return
     */

        private static Set<Integer> getTermToCompare(String file){
            Set<Integer>  termToCompare = new HashSet<>();
            try(BufferedReader in = new BufferedReader(new FileReader(file))) {
                String str;
                while ((str = in.readLine()) != null) {
                    String[] tokens = str.split(",");
                    termToCompare.add(Integer.parseInt(tokens[0]));
                }
                return termToCompare;
            }
            catch (IOException e) {
                System.out.println("File Read Error");
                return termToCompare ;
            }
        }

        public static void runTemporalAnalysisPipeline() throws  Exception{
            int k = 20;

            String sourceTweets = AppConfigs.TWEET_STREAM;
            String sourcePoliticians = AppConfigs.ORIGINAL_POLITICIANS;
            String indexTweets = AppConfigs.TWEET_INDEX;
            String indexNoPoliticians = AppConfigs.POLITICIAN_NO;
            String indexYesPoliticians = AppConfigs.POLITICIAN_YES;
            String clusterLocationYes = AppConfigs.CLUSTER_LOCATION_YES;
            String clusterLocationNo = AppConfigs.CLUSTER_LOCATION_NO;
            String caKcoreNo = AppConfigs.CA_KCORE_NO;
            String caKcoreYes = AppConfigs.CA_KCORE_YES;
            String caLCCNo = AppConfigs.CA_LCC_NO;
            String caLCCYes =AppConfigs.CA_LCC_YES;

            long grain12 = 43200000;
            long grain3 = 10800000;
            createTweetIndex(sourceTweets);
            createPoliticianIndex(sourcePoliticians);
             dividePoliticians(sourcePoliticians);
//             plotDistributions(indexTweets);
             plotDistributionsYesNo(indexYesPoliticians, indexNoPoliticians);
            performSaxAnalysis(indexYesPoliticians,clusterLocationYes, grain12);
            performSaxAnalysis(indexYesPoliticians,clusterLocationNo, grain12);
            performCoocurenceAnalysis(clusterLocationNo, caKcoreNo,caLCCNo, k);
            performCoocurenceAnalysis(clusterLocationYes, caKcoreYes, caLCCYes, k);
            plotTSComparaison(indexTweets,caLCCNo,clusterLocationNo,grain3,"LCCNO");
            plotTSComparaison(indexTweets,caLCCNo,clusterLocationNo,grain3,"KCORENO");
            plotTSComparaison(indexTweets,caLCCYes,clusterLocationYes,grain3,"LCCYES");
            plotTSComparaison(indexTweets,caLCCYes,clusterLocationYes,grain3,"KCOREYES");


        }
//        public static void main(String[] args) throws Exception {
//            int k = 20;
//            String sourceTweets = "./src/util/data/stream";
//            String sourcePoliticians = "./src/util/list_politician.csv";
//            String indexTweets = "./index/indexTweets";
//            String indexNoPoliticians = "./index/indexNo";
//            String indexYesPoliticians = "./index/indexYes";
//            String clusterLocationYes = "./yesClusters/yes_";
//            String clusterLocationNo = "./noClusters/no_";
//            String caKcoreNo = "./cooc/kcore/no/";
//            String caKcoreYes = "./cooc/kcore/yes/KCORE_YES_";
//            String caLCCNo ="./cooc/lcc/no/";
//            //String caLCCYes ="./ccoc/lcc/yes/LCC_YES_";
//            String caLCCYes = "./cooc/lcc/yes/LCC_YES_";
//
//            long grain12 = 43200000;
//            long grain3 = 10800000;
////            createTweetIndex(sourceTweets);
////            createPoliticianIndex(sourcePoliticians);
////            dividePoliticians(sourcePoliticians);
//            //plotDistributions(indexNoPoliticians);
//            //performSaxAnalysis(indexYesPoliticians,clusterLocationYes, grain12);
////            performSaxAnalysis(indexYesPoliticians,clusterLocationNo, grain12);
////            performCoocurenceAnalysis(clusterLocationNo, caKcoreNo,caLCCNo, k);
//            performCoocurenceAnalysis(clusterLocationYes, caKcoreYes, caLCCYes, k);
//            //plotTSComparaison(indexTweets,caLCCNo,clusterLocationNo,grain3,"LCCNO");
//            //plotTSComparaison(indexTweets,caKcoreNo,clusterLocationNo,grain3,"KCORENO");
//            plotTSComparaison(indexTweets,caLCCYes,clusterLocationYes,grain3,"LCCYES");
//            plotTSComparaison(indexTweets,caKcoreYes,clusterLocationYes,grain3,"KCOREYES");
//
//
//
//        }

    }
