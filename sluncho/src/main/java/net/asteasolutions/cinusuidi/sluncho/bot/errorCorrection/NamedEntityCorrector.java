package net.asteasolutions.cinusuidi.sluncho.bot.errorCorrection;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;

public class NamedEntityCorrector {
	
	public ArrayList<NamedEntity> candidates(String entity) {
		NamedEntitySearcher searcher = new NamedEntitySearcher();
		try {
			String query = "";
			String[] words = entity.split("\\s+");
			for(int i = 0; i < words.length; i++) {
				query +=  words[i] + "~0.8 ";
			}
			
			System.out.println(query);
			return searcher.search(query);
		} catch (IOException | ParseException | QueryNodeException e) {
			e.printStackTrace();
		}
		return new ArrayList<NamedEntity>();
	}
	
}