package net.asteasolutions.cinusuidi.sluncho.documentIndex;

import java.util.ArrayList;
import java.util.Iterator;

import net.asteasolutions.cinusuidi.sluncho.data.LegalDocument;
import net.asteasolutions.cinusuidi.sluncho.data.Paragraph;

public class QuestionAnswerBuilder {
	private boolean isEmpty(String str) {
		return str == null || str.length() == 0 || str == "" || str.matches("\\s+"); 
	}
	
	private void generateQuestions(Paragraph p, String groupId, String context, ArrayList<QuestionAnswer> qas) {
		QuestionAnswer qa = new QuestionAnswer();
		qa.context = context;
		qa.question = p.title;
		qa.answer = p.content;
                qa.groupId = groupId;
		
		if(!isEmpty(qa.answer) && !isEmpty(qa.question)) {
			qas.add(qa);
		}
		
		Iterator<Paragraph> subParagraphIter = p.paragraphs.iterator();
		String innerContext = context + '.' + p.title;
		while(subParagraphIter.hasNext()) {
			Paragraph child = subParagraphIter.next();
			generateQuestions(child,groupId, innerContext, qas);
		}
	}
	
	public ArrayList<QuestionAnswer> split(LegalDocument document) {
                String groupId = document.identifier;
		ArrayList<QuestionAnswer> qas = new ArrayList<QuestionAnswer>();
		generateQuestions(document, groupId, "", qas);
		return qas;
	}
}
