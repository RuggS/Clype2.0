package main;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import data.ClypeData;

/**
 * 
 * This class represents a server that sends and recieves data.
 * @author under
 *
 */
public class ClypeServer {

	private int port;
	private boolean closeConnection;
	private ArrayList<ServerSideClientIO> serverSideClientIOList;

	static final int DEFAULT_PORT = 7000;
	
	/**
	 * 
	 * Constructor that initializes the port and other settings.
	 * 
	 * @param port the port the server uses
	 */
	public ClypeServer(int port) throws IllegalArgumentException {
		
		if(port < 1024)
			throw new IllegalArgumentException();
		
		this.port = port;
		closeConnection = false;
		serverSideClientIOList = new ArrayList<ServerSideClientIO>();
	}
	
	/**
	 * Default constructor that defaults the port to 7000.
	 */
	public ClypeServer() {
		this(DEFAULT_PORT);
	}
	
	/**
	 * This method starts something
	 */
	public void start() {
		
		try {
			ServerSocket sskt = new ServerSocket(port);
			
			while(!closeConnection) {
				
				Socket sock = sskt.accept();
				
				ServerSideClientIO sck = new ServerSideClientIO(this, sock);
				
				this.serverSideClientIOList.add(sck);
				
				Thread t = new Thread(sck);
				t.start();
				
			}
			
			//code here
			sskt.close();
			
		} catch (UnknownHostException e) {
			System.err.println("Unknown Host: " + e.getMessage());
		} catch (NoRouteToHostException e) {
			System.err.println("Server unreachable " + e.getMessage());
		} catch (ConnectException e) {
			System.err.println("Connection refused: " + e.getMessage());
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
		
	}
	
	/**
	 * 
	 * Getter method for port.
	 * 
	 * @return the port of the server
	 */
	public int getPort() {
		return port;
	}
	
	/**
	 * 
	 * Sends data to all connected clients
	 * 
	 * @param dataToBroadcastToClients the data to be sent to all the clients
	 */
	public synchronized void broadcast(ClypeData dataToBroadcastToClients) {
		
		for(ServerSideClientIO s : this.serverSideClientIOList) {
			s.setDataToSendToClient(dataToBroadcastToClients);
			s.sendData();
		}
		
	}
	
	/**
	 * 
	 * Removes a client from the connected client list
	 * 
	 * @param s ServerSideClientIO object to be removed
	 */
	public synchronized void remove(ServerSideClientIO s) {
		this.serverSideClientIOList.remove(s);
	}
	
	/**
	 * @return a string of all the connected users
	 */
	public String getAllUsers() {
		String r = "\n";
		for(ServerSideClientIO s : this.serverSideClientIOList)
			r += s.getUsername() + "\n";
		
		return r;
	}
	
	/**
	 * Overriden equals(Object obj) method, which makes sure the obj is not null and also makes
	 * sure obj is of type ClypeServer. This method makes checks if the following are equal:
	 * port, and closeConnection.
	 */
	@Override
	public boolean equals(Object obj) {
		if(this == obj)
			return true;
		if(obj == null || !(obj instanceof ClypeServer))
			return false;
		
		ClypeServer c = (ClypeServer) obj;
		if(c.getPort() == this.getPort() && c.closeConnection == this.closeConnection)
			return true;
		else
			return false;
	}
	
	/**
	 * Overriden hashCode() method.
	 */
	@Override
	public int hashCode() {
		//from lecture notes
		int result = 17;
		result = 37*result + this.getPort();
		return result;
	}
	
	/**
	 * Overriden toString() method that returns/prints the following:
	 * port, whether or not the connection is closed, data to send to client, data to recieve from client
	 */
	@Override
	public String toString() {
		
		return "Port: " + this.getPort() + "\n"
				+ "Connection is closed: " + this.closeConnection + "\n";
		
	}
	
	
	
	/**
	 * @param args port_number
	 */
	public static void main(String args[]) {
		ClypeServer server;
		if(args.length == 0)
			server = new ClypeServer();
		else
			server = new ClypeServer(Integer.parseInt(args[0]));
		
		server.start();
	}
	
}