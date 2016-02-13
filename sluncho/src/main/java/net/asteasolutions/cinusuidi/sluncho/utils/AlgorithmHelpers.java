package net.asteasolutions.cinusuidi.sluncho.utils;

import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.en.EnglishAnalyzer;

public class AlgorithmHelpers {
	public List<String> getListOfStopWords() {
		java.util.Iterator<Object> iter = EnglishAnalyzer.getDefaultStopSet().iterator();
    	List<String> stopWords = new ArrayList<String>();
    	while(iter.hasNext()) {
    		char[] stopWord = (char[]) iter.next();
    	    stopWords.add(new String (stopWord));
    	}
    	return stopWords;
	}
}
