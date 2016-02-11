package net.asteasolutions.cinusuidi.sluncho.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.asteasolutions.cinusuidi.sluncho.facade.MongoDBFacade;
import net.asteasolutions.cinusuidi.sluncho.model.Question;

public class QuestionRepository {
	public static List<Question> originalQuestions;
	public static List<Question> allQuestions =  new ArrayList<>();
	public static List<String> labels = new ArrayList<>();
	
	public static HashMap<String, List<Question>> oneOutTrainingSets = new HashMap<>();
	public static HashMap<String, Question> oneOutTestingSet = new HashMap<>();
	
	private static MongoDBFacade mongoConnection = new MongoDBFacade();
	
	public static void setOriginalQuestions(List<Question> originalQ) {
		originalQuestions = originalQ;
	}
	
	
	public static void extractOriginalQuestions(){
		originalQuestions = mongoConnection.getAllOriginalQuestions();
		QuestionRepository.setOriginalQuestions(originalQuestions);
	}
	
	public static void extractAllQuestions() {		
 		if ( QuestionRepository.originalQuestions == null) {
 			extractOriginalQuestions();
 		}
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
		if ( QuestionRepository.originalQuestions == null) {
 			extractOriginalQuestions();
 		}
		
		for(Question question: QuestionRepository.originalQuestions) {
 			labels.add(question.getGroupId());
 		}
	}
	
	public static void extractOneOutSets() {
		if ( QuestionRepository.originalQuestions == null) {
 			extractOriginalQuestions();
 		}
		
		HashMap<String, List<Question>> allQuestionsGrouped = new HashMap<>();
		int maxSize = 0;
 		for(Question question: QuestionRepository.originalQuestions) {
        	List<Question> relQuestions = new ArrayList<>();
    		for(Question q: mongoConnection.getAllRelevantQuestions(question.getGroupId())) {
    			if(!q.getIsRelevantToOriginalQuestion().equals("Irrelevant")) {
    				relQuestions.add(q);
    			}
    		}
        	
        	if(maxSize < relQuestions.size()) {
        		maxSize = relQuestions.size();
        	}
        	
 			allQuestionsGrouped.put(question.getGroupId(),relQuestions);
 		}
 		
		for (Map.Entry<String, List<Question>> entry : allQuestionsGrouped.entrySet()) {
			String label = entry.getKey();
		    List<Question> groupedQuestions = entry.getValue();
		    for(int j = 0; j < groupedQuestions.size(); j++) {
		    	List<Question> trainSet = setWithout(allQuestionsGrouped, label, j);
		    	oneOutTrainingSets.put(label + new Integer(j).toString(), trainSet);
		    }
		    
		}
	}
	
	private static List<Question> setWithout(HashMap<String, List<Question>> allQuestionsGrouped,String key, Integer index) { 
		List<Question> result = new ArrayList<>();
		for (Map.Entry<String, List<Question>> entry : allQuestionsGrouped.entrySet()) {
			String label = entry.getKey();
		    List<Question> groupedQuestions = entry.getValue();
		    for(int j = 0; j < groupedQuestions.size(); j++) {
		    	if(key.equals(label) && j == index) {
		    		oneOutTestingSet.put(key + index.toString(), groupedQuestions.get(j));
		    	} else {
		    		result.add(groupedQuestions.get(j));
		    	}
		    }
		    
		}
		return result;
	}
	
	public static void main(String args[]){
		extractOneOutSets();
		System.out.println("-------------------_");
		System.out.println(oneOutTrainingSets.size());
		int counter = 0;
		for (Map.Entry<String, List<Question>> entry : oneOutTrainingSets.entrySet()) {
			String label = entry.getKey();
		    List<Question> groupedQuestions = entry.getValue();
		    for(int j = 0; j < groupedQuestions.size(); j++) {
		    	if(groupedQuestions.get(j).equals(oneOutTestingSet.get("Q2960"))) {
		    		System.out.println(true);
		    		counter++;
		    	}
		    }
		}
		System.out.println(counter);
	}
}
