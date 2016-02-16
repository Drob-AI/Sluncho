package net.asteasolutions.cinusuidi.sluncho.bot.questionRecognizers;

import java.util.List;
import net.asteasolutions.cinusuidi.sluncho.bot.CompositeQuery;
import net.asteasolutions.cinusuidi.sluncho.bot.Query;
import net.asteasolutions.cinusuidi.sluncho.bot.QuestionResult;

public interface IQuestionRecognizer {
    List<QuestionResult> classify(CompositeQuery query);
}
