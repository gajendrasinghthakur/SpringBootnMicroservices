package com.csc.fsg.nba.process.cashiering;
/*
 * *******************************************************************************<BR>
 * This program contains trade secrets and confidential information which are
 * proprietary to CSC Financial Services Group�.  The use, reproduction,
 * distribution or disclosure of this program, in whole or in part, without
 * the express written permission of CSC Financial Services Group is prohibited.
 * This program is also an unpublished work protected under the copyright laws
 * of the United States of America and other countries.
 *
 * If this program becomes published, the following notice shall apply:
 *    Property of Computer Sciences Corporation.<BR>
 *    Confidential. Not for publication.<BR>
 *    Copyright (c) 2002-2008 Computer Sciences Corporation. All Rights Reserved.<BR>
 * *******************************************************************************<BR>
 */
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;

import javax.sql.DataSource;

import com.csc.fsg.nba.exception.NbaDataAccessException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaTableAccessConstants;
import com.csc.fsg.nba.foundation.NbaTableConstants;
/**
 * This class controls the connections and other database related functionality
 * for the nbA cashiering and control tables. Code from the <code>NbaCashieringTable</code>
 * class has been moved to this class to enhance reusability. 
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA009</td><td>Version 2</td><td>Cashiering</td></tr>
 * <tr><td>NBA027</td><td>Version 3</td><td>Performance Tuning</td></tr>
 * <tr><td>NBA044</td><td>Version 3</td><td>Architectural changes</td></tr>
 * <tr><td>NBA093</td><td>Version 3</td><td>Upgrade to ACORD 2.8</td></tr>
 * <tr><td>SPR1808</td><td>Version 4</td><td>Create new database NBAAUXILIARY</td></tr>
 * <tr><td>ACN012</td><td>Version 4</td><td>Architecture Changes</td></tr>
 * <tr><td>APSL634</td><td>AXA Life phase 1</td><td>PERF - Database connection leak in Cashiering Workbench</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 3
 */
//SPR1808 changed it to be an abstract class
public abstract class NbaTableHelper implements NbaTableConstants,NbaTableAccessConstants {
	protected final static String TIME_FORMAT = "yyyy-MM-dd-HH.mm.ss";
	protected final static String SQLTIME_FORMAT = "YYYY-MM-DD-HH24.MI.SS";
	protected final static String SQL_ERROR = "An SQL Error occurred.";
	protected final static String SQL_SERVER_FORMAT = "yyyy-MM-dd HH:mm:ss";	//NBA093
	
	private static NbaLogger logger;
	
	protected Connection conn = null;  //PERF-APSL634
	protected Statement stmt = null;   //PERF-APSL634
	private boolean inTransaction = false;

	private String user = null;
	private String password = null;
	protected int dbType = 0;	//NBA093
	protected final static int ORACLE = 1;	//NBA093
	protected final static int SQL_SERVER = 2;	//NBA093
	/**
	 * Constructor for NbaTableHelper.
	 */
	public NbaTableHelper() {
		super();
	}
	/**
	 * Executes an Delete query on a new database connection.
	 * @param query Delete query to be executed
	 * @return int Number of rows affected
	 */
	public int executeDelete(String query) throws NbaDataAccessException {

		if (getLogger().isDebugEnabled()) { // NBA027
			getLogger().logDebug("executing SQL Delete: " + query);
		} // NBA027

		try {
			int rc = getStatement().executeUpdate(query);
			close();
			return (rc);
		} catch (SQLException se) {
			close();
			getLogger().logError("SQLException thrown executing SQL Delete: " + query);
			getLogger().logError("SQLException message: " + se.getMessage());	//NBA093
			throw new NbaDataAccessException(SQL_ERROR, se);
		}
	}
	/**
	 * Executes an Insert query on a new database connection.
	 * @param query Insert query to be executed
	 * @return int Number of rows affected
	 */
	public int executeInsert(String query) throws NbaDataAccessException {

		if (getLogger().isDebugEnabled()) { // NBA027
			getLogger().logDebug("executing SQL Insert: " + query);
		} // NBA027

		try {
			int rc = getStatement().executeUpdate(query);
			close();
			return (rc);
		} catch (SQLException se) {
			close();
			getLogger().logError("SQLException thrown executing SQL Insert: " + query);
			getLogger().logError("SQLException message: " + se.getMessage());	//NBA093
			throw new NbaDataAccessException(SQL_ERROR, se);
		}
	}
	/**
	 * Executes a Select query on a new database connection.
	 * @param query Select query to be executed
	 * @return java.sql.ResultSet Results of the query
	 */
	public ResultSet executeQuery(String query) throws NbaDataAccessException {

		if (getLogger().isDebugEnabled()) { // NBA027
			getLogger().logDebug("executing SQL query: " + query);
		} // NBA027

		try {
			ResultSet rs = getStatement().executeQuery(query);
			return (rs);
		} catch (SQLException se) {
			close();
			getLogger().logError("SQLException thrown executing SQL query: " + query);
			throw new NbaDataAccessException(SQL_ERROR, se);
		}
	}
	/**
	 * Executes an Update query on a new database connection.
	 * @param query Update query to be executed
	 * @return int Number of rows affected
	 */
	public int executeUpdate(String query) throws NbaDataAccessException {

		if (getLogger().isDebugEnabled()) { // NBA027
			getLogger().logDebug("executing SQL Update: " + query);
		} // NBA027

		try {
			int rc = getStatement().executeUpdate(query);
			close();
			return (rc);
		} catch (SQLException se) {
			close();
			getLogger().logError("SQLException thrown executing SQL Update: " + query);
			getLogger().logError("SQLException message: " + se.getMessage());	//NBA093
			throw new NbaDataAccessException(SQL_ERROR, se);
		}
	}
	/**
	 * Format a SQL update value for a long value.
	 * @param columnName java.lang.String
	 * @param value long
	 * @return java.lang.String
	 */
	public static String formatSQLUpdateValue(String columnName, long value) {

		return (columnName + " = " + Long.toString(value));
	}
	/**
	 * Format a SQL update value for a string value.
	 * @param columnName Name of the column being updated.
	 * @param value Value to update the column.
	 * @return java.lang.String
	 */
	public static String formatSQLUpdateValue(String columnName, String value) {

		if (value != null) {
			return (columnName + " = '" + value + "'");
		} else {
			return (columnName + " = null");
		}
	}
	/**
	 * Format an SQL update value for a BigDecimal value.
	 * @param columnName Name of the column being updated.
	 * @param value Value to update the column.
	 * @return java.lang.String
	 */
	public static String formatSQLUpdateValue(String columnName, java.math.BigDecimal value) {

		if (value != null) {
			return (columnName + " = " + value.toString());
		} else {
			return (columnName + " = null");
		}
	}
	/**
	 * Format a SQL update value for a Date value.
	 * @param columnName Name of the column being updated.
	 * @param value Value to update the column.
	 * @return java.lang.String
	 */
	public String formatSQLUpdateValue(String columnName, java.util.Date value) { //NBA093

		if (value != null) {
			return (columnName + " = " + formatSQLValue(value, getDbType())); //NBA093
		} else {
			return (columnName + " = null");
		}
	}
	/**
	 * Format an SQL update value for a boolean value.
	 * @param columnName Name of the column being updated.
	 * @param value Value to update the column.
	 * @return java.lang.String
	 */
	public static String formatSQLUpdateValue(String columnName, boolean value) {

		if (value) {
			return (formatSQLUpdateValue(columnName, NbaConstants.YES_VALUE));
		} else {
			return (formatSQLUpdateValue(columnName, NbaConstants.NO_VALUE));
		}
	}
	/**
	 * Format a long value appropriately for a SQL statement.
	 * @param value long
	 * @return java.lang.String
	 */
	public static String formatSQLValue(long value) {

		return (Long.toString(value));
	}
	/**
	 * Format a string value appropriately for a SQL statement.
	 * @param value java.lang.String
	 * @return java.lang.String
	 */
	public static String formatSQLValue(String value) {

		if (value != null) {
			return ("'" + value + "'");
		} else {
			return ("null");
		}
	}
	/**
	 * Format a BigDecimal value appropriately for a SQL statement.
	 * @param value java.math.BigDecimal
	 * @return java.lang.String
	 */
	public static String formatSQLValue(java.math.BigDecimal value) {

		if (value != null) {
			return (value.toString());
		} else {
			return ("null");
		}
	}
	/**
	 * Format a date value appropriately for a SQL statement.
	 * @param value java.util.Date
	 * @return java.lang.String
	 */
	public String formatSQLValue(java.util.Date value, int dbType) {	//NBA093
		//begin NBA093
		if (value != null) {
			if (dbType == ORACLE) {
				SimpleDateFormat dateFormat = new SimpleDateFormat(TIME_FORMAT);
				return ("TO_DATE('" + dateFormat.format(value) + "','" + SQLTIME_FORMAT + "')");
			} else if (dbType == SQL_SERVER) {
				SimpleDateFormat dateFormat = new SimpleDateFormat(SQL_SERVER_FORMAT);
				return "'" + dateFormat.format(value) + "'";
			}
		}
		return ("null");
		//end NBA093
	}
	/**
	 * Format a boolean value appropriately for a SQL statement.
	 * @param value boolean
	 * @return java.lang.String
	 */
	public static String formatSQLValue(boolean value) {

		if (value) {
			return (formatSQLValue(NbaConstants.YES_VALUE));
		} else {
			return (formatSQLValue(NbaConstants.NO_VALUE));
		}
	}
	/**
	 * Format a SQL Where criteria for a long value.
	 * @param columnName java.lang.String
	 * @param value long
	 * @return java.lang.String
	 */
	public static String formatSQLWhereCriterion(String columnName, long value) {

		return (columnName + " = " + Long.toString(value));
	}
	/**
	 * Format a SQL Where criteria for a string value.
	 * @param columnName Name of the column being updated.
	 * @param value Value to update the column.
	 * @return java.lang.String
	 */
	public static String formatSQLWhereCriterion(String columnName, String value) {

		if (value != null) {
			return (columnName + " = '" + value + "'");
		} else {
			return (columnName + " is null");
		}
	}
	/**
	 * Format a SQL Where criteria for a BigDecimal value.
	 * @param columnName Name of the column being updated.
	 * @param value Value to update the column.
	 * @return java.lang.String
	 */
	public static String formatSQLWhereCriterion(String columnName, java.math.BigDecimal value) {

		if (value != null) {
			return (columnName + " = " + value.toString());
		} else {
			return (columnName + " is null");
		}
	}
	/**
	 * Format a SQL Where criteria for a Date value.
	 * @param columnName Name of the column being updated.
	 * @param value Value to update the column.
	 * @return java.lang.String
	 */
	public String formatSQLWhereCriterion(String columnName, java.util.Date value) {	//NBA093

		if (value != null) {
			return (columnName + " = " + formatSQLValue(value, getDbType()));	//NBA093
		} else {
			return (columnName + " is null");
		}
	}
	/**
	 * Format a SQL Where criteria for a boolean value.
	 * @param columnName Name of the column being updated.
	 * @param value Value to update the column.
	 * @return java.lang.String
	 */
	public static String formatSQLWhereCriterion(String columnName, boolean value) {

		if (value) {
			return (formatSQLWhereCriterion(columnName, NbaConstants.YES_VALUE));
		} else {
			return (formatSQLWhereCriterion(columnName, NbaConstants.NO_VALUE));
		}
	}
	/**
	* Start a transaction for upcoming SQL commands.
	*/
	public void startTransaction() throws NbaDataAccessException {

		if (inTransaction) {
			throw new NbaDataAccessException("Already in a transaction, cannot start a new one.");
		}

		int retries = 0;
		do {
			close();
			try {
				conn = getDataSource().getConnection(getUser(), getPassword()); //SPR1808
				conn.setAutoCommit(false);
				inTransaction = true;
				return;
			} catch (SQLException sce) {
				if (retries >= 5) {
					throw new NbaDataAccessException("Unable to start a transaction.", sce);
				}
				retries++;
			}
		} while (retries < 5);
	}
	/**
	 * Convert a String database value to a boolean value.
	 * @param value String value to be converted
	 * @return boolean
	 */
	public static boolean stringToBoolean(String value) {

		if ((value != null) && (value.trim().equals(NbaConstants.YES_VALUE))) {
			return true;
		} else {
			return false;
		}
	}
	/**
	* Undo all changes made during this transaction.
	*/
	public void rollbackTransaction() throws NbaDataAccessException {

		if (inTransaction) {
			try {
				conn.rollback();
				// must set transaction indicator to false before calling close
				inTransaction = false;
				close();
			} catch (SQLException se) {
				throw new NbaDataAccessException("A failure occurred trying to rollback the transaction.", se);
			}
		} else {
			throw new NbaDataAccessException("Not in a transaction, nothing to rollback.");
		}
	}
	/**
 * Returns the user identification for the Cashiering data source.
 * @return java.lang.String
 */
public String getUser() {
	return user;
}
/**
 * Returns the password for the Cashiering data source.
 * @return java.lang.String
 */
public String getPassword() {
	return password;
}
/**
 * Answers a <code>Statement</code> by first obtaining an available connection from
 * the DataSource and then creating the new statement.
 * <p>If an SQLException is received (connection timeout), it retries the process
 * up to 5 times before conceeding failure and throwing an NbaDataAccessException.
 * @return java.sql.Statement
 */
public Statement getStatement() throws NbaDataAccessException {

	/*	conn = ds.getConnection(getUser(), getPassword());
		Statement temp = conn.createStatement();
		temp.execute("Select count(*) from NBA_PLANS");
		return temp;*/
	// if you want to implement catching StaleConnectionException, uncomment the following

	boolean retry = false;
	int retries = 0;
	do {
		try {
			retry = false;
			// if currently in a transaction, use the existing connection
			if (!inTransaction) {
				conn = getDataSource().getConnection(getUser(), getPassword()); //SPR1808
			}
			stmt = conn.createStatement();
			return stmt;
		} catch (SQLException sce) {
			if (retries < 5) {
				retry = true;
				close();
			} else {
				retry = false;
			}
			retries++;
		}
	} while (retry);
	throw new NbaDataAccessException("Unable to establish a database connection");
}


/**
 * Answers a <code>PreparedStatement</code> by first obtaining an available connection from
 * the DataSource and then preparing a new statement.
 * <p>If an SQLException is received (connection timeout), it retries the process
 * up to 5 times before conceeding failure and throwing an NbaDataAccessException.
 * @return java.sql.Statement
 */
//New Method NBA069
public PreparedStatement getPreparedStatement(String query) throws NbaDataAccessException {

	boolean retry = false;
	int retries = 0;
	do {
		try {
			retry = false;
			// if currently in a transaction, use the existing connection
			if (!inTransaction) {
				conn = getDataSource().getConnection(getUser(), getPassword()); //SPR1808
			}
			stmt = conn.prepareStatement(query);
			return (PreparedStatement)stmt;
		} catch (SQLException sce) {
			if (retries < 5) {
				retry = true;
				close();
			} else {
				retry = false;
			}
			retries++;
		}
	} while (retry);
	throw new NbaDataAccessException("Unable to establish a database connection");
}


	/**
	* Commit the current transaction and release the connection.
	*/
	public void commitTransaction() throws NbaDataAccessException {

		if (inTransaction) {
			try {
				conn.commit();
				// must set transaction indicator to false before calling close
				inTransaction = false;
				close();
			} catch (SQLException se) {
				getLogger().logError("SQLException message: " + se.getMessage());	//NBA093
				throw new NbaDataAccessException("A failure occurred trying to commit the transaction.", se);				
			}
		} else {
			throw new NbaDataAccessException("Not in a transaction, nothing to commit.");
		}
	}
/**
 * This method closes all objects associated with a connection -
 * the result set, the statement and the connection.  This frees the
 * connection for use by another service.
 * <p>This method should be invoked everytime the <code>getStatement</code> 
 * method is used to obtain a connection.
 */
public void close() throws NbaDataAccessException {
	try {
		if (stmt != null) {
			stmt.close();	//NbaConnectionFactory.close(stmt);
			stmt = null;
		}
		// do not close connection if in a transaction
		if (!inTransaction) {
			if (conn != null) {
				conn.close();	//NbaConnectionFactory.close(conn);
				conn = null;
			}
		}
	} catch (SQLException se) {
		throw new NbaDataAccessException(SQL_ERROR, se);
	}
}
/**
 * Return my <code>NbaLogger</code> implementation (e.g. NbaLogService).
 * @return com.csc.fsg.nba.foundation.NbaLogger
 */
protected static NbaLogger getLogger() {
	if (logger == null) {
		try {
			logger = NbaLogFactory.getLogger(NbaTableHelper.class.getName());
		} catch (Exception e) {
			NbaBootLogger.log("NbaTableHelper could not get a logger from the factory.");
			e.printStackTrace(System.out);
		}
	}
	return logger;
}
	/**
	 * Sets the password.
	 * @param password The password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * Sets the user.
	 * @param user The user to set
	 */
	public void setUser(String user) {
		this.user = user;
	}

	/**
	 * Returns the ds. Implement this method to return a valid datasource
	 * @return DataSource
	 */
	//SPR1808 make it an abstract method
	public abstract DataSource getDataSource(); 
	//SPR1808 code deleted
	/**
	 * Returns the dbType.
	 * @return int
	 */
	//NBA093 new method
	public int getDbType() {
		return dbType;
	}

	/**
	 * Sets the dbType.
	 * @param dbType The dbType to set
	 */
	//NBA093 new method
	public void setDbType(int dbType) {
		this.dbType = dbType;
	}
	// New Method NBLXA-1908
	public static String formatSQLWhereNotCriterion(String columnName, String value) {

		if (value != null) {
			return (columnName + " <> '" + value + "'");
		} else {
			return (columnName + " is not null");
		}
	}
}
