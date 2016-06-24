package fr.inria.smilk.ws.relationextraction.luceneSMILK;

/**
 * Created by dhouib on 20/06/2016.
 */
import fr.inria.smilk.ws.relationextraction.lucene.LuceneConstants;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import java.io.IOException;

public class LuceneTest {

    public static void main(String[] args) {
        // Construct a RAMDirectory to hold the in-memory representation
        // of the index.
        RAMDirectory idx = new RAMDirectory();

        try {
            // Make an writer to create the index
            IndexWriter writer =
                    new IndexWriter(idx,
                            new StandardAnalyzer(Version.LUCENE_30),
                            IndexWriter.MaxFieldLength.LIMITED);

            // Add some Document objects containing quotes
            writer.addDocument(createDocument("Allure Homme est un parfum masculin de Chanel, créé par Jacques Polge et sorti en 1999"));
            writer.addDocument(createDocument("Coco Mademoiselle est un parfum de Chanel lancé en 2001.Il a été créé par Jacques Polge, nez auprès des Parfums Chanel depuis 1978"));
            writer.addDocument(createDocument("J'adore est un parfum de Parfums Christian Dior créé en 1999. J'adore est le parfum féminin le plus vendu en France en 2013 et le second parfum le plus vendu en 2014."));
            writer.addDocument(createDocument("Trésor est un parfum de Lancôme créé en 1990 par Sophia Grosjman."));
            writer.addDocument(createDocument("Arpège est un parfum féminin de la maison Lanvin, créé en 1927."));
            // Optimize and close the writer to finish building the index
            writer.optimize();
            writer.close();

            // Build an IndexSearcher using the in-memory index
            IndexSearcher searcher = new IndexSearcher(idx);


      //      searchIndex(searcher,"Chanel");

            // Run some queries
            //search(searcher, "Chanel");
            search(searcher, "Trésor AND Lancôme");
            search(searcher, "Christian Dior");
            search(searcher, "Lanvin");

          //  search(searcher, "Allure Homme");
            search(searcher, "Coco Mademoiselle");
            search(searcher,"J'adore");
            search(searcher, "Trésor");
            search(searcher, "Arpège");

            String[] phrases = new String[]{"Allure Homme Chanel"};
            searchUsingPhraseQuery(searcher, phrases);

            searcher.close();
        }
        catch (IOException ioe) {
            // In this example we aren't really doing an I/O, so this
            // exception should never actually be thrown.
            ioe.printStackTrace();
        }
        catch (ParseException pe) {
            pe.printStackTrace();
        }
    }

    /**
     * Make a Document object with an un-indexed title field and an
     * indexed content field.
     */
    private static Document createDocument(String content) {
        Document doc = new Document();

        // Add the title as an unindexed field...

        //doc.add(new Field("title", title, Field.Store.YES, Field.Index.NO));


        // ...and the content as an indexed field. Note that indexed
        // Text fields are constructed using a Reader. Lucene can read
        // and index very large chunks of text, without storing the
        // entire content verbatim in the index. In this example we
        // can just wrap the content string in a StringReader.
        doc.add(new Field("content", content, Field.Store.YES, Field.Index.ANALYZED));

        return doc;
    }

    /**
     * Searches for the given string in the "content" field
     */
    private static void search(Searcher searcher, String queryString)
            throws ParseException, IOException {

        // Build a Query object
        QueryParser parser = new QueryParser(Version.LUCENE_30,
                "content",
                new StandardAnalyzer(Version.LUCENE_30));
        Query query = parser.parse(queryString);


        int hitsPerPage = 10;
        // Search for the query
        TopScoreDocCollector collector = TopScoreDocCollector.create(5 * hitsPerPage, false);
        searcher.search(query, collector);

        ScoreDoc[] hits = collector.topDocs().scoreDocs;

        int hitCount = collector.getTotalHits();
        System.out.println(hitCount + " total matching documents");

        // Examine the Hits object to see if there were any matches

        if (hitCount == 0) {
            System.out.println(
                    "No matches were found for \"" + queryString + "\"");
        } else {
            System.out.println("Hits for \"" +
                    queryString + "\" were found in quotes by:");

            // Iterate over the Documents in the Hits object
            for (int i = 0; i<hitCount; i++) {
                ScoreDoc scoreDoc = hits[i];
                int docId = scoreDoc.doc;
                float docScore = scoreDoc.score;
                System.out.println("docId: " + docId + "\t" + "docScore: " + docScore);

                Document doc = searcher.doc(docId);

                // Print the value that we stored in the "title" field. Note
                // that this Field was not indexed, but (unlike the
                // "contents" field) was stored verbatim and can be
                // retrieved.
                //System.out.println("  " + (i + 1) + ". " + doc.get("title"));
                System.out.println("Content: " + doc.get("content"));
            }
        }
        System.out.println();
    }



    private static void searchUsingPhraseQuery(Searcher searcher, String[] phrases)
            throws IOException, ParseException {

        long startTime = System.currentTimeMillis();

        PhraseQuery query = new PhraseQuery();

        query.setSlop(5);

        for (String word : phrases) {
            query.add(new Term("content", word));
        }

        int hitsPerPage = 10;
        // Search for the query
        TopScoreDocCollector collector = TopScoreDocCollector.create(5 * hitsPerPage, false);
        searcher.search(query, collector);

        ScoreDoc[] hits = collector.topDocs().scoreDocs;

        int hitCount = collector.getTotalHits();
        System.out.println(hitCount + " total matching documents");

        // Examine the Hits object to see if there were any matches

        if (hitCount == 0) {
            System.out.println(
                    "No matches were found for \"" + phrases + "\"");
        } else {
            System.out.println("Hits for \"" +
                    phrases + "\" were found in quotes by:");

            // Iterate over the Documents in the Hits object
            for (int i = 0; i < hitCount; i++) {
                ScoreDoc scoreDoc = hits[i];
                int docId = scoreDoc.doc;
                float docScore = scoreDoc.score;
                System.out.println("docId: " + docId + "\t" + "docScore: " + docScore);

                Document doc = searcher.doc(docId);

                // Print the value that we stored in the "title" field. Note
                // that this Field was not indexed, but (unlike the
                // "contents" field) was stored verbatim and can be
                // retrieved.
                //System.out.println("  " + (i + 1) + ". " + doc.get("title"));
                System.out.println("Content: " + doc.get("content"));
            }
        }
        System.out.println();
    }

    public static final String FIELD_CONTENTS = "contents";

    public static void searchIndex(IndexSearcher indexSearcher,String searchString) throws IOException, ParseException {
        System.out.println("Searching for '" + searchString + "'");

        //Directory directory = FSDirectory.getDirectory(INDEX_DIRECTORY);
        //IndexReader indexReader = IndexReader.open(directory);
        //IndexSearcher indexSearcher = new IndexSearcher(indexReader);

        Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_30);
        QueryParser queryParser = new QueryParser(Version.LUCENE_30,FIELD_CONTENTS, analyzer);
        Query query = queryParser.parse(searchString);
        int hitsPerPage = 10;
        // Search for the query
        //TopScoreDocCollector collector = TopScoreDocCollector.create(5 * hitsPerPage, false);
        TopScoreDocCollector collector = TopScoreDocCollector.create(10, false);
        indexSearcher.search(query, collector);
        ScoreDoc[] hits = collector.topDocs().scoreDocs;

        int hitCount = collector.getTotalHits();
        System.out.println(hitCount + " total matching documents");

        /*Hits hits = indexSearcher.search(query);
        System.out.println("Number of hits: " + hits.length());

        Iterator<Hit> it = hits.iterator();
        while (it.hasNext()) {
            Hit hit = it.next();
            Document document = hit.getDocument();
            String path = document.get(FIELD_PATH);
            System.out.println("Hit: " + path);
        }
*/
    }


}