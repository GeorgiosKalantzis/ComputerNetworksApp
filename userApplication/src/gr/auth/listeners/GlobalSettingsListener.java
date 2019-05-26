package gr.auth.listeners;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFormattedTextField;

import gr.auth.View;
import gr.auth.connections.UDP;

public class GlobalSettingsListener implements ActionListener {
	private final JFormattedTextField formattedTextFieldServerPort, formattedTextFieldClientPort;
	private final UDP UDPConnection;

	public GlobalSettingsListener(JFormattedTextField formattedTextFieldServerPort,
			JFormattedTextField formattedTextFieldClientPort, UDP UDPConnection) {
		
		this.formattedTextFieldServerPort = formattedTextFieldServerPort;
		this.formattedTextFieldClientPort = formattedTextFieldClientPort;
		this.UDPConnection = UDPConnection;
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		View.setSubmitButtonsEnabled(false);
		int clientPort = Integer.parseInt(formattedTextFieldClientPort.getText());
		int serverPort = Integer.parseInt(formattedTextFieldServerPort.getText());
		UDPConnection.setClientPort(clientPort);
		UDPConnection.setServerPort(serverPort);
		
		View.setStatusLineText("Ports set", View.STATUS_LINE_ACTION_DONE);
		View.setSubmitButtonsEnabled(true);
		
	}
	
	

}
