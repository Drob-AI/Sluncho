package net.asteasolutions.cinusuidi.sluncho.questionparser;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Corpus;
import gate.Document;
import gate.Factory;
import gate.FeatureMap;
import gate.creole.ConditionalSerialAnalyserController;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.persist.PersistenceException;
import gate.util.persistence.PersistenceManager;
import net.asteasolutions.cinusuidi.sluncho.bot.Query;
import net.asteasolutions.cinusuidi.sluncho.questionparser.exception.QuestionParserException;
import net.asteasolutions.cinusuidi.sluncho.questionparser.helpers.GateDocumentHelper;
import net.asteasolutions.cinusuidi.sluncho.questionparser.helpers.GateOffsetBoundary;

/**
 * A class that utilizes GATE to annotate questions
 *
 */
public class QuestionParser {
	private static String GATE_APP_PATH;

	private ConditionalSerialAnalyserController gatePipelineApp;

	public void init() throws PersistenceException, ResourceInstantiationException, IOException, URISyntaxException {
                GATE_APP_PATH = System.getProperty("gate.astea.app.home") + "/application.xgapp";
		if (gatePipelineApp == null) {
			gatePipelineApp = (ConditionalSerialAnalyserController)
					PersistenceManager.loadObjectFromFile(new File(GATE_APP_PATH));
		}
	}

	public Query parse(String question) throws QuestionParserException {
		Corpus corpus;
		Document document;
		Query query = new Query();
		try {
			corpus = Factory.newCorpus("GATE Corpus");
			document = Factory.newDocument(question);
		} catch (ResourceInstantiationException e) {
			e.printStackTrace();
			throw new QuestionParserException("Error creating question document");
		}
		corpus.add(document);
		gatePipelineApp.setCorpus(corpus);
		try {
			gatePipelineApp.execute();
		} catch (ExecutionException e) {
			e.printStackTrace();
			throw new QuestionParserException("Error processing question");
		}

		List<QueryTokenWrapper> importantAnnotations = new ArrayList<>();
                List<QueryTokenWrapper> basicAnnotations = new ArrayList<>();

		List<QueryTokenWrapper> asteaTokens = extractAsteaTokens(document);
		List<QueryTokenWrapper> neTokens = extractNETokens(document);
		List<QueryTokenWrapper> npTokens = extractNounPhrases(document);
		List<QueryTokenWrapper> defaultTokens = extractDefaultTokens(document);
		importantAnnotations = orderAnnotations(asteaTokens, neTokens, npTokens, defaultTokens);
                basicAnnotations = orderAnnotations(defaultTokens);
                

		QueryTokenWrapper nsubjToken = findNSubjToken(document);
		if (nsubjToken != null) {
			for (QueryTokenWrapper tokenWrapper : importantAnnotations) {
				if (tokenWrapper.overlaps(nsubjToken) &&
						tokenWrapper.getToken().getType() == AnnotationType.NOUN_PHRASE) {
					// mark as a subject token
					tokenWrapper.getToken().addAnnotation("Subject", "true");
				}
			}
		}
		List<QueryToken> result = new ArrayList<QueryToken>();
		for (QueryTokenWrapper wrapper: importantAnnotations) {
			result.add(wrapper.getToken());
		}
                List<QueryToken> basicResult = new ArrayList<QueryToken>();
                for (QueryTokenWrapper wrapper: basicAnnotations) {
                    basicResult.add(wrapper.getToken());
                }

		List<QueryToken> npTokensStripped = new ArrayList<>();
		for (QueryTokenWrapper wrapper : npTokens) {
			npTokensStripped.add(wrapper.getToken());
		}

                if(nsubjToken == null) {
                    query.subjectToken = null;
                } else {
                    query.subjectToken = nsubjToken.getToken();
                }

		query.allNPTokens = npTokensStripped;
		query.orderedTokens = result;
                query.posTokens = basicResult;

		return query;
	}

	private QueryTokenWrapper findNSubjToken(Document document) {
		AnnotationSet defaultAnnotations = document.getAnnotations().get("Dependency");
		for (Annotation annot : defaultAnnotations) {
			FeatureMap features = annot.getFeatures();
			if (features.get("kind").equals("nsubj")) {
				// assume there is only one nsubj, and return it
				return new QueryTokenWrapper(
						GateDocumentHelper.getAnnotationText(annot, document),
						AnnotationType.NSUBJ,
						GateDocumentHelper.getAnnotationBoundaries(annot));
			}
		}
		return null;
	}

	private List<QueryTokenWrapper> extractNounPhrases (Document document) {
		List<QueryTokenWrapper> slunchoTokens = new ArrayList<>();
		AnnotationSet defaultAnnotations = document.getAnnotations().get("SyntaxTreeNode");
		for (Annotation annot : defaultAnnotations) {
			FeatureMap features = annot.getFeatures();
			String category = features.get("cat").toString();
			if (category.startsWith("N")) {
				QueryTokenWrapper tokenWrapper = new QueryTokenWrapper(
						features.get("text").toString(),
						AnnotationType.NOUN_PHRASE,
						GateDocumentHelper.getAnnotationBoundaries(annot));
				slunchoTokens.add(tokenWrapper);
			}
		}
		Collections.sort(slunchoTokens);
		return slunchoTokens;
	}
	private List<QueryTokenWrapper> extractDefaultTokens(Document document) {
		AnnotationSet defaultAnnotations = document.getAnnotations().get("Token");
		List<QueryTokenWrapper> slunchoTokens = new ArrayList<>();
		for (Annotation annotation : defaultAnnotations) {
			FeatureMap features = annotation.getFeatures();
			QueryTokenWrapper tokenWrapper = new QueryTokenWrapper(
					features.get("string").toString(),
					AnnotationType.SIMPLE_TOKEN,
					GateDocumentHelper.getAnnotationBoundaries(annotation));
			QueryToken token = tokenWrapper.getToken();
			for (Object key : features.keySet()) {
				if (key.equals("category")) {
					token.addAnnotation((String)key, (String)features.get(key));
				}
			}
			slunchoTokens.add(tokenWrapper);
		}
		Collections.sort(slunchoTokens);
		return slunchoTokens;
	}

	@SafeVarargs
	private final List<QueryTokenWrapper> orderAnnotations(List<QueryTokenWrapper>... customAnnotations) {
		List<QueryTokenWrapper> orderedTokens = new LinkedList<>();
		for (List<QueryTokenWrapper> custom : customAnnotations) {
			insertOrdered(custom, orderedTokens);
		}
		return orderedTokens;
	}

	private void insertOrdered(List<QueryTokenWrapper> annotations, List<QueryTokenWrapper> orderedTokens) {
		for (QueryTokenWrapper tokenWrapper : annotations) {
			int insertionIdx = getInsertionIdx(orderedTokens, tokenWrapper);
			if (insertionIdx < 0) {
				continue;
			}
			if (orderedTokens.size() == 0 || (insertionIdx > 0 &&
					orderedTokens.get(insertionIdx - 1).getBoundary().end <= tokenWrapper.getBoundary().start) ||
			    (orderedTokens.size() > 0 && orderedTokens.get(0).getBoundary().start >= tokenWrapper.getBoundary().end)) {
				orderedTokens.add(insertionIdx, tokenWrapper);
			} else {
				System.err.println("Ignoring token " + tokenWrapper.getToken().getOriginalText() +
						" because it overlaps another token.");
			}
		}
	}

	private int getInsertionIdx(List<QueryTokenWrapper> orderedTokens, QueryTokenWrapper token) {
		int tokenInsertIdx = Collections.binarySearch(orderedTokens, token);
		// if found, return a negative idx - means "do not insert"
		tokenInsertIdx = -tokenInsertIdx;
		tokenInsertIdx -= 1;

		return tokenInsertIdx;
	}

	private List<QueryTokenWrapper> extractNETokens(Document document) {
		List<QueryTokenWrapper> importantAnnotations = new ArrayList<>();
		AnnotationSet stanfordNERAnnotations = document.getAnnotations("Stanford_NER");
		AnnotationSet annieNER = document.getAnnotations("ANNIE_NER");

		// Stanford is better for name candidates, use ANNIE for ip addresses and dates
		for (Annotation annotation : stanfordNERAnnotations) {
			if (annotation.getType().equals("PERSON")) {
				String annotatedText = GateDocumentHelper.getAnnotationText(annotation, document);
				importantAnnotations.add(new QueryTokenWrapper(
						annotatedText, AnnotationType.GENERIC_NAMED_ENTITY,
						GateDocumentHelper.getAnnotationBoundaries(annotation)));
			}
		}

		for (Annotation annotation : annieNER) {
			String annotType = annotation.getType();
			String annotationText = GateDocumentHelper.getAnnotationText(annotation, document);
			GateOffsetBoundary boundary = GateDocumentHelper.getAnnotationBoundaries(annotation);
			AnnotationType type = null;
			if (annotType.equals("Ip")) {
				type = AnnotationType.IP_ADDRESS;
			} else if (annotType.equals("TempDate")) {
				type = AnnotationType.DATE;
			}
			if (type != null) {
				importantAnnotations.add(new QueryTokenWrapper(annotationText, type, boundary));
			}
		}
		Collections.sort(importantAnnotations);
		return importantAnnotations;
	}

	private List<QueryTokenWrapper> extractAsteaTokens(Document document) {
		List<QueryTokenWrapper> importantAnnotations = new ArrayList<>();
		AnnotationSet asteaListsAnnotations = document.getAnnotations("Astea_ANNOT");
		for (Annotation annotation : asteaListsAnnotations) {
			FeatureMap allFeatures = annotation.getFeatures();
			String annotationType = (String) allFeatures.get("minorType");
			GateOffsetBoundary boundary = GateDocumentHelper.getAnnotationBoundaries(annotation);
			AnnotationType slunchoType = null;
			try {
				slunchoType = AnnotationType.fromString(annotationType);
				String annotatedText = GateDocumentHelper.getAnnotationText(annotation, document);
				importantAnnotations.add(new QueryTokenWrapper(annotatedText, slunchoType, boundary));
			} catch (Exception e) {
				// probably a mismatch between gate config and the annotations types
				e.printStackTrace();
			}
		}
		Collections.sort(importantAnnotations);
		return importantAnnotations;
	}

//	public static void main(String[] args) throws IOException, QuestionParserException, GateException, URISyntaxException {
//		Gate.init();
//		//Gate.getCreoleRegister().registerDirectories(new File("/home/mkraeva/GATE_Developer_8.1/plugins/Stanford_CoreNLP").toURI().toURL());
//		QuestionParser parser = new QuestionParser();
//		parser.init();
//		List<String> testQuestions = Arrays.asList("Who is Marina Kraeva?",
//					"What is Harry Avramov's IP address?",
//					"Who works on 23/12/2015?","What are the requirements for an Entry level engineer?");
//		for (String test : testQuestions) {
//			Query result = parser.parse(test);
//			System.out.println(test);
//			System.out.println(result.orderedTokens);
//			System.out.println();
//		}
//	}
}
