package jp.atr.commu_ai;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class SelectionIDConnector extends AbstractCommunicator {
	private static final String COMMAND_GET = "GET";
	private static final long TIMEOUT_THRESHOLD = 2000;
	private static final String SELECTED_ID_KEY = "selected_id";
	private static final String RECOMMENDED_ID_KEY = "recommended_id";
	private static final String OPERATION_KEY = "operation";
	private static final String COMMAND_TIMER = "TIMER";
	private static final String ELAPSED_TIME_KEY = "elapsed_time";
	private static final String CONVERSATION_RULE_KEY = "conversation_rule";
	public static final String COMMAND_CONVERSATION_END = "CONVERSATION_END";
	
	private List<String> currentIDList = new ArrayList<>();
	private String recommendedID = null;
	private long lastAskTime = System.currentTimeMillis();
	private long lastGetTime = System.currentTimeMillis();
	
	private List<IConversationTimerListener> listeners = new ArrayList<>();

	public SelectionIDConnector(String hostname, int port) {
		super(hostname, port);
	}
	
	

	public void addListener(IConversationTimerListener listener) {
		listeners.add(listener);
	}
	public void removeListener(IConversationTimerListener listener) {
		listeners.remove(listener);
	}
	private void fireConversationTimer(String rule, long elapsedTime) {
		for (IConversationTimerListener listener : listeners) {
			listener.onConversationRule(rule, elapsedTime);
		}
	}
	

	public List<String> getSelectedID(){
		return new ArrayList<String>(currentIDList);
	}
	public String getRecommendedID() {
		return recommendedID;
	}
	
	/**
	 * GETコマンドによって、観光地ID管理サーバから現在の選択IDリストを取得する。
	 * 取得したStringデータは、JsonエンコードされたStringリスト
	 * @return
	 */
	public boolean fetchData(){
		lastAskTime = System.currentTimeMillis();
		sendLine(COMMAND_GET);
		while(System.currentTimeMillis() - lastAskTime < TIMEOUT_THRESHOLD) {
			if(lastGetTime > lastAskTime) {
				return true;
			}
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	@Override
	protected void onReceived(String message) {
		//System.out.println(message);
		//currentIDList = JsonUtils.stringToArray(message);
		Map<String, Object> data = JsonUtils.stringToMap(message);
		
		// 対話時間終了コマンドの受信処理
		if(data.containsKey(OPERATION_KEY)) {
			String operation = (String)data.get(OPERATION_KEY);
			if(operation.equals(COMMAND_TIMER)) {
				String conversationRule = (String) data.get(CONVERSATION_RULE_KEY);
				int elapsedTimeInt = (int) data.get(ELAPSED_TIME_KEY);
				long elapsedTime = (long) elapsedTimeInt;
				//System.out.println(elapsedTime);
				fireConversationTimer(conversationRule, elapsedTime);
			}
		}
		// 観光地データの受信処理
		else {
			if(data.containsKey(SELECTED_ID_KEY) && data.get(SELECTED_ID_KEY) instanceof List<?>) {
				List<?> selecetedIDList = (List<?>)data.get(SELECTED_ID_KEY);
				currentIDList.clear();
				for (Object object : selecetedIDList) {
					currentIDList.add((String)object);
				}
			}
			else {
				System.out.println("No data : " + SELECTED_ID_KEY);
				return;
			}
			if(data.containsKey(RECOMMENDED_ID_KEY))
				recommendedID = (String)data.get(RECOMMENDED_ID_KEY);
			else {
				System.out.println("No data : " + RECOMMENDED_ID_KEY);
				return;
			}
			//System.out.println(data);
			lastGetTime = System.currentTimeMillis();
		}
	}

}
