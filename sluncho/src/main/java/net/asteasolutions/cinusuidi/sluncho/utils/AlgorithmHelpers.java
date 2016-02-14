package net.asteasolutions.cinusuidi.sluncho.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Map.Entry;

import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.deeplearning4j.berkeley.Pair;

import net.asteasolutions.cinusuidi.sluncho.data.QuestionRepository;
import net.asteasolutions.cinusuidi.sluncho.model.Question;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;

public class AlgorithmHelpers {
	public static final List<String> Domain_stop_words = Arrays.asList("I", "i", "it", "its", "my", "My", "it" ,"//", "any", "some", "about" , "?"
																		, "your", "just", "Thanks", "thanks", "Hi", "hi", "so", "I'm", "i'm", "-", 
																		"also","&", "u", "y", "btw", "it?", ";", "am", ":)", ":D", "guys", "??", "???", ".",
																		"..", "!", "/", "Hello", "hello");
	public List<String> getListOfDomainStopWords() {
		java.util.Iterator<Object> iter = EnglishAnalyzer.getDefaultStopSet().iterator();
    	List<String> stopWords = new ArrayList<String>();
    	while(iter.hasNext()) {
    		char[] stopWord = (char[]) iter.next();
    	    stopWords.add(new String (stopWord));
    	}
    	stopWords.addAll(Domain_stop_words);

    	return stopWords;
	}
	
	
	public List<String> getListOfStopWords() {
		java.util.Iterator<Object> iter = EnglishAnalyzer.getDefaultStopSet().iterator();
    	List<String> stopWords = new ArrayList<String>();
    	while(iter.hasNext()) {
    		char[] stopWord = (char[]) iter.next();
    	    stopWords.add(new String (stopWord));
    	}
    	
    	return stopWords;
	}
	
	public void countAllWords(){
		QuestionRepository repo = QuestionRepository.Instance();
		QuestionRepository.Instance().extractAllQuestions();
		Map<String, Integer> summaryResults = new HashMap<>();
		Comparator<Entry<String, Integer>> compare = new Comparator<Entry<String, Integer>>() {
			public int compare(Entry<String, Integer> first, Entry<String, Integer> second) {
				return first.getValue() > second.getValue() ? 1 : -1;
			}
		};
		
		for(Question q: repo.allQuestions){
			String[] content = q.getBody().split(" ");
			for(int i = 0; i < content.length; i++) {
				if(summaryResults.get(content[i]) == null) {
					summaryResults.put(content[i], 1);
				} else {
					summaryResults.put(content[i], summaryResults.get(content[i]) + 1);
				}
			}
			
		}
		PriorityQueue<Entry<String, Integer>> heap = new PriorityQueue<Entry<String, Integer>>(128, compare);
		
		for (Entry<String, Integer> entry : summaryResults.entrySet()) {
			heap.add(entry);
		}
		
		 while(!heap.isEmpty()) {
			 Entry<String, Integer> entry = heap.poll();
			 System.out.println(entry.getKey() + ": " + entry.getValue());
		}
		System.out.println(getListOfDomainStopWords());
	}
}
