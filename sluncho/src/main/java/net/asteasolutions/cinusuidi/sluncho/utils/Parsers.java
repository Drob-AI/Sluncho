package net.asteasolutions.cinusuidi.sluncho.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public final class Parsers {
	public static Date parseDate(String dateString) {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd"); 
	    Date startDate;
	    try {
	        startDate = df.parse(dateString);
	        return startDate;
	    } catch (ParseException e) {
	        e.printStackTrace();
	        return new Date();
	    }
		
	}
}
