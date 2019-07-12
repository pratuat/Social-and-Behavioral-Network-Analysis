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
import java.util.List;
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
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;

import io.TxtUtils;

import static io.TxtUtils.txtToList;
import static org.apache.lucene.util.Version.LUCENE_41;
import twitter4j.HashtagEntity;
import twitter4j.TwitterException;
import twitter4j.UserMentionEntity;
import utils.StatusWrapper;

public class indexTweets extends buildIndex {

    private LongField date;
    private Document tweet;
    private TextField tweetText;
    private LongField userId;
    private LongField followers;
    private StringField name;
    private StringField screenName;
    private TextField hashtags;
    private TextField mentioned;


    // Map that define which field is going to have which analyzer
    private Map<String, Analyzer> analyzerPerField;
    // Wrapper that allow to use different analyzers
    private PerFieldAnalyzerWrapper wrapper;

    public indexTweets(String sourcePath, String indexPath) {
        // Initialization
        this.tweet = new Document();

        // Initialization of field
        this.userId = new LongField("userId", 0L, Field.Store.YES);
        this.date = new LongField("date", 0L, Field.Store.YES);
        this.name = new StringField("name", "", Field.Store.YES);
        this.screenName = new StringField("screenName", "", Field.Store.YES);
        this.tweetText = new TextField("tweetText", "", Field.Store.YES);
        this.hashtags = new TextField("hashtags", "", Field.Store.YES);
        this.mentioned = new TextField("mentioned", "", Field.Store.YES);
        this.followers = new LongField("followers", 0L, Field.Store.YES);

        // Adding the various fields to tweet
        this.tweet.add(this.userId);
        this.tweet.add(this.date);
        this.tweet.add(this.name);
        this.tweet.add(this.screenName);
        this.tweet.add(this.tweetText);
        this.tweet.add(this.hashtags);
        this.tweet.add(this.mentioned);
        this.tweet.add(this.followers);

        // Initialize paths
        this.sourcePath = sourcePath;
        this.indexPath = indexPath;

    }

    @Override
    public void build() throws IOException, TwitterException {
        // Initialization (replace source path with default path)
        Path sourceDirPath = Paths.get(sourcePath);
        params(indexPath);

        // Get all the streams from the data provides
        DirectoryStream<Path> allStreams = Files.newDirectoryStream(sourceDirPath);

        // Initialization of the status wrapper which was provided
        StatusWrapper sw;
        int streamNumber = 1;
        // For each directory in the stream
        for (Path stream : allStreams) {
            System.out.println(stream);

            // get all files withing a stream folder
            DirectoryStream<Path> streamFiles = Files.newDirectoryStream(stream);
            int count = 1;

            int totalFiles = new File(stream.toString()).listFiles().length;

            // For each file within a stream
            for (Path file : streamFiles) {

                System.out.println(streamNumber + ") " + count + " out of " + totalFiles);
                count++;
                FileInputStream fstream = new FileInputStream(file.toString());
                GZIPInputStream gzstream = new GZIPInputStream(fstream);
                InputStreamReader isr = new InputStreamReader(gzstream, "UTF-8");
                BufferedReader br = new BufferedReader(isr);
                String line;
                // unzip the file and read the data
                while ((line = br.readLine()) != null) {
                    // reading the data was done using functionalities of StatusWrapper
                    sw = new StatusWrapper();
                    sw.load(line);
                    this.userId.setLongValue(sw.getStatus().getUser().getId());
                    this.date.setLongValue(sw.getTime());
                    this.name.setStringValue(sw.getStatus().getUser().getName().toLowerCase());
                    this.screenName.setStringValue(sw.getStatus().getUser().getScreenName());
                    String cleanedText = cleanText(sw.getStatus().getText());
                    this.tweetText.setStringValue(cleanedText);
                    this.followers.setLongValue((long) sw.getStatus().getUser().getFollowersCount());

                    String mentionedPeople = "";
                    for (UserMentionEntity user : sw.getStatus().getUserMentionEntities()) {
                        mentionedPeople += user.getText() + " ";
                    }
                    this.mentioned.setStringValue(mentionedPeople);


                    String hashtags = "";
                    for (HashtagEntity hashtag : sw.getStatus().getHashtagEntities()) {
                        hashtags += "#" + hashtag.getText() + " ";
                    }
                    this.hashtags.setStringValue(hashtags.toLowerCase());
                    // Add the document
                    this.writer.addDocument(this.tweet);
                }
            }
            streamNumber++;

            this.writer.commit();
        }


        this.writer.close();
    }

    @Override
    public void build(String name, ArrayList<String> values) throws IOException {
        params(indexPath);

        // A list of all the tweets of interest of the source index
        ArrayList<Document> interestedTweets;
        for (String value : values) {
            // retrieve all tweets (name) that match the value
            interestedTweets = search(name, value, 10000);
            System.out.println(value + " " + interestedTweets.size());
            for (Document tweet : interestedTweets) {
                this.userId.setLongValue(Long.parseLong(tweet.get("userId")));
                this.date.setLongValue(Long.parseLong(tweet.get("date")));
                this.name.setStringValue(tweet.get("name"));
                this.screenName.setStringValue(tweet.get("screenName"));
                this.tweetText.setStringValue(tweet.get("tweetText"));
                this.followers.setLongValue(Long.parseLong(tweet.get("followers")));
                this.mentioned.setStringValue(tweet.get("mentioned"));
                this.hashtags.setStringValue(tweet.get("hashtags"));

                //System.out.println(this.tweet);
                this.writer.addDocument(this.tweet);
            }

            this.writer.commit();
        }

        this.writer.close();
    }

    @Override
    public void params(String dirName) throws IOException {
        this.dir = new SimpleFSDirectory(new File(dirName));
        analyzerPerField = new HashMap<String, Analyzer>();
        analyzerPerField.put("tweetText", new ItalianAnalyzer(LUCENE_41));
        wrapper = new PerFieldAnalyzerWrapper(new WhitespaceAnalyzer(LUCENE_41), analyzerPerField);
        this.cfg = new IndexWriterConfig(LUCENE_41, wrapper);
        this.writer = new IndexWriter(dir, cfg);
    }

    // Method used to remove irrelevant parts from tweet texts
    private String cleanText(String uncleanedText) {
        String cleanedText = uncleanedText.replace("RT ", " ");
        cleanedText = cleanedText.replaceAll("htt\\S*", " ");
        cleanedText = cleanedText.replaceAll("htt\\S*$", " ");
        cleanedText = cleanedText.replaceAll("\\d+\\S*", " ");
        cleanedText = cleanedText.replaceAll("#\\S*", " ");
        cleanedText = cleanedText.replaceAll("@\\S*", " ");

        return cleanedText;
    }

    public static ArrayList<Document> search(String name, String value, int range) {
        try {
            String indexLocation = "./src/resources/index/indexTweets";
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

}
