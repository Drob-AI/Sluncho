package net.asteasolutions.cinusuidi.sluncho.bot;

import net.asteasolutions.cinusuidi.sluncho.model.Question;

public class QueryResult {

	private float certainty;
	private Question content;

	public QueryResult(Question content, float certainty) {
		this.content = content;
		this.certainty = certainty;
	}
        
        public Question question () {
            return content;
        }
	
	public String content() {
            if(content == null) {
                return null;
            }
            
            return content.getBody();
	}

	public float certainty() {
		return certainty;
	}
}
