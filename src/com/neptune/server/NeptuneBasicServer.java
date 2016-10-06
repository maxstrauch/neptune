/**
 * (c) 2009 by Maximilian Strauch.
 * 
 * This program is distributed WITHOUT ANY WARRANTY; without even the implied 
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package com.neptune.server;

import com.neptune.server.lib.Log;
import com.neptune.server.lib.NeptuneProtocol;
import com.neptune.server.lib.Server;
import com.neptune.server.lib.Util;
import com.neptune.server.usermanager.User;
import com.neptune.server.usermanager.UserManager;

public class NeptuneBasicServer extends Server {

	private NeptuneServer main;
	private boolean debug;
	private int reveivedLines;

	public NeptuneBasicServer(int serverPort, NeptuneServer ns) {
		super(serverPort);
		this.main = ns;
		
		this.debug = Boolean.parseBoolean(System.getProperty("neptune.debug"));
	}

	public int getReceivedLines() {
		return this.reveivedLines;
	}
	
	public void clearReceivedLines() {
		this.reveivedLines = 0;
	}
	
	

	
	
	
	
	
	
	
	
	/*
	 * incomming request handler
	 */
	
	public void income(String ip, int port, String transmission) {
		this.reveivedLines++;
		this.handleRequestAction(ip, port, transmission);
	}
	
	private synchronized void handleRequestAction(String ip, int port, String msg) {
		String command, parameters = null;
		String request = msg;
		
		// check the request
		if (request == null) {
			// debug
			Log.error("The request-string is null", debug);
			return;
		} else {
			if (request.contains(NeptuneProtocol.SEPARATOR)) {
				// read out the command and the parameters
				command = request.substring(0, request.indexOf(NeptuneProtocol.SEPARATOR));
				parameters = request.substring(request.indexOf(NeptuneProtocol.SEPARATOR) + 1);
			} else {
				// no command seperator: no parameters
				command = request;
			}
		}
		
		// check at least the command
		if (command == null) {
			// debug
			Log.error("Cannot read command from request (REQUEST=" + request + ")", debug);
			return;
		}
		
		// debug: show the requests incomming		
		Log.info("Incomming request from Client (" + ip + "@" + port + "):" + msg, debug);
		
		// call the request-handler
		this.handleRequest(command, parameters, ip, port);
	}
	
	private synchronized void handleRequest(String command, String parameters, String ip, int port) {
		// extract all parameters/tokens
		String[] param = Util.readParameters(parameters);
		// convert the given command to upper case
		String cmd = command.toUpperCase();
		
		// try to set the depricated msg
		if (Util.isBadCommandSyntax(command)) {
			this.setDepricatedInfoForUser(ip, port);
		}
		
		// check to recognize the requested action
		if (cmd.equals(NeptuneProtocol.QUIT_CMD1) || 
				cmd.equals(NeptuneProtocol.QUIT_CMD2) ||
				cmd.equals(NeptuneProtocol.QUIT_CMD3)) {
			// quit
			this.quitAction(ip, port, cmd.equals(NeptuneProtocol.QUIT_CMD3));
		} else if (cmd.equals(NeptuneProtocol.LOGIN_CMD)) {
			// login
			this.loginAction(ip, port, param);
		} else if (cmd.equals(NeptuneProtocol.DELE_CMD)) {
			// delete a field from the court
			this.deleteAction(ip, port, param);
		} else if (cmd.equals(NeptuneProtocol.PLAY_CMD)) {
			// start a new game 
			this.playCommand(ip, port);
		} else {
			this.commandNotFoundAction(ip, port);
		}
	}
	
	/*
	 * command execution
	 */
	
	private synchronized void playCommand(String ip, int port) {
		// check weather the user is allowed to run this command
		if (this.main.getUM().getUser(ip, port) == null) {
			this.send(ip, port, NeptuneProtocol.ERR_MSG + NeptuneProtocol.SEPARATOR + NeptuneProtocol.CMD_NO_PERMISSION);
			Log.info("Client " + ip + "@" + port + ": " +
						"is not allowed to request a new game", debug);
			return;
		}
		
		// force check if a game is running
		if (this.main.isGameRunning()) {
			this.send(ip, port, NeptuneProtocol.ERR_MSG + NeptuneProtocol.SEPARATOR + NeptuneProtocol.PLAY_GAME_RUNNING);
			Log.info("Cannot request a new game (" + ip + "@" + port + "): " +
						"a game is already running", debug);
			return;
		}

		// call the handler method
		int result = this.main.handleNewGameRequest(this.main.getUM().getUser(ip, port).getName());
		
		if (result == -1) {
			// send response to the client
			this.send(ip, port, NeptuneProtocol.ERR_MSG + NeptuneProtocol.SEPARATOR + 
					NeptuneProtocol.PLAY_CMD + NeptuneProtocol.SEPARATOR + NeptuneProtocol.PLAY_MIN_UNDER);
		} else if (result == 0) {
			// send response to the client
			this.send(ip, port, NeptuneProtocol.OK_MSG + NeptuneProtocol.SEPARATOR + 
					NeptuneProtocol.PLAY_CMD + NeptuneProtocol.SEPARATOR + NeptuneProtocol.PLAY_OK);
		} else if (result == -2) {
			// send to all onlogged
			User[] usr = this.main.getUM().getAllUsers();
			if (usr != null) {
				for (int i = 0; i < usr.length; i++) {
					this.send(usr[i].getIp(), usr[i].getPort(), NeptuneProtocol.ERR_MSG + NeptuneProtocol.SEPARATOR + 
							NeptuneProtocol.PLAY_CMD + NeptuneProtocol.SEPARATOR + NeptuneProtocol.PLAY_SERVER_F);
				}
			}
		}
		this.main.handleUserStateChangedEvent();
	}

	private synchronized void deleteAction(String ip, int port, String[] param) {
		// check weather the user is allowed to run this command
		if (this.main.getUM().getUser(ip, port) == null) {
			this.send(ip, port, NeptuneProtocol.ERR_MSG + NeptuneProtocol.SEPARATOR + NeptuneProtocol.DELE_CMD + NeptuneProtocol.SEPARATOR + NeptuneProtocol.CMD_NO_PERMISSION);
			Log.info("Client " + ip + "@" + port + ": " +
						"is not allowed to delete a field", debug);
			return;
		}
		
		// check if a game is running
		if (!this.main.isGameRunning()) {
			this.send(ip, port, NeptuneProtocol.ERR_MSG + NeptuneProtocol.SEPARATOR + NeptuneProtocol.DELE_CMD + NeptuneProtocol.SEPARATOR + NeptuneProtocol.DELE_NO_GAME_RUNNING);
			Log.info("Cannot execute the delete request (" + ip + "@" + port + "): " +
						"no game is running", debug);
			return;
		}
		
		// force syntax error check
		if (param == null) {
			this.send(ip, port, NeptuneProtocol.ERR_MSG + NeptuneProtocol.SEPARATOR + NeptuneProtocol.DELE_CMD + NeptuneProtocol.SEPARATOR + NeptuneProtocol.CMD_SYNTAX_ERR);
			Log.info("NullPointer: param-array is null; client " + 
					ip + "@" + port, debug);
			return;
		}
		
		// check if there are all parameters
		if (param.length != 2) {
			// found syntax error
			this.send(ip, port, NeptuneProtocol.ERR_MSG + NeptuneProtocol.SEPARATOR + NeptuneProtocol.DELE_CMD + NeptuneProtocol.SEPARATOR + NeptuneProtocol.CMD_SYNTAX_ERR);
			Log.info("Syntax error: execute delete command failed; client " + 
					ip + "@" + port, debug);
			return;
		}
		
		int x = 0;
		int y = 0;
		
		// parse coordinates
		try {
			x = Integer.parseInt(param[0]);
			y = Integer.parseInt(param[1]);
		} catch (Exception e) {
			// found syntax error
			this.send(ip, port, NeptuneProtocol.ERR_MSG + NeptuneProtocol.SEPARATOR + NeptuneProtocol.DELE_CMD + NeptuneProtocol.SEPARATOR + NeptuneProtocol.DELE_COORDS);
			Log.info("Syntax error (cannot parse coords): execute delete command failed; client " + 
					ip + "@" + port, debug);
			return;
		}
		
		// debug
		Log.info("Delete command received from client: " + ip + "@" + port, debug);
		
		// handle the command
		this.main.handleDeleteAction(x, y, this.main.getUM().getUser(ip, port).getName());
	}

	private synchronized void commandNotFoundAction(String ip, int port) {
		// found syntax error in cmd
		this.send(ip, port, NeptuneProtocol.ERR_MSG + NeptuneProtocol.SEPARATOR + NeptuneProtocol.CMD_NOT_FOUND);
		Log.info("CommandNotFound: client " + ip + "@" + port + 
					" entered invalid command, close the connection ...", debug);
		
		// check the number of command errors
		boolean unknownUser = false;
		int cnt = 0;
		try {
			User usr = this.main.getUM().getUser(ip, port);
			cnt = usr.cmdFalseCnt;
			usr.cmdFalseCnt = cnt + 1;
		} catch (Exception e) {
			/* do nothing - smile ;-) */
			unknownUser = true;
		}
		
		if (cnt > 3 || unknownUser) {
			// send an unpolite good bye
			this.send(ip, port, NeptuneProtocol.CONNECTION_CLOSED_FORCED);
			this.closeConnection(ip, port);
		}
	}

	private synchronized void loginAction(String ip, int port, String[] param) {
		// the pre-check
		if (this.main.isGameRunning()) {
			this.send(ip, port, NeptuneProtocol.ERR_MSG + NeptuneProtocol.SEPARATOR + NeptuneProtocol.LOGIN_CMD + NeptuneProtocol.SEPARATOR + NeptuneProtocol.LOGIN_GAME_RUNNING);
			Log.info("Cannot login client " + ip + "@" + port + ": " +
						"game is already running", debug);
			return;
		}
		
		// force syntax error check
		if (param == null) {
			this.send(ip, port, NeptuneProtocol.ERR_MSG + NeptuneProtocol.SEPARATOR + NeptuneProtocol.LOGIN_CMD + NeptuneProtocol.SEPARATOR + NeptuneProtocol.CMD_SYNTAX_ERR);
			Log.info("NullPointer: param-array is null; client " + 
					ip + "@" + port, debug);
			return;
		}
		
		// max number of players are reached
		if (this.main.getUM().getNumerOfUsers() >= NeptuneProtocol.MAX_PLAYERS) {
			this.send(ip, port, NeptuneProtocol.ERR_MSG + NeptuneProtocol.SEPARATOR + NeptuneProtocol.LOGIN_CMD + NeptuneProtocol.SEPARATOR + NeptuneProtocol.LOGIN_MAX_NUM_REACHED);
			Log.info("Client " + ip + "@" + port + "tried to log in: rejected; max number of users reached.", debug);
			return;
		}
		
		// force parameter check and check if somebody wants human readable responses
		boolean userFriendly = false;
//		if (this.main.usesPassword()) {
//			// check if there are all parameters
//			if (param.length != 2 && param.length != 3) {
//				// found syntax error
//				this.send(ip, port, NeptuneProtocol.ERR_MSG + NeptuneProtocol.SEPARATOR + NeptuneProtocol.LOGIN_CMD + NeptuneProtocol.SEPARATOR + NeptuneProtocol.LOGIN_FAILED);
//				Log.info("Syntax error: login failed; client " + 
//						ip + "@" + port + "; using password YES/client send no password", debug);
//				return;
//			}
//			
//			// check the password
//			if (!this.main.getPassword().equals(param[1])) {
//				// syntax error expected
//				this.send(ip, port, NeptuneProtocol.ERR_MSG + NeptuneProtocol.SEPARATOR + NeptuneProtocol.LOGIN_CMD + NeptuneProtocol.SEPARATOR + NeptuneProtocol.LOGIN_FAILED);
//				Log.info("Access denied: wrong password; client " + 
//						ip + "@" + port + "; using password YES", debug);
//				return;
//			}
//			
//			// force check for human readable argument
//			if (param.length == 3) {
//				userFriendly = (param[2].equals(NeptuneProtocol.HUMAN_ARGV) ? true : false);
//			}
//		} else {
		// check if there are all parameters
		if (param.length != 1 && param.length != 2) {
			// found syntax error
			this.send(ip, port, NeptuneProtocol.ERR_MSG + NeptuneProtocol.SEPARATOR + NeptuneProtocol.LOGIN_CMD + NeptuneProtocol.SEPARATOR + NeptuneProtocol.CMD_SYNTAX_ERR);
			Log.info("Syntax error: login failed; client " + 
					ip + "@" + port + "; using password NO", debug);
			return;
		}
		
		// force check for human readable argument
		if (param.length == 2 && !param[1].equals(NeptuneProtocol.HUMAN_ARGV)) {
			this.send(ip, port, NeptuneProtocol.ERR_MSG + NeptuneProtocol.SEPARATOR + NeptuneProtocol.LOGIN_CMD + NeptuneProtocol.SEPARATOR + NeptuneProtocol.LOGIN_NO_PWD_NEEDED);
			Log.info("Client " + ip + "@" + port + "; using password, although it is not needed", debug);
			return;
		}
		
		if (param.length == 2 && param[1].equals(NeptuneProtocol.HUMAN_ARGV)) {
			userFriendly = true;
		} else {
			userFriendly = false;
		}
//		}
		
		// get the user name
		String username = param[0];
		
		// check the username
		if (!username.matches("[a-zA-Z0-9]+")) {
			// all right
			this.send(ip, port, NeptuneProtocol.ERR_MSG + NeptuneProtocol.SEPARATOR + NeptuneProtocol.LOGIN_CMD + NeptuneProtocol.SEPARATOR + NeptuneProtocol.LOGIN_NAME_SYNTAX);
			Log.info("Client " + ip + "@" +  port + " " +
						"has entered a user name containing wrong characters '" + username + "'", debug);
			return;
		}
		
		// create a new user object and set the human readable argument
		User usr = new User(ip, port, username);
		usr.userFriendly = userFriendly;
		
		// add the user
		int add = this.main.getUM().addUser(usr);
		
		// check the result of the last operation
		if (add == 1) {
			// all right
			this.send(ip, port, NeptuneProtocol.OK_MSG + NeptuneProtocol.SEPARATOR + NeptuneProtocol.LOGIN_CMD + NeptuneProtocol.SEPARATOR + NeptuneProtocol.LOGIN_OK);
			Log.info("Client " + ip + "@" +  port + " " +
						"has successfully logged on as user '" + username + "'; FLAG-USERFRIENDLY=" + userFriendly, debug);
			this.main.handleUserStateChangedEvent();
			this.newUserOnlogged(ip, port, username);
		} else if (add == -1) {
			// the username is already known
			this.send(ip, port, NeptuneProtocol.ERR_MSG + NeptuneProtocol.SEPARATOR + NeptuneProtocol.LOGIN_CMD + NeptuneProtocol.SEPARATOR + NeptuneProtocol.LOGIN_NAME_ALREADY_KNOWN);
			Log.info("Client " + ip + "@" +  port + " " +
						"cannot log in: username '" + username + "' already exists", debug);
		} else {
			// server error (the ArrayList in UserManager don't want to add the user)
			this.send(ip, port, NeptuneProtocol.ERR_MSG + NeptuneProtocol.SEPARATOR + NeptuneProtocol.LOGIN_CMD + NeptuneProtocol.SEPARATOR + NeptuneProtocol.SERVER_ERROR);
			Log.info("Cannot add user (" + ip + "@" +  port + ", USERNAME='" + 
					username + "')" + " to the array list", debug);
		}
	}

	private synchronized void quitAction(String ip, int port, boolean silentQuit) {
		// extract the user
		User usr = null;
		try {
			usr = this.main.getUM().getUser(ip, port);
		} catch (Exception e) { /* don't watch at exceptions */ }
		
		
		// check the quit method
		if (silentQuit) {
			// client inquires to close the connection
			Log.info("Client (" + ip + "@" + port + ") inquires to logout", debug);
			
			// remove the user from the list
			this.main.getUM().removeUser(this.main.getUM().getUser(ip, port));
			this.main.handleUserStateChangedEvent();
			
			// inform the user
			this.send(ip, port, NeptuneProtocol.LOG_MSG);
		} else {
			// client inquires to close the connection
			Log.info("Client (" + ip + "@" + 
					port + ") inquires to close the connection", debug);
			
			// send good bye
			this.send(ip, port, NeptuneProtocol.QUIT_CMD1);
			
			// user uses depr cmd's: send a msg to prevent
			if (usr != null && usr.usesDepricatedCommands) {
				this.send(ip, port, NeptuneProtocol.USING_DEPR_CMDS);
			}
			
			this.send(ip, port, NeptuneProtocol.CONNECTION_CLOSED);
			// remove the user from the list
			this.main.getUM().removeUser(this.main.getUM().getUser(ip, port));
			this.main.handleUserStateChangedEvent();
			
			// quit
			this.closeConnection(ip, port);
		}
		
		// informing the other clients
		if (usr != null) {
			this.announceWithExceptionOf(ip, port, NeptuneProtocol.ULOG_MSG + 
					NeptuneProtocol.SEPARATOR + usr.getName());
		} else {
			Log.error("[!!] Cannot inform the other clients", debug);
		}
		return;
	}

	
	
	
	private void newUserOnlogged(String ip, int port, String nick) {
		// inform all clients that a new user is online
		Log.info("Informing clients about a new 'player'", debug);
		
		// build a new cmd
		String cmd = NeptuneProtocol.ULIN_MSG + NeptuneProtocol.SEPARATOR + nick;
		Log.info("Sending cmd to all with exception of " + nick +
				" (" + ip + "@" + port + "): >>" + cmd, debug);
		this.announceWithExceptionOf(ip, port, cmd);
		return;
	}
	
	
	

	
	public void closeConnectionCorret(String ip, int port) {
		this.quitAction(ip, port, false);
	}
	
	
	
	
	
	
	
	/*
	 * close and open handling
	 */
	
	public void closed(String ip, int port) {
		this.connectionClosed(ip, port);
	}
	
	public void newcome(String ip, int port) {
		this.connectionOpened(ip, port);
	}
	
	private synchronized void connectionClosed(String ip, int port) {
		// call the method in the main class
		this.main.handleClosedConnection(ip, port);
		// remove the user from there
		this.main.getUM().removeUser(this.main.getUM().getUser(ip, port));
		Log.info("Connection from " + ip + "@" + port + " closed", debug);
		this.main.handleUserStateChangedEvent();
		// check if a game is running
		this.main.doGameCheck();
	}
	
	private synchronized void connectionOpened(String ip, int port) {
		// a new victim!
		Log.info("New client connected: " + ip + "@" + port, debug);
		
		// check weather the server is ready for the client
		if (!this.main.serverReady(ip, port)) {
			this.send(ip, port, NeptuneProtocol.DISC_MSG + NeptuneProtocol.SEPARATOR + NeptuneProtocol.SERVER_NOT_AVAILABLE);
			Log.info("Client rejected; connection not accepted", debug);
			// close the connection
			this.closeConnection(ip, port);
			return;
		}

		// accept client
		this.send(ip, port, Util.replaceFlags(new String[][]{
						{"%name", this.main.getServerName()}
				}, NeptuneProtocol.CONN_MSG + NeptuneProtocol.SEPARATOR + NeptuneProtocol.CONNECTION_CREATED));
		Log.info("Client accepted", debug);
		
		Log.info("Informing the user about the other users ...", debug);
		// inform the user that there are other players ...
		User[] usr = this.main.getUM().getAllUsers();
		if (usr != null) {
			for (int i = 0; i < usr.length; i++) {
				this.send(ip, port, NeptuneProtocol.ULIN_MSG + 
						NeptuneProtocol.SEPARATOR + usr[i].getName());
			}
		}
		this.main.handleUserStateChangedEvent();
	}
	
	/*
	 * utils
	 */
	
	private void setDepricatedInfoForUser(String ip, int port) {
		// get the manager
		UserManager um = this.main.getUM();
		if (um == null) {
			return;
		}
		
		// get the user
		User usr = um.getUser(ip, port);
		if (usr == null) {
			return;
		}
		
		// set the value
		usr.usesDepricatedCommands = true;
	}
	
}
