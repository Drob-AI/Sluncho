package net.asteasolutions.cinusuidi.sluncho.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileSystemDocumentRepository implements IDocumentRepository {
	private final String DocumentRootDirectory = "documents";
	
	public String[] getDocumentsRefs() throws FileNotFoundException {
		File documentRoot = new File(DocumentRootDirectory);
		if (!documentRoot.exists()) {
			throw new FileNotFoundException("Cannot find document root: " + DocumentRootDirectory);
		}
		
		File[] files = documentRoot.listFiles();
		String[] documentPaths = new String[files.length];
		for(int i = 0; i < files.length; i++) {
			documentPaths[i] = files[i].getPath();
		}
		return documentPaths;
	}
	
	public IndexableDocument getDocument(String ref) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(ref));
		IndexableDocument doc = new IndexableDocument();
		doc.content = new String(encoded, Charset.defaultCharset());
		doc.title = ref;
		return doc;
	}
}

