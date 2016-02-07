package net.asteasolutions.cinusuidi.sluncho;

import gate.Gate;
import gate.util.GateException;
import gate.util.Out;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Properties;
import java.util.Scanner;
import net.asteasolutions.cinusuidi.sluncho.bot.Bot;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import net.asteasolutions.cinusuidi.sluncho.data.FileSystemDocumentRepository;
import net.asteasolutions.cinusuidi.sluncho.documentIndex.DocumentIndexer;
import net.asteasolutions.cinusuidi.sluncho.documentIndex.HtmlDocumentParser;
import net.asteasolutions.cinusuidi.sluncho.questionparser.QuestionParser;
import net.asteasolutions.cinusuidi.sluncho.questionparser.exception.QuestionParserException;
import net.asteasolutions.cinusuidi.sluncho.utils.ThreeGramProbabilityRepo;

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
//      Ensure the following startup parameters are set for java VM (with -D options):
//      -Dgate.home=<absolute path to gate>
//      -Dwordnet.database.dir=<absolute path to wordnet database>
//      -Dgate.astea.app.home=<absolute project path>/resources/gate

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

        Scanner s = new Scanner(System.in);
        String question = s.nextLine();
        String answer;
        try {
            answer = Bot.getAnswer(question);
            System.out.println(answer);
        } catch (QuestionParserException e) {
        // TODO Auto-generated catch block
        }
        s.close();
    }
}