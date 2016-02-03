package net.asteasolutions.cinusuidi.sluncho.documentIndex;

import net.asteasolutions.cinusuidi.sluncho.data.IndexableDocument;
import net.asteasolutions.cinusuidi.sluncho.data.LegalDocument;

public class IdentityDocumentParser implements IDocumentParser {

	@Override
	public LegalDocument parse(String documentIdentifier,
			IndexableDocument document) {
		LegalDocument result = new LegalDocument();
		result.title = document.title;
		result.content = document.content;
		return result;
	}

}
