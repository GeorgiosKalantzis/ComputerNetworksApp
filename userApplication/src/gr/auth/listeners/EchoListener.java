package gr.auth.listeners;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JTextField;
import javax.swing.JTextPane;

import gr.auth.View;
import gr.auth.connections.UDP;
import gr.auth.netpackets.Echo;

public class EchoListener implements ActionListener {
	private final JTextField requestCode, duration, numberOfPackets;
	private final JCheckBox hasTemperatureCode;
	private final JTextPane runningTime;
	private final UDP UDPConnection;

	public EchoListener(JTextField requestCode, JTextField duration, JTextField numberOfPackets,
			JCheckBox hasTemperatureCode, JTextPane runningTime, UDP UDPConnection) {
		
		this.requestCode = requestCode;
		this.duration = duration;
		this.numberOfPackets = numberOfPackets;
		this.hasTemperatureCode = hasTemperatureCode;
		this.runningTime = runningTime;
		this.UDPConnection = UDPConnection;

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		View.setSubmitButtonsEnabled(false);
		View.setStatusLineText("Echo test is running ...", View.STATUS_LINE_ACTION_RUNNING);
		
		// Run the test by calling execute() with an Echo object
		(new Echo(UDPConnection, requestCode.getText(), Integer.parseInt(duration.getText()),
				Integer.parseInt(numberOfPackets.getText()),hasTemperatureCode.isSelected(), runningTime)).execute();
		
		
	}
	
	
	

}
