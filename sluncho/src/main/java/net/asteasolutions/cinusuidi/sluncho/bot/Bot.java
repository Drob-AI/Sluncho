package net.asteasolutions.cinusuidi.sluncho.bot;

import gate.Gate;
import gate.creole.ResourceInstantiationException;
import gate.persist.PersistenceException;
import gate.util.GateException;
import gate.util.Out;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.asteasolutions.cinusuidi.sluncho.App;
import net.asteasolutions.cinusuidi.sluncho.bot.errorCorrection.POSPipelineProcessor;
import net.asteasolutions.cinusuidi.sluncho.bot.questionRecognizers.Doc2VecGroupClassifier;
import net.asteasolutions.cinusuidi.sluncho.data.FileSystemDocumentRepository;
import net.asteasolutions.cinusuidi.sluncho.data.IDocumentRepository;
import net.asteasolutions.cinusuidi.sluncho.documentIndex.DocumentIndexer;
import net.asteasolutions.cinusuidi.sluncho.documentIndex.HtmlDocumentParser;

import org.json.JSONException;
import org.json.JSONObject;

import net.asteasolutions.cinusuidi.sluncho.questionparser.QueryToken;
import net.asteasolutions.cinusuidi.sluncho.questionparser.QuestionParser;
import net.asteasolutions.cinusuidi.sluncho.questionparser.exception.QuestionParserException;

//TODO: this should be emitted from the pipeline
// and will be just a POCO object with the annotations
public class Bot {
	public static String getAnswer(String question) throws QuestionParserException {
            CompositeQuery query = App.questionParser.parseAll(question);
            
            List<QuestionResult> result = QueryAnswerer.getQueryResult(query);
            if(result != null) {
                return "niceee, but probably something should print something here";
            }
            return "I could not find an answer to your question. If you want to send your question to the umbrella team for answering click :thumbsup:";
	}
}
