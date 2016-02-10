package net.asteasolutions.cinusuidi.sluncho.data;

import java.util.ArrayList;
import java.util.List;

import net.asteasolutions.cinusuidi.sluncho.facade.MongoDBFacade;
import net.asteasolutions.cinusuidi.sluncho.model.Question;

public class QuestionRepository {
	public static List<Question> originalQuestions;
	public static List<Question> allQuestions =  new ArrayList<>();
	public static List<String> labels = new ArrayList<>();
	
	public static List<List<Question>> oneOutTrainingSets;
	public static void setOriginalQuestions(List<Question> originalQ) {
		originalQuestions = originalQ;
	}
	public static void extractOriginalQuestions(){
		MongoDBFacade mongoConnection = new MongoDBFacade();
		originalQuestions = mongoConnection.getAllOriginalQuestions();
		QuestionRepository.setOriginalQuestions(originalQuestions);
	}
	public static void extractAllQuestions() {		
 		if ( QuestionRepository.originalQuestions == null) {
 			extractOriginalQuestions();
 		}
 		MongoDBFacade mongoConnection = new MongoDBFacade();
 		for(Question question: QuestionRepository.originalQuestions) {
 			allQuestions.add(question);
        	List<Question> relQuestions = mongoConnection.getAllRelevantQuestions(question.getGroupId());
        	for(Question relQuestion: relQuestions) {
        		if(!relQuestion.getIsRelevantToOriginalQuestion().equals("Irrelevant")) {
        			allQuestions.add(relQuestion);
        		}
        	}
        }
	}
	
	public static void extractAllLabels() {
		for(Question question: QuestionRepository.originalQuestions) {
 			labels.add(question.getGroupId());
 		}
	}
	
}
