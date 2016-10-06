/**
 * (c) 2009 by Maximilian Strauch.
 * 
 * This program is distributed WITHOUT ANY WARRANTY; without even the implied 
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package com.neptune.client;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.neptune.client.gui.NeptuneClientGUI;
import com.neptune.client.gui.NeptuneClientSDD;
import com.neptune.model.GameFieldType;
import com.neptune.model.NeptuneClickPositionListener;
import com.neptune.model.NeptuneModel;
import com.neptune.server.lib.Log;
import com.neptune.server.lib.NeptuneProtocol;
import com.neptune.server.lib.Util;

public class NeptuneClient {

	private boolean debug;
	private NeptuneClientSDD ncsdd;
	private NeptuneClientGUI ncgui;
	private NeptuneClientConnection ncc;
	
	private Dimension courtSize;

	private boolean receivingMultilineMessage = false;
	private ArrayList<String> multilineMessage = null;
	private ArrayList<String> onloggedUsers = null;
	private NeptuneModel gameModel;
	private NeptuneModelClickEventListener eventHandler;
	private boolean gameFinished = false;
	private boolean quitWanted = false;
	private String name;
	
	public NeptuneClient() {
		this.debug = Boolean.parseBoolean(System.getProperty("neptune.debug"));
		this.gameModel = new NeptuneModel(0, 0);
		this.eventHandler = new NeptuneModelClickEventListener();
		
		this.onloggedUsers = new ArrayList<String>();
		
		// start collecting the data for starting the client
		Log.info("NeptuneClient started, going to collect the servers data ...",
				debug);
		this.initServerConnectionDialog();
	}

	private void initServerConnectionDialog() {
		// open the dialog in a edt
		final NeptuneClient nc = this;
		try {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					Log.info("Creating SDD-GUI-Dialog, please wait ...", debug);
					ncsdd = new NeptuneClientSDD(nc);
					ncsdd.setVisible(true);
				}
			});
		} catch (Exception e) {
			Log.error("There was an fatal exception: " + e.toString(), debug);
			this.fatalError();
		}
		return;
	}
	
	private void initMainGUI() {
		// open the main window
		final NeptuneClient nc = this;
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					Log.info("Creating the main gui, please wait ...", debug);
					ncgui = new NeptuneClientGUI(nc);
					ncgui.showStartPanel();
					ncgui.setVisible(true);
				}
			});
		} catch (Exception e) {
			Log.error("There was an fatal exception: " + e.toString(), debug);
			this.fatalError();
		}
	}
	
	/* ----------------------------------------------------------
	 * from the client connection
	 * ----------------------------------------------------------
	 */
	
	public synchronized void handleIncommingTransmission(String msg) {
		// important event: the quit event
		if (msg.startsWith(NeptuneProtocol.QUIT_CMD1)) {
			Log.info("The server sends the quit keyword: >>" + msg, debug);
			this.handleQuitAction();
		}
		
		/* ---------------------------------------
		 * multiline messages
		 * ---------------------------------------
		 */
		
		// check if a multiline message is in progress or ends
		if (this.receivingMultilineMessage && this.multilineMessage != null) {
			if (msg.equals(NeptuneProtocol.RESPONSE_END)) {
				// check if a multiline message ends
				String[] mm = new String[this.multilineMessage.size()];
				this.multilineMessage.toArray(mm);
				
				// remove the array
				this.multilineMessage.clear();
				this.multilineMessage = null;
				this.receivingMultilineMessage = false;
				System.gc();
				
				// the multiline message is read in
				Log.info("[>>] Multiline message assembled: " + Arrays.toString(mm), debug);
				
				// handle the court update event
				if (mm[0].startsWith(NeptuneProtocol.UPD_MSG)) {
					this.handleCourtUpdateAction(mm);
					return;
				}

				// handle the court ready event
				if (mm[0].startsWith(NeptuneProtocol.RDY_MSG)) {
					this.handleCourtSetUpAction(mm);
					return;
				}
			} else {
				// check if a multiline message comes in
				this.multilineMessage.add(msg);
				Log.info("[*] Incomming line for multiline message", debug);
			}
			return;
		}

		// check if a multiline message begins
		if (msg.startsWith(NeptuneProtocol.UPD_MSG) || 
				msg.startsWith(NeptuneProtocol.RDY_MSG)) {
			Log.info("Start new multiline message", debug);
			this.receivingMultilineMessage = true;
			this.multilineMessage = new ArrayList<String>();
			
			// add this line
			this.multilineMessage.add(msg);
			return;
		}
		
		/* ---------------------------------------
		 * single keyword actions
		 * ---------------------------------------
		 */
		
		// connection established - or not?
		if (msg.contains(NeptuneProtocol.CONN_BASIC)) { // connection
			Log.info("Connection established: >>" + msg, debug);
			if (msg.startsWith(NeptuneProtocol.DISC_MSG)) {
				// failed to connect: server rejected
				this.handleConnectionCreatedAction(false);
			} else {
				// all okay, dispose the dialog and start the main gui
				this.handleConnectionCreatedAction(true);
			}
			return;
		}
		
		// user logout event
		if (msg.startsWith(NeptuneProtocol.ULOG_MSG)) {
			Log.info("User logged out: >>" + msg, debug);
			
			// extract the users nick
			String name = null;
			try {
				name = msg.substring(msg.indexOf(NeptuneProtocol.SEPARATOR) + 1);
			} catch (Exception e) {
				Log.error("[!!] Cannot extract the user's nick", debug);
				return;
			}
			
			// call the event handler
			if (name != null) {
				this.handleUnexpectedUserLogout(name);
			}
			return;
		}
		
		// user login event
		if (msg.startsWith(NeptuneProtocol.ULIN_MSG)) {
			Log.info("User logged in: >>" + msg, debug);
			
			// extract the users nick
			String name = null;
			try {
				name = msg.substring(msg.indexOf(NeptuneProtocol.SEPARATOR) + 1);
			} catch (Exception e) {
				Log.error("[!!] Cannot extract the user's nick", debug);
				return;
			}
			
			// call the event handler
			if (name != null) {
				this.handleUnexpectedUserLogin(name);
			}
			return;
		}
		
		// game finished event
		if (msg.startsWith(NeptuneProtocol.GMEF_MSG)) {
			Log.info("The game is finished: >>" + msg, debug);
			
			// extract the highscore
			String highscore = null;
			try {
				highscore = msg.substring(msg.indexOf(NeptuneProtocol.SEPARATOR) + 1);
			} catch (Exception e) {
				Log.error("[!!] Cannot extract the highscore", debug);
				return;
			}
			
			// call the event handler
			if (highscore != null) {
				this.handleGameFinishedAction(highscore);
			} else {
				JOptionPane.showMessageDialog(this.ncgui, "Das Spiel wurde durch die anderen Mitspieler beendet", 
						"Spiel beendet", JOptionPane.INFORMATION_MESSAGE);
				this.ncgui.showStartPanel();
			}
			return;
		}
		
		// this user/client logout event
		if (msg.startsWith(NeptuneProtocol.LOG_MSG)) {
			Log.info("The user is logged out (forced): >>" + msg, debug);
			
			// call the handler
			this.handleLogoutAction();
			return;
		}
		
		// a game started
		if (msg.startsWith(NeptuneProtocol.L_CLOSED)) {
			Log.info("A game started, disable the start-button: >>" + msg, debug);
			this.ncgui.setStartButtonEnabled(false);
			return;
		}
		
		// a game finished
		if (msg.startsWith(NeptuneProtocol.L_OPENED)) {
			Log.info("A game finished, enable the start-button: >>" + msg, debug);
			this.ncgui.setStartButtonEnabled(true);
			return;
		}
		
		
		/* ---------------------------------------
		 * multiple keyword actions
		 * ---------------------------------------
		 */
		String[] transmission = new String[3];
		try {
			String container = msg;
			transmission[0] = container.substring(0, container.indexOf(NeptuneProtocol.SEPARATOR));
			container = container.substring(container.indexOf(NeptuneProtocol.SEPARATOR) + 1);
			transmission[1] = container.substring(0, container.indexOf(NeptuneProtocol.SEPARATOR));
			container = container.substring(container.indexOf(NeptuneProtocol.SEPARATOR) + 1);
			transmission[2] = container;
		} catch (Exception e) {
			Log.error("[!!] Cannot encode server response", debug);
			return;
		}
		Log.error("Server response: " + Arrays.toString(transmission), debug);
		
		// login successful - or not?
		if (transmission[1].equals(NeptuneProtocol.LOGIN_CMD)) {
			Log.info("Login result: >>" + msg, debug);
			if (transmission[0].equals(NeptuneProtocol.ERR_MSG)) {
				// failed to log in; check the cases:
				Log.error("Login failed", debug);
				this.name = null;
				
				// watch the error and tell it to the user
				if (Util.startChars(10, transmission[2]).equals(Util.startChars(10, NeptuneProtocol.LOGIN_NAME_ALREADY_KNOWN))) {
					Log.error("[!!] reason: user name is already known", debug);
					// gui info
					this.ncgui.loginErrorMsg("Der Benutzername ist bereits vergeben.");
				} else if (Util.startChars(10, transmission[2]).equals(Util.startChars(10, NeptuneProtocol.LOGIN_NAME_SYNTAX))) {
					Log.error("[!!] reason: user name contains wrong characters", debug);
					// gui info
					this.ncgui.loginErrorMsg("Der Benutzername beinhaltet verbotene Zeichen.");
				} else if (Util.startChars(10, transmission[2]).equals(Util.startChars(10, NeptuneProtocol.LOGIN_MAX_NUM_REACHED))) {
					Log.error("[!!] reason: maximum numbers of users reached", debug);
					// gui info
					this.ncgui.loginErrorMsg("Es ist bereits die maximale Nutzerzahl angemeldet.");
				} else if (Util.startChars(10, transmission[2]).equals(Util.startChars(10, NeptuneProtocol.LOGIN_GAME_RUNNING))) {
					Log.error("[!!] reason: a game is already running", debug);
					// gui info
					this.ncgui.loginErrorMsg("Es ist gerade ein Spiel im Gange.");
				} else if (Util.startChars(10, transmission[2]).equals(Util.startChars(10, NeptuneProtocol.LOGIN_FAILED))) {
					Log.error("[!!] reason: given password is wrong", debug);
					// gui info
					this.ncgui.loginErrorMsg("Der Benutzername ist bereits vergeben.");
				} else if (Util.startChars(10, transmission[2]).equals(Util.startChars(10, NeptuneProtocol.CMD_SYNTAX_ERR))) {
					Log.error("[!!] reason: syntax/server error", debug);
					// gui info
					this.ncgui.loginErrorMsg("Ein technischer Fehler ist aufgetreten.");
				}
				return;
			} else {
				// all okay
				Log.info("[:-] Login successful!", debug);
				this.handleLoginSuccessfulAction();
			}
		}
		
		// field not deleted
		if (transmission[1].equals(NeptuneProtocol.DELE_CMD)) {
			Log.info("Delete result: >>" + msg, debug);
			if (transmission[0].equals(NeptuneProtocol.ERR_MSG)) {
				// error
				// failed to delete the field
				Log.error("Liquidation failed", debug);
				
				// watch for the error
				if (Util.startChars(10, transmission[2]).equals(Util.startChars(10, NeptuneProtocol.DELE_NO_GAME_RUNNING))) {
					Log.error("[!!] reason: no game is running", debug);
				} else if (Util.startChars(10, transmission[2]).equals(Util.startChars(10, NeptuneProtocol.DELE_COORDS))) {
					Log.error("[!!] reason: coord containing errors", debug);
				} else if (Util.startChars(10, transmission[2]).equals(Util.startChars(10, NeptuneProtocol.CMD_NO_PERMISSION))) {
					Log.error("[!!] reason: access denied", debug);
				} else {
					Log.error("[!!] reason: syntax or server error", debug);
				}
				
				// call the handler
				this.handleDeleteFieldAction();
			} else {
				Log.fatal("[!!] This message cannot occur!", debug);
			}
		}
		
		// play a game
		if (transmission[1].equals(NeptuneProtocol.PLAY_CMD)) {
			Log.info("Play result: >>" + msg, debug);
			if (transmission[0].equals(NeptuneProtocol.ERR_MSG)) {
				// error
				this.handlePlayResponseAction(false, transmission[2]);
			} else {
				this.handlePlayResponseAction(true, null);
			}
		}
		return;
	}
	
	/* ----------------------------------------------------------
	 * event handlers for the gui/requests for the server
	 * ----------------------------------------------------------
	 */

	/**
	 * the client inquires to close the app
	 * @param frame callers (j-)frame
	 */
	public void handleExitApplicationAction(Component frame) {
		if (JOptionPane.showConfirmDialog(frame, "Soll NeptuneClient wirklich " +
				"beendet werden?", "Beenden", JOptionPane.OK_CANCEL_OPTION) == 0) {
			System.exit(0);
		}
		return;
	}
	
	/**
	 * the client wants to connect to the server
	 * @param ip ip adress of the server
	 * @param port port of the server
	 */
	public void notifyConnectToServer(String ip, int port) {
		// create a new client connection
		this.ncc = new NeptuneClientConnection(ip, port, this);
		
		// check the connection
		if (this.ncc.failure()) {
			Log.error("[!!] Cannot establish connection", debug);
			
			if (this.ncsdd != null) {
				// display an error msg
				JOptionPane.showMessageDialog(this.ncsdd,
						"Es konnte keine Verbindung zum Server aufgebaut werden.",
						"Verbinden fehlgeschlagen", JOptionPane.ERROR_MESSAGE);
				this.ncsdd.setStateOfComponents(true);
			} else {
				Log.error("[!!] NeptuneClientSDD is null; cannot display msg", debug);
			}
			return;
		}
		
		// wait for the server's response
		return;
	}
	
	/**
	 * the client wants to play
	 * @param nick nickname of the client
	 */
	public void notifyPlayGame(String nick) {
		Log.info("Trying to login at the server ...", debug);
		// build login str
		String loginCmd = NeptuneProtocol.LOGIN_CMD + NeptuneProtocol.SEPARATOR + nick;
		Log.info("[*] Cmd build, sending: <<" + loginCmd + ">>", debug);
		// send the command
		this.ncc.send(loginCmd);
		this.name = nick;
		
		// wait for the server's response
		return;
	}
	
	/**
	 * the user wants to delete a field
	 * @param x x coord of the field
	 * @param y y coord of the field
	 */
	public void notifyFieldDeleteAction(int x, int y) {
		Log.info("Trying to delete the field " + x + ", " + y + " and it's friends", debug);
		// build the delte str
		String deleCmd = NeptuneProtocol.DELE_CMD + NeptuneProtocol.SEPARATOR + x +
								NeptuneProtocol.SEPARATOR + y;
		Log.info("[*] Cmd build, sending: <<" + deleCmd + ">>", debug);
		// send the command
		this.ncc.send(deleCmd);
		
		// wait for the server's response
		return;
	}
	
	/**
	 * the user quits the app
	 */
	private void notifyQuitAction() {
		Log.info(" Sending quit cmd", debug);
		// build the quit str
		String quitCmd = NeptuneProtocol.QUIT_CMD1;
		Log.info(" [*] Cmd build, sending: <<" + quitCmd + ">>", debug);
		// send the command
		this.ncc.send(quitCmd);
		
		// wait for the server's response
		return;
	}
	
	/**
	 * sending the play cmd
	 */
	public void startGameAction() {
		Log.info("Sending play cmd", debug);
		// build the quit str
		String playCmd = NeptuneProtocol.PLAY_CMD;
		Log.info("[*] Cmd build, sending: <<" + playCmd + ">>", debug);
		// send the command
		this.ncc.send(playCmd);
		
		// wait for the server's response
		return;
	}
	
	/**
	 * quits the application and closes all conns
	 */
	public void notifyAppQuit() {
		Log.info("Quitting application ...", debug);
		
		// quitting the connection by sending keyword and calling close
		this.quitWanted = true;
		this.notifyQuitAction();
		return;
	}
	
	
	
	
	
	
	
	/* ----------------------------------------------------------
	 * inner events
	 * ----------------------------------------------------------
	 */
	
	private void handleLoginSuccessfulAction() {
		// show the game field
		this.ncgui.showGamePanel(this.name);
		
		Log.info("Show the start dialog with the play button ...", debug);
		// show the start dialog
		this.ncgui.showGameStartDialog();
	}
	
	private void handleUnexpectedUserLogout(String nick) {
		this.ncgui.userLogout(nick);
		// remove the user from the list
		this.modifyUserList(nick, true);
		
		// print the stack trace
		Log.info("Userlist: " + this.getUserListStackTrace(), debug);
		
		// call the gui event handler
		this.ncgui.handleUsersChangedAction();
		return;
	}
	
	private void handleUnexpectedUserLogin(String nick) {
		// add the user to the list
		this.modifyUserList(nick, false);
		
		// print the stack trace
		Log.info("Userlist: " + this.getUserListStackTrace(), debug);
		
		// call the gui event handler
		this.ncgui.handleUsersChangedAction();
		return;
	}
	
	private void handleGameFinishedAction(String highscoreAsString) {
		// change the gui to game finished
		this.gameFinished  = true;
		
		Log.info("Going to show the highscore of the current game", debug);
		// extract the highscore
		Object[][] highscore = this.extractHighScore(highscoreAsString);
		// show it
		this.ncgui.showHighscore(highscore);
		return;
	}
	
	private void handleQuitAction() {
		// close the connection
		this.ncc.close();
		Log.info("Connection to server closed", debug);
		
		if (!this.quitWanted) {
			Log.info("Server quitted this client ...", debug);
			// show a gui message
			JOptionPane.showMessageDialog(this.ncgui, "Der Server hat die Verbindung zu Ihnen " +
					"beendet.", "Verbindung geschlossen", JOptionPane.INFORMATION_MESSAGE);
		}
		
		// dispose the gui
		Log.info("Disposing the gui", debug);
		this.ncgui.dispose();
		
		// exit the jvm
		Log.info("Shutting down JVM, return value=0", debug);
		System.exit(0);
	}
	
	private void handleCourtUpdateAction(String[] cmd) {
		// check the response
		if (cmd.length != 2 || this.courtSize == null) {
			this.cannotUpdateGame();
			return;
		}
		
		// start extracting the number of points
		int points = -1;
		// extract the size of the game filed
		try {
			Matcher m = Pattern.compile("[0-9]+").matcher(cmd[0]);
			if (m.find()) {
				points = Integer.parseInt(m.group());
			}
		} catch (Exception e) {
			// lets happen ...
		}
		
		String[][] court = this.extractGameCourt(this.courtSize.width, this.courtSize.height, cmd[1]);
		// check
		if (court == null) {
			Log.error("Cannot parse given game court", debug);
			this.cannotUpdateGame();
			return;
		}
		
//		if (this.debug) {
//			Log.info("Update court", debug);
//			for (int i = 0; i < court.length; i++) {
//				String cline = "";
//				for (int j = 0; j < court[i].length; j++) {
//					cline += court[i][j] + " ";
//				}
//				Log.info(cline, debug);
//			}
//		}
//		
//		try {
//			Thread.sleep(200);
//		} catch (Exception e) {
//			// : handle exception
//		}
		
		// update
		Log.info("[>>] Updating court on the view", debug);
		this.ncgui.updateCourt(court, points);
		
		//this.ncgui.nv.repaint();
		
		this.ncgui.repaintView();
		return;
	}
	
	private void handleCourtSetUpAction(String[] cmd) {
		Log.info("Starting a new game ...", debug);
		// extract the court size
		int xsize = 0;
		int ysize = 0;
		try {
			Matcher m = Pattern.compile("[0-9]+").matcher(cmd[0]);
			if (m.find()) {
				xsize = Integer.parseInt(m.group());
				if (m.find()) {
					ysize = Integer.parseInt(m.group());
				}
			}
		} catch (Exception e) {
			Log.error("[!!] Exception while parsing the court size: " + e.toString(), debug);
			this.cannotStartGame();
			return;
		}
		
		// check the coordinates
		if (!Util.isInIntervall(NeptuneProtocol.COURT_MIN_X, NeptuneProtocol.COURT_MAX_X, xsize) ||
				!Util.isInIntervall(NeptuneProtocol.COURT_MIN_Y, NeptuneProtocol.COURT_MAX_Y, xsize)) {
			Log.error("[!!] Court dimensions are invalid: xsize=" + xsize + ", ysize=" + 
						ysize, debug);
			this.cannotStartGame();
			return;
		}
		Log.info("[*] Get court dimensions: xsize=" + xsize + ", ysize=" + ysize, debug);
		this.courtSize = new Dimension(xsize, ysize);
		
		// get the fields and colors
		ArrayList<GameFieldType> gfts = new ArrayList<GameFieldType>();
		Matcher m = Pattern.compile("[A-Za-z]+\\[.*?\\]").matcher(cmd[1]);
		while(m.find()) {
			String colorAndField = m.group();
			int r = -1;
			int g = -1;
			int b = -1;
			String type = null;
			
			// get the field type
			Matcher fieldType = Pattern.compile("[A-Za-z]+").matcher(colorAndField);
			if (fieldType.find()) {
				type = fieldType.group();
			}
			
			// get the colors
			Matcher colorElements = Pattern.compile("[0-9]+").matcher(colorAndField);
			if (colorElements.find()) {
				r = Integer.parseInt(colorElements.group());
				if (colorElements.find()) {
					g = Integer.parseInt(colorElements.group());
					if (colorElements.find()) {
						b = Integer.parseInt(colorElements.group());
					}
				}
			}
			
			// check and add
			if (Util.isColor(r, g, b) && type != null) {
				Color c = new Color(r, g, b);
				GameFieldType gf = new GameFieldType(type, c);
				gfts.add(gf);
				Log.info("Found new field type with color: " + type + ", " + c.toString(), debug);
			}
		}

		// convert to array 
		if (gfts.size() < 0) {
			Log.error("There are no game field types", debug);
			return;
		}
		GameFieldType[] filedTypes = new GameFieldType[gfts.size()];
		gfts.toArray(filedTypes);
		gfts.clear();
		
		// get the users and fields
		ArrayList<NeptuneClientUser> ufts = new ArrayList<NeptuneClientUser>();
		m = Pattern.compile("[A-Za-z0-9]+\\[.*?\\]").matcher(cmd[2]);
		while(m.find()) {
			String userAndFieldType = m.group();
			
			String name = null;
			ArrayList<String> fieldTypes = new ArrayList<String>();
			// get the name and reset the rest
			int startBrackets = userAndFieldType.indexOf("[");
			name = userAndFieldType.substring(0, startBrackets);
			userAndFieldType = userAndFieldType.substring(startBrackets);
			
			// get the filed types
			Matcher fieldType = Pattern.compile("[A-Za-z]+").matcher(userAndFieldType);
			while(fieldType.find()) {
				fieldTypes.add(fieldType.group());
			}
			
			// check and add
			String[] str = new String[fieldTypes.size()];
			fieldTypes.toArray(str);
			if (str.length > 0 && name != null) {
				NeptuneClientUser usr = new NeptuneClientUser(str, name, filedTypes);
				ufts.add(usr);
				Log.info("Found new user with colors: " + name + ", " + Arrays.toString(str), debug);
			}
		}
		
		// convert to array
		if (ufts.size() < 0) {
			Log.error("There are no users involved", debug);
			return;
		}
		NeptuneClientUser[] ncu = new NeptuneClientUser[ufts.size()];
		ufts.toArray(ncu);
		
		// get the game field
		String[][] gameField = this.extractGameCourt(xsize, ysize, cmd[3]);
	
		// check
		if (gameField == null) {
			Log.error("Cannot parse given game court", debug);
			return;
		}

		// create the model
		this.gameModel = new NeptuneModel(xsize, ysize);
		this.gameModel.setThirdPartyGameHandlingEnabled(true);
		this.gameModel.setFieldTypes(filedTypes);
		this.gameModel.createGameCourt(gameField);
		
		this.ncgui.setModel(this.gameModel, this.eventHandler, ncu);
		this.ncgui.disposeGameStartDialog();
	}
	
	private void handleConnectionCreatedAction(boolean state) {
		if (state) {
			// connection created ...
			this.ncsdd.dispose();
			this.initMainGUI();
		} else {
			// display an error msg
			JOptionPane.showMessageDialog(this.ncsdd,
					"Der Server verweigert die Verbindung.",
					"Verbinden fehlgeschlagen", JOptionPane.ERROR_MESSAGE);
			this.ncsdd.setStateOfComponents(true);
		}
		return;
	}
	
	private void handleDeleteFieldAction() {
		
	}
	
	private void handleLogoutAction() {
		if (this.gameFinished) {
			Log.info("Ignoring the logout because the user wants to view the results", debug);
			
			// ignore ...
			this.gameFinished = false;
		}
		
	}
	
	private void handlePlayResponseAction(boolean result, String response) {
		// case 1
		if (response != null && !result && response.startsWith(NeptuneProtocol.PLAY_MIN_UNDER.substring(0, 10))) {
			this.ncgui.playErrorMsg("Es sind mindestens 2 Spieler noetig fuer ein Spiel.");
			this.ncgui.setPlayButtonEnabled(true);
		}
		// case 2
		if (response != null && !result && response.startsWith(NeptuneProtocol.PLAY_SERVER_F.substring(0, 10))) {
			// the server cannot start the game, reset ...
			this.ncgui.showStartPanel();
		}
		return;
	}
	
	/* ----------------------------------------------------------
	 * other utils/gui messages
	 * ----------------------------------------------------------
	 */

	private void fatalError() {
		JOptionPane.showMessageDialog(null,
				"Neptune muss leider aufgrund eines fatalen Fehlers "
						+ "geschlossen werden.", "Entschuldigung ...",
				JOptionPane.ERROR_MESSAGE);
		System.exit(0);
	}
	
	private void cannotStartGame() {
		JOptionPane.showMessageDialog(null,
				"Das Spiel kann leider nicht gestartet werden.", "Entschuldigung ...",
				JOptionPane.ERROR_MESSAGE);
		System.exit(0);
	}
	
	private void cannotUpdateGame() {
		JOptionPane.showMessageDialog(null,
				"Das Updaten des Spielfeldes schlug fehl!", "Entschuldigung ...",
				JOptionPane.ERROR_MESSAGE);
		System.exit(0);
	}
	
	
	
	
	private String[][] extractGameCourt(int xsize, int ysize, String court) {
		String[][] result = new String[ysize][xsize];
		Matcher m = Pattern.compile("[A-Z\\-]+").matcher(court);
		int x = 0;
		int y = 0;
		
		while (m.find()) {
			result[y][x] = m.group();
			if (x < xsize-1) {
				x++;
			} else {
				x = 0;
				y++;
			}
		}
		return result;
	}
	
	private Object[][] extractHighScore(String highscoreAsString) {
		int length = highscoreAsString.split("],").length;
		Object[][] obbj = new Object[length][4];
		Matcher m = Pattern.compile("[A-Za-z0-9]+").matcher(highscoreAsString);
		
		int i = 0;
		int z = 0;
		while(m.find()) {
			obbj[i][z] = m.group();
			
			if (z < 3) {
				z++;
			} else {
				i++;
				z = 0;
			}
		}
		return obbj;
	}
	
	private void modifyUserList(String name, boolean remove) {
		// add a user
		if (!remove) {
			this.onloggedUsers.add(name);
			return;
		}
		
		// remove a user
		for (int i = 0; i < this.onloggedUsers.size(); i++) {
			if (this.onloggedUsers.get(i).equals(name)) {
				// check the calling case
				if (remove) {
					this.onloggedUsers.remove(i);
				}
				return;
			}
		}
		return;
	}
	
	private String getUserListStackTrace() {
		String stackTrace = "";
		for (int i = 0; i < this.onloggedUsers.size(); i++) {
			stackTrace += this.onloggedUsers.get(i);
			if (i < this.onloggedUsers.size() - 1) {
				stackTrace += ", ";
			}
		}
		return stackTrace;
	}
	
	public String[] retrieveUsers() {
		String[] users = new String[this.onloggedUsers.size()];
		this.onloggedUsers.toArray(users);
		return users;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/* ----------------------------------------------------------
	 * anonymous classes
	 * ----------------------------------------------------------
	 */
	
	/**
	 * This class handels the click-events on the court
	 * @author maximilianstrauch
	 */
	public class NeptuneModelClickEventListener implements NeptuneClickPositionListener {

		/**
		 * saves the last click event; is needed for a 'multiclick' bug
		 */
		private long lastOn = 0;
		
		/**
		 * Handle the onClick-Event
		 * @param x the x coord
		 * @param y the y coord
		 */
		public void onClick(int x, int y) {
			long now = System.currentTimeMillis();
			if ((this.lastOn + 150) >= now) {
				this.lastOn = now;
				Log.info("[!!] User onClick-event shorter than 150 ms, reject ...", debug);
				return;
			}
			this.lastOn = now;
			
			// get the coords
			x = ((int) x/gameModel.getFieldSize());
			y = ((int) y/gameModel.getFieldSize());
			
			// query the coords
			notifyFieldDeleteAction(x, y);
			return;
		}
		
	}
	
}