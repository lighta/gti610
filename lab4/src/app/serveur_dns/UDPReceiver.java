package app.serveur_dns;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import app.serveur_dns.message_dns.HeaderDNS;
import app.serveur_dns.message_dns.MessageDNS;
import app.serveur_dns.message_dns.QuestionDNS;
import app.serveur_dns.message_dns.ReponseDNS;

/**
 * Cette classe permet la reception d'un paquet UDP sur le port de reception
 * UDP/DNS. Elle analyse le paquet et extrait le hostname
 * 
 * Il s'agit d'un Thread qui ecoute en permanance pour ne pas affecter le
 * deroulement du programme
 * 
 * @author Max
 *
 */

public class UDPReceiver extends Thread {
	/**
	 * Les champs d'un Packet UDP -------------------------- En-tete (12
	 * octects) Question : l'adresse demande Reponse : l'adresse IP Autorite :
	 * info sur le serveur d'autorite Additionnel : information supplementaire
	 */

	/**
	 * Definition de l'En-tete d'un Packet UDP
	 * --------------------------------------- Identifiant Parametres QDcount
	 * Ancount NScount ARcount
	 * 
	 * L'identifiant est un entier permettant d'identifier la requete. �
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

	protected final static int BUF_SIZE = 1024;
	protected String SERVER_DNS = null;//serveur de redirection (ip)
	protected int portRedirect = 53; // port  de redirection (par defaut)
	protected int port = 53; // port de r�ception
	private String adrIP = null; //bind ip d'ecoute
	private String DomainName = "none";
	private String DNSFile = null;
	private boolean RedirectionSeulement = false;
	
	private class ClientInfo { //quick container
		public String client_ip = null;
		public int client_port = 0;
	};
	private HashMap<Integer, ClientInfo> Clients = new HashMap<>();
	
	private boolean stop = false;

	
	public UDPReceiver() {
	}

	public UDPReceiver(String SERVER_DNS, int Port) {
		this.SERVER_DNS = SERVER_DNS;
		this.port = Port;
	}
	
	
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

	public String getSERVER_DNS() {
		return SERVER_DNS;
	}

	public void setSERVER_DNS(String server_dns) {
		this.SERVER_DNS = server_dns;
	}



	public void setDNSFile(String filename) {
		DNSFile = filename;
	}

	public void run() {
		try {
			// *Creation d'un socket UDP
			DatagramSocket serveur = new DatagramSocket(this.port);
			
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
				
				String send_ip = rcvPacket.getAddress().getHostAddress();
				int send_port = rcvPacket.getPort();
				System.out.println("packet receive from ip="+send_ip+" port="+send_port);
				
				// *Creation d'un DataInputStream ou ByteArrayInputStream pour
				// manipuler les bytes du paquet

				ByteArrayInputStream stream = new ByteArrayInputStream(
						rcvPacket.getData());
				DataInputStream input = new DataInputStream(stream);

				this.readHeader(message, input);
				HeaderDNS header = message.getHeader();

				// ****** Dans le cas d'un paquet requ�te *****

				if (message.isQuery() && header.getANCOUNT()==0) {
					System.out.println("\n\n\npacket is a question");
					QuestionDNS question = message.getQuestion();
					// *Lecture du Query Domain name, a partir du 13 byte

					// *Sauvegarde du Query Domain name
					readQuestion(question,input);
					System.out.println("Qtype="+(int)question.getQTYPE());
					System.out.println("Qcless="+(int)question.getQCLASS());
					
					// *Sauvegarde de l'adresse, du port et de l'identifiant de la requete
					ClientInfo cl = new ClientInfo();
					cl.client_ip = send_ip;
					cl.client_port = send_port;

					// *Si le mode est redirection seulement
					if (this.RedirectionSeulement) {
						// *Rediriger le paquet vers le serveur DNS
						UDPSender UDPOut = new UDPSender(this.SERVER_DNS,this.portRedirect,serveur);		
						UDPOut.SendPacketNow(rcvPacket);
						Clients.put((int) message.getHeader().getID(), cl);
					} else// *Sinon
					{
						// *Rechercher l'adresse IP associe au Query Domain name
						// dans le fichier de correspondance de ce serveur					
						String domaineToSearch = question.getFlatQname();
						System.out.println("domaineToSearch="+domaineToSearch);
						List<String> listAdrfound = qf.StartResearch(domaineToSearch);
						
						// *Si la correspondance n'est pas trouvee
						if(listAdrfound.isEmpty())
						{
							System.out.println("Adresse not found transfering to ip="+this.SERVER_DNS+" port="+this.portRedirect);
							// *Rediriger le paquet vers le serveur DNS
							UDPSender UDPOut = new UDPSender(this.SERVER_DNS,this.portRedirect,serveur);
							UDPOut.SendPacketNow(rcvPacket);
							Clients.put((int) message.getHeader().getID(), cl);
						} else {
							// *Sinon	
							// *Creer le paquet de reponse a l'aide du
							// UDPAnswerPaquetCreator
							System.out.println("Adresse found replying to ip="+cl.client_ip+" port="+cl.client_port);
							UDPAnswerPacketCreator answer_creator = UDPAnswerPacketCreator.getInstance();
							// *Placer ce paquet dans le socket
							byte[] awnserbyte  = answer_creator.CreateAnswerPacket(buff,listAdrfound);
							DatagramPacket pktreply = new DatagramPacket(awnserbyte,awnserbyte.length);
							// *Envoyer le paquet
							UDPSender UDPOut = new UDPSender(cl.client_ip,cl.client_port,serveur);
							UDPOut.SendPacketNow(pktreply);
						}
					}
				} 
				else { // if(message.isResponse())
					int id = header.getID();
					String searchip = null;
					System.out.println("\n\n\npacket is a awnser ID = "+id);
					//1st we need a client to anwser to, so let check if we have that in our map
					ClientInfo cl = Clients.get(id);
					Clients.remove(id);
					
					if(cl.client_ip == null || cl.client_port == 0) {
						System.out.println("No client found associated with ID="+id+" skipping !!");
						continue; //don't handle data
					}
					System.out.println("Client found {\n\t ip="+cl.client_ip+" \n\t port="+cl.client_port+"\n}");
						
					if(message.isQuery()){ //has a query in it
						QuestionDNS question = message.getQuestion();
						readQuestion(question,input);
						searchip = question.getFlatQname();
					}
					
					int i;
					for(i = 0; i<header.getANCOUNT();i++) //parcour de toute les reponse
					{
						ReponseDNS reponse = new ReponseDNS();
						// *Lecture du Query Domain name, a partir du 13 byte
//						List<String> data = readQName(input);
//						if(data.isEmpty()) 
//							continue; //prendre la prochaine reponse				
//						reponse.setNAME(data);
						reponse.setNAME(message.getQuestion().getQNAME());
						input.readChar(); //lecture du name
						
						// *Passe par dessus Type et Class
						reponse.setTYPE(input.readChar());
						reponse.setCLASS(input.readChar());
						
						// *Passe par dessus les premiers champs du ressource record
						// pour arriver au ressource data
						// *qui contient l'adresse IP associe au hostname (dans le
						// fond saut de 16 bytes)
						input.readChar();
						input.readChar();
						//reponse.setTTL(input.readInt());
						char rdlenght =input.readChar();
						reponse.setRDLENGTH(rdlenght);
						int rdl = ((int)rdlenght)&(0x00FF);
						
						// *Capture de ou des adresse(s) IP (ANCOUNT est le nombre
						// de r�ponses retourn�es)			
						reponse.setRDATA(readRDATA(input,rdl));
						message.getReponse().add(reponse);
						
						System.out.println("type="+(int)reponse.getTYPE());
						System.out.println("class="+(int)reponse.getCLASS());
						System.out.println("ttl="+(int)reponse.getTTL());
						System.out.println("RDLenght="+(int)reponse.getRDLENGTH());
					} //finish reading packet
					
					ArrayList<String> listAdrfound = new ArrayList<>();
					List<String> tmpip =  qf.StartResearch(searchip);
					
					//recherche et enregistrement des IP trouver
					for(i = 0; i<header.getANCOUNT();i++){
						ReponseDNS reponse = message.getReponse().get(i);
						// *Ajouter la ou les correspondance(s) dans le fichier DNS
						// si elles ne y sont pas d�j�
						String domaineToSearch = reponse.getFlatName(); // *Sauvegarde du Query Domain name
						if(domaineToSearch == null)
							continue;		
						
						String ipRDATA = reponse.getFlatIP();
						if(ipRDATA == null)
							continue;
						if( !tmpip.contains(ipRDATA) ) //ajout seulement si le match host+ip non present
							anRecorder.StartRecord(domaineToSearch, ipRDATA);
						listAdrfound.add(ipRDATA);	
					}// end for ANCOUNT
					
					if(listAdrfound.isEmpty() == false){
						// *Faire parvenir le paquet reponse au demandeur original,
						// ayant emis une requete
						// *avec cet identifiant
						UDPAnswerPacketCreator answer_creator = UDPAnswerPacketCreator.getInstance();
						// *Placer ce paquet dans le socket
						byte[] awnserbyte  = answer_creator.CreateAnswerPacket(buff,listAdrfound);
						DatagramPacket pktreply = new DatagramPacket(awnserbyte,awnserbyte.length);
						
						// *Envoyer le paquet
						UDPSender UDPOut = new UDPSender(cl.client_ip,cl.client_port,serveur);
						UDPOut.SendPacketNow(pktreply);
					} //si aucune reponse trouve ne pasrepondre
				}// end else isReponse
			}// end while server
			
			serveur.close(); //closing server
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
		header.setQR( (flags&0001)==(0x0001) );
		header.setOpcode( (byte)(flags&(0x001E)) );
		header.setAA( (flags&(0x0020))==(0x0020) );
		header.setTC( (flags&(0x0040))==(0x0040) );
		header.setRD( (flags&(0x0080))==(0x0080) );
		header.setRA( (flags&(0x0100))==(0x0100) );		
		header.setZ( (byte) ((flags&(0x0E00))>>8) );
		header.setRCODE( (byte) ((flags&(0xF000))>>12) );
		
		header.setQDCOUNT(input.readChar());	
		header.setANCOUNT(input.readChar());
		header.setNSCOUNT(input.readChar());	
		header.setARCOUNT(input.readChar());
		
//		System.out.println("flag="+Integer.toBinaryString((int)flags));
//		System.out.println("QR="+header.getQR());
//		System.out.println("OpCode="+((int)header.getOpcode()));
//		System.out.println("AA="+header.getAA());
//		System.out.println("TC="+header.getTC());
//		System.out.println("RD="+header.getRD());
//		System.out.println("RA="+header.getRA());
//		System.out.println("Z="+((int)header.getZ()));
//		System.out.println("rCode="+((int)header.getRCODE()));	
		System.out.println("QDCOUNT="+((int)header.getQDCOUNT()));	
		System.out.println("ANCOUNT="+((int)header.getANCOUNT()));	
		System.out.println("NSCOUNT="+((int)header.getNSCOUNT()));
		System.out.println("ARCOUNT="+((int)header.getARCOUNT()));	
		return header;
	}
	
	private void readQuestion(QuestionDNS question, DataInputStream input) throws IOException {
		// *Sauvegarde du Query Domain name
		question.setQNAME(readQName(input));
		question.setQTYPE(input.readChar());
		question.setQCLASS(input.readChar());
	}
	
	private List<String> readQName(DataInputStream input) throws IOException {
		int len, i;
		String tmp;
		List<String> data = new ArrayList<>();
		while ((len = input.readByte()) != 0) {
			//System.out.println("len=" + len);
			tmp = "";
			for (i = 0; i < len; i++) {
				tmp += (char)input.readByte();
			}
			data.add(tmp);
		}
		return data;
	}
	
	private byte[] readRDATA(DataInputStream input, int rdlenght) throws IOException{
		int j=0;
		System.out.println("readRDATA rdlenght="+(int)rdlenght);
		byte[] rDATA = new byte[rdlenght];
		for(j=0;j<rdlenght;j++){
			rDATA[j] = input.readByte();
			System.out.println("\trdata["+j+"]=" + rDATA[j]);
		}
		return rDATA;
	}
}
