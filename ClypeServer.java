package main;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Scanner;

import data.*;

/**
 * 
 * @author Spencer
 *
 */
public class ClypeServer {
	
	public static final int DEFAULT_PORT = 7000;
	int port;
	boolean closeConnection = false;
	ArrayList<ServerSideClientIO> serverSideClientIOList;
	
	/**
	 * Constructor that takes an argument of port
	 * @param port
	 */
	public ClypeServer(int p) {
		if(p < 1024) {
			throw new IllegalArgumentException("Invalid port");
		}else {
			port = p;
			serverSideClientIOList = new ArrayList<ServerSideClientIO>();
		}
	}
	
	/**
	 * constructor that uses default port of 7000
	 */
	public ClypeServer() {
		this(DEFAULT_PORT);
	}
	
	/**
	 * Will start the session
	 */
	public void start() {
		try {
			ServerSocket skt = new ServerSocket( port);
			while(!closeConnection) {
				Socket clientSkt = skt.accept();
				ServerSideClientIO t = new ServerSideClientIO(this, clientSkt);
				serverSideClientIOList.add(t);
				Thread thread = new Thread(t);
				thread.start();
				if(closeConnection)
					clientSkt.close();
			}
			
			skt.close();


		} catch ( UnknownHostException uhe ) {
			System.err.println( "Host (server) is not known: " + uhe.getMessage() );
			closeConnection = true;
		} catch ( NoRouteToHostException nrthe ) {
			System.err.println( "A route cannot be established to the server: " + nrthe.getMessage() );
			closeConnection = true;
		} catch ( ConnectException ce ) {
			System.err.println( "Issues with connecting: " + ce.getMessage() );
			closeConnection = true;
		} catch ( IOException ioe ) {
			System.err.println( "Issues with IO: " + ioe.getMessage() );
			closeConnection = true;
		}
	}
	
	
	public synchronized void broadcast(ClypeData dataToBroadcastToClients) {
		for(ServerSideClientIO t : serverSideClientIOList) {
			t.setDataToSendToClient(dataToBroadcastToClients);
			t.sendData();
		}
	}
	
	public synchronized void remove(ServerSideClientIO serverSideClientToRemove) {
		serverSideClientIOList.remove(serverSideClientToRemove);
	}
	
	/**
	 * Used to view port value
	 * @return port
	 */
	public int getPort() {
		return port;
	}
	
	
	@Override
	public int hashCode() {
		return 0;
	}
	
	/**
	 * checks for equal ports
	 */
	@Override
	public boolean equals(Object o) {
		if(o == null) {
			return false;
		}
		
		if(!(o instanceof ClypeServer)) {
			return false;
		}
	
		ClypeServer s = (ClypeServer) o;
		return this.port == s.port;
	}
	
	@Override
	public String toString() {
		return "This is a Clype Server with port" + port;
	}
	
	public static void main(String[] args) {
		ClypeServer serve;
		if (args.length == 0) {
			serve = new ClypeServer();
		}else {
			serve = new ClypeServer(Integer.parseInt(args[0]));
		}
		
		serve.start();
//		System.out.println(serve.toString());
					
	}

	public String getListUser() {
		String list = "";
		for(ServerSideClientIO t : serverSideClientIOList) {
			list += t.dataToRecieveFromClient.getUserName() + '\n';
		}
		
		return list;
	}
	
}
