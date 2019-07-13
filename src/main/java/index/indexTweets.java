package index;

import org.apache.lucene.document.TextField;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.it.ItalianAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;

import io.TxtUtils;

import static io.TxtUtils.txtToList;
import static org.apache.lucene.util.Version.LUCENE_41;
import org.apache.lucene.util.BytesRef;
import twitter4j.HashtagEntity;
import twitter4j.TwitterException;
import twitter4j.UserMentionEntity;
import utils.AppConfigs;
import utils.StatusWrapper;

@SuppressWarnings("unused")
public class indexTweets extends buildIndex {
    // Setting up the main variable within the tweets
    private LongField date;
    private Document tweet;
    private TextField tweetText;
    private LongField userId;
    private LongField followers;
    private StringField name;
    private StringField screenName;
    private TextField hashtags;
    private TextField mentioned;
    private PerFieldAnalyzerWrapper pfaWrapper;
    private Map<String, Analyzer> analyzers;

    // Constructor initialize every variable within class
    public indexTweets(String stream, String index) {
        // Various paths
        this.stream = stream;
        this.index = index;
        // Initialize the tweet
        this.tweet = new Document();
        // Initialize the field within the tweet
        this.date = new LongField("date", 0L, Field.Store.YES);
        this.userId = new LongField("userId", 0L, Field.Store.YES);
        this.tweetText = new TextField("tweetText", "", Field.Store.YES);
        this.followers = new LongField("followers", 0L, Field.Store.YES);
        this.name = new StringField("name", "", Field.Store.YES);
        this.screenName = new StringField("screenName", "", Field.Store.YES);
        this.hashtags = new TextField("hashtags", "", Field.Store.YES);
        this.mentioned = new TextField("mentioned", "", Field.Store.YES);
        // assign the tweet fields within the document
        this.tweet.add(this.date);
        this.tweet.add(this.userId);
        this.tweet.add(this.tweetText);
        this.tweet.add(this.followers);
        this.tweet.add(this.name);
        this.tweet.add(this.screenName);
        this.tweet.add(this.hashtags);
        this.tweet.add(this.mentioned);
    }

    @Override
    public void params(String dirName) throws IOException {
        this.dir = new SimpleFSDirectory(new File(dirName));
        analyzers = new HashMap<String, Analyzer>();
        analyzers.put("tweetText", new ItalianAnalyzer(LUCENE_41));
        //analyzers.put("hashtags", new ItalianAnalyzer(LUCENE_41));
        pfaWrapper = new PerFieldAnalyzerWrapper(new WhitespaceAnalyzer(LUCENE_41), analyzers);
        this.cfg = new IndexWriterConfig(LUCENE_41, pfaWrapper);
        this.writer = new IndexWriter(dir, cfg);
    }

    @Override
    public void build() throws IOException, TwitterException {
        // Initialization (replace source path with default path)
        Path sourceDirPath = Paths.get(stream);
        params(index);

        // Get all the streams from the data provides
        DirectoryStream<Path> allStreams = Files.newDirectoryStream(sourceDirPath);

        // Initialization of the status wrapper which was provided
        StatusWrapper sw;
        int streamNumber = 1;
        // For each directory in the stream
        for (Path stream : allStreams) {
            System.out.println(stream);

            // get all files withing a stream folder
            DirectoryStream<Path> dirStream = Files.newDirectoryStream(stream);
            int count = 1;
            int totalFiles = new File(stream.toString()).listFiles().length;
            // For each file within a stream
            for (Path f : dirStream) {

                System.out.println(streamNumber + ") " + count + " out of " + totalFiles);
                count++;
                FileInputStream inputStream = new FileInputStream(f.toString());
                GZIPInputStream gzstream = new GZIPInputStream(inputStream);
                InputStreamReader inputReader = new InputStreamReader(gzstream, "UTF-8");
                BufferedReader br = new BufferedReader(inputReader);
                String line;
                // unzip the file and read the data
                while ((line = br.readLine()) != null) {
                    // reading the data was done using functionalities of StatusWrapper
                    sw = new StatusWrapper();
                    sw.load(line);
                    // get the various values for the tweets per field
                    String mentioned = "";
                    for (UserMentionEntity user : sw.getStatus().getUserMentionEntities()) {
                        mentioned += user.getText() + " ";
                    }
                    String hashtags = "";
                    for (HashtagEntity hashtag : sw.getStatus().getHashtagEntities()) {
                        hashtags += "#" + hashtag.getText() + " ";
                    }
                    this.date.setLongValue(sw.getTime());
                    this.userId.setLongValue(sw.getStatus().getUser().getId());
                    this.tweetText.setStringValue(formatText(sw.getStatus().getText()));
                    this.followers.setLongValue((long) sw.getStatus().getUser().getFollowersCount());
                    this.name.setStringValue(sw.getStatus().getUser().getName().toLowerCase());
                    this.screenName.setStringValue(sw.getStatus().getUser().getScreenName());
                    this.mentioned.setStringValue(mentioned);
                    this.hashtags.setStringValue(hashtags.toLowerCase());
                    this.writer.addDocument(this.tweet);
                }
                br.close();
            }
            streamNumber++;
            this.writer.commit();
        }
        this.writer.close();
    }

    @Override
    public void build(String name, ArrayList<String> values) throws IOException {
        params(index);
        // A list of all the tweets of interest of the source index
        ArrayList<Document> searchedTweets;
        for (String value : values) {
            // retrieve all tweets (name) that match the value
            searchedTweets = search(name, value, 10000);
            System.out.println(value + " " + searchedTweets.size());
            for (Document tweet : searchedTweets) {
                this.date.setLongValue(Long.parseLong(tweet.get("date")));
                this.userId.setLongValue(Long.parseLong(tweet.get("userId")));
                this.tweetText.setStringValue(tweet.get("tweetText"));
                this.followers.setLongValue(Long.parseLong(tweet.get("followers")));
                this.name.setStringValue(tweet.get("name"));
                this.screenName.setStringValue(tweet.get("screenName"));
                this.mentioned.setStringValue(tweet.get("mentioned"));
                this.hashtags.setStringValue(tweet.get("hashtags"));
                //System.out.println(this.tweet);
                this.writer.addDocument(this.tweet);
            }
            this.writer.commit();
        }

        this.writer.close();
    }

    /**
     * A list of detailed tweet corresponding to the searched items
     *
     * @param name  field to be searched in
     * @param value term to be seached
     * @param range number of returned resul
     * @return a list of  tweets matching our search parameters
     */
    public static ArrayList<Document> search(String name, String value, int range) {
        try {
            String indexLocation = AppConfigs.TWEET_INDEX;
            Directory dir = new SimpleFSDirectory(new File(indexLocation));
            DirectoryReader ir = DirectoryReader.open(dir);
            IndexSearcher searcher = new IndexSearcher(ir);
            Query q;
            q = new TermQuery(new Term(name, value));
            TopDocs td = searcher.search(q, range);
            ScoreDoc[] queryResults = td.scoreDocs;
            ArrayList<Document> results = new ArrayList<>();

            for (ScoreDoc element : queryResults) {
                Document d = searcher.doc(element.doc);
                results.add(d);
            }
            return results;
        } catch (IOException ex) {
            System.out.println("---> Problems with source files: IOException <---");
            ex.printStackTrace();

            return null;
        }
    }
    
    /*
    public static void fromScreenNameToUserId(String inputFile_ScreenNames, String outputFile_UserIds) throws FileNotFoundException, IOException, org.apache.lucene.queryparser.classic.ParseException {
        // List that will contain the twitter IDs
        ArrayList<String> twitterIDs = new ArrayList<String>();

        // Load the file containing screenNames into a list
        List<String> usersList = txtToList(inputFile_ScreenNames);
        int i = 0;
        for (String screenName : usersList) {
        	// retrieve the TwitterIDs from the tweets index
            ArrayList<Document> docs = search("screenName", screenName, 1);
            if (docs.isEmpty() != true) {
            	// Get the userId from the first resulting doc
                String userId = docs.get(0).get("userId");
                // Add it to the output list
                twitterIDs.add(userId);
            }
            i++;
        }
        TxtUtils.iterableToTxt(outputFile_UserIds, twitterIDs);
    }
    */

    /**
     * A method that search the occurence of two terms in the same tweet
     *
     * @param term1 Term to count and compare
     * @param term2 Term to count and compare
     * @param field Target field to look in
     * @return An integer corresponding to the count of tweets containing both words
     * @throws IOException
     * @throws org.apache.lucene.queryparser.classic.ParseException
     */
    public int search(String term1, String term2, String field) throws IOException, org.apache.lucene.queryparser.classic.ParseException {
        params(index);
        writer.close();
        String indexLocation = AppConfigs.TWEET_INDEX;
        Directory dir = new SimpleFSDirectory(new File(indexLocation));
        DirectoryReader ir = DirectoryReader.open(dir);
        IndexSearcher searcher = new IndexSearcher(ir);
        //QueryParser parser = new QueryParser(Version.LUCENE_41, field, analyzer);
        Query t1;
        Query t2;
//        if (stemming) {
//          //  t1 = parser.parse(term1);
//            //t2 = parser.parse(term2);
//        } else {
        t1 = new TermQuery(new Term(field, term1));
        t2 = new TermQuery(new Term(field, term2));
        //}


        BooleanQuery query = new BooleanQuery();
        query.add(t1, BooleanClause.Occur.MUST);
        query.add(t2, BooleanClause.Occur.MUST);

        TotalHitCountCollector collector = new TotalHitCountCollector();
        //System.out.println(searcher.search(query));
        searcher.search(query, collector);

        return (collector.getTotalHits());
    }

    /**
     * Method to check the occurence of a particular word within the index
     *
     * @param term  the word to be searched
     * @param field the particular field of interest within the index
     * @return an integer corresponding to the count of tweets containing the term
     * @throws IOException
     * @throws org.apache.lucene.queryparser.classic.ParseException
     */
    public int termFrequency(String term, String field) throws IOException, org.apache.lucene.queryparser.classic.ParseException {
        params(index);
        writer.close();
        String indexLocation = AppConfigs.TWEET_INDEX;
        Directory dir = new SimpleFSDirectory(new File(indexLocation));
        DirectoryReader ir = DirectoryReader.open(dir);
        IndexSearcher searcher = new IndexSearcher(ir);
        //QueryParser parser = new QueryParser(Version.LUCENE_41, field, analyzer);
        Query t = new TermQuery(new Term(field, term));

        BooleanQuery query = new BooleanQuery();
        query.add(t, BooleanClause.Occur.MUST);
        TotalHitCountCollector collector = new TotalHitCountCollector();
        searcher.search(t, collector);

        return (collector.getTotalHits());
    }

    /**
     * Removes not needed text within a string
     *
     * @param text
     * @return the text without not relevant text (links, rt, hashtags etx)
     */
    // Format the text to remove unneeded strings
    private String formatText(String text) {
        String formatedText = text.replace("RT ", " ");
        formatedText = formatedText.replaceAll("#\\S*", " ");
        formatedText = formatedText.replaceAll("@\\S*", " ");
        formatedText = formatedText.replaceAll("htt\\S*", " ");
        formatedText = formatedText.replaceAll("htt\\S*$", " ");
        formatedText = formatedText.replaceAll("\\d+\\S*", " ");

        return formatedText;
    }

}