package index;

import java.io.IOException;
import java.util.ArrayList;
import java.io.File;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.NumericUtils;
import twitter4j.TwitterException;
import java.util.HashMap;

import utils.AppConfigs;
import utils.csv;
import utils.twitter;

@SuppressWarnings("unused")
public class politicianIndex extends buildIndex {

    // Creating a new Lucene Document
    private Document politician;
    private StringField name;
    private StringField vote;
    private StringField party;
    private StringField screenName;
    private static String indexLocation = AppConfigs.TWEET_INDEX;
    private static IndexReader ir;

    public politicianIndex(String stream, String index) {
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
        this.stream = stream;
        this.index = index;
    }

    @SuppressWarnings("unused")
	@Override
    public void build() throws IOException {
        // Read the utils.csv file
        //csv csv = new csv();
        twitter tw = new twitter();
        // Will contains the utils.csv file rows
        ArrayList<String[]> lines;
        // Set builder params
        params(index);
        lines = csv.read_csv(stream,",");
        String id;
        String[] politicianInformation;
        int politicianfollowers;
        for (String[] line : lines) {
            // extracting the politian information from
            String politicianName = line[0];
            String party = line[1];
            String vote = line[2];
            // searching on twitter for politician id
            politicianInformation= tw.searchTwitterId(politicianName.toLowerCase());
            id = politicianInformation[0];
            politicianfollowers = Integer.parseInt(politicianInformation[1]);
            System.out.println("Looking for ID of : " + politicianName + ", followers: " + politicianfollowers);
            //adding politician to index if twiiter Id exist and number of followers above 1000
            if (!id.equals("") && politicianfollowers >= 1000) {
                this.name.setStringValue(politicianName);
                this.screenName.setStringValue(id);
                this.vote.setStringValue(vote);
                System.out.println("Adding the following politician to index");
                System.out.println(this.politician.get("name"));
                System.out.println(this.politician.get("screenName"));
                System.out.println(this.politician.get("vote"));
                this.writer.addDocument(this.politician);
            }
            System.out.println("----------------");
        }
        this.writer.commit();
        this.writer.close();
    }

    @Override
    public void build(String fieldName, ArrayList<String> fieldValues) throws IOException {
        throw new UnsupportedOperationException("Not needed here");
    }

    public static ArrayList<Document> search(String name, String value, int range) {
        try {
            // keep the results of the query
            ArrayList<Document> results = new ArrayList<>();
            // setup an index reader
            Directory dir = new SimpleFSDirectory(new File(indexLocation));
            ir = DirectoryReader.open(dir);
            // setup an index searcher
            IndexSearcher searcher = new IndexSearcher(ir);
            // set up the query
            Query q;
            q = new TermQuery(new Term(name, value));
            TopDocs td = searcher.search(q, range);
            //retrieve the results
            ScoreDoc[] queryResults = td.scoreDocs;
            for (ScoreDoc element : queryResults) {
                Document d = searcher.doc(element.doc);
                results.add(d);
            }
            return results;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public ArrayList<String> filter(String name, String value, String target, int range) {
        ArrayList<Document> results = new ArrayList<>();
        results.addAll(search(name, value, range));
        ArrayList<String> filterResults = new ArrayList<>();
        for (Document d : results) {
            filterResults.add(d.get(target));
        }
        return filterResults;

    }

}
