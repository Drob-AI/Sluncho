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

/**
 *
 * @author mihail
 */
public class SemanticRecognizer implements IQuestionRecognizer {

    @Override
    public QuestionResult classify(Query query) {
        try {
            DocumentSearcher semanticSearcher = new DocumentSearcher();
            FullTextSearcher fullTextSearcher = new FullTextSearcher();
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
        
            List<DocumentIndexEntry> semanticEntries = semanticSearcher.search(entry);
            List<DocumentIndexEntry> fullTextEntries = fullTextSearcher.search(entry);
            
            Map<String, Float> scorer = new HashMap<String, Float>();
            Map<String, Integer> counter = new HashMap<String, Integer>();
            
            for(DocumentIndexEntry semantic: semanticEntries) {
                if(!scorer.containsKey(semantic.questionId)) {
                    counter.put(semantic.questionId, 1);
                    scorer.put(semantic.questionId, semantic.score);
                } else {
                    Integer curCount = counter.get(semantic.questionId);
                    Float curScore = scorer.get(semantic.questionId);
                    curCount++;
                    curScore += semantic.score;
                    counter.put(semantic.questionId, curCount);
                    scorer.put(semantic.questionId, curScore);
                }
            }
            
            for(DocumentIndexEntry fullText: fullTextEntries) {
                if(scorer.containsKey(fullText.questionId)) {
                    Float curScore = scorer.get(fullText.questionId);
                    curScore += fullText.score * 1.5f;
                    scorer.put(fullText.questionId, curScore);
                }
            }
            
            String bestId = null;
            Float bestScore = 0f;
            
            for(String id: scorer.keySet()) {
                Float curScore = scorer.get(id) / (float) Math.sqrt(counter.get(id));
                if(curScore > bestScore) {
                    bestScore = curScore;
                    bestId = id;
                }
            }
            
            if(bestId == null) {
                return null;
            }
            
//            if(bestScore < 0.1) {
//                return null;
//            }
            
            return new QuestionResult(bestId, bestScore);
                
        } catch (IOException ex) {
            Logger.getLogger(SemanticRecognizer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(SemanticRecognizer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (QueryNodeException ex) {
            Logger.getLogger(SemanticRecognizer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
}
