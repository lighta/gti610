package app.serveur_dns;

import java.util.List;


public class UDPAnswerPacketCreator {
	
	int longueur;
	byte[] Answerpacket;
	
	public int getLongueur(){
		return longueur;
	}
	
	public byte[] getAnswrpacket(){
		return Answerpacket;
	}
	
	public UDPAnswerPacketCreator(){
		
	}
	
	public byte[] CreateAnswerPacket(byte[] Qpacket,List<String> listadrr){
		
		int ancount = listadrr.size();
		if(ancount == 0){
			System.out.println("No adresse to search exiting");
			return null;
		}
		System.out.println("Preparing packet for len="+ancount);
		
		//System.out.println("Le packet QUERY recu");
		
		for(int i = 0;i < Qpacket.length;i++){
			if(i%16 == 0){
				//System.out.println("\r");
			}
			//System.out.print(Integer.toHexString(Qpacket[i] & 0xff).toString() + " ");
		}
		//System.out.println("\r");
		
		//copie les informations dans un tableau qui est utilise de buffer
		//durant la modification du packet
		byte[] Querypacket = new byte[1024];
		for(int i = 0; i < Qpacket.length; i++){
			Querypacket[i] = Qpacket[i];
		}
		
		//modification de l'identifiant
		Querypacket[0] = (byte)Qpacket[0];
		Querypacket[1] = (byte)Qpacket[1];
		
		//modification des param�tres
		//Active le champ reponse dans l'en-t�te
		Querypacket[2] = (byte) 0x81; //QR+opcode+AA+TC+RD
		Querypacket[3] = (byte) 0x80; //RA+Z+RCODE
		Querypacket[4] = (byte) 0x00; //Qcount & 0xFF00
		Querypacket[5] = (byte) 0x01; //Qcount & 0x00FF
		
		Querypacket[6] = ((byte) ((ancount&(0xFF00)) >>8) ); //Ancount & 0xFF00
		Querypacket[7] = (byte) ((ancount&(0x00FF)) ); //Ancount & 0x00FF
		
		//Serveur authority --> 0 il n'y a pas de serveur d'autorit�
		Querypacket[8] = (byte) 0x00; //NScount & 0xFF00
		Querypacket[9] = (byte) 0x00; //NScount & 0x00FF
		
		Querypacket[10] = (byte) 0x00; //ARCOUNT & 0xFF00
		Querypacket[11] = (byte) 0x00; //NScount & 0x00FF

		//Lecture de l'hostname
		//ici comme on ne connait pas la grandeur que occupe le nom de domaine
		//nous devons rechercher l'index pour pouvoir placer l'adresse IP � la bonne endroit
		//dans le packet
		
		int nbchar = Querypacket[12]; //lecture du arcount de base
		String hostName = "";
		int index = 13;
		
		//lire qname
		while(nbchar != 0){			
			while(nbchar > 0) {
				hostName = hostName + String.valueOf(Character.toChars(Querypacket[index]));
			index++;
			nbchar--;
			}
			hostName = hostName + ".";
			
			nbchar = Querypacket[index];
			index++;
		}
		//System.out.println(hostName);       
		index = index - 1; 
    

		//Identification de la class
		//type
		Querypacket[index + 1] = (byte)0x00; //Qtype  & 0xFF00
		Querypacket[index + 2] = (byte)0x01; //Qtype  & 0x00FF
		//class
		Querypacket[index + 3] = (byte)0x00; //Qclass  & 0xFF00
		Querypacket[index + 4] = (byte)0x01; //Qclass  & 0x00FF
		
		
		//Champ reponse
		int i, lenanswer=16;
		int j=index + 5;
		for(i=0; i<ancount; i++){
			//name offset !TODO whaaaat ?
			Querypacket[i*lenanswer + j] = (byte) (0xC0); //name  & 0xFF00
			Querypacket[i*lenanswer + j + 1] = (byte) (0x0C); //name  & 0x00FF
			
			Querypacket[i*lenanswer + j + 2] = (byte) (0x00); //type  & 0xFF00
			Querypacket[i*lenanswer + j + 3] = (byte) 0x01;	//type  & 0x00FF
			
			
			Querypacket[i*lenanswer + j + 4] = (byte) 0x00; //class  & 0xFF00
			Querypacket[i*lenanswer + j + 5] = (byte) 0x01; //class & 0x00FF
			
			//TTL
			Querypacket[i*lenanswer + j + 6] = (byte) 0x00;
			Querypacket[i*lenanswer + j + 7] = (byte) 0x01;
			Querypacket[i*lenanswer + j + 8] = (byte) 0x1a;
			Querypacket[i*lenanswer + j + 9] = (byte) (0x6c);
			
			
			//Grace a l'index de possion, nous somme en mesure
			//de faire l'injection de l'adresse IP dans le packet
			//et ce � la bonne endroit
			Querypacket[i*lenanswer + j + 10] = (byte) (0x00); //RDLENGHT & 0xFF00
			Querypacket[i*lenanswer + j + 11] = 0x04;//taille RDLENGHT 0x00FF
			
			//Conversion de l'adresse IP de String en byte
			String adrr = listadrr.get(i);
			adrr = adrr.replace("."," ");
			String[] adr = adrr.split(" ");
			byte part1 = 0;
			byte part2 = 0;
			byte part3 = 0;
			byte part4 = 0;
			part1 = (byte)(Integer.parseInt(adr[0]) & 0xff);
			part2 = (byte)(Integer.parseInt(adr[1]) & 0xff);
			part3 = (byte)(Integer.parseInt(adr[2]) & 0xff);
			part4 = (byte)(Integer.parseInt(adr[3]) & 0xff);
			
			//IP RDATA
			Querypacket[i*lenanswer + j + 12] = (byte) (part1 & 0xff);
			Querypacket[i*lenanswer + j + 13] = (byte) (part2 & 0xff);
			Querypacket[i*lenanswer + j + 14] = (byte) (part3 & 0xff);
			Querypacket[i*lenanswer + j + 15] = (byte) (part4 & 0xff);
			j=0; //je sais c'est moche mais bon !
		}
		
		longueur = i*(lenanswer) + index + 5; 
		Answerpacket = new byte[this.longueur];
		for(i = 0; i < Answerpacket.length; i++){ //remply le reste de merde
			Answerpacket[i] = Querypacket[i];
		}
		
		System.out.println("Identifiant: 0x" + Integer.toHexString(Answerpacket[0] & 0xff) + Integer.toHexString(Answerpacket[1] & 0xff));
		System.out.println("parametre: 0x" + Integer.toHexString(Answerpacket[2] & 0xff) + Integer.toHexString(Answerpacket[3] & 0xff));
		System.out.println("question: 0x" + Integer.toHexString(Answerpacket[4] & 0xff) + Integer.toHexString(Answerpacket[5] & 0xff));
		System.out.println("reponse: 0x" + Integer.toHexString(Answerpacket[6] & 0xff) + Integer.toHexString(Answerpacket[7] & 0xff));
		System.out.println("autorite: 0x" + Integer.toHexString(Answerpacket[8] & 0xff) + Integer.toHexString(Answerpacket[9] & 0xff));
		System.out.println("info complementaire: 0x" + Integer.toHexString(Answerpacket[10] & 0xff) + Integer.toHexString(Answerpacket[11] & 0xff));
		
		
		for(i = 0;i < Answerpacket.length;i++){
			if(i%16 == 0){
				System.out.println("\r");
			}
			System.out.print(Integer.toHexString(Answerpacket[i] & 0xff).toString() + " ");
		}
		System.out.println("\r");
		
		return Answerpacket;
	}
}