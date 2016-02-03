package net.asteasolutions.cinusuidi.sluncho.bot.questionRecognizers;

import net.asteasolutions.cinusuidi.sluncho.bot.Query;
import net.asteasolutions.cinusuidi.sluncho.bot.QuestionResult;

public interface IQuestionRecognizer {
	QuestionResult classify(Query query);
}
