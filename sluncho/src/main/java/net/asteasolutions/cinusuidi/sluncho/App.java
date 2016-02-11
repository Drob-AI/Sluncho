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
import net.asteasolutions.cinusuidi.sluncho.documentIndex.IdentityDocumentParser;
import net.asteasolutions.cinusuidi.sluncho.documentIndex.QuestionAnswer;
import net.asteasolutions.cinusuidi.sluncho.facade.MongoDBFacade;
import net.asteasolutions.cinusuidi.sluncho.model.Question;
import net.asteasolutions.cinusuidi.sluncho.oneOutValidation.OneOutValidation;
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

        //calculates probability for 3-grams
        ThreeGramProbabilityRepo.loadProbability();
         
        //saves information from xml file to the database
        //change the file location
        
<<<<<<< HEAD
        QuestionRepository.extractAllQuestions();
        QuestionRepository.extractAllLabels();
        OneOutValidation testUnit = new OneOutValidation();
        
        testUnit.runDoc2vecClassifierrRandomTest();
//        testUnit.runDoc2vecClassifierFullTest();
        Doc2VecGroupClassifier.reset();
        Doc2VecGroupClassifier.train();   
 		
=======
        Doc2VecGroupClassifier.train();
        
        //Create document using identity
        IdentityDocumentParser idParser = new IdentityDocumentParser();

        //Create new repository for reading document from file system
        QuestionRepository repo = QuestionRepository.Instance();

        //Create new indexer for parsed documents
        DocumentIndexer questionIndexer = new DocumentIndexer(repo, idParser);
        questionIndexer.indexAll();
        questionIndexer.close();

>>>>>>> b29363dfdca1943c545404d65a325333b1ef6057
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