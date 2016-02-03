package net.asteasolutions.cinusuidi.sluncho.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.bson.Document;

import net.asteasolutions.cinusuidi.sluncho.facade.MongoDBFacade;

public class MongoDocumentRepository implements IDocumentRepository {

	@Override
	public String[] getDocumentsRefs() throws IOException {
		MongoDBFacade dbFacade = new MongoDBFacade();
		ArrayList<Document> docIds = dbFacade.getAllDocIds();
		String[] result = new String[docIds.size()];
		Iterator<Document> docIter = docIds.iterator();
		int idx = 0;
		while(docIter.hasNext()) {
			Document doc = docIter.next();
			result[idx] = doc.getObjectId("_id").toString();
			idx++;
		}
		// TODO Auto-generated method stub
		return result;
	}

	@Override
	public IndexableDocument getDocument(String ref) throws IOException {
		MongoDBFacade dbFacade = new MongoDBFacade();
		Document doc = dbFacade.getDocument(ref);
		IndexableDocument iDoc = new IndexableDocument();
		if(doc != null) {			
			iDoc.content = doc.getString("question");
			iDoc.title = doc.getString("answer");
		}
		return iDoc;
	}

}
