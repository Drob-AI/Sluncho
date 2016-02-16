/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.asteasolutions.cinusuidi.sluncho.bot;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author mihail
 */
public class CompositeQuery extends Query {

    public CompositeQuery() {
        super();
    }
    public CompositeQuery(Query get) {
        super(get);
    }
    public CompositeQuery(CompositeQuery get) {
        super(get);
    }
    public String getFullText() {
        String result = "";
        for(Query sentence: sentences) {
            result += sentence.originalText;
        }
        return result;
    }
    public List<Query> sentences = new ArrayList<>();
}
