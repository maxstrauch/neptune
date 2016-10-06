/**
 * (c) 2009 by Maximilian Strauch.
 * 
 * This program is distributed WITHOUT ANY WARRANTY; without even the implied 
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package com.neptune.run;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.neptune.client.NeptuneClient;
import com.neptune.server.NeptuneServerLauncher;
import com.neptune.server.lib.Log;
import com.neptune.server.lib.NeptuneProtocol;

public class Run {

	private static final String DEBUG = "-d";
	private static final String START_SERVER = "-s";
	private static final String START_CLIENT = "-c";
	
	private boolean debug;
	private NeptuneRun runGui;

	public Run(boolean debug, boolean startServer, boolean startClient) {
		if (debug) {
			System.setProperty("neptune.debug", "true");
		} else {
			System.setProperty("neptune.debug", "false");
		}
		
		this.setLookAndFeel();
		
		this.debug = Boolean.parseBoolean(System.getProperty("neptune.debug"));
		
		if (!startServer && !startClient) {
			Log.info("Starting NeptuneRun-GUI ...", debug);
			final Run r = this;
			try {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						runGui = new NeptuneRun(r);
						runGui.setVisible(true);
					}
				});
			} catch (Exception e) {
				Log.fatal("Cannot start choose GUI: " + e.toString(), debug);
				JOptionPane.showMessageDialog(null, "Die Auswahl-GUI konnte nicht " +
						"gestartet werden.", "Entschuldigung", JOptionPane.ERROR_MESSAGE);
				System.exit(0);
			}
		} else if (!startServer && startClient) {
			this.handleStartClientRequest();
		} else if (startServer && !startClient) {
			this.handleStartServerRequest();
		}
	}
	
	public void handleStartServerRequest() {
		Log.info("Starting the server ...", debug);
		if (this.runGui != null) {
			Log.info("[*] Closing the run gui", debug);
			this.runGui.dispose();
		}
		new NeptuneServerLauncher();
		
	}
	
	public void handleStartClientRequest() {
		Log.info("Starting the client ...", debug);
		if (this.runGui != null) {
			Log.info("[*] Closing the run gui", debug);
			this.runGui.dispose();
		}
		new NeptuneClient();
	}

	private void setLookAndFeel() {
		// get the os
		String os = System.getProperty("os.name");
		
		try {
			if(os.toLowerCase().contains("mac")) {
				Log.info("Using a Mac, trying to set the aqua LaF and etc's", debug);
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} else if(os.toLowerCase().contains("win")) {
				Log.info("Using a PC, trying to set Windows LaF", debug);
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} else if(os.toLowerCase().contains("linux")) {
				Log.info("Using Linux, trying to set the GTK LaF", debug);
				UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
			} else {
				UIManager.setLookAndFeel("javax.swing.plaf.metal");
			}
		} catch (Exception e) {
			Log.error("Cannot set platform specific LaF, using default: " + e.toString(), debug);
			try {
				UIManager.setLookAndFeel("javax.swing.plaf.metal");
			} catch (Exception e2) {
				// don't look at exceptions at this point
			}
		}
	}
	
	
	public static void help(String error) {
		System.out.println();
		if (error != null) {
			System.out.println("ERROR:\t" + error);
		}
		System.out.println("Neptune " + NeptuneProtocol.VERSION_STRING);
		System.out.println();
		System.out.println("Commandline options:");
		System.out.println("\t" + DEBUG + "\t start the debug mode/verbose mode");
		System.out.println();
		System.out.println("\t" + START_CLIENT + "\t start the NeptuneClient\r\nor\t"
				+ START_SERVER + "\t start the NeptuneServer");
	}
	
	
	public static void main(String[] args) {
			
		if (args.length < 1) {
			new Run(false, false, false);
		} else {
			int unrecognized = 0;
			boolean debug = false;
			boolean startS = false;
			boolean startC = false;
			
			
			for (int i = 0; i < args.length; i++) {
				if (args[i].equals(DEBUG)) {
					debug = true;
				} else if (args[i].equals(START_SERVER)) {
					startS = true;
				} else if (args[i].equals(START_CLIENT)) {
					startC = true;
				} else {
					unrecognized++;
				}
			}
			
			if (unrecognized > 0) {
				Run.help(null);
				return;
			}
		
			if (startC && startS) {
				Run.help("Cannot start server and client in the same process.");
				return;
			}
			
			new Run(debug, startS, startC);
			
		}
	}
	
}
