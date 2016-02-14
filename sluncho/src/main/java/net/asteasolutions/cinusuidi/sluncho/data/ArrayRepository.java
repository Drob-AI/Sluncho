/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.asteasolutions.cinusuidi.sluncho.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.asteasolutions.cinusuidi.sluncho.model.Question;

/**
 *
 * @author mihail
 */
class ArrayRepository implements IDocumentRepository {
    List<Question> arr;
    public ArrayRepository(List<Question> arr) {
        this.arr = arr;
    }

 @Override
    public String[] getDocumentsRefs() throws IOException {
        ArrayList<String> refs = new ArrayList<String>() {};
        for (Question q: arr) {
            refs.add(q.getQuestionId());
        }
        return refs.toArray(new String[refs.size()]);
    }

    @Override
    public IndexableDocument getDocument(String ref) throws IOException {
        for (Question q: arr) {
            if(q.getQuestionId().equals(ref)) {
                IndexableDocument result = new IndexableDocument();
                result.groupId = q.getGroupId();
                result.title = q.getSubject();
                result.content = q.getBody();
                return result;
            }
        }
        return null;
    }
    
}
