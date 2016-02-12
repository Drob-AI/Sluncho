package net.asteasolutions.cinusuidi.sluncho.bot.word2vecClassifierUtils;

import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.ops.transforms.Transforms;

public class Word2Vec4Phrases {
	
	public Word2Vec vec;
	
	public Word2Vec4Phrases(String txtModelPath) {
		this.vec = WordVectorSerializer.loadFullModel(txtModelPath);
	}
	
	public Word2Vec4Phrases(Word2Vec vec) {
		this.vec = vec;
	}
	
	/*
	public Word2Vec4Phrases(String filePath, 
			int iterations, int epochs, 
			int vectorLength, int minWordFreq, 
			int learningRate, int windowSize, 
			long randomSeed) throws Exception {
		
		SentenceIterator iter = new BasicLineIterator(filePath);
        TokenizerFactory t = new DefaultTokenizerFactory();
        t.setTokenPreProcessor(new CommonPreprocessor());
        InMemoryLookupCache cache = new InMemoryLookupCache();
        WeightLookupTable<VocabWord> table = new InMemoryLookupTable.Builder<VocabWord>()
                .vectorLength(vectorLength)
                .useAdaGrad(false)
                .cache(cache)
                .lr(learningRate).build();
        this.vec = new Word2Vec.Builder()
                .minWordFrequency(minWordFreq)
                .iterations(iterations)
                .epochs(epochs)
                .layerSize(vectorLength)
                .seed(randomSeed)
                .iterate(iter)
                .tokenizerFactory(t)
                .lookupTable(table)
                .vocabCache(cache)
                .build();
        vec.fit();
	}
	*/
	
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
