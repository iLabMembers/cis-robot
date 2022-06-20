package jp.atr.commu_ai;

import java.io.*;
import java.net.*;

public class TCPClient {
	protected String ip;
	protected int port;
	protected Socket socket;
	protected BufferedReader in;
	protected PrintWriter out;
	protected boolean connected = false;
	protected boolean debug = false;
	

	public TCPClient(String ip, int port) {
		this.ip = ip;
		this.port = port;
	}
	
	public void setDebug(boolean debug) {
		this.debug = debug;
	}
	private void printDebug(String message) {
		if(debug)
			System.out.println(message);
	}

	public boolean connect() {
		if(!connected){
			try {
				socket = new Socket(ip, port);
				printDebug("connect to " + ip + "/" + port);
				in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				out = new PrintWriter(socket.getOutputStream(), true);
			} catch (IOException e) {
				connected = false;
				printDebug("fail to connect " + ip + "/" + port);
				return false;
			}
			connected = true;
			return true;
		}
		else{
			printDebug("already connected");
			return false;
		}
	}

	public void disconnect() {
		if(connected){
			printDebug("client disconnected");
			try {
				in.close();
				out.close();
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			connected = false;
		}
	}
	
	public String sendLine(String cmd) {
		if (!connected)
			return "ERROR: Not connected";

		try {
			out.println(cmd);
			printDebug("[Client] send : " + cmd);
			return "OK";
		} catch (Exception e) {
			connected = false;
			return "ERROR: Server disconnected";
		}
	}
	
	public String readLineBlocking(){
		if (!connected)
			return "ERROR: Not connected";
		try {
			socket.setSoTimeout(0);
		} catch (SocketException e1) {
			///System.err.println(e1+" @"+this.getClass().getSimpleName());
		}
		try{
			String tmp = in.readLine();
			//printDebug(tmp);
			printDebug("[Client] receive : " + tmp);
			return tmp;
		}
		catch(IOException e){
			//printDebug(e.toString());
			return e.toString();
		}
	}
	public String readLineNonBlocking(){
		if (!connected)
			return "ERROR: Not connected";
		try {
			socket.setSoTimeout(10);
		} catch (SocketException e) {
			//System.err.println(e + " @" + this.getClass().getSimpleName());
		}
		try{
			int firstData = in.read();
			if(firstData > 0) {
				socket.setSoTimeout(0);
				String tmp = (char)firstData + in.readLine();
				printDebug("[Client] receive : " + tmp);
				return tmp;
			} else {
				return null;
			}
		}
		catch(IOException e){
			//printDebug(e.toString());
			return null;
		}
	}
	
	private String buffer = "";
	// for ROS bridge
	public String readJsonObject(){
		if (!isConnected())
			return "ERROR: Not connected";
		try {
			socket.setSoTimeout(10);
		} catch (SocketException e) {
			//System.err.println(e + " @" + this.getClass().getSimpleName());
		}
		try{
			String received = buffer;
			while(true) {
				int data = in.read();
				if(data <= 0) {
					buffer = received;
					received = "";
					break;
				}
				received += (char)data;
				// reset
				if(!received.startsWith("{") && received.endsWith("}{"))
					received = "{";
				if(JsonUtils.isProperJson(received)) {
					buffer = "";
					break;
				}
			}
			if(received.isEmpty())
				return null;
			printDebug("[Client] receive : " + received);
			return received;
		}
		catch(IOException e){
			//System.out.println(e.toString());
			return null;
		}
	}


	public boolean isConnected() {
		return connected;
	}
}
