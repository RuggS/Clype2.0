package main;

import java.io.*;
import java.net.*;
import java.util.Scanner;

import data.*;

/**
 * 
 * @author Spencer
 *
 */
public class ClypeClient {
	
	public static final int DEFAULT_PORT = 7000;
	String userName;
	String hostName;
	int port;
	static final String key = "TIME";
	boolean closeConnection = false;
	ClypeData dataToSendToServer;
	ClypeData dataToRecieveFromServer;
	Scanner inFromStd;
	ObjectInputStream inFromServer;
	ObjectOutputStream outToServer;
	
	/**
	 * Creates client using given parameters
	 * @param usernname
	 * @param hostnname
	 * @param port
	 */
	public ClypeClient(String usern, String hostn, int p) {
		if(usern == null) {
			throw new IllegalArgumentException("Null Username");
		}else if(hostn == null) {
			throw new IllegalArgumentException("Null host name");
		}if(p < 1024) {
			throw new IllegalArgumentException("Invalid Port");
		}
		userName = usern;
		hostName = hostn;
		port = p;
		dataToSendToServer = null;
		dataToRecieveFromServer = null;
		inFromServer = null;
		outToServer = null;
	}
	
	/**
	 * Creates client using default port of 7000
	 * @param username
	 * @param hostname
	 */
	public ClypeClient(String usern, String hostn) {
		this(usern, hostn, DEFAULT_PORT);
	}
	
	/**
	 * Creates client using default port of 7000 and default hostname of localhost
	 * @param username
	 */
	public ClypeClient(String usern) {
		this(usern, "localhost", DEFAULT_PORT);
	}
	
	/**
	 * Will start session 
	 */
	public void start() {
		try {
			Socket skt = new Socket(hostName, port);
			outToServer = new ObjectOutputStream(skt.getOutputStream());
			inFromServer = new ObjectInputStream(skt.getInputStream());

			ClientSideServerListener listener = new ClientSideServerListener(this);
			Thread t = new Thread(listener);
			t.start();
			
			while(!closeConnection) {
				//inFromStd = new Scanner(System.in);
				//readClientData();
				if(!closeConnection) {
					sendData();
				}
//				if(!closeConnection) {
//					receiveData();
//				}
//				if(!closeConnection) {
//					printData();
//				}
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
	
	/**
	 * 
	 */
	public void readClientData(String in) {
		String input = in;
		if (input.equals("DONE")) {
			closeConnection = true;
		}else if (in.substring(0, 7).equals("SENDFILE")) {
			closeConnection = false;
			input = in.substring(7);
			dataToSendToServer = new FileClypeData(userName, input, 2);
			try {
				((FileClypeData) dataToSendToServer).readFileContents();
			} catch (IOException ioe) {
				System.err.println("IO exception");
				dataToSendToServer = null;
			}
		}else if (input.equals("LISTUSERS")) {
//			closeConnection = true;
			//to do
			// doing
			dataToSendToServer = new MessageClypeData(userName, "", 0);
		}else {
			closeConnection = false;
			dataToSendToServer = new MessageClypeData(userName, input, 3);
		}
	}
	
	/**
	 * Will be used to send data to server
	 */
	public void sendData() {
		try {
			outToServer.writeObject(dataToSendToServer);
		}catch (IOException ioe) {
			System.err.println("IO exception: " + ioe.getMessage());
			closeConnection = true;
		}
	}
	
	/**
	 * Will be used to receive data from server
	 */
	public void receiveData() {
		try {
			dataToRecieveFromServer = (ClypeData) inFromServer.readObject();
		} catch (ClassNotFoundException cnfe) {
			System.err.println("Class not found exception: " + cnfe.getMessage());
			closeConnection = true;
		} catch (IOException ioe) {
			System.err.println("IO exception: " + ioe.getMessage());
			closeConnection = true;
		}
	}
	
	/**
	 * 
	 */
	public void printData() {
		if(dataToRecieveFromServer != null) {
			System.out.println(dataToRecieveFromServer.getUserName() + ": " + dataToRecieveFromServer.getData());
		}
	}
	
	/**
	 * 
	 * @return username
	 */
	public String getUserName() {
		return userName;
	}
	
	/**
	 * 
	 * @return hostname
	 */
	public String getHostName() {
		return hostName;
	}
	
	/**
	 * 
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
	 * compares username, hostname, and port
	 */
	@Override
	public boolean equals(Object o) {
		if(o == null) {
			return false;
		}
		
		if(!(o instanceof ClypeClient)) {
			return false;
		}
	
		ClypeClient c = (ClypeClient) o;
		return this.userName == c.userName && this.hostName == c.hostName && this.port == c.port;
	}
	
	@Override
	public String toString() {
		return "This is a Clype Client with user name " + userName + ", host name, " + hostName + ", and port" + port;
	}
	
	public static void main(String[] args) {
		String name = "";
		String hname = "";
		String p;
		int port = 0;
		ClypeClient cli;
		if (args.length == 0) {
			cli = new ClypeClient("Anonymous");
		}else {
			name = args[0].substring(0, args[0].length());
			for (int i = 0; i < args[0].length(); i++) {
				if(args[0].charAt(i) == '@') {
					name = args[0].substring(0, i);
					hname = args[0].substring(name.length() + 1, args[0].length());
				}else if(args[0].charAt(i) == ':') {
					hname = args[0].substring(name.length() + 1, i);
					p = args[0].substring(name.length() + hname.length() + 2, args[0].length());
					port = Integer.parseInt(p);
				}
			}
			
		}

		if(hname == "") {
			cli = new ClypeClient(name);
		}else if(port == 0) {
			cli = new ClypeClient(name, hname);
		}else {
			cli = new ClypeClient(name, hname, port);
		}
//		System.out.println(cli.toString());
		cli.start();
		
		
	}
	
}
