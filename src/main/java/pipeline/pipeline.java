package pipeline;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.apache.lucene.document.Document;
import twitter4j.TwitterException;
import index.indexTweets;
import index.politicianIndex;

public class pipeline {

        public static void createTweetIndex(String sourcePath) {

            System.out.println("Tweets Index Creation!");
            String indexPath = "index/indexTweets";
            // Initialize a new TweetsIndexBuilder
            indexTweets indexAllTweets = new indexTweets(sourcePath, indexPath);
            Path dir = Paths.get("index/indexTweets");
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
                // Advise the index already exist
                System.out.println(dir.toString() + ": Index already created!");
            }
        }

        public static void createPoliticianIndex(String sourcePath) {

            System.out.println("Politician Index Creation!");
            String indexPath = "index/indexPoliticians";
            // Initialize a new TweetsIndexBuilder
            politicianIndex indexPoliticians = new politicianIndex(sourcePath, indexPath);
            Path dir = Paths.get("index/indexPoliticians");
            if (!Files.exists(dir)) {
                try {
                    // Building the the index
                    indexPoliticians.build();
                } catch (IOException ex) {
                    System.out.println("---> Problems with source files: IOException <---");
                    ex.printStackTrace();
                } catch (TwitterException ex) {
                    System.out.println("---> Problems with Tweets: TwitterException <---");
                    ex.printStackTrace();
                }
            } else {
                // Advise the index already exist
                System.out.println(dir.toString() + ": Index already created!");
            }
        }

        public static void dividePoliticians(String sourcePath) {

            System.out.println("Dividing Politicians in Groups Yes/No Index Creation!");
            String indexPath = "index/indexPoliticians";
            // Initialize a new TweetsIndexBuilder
            politicianIndex indexPoliticians = new politicianIndex(sourcePath, indexPath);
            Path dir = Paths.get("index/indexPoliticians");
            // Divide politicians in YES and NO
            ArrayList<Document> yesPoliticians = indexPoliticians.search("vote", "yes", 10000);
            ArrayList<Document> noPoliticians = indexPoliticians.search("vote", "no", 10000);


            if (yesPoliticians != null && noPoliticians != null) {
                System.out.println("YES POLITICIANS: " + yesPoliticians.size());
                System.out.println("NO POLITICIANS: " + noPoliticians.size());
                System.out.println("TOT POLITICIANS: " + (yesPoliticians.size() + noPoliticians.size()));
            }

            // Loading all the tweets
            String indexYes = "index/indexYes";
            indexTweets indexAllTweets = new indexTweets(sourcePath, indexYes);

            // If the index of all yes tweets doesn't exist
            dir = Paths.get("index/indexYes");
            if (!Files.exists(dir)) {
                // Create it collecting all the yes ploticians screen name
                ArrayList<String> yesPoliticiansID = indexPoliticians.filter("vote", "yes", "screenName", 10000);
                try {
                    indexAllTweets.build("screenName", yesPoliticiansID);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                // Advise the index already exist
                System.out.println(dir.toString() + ": Index already created!");
            }

            String indexNo = "index/indexNo";
            indexTweets indexAllTweetsNo = new indexTweets(sourcePath, indexNo);


            // If the index of all no tweets doesn't exist
            dir = Paths.get("index/indexNo");
            if (!Files.exists(dir)) {
                // Create it collecting all the no ploticians screen name
                ArrayList<String> noScreenNames = indexPoliticians.filter("vote", "no", "screenName", 10000);
                try {
                    indexAllTweetsNo.build( "screenName", noScreenNames);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                // Advise the index already exist
                System.out.println(dir.toString() + ": Index already created!");
            }

        }


        public static void main(String[] args){
            String sourceTweets = "./src/util/data/stream";
            String sourcePoliticians = "./src/util/list_politician.csv";
            createTweetIndex(sourceTweets);
            createPoliticianIndex(sourcePoliticians);
            dividePoliticians(sourcePoliticians);
        }

    }
