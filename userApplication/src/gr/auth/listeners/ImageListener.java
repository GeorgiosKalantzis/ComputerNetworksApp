package gr.auth.listeners;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JTextPane;

import gr.auth.View;
import gr.auth.connections.UDP;
import gr.auth.netpackets.Image;

public class ImageListener implements ActionListener {
	
	private final JFormattedTextField formatedTextFieldImageRequestCode, formatedTextFieldImageDuration;
	private final JCheckBox checkBoxImageControlFlow;
	private final JComboBox<Integer> comboBoxImagePackageLength;
	private final JTextPane runtimeStatsOutput;
	private final JLabel runtimeImageOutput;
	private final UDP UDPConnection;
	
	public ImageListener(JFormattedTextField formatedTextFieldImageRequestCode, JCheckBox checkBoxImageControlFlow,
			JComboBox<Integer> comboBoxImagePackageLength, JFormattedTextField formatedTextFieldImageDuration,
			JTextPane runtimeStatsOutput, JLabel runtimeImageOutput, UDP UDPConnection) {
		this.formatedTextFieldImageRequestCode = formatedTextFieldImageRequestCode;
		this.checkBoxImageControlFlow = checkBoxImageControlFlow;
		this.comboBoxImagePackageLength = comboBoxImagePackageLength;
		this.formatedTextFieldImageDuration = formatedTextFieldImageDuration;
		this.runtimeStatsOutput = runtimeStatsOutput;
		this.runtimeImageOutput = runtimeImageOutput;
		this.UDPConnection = UDPConnection;
	}

	
	
	@Override
	public void actionPerformed(ActionEvent e) {
		
		View.setSubmitButtonsEnabled(false);
		View.setStatusLineText("Test running...", View.STATUS_LINE_ACTION_RUNNING);
		(new Image(UDPConnection, formatedTextFieldImageRequestCode.getText(),
				Integer.parseInt(formatedTextFieldImageDuration.getText()), checkBoxImageControlFlow.isSelected(),
				comboBoxImagePackageLength.getItemAt(comboBoxImagePackageLength.getSelectedIndex()), runtimeStatsOutput,
				runtimeImageOutput)).execute();

		
		
	}
	
	


}
