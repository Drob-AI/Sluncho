package net.asteasolutions.cinusuidi.sluncho.questionparser;

public enum AnnotationType {
	IP_ADDRESS("Ip"),
	DATE("Date"),
	GENERIC_NAMED_ENTITY("named_entity"),
	SIMPLE_TOKEN("simple_token"),
	NOUN_PHRASE("noun_phrase"),
	NSUBJ("nsubj");

	private String gateType;
	private AnnotationType(String type) {
		this.gateType = type;
	}
	
	public String getTextRepresentation() {
		return gateType;
	}
	
	public boolean isAstean() {
		return this.gateType.startsWith("astea");
	}
	public static AnnotationType fromString(String type) throws Exception {
		for (AnnotationType annot : AnnotationType.values()) {
			if (annot.gateType.equals(type)) {
				return annot;
			}
		}
		throw new Exception("Unrecognized annotation type: " + type);
	}
}
