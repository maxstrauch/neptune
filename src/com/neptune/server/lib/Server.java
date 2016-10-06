/**
 * (c) 2009 by Maximilian Strauch.
 * 
 * This program is distributed WITHOUT ANY WARRANTY; without even the implied 
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package com.neptune.server.lib;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

import com.neptune.server.usermanager.User;

/**
 * A TCP/IP-Server.
 * @author maximilianstrauch
 * @version 1.0.0-140609
 */
public abstract class Server {

	/**
	 * The server-socket.
	 */
	private ServerSocket serverSocket;
	
	/**
	 * The list of active connections.
	 */
	private Vector<ServerClientConnection> verbindungen;
	
	/**
	 * The server's thread (waiting for incomming messages).
	 */
	private ServerThread serverThread;
	
	/**
	 * The server's port.
	 */
	private int port;
	
	/**
	 * Is true if the server could not be started.
	 */
	private boolean failure;

	/**
	 * Creates a TCP/IP-Server.
	 * @param serverPort Port where the server listens
	 */
	public Server(int serverPort) {
		this.port = serverPort;
		this.failure = false;
		this.verbindungen = new Vector<ServerClientConnection>();
		try {
			this.serverSocket = new ServerSocket(this.port);
			this.serverThread = new ServerThread(this);
			this.serverThread.start();
		} catch (Exception e) {
			this.failure = true;
		}
	}

	/**
	 * Sends a transmission to a client.
	 * @param ipAdress Client's IP
	 * @param port Client's port
	 * @param transmission Transmission string
	 * @return Returns true on success otherwise false
	 */
	public boolean send(String ipAdress, int port, String transmission) {
		ServerClientConnection scc = this.getConnection(ipAdress, port);
		if (scc != null) {
			scc.send(transmission);
			return true;
		}
		return false;
	}
	
	/**
	 * Sends a transmission to all connected clients.
	 * @param transmission Transmission string
	 */
	public void announce(String transmission) {
		for (int i = 0; i < this.verbindungen.size(); i++) {
			this.send(this.verbindungen.elementAt(i).getIpAdress(), 
					this.verbindungen.elementAt(i).getPort(), transmission);
		}
		return;
	}
	
	public void announceWithExceptionOf(String ip, int port, String transmission) {
		for (int i = 0; i < this.verbindungen.size(); i++) {
			String ipTemp = this.verbindungen.elementAt(i).getIpAdress();
			int portTemp = this.verbindungen.elementAt(i).getPort();
			
			if (ipTemp.equals(ip) && port == portTemp) {
				// send nothing to this client ...
			} else {
				this.send(ipTemp, portTemp, transmission);
			}
		}
		return;
	}
	
	public void announceWithExceptionOf(User[] usr, String transmission) {
		if (usr == null) {
			this.announce(transmission);
			return;
		}
		
		for (int i = 0; i < this.verbindungen.size(); i++) {
			String ipTemp = this.verbindungen.elementAt(i).getIpAdress();
			int portTemp = this.verbindungen.elementAt(i).getPort();
			
			if (!this.isIn(usr, ipTemp, portTemp)) {
				this.send(ipTemp, portTemp, transmission);
			}
		}
		return;
	}
	
	private boolean isIn(User[] usr, String ip, int port) {
		for (int i = 0; i < usr.length; i++) {
			if (usr[i].getIp().equals(ip) && usr[i].getPort() == port) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns the identification string of this class/object.
	 * @return Identification string
	 */
	public String toString() {
		return "TCP/IP-Server@Port: " + this.port + (this.getIpAdress() != null ?
				" on " + this.getIpAdress() : "");
	}
	
	/**
	 * Returns the Server's IP
	 * @return Server's IP
	 */
	public String getIpAdress() {
		try {
			return InetAddress.getLocalHost().getHostAddress();
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * Checks weather the server is started correctly.
	 * @return Returns true on failure otherwise false
	 */
	public boolean failure() {
		return this.failure;
	}
	
	/**
	 * Closes a connection to a client.
	 * @param ipAdress Client's IP
	 * @param port Client's Port
	 * @return Returns true on success otherwise false
	 */
	public boolean closeConnection(String ipAdress, int port) {
		ServerClientConnection scc = this.getConnection(ipAdress, port);
		if (scc != null) {
			this.removeConnection(scc);
			boolean result = scc.close();
			this.closed(ipAdress, port);
			return result;
		}
		return true;
	}
	
	/**
	 * Closes the server.
	 * @return Returns true on success otherwise false
	 */
	public boolean close() {
		try {
			this.serverSocket.close();
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	/**
	 * Adds a connection to the list.
	 * @param scc A ServerClientConnection
	 */
	private void addConnection(ServerClientConnection scc) {
		this.verbindungen.add(scc);
		this.newcome(scc.getIpAdress(), scc.getPort());
		return;
	}
	
	/**
	 * Returns a server-client-connection.
	 * @param ipAdress Client's IP
	 * @param port Client's Port
	 * @return Returns ServerClientConnection-Object on success otherwise null
	 */
	private ServerClientConnection getConnection(String ipAdress, int port) {
		for (int i = 0; i < this.verbindungen.size(); i++) {
			if (this.verbindungen.elementAt(i).getIpAdress().equals(ipAdress) &&
				this.verbindungen.elementAt(i).getPort() == port) {
				return this.verbindungen.elementAt(i);
			}
		}
		return null;
	}
	
	/**
	 * Removes a server-client-connection from the list.
	 * @param scc A ServerClientConnection
	 */
	private void removeConnection(ServerClientConnection scc) {
		this.verbindungen.remove(scc);
		return;
	}

	/**
	 * Returns the vector with the connected clients
	 * @return Vector with ServerClientConnection objects
	 */
	public Vector<ServerClientConnection> getConnectedClients() {
		return this.verbindungen;
	}
	
	/**
	 * Abstract method (the child class needs to implement it).
	 * Is called if a new clients connects.
	 * @param ipAdresse Client's IP
	 * @param port Client's port
	 */
	public abstract void newcome(String ipAdresse, int port);
	
	/**
	 * Abstract method (the child class needs to implement it).
	 * Is called if a client closes the connection.
	 * @param ipAdresse Client's IP
	 * @param port Client's port
	 */
	public abstract void closed(String ipAdresse, int port);
	
	/**
	 * Abstract method (the child class needs to implement it).
	 * Is called if a new transmission comes from a client.
	 * @param ipAdresse Client's IP
	 * @param port Client's port
	 * @param transmission Transmissions string
	 */
	public abstract void income(String ipAdresse, int port, String transmission);
	
	/**
	 * Receives client transmissions in a thread.
	 * @author maximilianstrauch
	 * @version 1.0.0-070609
	 */
	private class ServerThread extends Thread {

		/**
		 * The server.
		 */
		private Server server;
		
		/**
		 * Creates a transmission receiver.
		 * @param server Server's object
		 */
		public ServerThread(Server server) {
			this.server = server;
		}
		
		/**
		 * Is started if this class is started as thread.
		 */
		public void run() {
			while (true) {
				try {
					Socket client = this.server.serverSocket.accept();
					ServerClientConnection scc = new ServerClientConnection(
							client, this.server
					);
					this.server.addConnection(scc);
					scc.start();
				} catch (Exception e) {
					// do nothing ...
				}
			}
		}
	}
	
	/**
	 * Implements the connection between server and client.
	 * @author maximilianstrauch
	 * @version 1.0.0-140609
	 */
	public class ServerClientConnection extends Thread {

		/**
		 * The server.
		 */
		private Server server;
		
		/**
		 * The client's socket.
		 */
		private Socket clientSocket;
		
		/**
		 * The input stuff.
		 */
		private BufferedReader input;
		
		/**
		 * The output stuff.
		 */
		private PrintStream output;
		
		/**
		 * Is true if the client is on-line.
		 */
		private boolean connectionOnline;

		/**
		 * The client's IP.
		 */
		private String ipAdress;
		
		/**
		 * The client's port.
		 */
		private int port;
		
		/**
		 * Creates a new connection between server and client.
		 * @param client Client's socket
		 * @param server Server's object
		 * @throws IOException BufferedReader/PrintStream encounters an error
		 */
		public ServerClientConnection(Socket client, Server server) throws IOException {
			this.server = server;
			this.clientSocket = client;
	    	this.connectionOnline = true;
	        this.input = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
	        this.output = new PrintStream(this.clientSocket.getOutputStream(), true);
	        this.ipAdress = this.clientSocket.getInetAddress().getHostAddress();
	        this.port = this.clientSocket.getPort();
		}
		
		/**
		 * Is started if this class is started as thread.
		 */
		public void run() {
			String transmission = null;
			while (this.connectionOnline) {
				transmission = this.receive();
				if (transmission == null) {
					if (this.connectionOnline) {
						this.server.closeConnection(this.getIpAdress(), this.getPort());
					}
				} else {
					this.server.income(this.getIpAdress(), this.getPort(), transmission);
				}
			}
		}
		
		/**
		 * Returns the identification string of this class/object.
		 * @return Identification string
		 */
		public String toString() {
			return "ServerClientConnection@Ip: " + this.getIpAdress() + " on port " + 
						this.getPort();
		}

		/**
		 * Sends a transmission to this client.
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
	     * Receives a transmission from this client.
	     * @return Transmission
	     */
	    public String receive() {
	        String message = null;
	        try {
	        	message = input.readLine();
	        } catch (Exception e) {
	        	message = null;
	        }       
	        return message;
	    }
	    
	    /**
	     * Closes the connection to this client.
	     * @return Returns true on success otherwise false
	     */
	    public boolean close() {
	    	this.connectionOnline = false;
	        try {
	            this.clientSocket.close(); 
	            this.clientSocket = null;
	        	this.input.close(); 
	        	this.input = null;
	        	this.output.close(); 
	            this.output = null;
	            return true;
	        } catch (Exception e) {
	        	return false;
	        }
	    }
		
	    /**
	     * Returns the IP of this client.
	     * @return Client's IP
	     */
		public String getIpAdress() {
			return this.ipAdress;	
		}
		
		/**
		 * Returns the port of this client
		 * @return Client's port
		 */
		public int getPort() {
			return this.port;
		}
		
	}
	
}
