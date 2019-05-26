package gr.auth.listeners;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class AudioRequestSpecificSampleListener implements ChangeListener {
	
	private final JFormattedTextField formatedTextFieldAudioRequestSpecificSample;
	private final JComboBox<String> comboBoxAudioPoolSelect;

	public AudioRequestSpecificSampleListener(JFormattedTextField formatedTextFieldAudioRequestSpecificSample,
			JComboBox<String> comboBoxAudioPoolSelect) {
		this.formatedTextFieldAudioRequestSpecificSample = formatedTextFieldAudioRequestSpecificSample;
		this.comboBoxAudioPoolSelect = comboBoxAudioPoolSelect;
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		boolean isSpecificSampleRequestSelected = ((JCheckBox) e.getSource()).isSelected();

		if (isSpecificSampleRequestSelected) {
			this.comboBoxAudioPoolSelect.setSelectedItem(comboBoxAudioPoolSelect.getItemAt(1));
		}

		this.formatedTextFieldAudioRequestSpecificSample.setEnabled(isSpecificSampleRequestSelected);
		this.comboBoxAudioPoolSelect.setEnabled(!isSpecificSampleRequestSelected);
	}

	
	

}
