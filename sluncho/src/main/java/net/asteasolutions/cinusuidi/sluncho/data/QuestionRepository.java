package net.asteasolutions.cinusuidi.sluncho.data;

import java.util.List;

import net.asteasolutions.cinusuidi.sluncho.model.Question;

public class QuestionRepository {
	public static List<Question> originalQuestions;
	public static void setOriginalQuestions(List<Question> originalQ) {
		originalQuestions = originalQ;
	}
}
