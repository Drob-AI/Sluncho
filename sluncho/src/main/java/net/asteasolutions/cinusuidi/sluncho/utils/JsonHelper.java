package net.asteasolutions.cinusuidi.sluncho.utils;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public final class JsonHelper {
	public static ArrayList<JSONObject> JSONparseArray(String jsonArray) throws JSONException { 
		ArrayList<JSONObject> result = new ArrayList<JSONObject>();
		JSONArray myjson = new JSONArray(jsonArray);
		for(int i = 0; i < myjson.length(); i++){
			JSONObject obj = new JSONObject(myjson.get(i).toString());
			result.add(obj);
		}
		return result;
	}
}
