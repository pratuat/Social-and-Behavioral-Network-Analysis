package support_users;

import com.AppConfigs;
import org.apache.lucene.index.CheckIndex;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.File;

public class test_main {
    public static final String indexPath = AppConfigs.TWEET_INDEX_PATH;

    public static void main(String[] args){
        File path = new File(indexPath);
        try {
            Directory directory = FSDirectory.open(path);
            CheckIndex ci = new CheckIndex(directory);
            IndexReader indexReader = DirectoryReader.open(directory);
            System.out.print(indexReader.numDocs());

        } catch (Exception  e){
            System.out.print(e.getStackTrace());
        }
    }
}
