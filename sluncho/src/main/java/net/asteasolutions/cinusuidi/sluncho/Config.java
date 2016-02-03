package net.asteasolutions.cinusuidi.sluncho;

public final class Config 
{

	public static String getConfigProperty(String name) {
		return System.getProperty(name);
	}
	
	public static String ipToHostnameServiceUrl() {
		return Config.getConfigProperty("sluncho.source.ipToHostname");
	}
	
	public static String hostnameToIpServiceUrl() {
		return Config.getConfigProperty("sluncho.source.hostnameToIp");
	}
	
	public static String availabilityInfoServiceUrl() {
		String url = "http://localhost:6969";
		return url;
		//		return Config.getConfigProperty(url);
	}
}
