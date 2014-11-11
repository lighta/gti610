package app.serveur_dns;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import app.serveur_dns.message_dns.HeaderDNS;
import app.serveur_dns.message_dns.MessageDNS;
import app.serveur_dns.message_dns.QuestionDNS;
import app.serveur_dns.message_dns.ReponseDNS;

/**
 * Cette classe permet la r�ception d'un paquet UDP sur le port de r�ception
 * UDP/DNS. Elle analyse le paquet et extrait le hostname
 * 
 * Il s'agit d'un Thread qui �coute en permanance pour ne pas affecter le
 * d�roulement du programme
 * 
 * @author Max
 *
 */

public class UDPReceiver extends Thread {
	/**
	 * Les champs d'un Packet UDP -------------------------- En-t�te (12
	 * octects) Question : l'adresse demand� R�ponse : l'adresse IP Autorit� :
	 * info sur le serveur d'autorit� Additionnel : information suppl�mentaire
	 */

	/**
	 * D�finition de l'En-t�te d'un Packet UDP
	 * --------------------------------------- Identifiant Param�tres QDcount
	 * Ancount NScount ARcount
	 * 
	 * � identifiant est un entier permettant d�identifier la requete. �
	 * parametres contient les champs suivant : � QR (1 bit) : indique si le
	 * message est une question (0) ou une reponse (1). � OPCODE (4 bits) : type
	 * de la requete (0000 pour une requete simple). � AA (1 bit) : le serveur
	 * qui a fourni la reponse a-t�il autorite sur le domaine? � TC (1 bit) :
	 * indique si le message est tronque. � RD (1 bit) : demande d�une requete
	 * recursive. � RA (1 bit) : indique que le serveur peut faire une demande
	 * recursive. � UNUSED, AD, CD (1 bit chacun) : non utilises. � RCODE (4
	 * bits) : code de retour. 0 : OK, 1 : erreur sur le format de la requete,
	 * 2: probleme du serveur, 3 : nom de domaine non trouve (valide seulement
	 * si AA), 4 : requete non supportee, 5 : le serveur refuse de repondre
	 * (raisons de s�ecurite ou autres). � QDCount : nombre de questions. �
	 * ANCount, NSCount, ARCount : nombre d�entrees dans les champs �Reponse�,
	 * �Autorite�, �Additionnel�.
	 */

	/**
	 * Les champs Reponse, Autorite, Additionnel sont tous representes de la
	 * meme maniere :
	 *
	 * � Nom (16 bits) : Pour eviter de recopier la totalite du nom, on utilise
	 * des offsets. Par exemple si ce champ vaut C0 0C, cela signifie qu�on a un
	 * offset (C0) de 12 (0C) octets. C�est-a-dire que le nom en clair se trouve
	 * au 12eme octet du message. � Type (16 bits) : idem que pour le champ
	 * Question. � Class (16 bits) : idem que pour le champ Question. � TTL (32
	 * bits) : dur�ee de vie de l�entr�ee. � RDLength (16 bits): nombre d�octets
	 * de la zone RDData. � RDData (RDLength octets) : reponse
	 */

	private DataInputStream d = null;
	protected final static int BUF_SIZE = 1024;
	protected String SERVER_DNS = null;//serveur de redirection
	protected int portRedirect = 53; // port  de redirection (par defaut)
	protected int port = 53; // port de r�ception
	private String DomainName = "none";
	private String DNSFile = null;
	private String adrIP = null;
	private boolean RedirectionSeulement = false;
	private String adresseIP = null;

	private String client_ip = null;
	private int client_port = 0;
	
	private boolean stop = false;

	public void setport(int p) {
		this.port = p;
	}

	public void setRedirectionSeulement(boolean b) {
		this.RedirectionSeulement = b;
	}

	public String gethostNameFromPacket() {
		return DomainName;
	}

	public String getAdrIP() {
		return adrIP;
	}

	private void setAdrIP(String ip) {
		adrIP = ip;
	}

	public void sethostNameFromPacket(String hostname) {
		this.DomainName = hostname;
	}

	public String getSERVER_DNS() {
		return SERVER_DNS;
	}

	public void setSERVER_DNS(String server_dns) {
		this.SERVER_DNS = server_dns;
	}

	public UDPReceiver() {
	}

	public UDPReceiver(String SERVER_DNS, int Port) {
		this.SERVER_DNS = SERVER_DNS;
		this.port = Port;
	}

	public void setDNSFile(String filename) {
		DNSFile = filename;
	}

	public void run() {
		try {
			DatagramSocket serveur;
			// *Creation d'un socket UDP
			serveur = new DatagramSocket(this.port);
			
			AnswerRecorder anRecorder = new AnswerRecorder(DNSFile);
			QueryFinder qf = new QueryFinder(this.DNSFile);
			
			// *Boucle infinie de recpetion
			while (!this.stop) {
				MessageDNS message = new MessageDNS();
				byte[] buff = new byte[0xFF];
				// *Reception d'un paquet UDP via le socket

				DatagramPacket rcvPacket = new DatagramPacket(buff, buff.length);
				System.out.println("Server is listening on "+serveur.getLocalAddress().toString() +" port "+serveur.getLocalPort() );
				serveur.receive(rcvPacket);
				
				client_ip = rcvPacket.getAddress().getHostAddress();
				client_port = rcvPacket.getPort();
				System.out.println("packet receive from ip="+client_ip+" port="+client_port);
				
				// *Creation d'un DataInputStream ou ByteArrayInputStream pour
				// manipuler les bytes du paquet

				ByteArrayInputStream stream = new ByteArrayInputStream(
						rcvPacket.getData());
				DataInputStream input = new DataInputStream(stream);

				this.readHeader(message, input);

				// ****** Dans le cas d'un paquet requ�te *****

				if (message.isQuery()) {
					QuestionDNS question = message.getQuestion();
					// *Lecture du Query Domain name, a partir du 13 byte
					byte len = 0;
					int i, j = 0;
					
					//lecture du qname
					String tmp;
					List<String> data = new ArrayList<>();
					while ((len = input.readByte()) != 0) {
						System.out.println("len=" + len);
						tmp = "";
						for (i = 0; i < len; i++) {
							tmp += (char)input.readByte();
						}
						data.add(tmp);
					}
					question.setQNAME(data);
					
					question.setQTYPE(input.readChar());
					question.setQCLASS(input.readChar());

					// *Sauvegarde du Query Domain name
					// *Sauvegarde de l'adresse, du port et de l'identifiant de la requete
					
					// rcvPacket.getAddress();
					// rcvPacket.getPort();

					// *Si le mode est redirection seulement
					if (this.RedirectionSeulement) {
						// *Rediriger le paquet vers le serveur DNS
						UDPSender UDPOut = new UDPSender(this.SERVER_DNS,this.portRedirect);		
						UDPOut.SendPacketNow(rcvPacket);
						
					} else// *Sinon
					{
						
						// *Rechercher l'adresse IP associe au Query Domain name
						// dans le fichier de
						// *correspondance de ce serveur
						
						String domaineToSearch = question.getFlatQname();
						System.out.println("domaineToSearch="+domaineToSearch);
						String ipFound = qf.StartResearch(domaineToSearch);
						// *Si la correspondance n'est pas trouvee
						if(ipFound.equalsIgnoreCase("none"))
						{
							// *Rediriger le paquet vers le serveur DNS
							UDPSender UDPOut = new UDPSender(this.SERVER_DNS,this.portRedirect);
							UDPOut.SendPacketNow(rcvPacket);
						}else{
							// *Sinon	
							// *Creer le paquet de reponse a l'aide du
							// UDPAnswerPaquetCreator
							UDPAnswerPacketCreator answer = new UDPAnswerPacketCreator();
							// *Placer ce paquet dans le socket
							byte[] awnserbyte  = answer.CreateAnswerPacket(buff,ipFound);
							DatagramPacket pktreply = new DatagramPacket(awnserbyte,awnserbyte.length);
							// *Envoyer le paquet
							UDPSender UDPOut = new UDPSender(client_ip,client_port);
							UDPOut.SendPacketNow(pktreply);
						}
					}
				} else // if(message.isResponse())
				{
					// ****** Dans le cas d'un paquet reponse *****
					// *Lecture du Query Domain name, a partir du 13 byte
					char len = 0;
					int i, j = 0;
					
					
					ReponseDNS reponse;
					for(i = 0; i<message.getHeader().getANCOUNT();++i)
					{
						reponse = new ReponseDNS();
						
						// *Lecture du Query Domain name, a partir du 13 byte
						List<String> data = new ArrayList<>();
						String tmp;
						while ((len = input.readChar()) != 0) {
							tmp = "";
							for (i = 0; i < len; i++) {
								tmp += input.readChar();
							}

							data.add(tmp);
						}
						if(data.isEmpty()) 
							continue; //prendre la prochaine reponse
						
						reponse.setNAME(data);
						
						// *Passe par dessus Type et Class
						reponse.setTYPE(input.readChar());
						reponse.setCLASS(input.readChar());
						
						// *Passe par dessus les premiers champs du ressource record
						// pour arriver au ressource data
						// *qui contient l'adresse IP associe au hostname (dans le
						// fond saut de 16 bytes)
						reponse.setTTL(input.readInt());
						reponse.setRDLENGTH(input.readChar());
						
						// *Capture de ou des adresse(s) IP (ANCOUNT est le nombre
						// de r�ponses retourn�es)
						byte[] rDATA = new byte[(int)reponse.getRDLENGTH()];
						String ipRDATA = "";
						for(j=0;j<reponse.getRDLENGTH();++j)
						{
							rDATA[j] = input.readByte();
							ipRDATA += rDATA[j] + "."; 
						}
						
						ipRDATA = ipRDATA.substring(0, ipRDATA.length()-1);
						
						reponse.setRDATA(rDATA);
						
						message.getReponse().add(reponse);
						
						
						// *Ajouter la ou les correspondance(s) dans le fichier DNS
						// si elles ne y sont pas d�j�
						String domaineToSearch = reponse.getFlatName(); // *Sauvegarde du Query Domain name
						String tmpip =  qf.StartResearch(domaineToSearch);
						if(tmpip.compareToIgnoreCase(ipRDATA) != 0 ) //ajout seulement si le match host+ip non present
							anRecorder.StartRecord(domaineToSearch, ipRDATA);
						
						// *Faire parvenir le paquet reponse au demandeur original,
						// ayant emis une requete
						// *avec cet identifiant
						UDPAnswerPacketCreator answer = new UDPAnswerPacketCreator();
						// *Placer ce paquet dans le socket
						byte[] awnserbyte  = answer.CreateAnswerPacket(buff,ipRDATA);
						DatagramPacket pktreply = new DatagramPacket(awnserbyte,awnserbyte.length);
						
						// *Envoyer le paquet
						UDPSender UDPOut = new UDPSender(client_ip,client_port);
						UDPOut.SendPacketNow(pktreply);
					}// end for ANCOUNT
				}// end else isReponse
			}// end while server
					


			serveur.close();

		} catch (Exception e) {
			System.err.println("Probl�me � l'ex�cution :");
			e.printStackTrace(System.err);
		}
	}
	
	/**
	 * Read a given number of bit and concatenate them. (must be below 8)
	 * @param input : stream to read
	 * @return : concatenated bit
	 * @throws IOException
	 */
	private byte readbit(DataInputStream input, int len) throws IOException{
		if(len > 8)
			throw new IOException();
		byte tmp = 0;
		for (int i = 0; i < len; i++) {
			tmp <<= 1;
			tmp |= input.readBoolean() ? 1 : 0;
		}
		return tmp;
	}

	private HeaderDNS readHeader(MessageDNS message, DataInputStream input)
			throws IOException {
		HeaderDNS header = message.getHeader();
		// *Lecture et sauvegarde des deux premier bytes, qui specifie
		// l'identifiant

		header.setID(input.readChar());
		System.out.println("ID="+((int)header.getID()));

		// * lecture du bit QR qui indique si le paquet est une requ�te ou ne
		// r�ponse.
		// * vous pouvez aussi vous servir du huitieme byte (ANCount), qui
		// specifie le nombre de reponses
		// * dans le message (si ANCount = 0 alors c'est une requ�te)

		char flags = input.readChar();
		header.setQR( (flags&1)==1 );
		header.setOpcode( (byte)(flags&(0x001E)) );
		header.setAA( (flags&(0x0020))==(0x0020) );
		header.setTC( (flags&(0x0040))==(0x0040) );
		header.setRD( (flags&(0x0080))==(0x0080) );
		header.setRA( (flags&(0x0100))==(0x0100) );		
		header.setZ( (byte) ((flags&(0x0E00))>>8) );
		header.setRCODE( (byte) ((flags&(0xF000))>>12) );
		
//		byte opcode = readbit(input,4);
//		header.setOpcode( opcode );
//		header.setAA(input.readBoolean());
//		header.setTC(input.readBoolean());
//		header.setRD(input.readBoolean());
//		header.setRA(input.readBoolean());		
//		byte Z = readbit(input,3);
//		header.setZ(Z);
//		byte rCode = readbit(input,4);
//		header.setRCODE(rCode);
		
		System.out.println("flag="+Integer.toBinaryString((int)flags));
		System.out.println("QR="+header.getQR());
		System.out.println("OpCode="+((int)header.getOpcode()));
		System.out.println("AA="+header.getAA());
		System.out.println("TC="+header.getTC());
		System.out.println("RD="+header.getRD());
		System.out.println("RA="+header.getRA());
		System.out.println("Z="+((int)header.getZ()));
		System.out.println("rCode="+((int)header.getRCODE()));	
		
		header.setQDCOUNT(input.readChar());
		System.out.println("QDCOUNT="+((int)header.getQDCOUNT()));	
		header.setANCOUNT(input.readChar());
		System.out.println("ANCOUNT="+((int)header.getANCOUNT()));	
		header.setNSCOUNT(input.readChar());
		System.out.println("NSCOUNT="+((int)header.getNSCOUNT()));	
		header.setARCOUNT(input.readChar());
		System.out.println("ARCOUNT="+((int)header.getARCOUNT()));	

		return header;
	}
}
