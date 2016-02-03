package net.asteasolutions.cinusuidi.sluncho.bot;

public class QueryResult {

	private float certainty;
	private String content;

	public QueryResult(String content, float certainty) {
		this.content = content;
		this.certainty = certainty;
	}
	
	public String content() {
		return content;
	}

	public float certainty() {
		return certainty;
	}
}
