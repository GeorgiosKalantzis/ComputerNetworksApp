package gr.auth.netpackets;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTextPane;
import javax.swing.SwingWorker;

import gr.auth.View;
import gr.auth.connections.UDP;

/**
 * 
 * @author Georgios Kalantzis
 * 
 * <h1> Class responsible for images transmission using swing worker</h1>
 *
 */

public class Image extends SwingWorker<Void, Boolean> {

	private static final int IMAGE_WIDTH = 320;
	private static final int IMAGE_HEIGHT = 240;

	private static UDP UDPConnection;
	private String requestCode;
	private int duration, packetLength;
	private boolean flowControl;
	private static JTextPane displayOutput;
	private static JLabel displayImageOutput;
	private ArrayList<Byte> displayImageBytes;

	private boolean isSuccessfull = false;
	private String status;
	private long imageTime;
	private int imageNumberOfPackets;
	private float averageImageTime, averageImageNumberOfPackets, imageSize, averageImageSize, fps;
	
	/**
	 * <h1> Constructor </h1>
	 * 
	 * @param UDPConnection UDP DatagramSocket pointing to ITHAKI
	 * @param requestCode request code needed for ITHAKI's response.
	 * @param duration duration of transmission.
	 * @param flowControl for implementing controlled transmission.
	 * @param packetLength determine the user's desired length for the packets.
	 * @param displayOutput to display to the GUI the result.
	 * @param displayImageOutput to display to the GUI the image.
	 */
	public Image(UDP UDPConnection, String requestCode, int duration, boolean flowControl, int packetLength,
			JTextPane displayOutput, JLabel displayImageOutput) {

		Image.UDPConnection = UDPConnection;
		this.requestCode = requestCode;
		this.duration = duration;
		this.packetLength = packetLength;
		this.flowControl = flowControl;
		Image.displayOutput = displayOutput;
		Image.displayImageOutput = displayImageOutput;

	}
	
	/**
	 * 
	 *  <h1> All the process required for receiving images. </h1>
	 * 
	 */

	@Override
	protected Void doInBackground(){

		boolean monitorDuration = !(duration == 0);
		byte rxbuffer[] = new byte[packetLength];

		DatagramPacket imageRequestPacket = null;
		DatagramPacket imageReceivedPacket = new DatagramPacket(rxbuffer, rxbuffer.length);
		DatagramPacket imageNextPacket = null;
		
		// Start the timing.
		long startTime = System.currentTimeMillis(); 
		
		// Required variables for collecting various measurements
		long totalImagesTime = 0;
		int numberOfImages = 0, totalImagesNumberOfPackets = 0, totalImagesSize = 0;

		averageImageTime = 0;
		averageImageNumberOfPackets = 0;
		averageImageSize = 0;
		fps = 0;
		
		// Create "Next" DatagramPacket if the user wants flow control.
		if (flowControl) {
			requestCode = requestCode + "FLOW=ON";
			imageNextPacket = new DatagramPacket("NEXT".getBytes(), "NEXT".getBytes().length, UDP.getHostAddress(),
					UDPConnection.getServerPort());

		}
		
		/*
		 *  Append the appropriate prefix to the request code,
		 *  depending on user's choice for packet's length.
		 *  
		 */
		
		if (packetLength != 128) {

			requestCode = requestCode + "UDP=" + packetLength;
		}

		requestCode = requestCode + "\r";
		
		// DatagramPacket to send to ITHAKI.
		imageRequestPacket = new DatagramPacket(requestCode.getBytes(), requestCode.getBytes().length,
				UDP.getHostAddress(), UDPConnection.getServerPort());

		
		// ------------ INITIATE COMMUNICATION PROCESS -------------------
		while (true) {
			// To save every image's bytes.
			ArrayList<Byte> loadImageBytes = new ArrayList<Byte>();

			FileOutputStream imageOutputStream = null;

			try {
				// IOStream for writing every image.
				imageOutputStream = new FileOutputStream("output/image-" + requestCode + "-" + numberOfImages + ".jpg",
						false);

			} catch (FileNotFoundException e) {
				e.printStackTrace();
				isSuccessfull = false;
				status = "Image file open failure";
				return null;

			}

			boolean nextImage = false;

			imageNumberOfPackets = 0;
			imageTime = 0;
			imageSize = 0;

			try {
				// Send the packet.
				UDPConnection.getConnection().send(imageRequestPacket);

			} catch (IOException ex1) {
				ex1.printStackTrace();
				isSuccessfull = false;
				status = "Request send failure";

				try {
					imageOutputStream.close();
				} catch (IOException ex2) {
					ex2.printStackTrace();
					isSuccessfull = false;
					status = "Image file close failure";
					return null;
				}

				return null;

			}

			long imageStartTime = System.currentTimeMillis();
			
			//---------- RECEIVING IMAGE BYTES------------------
			while (true) {
				
				try {
					// Receive packet
					UDPConnection.getConnection().receive(imageReceivedPacket);
				} catch (IOException exception1) {
					exception1.printStackTrace();
					isSuccessfull = false;
					status = "Receiving failure";

					try {
						imageOutputStream.close();
					} catch (IOException exception2) {
						exception2.printStackTrace();
						isSuccessfull = false;
						status = "Image file close failure";
						return null;

					}

					return null;
				}
				// Count packets
				++imageNumberOfPackets;

				try {
					// Write bytes to file.
					imageOutputStream.write(rxbuffer, 0, rxbuffer.length);

				} catch (IOException exception1) {
					exception1.printStackTrace();
					isSuccessfull = false;
					status = "Image file write failure";
					try {
						imageOutputStream.close();
					} catch (IOException exception2) {
						exception2.printStackTrace();
						isSuccessfull = false;
						status = "Image file close failure";
						return null;
					}
					return null;
				}
				
				

				for (int i = 0; i < rxbuffer.length; i++) {
					
					loadImageBytes.add(rxbuffer[i]);

					if (rxbuffer[i] != 0) {
						// Denote the ending delimiter for JPG images.
						if ((i < rxbuffer.length - 1) && (rxbuffer[i] == (byte) 255)
								&& (rxbuffer[i + 1] == (byte) 217)) {
							
							nextImage = true;
						}

						rxbuffer[i] = 0;
					}

				}
				
				if(imageNumberOfPackets % 20 == 0) {
					// To KiloBytes
					imageSize = (float) ((loadImageBytes.size()) / 1000.0);
					imageTime = System.currentTimeMillis() - imageStartTime;
					publish(false);
			
				}
				
				// To next iteration
				if(nextImage) {
					imageTime = System.currentTimeMillis() - imageStartTime;
					break;
				}
				// Flow control implementation , send "NEXT" packet
				if(flowControl) {
					try {
						UDPConnection.getConnection().send(imageNextPacket);
						
					}catch(IOException e) {
						
						e.printStackTrace();
						isSuccessfull = false;
						status = "Request send failure";
						
						try {
							imageOutputStream.close();
							
						} catch (IOException ex1) {
							
							ex1.printStackTrace();
							isSuccessfull = false;
							status = "Image file close failure";
							return null;
						}
						
						return null;
					}
				}
					

			}
			
			try {
				// Close the current image's IOStream.
				imageOutputStream.flush();
				imageOutputStream.close();
				
			}catch(IOException ex) {
				
				ex.printStackTrace();
				isSuccessfull = false;
				status = "Image file close failure";
				return null;
				
			}
			
			++numberOfImages;
			totalImagesTime += imageTime;
			averageImageTime = (float) totalImagesTime / (float) numberOfImages;
			totalImagesNumberOfPackets += imageNumberOfPackets;
			averageImageNumberOfPackets = (float) totalImagesNumberOfPackets / (float) numberOfImages;
			totalImagesSize += imageSize;
			averageImageSize = totalImagesSize / (float) numberOfImages;
			displayImageBytes = loadImageBytes;
			fps = (float) ((numberOfImages)/ (totalImagesTime / 1000.0));
			publish(true);
			
			if(monitorDuration) {
				if((System.currentTimeMillis() - startTime) >= (duration * 1000)) {
					break;
				}
			}

		}
		
		isSuccessfull = true;
		status = "Test finished successfully";
		return null;
	}

	@Override
	protected void process(List<Boolean> shouldRefreshImage) {
		
		displayOutput.setText("Current Image" + "\t" + "\t" + "Average" + "\n" + "Time elapsed = " 
								+ imageTime + "ms\t" + "Average image time = " + averageImageTime + "ms\n"  
								+ "Number of packets = " + imageNumberOfPackets + "\t" + "Average number of packets = " 
								+ averageImageNumberOfPackets + "\n" + "Image size = " + imageSize + " KB\t" 
								+ "Average image size = " + averageImageSize + " KB\n" + "\n" + "FPS = " + fps);
		
		
		if(shouldRefreshImage.get(0)) {
			byte[] imageBytes = new byte[displayImageBytes.size()];
			for(int i = 0 ; i < displayImageBytes.size(); i++) {
				imageBytes[i] = displayImageBytes.get(i);
			}
			
			ImageIcon image = new ImageIcon(imageBytes);
			
			BufferedImage resizedImg = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_INT_ARGB);
			
			Graphics2D graph = resizedImg.createGraphics();
			
			graph.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			graph.drawImage(image.getImage(), 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT, null);
			graph.dispose();
			image = new ImageIcon(resizedImg);

			displayImageOutput.setIcon(image);

			
			
			
		}
		
		
		
		
		
		
		
	}
	
	@Override
	protected void done() {
		View.setStatusLineText(status,
				((isSuccessfull) ? View.STATUS_LINE_ACTION_DONE : View.STATUS_LINE_ACTION_ERROR));
		View.setSubmitButtonsEnabled(true);
	}

	
	
	
	
	
	
	
	
	
	
	

}
