package jp.atr.commu_ai;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;

/**
 * 顔認識システムの起動しているROSに接続するクラス
 *
 */
public class FaceRecognitionManager extends AbstractCommunicator {
	private String subscribeID = "JavaSample";
	private String topicName = "/camera/main_person_cam";
	private String subscribeMsg = "face_recognition/People";
	
	private int age = -1;
	private String gender = null;
	private String emotion = null;
	
	
	
	public FaceRecognitionManager(String hostname, int port){
		super(hostname, port);
		isRosClient = true;
	}

	/**
	 * 年齢取得。Face++限定
	 * @return
	 */
	public int getAge() {
		return age;
	}
	/**
	 * 性別取得。Face++限定
	 * @return
	 */
	public String getGender() {
		return gender;
	}
	/**
	 * 表情取得
	 * @return
	 */
	public String getEmotion() {
		return emotion;
	}
	
	
	
	public void start() {
		super.start();
		subscribeTopic();
	}
	public void stop() {
		unsubscribe();
		super.stop();
	}

	private void subscribeTopic() {
		Map<String, Object> map = new HashMap<>();
		map.put("op", "subscribe");
		map.put("topic", topicName);
		map.put("type", subscribeMsg);
		map.put("id", subscribeID);
		client.sendLine(JsonUtils.jsonToString(JsonUtils.createJsonObject(map)));
	}
	private void unsubscribe() {
		Map<String, Object> map = new HashMap<>();
		map.put("op", "unsubscribe");
		map.put("topic", topicName);
		map.put("id", subscribeID);
		client.sendLine(JsonUtils.jsonToString(JsonUtils.createJsonObject(map)));
	}
	
	

	@Override
	protected void onReceived(String message) {
		JsonObject jsonObject = JsonUtils.stringToJsonObject(message);
		JsonObject msgObject = jsonObject.getJsonObject("msg");
		JsonArray persons = msgObject.get("persons").asJsonArray();
		if(persons.isEmpty())
			return;
		Iterator<JsonValue> itt = persons.iterator();
		while(itt.hasNext()){
			JsonObject obj = itt.next().asJsonObject();
			ArrayList<String> emotionClasses = new ArrayList<String>();
			ArrayList<Double> emotionProbs = new ArrayList<Double>();
			boolean hasFace = false;
			double detectionConfidence = 0.0;
			boolean hasFacepp = false;
			JsonObject faceppObject = null;

			// 各データ参照
			for(Entry<String, JsonValue> entry : obj.entrySet()){
				//System.out.println(entry.getKey() +":"+entry.getValue());
				if(entry.getKey().equals("header")){
					JsonObject headerObj = entry.getValue().asJsonObject();
					JsonObject stamp = headerObj.get("stamp").asJsonObject();
					// 認識タイムスタンプ
					long secs = Long.valueOf(stamp.get("secs").toString());
					long nsecs = Long.valueOf(stamp.get("nsecs").toString());
				} 
				else if(entry.getKey().equals("emotion_classes")){
					// 表情認識分類クラス
					if(!entry.getValue().asJsonArray().isEmpty()){
						Iterator<JsonValue> itt_emotion = entry.getValue().asJsonArray().iterator();
						while(itt_emotion.hasNext())
							emotionClasses.add(itt_emotion.next().toString());
					}
				}
				else if(entry.getKey().equals("emotion_probs")){
					// 表情認識分類クラスの各probability
					if(!entry.getValue().asJsonArray().isEmpty()){
						Iterator<JsonValue> itt_emotion = entry.getValue().asJsonArray().iterator();
						while(itt_emotion.hasNext())
							emotionProbs.add(Double.valueOf(itt_emotion.next().toString()));
					}
				} else if(entry.getKey().equals("keypoints")){
					// 顔の各特徴点二次元座標
					JsonArray keypoints = entry.getValue().asJsonArray();
				} else if(entry.getKey().equals("has_face")){
					hasFace = Boolean.valueOf(entry.getValue().toString());
				} else if(entry.getKey().equals("face")){
					JsonObject face = entry.getValue().asJsonObject();
					// 顔検出の確信度
					detectionConfidence = Double.valueOf(face.get("detection_confidence").toString());
					// 顔の二次元矩形座標
					JsonArray bbox = face.get("bbox").asJsonArray();
				} else if(entry.getKey().equals("has_facepp_result")){
					hasFacepp = Boolean.valueOf(entry.getValue().toString());
				} else if(entry.getKey().equals("facepp_result")){
					faceppObject = entry.getValue().asJsonObject();
				}
			}

			String emotionMaxClass = "";
			double emotionMaxProb = -1.0;
			if(emotionClasses.size() == emotionProbs.size() && !emotionClasses.isEmpty()) {
				for(int i = 0; i < emotionClasses.size(); i++) {
					if(emotionMaxProb < emotionProbs.get(i)){
						emotionMaxClass = emotionClasses.get(i);
						emotionMaxProb = emotionProbs.get(i);
					}
				}
			}
			if(detectionConfidence > 0.5) {
				//System.out.println(String.format("emotion[%s(%f)]", emotionMaxClass.replace("\"", ""), emotionMaxProb));
				emotion = emotionMaxClass.replace("\"", "");
			}
			else
				emotion = null;
			
			// Face++認識結果
			if(hasFacepp && faceppObject != null) {
				int faceppAge = Integer.valueOf(faceppObject.get("age").toString());
				String faceppGender = faceppObject.get("gender").toString().replace("\"", "");
				//System.out.println(String.format("Face++ age[%d] gender[%s]", faceppAge, faceppGender));
				age = faceppAge;
				gender = faceppGender;
			}
		}
	}

	
	
}
