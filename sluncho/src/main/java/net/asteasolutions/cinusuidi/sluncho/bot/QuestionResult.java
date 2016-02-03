package net.asteasolutions.cinusuidi.sluncho.bot;

public class QuestionResult {
	private String documentName;
	private float certainty;

	public QuestionResult(String documentName, float certainty) {
		this.documentName = documentName;
		this.certainty = certainty;
	}
	
	public String documentName() {
		return documentName;
	}
	
	public float certainty() {
		return certainty;
	}
}
