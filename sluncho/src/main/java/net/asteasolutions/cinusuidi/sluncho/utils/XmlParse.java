/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.asteasolutions.cinusuidi.sluncho.utils;

/**
 *
 * @author marmot
 */
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.asteasolutions.cinusuidi.sluncho.facade.MongoDBFacade;
import net.asteasolutions.cinusuidi.sluncho.model.Question;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

public class XmlParse {
    
    private static final byte ORIGINAL_QUESTION = 0;
    
    private static final byte RELEVANT_QUESTION = 1;

    private final String filePath;

    private final String fileName;

    public XmlParse(String filePath, String fileName) {
        this.fileName = fileName;
        this.filePath = filePath + fileName;
    }

    private Document ParseXml() throws DocumentException {
        File file = new File(filePath);
        SAXReader reader = new SAXReader();
        Document doc = reader.read(file);
        return doc;
    }

    private void setRelCommentProperties(Node originalQuestion, Map<String, Object> relQuestionMap) {
        Node thread = originalQuestion.selectSingleNode("Thread");
        List<Node> relComments = thread.selectNodes("RelComment");
        for (Node comment : relComments) {
            String commentId = comment.valueOf("@RELC_ID");
            if (relQuestionMap.isEmpty() || !relQuestionMap.containsKey(commentId)) {
                Map<String, Object> relCommentInfo = new LinkedHashMap<>();
                relCommentInfo.put("RelCBody", comment.selectSingleNode("RelCClean").getText());
                relCommentInfo.put("IsRelevantToOrgQ", comment.valueOf("@RELC_RELEVANCE2ORGQ"));
                relCommentInfo.put("IsRelevantToRelQ", comment.valueOf("@RELC_RELEVANCE2RELQ"));
                relQuestionMap.put(commentId, relCommentInfo);
            }
        }
    }

    private Map<String, Object> setRelQProperties(Node originalQuestion, Map<String, Object> relQuestionMap) {
        Node thread = originalQuestion.selectSingleNode("Thread");
        Node relQuestion = thread.selectSingleNode("RelQuestion");
        String relQId = relQuestion.valueOf("@RELQ_ID");
        Map<String, Object> relQuestionInfo = new LinkedHashMap<>();
        if (relQuestionMap.isEmpty() || !relQuestionMap.containsKey(relQId)) {
            relQuestionInfo.put("IsRelevant", relQuestion.valueOf("@RELQ_RELEVANCE2ORGQ"));
            relQuestionInfo.put("RelQBody", relQuestion.valueOf("RelQClean"));
            relQuestionInfo.put("RelComment", new LinkedHashMap<String, Object>());
            relQuestionMap.put(relQId, relQuestionInfo);
        }
        return relQuestionInfo;
    }

    private Map<String, Object> setOriginalQProperties(Node originalQuestion) {
        Map<String, Object> orgQuestionInfo = new LinkedHashMap<>();

        orgQuestionInfo.put("OrgQBody", originalQuestion.selectSingleNode("OrgQClean").getText());
        orgQuestionInfo.put("RelQuestion", new LinkedHashMap<String, Object>());
        return orgQuestionInfo;
    }

    public Map<String, Object> parseFile() {
        Map<String, Object> documentMap = new LinkedHashMap<>();
        try {
            Document document = ParseXml();

            List<Node> orgQuestions = document.selectNodes("/xml/OrgQuestion");
            for (Node question : orgQuestions) {
                String originalQuestionId = question.valueOf("@ORGQ_ID");
                if (documentMap.isEmpty() || !documentMap.containsKey(originalQuestionId)) {
                    Map<String, Object> orgQ = setOriginalQProperties(question);
                    Map<String, Object> relQuestion = (Map<String, Object>) orgQ.get("RelQuestion");
                    relQuestion = setRelQProperties(question, relQuestion);
                    Map<String, Object> relComment = (Map<String, Object>) relQuestion.get("RelComment");
                    setRelCommentProperties(question, relComment);
                    documentMap.put(originalQuestionId, orgQ);
                } else if (documentMap.containsKey(originalQuestionId)) {
                    Map<String, Object> orgQ = (Map<String, Object>) documentMap.get(originalQuestionId);
                    Map<String, Object> relQuestion = (Map<String, Object>) orgQ.get("RelQuestion");
                    relQuestion = setRelQProperties(question, relQuestion);
                    Map<String, Object> relComment = (Map<String, Object>) relQuestion.get("RelComment");
                    setRelCommentProperties(question, relComment);
                }

            }
        } catch (DocumentException ex) {
            Logger.getLogger(XmlParse.class.getName()).log(Level.SEVERE, null, ex);
        }
        return documentMap;
    }

    public void parseFileAndSaveToDatabase() {
        Map<String, Object> documentMap = parseFile();
        if (!documentMap.isEmpty()) {
            MongoDBFacade mongoConnection = new MongoDBFacade();
            
            for (Map.Entry<String, Object> entry : documentMap.entrySet()) {
                String id = entry.getKey();
                Map<String, Object> orgQuestionInfo = (Map<String, Object>) entry.getValue();

                Question originalQ = new Question(ORIGINAL_QUESTION, id, (String) orgQuestionInfo.get("OrgQBody"));
                mongoConnection.createXmlDocument(originalQ);
                
                Map<String, Object> relevantQ = (Map<String, Object>) orgQuestionInfo.get("RelQuestion");
                for (Object relQValue : relevantQ.values()) {
                    Map<String, Object> relQuestionInfo = (Map<String, Object>) relQValue;
                    String body = (String) relQuestionInfo.get("RelQBody");
                    String isRelevant = (String) relQuestionInfo.get("IsRelevant");
                    Question relativeQ = new Question(RELEVANT_QUESTION, id, body, isRelevant);
                    mongoConnection.createXmlDocument(relativeQ);
                }
            }
        }

    }

    public void parseXmlAndSaveToFile(String fileName, String saveFileLocation) throws IOException {
        Map<String, Object> documentMap = parseFile();

        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(saveFileLocation + fileName + ".txt"), "utf-8"))) {
            for (Object orgQValue : documentMap.values()) {
                Map<String, Object> orgQuestionInfo = (Map<String, Object>) orgQValue;
                writer.write((String) orgQuestionInfo.get("OrgQClean"));
                writer.write(System.getProperty("line.separator"));

                Map<String, Object> relQInfo = (Map<String, Object>) orgQuestionInfo.get("RelQuestion");
                for (Object relQValue : relQInfo.values()) {
                    Map<String, Object> relQProperties = (Map<String, Object>) relQValue;
                    writer.write((String) relQProperties.get("RelQClean"));
                    writer.write(System.getProperty("line.separator"));

                    Map<String, Object> relCommentInfo = (Map<String, Object>) relQProperties.get("RelComment");
                    for (Object relCommentValue : relCommentInfo.values()) {
                        Map<String, Object> relCommentProperties = (Map<String, Object>) relCommentValue;
                        writer.write((String) relCommentProperties.get("RelCClean"));
                        writer.write(System.getProperty("line.separator"));
                    }
                }

            }
        }
    }
}
