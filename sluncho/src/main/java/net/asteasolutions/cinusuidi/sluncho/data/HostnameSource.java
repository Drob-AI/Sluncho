package net.asteasolutions.cinusuidi.sluncho.data;

import java.io.IOException;

import net.asteasolutions.cinusuidi.sluncho.Config;
import net.asteasolutions.cinusuidi.sluncho.utils.JavaHTTPClient;

public class HostnameSource implements IDataSource {
	public String getDocument(String docId) {
		try {
			return JavaHTTPClient.get(Config.ipToHostnameServiceUrl(), docId);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "error";
		}
	}
}
