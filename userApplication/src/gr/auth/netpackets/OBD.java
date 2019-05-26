package gr.auth.netpackets;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.SwingWorker;

import gr.auth.View;
import gr.auth.connections.TCP;
import gr.auth.connections.UDP;

/**
 * <h1> OBD class responsible for collecting real world source data ,hosted at ITHAKI,
 * from a vehicle's electronic controller unit (ECU) </h1>
 * 
 * <br> Note: Contains two constructors , whether the user wants UDP or TCP socket.
 * 
 * @author Georgios Kalantzis
 *
 */

public class OBD extends SwingWorker<Void,Void>{
	
	private static final int MAX_RESPONSE_LENGTH = 12;
	private static final String[] OBD_UDP_REQUEST_CODES = new String[] { "01 1F", "01 0F", "01 11", "01 0C", "01 0D",
			"01 05" };
	private static final int ENGINE_RUN_TIME = 0;
	private static final int INTAKE_AIR_TEMPERATURE = 1;
	private static final int THROTTLE_POSITION = 2;
	private static final int ENGINE_RPM = 3;
	private static final int VEHICLE_SPEED = 4;
	private static final int COOLANT_TEMPERATURE = 5;

	private final TCP TCPConnection;
	private final UDP UDPConnection;
	private String requestCode;
	private final boolean useUDP; // If we want to receive packets via UDP.
	private final int duration;
	private final JLabel engineRunTimeOutput;
	private final JLabel airTempOutput;
	private final JLabel throttlePositionOutput;
	private final JLabel engineRPMOutput;
	private final JLabel speedOutput;
	private final JLabel coolantTemperatureOutput;
	private final JLabel packetsTotalTimeOutput;

	private boolean isSuccessfull = false;
	private String status;
	private String[] outputData = new String[7];
	
	private ArrayList<Integer> engineRunTimeValues = new ArrayList<>(), intakeAirTemperatureValues = new ArrayList<>(),
			vehicleSpeedValues = new ArrayList<>(), coolantTemperatureValues = new ArrayList<>();
	
	private ArrayList<Float> throttlePositionValues = new ArrayList<>(), engineRPMValues = new ArrayList<>();
	
	/**
	 * <h1> First UDP constructor </h1>
	 * 
	 * @param UDPConnection
	 * @param requestCode
	 * @param duration
	 * @param engineRunTimeOutput
	 * @param airTempOutput
	 * @param throttlePositionOutput
	 * @param engineRPMOutput
	 * @param speedOutput
	 * @param coolantTemperatureOutput
	 * @param packetsTotalTimeOutput
	 */
	
	public OBD(UDP UDPConnection, String requestCode, int duration, JLabel engineRunTimeOutput,
			JLabel airTempOutput, JLabel throttlePositionOutput, JLabel engineRPMOutput,
			JLabel speedOutput, JLabel coolantTemperatureOutput,
			JLabel packetsTotalTimeOutput) {
		this.TCPConnection = null;
		this.UDPConnection = UDPConnection;
		this.requestCode = requestCode;
		this.useUDP = true;
		this.duration = duration;
		this.engineRunTimeOutput = engineRunTimeOutput;
		this.airTempOutput = airTempOutput;
		this.throttlePositionOutput = throttlePositionOutput;
		this.engineRPMOutput = engineRPMOutput;
		this.speedOutput = speedOutput;
		this.coolantTemperatureOutput = coolantTemperatureOutput;
		this.packetsTotalTimeOutput = packetsTotalTimeOutput;
	}
	
	/**
	 * <h1> Second TCP constructor </h1>
	 * 
	 * @param TCPConnection
	 * @param requestCode
	 * @param duration
	 * @param engineRunTimeOutput
	 * @param airTempOutput
	 * @param throttlePositionOutput
	 * @param engineRPMOutput
	 * @param speedOutput
	 * @param coolantTemperatureOutput
	 * @param packetsTotalTimeOutput
	 */
	
	public OBD(TCP TCPConnection, String requestCode, int duration, JLabel engineRunTimeOutput,
			JLabel airTempOutput, JLabel throttlePositionOutput, JLabel engineRPMOutput,
			JLabel speedOutput, JLabel coolantTemperatureOutput,
			JLabel packetsTotalTimeOutput) {
		this.TCPConnection = TCPConnection;
		this.UDPConnection = null;
		this.requestCode = requestCode;
		this.useUDP = false;
		this.duration = duration;
		this.engineRunTimeOutput = engineRunTimeOutput;
		this.airTempOutput = airTempOutput;
		this.throttlePositionOutput = throttlePositionOutput;
		this.engineRPMOutput = engineRPMOutput;
		this.speedOutput = speedOutput;
		this.coolantTemperatureOutput = coolantTemperatureOutput;
		this.packetsTotalTimeOutput = packetsTotalTimeOutput;
	}
	
	/**
	 * <h1> Extract the values from the response with their corresponding formula </h1>
	 * 
	 * @param rxbuffer
	 * @param bytesReturned
	 * @param loopIndex
	 * @return
	 */
	
	

	private String getValueFromResponse(byte[] rxbuffer, int bytesReturned, int loopIndex) {
		
		int returnValueXX = 0, returnValueYY = 0;
		String value = "";
		
		// Identify the return values
		if (bytesReturned == 9) {
			String hexValueXX = "" + (char) rxbuffer[6] + (char) rxbuffer[7];

			returnValueXX = Integer.parseInt(hexValueXX, 16);
			
		}
			
			
		if (bytesReturned == 12) {
			String hexValueXX = "" + (char) rxbuffer[6] + (char) rxbuffer[7];
			String hexValueYY = "" + (char) rxbuffer[9] + (char) rxbuffer[10];

			returnValueXX = Integer.parseInt(hexValueXX, 16);
			returnValueYY = Integer.parseInt(hexValueYY, 16);
		}
		
		// Calculate the corresponding formulas
		switch (loopIndex) {
		case ENGINE_RUN_TIME:
			value = "" + 256 * returnValueXX + returnValueYY;
			engineRunTimeValues.add(256 * returnValueXX + returnValueYY);
			break;
		case INTAKE_AIR_TEMPERATURE:
			value = "" + (returnValueXX - 40);
			intakeAirTemperatureValues.add(returnValueXX - 40);
			break;
		case THROTTLE_POSITION:
			value = "" + (100 * returnValueXX / 255);
			throttlePositionValues.add((float) (100 * returnValueXX / 255));
			break;
		case ENGINE_RPM:
			value = "" + ((256 * returnValueXX + returnValueYY) / 4);
			engineRPMValues.add((float) ((256 * returnValueXX + returnValueYY) / 4));
			break;
		case VEHICLE_SPEED:
			value = "" + returnValueXX;
			vehicleSpeedValues.add(returnValueXX);
			break;
		case COOLANT_TEMPERATURE:
			value = "" + (returnValueXX - 40);
			coolantTemperatureValues.add(returnValueXX - 40);
			break;
		default:
			break;
		}
		
		return value;


		
	}
	
	
	@Override
	protected Void doInBackground() {
		
		int vehicleOutputFileCounter = 0;
		byte rxbuffer[] = new byte[MAX_RESPONSE_LENGTH];
		
		long totalTimeElapsed = 0, OBDPacketStart = 0;
		FileOutputStream vehicleOutputStream = null;
		File vehicleOutputFile = null;
		
		// Separate the files for the consecutive tests.
		do {
			vehicleOutputFile = new File("output/vehicle_" + (useUDP ? requestCode : "TCP")
					+ (vehicleOutputFileCounter == 0 ? "" : "_" + vehicleOutputFileCounter) + ".csv");
			if (!vehicleOutputFile.isFile()) {
				break;
			}
			++vehicleOutputFileCounter;
		} while (true);
		

		try {
			vehicleOutputStream = new FileOutputStream(vehicleOutputFile, false);
		} catch (FileNotFoundException exception) {
			exception.printStackTrace();
			isSuccessfull = false;
			status = "Vehicle file open failure!";
			return null;
		}

		// UDP connection
		if (useUDP) {
			DatagramPacket OBDRequestPacket = null,
					OBDResponsePacket = new DatagramPacket(rxbuffer, rxbuffer.length);
			
			// Users specifies the desired duration of transmission.
			while (totalTimeElapsed < duration * 1000) {
				
				for (int i = 0; i < OBD_UDP_REQUEST_CODES.length; ++i) {
					
					int bytesReturned = MAX_RESPONSE_LENGTH;
					String OBDRequestCode = requestCode + "OBD=" + OBD_UDP_REQUEST_CODES[i];
					
					OBDRequestPacket = new DatagramPacket(OBDRequestCode.getBytes(), OBDRequestCode.getBytes().length,
							UDP.getHostAddress(), UDPConnection.getServerPort());
					
					// Send the request packet.
					try {
						UDPConnection.getConnection().send(OBDRequestPacket);
						
					} catch (IOException exception) {
						exception.printStackTrace();
						try {
							vehicleOutputStream.close();
						} catch (IOException inception) {
							inception.printStackTrace();
							isSuccessfull = false;
							status = "Vehicle file close failure, after request send failure!";
							return null;
						}
						isSuccessfull = false;
						status = "Request send failure!";
						return null;
					}

					OBDPacketStart = System.currentTimeMillis();
					
					try {
						// Receive packet
						UDPConnection.getConnection().receive(OBDResponsePacket);
						
					} catch (IOException exception) {
						exception.printStackTrace();
						try {
							vehicleOutputStream.close();
						} catch (IOException inception) {
							inception.printStackTrace();
							isSuccessfull = false;
							status = "Vehicle file close failure, after response receive failure!";
							return null;
						}
						isSuccessfull = false;
						status = "Response receive failure!";
						return null;
					}
					
					totalTimeElapsed += System.currentTimeMillis() - OBDPacketStart;
					
					// Calculate the the number of bytes returned.
					for (int j = MAX_RESPONSE_LENGTH - 1; j > 0; --j) {
						
						if (rxbuffer[j] == 0) {
							--bytesReturned;
						}
					}
					
					// Use of getValuefromResponse() to calculate the formulas.
					outputData[i] = getValueFromResponse(rxbuffer, bytesReturned + 1, i);

					for (int j = 0; j < MAX_RESPONSE_LENGTH; ++j) {
						rxbuffer[j] = 0;
					}
				}

				outputData[6] = "" + totalTimeElapsed;
				publish();
			}
			
		// TCP connection
		} else {
			
			// Users specifies the desired duration of transmission.
			while (totalTimeElapsed < duration * 1000) {
				
				for (int i = 0; i < OBD_UDP_REQUEST_CODES.length; ++i) {
					int bytesReturned = 0;

					try {
						TCPConnection.getConnection().getOutputStream().write((OBD_UDP_REQUEST_CODES[i] + (char) 13).getBytes());
						
					} catch (IOException exception) {
						exception.printStackTrace();
						try {
							vehicleOutputStream.close();
						} catch (IOException inception) {
							inception.printStackTrace();
							isSuccessfull = false;
							status = "Vehicle file close failure, after request send failure!";
							return null;
						}
						isSuccessfull = false;
						status = "Request send failure!";
						return null;
					}
					OBDPacketStart = System.currentTimeMillis();
					try {
						bytesReturned = TCPConnection.getConnection().getInputStream().read(rxbuffer);
					} catch (IOException exception) {
						exception.printStackTrace();
						try {
							vehicleOutputStream.close();
						} catch (IOException inception) {
							inception.printStackTrace();
							isSuccessfull = false;
							status = "Vehicle file close failure, after response receive failure!";
							return null;
						}
						isSuccessfull = false;
						status = "Response receive failure!";
						return null;
					}
					totalTimeElapsed += System.currentTimeMillis() - OBDPacketStart;

					if (bytesReturned == -1) {
						try {
							vehicleOutputStream.close();
						} catch (IOException inception) {
							inception.printStackTrace();
							isSuccessfull = false;
							status = "OBD file close failure, after connection failure!";
							return null;
						}
						isSuccessfull = false;
						status = "Server closed the connection";
						return null;
						
					} 

					outputData[i] = getValueFromResponse(rxbuffer, bytesReturned, i);
				}
				outputData[6] = "" + totalTimeElapsed;
				publish();
			}
		}
		try {

			for (Integer engineRunTime : engineRunTimeValues) {
				vehicleOutputStream.write((engineRunTime + "\t").getBytes(), 0,
						(engineRunTime + "\t").getBytes().length);
			}
			vehicleOutputStream.write(("\n").getBytes(), 0, ("\n").getBytes().length);

			for (Integer intakeAirTemperatureValues : intakeAirTemperatureValues) {
				vehicleOutputStream.write((intakeAirTemperatureValues + "\t").getBytes(), 0,
						(intakeAirTemperatureValues + "\t").getBytes().length);
			}
			vehicleOutputStream.write(("\n").getBytes(), 0, ("\n").getBytes().length);

			for (Float throttlePositionValues : throttlePositionValues) {
				vehicleOutputStream.write((throttlePositionValues + "\t").getBytes(), 0,
						(throttlePositionValues + "\t").getBytes().length);
			}
			vehicleOutputStream.write(("\n").getBytes(), 0, ("\n").getBytes().length);

			for (Float engineRPMValues : engineRPMValues) {
				vehicleOutputStream.write((engineRPMValues + "\t").getBytes(), 0,
						(engineRPMValues + "\t").getBytes().length);
			}
			vehicleOutputStream.write(("\n").getBytes(), 0, ("\n").getBytes().length);

			for (Integer vehicleSpeedValues : vehicleSpeedValues) {
				vehicleOutputStream.write((vehicleSpeedValues + "\t").getBytes(), 0,
						(vehicleSpeedValues + "\t").getBytes().length);
			}
			vehicleOutputStream.write(("\n").getBytes(), 0, ("\n").getBytes().length);

			for (Integer coolantTemperatureValues : coolantTemperatureValues) {
				vehicleOutputStream.write((coolantTemperatureValues + "\t").getBytes(), 0,
						(coolantTemperatureValues + "\t").getBytes().length);
			}
		} catch (IOException exception) {
			exception.printStackTrace();
			try {
				vehicleOutputStream.close();
			} catch (IOException inception) {
				inception.printStackTrace();
				isSuccessfull = false;
				status = "Vehicle file close failure, after buffer write failure!";
				return null;
			}
			isSuccessfull = false;
			status = "Vehicle file buffer write failure!";
			return null;
		}

		try {
			vehicleOutputStream.flush();
			vehicleOutputStream.close();
		} catch (IOException exception) {
			exception.printStackTrace();
			isSuccessfull = false;
			status = "Vehicle file buffer flush/close failure!";
			return null;
		}
		isSuccessfull = true;
		status = "Test finished successfully.";
		return null;
	}
	
	@Override
	protected void process(List<Void> notInUse) {
		engineRunTimeOutput.setText(outputData[0] + " s");
		airTempOutput.setText(outputData[1] + " °C");
		throttlePositionOutput.setText(outputData[2] + " %");
		engineRPMOutput.setText(outputData[3] + " RPM");
		speedOutput.setText(outputData[4] + " Km/h");
		coolantTemperatureOutput.setText(outputData[5] + " °C");
		packetsTotalTimeOutput.setText(outputData[6] + " ms");
	}

	@Override
	protected void done() {
		View.setStatusLineText(status,
				((isSuccessfull) ? View.STATUS_LINE_ACTION_DONE : View.STATUS_LINE_ACTION_ERROR));
		View.setSubmitButtonsEnabled(true);
	}

	
	


		

	
	




	
	

}
