package net.asteasolutions.cinusuidi.sluncho.data;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

public class BotQuestion {
	private String text;

	public String getText() {
		return text;
	}

	public BotQuestion(String json) {
		text = json;
	}

	public String toJSON() {
		try {
			return new JSONObject().put("text", text).put("status", 200).toString();
		} catch (JSONException e) {
			e.printStackTrace();
			return "{\"status\":500,\"error\":\"Internal Server Error\",\"exception\":\"org.json.JSONException\"}";
		}
	}
}
