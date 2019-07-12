package utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.NumericUtils;
import twitter4j.QueryResult;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.conf.ConfigurationBuilder;

//taken from the GITHUB GUYS

public  class twitter {
    public IndexReader ir;
    public static final int MINIMUMFOLLOWERS = 5000;

    public IndexSearcher is;
    public String indexLocation = "./src/resources/index/indexTweets";

    public twitter() {

    }

    public ArrayList<Document> search(String fieldName, String fieldValue) {
        try {
            // Set index params (path, ir and searcher)
            Directory dir = new SimpleFSDirectory(new File(indexLocation));

            ir = DirectoryReader.open(dir);
            IndexSearcher searcher = new IndexSearcher(ir);

            Query q;

            // if the field is a LongField
            if (fieldName.equals("date") || fieldName.equals("userId")) {
                //Convert the fieldValue in a ByteRef
                BytesRef ref = new BytesRef();
                NumericUtils.longToPrefixCoded(Long.parseLong(fieldValue), 0, ref);
                // Create the query
                q = new TermQuery(new Term(fieldName, ref));
            } else {
                // Create the query
                q = new TermQuery(new Term(fieldName, fieldValue));
            }

            // Execute the query
            TopDocs top = searcher.search(q, MINIMUMFOLLOWERS);
            // Get query results
            ScoreDoc[] hits = top.scoreDocs;

            // Collect the documents inside the query results
            ArrayList<Document> results = new ArrayList<>();

            for (ScoreDoc entry : hits) {
                Document doc = searcher.doc(entry.doc);
                results.add(doc);
            }

            // Return the list of Docs
            return results;

        } catch (IOException ex) {
            System.out.println("---> Problems with source files: IOException <---");
            ex.printStackTrace();

            return null;
        }
    }


    public static twitter4j.Twitter getTwitter() {
        ConfigurationBuilder cfg = new ConfigurationBuilder();
        cfg.setOAuthAccessToken("804265252221308928-QMQRe5XZPRSBmXqZTYD1ESzOoiNDY7y");
        cfg.setOAuthAccessTokenSecret("ykK4tbpXus9Yggh41ChMnbct4mHjbc0gDxEcyOYerX9SD");
        cfg.setOAuthConsumerKey("42Q1oVVQ8wDmWRPAk8tx0lMRk");
        cfg.setOAuthConsumerSecret("MIjyDlLibLTwpZgUzIlttUvzhs3tdwDLwj7j5WPgQ3galoC7mK");
        TwitterFactory tf = new TwitterFactory(cfg.build());
        return tf.getInstance();
    }
/*
    public static QueryResult findTweetsAboutReferendum(Twitter twitter) throws TwitterException {
        Query query = new Query("#referendum");
        query.setSince("2016-09-01");
        query.setUntil("2016-12-05");

        QueryResult result = twitter.search(query);
        if (result.getTweets() == null || result.getTweets().isEmpty()) {
            return null;
        }
        return result;
    }

    public static String fromNameToTwitterScreenName(String name) throws InterruptedException {

        Twitter twitte = twitter.getTwitter();
        boolean done = true;
        do {
            try {
                User user = twitte.searchUsers(name, 0).get(0);
                if (user.getFollowersCount() >= MINIMUMFOLLOWERS) {
                    name = "@" + user.getScreenName();
                } else {
                    name = "";
                }
            } catch (TwitterException ex) {
                //Logger.getLogger(PoliticiansLoader.class.getName()).log(Level.SEVERE, null, ex);
                TimeUnit.MINUTES.sleep(3);
                done = false;
            } catch (java.lang.IndexOutOfBoundsException e) {
                name = "";
            }
        } while (!done);

        return name;
    }

    public static long getTwitterID(String name) throws InterruptedException {
        long id = 0;
        Twitter twitte = twitter.getTwitter();
        boolean done = true;
        do {
            try {
                User user = twitte.searchUsers(name, 0).get(0);
                id = user.getId();

            } catch (TwitterException ex) {
                //Logger.getLogger(PoliticiansLoader.class.getName()).log(Level.SEVERE, null, ex);
                TimeUnit.MINUTES.sleep(3);
                done = false;
            } catch (java.lang.IndexOutOfBoundsException e) {
                id = 0;
            }
        } while (!done);

        return id;
    }*/

    public String[] searchTwitterId(String name) throws IOException {

        //ArrayList<String> fieldValues = new ArrayList<String>();
        // Search user that match name + surname
        //fieldValues.add((name).toLowerCase());

        ArrayList<Document> results = search("name", name);
        //System.out.println(results);
        // Variable that will mantain the max number of followers among the users found
        int max = 0;
        // User id related to the max
        String id = "";

        // For each document found
        for (Document doc : results) {
            // check if the user that made it has the more influencer of our actual max
            if (Integer.parseInt(doc.get("followers")) >= max) {
                // And in case take it as new max

                max = Integer.parseInt(doc.get("followers"));
                id = doc.get("screenName");
            }
        }
        // Return the max
        String[] result = {id, new Integer(max).toString()};
        return result;
    }

    public static void main(String[] args) throws TwitterException, FileNotFoundException, IOException {
       twitter tw = new twitter();
        String[] results = tw.searchTwitterId("guglielmo picchi");
        for (String e : results){System.out.println(e);}
        System.out.println(results[0]);
    }
}

