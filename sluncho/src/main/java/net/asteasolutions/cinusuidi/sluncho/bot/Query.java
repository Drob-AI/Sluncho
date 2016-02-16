package net.asteasolutions.cinusuidi.sluncho.bot;

import java.util.ArrayList;
import java.util.List;

import net.asteasolutions.cinusuidi.sluncho.questionparser.QueryToken;

public class Query {
	
	public String originalText;
	public String extendedOriginalText;
    //Annotations
    //Pos tagged token, they have category annotation
    public List<QueryToken> posTokens;
    //Noun groups, astea tokens, etc..
    public List<QueryToken> orderedTokens;
    //All noun group
    public List<QueryToken> allNPTokens;
    
    //Token containing the subject and the predicate
    public QueryToken subjectToken;
    
    //<LUCENE SEARCHABLE>
    public QueryToken subject;
    public QueryToken predicate;
    public QueryToken nounPhrase;
    
    //Noun group of the subject
    public List<QueryToken> subjectGroup;
    
    //Verb group of the predicate
    public List<QueryToken> predicateGroup;
    
    //Noun group of the addition, can often be empty
    public List<QueryToken> additionGroup;
    //</LUCENE SEARCHABLE>
    
    private ArrayList<QueryToken> copy(List<QueryToken> src) {
    	if(src == null) {
    		return null;
    	}
    	ArrayList<QueryToken> result = new ArrayList<QueryToken>();
    	
    	for(QueryToken next: src) {
    		QueryToken copyNext = new QueryToken(next);
    		result.add(copyNext);
    	}
    	
    	return result;
    }
    
    public Query(Query other) {    	
    	this.originalText = other.originalText;
    	this.extendedOriginalText = other.extendedOriginalText;

        this.posTokens = copy(other.posTokens);
        this.orderedTokens = copy(other.orderedTokens);
        this.allNPTokens = copy(other.allNPTokens);
        
        if(other.subjectToken != null) {
        	this.subjectToken = new QueryToken(other.subjectToken);
        }
        

        if(other.subject != null) {
        	this.subject = new QueryToken(other.subject);
        }

        if(other.predicate != null) {
        	this.predicate = new QueryToken(other.predicate);
        }
        
        if(other.nounPhrase != null) {
        	this.nounPhrase = new QueryToken(other.nounPhrase);
        }
        
        this.subjectGroup = copy(other.subjectGroup);
        this.predicateGroup = copy(other.predicateGroup);
        this.additionGroup = copy(other.additionGroup);
	}
    
    public ArrayList<ArrayList<String>> toWordsWithTypes() {
    	ArrayList<ArrayList<String>> result = new ArrayList<ArrayList<String>>();
    	for(QueryToken next: this.posTokens) {
    		ArrayList<String> forAdd = new ArrayList<>();
    		forAdd.add(next.getOriginalText());
    		forAdd.add(next.getPartOfSpeechForNgrams());
    		result.add(forAdd);
    	}
    	return result;
    }
    //ugly but needed (if somewhere hase new Query();
    public Query() {
		
	}
    
    public Integer getSubjectPosTokenIndex() {
    	for(int i = 0; i < posTokens.size(); i++) {
    		if(posTokens.get(i).getBoundary().start.equals(subject.getBoundary().start)) {
    			return i;
    		}
    	}
    	return -1;
    }
    
    public Integer getPredicatePosTokenIndex() {
    	for(int i = 0; i < posTokens.size(); i++) {
    		if(posTokens.get(i).getBoundary().start.equals(predicate.getBoundary().start)) {
    			return i;
    		}
    	}
    	return -1;
    }
	@Override
    public String toString() {
            String result = "";
            for(QueryToken token: posTokens) {
                    result += token.toString() + " ";
            }
            return result;
    }

	public void updateWith(ArrayList<ArrayList<String>> sentence) {
		Integer subjectIndex = this.getSubjectPosTokenIndex();
		Integer predicateIndex = this.getPredicatePosTokenIndex();
		
		for(int i = 0; i < sentence.size(); i++) {
			this.posTokens.get(i).setOriginalText(sentence.get(i).get(0));
		}
		
		subject.setOriginalText(posTokens.get(subjectIndex).getOriginalText());
		predicate.setOriginalText(posTokens.get(predicateIndex).getOriginalText());
	}
}
