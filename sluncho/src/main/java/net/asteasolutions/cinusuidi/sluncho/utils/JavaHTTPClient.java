package net.asteasolutions.cinusuidi.sluncho.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import net.asteasolutions.cinusuidi.sluncho.Config;

public final class JavaHTTPClient {
	public static String get(String domain, String url) throws IOException {
		URL urlWrapper = new URL(domain + url);
        URLConnection yc = urlWrapper.openConnection();
        BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
        String inputLine, result = "";

        while ((inputLine = in.readLine()) != null) {
        	result += inputLine;
        }
        
        in.close();
        
		return result;
	}
}
