package net.asteasolutions.cinusuidi.sluncho.bot.questionRecognizers;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.PriorityQueue;

import org.deeplearning4j.berkeley.Pair;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;

import net.asteasolutions.cinusuidi.sluncho.bot.Query;
import net.asteasolutions.cinusuidi.sluncho.bot.QuestionResult;
import net.asteasolutions.cinusuidi.sluncho.bot.word2vecClassifierUtils.QuestionSentencePreProcessor;
import net.asteasolutions.cinusuidi.sluncho.bot.word2vecClassifierUtils.RelevantQuestionsSentenceIterator;
import net.asteasolutions.cinusuidi.sluncho.bot.word2vecClassifierUtils.Word2Vec4Phrases;
import net.asteasolutions.cinusuidi.sluncho.model.Question;
import net.asteasolutions.cinusuidi.sluncho.questionparser.QueryToken;

/**
 *
 * @author dimiter
 */
public class WordEmbeddingsRecognizer implements IQuestionRecognizer {

	private final static int VECTOR_LENGTH = 100;
	private final static double LEARNING_RATE = 0.025f;
	private final static int ITERATIONS = 1;
	private final static int EPOCHS = 20;// 20;
	private final static int WIN_SIZE = 5;
	private final static int MIN_WORD_FREQ = 2;
	private final static long RANDOM_SEED = 42;

	private Word2Vec4Phrases phraseComparator;

	private double threshold = 0.33;

	private int scoresTop = 30;
	private int scoresTopAdditionGroup = 30;
	private int scoresTopSubjectGroup = 30;
	private int scoresTopNNsAndJJsAndRBs = 30;
	private double scoresMultiplierAdditionGroup = 1.2;
	private double scoresMultiplierSubjectGroup = 1.2;
	private double scoresMultiplierNNsAndJJsAndRBs = 1.2;
	
	public List<Question> allQ = null;

	public WordEmbeddingsRecognizer() {
		try {
			train();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public WordEmbeddingsRecognizer(List<Question> allQ) {
		this();
		this.allQ = allQ;
	}

	public void train() throws Exception {
		this.phraseComparator = new Word2Vec4Phrases(WordEmbeddingsRecognizer.ITERATIONS,
				WordEmbeddingsRecognizer.EPOCHS, WordEmbeddingsRecognizer.VECTOR_LENGTH,
				WordEmbeddingsRecognizer.MIN_WORD_FREQ, WordEmbeddingsRecognizer.LEARNING_RATE,
				WordEmbeddingsRecognizer.WIN_SIZE, WordEmbeddingsRecognizer.RANDOM_SEED);
	}

	@Override
	public List<QuestionResult> classify(Query query) {
		// get different phrases to match
		String queryPhrase = query.originalText;
		String queryPhraseAdditionGroup = "";
		String queryPhraseSubjectGroup = "";
		String queryPhraseNNsAndJJsAndRBs = "";
		for (QueryToken token : query.additionGroup) {
			queryPhraseAdditionGroup += token.getOriginalText() + " ";
		}
		for (QueryToken token : query.subjectGroup) {
			queryPhraseSubjectGroup += token.getOriginalText() + " ";
		}
		for (QueryToken token : query.posTokens) {
			if (token.getAnnotations().get("category").startsWith("NN")
					|| token.getAnnotations().get("category").startsWith("JJ")
					|| token.getAnnotations().get("category").startsWith("RB")) {
				queryPhraseNNsAndJJsAndRBs += token.getOriginalText() + " ";
			}
		}

		// init queues to hold answers
		List<Pair<String, Double>> scores = new ArrayList<>();
		List<Pair<String, Double>> scoresAdditionGroup = new ArrayList<>();
		List<Pair<String, Double>> scoresSubjectGroup = new ArrayList<>();
		List<Pair<String, Double>> scoresNNsAndJJsAndRBs = new ArrayList<>();
		
		
		// iterate through database entries
		RelevantQuestionsSentenceIterator questionsIterator = new RelevantQuestionsSentenceIterator(allQ);
		questionsIterator.setPreProcessor(new QuestionSentencePreProcessor());
		queryPhrase = questionsIterator.getPreProcessor().preProcess(queryPhrase);
		
		while (questionsIterator.hasNext()) {
			String question = questionsIterator.nextSentence();
			String phrase = question;// .getContent();
			String gid = questionsIterator.currentLabel();
			double similarity = phraseComparator.cosineSimilarityPhraseVec(queryPhrase.split("  *"), phrase.split("  *"));
			if (similarity > this.threshold) {
				scores.add(new Pair<String, Double>(gid + " : " + question, similarity));
			}

			// use other metrics also
			similarity = phraseComparator.cosineSimilarityPhraseVec(queryPhraseAdditionGroup.split("  *"),
					phrase.split("  *"));
			if (similarity > this.threshold) {
				scoresAdditionGroup.add(new Pair<String, Double>(gid + " : " + question, similarity));
			}
			similarity = phraseComparator.cosineSimilarityPhraseVec(queryPhraseSubjectGroup.split("  *"),
					phrase.split("  *"));
			if (similarity > this.threshold) {
				scoresSubjectGroup.add(new Pair<String, Double>(gid + " : " + question, similarity));
			}
			similarity = phraseComparator.cosineSimilarityPhraseVec(queryPhraseNNsAndJJsAndRBs.split("  *"),
					phrase.split("  *"));
			if (similarity > this.threshold) {
				scoresNNsAndJJsAndRBs.add(new Pair<String, Double>(gid + " : " + question, similarity));
			}
		}

		
		// use results in other queues to update the main one
//		updateMetrics(scoresTopAdditionGroup, scoresMultiplierAdditionGroup, scoresAdditionGroup, scores);
//		updateMetrics(scoresTopSubjectGroup, scoresMultiplierSubjectGroup, scoresSubjectGroup, scores);
		updateMetrics(scoresTopNNsAndJJsAndRBs, scoresMultiplierNNsAndJJsAndRBs, scoresNNsAndJJsAndRBs, scores);

		return getBest(scores);
	}

	private void updateMetrics(int topN, double multiplier, 
			List<Pair<String, Double>> readList, List<Pair<String, Double>> writeList) {
		
		for (int i = 0; i < topN && i < readList.size(); i++) {
			Pair<String, Double> p = readList.get(i);
			for (Pair<String, Double> pair : writeList) {
				if (p.getFirst().equals(pair.getFirst())) {
					pair.setSecond(pair.getSecond() * multiplier);
					break;
				}
			}
		}
	}
	
	private List<QuestionResult> getBest(List<Pair<String, Double>> scores) {
		Map<String, Double> bestScoresGroupped = new HashMap<>();
		for (Pair<String, Double> pair : scores) {
			String[] docNameAndGroupId = pair.getFirst().split(" : ", 2);
			Double maxValue = (Double) Math.max(
					(double) bestScoresGroupped.getOrDefault(docNameAndGroupId[0], -1.0d),
					(double) pair.getSecond());
			bestScoresGroupped.put(docNameAndGroupId[0], maxValue);
		}
		
		List<QuestionResult> result = new ArrayList<>();
		for (String group : bestScoresGroupped.keySet()) {
			result.add(new QuestionResult(group, group, (float) (double) bestScoresGroupped.get(group)));
		}
		result.sort(new Comparator<QuestionResult>() {
			public int compare(QuestionResult qr1, QuestionResult qr2) {
				// we want the highest element (max. similar) at the top
				return ((Float) qr2.certainty()).compareTo((Float) qr1.certainty());
			}
		});
		return result;
	}
	
//	private List<QuestionResult> getBest(List<Pair<String, Double>> scores) {
//		scores.sort(new Comparator<Pair<String, Double>>() {
//			public int compare(Pair<String, Double> pr1, Pair<String, Double> pr2) {
//				// we want the highest element (max. similar) at the top
//				return pr2.getSecond().compareTo(pr1.getSecond());
//			}
//		});
//		List<QuestionResult> bestScores = new ArrayList<>();
//		for (int i = 0; i < scoresTop && i < scores.size(); i++) {
//			String[] docNameAndGroupId = scores.get(i).getFirst().split(" : ", 2);
//			QuestionResult qr = new QuestionResult(docNameAndGroupId[1], docNameAndGroupId[0], (float) (double) scores.get(i).getSecond());
//			bestScores.add(qr);
//		}
//		return bestScores;
//	}

	// public static void main(String[] args) throws Exception {
	// WordEmbeddingsRecognizer wer = new WordEmbeddingsRecognizer();
	// wer.train();
	// Query q = new Query();
	// QuestionResult qr = wer.classify(q);
	// System.out.println(qr.toString());
	// }

}
