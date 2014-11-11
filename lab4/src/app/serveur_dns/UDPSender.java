package app.serveur_dns;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

	public class UDPSender  {

	private final static int BUF_SIZE = 1024;
	
	private String dest_ip = null; //ip de reception
	private int dest_port = 53;  // port de reception
	private DatagramSocket SendSocket = null;
	private InetAddress addr = null;
	
	/**
	 * Contructor
	 * @param destip = adresse ip a envoyer le paquet
	 * @param destport = port a envoyer le paquet
	 */
	public UDPSender(String destip, int destport){
		try {
			SendSocket = new DatagramSocket();
			this.dest_port = destport;
			this.dest_ip = destip;
			//cree l'adresse de destination
			this.addr = InetAddress.getByName(dest_ip);
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public UDPSender(InetAddress address, int port) {
		try {
			SendSocket = new DatagramSocket();
			this.dest_port = port;
			this.addr = address;
			dest_ip = address.getHostAddress();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	public String getDest_ip() {
		return dest_ip;
	}

	public int getDest_port() {
		return dest_port;
	}

	public InetAddress getAddr() {
		return addr;
	}

	
	public void SendPacketNow(DatagramPacket packet) 
		throws IOException {
		//Envoi du packet � un serveur dns pour interrogation
		if(SendSocket == null)
			throw new IOException("Invalid Socket for send (null)");
		try {
			//Cr�e le packet
			packet.setAddress(addr);
			packet.setPort(dest_port);
			//Envoi le packet
			SendSocket.send(packet);

		} catch (Exception e) {
			System.err.println("Probl�me � l'ex�cution :");
			e.printStackTrace(System.err);
		}
	}
}