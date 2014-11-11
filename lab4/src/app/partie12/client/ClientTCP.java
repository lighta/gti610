package app.partie12.client;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Scanner;

import app.partie12.server.ServeurTCP;

public class ClientTCP {
	private Socket clientSocket;
	String servip;
	int servport;

	public ClientTCP(String servip, int servport) {
		this.servip = servip;
		this.servport = servport;
	}

	private void connect() {
		try {
			clientSocket = new Socket(servip, servport);
			readdata();
		} catch (IOException e) {
			System.out.println(e);
		}
	}

	private void close() {
		try {
			clientSocket.close();
		} catch (IOException e) {
			System.out.println(e);
		}
	}

	private void readdata() {
		int MAXLENGTH = 256;
		byte[] buff = new byte[MAXLENGTH];
		try {
			InputStream in = clientSocket.getInputStream();
			in.read(buff);
			System.out.println("Data read 1= { ");
			int i=0;
			while(buff.length > i && buff[i]!='\0' ){
				System.out.printf("%c", buff[i]);
				i++;
			}
			System.out.println("\n } End read");
			
			System.out.println("Data read 2= { ");
			i=0;
			while(buff.length > i && buff[i]!='\0'){
				System.out.println("\t"+buff[i]);
				i++;
			}
			System.out.println("} End read");
		} catch (IOException e) {
			System.out.println(e);
		}
		// (le message Hello from server est récupéré par le client dans le
		// tableau buff)
	}

	public static void main(String[] args) {	
		int portconf = ServeurTCP.PORT_DEFAULT;
		String ipconf = ServeurTCP.IP_DEFAULT;
		
		if (args.length > 0) {
			Scanner in = new Scanner(System.in);
			System.out.println("Veuilllez precisez le port d ecoute. default ["
					+ portconf + "] ");
			portconf = in.nextInt();
			in.nextLine();
			in.close();
		}
		
		ClientTCP client = new ClientTCP(ipconf, portconf);
		client.connect();
		client.close();
	}
}
