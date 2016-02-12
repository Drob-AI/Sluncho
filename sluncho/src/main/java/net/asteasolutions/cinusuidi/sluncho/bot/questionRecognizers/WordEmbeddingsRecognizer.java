package net.asteasolutions.cinusuidi.sluncho.bot.questionRecognizers;

import java.io.FileNotFoundException;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

import org.apache.commons.collections.OrderedMap;
import org.deeplearning4j.berkeley.Pair;
import org.deeplearning4j.models.embeddings.WeightLookupTable;
import org.deeplearning4j.models.embeddings.inmemory.InMemoryLookupTable;
import org.deeplearning4j.models.word2vec.VocabWord;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.models.word2vec.wordstore.inmemory.InMemoryLookupCache;
import org.deeplearning4j.text.documentiterator.LabelledDocument;
import org.deeplearning4j.text.sentenceiterator.BasicLineIterator;
import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.nd4j.linalg.api.ndarray.INDArray;

import net.asteasolutions.cinusuidi.sluncho.bot.Query;
import net.asteasolutions.cinusuidi.sluncho.bot.QuestionResult;
import net.asteasolutions.cinusuidi.sluncho.bot.doc2vecClassifierUtils.RelevantQuestionsIterator;
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
	
	private SentenceIterator iter;
	private TokenizerFactory t;
	private InMemoryLookupCache cache;
	private WeightLookupTable<VocabWord> table;
	
	private Word2Vec vec;
	
	public void train() throws FileNotFoundException {
		// TODO: change to sentence iterator
		String filePath = "/home/dimiter/Downloads/SemEval2016-Task3-CQA-QL-train-part2-with-multiline.txt";
        this.iter = new BasicLineIterator(filePath);
        this.t = new DefaultTokenizerFactory();
        t.setTokenPreProcessor(new CommonPreprocessor());

        // manual creation of VocabCache and WeightLookupTable usually isn't necessary
        // but in this case we'll need them
        this.cache = new InMemoryLookupCache();
        this.table = new InMemoryLookupTable.Builder<VocabWord>()
                .vectorLength(VECTOR_LENGTH)
                .useAdaGrad(false)
                .cache(cache)
                .lr(LEARNING_RATE).build();
        
        this.vec = new Word2Vec.Builder()
                .minWordFrequency(MIN_WORD_FREQ)
                .iterations(ITERATIONS)
                .epochs(EPOCHS)
                .layerSize(VECTOR_LENGTH)
                .seed(42)
                .windowSize(WIN_SIZE)
                .iterate(iter)
                .tokenizerFactory(t)
                .lookupTable(table)
                .vocabCache(cache)
                .build();

        vec.fit();
	}
	
	@Override
	public QuestionResult classify(Query query) {
		try {
			this.train();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		// TODO: change to sentence iterator
		String filePath = "/home/dimiter/Downloads/SemEval2016-Task3-CQA-QL-train-part2-with-multiline.txt";
		SentenceIterator questionsIterator = null;
		try {
			questionsIterator = new BasicLineIterator(filePath);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		String queryPhrase = query.toString();
		Word2Vec4Phrases phraseComparator = new Word2Vec4Phrases(vec);
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
		return new QuestionResult(bestMatch.getFirst(), (float) (double) bestMatch.getSecond());
	}
	
//	public static void main(String[] args) throws FileNotFoundException {
//		WordEmbeddingsRecognizer wer = new WordEmbeddingsRecognizer();
//		wer.train();
//		Query q = new Query();
//		QuestionResult qr = wer.classify(q);
//		System.out.println(qr.toString());
//	}

}
