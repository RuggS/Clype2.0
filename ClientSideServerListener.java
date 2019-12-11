package main;

import java.util.Scanner;

public class ClientSideServerListener implements Runnable{
	
	ClypeClient  cli;
	

	public ClientSideServerListener(ClypeClient client) {
		cli = client;
	}
	
	@Override
	public void run() {
		
		while(!cli.closeConnection) {
//			cli.inFromStd = new Scanner(System.in);
//			cli.readClientData();
//			if(!cli.closeConnection) {
//				cli.sendData();
//			}
			if(!cli.closeConnection) {
				cli.receiveData();
			}
			if(!cli.closeConnection) {
				cli.printData();
			}
		}
		
	}

}
