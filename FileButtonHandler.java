package application;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.TextField;
import data.*;
import main.*;

public class FileButtonHandler implements EventHandler<ActionEvent>{

	private TextField textField;
	private String message;
	private ClypeClient client;
	
	public FileButtonHandler(TextField t, ClypeClient cli) {
		textField = t;
		client = cli;
	}
	
	@Override
	public void handle(ActionEvent e) {
		message = "SENDFILE" + textField.getText();
		client.readClientData(message);
		
	}

}
