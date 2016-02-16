/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.asteasolutions.cinusuidi.sluncho.bot.questionRecognizers;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import net.asteasolutions.cinusuidi.sluncho.App;
import net.asteasolutions.cinusuidi.sluncho.bot.CompositeQuery;
import net.asteasolutions.cinusuidi.sluncho.bot.Query;
import net.asteasolutions.cinusuidi.sluncho.bot.QueryAnswerer;
import net.asteasolutions.cinusuidi.sluncho.bot.QuestionResult;
import net.asteasolutions.cinusuidi.sluncho.data.QuestionRepository;
import net.asteasolutions.cinusuidi.sluncho.model.Question;
import net.asteasolutions.cinusuidi.sluncho.questionparser.QueryToken;
import net.asteasolutions.cinusuidi.sluncho.questionparser.exception.QuestionParserException;
import net.asteasolutions.cinusuidi.sluncho.utils.AlgorithmHelpers;
import org.apache.commons.collections.Bag;
import org.apache.commons.collections.bag.HashBag;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.ops.transforms.Transforms;
import org.tartarus.snowball.ext.PorterStemmer;

/**
 *
 * @author marmot
 */
public class BagOfWordsClassifier implements IQuestionRecognizer {

    private final Map<String, Bag> trainingBag;

    public BagOfWordsClassifier() {
        trainingBag = getBagOfWords(QuestionRepository.Instance().oneOutRandomTrainingSet);
    }

    public void runBagOfWordsRandomTest(Integer topNResults) {
        Integer success = 0;
        List<String> stopWords = new AlgorithmHelpers().getListOfDomainStopWords();
        //Map<String, Bag> trainingBag = getBagOfWords(stopWords, QuestionRepository.Instance().oneOutRandomTrainingSet);
        //Map<String, Bag> testingBag = getBagOfWords(stopWords, QuestionRepository.Instance().oneOutRandomTestingSet);
        //success = calculate(trainingBag, testingBag);

        BigDecimal all = new BigDecimal(QuestionRepository.Instance().oneOutRandomTestingSet.size());
        BigDecimal precision = new BigDecimal(success).divide(all);
        System.out.println(precision.toString());

    }

    private Map<String, Bag> getBagOfWords(List<Question> allQuestions) {
        List<String> stopWords = new AlgorithmHelpers().getListOfDomainStopWords();
        Map<String, Bag> trainingSetBag = new HashMap<>();
        for (Question question : allQuestions) {
            Query query = null;
            try {
                query = App.questionParser.parse(question.getBody());
                List<QueryToken> allTokens = query.posTokens;
                List<String> allWordsInQuestion = new ArrayList<>();
                for (QueryToken queryToken : allTokens) {
                    allWordsInQuestion.add(queryToken.getOriginalText());
                }
                allWordsInQuestion.removeAll(stopWords);
                if (trainingSetBag.containsKey(question.getGroupId())) {
                    Bag containingBag = trainingSetBag.get(question.getGroupId());
                    for (String text : allWordsInQuestion) {
                        containingBag.add(stemTerm(text.toLowerCase()));
                    }

                } else {
                    Bag bagOfWords = new HashBag();
                    for (String text : allWordsInQuestion) {
                        bagOfWords.add(stemTerm(text.toLowerCase()));
                    }
                    trainingSetBag.put(question.getGroupId(), bagOfWords);
                }
            } catch (QuestionParserException e) {
                e.printStackTrace();
            }
        }
        return trainingSetBag;
    }

    private int calculate(Map<String, Bag> trainingBagOfWords, Map<String, Bag> testingBagOfWords) {

        int successfullyPredicted = 0;
        for (Map.Entry<String, Bag> testingEntry : testingBagOfWords.entrySet()) {
            String testingKey = testingEntry.getKey();
            Bag testingBag = testingEntry.getValue();
            List<String> allTestingWords = new ArrayList<>(testingBag.uniqueSet());
            String predictedGrroupId = "";
            double bestCosineSimilarity = 0;
            for (Map.Entry<String, Bag> trainingEntry : trainingBagOfWords.entrySet()) {
                List<Integer> trainingVector = new LinkedList<>();
                List<Integer> testingVector = new LinkedList<>();

                String trainingKey = trainingEntry.getKey();
                Bag trainingBag = trainingEntry.getValue();

                List<String> allTrainingWords = new ArrayList<>(trainingBag.uniqueSet());

                if (allTestingWords.size() > allTrainingWords.size()) {
                    for (String word : allTestingWords) {
                        if (allTrainingWords.contains(word)) {
                            trainingVector.add(trainingBag.getCount(word));
                            testingVector.add(testingBag.getCount(word));
                            allTrainingWords.remove(word);
                        } else {
                            testingVector.add(testingBag.getCount(word));
                            trainingVector.add(0);
                        }
                    }
                    for (String word : allTrainingWords) {
                        testingVector.add(0);
                        trainingVector.add(trainingBag.getCount(word));
                    }
                } else if (allTestingWords.size() <= allTrainingWords.size()) {
                    for (String word : allTrainingWords) {
                        if (allTestingWords.contains(word)) {
                            trainingVector.add(trainingBag.getCount(word));
                            testingVector.add(testingBag.getCount(word));
                            allTestingWords.remove(word);
                        } else {
                            testingVector.add(0);
                            trainingVector.add(trainingBag.getCount(word));
                        }

                    }
                    for (String word : allTestingWords) {
                        testingVector.add(testingBag.getCount(word));
                        trainingVector.add(0);
                    }
                }

                double[] training = new double[trainingVector.size()];
                double[] testing = new double[testingVector.size()];
                for (int i = 0; i < testingVector.size(); i++) {
                    testing[i] = (double) testingVector.get(i);
                }
                for (int i = 0; i < trainingVector.size(); i++) {
                    training[i] = (double) trainingVector.get(i);
                }

                INDArray nd = Nd4j.create(training);
                INDArray nd2 = Nd4j.create(testing);
                double similarity = Transforms.cosineSim(nd, nd2);
                if (similarity > bestCosineSimilarity) {
                    bestCosineSimilarity = similarity;
                    predictedGrroupId = trainingKey;
                }
            }
            System.out.println("PREDICTED GROUP: " + predictedGrroupId + " !!!!!!!!!!!");
            if (testingKey.equals(predictedGrroupId)) {
                System.out.println("WAS RIGHT");
                successfullyPredicted++;
            } else {
                System.out.println("WAS NOT RIGHT");
            }
        }
        return successfullyPredicted;
    }

    private String stemTerm(String term) {
        PorterStemmer stem = new PorterStemmer();
        stem.setCurrent(term);
        stem.stem();
        String result = stem.getCurrent();
        return result;
    }

    @Override
    public List<QuestionResult> classify(CompositeQuery query) {
        List<QuestionResult> allResults = new ArrayList<>();

        List<String> stopWords = new AlgorithmHelpers().getListOfDomainStopWords();
        Bag testingBag = new HashBag();

        List<QueryToken> allTokens = query.posTokens;
        List<String> allWordsInQuestion = new ArrayList<>();
        for (QueryToken queryToken : allTokens) {
            allWordsInQuestion.add(queryToken.getOriginalText());
        }
        allWordsInQuestion.removeAll(stopWords);
        for (String text : allWordsInQuestion) {
            testingBag.add(stemTerm(text.toLowerCase()));
        }

        List<String> allTestingWords = new ArrayList<>(testingBag.uniqueSet());
        double minCosineSimilarity = 0;
        String chosenGroupId = "";
        for (Map.Entry<String, Bag> trainingEntry : trainingBag.entrySet()) {
            List<Integer> trainingVector = new LinkedList<>();
            List<Integer> testingVector = new LinkedList<>();

            String trainingKey = trainingEntry.getKey();
            Bag trainingBag = trainingEntry.getValue();

            List<String> allTrainingWords = new ArrayList<>(trainingBag.uniqueSet());

            if (allTestingWords.size() > allTrainingWords.size()) {
                for (String word : allTestingWords) {
                    if (allTrainingWords.contains(word)) {
                        trainingVector.add(trainingBag.getCount(word));
                        testingVector.add(testingBag.getCount(word));
                        allTrainingWords.remove(word);
                    } else {
                        testingVector.add(testingBag.getCount(word));
                        trainingVector.add(0);
                    }
                }
                for (String word : allTrainingWords) {
                    testingVector.add(0);
                    trainingVector.add(trainingBag.getCount(word));
                }
            } else if (allTestingWords.size() <= allTrainingWords.size()) {
                for (String word : allTrainingWords) {
                    if (allTestingWords.contains(word)) {
                        trainingVector.add(trainingBag.getCount(word));
                        testingVector.add(testingBag.getCount(word));
                        allTestingWords.remove(word);
                    } else {
                        testingVector.add(0);
                        trainingVector.add(trainingBag.getCount(word));
                    }

                }
                for (String word : allTestingWords) {
                    testingVector.add(testingBag.getCount(word));
                    trainingVector.add(0);
                }
            }

            double[] training = new double[trainingVector.size()];
            double[] testing = new double[testingVector.size()];
            for (int i = 0; i < testingVector.size(); i++) {
                testing[i] = (double) testingVector.get(i);
            }
            for (int i = 0; i < trainingVector.size(); i++) {
                training[i] = (double) trainingVector.get(i);
            }

            INDArray nd = Nd4j.create(training);
            INDArray nd2 = Nd4j.create(testing);
            double similarity = Transforms.cosineSim(nd, nd2);
            QuestionResult result = new QuestionResult(trainingKey, trainingKey, (float) similarity);
            allResults.add(result);
//            if (similarity > minCosineSimilarity) {
//                minCosineSimilarity = similarity;
//                chosenGroupId = trainingKey;
//
//            }
        }
        QuestionResult result = new QuestionResult(chosenGroupId, chosenGroupId, (float) minCosineSimilarity);
        allResults.add(result);

        return allResults;
    }

}
