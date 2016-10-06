/**
 * (c) 2009 by Maximilian Strauch.
 * 
 * This program is distributed WITHOUT ANY WARRANTY; without even the implied 
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package com.neptune.server.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.WindowConstants;

import com.neptune.server.NeptuneServer;
import com.neptune.server.lib.Log;
import com.neptune.server.lib.NeptuneProtocol;

public class NeptuneServerGUI extends JFrame {

	private static final long serialVersionUID = 1L;
	
	private boolean debug;
	private NeptuneServer ns;
	private ArrayList<Object[]> users;
	
	private JList connectionList;
	private JCheckBox serverReady;
	private JTextArea gameHistory;

	private JTextPane about;

	public NeptuneServerGUI(NeptuneServer ns) {
		super("NeptuneServer " + NeptuneProtocol.VERSION_STRING);
		this.ns = ns;
		this.users = new ArrayList<Object[]>();
		this.debug = Boolean.parseBoolean(System.getProperty("neptune.debug"));
		
		
		// gui
		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		this.setIconImage(new ImageIcon(this.getClass().getResource("/res/icons/neptune-logo.png")).getImage());
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
				handleExitAction();
			}
		});
		this.initComponents();
		
		JPanel container = new JPanel();
		container.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
		container.setLayout(new BorderLayout());
		container.add(this.createBody(), BorderLayout.CENTER);
		this.add(container, BorderLayout.CENTER);
		this.add(this.createFooter(), BorderLayout.SOUTH);
		
		this.pack();
		this.setSize(new Dimension(
				(this.getInsets().left+this.getInsets().right)+390,
				(this.getInsets().top+this.getInsets().bottom)+300)
			);
		this.setResizable(false);
		this.setLocationRelativeTo(null);
	}
	
	private void initComponents() {
		this.connectionList = new JList(new DefaultListModel());
		this.serverReady = new JCheckBox("Verbindungen annehmen");
		this.serverReady.setSelected(true);
		this.serverReady.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				ns.setServerReady(serverReady.isSelected());
			}
		});
		
		this.gameHistory = new JTextArea();
		this.gameHistory.setEditable(false);
		this.gameHistory.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		this.setNoGameMsg();
		
		this.about = new JTextPane();
		this.about.setEditable(false);
		this.about.setText(
				"NeptuneServer " + NeptuneProtocol.VERSION_STRING + " und NeptuneClient" +
				"(c) 2009, Maximilian Strauch.\r\n" +
				"Lizenz: CreativeCommons by-nc-sa" +
				"\r\n\r\n" +
				"Benutzte APIs:\r\n" +
				"(1) RiverLayout by David Ekholm (v. 1.1 (2005-05-23))\r\n" +
				"Lizenz: LGPL"
		);
	}

	private JTabbedPane createBody() {
		JTabbedPane panel = new JTabbedPane();
		
		// general panel
		JPanel general = new JPanel();
		general.setLayout(new BorderLayout());
		general.setOpaque(false);
		general.setBackground(Color.yellow);
		general.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		
		
		// create the head with the connection list
		JPanel headPart = new JPanel();
		headPart.setOpaque(false);
		headPart.setBorder(BorderFactory.createTitledBorder("Verbindungen"));
		headPart.setPreferredSize(new Dimension(0, 200));
		headPart.setLayout(new BorderLayout());
		
		JPanel cont01 = new JPanel();
		cont01.setOpaque(false);
		cont01.setLayout(new BorderLayout());
		cont01.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		
		Box head = new Box(BoxLayout.X_AXIS);
		JButton kick = new JButton(new ImageIcon(this.getClass().getResource("/res/icons/delete_16.png")));
		kick.setToolTipText("Benutzer abmelden");
		kick.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				handleKickSelectedUserAction();
			}
		});
		
		
		head.add(kick);
		head.add(Box.createHorizontalGlue());
		cont01.add(head, BorderLayout.NORTH);
		this.serverReady.setOpaque(false);
		cont01.add(this.serverReady, BorderLayout.SOUTH);
		JPanel container = new JPanel();
		container.setLayout(new BorderLayout());
		container.setOpaque(false);
		container.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
		container.add(new JScrollPane(this.connectionList), BorderLayout.CENTER);
		cont01.add(container, BorderLayout.CENTER);

		headPart.add(cont01, BorderLayout.CENTER);
		general.add(headPart, BorderLayout.CENTER);
		panel.addTab("Allgemein", general);
		
		JPanel history = new JPanel();
		history.setLayout(new BorderLayout());
		history.setOpaque(false);
		history.setBackground(Color.yellow);
		history.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		history.add(new JScrollPane(this.gameHistory), BorderLayout.CENTER);
		panel.addTab("Spielverlauf", history);
		
		JPanel about = new JPanel();
		about.setLayout(new BorderLayout());
		about.setOpaque(false);
		about.setBackground(Color.yellow);
		about.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		about.add(new JScrollPane(this.about), BorderLayout.CENTER);
		panel.addTab("About", about);
		return panel;
	}
	
	
	private JPanel createFooter() {
		Box box = new Box(BoxLayout.X_AXIS);

		// the buttons
		JButton cancel = new JButton("Beenden");

		// seperate the button
		box.add(Box.createHorizontalGlue());

		// cancel button
		cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				handleExitAction();
			}
		});
		box.add(cancel);

		// create the footer
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		panel.add(box, BorderLayout.CENTER);
		return panel;
	}
	
	public void setGameFinished() {
		this.gameHistory.append("Spielende!");
	}
	
	public void setNoGameMsg() {
		this.gameHistory.setText("Derzeit ist kein Spiel aktiv.");
	}
	
	public void appendCourt(String court, int x, int y, String name) {
		String result = "";
		if (name != null && x > -1 && y > -1) {
			result += "\r\n\r\nFeld x=" + x + ", y=" + y + " entfernt von " + name + "\r\n";
		} else {
			this.gameHistory.setText("");
			result += "Spiel gestartet\r\n-----------------------------\r\n";
		}

		result += court.replaceAll(NeptuneProtocol.FIELD_SEPARATOR, " ")
				.replaceAll(NeptuneProtocol.LINE_SEPARATOR, "\r\n");
		this.gameHistory.append(result);
	}

	public void handleUpdateUserList() {
		try {
			// retrieve users
			ArrayList<Object[]> usrs = this.ns.getConnectionList();
			DefaultListModel dlm = (DefaultListModel) this.connectionList.getModel();
			
			if (dlm == null || usrs == null) {
				return;
			}
			
			this.users.clear();
			for (int i = 0; i < usrs.size(); i++) {
				this.users.add(usrs.get(i));
			}
			
			
			dlm.removeAllElements();
			
			for (int i = 0; i < this.users.size(); i++) {
				Object[] temp = this.users.get(i);
				dlm.addElement(
						(((Boolean) temp[3]) ? "[p]" : "[-]") + " " +
						(((String) temp[0]) != null ? temp[0] : "[connected]") + " " +
						"(" + temp[1] + "@" + temp[2] + ")"
				);
			}
			Log.info("[GUI] User list updated.", debug);
		} catch (Exception e) {
			Log.error("[GUI] Cannot update user list: " + e.toString(), debug);
		}
		return;
	}
	
	private void handleKickSelectedUserAction() {
		// retrive selection
		int[] indices = this.connectionList.getSelectedIndices();
		if (indices.length < 1) {
			return;
		}
		
		// remove them
		for (int i = 0; i < indices.length; i++) {
			try {
				this.ns.forceQuitUser(
						((String) this.users.get(indices[i])[1]),
						((Integer) this.users.get(indices[i])[2]),
						((String) this.users.get(indices[i])[0])
				);
			} catch (Exception e) {
				Log.error("Cannot force quit user (list.length=" + this.users.size() + 
						", index=" + indices[i] + "): " + e.toString(), debug);
				e.printStackTrace();
			}
		}
		
		// repaint or update the list
		this.handleUpdateUserList();
		return;
	}
	
	private void handleExitAction() {
		if (JOptionPane.showConfirmDialog(this, "Soll NeptuneServer wirklich beendet werden?", 
				"Beenden", JOptionPane.YES_NO_OPTION) == 0) {
			this.ns.handleQuitAction();
			System.exit(0);
		}
		return;
	}
	
}
