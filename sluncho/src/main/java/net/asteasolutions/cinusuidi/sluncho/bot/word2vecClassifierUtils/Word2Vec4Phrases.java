package net.asteasolutions.cinusuidi.sluncho.bot.word2vecClassifierUtils;

import java.awt.image.LookupTable;

import org.deeplearning4j.models.embeddings.WeightLookupTable;
import org.deeplearning4j.models.embeddings.inmemory.InMemoryLookupTable;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.VocabWord;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.models.word2vec.wordstore.inmemory.InMemoryLookupCache;
import org.deeplearning4j.text.sentenceiterator.BasicLineIterator;
import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.ops.transforms.Transforms;

public class Word2Vec4Phrases {
	
	private int VECTOR_LENGTH;
    private double LEARNING_RATE;
    private int ITERATIONS;
    private int EPOCHS;
    private int WIN_SIZE;
    private int MIN_WORD_FREQ;
    private long RANDOM_SEED;
	
	private SentenceIterator iter;
	private TokenizerFactory t;
	private InMemoryLookupCache cache;
	private WeightLookupTable<VocabWord> table;
	
	public Word2Vec vec;
	
	public Word2Vec4Phrases(String txtModelPath) {
		this.vec = WordVectorSerializer.loadFullModel(txtModelPath);
	}
	
	public Word2Vec4Phrases(Word2Vec vec) {
		this.vec = vec;
	}
	
	public Word2Vec4Phrases(int iterations, int epochs, 
			int vectorLength, int minWordFreq, 
			double learningRate, int windowSize, 
			long randomSeed) throws Exception {
		
		this.VECTOR_LENGTH = vectorLength;
		this.LEARNING_RATE = learningRate;
		this.ITERATIONS = iterations;
		this.EPOCHS = epochs;
		this.WIN_SIZE = windowSize;
		this.MIN_WORD_FREQ = minWordFreq;
		this.RANDOM_SEED = randomSeed;
		
		String filePath = "/home/dimiter/Downloads/SemEval2016-Task3-CQA-QL-train-part2-with-multiline.txt";
        this.iter = new BasicLineIterator(filePath);
		//this.iter = new RelevantQuestionsSentenceIterator();
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
                .seed(RANDOM_SEED)
                .windowSize(WIN_SIZE)
                .iterate(iter)
                .tokenizerFactory(t)
                .lookupTable(table)
                .vocabCache(cache)
                .build();

        vec.fit();
	}
	
	public double cosineSimilarityPhraseVec(String[] phrase1Words, String[] phrase2Words) {
		INDArray phrase1Vector = calculatePhraseVecCentroid(phrase1Words);
		INDArray phrase2Vector = calculatePhraseVecCentroid(phrase2Words);
		return Transforms.cosineSim(phrase1Vector, phrase2Vector);
	}
	
	// phrase vector is centroid of all word vectors
	public INDArray calculatePhraseVecCentroid(String[] phrase) {
		INDArray centroid = Nd4j.create(vec.lookupTable().getVectorLength());
		for (String word : phrase) {
			INDArray wordVec = vec.lookupTable().vector(word).dup();
			centroid.addi(wordVec); // vector addition, in-place
		}
		Transforms.unitVec(centroid); //centroid.divi(centroid.norm2(1));
		return centroid;
	}

	// phrase vector is mean of all word vectors multiplied by their tf-idf score
	public INDArray calculatePhraseVecTfIdf(String[] phrase, double[] tfIdfScores) {
		INDArray linCombo = Nd4j.create(vec.lookupTable().getVectorLength());
		int i = 0;
		for (String word : phrase) {
			INDArray wordVec = vec.lookupTable().vector(word).dup()
					.muli(tfIdfScores[i++]); // multiply by tf-idf score, in-place
			linCombo.addi(wordVec); // vector addition, in-place
		}
		Transforms.unitVec(linCombo); //linCombo.divi(linCombo.norm2(1));
		return linCombo;
	}
	
	public double cosineSimilarityPhraseVecTfIdf(
			String[] phrase1Words, String[] phrase2Words, 
			double[] phrase1TfIdfScore, double[] phrase2TfIdfScore) {
		INDArray phrase1Vector = calculatePhraseVecTfIdf(phrase1Words, phrase1TfIdfScore);
		INDArray phrase2Vector = calculatePhraseVecTfIdf(phrase2Words, phrase2TfIdfScore);
		return Transforms.cosineSim(phrase1Vector, phrase2Vector);
	}
}
