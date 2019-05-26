package gr.auth.netpackets;

import java.io.*;
import java.net.*;
import java.util.List;
import javax.swing.*;

import gr.auth.View;
import gr.auth.connections.UDP;

/**
 * 
 * <h1>Echo testing process using swing worker.</h1>
 * 
 * @author Georgios Kalantzis 
 *
 */

public class Echo extends SwingWorker<Void, Long> {

	private static final int RESPONSE_BUFFER_LENGTH = 2048;

	private static UDP UDPConnection;

	private String requestCode;

	private int duration;

	private int numberOfPackets;

	private boolean getTemperature;

	private static JTextPane displayOutput;

	private boolean isSuccessfull = false;

	private String status;

	/**
	 * <h1>Constructor</h1>
	 * 
	 * @param UDPConnection   UDP object needed for requesting/receiving packets, composition with UDP class.
	 * @param requestCode     the code necessary for communication with ITHAKI.
	 * @param duration        duration of communication.
	 * @param numberOfPackets for counting the packets.
	 * @param getTemperature  boolean for computing the temperature also
	 * @param displayOutput	  results displayed to the app
	 */

	public Echo(UDP UDPConnection, String requestCode, int duration, int numberOfPackets, boolean getTemperature,
			JTextPane displayOutput) {

		Echo.UDPConnection = UDPConnection;
		this.requestCode = requestCode;
		this.duration = duration;
		this.numberOfPackets = numberOfPackets;
		this.getTemperature = getTemperature;
		Echo.displayOutput = displayOutput;

	}

	@Override
	protected Void doInBackground() throws Exception {

		int currentPacketNumber = 0;

		int echoOutputFileCounter = 0;

		boolean monitorNumberOfPackets = !(numberOfPackets == 0); // If the user wants to specify the number of packets to be transmitted.

		boolean monitorDuration = !(duration == 0); // If the user wants to specify the duration of communication.

		byte rxbuffer[] = new byte[RESPONSE_BUFFER_LENGTH]; // Response to be stored.

		DatagramPacket echoRequestPacket = null;

		DatagramPacket echoReceivedPacket = new DatagramPacket(rxbuffer, rxbuffer.length);

		long start = System.currentTimeMillis(); // Start timing

		long packetStartTime = 0, packetEndTime = 0, runningTime = 0;

		String temperature = null;

		FileOutputStream echoOutputStream = null;

		File echoOutputFile = null;

		

		do {

			echoOutputFile = new File("output/echo--" + requestCode
					+ (echoOutputFileCounter == 0 ? "" : "-" + echoOutputFileCounter) + ".csv");
			
			/*
			 * if(!echoOutputFile.exists()) { echoOutputFile.createNewFile(); }
			 */

			if (!echoOutputFile.isFile()) {
				break;
			}

			++echoOutputFileCounter;

		} while (true);

		// Initialize IOStream to the file.
		try {
			echoOutputStream = new FileOutputStream(echoOutputFile); 
			
		} catch (IOException e) {
			e.printStackTrace();
			isSuccessfull = false;
			status = "File open failure";
			return null;

		}

		// Update request code to receive temperature measurements
		if (getTemperature) {
			requestCode = requestCode + "T00";
		}
		// Add escape character
		requestCode = requestCode + "\r";

		// DatagramPacket to deliver the request code to ITHAKI
		echoRequestPacket = new DatagramPacket(requestCode.getBytes(), requestCode.getBytes().length,
				UDP.getHostAddress(), UDPConnection.getServerPort());

		// ----- INITIATE COMMUNATICATION PROCCESS --------//

		while (true) {
			
			// Calculate number of packets
			currentPacketNumber++;

			try {
				UDPConnection.getConnection().send(echoRequestPacket); // Send request to ITHAKI for receiving packets

			} catch (SocketException e) {

				e.printStackTrace();

				try {

					echoOutputStream.close();

				} catch (IOException ex) {

					ex.printStackTrace();
					isSuccessfull = false;
					status = "File close failure";

				}

				isSuccessfull = false;
				status = "Request send failure!";
				return null;

			}

			try {
				
				// To calculate arrival time of the packets
				packetStartTime = System.currentTimeMillis();
				UDPConnection.getConnection().receive(echoReceivedPacket);
				
				

			} catch (SocketException ex) {
				ex.printStackTrace();

				try {
					echoOutputStream.close();

				} catch (IOException ex1) {

					ex1.printStackTrace();
					isSuccessfull = false;
					status = "file close failure";
					return null;
				}

				isSuccessfull = false;
				status = "receive failure";
				return null;
			}

			String response = new String(rxbuffer);

			packetEndTime = System.currentTimeMillis();

			long packetArrivalTime = packetEndTime - packetStartTime;

			runningTime += packetArrivalTime;

			try {
				echoOutputStream.write((packetArrivalTime + "\t").getBytes(), 0,
						(packetArrivalTime + "\t").getBytes().length);

			} catch (IOException exception) {

				exception.printStackTrace();

				try {
					echoOutputStream.close();

				} catch (IOException inception) {

					inception.printStackTrace();
					isSuccessfull = false;
					status = "Echo file close failure, after buffer write failure!";
					return null;
				}
				isSuccessfull = false;
				status = "Echo file buffer write failure!";
				return null;
			}

			if (getTemperature) {
				if (currentPacketNumber == 1) {
					temperature = response.substring(response.indexOf("+"), response.indexOf(" C"));

					publish(packetArrivalTime, runningTime / currentPacketNumber, Long.parseLong(temperature));

				}
			} else {

				publish(packetArrivalTime, runningTime / currentPacketNumber, null);
			}

			if (monitorNumberOfPackets) {

				if (numberOfPackets == currentPacketNumber) {

					try {
						echoOutputStream.flush();
						echoOutputStream.close();

					} catch (IOException exception) {

						exception.printStackTrace();
						isSuccessfull = false;
						status = "Close failure";
						return null;
					}
					isSuccessfull = true;
					status = "Finished successfully";
					return null;

				}

			}

			if (monitorDuration) {
				if ((packetEndTime - start) >= (duration * 1000)) {
					try {
						echoOutputStream.flush();
						echoOutputStream.close();

					} catch (IOException exception) {
						exception.printStackTrace();
						isSuccessfull = false;
						status = "Close file failure";
						return null;
					}

					isSuccessfull = true;
					status = "Finished successfully";
					return null;

				}
			}

		}

	}

	@Override
	protected void process(List<Long> measurements) {
		
		if (measurements.size() != 3) {
			return;
		}else {

			if (measurements.get(2) == null) {
				displayOutput.setText("Last packet ping time:\t" + measurements.get(0) + " ms\nAverage packet ping time:\t"
						+ measurements.get(1) + " ms\nTemperature = NaN");
			} else {
				displayOutput.setText("Last packet ping time:\t" + measurements.get(0) + " ms\nAverage packet ping time:\t"
						+ measurements.get(1) + " ms\nTemperature = " + measurements.get(2) + " °C");
			}
			
		}
		
		


	}

	@Override
	protected void done() {

		View.setStatusLineText(status,
				((isSuccessfull) ? View.STATUS_LINE_ACTION_DONE : View.STATUS_LINE_ACTION_ERROR));
		View.setSubmitButtonsEnabled(true);

	}

}
