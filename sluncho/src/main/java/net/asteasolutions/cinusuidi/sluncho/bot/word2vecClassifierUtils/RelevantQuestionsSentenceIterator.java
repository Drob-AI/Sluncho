package net.asteasolutions.cinusuidi.sluncho.bot.word2vecClassifierUtils;

import java.util.ArrayList;
import java.util.List;

import org.deeplearning4j.text.documentiterator.LabelledDocument;
import org.deeplearning4j.text.sentenceiterator.SentencePreProcessor;
import org.deeplearning4j.text.sentenceiterator.labelaware.LabelAwareSentenceIterator;

import net.asteasolutions.cinusuidi.sluncho.data.QuestionRepository;
import net.asteasolutions.cinusuidi.sluncho.model.Question;

public class RelevantQuestionsSentenceIterator implements LabelAwareSentenceIterator {

	protected List<Question> questions = new ArrayList<>();
	protected List<String> labels = new ArrayList<>();
	protected Integer position = new Integer(0);
	protected String currentLabel;
	private SentencePreProcessor sentencePreProcessor;

	public RelevantQuestionsSentenceIterator(List<Question> allQ) {
		questions = allQ;
		//labels = QuestionRepository.Instance().labels;
	}

	public RelevantQuestionsSentenceIterator() {
		QuestionRepository repo = QuestionRepository.Instance();
		questions = repo.allQuestions;
		//labels = repo.labels;
		//System.out.println("RelevantQuestionsSentenceIterator() called");
	}

	@Override
	public String nextSentence() {
		String ret = questions.get(position).getBody();
        //currentLabel = labels.get(position);
        if(sentencePreProcessor != null)
            ret = sentencePreProcessor.preProcess(ret);
        position++;
        return ret;
	}

	@Override
	public boolean hasNext() {
		return position < questions.size();
	}

	@Override
	public void reset() {
		position = 0;

	}

	@Override
	public void finish() {
		// TODO Auto-generated method stub
	}

	@Override
    public SentencePreProcessor getPreProcessor() {
        return sentencePreProcessor;
    }

    @Override
    public void setPreProcessor(SentencePreProcessor preProcessor) {
        this.sentencePreProcessor = preProcessor;
    }

	@Override
	public String currentLabel() {
		return this.currentLabel;
	}

	@Override
	public List<String> currentLabels() {
		return this.labels;
	}

}
