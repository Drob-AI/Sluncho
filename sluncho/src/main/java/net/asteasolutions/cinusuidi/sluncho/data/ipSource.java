package net.asteasolutions.cinusuidi.sluncho.data;

import java.io.IOException;

import net.asteasolutions.cinusuidi.sluncho.Config;
import net.asteasolutions.cinusuidi.sluncho.utils.JavaHTTPClient;

public final class ipSource implements IDataSource{

	@Override
	public String getDocument(String docId) {
		try {
			return JavaHTTPClient.get(Config.availabilityInfoServiceUrl(), docId);
		} catch (IOException e) {
			e.printStackTrace();
			return "error";
		}
	}

}
