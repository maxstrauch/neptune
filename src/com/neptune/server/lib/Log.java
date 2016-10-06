/**
 * (c) 2009 by Maximilian Strauch.
 * 
 * This program is distributed WITHOUT ANY WARRANTY; without even the implied 
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package com.neptune.server.lib;


import java.util.Date;

public class Log {

	public static void info(String message, boolean debug) {
		if (debug) {
			System.out.println(new Date().toString() + " [INFO] " + message);
		}
	}

	public static void error(String message, boolean debug) {
		if (debug) {
			System.out.println(new Date().toString() + " [ERROR] " + message);
		}
	}
	
	public static void fatal(String message, boolean debug) {
		if (debug) {
			System.out.println(new Date().toString() + " [FATAL] " + message);
		}
	}
	
	
}
