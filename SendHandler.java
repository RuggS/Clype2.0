package application;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.TextField;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import data.*;
import main.*;

public class SendHandler implements EventHandler<ActionEvent>{

	
	ClypeClient client;

	public SendHandler(ClypeClient cli) {
		client = cli;
	}

	@Override
	public void handle(ActionEvent e) {
		client.setSend(true);

	}

}
