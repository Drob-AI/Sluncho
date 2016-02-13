package net.asteasolutions.cinusuidi.sluncho.documentIndex;

import gate.creole.ResourceInstantiationException;
import gate.persist.PersistenceException;
import gate.util.Out;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.asteasolutions.cinusuidi.sluncho.App;
import net.asteasolutions.cinusuidi.sluncho.bot.Query;
import net.asteasolutions.cinusuidi.sluncho.bot.errorCorrection.POSPipelineProcessor;

import net.asteasolutions.cinusuidi.sluncho.data.IDocumentRepository;
import net.asteasolutions.cinusuidi.sluncho.data.IndexableDocument;
import net.asteasolutions.cinusuidi.sluncho.data.LegalDocument;
import net.asteasolutions.cinusuidi.sluncho.questionparser.QueryToken;
import net.asteasolutions.cinusuidi.sluncho.questionparser.QuestionParser;
import net.asteasolutions.cinusuidi.sluncho.questionparser.exception.QuestionParserException;
import org.apache.commons.lang.StringUtils;

public class DocumentIndexer {
    private IDocumentRepository repo;
    private IDocumentParser parser;
    private QuestionAnswerBuilder qaBuilder;
    private QuestionParser questionParser;
    private DocumentIndex idx;
    private FullTextIndexer fullIdx;
    private boolean buildedNow = false;

    public DocumentIndexer(IDocumentRepository repo, IDocumentParser parser) {
        this.repo = repo;
        this.parser = parser;
        this.qaBuilder = new QuestionAnswerBuilder();
        this.questionParser = App.questionParser;
        this.fullIdx = new FullTextIndexer();
        this.idx = new DocumentIndex();
    }
    
    public void initIndexes() throws IOException {
    	buildedNow = true;
    	idx.init();
        fullIdx.init();
    }

    public void indexAll() throws QuestionParserException {
        try {
        	if(!idx.isBuild() || !fullIdx.isBuild()) {
        		initIndexes();
	        	
	            String[] ref = repo.getDocumentsRefs();
	            for(int i = 0; i < ref.length; i++) {
	                index(ref[i]);
	            }
                    idx.close();
                    fullIdx.close();
        	}
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public DocumentIndexEntry getIndexEntry(Query analyzedSentance) {
        DocumentIndexEntry entr = new DocumentIndexEntry();
        
        POSPipelineProcessor processor = new POSPipelineProcessor();
        analyzedSentance = processor.simpleAnnotate(analyzedSentance);

        entr.additionGroup = "";
        entr.predicate = "";
        entr.subject = "";
        for(QueryToken addition: analyzedSentance.additionGroup) {
            entr.additionGroup += " " + addition.getOriginalText();
        }
        for(QueryToken predicate: analyzedSentance.predicateGroup) {
            entr.predicate += " " + predicate.getOriginalText();
        }
        for(QueryToken subject: analyzedSentance.subjectGroup) {
            entr.subject += " " + subject.getOriginalText();
        }
        
        return entr;
    }
    
    public void indexSentance(String questionId, String sentance, String type) {
        try {
            //String[] sentences = StringUtils.split(qa.answer, ".!?");
            Query tokenizedSentance = questionParser.parse(sentance);
            DocumentIndexEntry entry = getIndexEntry(tokenizedSentance);
            entry.questionId = questionId;
            entry.type = type;
            Out.println("(" + entry.questionId + "," + entry.type + "," + entry.subject + "," + entry.predicate + "," + entry.additionGroup + ")");
            idx.index(entry);
        } catch (QuestionParserException ex) {
            ex.printStackTrace();
        }
    }
    
    public void indexQuestionPart(String questionId, String fullText, String type) {
        String[] sentences = StringUtils.split(fullText, ".!?");
        for(int i = 0; i < sentences.length; i++) {
            indexSentance(questionId, sentences[i], type);
        }
    }

    public void index(String ref) throws QuestionParserException {
        try {
            IndexableDocument doc = repo.getDocument(ref);
            LegalDocument legDoc = parser.parse(ref, doc);
            ArrayList<QuestionAnswer> qas = qaBuilder.split(legDoc);
            Iterator<QuestionAnswer> iter = qas.iterator();

            Out.println();
            Out.println("#Parsing Doc ref:" + ref);
            while(iter.hasNext()) {
                QuestionAnswer qa = iter.next();

                indexQuestionPart(ref, qa.answer, "0");
//              indexQuestionPart(questionId, qa.context, "1");
                indexQuestionPart(ref, qa.question, "2");
                fullIdx.index(ref, qa.question, qa.answer, qa.context);

                
                
            }
            
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public void close() {
        try {
        	if (buildedNow) {
        		idx.close();
            	fullIdx.close();
        	}
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
