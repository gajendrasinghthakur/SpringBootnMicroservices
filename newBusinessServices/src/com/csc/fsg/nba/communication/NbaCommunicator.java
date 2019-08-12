package com.csc.fsg.nba.communication;

/*
 * ************************************************************** <BR>
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
 * ************************************************************** <BR>
 */
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;

import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaExceptionType;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.configuration.Provider;
import com.csc.fsg.nba.webservice.client.NbaHooperHolmesWebServiceClient;
import com.csc.fsg.nba.webservice.client.NbaProviderCommServiceProxy;

/**
 * NbaCommunicator provides communication services between nbAccelerator and
 * outside systems.  Services provided by the communication include HTTP, FTP,
 * dialup access (via the creation of a file for processing by third-party software)
 * and Web Services.  It extends the NbaAbsCommunicator class.
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA008</td><td>Version 2</td><td>Requirements Ordering and Receipting</td><tr>
 * <tr><td>NBA044</td><td>Version 3</td><td>Architecture Changes</td></tr>
 * <tr><td>NBA081</td><td>Version 3</td><td>Hooper Holmes</td></tr>
 * <tr><td>NBA038</td><td>Version 3</td><td>Reinsurance</td></tr>
 * <tr><td>ACN012</td><td>Version 4</td><td>Architecture Changes</td></tr>
 * <tr><td>ACN009</td><td>Version 4</td><td>ACORD 401/402 MIB Inquiry and Update Migration</td></tr>
 * <tr><td>NBA103</td><td>Version 4</td><td>Logging</td></tr> 
 * <tr><td>ACN014</td><td>Version 4</td><td>ACORD 121/1122 General Requirement Request Migration</td></tr>
 * <tr><td>SPR3290</td><td>Version 7</td><td>General source code clean up during version 7</td></tr>
 * <tr><td>AXAL3.7.31</td><td>AXA Life Phase 1</td><td>Provider Interface - MIB</td></tr> 
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @see insert reference here (optional - delete if not used)
 * @since New Business Accelerator - Version 2
 */
// ACN009 modified to extend the NbaAbsCommunicator class
public class NbaCommunicator extends NbaAbsCommunicator {
	public final static java.lang.String COMM_HTTP = "HTTP";
	//ACN014 code deleted
	public final static java.lang.String COMM_DIALUP = "MODEM";
	public final static java.lang.String COMM_WEBSERVICE= "WEBSERVICE"; //NBA081
	private int accessMethod = 0;
	public final static int COMM_HTTP_ACCESS = 1;
	private static final String SERVLET_URL = "NbaFileServlet";
	//ACN014 code deleted
	public final static int COMM_DIALUP_ACCESS = 2;
	public final static int COMM_WEBSERVICE_ACCESS = 4; // NBA081 
	public Provider provider = null; //ACN012
	private static NbaLogger logger = null; //NBA044   
	
	/**
	 * NbaProviderCommunicator default constructor.
	 */
	public NbaCommunicator() {
		super();
	}
	/**
	 * Return my <code>NbaLogger</code> implementation (e.g. NbaLogService).
	 * @return com.csc.fsg.nba.foundation.NbaLogger
	 */
	// NBA044 New Method
	private static NbaLogger getLogger() {
		if (logger == null) {
			try {
				logger = NbaLogFactory.getLogger(NbaCommunicator.class.getName());
			} catch (Exception e) {
				NbaBootLogger.log("NbaCommunicator could not get a logger from the factory.");
				e.printStackTrace(System.out);
			}
		}
		return logger;
	}
	/**
	 * Using aNewProvider input, initializes the provider and the access method.
	 * @param aNewProvider the provider to whom the message will be sent
	 */
	//ACN009 new method
	public void initialize(Provider aNewProvider) {
		setProvider(aNewProvider);
		setAccess(aNewProvider.getAccess());
	}
	/**
	 * Parses the <code>NbaConfigProvider</code> object to determine the method
	 * of communication to be used for this provider.
	 * @param aNewProvider the provider to whom the message will be sent
	 */
	//ACN012 CHANGED SIGNATURE
	public NbaCommunicator(Provider aNewProvider) {
		super();
		setProvider(aNewProvider);
		// based on the provider string, find the access method
		//NBA038 code deleted
		setAccess(aNewProvider.getAccess()); //NBA038
	}
	/**
	 * Answers the method of communication for the provider.
	 * @return int
	 */
	protected int getAccessMethod() {
		return accessMethod;
	}
	/**
	 * Answers the provider 
	 * @return NbaConfigProvider
	 */
	//ACN012 CHANGED SIGNATURE
	public Provider getProvider() {
		return provider;
	}
	/**
	 * This method provides a method of posting a Serialized object to a Java servlet and getting object(s)
	 * in return.
	 * @param objs The objects that serve as input to the servlet
	 * @return ObjectInputStream A stream that contains the servlet response. It should be closed by the calling object.
	 * @exception com.csc.fsg.nba.exception.NbaBaseException Application exception to wrap any other exceptions
	 */
	
	public ObjectInputStream postObjectsToServlet(Serializable objs[]) throws NbaBaseException {
		try {
			URL servlet = new URL(NbaConfiguration.getInstance().getFileLocation("url")+SERVLET_URL); // Create a URL for the target servlet
			return postObjectsToServlet(servlet, objs);
		} catch (Exception e) {
			throw new NbaBaseException(NbaBaseException.INVOKE_SERVER, e);
		}
	}
	/**
	 * This method provides a method of posting a Serialized object to a Java servlet and getting object(s)
	 * in return.
	 * @param servlet The URL of the target servlet
	 * @param objs The objects that serve as input to the servlet
	 * @return ObjectInputStream A stream that contains the servlet response. It should be closed by the calling object.
	 * @exception com.csc.fsg.nba.exception.NbaBaseException Application exception to wrap any other exceptions
	 */
	
	public ObjectInputStream postObjectsToServlet(URL servlet, Serializable objs[]) throws NbaBaseException {
		try {
			//SPR3290 deleted code
			URLConnection con = servlet.openConnection();
			con.setDoInput(true);
			con.setDoOutput(true);
			con.setUseCaches(false);
			con.setDefaultUseCaches(false);
			con.setRequestProperty("Content-Type", "application/octet-stream");
			if (NbaUtils.cookie != null) {
				con.setRequestProperty("Cookie", NbaUtils.cookie);
			}
	
			// Write the argument as post data
			ObjectOutputStream out = new ObjectOutputStream(con.getOutputStream());
			int numObjects = objs.length;
			for (int i = 0; i < numObjects; i++) {
				out.writeObject(objs[i]);
			}
	
			out.flush();
			out.close();
	
			return new ObjectInputStream(con.getInputStream());
		} catch (Exception e) {
			throw new NbaBaseException(NbaBaseException.INVOKE_SERVER, e);
		}
	}
	/**
	 * This method processes the dialup transaction for a provider by copying the file
	 * to the folder monitored by the Provider's software.  The message passed to the method
	 * is an ArrayList that contains one or more HashMaps.  Each HashMap should have two keys:
	 * FILENAME which is the name of the file to be created and DATA which is the data to be written
	 * to the file.  This provides the ability to create multiple files which may be needed when 
	 * authorizations must accompany a requirement. 
	 * @param path the path to which the file should be written
	 * @param message an ArrayList containing HashMaps that contain filenames and data
	 * @return Object an Object containing the result of the request
	 * @exception com.csc.fsg.nba.exception.NbaBaseException
	 */
	private Object processDialupTransaction(String path, Object message) throws NbaBaseException {
		try {
			Object retObj = null;
			ArrayList aList = (ArrayList) message;
			for (int i = 0; i < aList.size(); i++) {
				HashMap aMap = (HashMap) aList.get(i);
				String filename = (String) aMap.get("FILENAME");
				String data = (String) aMap.get("DATA");
				Serializable parms[] = { NbaConstants.S_FUNC_FILE_WRITE, path + filename, data };
				ObjectInputStream in = postObjectsToServlet(parms); // Execute the servlet transaction
				retObj = in.readObject(); // Read and use the result value
				if (retObj instanceof NbaBaseException) {
					in.close();
					throw (NbaBaseException) retObj;
				} else if (retObj instanceof Throwable) {
					in.close();
					throw new NbaBaseException(NbaBaseException.INVOKE_SERVER, (Throwable) retObj);
				}
				in.close();
			}
			return (String) retObj;
		} catch (NbaBaseException nbe) {
			throw nbe;
		} catch (Throwable t) {
			throw new NbaBaseException(NbaBaseException.INVOKE_SERVER, t);
		}
	}
	/**
	 * This method handles the submission and response of HTTP POST transactions
	 * to requirement providers.
	 * @param target the URL to which the message should be posted
	 * @param message the message to be sent to the provider
	 * @return Object an Object containing the response from the target server, returned as
	 *              a String
	 * @exception com.csc.fsg.nba.exception.NbaBaseException
	 */
	private Object processHttpTransaction(String target, Object message) throws NbaBaseException {
		// SPR3290 code deleted
		try {
			//	URL url = new URL("http://xmltest.exams4web.com/ParamedRequest.asp");
			URL url = new URL(target);
			HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
			urlConn.setDoInput(true);
			urlConn.setDoOutput(true);
			urlConn.setUseCaches(false);
			urlConn.setRequestMethod("POST");
			urlConn.setRequestProperty("Accept-Language", "en");
			urlConn.setAllowUserInteraction(false);
			urlConn.setRequestProperty("Content-length", String.valueOf(((String)message).length()));
			//urlConn.setRequestProperty("Content-Type", "application/octet-stream");
			//urlConn.setRequestProperty("Content-Type", "text/html");
			urlConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			DataOutputStream out = new DataOutputStream(urlConn.getOutputStream());
			out.writeBytes((String)message);
			out.flush();
			out.close();
			BufferedReader in = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
			StringBuffer response = new StringBuffer();
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
				getLogger().logDebug(inputLine); //NBA044
				if (inputLine == null)
					break;
			}
			in.close();
			return response.toString();
	
		} catch (Exception e) {
			if (e instanceof NbaBaseException) {
				throw new NbaBaseException(e);
			} else {
				throw new NbaBaseException("HTTP Communication Failure", e);
			}
		}
	}
	/**
	 * This method interrogates the accessMethod for the provider and
	 * determines which class method to use to communicate with the
	 * provider.
	 * @param target a path or web address to which the message should be writtern/posted
	 * @param message the message to be sent to the provider
	 * @return Object an Object representing the result of the communication
	 * @exception com.csc.fsg.nba.exception.NbaBaseException
	 */
	public Object processMessage(String target, Object message) throws NbaBaseException {
		switch (accessMethod) {
			case COMM_HTTP_ACCESS :
				{
					return processHttpTransaction(target, message);
				}
			case COMM_DIALUP_ACCESS :
				{
					return processDialupTransaction(target, message);
				}
			//ACN014 code deleted
			case COMM_WEBSERVICE_ACCESS : // NBA081
				{
					return processWebServiceTransaction(target, message);
				}
			default :
				throw new NbaBaseException("Communication Access Method not specified");
		}
	}
	/**
	 * Initializes the means of communicating with a provider.
	 * @param newAccessMethod The access method to be set which is means of communicating with a provider
	 */
	protected void setAccessMethod(int newAccessMethod) {
		accessMethod = newAccessMethod;
	}
	/**
	 * Initializes the provider with whom communication is desired
	 * @param newProvider The provider to be set with whom communication is desired
	 */
	//ACN012 CHANGED SIGNATURE
	public void setProvider(Provider newProvider) {
		provider = newProvider;
	}
	
	/**
	 * This method handles the submission to Web Service transactions.
	 * to both Hooper Holmes and other providers who use a web service.
	 * @param target the URL to which the message should be posted
	 * @param message the message to be sent to the provider
	 * @return Object an Object containing the response from the target server, returned as
	 *              an Element
	 * @exception com.csc.fsg.nba.exception.NbaBaseException
	 */
	// NBA081 - new method
	protected Object processWebServiceTransaction(String target, Object message) throws NbaBaseException {
		//NBA103 - removed code
		try{
			// begin ACN009
			if( getProvider().equals("HPH")) {
				NbaHooperHolmesWebServiceClient obj1 = new NbaHooperHolmesWebServiceClient(provider);
				return obj1.SubmitOrder_((org.w3c.dom.Element)message); //NBA103
			}
			else {
				NbaProviderCommServiceProxy obj1 = new NbaProviderCommServiceProxy();
				obj1.setStringURL(provider.getUrl());
				return obj1.submitProviderRequest((String)message); //NBA103
			}
		//begin NBA103			
		}catch(Throwable t){ 
			NbaBaseException e = new NbaBaseException("Webservice failure.", t, NbaExceptionType.FATAL);
			throw e;
		}
		// end NBA103			
	  	// end ACN009
	}
	
	/**
	 * Sets the access method
	 * @param accessType The access type based on which access method is set
	 */
	//NBA038 New Method
	public void setAccess(String accessType){
		if (accessType.substring(0, 4).equalsIgnoreCase(COMM_HTTP)) {
			setAccessMethod(COMM_HTTP_ACCESS);
		} else if (accessType.equalsIgnoreCase(COMM_DIALUP)) {
			setAccessMethod(COMM_DIALUP_ACCESS);
		//ACN014 code deleted
		} else if (accessType.equalsIgnoreCase(COMM_WEBSERVICE)) {
			setAccessMethod(COMM_WEBSERVICE_ACCESS);
		}
	}

	/* (non-Javadoc)
	 * @see com.csc.fsg.nba.communication.NbaAbsCommunicator#processMessage(java.lang.String, java.lang.Object, com.csc.fsg.nba.vo.NbaUserVO)
	 */
	//AXAL3.7.31 New method
	// dummy implementation - needs to be implemented if required.
	public Object processMessage(String target, Object message, NbaUserVO user) throws NbaBaseException {
		return processMessage(target, message); //added code to call the existing method
		//return null;
	}
}
