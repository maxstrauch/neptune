/**
 * (c) 2009 by Maximilian Strauch.
 * 
 * This program is distributed WITHOUT ANY WARRANTY; without even the implied 
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package com.neptune.server;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.neptune.server.gui.NeptuneServerDD;
import com.neptune.server.lib.Log;

public class NeptuneServerLauncher {

	private boolean debug;
	private NeptuneServerDD nsdd;
	private NeptuneServer ns;

	public NeptuneServerLauncher() {
		this.debug = Boolean.parseBoolean(System.getProperty("neptune.debug"));
		
		// init gui
		this.initPreGUI();
	}
	
	private void initPreGUI() {
		Log.info("Starting the data dialog for the server ...", debug);
		
		// starting dialog
		final NeptuneServerLauncher nsl = this;
		try {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					nsdd = new NeptuneServerDD(nsl);
					nsdd.setVisible(true);
				}
			});
		} catch (Exception e) {
			Log.fatal("Cannot start pre GUI: " + e.toString(), debug);
			JOptionPane.showMessageDialog(null, "Der Server-Start-Dialog konnte nicht " +
					"gestartet werden", "Entschuldigung", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
	}
	
	public void handleStartServerRequest(final int port, final boolean askOption) {
		this.ns = new NeptuneServer(port, askOption);
		
		// check errors
		if (!this.ns.status()) {
			this.ns.close();
			Log.info("Cannot start server on port " + port, debug);
			this.nsdd.setStateOfComponents(true);
			return;
		}
		this.nsdd.dispose();
		
		// init server gui
		this.ns.initServerGUI();
		return;
	}
	
}
