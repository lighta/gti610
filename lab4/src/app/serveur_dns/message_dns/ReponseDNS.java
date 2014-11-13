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
	
	public String getFlatIP(){
		if(RDATA.length==0)
			return null;
		int j;
		
		String ipRDATA = "";
		for(j=0;j<this.RDLENGTH-1;++j){
			int tmp;
			if( (RDATA[j]&(0x80))==(0x80) )
				tmp=(RDATA[j]&(0x7F))+128;
			else
				tmp=RDATA[j];
			ipRDATA += tmp + "."; 
		}
		ipRDATA += RDATA[j]; //last one doesn't have a dot
		System.out.println("getFlatIP IP read = "+ipRDATA);
		return ipRDATA;
	}
}
