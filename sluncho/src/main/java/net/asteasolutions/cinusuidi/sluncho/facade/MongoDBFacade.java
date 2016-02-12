package net.asteasolutions.cinusuidi.sluncho.facade;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.Block;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Projections;
import java.util.List;
import net.asteasolutions.cinusuidi.sluncho.model.Question;

public class MongoDBFacade {

    private MongoClient mongoClient;
    private MongoDatabase slunchoDB;

    public MongoDBFacade() {
        mongoClient = new MongoClient("localhost", 27017);
        slunchoDB = mongoClient.getDatabase("sluncho");
    }

    public ArrayList<Document> getAllDocIds() {
        ArrayList<Document> result = new ArrayList<Document>();
        MongoCollection<Document> qaCollection = slunchoDB.getCollection("QuestionAnswer");
        MongoCursor<Document> cursor = qaCollection.find().projection(Projections.fields(Projections.include("_id"))).iterator();
        try {
            while (cursor.hasNext()) {
                Document nextDoc = cursor.next();
                result.add(nextDoc);
            }
        } finally {
            cursor.close();
        }
        return result;
    }

    public Document getDocument(String id) {
        MongoCollection<Document> qaCollection = slunchoDB.getCollection("QuestionAnswer");
        BasicDBObject query = new BasicDBObject();
        query.put("_id", new ObjectId(id));
        Document result = qaCollection.find(query).first();
        return result;
    }

    public void createDocument(String question) {
        MongoCollection<Document> qaCollection = slunchoDB.getCollection("QuestionAnswer");
        Map<String, Object> questionMap = new HashMap<>();
        questionMap.put("question", question);
        questionMap.put("answer", "");
        questionMap.put("resolved", false);
        Document newQuestion = new Document(questionMap);
        qaCollection.insertOne(newQuestion);
    }

    public void createXmlDocument(Question question) {
        MongoCollection<Document> qaCollection = slunchoDB.getCollection("XmlQuestions");

        qaCollection.insertOne(question.toJSON());
    }

    public Document getXmlDocument(String id) {
        MongoCollection<Document> qaCollection = slunchoDB.getCollection("XmlQuestions");
        BasicDBObject query = new BasicDBObject();
        query.put("id", id);
        Document result = qaCollection.find(query).first();
        return result;
    }

    public List<Question> getAllOriginalQuestions() {
        MongoCollection<Document> qaCollection = slunchoDB.getCollection("XmlQuestions");

        BasicDBObject query = new BasicDBObject();
        query.put("isOriginalQuestion", Question.ORIGINAL_QUESTION);
        FindIterable<Document> result = qaCollection.find(query);
        
        final List<Question> allOriginalQuesions = new ArrayList<>();
        result.forEach(new Block<Document>() {
            @Override
            public void apply(final Document document) {
                String questionId = (String) document.get("questionId"); 
                String groupId = (String) document.get("groupId");
                String subject = (String) document.get("subject");
                String body = (String) document.get("body");
                Question question = new Question(questionId, (byte)0, groupId, subject, body);
                allOriginalQuesions.add(question);
            }
        });
        return allOriginalQuesions;
    }
    
    public List<Question> getAllRelevantQuestions(String groupId) {
        MongoCollection<Document> qaCollection = slunchoDB.getCollection("XmlQuestions");
        
        BasicDBObject query = new BasicDBObject();
        query.put("isOriginalQuestion", Question.RELEVANT_QUESTION);
        query.put("groupId", groupId);
        FindIterable<Document> result = qaCollection.find(query);
        
        final List<Question> allRelevantQuesions = new ArrayList<>();
        result.forEach(new Block<Document>() {
            @Override
            public void apply(final Document document) {
                
                String questionId = (String) document.get("questionId"); 
                String groupId = (String) document.get("groupId");
                String subject = (String) document.get("subject");
                String body = (String) document.get("body");
                String isRelevant = (String) document.get("isRelevantToOriginalQuestion");
                Question question = new Question(questionId, (byte)1, groupId, subject, body, isRelevant);
                allRelevantQuesions.add(question);
            }
        });
        return allRelevantQuesions;
    }
    
    public List<Question> getAllQuestions() {
        MongoCollection<Document> qaCollection = slunchoDB.getCollection("XmlQuestions");
        
        BasicDBObject query = new BasicDBObject();
        FindIterable<Document> result = qaCollection.find(query);
        
        final List<Question> allRelevantQuesions = new ArrayList<>();
        result.forEach(new Block<Document>() {
            @Override
            public void apply(final Document document) {
                
                String questionId = (String) document.get("questionId"); 
                String groupId = (String) document.get("groupId");
                String subject = (String) document.get("subject");
                String body = (String) document.get("body");
                String isRelevant = (String) document.get("isRelevantToOriginalQuestion");
                Question question = new Question(questionId, (byte)1, groupId, subject, body, isRelevant);
                allRelevantQuesions.add(question);
            }
        });
        return allRelevantQuesions;
    }
}
