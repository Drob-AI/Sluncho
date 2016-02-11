package net.asteasolutions.cinusuidi.sluncho.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import net.asteasolutions.cinusuidi.sluncho.facade.MongoDBFacade;
import net.asteasolutions.cinusuidi.sluncho.model.Question;

public class QuestionRepository {
	public static List<Question> originalQuestions;
	public static List<Question> allQuestions =  new ArrayList<>();
	public static List<String> labels = new ArrayList<>();
	
	//needed for the full testing process
	public static HashMap<String, List<Question>> oneOutFullTrainingSets = new HashMap<>();
	public static HashMap<String, Question> oneOutFullTestingSet = new HashMap<>();
	
	// needed for the random testing process
	public static List<Question> oneOutRandomTrainingSet = new ArrayList<>();
	public static List<Question> oneOutRandomTestingSet = new ArrayList<>();
	
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
 			extractAllLabels();
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
	
	public static void extractAllOneOutSets() {
		if ( QuestionRepository.originalQuestions == null) {
 			extractOriginalQuestions();
 		}
		
		HashMap<String, List<Question>> allQuestionsGrouped = new HashMap<>();
		int maxSize = 0;
 		for(Question question: QuestionRepository.originalQuestions) {
        	List<Question> relQuestions = new ArrayList<>();
        	relQuestions.add(question);
        	
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
		    	oneOutFullTrainingSets.put(label + new Integer(j).toString(), trainSet);
		    }
		    
		}
	}
	
	public static void extractRandomOneOutSet() {
		if ( QuestionRepository.originalQuestions == null) {
 			extractOriginalQuestions();
 			extractAllQuestions();
 		}
		
		HashMap<String, List<Question>> allQuestionsGrouped = new HashMap<>();
		int maxSize = 0;
 		for(Question question: QuestionRepository.originalQuestions) {
        	List<Question> relQuestions = new ArrayList<>();
        	relQuestions.add(question);
        	
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
		    
//		    Integer j = ThreadLocalRandom.current().nextInt(0, groupedQuestions.size());
		    Integer j = 0;
		    oneOutRandomTestingSet.add(groupedQuestions.get(j));
		}
		
		oneOutRandomTrainingSet.addAll(allQuestions);
		oneOutRandomTrainingSet.removeAll(oneOutRandomTestingSet);
	}
	
	private static List<Question> setWithout(HashMap<String, List<Question>> allQuestionsGrouped,String key, Integer index) { 
		List<Question> result = new ArrayList<>();
		for (Map.Entry<String, List<Question>> entry : allQuestionsGrouped.entrySet()) {
			String label = entry.getKey();
		    List<Question> groupedQuestions = entry.getValue();
		    for(int j = 0; j < groupedQuestions.size(); j++) {
		    	if(key.equals(label) && j == index) {
		    		oneOutFullTestingSet.put(key + index.toString(), groupedQuestions.get(j));
		    	} else {
		    		result.add(groupedQuestions.get(j));
		    	}
		    }
		    
		}
		return result;
	}
	
//	public static void main(String args[]){
//		extractRandomOneOutSet();
//		System.out.println(oneOutRandomTrainingSet.size());
//		oneOutRandomTrainingSet.removeAll(oneOutRandomTestingSet);
//		System.out.println(oneOutRandomTrainingSet.size());
//	}
}
