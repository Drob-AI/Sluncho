package net.asteasolutions.cinusuidi.sluncho.bot.doc2vecClassifierUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.deeplearning4j.text.documentiterator.LabelAwareIterator;
import org.deeplearning4j.text.documentiterator.LabelledDocument;
import org.deeplearning4j.text.documentiterator.LabelsSource;

import lombok.NonNull;
import net.asteasolutions.cinusuidi.sluncho.data.QuestionRepository;
import net.asteasolutions.cinusuidi.sluncho.facade.MongoDBFacade;
import net.asteasolutions.cinusuidi.sluncho.model.Question;

public class RelevantQuestionsIterator implements LabelAwareIterator {
	 	protected List<Question> questions = new ArrayList<>();
	 	protected List<String> labels = new ArrayList<>();
	 	protected Integer position = new Integer(0);
	 	
	 	public void setQuestions(List<Question> questions) {
	 		this.questions = questions;
	 	}
	 	public RelevantQuestionsIterator() {
	 		if(questions.size() == 0 ) {
		 		MongoDBFacade mongoConnection = new MongoDBFacade();
		 		if ( QuestionRepository.originalQuestions == null) {
		 	        List<Question> originalQuestions = mongoConnection.getAllOriginalQuestions();
		 			QuestionRepository.setOriginalQuestions(originalQuestions);
		 		}
		 		
		 		for(Question question: QuestionRepository.originalQuestions) {
		 			this.questions.add(question);
	 	        	List<Question> relQuestions = mongoConnection.getAllRelevantQuestions(question.getGroupId());
	 	        	for(Question relQuestion: relQuestions) {
	 	        		if(!relQuestion.getIsRelevantToOriginalQuestion().equals("Irrelevant")) {
	 	        			this.questions.add(relQuestion);
	 	        		}
	 	        	}
	 	        }
		 		
		 		for(Question question: this.questions) {
		 			System.err.println(question.getIsRelevantToOriginalQuestion());
		 		}
	//	 		
		 		for(Question question: QuestionRepository.originalQuestions) {
		 			labels.add(question.getGroupId());
		 		}
	 		}
		}
	 	
	    @Override
	    public boolean hasNextDocument() {
	    	return position < questions.size();
	    }


	    @Override
	    public LabelledDocument nextDocument() {
	    	LabelledDocument document = new LabelledDocument();
	    	document.setContent(questions.get(position).getBody());
	    	document.setLabel(questions.get(position).getGroupId());
	    	position++;
	    	return document;
	    }

	    @Override
	    public void reset() {
	        position = 0;
	    }

	    @Override
	    public LabelsSource getLabelsSource() {
	    	LabelsSource source = new LabelsSource(labels);
	    	return source;
	    }
}

