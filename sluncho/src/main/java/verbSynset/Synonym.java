package verbSynset;

public class Synonym implements Comparable<Synonym>{
	
	String synonumWord;
	String description;
	Integer countTag;
	String wordType;
	
	public String getSynonumWord() {
		return synonumWord;
	}

	public String getDescription() {
		return description;
	}

	public Integer getCountTag() {
		return countTag;
	}

	public String getWordType() {
		return wordType;
	}

	
	public Synonym(String synonumWord, String description, Integer countTag, String wordType) {
		this.synonumWord = synonumWord;
		this.description = description;
		this.countTag = countTag;
		this.wordType = wordType;	
	}
	
	public void print() {
		System.out.println(this.synonumWord + ":" + this.description + "(" + this.wordType + ")");
		System.out.println(countTag);
	}
	
	public String getwordTypeForProbabilityCalc() {
//		sorry for this one
//		parsing to this format:
//		http://ucrel.lancs.ac.uk/claws7tags.html 
		if(this.wordType.equals("noun")) {
			return "n";
		} else if(this.wordType.equals("verb")) {
			return "v";
		} else if(this.wordType.equals("adjective")) {
			return "j";
		} else if(this.wordType.equals("adverb")) {
			return "r";
		} else {
			// just making java happy with this one:(
			return "n";
		}
		
	}
	
	@Override
	public int compareTo(Synonym other) {
		return Integer.valueOf(other.countTag).compareTo(this.countTag);
	}
	
}
