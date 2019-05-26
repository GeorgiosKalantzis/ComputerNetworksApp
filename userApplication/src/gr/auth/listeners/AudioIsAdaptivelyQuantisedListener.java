package gr.auth.listeners;

import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class AudioIsAdaptivelyQuantisedListener implements ChangeListener {
	private final JFormattedTextField formatedTextFieldAudioBetaParameter;


	public AudioIsAdaptivelyQuantisedListener(JFormattedTextField formatedTextFieldAudioBetaParameter) {
		this.formatedTextFieldAudioBetaParameter = formatedTextFieldAudioBetaParameter;
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		this.formatedTextFieldAudioBetaParameter.setEnabled(!((JCheckBox) e.getSource()).isSelected());
	}


}
