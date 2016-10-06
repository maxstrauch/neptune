/**
 * (c) 2009 by Maximilian Strauch.
 * 
 * This program is distributed WITHOUT ANY WARRANTY; without even the implied 
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package com.neptune.server;

import java.util.ArrayList;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.neptune.server.gui.NeptuneServerGUI;
import com.neptune.server.lib.Log;
import com.neptune.server.lib.NeptuneProtocol;
import com.neptune.server.lib.Server;
import com.neptune.server.lib.Util;
import com.neptune.server.usermanager.User;
import com.neptune.server.usermanager.UserManager;


public class NeptuneServer {

	private boolean debug;
	private UserManager um;
	private NeptuneGameServer gameServer;
	private NeptuneBasicServer server;
	private String name = NeptuneProtocol.SERVER_SIGNATURE;
	private boolean gameRunning;
	
	private NeptuneServerGUI gui;
	private boolean askEveryConnection;
	private boolean serverEnabled = true;
	private User[] startGameUsers = null;
	
	public NeptuneServer(int port, boolean askOption) {
		
		this.debug = Boolean.parseBoolean(System.getProperty("neptune.debug"));
		this.askEveryConnection = askOption;
		
		this.gameRunning = false;
		
		// create a new user manager
		this.um = new UserManager();
		
		// start servers etc
		this.gameServer = new NeptuneGameServer(this);
		this.server = new NeptuneBasicServer(port, this);
		if (this.server.failure()) {
			Log.error("[!!] Cannot start server on port " + port, debug);
		}
		Log.error("[**] Server started on port " + port, debug);
		
		

		
		

	}
	
	
	
	
	
	
	public void forceQuitUser(String ip, int port, String name) {
		// close the connection unpolite
		this.server.send(ip, port, NeptuneProtocol.QUIT_CMD1);
		this.server.send(ip, port, NeptuneProtocol.CONNECTION_CLOSED_FORCED);
		this.server.closeConnection(ip, port);
		
		if (name != null) {
			// inform the others
			this.server.announce(NeptuneProtocol.ULOG_MSG + NeptuneProtocol.SEPARATOR + 
					name);
		}
		return;
	}
	
	
	
	public ArrayList<Object[]> getConnectionList() {
		ArrayList<Object[]> list = new ArrayList<Object[]>();
		
		// get a list of ALL connected clients
		Vector<Server.ServerClientConnection> scc = this.server.getConnectedClients();
		
		// loop through them and process them
		for (int i = 0; i < scc.size(); i++) {
			Server.ServerClientConnection sccTemp = scc.elementAt(i);
			
			// check if the user already exists
			User usrTemp = this.um.getUser(sccTemp.getIpAdress(), sccTemp.getPort());
			if (usrTemp != null) {
				list.add(new Object[]{
					usrTemp.getName(),	// 1. field: user's name or null
					usrTemp.getIp(), 	// 2. field: user's ip
					usrTemp.getPort(),	// 3. field: user's port
					usrTemp.isPlayCmdSend() // 4. field is play cmd send
				});
			} else {
				list.add(new Object[]{
						null,	// 1. field: user's name or null
						sccTemp.getIpAdress(), 	// 2. field: user's ip
						sccTemp.getPort(),	// 3. field: user's port
						false // 4. field is play cmd send
					});
			}
		}
		
		return list;
	}
	
	
	public void setServerReady(boolean ready) {
		this.serverEnabled = ready;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	public boolean status() {
		return (this.server.failure() ? false : true);
	}
	
	public void close() {
		Thread.currentThread().interrupt();
	}
	
	public void initServerGUI() {
		Log.info("Starting the server gui ...", debug);
		
		// starting dialog
		final NeptuneServer ns = this;
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					gui = new NeptuneServerGUI(ns);
					gui.setVisible(true);
				}
			});
		} catch (Exception e) {
			Log.fatal("Cannot start server GUI: " + e.toString(), debug);
			JOptionPane.showMessageDialog(null, "Die Server-GUI konnte nicht " +
					"gestartet werden.", "Entschuldigung", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
	}
	
	
//	private void initGui(final NeptuneServer ns) {
//		try {
//			SwingUtilities.invokeAndWait(new Runnable() {
//				public void run() {
//					gui = new NeptuneServerGUI(ns);
//					gui.setVisible(true);
//				}
//			});
//		} catch (Exception e) {
//			JOptionPane.showMessageDialog(null, "GUI failed to start. System exit.");
//			System.exit(0);
//		}
//	}
	
//	public void exit() {
//		this.gui.appendText("Beende ... ");
//		System.exit(0);
//	}
	
//	public boolean startNewGame(int x, int y) {
//		if (this.newGame(x, y) == 1) {
//			return true;
//		}
//		return false;
//	}
//	
	public synchronized boolean handleDeleteAction(int x, int y, String name) {
		// debug
		Log.info("Remove Field for user '" + name + "': x=" + x + ", y=" + y, debug);
		
		// do the removement
		if (this.gameServer.removeField(x, y, name)) {
			// all okay
			Log.info("User '" + name + "' has modified the court. Update it for all ...", debug);
			// send to all
			this.runUpdater();
			
			// check if anybody can remove fields
			if (!this.gameServer.anyCombinationsLeft()) {
				this.gameFinishedForAll();
				this.gui.setGameFinished();
				return false;
			}
			
			this.gui.appendCourt(this.gameServer.getCourtAsString(), x, y, name);
			return false;
		} else {
			return true;
		}
	}
	
	public synchronized int handleNewGameRequest(String name) {
		Log.info("New game request ...", debug);
		User[] u = this.um.getAllUsers();
		
		if (u.length < NeptuneProtocol.MIN_PLAYERS) {
			User usr = this.um.getUser(name);
			if (usr != null) {
				this.server.send(usr.getIp(), usr.getPort(), NeptuneProtocol.PLAY_MIN_UNDER);
			}
			return -1;
		}
		
		this.um.getUser(name).setPlayCmdSend(true);


		
		int all = 0;
		
		for (int i = 0; i < u.length; i++) {
			if (u[i].isPlayCmdSend()) {
				all++;
			}
		}

		if (all >= u.length) {
			Log.info("New Game started", debug);
			
			if (this.newGame(20, 12)) {
				return 0;
			} else {
				return -2;
			}
			
		}
		
		return 0;
	}	
	
	public synchronized void handleClosedConnection(String ip, int port) {
		// get the user
		User usr = this.um.getUser(ip, port);
		if (usr == null) {
			return;
		}
		// remove the user
		this.um.removeUser(usr);
		
		// broadcast it
		String name = usr.getName();
		this.sendToAllUsers(NeptuneProtocol.CCMD_USER_LOGOUT.replaceAll("%usr%", name), name);
		
		if (!this.isAPlayerOnline()) {
			this.gameRunning = false;
		}
//		this.gui.appendText("Update the user list (a user closed the connection) ...");
//		this.gui.updateUserList();
	}
	
	private boolean isAPlayerOnline() {
		if (this.um == null) {
			return false;
		}
		
		if (this.um.getAllUsers().length < 1) {
			return false;
		}
		return true;
	}

	
	/*
	 * gui/start methods
	 */
	
	/**
	 * -2: kann nicht senden: kein user
	 * -1: Court kann nicht erstellt werden
	 * 0: Spieleigenschaften falsch
	 * 1: alles klar
	 */
	private boolean newGame(int xsize, int ysize) {
		
		// start game
		this.gameRunning = true;

		// clear all
		this.gameServer.clearAll();
		
		// set all users playing
		User[] usr = (this.um == null ? null : this.um.getAllUsers());
		// forche check
		if (usr == null || usr.length < 1) {
//			this.gui.appendText("Cannot start game (no user online)!");
			this.gameRunning = false;
			return false;
		}
		for (int i = 0; i < usr.length; i++) {
			usr[i].setPlaying(true);
		}

		// create a new court (randomized)
		if (!this.gameServer.createNewCourt(xsize, ysize, this.um.getAllUsers(), NeptuneProtocol.COLORS_PER_PLAYER)) {
//			this.gui.appendText("Cannot start game (cannot create court)!");
			this.gameRunning = false;
			return false;
		}
		
		// compute all information for the users
		String fieldsAndColors = this.gameServer.getFieldAndColorsString();
		String enemies = this.gameServer.getEnemiesString();
		String court = this.gameServer.getCourtAsString();
		
		if (fieldsAndColors == null || enemies == null || court == null) {
//			this.gui.appendText("Cannot start game (wrong data)!");
			this.gameRunning = false;
			return false;
		}
		
		// create main command
		String[] cmd = new String[5];
		cmd[0] = Util.replaceFlags(
				new String[][]{
						{"%sep%", NeptuneProtocol.SEPARATOR},
						{"%xsize%", this.gameServer.getDimension().width + ""},
						{"%ysize%", this.gameServer.getDimension().height + ""},
						{"%clrs%", fieldsAndColors},
						{"%enem%", enemies},
						{"%court%", court}
				}, NeptuneProtocol.CCMD_READY);
		cmd[1] = fieldsAndColors;
		cmd[2] = enemies;
		cmd[3] = court;
		cmd[4] = NeptuneProtocol.RESPONSE_END;

		for (int i = 0; i < usr.length; i++) {
			String ip = usr[i].getIp();
			int port = usr[i].getPort();
			
			for (int j = 0; j < cmd.length; j++) {
				
				if (j == 3 && usr[i].userFriendly) {
					String[] cmdUserFriendly = cmd[j].split(NeptuneProtocol.LINE_SEPARATOR);
					for (int k = 0; k < cmdUserFriendly.length; k++) {
						this.server.send(ip, port, cmdUserFriendly[k]);
					}
				} else {
					this.server.send(ip, port, cmd[j]);
				}
			}
			
		}
		
		this.gui.appendCourt(court, -1, -1, null);
//		this.gui.appendText("New game started ...");
		this.startGameUsers = this.um.getAllUsers();
		
		
		this.server.announceWithExceptionOf(this.um.getAllUsers(), NeptuneProtocol.L_CLOSED);
		return true;
	}

	public void doGameCheck() {
		Log.info("Checking if a game should be closed", debug);
		User[] usr = this.um.getAllUsers();
		if (this.gameRunning) {
			if (usr != null && this.startGameUsers != null 
					&& usr.length > 0 && usr.length == this.startGameUsers.length) {
				Log.info("All okay, enough users", debug);
			} else {
				if (usr.length > 0 && usr.length != this.startGameUsers.length) {
					// a user logged of
					User[] usrs = this.um.getAllUsers();
					for (int i = 0; i < this.startGameUsers.length; i++) {
						if (!this.isIn(usrs, this.startGameUsers[i].getName())) {
							Log.info("User '" + this.startGameUsers[i].getName() + "' logged of!", debug);
							Log.info("[>>] Removing his field types from the court", debug);
							this.gameServer.userQuitted(this.startGameUsers[i].getFieldTypesAsArray());
							// run the updater
							this.runUpdater();
						}
					}
					// re-set userlist
					this.startGameUsers = usrs;
				} else {
					// deactivate game
					this.gameRunning = false;
					// announce it to all connected clients
					this.server.announce(NeptuneProtocol.L_OPENED);
				}
			}
		}
		return;
	}
	
	private boolean isIn(User[] usr, String name) {
		for (int i = 0; i < usr.length; i++) {
			if (usr[i].getName().equals(name)) {
				return true;
			}
		}
		return false;
	}
	
	private void runUpdater() {
		
		String court = this.gameServer.getCourtAsString();
		if (court == null) {
			return;
		}
		
		if (this.um == null) {
			return;
		}
		
		User[] usr = this.um.getAllUsers();
		for (int i = 0; i < usr.length; i++) {
				
			String ip = usr[i].getIp();
			int port = usr[i].getPort();
					
			this.server.send(ip, port, Util.replaceFlags(new String[][]{
					{"%points%", (usr[i].getCounter() != null ? usr[i].getCounter().getTotalPoints() + "" : "0")}
			}, NeptuneProtocol.CCMD_UPD));
			
			if (usr[i].userFriendly) {
				String[] cmdUserFriendly = court.split(NeptuneProtocol.LINE_SEPARATOR);
				for (int k = 0; k < cmdUserFriendly.length; k++) {
					this.server.send(ip, port, cmdUserFriendly[k]);
				}
			} else {
				this.server.send(ip, port, court);
			}
			
			this.server.send(ip, port, NeptuneProtocol.RESPONSE_END);
		}
	}
	
	private String createHighscore(ArrayList<NeptuneGameResult> currentGameResults) {
		String result = "";
				
		Object[][] highscore = new Object[currentGameResults.size()][2];
		int[] points = new int[currentGameResults.size()];
		
		for (int i = 0; i < currentGameResults.size(); i++) {
			NeptuneGameResult g = currentGameResults.get(i);
			highscore[i][0] = g.getName();
			highscore[i][1] = g;
			points[i] = g.getTotalPoints();
		}
		
		java.util.Arrays.sort(points);
		
		int i = points.length - 1;
		int ncnt = 0;
		while (i > -1) {
			result += Util.replaceFlags(new String[][]{
					{"%usr%", (String) highscore[i][0]},
					{"%pos%", String.valueOf((ncnt+1))},
					{"%points%", String.valueOf(points[i])},
					{"%sec%", ((NeptuneGameResult) highscore[i][1]).getTotalTimeAsSeconds()}
			}, NeptuneProtocol.HIGHSCORE_USER);
			
			if (ncnt < currentGameResults.size() - 1) {
				result += NeptuneProtocol.FIELD_SEPARATOR;
			}
			i--;
			ncnt++;
		}
		return result;
	}
	
	public void gameFinishedForAll() {
		// debug
		Log.info("Game finished for all users. Organize all things ...", debug);
		this.startGameUsers = null;
		
		// create the high score
		ArrayList<NeptuneGameResult> grs = new ArrayList<NeptuneGameResult>();
		long time = System.currentTimeMillis();
		User[] usrs = this.um.getAllUsers();
		for (int i = 0; i < usrs.length; i++) {
			// set the time
			usrs[i].getCounter().endTime = time;
			
			// get the game results
			NeptuneGameResult gr = usrs[i].assembleInformation();
			usrs[i].clearAll();
			grs.add(gr);
		}
		String highscore = this.createHighscore(grs);
		
		// send to all
		this.sendToAllUsers(NeptuneProtocol.CCMD_GAME_FINISHED.replaceAll("%score%", highscore), null);
		
		// log all users off
		User[] users = this.um.getAllUsers();
		for (int i = 0; i < users.length; i++) {
			this.server.send(users[i].getIp(), users[i].getPort(), NeptuneProtocol.LOG_MSG);
			this.um.removeUser(users[i]);
			this.server.announceWithExceptionOf(users[i].getIp(), users[i].getPort(), 
					NeptuneProtocol.ULOG_MSG + NeptuneProtocol.SEPARATOR + users[i].getName());
		}
		
		this.handleUserStateChangedEvent();
		// deactivate game
		this.gameRunning = false;
		// announce it
		this.server.announce(NeptuneProtocol.L_OPENED);
	}
	
	public void sendToAllUsers(String msg, String name) {
		User[] usr = this.um.getAllUsers();
		for (int i = 0; i < usr.length; i++) {
			String ip = usr[i].getIp();
			int port = usr[i].getPort();
			if (!usr[i].getName().equals(name)) {
				this.server.send(ip, port, msg);
			}
		}
	}
	
	
	public String getServerName() {
		return this.name;
	}
	
	public NeptuneBasicServer getServer() {
		return this.server;
	}

	
	public UserManager getUM() {
		return this.um;
	}
	
	public boolean isGameRunning() {
		return this.gameRunning;
	}
	

	
	
	
	
	
	
	public void handleUserStateChangedEvent() {
		Log.info("Init: repaint the user list", debug);
		// call the handler on the gui
		this.gui.handleUpdateUserList();
	}
	
	
	/**
	 * Checks if the server is ready or if the connection 
	 * should be accepted
	 * @param ip IP adress of the client wich want's to be connected
	 * @return whether this action is okay or not
	 */
	public boolean serverReady(String ip, int port) {
		// check if the server is enabled
		if (this.serverEnabled) {
			// check if the connection should be checked by the user
			if (this.askEveryConnection) {
				// show a confirm dialog
				if (JOptionPane.showConfirmDialog(this.gui, "Soll die Verbindung von dem " +
						"Client\r\n        " + ip + "@" + port + "\r\nakzeptiert werden?", "Neue " +
						"Verbindung", JOptionPane.YES_NO_OPTION) == 0) {
					return true;
				}
				return false;
			}
			return true;
		}
		return false;
	}
	
	
	public void handleQuitAction() {
		// quit all users
		Log.info("Quitting all users and shutting down the server ...", debug);
		// get a list of ALL connected clients
		Vector<Server.ServerClientConnection> scc = this.server.getConnectedClients();
		
		// loop through them and process them
		for (int i = 0; i < scc.size(); i++) {
			Server.ServerClientConnection sccTemp = scc.elementAt(i);
			this.server.closeConnection(sccTemp.getIpAdress(), sccTemp.getPort());
		}
		
		// shutting down the server
		this.server.close();
		this.gui.dispose();
		return;
	}
	
}
