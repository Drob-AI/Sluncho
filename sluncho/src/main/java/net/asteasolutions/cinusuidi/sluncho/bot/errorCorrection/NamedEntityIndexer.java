package net.asteasolutions.cinusuidi.sluncho.bot.errorCorrection;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import gate.util.Out;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;


public class NamedEntityIndexer {
  
	public NamedEntityIndexer() {}

	public static void Index(ArrayList<NamedEntity> namedEntities) {
		String indexPath = "namedEntityIndex";
    
		try {
			Directory dir = FSDirectory.open(Paths.get(indexPath));
			Analyzer analyzer = new StandardAnalyzer();
			IndexWriterConfig iwc = new IndexWriterConfig(analyzer);

			iwc.setOpenMode(OpenMode.CREATE);

			IndexWriter writer = new IndexWriter(dir, iwc);
			indexNamedEntities(writer, namedEntities);

			writer.close();

		} catch (IOException e) {
			Out.pr("Error while indexing named entities");
			e.printStackTrace();
		}
	}

	static void indexNamedEntities(final IndexWriter writer, ArrayList<NamedEntity> namedEntities) throws IOException  {
		Iterator<NamedEntity> iter = namedEntities.iterator();
		while(iter.hasNext()) {
			indexEntity(writer, iter.next());
		}
	}

	static void indexEntity(IndexWriter writer, NamedEntity namedEntity) throws IOException {
		Document doc = new Document();
		Field typeField = new StringField("type", namedEntity.type, Field.Store.YES);
		doc.add(typeField);
		FieldType docTypeWithTerms = new FieldType();
		docTypeWithTerms.setStored(true);
		docTypeWithTerms.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS);
		docTypeWithTerms.setStoreTermVectors(true);
		
		Field nameField = new Field("name", namedEntity.name,docTypeWithTerms);
//		Field nameField = new TextField("name",namedEntity.name, Field.Store.YES);
		doc.add(nameField);
		
		writer.addDocument(doc);
	}
}