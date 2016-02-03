package net.asteasolutions.cinusuidi.sluncho.questionparser.helpers;

import gate.Annotation;
import gate.Document;
import gate.util.InvalidOffsetException;

public class GateDocumentHelper {
	public static String getAnnotationText (Annotation annotation, Document gateDoc) {
		Long start = annotation.getStartNode().getOffset();
		Long end = annotation.getEndNode().getOffset();
		String result = "";
		try {
			result = gateDoc.getContent().getContent(start,end).toString();
		} catch (InvalidOffsetException e) {
			e.printStackTrace();
			System.err.println("GATE is stupid, it returned invalid offsets!");
		}
		return result;
	}

	public static GateOffsetBoundary getAnnotationBoundaries (Annotation annotation) {
		return new GateOffsetBoundary(
				annotation.getStartNode().getOffset(),
				annotation.getEndNode().getOffset());
	}
}
