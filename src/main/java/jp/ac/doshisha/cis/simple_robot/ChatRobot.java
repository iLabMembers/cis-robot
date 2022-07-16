package jp.ac.doshisha.cis.simple_robot;

import jp.atr.commu_ai.*;
import jp.xkzm.robot.conversation.*;
import jp.xkzm.robot.conversation.monologues.MonologueFactory;
import jp.xkzm.robot.conversation.monologues.impls.ConclusionMonologue;
import jp.xkzm.robot.conversation.monologues.impls.IntroductionMonologue;
import jp.xkzm.robot.conversation.questions.ActivityQuestionSample;
import jp.xkzm.robot.conversation.response_evaluation.ResponseEvaluation;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;


/**
 *
 * このプログラムは対話コンペティションのサンプルプログラムです。
 * 対話コンペティション用の各プログラムとの通信を行い、基本的なインタラクションを構成します。
 * @author Tomo Funayama
 *
 */
class ChatRobot implements IConversationTimerListener {
	private static final String PROPERTIES_PATH = "setting.properties";
	private static final String SELECTION_ID_CONNECTOR_KEY = "selection_id_connector";
	private static final List<String> SERVER_NAME_LIST = Arrays.asList(SELECTION_ID_CONNECTOR_KEY);
	private static final String SERVER_HOST_KEY = "host";
	private static final String SERVER_PORT_KEY = "port";
	private static final String SIGHT_DATA_PATH_KEY = "sight_data_path";

	private final ConversationFlow cf;
	private final Scanner          scanner;

	private SelectionIDConnector selectionIDConnector;
	private SightDataManager sightDataManager = null;

	private Map<String, String> serverHostMap = new HashMap<>();
	private Map<String, Integer> serverPortMap = new HashMap<>();
	private String sightDataFileName = null;
	private boolean toEndConversation = false;


	public ChatRobot() {
		readProperties();
		readSightData();
		initClients();

		this.scanner = new Scanner(System.in);
		this.cf      = ConversationFlow.getConversationFlow();
	}

	/**
	 * プロパティファイルから各設定値を読み込む
	 */
	private void readProperties() {
		Properties properties = new Properties();
		try {
			URL url = ChatRobot.class.getClassLoader().getResource(PROPERTIES_PATH);
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
		} catch (IOException | URISyntaxException e) {
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
		selectionIDConnector = new SelectionIDConnector(serverHostMap.get(SELECTION_ID_CONNECTOR_KEY), serverPortMap.get(SELECTION_ID_CONNECTOR_KEY));
		selectionIDConnector.addListener(this);
	}
	
	/**
	 * サンプルインタラクション起動
	 */
	public void run(boolean useFaceRecognition) {

		selectionIDConnector.start();

		startProcess();

		selectionIDConnector.stop();

	}

	private void prepareConversationFlow() {

		ConversationContent introduction = new IntroductionMonologue();

		ActivityQuestionSample activeQuestionSet11 = new ActivityQuestionSample("");
		ActivityQuestionSample activeQuestionSet12 = new ActivityQuestionSample("");
		activeQuestionSet11.setNextConversations(
				new ArrayList<>() {{
					add(MonologueFactory.createSimpleMonologue("Yes", "Good!")); // The robot says line when speech is in
					add(activeQuestionSet12);
				}},
				ResponseEvaluation.CONTAINS);

		activeQuestionSet12.setKey("No")
				.setNextConversations(
						new ArrayList<>() {{
							add(MonologueFactory.createSimpleMonologue("Yes", "You should go to a park!"));
							add(MonologueFactory.createSimpleMonologue("No", "You should go to a museum!"));
						}},
						ResponseEvaluation.CONTAINS);

		ActivityQuestionSample QuestionSet21 = new ActivityQuestionSample("");
		ActivityQuestionSample QuestionSet22 = new ActivityQuestionSample("");
		activeQuestionSet11.setNextConversations(
				new ArrayList<>() {{
					add(MonologueFactory.createSimpleMonologue("", "Good!"));
					add(activeQuestionSet12);
				}},
				ResponseEvaluation.CONTAINS);

		activeQuestionSet12.setKey("No")
				.setNextConversations(
						new ArrayList<>() {{
							add(MonologueFactory.createSimpleMonologue("Yes", "You should go to a park!"));
							add(MonologueFactory.createSimpleMonologue("Yes", "You should go to a museum!"));
						}},
						ResponseEvaluation.CONTAINS);


		ConversationContent conclusion  = new ConclusionMonologue();

		cf.setIntroduction(introduction)
				.appendStartPoint(activeQuestionSet11)
				.setConclusion(conclusion);

	}
	
	/**
	 * サンプルインタラクションのメインループ
	 */
	private void startProcess() {

		prepareConversationFlow();

		say("こんにちは！");

		cf.startConversation();

	}
	/**
	 * サンプルインタラクションのメインループ処理
	 * 音声認識結果を取得し、それぞれの機能を実行する
	 * @return ループ終了するか
	 */
	private boolean process() {
		if(toEndConversation) {
			say("時間になりました");
			say("システムを終了します");
			toEndConversation = false;
			return false;
		}


		return true;
	}

	private void prompt() {
		System.out.print("[You]  ");
		String input = this.scanner.next();
	}


	/**
	 * 選択された観光地情報の紹介サンプル。
	 */
	private void introduceSight() {
		if(sightDataManager == null) {
			say("すみません。観光地データが読み込めていません。");
			return;
		}
		boolean result = selectionIDConnector.fetchData();
		if(!result) {
			say("すみません。ご希望の観光地が取得できませんでした。");
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
		say(builder.toString());
		//speechGenerator.say("ここに、観光地の紹介を入れてみてくださいね。");
		String recommendedID = selectionIDConnector.getRecommendedID();
		String sightName = sightDataManager.getSightName(recommendedID);
		if(sightName != null) {
			say(String.format("おすすめは、%sですよ。", sightName));
		}
		say("では、観光地紹介を終わります。");
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
	
	public static void say(String text) {

		System.out.println("[Robot] " + text);

	}
	
	public static void main(String[] args) {

		ChatRobot cisRobot = new ChatRobot();

		cisRobot.run(true);

	}

}
