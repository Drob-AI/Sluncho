package net.asteasolutions.cinusuidi.sluncho.questionparser;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import net.asteasolutions.cinusuidi.sluncho.questionparser.helpers.GateOffsetBoundary;

public class QueryToken {
	private String originalText;
	private AnnotationType type;
	private GateOffsetBoundary boundary;

	private Map<String, String> annotations;

	public QueryToken(String text, AnnotationType mainType, GateOffsetBoundary boundary) {
		originalText = text;
		type = mainType;
		annotations = new HashMap<>();
        this.boundary = boundary;
	}
	
//	public static void main(String[] args) {
//		
//	}
	//deep constructor
	public QueryToken(String text, AnnotationType mainType, GateOffsetBoundary boundary, Map<String, String> ann) {
		originalText = text;
		try {
			type = AnnotationType.fromString(mainType.getTextRepresentation());
			// TODO: deep copy annotations
			annotations = new HashMap<>();
			for(Entry<?, ?> e: ann.entrySet()){
				annotations.put((String)e.getKey(), (String)e.getValue());
			}
	        this.boundary = new GateOffsetBoundary(boundary);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
    public QueryToken(QueryToken other) {
		this(other.getOriginalText(), other.getType(), other.getBoundary(), other.getAnnotations());
	}

	public GateOffsetBoundary getBoundary() {
        return boundary;
    }
    
    public void setOriginalText(String text) {
        originalText = text;
    }
        
	public String getOriginalText () {
		return originalText;
	}
	
	public AnnotationType getType() {
		return type;
	}
	
	public void addAnnotation(String type, String value) {
		annotations.put(type, value);
	}
	
	public String getPartOfSpeechForNgrams() {
		
		String gatePartOfSpeechTag = annotations.get("category").toLowerCase();
		//type matching between to one letter between: 
		// https://gate.ac.uk/sale/tao/splitap7.html
		// and http://ucrel.lancs.ac.uk/claws7tags.html
		// TODO: implement better matching (with more letters, eventually full)
		if(gatePartOfSpeechTag == "md") {
			return "v";
		} 
		
		if(gatePartOfSpeechTag.charAt(0) == 'w') {
			return "r";
		}
		
		return annotations.get("category").substring(0,1).toLowerCase();
	}
	
	public void getAnnotation(String type) {
		annotations.get(type);
	}
	
    public Map<String, String> getAnnotations() {
        return annotations;
    }
	
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder(type.getTextRepresentation());
		result.append(": ");
		result.append(originalText);
		result.append("(Annotations: ");
		for (Entry<String, String> entry : annotations.entrySet()) {
			result.append(entry.getKey());
			result.append("=");
			result.append(entry.getValue());
			result.append(",");
		}
		result.append(")");
		return result.toString();
	}

    public void setType(AnnotationType annotationType) {
        type = annotationType;
    }
}
