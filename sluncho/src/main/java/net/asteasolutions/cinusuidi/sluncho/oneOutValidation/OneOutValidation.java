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
		QuestionRepository.Instance().extractAllOneOutSets();
		Integer success = new Integer(0);
		System.out.println(QuestionRepository.Instance().oneOutFullTrainingSets.size());
		for (Map.Entry<String, List<Question>> entry : QuestionRepository.Instance().oneOutFullTrainingSets.entrySet()) {
			String label = entry.getKey();
		    List<Question> groupedQuestions = entry.getValue();
		    
		    Doc2VecGroupClassifier.trainWithQuestions(groupedQuestions);
		    
		    Doc2VecGroupClassifier classifyer = new Doc2VecGroupClassifier();
		    Question forTesting = QuestionRepository.Instance().oneOutFullTestingSet.get(label);
		    String resultLabel = classifyer.classifyToGroup(forTesting).getFirst();
		    if(resultLabel.equals(forTesting.getGroupId())){
		    	success++;
		    }
		    Doc2VecGroupClassifier.reset();
		}
		System.out.println(success/ (QuestionRepository.Instance().oneOutFullTestingSet.size()));
	}
	
	public void runDoc2vecClassifierrRandomTest(){
		QuestionRepository.Instance().extractRandomOneOutSet();
		Integer success = new Integer(0);
		
		Doc2VecGroupClassifier.trainWithQuestions(QuestionRepository.Instance().oneOutRandomTrainingSet);
//		Doc2VecGroupClassifier.train();
		for (Question forTesting: QuestionRepository.Instance().oneOutRandomTestingSet) {
		    Doc2VecGroupClassifier classifyer = new Doc2VecGroupClassifier();
		    
		    String resultLabel = classifyer.classifyToGroup(forTesting).getFirst();
		    if(resultLabel.equals(forTesting.getGroupId())){
		    	success++;
		    }
		}
		System.out.println(success + "/" + QuestionRepository.Instance().oneOutRandomTestingSet.size());
		
		BigDecimal all = new BigDecimal(QuestionRepository.Instance().oneOutRandomTestingSet.size());
		BigDecimal precision = new BigDecimal(success).divide(all);
		System.out.println(precision.toString());
	}
	
	public static void main(String args[]) {       
		OneOutValidation a = new OneOutValidation();
		a.runDoc2vecClassifierrRandomTest();
		Doc2VecGroupClassifier.reset();
	}
	
}
