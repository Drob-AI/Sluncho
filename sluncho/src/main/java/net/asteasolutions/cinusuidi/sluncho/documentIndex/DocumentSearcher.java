/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.asteasolutions.cinusuidi.sluncho.documentIndex;

import java.io.IOException;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import net.asteasolutions.cinusuidi.sluncho.bot.errorCorrection.NamedEntity;
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
public class DocumentSearcher {
    public ArrayList<DocumentIndexEntry> search(DocumentIndexEntry q) throws IOException, ParseException, QueryNodeException {
            ArrayList<DocumentIndexEntry> result = new ArrayList<DocumentIndexEntry>();
            String indexPath = "questionAnswerIndex";
            IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexPath)));

            IndexSearcher searcher = new IndexSearcher(reader);
            Analyzer analyzer = new StandardAnalyzer();
            QueryParser parser = new QueryParser("questionId", analyzer);
            
            String subject = SearchUtils.replaceSpacesWithOR(q.subject);
            String predicate = SearchUtils.replaceSpacesWithOR(q.predicate);
            String addition = SearchUtils.replaceSpacesWithOR(q.additionGroup);
            
            Integer additionCount = SearchUtils.cleanup(q.additionGroup).split(" ").length;
            Float additionStrength = 0.5f + additionCount * 0.5f;
            String additionStrenghtBoost = new DecimalFormat("#.#").format(additionStrength);
            
            String answerQuery = "(subject:(" + subject + ") AND "
                    + "predicate:(" + predicate + "))^2 "
                    + "OR additionGroup:(" + addition + ")^" + additionStrenghtBoost;
            
            String questionQuery = "((subject:(" + subject + ") AND additionGroup:(" + addition + ")^"+ additionStrenghtBoost +") OR "
                    + "(subject:(" + addition + ") AND additionGroup:(" + subject + "))^0.5) OR (predicate: " + predicate + ")";
            
            String rawQuery = "(+type:2 AND (" + questionQuery + "))^2 OR (+type:0 AND (" + answerQuery + "))";
            
            Query query = parser.parse(rawQuery);

            TopDocs results = searcher.search(query, 50);

            ScoreDoc[] hits = results.scoreDocs;

            int numTotalHits = results.totalHits;

            for(int i = 0; i < numTotalHits; i++) {
                    Document doc = searcher.doc(hits[i].doc);
                    DocumentIndexEntry entity = new DocumentIndexEntry();
                    entity.type = doc.get("type");
                    entity.groupId = doc.get("groupId");
                    entity.additionGroup = doc.get("additionGroup");
                    entity.score = hits[i].score;
                    entity.subject = doc.get("subject");;
                    entity.questionId = doc.get("questionId");
                    entity.predicate = doc.get("predicate");
                    result.add(entity);
            }

            return result;
    }
}
