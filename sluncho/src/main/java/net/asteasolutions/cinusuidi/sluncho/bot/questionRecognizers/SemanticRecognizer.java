/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.asteasolutions.cinusuidi.sluncho.bot.questionRecognizers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.asteasolutions.cinusuidi.sluncho.bot.Query;
import net.asteasolutions.cinusuidi.sluncho.bot.QuestionResult;
import net.asteasolutions.cinusuidi.sluncho.documentIndex.DocumentIndexEntry;
import net.asteasolutions.cinusuidi.sluncho.documentIndex.DocumentSearcher;
import net.asteasolutions.cinusuidi.sluncho.documentIndex.FullTextSearcher;
import net.asteasolutions.cinusuidi.sluncho.questionparser.QueryToken;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.deeplearning4j.berkeley.Pair;

/**
 *
 * @author mihail
 */
public class SemanticRecognizer implements IQuestionRecognizer {

    @Override
    public List<QuestionResult> classify(Query query) {
        ArrayList<QuestionResult> result = new ArrayList<>();
        ArrayList<QuestionResult> trueResult = new ArrayList<>();
            
        try {
            DocumentSearcher docSearcher = new DocumentSearcher();
            DocumentIndexEntry entry = new DocumentIndexEntry();
            entry.additionGroup = "";
            entry.predicate = "";
            entry.subject = "";
            for(QueryToken token: query.additionGroup) {
                entry.additionGroup += " " + token.getOriginalText();
            }
            for(QueryToken token: query.subjectGroup) {
                entry.subject += " " + token.getOriginalText();
            }
            for(QueryToken token: query.predicateGroup) {
                entry.predicate += " " + token.getOriginalText();
            }
        
            List<DocumentIndexEntry> docEntries = docSearcher.search(entry);
            
            HashMap<String, Pair<String, Float>> map = new HashMap<>();
            
            for(DocumentIndexEntry doc: docEntries) {
                if(!map.containsKey(doc.questionId)) {
                    map.put(doc.questionId, new Pair<>(doc.groupId, 0f));
                }
                Pair<String, Float> p = map.get(doc.questionId);
                p.setSecond(p.getSecond() + doc.score);
            }
            
            for(String key: map.keySet()) {
                Pair<String, Float> p = map.get(key);
                result.add(new QuestionResult(key, p.getFirst(), p.getSecond()));
            }
            
        } catch (IOException | ParseException | QueryNodeException ex) {
            ex.printStackTrace();
        }
        return trueResult;
    }
    
}
