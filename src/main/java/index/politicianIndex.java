package index;

import java.io.IOException;
import java.util.ArrayList;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import twitter4j.TwitterException;
import java.util.HashMap;
import utils.csv;
import utils.twitter;

public class politicianIndex extends buildIndex {


    // Creating a new Lucene Document
    private Document politician;
    private StringField name;
    private StringField vote;
    private StringField party;
    private StringField screenName;

    /**
     * Initialize builder parameters
     *
     * @param sourcePath where the data to create the index are stored
     * @param indexPath where the index will be stored
     */
    public politicianIndex(String sourcePath, String indexPath) {
        // Initialize the document
        this.politician = new Document();

        // Initialization
        this.name = new StringField("name", "", Field.Store.YES);
        this.vote = new StringField("vote", "", Field.Store.YES);
        this.party = new StringField( "party", "", Field.Store.YES);
        this.screenName = new StringField("screenName", "", Field.Store.YES);


        politician.add(name);
        politician.add(vote);
        politician.add(party);
        politician.add(screenName);

        // Paths
        this.sourcePath = sourcePath;
        this.indexPath = indexPath;
    }

    /**
     * Create the index starting from a utils.csv file of politicians
     * @throws IOException
     * @throws TwitterException
     */
    @Override
    public void build() throws IOException, TwitterException {
        // Read the utils.csv file
        csv csv = new csv();
        // Will contains the utils.csv file rows
        ArrayList<String[]> rows;
        // Set builder params
        params(indexPath);
        // Get the whole CSV rows
        rows = csv.read_csv(sourcePath,",");


    }

    @Override
    public void build(String fieldName, ArrayList<String> fieldValues) throws IOException {
        // Not implemented
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }


}
