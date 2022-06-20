package jp.atr.commu_ai;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;


/**
 * 
 * このプログラムは対話コンペティションのサンプルプログラムです。
 * 対話コンペティション用の各プログラムとの通信を行い、基本的なインタラクションを構成します。
 * @author Tomo Funayama
 * 
 */
public class CompetitionSample implements IConversationTimerListener {
	private static final String PROPERTIES_PATH = "setting.properties";
	private static final String SPEECH_RECOGNITION_KEY = "speech_recognition";
	private static final String SPEECH_GENERATOR_KEY = "speech_generator";
	private static final String ROBOT_EXPRESSION_CONTROLLER_KEY = "robot_expression_controller";
	private static final String ROBOT_BODY_CONTROLLER_KEY = "robot_body_controller";
	private static final String FACE_RECOGNITION_KEY = "face_recognition";
	private static final String SELECTION_ID_CONNECTOR_KEY = "selection_id_connector";
	private static final List<String> SERVER_NAME_LIST = Arrays.asList(
			SPEECH_RECOGNITION_KEY,
			SPEECH_GENERATOR_KEY,
			ROBOT_EXPRESSION_CONTROLLER_KEY,
			ROBOT_BODY_CONTROLLER_KEY,
			FACE_RECOGNITION_KEY,
			SELECTION_ID_CONNECTOR_KEY);
	private static final String SERVER_HOST_KEY = "host";
	private static final String SERVER_PORT_KEY = "port";
	private static final String SIGHT_DATA_PATH_KEY = "sight_data_path";
	
	private SpeechRecognitionManager speechRecognition;
	private SpeechGenerator speechGenerator;
	private RobotExpressionController robotExpressionController;
	private RobotBodyController robotBodyController;
	private FaceRecognitionManager faceRecognition;
	private SelectionIDConnector selectionIDConnector;
	private SightDataManager sightDataManager = null;

	private Map<String, String> serverHostMap = new HashMap<>();
	private Map<String, Integer> serverPortMap = new HashMap<>();
	private String sightDataFileName = null;
	private boolean toEndConversation = false;
	
	public CompetitionSample() {
		readProperties();
		readSightData();
		initClients();
	}

	/**
	 * プロパティファイルから各設定値を読み込む
	 */
	private void readProperties() {
		Properties properties = new Properties();
		try {
			URL url = CompetitionSample.class.getClassLoader().getResource(PROPERTIES_PATH);
			InputStream inputStream = new FileInputStream(new File(url.toURI()));
			properties.load(inputStream);
			for (String serverName : SERVER_NAME_LIST) {
				String host = properties.getProperty(String.format("%s_%s", serverName, SERVER_HOST_KEY));
				host = host != null ? host : "localhost";
				serverHostMap.put(serverName, host);
				String port = properties.getProperty(String.format("%s_%s", serverName, SERVER_PORT_KEY));
				port = port != null ? port : "1000";
				try {
					serverPortMap.put(serverName, Integer.parseInt(port));
				} catch(NumberFormatException e) {
					serverPortMap.put(serverName, 1000);
				}
			}
			sightDataFileName = properties.getProperty(SIGHT_DATA_PATH_KEY);
			inputStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 観光地データの読み込み
	 */
	private void readSightData() {
		if(sightDataFileName == null) {
			System.out.println("NO Sight data SET.");
			return;
		}
		sightDataManager = new SightDataManager(sightDataFileName);
	}
	
	
	/**
	 * 通信クライアントの初期化。各プログラムのホストとポートを指定する
	 */
	private void initClients() {
		/*speechRecognition = new SpeechRecognitionManager("localhost", 8888);
		speechGenerator = new SpeechGenerator("jhon-mouse", 3456);
		robotExpressionController = new RobotExpressionController("jhon-mouse", 20000);
		robotBodyController = new RobotBodyController("jhon-mouse", 21000);
		//faceRecognition = new FaceRecognitionManager("localhost", 10091);
		faceRecognition = new FaceRecognitionManager("hil-mouse03", 10091);
		selectionIDConnector = new SelectionIDConnector("localhost", 8001);*/
		speechRecognition = new SpeechRecognitionManager(serverHostMap.get(SPEECH_RECOGNITION_KEY), serverPortMap.get(SPEECH_RECOGNITION_KEY));
		speechGenerator = new SpeechGenerator(serverHostMap.get(SPEECH_GENERATOR_KEY), serverPortMap.get(SPEECH_GENERATOR_KEY));
		robotExpressionController = new RobotExpressionController(serverHostMap.get(ROBOT_EXPRESSION_CONTROLLER_KEY), serverPortMap.get(ROBOT_EXPRESSION_CONTROLLER_KEY));
		robotBodyController = new RobotBodyController(serverHostMap.get(ROBOT_BODY_CONTROLLER_KEY), serverPortMap.get(ROBOT_BODY_CONTROLLER_KEY));
		//faceRecognition = new FaceRecognitionManager("localhost", 10091);
		faceRecognition = new FaceRecognitionManager(serverHostMap.get(FACE_RECOGNITION_KEY), serverPortMap.get(FACE_RECOGNITION_KEY));
		selectionIDConnector = new SelectionIDConnector(serverHostMap.get(SELECTION_ID_CONNECTOR_KEY), serverPortMap.get(SELECTION_ID_CONNECTOR_KEY));
		selectionIDConnector.addListener(this);
	}
	
	/**
	 * サンプルインタラクション起動
	 * @param useFaceRecognition 顔認識システムとの通信を行うか
	 */
	public void run(boolean useFaceRecognition) {
		speechRecognition.start();
		speechGenerator.start();
		robotExpressionController.start();
		robotBodyController.start();
		selectionIDConnector.start();
		if(useFaceRecognition)
			faceRecognition.start();
		speechGenerator.say("システムを起動しました");
		
		startProcess();

		speechRecognition.stop();
		speechGenerator.stop();
		robotExpressionController.stop();
		robotBodyController.stop();
		selectionIDConnector.stop();
		if(useFaceRecognition)
			faceRecognition.stop();
	}
	
	/**
	 * サンプルインタラクションのメインループ
	 */
	private void startProcess() {
		while(true) {
			if(!process())
				break;
			try {
				Thread.sleep(30);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	/**
	 * サンプルインタラクションのメインループ処理
	 * 音声認識結果を取得し、それぞれの機能を実行する
	 * @return ループ終了するか
	 */
	private boolean process() {
		if(toEndConversation) {
			speechGenerator.say("時間になりました");
			speechGenerator.say("システムを終了します");
			toEndConversation = false;
			return false;
		}
		String speech = speechRecognition.readResult();
		if(speech == null)
			return true;
		switch(speech) {
		case "こんにちは":
			speechGenerator.say("こんにちは");
			break;
		case "笑顔":
			// 表情指定
			robotExpressionController.setExpression("fullsmile");
			break;
		case "ムードベース":
			// 表情指定
			robotExpressionController.setExpression("MoodBasedFACS");
			break;
		case "正面向いて":
			// 視線指定
			robotBodyController.setGaze(0.0, 1.2, 1.5);
			break;
		case "右向いて":
			robotBodyController.setGaze(1.0, 1.2, 1.5);
			break;
		case "人を見て":
			robotBodyController.setGazeToObject("humanhead");
			break;
		case "モニターを見て":
			robotBodyController.setGazeToObject("monitor");
			break;
		case "表情を教えて":
			// 表情認識結果取得
			String emotion = faceRecognition.getEmotion();
			if(emotion != null)
				speechGenerator.say(String.format("表情は%sです", emotion));
			else
				speechGenerator.say("表情はわかりません");
			break;
		case "年齢を教えて":
			// 顔認識結果取得
			int age = faceRecognition.getAge();
			if(age != -1)
				speechGenerator.say(String.format("年齢は%d歳です", age));
			else
				speechGenerator.say("年齢はわかりません");
			break;
		case "性別を教えて":
			// 顔認識結果取得
			String gender = faceRecognition.getGender();
			if(gender != null)
				speechGenerator.say(String.format("性別は%sです", gender));
			else
				speechGenerator.say("性別はわかりません");
			break;
		case "観光地について教えてください":
			introduceSight();
			break;
		case "終了":
			speechGenerator.say("システムを終了します");
			return false;
		default:
			break;
		}
		return true;
	}
	

	/**
	 * 選択された観光地情報の紹介サンプル。
	 */
	private void introduceSight() {
		if(sightDataManager == null) {
			speechGenerator.say("すみません。観光地データが読み込めていません。");
			return;
		}
		boolean result = selectionIDConnector.fetchData();
		if(!result) {
			speechGenerator.say("すみません。ご希望の観光地が取得できませんでした。");
			return;
		}
		List<String> sightList = selectionIDConnector.getSelectedID();
		StringBuilder builder = new StringBuilder("はい。ご希望の観光地は、");
		for (String sightID : sightList) {
			String sightName = sightDataManager.getSightName(sightID);
			if(sightName == null)
				continue;
			builder.append(sightName);
			builder.append("と");
		}
		builder.deleteCharAt(builder.length() - 1);
		builder.append("ですね。");
		speechGenerator.say(builder.toString());
		//speechGenerator.say("ここに、観光地の紹介を入れてみてくださいね。");
		String recommendedID = selectionIDConnector.getRecommendedID();
		String sightName = sightDataManager.getSightName(recommendedID);
		if(sightName != null) {
			speechGenerator.say(String.format("おすすめは、%sですよ。", sightName));
		}
		speechGenerator.say("では、観光地紹介を終わります。");
	}
	
	
	public void test() {
		/*robotController.start();
		robotController.setExpression("angry");
		//robotController.setGaze(2.0, 2.2, 1.5);
		robotController.stop();
		*/
		
		/*faceRecognition.start();
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		faceRecognition.stop();*/

		/*robotBodyController.start();
		robotBodyController.setGazeToObject("humanhead");
		robotBodyController.stop();*/
		
		selectionIDConnector.start();
		System.out.println(selectionIDConnector.fetchData());
		System.out.println(selectionIDConnector.getSelectedID());
		System.out.println(selectionIDConnector.getRecommendedID());
		selectionIDConnector.stop();
	}


	/**
	 * 対話時間終了コマンドの受信後処理
	 */
	@Override
	public void onConversationRule(String rule, long elapsedTime) {
		if(rule.equals(SelectionIDConnector.COMMAND_CONVERSATION_END))
			toEndConversation = true;
	}
	
	
	
	public static void main(String[] args) {
		CompetitionSample sample = new CompetitionSample();
		sample.run(true);
		//sample.test();
	}
}
