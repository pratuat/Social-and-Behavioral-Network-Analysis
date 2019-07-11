package index;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.analysis.it.ItalianAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import static org.apache.lucene.util.Version.LUCENE_41;
import org.apache.lucene.util.Version;
import twitter4j.TwitterException;


public abstract class buildIndex {
    // Setting up the maoin parameters
    public static final String stopwordsLocation = "src/util/stopwords.txt";
    public Directory dir;
    public Analyzer analyzer;
    public IndexWriterConfig cfg;
    public IndexWriter writer;
    public String stream;
    public String index;

    // Assigning the various parameters values
    public void params(String dirName) throws IOException {
        this.dir = new SimpleFSDirectory(new File(dirName));
        this.analyzer = new ItalianAnalyzer(LUCENE_41, getStopwords(stopwordsLocation));
        this.cfg = new IndexWriterConfig(LUCENE_41, analyzer);
        this.writer = new IndexWriter(dir, cfg);
    }
    // Abstract build classes
    public abstract void build() throws IOException, TwitterException ;
    public abstract void build(String fieldName, ArrayList<String> fieldValues) throws IOException;

    // Load up the various italian stopword for lucene
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
}
