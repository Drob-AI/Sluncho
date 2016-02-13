package net.asteasolutions.cinusuidi.sluncho.bot.questionRecognizers;

import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

import org.deeplearning4j.berkeley.Pair;

import net.asteasolutions.cinusuidi.sluncho.bot.Query;
import net.asteasolutions.cinusuidi.sluncho.bot.QuestionResult;
import net.asteasolutions.cinusuidi.sluncho.bot.word2vecClassifierUtils.RelevantQuestionsSentenceIterator;
import net.asteasolutions.cinusuidi.sluncho.bot.word2vecClassifierUtils.Word2Vec4Phrases;

/**
*
* @author dimiter
*/
public class WordEmbeddingsRecognizer implements IQuestionRecognizer {

	private final static int VECTOR_LENGTH = 100;
    private final static double LEARNING_RATE = 0.025f;
    private final static int ITERATIONS = 1;
    private final static int EPOCHS = 20;//20;
    private final static int WIN_SIZE = 5;
    private final static int MIN_WORD_FREQ = 5;
    private final static long RANDOM_SEED = 42;

    private Word2Vec4Phrases phraseComparator;
	
	public void train() throws Exception {
    	this.phraseComparator = new Word2Vec4Phrases(this.ITERATIONS, this.EPOCHS, 
    			this.VECTOR_LENGTH, this.MIN_WORD_FREQ, 
    			this.LEARNING_RATE, this.WIN_SIZE, 
    			this.RANDOM_SEED);
    }
	
	@Override
	public List<QuestionResult> classify(Query query) {
		String queryPhrase = query.originalText;
		
		RelevantQuestionsSentenceIterator questionsIterator = new RelevantQuestionsSentenceIterator();
		PriorityQueue<Pair<String, Double>> scores = new PriorityQueue<>(1, new Comparator<Pair<String, Double>>() {
			public int compare(Pair<String, Double> pr1, Pair<String, Double> pr2) {
				// we want the highest element (max. similar) at the top
		        return pr2.getSecond().compareTo(pr1.getSecond());
		    }
		});
		while (questionsIterator.hasNext()) {
            String question = questionsIterator.nextSentence();
            String phrase = question;//.getContent();
            double similarity = phraseComparator.cosineSimilarityPhraseVec(
            		queryPhrase.split(" "), phrase.split(" "));
            scores.add(new Pair<String, Double>(question, similarity));
        }
		
		Pair<String, Double> bestMatch = scores.peek();
                //SORRY SORRY SORRY SORRY
//		return new QuestionResult(bestMatch.getFirst(), (float) (double) bestMatch.getSecond());
                return null;
	}
	
//	public static void main(String[] args) throws Exception {
//		WordEmbeddingsRecognizer wer = new WordEmbeddingsRecognizer();
//		wer.train();
//		Query q = new Query();
//		QuestionResult qr = wer.classify(q);
//		System.out.println(qr.toString());
//	}

}
