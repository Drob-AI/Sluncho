package net.asteasolutions.cinusuidi.sluncho.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import net.asteasolutions.cinusuidi.sluncho.facade.MongoDBFacade;
import net.asteasolutions.cinusuidi.sluncho.model.Question;
import net.asteasolutions.cinusuidi.sluncho.utils.XmlParse;

public class QuestionRepository implements IDocumentRepository {
    private static QuestionRepository instance;
    
    private QuestionRepository() {
        this.mongoConnection = new MongoDBFacade();
        extractOriginalQuestions();
        
        if(originalQuestions.isEmpty()){
            XmlParse parser = new XmlParse(System.getProperty("dataPath"), System.getProperty("dataFileName"));
            parser.parseFileAndSaveToDatabase();
            extractOriginalQuestions();
        }
        
        extractAllQuestions();
        extractAllLabels();
        
        extractAllRelevantQuestions();
    }
    
    public static QuestionRepository Instance() {
        if(instance == null) {
            instance = new QuestionRepository();
        }
        return instance;
    }
    
    public List<Question> originalQuestions;
	public List<Question> allQuestions =  new ArrayList<>();
	public List<String> labels = new ArrayList<>();

	public List<Question> allRelevantQuestions = new ArrayList<>();
	
	//needed for the full testing process
	public HashMap<String, List<Question>> oneOutFullTrainingSets = new HashMap<>();
	public HashMap<String, Question> oneOutFullTestingSet = new HashMap<>();
	
	// needed for the random testing process
	public List<Question> oneOutRandomTrainingSet = new ArrayList<>();
	public List<Question> oneOutRandomTestingSet = new ArrayList<>();
	
	private MongoDBFacade mongoConnection = new MongoDBFacade();
	
	public void setOriginalQuestions(List<Question> originalQ) {
		originalQuestions = originalQ;
	}
	
	
	public void extractOriginalQuestions(){
		originalQuestions = mongoConnection.getAllOriginalQuestions();
		this.setOriginalQuestions(originalQuestions);
	}
	
	public void extractAllQuestions() {		
 		if ( this.originalQuestions == null) {
 			this.extractOriginalQuestions();
 			this.extractAllLabels();
 		}
 		
 		for(Question question: this.originalQuestions) {
 			allQuestions.add(question);
        	List<Question> relQuestions = mongoConnection.getAllRelevantQuestions(question.getGroupId());
        	for(Question relQuestion: relQuestions) {
        		if(!relQuestion.getIsRelevantToOriginalQuestion().equals("Irrelevant")) {
        			allQuestions.add(relQuestion);
        		}
        	}
        }
	}
	
	public void extractAllRelevantQuestions() {		
 		if ( this.originalQuestions == null) {
 			this.extractOriginalQuestions();
 			this.extractAllLabels();
 		}
 		
 		for(Question question: this.originalQuestions) {
 			//allQuestions.add(question);
        	List<Question> relQuestions = mongoConnection.getAllRelevantQuestions(question.getGroupId());
        	for(Question relQuestion: relQuestions) {
        		if(!relQuestion.getIsRelevantToOriginalQuestion().equals("Irrelevant")) {
        			allRelevantQuestions.add(relQuestion);
        		}
        	}
        }
	}
	
	public void extractAllLabels() {
		if ( this.originalQuestions == null) {
 			extractOriginalQuestions();
 		}
		
		for(Question question: this.originalQuestions) {
 			labels.add(question.getGroupId());
 		}
	}
	
	public void extractAllOneOutSets() {
		if ( this.originalQuestions == null) {
 			this.extractOriginalQuestions();
 		}
		
		HashMap<String, List<Question>> allQuestionsGrouped = new HashMap<>();
		int maxSize = 0;
 		for(Question question: this.originalQuestions) {
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
	
	public void extractRandomOneOutSet() {
		if ( this.originalQuestions == null) {
 			extractOriginalQuestions();
 			extractAllQuestions();
 		}
		
		HashMap<String, List<Question>> allQuestionsGrouped = new HashMap<>();
		int maxSize = 0;
 		for(Question question: this.originalQuestions) {
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
		    Integer j = groupedQuestions.size() > 1 ? 1: 0;
		    oneOutRandomTestingSet.add(groupedQuestions.get(j));
		}
		
		oneOutRandomTrainingSet.addAll(allQuestions);
		oneOutRandomTrainingSet.removeAll(oneOutRandomTestingSet);
	}
	
	private List<Question> setWithout(HashMap<String, List<Question>> allQuestionsGrouped,String key, Integer index) { 
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
        
    public IDocumentRepository getTrainingSetRepository () {
        return new ArrayRepository(oneOutRandomTrainingSet);
    }
	
    @Override
    public String[] getDocumentsRefs() throws IOException {
        ArrayList<String> refs = new ArrayList<String>() {};
        for (Question q: allQuestions) {
            refs.add(q.getQuestionId());
        }
        return refs.toArray(new String[refs.size()]);
    }

    @Override
    public IndexableDocument getDocument(String ref) throws IOException {
        for (Question q: allQuestions) {
            if(q.getQuestionId().equals(ref)) {
                IndexableDocument result = new IndexableDocument();
                result.groupId = q.getGroupId();
                result.title = q.getSubject();
                result.content = q.getBody();
                return result;
            }
        }
        return null;
    }
}
