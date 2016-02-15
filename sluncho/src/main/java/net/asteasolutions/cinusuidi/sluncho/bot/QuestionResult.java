package net.asteasolutions.cinusuidi.sluncho.bot;

public class QuestionResult {
	private String documentName;
	private String groupId;
	private float certainty;

	public QuestionResult(String documentName,String groupId, float certainty) {
            this.documentName = documentName;
            this.certainty = certainty;
            this.groupId = groupId;
	}
	
	public String documentName() {
            return documentName;
	}
	
    public String groupId() {
        return groupId;
    }
        
	public float certainty() {
            return certainty;
	}
	
	public void setCertainty(float newCertainty) {
        certainty = newCertainty;
	}
}
