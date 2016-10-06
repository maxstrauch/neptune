/**
 * (c) 2009 by Maximilian Strauch.
 * 
 * This program is distributed WITHOUT ANY WARRANTY; without even the implied 
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package com.neptune.client.lib;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

/**
 * A TCP/IP-Client.
 * @author maximilianstrauch
 * @version 1.0.0-070609
 */
public abstract class Client {

	/**
	 * The connection between the client and server.
	 */
	private ClientServerConnection connection;
	
	/**
	 * The transmission receiver.
	 */
	private ClientThread target;
	
	/**
	 * Is true if the server could not be started.
	 */
	private boolean failure;

	/**
	 * save the calling class/object
	 */
	private Object caller;

	/**
	 * Creates a TCP/IP-Client.
	 * @param ipAdress Server's IP
	 * @param port Server's port
	 */
	public Client(String ipAdress, int port, Object caller) {
		this.caller = caller;
		this.failure = false;
		try {
			this.connection = new ClientServerConnection(ipAdress, port);
			this.target = new ClientThread(this, this.connection);
			this.target.start();
		} catch (Exception e) {
			this.failure = true;
		}
	}
	
	public Object getCaller() {
		return this.caller;
	}
	
	/**
	 * Sends a transmission to the connected server.
	 * @param transmission Transmission string
	 * @return Returns true on success otherwise false
	 */
	public boolean send(String transmission) {
		if (this.failure) {
			return false;
		}
		return this.connection.send(transmission);
	}

	/**
	 * Returns the identification string of this class/object.
	 * @return Identification string
	 */
	public String toString() {
		return "TCP/IP-Client (TargetIp: " + this.connection.getIpAdress() + " on " + 
								this.connection.getPort() + ")";
	}
	
	/**
	 * Closes the connection to the server.
	 * @return Returns true on success otherwise false
	 */
	public boolean close() {
		if (this.target != null) {
			this.target.close();
			this.target = null;
		}
		return this.connection.close();
	}
	
	/**
	 * Checks weather the server is started correctly.
	 * @return Returns true on failure otherwise false
	 */
	public boolean failure() {
		return this.failure;
	}
	
	/**
	 * Abstract method (the child class needs to implement it).
	 * Is called if a new transmission comes from the server.
	 * @param transmission Returns the transmission.
	 */
	public abstract void income(String transmission);
	

	/**
	 * Implements the connection between client and server.
	 * @author maximilianstrauch
	 * @version 1.0.0-070609
	 */
	private class ClientServerConnection extends Thread {

		/**
		 * The client-socket.
		 */
		private Socket socket;
		
		/**
		 * The message-input.
		 */
		private BufferedReader input;
		
		/**
		 * The message-output.
		 */
		private PrintStream output;
		
		/**
		 * The server's port.
		 */
		private int port;
		
		/**
		 * The server's IP
		 */
		private String ipAdress;
		
		/**
		 * Creates a new connection to the server.
		 * @param ipAdress Server's IP
		 * @param port Server's port
		 * @throws IOException Socket/BufferedReader/PrintStream encounters an error
		 */
		public ClientServerConnection(String ipAdress, int port) throws IOException {
			this.ipAdress = ipAdress;
			this.port = port;
			this.socket = new Socket(this.ipAdress, this.port);
	        this.input = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
	        this.output = new PrintStream(this.socket.getOutputStream(), true);
		}

		/**
		 * Returns the identification string of this class/object.
		 * @return Identification string
		 */
		public String toString() {
			return "ClientServerConnection@TargetIp: " + this.getIpAdress() + " on port " + 
						this.getPort();
		}

		/**
		 * Sends a transmission to the server.
		 * @param transmission Transmission string
		 * @return Returns true on success otherwise false
		 */
	    public boolean send(String transmission) {
	        try {
	        	this.output.println(transmission);
	        	return true;
	        } catch (Exception e) {
	        	return false;
	        }                        
	    }

	    /**
	     * Receives an String from the server.
	     * @return Returns a string or null
	     */
	    public String receive() {
	        String message = null;
	        try {
	        	message = this.input.readLine();
	        } catch (Exception e) {
	        	message = null;
	        }       
	        return message;
	    }
	    
	    /**
	     * Closes the client-server-connection.
	     * @return Returns true on success otherwise false
	     */
	    public boolean close() {
	        try {
	        	this.input.close(); 
	        	this.input = null;
	        	this.output.close(); 
	        	this.output = null;
	        	this.socket.close(); 
	            this.socket = null;
	            return true;
	        } catch (Exception e) {
	        	return false;
	        }
	    }
			
	    /**
	     * Returns the server's IP.
	     * @return Server's IP
	     */
		public String getIpAdress() {
			return this.ipAdress;	
		}
		
		/**
		 * Returns the port of the server.
		 * @return Server's port
		 */
		public int getPort() {
			return this.port;
		}
		
	}
	
	/**
	 * Receives server transmissions in a thread.
	 * @author maximilianstrauch
	 * @version 1.0.0-070609
	 */
	private class ClientThread extends Thread {
		
		/**
		 * The server connection.
		 */
		private ClientServerConnection connection;
		
		/**
		 * The client-object.
		 */
		private Client client;
		
		/**
		 * Is true if the client is on-line.
		 */
		private boolean online;

		/**
		 * Creates the waiting client with detailed information.
		 * @param client Client
		 * @param csc Connection to the server
		 */
		public ClientThread(Client client, ClientServerConnection csc) {
			this.client = client;
			this.connection = csc;
			this.online = true;
		}
		
		/**
		 * The client ist waiting/getting transmissions.
		 */
		public void run() {
            boolean sending = true;
            String transmission = "";
            while (this.online && sending) {
            	if (this.online) {
            		transmission = this.connection.receive();
            		sending = (transmission != null ? true : false);
            		if (sending) {
            			this.client.income(transmission);
            		}
            	}
            }
            return;
		}
		
		/**
		 * Closes this class/object.
		 */
		public void close() {
			this.online = false;
			return;
		}
		
	}
	
}
