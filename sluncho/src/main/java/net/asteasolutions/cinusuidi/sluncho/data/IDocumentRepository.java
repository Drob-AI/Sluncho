package net.asteasolutions.cinusuidi.sluncho.data;

import java.io.IOException;

public interface IDocumentRepository {
	String[] getDocumentsRefs() throws IOException;
	IndexableDocument getDocument(String ref) throws IOException;
}
