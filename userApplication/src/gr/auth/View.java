package gr.auth;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.SystemColor;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;

import javax.imageio.ImageIO;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.border.EmptyBorder;
import javax.swing.text.MaskFormatter;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import gr.auth.connections.TCP;
import gr.auth.connections.UDP;
import gr.auth.listeners.AudioIsAdaptivelyQuantisedListener;
import gr.auth.listeners.AudioListener;
import gr.auth.listeners.AudioRequestSpecificSampleListener;
import gr.auth.listeners.EchoListener;
import gr.auth.listeners.GlobalSettingsListener;
import gr.auth.listeners.ImageListener;
import gr.auth.listeners.OBDListener;
import gr.auth.netpackets.IthakiCopter;

/**
 * 
 * @author GeorgiosKalantzis
 *
 */

public class View {

	private static final int WINDOW_INIT_WIDTH = 720;
	private static final int WINDOW_INIT_HEIGHT = 800;
	private static final int WINDOW_INIT_X = 350;
	private static final int WINDOW_INIT_Y = 60;

	public static final Color STATUS_LINE_ACTION_DONE = Color.getHSBColor((float) 0.29193902, 1, (float) 0.6);
	public static final Color STATUS_LINE_ACTION_RUNNING = Color.getHSBColor((float) 0.083333336, 1, (float) 0.9019608);
	public static final Color STATUS_LINE_ACTION_ERROR = Color.getHSBColor(0, 1, (float) 0.8);

	private static JFrame mainFrame;
	private static JLabel mainFrameStatusLine;
	private static JButton btnGlobalSettingsSubmit;
	private static JButton btnEchoSubmit;
	private static JButton btnImageSubmit;
	private static JButton btnAudioSubmit;
	private static JButton btnIthakiCopterSubmit;
	private static JButton btnOBDSubmit;

	private static UIDefaults uiDefaults = null;

	public static void main(String[] args) {

		// Set UIManager Metal.

		try {
			for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
				if ("Metal".equals(info.getName())) {
					UIManager.setLookAndFeel(info.getClassName());

					uiDefaults = new UIDefaults();
					uiDefaults.put("TextPane[Enabled].backgroundPainter", Color.DARK_GRAY);
					break;
				} else {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				}
			}
		} catch (Exception exception) {
			exception.printStackTrace();

		}

		/*
		 * Make all the swing related work at Event Dispatch Thread to maintain a
		 * responsive GUI.
		 */
		EventQueue.invokeLater(new Runnable() {

			@Override
			public void run() {
				try {

					// Begin the GUI.
					new View();
					View.mainFrame.setVisible(true);

				} catch (Exception exception) {
					exception.printStackTrace();
				}
			}
		});

	}

	public static void setStatusLineText(String text, Color color) {
		mainFrameStatusLine.setText(text);
		mainFrameStatusLine.setForeground(color);
		mainFrame.invalidate();
	}

	public static void setSubmitButtonsEnabled(boolean enabled) {
		btnGlobalSettingsSubmit.setEnabled(enabled);
		btnEchoSubmit.setEnabled(enabled);
		// btnImageSubmit.setEnabled(enabled);
		// btnAudioSubmit.setEnabled(enabled);
		// btnCopterSubmit.setEnabled(enabled);
		btnOBDSubmit.setEnabled(enabled);
		mainFrame.invalidate();
	}

	private UDP UDPConnection;
	private TCP TCPConnectionOBD;

	/**
	 * Create the application
	 */

	public View() {

		UDPConnection = new UDP("155.207.18.208", 38000, 48000);
		TCPConnectionOBD = new TCP("155.207.18.208", 29078);

		try {
			// Initialize the GUI.
			GUIInitialization();

		} catch (ParseException exception) {
			exception.printStackTrace();
			return;
		}

	}

	private void GUIInitialization() throws ParseException {

		// Frame
		mainFrame = new JFrame();
		mainFrame.setBackground(Color.DARK_GRAY);
		mainFrame.getContentPane().setForeground(Color.DARK_GRAY);
		mainFrame.setForeground(SystemColor.window);
		mainFrame.getContentPane().setBackground(Color.DARK_GRAY);
		GridBagLayout mainFrameGrid = new GridBagLayout();
		mainFrameGrid.columnWidths = new int[] { 0, 0 };
		mainFrameGrid.rowHeights = new int[] { 0, 0 };
		mainFrameGrid.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		mainFrameGrid.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		mainFrame.getContentPane().setLayout(mainFrameGrid);

		// Tabs
		JTabbedPane mainFrameTabbedPane = new JTabbedPane(SwingConstants.TOP);
		mainFrameTabbedPane.setBackground(SystemColor.window);
		mainFrameTabbedPane.setForeground(Color.DARK_GRAY);
		mainFrameTabbedPane.setBorder(new EmptyBorder(3, 8, 3, 8));
		GridBagConstraints mainFrameTabbedPaneGrid = new GridBagConstraints();

		mainFrameTabbedPaneGrid.insets = new Insets(0, 0, 0, 0);
		mainFrameTabbedPaneGrid.fill = GridBagConstraints.BOTH;
		mainFrameTabbedPaneGrid.anchor = GridBagConstraints.NORTH;
		mainFrameTabbedPaneGrid.gridx = 0;
		mainFrameTabbedPaneGrid.gridy = 0;
		mainFrameTabbedPaneGrid.weightx = 1.0;
		mainFrameTabbedPaneGrid.weighty = 1.0;
		mainFrame.getContentPane().add(mainFrameTabbedPane, mainFrameTabbedPaneGrid);

		// Global Settings
		JPanel globalPanel = new JPanel();
		globalPanel.setBorder(new EmptyBorder(2, 2, 2, 2));
		globalPanel.setBackground(Color.GRAY);
		mainFrameTabbedPane.addTab("Global Settings", null, globalPanel, null);
		GridBagLayout globalPanelGrid = new GridBagLayout();
		globalPanelGrid.columnWidths = new int[] { 0, 0, 0 };
		globalPanelGrid.rowHeights = new int[] { 0, 0, 0, 0, 0 };
		globalPanelGrid.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		globalPanelGrid.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		globalPanel.setLayout(globalPanelGrid);

		JLabel lblGlobalTitle = new JLabel("Global Settings");
		lblGlobalTitle.setFont(new Font("CMU Sans Serif", Font.BOLD, 25));
		lblGlobalTitle.setForeground(Color.WHITE);
		lblGlobalTitle.setHorizontalAlignment(SwingConstants.CENTER);
		GridBagConstraints lblGlobalTitleGrid = new GridBagConstraints();
		lblGlobalTitleGrid.insets = new Insets(0, 0, 5, 0);
		lblGlobalTitleGrid.anchor = GridBagConstraints.NORTH;
		lblGlobalTitleGrid.gridx = 0;
		lblGlobalTitleGrid.gridy = 0;
		lblGlobalTitleGrid.gridwidth = 2;
		globalPanel.add(lblGlobalTitle, lblGlobalTitleGrid);

		JLabel lblServerIP = new JLabel("Server IP address:");
		lblServerIP.setFont(new Font("CMU Sans Serif", Font.PLAIN, 18));
		lblServerIP.setForeground(Color.WHITE);
		GridBagConstraints lblServerIPGrid = new GridBagConstraints();
		lblServerIPGrid.insets = new Insets(0, 0, 5, 5);
		lblServerIPGrid.anchor = GridBagConstraints.WEST;
		lblServerIPGrid.gridx = 0;
		lblServerIPGrid.gridy = 1;
		lblServerIPGrid.weightx = 1.0;
		globalPanel.add(lblServerIP, lblServerIPGrid);

		JLabel lblServerIPField = new JLabel("155.207.18.208"); // ithaki.eng.auth.gr
		lblServerIPField.setFont(new Font("CMU Sans Serif", Font.PLAIN, 18));
		lblServerIPField.setForeground(Color.CYAN);
		GridBagConstraints lblServerIPFieldGrid = new GridBagConstraints();
		lblServerIPFieldGrid.insets = new Insets(0, 0, 5, 0);
		lblServerIPFieldGrid.anchor = GridBagConstraints.EAST;
		lblServerIPFieldGrid.fill = GridBagConstraints.HORIZONTAL;
		lblServerIPFieldGrid.gridx = 1;
		lblServerIPFieldGrid.gridy = 1;
		globalPanel.add(lblServerIPField, lblServerIPFieldGrid);

		JLabel lblServerPort = new JLabel("Server listening port:");
		lblServerPort.setFont(new Font("CMU Sans Serif", Font.PLAIN, 20));
		lblServerPort.setForeground(Color.WHITE);
		GridBagConstraints gbc_lblServerPort = new GridBagConstraints();
		gbc_lblServerPort.insets = new Insets(0, 0, 5, 5);
		gbc_lblServerPort.anchor = GridBagConstraints.WEST;
		gbc_lblServerPort.gridx = 0;
		gbc_lblServerPort.gridy = 2;
		gbc_lblServerPort.weightx = 1.0;
		globalPanel.add(lblServerPort, gbc_lblServerPort);

		JFormattedTextField formatedTextFieldServerPort = new JFormattedTextField(new MaskFormatter("380##"));
		formatedTextFieldServerPort.setFont(new Font("CMU Sans Serif", Font.PLAIN, 20));
		formatedTextFieldServerPort.setText("38000");
		GridBagConstraints gbc_formatedTextFieldServerPort = new GridBagConstraints();
		gbc_formatedTextFieldServerPort.insets = new Insets(0, 0, 5, 0);
		gbc_formatedTextFieldServerPort.anchor = GridBagConstraints.EAST;
		gbc_formatedTextFieldServerPort.fill = GridBagConstraints.HORIZONTAL;
		gbc_formatedTextFieldServerPort.gridx = 1;
		gbc_formatedTextFieldServerPort.gridy = 2;
		globalPanel.add(formatedTextFieldServerPort, gbc_formatedTextFieldServerPort);

		JLabel lblClientPort = new JLabel("Client listening port:");
		lblClientPort.setFont(new Font("CMU Sans Serif", Font.PLAIN, 18));
		lblClientPort.setForeground(Color.WHITE);
		GridBagConstraints gbc_lblClientPort = new GridBagConstraints();
		gbc_lblClientPort.insets = new Insets(0, 0, 5, 5);
		gbc_lblClientPort.anchor = GridBagConstraints.WEST;
		gbc_lblClientPort.gridx = 0;
		gbc_lblClientPort.gridy = 3;
		gbc_lblClientPort.weightx = 1.0;
		globalPanel.add(lblClientPort, gbc_lblClientPort);

		JFormattedTextField formatedTextFieldClientPort = new JFormattedTextField(new MaskFormatter("480##"));
		formatedTextFieldClientPort.setFont(new Font("CMU Sans Serif", Font.PLAIN, 20));
		formatedTextFieldClientPort.setText("48000");
		GridBagConstraints gbc_formatedTextFieldClientPort = new GridBagConstraints();
		gbc_formatedTextFieldClientPort.insets = new Insets(0, 0, 5, 0);
		gbc_formatedTextFieldClientPort.anchor = GridBagConstraints.EAST;
		gbc_formatedTextFieldClientPort.fill = GridBagConstraints.HORIZONTAL;
		gbc_formatedTextFieldClientPort.gridx = 1;
		gbc_formatedTextFieldClientPort.gridy = 3;
		globalPanel.add(formatedTextFieldClientPort, gbc_formatedTextFieldClientPort);

		btnGlobalSettingsSubmit = new JButton("Submit");
		btnGlobalSettingsSubmit.setFont(new Font("CMU Sans Serif", Font.BOLD, 18));
		GridBagConstraints gbc_btnGlobalSettingsSubmit = new GridBagConstraints();
		gbc_btnGlobalSettingsSubmit.anchor = GridBagConstraints.NORTHEAST;
		gbc_btnGlobalSettingsSubmit.gridx = 1;
		gbc_btnGlobalSettingsSubmit.gridy = 4;
		globalPanel.add(btnGlobalSettingsSubmit, gbc_btnGlobalSettingsSubmit);

		JTextPane textPaneGlobalSettings = new JTextPane();
		textPaneGlobalSettings.setEditable(false);
		textPaneGlobalSettings.setFont(new Font("Courier new", Font.PLAIN, 14));
		textPaneGlobalSettings.setForeground(Color.GREEN);
		textPaneGlobalSettings.putClientProperty("Nimbus.Overrides", uiDefaults);
		textPaneGlobalSettings.putClientProperty("Nimbus.Overrides.InheritDefaults", true);
		textPaneGlobalSettings.setBackground(Color.DARK_GRAY);
		textPaneGlobalSettings.setText("\n\n Application requires:\n"
				+ "    1. port forwarding of the ports 48000 - 48032 using the UDP\n" + "       protocol\n"
				+ "    2. addition of appropriate rules to the firewall, if one is\n"
				+ "       running on the computer, allowing the above connection\n"
				+ "\n\n  Sometimes the server closes the connection too soon, resulting in loss of packets and/or "
				+ "not establishing a stable connection at all.\n"
				+ "  This is particularly present in the TCP connection in the copter test. A partial solution is "
				+ "having the copter web-page open in a browser while running the test from this app or restarting the "
				+ "application and retrying the test (may need multiple restarts + retries).");
		SimpleAttributeSet sa = new SimpleAttributeSet();
		StyleConstants.setAlignment(sa, StyleConstants.ALIGN_JUSTIFIED);
		textPaneGlobalSettings.getStyledDocument().setParagraphAttributes(236, 377, sa, false);
		GridBagConstraints gbc_textPaneGlobalSettings = new GridBagConstraints();
		gbc_textPaneGlobalSettings.insets = new Insets(0, 0, 5, 0);
		gbc_textPaneGlobalSettings.gridwidth = 2;
		gbc_textPaneGlobalSettings.anchor = GridBagConstraints.NORTHWEST;
		gbc_textPaneGlobalSettings.fill = GridBagConstraints.BOTH;
		gbc_textPaneGlobalSettings.gridx = 0;
		gbc_textPaneGlobalSettings.gridy = 5;
		gbc_textPaneGlobalSettings.weighty = 1.0;
		globalPanel.add(textPaneGlobalSettings, gbc_textPaneGlobalSettings);

		// ECHO

		JPanel echoPanel = new JPanel();
		echoPanel.setBorder(new EmptyBorder(2, 2, 2, 2));
		echoPanel.setBackground(Color.DARK_GRAY);
		echoPanel.setForeground(Color.WHITE);
		mainFrameTabbedPane.addTab("Echo", null, echoPanel, null);
		GridBagLayout gbl_echoPanel = new GridBagLayout();
		gbl_echoPanel.columnWidths = new int[] { 68, 0 };
		gbl_echoPanel.rowHeights = new int[] { 0, 0, 91, 15, 0, 0 };
		gbl_echoPanel.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_echoPanel.rowWeights = new double[] { 0.0, 1.0, 0.0, 0.0, 1.0, Double.MIN_VALUE };
		echoPanel.setLayout(gbl_echoPanel);

		JLabel lblEchoTitle = new JLabel("Test Echo");
		lblEchoTitle.setFont(new Font("CMU Sans Serif", Font.BOLD, 25));
		lblEchoTitle.setForeground(Color.WHITE);
		GridBagConstraints gbc_lblEchoTitle = new GridBagConstraints();
		gbc_lblEchoTitle.insets = new Insets(0, 0, 5, 0);
		gbc_lblEchoTitle.anchor = GridBagConstraints.NORTH;
		gbc_lblEchoTitle.gridx = 0;
		gbc_lblEchoTitle.gridy = 0;
		echoPanel.add(lblEchoTitle, gbc_lblEchoTitle);

		JPanel echoTabMainContentPanel = new JPanel();
		echoTabMainContentPanel.setBackground(Color.DARK_GRAY);
		GridBagConstraints gbc_echoTabMainContentPanel = new GridBagConstraints();
		gbc_echoTabMainContentPanel.gridheight = 3;
		gbc_echoTabMainContentPanel.insets = new Insets(0, 0, 5, 0);
		gbc_echoTabMainContentPanel.fill = GridBagConstraints.BOTH;
		gbc_echoTabMainContentPanel.gridx = 0;
		gbc_echoTabMainContentPanel.gridy = 1;
		echoPanel.add(echoTabMainContentPanel, gbc_echoTabMainContentPanel);
		GridBagLayout gbl_echoTabMainContentPanel = new GridBagLayout();
		gbl_echoTabMainContentPanel.columnWidths = new int[] { 0, 0, 0 };
		gbl_echoTabMainContentPanel.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0 };
		gbl_echoTabMainContentPanel.columnWeights = new double[] { 1.0, 0.0, Double.MIN_VALUE };
		gbl_echoTabMainContentPanel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE };
		echoTabMainContentPanel.setLayout(gbl_echoTabMainContentPanel);

		JLabel lblEchoRequestCode = new JLabel("Echo request code:");
		lblEchoRequestCode.setFont(new Font("CMU Sans Serif", Font.PLAIN, 18));
		lblEchoRequestCode.setForeground(Color.WHITE);
		GridBagConstraints gbc_lblEchoRequestCode = new GridBagConstraints();
		gbc_lblEchoRequestCode.insets = new Insets(0, 0, 5, 5);
		gbc_lblEchoRequestCode.anchor = GridBagConstraints.WEST;
		gbc_lblEchoRequestCode.gridx = 0;
		gbc_lblEchoRequestCode.gridy = 0;
		gbc_lblEchoRequestCode.weightx = 1.0;
		echoTabMainContentPanel.add(lblEchoRequestCode, gbc_lblEchoRequestCode);

		JFormattedTextField formatedTextFieldEchoRequestCode = new JFormattedTextField(new MaskFormatter("E####"));
		formatedTextFieldEchoRequestCode.setFont(new Font("CMU Sans Serif", Font.PLAIN, 18));
		formatedTextFieldEchoRequestCode.setText("E0000");
		GridBagConstraints gbc_txtEchoRequestCode = new GridBagConstraints();
		gbc_txtEchoRequestCode.insets = new Insets(0, 0, 5, 0);
		gbc_txtEchoRequestCode.anchor = GridBagConstraints.EAST;
		gbc_txtEchoRequestCode.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtEchoRequestCode.gridx = 1;
		gbc_txtEchoRequestCode.gridy = 0;
		echoTabMainContentPanel.add(formatedTextFieldEchoRequestCode, gbc_txtEchoRequestCode);
		formatedTextFieldEchoRequestCode.setColumns(10);

		JFormattedTextField formatedTextFieldEchoNumberOfPackets = new JFormattedTextField(new MaskFormatter("##"));
		formatedTextFieldEchoNumberOfPackets.setFont(new Font("CMU Sans Serif", Font.PLAIN, 18));
		formatedTextFieldEchoNumberOfPackets.setText("00");
		GridBagConstraints gbc_formatedTextFieldEchoNumberOfPackets = new GridBagConstraints();
		gbc_formatedTextFieldEchoNumberOfPackets.insets = new Insets(0, 0, 5, 0);
		gbc_formatedTextFieldEchoNumberOfPackets.anchor = GridBagConstraints.EAST;
		gbc_formatedTextFieldEchoNumberOfPackets.fill = GridBagConstraints.HORIZONTAL;
		gbc_formatedTextFieldEchoNumberOfPackets.gridx = 1;
		gbc_formatedTextFieldEchoNumberOfPackets.gridy = 2;
		echoTabMainContentPanel.add(formatedTextFieldEchoNumberOfPackets, gbc_formatedTextFieldEchoNumberOfPackets);
		formatedTextFieldEchoNumberOfPackets.setColumns(10);

		JFormattedTextField formatedTextFieldEchoDuration = new JFormattedTextField(new MaskFormatter("##"));
		formatedTextFieldEchoDuration.setFont(new Font("CMU Sans Serif", Font.PLAIN, 18));
		formatedTextFieldEchoDuration.setText("00");
		GridBagConstraints gbc_formatedTextFieldEchoDuration = new GridBagConstraints();
		gbc_formatedTextFieldEchoDuration.insets = new Insets(0, 0, 5, 0);
		gbc_formatedTextFieldEchoDuration.anchor = GridBagConstraints.EAST;
		gbc_formatedTextFieldEchoDuration.fill = GridBagConstraints.HORIZONTAL;
		gbc_formatedTextFieldEchoDuration.gridx = 1;
		gbc_formatedTextFieldEchoDuration.gridy = 1;
		echoTabMainContentPanel.add(formatedTextFieldEchoDuration, gbc_formatedTextFieldEchoDuration);
		formatedTextFieldEchoDuration.setColumns(10);

		JLabel lblEchoDuration = new JLabel("Echo duration (in seconds):");
		lblEchoDuration.setFont(new Font("CMU Sans Serif", Font.PLAIN, 18));
		lblEchoDuration.setForeground(Color.WHITE);
		GridBagConstraints gbc_lblEchoDuration = new GridBagConstraints();
		gbc_lblEchoDuration.insets = new Insets(0, 0, 5, 5);
		gbc_lblEchoDuration.anchor = GridBagConstraints.WEST;
		gbc_lblEchoDuration.gridx = 0;
		gbc_lblEchoDuration.gridy = 1;
		gbc_lblEchoDuration.weightx = 1.0;
		echoTabMainContentPanel.add(lblEchoDuration, gbc_lblEchoDuration);

		JLabel lblEchoNumberOfPackets = new JLabel("Number of packets:");
		lblEchoNumberOfPackets.setFont(new Font("CMU Sans Serif", Font.PLAIN, 18));
		lblEchoNumberOfPackets.setForeground(Color.WHITE);
		GridBagConstraints gbc_lblEchoNumberOfPackets = new GridBagConstraints();
		gbc_lblEchoNumberOfPackets.insets = new Insets(0, 0, 5, 5);
		gbc_lblEchoNumberOfPackets.anchor = GridBagConstraints.WEST;
		gbc_lblEchoNumberOfPackets.gridx = 0;
		gbc_lblEchoNumberOfPackets.gridy = 2;
		gbc_lblEchoNumberOfPackets.weightx = 1.0;
		echoTabMainContentPanel.add(lblEchoNumberOfPackets, gbc_lblEchoNumberOfPackets);

		JLabel lblGetTemperature = new JLabel("Get temperature reading?");
		lblGetTemperature.setFont(new Font("CMU Sans Serif", Font.PLAIN, 18));
		lblGetTemperature.setForeground(Color.WHITE);
		GridBagConstraints gbc_lblGetTemperature = new GridBagConstraints();
		gbc_lblGetTemperature.insets = new Insets(0, 0, 5, 5);
		gbc_lblGetTemperature.anchor = GridBagConstraints.WEST;
		gbc_lblGetTemperature.gridx = 0;
		gbc_lblGetTemperature.gridy = 3;
		gbc_lblGetTemperature.weightx = 1.0;
		echoTabMainContentPanel.add(lblGetTemperature, gbc_lblGetTemperature);

		JCheckBox checkBoxGetTemperature = new JCheckBox("");
		checkBoxGetTemperature.setBackground(Color.DARK_GRAY);
		GridBagConstraints gbc_checkBoxGetTemperature = new GridBagConstraints();
		gbc_checkBoxGetTemperature.insets = new Insets(0, 0, 5, 0);
		gbc_checkBoxGetTemperature.anchor = GridBagConstraints.WEST;
		gbc_checkBoxGetTemperature.gridx = 1;
		gbc_checkBoxGetTemperature.gridy = 3;
		echoTabMainContentPanel.add(checkBoxGetTemperature, gbc_checkBoxGetTemperature);

		btnEchoSubmit = new JButton("Submit");
		btnEchoSubmit.setFont(new Font("CMU Sans Serif", Font.BOLD, 18));
		btnEchoSubmit.setEnabled(true);
		GridBagConstraints gbc_btnEchoSubmit = new GridBagConstraints();
		gbc_btnEchoSubmit.insets = new Insets(0, 0, 5, 0);
		gbc_btnEchoSubmit.anchor = GridBagConstraints.NORTHEAST;
		gbc_btnEchoSubmit.gridx = 1;
		gbc_btnEchoSubmit.gridy = 4;
		echoTabMainContentPanel.add(btnEchoSubmit, gbc_btnEchoSubmit);

		JTextPane textPaneEcho = new JTextPane();
		textPaneEcho.setEditable(false);
		textPaneEcho.setFont(new Font("Courier new", Font.PLAIN, 14));
		textPaneEcho.setForeground(Color.GREEN);
		textPaneEcho.putClientProperty("Metal.Overrides", uiDefaults);
		textPaneEcho.putClientProperty("Metal.Overrides.InheritDefaults", true);
		textPaneEcho.setBackground(Color.DARK_GRAY);
		textPaneEcho.setText("Last packet ping time:\t0 ms\nAverage packet ping time:\t0 ms\nTemperature = NaN");
		GridBagConstraints gbc_textPaneEcho = new GridBagConstraints();
		gbc_textPaneEcho.insets = new Insets(0, 0, 5, 0);
		gbc_textPaneEcho.gridwidth = 2;
		gbc_textPaneEcho.gridheight = 2;
		gbc_textPaneEcho.fill = GridBagConstraints.BOTH;
		gbc_textPaneEcho.gridx = 0;
		gbc_textPaneEcho.gridy = 5;
		echoTabMainContentPanel.add(textPaneEcho, gbc_textPaneEcho);

		// IMAGE

		JPanel imagePanel = new JPanel();
		imagePanel.setBorder(new EmptyBorder(2, 2, 2, 2));
		imagePanel.setBackground(Color.DARK_GRAY);
		imagePanel.setForeground(Color.WHITE);
		mainFrameTabbedPane.addTab("Image", null, imagePanel, null);
		GridBagLayout gridImagePanel = new GridBagLayout();
		gridImagePanel.columnWidths = new int[] { 176, 0 };
		gridImagePanel.rowHeights = new int[] { 15, 0, 0 };
		gridImagePanel.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gridImagePanel.rowWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		imagePanel.setLayout(gridImagePanel);

		JLabel lblImageTransmissionTitle = new JLabel("Test Image Transmission");
		lblImageTransmissionTitle.setFont(new Font("CMU Sans Serif", Font.BOLD, 25));
		lblImageTransmissionTitle.setForeground(Color.WHITE);
		GridBagConstraints gbc_lblImageTransmissionTitle = new GridBagConstraints();
		gbc_lblImageTransmissionTitle.insets = new Insets(0, 0, 5, 0);
		gbc_lblImageTransmissionTitle.anchor = GridBagConstraints.NORTH;
		gbc_lblImageTransmissionTitle.gridx = 0;
		gbc_lblImageTransmissionTitle.gridy = 0;
		imagePanel.add(lblImageTransmissionTitle, gbc_lblImageTransmissionTitle);

		JPanel imageTabMainContentPanel = new JPanel();
		imageTabMainContentPanel.setBackground(Color.DARK_GRAY);
		GridBagConstraints gbc_imageTabMainContentPanel = new GridBagConstraints();
		gbc_imageTabMainContentPanel.gridheight = 3;
		gbc_imageTabMainContentPanel.insets = new Insets(0, 0, 5, 0);
		gbc_imageTabMainContentPanel.fill = GridBagConstraints.BOTH;
		gbc_imageTabMainContentPanel.gridx = 0;
		gbc_imageTabMainContentPanel.gridy = 1;
		imagePanel.add(imageTabMainContentPanel, gbc_imageTabMainContentPanel);
		GridBagLayout gbl_imageTabMainContentPanel = new GridBagLayout();
		gbl_imageTabMainContentPanel.columnWidths = new int[] { 0, 0, 0 };
		gbl_imageTabMainContentPanel.rowHeights = new int[] { 0, 0, 0, 0, 0 };
		gbl_imageTabMainContentPanel.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		gbl_imageTabMainContentPanel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		imageTabMainContentPanel.setLayout(gbl_imageTabMainContentPanel);

		JLabel lblImageRequestCode = new JLabel("Image request code:");
		lblImageRequestCode.setFont(new Font("CMU Sans Serif", Font.PLAIN, 18));
		lblImageRequestCode.setForeground(Color.WHITE);
		GridBagConstraints gbc_lblImageRequestCode = new GridBagConstraints();
		gbc_lblImageRequestCode.insets = new Insets(0, 0, 5, 5);
		gbc_lblImageRequestCode.anchor = GridBagConstraints.WEST;
		gbc_lblImageRequestCode.gridx = 0;
		gbc_lblImageRequestCode.gridy = 0;
		gbc_lblImageRequestCode.weightx = 1.0;
		imageTabMainContentPanel.add(lblImageRequestCode, gbc_lblImageRequestCode);

		JFormattedTextField formatedTextFieldImageRequestCode = new JFormattedTextField(new MaskFormatter("M####"));
		formatedTextFieldImageRequestCode.setFont(new Font("CMU Sans Serif", Font.PLAIN, 18));
		formatedTextFieldImageRequestCode.setText("M0000");
		GridBagConstraints gbc_formatedTextFieldImageRequestCode = new GridBagConstraints();
		gbc_formatedTextFieldImageRequestCode.insets = new Insets(0, 0, 5, 0);
		gbc_formatedTextFieldImageRequestCode.anchor = GridBagConstraints.EAST;
		gbc_formatedTextFieldImageRequestCode.fill = GridBagConstraints.HORIZONTAL;
		gbc_formatedTextFieldImageRequestCode.gridx = 1;
		gbc_formatedTextFieldImageRequestCode.gridy = 0;
		imageTabMainContentPanel.add(formatedTextFieldImageRequestCode, gbc_formatedTextFieldImageRequestCode);
		formatedTextFieldImageRequestCode.setColumns(10);

		JLabel lblImageControlFlow = new JLabel("Control the flow of transmission? :");
		lblImageControlFlow.setFont(new Font("CMU Sans Serif", Font.PLAIN, 18));
		lblImageControlFlow.setForeground(Color.WHITE);
		GridBagConstraints gbc_lblImageControlFlow = new GridBagConstraints();
		gbc_lblImageControlFlow.insets = new Insets(0, 0, 5, 5);
		gbc_lblImageControlFlow.anchor = GridBagConstraints.WEST;
		gbc_lblImageControlFlow.gridx = 0;
		gbc_lblImageControlFlow.gridy = 1;
		gbc_lblImageControlFlow.weightx = 1.0;
		imageTabMainContentPanel.add(lblImageControlFlow, gbc_lblImageControlFlow);

		JCheckBox checkBoxImageControlFlow = new JCheckBox("");
		checkBoxImageControlFlow.setBackground(Color.DARK_GRAY);
		GridBagConstraints gbc_checkBoxImageControlFlow = new GridBagConstraints();
		gbc_checkBoxImageControlFlow.insets = new Insets(0, 0, 5, 0);
		gbc_checkBoxImageControlFlow.anchor = GridBagConstraints.WEST;
		gbc_checkBoxImageControlFlow.gridx = 1;
		gbc_checkBoxImageControlFlow.gridy = 1;
		imageTabMainContentPanel.add(checkBoxImageControlFlow, gbc_checkBoxImageControlFlow);

		JLabel lblImagePacketLength = new JLabel("Length:");
		lblImagePacketLength.setFont(new Font("CMU Sans Serif", Font.PLAIN, 18));
		lblImagePacketLength.setForeground(Color.WHITE);
		GridBagConstraints gbc_lblImagePacketLength = new GridBagConstraints();
		gbc_lblImagePacketLength.insets = new Insets(0, 0, 5, 5);
		gbc_lblImagePacketLength.anchor = GridBagConstraints.WEST;
		gbc_lblImagePacketLength.gridx = 0;
		gbc_lblImagePacketLength.gridy = 2;
		gbc_lblImagePacketLength.weightx = 1.0;
		imageTabMainContentPanel.add(lblImagePacketLength, gbc_lblImagePacketLength);

		JComboBox<Integer> comboBoxImagePacketLength = new JComboBox<Integer>();
		comboBoxImagePacketLength.setModel(new DefaultComboBoxModel<Integer>(new Integer[] { 128, 256, 512, 1024 }));
		GridBagConstraints gbc_comboBoxImagePacketLength = new GridBagConstraints();
		gbc_comboBoxImagePacketLength.insets = new Insets(0, 0, 5, 0);
		gbc_comboBoxImagePacketLength.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBoxImagePacketLength.gridx = 1;
		gbc_comboBoxImagePacketLength.gridy = 2;
		imageTabMainContentPanel.add(comboBoxImagePacketLength, gbc_comboBoxImagePacketLength);

		JLabel lblImageDuration = new JLabel("Duration:");
		lblImageDuration.setFont(new Font("CMU Sans Serif", Font.PLAIN, 18));
		lblImageDuration.setForeground(Color.WHITE);
		GridBagConstraints gbc_lblImageDuration = new GridBagConstraints();
		gbc_lblImageDuration.insets = new Insets(0, 0, 5, 5);
		gbc_lblImageDuration.anchor = GridBagConstraints.WEST;
		gbc_lblImageDuration.gridx = 0;
		gbc_lblImageDuration.gridy = 3;
		gbc_lblImageDuration.weightx = 1.0;
		imageTabMainContentPanel.add(lblImageDuration, gbc_lblImageDuration);

		JFormattedTextField formatedTextFieldImageDuration = new JFormattedTextField(new MaskFormatter("##"));
		formatedTextFieldImageDuration.setFont(new Font("CMU Sans Serif", Font.PLAIN, 18));
		formatedTextFieldImageDuration.setText("10");
		GridBagConstraints gbc_formatedTextFieldImageDuration = new GridBagConstraints();
		gbc_formatedTextFieldImageDuration.insets = new Insets(0, 0, 5, 0);
		gbc_formatedTextFieldImageDuration.anchor = GridBagConstraints.EAST;
		gbc_formatedTextFieldImageDuration.fill = GridBagConstraints.HORIZONTAL;
		gbc_formatedTextFieldImageDuration.gridx = 1;
		gbc_formatedTextFieldImageDuration.gridy = 3;
		imageTabMainContentPanel.add(formatedTextFieldImageDuration, gbc_formatedTextFieldImageDuration);
		formatedTextFieldImageDuration.setColumns(10);

		btnImageSubmit = new JButton("Submit");
		btnImageSubmit.setFont(new Font("CMU Sans Serif", Font.BOLD, 18));
		btnImageSubmit.setEnabled(true);
		GridBagConstraints gbc_btnImageSubmit = new GridBagConstraints();
		gbc_btnImageSubmit.anchor = GridBagConstraints.NORTHEAST;
		gbc_btnImageSubmit.gridx = 1;
		gbc_btnImageSubmit.gridy = 4;
		imageTabMainContentPanel.add(btnImageSubmit, gbc_btnImageSubmit);

		JPanel imageRuntimeOutputPanel = new JPanel();
		imageRuntimeOutputPanel.setBackground(Color.DARK_GRAY);
		GridBagConstraints gbc_imageRuntimeOutputPanel = new GridBagConstraints();
		gbc_imageRuntimeOutputPanel.weighty = 1.0;
		gbc_imageRuntimeOutputPanel.gridheight = 2;
		gbc_imageRuntimeOutputPanel.gridwidth = 2;
		gbc_imageRuntimeOutputPanel.insets = new Insets(0, 0, 0, 5);
		gbc_imageRuntimeOutputPanel.fill = GridBagConstraints.BOTH;
		gbc_imageRuntimeOutputPanel.gridx = 0;
		gbc_imageRuntimeOutputPanel.gridy = 5;
		imageTabMainContentPanel.add(imageRuntimeOutputPanel, gbc_imageRuntimeOutputPanel);
		GridBagLayout gbl_imageRuntimeOutputPanel = new GridBagLayout();
		gbl_imageRuntimeOutputPanel.columnWidths = new int[] { 0, 0, 0 };
		gbl_imageRuntimeOutputPanel.rowHeights = new int[] { 0, 0, 0, 0 };
		gbl_imageRuntimeOutputPanel.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		gbl_imageRuntimeOutputPanel.rowWeights = new double[] { 0.0, 0.0, 0.0, Double.MIN_VALUE };
		imageRuntimeOutputPanel.setLayout(gbl_imageRuntimeOutputPanel);

		JTextPane textPaneImageStatsOutput = new JTextPane();
		textPaneImageStatsOutput.setEditable(false);
		textPaneImageStatsOutput.setFont(new Font("Courier new", Font.PLAIN, 14));
		textPaneImageStatsOutput.setForeground(Color.GREEN);
		textPaneImageStatsOutput.putClientProperty("Nimbus.Overrides", uiDefaults);
		textPaneImageStatsOutput.putClientProperty("Nimbus.Overrides.InheritDefaults", true);
		textPaneImageStatsOutput.setBackground(Color.DARK_GRAY);
		textPaneImageStatsOutput.setText("Current Image" + "\t" + "\t" + "Average" + "\n" + "Time elapsed = 0 ms" + "\t"
				+ "Average image time = 0 ms" + "\n" + "Number of packets = 0" + "\t" + "Average number of packets = 0"
				+ "\n" + "Image size = 0 KB" + "\t\t" + "Average image size = 0 KB" + "\n" + "\n" + "FPS = 0");
		GridBagConstraints gbc_textPaneImageStatsOutput = new GridBagConstraints();
		gbc_textPaneImageStatsOutput.insets = new Insets(0, 0, 5, 0);
		gbc_textPaneImageStatsOutput.anchor = GridBagConstraints.NORTHWEST;
		gbc_textPaneImageStatsOutput.fill = GridBagConstraints.NONE;
		gbc_textPaneImageStatsOutput.gridx = 0;
		gbc_textPaneImageStatsOutput.gridy = 0;
		gbc_textPaneImageStatsOutput.weighty = 1.0;
		imageRuntimeOutputPanel.add(textPaneImageStatsOutput, gbc_textPaneImageStatsOutput);

		JLabel lblImageRuntimeOutput = new JLabel("");
		lblImageRuntimeOutput.setBackground(Color.DARK_GRAY);
		GridBagConstraints gbc_lblImageRuntimeOutput = new GridBagConstraints();
		gbc_lblImageRuntimeOutput.insets = new Insets(0, 0, 0, 5);
		gbc_lblImageRuntimeOutput.anchor = GridBagConstraints.CENTER;
		gbc_lblImageRuntimeOutput.fill = GridBagConstraints.NONE;
		gbc_lblImageRuntimeOutput.gridx = 0;
		gbc_lblImageRuntimeOutput.gridy = 2;
		gbc_lblImageRuntimeOutput.weightx = 1.0;
		gbc_lblImageRuntimeOutput.weighty = 1.0;
		imageRuntimeOutputPanel.add(lblImageRuntimeOutput, gbc_lblImageRuntimeOutput);

		// OBD

		JPanel OBDPanel = new JPanel();
		OBDPanel.setBorder(new EmptyBorder(2, 2, 2, 2));
		OBDPanel.setBackground(Color.DARK_GRAY);
		OBDPanel.setForeground(Color.WHITE);
		mainFrameTabbedPane.addTab("OBD", null, OBDPanel, null);
		GridBagLayout gbl_vehiclePanel = new GridBagLayout();
		gbl_vehiclePanel.columnWidths = new int[] { 0, 0 };
		gbl_vehiclePanel.rowHeights = new int[] { 0, 0 };
		gbl_vehiclePanel.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_vehiclePanel.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		OBDPanel.setLayout(gbl_vehiclePanel);

		JLabel lblVehicleTestTitle = new JLabel("Test OBD data transmission");
		lblVehicleTestTitle.setFont(new Font("CMU Sans Serif", Font.BOLD, 25));
		lblVehicleTestTitle.setForeground(Color.WHITE);
		GridBagConstraints gbc_lblVehicleTestTitle = new GridBagConstraints();
		gbc_lblVehicleTestTitle.insets = new Insets(0, 0, 5, 0);
		gbc_lblVehicleTestTitle.anchor = GridBagConstraints.NORTH;
		gbc_lblVehicleTestTitle.gridx = 0;
		gbc_lblVehicleTestTitle.gridy = 0;
		OBDPanel.add(lblVehicleTestTitle, gbc_lblVehicleTestTitle);

		JPanel vehicleTabMainContentPanel = new JPanel();
		vehicleTabMainContentPanel.setBackground(Color.DARK_GRAY);
		GridBagConstraints gbc_vehicleTabMainContentPanel = new GridBagConstraints();
		gbc_vehicleTabMainContentPanel.gridheight = 3;
		gbc_vehicleTabMainContentPanel.insets = new Insets(0, 0, 5, 0);
		gbc_vehicleTabMainContentPanel.fill = GridBagConstraints.BOTH;
		gbc_vehicleTabMainContentPanel.gridx = 0;
		gbc_vehicleTabMainContentPanel.gridy = 1;
		OBDPanel.add(vehicleTabMainContentPanel, gbc_vehicleTabMainContentPanel);
		GridBagLayout gbl_vehicleTabMainContentPanel = new GridBagLayout();
		gbl_vehicleTabMainContentPanel.columnWidths = new int[] { 0, 0, 0 };
		gbl_vehicleTabMainContentPanel.rowHeights = new int[] { 0, 0, 0, 0, 0 };
		gbl_vehicleTabMainContentPanel.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		gbl_vehicleTabMainContentPanel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		vehicleTabMainContentPanel.setLayout(gbl_vehicleTabMainContentPanel);

		JLabel useUDP = new JLabel("Use a UDP connection to make the requests:");
		useUDP.setFont(new Font("CMU Sans Serif", Font.PLAIN, 18));
		useUDP.setForeground(Color.WHITE);
		GridBagConstraints gbc_lblVehicleUseUDP = new GridBagConstraints();
		gbc_lblVehicleUseUDP.insets = new Insets(0, 0, 5, 5);
		gbc_lblVehicleUseUDP.anchor = GridBagConstraints.WEST;
		gbc_lblVehicleUseUDP.gridx = 0;
		gbc_lblVehicleUseUDP.gridy = 0;
		gbc_lblVehicleUseUDP.weightx = 1.0;
		vehicleTabMainContentPanel.add(useUDP, gbc_lblVehicleUseUDP);

		JCheckBox checkBoxVehicleUseUDP = new JCheckBox("");
		checkBoxVehicleUseUDP.setBackground(Color.DARK_GRAY);
		GridBagConstraints gbc_checkBoxVehicleUseUDP = new GridBagConstraints();
		gbc_checkBoxVehicleUseUDP.insets = new Insets(0, 0, 5, 0);
		gbc_checkBoxVehicleUseUDP.anchor = GridBagConstraints.WEST;
		gbc_checkBoxVehicleUseUDP.gridx = 1;
		gbc_checkBoxVehicleUseUDP.gridy = 0;
		vehicleTabMainContentPanel.add(checkBoxVehicleUseUDP, gbc_checkBoxVehicleUseUDP);

		JLabel lblVehicleRequestCode = new JLabel("Vehicle OBD-II request code:");
		lblVehicleRequestCode.setFont(new Font("CMU Sans Serif", Font.PLAIN, 18));
		lblVehicleRequestCode.setForeground(Color.WHITE);
		GridBagConstraints gbc_lblVehicleRequestCode = new GridBagConstraints();
		gbc_lblVehicleRequestCode.insets = new Insets(0, 0, 5, 5);
		gbc_lblVehicleRequestCode.anchor = GridBagConstraints.WEST;
		gbc_lblVehicleRequestCode.gridx = 0;
		gbc_lblVehicleRequestCode.gridy = 1;
		gbc_lblVehicleRequestCode.weightx = 1.0;
		vehicleTabMainContentPanel.add(lblVehicleRequestCode, gbc_lblVehicleRequestCode);

		JFormattedTextField formatedTextFieldVehicleRequestCode = new JFormattedTextField(new MaskFormatter("'V####"));
		formatedTextFieldVehicleRequestCode.setEnabled(true);
		formatedTextFieldVehicleRequestCode.setFont(new Font("CMU Sans Serif", Font.PLAIN, 18));
		formatedTextFieldVehicleRequestCode.setText("V0000");
		GridBagConstraints gbc_formatedTextFieldVehicleRequestCode = new GridBagConstraints();
		gbc_formatedTextFieldVehicleRequestCode.insets = new Insets(0, 0, 5, 0);
		gbc_formatedTextFieldVehicleRequestCode.anchor = GridBagConstraints.EAST;
		gbc_formatedTextFieldVehicleRequestCode.fill = GridBagConstraints.HORIZONTAL;
		gbc_formatedTextFieldVehicleRequestCode.gridx = 1;
		gbc_formatedTextFieldVehicleRequestCode.gridy = 1;
		vehicleTabMainContentPanel.add(formatedTextFieldVehicleRequestCode, gbc_formatedTextFieldVehicleRequestCode);
		formatedTextFieldVehicleRequestCode.setColumns(10);

		JLabel lblVehicleDuration = new JLabel("Duration:");
		lblVehicleDuration.setFont(new Font("CMU Sans Serif", Font.PLAIN, 18));
		lblVehicleDuration.setForeground(Color.WHITE);
		GridBagConstraints gbc_lblVehicleDuration = new GridBagConstraints();
		gbc_lblVehicleDuration.insets = new Insets(0, 0, 5, 5);
		gbc_lblVehicleDuration.anchor = GridBagConstraints.WEST;
		gbc_lblVehicleDuration.gridx = 0;
		gbc_lblVehicleDuration.gridy = 2;
		gbc_lblVehicleDuration.weightx = 1.0;
		vehicleTabMainContentPanel.add(lblVehicleDuration, gbc_lblVehicleDuration);

		JFormattedTextField formatedTextFieldVehicleDuration = new JFormattedTextField(new MaskFormatter("####"));
		formatedTextFieldVehicleDuration.setFont(new Font("CMU Sans Serif", Font.PLAIN, 18));
		formatedTextFieldVehicleDuration.setText("0010");
		GridBagConstraints gbc_formatedTextFieldVehicleDuration = new GridBagConstraints();
		gbc_formatedTextFieldVehicleDuration.insets = new Insets(0, 0, 5, 0);
		gbc_formatedTextFieldVehicleDuration.anchor = GridBagConstraints.EAST;
		gbc_formatedTextFieldVehicleDuration.fill = GridBagConstraints.HORIZONTAL;
		gbc_formatedTextFieldVehicleDuration.gridx = 1;
		gbc_formatedTextFieldVehicleDuration.gridy = 2;
		vehicleTabMainContentPanel.add(formatedTextFieldVehicleDuration, gbc_formatedTextFieldVehicleDuration);
		formatedTextFieldVehicleDuration.setColumns(10);

		btnOBDSubmit = new JButton("Submit");
		btnOBDSubmit.setFont(new Font("CMU Sans Serif", Font.BOLD, 18));
		btnOBDSubmit.setEnabled(true);
		GridBagConstraints gbc_btnVehicleSubmit = new GridBagConstraints();
		gbc_btnVehicleSubmit.anchor = GridBagConstraints.NORTHEAST;
		gbc_btnVehicleSubmit.gridx = 1;
		gbc_btnVehicleSubmit.gridy = 3;
		vehicleTabMainContentPanel.add(btnOBDSubmit, gbc_btnVehicleSubmit);

		JLabel lblVehicleEngineRunTime = new JLabel("Engine run time = ");
		lblVehicleEngineRunTime.setFont(new Font("Courier new", Font.PLAIN, 14));
		lblVehicleEngineRunTime.setForeground(Color.GREEN);
		lblVehicleEngineRunTime.setBackground(Color.DARK_GRAY);
		GridBagConstraints gbc_lblVehicleEngineRunTime = new GridBagConstraints();
		gbc_lblVehicleEngineRunTime.insets = new Insets(0, 0, 5, 0);
		gbc_lblVehicleEngineRunTime.anchor = GridBagConstraints.EAST;
		gbc_lblVehicleEngineRunTime.gridx = 0;
		gbc_lblVehicleEngineRunTime.gridy = 4;
		vehicleTabMainContentPanel.add(lblVehicleEngineRunTime, gbc_lblVehicleEngineRunTime);

		JLabel lblVehicleEngineRunTimeOutput = new JLabel("NaN s");
		lblVehicleEngineRunTimeOutput.setFont(new Font("Courier new", Font.PLAIN, 14));
		lblVehicleEngineRunTimeOutput.setForeground(Color.GREEN);
		lblVehicleEngineRunTimeOutput.setBackground(Color.DARK_GRAY);
		GridBagConstraints gbc_lblVehicleEngineRunTimeOutput = new GridBagConstraints();
		gbc_lblVehicleEngineRunTimeOutput.insets = new Insets(0, 0, 5, 5);
		gbc_lblVehicleEngineRunTimeOutput.anchor = GridBagConstraints.WEST;
		gbc_lblVehicleEngineRunTimeOutput.gridx = 1;
		gbc_lblVehicleEngineRunTimeOutput.gridy = 4;
		gbc_lblVehicleEngineRunTimeOutput.weightx = 1.0;
		vehicleTabMainContentPanel.add(lblVehicleEngineRunTimeOutput, gbc_lblVehicleEngineRunTimeOutput);

		JLabel lblVehicleAirTemp = new JLabel("Intake air temperature = ");
		lblVehicleAirTemp.setFont(new Font("Courier new", Font.PLAIN, 14));
		lblVehicleAirTemp.setForeground(Color.GREEN);
		lblVehicleAirTemp.setBackground(Color.DARK_GRAY);
		GridBagConstraints gbc_lblVehicleAirTemp = new GridBagConstraints();
		gbc_lblVehicleAirTemp.insets = new Insets(0, 0, 5, 0);
		gbc_lblVehicleAirTemp.anchor = GridBagConstraints.EAST;
		gbc_lblVehicleAirTemp.gridx = 0;
		gbc_lblVehicleAirTemp.gridy = 5;
		vehicleTabMainContentPanel.add(lblVehicleAirTemp, gbc_lblVehicleAirTemp);

		JLabel lblVehicleAirTempOutput = new JLabel("NaN °C");
		lblVehicleAirTempOutput.setFont(new Font("Courier new", Font.PLAIN, 14));
		lblVehicleAirTempOutput.setForeground(Color.GREEN);
		lblVehicleAirTempOutput.setBackground(Color.DARK_GRAY);
		GridBagConstraints gbc_lblVehicleAirTempOutput = new GridBagConstraints();
		gbc_lblVehicleAirTempOutput.insets = new Insets(0, 0, 5, 5);
		gbc_lblVehicleAirTempOutput.anchor = GridBagConstraints.WEST;
		gbc_lblVehicleAirTempOutput.gridx = 1;
		gbc_lblVehicleAirTempOutput.gridy = 5;
		gbc_lblVehicleAirTempOutput.weightx = 1.0;
		vehicleTabMainContentPanel.add(lblVehicleAirTempOutput, gbc_lblVehicleAirTempOutput);

		JLabel lblVehicleThrottlePosition = new JLabel("Throttle position = ");
		lblVehicleThrottlePosition.setFont(new Font("Courier new", Font.PLAIN, 14));
		lblVehicleThrottlePosition.setForeground(Color.GREEN);
		lblVehicleThrottlePosition.setBackground(Color.DARK_GRAY);
		GridBagConstraints gbc_lblVehicleThrottlePosition = new GridBagConstraints();
		gbc_lblVehicleThrottlePosition.insets = new Insets(0, 0, 5, 0);
		gbc_lblVehicleThrottlePosition.anchor = GridBagConstraints.EAST;
		gbc_lblVehicleThrottlePosition.gridx = 0;
		gbc_lblVehicleThrottlePosition.gridy = 6;
		vehicleTabMainContentPanel.add(lblVehicleThrottlePosition, gbc_lblVehicleThrottlePosition);

		JLabel lblVehicleThrottlePositionOutput = new JLabel("NaN %");
		lblVehicleThrottlePositionOutput.setFont(new Font("Courier new", Font.PLAIN, 14));
		lblVehicleThrottlePositionOutput.setForeground(Color.GREEN);
		lblVehicleThrottlePositionOutput.setBackground(Color.DARK_GRAY);
		GridBagConstraints gbc_lblVehicleThrottlePositionOutput = new GridBagConstraints();
		gbc_lblVehicleThrottlePositionOutput.insets = new Insets(0, 0, 5, 5);
		gbc_lblVehicleThrottlePositionOutput.anchor = GridBagConstraints.WEST;
		gbc_lblVehicleThrottlePositionOutput.gridx = 1;
		gbc_lblVehicleThrottlePositionOutput.gridy = 6;
		gbc_lblVehicleThrottlePositionOutput.weightx = 1.0;
		vehicleTabMainContentPanel.add(lblVehicleThrottlePositionOutput, gbc_lblVehicleThrottlePositionOutput);

		JLabel lblVehicleEngineRPM = new JLabel("Engine RPM = ");
		lblVehicleEngineRPM.setFont(new Font("Courier new", Font.PLAIN, 14));
		lblVehicleEngineRPM.setForeground(Color.GREEN);
		lblVehicleEngineRPM.setBackground(Color.DARK_GRAY);
		GridBagConstraints gbc_lblVehicleEngineRPM = new GridBagConstraints();
		gbc_lblVehicleEngineRPM.insets = new Insets(0, 0, 5, 0);
		gbc_lblVehicleEngineRPM.anchor = GridBagConstraints.EAST;
		gbc_lblVehicleEngineRPM.gridx = 0;
		gbc_lblVehicleEngineRPM.gridy = 7;
		vehicleTabMainContentPanel.add(lblVehicleEngineRPM, gbc_lblVehicleEngineRPM);

		JLabel lblVehicleEngineRPMOutput = new JLabel("NaN RPM");
		lblVehicleEngineRPMOutput.setFont(new Font("Courier new", Font.PLAIN, 14));
		lblVehicleEngineRPMOutput.setForeground(Color.GREEN);
		lblVehicleEngineRPMOutput.setBackground(Color.DARK_GRAY);
		GridBagConstraints gbc_lblVehicleEngineRPMOutput = new GridBagConstraints();
		gbc_lblVehicleEngineRPMOutput.insets = new Insets(0, 0, 5, 5);
		gbc_lblVehicleEngineRPMOutput.anchor = GridBagConstraints.WEST;
		gbc_lblVehicleEngineRPMOutput.gridx = 1;
		gbc_lblVehicleEngineRPMOutput.gridy = 7;
		gbc_lblVehicleEngineRPMOutput.weightx = 1.0;
		vehicleTabMainContentPanel.add(lblVehicleEngineRPMOutput, gbc_lblVehicleEngineRPMOutput);

		JLabel lblVehicleSpeed = new JLabel("Vehicle speed = ");
		lblVehicleSpeed.setFont(new Font("Courier new", Font.PLAIN, 14));
		lblVehicleSpeed.setForeground(Color.GREEN);
		lblVehicleSpeed.setBackground(Color.DARK_GRAY);
		GridBagConstraints gbc_lblVehicleSpeed = new GridBagConstraints();
		gbc_lblVehicleSpeed.insets = new Insets(0, 0, 5, 0);
		gbc_lblVehicleSpeed.anchor = GridBagConstraints.EAST;
		gbc_lblVehicleSpeed.gridx = 0;
		gbc_lblVehicleSpeed.gridy = 8;
		vehicleTabMainContentPanel.add(lblVehicleSpeed, gbc_lblVehicleSpeed);

		JLabel lblVehicleSpeedOutput = new JLabel("NaN Km/h");
		lblVehicleSpeedOutput.setFont(new Font("Courier new", Font.PLAIN, 14));
		lblVehicleSpeedOutput.setForeground(Color.GREEN);
		lblVehicleSpeedOutput.setBackground(Color.DARK_GRAY);
		GridBagConstraints gbc_lblVehicleSpeedOutput = new GridBagConstraints();
		gbc_lblVehicleSpeedOutput.insets = new Insets(0, 0, 5, 5);
		gbc_lblVehicleSpeedOutput.anchor = GridBagConstraints.WEST;
		gbc_lblVehicleSpeedOutput.gridx = 1;
		gbc_lblVehicleSpeedOutput.gridy = 8;
		gbc_lblVehicleSpeedOutput.weightx = 1.0;
		vehicleTabMainContentPanel.add(lblVehicleSpeedOutput, gbc_lblVehicleSpeedOutput);

		JLabel lblVehicleCoolantTemperature = new JLabel("Coolant temperature = ");
		lblVehicleCoolantTemperature.setFont(new Font("Courier new", Font.PLAIN, 14));
		lblVehicleCoolantTemperature.setForeground(Color.GREEN);
		lblVehicleCoolantTemperature.setBackground(Color.DARK_GRAY);
		GridBagConstraints gbc_lblVehicleCoolantTemperature = new GridBagConstraints();
		gbc_lblVehicleCoolantTemperature.insets = new Insets(0, 0, 5, 0);
		gbc_lblVehicleCoolantTemperature.anchor = GridBagConstraints.EAST;
		gbc_lblVehicleCoolantTemperature.gridx = 0;
		gbc_lblVehicleCoolantTemperature.gridy = 9;
		vehicleTabMainContentPanel.add(lblVehicleCoolantTemperature, gbc_lblVehicleCoolantTemperature);

		JLabel lblVehicleCoolantTemperatureOutput = new JLabel("NaN °C");
		lblVehicleCoolantTemperatureOutput.setFont(new Font("Courier new", Font.PLAIN, 14));
		lblVehicleCoolantTemperatureOutput.setForeground(Color.GREEN);
		lblVehicleCoolantTemperatureOutput.setBackground(Color.DARK_GRAY);
		GridBagConstraints gbc_lblVehicleCoolantTemperatureOutput = new GridBagConstraints();
		gbc_lblVehicleCoolantTemperatureOutput.insets = new Insets(0, 0, 5, 5);
		gbc_lblVehicleCoolantTemperatureOutput.anchor = GridBagConstraints.WEST;
		gbc_lblVehicleCoolantTemperatureOutput.gridx = 1;
		gbc_lblVehicleCoolantTemperatureOutput.gridy = 9;
		gbc_lblVehicleCoolantTemperatureOutput.weightx = 1.0;
		vehicleTabMainContentPanel.add(lblVehicleCoolantTemperatureOutput, gbc_lblVehicleCoolantTemperatureOutput);

		JLabel lblVehiclePacketsTotalTime = new JLabel("Total time needed to get the information = ");
		lblVehiclePacketsTotalTime.setFont(new Font("Courier new", Font.PLAIN, 14));
		lblVehiclePacketsTotalTime.setForeground(Color.GREEN);
		lblVehiclePacketsTotalTime.setBackground(Color.DARK_GRAY);
		GridBagConstraints gbc_lblVehiclePacketsTotalTime = new GridBagConstraints();
		gbc_lblVehiclePacketsTotalTime.insets = new Insets(0, 0, 5, 0);
		gbc_lblVehiclePacketsTotalTime.anchor = GridBagConstraints.NORTHEAST;
		gbc_lblVehiclePacketsTotalTime.gridx = 0;
		gbc_lblVehiclePacketsTotalTime.gridy = 10;
		gbc_lblVehiclePacketsTotalTime.weighty = 1.0;
		vehicleTabMainContentPanel.add(lblVehiclePacketsTotalTime, gbc_lblVehiclePacketsTotalTime);

		JLabel lblVehiclePacketsTotalTimeOutput = new JLabel("NaN ms");
		lblVehiclePacketsTotalTimeOutput.setFont(new Font("Courier new", Font.PLAIN, 14));
		lblVehiclePacketsTotalTimeOutput.setForeground(Color.GREEN);
		lblVehiclePacketsTotalTimeOutput.setBackground(Color.DARK_GRAY);
		GridBagConstraints gbc_lblVehiclePacketsTotalTimeOutput = new GridBagConstraints();
		gbc_lblVehiclePacketsTotalTimeOutput.insets = new Insets(0, 0, 5, 5);
		gbc_lblVehiclePacketsTotalTimeOutput.anchor = GridBagConstraints.NORTHWEST;
		gbc_lblVehiclePacketsTotalTimeOutput.gridx = 1;
		gbc_lblVehiclePacketsTotalTimeOutput.gridy = 10;
		gbc_lblVehiclePacketsTotalTimeOutput.weightx = 1.0;
		gbc_lblVehiclePacketsTotalTimeOutput.weighty = 1.0;
		vehicleTabMainContentPanel.add(lblVehiclePacketsTotalTimeOutput, gbc_lblVehiclePacketsTotalTimeOutput);

		// ITHAKI COPTER

		JPanel ithakiCopterPanel = new JPanel();
		ithakiCopterPanel.setBorder(new EmptyBorder(2, 2, 2, 2));
		ithakiCopterPanel.setBackground(Color.DARK_GRAY);
		ithakiCopterPanel.setForeground(Color.WHITE);
		mainFrameTabbedPane.addTab("Ithaki Copter", null, ithakiCopterPanel, null);
		GridBagLayout gbl_ithakiCopterPanel = new GridBagLayout();
		gbl_ithakiCopterPanel.columnWidths = new int[] { 0, 0 };
		gbl_ithakiCopterPanel.rowHeights = new int[] { 0, 0 };
		gbl_ithakiCopterPanel.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_ithakiCopterPanel.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		ithakiCopterPanel.setLayout(gbl_ithakiCopterPanel);

		JLabel lblCopterTestTitle = new JLabel("Test Ithaki Copter");
		lblCopterTestTitle.setFont(new Font("CMU Sans Serif", Font.BOLD, 25));
		lblCopterTestTitle.setForeground(Color.WHITE);
		GridBagConstraints gbc_lblCopterTestTitle = new GridBagConstraints();
		gbc_lblCopterTestTitle.insets = new Insets(0, 0, 5, 0);
		gbc_lblCopterTestTitle.anchor = GridBagConstraints.NORTH;
		gbc_lblCopterTestTitle.gridx = 0;
		gbc_lblCopterTestTitle.gridy = 0;
		ithakiCopterPanel.add(lblCopterTestTitle, gbc_lblCopterTestTitle);

		JPanel copterTabMainContentPanel = new JPanel();
		copterTabMainContentPanel.setBackground(Color.DARK_GRAY);
		GridBagConstraints gbc_copterTabMainContentPanel = new GridBagConstraints();
		gbc_copterTabMainContentPanel.gridheight = 3;
		gbc_copterTabMainContentPanel.insets = new Insets(0, 0, 5, 0);
		gbc_copterTabMainContentPanel.fill = GridBagConstraints.BOTH;
		gbc_copterTabMainContentPanel.gridx = 0;
		gbc_copterTabMainContentPanel.gridy = 1;
		ithakiCopterPanel.add(copterTabMainContentPanel, gbc_copterTabMainContentPanel);
		GridBagLayout gbl_copterTabMainContentPanel = new GridBagLayout();
		gbl_copterTabMainContentPanel.columnWidths = new int[] { 0, 0, 0 };
		gbl_copterTabMainContentPanel.rowHeights = new int[] { 0, 0, 0, 0, 0 };
		gbl_copterTabMainContentPanel.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		gbl_copterTabMainContentPanel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		copterTabMainContentPanel.setLayout(gbl_copterTabMainContentPanel);

		JLabel lblCopterRequestCode = new JLabel("Ithakicopter request code:");
		lblCopterRequestCode.setFont(new Font("CMU Sans Serif", Font.PLAIN, 18));
		lblCopterRequestCode.setForeground(Color.WHITE);
		GridBagConstraints gbc_lblCopterRequestCode = new GridBagConstraints();
		gbc_lblCopterRequestCode.insets = new Insets(0, 0, 5, 5);
		gbc_lblCopterRequestCode.anchor = GridBagConstraints.WEST;
		gbc_lblCopterRequestCode.gridx = 0;
		gbc_lblCopterRequestCode.gridy = 0;
		gbc_lblCopterRequestCode.weightx = 1.0;
		copterTabMainContentPanel.add(lblCopterRequestCode, gbc_lblCopterRequestCode);

		JFormattedTextField formatedTextFieldCopterRequestCode = new JFormattedTextField(new MaskFormatter("'Q####"));
		formatedTextFieldCopterRequestCode.setFont(new Font("CMU Sans Ser8f", Font.PLAIN, 12));
		formatedTextFieldCopterRequestCode.setText("Q0000");
		GridBagConstraints gbc_formatedTextFieldCopterRequestCode = new GridBagConstraints();
		gbc_formatedTextFieldCopterRequestCode.insets = new Insets(0, 0, 5, 0);
		gbc_formatedTextFieldCopterRequestCode.anchor = GridBagConstraints.EAST;
		gbc_formatedTextFieldCopterRequestCode.fill = GridBagConstraints.HORIZONTAL;
		gbc_formatedTextFieldCopterRequestCode.gridx = 1;
		gbc_formatedTextFieldCopterRequestCode.gridy = 0;
		copterTabMainContentPanel.add(formatedTextFieldCopterRequestCode, gbc_formatedTextFieldCopterRequestCode);
		formatedTextFieldCopterRequestCode.setColumns(10);

		JLabel useUDPCopter = new JLabel("Use a UDP connection collecting data");
		useUDPCopter.setFont(new Font("CMU Sans Serif", Font.PLAIN, 18));
		useUDPCopter.setForeground(Color.WHITE);
//		JSlider sliderCopterFlightLevel = new JSlider(SwingConstants.HORIZONTAL, 54, 412, 54);
//		sliderCopterFlightLevel.setSnapToTicks(true);
//		sliderCopterFlightLevel.setMajorTickSpacing(1);
//		sliderCopterFlightLevel.setBackground(Color.DARK_GRAY);
		GridBagConstraints gbc_lblCopterUseUDP = new GridBagConstraints();
		gbc_lblCopterUseUDP.insets = new Insets(0, 5, 5, 0);
		gbc_lblCopterUseUDP.anchor = GridBagConstraints.WEST;
		gbc_lblCopterUseUDP.gridx = 0;
		gbc_lblCopterUseUDP.gridy = 0;
		// gbc_lblCopterUseUDP.weightx = 2.0;
		copterTabMainContentPanel.add(useUDPCopter, gbc_lblCopterUseUDP);

		JCheckBox checkBoxCopterUseUDP = new JCheckBox("");
		checkBoxCopterUseUDP.setBackground(Color.DARK_GRAY);
		GridBagConstraints gbc_checkBoxCopterUseUDP = new GridBagConstraints();
		gbc_checkBoxCopterUseUDP.insets = new Insets(0, 5, 5, 5);
		gbc_checkBoxCopterUseUDP.anchor = GridBagConstraints.WEST;
		gbc_checkBoxCopterUseUDP.gridx = 1;
		gbc_checkBoxCopterUseUDP.gridy = 0;
		copterTabMainContentPanel.add(checkBoxCopterUseUDP, gbc_checkBoxCopterUseUDP);

//		JLabel lblCopterFlightLevel = new JLabel("Desired flight level:\t54");
//		lblCopterFlightLevel.setFont(new Font("CMU Sans Serif", Font.PLAIN, 18));
//		lblCopterFlightLevel.setForeground(Color.ORANGE);
//		GridBagConstraints gbc_lblCopterFlightLevel = new GridBagConstraints();
//		gbc_lblCopterFlightLevel.insets = new Insets(0, 0, 5, 5);
//		gbc_lblCopterFlightLevel.anchor = GridBagConstraints.CENTER;
//		gbc_lblCopterFlightLevel.gridx = 0;
//		gbc_lblCopterFlightLevel.gridy = 2;
//		gbc_lblCopterFlightLevel.gridwidth = 2;
//		copterTabMainContentPanel.add(lblCopterFlightLevel, gbc_lblCopterFlightLevel);

		btnIthakiCopterSubmit = new JButton("Submit");
		btnIthakiCopterSubmit.setFont(new Font("CMU Sans Serif", Font.BOLD, 18));
		btnIthakiCopterSubmit.setEnabled(true);
		GridBagConstraints gbc_btnCopterSubmit = new GridBagConstraints();
		gbc_btnCopterSubmit.anchor = GridBagConstraints.NORTHEAST;
		gbc_btnCopterSubmit.gridx = 1;
		gbc_btnCopterSubmit.gridy = 3;
		copterTabMainContentPanel.add(btnIthakiCopterSubmit, gbc_btnCopterSubmit);

		JPanel copterOutputContentPanel = new JPanel();
		copterOutputContentPanel.setBackground(Color.DARK_GRAY);
		GridBagConstraints gbc_copterOutputContentPanel = new GridBagConstraints();
		gbc_copterOutputContentPanel.gridheight = 3;
		gbc_copterOutputContentPanel.gridwidth = 2;
		gbc_copterOutputContentPanel.insets = new Insets(5, 0, 5, 0);
		gbc_copterOutputContentPanel.fill = GridBagConstraints.BOTH;
		gbc_copterOutputContentPanel.gridx = 0;
		gbc_copterOutputContentPanel.gridy = 4;
		copterTabMainContentPanel.add(copterOutputContentPanel, gbc_copterOutputContentPanel);
		GridBagLayout gbl_copterOutputContentPanel = new GridBagLayout();
		gbl_copterOutputContentPanel.columnWidths = new int[] { 0, 0, 0 };
		gbl_copterOutputContentPanel.rowHeights = new int[] { 0, 0, 0, 0, 0 };
		gbl_copterOutputContentPanel.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		gbl_copterOutputContentPanel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		copterOutputContentPanel.setLayout(gbl_copterOutputContentPanel);

		JLabel lblCopterTemperature = new JLabel("Temperature in the room = ");
		lblCopterTemperature.setFont(new Font("Courier new", Font.PLAIN, 14));
		lblCopterTemperature.setForeground(Color.GREEN);
		lblCopterTemperature.setBackground(Color.DARK_GRAY);
		GridBagConstraints gbc_lblCopterTemperature = new GridBagConstraints();
		gbc_lblCopterTemperature.insets = new Insets(0, 0, 5, -2);
		gbc_lblCopterTemperature.fill = GridBagConstraints.NONE;
		gbc_lblCopterTemperature.anchor = GridBagConstraints.ABOVE_BASELINE_TRAILING;
		gbc_lblCopterTemperature.gridx = 0;
		gbc_lblCopterTemperature.gridy = 0;
		copterOutputContentPanel.add(lblCopterTemperature, gbc_lblCopterTemperature);

		JLabel lblCopterTemperatureOutput = new JLabel("NaN °C");
		lblCopterTemperatureOutput.setFont(new Font("Courier new", Font.PLAIN, 14));
		lblCopterTemperatureOutput.setForeground(Color.GREEN);
		lblCopterTemperatureOutput.setBackground(Color.DARK_GRAY);
		GridBagConstraints gbc_lblCopterTemperatureOutput = new GridBagConstraints();
		gbc_lblCopterTemperatureOutput.insets = new Insets(0, 0, 5, 0);
		gbc_lblCopterTemperatureOutput.fill = GridBagConstraints.NONE;
		gbc_lblCopterTemperatureOutput.anchor = GridBagConstraints.ABOVE_BASELINE_LEADING;
		gbc_lblCopterTemperatureOutput.gridx = 1;
		gbc_lblCopterTemperatureOutput.gridy = 0;
		copterOutputContentPanel.add(lblCopterTemperatureOutput, gbc_lblCopterTemperatureOutput);

		JLabel lblCopterPressure = new JLabel("Atmospheric pressure in the room = ");
		lblCopterPressure.setFont(new Font("Courier new", Font.PLAIN, 14));
		lblCopterPressure.setForeground(Color.GREEN);
		lblCopterPressure.setBackground(Color.DARK_GRAY);
		GridBagConstraints gbc_lblCopterPressure = new GridBagConstraints();
		gbc_lblCopterPressure.insets = new Insets(0, 0, 5, -2);
		gbc_lblCopterPressure.fill = GridBagConstraints.NONE;
		gbc_lblCopterPressure.anchor = GridBagConstraints.ABOVE_BASELINE_TRAILING;
		gbc_lblCopterPressure.gridx = 0;
		gbc_lblCopterPressure.gridy = 1;
		copterOutputContentPanel.add(lblCopterPressure, gbc_lblCopterPressure);

		JLabel lblCopterPressureOutput = new JLabel("NaN mBar");
		lblCopterPressureOutput.setFont(new Font("Courier new", Font.PLAIN, 14));
		lblCopterPressureOutput.setForeground(Color.GREEN);
		lblCopterPressureOutput.setBackground(Color.DARK_GRAY);
		GridBagConstraints gbc_lblCopterPressureOutput = new GridBagConstraints();
		gbc_lblCopterPressureOutput.insets = new Insets(0, 0, 5, 0);
		gbc_lblCopterPressureOutput.fill = GridBagConstraints.NONE;
		gbc_lblCopterPressureOutput.anchor = GridBagConstraints.ABOVE_BASELINE_LEADING;
		gbc_lblCopterPressureOutput.gridx = 1;
		gbc_lblCopterPressureOutput.gridy = 1;
		copterOutputContentPanel.add(lblCopterPressureOutput, gbc_lblCopterPressureOutput);

		JLabel lblCopterAltitude = new JLabel("Copter's flight altitude = ");
		lblCopterAltitude.setFont(new Font("Courier new", Font.PLAIN, 14));
		lblCopterAltitude.setForeground(Color.GREEN);
		lblCopterAltitude.setBackground(Color.DARK_GRAY);
		GridBagConstraints gbc_lblCopterAltitude = new GridBagConstraints();
		gbc_lblCopterAltitude.insets = new Insets(0, 0, 5, -2);
		gbc_lblCopterAltitude.fill = GridBagConstraints.NONE;
		gbc_lblCopterAltitude.anchor = GridBagConstraints.ABOVE_BASELINE_TRAILING;
		gbc_lblCopterAltitude.gridx = 0;
		gbc_lblCopterAltitude.gridy = 2;
		copterOutputContentPanel.add(lblCopterAltitude, gbc_lblCopterAltitude);

		JLabel lblCopterAltitudeOutput = new JLabel("NaN px above GND");
		lblCopterAltitudeOutput.setFont(new Font("Courier new", Font.PLAIN, 14));
		lblCopterAltitudeOutput.setForeground(Color.GREEN);
		lblCopterAltitudeOutput.setBackground(Color.DARK_GRAY);
		GridBagConstraints gbc_lblCopterAltitudeOutput = new GridBagConstraints();
		gbc_lblCopterAltitudeOutput.insets = new Insets(0, 0, 5, 0);
		gbc_lblCopterAltitudeOutput.fill = GridBagConstraints.NONE;
		gbc_lblCopterAltitudeOutput.anchor = GridBagConstraints.ABOVE_BASELINE_LEADING;
		gbc_lblCopterAltitudeOutput.gridx = 1;
		gbc_lblCopterAltitudeOutput.gridy = 2;
		copterOutputContentPanel.add(lblCopterAltitudeOutput, gbc_lblCopterAltitudeOutput);

		JLabel lblCopterLeftMotor = new JLabel("Copter's left motor power = ");
		lblCopterLeftMotor.setFont(new Font("Courier new", Font.PLAIN, 14));
		lblCopterLeftMotor.setForeground(Color.GREEN);
		lblCopterLeftMotor.setBackground(Color.DARK_GRAY);
		GridBagConstraints gbc_lblCopterLeftMotor = new GridBagConstraints();
		gbc_lblCopterLeftMotor.insets = new Insets(0, 0, 5, -2);
		gbc_lblCopterLeftMotor.fill = GridBagConstraints.NONE;
		gbc_lblCopterLeftMotor.anchor = GridBagConstraints.ABOVE_BASELINE_TRAILING;
		gbc_lblCopterLeftMotor.gridx = 0;
		gbc_lblCopterLeftMotor.gridy = 3;
		copterOutputContentPanel.add(lblCopterLeftMotor, gbc_lblCopterLeftMotor);

		JLabel lblCopterLeftMotorOutput = new JLabel("NaN %");
		lblCopterLeftMotorOutput.setFont(new Font("Courier new", Font.PLAIN, 14));
		lblCopterLeftMotorOutput.setForeground(Color.GREEN);
		lblCopterLeftMotorOutput.setBackground(Color.DARK_GRAY);
		GridBagConstraints gbc_lblCopterLeftMotorOutput = new GridBagConstraints();
		gbc_lblCopterLeftMotorOutput.insets = new Insets(0, 0, 5, 0);
		gbc_lblCopterLeftMotorOutput.fill = GridBagConstraints.NONE;
		gbc_lblCopterLeftMotorOutput.anchor = GridBagConstraints.ABOVE_BASELINE_LEADING;
		gbc_lblCopterLeftMotorOutput.gridx = 1;
		gbc_lblCopterLeftMotorOutput.gridy = 3;
		copterOutputContentPanel.add(lblCopterLeftMotorOutput, gbc_lblCopterLeftMotorOutput);

		JLabel lblCopterRightMotor = new JLabel("Copter's right motor power = ");
		lblCopterRightMotor.setFont(new Font("Courier new", Font.PLAIN, 14));
		lblCopterRightMotor.setForeground(Color.GREEN);
		lblCopterRightMotor.setBackground(Color.DARK_GRAY);
		GridBagConstraints gbc_lblCopterRightMotor = new GridBagConstraints();
		gbc_lblCopterRightMotor.insets = new Insets(0, 0, 5, -2);
		gbc_lblCopterRightMotor.fill = GridBagConstraints.NONE;
		gbc_lblCopterRightMotor.anchor = GridBagConstraints.ABOVE_BASELINE_TRAILING;
		gbc_lblCopterRightMotor.gridx = 0;
		gbc_lblCopterRightMotor.gridy = 4;
		copterOutputContentPanel.add(lblCopterRightMotor, gbc_lblCopterRightMotor);

		JLabel lblCopterRightMotorOutput = new JLabel("NaN %");
		lblCopterRightMotorOutput.setFont(new Font("Courier new", Font.PLAIN, 14));
		lblCopterRightMotorOutput.setForeground(Color.GREEN);
		lblCopterRightMotorOutput.setBackground(Color.DARK_GRAY);
		GridBagConstraints gbc_lblCopterRightMotorOutput = new GridBagConstraints();
		gbc_lblCopterRightMotorOutput.insets = new Insets(0, 0, 5, 0);
		gbc_lblCopterRightMotorOutput.fill = GridBagConstraints.NONE;
		gbc_lblCopterRightMotorOutput.anchor = GridBagConstraints.ABOVE_BASELINE_LEADING;
		gbc_lblCopterRightMotorOutput.gridx = 1;
		gbc_lblCopterRightMotorOutput.gridy = 4;
		copterOutputContentPanel.add(lblCopterRightMotorOutput, gbc_lblCopterRightMotorOutput);

		JLabel lblCopterImageOutput = new JLabel("");
		try {
			URL copterImageOutputUrl = new URL(IthakiCopter.IMAGE_URL);
			BufferedImage copterImageOutput = ImageIO.read(copterImageOutputUrl);
			copterImageOutput = copterImageOutput.getSubimage(IthakiCopter.IMAGE_CROP_START_X,
					IthakiCopter.IMAGE_CROP_START_Y, IthakiCopter.IMAGE_CROP_WIDTH, IthakiCopter.IMAGE_CROP_HEIGHT);
			lblCopterImageOutput.setIcon(new ImageIcon(copterImageOutput));
		} catch (MalformedURLException exception) {
			exception.printStackTrace();
		} catch (IOException exception) {
			exception.printStackTrace();
		}
		lblCopterImageOutput.setForeground(Color.GREEN);
		lblCopterImageOutput.setBackground(Color.DARK_GRAY);
		GridBagConstraints gbc_lblCopterImageOutput = new GridBagConstraints();
		gbc_lblCopterImageOutput.insets = new Insets(0, 0, 5, 0);
		gbc_lblCopterImageOutput.fill = GridBagConstraints.NONE;
		gbc_lblCopterImageOutput.anchor = GridBagConstraints.CENTER;
		gbc_lblCopterImageOutput.gridx = 0;
		gbc_lblCopterImageOutput.gridy = 5;
		gbc_lblCopterImageOutput.weighty = 1.0;
		gbc_lblCopterImageOutput.gridwidth = 2;
		copterOutputContentPanel.add(lblCopterImageOutput, gbc_lblCopterImageOutput);

//		JProgressBar progressBarCopterLeftMotorPower = new JProgressBar(SwingConstants.VERTICAL);
//		progressBarCopterLeftMotorPower.setStringPainted(true);
//		progressBarCopterLeftMotorPower.setFont(new Font("Courier new", Font.PLAIN, 14));
//		progressBarCopterLeftMotorPower.setString("Left motor power");
//		progressBarCopterLeftMotorPower.setMaximum(100);
//		progressBarCopterLeftMotorPower.setMinimum(0);
//		GridBagConstraints gbc_progressBarCopterLeftMotorPower = new GridBagConstraints();
//		gbc_progressBarCopterLeftMotorPower.insets = new Insets(0, 0, 5, 8);
//		gbc_progressBarCopterLeftMotorPower.anchor = GridBagConstraints.NORTH;
//		gbc_progressBarCopterLeftMotorPower.fill = GridBagConstraints.BOTH;
//		gbc_progressBarCopterLeftMotorPower.gridx = 3;
//		gbc_progressBarCopterLeftMotorPower.gridy = 0;
//		gbc_progressBarCopterLeftMotorPower.gridheight = 6;
//		gbc_progressBarCopterLeftMotorPower.weightx = 1.0;
//		gbc_progressBarCopterLeftMotorPower.weighty = 1.0;
//		copterOutputContentPanel.add(progressBarCopterLeftMotorPower, gbc_progressBarCopterLeftMotorPower);
//
//		JProgressBar progressBarCopterRightMotorPower = new JProgressBar(SwingConstants.VERTICAL);
//		progressBarCopterRightMotorPower.setStringPainted(true);
//		progressBarCopterRightMotorPower.setFont(new Font("Courier new", Font.PLAIN, 14));
//		progressBarCopterRightMotorPower.setString("Right motor power");
//		progressBarCopterRightMotorPower.setMaximum(100);
//		progressBarCopterRightMotorPower.setMinimum(0);
//		GridBagConstraints gbc_progressBarCopterRightMotorPower = new GridBagConstraints();
//		gbc_progressBarCopterRightMotorPower.insets = new Insets(0, 0, 5, 0);
//		gbc_progressBarCopterRightMotorPower.anchor = GridBagConstraints.NORTH;
//		gbc_progressBarCopterRightMotorPower.fill = GridBagConstraints.BOTH;
//		gbc_progressBarCopterRightMotorPower.gridx = 4;
//		gbc_progressBarCopterRightMotorPower.gridy = 0;
//		gbc_progressBarCopterRightMotorPower.gridheight = 6;
//		gbc_progressBarCopterRightMotorPower.weightx = 1.0;
//		gbc_progressBarCopterRightMotorPower.weighty = 1.0;
//		copterOutputContentPanel.add(progressBarCopterRightMotorPower, gbc_progressBarCopterRightMotorPower);
		
		// AUDIO STREAMING
		
		
		JPanel audioPanel = new JPanel();
		audioPanel.setBorder(new EmptyBorder(2, 2, 2, 2));
		audioPanel.setBackground(Color.DARK_GRAY);
		audioPanel.setForeground(Color.WHITE);
		mainFrameTabbedPane.addTab("Audio Streaming", null, audioPanel, null);
		GridBagLayout gbl_audioPanel = new GridBagLayout();
		gbl_audioPanel.columnWidths = new int[] { 176, 0 };
		gbl_audioPanel.rowHeights = new int[] { 15, 0, 0 };
		gbl_audioPanel.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_audioPanel.rowWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		audioPanel.setLayout(gbl_audioPanel);

		JLabel lblAudioStreamTitle = new JLabel("Test Audio Streaming");
		lblAudioStreamTitle.setFont(new Font("CMU Sans Serif", Font.BOLD, 25));
		lblAudioStreamTitle.setForeground(Color.WHITE);
		GridBagConstraints gbc_lblAudioStreamTitle = new GridBagConstraints();
		gbc_lblAudioStreamTitle.insets = new Insets(0, 0, 5, 0);
		gbc_lblAudioStreamTitle.anchor = GridBagConstraints.NORTH;
		gbc_lblAudioStreamTitle.gridx = 0;
		gbc_lblAudioStreamTitle.gridy = 0;
		audioPanel.add(lblAudioStreamTitle, gbc_lblAudioStreamTitle);

		JPanel audioTabMainContentPanel = new JPanel();
		audioTabMainContentPanel.setBackground(Color.DARK_GRAY);
		GridBagConstraints gbc_audioTabMainContentPanel = new GridBagConstraints();
		gbc_audioTabMainContentPanel.gridheight = 3;
		gbc_audioTabMainContentPanel.insets = new Insets(0, 0, 5, 0);
		gbc_audioTabMainContentPanel.fill = GridBagConstraints.BOTH;
		gbc_audioTabMainContentPanel.gridx = 0;
		gbc_audioTabMainContentPanel.gridy = 1;
		audioPanel.add(audioTabMainContentPanel, gbc_audioTabMainContentPanel);
		GridBagLayout gbl_audioTabMainContentPanel = new GridBagLayout();
		gbl_audioTabMainContentPanel.columnWidths = new int[] { 0, 0, 0 };
		gbl_audioTabMainContentPanel.rowHeights = new int[] { 0, 0, 0, 0, 0 };
		gbl_audioTabMainContentPanel.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		gbl_audioTabMainContentPanel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		audioTabMainContentPanel.setLayout(gbl_audioTabMainContentPanel);

		JLabel lblAudioRequestCode = new JLabel("Audio stream request code:");
		lblAudioRequestCode.setFont(new Font("CMU Sans Serif", Font.PLAIN, 18));
		lblAudioRequestCode.setForeground(Color.WHITE);
		GridBagConstraints gbc_lblAudioRequestCode = new GridBagConstraints();
		gbc_lblAudioRequestCode.insets = new Insets(0, 0, 5, 5);
		gbc_lblAudioRequestCode.anchor = GridBagConstraints.WEST;
		gbc_lblAudioRequestCode.gridx = 0;
		gbc_lblAudioRequestCode.gridy = 0;
		gbc_lblAudioRequestCode.weightx = 1.0;
		gbc_lblAudioRequestCode.gridwidth = 2;
		audioTabMainContentPanel.add(lblAudioRequestCode, gbc_lblAudioRequestCode);

		JFormattedTextField formatedTextFieldAudioRequestCode = new JFormattedTextField(new MaskFormatter("'A####"));
		formatedTextFieldAudioRequestCode.setFont(new Font("CMU Sans Serif", Font.PLAIN, 18));
		formatedTextFieldAudioRequestCode.setText("A0000");
		GridBagConstraints gbc_formatedTextFieldAudioRequestCode = new GridBagConstraints();
		gbc_formatedTextFieldAudioRequestCode.insets = new Insets(0, 0, 5, 0);
		gbc_formatedTextFieldAudioRequestCode.anchor = GridBagConstraints.EAST;
		gbc_formatedTextFieldAudioRequestCode.fill = GridBagConstraints.HORIZONTAL;
		gbc_formatedTextFieldAudioRequestCode.gridx = 2;
		gbc_formatedTextFieldAudioRequestCode.gridy = 0;
		audioTabMainContentPanel.add(formatedTextFieldAudioRequestCode, gbc_formatedTextFieldAudioRequestCode);
		formatedTextFieldAudioRequestCode.setColumns(10);

		JLabel lblAudioRequestSpecificSample = new JLabel("Request specific sample:");
		lblAudioRequestSpecificSample.setFont(new Font("CMU Sans Serif", Font.PLAIN, 18));
		lblAudioRequestSpecificSample.setForeground(Color.WHITE);
		GridBagConstraints gbc_lblAudioRequestSpecificImage = new GridBagConstraints();
		gbc_lblAudioRequestSpecificImage.insets = new Insets(0, 0, 5, 5);
		gbc_lblAudioRequestSpecificImage.anchor = GridBagConstraints.WEST;
		gbc_lblAudioRequestSpecificImage.gridx = 0;
		gbc_lblAudioRequestSpecificImage.gridy = 1;
		gbc_lblAudioRequestSpecificImage.weightx = 1.0;
		gbc_lblAudioRequestSpecificImage.gridwidth = 2;
		audioTabMainContentPanel.add(lblAudioRequestSpecificSample, gbc_lblAudioRequestSpecificImage);

		JCheckBox checkBoxAudioRequestSpecificSample = new JCheckBox("");
		checkBoxAudioRequestSpecificSample.setBackground(Color.DARK_GRAY);
		GridBagConstraints gbc_checkBoxAudioRequestSpecificImageSample = new GridBagConstraints();
		gbc_checkBoxAudioRequestSpecificImageSample.insets = new Insets(0, 0, 5, 0);
		gbc_checkBoxAudioRequestSpecificImageSample.anchor = GridBagConstraints.EAST;
		gbc_checkBoxAudioRequestSpecificImageSample.gridx = 1;
		gbc_checkBoxAudioRequestSpecificImageSample.gridy = 1;
		audioTabMainContentPanel.add(checkBoxAudioRequestSpecificSample, gbc_checkBoxAudioRequestSpecificImageSample);

		JFormattedTextField formatedTextFieldAudioRequestSpecificSample = new JFormattedTextField(
				new MaskFormatter("'L##"));
		formatedTextFieldAudioRequestSpecificSample.setFont(new Font("CMU Sans Serif", Font.PLAIN, 18));
		formatedTextFieldAudioRequestSpecificSample.setText("L00");
		formatedTextFieldAudioRequestSpecificSample.setEnabled(false);
		GridBagConstraints gbc_formatedTextFieldAudioRequestSpecificImage = new GridBagConstraints();
		gbc_formatedTextFieldAudioRequestSpecificImage.insets = new Insets(0, 0, 5, 0);
		gbc_formatedTextFieldAudioRequestSpecificImage.anchor = GridBagConstraints.EAST;
		gbc_formatedTextFieldAudioRequestSpecificImage.fill = GridBagConstraints.HORIZONTAL;
		gbc_formatedTextFieldAudioRequestSpecificImage.gridx = 2;
		gbc_formatedTextFieldAudioRequestSpecificImage.gridy = 1;
		audioTabMainContentPanel.add(formatedTextFieldAudioRequestSpecificSample,
				gbc_formatedTextFieldAudioRequestSpecificImage);
		formatedTextFieldAudioRequestSpecificSample.setColumns(10);

		JLabel lblAudioPoolAndNumberOfPackets = new JLabel("Audio pool and number of packets:");
		lblAudioPoolAndNumberOfPackets.setFont(new Font("CMU Sans Serif", Font.PLAIN, 18));
		lblAudioPoolAndNumberOfPackets.setForeground(Color.WHITE);
		GridBagConstraints gbc_lblAudioPoolAndNumberOfPackets = new GridBagConstraints();
		gbc_lblAudioPoolAndNumberOfPackets.insets = new Insets(0, 0, 5, 5);
		gbc_lblAudioPoolAndNumberOfPackets.anchor = GridBagConstraints.WEST;
		gbc_lblAudioPoolAndNumberOfPackets.gridx = 0;
		gbc_lblAudioPoolAndNumberOfPackets.gridy = 2;
		gbc_lblAudioPoolAndNumberOfPackets.weightx = 2.0;
		audioTabMainContentPanel.add(lblAudioPoolAndNumberOfPackets, gbc_lblAudioPoolAndNumberOfPackets);

		JComboBox<String> comboBoxAudioPoolSelect = new JComboBox<String>();
		comboBoxAudioPoolSelect.setModel(new DefaultComboBoxModel<String>(new String[] { "T", "F" }));
		GridBagConstraints gbc_comboBoxAudioPoolSelect = new GridBagConstraints();
		gbc_comboBoxAudioPoolSelect.insets = new Insets(0, 0, 5, 0);
		gbc_comboBoxAudioPoolSelect.fill = GridBagConstraints.NONE;
		gbc_comboBoxAudioPoolSelect.anchor = GridBagConstraints.EAST;
		gbc_comboBoxAudioPoolSelect.gridx = 1;
		gbc_comboBoxAudioPoolSelect.gridy = 2;
		audioTabMainContentPanel.add(comboBoxAudioPoolSelect, gbc_comboBoxAudioPoolSelect);

		JFormattedTextField formatedTextFieldAudioNumberOfPackets = new JFormattedTextField(new MaskFormatter("###"));
		formatedTextFieldAudioNumberOfPackets.setFont(new Font("CMU Sans Serif", Font.PLAIN, 18));
		formatedTextFieldAudioNumberOfPackets.setText("320");
		GridBagConstraints gbc_formatedTextFieldAudioNumberOfPackets = new GridBagConstraints();
		gbc_formatedTextFieldAudioNumberOfPackets.insets = new Insets(0, 0, 5, 0);
		gbc_formatedTextFieldAudioNumberOfPackets.anchor = GridBagConstraints.EAST;
		gbc_formatedTextFieldAudioNumberOfPackets.fill = GridBagConstraints.HORIZONTAL;
		gbc_formatedTextFieldAudioNumberOfPackets.gridx = 2;
		gbc_formatedTextFieldAudioNumberOfPackets.gridy = 2;
		gbc_formatedTextFieldAudioNumberOfPackets.weightx = 1.0;
		audioTabMainContentPanel.add(formatedTextFieldAudioNumberOfPackets, gbc_formatedTextFieldAudioNumberOfPackets);
		formatedTextFieldAudioNumberOfPackets.setColumns(10);

		JLabel lblAudioAdaptiveQuantiser = new JLabel("Adaptive quantiser:");
		lblAudioAdaptiveQuantiser.setFont(new Font("CMU Sans Serif", Font.PLAIN, 18));
		lblAudioAdaptiveQuantiser.setForeground(Color.WHITE);
		GridBagConstraints gbc_lblAudioAdaptiveQuantiser = new GridBagConstraints();
		gbc_lblAudioAdaptiveQuantiser.insets = new Insets(0, 0, 5, 5);
		gbc_lblAudioAdaptiveQuantiser.anchor = GridBagConstraints.WEST;
		gbc_lblAudioAdaptiveQuantiser.gridx = 0;
		gbc_lblAudioAdaptiveQuantiser.gridy = 3;
		gbc_lblAudioAdaptiveQuantiser.weightx = 1.0;
		gbc_lblAudioAdaptiveQuantiser.gridwidth = 2;
		audioTabMainContentPanel.add(lblAudioAdaptiveQuantiser, gbc_lblAudioAdaptiveQuantiser);

		JCheckBox checkBoxAudioAdaptiveQuantiser = new JCheckBox("");
		checkBoxAudioAdaptiveQuantiser.setBackground(Color.DARK_GRAY);
		GridBagConstraints gbc_checkBoxAudioAdaptiveQuantiser = new GridBagConstraints();
		gbc_checkBoxAudioAdaptiveQuantiser.insets = new Insets(0, 0, 5, 0);
		gbc_checkBoxAudioAdaptiveQuantiser.anchor = GridBagConstraints.WEST;
		gbc_checkBoxAudioAdaptiveQuantiser.gridx = 2;
		gbc_checkBoxAudioAdaptiveQuantiser.gridy = 3;
		audioTabMainContentPanel.add(checkBoxAudioAdaptiveQuantiser, gbc_checkBoxAudioAdaptiveQuantiser);

		JLabel lblAudioBetaParameter = new JLabel("Quantiser beta parameter:");
		lblAudioBetaParameter.setFont(new Font("CMU Sans Serif", Font.PLAIN, 18));
		lblAudioBetaParameter.setForeground(Color.WHITE);
		GridBagConstraints gbc_lblAudioBetaParameter = new GridBagConstraints();
		gbc_lblAudioBetaParameter.insets = new Insets(0, 0, 5, 5);
		gbc_lblAudioBetaParameter.anchor = GridBagConstraints.WEST;
		gbc_lblAudioBetaParameter.gridx = 0;
		gbc_lblAudioBetaParameter.gridy = 4;
		gbc_lblAudioBetaParameter.weightx = 1.0;
		gbc_lblAudioBetaParameter.gridwidth = 2;
		audioTabMainContentPanel.add(lblAudioBetaParameter, gbc_lblAudioBetaParameter);

		JFormattedTextField formatedTextFieldAudioBetaParameter = new JFormattedTextField(new MaskFormatter("#"));
		formatedTextFieldAudioBetaParameter.setFont(new Font("CMU Sans Serif", Font.PLAIN, 18));
		formatedTextFieldAudioBetaParameter.setText("1");
		GridBagConstraints gbc_formatedTextFieldAudioBetaParameter = new GridBagConstraints();
		gbc_formatedTextFieldAudioBetaParameter.insets = new Insets(0, 0, 5, 0);
		gbc_formatedTextFieldAudioBetaParameter.anchor = GridBagConstraints.EAST;
		gbc_formatedTextFieldAudioBetaParameter.fill = GridBagConstraints.HORIZONTAL;
		gbc_formatedTextFieldAudioBetaParameter.gridx = 2;
		gbc_formatedTextFieldAudioBetaParameter.gridy = 4;
		gbc_formatedTextFieldAudioBetaParameter.weightx = 1.0;
		audioTabMainContentPanel.add(formatedTextFieldAudioBetaParameter, gbc_formatedTextFieldAudioBetaParameter);
		formatedTextFieldAudioBetaParameter.setColumns(10);

		JLabel lblAudioQParameter = new JLabel("Player's number of bits:");
		lblAudioQParameter.setFont(new Font("CMU Sans Serif", Font.PLAIN, 18));
		lblAudioQParameter.setForeground(Color.WHITE);
		GridBagConstraints gbc_lblAudioQParameter = new GridBagConstraints();
		gbc_lblAudioQParameter.insets = new Insets(0, 0, 5, 5);
		gbc_lblAudioQParameter.fill = GridBagConstraints.NONE;
		gbc_lblAudioQParameter.anchor = GridBagConstraints.NORTHWEST;
		gbc_lblAudioQParameter.gridx = 0;
		gbc_lblAudioQParameter.gridy = 5;
		gbc_lblAudioQParameter.weightx = 1.0;
		gbc_lblAudioQParameter.gridwidth = 2;
		audioTabMainContentPanel.add(lblAudioQParameter, gbc_lblAudioQParameter);

		JComboBox<Integer> comboBoxAudioQParameter = new JComboBox<Integer>();
		comboBoxAudioQParameter.setModel(new DefaultComboBoxModel<Integer>(new Integer[] { 8, 16 }));
		GridBagConstraints gbc_comboBoxAudioQParameter = new GridBagConstraints();
		gbc_comboBoxAudioQParameter.insets = new Insets(0, 0, 5, 0);
		gbc_comboBoxAudioQParameter.fill = GridBagConstraints.NONE;
		gbc_comboBoxAudioQParameter.anchor = GridBagConstraints.NORTHWEST;
		gbc_comboBoxAudioQParameter.gridx = 2;
		gbc_comboBoxAudioQParameter.gridy = 5;
		audioTabMainContentPanel.add(comboBoxAudioQParameter, gbc_comboBoxAudioQParameter);

		btnAudioSubmit = new JButton("Submit");
		btnAudioSubmit.setFont(new Font("CMU Sans Serif", Font.BOLD, 18));
		btnAudioSubmit.setEnabled(true);
		GridBagConstraints gbc_btnAudioSubmit = new GridBagConstraints();
		gbc_btnAudioSubmit.fill = GridBagConstraints.NONE;
		gbc_btnAudioSubmit.anchor = GridBagConstraints.EAST;
		gbc_btnAudioSubmit.gridx = 2;
		gbc_btnAudioSubmit.gridy = 6;
		audioTabMainContentPanel.add(btnAudioSubmit, gbc_btnAudioSubmit);

		JLabel lblAudioStatsOutput = new JLabel("Streaming speed = ");
		lblAudioStatsOutput.setFont(new Font("Courier new", Font.PLAIN, 14));
		lblAudioStatsOutput.setForeground(Color.GREEN);
		lblAudioStatsOutput.setBackground(Color.DARK_GRAY);
		GridBagConstraints gbc_lblAudioStatsOutput = new GridBagConstraints();
		gbc_lblAudioStatsOutput.insets = new Insets(0, 0, 5, -2);
		gbc_lblAudioStatsOutput.fill = GridBagConstraints.NONE;
		gbc_lblAudioStatsOutput.anchor = GridBagConstraints.ABOVE_BASELINE_TRAILING;
		gbc_lblAudioStatsOutput.gridx = 0;
		gbc_lblAudioStatsOutput.gridy = 7;
		audioTabMainContentPanel.add(lblAudioStatsOutput, gbc_lblAudioStatsOutput);

		JTextPane textPaneAudioStatsOutput = new JTextPane();
		textPaneAudioStatsOutput.setEditable(false);
		textPaneAudioStatsOutput.setFont(new Font("Courier new", Font.PLAIN, 14));
		textPaneAudioStatsOutput.setForeground(Color.GREEN);
		textPaneAudioStatsOutput.putClientProperty("Nimbus.Overrides", uiDefaults);
		textPaneAudioStatsOutput.putClientProperty("Nimbus.Overrides.InheritDefaults", true);
		textPaneAudioStatsOutput.setBackground(Color.DARK_GRAY);
		textPaneAudioStatsOutput.setText("0 KB/s");
		GridBagConstraints gbc_textPaneAudioStatsOutput = new GridBagConstraints();
		gbc_textPaneAudioStatsOutput.insets = new Insets(0, 0, 5, 0);
		gbc_textPaneAudioStatsOutput.anchor = GridBagConstraints.ABOVE_BASELINE_LEADING;
		gbc_textPaneAudioStatsOutput.fill = GridBagConstraints.NONE;
		gbc_textPaneAudioStatsOutput.gridx = 1;
		gbc_textPaneAudioStatsOutput.gridy = 7;
		gbc_textPaneAudioStatsOutput.gridwidth = 2;
		audioTabMainContentPanel.add(textPaneAudioStatsOutput, gbc_textPaneAudioStatsOutput);

		JProgressBar progressBarAudioStreamer = new JProgressBar();
		progressBarAudioStreamer.setMaximum(100);
		progressBarAudioStreamer.setMinimum(0);
		if (!UIManager.getLookAndFeel().getName().equals("Nimbus")) {
			progressBarAudioStreamer.setForeground(STATUS_LINE_ACTION_RUNNING);
		}
		progressBarAudioStreamer.setStringPainted(true);
		progressBarAudioStreamer.setFont(new Font("Courier new", Font.PLAIN, 14));
		progressBarAudioStreamer.setString("Download progress");
		GridBagConstraints gbc_progressBarAudioStreamer = new GridBagConstraints();
		gbc_progressBarAudioStreamer.insets = new Insets(0, 0, 5, 0);
		gbc_progressBarAudioStreamer.anchor = GridBagConstraints.CENTER;
		gbc_progressBarAudioStreamer.fill = GridBagConstraints.HORIZONTAL;
		gbc_progressBarAudioStreamer.gridx = 0;
		gbc_progressBarAudioStreamer.gridy = 8;
		gbc_progressBarAudioStreamer.gridwidth = 3;
		audioTabMainContentPanel.add(progressBarAudioStreamer, gbc_progressBarAudioStreamer);

		JProgressBar progressBarAudioPlayer = new JProgressBar();
		progressBarAudioPlayer.setMaximum(1000);
		progressBarAudioPlayer.setMinimum(0);
		if (!UIManager.getLookAndFeel().getName().equals("Nimbus")) {
			progressBarAudioPlayer.setForeground(STATUS_LINE_ACTION_DONE);
		}
		progressBarAudioPlayer.setStringPainted(true);
		progressBarAudioPlayer.setFont(new Font("Courier new", Font.PLAIN, 14));
		progressBarAudioPlayer.setString("Playback progress");
		GridBagConstraints gbc_progressBarAudioPlayer = new GridBagConstraints();
		gbc_progressBarAudioPlayer.insets = new Insets(0, 0, 5, 0);
		gbc_progressBarAudioPlayer.anchor = GridBagConstraints.NORTH;
		gbc_progressBarAudioPlayer.fill = GridBagConstraints.HORIZONTAL;
		gbc_progressBarAudioPlayer.gridx = 0;
		gbc_progressBarAudioPlayer.gridy = 9;
		gbc_progressBarAudioPlayer.weighty = 1.0;
		gbc_progressBarAudioPlayer.gridwidth = 3;
		audioTabMainContentPanel.add(progressBarAudioPlayer, gbc_progressBarAudioPlayer);


		// Status Line

		mainFrameStatusLine = new JLabel("Ready.");
		mainFrameStatusLine.setFont(new Font("CMU Sans Serif", Font.BOLD, 18));
		mainFrameStatusLine.setBackground(SystemColor.window);
		mainFrameStatusLine.setForeground(Color.DARK_GRAY);
		mainFrameStatusLine.setBorder(new EmptyBorder(0, 9, 3, 8));
		GridBagConstraints gbc_mainFrameStatusLine = new GridBagConstraints();
		gbc_mainFrameStatusLine.fill = GridBagConstraints.BOTH;
		gbc_mainFrameStatusLine.anchor = GridBagConstraints.SOUTH;
		gbc_mainFrameStatusLine.gridx = 0;
		gbc_mainFrameStatusLine.gridy = 1;
		gbc_mainFrameStatusLine.weightx = 1.0;
		mainFrame.getContentPane().add(mainFrameStatusLine, gbc_mainFrameStatusLine);

		// Frame General Settings
		mainFrame.setTitle("Δίκτυα Υπολογιστών ΙΙ");
		mainFrame.setBounds(WINDOW_INIT_X, WINDOW_INIT_Y, WINDOW_INIT_WIDTH, WINDOW_INIT_HEIGHT);
		mainFrame.setResizable(false);
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Action Listeners

		btnGlobalSettingsSubmit.addActionListener(
				new GlobalSettingsListener(formatedTextFieldServerPort, formatedTextFieldClientPort, UDPConnection));

		btnEchoSubmit
				.addActionListener(new EchoListener(formatedTextFieldEchoRequestCode, formatedTextFieldEchoDuration,
						formatedTextFieldEchoNumberOfPackets, checkBoxGetTemperature, textPaneEcho, UDPConnection));

		btnImageSubmit.addActionListener(new ImageListener(formatedTextFieldImageRequestCode, checkBoxImageControlFlow,
				comboBoxImagePacketLength, formatedTextFieldImageDuration, textPaneImageStatsOutput,
				lblImageRuntimeOutput, UDPConnection));
		
		btnAudioSubmit.addActionListener(new AudioListener(formatedTextFieldAudioRequestCode,
				checkBoxAudioRequestSpecificSample, formatedTextFieldAudioRequestSpecificSample,
				comboBoxAudioPoolSelect, formatedTextFieldAudioNumberOfPackets, checkBoxAudioAdaptiveQuantiser,
				formatedTextFieldAudioBetaParameter, comboBoxAudioQParameter, textPaneAudioStatsOutput,
				progressBarAudioStreamer, progressBarAudioPlayer, UDPConnection));

		checkBoxAudioRequestSpecificSample.addChangeListener(new AudioRequestSpecificSampleListener(
				formatedTextFieldAudioRequestSpecificSample, comboBoxAudioPoolSelect));

		checkBoxAudioAdaptiveQuantiser
				.addChangeListener(new AudioIsAdaptivelyQuantisedListener(formatedTextFieldAudioBetaParameter));


		btnOBDSubmit.addActionListener(new OBDListener(formatedTextFieldVehicleRequestCode, checkBoxVehicleUseUDP,
				formatedTextFieldVehicleDuration, lblVehicleEngineRunTimeOutput, lblVehicleAirTempOutput,
				lblVehicleThrottlePositionOutput, lblVehicleEngineRPMOutput, lblVehicleSpeedOutput,
				lblVehicleCoolantTemperatureOutput, lblVehiclePacketsTotalTimeOutput, TCPConnectionOBD, UDPConnection));

	}

}
