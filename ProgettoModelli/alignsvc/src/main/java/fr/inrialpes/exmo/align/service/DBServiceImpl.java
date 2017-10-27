/*
 * $Id: DBServiceImpl.java 1831 2013-03-09 18:58:49Z euzenat $
 *
 * Copyright (C) Seungkeun Lee, 2006
 * Copyright (C) INRIA, 2007-2009, 2013
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package fr.inrialpes.exmo.align.service;

import java.lang.ClassNotFoundException;
import java.lang.IllegalAccessException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implement Class
 */
public class DBServiceImpl implements DBService {
    final static Logger logger = LoggerFactory.getLogger( DBServiceImpl.class );

    int id = 0;
    Connection conn = null;
    JdbcConnectionConf conf;
    //To be used in reconnect()
    String driverPrefix = "jdbc:mysql";
    //String driverPrefix = "jdbc:postgresql";
    CacheImpl cache = null;
    
    public DBServiceImpl() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
	Class.forName("com.mysql.jdbc.Driver").newInstance();
	//Class.forName("org.postgresql.Driver").newInstance();
    }

    public DBServiceImpl( String driver, String prefix, String DBPort ) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
	Class.forName(driver).newInstance();
	driverPrefix = prefix;
	conf.setPort(DBPort);
    }

    public void init() {
    }
	 	
    public void connect( String password ) throws SQLException {
    connect( conf.getIPAddress(), conf.getPort(), conf.getUser(), password, conf.getDatabase() );
    }
    
    public void connect( String user, String password ) throws SQLException {
	connect( conf.getIPAddress(), conf.getPort(), user, password, conf.getDatabase() );
    }
    
    public void connect( String port, String user, String password ) throws SQLException {
	connect( conf.getIPAddress(), port, user, password, conf.getDatabase() );
    }
    
    public void connect(String IPAddress, String port, String user, String password ) throws SQLException {
	connect( IPAddress, port, user, password, conf.getDatabase() );
	}

    public void connect(String IPAddress, String port, String user, String password, String database ) throws SQLException {
	conf.setDbpass(password);
	conn = DriverManager.getConnection(driverPrefix+"://"+IPAddress+":"+port+"/"+database, user, password);
	}
    //with "dbpass" given by "connect"
    public Connection reconnect() throws SQLException {
	conn = DriverManager.getConnection(driverPrefix+"://"+conf.getIPAddress()+":"+conf.getPort()+"/"+conf.getDatabase(), conf.getUser(), conf.getDbpass());
	return conn;
    }

    public Connection getConnection() throws SQLException {
	if (conn==null || conn.isClosed())
		return reconnect();
	return conn;
    }

    public void close() {
	try {
	    conn.close();
	} catch (Exception ex) {
	    logger.debug( "IGNORED Closing exception", ex );
	}
    }
    
}
