/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.asteasolutions.cinusuidi.sluncho.documentIndex;

import gate.util.Out;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.asteasolutions.cinusuidi.sluncho.bot.errorCorrection.NamedEntity;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 *
 * @author mihail
 */
public class DocumentIndex {
    String indexPath = "questionAnswerIndex";
    IndexWriter writer;
    
    public void init() throws IOException {
        Directory dir = FSDirectory.open(Paths.get(indexPath));
        Analyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig iwc = new IndexWriterConfig(analyzer);

        iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);

        writer = new IndexWriter(dir, iwc);
    }
    
    public void close() throws IOException {
        writer.close();
    }

    void index(DocumentIndexEntry entry) {
        try {
            Document doc = new Document();
            Field typeField = new StringField("type", entry.type, Field.Store.YES);
            Field groupIdField = new StringField("groupId", entry.groupId, Field.Store.YES);
            Field questionIdField = new StringField("questionId", entry.questionId, Field.Store.YES);
            Field subjectField = new TextField("subject", entry.subject, Field.Store.YES);
            Field predicateField = new TextField("predicate", entry.predicate, Field.Store.YES);
            Field additionGroupField = new TextField("additionGroup", entry.additionGroup, Field.Store.YES);
            
            doc.add(typeField);
            doc.add(groupIdField);
            doc.add(questionIdField);
            doc.add(subjectField);
            doc.add(predicateField);
            doc.add(additionGroupField);
            
            writer.addDocument(doc);
        } catch (IOException ex) {
            Logger.getLogger(DocumentIndex.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public boolean isBuild() {
		return new File(indexPath).exists();
	}
}
