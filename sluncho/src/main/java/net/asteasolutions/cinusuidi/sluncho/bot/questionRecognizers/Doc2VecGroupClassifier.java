package net.asteasolutions.cinusuidi.sluncho.bot.questionRecognizers;

import org.canova.api.util.ClassPathResource;
import org.deeplearning4j.berkeley.Pair;
import org.deeplearning4j.models.embeddings.inmemory.InMemoryLookupTable;
import org.deeplearning4j.models.embeddings.learning.impl.elements.SkipGram;
import org.deeplearning4j.models.paragraphvectors.ParagraphVectors;
import org.deeplearning4j.models.word2vec.VocabWord;
import org.deeplearning4j.text.documentiterator.LabelAwareIterator;
import org.deeplearning4j.text.documentiterator.LabelledDocument;
import org.deeplearning4j.text.documentiterator.SimpleLabelAwareIterator;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.asteasolutions.cinusuidi.sluncho.bot.Query;
import net.asteasolutions.cinusuidi.sluncho.bot.doc2vecClassifierUtils.LabelSeeker;
import net.asteasolutions.cinusuidi.sluncho.bot.doc2vecClassifierUtils.MeansBuilder;
import net.asteasolutions.cinusuidi.sluncho.bot.doc2vecClassifierUtils.RelevantQuestionsIterator;

import java.util.List;

/**
 * This is basic example for documents classification done with DL4j ParagraphVectors.
 * The overall idea is to use ParagraphVectors in the same way we use LDA: topic space modelling.
 *
 * In this example we assume we have few labeled categories that we can use for training, and few unlabeled documents. And our goal is to determine, which category these unlabeled documents fall into
 *
 *
 * Please note: This example could be improved by using learning cascade for higher accuracy, but that's beyond basic example paradigm.
 *
 * @author raver119@gmail.com
 */
public class Doc2VecGroupClassifier {

    private static final Logger log = LoggerFactory.getLogger(ParagraphVectorsClassifierExample.class);
    public static ParagraphVectors paragraphVectors;
    public static TokenizerFactory tokenizer;
    public static RelevantQuestionsIterator iterator;
    
    public static void  train() {
    	iterator = new RelevantQuestionsIterator();
    	
        tokenizer = new DefaultTokenizerFactory();
        tokenizer.setTokenPreProcessor(new CommonPreprocessor());

        // ParagraphVectors training configuration
        paragraphVectors = new ParagraphVectors.Builder()
                .learningRate(0.025)
                .minLearningRate(0.001)
                .batchSize(1000)
                .epochs(20)
                .iterate(iterator)
                .trainWordVectors(true)
                .tokenizerFactory(tokenizer)
                .build();

        // Start model training
        paragraphVectors.fit();
    }
    
    public Pair<String, Double> classifyToGroup(Query query) {
        MeansBuilder meansBuilder = new MeansBuilder((InMemoryLookupTable<VocabWord>) paragraphVectors.getLookupTable(), tokenizer);
        LabelSeeker seeker = new LabelSeeker(iterator.getLabelsSource().getLabels(), (InMemoryLookupTable<VocabWord>)  paragraphVectors.getLookupTable());
        
        LabelledDocument queryDoc = new LabelledDocument();
        queryDoc.setContent(query.originalText);
        INDArray documentAsCentroid = meansBuilder.documentAsVector(queryDoc);
		return seeker.getMaxScore(documentAsCentroid);
    }
    
//    public static void main(String[] args) throws Exception {
//        /*
//         At this point we assume that we have model built and we can check, which categories our unlabeled document falls into
//         So we'll start loading our unlabeled documents and checking them
//        */
//      
//        RelevantQuestionsIterator unlabeledIterator = new RelevantQuestionsIterator();
//        MeansBuilder meansBuilder = new MeansBuilder((InMemoryLookupTable<VocabWord>) paragraphVectors.getLookupTable(), tokenizer);
//        LabelSeeker seeker = new LabelSeeker(unlabeledIterator.getLabelsSource().getLabels(), (InMemoryLookupTable<VocabWord>)  paragraphVectors.getLookupTable());
//
//        while (unlabeledIterator.hasNextDocument()) {
//            LabelledDocument document = unlabeledIterator.nextDocument();
//
//            INDArray documentAsCentroid = meansBuilder.documentAsVector(document);
//            List<Pair<String, Double>> scores = seeker.getScores(documentAsCentroid);
//
//            log.info("Document '" + document.getLabel() + "' falls into the following categories: ");
//            for (Pair<String, Double> score: scores) {
//                log.info("        " + score.getFirst() + ": " + score.getSecond());
//            }
//
//        }
//    }
}
