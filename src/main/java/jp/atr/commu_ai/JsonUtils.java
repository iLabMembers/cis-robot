package jp.atr.commu_ai;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;
import javax.json.JsonWriter;

public class JsonUtils {
	public static JsonObject createJsonObject(Map<String, Object> map) {
		JsonObjectBuilder builder = Json.createObjectBuilder();
		for (Entry<String, Object> entry : map.entrySet()) {
			if(entry.getValue() instanceof String)
				builder.add(entry.getKey(), (String)entry.getValue());
			else if(entry.getValue() instanceof Integer)
				builder.add(entry.getKey(), (Integer)entry.getValue());
			else if(entry.getValue() instanceof Double)
				builder.add(entry.getKey(), (Double)entry.getValue());
			else if(entry.getValue() instanceof JsonObject)
				builder.add(entry.getKey(), (JsonObject)entry.getValue());
			else if(entry.getValue() instanceof List<?>)
				builder.add(entry.getKey(), createJsonArray((List<?>)entry.getValue()));
		}
		return builder.build();
	}
	/*public static JsonArray createJsonArray(List<Object> list) {
		JsonArrayBuilder builder = Json.createArrayBuilder();
		for (Object data : list) {
			if(data instanceof String)
				builder.add((String)data);
			else if(data instanceof Integer)
				builder.add((Integer)data);
			else if(data instanceof Double)
				builder.add((Double)data);
		}
		return builder.build();
	}*/
	public static JsonArray createJsonArray(List<?> list) {
		JsonArrayBuilder builder = Json.createArrayBuilder();
		for (Object data : list) {
			if(data instanceof String)
				builder.add((String)data);
			else if(data instanceof Integer)
				builder.add((Integer)data);
			else if(data instanceof Double)
				builder.add((Double)data);
		}
		return builder.build();
	}

	public static String jsonToString(JsonObject jsonObject) {
		StringWriter stWriter = new StringWriter();
		JsonWriter jsonWriter = Json.createWriter(stWriter);
		jsonWriter.write(jsonObject);
		jsonWriter.close();
		String jsonString = stWriter.toString();
		return jsonString;
	}
	public static String jsonToString(JsonArray jsonArray) {
		StringWriter stWriter = new StringWriter();
		JsonWriter jsonWriter = Json.createWriter(stWriter);
		jsonWriter.write(jsonArray);
		jsonWriter.close();
		String jsonString = stWriter.toString();
		return jsonString;
	}

	public static String mapToJsonString(Map<String, Object> map) {
		return jsonToString(createJsonObject(map));
	}
	public static String listToJsonString(List<Object> list) {
		return jsonToString(createJsonArray(list));
	}
	
	

	public static JsonObject stringToJsonObject(String jsonString) {
		JsonReader jsonReader = Json.createReader(new StringReader(jsonString));
		JsonObject jsonObject = jsonReader.readObject();
		return jsonObject;
	}

	public static Map<String, Object> stringToMap(String jsonString) {
		JsonObject jsonObject = stringToJsonObject(jsonString);
		Map<String, Object> map = new HashMap<>();
		for (Entry<String, JsonValue> entry : jsonObject.entrySet()) {
			if(entry.getValue().getValueType() == ValueType.STRING)
				map.put(entry.getKey(), jsonObject.getString(entry.getKey()));
			else if(entry.getValue().getValueType() == ValueType.NUMBER)
				map.put(entry.getKey(), jsonObject.getInt(entry.getKey()));
			else if(entry.getValue().getValueType() == ValueType.ARRAY)
				map.put(entry.getKey(), jsonArrayToArray(jsonObject.getJsonArray(entry.getKey())));
		}
		return map;
	}
	public static List<String> stringToArray(String jsonString) {
		JsonReader jsonReader = Json.createReader(new StringReader(jsonString));
		JsonArray jsonArray = jsonReader.readArray();
		return jsonArrayToArray(jsonArray);
	}
	public static List<String> jsonArrayToArray(JsonArray jsonArray) {
		List<String> list = new LinkedList<>();
		for (JsonValue value : jsonArray) {
			list.add(value.toString().replace("\"", ""));
		}
		return list;
	}
	
	
	public static boolean isProperJson(String jsonString) {
		if(!jsonString.startsWith("{") || !jsonString.endsWith("}"))
			return false;
		int startCount = countChar(jsonString, '{');
		int finishCount = countChar(jsonString, '}');
		return startCount == finishCount;
	}
	private static int countChar(String string, char countChar) {
		int count = 0;
		for (int i = 0; i < string.length(); i++) {
			if (string.charAt(i) == countChar)
				count++;
		}
		return count;
	}
}

