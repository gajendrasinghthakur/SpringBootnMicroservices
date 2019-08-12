package com.csc.fsg.nba.backendadapter.cyberlife;

/*
 * **************************************************************************<BR>
 * This program contains trade secrets and confidential information which<BR>
 * are proprietary to CSC Financial Services Group®.  The use,<BR>
 * reproduction, distribution or disclosure of this program, in whole or in<BR>
 * part, without the express written permission of CSC Financial Services<BR>
 * Group is prohibited.  This program is also an unpublished work protected<BR>
 * under the copyright laws of the United States of America and other<BR>
 * countries.  If this program becomes published, the following notice shall<BR>
 * apply:
 *     Property of Computer Sciences Corporation.<BR>
 *     Confidential. Not for publication.<BR>
 *     Copyright (c) 2002-2008 Computer Sciences Corporation. All Rights Reserved.<BR>
 * **************************************************************************<BR>
 */
import java.util.Hashtable;

import javax.naming.ConfigurationException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.resource.ResourceException;
import javax.resource.cci.Connection;
import javax.resource.cci.ConnectionFactory;
import javax.resource.cci.ConnectionSpec;
import javax.resource.cci.Interaction;
import javax.resource.cci.InteractionSpec;
import javax.resource.cci.MappedRecord;

import com.csc.fs.ErrorHandler;
import com.csc.fs.logging.LogHandler;
import com.csc.fs.ra.SimpleConnectionSpecImpl;
import com.csc.fs.ra.SimpleInteractionSpecImpl;
import com.csc.fs.ra.SimpleMappedRecord;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaExceptionType;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.vo.NbaConfiguration;

/**
 * Provide a wrapper for the Native Call header to NbaComm.dll
 * 
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>SPR2662</td><td>Version 6</td><td>Poller stops when invalid contract data is present</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 1
 */
public class NbaCommWrapper {
	private static com.csc.fsg.nba.foundation.NbaLogger logger = null;

	static {
		try {
			System.loadLibrary("NbaComm");
		} catch (UnsatisfiedLinkError e) {

		}

	}

	// NBA076 changed protected to public
	public java.lang.String security;

	public java.lang.String hostip;

	public java.lang.String port;

	public java.lang.String eventid;

	public java.lang.String request;

	public java.lang.String userid;

	/**
	 * NbaCommWrapper constructor.
	 */
	public NbaCommWrapper() {
		super();
	}

	/**
	 * Native call header to NbaComm.dll
	 * 
	 * @param userid
	 *            UserId contained in the configuration file
	 * @param hostip
	 *            Ip address contained in the configuration file
	 * @param port
	 *            Port contained in the configuration file
	 * @param security
	 *            Contains whether the security is turned ON or OFF on the host
	 * @param request
	 *            Contains the request for the host
	 * @param eventId
	 *            Contains the event type for the host
	 * @return response from the host
	 */
	public static native String Call(String userid, String hostip, String port,
			String security, String request, String eventid);

	/**
	 * Calls the native method in NbaComm.dll
	 * 
	 * @param request
	 *            DXE request to the host
	 * @return response containing DXE to back from the host
	 * @exception throws
	 *                NbaBaseException
	 */

	//NBA076 changed protected to public
	public String callCommWrapper(String request) throws NbaBaseException {
		String response = " ";
		try {
			getLogger().logDebug(request);
			
			String access = getJNDI("CyberLifeAccess");
			if (access != null && !access.equals("NATIVE")) {
				// split the access identifier into provider URL and JNDI Name
				String providerURL = "";
				String jndiName = "";
				int pos = access.indexOf("$$"); 
				if(pos > -1){
					providerURL = access.substring(0, pos);
					jndiName = access.substring(pos + "$$".length());
				}
				
				// locate and the connection/connection factory for the given JNDI name
				Connection conn = getConnection(providerURL, jndiName);
				if(conn != null){
					Interaction ix = null;
					try {
						ix = conn.createInteraction();
					} catch (ResourceException e) {
						throw new NbaBaseException(
								NbaBaseException.BACKEND_ADAPTER_COMM_LAYER, e,
								NbaExceptionType.FATAL);
					}
					InteractionSpec iSpec = new SimpleInteractionSpecImpl();
					try {
						// construct the record
						MappedRecord record = new SimpleMappedRecord();
						record.put("REQUEST", request);
						record.put("SECURITY", security);
						record.put("USERID", userid);
						record.put("EVENTID", eventid);
						record.put("HOST", hostip);
						record.put("PORT", port);
						MappedRecord responserec = new SimpleMappedRecord();
						ix.execute(iSpec, record, responserec);
						
						if(responserec.containsKey("EXCEPTION")){
							throw new NbaBaseException(
									(String)responserec.get("AREA"), (Exception)responserec.get("EXCEPTION"),
									NbaExceptionType.FATAL);
						} else {
							response = (String)responserec.get("RESULT");
						}
					} catch (ResourceException e) {
						throw new NbaBaseException(
								NbaBaseException.BACKEND_ADAPTER_COMM_LAYER, e,
								NbaExceptionType.FATAL);
					} finally {
						try {
							ix.close();
							conn.close();
						} catch (ResourceException e) {
						}
					}
				} else {
					throw new NbaBaseException(
							NbaBaseException.BACKEND_ADAPTER_COMM_LAYER, new Exception("Unable to locate CL DXE Resource Adapter"),
							NbaExceptionType.FATAL);
				}
			} else {
				response = Call(userid, hostip, port, security, request, eventid);
			}
			getLogger().logDebug(response);
		} catch (Exception e) {
			throw new NbaBaseException(
					NbaBaseException.BACKEND_ADAPTER_COMM_LAYER, e,
					NbaExceptionType.FATAL); //SPR2662
		}

		return (response);

	}
	
	public Object lookup(String providerURL, String jndiName){
		Object obj = null;
		try {
			Hashtable env = new Hashtable();
			env.put(Context.PROVIDER_URL, providerURL);
			Context initCtx = new InitialContext(env);
			try {
				obj = initCtx.lookup(jndiName);
			} catch (NameNotFoundException ignore) {
			} catch (ConfigurationException ignore) {
			}
			if (obj == null) {
				if (!jndiName.startsWith("java:comp/env/")) {
					try {
						obj = initCtx.lookup("java:comp/env/" + jndiName);
					} catch (NameNotFoundException ignore) {
					} catch (ConfigurationException ignore) {
					}
				}
			}
		} catch (Exception e) {
		}
		return obj;
	}
	
	public ConnectionFactory getConnectionFactory(String providerURL, String jndiName) {
		ConnectionFactory cf = null;
		ConnectionFactory connectionFactoryImpl = (ConnectionFactory) lookup(providerURL, jndiName);
		if (connectionFactoryImpl == null) {
			ErrorHandler.process(
				getClass(),
				"No ConnectionFactory bound to jndiname: " + jndiName);
		} else {
			cf = connectionFactoryImpl;
		}

		return cf;
	}
	
	protected Connection getConnection(String providerURL, String jndiName) {
		Connection conn = null;
		ConnectionSpec connectionSpec = new SimpleConnectionSpecImpl();
		try {
			ConnectionFactory cf = getConnectionFactory(providerURL, jndiName);
			conn = cf.getConnection(connectionSpec);
		} catch (ResourceException re){
			LogHandler.Factory.LogError(this, "Exception occured while obtaining Connection Factory for [" + providerURL + "] [" + jndiName + "] [" + (re.getLinkedException() != null ? re.getLinkedException().getMessage() : re.getMessage()) + "]");
		} catch (Exception e) {
			LogHandler.Factory.LogError(this, "Exception occured while obtaining Connection Factory for [" + providerURL + "] [" + jndiName + "] [" + e.getMessage() + "]");
		}
		return conn;
	}

	/**
	 * Determine if the resource adapter is used.
	 * 
	 * @param function -
	 *            the workflow function
	 * @return true if: (1) there is a Attribute in the BusinessRules section of
	 *         the congfiguration which matches the function and its value is
	 *         not "NATIVE"; Otherwise return false
	 */
	protected String getJNDI(String function) {
		try {
			String ra = NbaConfiguration.getInstance()
					.getBusinessRulesAttributeValue(function);
			return ra;
		} catch (NbaBaseException e) { // No entry
		}
		return "NATIVE";
	}

	/**
	 * Return my <code>NbaLogger</code> implementation (e.g. NbaLogService).
	 * 
	 * @return com.csc.fsg.nba.foundation.NbaLogger
	 */
	private static NbaLogger getLogger() {
		if (logger == null) {
			try {
				logger = NbaLogFactory
						.getLogger(NbaCommWrapper.class.getName());
			} catch (Exception e) {
				NbaBootLogger
						.log("NbaCommWrapper could not get a logger from the factory.");
				e.printStackTrace(System.out);
			}
		}
		return logger;
	}
}
