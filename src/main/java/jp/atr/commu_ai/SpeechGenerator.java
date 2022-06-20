package jp.atr.commu_ai;

import java.util.HashMap;
import java.util.Map;

import javax.json.JsonObject;

/**
 * 音声合成サーバに接続し音声合成指令を行うクラス
 *
 */
public class SpeechGenerator extends AbstractCommunicator {
	private boolean isSpeaking = false;
	

	public SpeechGenerator(String hostname, int port) {
		super(hostname, port);
	}

	
	/**
	 * 発話指令送信メソッド。発話完了するまで待機する
	 * @param speechContent 発話内容
	 */
	public void say(String speechContent) {
		if(!client.isConnected())
			return;
		String command = createSpeechCommand(speechContent);
		isSpeaking = true;
		sendLine(command);
		while(isSpeaking) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
		}
	}
	public boolean isSpeaking() {
		return isSpeaking;
	}
	

	private String createSpeechCommand(String speechContent) {
		Map<String, Object> map = new HashMap<>();
		map.put("engine", "HOYA");
		map.put("speaker", "hikari");
		map.put("text", speechContent);
		//map.put("emotion", "happiness");
		//map.put("emotion_level", 2);
		return JsonUtils.jsonToString(JsonUtils.createJsonObject(map));
	}
	private String parseResult(String received) {
		JsonObject retJson = JsonUtils.stringToJsonObject(received);
		String result = retJson.getString("result");
		return result;
	}

	@Override
	protected void onReceived(String message) {
		String result = parseResult(message);
		switch(result) {
		case "success-start":
			break;
		case "success-end":
			isSpeaking = false;
			break;
		case "failed":
			System.out.println("speech generate process failed.");
			isSpeaking = false;
			break;
		default:
			isSpeaking = false;
			break;
		}
	}
	
	
}
