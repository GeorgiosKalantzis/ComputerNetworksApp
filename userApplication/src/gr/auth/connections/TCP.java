package gr.auth.connections;

import java.net.*;

/**
 * <h1> Establish TCP connection with ITHAKI server </h1>
 * 
 * @author Georgios Kalantzis
 *
 */

public class TCP {
	
	private static final int TIME_OUT = 2000;
	
	private String serverIP = "";
	
	private int serverPort = 0;

	private Socket connection = null;
	
	private static InetAddress hostAddress;
	
	/**
	 * <h1> Constructor </h1>
	 * 
	 * <br> Establish the connection.
	 * 
	 * @param serverIP
	 * @param serverPort
	 * @param clientPort
	 */
	
	public TCP(String serverIP, int serverPort) {
		this.serverIP = serverIP;
		this.serverPort = serverPort;
		
		try {
			String[] serverIpSplit = serverIP.split("\\.");
			
			byte[] serverIpBytes = {(byte) Integer.parseInt(serverIpSplit[0]),
					(byte) Integer.parseInt(serverIpSplit[1]), (byte) Integer.parseInt(serverIpSplit[2]),
					(byte) Integer.parseInt(serverIpSplit[3]) 
					
			};
			
			setHostAddress(InetAddress.getByAddress(serverIpBytes));
			
			connection = new Socket(TCP.hostAddress,this.serverPort);
			connection.setSoTimeout(TIME_OUT);
			
			
		}catch(Exception e) {
			e.printStackTrace();
			
		}
		
	}

	public String getServerIP() {
		return serverIP;
	}

	public void setServerIP(String serverIP) {
		this.serverIP = serverIP;
	}

	public int getServerPort() {
		return serverPort;
	}

	public void setServerPort(int serverPort) {
		this.serverPort = serverPort;
	}

	public Socket getConnection() {
		return connection;
	}

	public void setConnection(Socket connection) {
		this.connection = connection;
	}

	public static InetAddress getHostAddress() {
		return hostAddress;
	}

	public static void setHostAddress(InetAddress hostAddress) {
		TCP.hostAddress = hostAddress;
	}
	
	
	
	
	
	

}
