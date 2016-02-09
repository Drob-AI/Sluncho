package net.asteasolutions.cinusuidi.sluncho;

import java.util.Properties;

import gate.util.Out;

public class BeforeStartConfig {
	public static void configSystemProperties() {
		Out.prln(System.getProperty("gate.home", "/home/marmot/GATE_Developer_8.1"));
        System.setProperty("wordnet.database.dir", "/home/marmot/Downloads/WordNet-3.0/dict");
        System.setProperty("gate.astea.app.home", "/home/marmot/Downloads/sluncho/resources/gate");
        System.setProperty("gate.astea.app.home", "/home/marmot/Downloads/sluncho/resources/gate");
        Properties props = System.getProperties();
        
        String xmlFilePath = "/home/hary/D/sluncho/sluncho/documents/";
        String xmlFileName = "SemEval2016-Task3-CQA-QL-dev-with-multiline.xml";
      
        System.setProperty("dataPath", xmlFilePath);
        System.setProperty("dataFileName", xmlFileName);
        
        
        props.setProperty("gate.home", "/home/marmot/GATE_Developer_8.1");
	}
}
