package app.partie12.Server;

import java.io.*;
import java.net.*;

public class Server {
	public static final int PORT_DEFAULT = 20500;
	public static final int METHOD_Q1 = 1, METHOD_Q2 = 2;
	private static Thread servth;

	public static void main(String argv[]) throws Exception {
		final BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		final int methode = getMethod(br);
		starting(br,methode);
		br.close();
	}
	
	private static int getMethod(final BufferedReader br) throws IOException {	
		int methode = 0;
		String input = null;
		while( true ){
			System.out.println("Entrer la methode que vous voulez utiliser (1 ou 2)");
			try {
				input = br.readLine();
		         methode = Integer.parseInt(input);
		      } catch (IOException ioe) {
		         System.out.println("IO error trying to read your name!");
		      } catch( NumberFormatException fmt){
			         System.out.println("Entrie not a number");
		      }
			if(!(methode == METHOD_Q1 || methode == METHOD_Q2))
				System.out.println("Methode not recognized");
			else
				break;
		}
		return methode;
	}
	
	private static void starting(final BufferedReader br,final int methode) throws Exception{
		System.out.println("Starting server with methode "+methode+"...");
		servth = new ServerTCP(methode);
		servth.start();
		System.out.println("Pour stopper appuyer sur une entre...");
		br.readLine();
		servth.interrupt();
		// Attente de la fin du thread
		servth.join();
		while(servth.isAlive()) {
			Thread.sleep(1*1000); //waiting end
		}
		System.out.println("Server is closed");
	}

	
	public static class ServerTCP extends Thread
	{
		
		public static class MultiThread extends Thread {
			
		}
		
		ServerSocket servSocket;
		int methode;
		
		public ServerTCP(int methode) throws IOException {
			servSocket = new ServerSocket(PORT_DEFAULT);
			this.methode = methode;
		}
		
		private void closeS() {
			System.out.println("Server is closing");
			try {
				servSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		@Override
		public void run()
		{
			// Tant que le thread n'est pas interrompu...
			while (! isInterrupted())
			{
				try
				{
					System.out.println("Server is listening on port="+ PORT_DEFAULT);
					final Socket connectionSocket = servSocket.accept();
					switch (methode) {
						case 1: {
							BufferedReader inFromClient = new BufferedReader(
									new InputStreamReader(
											connectionSocket.getInputStream()));
							DataOutputStream outToClient = new DataOutputStream(
									connectionSocket.getOutputStream());
							String clientSentence = inFromClient.readLine();
							System.out.println("Received: " + clientSentence);
							String capitalizedSentence = clientSentence.toUpperCase() + '\n';

							System.out.println("Starting simulation long traitement ");
							Thread.sleep(20 * 1000);
							System.out.println("End simulation");
							outToClient.writeBytes(capitalizedSentence);
							break;
						}
						case 2: {
							new Thread() {
								public void run() {
									try {
										BufferedReader inFromClient = new BufferedReader(
												new InputStreamReader(connectionSocket.getInputStream()));
										DataOutputStream outToClient = new DataOutputStream(
												connectionSocket.getOutputStream());
										String clientSentence = inFromClient.readLine();
										System.out.println("Received: "
												+ clientSentence);
										String capitalizedSentence = clientSentence
												.toUpperCase() + '\n';

										System.out
												.println("Starting simulation long traitement ");
										Thread.sleep(20 * 1000);
										System.out.println("End simulation");

										outToClient.writeBytes(capitalizedSentence);
									} catch (IOException e) {
										e.printStackTrace();
									} catch (InterruptedException e) {
										System.out.println("Traitement Interupt");
									//	e.printStackTrace();
										Thread.currentThread().interrupt();
									}
								}
							}.start();
							break;
						}
					}
				}
				catch (InterruptedException e)
				{
					System.out.println("J'ai �t� interrompu !");
					// Activation du flag d'interruption
					Thread.currentThread().interrupt();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			closeS();
		}
	}
	
}
