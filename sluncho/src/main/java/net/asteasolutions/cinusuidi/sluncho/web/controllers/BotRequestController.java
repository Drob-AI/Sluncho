package net.asteasolutions.cinusuidi.sluncho.web.controllers;


import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;


import net.asteasolutions.cinusuidi.sluncho.data.HostnameSource;
import net.asteasolutions.cinusuidi.sluncho.data.IDocumentRepository;
import net.asteasolutions.cinusuidi.sluncho.data.MongoDocumentRepository;
import net.asteasolutions.cinusuidi.sluncho.data.ipSource;
import net.asteasolutions.cinusuidi.sluncho.documentIndex.DocumentIndexer;
import net.asteasolutions.cinusuidi.sluncho.documentIndex.HtmlDocumentParser;
import net.asteasolutions.cinusuidi.sluncho.documentIndex.IDocumentParser;
import net.asteasolutions.cinusuidi.sluncho.documentIndex.IdentityDocumentParser;
import net.asteasolutions.cinusuidi.sluncho.questionparser.exception.QuestionParserException;
import net.asteasolutions.cinusuidi.sluncho.web.models.BotResponse;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BotRequestController {
    private static final String template = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();
    
    @RequestMapping("/bot")
    public BotResponse greeting(@RequestParam(value="name", defaultValue="World") String name) {
        try {
            IDocumentRepository repository = new MongoDocumentRepository();
            IDocumentParser parser = new IdentityDocumentParser();
            DocumentIndexer idxer = new DocumentIndexer(repository, parser);
            idxer.indexAll();
            return new BotResponse(counter.incrementAndGet(), String.format(template, "gggg"));
        } catch (QuestionParserException ex) {
            Logger.getLogger(BotRequestController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new BotResponse(counter.incrementAndGet(), String.format(template, "gggg"));
    }
    
    @RequestMapping("/bot1")
    public BotResponse dummy(@RequestParam(value="name", defaultValue="World") String name) {
    	ipSource src = new ipSource();
		return new BotResponse(counter.incrementAndGet(), String.format(template, src.getDocument("havramov")));
    }
}
