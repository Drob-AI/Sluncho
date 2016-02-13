package net.asteasolutions.cinusuidi.sluncho.bot;

import java.util.ArrayList;
import java.util.Iterator;
import net.asteasolutions.cinusuidi.sluncho.bot.errorCorrection.POSPipelineProcessor;

import net.asteasolutions.cinusuidi.sluncho.bot.postPipelineProcessors.IPostPipelineProcessor;
import net.asteasolutions.cinusuidi.sluncho.bot.postPipelineProcessors.LuceneNamedEntityCorrector;
import net.asteasolutions.cinusuidi.sluncho.bot.postPipelineProcessors.SynonymusQueryEnchancer;
import net.asteasolutions.cinusuidi.sluncho.bot.questionRecognizers.FullTextRecognizer;
import net.asteasolutions.cinusuidi.sluncho.bot.questionRecognizers.IQuestionRecognizer;
import net.asteasolutions.cinusuidi.sluncho.bot.questionRecognizers.SemanticRecognizer;
import net.asteasolutions.cinusuidi.sluncho.bot.questionRecognizers.WordEmbeddingsRecognizer;
import net.asteasolutions.cinusuidi.sluncho.data.QuestionRepository;
import net.asteasolutions.cinusuidi.sluncho.model.Question;

public final class QueryAnswerer {
	private static ArrayList<IPostPipelineProcessor> postProcessors = new ArrayList<IPostPipelineProcessor>();
	private static ArrayList<IQuestionRecognizer> questionHandlers = new ArrayList<IQuestionRecognizer>();
	
	static {
//		postProcessors.add(new LuceneNamedEntityCorrector());
                postProcessors.add(new POSPipelineProcessor());
		postProcessors.add(new SynonymusQueryEnchancer());
//		
//		questionHandlers.add(new HostnameHandler());
//		questionHandlers.add(new AsteaEntitiesHandler());
                questionHandlers.add(new SemanticRecognizer());
                questionHandlers.add(new FullTextRecognizer());
                questionHandlers.add(new WordEmbeddingsRecognizer());
	}
	
	public static QueryResult getQueryResult(Query query) {
		ArrayList<Query> alternateQueries = postProcessQuery(query);

		Iterator<Query> iter = alternateQueries.iterator();

		while(iter.hasNext()) {
			Query curQuery = iter.next();
			System.out.println("Search for answer for query: " + query.orderedTokens);
			ClassifiedResult result = getQueryAnswer(curQuery);
			if(result != null && result.certainty() > 0) {
                            return result.getQueryResult();
                        }
		}
		
		return new QueryResult(null, 0);
	}
	
	private static ArrayList<Query> postProcessQuery(Query query) {
		Iterator<IPostPipelineProcessor> processorIter = postProcessors.iterator();
		
		ArrayList<Query> result = new ArrayList<Query>();
		result.add(query);
		
		while(processorIter.hasNext()) {
			IPostPipelineProcessor currentProcessor = processorIter.next();
			result = currentProcessor.expand(result);
		}
		
		return result;
	}
	
	private static ClassifiedResult getQueryAnswer(Query query) {
		Iterator<IQuestionRecognizer> iter = questionHandlers.iterator();
		
		while(iter.hasNext()) {
                    IQuestionRecognizer recognizer = iter.next();
                    QuestionResult currentResult = recognizer.classify(query);
                    if(currentResult != null && currentResult.certainty() > 0) {
                        return new ClassifiedResult(currentResult);
                    }
		}
		
		return new ClassifiedResult(null);
	}
	
	private static class ClassifiedResult
	{
		private QuestionResult result;

		public ClassifiedResult(QuestionResult bestResult) {
			this.result = bestResult;
		}
		
		public QueryResult getQueryResult() {
			if(result == null) {
				return new QueryResult(null, 0);
			}
                        QuestionRepository repo = QuestionRepository.Instance();
                        
                        Question resultQuestion = null;
                        
                        for(Question q : repo.allQuestions) {
                            if(q.getQuestionId().equals(result.documentName())) {
                                resultQuestion = q;
                                break;
                            }
                        }
                        
			return new QueryResult(resultQuestion, result.certainty());
		}
		
		public float certainty() {
			if(result == null) {
				return 0;
			}
			return result.certainty();
		}
	}
}