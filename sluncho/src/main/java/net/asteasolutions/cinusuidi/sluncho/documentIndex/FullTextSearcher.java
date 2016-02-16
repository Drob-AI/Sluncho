/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.asteasolutions.cinusuidi.sluncho.documentIndex;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import net.asteasolutions.cinusuidi.sluncho.utils.SearchUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

/**
 *
 * @author mihail
 */
public class FullTextSearcher {
    public ArrayList<DocumentIndexEntry> search(DocumentIndexEntry q) throws IOException, ParseException, QueryNodeException {
            ArrayList<DocumentIndexEntry> result = new ArrayList<DocumentIndexEntry>();
            String indexPath = "fullTextIndex";
            IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexPath)));

            IndexSearcher searcher = new IndexSearcher(reader);
            Analyzer analyzer = new StandardAnalyzer();
            QueryParser parser = new QueryParser("questionId", analyzer);
//            String queryString = "\"" + q.predicate + " " + q.subject + "\""; /*+ " " + q.additionGroup + "\""*/;
//            Query query = parser.parse("(question:" + queryString + ")^4 OR (answer:" + queryString+ ") OR (context:" + queryString + ")^2");
            
            String queryString = QueryParser.escape(q.predicate) + " " + QueryParser.escape(q.subject) + " " + QueryParser.escape(q.additionGroup);
            queryString = SearchUtils.replaceSpacesWithOR(queryString);
            Query query = parser.parse("(question:" + queryString + ")^4 OR (answer:" + queryString+ ") OR (context:" + queryString + ")^2");
            
            TopDocs results = searcher.search(query, 50);

            ScoreDoc[] hits = results.scoreDocs;

            int numTotalHits = results.totalHits;

            for(int i = 0; i < Math.min(numTotalHits, 50); i++) {
                    Document doc = searcher.doc(hits[i].doc);
                    DocumentIndexEntry entity = new DocumentIndexEntry();
                    entity.questionId = doc.get("questionId");
                    entity.groupId = doc.get("groupId");
                    entity.score = hits[i].score;
                    result.add(entity);
            }

            return result;
    }

    public String searchById(String docId) throws ParseException, IOException {
        ArrayList<DocumentIndexEntry> result = new ArrayList<DocumentIndexEntry>();
            String indexPath = "fullTextIndex";
            IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexPath)));

            IndexSearcher searcher = new IndexSearcher(reader);
            Analyzer analyzer = new StandardAnalyzer();
            QueryParser parser = new QueryParser("questionId", analyzer);
            String queryString = docId;
            Query query = parser.parse(queryString);

            TopDocs results = searcher.search(query, 1);

            ScoreDoc[] hits = results.scoreDocs;

            int numTotalHits = results.totalHits;

            if(numTotalHits > 0) {
                    Document doc = searcher.doc(hits[0].doc);
                    DocumentIndexEntry entity = new DocumentIndexEntry();
                    return doc.get("answer");
            }

            return "Could not find an answer to your question";
    }
}
