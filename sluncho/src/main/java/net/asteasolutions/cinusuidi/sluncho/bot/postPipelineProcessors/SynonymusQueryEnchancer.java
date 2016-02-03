package net.asteasolutions.cinusuidi.sluncho.bot.postPipelineProcessors;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.PriorityQueue;

import org.apache.commons.lang.SerializationUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.sun.xml.internal.bind.v2.schemagen.xmlschema.List;

import edu.smu.tspell.wordnet.AdjectiveSynset;
import edu.smu.tspell.wordnet.AdverbSynset;
import edu.smu.tspell.wordnet.NounSynset;
import edu.smu.tspell.wordnet.Synset;
import edu.smu.tspell.wordnet.VerbSynset;
import edu.smu.tspell.wordnet.WordNetDatabase;
import gate.Gate;
import gate.util.compilers.eclipse.jdt.internal.compiler.lookup.AnnotatableTypeSystem;
import net.asteasolutions.cinusuidi.sluncho.bot.Query;
import net.asteasolutions.cinusuidi.sluncho.questionparser.AnnotationType;
import net.asteasolutions.cinusuidi.sluncho.questionparser.QueryToken;
import net.asteasolutions.cinusuidi.sluncho.questionparser.QuestionParser;
import net.asteasolutions.cinusuidi.sluncho.questionparser.helpers.GateOffsetBoundary;
import net.asteasolutions.cinusuidi.sluncho.utils.ThreeGramCalculator;
import verbSynset.Synonym;


public class SynonymusQueryEnchancer implements IPostPipelineProcessor {

	private static ArrayList<ArrayList<ArrayList<String>>> generateSynonymusSentences(ArrayList<ArrayList<String>> sentence, Integer wordWithTypeIndex) {

	 	ArrayList<ArrayList<ArrayList<String>>> allQueries = new ArrayList<>();
	 	SynonymusQueryEnchancer synonymusGenerator = new SynonymusQueryEnchancer();
	 	ArrayList<String> wordWithType = sentence.get(wordWithTypeIndex);
	 	
	 	ArrayList<Synonym> synonyms = synonymusGenerator.getSynonyms(wordWithType.get(0));
	 	Integer found = 0;
	 	Integer maxGeneratedSynonys = 10;
	 	
	 	for (int i = 0; i < synonyms.size(); i++){
	 		if(found > maxGeneratedSynonys){
	 			break;
	 		}
	 		
	 		Synonym synonym = synonyms.get(i);
	 			 		
	 		if(synonym.getwordTypeForProbabilityCalc().equals(wordWithType.get(1)) && !synonym.getSynonumWord().equals(wordWithType.get(0))) {
	 			
	 			ArrayList<ArrayList<String>> nextSentence = new ArrayList<ArrayList<String>>();
	 			for (int k = 0; k < sentence.size(); k++) {
		 			nextSentence.add(new ArrayList<String>(2));
		 			nextSentence.get(k).add(sentence.get(k).get(0));
		 			nextSentence.get(k).add(sentence.get(k).get(1));
	 		 	}
	 			nextSentence.get(wordWithTypeIndex).set(0, synonym.getSynonumWord());
	 			allQueries.add(nextSentence);
		 		found++;
	 		}
	 		
	 	}
	 	
	 	Collections.sort(allQueries, new Comparator<ArrayList<ArrayList<String>>>() {
			@Override
			public int compare(ArrayList<ArrayList<String>> o1, ArrayList<ArrayList<String>> o2) {
				String[][] o1_c = new String[o1.size()][2];
				for(int i = 0; i < o1.size(); i++) {
					o1_c[i][0] = o1.get(i).get(0);
					o1_c[i][1] = o1.get(i).get(1);
				}
				
				String[][] o2_c = new String[o1.size()][2];
				
				for(int i = 0; i < o2.size(); i++) {
					o2_c[i][0] = o2.get(i).get(0);
					o2_c[i][1] = o2.get(i).get(1);
				}
				
				return ThreeGramCalculator.calculate(o2_c).compareTo(ThreeGramCalculator.calculate(o1_c));
			}
	 	});
	 	
		return allQueries;
	 }
	
	private VerbSynset parseToVerbSunset(Synset synset) {
		VerbSynset result;
		try {
			result = (VerbSynset)(synset);
			return result;
		} catch(java.lang.ClassCastException e) {
			return null;
		}
	}

	private NounSynset parseToNounSynset(Synset synset) {
		NounSynset result;
		try {
			result = (NounSynset)(synset);
			return result;
		} catch(java.lang.ClassCastException e) {
			return null;
		}
	}

	private AdjectiveSynset parseToAdjectiveSynset(Synset synset) {
		AdjectiveSynset result;
		try {
			result = (AdjectiveSynset)(synset);
			return result;
		} catch(java.lang.ClassCastException e) {
			return null;
		}
	}

	private AdverbSynset parseToAdverbSynset(Synset synset) {
		AdverbSynset result;
		try {
			result = (AdverbSynset)(synset);
			return result;
		} catch(java.lang.ClassCastException e) {
			return null;
		}
	}

	private ArrayList<Synonym> getSynonyms(String word) {
		//TODO Place this in the right place
		
		VerbSynset verbSynset;
		NounSynset nounSynSet;
		AdjectiveSynset adjectiveSynset;
		AdverbSynset adverbSynset;

		WordNetDatabase database = WordNetDatabase.getFileInstance();
		Synset[] synsets = database.getSynsets(word);
		PriorityQueue<Synonym> synonyms = new PriorityQueue<Synonym>();

		ArrayList<Synonym> result = new ArrayList<>();

		for (int i = 0; i < synsets.length; i++) {
			verbSynset = parseToVerbSunset(synsets[i]);
			nounSynSet = parseToNounSynset(synsets[i]);
			adjectiveSynset = parseToAdjectiveSynset(synsets[i]);
			adverbSynset = parseToAdverbSynset(synsets[i]);

			if(verbSynset != null) {
				String verbSynonum = verbSynset.getWordForms()[0];
				synonyms.add(new Synonym(verbSynonum, verbSynset.getDefinition(), verbSynset.getTagCount(verbSynonum), "verb"));
			}

			if(nounSynSet != null) {
				String nounSynonum = nounSynSet.getWordForms()[0];
				synonyms.add(new Synonym(nounSynonum, nounSynSet.getDefinition(), nounSynSet.getTagCount(nounSynonum), "noun"));
			}

			if(adjectiveSynset != null) {
				String adjectiveSynonum = adjectiveSynset.getWordForms()[0];
				synonyms.add(new Synonym(adjectiveSynonum, adjectiveSynset.getDefinition(), adjectiveSynset.getTagCount(adjectiveSynonum), "adjective"));
			}

			if(adverbSynset != null) {
				String adverbSynonum = adverbSynset.getWordForms()[0];
				synonyms.add(new Synonym(adverbSynonum, adverbSynset.getDefinition(), adverbSynset.getTagCount(adverbSynonum), "adverb"));
			}
		}
		Synonym next = null;
		while((next = synonyms.poll()) != null){
			result.add(next);
		}
		return result;
	}
	
	@Override
	public ArrayList<Query> expand(ArrayList<Query> queries){
		ArrayList<Query> result = new ArrayList<Query>(queries);
		
		for(int i = 0; i < queries.size(); i++) {
			ArrayList<ArrayList<ArrayList<String>>> synonymPredicateSentences = generateSynonymusSentences(queries.get(i).toWordsWithTypes() , queries.get(i).getPredicatePosTokenIndex());
			ArrayList<ArrayList<ArrayList<String>>> synonymSybjectSentences = generateSynonymusSentences(queries.get(i).toWordsWithTypes() , queries.get(i).getSubjectPosTokenIndex());
			
			for( ArrayList<ArrayList<String>> next: synonymPredicateSentences) {
				Query newQuery = new Query(queries.get(i));
				newQuery.updateWith(next);
				result.add(newQuery);
			}
			
			for( ArrayList<ArrayList<String>> next: synonymSybjectSentences) {
				Query newQuery = new Query(queries.get(i));
				newQuery.updateWith(next);
				result.add(newQuery);
			}
		}
		
		return result;
	}

}
