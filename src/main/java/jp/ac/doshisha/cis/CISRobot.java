package jp.ac.doshisha.cis;

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

public class CISRobot implements IConversationTimerListener {

    private static final String       PROPERTIES_PATH                 = "setting.properties";
    private static final String       SPEECH_RECOGNITION_KEY          = "speech_recognition";
    private static final String       SPEECH_GENERATOR_KEY            = "speech_generator";
    private static final String       ROBOT_EXPRESSION_CONTROLLER_KEY = "robot_expression_controller";
    private static final String       ROBOT_BODY_CONTROLLER_KEY       = "robot_body_controller";
    private static final String       FACE_RECOGNITION_KEY            = "face_recognition";
    private static final String       SELECTION_ID_CONNECTOR_KEY      = "selection_id_connector";
    private static final String       SERVER_HOST_KEY                 = "host";
    private static final String       SERVER_PORT_KEY                 = "port";
    private static final String       SIGHT_DATA_PATH_KEY             = "sight_data_path";
    private static final List<String> SERVER_NAME_LIST                =
            Arrays.asList(
                    SPEECH_RECOGNITION_KEY,
                    SPEECH_GENERATOR_KEY,
                    ROBOT_EXPRESSION_CONTROLLER_KEY,
                    ROBOT_BODY_CONTROLLER_KEY,
                    FACE_RECOGNITION_KEY,
                    SELECTION_ID_CONNECTOR_KEY
            );


    private SpeechRecognitionManager  speechRecognition;
    private SpeechGenerator           speechGenerator;
    private RobotExpressionController robotExpressionController;
    private RobotBodyController       robotBodyController;
    private FaceRecognitionManager    faceRecognition;
    private SelectionIDConnector      selectionIDConnector;
    private SightDataManager          sightDataManager  = null;
    private Map<String, String>       serverHostMap     = new HashMap<>();
    private Map<String, Integer>      serverPortMap     = new HashMap<>();
    private String                    sightDataFileName = null;
    private boolean                   toEndConversation = false;

    /*
     *  以下，拡張
     */
    private static Object               inputStream  = null;  // 入力の種類
    private static Object               outputStream = null;  // 出力の種類
    private static Map<String, Boolean> useServer    =        // 各サーバを利用するか否か
            new HashMap<>(){{
                put(SPEECH_RECOGNITION_KEY,          false);
                put(SPEECH_GENERATOR_KEY,            false);
                put(ROBOT_EXPRESSION_CONTROLLER_KEY, false);
                put(ROBOT_BODY_CONTROLLER_KEY,       false);
                put(FACE_RECOGNITION_KEY,            false);
                put(SELECTION_ID_CONNECTOR_KEY,      false);
            }};

    private ConversationFlow conversationFlow;                // 会話の流れを定義するクラス

    public CISRobot() {

        readProperties();
        readSightData();
        initClients();

        /*
         * 以下，拡張
         */
        // チャットロボットかマルチモーダルロボットか設定
        if (useServer.get(SPEECH_GENERATOR_KEY)) inputStream = speechRecognition;
        else                                     inputStream = new Scanner(System.in);

        if (useServer.get(SPEECH_GENERATOR_KEY)) outputStream = speechGenerator;
        else                                     outputStream = new PrintWriter(System.out);

        // 会話制御用クラスインスタンスの生成
        conversationFlow = ConversationFlow.getConversationFlow();

    }

    /**
     * プロパティファイルから各設定値を読み込む
     */
    private void readProperties() {

        Properties properties = new Properties();

        try {

            URL url = jp.atr.commu_ai.CompetitionSample.class.getClassLoader().getResource(PROPERTIES_PATH);
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

                // 各サーバを利用するかどうか設定
                useServer.put(serverName, Boolean.valueOf(properties.getProperty(serverName)));

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

        if (useServer.get(SPEECH_RECOGNITION_KEY))          speechRecognition         = new SpeechRecognitionManager(serverHostMap.get(SPEECH_RECOGNITION_KEY), serverPortMap.get(SPEECH_RECOGNITION_KEY));
        if (useServer.get(SPEECH_GENERATOR_KEY))            speechGenerator           = new SpeechGenerator(serverHostMap.get(SPEECH_GENERATOR_KEY), serverPortMap.get(SPEECH_GENERATOR_KEY));
        if (useServer.get(ROBOT_EXPRESSION_CONTROLLER_KEY)) robotExpressionController = new RobotExpressionController(serverHostMap.get(ROBOT_EXPRESSION_CONTROLLER_KEY), serverPortMap.get(ROBOT_EXPRESSION_CONTROLLER_KEY));
        if (useServer.get(ROBOT_BODY_CONTROLLER_KEY))       robotBodyController       = new RobotBodyController(serverHostMap.get(ROBOT_BODY_CONTROLLER_KEY), serverPortMap.get(ROBOT_BODY_CONTROLLER_KEY));
        if (useServer.get(FACE_RECOGNITION_KEY))            faceRecognition           = new FaceRecognitionManager(serverHostMap.get(FACE_RECOGNITION_KEY), serverPortMap.get(FACE_RECOGNITION_KEY));

        selectionIDConnector = new SelectionIDConnector(serverHostMap.get(SELECTION_ID_CONNECTOR_KEY), serverPortMap.get(SELECTION_ID_CONNECTOR_KEY));
        selectionIDConnector.addListener(this);

    }

    /**
     *
     */
    public void run() {

        if (useServer.get(SPEECH_RECOGNITION_KEY))          speechRecognition.start();
        if (useServer.get(SPEECH_GENERATOR_KEY))            speechGenerator.start();
        if (useServer.get(ROBOT_EXPRESSION_CONTROLLER_KEY)) robotExpressionController.start();
        if (useServer.get(ROBOT_BODY_CONTROLLER_KEY))       robotBodyController.start();
        if (useServer.get(FACE_RECOGNITION_KEY))            faceRecognition.start();
        selectionIDConnector.start();

        say("システムを起動しました");

        startProcess();

        if (useServer.get(SPEECH_RECOGNITION_KEY))          speechRecognition.stop();
        if (useServer.get(SPEECH_GENERATOR_KEY))            speechGenerator.stop();
        if (useServer.get(ROBOT_EXPRESSION_CONTROLLER_KEY)) robotExpressionController.stop();
        if (useServer.get(ROBOT_BODY_CONTROLLER_KEY))       robotBodyController.stop();
        if (useServer.get(FACE_RECOGNITION_KEY))            faceRecognition.stop();
        selectionIDConnector.stop();

    }


    /**
     * サンプルインタラクションのメインループ
     */
    private void startProcess() {

        if (! prepareConversationFlow()) {

            return;

        }

        this.conversationFlow.startConversation();

    }

    /**
     * 対話時間終了コマンドの受信後処理
     */
    @Override
    public void onConversationRule(String rule, long elapsedTime) {

        if(rule.equals(SelectionIDConnector.COMMAND_CONVERSATION_END)) toEndConversation = true;

    }


    /**
     * 会話の流れを準備
     * @return 登録した全ての発話インスタンスに不正がないかチェック
     */
    public boolean prepareConversationFlow() {

        // ロボット起動の始まりの発話
        ConversationContent introduction = new IntroductionMonologue();

        // flow 1
        ActivityQuestionSample activeQuestionSet1 = new ActivityQuestionSample();
        ActivityQuestionSample activeQuestionSet11 = new ActivityQuestionSample();
        activeQuestionSet1
                .setNextConversations(
                        new ArrayList<>() {{
                            add(MonologueFactory.createSimpleMonologue("Yes", "Good!")); // The robot says line when speech is in
                            add(activeQuestionSet11);
                        }},
                        ResponseEvaluation.CONTAINS);
        activeQuestionSet11
                .setKey("No")
                .setNextConversations(
                        new ArrayList<>() {{
                            add(MonologueFactory.createSimpleMonologue("Yes", "You should go to a park!"));
                            add(MonologueFactory.createSimpleMonologue("No", "You should go to a museum!"));
                        }},
                        ResponseEvaluation.CONTAINS);

        // flow 2
        ActivityQuestionSample activeQuestionSet2 = new ActivityQuestionSample();
        ActivityQuestionSample activeQuestionSet21 = new ActivityQuestionSample();
        activeQuestionSet2.setNextConversations(
                new ArrayList<>() {{
                    add(MonologueFactory.createSimpleMonologue("Yes", "Good!")); // The robot says line when speech is in
                    add(activeQuestionSet21);
                }},
                ResponseEvaluation.CONTAINS);
        activeQuestionSet21.setKey("No")
                .setNextConversations(
                        new ArrayList<>() {{
                            add(MonologueFactory.createSimpleMonologue("Yes", "You should go to a park!"));
                            add(MonologueFactory.createSimpleMonologue("No", "You should go to a museum!"));
                        }},
                        ResponseEvaluation.CONTAINS);

        // 会話の締め
        ConversationContent conclusion  = new ConclusionMonologue();

        // 会話の流れを登録する
        this.conversationFlow
                .setIntroduction(introduction)
                // StartPoint として登録されたものから選択され会話が始まる（デフォルト：ランダム）
                .appendStartPoint(activeQuestionSet1) 
                .appendStartPoint(activeQuestionSet2)
                .setConclusion(conclusion);


        // 登録された発話インスタンスに不正がないかチェック
        return this.conversationFlow.checkReadyAllConversation();

    }

    /**
     * ロボットが発話する
     * @param text 発話内容
     */
    public static void say(String text) {

        if   (useServer.get(SPEECH_GENERATOR_KEY)) ((SpeechGenerator) outputStream).say("システムを起動しました");
        else {
            ((PrintWriter) outputStream).println("[Robot] " + text);
            ((PrintWriter) outputStream).flush();
        }

    }

    /**
     * ロボットを入力待ち状態にし，入力された値を返す
     * @return 入力された発話内容の文字列を返す
     */
    public static String listen() {

        String speech;
        if   (useServer.get(SPEECH_RECOGNITION_KEY)) {

            speech = ((SpeechRecognitionManager) inputStream).readResult();
            while(speech == null) {


                try {

                    Thread.sleep(30);

                } catch (InterruptedException e) {

                    e.printStackTrace();

                }

                speech = ((SpeechRecognitionManager) inputStream).readResult();

            }

        } else {

            ((PrintWriter) outputStream).print("[You] ");
            ((PrintWriter) outputStream).flush();
            speech = ((Scanner) inputStream).next();

        }

        return speech;

    }

    /**
     *
     * @param args 使用しない
     */
    public static void main(String[] args) {

        CISRobot cisRobot = new CISRobot();

        cisRobot.run();

    }

}
