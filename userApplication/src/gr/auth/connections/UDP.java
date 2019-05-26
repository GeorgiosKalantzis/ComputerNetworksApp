package gr.auth.connections;

import java.net.*;

/**
 * <h1> Establish UDP connection with ITHAKI server </h1>
 * 
 * @author Georgios Kalantzis
 *
 */

public class UDP {

	private static final int TIME_OUT = 2000; 

	private String serverIp = "";

	private int serverPort = 0;

	private int clientPort = 0;

	private DatagramSocket connection = null;

	private static InetAddress hostAddress;
	
	/**
	 * 
	 * <h1> Constructor </h1>
	 * 
	 * <b>Also, establishes DatagramSocket connection , sets timeout
	 * and the InetAddress </b>
	 * 
	 * @param serverIp ITHAKI's IP.
	 * @param serverPort ITHAKI's port.
	 * @param clientPort local port.
	 * @exception throws SocketException.
	 * 
	 */
	

	public UDP(String serverIp, int serverPort, int clientPort) {
		this.serverIp = serverIp;
		this.serverPort = serverPort;
		this.clientPort = clientPort;

		try {

			connection = new DatagramSocket(this.clientPort);
			connection.setSoTimeout(TIME_OUT);

			String[] serverIpSplit = serverIp.split("\\.");

			byte[] serverIpBytes = { (byte) Integer.parseInt(serverIpSplit[0]),
					(byte) Integer.parseInt(serverIpSplit[1]), (byte) Integer.parseInt(serverIpSplit[2]),
					(byte) Integer.parseInt(serverIpSplit[3]) };
			
			setHostAddress(InetAddress.getByAddress(serverIpBytes));

		} catch (Exception e) {
			
			e.printStackTrace();
			

		}

	}
	
	/**
	 * 
	 * @return server ITHAKI IP.
	 */

	public String getServerIp() {
		return serverIp;
	}
	
	/**
	 * 
	 * @return server ITHAKI port.
	 */
	
	public int getServerPort() {
		return serverPort;
	}
	
	/**
	 * 
	 * @return DatagraSocket object for connecting to ITHAKI.
	 */
	
	public DatagramSocket getConnection() {
		return connection;
	}
	
	/**
	 * 
	 * @return InetAddress object for holding ITHAKI IP.
	 */
	
	public static InetAddress getHostAddress() {
		return hostAddress;
	}
	
	/**
	 * 
	 * @param serverIp set ITHAKI's IP.
	 */
	
	public void setServerIp(String serverIp) {
		this.serverIp = serverIp;
	}
	
	/**
	 * 
	 * @param serverPort set ITHAKI's port.
	 */

	public void setServerPort(int serverPort) {
		this.serverPort = serverPort;
	}
	
	/**
	 * 
	 * @param hostAddress set InetAddress object for ITHAKI's IP.
	 */


	public static void setHostAddress(InetAddress hostAddress) {
		UDP.hostAddress = hostAddress;
	}
	
	/**
	 * @param localPort set local port.
	 * @throws SocketException as the method tries to reestablish a
	 * 			DatagramSocket , in case the clientPort is different from 
	 * 			the current.
	 */
	public boolean setClientPort(int clientPort) {
		if (this.clientPort == clientPort) {
			return true;
		}
		try {
			connection = new DatagramSocket(clientPort);
			this.clientPort = clientPort;
			return true;
		}catch(SocketException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	
	
	
	
	
	
	
	
	
	
}
