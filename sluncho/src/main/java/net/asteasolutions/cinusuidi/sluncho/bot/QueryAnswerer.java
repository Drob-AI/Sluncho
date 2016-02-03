package net.asteasolutions.cinusuidi.sluncho.bot;

import java.util.ArrayList;
import java.util.Iterator;
import net.asteasolutions.cinusuidi.sluncho.bot.errorCorrection.POSPipelineProcessor;

import net.asteasolutions.cinusuidi.sluncho.bot.postPipelineProcessors.IPostPipelineProcessor;
import net.asteasolutions.cinusuidi.sluncho.bot.postPipelineProcessors.LuceneNamedEntityCorrector;
import net.asteasolutions.cinusuidi.sluncho.bot.postPipelineProcessors.SynonymusQueryEnchancer;
import net.asteasolutions.cinusuidi.sluncho.bot.questionHandlers.IQuestionHandler;
import net.asteasolutions.cinusuidi.sluncho.bot.questionHandlers.SemanticHandler;
import net.asteasolutions.cinusuidi.sluncho.bot.questionRecognizers.IQuestionRecognizer;

public final class QueryAnswerer {
	private static ArrayList<IPostPipelineProcessor> postProcessors = new ArrayList<IPostPipelineProcessor>();
	private static ArrayList<IQuestionHandler> questionHandlers = new ArrayList<IQuestionHandler>();
	
	static {
		postProcessors.add(new LuceneNamedEntityCorrector());
                postProcessors.add(new POSPipelineProcessor());
		postProcessors.add(new SynonymusQueryEnchancer());
//		
//		questionHandlers.add(new HostnameHandler());
//		questionHandlers.add(new AsteaEntitiesHandler());
                questionHandlers.add(new SemanticHandler());
	}
	
	public static QueryResult getQueryResult(Query query) {
		ArrayList<Query> alternateQueries = postProcessQuery(query);
//		ClassifiedResult bestClassifiedResult = null;
		
		Iterator<Query> iter = alternateQueries.iterator();

		while(iter.hasNext()) {
			Query curQuery = iter.next();
			System.out.println("Search for answer for query: " + query.orderedTokens);
			ClassifiedResult result = getQueryAnswer(curQuery);
			if(result != null && result.certainty() > 0) {
                            return result.getQueryResult();
                        }
		}
		
		return new QueryResult("Could not find an answer", 0);
//		return bestClassifiedResult.getQueryResult();
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
		Iterator<IQuestionHandler> iter = questionHandlers.iterator();
		
		while(iter.hasNext()) {
			IQuestionHandler currentHandler = iter.next();
			IQuestionRecognizer recognizer = currentHandler.getQuestionRecognizer();
			QuestionResult currentResult = recognizer.classify(query);
                        if(currentResult != null && currentResult.certainty() > 0) {
                            return new ClassifiedResult(currentResult, currentHandler);
                        }
		}
		
		return new ClassifiedResult(null, null);
	}
	
	private static class ClassifiedResult
	{
		private QuestionResult result;
		private IQuestionHandler handler;

		public ClassifiedResult(QuestionResult bestResult,
				IQuestionHandler bestHandler) {
			this.result = bestResult;
			this.handler = bestHandler;
		}
		
		public QueryResult getQueryResult() {
			if(handler == null || result == null) {
				return new QueryResult("Could not find an answer", 0);
			}
			return new QueryResult(handler.getDataSource().getDocument(result.documentName()), result.certainty());
		}
		
		public float certainty() {
			if(result == null) {
				return 0;
			}
			return result.certainty();
		}
	}
}