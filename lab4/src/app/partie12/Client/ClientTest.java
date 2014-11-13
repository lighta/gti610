package app.partie12.Client;

import java.io.*; 
import java.net.*;
import java.util.Scanner;

import app.partie12.Server.Server;

public class ClientTest {

	public static void main(String argv[]){
		String modifiedSentence;
		boolean running=true;
		
		Scanner in = new Scanner(System.in);
		
		while(running){	
			Socket clientSocket;
			String inFromUser;
			
			System.out.println("Veuillez entree l'information a envoyer au serveur");
			inFromUser = in.nextLine();
			try {
				clientSocket = new Socket("localhost", app.partie12.Server.Server.PORT_DEFAULT);
				DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
				BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				outToServer.writeBytes(inFromUser + '\n');
				modifiedSentence = inFromServer.readLine();
				System.out.println("FROM SERVER: " + modifiedSentence);
				clientSocket.close();
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("Souhaitez vous continuez O/N");
			inFromUser = in.nextLine();
			if(inFromUser.toUpperCase().contentEquals("N"))
				running=false;
		}
		in.close();
	}

}