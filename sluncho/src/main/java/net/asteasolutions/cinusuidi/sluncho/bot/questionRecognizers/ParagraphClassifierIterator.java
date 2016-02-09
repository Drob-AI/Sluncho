package net.asteasolutions.cinusuidi.sluncho.bot.questionRecognizers;

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

public class ParagraphClassifierIterator implements LabelAwareIterator {
	 	protected List<String> texts = new ArrayList<>();
	 	protected List<String> labels = new ArrayList<>();
	 	protected Integer position = new Integer(0);
	 	
	 	public ParagraphClassifierIterator() {
	 		texts.add(0, "this is hary");
	 		texts.add(1, "i love dogs");
	 		texts.add(2, "java sucks");
	 		
	 		labels.add(0, "hary");
	 		labels.add(1, "dogs");
	 		labels.add(2, "java");
		}
	 	
	    @Override
	    public boolean hasNextDocument() {
	    	return position < texts.size();
	    }


	    @Override
	    public LabelledDocument nextDocument() {
	    	LabelledDocument document = new LabelledDocument();
	    	document.setContent(texts.get(position));
	    	document.setLabel(labels.get(position));
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
