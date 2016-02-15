package net.asteasolutions.cinusuidi.sluncho.model;

import java.util.LinkedHashMap;
import java.util.Map;

import org.bson.Document;

public class Comment {
	public String commentId;
	public String relQId;
	public String orgQId;
	public String isRelevantToRelQ;
	public String isRelevantToOrgQ;
	public String body;
	
	public Comment(String commentId, String relQId, String orgQId, 
			String isRelevantToRelQ, String isRelevantToOrgQ, String body) {
		this.commentId = commentId;
		this.relQId = relQId;
		this.orgQId = orgQId;
		this.isRelevantToRelQ = isRelevantToRelQ;
		this.isRelevantToOrgQ = isRelevantToOrgQ;
		this.body = body;
	}
	
	public Document toJSON() {
        Map<String, Object> commentMap = new LinkedHashMap<>();
        commentMap.put("commentId", this.commentId);
        commentMap.put("relQId", this.relQId);
        commentMap.put("orgQId", this.orgQId);
        commentMap.put("isRelevantToRelQ", this.isRelevantToRelQ);
        commentMap.put("isRelevantToOrgQ", this.isRelevantToOrgQ);
        commentMap.put("body", this.body);
        return new Document(commentMap);
    }
}
