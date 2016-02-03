package net.asteasolutions.cinusuidi.sluncho.bot.errorCorrection;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.*;

public class NamedEntitySearcher {
	public ArrayList<NamedEntity> search(String queryString) throws IOException, ParseException, QueryNodeException {
		ArrayList<NamedEntity> result = new ArrayList<NamedEntity>();
		String indexPath = "namedEntityIndex";
		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexPath)));
		
		IndexSearcher searcher = new IndexSearcher(reader);
		Analyzer analyzer = new StandardAnalyzer();
		//StandardQueryParser parser = new StandardQueryParser(analyzer);
		QueryParser parser = new QueryParser("name", analyzer);
		Query query = parser.parse(queryString);
		//Query query = parser.parse(queryString, "name");
		
		TopDocs results = searcher.search(query, 5);
		
		ScoreDoc[] hits = results.scoreDocs;	
		int numTotalHits = Math.min(results.totalHits, 5);
		for(int i = 0; i < numTotalHits; i++) {
			Document doc = searcher.doc(hits[i].doc);
			NamedEntity entity = new NamedEntity();
			entity.type = doc.get("type");
			entity.name = doc.get("name");
			entity.score = hits[i].score;
			result.add(entity);
		}
		
		return result;
	}
}
