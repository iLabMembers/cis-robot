package jp.atr.commu_ai;

/**
 * ロボットの表情を制御するサーバ(JointMapper)に接続し制御指令を行うクラス
 *
 */
public class RobotExpressionController extends AbstractCommunicator {
	public RobotExpressionController(String hostname, int port) {
		super(hostname, port);
	}

	/**
	 * 表情指定指令送信メソッド
	 * @param expressionName 表情名
	 */
	public void setExpression(String expressionName) {
		String command = createExpressionCommand(expressionName);
		sendLine(command);
	}

	private String createExpressionCommand(String expressionName) {
		return String.format("expression %s", expressionName);
	}
	
	@Override
	protected void onReceived(String message) {
	}
}
