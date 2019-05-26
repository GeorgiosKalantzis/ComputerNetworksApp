package gr.auth.listeners;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;

import gr.auth.View;
import gr.auth.connections.TCP;
import gr.auth.connections.UDP;
import gr.auth.netpackets.OBD;

public class OBDListener implements ActionListener {

	private static JFormattedTextField formattedTextFieldRequestCode;
	private static JCheckBox checkBoxUseUDP;
	private static JFormattedTextField formattedTextFieldDuration;
	private static JLabel engineRunTimeOutput;
	private static JLabel airTempOutput;
	private static JLabel throttlePositionOutput;
	private static JLabel engineRPMOutput;
	private static JLabel speedOutput;
	private static JLabel coolantTemperatureOutput;
	private static JLabel packetsTotalTimeOutput;
	private static TCP TCPConnection;
	private static UDP UDPConnection;

	public OBDListener(JFormattedTextField formattedTextFieldRequestCode, JCheckBox checkBoxUseUDP,
			JFormattedTextField formattedTextFieldDuration, JLabel engineRunTimeOutput, JLabel airTempOutput,
			JLabel throttlePositionOutput, JLabel engineRPMOutput, JLabel speedOutput, JLabel coolantTemperatureOutput,
			JLabel packetsTotalTimeOutput, TCP TCPConnection, UDP UDPConnection) {
		
		OBDListener.formattedTextFieldRequestCode = formattedTextFieldRequestCode;
		OBDListener.checkBoxUseUDP = checkBoxUseUDP;
		OBDListener.formattedTextFieldDuration = formattedTextFieldDuration;
		OBDListener.engineRunTimeOutput = engineRunTimeOutput;
		OBDListener.airTempOutput = airTempOutput;
		OBDListener.throttlePositionOutput = throttlePositionOutput;
		OBDListener.engineRPMOutput = engineRPMOutput;
		OBDListener.speedOutput = speedOutput;
		OBDListener.coolantTemperatureOutput = coolantTemperatureOutput;
		OBDListener.packetsTotalTimeOutput = packetsTotalTimeOutput;
		OBDListener.TCPConnection = TCPConnection;
		OBDListener.UDPConnection = UDPConnection;

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
		View.setSubmitButtonsEnabled(false);
		View.setStatusLineText("Test running...", View.STATUS_LINE_ACTION_RUNNING);
		
		// UDP or TCP ?
		if (checkBoxUseUDP.isSelected()) {
			new OBD(UDPConnection, formattedTextFieldRequestCode.getText(),
					Integer.parseInt(formattedTextFieldDuration.getText()), engineRunTimeOutput,
					airTempOutput, throttlePositionOutput, engineRPMOutput,
					speedOutput, coolantTemperatureOutput, packetsTotalTimeOutput)
							.execute();
		} else {
			new OBD(TCPConnection, formattedTextFieldRequestCode.getText(),
					Integer.parseInt(formattedTextFieldDuration.getText()), engineRunTimeOutput,
					airTempOutput, throttlePositionOutput, engineRPMOutput,
					speedOutput, coolantTemperatureOutput, packetsTotalTimeOutput)
							.execute();
		}

		

	}

}
