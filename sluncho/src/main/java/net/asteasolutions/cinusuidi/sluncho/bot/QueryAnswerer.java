package net.asteasolutions.cinusuidi.sluncho.bot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import net.asteasolutions.cinusuidi.sluncho.bot.errorCorrection.POSPipelineProcessor;

import net.asteasolutions.cinusuidi.sluncho.bot.postPipelineProcessors.IPostPipelineProcessor;
import net.asteasolutions.cinusuidi.sluncho.bot.postPipelineProcessors.LuceneNamedEntityCorrector;
import net.asteasolutions.cinusuidi.sluncho.bot.postPipelineProcessors.SynonymusQueryEnchancer;
import net.asteasolutions.cinusuidi.sluncho.bot.questionRecognizers.BagOfWordsClassifier;
import net.asteasolutions.cinusuidi.sluncho.bot.questionRecognizers.FullTextRecognizer;
import net.asteasolutions.cinusuidi.sluncho.bot.questionRecognizers.IQuestionRecognizer;
import net.asteasolutions.cinusuidi.sluncho.bot.questionRecognizers.SemanticRecognizer;
//import net.asteasolutions.cinusuidi.sluncho.bot.questionRecognizers.WordEmbeddingsRecognizer;
import net.asteasolutions.cinusuidi.sluncho.data.QuestionRepository;
import net.asteasolutions.cinusuidi.sluncho.model.Question;

public final class QueryAnswerer {
	public static ArrayList<IPostPipelineProcessor> postProcessors = new ArrayList<IPostPipelineProcessor>();
	public static ArrayList<IQuestionRecognizer> questionHandlers = new ArrayList<IQuestionRecognizer>();
	
	static {
//		postProcessors.add(new LuceneNamedEntityCorrector());
                postProcessors.add(new POSPipelineProcessor());
//		postProcessors.add(new SynonymusQueryEnchancer());
//		
//		questionHandlers.add(new HostnameHandler());
//		questionHandlers.add(new AsteaEntitiesHandler());
                questionHandlers.add(new SemanticRecognizer());
                questionHandlers.add(new FullTextRecognizer());
                questionHandlers.add(new BagOfWordsClassifier());
//                questionHandlers.add(new WordEmbeddingsRecognizer());
	}
	
	public static List<QuestionResult> getQueryResult(CompositeQuery query) {
            ArrayList<CompositeQuery> alternateQueries = postProcessQuery(query);

            Iterator<CompositeQuery> iter = alternateQueries.iterator();

            ArrayList<QuestionResult> results = new ArrayList<>();

            //TODO: find these some better way
            while(iter.hasNext()) {
                CompositeQuery curQuery = iter.next();
                System.out.println("Search for answer for query: " + query.orderedTokens);
                List<QuestionResult> qResults = getQueryAnswer(curQuery);
                if(results.size() == 0){
                    for(int i = 0; i < qResults.size(); i++) {
                            qResults.get(i).votes = (qResults.size() - i);
                    }
                    results.addAll(qResults);
                } else {
                        for(QuestionResult foundResult: results) {
                            int i = 0;
                            for(QuestionResult newResult: qResults) {
                                    if (foundResult.groupId().equals(newResult.groupId())) {
                                            foundResult.votes += (qResults.size()  - i);
                                    }
                                    i++;
                            }
                        }
                }
            }
        
            results.sort(new Comparator<QuestionResult>() {

                    @Override
                    public int compare(QuestionResult o1, QuestionResult o2) {
                        return ((Float)o2.certainty()).compareTo(o1.certainty());
                    }
            });
		
            return results;
	}
	
	private static ArrayList<CompositeQuery> postProcessQuery(CompositeQuery query) {
		Iterator<IPostPipelineProcessor> processorIter = postProcessors.iterator();
		
		ArrayList<CompositeQuery> result = new ArrayList<>();
		result.add(query);
		
		while(processorIter.hasNext()) {
			IPostPipelineProcessor currentProcessor = processorIter.next();
			result = currentProcessor.expand(result);
		}
		
		return result;
	}
	
	private static List<QuestionResult> getQueryAnswer(CompositeQuery query) {
            Iterator<IQuestionRecognizer> iter = questionHandlers.iterator();

            ArrayList<List<QuestionResult>> questionResults = new ArrayList<>();
            ArrayList<QuestionResult> results = new ArrayList<>();

            int maxLen = 0;
            //TODO: find these some better way
            while(iter.hasNext()) {
                IQuestionRecognizer curRec = iter.next();
                System.out.println("Search for answer for query: " + query.orderedTokens);
                List<QuestionResult> qResults = curRec.classify(query);
                maxLen = Integer.max(qResults.size(), maxLen);
                questionResults.add(qResults);
            }
            
            for(List<QuestionResult> qResults: questionResults) {
                float min = Float.MAX_VALUE, max = Float.MIN_VALUE;
                for(QuestionResult newResult: qResults) {
                    float score = newResult.certainty();
                    min = Float.min(score, min);
                    max = Float.max(score, max);
                }
                float range = max - min;
                if(results.size() == 0){
                    for(int i = 0; i < qResults.size(); i++) {
                        QuestionResult result = qResults.get(i);
                        result.setCertainty((result.certainty() - min) / range);
                    }
                    results.addAll(qResults);
                } else {
                    for(QuestionResult foundResult: results) {
                        int i = 0;
                        for(QuestionResult newResult: qResults) {
                            if (foundResult.groupId().equals(newResult.groupId())) {
                                float score = foundResult.certainty() + (newResult.certainty() - min) / range;
                                foundResult.setCertainty(score);
                            }
                            i++;
                        }
                    }
                }
            }
        
            results.sort(new Comparator<QuestionResult>() {

                    @Override
                    public int compare(QuestionResult o1, QuestionResult o2) {
                        return ((Float)o2.certainty()).compareTo(o1.certainty());
                    }
            });
		
            return results;
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