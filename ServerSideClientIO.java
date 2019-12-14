package main;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.Socket;
import java.net.UnknownHostException;

import data.ClypeData;
import data.MessageClypeData;

public class ServerSideClientIO implements Runnable {
	
	private boolean closeConnection;
	private ClypeData dataToReceiveFromClient, dataToSendToClient;
	private ObjectInputStream inFromClient;
	private ObjectOutputStream outToClient;
	private ClypeServer server;
	private Socket clientSocket;
	
	public ServerSideClientIO(ClypeServer server, Socket clientSocket) {
		this.server = server;
		this.clientSocket = clientSocket;
		closeConnection = false;
		this.inFromClient = null;
		this.outToClient = null;
		this.dataToReceiveFromClient = null;
		this.dataToSendToClient = null;
	}
	
	/**
	 * Receives data
	 */
	public void receiveData() {
		try {
			dataToReceiveFromClient = (ClypeData) inFromClient.readObject();
			
			if(dataToReceiveFromClient.getType() == ClypeData.ALLUSERS) {
				dataToReceiveFromClient = new MessageClypeData(dataToReceiveFromClient.getUserName(), this.server.getAllUsers(), ClypeData.ALLUSERS);
			}
			
			if(dataToReceiveFromClient.getType() == ClypeData.LOGOUT) {
				this.closeConnection = true;
				this.server.remove(this);
			}
		} catch (ClassNotFoundException | IOException e) {
			// TODO Auto-generated catch block
			System.err.println(e.getMessage());
			closeConnection = true;
		}
	}
	
	/**
	 * Sends data
	 */
	public void sendData() {
		try {
			outToClient.writeObject(dataToSendToClient);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.err.println("ERROR: " + e.getMessage());
			closeConnection = true;
		}
	}
	
	/**
	 * @param dataToSendToClient data to send to client
	 */
	public void setDataToSendToClient(ClypeData dataToSendToClient) {
		this.dataToSendToClient = dataToSendToClient;
	}
	
	public String getUsername() {
		return this.dataToReceiveFromClient.getUserName();
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
		try {
			
			outToClient = new ObjectOutputStream(clientSocket.getOutputStream());
			inFromClient = new ObjectInputStream(clientSocket.getInputStream());
			
			while(!closeConnection) {
				receiveData();
				dataToSendToClient = dataToReceiveFromClient;
				this.server.broadcast(dataToSendToClient);
			}
			
			inFromClient.close();
			outToClient.close();
			clientSocket.close();
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

}
