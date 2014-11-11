package app.serveur_dns.message_dns;

import java.util.List;

public class QuestionDNS {
	private List<String> QNAME;
	private char QTYPE;
	private char QCLASS;
	
	public List<String> getQNAME() {
		return QNAME;
	}
	public void setQNAME(List<String> qNAME) {
		QNAME = qNAME;
	}
	public char getQTYPE() {
		return QTYPE;
	}
	public void setQTYPE(char qTYPE) {
		QTYPE = qTYPE;
	}
	public char getQCLASS() {
		return QCLASS;
	}
	public void setQCLASS(char qCLASS) {
		QCLASS = qCLASS;
	}
	
	public String getFlatQname(){
		if(QNAME.isEmpty())
			return null;
		String tmp="";
		int i;
		for(i=0; i<QNAME.size()-1; i++){
			tmp += QNAME.get(i)+".";
		}
		tmp += QNAME.get(i); //rajout du dernier element sans point
		return tmp;
	}
}
