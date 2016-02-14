package net.asteasolutions.cinusuidi.sluncho.oneOutValidation;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.deeplearning4j.berkeley.Pair;

import net.asteasolutions.cinusuidi.sluncho.App;
import net.asteasolutions.cinusuidi.sluncho.bot.Query;
import net.asteasolutions.cinusuidi.sluncho.bot.QuestionResult;
import net.asteasolutions.cinusuidi.sluncho.bot.errorCorrection.POSPipelineProcessor;
import net.asteasolutions.cinusuidi.sluncho.bot.questionRecognizers.Doc2VecGroupClassifier;
import net.asteasolutions.cinusuidi.sluncho.bot.questionRecognizers.WordEmbeddingsRecognizer;
import net.asteasolutions.cinusuidi.sluncho.data.QuestionRepository;
import net.asteasolutions.cinusuidi.sluncho.model.Question;
import net.asteasolutions.cinusuidi.sluncho.questionparser.exception.QuestionParserException;

public class OneOutValidation {
	//this could take hours: 
	public OneOutValidation() {
		QuestionRepository.Instance().extractRandomOneOutSet();
		//Doc2VecGroupClassifier.trainWithQuestions(QuestionRepository.Instance().oneOutRandomTrainingSet);
	}
	public  void runDoc2vecClassifierFullTest(){
		QuestionRepository.Instance().extractAllOneOutSets();
		Integer success = new Integer(0);
//		System.out.println(QuestionRepository.Instance().oneOutFullTrainingSets.size());
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
//		System.out.println(success/ (QuestionRepository.Instance().oneOutFullTestingSet.size()));
	}
	
	public void runWordEmbeddingsClassifierrRandomTest(Integer topNResults){
		
		Integer success = new Integer(0);
		List<Question> allRelQ = QuestionRepository.Instance().allRelevantQuestions;
	    WordEmbeddingsRecognizer classifyer = new WordEmbeddingsRecognizer(allRelQ);
		
		for (Question forTesting: QuestionRepository.Instance().oneOutRandomTestingSet) {
		    
		    Query forTestingQuery = null;
			try {
				forTestingQuery = App.questionParser.parse(forTesting.getBody());
				POSPipelineProcessor proc = new POSPipelineProcessor();
				ArrayList<Query> forTestingQueryList = new ArrayList<Query>();
				forTestingQueryList.add(forTestingQuery);
				forTestingQueryList = proc.expand(forTestingQueryList);
				forTestingQuery = forTestingQueryList.get(0);
			} catch (QuestionParserException e) {
				e.printStackTrace();
			}
		    List<QuestionResult> resultsLabel = classifyer.classify(forTestingQuery);
		    resultsLabel = resultsLabel.subList(0, Math.min(resultsLabel.size(), topNResults));
		    
//		    System.out.println("------------------------");
		    for(QuestionResult labelResult: resultsLabel) {
//		    	System.out.println(labelResult.getFirst() + ": "  + labelResult.getSecond());
		    	if(labelResult.groupId().equals(forTesting.getGroupId())){
			    	success++;
			    }
		    }
		    
		}
		System.out.println(success + "/" + QuestionRepository.Instance().oneOutRandomTestingSet.size());
		
		BigDecimal all = new BigDecimal(QuestionRepository.Instance().oneOutRandomTestingSet.size());
		BigDecimal precision = new BigDecimal(success).divide(all);
		System.out.println(precision.toString());
	}

//	public static void main(String args[]) {       
//		OneOutValidation a = new OneOutValidation();
//		
//		System.out.println("???????????????????????");
//		System.out.println("Top 5:");
//		a.runDoc2vecClassifierrRandomTest(5);
//		System.out.println("??????????????????????");
//		System.out.println("Top 1:");
//		a.runDoc2vecClassifierrRandomTest(1);
//		Doc2VecGroupClassifier.reset();
//	}
	
}
