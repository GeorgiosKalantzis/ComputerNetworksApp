
package gr.auth.listeners;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JProgressBar;
import javax.swing.JTextPane;

import gr.auth.View;
import gr.auth.connections.UDP;
import gr.auth.netpackets.AudioStreaming;


public class AudioListener implements ActionListener {

	private final JFormattedTextField formatedTextFieldAudioRequestCode, formatedTextFieldAudioRequestSpecificSample,
			formatedTextFieldAudioNumberOfPackets, formatedTextFieldAudioBetaParameter;
	private final JComboBox<String> comboBoxAudioPoolSelect;
	private final JCheckBox checkBoxAudioRequestSpecificSample, checkBoxAudioAdaptiveQuantiser;
	private final JComboBox<Integer> comboBoxAudioQParameter;
	private final JTextPane textPaneAudioStatsOutput;
	private final JProgressBar progressBarAudioStreamer, progressBarAudioPlayer;
	private final UDP UDPConnection;

	public AudioListener(JFormattedTextField formatedTextFieldAudioRequestCode,
			JCheckBox checkBoxAudioRequestSpecificSample,
			JFormattedTextField formatedTextFieldAudioRequestSpecificSample, JComboBox<String> comboBoxAudioPoolSelect,
			JFormattedTextField formatedTextFieldAudioNumberOfPackets, JCheckBox checkBoxAudioAdaptiveQuantiser,
			JFormattedTextField formatedTextFieldAudioBetaParameter, JComboBox<Integer> comboBoxAudioQParameter,
			JTextPane textPaneAudioStatsOutput, JProgressBar progressBarAudioStreamer,
			JProgressBar progressBarAudioPlayer, UDP UDPConnection) {
		
		this.formatedTextFieldAudioRequestCode = formatedTextFieldAudioRequestCode;
		this.checkBoxAudioRequestSpecificSample = checkBoxAudioRequestSpecificSample;
		this.formatedTextFieldAudioRequestSpecificSample = formatedTextFieldAudioRequestSpecificSample;
		this.comboBoxAudioPoolSelect = comboBoxAudioPoolSelect;
		this.formatedTextFieldAudioNumberOfPackets = formatedTextFieldAudioNumberOfPackets;
		this.checkBoxAudioAdaptiveQuantiser = checkBoxAudioAdaptiveQuantiser;
		this.formatedTextFieldAudioBetaParameter = formatedTextFieldAudioBetaParameter;
		this.comboBoxAudioQParameter = comboBoxAudioQParameter;
		this.textPaneAudioStatsOutput = textPaneAudioStatsOutput;
		this.progressBarAudioStreamer = progressBarAudioStreamer;
		this.progressBarAudioPlayer = progressBarAudioPlayer;
		this.UDPConnection = UDPConnection;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		View.setSubmitButtonsEnabled(false);
		View.setStatusLineText("Test running...", View.STATUS_LINE_ACTION_RUNNING);
		(new AudioStreaming(UDPConnection, formatedTextFieldAudioRequestCode.getText(),
				checkBoxAudioRequestSpecificSample.isSelected(),
				Integer.parseInt(formatedTextFieldAudioRequestSpecificSample.getText().substring(1)),
				comboBoxAudioPoolSelect.getItemAt(comboBoxAudioPoolSelect.getSelectedIndex()),
				Integer.parseInt(formatedTextFieldAudioNumberOfPackets.getText()),
				checkBoxAudioAdaptiveQuantiser.isSelected(),
				Integer.parseInt(formatedTextFieldAudioBetaParameter.getText()),
				comboBoxAudioQParameter.getItemAt(comboBoxAudioQParameter.getSelectedIndex()), textPaneAudioStatsOutput,
				progressBarAudioStreamer, progressBarAudioPlayer)).execute();
	}
}

