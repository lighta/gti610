package app.partie12.server;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class ServeurTCP {
	public static final int PORT_DEFAULT = 20500;
	public static final String IP_DEFAULT = "127.0.0.1";

	private ServerSocket serveur;
	private Socket connexionSocket;
	int portNumber;

	public ServeurTCP(int portNumber) {
		this.portNumber = portNumber;
		try {
			serveur = new ServerSocket(portNumber);
		} catch (IOException e) {
			System.out.println(e);
		}
	}

	private void start() {
		try {
			System.out.println("Server is listening on port "+portNumber);
			connexionSocket = serveur.accept();
			reply();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void reply() {
		String data = "Hello from server";
		try {
			OutputStream out = connexionSocket.getOutputStream();
			out.write(data.getBytes());
			System.out.println("Server is sending "+data);
		} catch (IOException e) {
			System.out.println(e);
		}
	}

	private void stop() {
		try {
			System.out.println("Server is closing");
			connexionSocket.close(); // Close the socket. We are
			// done with this client!
			serveur.close(); // close the server socket
			System.out.println("Server is closed");
		} catch (IOException e) {
			System.out.println(e);
		}
	}

	public static void main(String[] args) {	
		int portconf = PORT_DEFAULT;

		if (args.length > 0) {
			Scanner in = new Scanner(System.in);
			System.out.println("Veuilllez precisez le port d ecoute. default ["
					+ portconf + "] ");
			portconf = in.nextInt();
			in.nextLine();
			in.close();
		}
		ServeurTCP serv= new ServeurTCP(portconf);
		serv.start();
		serv.stop();
	}
}
