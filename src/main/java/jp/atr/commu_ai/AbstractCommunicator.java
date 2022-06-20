package jp.atr.commu_ai;

public abstract class AbstractCommunicator {
	protected TCPClient client;
	protected boolean isRosClient = false;
	
	public AbstractCommunicator(String hostname, int port) {
		client = new TCPClient(hostname, port);
		System.out.println(String.format("Client Info [%s] %s : %s", this.getClass().getSimpleName(), hostname, port));
	}
	
	public void start() {
		if(!client.connect()) {
			System.out.println("could not start " + this.getClass().getSimpleName());
			return;
		}
		startReceiveThread();
	}
	public void stop() {
		client.disconnect();
	}
	
	protected void sendLine(String command) {
		System.out.println(String.format("[%s]send command -> %s", this.getClass().getSimpleName(), command));
		client.sendLine(command);
	}
	
	private void startReceiveThread() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				while(client.connected) {
					try {
						String message;
						if(isRosClient)
							message = client.readJsonObject();
						else
							message = client.readLineNonBlocking();
						if(message != null && !message.startsWith("ERROR"))
							onReceived(message);
					} catch (Exception e) {
					}
				}
			}
		}).start();
	}
	
	abstract protected void onReceived(String message);
	

	
}
