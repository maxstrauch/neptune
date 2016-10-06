/**
 * (c) 2009 by Maximilian Strauch.
 * 
 * This program is distributed WITHOUT ANY WARRANTY; without even the implied 
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package com.neptune.client.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.text.DecimalFormat;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import com.neptune.client.NeptuneClient;
import com.neptune.gui.lib.RiverLayout;
import com.neptune.server.lib.Log;

public class NeptuneClientSDD extends JFrame {

	private static final long serialVersionUID = 1L;
	private static final Pattern pat = Pattern
			.compile("\\b(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\."
					+ "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\."
					+ "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\."
					+ "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\b");

	private NeptuneClient nc;
	private JFormattedTextField serverIp;
	private JFormattedTextField serverPort;
	private JButton okay;
	private boolean debug;
	private JButton cancel;

	public NeptuneClientSDD(final NeptuneClient nc) {
		super("NepuneClient - Verbindungsdaten");
		this.debug = Boolean.parseBoolean(System.getProperty("neptune.debug"));
		this.nc = nc;
		this.setLayout(new BorderLayout());

		// window close listener
		final NeptuneClientSDD ncsdd = this;
		this.setIconImage(new ImageIcon(this.getClass().getResource("/res/icons/neptune-logo.png")).getImage());
		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		this.setLayout(new BorderLayout());
		this.addWindowListener(new WindowListener() {
			public void windowOpened(WindowEvent e) {
			}

			public void windowIconified(WindowEvent e) {
			}

			public void windowDeiconified(WindowEvent e) {
			}

			public void windowDeactivated(WindowEvent e) {
			}

			public void windowClosed(WindowEvent e) {
			}

			public void windowActivated(WindowEvent e) {
			}

			public void windowClosing(WindowEvent e) {
				nc.handleExitApplicationAction(ncsdd);
			}
		});

		this.initComponents();

		this.add(this.createHead(), BorderLayout.NORTH);
		this.add(this.createBody(), BorderLayout.CENTER);
		this.add(this.createFooter(), BorderLayout.SOUTH);

		this.pack();
		this.setSize(new Dimension(400, 250));
		this.setLocationRelativeTo(null);
		this.setResizable(false);
	}

	private void initComponents() {
		this.serverIp = new JFormattedTextField("127.0.0.1");
		this.serverPort = new JFormattedTextField(new DecimalFormat("###"));
		this.serverPort.setText("6666");
	}

	private JPanel createHead() {
		JPanel panel = new JPanel();
		panel.setPreferredSize(new Dimension(0, 75));
		panel.setLayout(new BorderLayout());
		panel.add(new JLabel(new ImageIcon(this.getClass().getResource("/res/images/head-client.png"))), BorderLayout.CENTER);
		return panel;
	}

	private JPanel createBody() {
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createEmptyBorder(20, 10, 0, 10));
		panel.setLayout(new RiverLayout());
		panel.add(new JLabel("Server-IP:"));
		panel.add("tab hfill", this.serverIp);
		panel.add("p", new JLabel("Server-Port:"));
		panel.add("tab hfill", this.serverPort);
		return panel;
	}

	private JPanel createFooter() {
		Box box = new Box(BoxLayout.X_AXIS);

		// the buttons
		this.cancel = new JButton("Abbrechen");
		this.okay = new JButton("Anmelden");

		// seperate the button
		box.add(Box.createHorizontalGlue());

		// okay button
		this.okay.setPreferredSize(this.cancel.getPreferredSize());
		this.okay.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				handleUserInteraction("OK");
			}
		});
		box.add(this.okay);

		// seperate the button
		box.add(Box.createHorizontalStrut(10));

		// cancel button
		this.cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				handleUserInteraction("CANC");
			}
		});
		box.add(this.cancel);

		// create the footer
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		panel.add(box, BorderLayout.CENTER);
		return panel;
	}

	private void handleUserInteraction(String action) {

		if (action.equals("OK")) {
			// check if all data are inserted
			int port = -1;
			String ip = null;

			// collect
			try {
				ip = this.serverIp.getText();
				port = Integer.parseInt(this.serverPort.getText());
			} catch (Exception e) {
				Log.error("Cannot parse given data (IP=" + ip + ", Port="
						+ port + "): " + e.toString(), debug);
				ip = null;
				port = -1;
			}

			// check it ...
			if (0 < port && port < 65000 && checkString(ip)) {
				// okay ... disable all components
				this.setStateOfComponents(false);
				final String rIp = ip;
				final int rPort = port;
				new Thread(new Runnable() {
					public void run() {
						// execute it in a own thread
						nc.notifyConnectToServer(rIp, rPort);
					}
				}).start();
				return;
			}
			JOptionPane.showMessageDialog(this,
					"IP oder Port des Servers sind syntaktisch falsch!",
					"Eingaben fehlerhaft", JOptionPane.ERROR_MESSAGE);
		} else if (action.equals("CANC")) {
			// handle exit
			this.nc.handleExitApplicationAction(this);
		}
		return;
	}
	
	public void setStateOfComponents(boolean enabled) {
		this.serverIp.setEnabled(enabled);
		this.serverPort.setEnabled(enabled);
		this.okay.setEnabled(enabled);
		this.cancel.setEnabled(enabled);
	}
	
	public static boolean checkString(String s) {
		return pat.matcher(s).matches();
	}

}
