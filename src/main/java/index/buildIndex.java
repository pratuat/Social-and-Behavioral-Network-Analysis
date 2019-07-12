package index;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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
    // from github guys
    public static final String STREAM_FILES_LOCATION = "src/resources/data/stream/";
    public static final File[] SUB_DIRECTORIES = new File(STREAM_FILES_LOCATION).listFiles((File file) -> file.isDirectory());

    public static final String RESOURCES_DIRECTORY = "src/resources/";
    public static final String INDEX_DIRECTORY = "src/resources/";
    public static final String STOPWORDS_FILENAME = "stopwords.txt";


    public Directory dir;
    public Analyzer analyzer;
    public IndexWriterConfig cfg;
    public IndexWriter writer;
    public String sourcePath;

    public String indexPath;

    /**
     * Abstract method that build the index
     * @throws IOException
     * @throws TwitterException
     */

    public static final CharArraySet STOPWORDS;

    // from githubguys
    static {
        STOPWORDS = CharArraySet.copy(Version.LUCENE_41, ItalianAnalyzer.getDefaultStopSet());

        try {
            FileInputStream inputStream;
            InputStreamReader inputReader;
            BufferedReader br;

            inputStream = new FileInputStream(RESOURCES_DIRECTORY + STOPWORDS_FILENAME);
            inputReader = new InputStreamReader(inputStream);
            br = new BufferedReader(inputReader);

            String stopword;
            ArrayList<String> stopwords = new ArrayList();

            while ((stopword = br.readLine()) != null) {
                stopwords.add(stopword);
            }

            STOPWORDS.addAll(stopwords);

        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    public void params(String dirName) throws IOException {
        this.dir = new SimpleFSDirectory(new File(dirName));
        this.analyzer = new ItalianAnalyzer(LUCENE_41, STOPWORDS);
        this.cfg = new IndexWriterConfig(LUCENE_41, analyzer);
        this.writer = new IndexWriter(dir, cfg);
    }
    public abstract void build() throws IOException, TwitterException ;
    public abstract void build(String fieldName, ArrayList<String> fieldValues) throws IOException;
}
