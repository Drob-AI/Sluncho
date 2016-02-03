package net.asteasolutions.cinusuidi.sluncho.data;

import java.util.LinkedHashMap;

/**
 * A simple cache that stores answers to queries classified as AsteaEntities questions.
 * The QuestionRecognizer will generate unique IDs and use them as keys in this map
 * to store the answer generated for a given query.
 * When the cache is full, oldest entries will be removed
 * 
 */
public class AsteaEntitiesAnswerSource extends LinkedHashMap<String, String> implements IDataSource {
	private static final long serialVersionUID = 6090495522160644737L;

	private static final int DEFAULT_SIZE = 1000;
	
	private int maxSize;
	
	public AsteaEntitiesAnswerSource () {
		this.maxSize = DEFAULT_SIZE;
	}
	
	public AsteaEntitiesAnswerSource (int maxSize) {
		this.maxSize = maxSize;
	}

	public void registerAnswer (String questionId, String answer) {
		this.put(questionId, answer);
	}
	
	@Override
	protected boolean removeEldestEntry(java.util.Map.Entry<String,String> eldest) {
		return this.size() > this.maxSize;
	};
	
	@Override
	public String getDocument(String docId) {
		return this.get(docId);
	}
	
	
}
