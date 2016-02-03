package net.asteasolutions.cinusuidi.sluncho.bot;

import java.io.File;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.Properties;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Corpus;
import gate.Document;
import gate.Factory;
import gate.FeatureMap;
import gate.Gate;
import gate.creole.ConditionalSerialAnalyserController;
import gate.util.GateException;
import gate.util.Out;
import gate.util.persistence.PersistenceManager;

public class SlunchoBot {
	void answer(String question) throws GateException, IOException {
		// initialise the GATE library
//		  File gateHome = new File("~/GATE_Developer_8.1");
//		  Gate.setGateHome(gateHome);
		  Out.prln("Initialising GATE...");
		  Gate.init();
		  Out.prln("...GATE initialised");
		
		  String documentString = "This is my document string. Parse this motherfucker.";
		
		  // create a GATE corpus and add a document for each command-line
		  // argument
		  Corpus corpus = Factory.newCorpus("StandAloneAnnie corpus");
		  Document document = Factory.newDocument(documentString);
		  corpus.add(document);
		// load an application from a gapp file
		ConditionalSerialAnalyserController myapp = (ConditionalSerialAnalyserController) PersistenceManager
				.loadObjectFromFile(new File("src/main/resources/gate/application.xgapp"));

		// set a corpus for the app
		myapp.setCorpus(corpus);

		// execute the application
		myapp.execute();		  

		// print results
		AnnotationSet gateAnnotations = document.getAnnotations();
		for (Annotation annotation : gateAnnotations) {
			FeatureMap allFeatures = annotation.getFeatures();
			for (Entry<Object,Object> feature : allFeatures.entrySet()) {
				System.out.println("feature type: " + feature.getKey().toString() +
							" " + ", value: " + feature.getValue().toString());
			}
		}
	}
}
