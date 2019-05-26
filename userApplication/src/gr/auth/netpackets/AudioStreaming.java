package gr.auth.netpackets;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.swing.JProgressBar;
import javax.swing.JTextPane;
import javax.swing.SwingWorker;



import gr.auth.View;
import gr.auth.connections.UDP;

/**
 * 
 * <h1> Class responsible for audio streaming using UDP. Extends swing worker </h1>
 * 
 * @author Georgios Kalantzis
 *
 */

public class AudioStreaming extends SwingWorker<Void, Void> {
	
	private static final int NON_ADAPTIVELY_QUANTISED_PACKET_LENGTH = 128;
	private static final int ADAPTIVELY_QUANTISED_PACKET_LENGTH = 132;
	private static final int SAMPLE_RATE = 8000;
	private static final int NUMBER_OF_CHANNELS = 1;
	private static final boolean IS_SIGNED = true;
	private static final boolean IS_BIG_ENDIAN = false;

	private static UDP UDPConnection;
	private String requestCode, audioPool;
	private int specificSampleIndex, numberOfPacketsToRequest, betaParameter, qParameter;
	private boolean isAdaptivelyQuantised, requestSpecificSample;
	private static JTextPane displayOutput;
	private JProgressBar progressBarAudioStreamer, progressBarAudioPlayer;

	private boolean isSuccessfull = false;
	private String status;
	private int numberOfBytesWritenToBuffer = 0;
	private int bytesPerPacket;
	private float streamingProgress = 0;
	private float streamingSpeed = 0;
	private long playingProgress = 0;
	
	/**
	 * 
	 * <h1> Constructor </h1>
	 * 
	 * <br> The number of bytes per packet depends on whether the decoding 
	 * 		is adaptively quantised or not.
	 * 
	 * @param UDPConnection
	 * @param requestCode
	 * @param shouldRequestSpecificSample
	 * @param specificSampleIndex
	 * @param audioPool
	 * @param numberOfPacketsToRequest
	 * @param isAdaptivelyQuantised
	 * @param betaParameter
	 * @param qParameter
	 * @param runtimeStatsOutput
	 * @param progressBarAudioStreamer
	 * @param progressBarAudioPlayer
	 */

	public AudioStreaming(UDP UDPConnection, String requestCode, boolean shouldRequestSpecificSample,
			int specificSampleIndex, String audioPool, int numberOfPacketsToRequest, boolean isAdaptivelyQuantised,
			int betaParameter, int qParameter, JTextPane displayOutput, JProgressBar progressBarAudioStreamer,
			JProgressBar progressBarAudioPlayer) {
		
		AudioStreaming.UDPConnection = UDPConnection;
		this.requestCode = requestCode;
		this.requestSpecificSample = shouldRequestSpecificSample;
		this.specificSampleIndex = specificSampleIndex;
		this.audioPool = audioPool;
		this.numberOfPacketsToRequest = numberOfPacketsToRequest;
		this.isAdaptivelyQuantised = isAdaptivelyQuantised;
		this.betaParameter = betaParameter;
		this.qParameter = qParameter;
		AudioStreaming.displayOutput = displayOutput;
		this.progressBarAudioStreamer = progressBarAudioStreamer;
		this.progressBarAudioPlayer = progressBarAudioPlayer;

		if (!isAdaptivelyQuantised) {
			bytesPerPacket = 128 * 2;
		} else {
			bytesPerPacket = 128 * 4;
		}
	}
	
	/**
	 * 
	 * <br> Implement an another thread for the management of the elastic buffer.
	 * 
	 */
	

	@Override
	protected Void doInBackground() {
		
		int numberOfBytesPassedToLineOut = 0;
		
		byte[] decodedDPCMBuffer = new byte[numberOfPacketsToRequest * bytesPerPacket];
		
		AudioFormat linearPCM = new AudioFormat(SAMPLE_RATE, qParameter, NUMBER_OF_CHANNELS, IS_SIGNED, IS_BIG_ENDIAN); // Format Object.
		
		AudioStreamer audioStreamer = new AudioStreamer(decodedDPCMBuffer);
		Thread audioStreamerThread = new Thread(audioStreamer);
		SourceDataLine lineOut;
		
		// Start the thread.
		audioStreamerThread.start();
		while (numberOfBytesWritenToBuffer < ((8192 - 7500) * (numberOfPacketsToRequest / 32))) {
			publish();
			// wait a bit
		}

		playingProgress = 0;
		try {
			lineOut = AudioSystem.getSourceDataLine(linearPCM);
			lineOut.open(linearPCM, 2560);

			lineOut.start();

			while (audioStreamerThread.isAlive() && playingProgress < numberOfPacketsToRequest * bytesPerPacket) {
				
				playingProgress = lineOut.getLongFramePosition();
				if (isAdaptivelyQuantised) {
					playingProgress *= 2;
				}
				publish();

				if (numberOfBytesPassedToLineOut != numberOfBytesWritenToBuffer) {
					
					numberOfBytesPassedToLineOut += lineOut.write(decodedDPCMBuffer, numberOfBytesPassedToLineOut,
							numberOfBytesWritenToBuffer - numberOfBytesPassedToLineOut);
				}
			}

			lineOut.stop();
			lineOut.close();
		} catch (LineUnavailableException exception) {
			exception.printStackTrace();
		}
		streamingProgress = 100;
		publish();

		isSuccessfull = true;
		status = "Test finished successfully.";
		return null;
	}

	@Override
	protected void process(List<Void> notInUse) {
		displayOutput.setText("" + streamingSpeed + "KB/s");
		progressBarAudioStreamer.setValue((int) (streamingProgress + ((streamingProgress + 1) / 100)));
		progressBarAudioPlayer.setValue((int) (1000 * ((float) playingProgress / (numberOfPacketsToRequest * bytesPerPacket))));
	}

	@Override
	protected void done() {
		View.setStatusLineText(status,
				((isSuccessfull) ? View.STATUS_LINE_ACTION_DONE : View.STATUS_LINE_ACTION_ERROR));
		View.setSubmitButtonsEnabled(true);
	}
	/**
	 * 
	 * Inner class implements Runnable for the task of audioStreamerThread.
	 * 
	 * @author Georgios Kalantzis
	 *
	 */

	class AudioStreamer implements Runnable {
		private byte[] decodedDPCMBuffer;
		private int[] meansArray = null, betasArray = null;
		
		/**
		 * <h1> Constructor </h1>
		 * @param decodedDPCMBuffer
		 */
		public AudioStreamer(byte[] decodedDPCMBuffer) {
			this.decodedDPCMBuffer = decodedDPCMBuffer;
		}
		
		/**
		 * DPCM decoding process.
		 * 
		 * @param packetBytes
		 * @param decodedPacket
		 * @param beta
		 * @param packetIndex
		 */
		private void decodeDPCMPacket(byte[] packetBytes, byte[] decodedPacket, int beta, int packetIndex) {
			
			for (int i = 0; i < packetBytes.length; i++) {
				
				decodedPacket[packetIndex * bytesPerPacket + 2 * i] = (byte) ((((packetBytes[i] >> 4) & 0x0F) - 8)
						+ beta * ((packetIndex > 0) ? decodedPacket[packetIndex * bytesPerPacket + 2 * i - 1] : 0));
				decodedPacket[packetIndex * bytesPerPacket + 2 * i + 1] = (byte) (((packetBytes[i] & 0x0F) - 8)
						+ beta * decodedPacket[packetIndex * bytesPerPacket + 2 * i]);
			}
		}
		
		/**
		 * AQDPCM decoding process.
		 * 
		 * @param packetBytes
		 * @param decodedPacket
		 * @param packetIndex
		 * @param previousNibble
		 * @return
		 */

		private int decodeAQDPCMPacket(byte[] packetBytes, byte[] decodedPacket, int packetIndex, int previousNibble) {
			
			int nibble = previousNibble, mean = 0, beta = 1;

			{
				byte[] tempByte = new byte[4];
				byte meanSign = (byte) ((packetBytes[1] & 0x80) != 0 ? 0xff : 0x00);
				tempByte[3] = meanSign;
				tempByte[2] = meanSign;
				tempByte[1] = packetBytes[1];
				tempByte[0] = packetBytes[0];

				mean = ByteBuffer.wrap(tempByte).order(ByteOrder.LITTLE_ENDIAN).getInt();

				byte betaSign = (byte) ((packetBytes[3] & 0x80) != 0 ? 0xff : 0x00);
				tempByte[3] = betaSign;
				tempByte[2] = betaSign;
				tempByte[1] = packetBytes[3];
				tempByte[0] = packetBytes[2];

				beta = ByteBuffer.wrap(tempByte).order(ByteOrder.LITTLE_ENDIAN).getInt();

				meansArray[packetIndex] = mean;
				betasArray[packetIndex] = beta;
			}

			for (int i = 4; i < 132; ++i) {
				int upperDifference = (((packetBytes[i] >>> 4) & 0x0f) - 8) * beta,
						lowerDifference = ((packetBytes[i] & 0x0f) - 8) * beta;

				int firstSamplePair = upperDifference + nibble + mean;
				int secondSamplePair = lowerDifference + upperDifference + mean;
				nibble = lowerDifference;

				decodedPacket[packetIndex * 512 + 4 * (i - 4)] = (byte) (firstSamplePair);
				decodedPacket[packetIndex * 512 + 4 * (i - 4) + 1] = (byte) (firstSamplePair / 256 > 127 ? 127
						: firstSamplePair / 256 < -128 ? -128 : firstSamplePair / 256);
				decodedPacket[packetIndex * 512 + 4 * (i - 4) + 2] = (byte) (secondSamplePair);
				decodedPacket[packetIndex * 512 + 4 * (i - 4) + 3] = (byte) (secondSamplePair / 256 > 127 ? 127
						: secondSamplePair / 256 < -128 ? -128 : secondSamplePair / 256);
			}
			return nibble;
		}
		/**
		 * 
		 * The thread's task.
		 * 
		 */

		@Override
		public void run() {
			
			int numberOfPackets = 0, prevNibble = 0, audioOutputFileCounter = 0;
			byte[] rxbuffer = null;
			DatagramPacket audioRequestPacket = null, audioResponsePacket = null;
			FileOutputStream audioDiffStream = null, audioOutputStream = null, audioParametersStream = null;
			File audioDiffFile = null, audioOutputFile = null, audioParametersFile = null;

			// Counter for the different files while running tests.
			do {
				audioDiffFile = new File("output/audioDiff_" + requestCode
						+ (audioOutputFileCounter == 0 ? "" : "_" + audioOutputFileCounter) + ".csv");
				audioOutputFile = new File("output/audio_" + requestCode
						+ (audioOutputFileCounter == 0 ? "" : "_" + audioOutputFileCounter) + ".csv");
				if (isAdaptivelyQuantised) {
					audioParametersFile = new File("output/audioParams_" + requestCode
							+ (audioOutputFileCounter == 0 ? "" : "_" + audioOutputFileCounter) + ".csv");
				}
				if (!audioDiffFile.isFile() && !audioOutputFile.isFile()
						&& (!isAdaptivelyQuantised || !audioParametersFile.isFile())) {
					break;
				}
				++audioOutputFileCounter;
			} while (true);
			
			// Create the file streams.
			try {
				audioDiffStream = new FileOutputStream(audioDiffFile, false);
				audioOutputStream = new FileOutputStream(audioOutputFile, false);
				if (isAdaptivelyQuantised) {
					audioParametersStream = new FileOutputStream(audioParametersFile, false);
				}
			} catch (FileNotFoundException exception) {
				exception.printStackTrace();
				isSuccessfull = false;
				status = "File open failure!";
				return;
			}
			
			// Create the corresponding buffers to receive the bytes.
			if (!isAdaptivelyQuantised) {
				rxbuffer = new byte[NON_ADAPTIVELY_QUANTISED_PACKET_LENGTH];
				audioResponsePacket = new DatagramPacket(rxbuffer, rxbuffer.length);
			} else {
				meansArray = new int[numberOfPacketsToRequest];
				betasArray = new int[numberOfPacketsToRequest];
				rxbuffer = new byte[ADAPTIVELY_QUANTISED_PACKET_LENGTH];
				requestCode = requestCode + "AQ";
				audioResponsePacket = new DatagramPacket(rxbuffer, rxbuffer.length);
			}

			if (requestSpecificSample) {
				requestCode = requestCode + "L"
						+ (specificSampleIndex < 10 ? "0" + specificSampleIndex : specificSampleIndex);
			}

			requestCode = requestCode + audioPool + (numberOfPacketsToRequest < 100
					? "0" + (numberOfPacketsToRequest < 10 ? "0" + numberOfPacketsToRequest : numberOfPacketsToRequest)
					: numberOfPacketsToRequest);

			// Request packet.
			audioRequestPacket = new DatagramPacket(requestCode.getBytes(), requestCode.getBytes().length,
					UDP.getHostAddress(), UDPConnection.getServerPort());
			try {
				// Send it.
				UDPConnection.getConnection().send(audioRequestPacket);
			} catch (IOException exception) {
				exception.printStackTrace();
				try {
					audioDiffStream.close();
					audioOutputStream.close();
					if (isAdaptivelyQuantised) {
						audioParametersStream.close();
					}
				} catch (IOException inception) {
					inception.printStackTrace();
					isSuccessfull = false;
					status = "File close failure, after request send failure!";
					return;
				}
				isSuccessfull = false;
				status = "Request send failure!";
				return;
			}

			while (true) {
				// Start timing.
				long packetDownloadTime = 0, packetStartTime = System.currentTimeMillis();

				try {
					// Receive packet.
					UDPConnection.getConnection().receive(audioResponsePacket);
				} catch (IOException exception) {
					exception.printStackTrace();
					try {
						audioDiffStream.close();
						audioOutputStream.close();
						if (isAdaptivelyQuantised) {
							audioParametersStream.close();
						}
					} catch (IOException inception) {
						inception.printStackTrace();
						isSuccessfull = false;
						status = "File close failure, after response receive failure!";
						return;
					}
					isSuccessfull = false;
					status = "Response receive failure!";
					return;
				}

				packetDownloadTime = System.currentTimeMillis() - packetStartTime;
				
				// Write to file stream.
				for (byte audioSample : rxbuffer) {
					try {
						audioDiffStream.write((audioSample + "\t").getBytes(), 0,
								(audioSample + "\t").getBytes().length);
					} catch (IOException exception) {
						exception.printStackTrace();
						try {
							audioDiffStream.close();
							audioOutputStream.close();
							if (isAdaptivelyQuantised) {
								audioParametersStream.close();
							}
						} catch (IOException inception) {
							inception.printStackTrace();
							isSuccessfull = false;
							status = "File close failure, after buffer write failure!";
							return;
						}
						isSuccessfull = false;
						status = "Diff buffer write failure!";
						return;
					}
				}

				if (!isAdaptivelyQuantised) {
					decodeDPCMPacket(rxbuffer, decodedDPCMBuffer, betaParameter, numberOfPackets);
				} else {
					prevNibble = decodeAQDPCMPacket(rxbuffer, decodedDPCMBuffer, numberOfPackets, prevNibble);
				}
				++numberOfPackets;

				streamingProgress = ((float) numberOfPackets / (float) numberOfPacketsToRequest) * 100;
				streamingSpeed = (float) (2 * rxbuffer.length) / (float) (packetDownloadTime);

				numberOfBytesWritenToBuffer = numberOfPackets * bytesPerPacket;

				if (numberOfPackets == numberOfPacketsToRequest) {
					break;
				}
			}

			try {
				for (byte audioSample : decodedDPCMBuffer) {
					audioOutputStream.write((audioSample + "\t").getBytes(), 0, (audioSample + "\t").getBytes().length);
				}

				if (isAdaptivelyQuantised) {
					for (int mean : meansArray) {
						audioParametersStream.write((mean + "\t").getBytes(), 0, (mean + "\t").getBytes().length);
					}
					audioParametersStream.write(("\n").getBytes(), 0, ("\n").getBytes().length);

					for (int beta : betasArray) {
						audioParametersStream.write((beta + "\t").getBytes(), 0, (beta + "\t").getBytes().length);
					}
				}

				audioDiffStream.flush();
				audioDiffStream.close();
				audioOutputStream.flush();
				audioOutputStream.close();
				if (isAdaptivelyQuantised) {
					audioParametersStream.flush();
					audioParametersStream.close();
				}
			} catch (IOException exception) {
				exception.printStackTrace();
				isSuccessfull = false;
				status = "Audio buffer write failure!";
				return;
			}
		}
	}


}
