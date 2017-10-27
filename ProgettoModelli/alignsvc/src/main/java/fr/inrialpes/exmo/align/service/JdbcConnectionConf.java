package fr.inrialpes.exmo.align.service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * @author vince
 *
 */
public class JdbcConnectionConf {

	private String IPAddress;
	private String user;
	private String dbpass;
	private String port;
	private String database;
	
	
	
	/**
	 * Legge le informazioni necessarie per connetteri alla base di dati dal file "db.dat".
	 * 
	 * @throws IOException
	 */
	JdbcConnectionConf() throws IOException{
		BufferedReader in = null;
		try{	
			in = new BufferedReader(new FileReader("db.dat"));
			IPAddress = in.readLine();
			user = in.readLine();
			dbpass = in.readLine();
			port = in.readLine();
			database = in.readLine();
			
		}finally{
			if(in!=null) in.close();
		}
	}

	/**
	 * Restituisce l'URL della base di dati.
	 * 
	 * @return URL della base di dati.
	 */
	String getIPAddress() {
		return IPAddress;
	}

	/**
	 * Restituisce lo username necessario per connettersi alla base di dati.
	 * 
	 * @return username.
	 */
	String getUser() {
		return user;
	}

	/**
	 * Restituisce la password necessaria per connettersi alla base di dati.
	 * 
	 * @return dbpass.
	 */
	String getDbpass() {
		return dbpass;
	}

	
	public void setDbpass(String dbpass) {
		this.dbpass = dbpass;
	}

	/**
	 * Restituisce la porta necessaria per connettersi alla base di dati.
	 * 
	 * @return port.
	 */
	String getPort() {
		return port;
	}

	
	public void setPort(String port) {
		this.port = port;
	}

	/**
	 * Restituisce il database necessario per connettersi alla base di dati.
	 * 
	 * @return database.
	 */
	String getDatabase() {
		return database;
	}
	
	
	
}
