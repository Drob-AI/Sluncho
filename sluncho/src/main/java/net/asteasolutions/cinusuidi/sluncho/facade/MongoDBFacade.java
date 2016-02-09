package net.asteasolutions.cinusuidi.sluncho.facade;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Projections;

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

    public void createXmlDocument(Map<String, Object> documentMap) {
        MongoCollection<Document> qaCollection = slunchoDB.getCollection("XmlQuestions");

        Document newQuestion = new Document(documentMap);
        qaCollection.insertOne(newQuestion);
    }
    
    public Document getXmlDocument(String id) {
        MongoCollection<Document> qaCollection = slunchoDB.getCollection("XmlQuestions");
        BasicDBObject query = new BasicDBObject();
        query.put("id", id);
        Document result = qaCollection.find(query).first();
        return result;
    }
    
    public FindIterable<Document> getAllXmlDocuments() {
        MongoCollection<Document> qaCollection = slunchoDB.getCollection("XmlQuestions");

        FindIterable<Document> result = qaCollection.find();
        return result;
    }
}
