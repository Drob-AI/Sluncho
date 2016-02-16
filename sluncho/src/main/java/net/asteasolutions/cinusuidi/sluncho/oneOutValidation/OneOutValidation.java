package net.asteasolutions.cinusuidi.sluncho.oneOutValidation;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.asteasolutions.cinusuidi.sluncho.App;
import net.asteasolutions.cinusuidi.sluncho.BeforeStartConfig;
import net.asteasolutions.cinusuidi.sluncho.bot.Query;
import net.asteasolutions.cinusuidi.sluncho.bot.QueryAnswerer;
import net.asteasolutions.cinusuidi.sluncho.bot.QueryResult;
import net.asteasolutions.cinusuidi.sluncho.bot.QuestionResult;

import org.deeplearning4j.berkeley.Pair;

import net.asteasolutions.cinusuidi.sluncho.App;
import net.asteasolutions.cinusuidi.sluncho.bot.CompositeQuery;
import net.asteasolutions.cinusuidi.sluncho.bot.Query;
import net.asteasolutions.cinusuidi.sluncho.bot.QuestionResult;
import net.asteasolutions.cinusuidi.sluncho.bot.errorCorrection.POSPipelineProcessor;
import net.asteasolutions.cinusuidi.sluncho.bot.questionRecognizers.BagOfWordsClassifier;
import net.asteasolutions.cinusuidi.sluncho.bot.questionRecognizers.Doc2VecGroupClassifier;

import net.asteasolutions.cinusuidi.sluncho.bot.questionRecognizers.WordEmbeddingsRecognizer;
import net.asteasolutions.cinusuidi.sluncho.bot.questionRecognizers.FullTextRecognizer;
import net.asteasolutions.cinusuidi.sluncho.bot.questionRecognizers.SemanticRecognizer;
import net.asteasolutions.cinusuidi.sluncho.data.IDocumentRepository;
import net.asteasolutions.cinusuidi.sluncho.data.QuestionRepository;
import net.asteasolutions.cinusuidi.sluncho.documentIndex.DocumentIndexer;
import net.asteasolutions.cinusuidi.sluncho.documentIndex.IdentityDocumentParser;
import net.asteasolutions.cinusuidi.sluncho.model.Question;
import net.asteasolutions.cinusuidi.sluncho.questionparser.exception.QuestionParserException;
import net.asteasolutions.cinusuidi.sluncho.utils.CommentSummarizer;

public class OneOutValidation {
	//this could take hours: 
	public OneOutValidation() {
		QuestionRepository.Instance().extractRandomOneOutSet();
		Doc2VecGroupClassifier.trainWithQuestions(QuestionRepository.Instance().oneOutRandomTrainingSet);
	}

	public void runWordEmbeddingsClassifierrRandomTest(Integer topNResults){
		
		Integer success = new Integer(0);
//		WordEmbeddingsRecognizer classifyer = new WordEmbeddingsRecognizer();
		WordEmbeddingsRecognizer classifyer = new WordEmbeddingsRecognizer(
				QuestionRepository.Instance().oneOutRandomTrainingSet);
		
		for (Question forTesting: QuestionRepository.Instance().oneOutRandomTestingSet) {
		    CompositeQuery forTestingQuery = null;
			try {
				forTestingQuery = App.questionParser.parseAll(forTesting.getBody());
				POSPipelineProcessor proc = new POSPipelineProcessor();
				ArrayList<CompositeQuery> forTestingQueryList = new ArrayList<CompositeQuery>();
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
		    	//System.out.println("success++: " + success);
		    }
		    
		}
		System.out.println(success + "/" + QuestionRepository.Instance().oneOutRandomTestingSet.size());
		
		BigDecimal all = new BigDecimal(QuestionRepository.Instance().oneOutRandomTestingSet.size());
		BigDecimal precision = new BigDecimal(success).divide(all);
		System.out.println(precision.toString());
	}
        
    public void runSemanticClasifierRandomTest(int topNResults) throws QuestionParserException {
        Integer success = new Integer(0);
        
        IDocumentRepository trainingSet = QuestionRepository.Instance().getTrainingSetRepository();

        IdentityDocumentParser idParser = new IdentityDocumentParser();

        DocumentIndexer questionIndexer = new DocumentIndexer(trainingSet, idParser);
        
        questionIndexer.indexAll();
        questionIndexer.close();
        
        QueryAnswerer.questionHandlers = new ArrayList<>();
        QueryAnswerer.questionHandlers.add(new SemanticRecognizer());

        for (Question forTesting: QuestionRepository.Instance().oneOutRandomTestingSet) {
            CompositeQuery squery = App.questionParser.parseAll(forTesting.getSubject() + "." + forTesting.getBody());
        
//                List<QuestionResult> bresult = QueryAnswerer.getQueryResult(bquery);
            List<QuestionResult> sresult = QueryAnswerer.getQueryResult(squery);
            
            int checksRemaining = topNResults;

            System.out.println("------------------------");
            for(QuestionResult labelResult: sresult) {
                if(checksRemaining == 0) break;
                System.out.println(labelResult.groupId()+ ": "  + labelResult.certainty());
                if(labelResult.groupId().equals(forTesting.getGroupId())){
                    success++;
                }
                checksRemaining--;
            }

        }
        
        System.out.println(success + "/" + QuestionRepository.Instance().oneOutRandomTestingSet.size());

        BigDecimal all = new BigDecimal(QuestionRepository.Instance().oneOutRandomTestingSet.size());
        BigDecimal precision = new BigDecimal(success).divide(all);
        System.out.println(precision.toString());
    }
    
    public void runFulltextClasifierRandomTest(int topNResults) throws QuestionParserException {
    
            Integer success = new Integer(0);
            
            IDocumentRepository trainingSet = QuestionRepository.Instance().getTrainingSetRepository();
	
            IdentityDocumentParser idParser = new IdentityDocumentParser();

            DocumentIndexer questionIndexer = new DocumentIndexer(trainingSet, idParser);
            
            questionIndexer.indexAll();
            questionIndexer.close();
            
            QueryAnswerer.questionHandlers = new ArrayList<>();
            QueryAnswerer.questionHandlers.add(new FullTextRecognizer());

            for (Question forTesting: QuestionRepository.Instance().oneOutRandomTestingSet) {
                Query bquery = App.questionParser.parse(forTesting.getBody());
                CompositeQuery squery = App.questionParser.parseAll(forTesting.getSubject());
            
//                List<QuestionResult> bresult = QueryAnswerer.getQueryResult(bquery);
                List<QuestionResult> sresult = QueryAnswerer.getQueryResult(squery);
                
                int checksRemaining = topNResults;

                System.out.println("------------------------");
                for(QuestionResult labelResult: sresult) {
                    if(checksRemaining == 0) break;
                    System.out.println(labelResult.groupId()+ ": "  + labelResult.certainty());
                    if(labelResult.groupId().equals(forTesting.getGroupId())){
                        success++;
                        break;
                    }
                    checksRemaining--;
                }

            }
            
            System.out.println(success + "/" + QuestionRepository.Instance().oneOutRandomTestingSet.size());

            BigDecimal all = new BigDecimal(QuestionRepository.Instance().oneOutRandomTestingSet.size());
            BigDecimal precision = new BigDecimal(success).divide(all);
            System.out.println(precision.toString());
        }
    
    public void runDoc2vecClassifierrRandomTest(Integer topNResults) throws QuestionParserException{
        
        Integer success = new Integer(0);
        
        QueryAnswerer.questionHandlers = new ArrayList<>();
        QueryAnswerer.questionHandlers.add(new Doc2VecGroupClassifier());
                
        for (Question forTesting: QuestionRepository.Instance().oneOutRandomTestingSet) {
            CompositeQuery bquery = App.questionParser.parseAll(forTesting.getBody());
//            Query squery = App.questionParser.parse(forTesting.getSubject());
        
            List<QuestionResult> bresult = QueryAnswerer.getQueryResult(bquery);
//          List<QuestionResult> sresult = QueryAnswerer.getQueryResult(squery);
            
            int checksRemaining = topNResults;

            System.out.println("------------------------");
            for(QuestionResult labelResult: bresult) {
                if(checksRemaining == 0) break;
                System.out.println(labelResult.groupId()+ ": "  + labelResult.certainty());
                if(labelResult.groupId().equals(forTesting.getGroupId())){
                    success++;
                    break;
                }
                checksRemaining--;
            }

        }
        
        
//        for (Question forTesting: QuestionRepository.Instance().oneOutRandomTestingSet) {
//          Doc2VecGroupClassifier classifyer = new Doc2VecGroupClassifier();
//          List<QuestionResult> resultsLabel = classifyer.bagginClassifyToTopNGroups(forTesting, topNResults);
//
//     //       System.out.println("------------------------");
//          for(QuestionResult labelResult: resultsLabel) {
//     //         System.out.println(labelResult.getFirst() + ": "  + labelResult.getSecond());
//            if(labelResult.groupId().equals(forTesting.getGroupId())){
//                success++;
//              }
//            }
//        }
        
        System.out.println(success + "/" + QuestionRepository.Instance().oneOutRandomTestingSet.size());

        BigDecimal all = new BigDecimal(QuestionRepository.Instance().oneOutRandomTestingSet.size());
        BigDecimal precision = new BigDecimal(success).divide(all);
        System.out.println(precision.toString());
        
    }
	
    public void runFullSystemClassifierRandomTest(Integer topNResults) throws QuestionParserException {
    	Integer success = new Integer(0);
        
        IDocumentRepository trainingSet = QuestionRepository.Instance().getTrainingSetRepository();

        IdentityDocumentParser idParser = new IdentityDocumentParser();

        DocumentIndexer questionIndexer = new DocumentIndexer(trainingSet, idParser);
        
        questionIndexer.indexAll();
        questionIndexer.close();
        
        QueryAnswerer.questionHandlers = new ArrayList<>();
        
        // assemble Voltron !!!...
        QueryAnswerer.questionHandlers.add(new Doc2VecGroupClassifier());
        QueryAnswerer.questionHandlers.add(new FullTextRecognizer());
        QueryAnswerer.questionHandlers.add(new SemanticRecognizer());
       QueryAnswerer.questionHandlers.add(new WordEmbeddingsRecognizer(QuestionRepository.Instance().oneOutRandomTrainingSet));
       QueryAnswerer.questionHandlers.add(new BagOfWordsClassifier());
        
        for (Question forTesting: QuestionRepository.Instance().oneOutRandomTestingSet) {
            CompositeQuery bquery = App.questionParser.parseAll(forTesting.getBody() + "." + forTesting.getSubject());
            System.err.println(forTesting.getSubject());
            
            List<QuestionResult> bresult = QueryAnswerer.getQueryResult(bquery);
            int checksRemaining = topNResults;

            System.out.println("------------------------");
            for(QuestionResult labelResult: bresult) {
                if(checksRemaining == 0) break;
                
                System.out.println(labelResult.groupId()+ ": "  + labelResult.certainty());
                
                if(labelResult.groupId().equals(forTesting.getGroupId())){
                    success++;
                    break;
                }
                checksRemaining--;
            }
            
            String topGroupId = bresult.get(0).groupId();
            String summary = CommentSummarizer.summarizeGroup(topGroupId);
            System.out.println(">> Summary of comments for " + topGroupId + ": " + summary + "\n");
        }
        
        System.out.println(success + "/" + QuestionRepository.Instance().oneOutRandomTestingSet.size());
        BigDecimal all = new BigDecimal(QuestionRepository.Instance().oneOutRandomTestingSet.size());
        BigDecimal precision = new BigDecimal(success).divide(all);
        System.out.println(precision.toString());
    }
	
}
