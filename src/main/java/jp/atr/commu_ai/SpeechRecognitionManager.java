package jp.atr.commu_ai;

/**
 * 音声認識サーバに接続するクラス
 *
 */
public class SpeechRecognitionManager extends AbstractCommunicator {
	private String recognitionResult = null;
	
	public SpeechRecognitionManager(String hostname, int port) {
		super(hostname, port);
	}

	@Override
	public void start() {
		super.start();
		client.sendLine("start");
	}
	@Override
	public void stop() {
		client.sendLine("stop");
		super.stop();
	}
	
	public String readResult() {
		String tmp = null;
		if(recognitionResult != null) {
			tmp = new String(recognitionResult);
			recognitionResult = null;
		}
		return tmp;
	}

	@Override
	protected void onReceived(String message) {
		if(message.startsWith("result:")) {
			recognitionResult = message.substring(7);
			System.out.println("ASR : " + recognitionResult);
		}
	}
}
