package net.asteasolutions.cinusuidi.sluncho.documentIndex;

import net.asteasolutions.cinusuidi.sluncho.data.IndexableDocument;
import net.asteasolutions.cinusuidi.sluncho.data.LegalDocument;

public interface IDocumentParser {
	LegalDocument parse(String documentIdentifier, IndexableDocument document);
}
