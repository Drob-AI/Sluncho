/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.asteasolutions.cinusuidi.sluncho.documentIndex;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

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

import ch.qos.logback.core.util.FileUtil;

/**
 *
 * @author mihail
 */
public class FullTextIndexer {
    String indexPath = "fullTextIndex";
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

    void index(String questionId, String groupId, String question, String answer, String context) {
        try {
            Document doc = new Document();
            Field questionIdField = new TextField("questionId", questionId, Field.Store.YES);
            Field groupIdField = new TextField("groupId", groupId, Field.Store.YES);
            Field questionField = new TextField("question", question, Field.Store.YES);
            Field answerField = new TextField("answer", answer, Field.Store.YES);
            Field contextField = new TextField("context", context, Field.Store.YES);
            
            doc.add(questionIdField);
            doc.add(groupIdField);
            doc.add(questionField);
            doc.add(answerField);
            doc.add(contextField);
            
            writer.addDocument(doc);
        } catch (IOException ex) {
            Logger.getLogger(DocumentIndex.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

	public boolean isBuild() {
		return new File(indexPath).exists();
	}
}
