/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.asteasolutions.cinusuidi.sluncho.bot.questionHandlers;

import net.asteasolutions.cinusuidi.sluncho.bot.questionRecognizers.IQuestionRecognizer;
import net.asteasolutions.cinusuidi.sluncho.bot.questionRecognizers.SemanticRecognizer;
import net.asteasolutions.cinusuidi.sluncho.data.IDataSource;
import net.asteasolutions.cinusuidi.sluncho.data.SemanticSource;

/**
 *
 * @author mihail
 */
public class SemanticHandler implements IQuestionHandler {

    @Override
    public IQuestionRecognizer getQuestionRecognizer() {
        return new SemanticRecognizer();
    }

    @Override
    public IDataSource getDataSource() {
        return new SemanticSource();
    }
    
}
