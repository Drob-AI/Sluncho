package net.asteasolutions.cinusuidi.sluncho.bot.questionHandlers;

import net.asteasolutions.cinusuidi.sluncho.bot.questionRecognizers.IQuestionRecognizer;
import net.asteasolutions.cinusuidi.sluncho.data.IDataSource;

public interface IQuestionHandler {
	IQuestionRecognizer getQuestionRecognizer();
	IDataSource getDataSource();
}
