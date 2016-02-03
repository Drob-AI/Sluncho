package net.asteasolutions.cinusuidi.sluncho.web.controllers;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.asteasolutions.cinusuidi.sluncho.bot.Bot;
import net.asteasolutions.cinusuidi.sluncho.bot.Query;
import net.asteasolutions.cinusuidi.sluncho.bot.QueryAnswerer;
import net.asteasolutions.cinusuidi.sluncho.bot.QuestionResult;
import net.asteasolutions.cinusuidi.sluncho.data.BotQuestion;
import net.asteasolutions.cinusuidi.sluncho.data.IDocumentRepository;
import net.asteasolutions.cinusuidi.sluncho.data.MongoDocumentRepository;
import net.asteasolutions.cinusuidi.sluncho.documentIndex.DocumentIndexer;
import net.asteasolutions.cinusuidi.sluncho.documentIndex.IDocumentParser;
import net.asteasolutions.cinusuidi.sluncho.documentIndex.IdentityDocumentParser;
import net.asteasolutions.cinusuidi.sluncho.facade.MongoDBFacade;
import net.asteasolutions.cinusuidi.sluncho.questionparser.QuestionParser;
import net.asteasolutions.cinusuidi.sluncho.questionparser.exception.QuestionParserException;
import net.asteasolutions.cinusuidi.sluncho.web.models.BotResponse;
import org.json.JSONException;
import org.json.JSONObject;

import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.MediaType;

@RestController
public class BotQuestionController {
	@RequestMapping("/qqq")
    public BotQuestion greeting(@RequestParam(value="question", defaultValue="World") String question) {
        String answer;
		try {
			answer = Bot.getAnswer(question);
			return new BotQuestion(answer);
		} catch (QuestionParserException e) {
			// TODO Auto-generated catch block
			return new BotQuestion("I could not find an answer to your question.");
		}
    }
	
    @RequestMapping(value = "/question", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody String question(@RequestBody BotQuestion question) {
        System.out.printf("Asking \"%s\"\n", question.getText());
        String answer;
        try {
            answer = Bot.getAnswer(question.getText());
            JSONObject obj= new JSONObject();
            obj.put("text", answer);
            return obj.toString();
        } catch (QuestionParserException e) {
        // TODO Auto-generated catch block
        } catch (JSONException ex) {
            Logger.getLogger(BotQuestionController.class.getName()).log(Level.SEVERE, null, ex);
        }
        JSONObject internalError= new JSONObject();
        return "{text:\"An error occured when parsing your question, please report it to the relevant authorities.\"";
    }

    @RequestMapping(value = "/downvote", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody String downvote(@RequestBody BotQuestion question) {
    	MongoDBFacade mongoConnection = new MongoDBFacade();
    	mongoConnection.createDocument(question.getText());
        System.out.printf("Downvoting \"%s\"\n", question.getText());
        return "{\"status\":200}";
    }
    
    @RequestMapping(value = "/indexDoc", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody String indexing(@RequestBody String id) {
    	if(id == null) {
    		return "{\"status\":400}";
    	}
    	
    	try {
    		MongoDBFacade mongoConnection = new MongoDBFacade();
        	if(mongoConnection.getDocument(id) != null) {
        		DocumentIndexer indexer = new DocumentIndexer(new MongoDocumentRepository(), new IdentityDocumentParser());
        		indexer.initIndexes();
    			indexer.index(id);
        	}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (QuestionParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
        return "{\"status\":200}";
    }
    
}
