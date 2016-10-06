/**
 * (c) 2009 by Maximilian Strauch.
 * 
 * This program is distributed WITHOUT ANY WARRANTY; without even the implied 
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package com.neptune.run;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

import com.neptune.server.lib.Log;

public class NeptuneRun extends JFrame {

	private static final long serialVersionUID = 1L;

	private boolean debug;
	private JButton startServer;
	private JButton startClient;
	private Run run;

	public NeptuneRun(Run run) {
		super("Neptune");
		this.run = run;
		
		this.debug = Boolean.parseBoolean(System.getProperty("neptune.debug"));
		
		Log.info("NeptuneRun startet ...", debug);
		
		this.setLayout(new BorderLayout());
		this.setIconImage(new ImageIcon(this.getClass().getResource("/res/icons/neptune-logo.png")).getImage());

		// window close listener
		this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		this.setLayout(new BorderLayout());

		this.initComponents();

		this.add(this.createBody(), BorderLayout.CENTER);

		this.pack();
		this.setSize(new Dimension(275, 175));
		this.setLocationRelativeTo(null);
		this.setResizable(false);
	}

	private void initComponents() {
		this.startServer = new JButton(new ImageIcon(this.getClass().getResource("/res/icons/flag_32.png")));
		this.startServer.setText("NeptuneServer");
		this.startServer.setIconTextGap(20);
		this.startServer.setHorizontalAlignment(SwingConstants.LEFT);
		this.startServer.setFont(new Font(this.startServer.getFont().getFamily(), Font.BOLD, 16));
		this.startServer.setPreferredSize(new Dimension(0, 40));
		this.startServer.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				run.handleStartServerRequest();
			}
		});
		
		this.startClient = new JButton(new ImageIcon(this.getClass().getResource("/res/icons/monitor_32.png")));
		this.startClient.setText("NeptuneClient");
		this.startClient.setIconTextGap(20);
		this.startClient.setHorizontalAlignment(SwingConstants.LEFT);
		this.startClient.setFont(new Font(this.startServer.getFont().getFamily(), Font.BOLD, 16));
		this.startClient.setPreferredSize(new Dimension(0, 40));
		this.startClient.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				run.handleStartClientRequest();
			}
		});
	}

	private JPanel createBody() {
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
		panel.setLayout(new GridLayout(2, 0, 0, 10));
		panel.add(this.startClient);
		panel.add(this.startServer);
		return panel;
	}

}
