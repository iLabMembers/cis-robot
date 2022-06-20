package jp.atr.commu_ai;

import java.util.HashMap;
import java.util.Map;

import javax.json.JsonObject;

/**
 * ロボットの体の向き、視線を制御するサーバ(MiracleHuman)に接続し制御指令を行うクラス
 *
 */
public class RobotBodyController extends AbstractCommunicator {

	public RobotBodyController(String hostname, int port) {
		super(hostname, port);
	}

	@Override
	protected void onReceived(String message) {
	}

	/**
	 * 視線座標指定指令送信メソッド。3次元座標を指定する
	 * @param x
	 * @param y
	 * @param z
	 */
	public void setGaze(double x, double y, double z) {
		String command = createGazeCommand("EyeController", x, y, z);
		sendLine(command);
		command = createGazeCommand("HeadController", x, y, z);
		sendLine(command);
	}

	/**
	 * 視線を向けるオブジェクト指定指令送信メソッド
	 * @param objectName オブジェクト名
	 */
	public void setGazeToObject(String objectName) {
		String command = createGazeCommand("EyeController", objectName, 0, 0, 0);
		sendLine(command);
		command = createGazeCommand("HeadController", objectName, 0, 0, 0);
		sendLine(command);
	}
	

	private String createGazeCommand(String controllerName, double x, double y, double z) {
		return createGazeCommand(controllerName, "", x, y, z);
	}
	private String createGazeCommand(String controllerName, String objectName, double x, double y, double z) {
		Map<String, Object> point = new HashMap<>();
		point.put("x", x);
		point.put("y", y);
		point.put("z", z);
		JsonObject pointObject = JsonUtils.createJsonObject(point);
		
		Map<String, Object> map = new HashMap<>();
		map.put("id", controllerName);
		map.put("motionTowardObject", objectName);
		map.put("targetMotionMode", 2);
		map.put("targetPoint", pointObject);
		map.put("translateSpeed", 2.0);
		String json = JsonUtils.jsonToString(JsonUtils.createJsonObject(map));
		return String.format("%s=%s", controllerName, json);
	}
}
