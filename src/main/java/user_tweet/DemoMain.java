package user_tweet;

import com.AppConfigs;
import index.IndexBuilder;
import org.apache.lucene.analysis.it.ItalianAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.NumericUtils;
import org.apache.lucene.util.Version;

import java.io.File;
import java.io.IOException;

import static org.apache.lucene.util.Version.LUCENE_41;

public class DemoMain {

    public static void main(String[] args){

        File directoryIndex = new File(AppConfigs.ALL_TWEET_INDEX);

        IndexReader reader = null;
        try {
            reader = DirectoryReader.open(FSDirectory.open(directoryIndex));
        } catch (IOException e) {
            e.printStackTrace();
        }
        IndexSearcher searcher = new IndexSearcher(reader);
        StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_41);

        QueryParser parser = new QueryParser(LUCENE_41, "screenName", analyzer);
        Query query = null;
        try {
            query = parser.parse("screenName: fieramosca59");
        } catch (ParseException e) {
            e.printStackTrace();
        }

        TopDocs top = null;
        try {
            top = searcher.search(query, 5);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Document doc;

        for (ScoreDoc hit: top.scoreDocs) {
            try {
                doc = searcher.doc(hit.doc);
                System.out.println(doc.get("name") + "\t||\t" + doc.get("screenName"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
