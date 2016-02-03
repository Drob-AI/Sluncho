/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.asteasolutions.cinusuidi.sluncho.data;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.asteasolutions.cinusuidi.sluncho.documentIndex.FullTextSearcher;
import org.apache.lucene.queryparser.classic.ParseException;

/**
 *
 * @author mihail
 */
public class SemanticSource implements IDataSource {

    @Override
    public String getDocument(String docId) {
        try {
            FullTextSearcher searcher = new FullTextSearcher();
            String answer = searcher.searchById(docId);
            return answer;
        } catch (ParseException ex) {
            Logger.getLogger(SemanticSource.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SemanticSource.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "I could not find anything in the costitution for your problem :(";
    }
    
}
