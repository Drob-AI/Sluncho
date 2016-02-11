package net.asteasolutions.cinusuidi.sluncho.oneOutValidation;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import net.asteasolutions.cinusuidi.sluncho.bot.questionRecognizers.Doc2VecGroupClassifier;
import net.asteasolutions.cinusuidi.sluncho.data.QuestionRepository;
import net.asteasolutions.cinusuidi.sluncho.model.Question;

public class OneOutValidation {
	//this could take hours: 
	public  void runDoc2vecClassifierFullTest(){
		QuestionRepository.extractAllOneOutSets();
		Integer success = new Integer(0);
		System.out.println(QuestionRepository.oneOutFullTrainingSets.size());
		for (Map.Entry<String, List<Question>> entry : QuestionRepository.oneOutFullTrainingSets.entrySet()) {
			String label = entry.getKey();
		    List<Question> groupedQuestions = entry.getValue();
		    
		    Doc2VecGroupClassifier.trainWithQuestions(groupedQuestions);
		    
		    Doc2VecGroupClassifier classifyer = new Doc2VecGroupClassifier();
		    Question forTesting = QuestionRepository.oneOutFullTestingSet.get(label);
		    String resultLabel = classifyer.classifyToGroup(forTesting).getFirst();
		    if(resultLabel.equals(forTesting.getGroupId())){
		    	success++;
		    }
		    Doc2VecGroupClassifier.reset();
		}
		System.out.println(success/ (QuestionRepository.oneOutFullTestingSet.size()));
	}
	
	public void runDoc2vecClassifierrRandomTest(){
		QuestionRepository.extractRandomOneOutSet();
		Integer success = new Integer(0);
		
		Doc2VecGroupClassifier.trainWithQuestions(QuestionRepository.oneOutRandomTrainingSet);
//		Doc2VecGroupClassifier.train();
		for (Question forTesting: QuestionRepository.oneOutRandomTestingSet) {
		    Doc2VecGroupClassifier classifyer = new Doc2VecGroupClassifier();
		    
		    String resultLabel = classifyer.classifyToGroup(forTesting).getFirst();
		    if(resultLabel.equals(forTesting.getGroupId())){
		    	success++;
		    }
		}
		System.out.println(success + "/" + QuestionRepository.oneOutRandomTestingSet.size());
		
		BigDecimal all = new BigDecimal(QuestionRepository.oneOutRandomTestingSet.size());
		BigDecimal precision = new BigDecimal(success).divide(all);
		System.out.println(precision.toString());
	}
	
//	public static void main(String args[]) {
//
//        QuestionRepository.extractOriginalQuestions();
//        QuestionRepository.extractAllLabels();
//        QuestionRepository.extractAllQuestions();
//        
//		OneOutValidation a = new OneOutValidation();
//		a.runDoc2vecClassifierrRandomTest();
//		Doc2VecGroupClassifier.reset();
//	}
	
}
