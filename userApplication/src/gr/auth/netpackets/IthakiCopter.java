package gr.auth.netpackets;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SwingWorker;

import gr.auth.View;
import gr.auth.connections.TCP;
//import gr.auth.connections.UDP;

/**
 * <h1> Class responsible for collecting telemetry data from IthakiCopter using UDP and also
 * autopilot mode using TCP </h1>
 * 
 * @author Georgios Kalantzis
 *
 */

public class IthakiCopter extends SwingWorker<Void, Void> {

	private final static String PREAMPLE_TEXT = "HTTP/1.1 200 OK\r\n"
			+ "Server: Experimental Ithakicopter Java Server Version 0.4\r\n" + "Content-Type: text/html\r\n" + "\n"
			+ "\n" + "\n" + "Ithakicopter remote control.<br>\r\n"
			+ "Ready to enter a request-response control session.<br>\r\n" + "Request packet format :<br>\r\n"
			+ "AUTO FLIGHTLEVEL=FFF LMOTOR=LLL RMOTOR=RRR PILOT <CR><LF><br>\r\n" + "Response packet format :<br>\r\n"
			+ "ITHAKICOPTER LMOTOR=LLL RMOTOR=RRR ALTITUDE=AAA TEMPERATURE=TT.TT PRESSURE=PPPP.PP TELEMETRY <CR><LF><br>\r\n"
			+ "<br>\r\n";

	public final static int IMAGE_CROP_START_X = 4;
	public final static int IMAGE_CROP_START_Y = 2;
	public final static int IMAGE_CROP_WIDTH = 218;
	public final static int IMAGE_CROP_HEIGHT = 486;

	private static final int RESPONSE_LENGTH = 128;
	private static final int TEXT_RESPONSE_LENGTH = 429;
	public static final String IMAGE_URL = "http://ithaki.eng.auth.gr:38098/ithakicopter.msp&m=62&x=0&z=0&d=396&v=quad";

	private static TCP TCPConnection;
	//private static UDP UDPConnection;

	private int userAltitude;
	private int userLeftMotor;
	private int userRightMotor;

	private boolean isSuccessfull;
	private String status;

	private double leftMotor, rightMotor;
	private float temperature, pressure;
	private int altitude;

	private static JLabel temperatureOutput;
	private static JLabel pressureOutput;
	private static JLabel altitudeOutput;
	private static JLabel leftMotorOutput;
	private static JLabel rightMotorOutput;
	private static JLabel copterImageOutput;
	private BufferedImage copterImage = null;

	public IthakiCopter(TCP TCPConnection, int userAltitude, int userLeftMotor, int userRightMotor,
			JLabel temperatureOutput, JLabel pressureOutput, JLabel altitudeOutput, JLabel leftMotorOutput,
			JLabel rightMotorOutput) {

		IthakiCopter.TCPConnection = TCPConnection;
		this.userAltitude = userAltitude;
		IthakiCopter.temperatureOutput = temperatureOutput;
		IthakiCopter.pressureOutput = pressureOutput;
		IthakiCopter.altitudeOutput = altitudeOutput;
		IthakiCopter.leftMotorOutput = leftMotorOutput;
		IthakiCopter.rightMotorOutput = rightMotorOutput;
		this.userLeftMotor = userLeftMotor;
		this.userRightMotor = userRightMotor;
	}

	@Override
	protected Void doInBackground() throws Exception {

		String requestCode = "GET /index.html HTTP/1.0\r\n\r\n";

		StringBuilder responseString = new StringBuilder();

		byte[] rxbuffer = new byte[TEXT_RESPONSE_LENGTH];

		URL outputImageURL = new URL(IMAGE_URL);

		try {
			TCPConnection.getConnection().getOutputStream().write(requestCode.getBytes());
			TCPConnection.getConnection().getInputStream().read(rxbuffer);
		} catch (IOException exception) {
			exception.printStackTrace();
			isSuccessfull = false;
			status = "Request send/response receive failure!";
			return null;
		}

		for (byte responseByte : rxbuffer) {
			responseString.append((char) responseByte);
		}

		if ((responseString.length() != 0) && responseString.toString().equals(PREAMPLE_TEXT)) {

			rxbuffer = new byte[RESPONSE_LENGTH];

			while (true) {

				int numberOfBytes;

				requestCode = "AUTO FLIGHTLEVEL=" + userAltitude + " LMOTOR=" + userLeftMotor + " RMOTOR="
						+ userRightMotor + " PILOT \r\n";

				try {
					TCPConnection.getConnection().getOutputStream().write(requestCode.getBytes());
					numberOfBytes = TCPConnection.getConnection().getInputStream().read(rxbuffer);

					if (numberOfBytes == -1) {
						isSuccessfull = false;
						status = "Server closed the connection";
						return null;
					}

				} catch (IOException exception) {

					exception.printStackTrace();
					isSuccessfull = false;
					status = "Request/Receive failure";
					return null;

				}

				for (byte responseByte : rxbuffer) {

					responseString.append((char) responseByte);

				}

				leftMotor = Integer.parseInt(responseString.substring(20, 23)) / 2.55;
				rightMotor = Integer.parseInt(responseString.substring(31, 34)) / 2.55;
				altitude = Integer.parseInt(responseString.substring(44, 47));
				temperature = Float.parseFloat(responseString.substring(61, 66));
				pressure = Float.parseFloat(responseString.substring(76, 83));

				try {

					copterImage = ImageIO.read(outputImageURL);

				} catch (IOException exception) {
					exception.printStackTrace();
					isSuccessfull = false;
					status = "Image IO failure";
					return null;
				}

				copterImage = copterImage.getSubimage(IMAGE_CROP_START_X, IMAGE_CROP_START_Y, IMAGE_CROP_WIDTH,
						IMAGE_CROP_HEIGHT);

				publish();

			}

		}

		isSuccessfull = true;
		status = "Test finished successfully";

		return null;
	}

	@Override
	protected void process(List<Void> notInUse) {
		temperatureOutput.setText(temperature + " °C");
		pressureOutput.setText(pressure + " mBar");
		altitudeOutput.setText(altitude + " px above GND");
		leftMotorOutput.setText((new DecimalFormat("#.###")).format(leftMotor) + " %");
		rightMotorOutput.setText((new DecimalFormat("#.###")).format(rightMotor) + " %");

		copterImageOutput.setIcon(new ImageIcon(copterImage));
	}

	@Override
	protected void done() {
		View.setStatusLineText(status,
				((isSuccessfull) ? View.STATUS_LINE_ACTION_DONE : View.STATUS_LINE_ACTION_ERROR));
		View.setSubmitButtonsEnabled(true);
	}

}
