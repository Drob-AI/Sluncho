package net.asteasolutions.cinusuidi.sluncho.bot.word2vecClassifierUtils;

import org.deeplearning4j.text.sentenceiterator.SentencePreProcessor;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.StringCleaning;

public class QuestionSentencePreProcessor implements SentencePreProcessor {

	@Override
	public String preProcess(String sentence) {
		return StringCleaning.stripPunct(sentence).toLowerCase();
	}

}
