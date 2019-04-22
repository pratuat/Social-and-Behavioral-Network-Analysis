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
import utils.csv;
import utils.twitter;

public class politicianIndex extends buildIndex {

    // Creating a new Lucene Document
    private Document politician;
    private StringField name;
    private StringField vote;
    private StringField party;
    private StringField screenName;
    private String indexLocation = "./index/indexPoliticians";
    private IndexReader ir;

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

    @Override
    public void build() throws IOException, TwitterException {
        // Read the utils.csv file
        csv csv = new csv();
        twitter tw = new twitter();
        // Will contains the utils.csv file rows
        ArrayList<String[]> rows;
        // Set builder params
        params(indexPath);
        rows = csv.read_csv(sourcePath,",");
        String id;
        String[] result;
        int followers;
        for (String[] row : rows) {
            String politicianName = row[0];
            String party = row[1];
            String vote = row[2];
            result = tw.searchTwitterId(politicianName.toLowerCase());
            id = result[0];
            followers = Integer.parseInt(result[1]);
            System.out.println("Search for : " + politicianName + ", followers: " + followers);
            if (!id.equals("") && followers >= 800) {
                this.name.setStringValue(politicianName);
                this.screenName.setStringValue(id);
                this.vote.setStringValue(vote);
                System.out.println("Adding the following to index");
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

    public ArrayList<Document> search(String name, String value, int range) {
        try {
            Directory dir = new SimpleFSDirectory(new File(indexLocation));
            ir = DirectoryReader.open(dir);
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
