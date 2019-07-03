package index;

import java.io.File;
import java.io.IOException;
import org.apache.lucene.analysis.it.ItalianAnalyzer;
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

import static org.apache.lucene.util.Version.LUCENE_41;

public class IndexSearcher {

    public IndexReader reader;
    public org.apache.lucene.search.IndexSearcher searcher;
    public ItalianAnalyzer analyzer;

    public IndexSearcher(String indexFolder) throws IOException {
        File directoryIndex = new File(indexFolder);
        reader = DirectoryReader.open(FSDirectory.open(directoryIndex));
        searcher = new org.apache.lucene.search.IndexSearcher(reader);
        analyzer = new ItalianAnalyzer(Version.LUCENE_41, IndexBuilder.STOPWORDS);
    }

    public Document[] searchByField(String field, Object query, int totalResults) throws IOException, ParseException {

        QueryParser parser = new QueryParser(LUCENE_41, field, analyzer);
        Query q;
        
        if (query instanceof String) {
            q = parser.parse("+" + query.toString());
        } else {
            BytesRef ref = new BytesRef();
            NumericUtils.longToPrefixCoded((long) query, 0, ref);
            q = new TermQuery(new Term(field, ref));
        }

//        q = new MatchAllDocsQuery();

        TopDocs top = searcher.search(q, totalResults);
        ScoreDoc[] hits = top.scoreDocs;

        if (hits.length == 0) {
            return null;
        }

        int resultArraySize = 0;
        // calculate the size of the result array
        if (totalResults < hits.length) {
            resultArraySize = totalResults;
        } else {
            resultArraySize = hits.length;
        }

        Document[] docsResult = new Document[resultArraySize];

        // go on each hit
        for (int i = 0; i < resultArraySize; i++) {
            ScoreDoc entry = hits[i];
            docsResult[i] = searcher.doc(entry.doc);
        }

        return docsResult;
    }

}
