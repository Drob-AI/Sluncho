package net.asteasolutions.cinusuidi.sluncho;

import gate.Gate;
import gate.util.GateException;
import gate.util.Out;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

import net.asteasolutions.cinusuidi.sluncho.BeforeStartConfig;
import net.asteasolutions.cinusuidi.sluncho.bot.Bot;
import net.asteasolutions.cinusuidi.sluncho.bot.questionRecognizers.Doc2VecGroupClassifier;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import net.asteasolutions.cinusuidi.sluncho.data.FileSystemDocumentRepository;
import net.asteasolutions.cinusuidi.sluncho.data.QuestionRepository;
import net.asteasolutions.cinusuidi.sluncho.documentIndex.DocumentIndexer;
import net.asteasolutions.cinusuidi.sluncho.documentIndex.HtmlDocumentParser;
import net.asteasolutions.cinusuidi.sluncho.facade.MongoDBFacade;
import net.asteasolutions.cinusuidi.sluncho.model.Question;
import net.asteasolutions.cinusuidi.sluncho.questionparser.QuestionParser;
import net.asteasolutions.cinusuidi.sluncho.questionparser.exception.QuestionParserException;
import net.asteasolutions.cinusuidi.sluncho.utils.ThreeGramProbabilityRepo;
import net.asteasolutions.cinusuidi.sluncho.utils.XmlParse;

/**
 * Hello world!
 *
 */
@Configuration
@ComponentScan
@EnableAutoConfiguration
public class App 
{
    public static QuestionParser questionParser = new QuestionParser();
    public static void main(String args[]) throws IOException, URISyntaxException, GateException, QuestionParserException {
    	BeforeStartConfig.configSystemProperties();

        //fetch data and train name entity corrector index

        // ... what do you think?
        Gate.init();
        //initialize ANNIE and Standford parser
        questionParser.init();
        //Create new document parser for html documents
        HtmlDocumentParser htmlParser = new HtmlDocumentParser();

        //Create new repository for reading document from file system
        FileSystemDocumentRepository fsDocRepo = new FileSystemDocumentRepository();

        //Create new indexer for parsed documents
        DocumentIndexer fileIndexer = new DocumentIndexer(fsDocRepo, htmlParser);
        fileIndexer.indexAll();
        fileIndexer.close();

        //calculates probability for 3-grams
        ThreeGramProbabilityRepo.loadProbability();
         
        //saves information from xml file to the database
        //change the file location

        MongoDBFacade mongoConnection = new MongoDBFacade();
        List<Question> originalQuestions = mongoConnection.getAllOriginalQuestions();              
        if(originalQuestions.isEmpty()){
            XmlParse parser = new XmlParse(System.getProperty("dataPath"), System.getProperty("dataFileName"));
            parser.parseFileAndSaveToDatabase();
            originalQuestions = mongoConnection.getAllOriginalQuestions(); 
        } 
        QuestionRepository.setOriginalQuestions(originalQuestions);
        
        Doc2VecGroupClassifier.train();   
 		
        Scanner s = new Scanner(System.in);
        while (true) {
	        String question = s.nextLine();
	        String answer;
	        try {
	            answer = Bot.getAnswer(question);
	            System.out.println(answer);
	        } catch (QuestionParserException e) {
	        // TODO Auto-generated catch block
	        }
        }
//        s.close();
    }
}