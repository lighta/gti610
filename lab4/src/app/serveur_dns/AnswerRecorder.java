package app.serveur_dns;
import java.io.FileWriter;
import java.io.IOException;

/***
 * Cette classe est utilisé pour enregistrer une réponse
 * dans le fichier texte en provenance d'un Server DNS autre.
 * @author Max
 *
 */

/**
 * @author aj98150
 *
 */
public class AnswerRecorder {
	private String filename = null;

	/**
	 * Construteur
	 * @param filename : Nom du fichier pour sauvegarder les adressesIP et hostname
	 * 
	 */
	public AnswerRecorder(String filename){
		this.filename = filename;
	}
	
	
	
	/**
	 * @return file name
	 */
	public String getFilename() {
		return filename;
	}

	/**
	 * @param hostname
	 * @param adresseIP
	 */
	public void StartRecord(String hostname,String adresseIP){

		try {
			FileWriter writerFichierSource = new FileWriter(filename,true);
			writerFichierSource.write(hostname + " " + adresseIP);
			writerFichierSource.write("\r\n");
			writerFichierSource.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
