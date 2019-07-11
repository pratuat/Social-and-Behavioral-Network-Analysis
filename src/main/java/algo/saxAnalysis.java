package algo;



import java.io.*;
import java.util.*;
import net.seninp.jmotif.sax.SAXException;
import org.apache.commons.lang3.CharUtils;
import org.apache.lucene.analysis.it.ItalianAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.index.*;
import org.apache.lucene.misc.HighFreqTerms;
import org.apache.lucene.misc.TermStats;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;
import net.seninp.jmotif.sax.alphabet.NormalAlphabet;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.jfree.data.time.*;
import org.jfree.data.xy.XYDataset;
import utils.sax;

import utils.plot;

import javax.print.attribute.standard.MediaSize;

public class saxAnalysis {

    public static final String stopwordsLocation = "src/util/stopwords.txt";

    /**
     * Create the SAX representation of the top N more frequent terms
     * @param termCountOccurenceTS : a Hashmap countaining as key the term and as value an array containing
     *                             the count of a term at each time interval
     * @param sizeAlphabet: number of levels we have 
     * @return A hashmap containing as key the terms and as value a string which each letter corresponding to the
     *          count of the word represented as a letter. 
     * @throws SAXException
     */

    public static HashMap<String, String> buildSaxRepresentation(HashMap<String, double[]> termCountOccurenceTS, int sizeAlphabet) throws SAXException {
        // initialization and normalization of main parameters
        HashMap<String, String> saxRepresentation = new HashMap<>();
        double maxCount = getMaxCount(termCountOccurenceTS);
        HashMap<String, double[]> normalizedTermCountTS = normalize(termCountOccurenceTS);

        // building the sax representation for each term
        normalizedTermCountTS.keySet().forEach((term) -> {
            String sax;
            try {
                sax saxUtils = new sax(sizeAlphabet, Math.round(sizeAlphabet / maxCount));
                sax = saxUtils.createSAX(normalizedTermCountTS.get(term));
                saxRepresentation.put(term, sax);
            } catch (SAXException ex) {
                System.out.println(ex.getMessage());
            }
        });
        return saxRepresentation;
    }



    /**
    Method to return the date range within an index
        @param  - indexLocation Location of the index
        @return - min and max date
        @trows IOException

     */
    public static long[] extractDateRange(String indexLocation) throws IOException {
        long date;
        long[] dateRange = new long[2];
        long minDate = Long.MAX_VALUE;
        long maxDate = -11111;

        Directory dir = new SimpleFSDirectory(new File(indexLocation));
        DirectoryReader ir = DirectoryReader.open(dir);
        Document tweet;

        for (int i = 0; i < ir.maxDoc(); i++) {
            tweet = ir.document(i);
            //System.out.println(tweet);
            if ((date = Long.parseLong(tweet.get("date"))) < minDate) {
                minDate = date;
            }
            if ((date = Long.parseLong(tweet.get("date"))) > maxDate) {
                maxDate = date;
            }
        }

        dateRange[0] = minDate;
        dateRange[1] = maxDate;
//        System.out.println(dateRange[0]);
//        System.out.println(dateRange[1]);
        return dateRange;

    }

    /**
     Method to return the top N post frequent terms within the index
     @param  N - the number of terms to be selected
     @param  indexLocation: the location of the index file
     @return ArrayList<String> top-terms the top N post frequent terms within the Lucene index
     @trows Exception

     */
    public static ArrayList<String> extractTopNTerms(int N, String indexLocation) throws IOException, Exception {

        Directory dir = new SimpleFSDirectory(new File(indexLocation));
        DirectoryReader ir  = DirectoryReader.open(dir);
        TermStats[] ts = HighFreqTerms.getHighFreqTerms(ir, N, "tweetText");
        ArrayList<String> topNTerms = new ArrayList<>();
        for (TermStats t : ts) {

            topNTerms.add(t.termtext.utf8ToString());
        }
        return topNTerms;

    }

    /**
     * Method to count the occurence of N terms over time
     * @param indexLocation : Path of the index to be searched
     * @param termList: List of terms to be searched
     * @param dateEnd: Latest/last/limit date
     * @param dateStart: start date
     * @param step: size of each interval which words need to be counted
     * @throw Exception
     * @return A Hashmap containing the terms as key and an array of numbers corresponding to the occurence
     *          of each term in a step
    */
    public static HashMap<String, double[]> countOccurenceOverTime( ArrayList<String> termList, long dateStart, long dateEnd,  long step, String indexLocation) throws IOException, Exception {
        // initialize the parameters
        // index reader
        File f = new File(indexLocation);
        FSDirectory dir = FSDirectory.open(f);
        IndexReader ir = DirectoryReader.open(dir);
        Document doc;
        ItalianAnalyzer analyzer = new ItalianAnalyzer(Version.LUCENE_41, getStopwords(stopwordsLocation));
        int currentTimeSlot;

        // getting the number of slots based on the grain
        HashMap<String, double[]> termCountTS = new HashMap<>();
        int steps = (int) Math.ceil((dateEnd - dateStart) / step) + 1;
        termList.forEach((term) -> {
            termCountTS.put(term, new double[steps]);
        });

        Long tweet;
        double[] currentCount;
        // iteration over the whole index
        for (int i = 0; i < ir.maxDoc(); i++) {
            doc = ir.document(i);
            tweet = new Long(doc.get("date"));
            // get the time slot the tweet belongs to
            currentTimeSlot = (int) Math.floor((tweet - dateStart) / step);
            // get the current term and count its occurence in each tweet at that current time slot
            for (String term : tokenizeString( analyzer, doc.get("tweetText"))) {
                if (termCountTS.containsKey(term)) {
                    // if tweet occurs update get the term from hashmap
                    currentCount = termCountTS.get(term);
                    // increase the count by 1 at  that currentTimeslot
                    currentCount[currentTimeSlot]++;
                    // update the count
                    termCountTS.put(term, currentCount);
                }
            }
        }
        return termCountTS;
    }

    /**
     *
     * @param analyzer: the Lucene language analyser to be used
     * @param string: the text to be tokenized
     * @return An array containing the words/tokens contained in the array
     */
    public static ArrayList<String> tokenizeString(ItalianAnalyzer analyzer, String string) {
        ArrayList<String> result = new ArrayList<String>();
        try {
            TokenStream stream  = analyzer.tokenStream(null, new StringReader(string));
            stream.reset();
            while (stream.incrementToken()) {
                result.add(stream.getAttribute(CharTermAttribute.class).toString());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return result;
    }




    /**
     * Method that saves all cluster to file
     * @param clusterNumbers - cluster number identified by the kmeans
     * @param loc - save location
     * @param clusters - the term to cluster association
     * @param saxRepresentation - the saxRepresentation of the word
     * @throws IOException
     */
    public static void saveAllClusters (int clusterNumbers, String loc, Map <String, Integer> clusters,
                                      HashMap<String, String> saxRepresentation)throws IOException{

        for (int i=0; i < clusterNumbers; i++){
            writeClusters(i,loc+i+".txt", clusters, saxRepresentation);
        }

    }

    /**
     * Return the number of tweets in a time interval
     * @param indexLoc Location of the index
     * @param start start date
     * @param end end date
     * @param steps step size
     * @return return the number of tweets for each time steps
     * @throws IOException
     * @throws org.apache.lucene.queryparser.classic.ParseException
     */
    public static HashMap<Long,Integer> ntweets(String indexLoc, long start, long end, long steps) throws IOException, org.apache.lucene.queryparser.classic.ParseException {
        HashMap<Long,Integer> result = new HashMap<>();
        //String indexLocation = "./index/indexTweets";
        String indexLocation = indexLoc;
        Directory dir = new SimpleFSDirectory(new File(indexLocation));
        DirectoryReader ir = DirectoryReader.open(dir);
        IndexSearcher searcher = new IndexSearcher(ir);
        while (start <= end) {
            Query query = NumericRangeQuery.newLongRange("date", start, start+steps, true, false);
            TopDocs count = searcher.search(query, 1000000000);
            result.put(start, count.totalHits);
            start = start+steps;
        }
        return result;
    }

    /**
     * Return the number of tweets in a time interval
     * @param indexLoc Location of the index
     * @param start start date
     * @param end end date
     * @param steps step size
     * @return return the number of tweets for each time steps
     * @throws IOException
     * @throws org.apache.lucene.queryparser.classic.ParseException
     */
    public static HashMap<Long,Integer> countTweetTerm(String indexLoc,String term, long start, long end, long steps) throws IOException, org.apache.lucene.queryparser.classic.ParseException {
        HashMap<Long,Integer> result = new HashMap<>();
        //String indexLocation = "./index/indexTweets";
        String indexLocation = indexLoc;
        Directory dir = new SimpleFSDirectory(new File(indexLocation));
        DirectoryReader ir = DirectoryReader.open(dir);
        IndexSearcher searcher = new IndexSearcher(ir);
        while (start <= end) {
            BooleanQuery query = new BooleanQuery();
            Query q = NumericRangeQuery.newLongRange("date", start, start+steps, true, false);

            query.add(q, BooleanClause.Occur.MUST);
            Query t = new TermQuery(new Term("tweetText", term));
           // Query t2 = new TermQuery(new Term("hashtags", term));
            query.add(t, BooleanClause.Occur.MUST);
            //query.add(t2,BooleanClause.Occur.SHOULD );
            TotalHitCountCollector collector = new TotalHitCountCollector();

             searcher.search(query, collector);
            //System.out.println(collector.getTotalHits());
            //TopDocs count = searcher.search(query, 1000000000);
            int count= collector.getTotalHits();
            result.put(start, count);
            //System.out.println(count);
            start = start+steps;
        }
        return result;
    }

//    BooleanQuery query = new BooleanQuery();
//        query.add(t1, BooleanClause.Occur.MUST);
//    TotalHitCountCollector collector = new TotalHitCountCollector();
//        searcher.search(t1, collector);
//
//        return (collector.getTotalHits());

    /**
     * Return the converted dataset for plotting
     * @param dataset Data set for timeserie plotting
     * @param values Numbers to ve added from plotting
     * @param term - term to be converted to serie
     * @return a timeseries dataset for plotting
     */
    public static TimeSeriesCollection createDataset(TimeSeriesCollection dataset, HashMap<Long, Integer> values, String term) {
        TimeSeries series = new TimeSeries(term);
        for (HashMap.Entry<Long,Integer> entry : values.entrySet()){
            series.add(new Hour(new Date(entry.getKey())), entry.getValue());
        }
        dataset.addSeries(series);
        return dataset;
    }

    /**
     * Get the maximum value/count
     * @param termCount - - count over time of the of the top N most frequent terms
     * @return a number representing the maximum value
     */
    private static double getMaxCount(HashMap<String, double[]> termCount){
        TreeSet totalCount = new TreeSet();
        termCount.values().forEach((double[] count) -> {
            for (int i = 0; i < count.length; i++) {
                totalCount.add(count[i]);
            }
        });
        double maxCount = (double) totalCount.last();

        return maxCount;

    }

    /**
     * Normilize the valious term frequency between 0 and 1
     * @param termCount - count over time of the of the top N most frequent terms
     * @return a hashmap containing the term as a key and as value normalized term count occurence
     */
    private static HashMap<String, double[]> normalize(HashMap<String, double[]> termCount){
        TreeSet totalCount = new TreeSet();
        // get the total occurence count
        termCount.values().forEach((double[] count) -> {
            for (int i = 0; i < count.length; i++) {
                totalCount.add(count[i]);
            }
        });
        double maxCount = (double) totalCount.last();
        // divide by max to get nomalized values
        termCount.values().forEach((double[] count) -> {
            for (int i = 0; i < count.length; i++) {
                count[i] = count[i] / maxCount;
            }
        });

        return termCount;

    }

    /**
     *
     * @param loc - location of the stopword directory
     * @return return a charArrayset corresponding to the list of stopwords which should not be considered for the analysis
     */
    private static final CharArraySet getStopwords(String loc){
        CharArraySet result = CharArraySet.copy(Version.LUCENE_41, ItalianAnalyzer.getDefaultStopSet());

        try {
            // initialize the reader
            FileInputStream inputStream = new FileInputStream(loc);
            InputStreamReader inputReader = new InputStreamReader(inputStream);
            BufferedReader br = new BufferedReader(inputReader);
            String line;
            ArrayList<String> stopwords = new ArrayList();
            // read every stopwork until no more
            while ((line = br.readLine()) != null) {
                stopwords.add(line);
            }
            result.addAll(stopwords);
            return result;

        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            return result;
        }

    }

    /**
     * method that save one cluster to file
     * @param clusterNumber - cluster number identified by the kmeans
     * @param loc - save location
     * @param clusters - the term to cluster association
     * @param saxRepresentation - the saxRepresentation of the word
     * @throws IOException
     */
    private static void writeClusters (int clusterNumber, String loc, Map <String, Integer> clusters,
                                       HashMap<String, String> saxRepresentation)throws IOException{

        File file = new File(loc);
        file.getParentFile().mkdirs();
        PrintWriter pw = new PrintWriter(new FileWriter(file));


        Object[] clusterTerms =  kmeans.filterByValue(clusters,value -> value == clusterNumber).keySet().toArray();
        for (int i =0; i<clusterTerms.length; i++){
            pw.println(clusterTerms[i] + " " + saxRepresentation.get(clusterTerms[i]));
        }
        pw.close();
    }

    public static void main(String[] args) throws IOException, Exception{
//        String sourceTweets = "./src/util/data/stream";
//        String sourcePoliticians = "./src/util/list_politician.csv";
          String indexLocation = "./index/indexNo";
//        String indexYes = "./index/indexYes";
//        String indexTweets = "./index/indexTweets";
        long step = 43200000;
//        long[] date = extractDateRange(indexLocation);
//        ArrayList topN = extractTopNTerms(100, indexLocation);
//        ItalianAnalyzer analyzer = new ItalianAnalyzer(Version.LUCENE_41);
//        HashMap<String, double[]> hm = createTermsTimeSeries(indexLocation, topN,date[1], date[0], step );
//        System.out.println(createTermsTimeSeries(indexLocation, topN,date[1], date[0], step ));
//        HashMap<String, String> saxRep = fromFreqVectorsToSAXStrings(hm,15);
//        kmeans km = new kmeans(saxRep,10);
//        Map<String, Integer> res ;
//        res = km.fit();
//        System.out.println(res);
//
//
//        //System.out.println(ntweets(date[0], date[1], step));
//        HashMap<Long, Integer> distribution = ntweets(indexLocation, date[0], date[1], step);
//        HashMap<Long, Integer> distribution2 = ntweets(indexYes, date[0], date[1], step);

//        File file = new File("./src/util/temp");
//        FileOutputStream f = new FileOutputStream(file);
//        ObjectOutputStream s = new ObjectOutputStream(f);
//        s.writeObject(distribution);
//        s.close();
//        File file = new File("./src/util/temp");
//        FileInputStream f = new FileInputStream(file);
//        ObjectInputStream s = new ObjectInputStream(f);
//        HashMap<Long, Integer> distribution = (HashMap<Long, Integer>) s.readObject();
//        s.close();
//        TimeSeriesCollection dataset = new TimeSeriesCollection();
//        dataset = createDataset(dataset, distribution, "tweets distribution");
//        //dataset= createDataset(dataset,distribution2, "tweet distribution");
//        plot p_yesno2 = new plot("Tweet Distirbution", "Tweet Distribution over time", dataset, "./src/util/Tweets-YN.png", 200);
//       // p_yesno2.pack();
//        //p_yesno2.setLocation(800, 20);
//        p_yesno2.setVisible(true);


        String clusterLocation = "./noClusters/No_";
        //saveAllClusters(3, clusterLocation,res,saxRep);
        //final int v = 0;
        //Object[] r = kmeans.filterByValue(res,value -> value == v).keySet().toArray();
       // System.out.println(saxRep.get(r[v]));

       // String test = "0000t";
        //System.out.println(test + 1);
        long[] date = extractDateRange(indexLocation);
        System.out.println(date[0]);
        System.out.println(date[1]);
        countTweetTerm(indexLocation,"fondi", date[0], date[1], 10800000L);

//        fondi
//                renzi
//        sostegn
    }
}