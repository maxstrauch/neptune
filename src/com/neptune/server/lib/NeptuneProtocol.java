/**
 * (c) 2009 by Maximilian Strauch.
 * 
 * This program is distributed WITHOUT ANY WARRANTY; without even the implied 
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package com.neptune.server.lib;

public class NeptuneProtocol {

	public static final String VERSION_STRING = "1.5G";
	public static final String SERVER_SIGNATURE = "neptune server (" + VERSION_STRING + ")";
	
	public static final int COURT_MIN_X = 5;
	public static final int COURT_MAX_X = 25;
	public static final int COURT_MIN_Y = 5;
	public static final int COURT_MAX_Y = 20;
	
	public static final int MIN_PLAYERS = 2;
	public static final int MAX_PLAYERS = 4;
	public static final int COLORS_PER_PLAYER = 2;
	
	// maxlength 1 char
	public static final String SEPARATOR = " ";
	public static final String FIELD_SEPARATOR = ",";
	public static final String LINE_SEPARATOR = ";";
	public static final String GAME_FIELD_NULL = "-";
	public static final String RESPONSE_END = ".";
	public static final String EQUALS = "=";
	public static final String CONN_BASIC = "CONNECTED";
	
	public static final String HUMAN_ARGV = "-h";
	public static final String REGEX_VALID_USERNAME = "[a-zA-Z0-9]+";
	public static final String COLOR_FIELD = "%typ%[%r%,%g%,%b%]";
	public static final String ENEMY_FIELD = "%usr%[%csv%]";
	public static final String HIGHSCORE_USER = "%usr%[%pos%.:%points%,%sec%]";
	
	public static final String OK_MSG = "+OK";
	public static final String ERR_MSG = "-ERR";

	public static final String CONN_MSG = "+" + CONN_BASIC;
	public static final String DISC_MSG = "-DIS" + CONN_BASIC;
	public static final String UPD_MSG = "+UPDATE";
	public static final String RDY_MSG = "+READY";
	public static final String GMEF_MSG = "+GAMEFINISHED";
	public static final String ULOG_MSG = "+USERLOGOUT";
	public static final String ULIN_MSG = "+USERLOGIN";
	public static final String LOG_MSG = "+LOGOUT";
	
	public static final String L_CLOSED = "-LOGINCLOSED";
	public static final String L_OPENED = "+LOGINOPENED";
	
	public static final String LOGIN_CMD = "LOGIN";
	public static final String QUIT_CMD1 = "QUIT";
	public static final String QUIT_CMD2 = "EXIT";
	public static final String QUIT_CMD3 = "LOGOUT";
	public static final String DELE_CMD = "DELE";
	public static final String PLAY_CMD = "PLAY";
	
	public static final String CONNECTION_CREATED = "%name ready. Nice to meet you.";
	public static final String CONNECTION_CLOSED = "Have a nice day, see ya later.";
	public static final String CONNECTION_CLOSED_FORCED = "I can break rules, too.";
	public static final String USING_DEPR_CMDS = "CAUTION: You should use for commands uppercase letters. Check the manual to correct these mistakes.";
	
	public static final String CMD_NOT_FOUND = "commant cannot be recognized.";
	public static final String CMD_SYNTAX_ERR = "check the syntax of the command you typed in.";
	public static final String CMD_NO_PERMISSION = "access denied.";
	
	public static final String SERVER_NOT_AVAILABLE = "Connection rejected. Try again later.";
	public static final String SERVER_ERROR = "that was my fault. Sorry.";
	
	public static final String LOGIN_OK = "login successfull.";
	public static final String LOGIN_MAX_NUM_REACHED = "max number of users reached.";
	public static final String LOGIN_NAME_ALREADY_KNOWN = "user name is already in use.";
	public static final String LOGIN_NAME_SYNTAX = "check the syntax of the user name; allowed chars: a-z, A-Z, 0-9";
	public static final String LOGIN_GAME_RUNNING = "to late! The game is already running. Try again later.";
	public static final String LOGIN_FAILED = "access denied. It seems that the password is wrong.";
	public static final String LOGIN_NO_PWD_NEEDED = "the given password is not needed.";
	
	@Deprecated
	public static final String DELE_OK = "";
	
	public static final String DELE_NO_GAME_RUNNING = "there is no active game.";
	public static final String DELE_COORDS = "invalid coordinates.";
	
	public static final String PLAY_OK = "game desire registered.";
	public static final String PLAY_GAME_RUNNING = "a game is already running. Try again later.";
	public static final String PLAY_MIN_UNDER = "at least two players are needed";
	public static final String PLAY_SERVER_F = "game cannot be startet (server error).";
	
	@Deprecated
	public static final String ONLY_IN_DEBUG_AVAIL = "this command is only available in the debug mode.";
	
	public static final String CCMD_READY = RDY_MSG + " %xsize%%sep%%ysize%";
	public static final String CCMD_UPD = UPD_MSG + " you have %points% points (total)";
	public static final String CCMD_GAME_FINISHED  = GMEF_MSG + " %score%";
	public static final String CCMD_USER_LOGOUT  = " %usr%";
	
}
