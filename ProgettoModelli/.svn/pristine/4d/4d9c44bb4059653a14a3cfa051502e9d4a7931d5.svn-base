package alignsvc.src.main.java.fr.inrialpes.exmo.align.service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Parameter class
 */
public class DBMSParameters {


	private String DBHOST;
	private String DBPORT;
	private String DBUSER;
	private String DBPASS;
	private String DBBASE;
	private String DBMS;
	
	public DBMSParameters()throws IOException{
		BufferedReader in = null;
		try{	
			in = new BufferedReader(new FileReader("db.dat"));
			DBHOST = in.readLine();
			DBPORT = in.readLine();
			DBUSER = in.readLine();
			DBPASS = in.readLine();
			DBBASE = in.readLine();
			DBMS = in.readLine();
			
		}finally{
			if(in!=null) in.close();
		}
	}

	/**
	 * Restituisce l'host necessario alla connessione alla base di dati.
	 * 
	 * @return DBHOST
	 */
	public String getDBHOST() {
		return DBHOST;
	}

	/**
	 * Modifica la porta necessaria alla connessione alla base di dati
	 * 
	 * @param dBPORT
	 */
	public void setDBPORT(String dBPORT) {
		DBPORT = dBPORT;
	}

	/**
	 * Restituisce la porta necessaria alla connessione alla base di dati.
	 * 
	 * @return DBPORT
	 */
	public String getDBPORT() {
		return DBPORT;
	}

	/**
	 * Restituisce l'user necessario alla connessione alla base di dati.
	 * 
	 * @return DBUSER
	 */
	public String getDBUSER() {
		return DBUSER;
	}

	/**
	 * Restituisce la password necessaria alla connessione alla base di dati.
	 * 
	 * @return DBPASS
	 */
	public String getDBPASS() {
		return DBPASS;
	}

	/**
	 * Restituisce il nome della base di dati.
	 * 
	 * @return DBBASE
	 */
	public String getDBBASE() {
		return DBBASE;
	}

	/**
	 * Restituisce la base di dati.
	 * 
	 * @return DBMS
	 */
	public String getDBMS() {
		return DBMS;
	}

	/**
	 * Modifica l'host necessario alla connessione alla base di dati
	 * 
	 * @param dBHOST
	 */
	public void setDBHOST(String dBHOST) {
		DBHOST = dBHOST;
	}

	/**
	 * Modifica l'user necessario alla connessione alla base di dati
	 * 
	 * @param dBUSER
	 */
	public void setDBUSER(String dBUSER) {
		DBUSER = dBUSER;
	}

	/**
	 * Modifica la password necessaria alla connessione alla base di dati
	 * 
	 * @param dBPASS
	 */
	public void setDBPASS(String dBPASS) {
		DBPASS = dBPASS;
	}

	/**
	 * Modifica il nome della base di dati.
	 * 
	 * @param dBBASE
	 */
	public void setDBBASE(String dBBASE) {
		DBBASE = dBBASE;
	}

	/**
	 * Modifica il nome del tipo di base di dati.
	 * 
	 * @param dBMS
	 */
	public void setDBMS(String dBMS) {
		DBMS = dBMS;
	}
	
	
	
}
