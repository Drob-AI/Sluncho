package net.asteasolutions.cinusuidi.sluncho.bot.questionRecognizers;

import org.deeplearning4j.berkeley.Pair;
import org.deeplearning4j.models.embeddings.WeightLookupTable;
import org.deeplearning4j.models.embeddings.inmemory.InMemoryLookupTable;
import org.deeplearning4j.models.embeddings.learning.impl.elements.SkipGram;
import org.deeplearning4j.models.paragraphvectors.ParagraphVectors;
import org.deeplearning4j.models.word2vec.VocabWord;
import org.deeplearning4j.text.documentiterator.LabelledDocument;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.EndingPreProcessor;

import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.asteasolutions.cinusuidi.sluncho.bot.Query;
import net.asteasolutions.cinusuidi.sluncho.bot.doc2vecClassifierUtils.LabelSeeker;
import net.asteasolutions.cinusuidi.sluncho.bot.doc2vecClassifierUtils.MeansBuilder;
import net.asteasolutions.cinusuidi.sluncho.bot.doc2vecClassifierUtils.RelevantQuestionsIterator;
import net.asteasolutions.cinusuidi.sluncho.model.Question;
import net.asteasolutions.cinusuidi.sluncho.utils.AlgorithmHelpers;
import scala.util.parsing.combinator.testing.Str;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Map.Entry;

/**
 * This is basic example for documents classification done with DL4j
 * ParagraphVectors. The overall idea is to use ParagraphVectors in the same way
 * we use LDA: topic space modelling.
 *
 * In this example we assume we have few labeled categories that we can use for
 * training, and few unlabeled documents. And our goal is to determine, which
 * category these unlabeled documents fall into
 *
 *
 * Please note: This example could be improved by using learning cascade for
 * higher accuracy, but that's beyond basic example paradigm.
 *
 * @author raver119@gmail.com
 */
public class Doc2VecGroupClassifier {

	private static final Logger log = LoggerFactory.getLogger(ParagraphVectorsClassifierExample.class);
	public static ParagraphVectors paragraphVectors;
	public static List<ParagraphVectors> bagOfClassifiers;
	
	public static TokenizerFactory tokenizer;
	public static RelevantQuestionsIterator iterator;

	private final static double LEARNING_RATE = 0.025;
	private final static double MIN_LEARNING_RATE = 0.01;
	private final static int BATCH_SIZE = 100;
	// epohce should be 40
	private final static int EPOCHS = 40;
	private final static boolean WORDS_VECTORS = true;

	public static void train() {
		iterator = new RelevantQuestionsIterator();
		tokenizer = new DefaultTokenizerFactory();
		tokenizer.setTokenPreProcessor(new EndingPreProcessor());

		AlgorithmHelpers algoHelper = new AlgorithmHelpers();
		List<String> stopWords = algoHelper.getListOfStopWords();

		// ParagraphVectors training configuration
		paragraphVectors = new ParagraphVectors.Builder().learningRate(LEARNING_RATE).iterations(80).seed(80)
				.stopWords(stopWords).minLearningRate(MIN_LEARNING_RATE).batchSize(BATCH_SIZE).epochs(EPOCHS)
				.iterate(iterator).trainWordVectors(WORDS_VECTORS).tokenizerFactory(tokenizer).build();

		// Start model training
		paragraphVectors.fit();
	}

	public static void trainWithQuestions(final List<Question> questions) {
		bagOfClassifiers = new ArrayList<>();
		bagOfClassifiers = Collections.synchronizedList(new ArrayList<ParagraphVectors>());
		
		iterator = new RelevantQuestionsIterator(questions);

		tokenizer = new DefaultTokenizerFactory();
//		tokenizer.setTokenPreProcessor(new EndingPreProcessor());
		tokenizer.setTokenPreProcessor(new CommonPreprocessor());

		// ParagraphVectors training configuration
		// seed 10 -> 0.4
		Thread t1 = new Thread(new Runnable() {
			
			@Override
			public void run() {
				AlgorithmHelpers algoHelper = new AlgorithmHelpers();
				List<String> stopWords = algoHelper.getListOfStopWords();
				RelevantQuestionsIterator iter = new RelevantQuestionsIterator(questions);
				
				ParagraphVectors pVectors = new ParagraphVectors.Builder()
						.learningRate(LEARNING_RATE)
						.minLearningRate(MIN_LEARNING_RATE)
						.seed(10)
						.stopWords(stopWords)
						.batchSize(BATCH_SIZE)
						.epochs(EPOCHS)
						.iterate(iter)
						.trainWordVectors(WORDS_VECTORS)
						.tokenizerFactory(tokenizer).build();

				// Start model training
				pVectors.fit();
				bagOfClassifiers.add(pVectors);
			}
		});
		
		Thread t2 = new Thread(new Runnable() {
			
			@Override
			public void run() {
				AlgorithmHelpers algoHelper = new AlgorithmHelpers();
				List<String> stopWords = algoHelper.getListOfStopWords();
				RelevantQuestionsIterator iter = new RelevantQuestionsIterator(questions);
				
				//iterations 10 no seed -> 72
				ParagraphVectors pVectors = new ParagraphVectors.Builder()
						.learningRate(0.025)
		                .minLearningRate(0.001)
		                .iterations(10)
		                .batchSize(1000)
		                .epochs(20)
		                .stopWords(stopWords)
		                .iterate(iter)
		                .trainWordVectors(true)
		                .tokenizerFactory(tokenizer)
		                .build();

				// Start model training
				pVectors.fit();
				bagOfClassifiers.add(pVectors);
			}
		});

		Thread t3 = new Thread(new Runnable() {
			
			@Override
			public void run() {
				AlgorithmHelpers algoHelper = new AlgorithmHelpers();
				List<String> stopWords = algoHelper.getListOfStopWords();
				RelevantQuestionsIterator iter = new RelevantQuestionsIterator(questions);
				
				ParagraphVectors pVectors = new ParagraphVectors.Builder()
						.learningRate(0.05)
		                .minLearningRate(0.01)
		                .iterations(50)
		                .seed(20)
		                .batchSize(100)
		                .epochs(20)
		                .stopWords(stopWords)
		                .iterate(iter)
		                .trainWordVectors(true)
		                .tokenizerFactory(tokenizer)
		                .build();

				// Start model training
				pVectors.fit();
				bagOfClassifiers.add(pVectors);
			}
		});
		Thread t4 = new Thread(new Runnable() {
			
			@Override
			public void run() {
				AlgorithmHelpers algoHelper = new AlgorithmHelpers();
				List<String> stopWords = algoHelper.getListOfStopWords();
				RelevantQuestionsIterator iter = new RelevantQuestionsIterator(questions);
				
				ParagraphVectors pVectors = new ParagraphVectors.Builder()
						.learningRate(0.035)
		                .minLearningRate(0.001)
		                .iterations(100)
		                .seed(50)
		                .batchSize(200)
		                .epochs(10)
		                .stopWords(stopWords)
		                .iterate(iter)
		                .trainWordVectors(true)
		                .tokenizerFactory(tokenizer)
		                .build();

				// Start model training
				pVectors.fit();
				bagOfClassifiers.add(pVectors);
			}
		});
		
		Thread t5 = new Thread(new Runnable() {
			
			@Override
			public void run() {
				AlgorithmHelpers algoHelper = new AlgorithmHelpers();
				List<String> stopWords = algoHelper.getListOfStopWords();
				RelevantQuestionsIterator iter = new RelevantQuestionsIterator(questions);
				
				ParagraphVectors pVectors = new ParagraphVectors.Builder()
						.learningRate(0.055)
		                .minLearningRate(0.01)
		                .seed(40)
		                .batchSize(300)
		                .epochs(40)
		                .stopWords(stopWords)
		                .iterate(iter)
		                .trainWordVectors(true)
		                .tokenizerFactory(tokenizer)
		                .build();

				// Start model training
				pVectors.fit();
				bagOfClassifiers.add(pVectors);
			}
		});
		
		Thread t6 = new Thread(new Runnable() {
			
			@Override
			public void run() {
				AlgorithmHelpers algoHelper = new AlgorithmHelpers();
				List<String> stopWords = algoHelper.getListOfStopWords();
				RelevantQuestionsIterator iter = new RelevantQuestionsIterator(questions);
				
				ParagraphVectors pVectors = new ParagraphVectors.Builder()
						.learningRate(0.1)
						.minLearningRate(0.05)
						.trainElementsRepresentation(true)
						.stopWords(stopWords)
						.batchSize(30) // 10 
						.epochs(50) // 20
						.iterate(iter)
						.trainWordVectors(true)
						.workers(3)
						.tokenizerFactory(tokenizer).build();

				// Start model training
				pVectors.fit();
				bagOfClassifiers.add(pVectors);
			}
		});
		
		Thread t7 = new Thread(new Runnable() {
			
			@Override
			public void run() {
				AlgorithmHelpers algoHelper = new AlgorithmHelpers();
				List<String> stopWords = algoHelper.getListOfStopWords();
				RelevantQuestionsIterator iter = new RelevantQuestionsIterator(questions);
				
				ParagraphVectors pVectors = new ParagraphVectors.Builder()
						.learningRate(0.1)
						.minLearningRate(0.05)
						.trainElementsRepresentation(true)
						.stopWords(stopWords)
						.batchSize(10)
						.epochs(20)
						.iterate(iter)
						.trainWordVectors(true)
						.workers(3)
						.tokenizerFactory(tokenizer).build();

				// Start model training
				pVectors.fit();
				bagOfClassifiers.add(pVectors);
			}
		});
		
		Thread t8 = new Thread(new Runnable() {
			
			@Override
			public void run() {
				AlgorithmHelpers algoHelper = new AlgorithmHelpers();
				List<String> stopWords = algoHelper.getListOfStopWords();
				RelevantQuestionsIterator iter = new RelevantQuestionsIterator(questions);
				
				ParagraphVectors pVectors = new ParagraphVectors.Builder()
						.learningRate(0.03)
						.minLearningRate(0.025)
						.stopWords(stopWords)
						.layerSize(100)
						.batchSize(200) 
						.epochs(30)
						.iterate(iter)
						.trainWordVectors(true)
						.workers(3)
						.windowSize(7)
						.tokenizerFactory(tokenizer).build();

				// Start model training
				pVectors.fit();
				bagOfClassifiers.add(pVectors);
			}
		});
	
		// 1 2 5 6   -> 0.9 (top 5) 0.7 ( top 1)
		// 1 2 5 6 7 -> 0.88 (top 5) 0.74 ( top 1) + 8 = 76
		// 1 5 6 7 ->  0.9 (top 5) 0.72 ( top 1)
		t1.start();
//		t2.start();
//		t3.start();
//		t4.start();
//		t5.start();
		t6.start();
//		t7.start();
		t8.start();
		try {
			t1.join();
			t2.join();
//			t3.join();
//			t4.join();
			t5.join();
			t6.join();
			t7.join();
			t8.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void reset() {
		paragraphVectors = null;
		tokenizer = null;
		iterator = null;
	}

	public Pair<String, Double> classifyToGroup(Query query) {
		MeansBuilder meansBuilder = new MeansBuilder((InMemoryLookupTable<VocabWord>) paragraphVectors.getLookupTable(),
				tokenizer);
		LabelSeeker seeker = new LabelSeeker(iterator.getLabelsSource().getLabels(),
				(InMemoryLookupTable<VocabWord>) paragraphVectors.getLookupTable());

		LabelledDocument queryDoc = new LabelledDocument();
		queryDoc.setContent(query.originalText);
		INDArray documentAsCentroid = meansBuilder.documentAsVector(queryDoc);
		return seeker.getMaxScore(documentAsCentroid);
	}

	public Pair<String, Double> classifyToGroup(Question query) {
		MeansBuilder meansBuilder = new MeansBuilder((InMemoryLookupTable<VocabWord>) paragraphVectors.getLookupTable(),
				tokenizer);
		LabelSeeker seeker = new LabelSeeker(iterator.getLabelsSource().getLabels(),
				(InMemoryLookupTable<VocabWord>) paragraphVectors.getLookupTable());

		LabelledDocument queryDoc = new LabelledDocument();
		queryDoc.setContent(query.getBody());
		INDArray documentAsCentroid = meansBuilder.documentAsVector(queryDoc);
		return seeker.getMaxScore(documentAsCentroid);
	}

	public List<Pair<String, Double>> classifyToTopNGroups(Question query, Integer n) {
		MeansBuilder meansBuilder = new MeansBuilder((InMemoryLookupTable<VocabWord>) paragraphVectors.getLookupTable(),
				tokenizer);
		LabelSeeker seeker = new LabelSeeker(iterator.getLabelsSource().getLabels(),
				(InMemoryLookupTable<VocabWord>) paragraphVectors.getLookupTable());

		LabelledDocument queryDoc = new LabelledDocument();
		queryDoc.setContent(query.getBody());
		INDArray documentAsCentroid = meansBuilder.documentAsVector(queryDoc);
		return seeker.getMaxNScores(documentAsCentroid, n);
	}

	private List<Pair<String, Double>> getBest(Map<String, Double> summary, Integer n) {
		Comparator<Entry<String, Double>> compare = new Comparator<Entry<String, Double>>() {
			public int compare(Entry<String, Double> first, Entry<String, Double> second) {
				return first.getValue() > second.getValue() ? -1 : 1;
			}
		};
		
		List<Pair<String, Double>> result = new ArrayList<>();
		PriorityQueue<Entry<String, Double>> heap = new PriorityQueue<Entry<String, Double>>(n, compare);
		
		for (Entry<String, Double> entry : summary.entrySet()) {
			heap.add(entry);
		}
		
		while(!heap.isEmpty() && n > 0) {
			Entry<String, Double> forParsing = heap.remove();
			result.add(new Pair<String, Double>(forParsing.getKey(), forParsing.getValue()));
			n--;
		}
		return result;
	}

	public List<Pair<String, Double>> bagginClassifyToTopNGroups(Question query, Integer n) {
		
		Map<String, Double> summaryResults = new HashMap<>();
		System.out.println("Sizeee" + bagOfClassifiers.size());
		
		for (ParagraphVectors claasifier : bagOfClassifiers) {
			MeansBuilder meansBuilder = new MeansBuilder((InMemoryLookupTable<VocabWord>) claasifier.getLookupTable(), tokenizer);
			LabelSeeker seeker = new LabelSeeker(iterator.getLabelsSource().getLabels(),
					(InMemoryLookupTable<VocabWord>) claasifier.getLookupTable());

			LabelledDocument queryDoc = new LabelledDocument();
			queryDoc.setContent(query.getBody());
			INDArray documentAsCentroid = meansBuilder.documentAsVector(queryDoc);

			Map<String, Double> results = seeker.getScoresAsMap(documentAsCentroid);

			for (Entry<String, Double> entry : results.entrySet()) {
				String label = entry.getKey();
				Double value = entry.getValue();
				
				Double oldValue = summaryResults.getOrDefault(label, 0.0);
				summaryResults.put(label, oldValue + value);
			}
		}

		return getBest(summaryResults, n);
	}

	// public static void main(String[] args) throws Exception {
	// java.util.Iterator<Object> iter =
	// EnglishAnalyzer.getDefaultStopSet().iterator();
	// List<String> stopWords = new ArrayList<String>();
	// while(iter.hasNext()) {
	// char[] stopWord = (char[]) iter.next();
	// stopWords.add(new String (stopWord));
	// }
	// }
}
