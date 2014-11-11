package app.serveur_dns.message_dns;

import java.util.List;

public class ReponseDNS {
	private List<String> NAME;
	private char TYPE;
	private char CLASS;
	private int TTL;
	private char RDLENGTH;
	private byte[] RDATA;
	
	public List<String> getNAME() {
		return NAME;
	}
	public void setNAME(List<String> nAME) {
		NAME = nAME;
	}
	public char getTYPE() {
		return TYPE;
	}
	public void setTYPE(char tYPE) {
		TYPE = tYPE;
	}
	public char getCLASS() {
		return CLASS;
	}
	public void setCLASS(char cLASS) {
		CLASS = cLASS;
	}
	public int getTTL() {
		return TTL;
	}
	public void setTTL(int tTL) {
		TTL = tTL;
	}
	public char getRDLENGTH() {
		return RDLENGTH;
	}
	public void setRDLENGTH(char rDLENGTH) {
		RDLENGTH = rDLENGTH;
	}
	public byte[] getRDATA() {
		return RDATA;
	}
	public void setRDATA(byte[] rDATA) {
		RDATA = rDATA;
	}
	
	public String getFlatName(){
		if(NAME.isEmpty())
			return null;
		String tmp="";
		int i;
		for(i=0; i<NAME.size()-1; i++){
			tmp += NAME.get(i)+".";
		}
		tmp += NAME.get(i); //rajout du dernier element sans point
		return tmp;
	}
}
