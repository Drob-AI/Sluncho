/*
 *  StandAloneAnnie.java
 *
 *
 * Copyright (c) 2000-2001, The University of Sheffield.
 *
 * This file is part of GATE (see http://gate.ac.uk/), and is free
 * software, licenced under the GNU Library General Public License,
 * Version 2, June1991.
 *
 * A copy of this licence is included in the distribution in the file
 * licence.html, and is also available at http://gate.ac.uk/gate/licence.html.
 *
 *  hamish, 29/1/2002
 *
 *  $Id: StandAloneAnnie.java,v 1.6 2006/01/09 16:43:22 ian Exp $
 */

package net.asteasolutions.cinusuidi.sluncho.bot;

import gate.Annotation;
import gate.Corpus;
import gate.CorpusController;
import gate.Gate;
import gate.util.GateException;
import gate.util.Out;
import gate.util.persistence.PersistenceManager;

import java.io.File;
import java.io.IOException;
import java.util.Vector;

/**
 * This class illustrates how to use ANNIE as a sausage machine
 * in another application - put ingredients in one end (URLs pointing
 * to documents) and get sausages (e.g. Named Entities) out the
 * other end.
 * <P><B>NOTE:</B><BR>
 * For simplicity's sake, we don't do any exception handling.
 */
public class Pipeline  {
	/** The Corpus Pipeline application to contain ANNIE */
	private CorpusController annieController;

	/**
	 * Initialise the ANNIE system. This creates a "corpus pipeline"
	 * application that can be used to run sets of documents through
	 * the extraction system.
	 */
	public void initAnnie() throws GateException, IOException {
		Out.prln("Initialising ANNIE...");

		// load the ANNIE application from the saved state in plugins/ANNIE
		File pluginsHome = Gate.getPluginsHome();
		File anniePlugin = new File(pluginsHome, "ANNIE");
		File annieGapp = new File(anniePlugin, "ANNIE_with_defaults.gapp");
		annieController = (CorpusController) PersistenceManager.loadObjectFromFile(annieGapp);

		Out.prln("...ANNIE loaded");
	} // initAnnie()

	/** Tell ANNIE's controller about the corpus you want to run on */
	public void setCorpus(Corpus corpus) {
		annieController.setCorpus(corpus);
	} // setCorpus

	/** Run ANNIE */
	public void execute() throws GateException {
		Out.prln("Running ANNIE...");
		annieController.execute();
		Out.prln("...ANNIE complete");
	} // execute()

	/**
	 *
	 */
	public static class SortedAnnotationList extends Vector {
		public SortedAnnotationList() {
			super();
		} // SortedAnnotationList
		
	    public boolean addSortedExclusive(Annotation annot) {
	    	Annotation currAnot = null;
	
	      // overlapping check
	    	for (int i=0; i<size(); ++i) {
	    		currAnot = (Annotation) get(i);
	    		if(annot.overlaps(currAnot)) {
	    			return false;
	    		} // if
	    	} // for
	
	    	long annotStart = annot.getStartNode().getOffset().longValue();
	    	long currStart;
	    	// insert
	    	for (int i=0; i < size(); ++i) {
	    		currAnot = (Annotation) get(i);
	    		currStart = currAnot.getStartNode().getOffset().longValue();
	    		if(annotStart < currStart) {
	    			insertElementAt(annot, i);
	    			/*
	           		Out.prln("Insert start: "+annotStart+" at position: "+i+" size="+size());
	           		Out.prln("Current start: "+currStart);
	    			 */
	    			return true;
	    		} // if
	    	} // for
	
	    	int size = size();
	    	insertElementAt(annot, size);
	    	//Out.prln("Insert start: "+annotStart+" at size position: "+size);
			return true;
	    } // addSorted
	} // SortedAnnotationList
} // class StandAloneAnnie
