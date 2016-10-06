/**
 * (c) 2009 by Maximilian Strauch.
 * 
 * This program is distributed WITHOUT ANY WARRANTY; without even the implied 
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package com.neptune.client.gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;

import com.neptune.client.NeptuneClient;
import com.neptune.client.NeptuneClientUser;
import com.neptune.client.NeptuneClient.NeptuneModelClickEventListener;
import com.neptune.client.gui.lib.HighscoreBar;
import com.neptune.client.gui.lib.PlayMembersBar;
import com.neptune.model.NeptuneModel;
import com.neptune.model.NeptuneView;
import com.neptune.server.lib.NeptuneProtocol;

public class NeptuneClientGUI extends JFrame {

	private static final long serialVersionUID = 1L;

	public static final String startGUI = "startApp";
	public static final String gameGUI = "gameApp";
	public static final String endGameGUI = "finishedGUI";

	private JList users;
	
	private NeptuneView nv;

	private JLabel points;
	private JPanel main;
	private NeptuneClient nc;
	private JButton start;
	private PlayMembersBar footer;
	private HighscoreBar highscore;

	private JButton play;

	public NeptuneClientGUI(NeptuneClient nc) {
		super("Neptune");
		this.nc = nc;
		
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
				handleQuitAction(true);
			}
		});
		
		
		
		this.setLayout(new BorderLayout());
		
		this.nv = new NeptuneView(new NeptuneModel(18, 13));
		
		
		
		
		
		JPanel start = this.createStartGUI(this);
		JPanel game = this.createGameGUI();
		JPanel endGame = this.createEndGameGUI();
		
		
		this.main = new JPanel();
		this.main.setLayout(new CardLayout());
		
		this.main.add(start, startGUI);
		this.main.add(game, gameGUI);
		this.main.add(endGame, endGameGUI);
		
		this.add(this.main, BorderLayout.CENTER);
		
		this.pack();
		this.setSize(new Dimension(
				(this.getInsets().left+this.getInsets().right)+600,
				(this.getInsets().top+this.getInsets().bottom)+470)
			);
		this.setMinimumSize(new Dimension(
				(this.getInsets().left+this.getInsets().right)+600,
				(this.getInsets().top+this.getInsets().bottom)+470)
			);
		this.setResizable(false);
		this.setLocationRelativeTo(null);
	}
	
	
	
	
	
	
	
	
	
	
	
	private JPanel createEndGameGUI() {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		this.highscore = new HighscoreBar();
		this.highscore.setLayout(new BorderLayout());
		
		Box forwardButton = new Box(BoxLayout.X_AXIS);
		forwardButton.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		JButton restart = new JButton("Weiter");
		restart.setIcon(new ImageIcon(this.getClass().getResource("/res/icons/right_16.png")));
		restart.setOpaque(false);
		restart.setVerticalTextPosition(AbstractButton.CENTER);
		restart.setHorizontalTextPosition(AbstractButton.LEFT);
		restart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				showInFrame(startGUI);
			}
		});
		
		forwardButton.add(Box.createHorizontalGlue());
		forwardButton.add(restart);
		
		this.highscore.add(forwardButton, BorderLayout.SOUTH);
		panel.add(this.highscore, BorderLayout.CENTER);
		return panel;
	}

	private JPanel createGameGUI() {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		
		/* head */
		BufferedImage img = null;
		try {
			img = ImageIO.read(this.getClass().getResource("/res/images/nc-head.png"));
		} catch (Exception e) {
			
		}
		final BufferedImage a = img;
		JPanel header = new JPanel() {
			private static final long serialVersionUID = 1L;

			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				if (a != null) {
					g.drawImage(a, 0, 0, this);
				} else {
					g.setColor(Color.black);
					g.fillRect(0, 0, 1000, 100);
				}
			}
		};
		header.setPreferredSize(new Dimension(0, 40));
		header.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
		header.setLayout(new BorderLayout());
		
		Box topBar = new Box(BoxLayout.X_AXIS);
		this.points = new JLabel("0 Punkte");
		this.points.setForeground(Color.lightGray);
		this.points.setFont(new Font(this.points.getFont().getFamily(), Font.BOLD, 14));
		topBar.add(this.points);
		topBar.add(Box.createHorizontalGlue());
		JButton quit = new JButton("Ende");
		quit.setOpaque(false);
		quit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				handleQuitAction(false);		
			}
		});
		topBar.add(quit);
		
		header.add(topBar, BorderLayout.CENTER);
		panel.add(header, BorderLayout.NORTH);
		
		/* neptune view */
		panel.add(this.nv, BorderLayout.CENTER);
		
		/* footer */
		this.footer = new PlayMembersBar(null);
		this.footer.setPreferredSize(new Dimension(0, 70));
		panel.add(this.footer, BorderLayout.SOUTH);
		return panel;
	}



	private JPanel createStartGUI(final NeptuneClientGUI those) {
		BufferedImage img = null;
		try {
			img = ImageIO.read(this.getClass().getResource("/res/images/nc-bg.png"));
		} catch (Exception e) {
		}
		
		
		final BufferedImage a = img;
		
		JPanel panel = new JPanel() {
			
			private static final long serialVersionUID = 1L;

			protected void paintComponent(Graphics g) {
				
				
				super.paintComponent(g);

				if (a != null) {
					g.drawImage(a, 0, -50, null);
				}
				
				
				
			}
			
		};

		panel.setLayout(new GridLayout(3, 0));
		
		
		JPanel top = new JPanel();
		top.setLayout(new BorderLayout());
		top.setOpaque(false);
		JLabel logo = new JLabel(new ImageIcon(this.getClass().getResource("/res/images/nc-logo.png")));
		top.add(logo, BorderLayout.WEST);
		
		// -----------------------------------
		
		JPanel center = new JPanel();
		center.setLayout(new BorderLayout());
		center.setOpaque(false);
		
		JPanel container = new JPanel();
		container.setPreferredSize(new Dimension(250, 0));
		container.setLayout(new BorderLayout());
		container.setOpaque(false);
		container.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		
		this.start = new JButton(new ImageIcon(this.getClass().getResource("/res/icons/tick_48.png")));
		start.setText("Spiel starten");
		start.setVerticalTextPosition(AbstractButton.BOTTOM);
		start.setHorizontalTextPosition(AbstractButton.CENTER);
		start.setOpaque(false);
		start.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String nickName = JOptionPane.showInputDialog(those, "Bitte geben " +
						"Sie Ihren Nicknamen ein:", "Spiel starten", 
						JOptionPane.QUESTION_MESSAGE);
				
				if (nickName == null) {
					return;
				}
				
				if (!nickName.matches(NeptuneProtocol.REGEX_VALID_USERNAME) || nickName.length() < 1) {
					JOptionPane.showMessageDialog(those, "Der Nickname ist nicht " +
							"moeglich!", "Fehler", JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				nc.notifyPlayGame(nickName);
			}
		});
		JButton quit = new JButton("Ende");
		quit.setOpaque(false);
		quit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				handleQuitAction(false);
			}
		});
		
		
		container.add(start, BorderLayout.CENTER);
		
		JPanel container2 = new JPanel();
		container2.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
		container2.setLayout(new BorderLayout());
		container2.setOpaque(false);
		
		container2.add(quit);
		
		container.add(container2, BorderLayout.SOUTH);
		
		
		
		center.add(container, BorderLayout.EAST);
		
		// -----------------------------------
		
		JPanel bottom = new JPanel();
		bottom.setLayout(new BorderLayout());
		bottom.setOpaque(false);
		bottom.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		
		JPanel uListContainer = new JPanel();
		uListContainer.setOpaque(false);
		uListContainer.setPreferredSize(new Dimension(250, 0));
		uListContainer.setBorder(BorderFactory.createTitledBorder("Angemeldete Mitspieler"));
		uListContainer.setLayout(new BorderLayout());
		
		
		this.users = new JList(new DefaultListModel());
		
		uListContainer.add(new JScrollPane(this.users), BorderLayout.CENTER);		
		
		
		
		
		bottom.add(uListContainer, BorderLayout.WEST);
		
		panel.add(top);
		panel.add(center);
		panel.add(bottom);
		return panel;
	}
	
	public void showStartPanel() {
		this.showInFrame(startGUI);
		this.setStartButtonEnabled(true);
	}
	
	public void showGamePanel(String name) {
		this.points.setText("0 Punkte");
		this.footer.setName(name);
		this.nv.resetModel(null);
		this.nv.unInstallMouseListener();
		this.nv.addClickPositionListener(null);
		this.nv.setThirdPartyGameHandlingEnabled(true);
		this.nv.repaint();
		this.footer.clear();
		this.setTitle("Neptune - " + name);
		this.showInFrame(gameGUI);
	}
	
	public void repaintView() {
		this.nv.repaint();
	}
	
	private void showHighscorePanel() {
		this.footer.setOtherError(false);
		this.showInFrame(endGameGUI);
	}

	public void setStartButtonEnabled(boolean enabled) {
		this.start.setEnabled(enabled);
	}

	public void setPlayButtonEnabled(boolean enabled) {
		this.play.setEnabled(enabled);
	}
	
	private void showInFrame(String panel) {
		CardLayout mgr = ((CardLayout) this.main.getLayout());
		mgr.show(this.main, panel);
	}

	public void showGameStartDialog() {
		this.footer.setOtherError(true);
		JPanel glass = new JPanel();
		glass.setOpaque(false);
		glass.setLayout(new BorderLayout());
		glass.addMouseListener(new MouseListener() {
			public void mouseReleased(MouseEvent e) {
			}
			
			public void mousePressed(MouseEvent e) {
			}
			
			public void mouseExited(MouseEvent e) {
			}
			
			public void mouseEntered(MouseEvent e) {
			}
			
			public void mouseClicked(MouseEvent e) {
			}
		});
		glass.addMouseMotionListener(new MouseMotionListener() {
			
			public void mouseMoved(MouseEvent e) {
			}
			
			public void mouseDragged(MouseEvent e) {
			}
		});
		
		
		JPanel main = new JPanel() {
			private static final long serialVersionUID = 1L;
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				g.setColor(new Color(0, 0, 0, 200));
				g.fillRect(0, 0, 1000, 1000);
			}
			
		};
		main.setLayout(null);
		main.setOpaque(false);
		this.play = new JButton(new ImageIcon(this.getClass().getResource("/res/icons/flag_48.png")));
		this.play.setText("PLAY");
		this.play.setVerticalTextPosition(AbstractButton.BOTTOM);
		this.play.setHorizontalTextPosition(AbstractButton.CENTER);
		this.play.setOpaque(false);
		this.play.setBounds(
				((this.getSize().width-this.getInsets().left-this.getInsets().right)-200)/2, 
				((this.getSize().height-this.getInsets().top-this.getInsets().bottom)-100)/2, 
				200, 100);
		this.play.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				((JButton) arg0.getSource()).setEnabled(false);
				nc.startGameAction();
			}
		});
		main.add(play);
		glass.add(main, BorderLayout.CENTER);
		glass.setVisible(true);
		this.setGlassPane(glass);
		this.getGlassPane().setVisible(true);
	}
	
	public void disposeGameStartDialog() {
		this.getGlassPane().setVisible(false);
	}
	
	public void showHighscore(Object[][] highscore) {
		this.showHighscorePanel();
		this.setTitle("Neptune");
		this.highscore.setHighscore(highscore);
		this.highscore.repaint();
	}
	
	public void updateCourt(String[][] newCourt, int points) {
		this.nv.updateCourt(newCourt);
		this.points.setText(points + " Punkte");
	}
	
	public void loginErrorMsg(String msg) {
		JOptionPane.showMessageDialog(this, msg, 
				"Anmelden fehlgeschlagen", JOptionPane.ERROR_MESSAGE);
	}
	
	public void playErrorMsg(String msg) {
		JOptionPane.showMessageDialog(this, msg, 
				"Spielerzahl zu klein", JOptionPane.ERROR_MESSAGE);
	}
	
	public void userLogout(String name) {
		this.footer.removeUser(name);
		this.footer.repaint();
	}

	public void setModel(NeptuneModel gameModel,
			NeptuneModelClickEventListener eventHandler, NeptuneClientUser[] ncu) {
		
		this.nv.resetModel(gameModel);
		this.nv.installMouseListener();
		this.nv.addClickPositionListener(eventHandler);
		this.nv.setThirdPartyGameHandlingEnabled(true);
		this.nv.repaint();
		
		this.footer.setUsers(ncu);
		this.footer.repaint();
	}

	public void handleUsersChangedAction() {
		String[] users = this.nc.retrieveUsers();
		DefaultListModel dlm = (DefaultListModel) this.users.getModel();
		
		if (users == null || dlm == null) {
			return;
		}
		
		dlm.removeAllElements();
		
		for (int i = 0; i < users.length; i++) {
			dlm.addElement(users[i]);
		}
		return;
	}
	
	private void handleQuitAction(boolean hard) {
		if (JOptionPane.showConfirmDialog(this, "Soll Neptune wirklich beendet werden?", 
				"Beenden?", JOptionPane.OK_CANCEL_OPTION) == 0) {
			this.nc.notifyAppQuit();
			if (hard) {
				System.exit(0);
			}
		}
	}

	/*
	public static void main(String[] args) {
		NCGUI n = new NCGUI(null);
		n.setVisible(true);
		
//		n.showGamePanel("max");
//		n.showGameStartDialog();
//		
//		
//		NeptuneModel nm = new NeptuneModel(20, 12);
//		nm.setThirdPartyGameHandlingEnabled(true);
//		
//		nm.setThirdPartyGameHandlingEnabled(true);
//		nm.setFieldTypes(new GameFieldType[]{
//				new GameFieldType("A", Color.orange),
//				new GameFieldType("B", Color.green),
//				new GameFieldType("C", Color.red),
//				new GameFieldType("D", Color.yellow)
//		});
//		
//		String[][] c = new String[12][20];
//		Random r = new Random();
//		for (int i = 0; i < c.length; i++) {
//			for (int j = 0; j < c[i].length; j++) {
//				c[i][j] = ((char)(65+r.nextInt(4))) + "";
//			}
//		}
//		
//		nm.createGameCourt(c);
//
//		
//		NeptuneClientUser[] ncu = new NeptuneClientUser[]{
//			new NeptuneClientUser(new String[]{"A", "B"}, "max", new GameFieldType[]{
//					new GameFieldType("A", Color.orange),
//					new GameFieldType("B", Color.green)
//			}),
//			new NeptuneClientUser(new String[]{"C", "D"}, "jojosadkasd jklasdj laksd lkasd as jkalsd jklas kjld", new GameFieldType[]{
//					new GameFieldType("C", Color.red),
//					new GameFieldType("D", Color.yellow)
//			})
//		};
//		
//		n.setModel(nm, null, ncu);
		
//		
//		n.showHighscore(new Object[][] {
//				// name - place - points - seconds
//				{"max","1","72","6"},
//				{"jojo","2","11","20"},
//				{"test1", "3", "10", "33"}
//		});
		
	}*/
	
}
