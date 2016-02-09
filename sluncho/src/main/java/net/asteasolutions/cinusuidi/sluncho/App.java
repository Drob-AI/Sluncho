package net.asteasolutions.cinusuidi.sluncho;

import gate.Gate;
import gate.creole.ResourceInstantiationException;
import gate.persist.PersistenceException;
import gate.util.GateException;
import gate.util.Out;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import net.asteasolutions.cinusuidi.sluncho.bot.errorCorrection.NamedEntity;
import net.asteasolutions.cinusuidi.sluncho.bot.errorCorrection.NamedEntityIndexer;
import net.asteasolutions.cinusuidi.sluncho.bot.errorCorrection.NamedEntitySearcher;
import net.asteasolutions.cinusuidi.sluncho.data.FileSystemDocumentRepository;
import net.asteasolutions.cinusuidi.sluncho.data.IDocumentRepository;
import net.asteasolutions.cinusuidi.sluncho.documentIndex.DocumentIndex;
import net.asteasolutions.cinusuidi.sluncho.documentIndex.DocumentIndexer;
import net.asteasolutions.cinusuidi.sluncho.documentIndex.HtmlDocumentParser;
import net.asteasolutions.cinusuidi.sluncho.facade.MongoDBFacade;
import net.asteasolutions.cinusuidi.sluncho.questionparser.QuestionParser;
import net.asteasolutions.cinusuidi.sluncho.questionparser.exception.QuestionParserException;
import net.asteasolutions.cinusuidi.sluncho.utils.ThreeGramProbabilityRepo;
import net.asteasolutions.cinusuidi.sluncho.utils.XmlParse;
import org.bson.Document;

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
		Out.prln(System.getProperty("gate.home", "/home/marmot/GATE_Developer_8.1"));
		System.setProperty("wordnet.database.dir", "/home/marmot/Downloads/WordNet-3.0/dict");
		System.setProperty("gate.astea.app.home", "/home/marmot/Downloads/sluncho/resources/gate");
		Properties props = System.getProperties();
		props.setProperty("gate.home", "/home/marmot/GATE_Developer_8.1");
        
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
                String xmlFilePath = "/home/marmot/Downloads/SEMEVAL/SEMEVAL/semeval2016-task3-cqa-ql-traindev-v3.2/v3.2/dev/";
                String xmlFileName = "SemEval2016-Task3-CQA-QL-dev-with-multiline.xml";
                
                MongoDBFacade mongoConnection = new MongoDBFacade();
                Document xmlDocumentInDatabase = mongoConnection.getXmlDocument(xmlFileName);              
                if(xmlDocumentInDatabase == null){
                    XmlParse parser = new XmlParse(xmlFilePath, xmlFileName);
                    parser.parseFileAndSaveToDatabase();
                }
		
		SpringApplication.run(App.class, args);
	}
}
