package main;

import java.io.*;
import java.net.*;

import data.*;

public class ServerSideClientIO implements Runnable{

	boolean closeConnection = false;
	ClypeData dataToRecieveFromClient;
	ClypeData dataToSendToClient;
	ObjectInputStream inFromClient;
	ObjectOutputStream outToClient;
	ClypeServer serv;
	Socket clientSocket;
	
	public  ServerSideClientIO(ClypeServer server, Socket clientSocket) {
		serv = server;
		this.clientSocket = clientSocket;
		dataToRecieveFromClient = null;
		dataToSendToClient = null;
		inFromClient = null;
		outToClient = null;
	}
	
	public void receiveData() {
		try {
			dataToRecieveFromClient = (ClypeData) inFromClient.readObject();
			if(dataToRecieveFromClient.GetType() == 1) {
				closeConnection = true;
			}else if (dataToRecieveFromClient.GetType() == 0) {
				dataToSendToClient = new MessageClypeData("Server", serv.getListUser(), 0);
			}else {
				dataToSendToClient = dataToRecieveFromClient;
			}
			
			System.out.println("in server receive: " + dataToRecieveFromClient.getData());
		} catch (ClassNotFoundException cnfe) {
			System.err.println("Class not found exception: " + cnfe.getMessage());
			closeConnection = true;
		} catch (IOException ioe) {
			System.err.println("IO exception: " + ioe.getMessage());
			closeConnection = true;
		}
	}
	
	public void sendData() {
		try {
			outToClient.writeObject(dataToSendToClient);
		}catch (IOException ioe) {
			System.err.println("IO exception: " + ioe.getMessage());
			closeConnection = true;
		}
	}
	
	public void setDataToSendToClient (ClypeData dataToSendToClient) {
		this.dataToSendToClient = dataToSendToClient;
	}
	
	@Override
	public void run() {
		try {
			outToClient = new ObjectOutputStream(clientSocket.getOutputStream());
			inFromClient = new ObjectInputStream(clientSocket.getInputStream());




			while(!closeConnection) {
				receiveData();
				if(!closeConnection) {
					this.serv.broadcast(dataToSendToClient);
				}
			}
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
	
	

}
