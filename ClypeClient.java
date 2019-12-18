package main;

import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;
import data.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
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
	static ClypeData dataToRecieveFromServer;
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
	private static String userList = "";
	static ClypeData dataToGui;
	
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
			
			dataToSendToServer = new MessageClypeData(userName, "", ClypeData.GET_USERS);
			sendData();
			
			while(!closeConnection) {
				try {
					wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(!closeConnection && this.send) {
					System.out.println("test msg: " + msg);
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
			dataToSendToServer = new MessageClypeData(userName, "", 1);
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
	
	public void sendAudio(String path) {
		dataToSendToServer = new AudioClypeData(path, userName, ClypeData.SEND_AUDIO);

	}

	public void sendImage(String path) {
		dataToSendToServer = new PictureClypeData(path, userName, ClypeData.SEND_PICTURE);
	}

	public void sendFile(String path) {
		dataToSendToServer = new FileClypeData(userName, path, 2);
		try {
			((FileClypeData) dataToSendToServer).readFileContents();
		} catch (IOException ioe) {
			System.err.println("IO exception");
			dataToSendToServer = null;
		}
	}

	public synchronized void setMediaSend(boolean s) {
		sendMedia = s;
		notify();
	}
	
	public synchronized void setSend(boolean s) {
		send = s;
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
				if(dataToRecieveFromServer.getType() == ClypeData.GET_USERS) {
					userList = "Users: " + dataToRecieveFromServer.getData();
				}else if(dataToRecieveFromServer.getType() == ClypeData.SEND_PICTURE) {
					dataToRecieveFromServer.getData();
				}else if(dataToRecieveFromServer.getType() == ClypeData.SEND_AUDIO) {
					//code for audio to gui here
				}else {
					toGui = dataToRecieveFromServer.getUserName() + ": " + dataToRecieveFromServer.getData();
				}
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
	
	@SuppressWarnings("static-access")
	@Override
	public void start(Stage primaryStage) {
		try {
			FlowPane logOnScreen = new FlowPane();
			TextField name = new TextField();
			TextField hname = new TextField();
			TextField port = new TextField();
			Button logOn = new Button("Log On");
			Scene log = new Scene(logOnScreen,400,400);
			
			
			
			name.setPromptText("User Name");
			logOnScreen.getChildren().add(name);
			hname.setPromptText("Host Name");
			logOnScreen.getChildren().add(hname);
			port.setPromptText("Port Number");
			logOnScreen.getChildren().add(port);
			
			
			logOn.setOnAction( new EventHandler<ActionEvent>() {			
				public void handle( ActionEvent e ) {
					GridPane root = new GridPane();
					Button sendButton = new Button("Send Message");
					Button loadButton = new Button("Send Media");
					Label userListLabel = new Label();
					FileChooser fileChooser = new FileChooser();
					fileChooser.getExtensionFilters().addAll(
						     new FileChooser.ExtensionFilter("Text Files", "*.txt"),
						     new FileChooser.ExtensionFilter("Images", "*.jpg","*.png"),
						     new FileChooser.ExtensionFilter("Audio Files", "*.mp3")
						);
					
					root.getChildren().add(usrMsg);
					root.setRowIndex(usrMsg, 0);
					root.setColumnIndex(usrMsg, 0);
					root.getChildren().add(sendButton);
					root.setRowIndex(sendButton, 0);
					root.setColumnIndex(sendButton, 1);
					root.getChildren().add(loadButton);
					root.setRowIndex(loadButton, 0);
					root.setColumnIndex(loadButton, 2);
					root.getChildren().add(messageHist);
					root.setRowIndex(messageHist, 1);
					root.setColumnIndex(messageHist, 0);
					root.setColumnSpan(messageHist, 2);
					root.getChildren().add(userListLabel);
					root.setRowIndex(userListLabel, 1);
					root.setColumnIndex(userListLabel, 2);
					
					Scene scene = new Scene(root,400,400);
					String uName = name.getText();
					String hName = hname.getText();
					String portstr = port.getText();
					ClypeClient client;
					if(uName.equals("")) {
						client = new ClypeClient();
					}else if(hName.equals("")) {
						client = new ClypeClient(uName);
					}else if(portstr.equals("")) {
						client = new ClypeClient(uName, hName);
					}else {
						int portnum = Integer.parseInt(portstr);
						client = new ClypeClient(uName, hName, portnum);
					}
					
					
					Thread clientRunner = new Thread( new Runnable() {
		        		@Override
		        		public void run() {
		        			client.start();
		        		}
					});	 
					
					Thread guiPrinter = new Thread(new Runnable() {

			            @Override
			            public void run() {
			                Runnable updater = new Runnable() {

			                    @Override
			                    public void run() {
			                    	if(toGui != null) {
			                    		Label l = new Label(toGui);
			                    		l.setWrapText(true);
			                    		messageHist.getChildren().add(l);
			                    		toGui = null;
			                    	}else if(dataToRecieveFromServer != null && dataToRecieveFromServer.getType() == ClypeData.SEND_PICTURE) {
			                    		ImageView image = new ImageView( (Image) dataToRecieveFromServer.getData());
//			                    		image.setFitWidth(100);
			                    		image.setPreserveRatio(true);
			                    		image.fitWidthProperty().bind(messageHist.widthProperty());
			                    		messageHist.getChildren().add(image);
			                    		dataToRecieveFromServer = null;
			                    	}else if(dataToRecieveFromServer != null && dataToRecieveFromServer.getType() == ClypeData.SEND_AUDIO) {
			                    		MediaPlayer player = new MediaPlayer((Media) dataToRecieveFromServer.getData());
			                    		MediaView audio = new MediaView(player);
			                    		AudioPlayer audioPlayer = new AudioPlayer(player);
			                			
			                    		messageHist.getChildren().add(audio);
			                    		messageHist.getChildren().add(audioPlayer);
			                    		System.out.println("test");
			                    		dataToRecieveFromServer = null;
			                    	}
			                    }
			                };

			                while (true) {
			                    try {
			                        Thread.sleep(100);
			                    } catch (InterruptedException ex) {
			                    }

			                    // UI update is run on the Application thread
			                    Platform.runLater(updater);
			                }
			            }

			        });
					
					Thread userPrinter = new Thread(new Runnable() {

			            @Override
			            public void run() {
			                Runnable updater = new Runnable() {

			                    @Override
			                    public void run() {
			                    	userListLabel.setText(userList);
			                    }
			                };

			                while (true) {
			                    try {
			                        Thread.sleep(200);
			                    } catch (InterruptedException ex) {
			                    }

			                    // UI update is run on the Application thread
			                    Platform.runLater(updater);
			                }
			            }

			        });
					
					sendButton.setOnAction( new EventHandler<ActionEvent>() {			
						public void handle( ActionEvent e ) {
							client.setSend(true);
							client.setmsg(usrMsg.getText());
							usrMsg.clear();
						}
					});	
					
					loadButton.setOnAction(z -> {
						File selectedFile = fileChooser.showOpenDialog(primaryStage);
						if(selectedFile != null) {
							client.mediaMsg = selectedFile.toString();
							client.setMediaSend(true);
						}
					});
					
					usrMsg.setOnKeyPressed( new EventHandler<KeyEvent>() {
						public void handle( KeyEvent ke ) {
							if ( ke.getCode() == KeyCode.ENTER ) {
								client.setSend(true);
								client.setmsg(usrMsg.getText());
								usrMsg.clear();
							}
						}
					});
					
					
					primaryStage.setScene(scene);
					primaryStage.show();
					
					clientRunner.start();
					guiPrinter.start();
					userPrinter.start();
					
				}
			});	

			logOnScreen.getChildren().add(logOn);

			
			primaryStage.setScene(log);
			primaryStage.show();
			
			
			
			
			
			
//			Service<String> service = new Service<String>() {
//
//				@Override
//				protected Task<String> createTask() {
//					Task<String> task = new Task<String>() {
//						@Override
//						protected String call() {
//							
//							client.start();
//							return "done";
//						}
//					};
//					return task;
//					
//				}
//	        		
//	        };
	        
	        
	        
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public static void main(String[] args) {
		launch(args);
	}
}
	
