package app.serveur_dns.message_dns;

import java.util.ArrayList;
import java.util.List;

public class MessageDNS {
	
	private HeaderDNS header;
	private QuestionDNS question;
	private List<ReponseDNS> reponse;
	
	public MessageDNS() {
		this.header = new HeaderDNS();
		this.question = new QuestionDNS();
		this.reponse = new ArrayList<>();
	}

	public HeaderDNS getHeader() {
		return header;
	}

	public void setHeader(HeaderDNS header) {
		this.header = header;
	}

	public QuestionDNS getQuestion() {
		return question;
	}

	public void setQuestion(QuestionDNS question) {
		this.question = question;
	}

	public List<ReponseDNS> getReponse() {
		return reponse;
	}

	public boolean isQuery() {
		return (this.header.getQR()==false);
	}
	
	public boolean isResponse() {
		return (this.header.getQR()==true);
	}
	
	
}
