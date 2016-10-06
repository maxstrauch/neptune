/**
 * (c) 2009 by Maximilian Strauch.
 * 
 * This program is distributed WITHOUT ANY WARRANTY; without even the implied 
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package com.neptune.client;

import com.neptune.client.lib.Client;

public class NeptuneClientConnection extends Client {

	private NeptuneClient nc;
//	private boolean forwardingEnabled = true;
	
	public NeptuneClientConnection(String ipAdress, int port, NeptuneClient nc) {
		super(ipAdress, port, nc);
		this.nc = nc;
	}
	
	public void income(String transmission) {
		
//		if (this.forwardingEnabled) {
		if (this.nc == null) {
//			System.out.println(">>> " + nc);
			this.nc = (NeptuneClient) this.getCaller();
//			System.out.println(">>> " + nc);
		}
		
//		System.err.println(transmission);
		
		nc.handleIncommingTransmission(transmission);
		
//		} else {
//			System.err.println("nof");
//		}
	}

//	public void setTransmissionForwardingEnabled(boolean enabled) {
//		this.forwardingEnabled = enabled;
//	}
	
}
