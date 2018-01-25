package com.sinosoft.mobileshop.util;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

import com.google.gson.Gson;
import com.google.gson.JsonArray;

public class JsonUtil {

	public static Gson gson = new Gson();

	public static <T> List<T> jsonToBeanList(JSONArray jsonArray, Class T) {
		if (jsonArray == null || jsonArray.length() <= 0) {
			return null;
		}
		List<T> objList = new ArrayList<T>();
		try {
			for (int i = 0; i < jsonArray.length(); i++) {
				String jsonStr = jsonArray.get(i).toString();
				objList.add((T) gson.fromJson(jsonStr, T));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return objList;
	}
	
	public static <T> List<T> strToBeanList(String str, Class T) {
		if (str == null || "".equals(str)) {
			return null;
		}
		JSONArray jsonArray = null;
		List<T> objList = new ArrayList<T>(); ;
		try {
			jsonArray = new JSONArray(str);
			new ArrayList<T>();
			for (int i = 0; i < jsonArray.length(); i++) {
				String jsonStr = jsonArray.get(i).toString();
				objList.add((T) gson.fromJson(jsonStr, T));
			}
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		return objList;
	}
	
	
	public static Object strToBean(String str, Class T) {
		if (str == null || "".equals(str)) {
			return null;
		}
		Object bean = null;
		try {
			bean = gson.fromJson(str, T);
		} catch(Exception e) {
		}
		return bean;
	}
}
