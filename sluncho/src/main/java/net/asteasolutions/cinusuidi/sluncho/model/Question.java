/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.asteasolutions.cinusuidi.sluncho.model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.asteasolutions.cinusuidi.sluncho.facade.MongoDBFacade;
import org.bson.Document;

/**
 *
 * @author marmot
 */
public class Question {

    public static final byte ORIGINAL_QUESTION = 1;

    public static final byte RELEVANT_QUESTION = 0;
    
    //1 - original question
    //0 - relevant question
    //2 - comment (not implemented)
    private byte isOriginalQuestion;
    
    private String questionId;

    private String groupId;

    private String subject;
    
    private String body;

    private String isRelevantToOriginalQuestion;

    private List<Question> relQuestions;

    public Question(String id, byte isOriginalQuestion, String groupId, String subject, String body) {
        this.questionId = id;
        this.isOriginalQuestion = isOriginalQuestion;
        this.groupId = groupId;
        this.subject = subject;
        this.body = body;
    }

    public Question(String id, byte isOriginalQuestion, String groupId, String subject, String body, String isRelevantToOriginalQuestion) {
        this.questionId = id;
        this.isOriginalQuestion = isOriginalQuestion;
        this.groupId = groupId;
        this.subject = subject;
        this.body = body;
        this.isRelevantToOriginalQuestion = isRelevantToOriginalQuestion;
    }

    public Document toJSON() {
        Map<String, Object> questionMap = new LinkedHashMap<>();
        questionMap.put("questionId", this.questionId);
        questionMap.put("groupId", groupId);
        questionMap.put("isOriginalQuestion", isOriginalQuestion);
        questionMap.put("subject", this.subject);
        questionMap.put("body", body);
        if (isOriginalQuestion != ORIGINAL_QUESTION) {
            questionMap.put("isRelevantToOriginalQuestion", isRelevantToOriginalQuestion);
        }
        return new Document(questionMap);
    }

    public byte getIsOriginalQuestion() {
        return isOriginalQuestion;
    }

    public void setIsOriginalQuestion(byte isOriginalQuestion) {
        this.isOriginalQuestion = isOriginalQuestion;
    }
    
    public String getQuestionId() {
        return questionId;
    }

    public String getSubject() {
        return subject;
    }
    
    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public List<Question> getRelQuestions() {
        if (isOriginalQuestion == ORIGINAL_QUESTION) {
            if (relQuestions == null) {
                MongoDBFacade mongoConnection = new MongoDBFacade();
                relQuestions = mongoConnection.getAllRelevantQuestions(groupId);
            }
            return relQuestions;
        } else {
            return null;
        }
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getIsRelevantToOriginalQuestion() {
        return isRelevantToOriginalQuestion;
    }

    public void setIsRelevantToOriginalQuestion(String isRelevantToOriginalQuestion) {
        this.isRelevantToOriginalQuestion = isRelevantToOriginalQuestion;
    }

}
