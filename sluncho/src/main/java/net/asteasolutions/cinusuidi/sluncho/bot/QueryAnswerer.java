package net.asteasolutions.cinusuidi.sluncho.bot;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.asteasolutions.cinusuidi.sluncho.bot.errorCorrection.POSPipelineProcessor;

import net.asteasolutions.cinusuidi.sluncho.bot.postPipelineProcessors.IPostPipelineProcessor;
import net.asteasolutions.cinusuidi.sluncho.bot.postPipelineProcessors.LuceneNamedEntityCorrector;
import net.asteasolutions.cinusuidi.sluncho.bot.postPipelineProcessors.SynonymusQueryEnchancer;
import net.asteasolutions.cinusuidi.sluncho.bot.questionRecognizers.IQuestionRecognizer;
import net.asteasolutions.cinusuidi.sluncho.bot.questionRecognizers.SemanticRecognizer;
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
//                questionHandlers.add(new FullTextRecognizer());
//              questionHandlers.add(new WordEmbeddingsRecognizer());
	}
	
	public static QueryResult getQueryResult(Query query) {
		ArrayList<Query> alternateQueries = postProcessQuery(query);

		Iterator<Query> iter = alternateQueries.iterator();
                
                ArrayList<QuestionResult> results = new ArrayList<>();

		while(iter.hasNext()) {
                    Query curQuery = iter.next();
                    System.out.println("Search for answer for query: " + query.orderedTokens);
                    List<QuestionResult> qResults = getQueryAnswer(curQuery);
                    results.addAll(qResults);
		}
                
                //VERY VERY IMPORTANT
                //TODO: use some algorighm to determine this instead of taking the first element
                QuestionResult qResult = results.get(0);
                ClassifiedResult result = new ClassifiedResult(qResult);
                //VERY VERY IMPORTANT
                
                if(result != null && result.certainty() > 0) {
                    return result.getQueryResult();
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
	
	private static List<QuestionResult> getQueryAnswer(Query query) {
		Iterator<IQuestionRecognizer> iter = questionHandlers.iterator();
                
                ArrayList<QuestionResult> answers = new ArrayList<>();
		
		while(iter.hasNext()) {
                    IQuestionRecognizer recognizer = iter.next();
                    List<QuestionResult> recognizerResults = recognizer.classify(query);
                    //TODO: add some kind of normalization for scores here ?
                    for (QuestionResult result: recognizerResults) {
                        answers.add(result);
                    }
		}
		
		return answers;
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