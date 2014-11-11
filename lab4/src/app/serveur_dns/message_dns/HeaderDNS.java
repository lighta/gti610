package app.serveur_dns.message_dns;

public class HeaderDNS {
	private char ID;
	private boolean QR;
	private byte Opcode;
	private boolean AA;
	private boolean TC;
	private boolean RD;
	private boolean RA;
	private byte Z;
	private byte RCODE;
	private char QDCOUNT;
	private char ANCOUNT;
	private char NSCOUNT;
	private char ARCOUNT;
	
	
	public HeaderDNS() {
		ANCOUNT = 0;
	}
	
	public char getID() {
		return ID;
	}
	public void setID(char iD) {
		ID = iD;
	}
	public boolean getQR() {
		return QR;
	}
	public void setQR(boolean qR) {
		QR = qR;
	}
	public byte getOpcode() {
		return (byte) (Opcode & 0x0F);
	}
	public void setOpcode(byte opcode) {
		Opcode = opcode;
	}
	public boolean getAA() {
		return AA;
	}
	public void setAA(boolean aA) {
		AA = aA;
	}
	public boolean getTC() {
		return TC;
	}
	public void setTC(boolean tC) {
		TC = tC;
	}
	public boolean getRD() {
		return RD;
	}
	public void setRD(boolean rD) {
		RD = rD;
	}
	public boolean getRA() {
		return RA;
	}
	public void setRA(boolean rA) {
		RA = rA;
	}
	public byte getZ() {
		return (byte) (Z & 0x07);
	}
	public void setZ(byte z) {
		Z = z;
	}
	public byte getRCODE() {
		return (byte) (RCODE & 0x0F);
	}
	public void setRCODE(byte rCODE) {
		RCODE = rCODE;
	}
	public char getQDCOUNT() {
		return QDCOUNT;
	}
	public void setQDCOUNT(char qDCOUNT) {
		QDCOUNT = qDCOUNT;
	}
	public char getANCOUNT() {
		return ANCOUNT;
	}
	public void setANCOUNT(char aNCOUNT) {
		ANCOUNT = aNCOUNT;
	}
	public char getNSCOUNT() {
		return NSCOUNT;
	}
	public void setNSCOUNT(char nSCOUNT) {
		NSCOUNT = nSCOUNT;
	}
	public char getARCOUNT() {
		return ARCOUNT;
	}
	public void setARCOUNT(char aRCOUNT) {
		ARCOUNT = aRCOUNT;
	}
	
}
