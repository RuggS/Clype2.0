package main;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.util.Scanner;

import javax.imageio.ImageIO;

import application.SendHandler;
import data.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * 
 * @author Spencer
 *
 */
public class ClypeClient extends Application {
	
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
	private TextField usrMsg = new TextField();
	private VBox messageHist = new VBox();
	private static boolean send = false;
	private static boolean sendMedia = false;
	private String msg = "";
	private String mediaMsg = "";
	private static String toGui = null;
	
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
	
	public ClypeClient() {
		this("temp", "localhost", DEFAULT_PORT);
	}
	
	/**
	 * Will start session 
	 */
	public synchronized void start() {
		try {
			Socket skt = new Socket(hostName, port);
			outToServer = new ObjectOutputStream(skt.getOutputStream());
			inFromServer = new ObjectInputStream(skt.getInputStream());

			ClientSideServerListener listener = new ClientSideServerListener(this);
			Thread t = new Thread(listener);
			t.start();
			
			while(!closeConnection) {
				try {
					wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(!closeConnection && this.send) {
					readClientData();
					this.send = false;
					sendData();
				} else if(!closeConnection && this.sendMedia) {
					readClientData();
					this.sendMedia = false;
					sendData();
				}
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
	public synchronized void readClientData() {
		String input = msg;
		if (input.equals("DONE")) {
			closeConnection = true;
		}else if (sendMedia) {
			input = mediaMsg;
			closeConnection = false;
			if(input.endsWith("txt") || input.endsWith("TXT")) {
				this.sendFile(input);
			} else if(input.endsWith("mp3") || input.endsWith("MP3")) {
				this.sendAudio(input);
			} else if(input.endsWith("jpg") || input.endsWith("png") || input.endsWith("JPG") || input.endsWith("PNG")) {
				this.sendImage(input);
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
	
	public void sendAudio(String path) {
		String filePath = path.substring(5);
		dataToSendToServer = new AudioClypeData(filePath, userName, 2);
		
	}
	
	public void sendImage(String path) {
		String filePath = path.substring(5);
		dataToSendToServer = new PictureClypeData(filePath, userName, 2);
	}
	
	public void sendFile(String path) {
		String filePath = path.substring(5);
		dataToSendToServer = new FileClypeData(userName, filePath, 2);
		try {
			((FileClypeData) dataToSendToServer).readFileContents();
		} catch (IOException ioe) {
			System.err.println("IO exception");
			dataToSendToServer = null;
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
	
	public synchronized void setSend(boolean s) {
		send = s;
		notify();
	}
	
	public synchronized void setMediaSend(boolean s) {
		sendMedia = s;
		notify();
	}
	
	public synchronized void setmsg(String s) {
		msg = s;
	}
	
	/**
	 * Will be used to receive data from server
	 */
	public void receiveData() {
		try {
			dataToRecieveFromServer = (ClypeData) inFromServer.readObject();
			if(dataToRecieveFromServer != null) {
				toGui = dataToRecieveFromServer.getUserName() + ": " + dataToRecieveFromServer.getData();
			}
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
	
	public void printGui() {
		if(dataToRecieveFromServer != null) {
			messageHist.getChildren().add(new Label(dataToRecieveFromServer.getUserName() + ": " + dataToRecieveFromServer.getData()));
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
	
	@Override
	public void start(Stage primaryStage) {
		try {
			ClypeClient client = new ClypeClient("GuiTest");
			
	        
	        Thread t = new Thread( new Runnable() {
        		@Override
        		public void run() {
        			client.start();
        		}
        });	 
	        
	        Thread thread = new Thread(new Runnable() {

	            @Override
	            public void run() {
	                Runnable updater = new Runnable() {

	                    @Override
	                    public void run() {
	                    	if(toGui != null) {
	                    		messageHist.getChildren().add(new Label(toGui));
	                    		toGui = null;
	                    	}
	                    }
	                };

	                while (true) {
	                    try {
	                        Thread.sleep(1000);
	                    } catch (InterruptedException ex) {
	                    }

	                    // UI update is run on the Application thread
	                    Platform.runLater(updater);
	                }
	            }

	        });
	        
//			service.start();
			FlowPane root = new FlowPane();
			FileChooser fileChooser = new FileChooser();
			fileChooser.getExtensionFilters().addAll(
				     new FileChooser.ExtensionFilter("Text Files", "*.txt"),
				     new FileChooser.ExtensionFilter("Images", "*.jpg","*.png"),
				     new FileChooser.ExtensionFilter("Audio Files", "*.mp3")
				);
			
			Button loadButton = new Button("Send Media");
			Button sendButton = new Button("Send Message");
			sendButton.setOnAction( new EventHandler<ActionEvent>() {			
				public void handle( ActionEvent e ) {
					client.msg = usrMsg.getText();
					client.setSend(true);
				}
			});	
			
			loadButton.setOnAction(e -> {
				File selectedFile = fileChooser.showOpenDialog(primaryStage);
				client.mediaMsg = selectedFile.toURI().toString();
				client.setMediaSend(true);
			});
			
		
			
			//root.getChildren().add()


			root.getChildren().add(usrMsg);
			root.getChildren().add(sendButton);
			root.getChildren().add(loadButton);
			root.getChildren().add(messageHist);
//			messageHist.getChildren().add(new Label("test"));
			
			
			Scene scene = new Scene(root,400,400);

			//scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.show();
			
			t.start();
			thread.start();

		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public static void main(String[] args) {
		launch(args);
	}
}