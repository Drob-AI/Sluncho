package net.asteasolutions.cinusuidi.sluncho.documentIndex;

import java.util.Iterator;

import net.asteasolutions.cinusuidi.sluncho.data.IndexableDocument;
import net.asteasolutions.cinusuidi.sluncho.data.LegalDocument;
import net.asteasolutions.cinusuidi.sluncho.data.Paragraph;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class HtmlDocumentParser implements IDocumentParser {
	private int getHeadingLevel(Element e) {
		String tagName = e.tagName();
		switch(tagName) {
                    case "h1" :
                        return 1;
                    case "h2" :
                        return 2;
		case "h3" : 
			return 3;
		case "h4" : 
			return 4;
		case "h5" : 
			return 5;
		case "h6" : 
			return 6;
		}
		return -1;
	}
		
	private boolean isHeading(Element e) {
		String tagName = e.tagName();
		return tagName == "h1" || tagName == "h2" || tagName == "h3" || tagName == "h4" || tagName == "h5" || tagName == "h6";
	}
	
	public LegalDocument parse(String documentIdentifier, IndexableDocument document) {
		Document doc = Jsoup.parse(document.content);
		Element body = doc.body();
		
		LegalDocument legalDocument = new LegalDocument();
		Paragraph currentParagraph = legalDocument;
		StringBuilder currentContent = new StringBuilder();
		
		
		Iterator<Element> bodyIterator = body.children().iterator();
		while(bodyIterator.hasNext()) {
			Element current = bodyIterator.next();
			if(isHeading(current)) {
				int level = getHeadingLevel(current);
				currentParagraph.content = currentContent.toString();
				currentContent = new StringBuilder();
				while(level <= currentParagraph.level) {
					currentParagraph = currentParagraph.parent;
				}
				Paragraph last = currentParagraph;
				currentParagraph = new Paragraph(level, currentParagraph);
				last.paragraphs.add(currentParagraph);
				currentParagraph.title = current.text();
			} else {
				currentContent.append(current.text());
			}
		}
		
		legalDocument.identifier = documentIdentifier;
		legalDocument.title = body.getElementsByClass("title").text();
		
		return legalDocument;
	}
}
