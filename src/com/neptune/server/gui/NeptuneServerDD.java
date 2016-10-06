/**
 * (c) 2009 by Maximilian Strauch.
 * 
 * This program is distributed WITHOUT ANY WARRANTY; without even the implied 
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package com.neptune.server.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.WindowConstants;

import com.neptune.gui.lib.RiverLayout;
import com.neptune.server.NeptuneServerLauncher;
import com.neptune.server.lib.Log;

public class NeptuneServerDD extends JFrame {

	private static final long serialVersionUID = 1L;

	private JSpinner serverPort;
	private JButton okay;
	private boolean debug;
	private JButton cancel;

	private JCheckBox askConn;

	private NeptuneServerLauncher nsl;

	public NeptuneServerDD(NeptuneServerLauncher nsl) {
		super("NepuneServer - Startoptionen");
		
		this.nsl = nsl;
		this.debug = Boolean.parseBoolean(System.getProperty("neptune.debug"));
		this.setLayout(new BorderLayout());
		this.setIconImage(new ImageIcon(this.getClass().getResource("/res/icons/neptune-logo.png")).getImage());

		// window close listener
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
				handleExitApp();
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
		this.serverPort = new JSpinner(new SpinnerNumberModel(6666, 1024, 65000, 1));
		this.askConn = new JCheckBox("Bei jeder neuen Verbindung nachfragen");
		this.askConn.setSelected(false);
	}

	private JPanel createHead() {
		JPanel panel = new JPanel();
		panel.setPreferredSize(new Dimension(0, 75));
		panel.setLayout(new BorderLayout());
		panel.add(new JLabel(new ImageIcon(this.getClass().getResource("/res/images/head-server.png"))), BorderLayout.CENTER);
		return panel;
	}

	private JPanel createBody() {
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createEmptyBorder(20, 10, 0, 10));
		panel.setLayout(new RiverLayout());
		panel.add(new JLabel("Port:"));
		panel.add("tab hfill", this.serverPort);
		panel.add("p", this.askConn);
		return panel;
	}

	private JPanel createFooter() {
		Box box = new Box(BoxLayout.X_AXIS);

		// the buttons
		this.cancel = new JButton("Beenden");
		this.okay = new JButton("Starten");

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
			boolean askConn = false;

			// collect
			try {
				port = (Integer) this.serverPort.getValue();
				askConn = this.askConn.isSelected();
			} catch (Exception e) {
				Log.error("Cannot parse given data (Port=" + port + "): " + e.toString(), debug);
				port = -1;
			}

			// check it ...
			if (1024 <= port && port <= 65000) {
				// okay ... disable all components
				this.setStateOfComponents(false);
				final int rPort = port;
				final boolean ask = askConn;
				new Thread(new Runnable() {
					public void run() {
						// execute it in a own thread
						nsl.handleStartServerRequest(rPort, ask);
					}
				}).start();
				return;
			}
			JOptionPane.showMessageDialog(this,
					"Port nicht akzeptiert!",
					"Eingaben fehlerhaft", JOptionPane.ERROR_MESSAGE);
		} else if (action.equals("CANC")) {
			// handle exit
			this.handleExitApp();
		}
		return;
	}
	
	public void setStateOfComponents(boolean enabled) {
		this.serverPort.setEnabled(enabled);
		this.askConn.setEnabled(enabled);
		this.okay.setEnabled(enabled);
		this.cancel.setEnabled(enabled);
	}
	
	
	
	private void handleExitApp() {
		if (JOptionPane.showConfirmDialog(this, "Soll NeptuneServer wirklich beendet werden?", "Beenden", 
				JOptionPane.YES_NO_OPTION) == 0) {
			System.exit(0);
		}
		return;
	}

}
