package com.csc.fsg.nba.backendadapter.cyberlifeInforce;
/*
 * *******************************************************************************<BR>
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
 * 
 * *******************************************************************************<BR>
 */
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.csc.fs.Message;
import com.csc.fs.Result;
import com.csc.fs.SystemAccess;
import com.csc.fs.svcloc.ServiceLocator;
import com.csc.fsg.nba.backendadapter.NbaBackEndAdapter;
import com.csc.fsg.nba.backendadapter.cyberlife.NbaCyberConstants;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.txlife.TXLife;
import com.tbf.xml.XmlValidationError;
/**
 * @(#)NbaCyberInforceAdapter.java
 * <p>
 * <b>Description:</b>&nbsp; This class acts as the entry point for the CyberLife
 *  backend adapter.  SubmitRequestToHost is the primary method that processes the   
 *   XML transaction to the host and then returns the host response in XML format.     
 * <br>
 * <br>
 * <b>Collaborators:</b>&nbsp; 
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th>
 * <th align=left>Release</th>
 * <th align=left>Description</th>
 * </thead>
 * <tr><td>NBA076</td><td>Version 3</td><td>Initial Development</td></tr>
 * <tr><td>NBA112</td><td>Version 4</td><td>Agent Name and Address</td></tr>
 * <tr><td>NBA103</td><td>Version 4</td><td>Logging</td></tr>
 * <tr><td>ACN012</td><td>Version 4</td><td>Architecture Changes</td></tr>
 * <tr><td>NBA104</td><td>Version 4</td><td>Pending Contract Calculations</td></tr>
 * <tr><td>NBA077</td><td>Version 4</td><td>Reissues and Complex Change Etc.</td></tr>
 * <tr><td>NBA105</td><td>Version 4</td><td>Underwriting Risk.</td></tr>
 * <tr><td>SPR2992</td><td>Version 6</td><td>General Code Cleanup</td></tr>
 * <tr><td>SPR2662</td><td>Version 6</td><td>Poller stops when invalid contract data is present</td></tr>
 * <tr><td>NBA195</td><td>Version 7</td><td>JCA Adapter for DXE Interface to CyberLife</td></tr>
 * <tr><td>SPR3290</td><td>Version 7</td><td>General source code clean up during version 7</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 3
 */
public class NbaCyberInforceAdapter implements NbaBackEndAdapter, NbaCyberInforceConstants, NbaCyberConstants {
	private static com.csc.fsg.nba.foundation.NbaLogger logger = null;
	/**
	 * NbaCyberInforceAdapter constructor.
	 */
	public NbaCyberInforceAdapter() {
		super();
	}
	// NBA195 code deleted
	/**
	 * Main method that handles the method calls to send DXE to the host
	 * @param XML document Contains the transaction request for the host 
	 * @return XML reponse from the host
	 * @exception throws NbaBaseException and java.rmi.RemoteException
	 */
	public NbaTXLife submitRequestToHost(NbaTXLife NbatxLife) throws NbaBaseException {
		// Begin NBA195
		// Get System Access
		SystemAccess sysAccess = (SystemAccess) ServiceLocator.lookup(SystemAccess.SERVICENAME);
		// Prepare Input as to the service
		List list = new ArrayList();
		list.add(NbatxLife);
		// invoke CyberAdapter Service for CyberLifeDXE External System
		Result result = sysAccess.invoke("CyberLifeDXE/CyberInforceAdapter", list);
		NbaTXLife nbaTxlifeResponse = null;
		if (!result.hasErrors()) {
			// if there are no errors, get the response out of result object
			nbaTxlifeResponse = (NbaTXLife) result.getData().get(0);
		} else {
			Message msgs[] = result.getMessages();
			if (msgs != null && msgs.length > 0) {
				List data = msgs[0].getData();
				if (data != null && data.size() > 0) {
					throw new NbaBaseException(data.get(0).toString());
				}
			}
		}
		// End NBA195
		return nbaTxlifeResponse;
	}
	/**
	 * Main method that handles the method calls to send DXE to the host
	 * 
	 * @param XML
	 *            document Contains the transaction request for the host
	 * @return XML reponse from the host
	 * @exception throws
	 *                NbaBaseException and java.rmi.RemoteException
	 */
	public String submitRequestToHost(String xmlDoc) throws NbaBaseException {
		// Begin NBA195
		try {
			NbaTXLife nbaTXLife = new NbaTXLife(xmlDoc);
			return submitRequestToHost(nbaTXLife).toXmlString();
		} catch (Exception exp) {
			throw new NbaBaseException(NbaBaseException.SOURCE_XML);
		}
		// End NBA195
	}
	/**
	 * Return my <code>NbaLogger</code> implementation (e.g. NbaLogService).
	 * 
	 * @return com.csc.fsg.nba.foundation.NbaLogger
	 */
	private static NbaLogger getLogger() {
		if (logger == null) {
			try {
				logger = NbaLogFactory.getLogger(NbaCyberInforceAdapter.class.getName()); //NBA103
			} catch (Exception e) {
				NbaBootLogger.log("NbaCyberInforceAdapter could not get a logger from the factory.");
				e.printStackTrace(System.out);
			}
		}
		return logger;
	} 
	/**
	 * Main method for loading and running test XML
     * @param args java.lang.String[]
	 */
	public static void main(String[] args) throws NbaBaseException, IOException {
		FileReader inputFile = new FileReader("c:\\nba\\dxe\\508loan3.xml");
		//FileWriter outputFile = new FileWriter("c:\\nba\\dxe\\508DXE.txt");
		char charbuf[];
		int chars_read = 0;
		charbuf = new char[64 * 1024];
		// SPR3290 code deleted
		TXLife txLife = null;
		NbaTXLife nbaTxlife = new NbaTXLife();
		NbaTXLife nbaTxlifeResponse = new NbaTXLife();
		TXLife txlifeResponse = new TXLife();
	
		try {
			chars_read = inputFile.read(charbuf, 0, (64 * 1024));
			inputFile.close();
			if (chars_read > 0) {
				String xmlDoc = new String(charbuf, 0, chars_read);
				NbaCyberInforceAdapter testAdapt = new NbaCyberInforceAdapter();
				ByteArrayInputStream inputstream = new ByteArrayInputStream(xmlDoc.getBytes());
				txLife = TXLife.unmarshal(inputstream);
				nbaTxlife.setTXLife(txLife);
				getLogger().logDebug("About to call adapter");
				nbaTxlifeResponse = testAdapt.submitRequestToHost(nbaTxlife);
				txlifeResponse = nbaTxlifeResponse.getTXLife();
				getLogger().logDebug(txlifeResponse); // SPR3290
			}
		} catch (Exception e) {
			//
			// Check for any validation errors.
			//
			if (txLife != null) {
				java.util.Vector v = txLife.getValidationErrors();
				if (v != null) {
					for (int ndx = 0; ndx < v.size(); ndx++) {
						XmlValidationError error = (XmlValidationError) v.get(ndx);
						System.out.print("\tError(" + ndx + "): ");
						if (error != null)
							if (getLogger().isErrorEnabled()) {
								getLogger().logError(error.getErrorMessage());
							} else
								getLogger().logError("A problem occurred retrieving the validation error."); //NBA044
					}
				}
			}
			throw new NbaBaseException(NbaBaseException.BACKEND_ADAPTER_PARSE, e);
		}
	}
	/**
	 * Load and execute XML from a file
	 * @throws NbaBaseException
	 * @throws IOException
	 */
	public void test() throws NbaBaseException, IOException {
			FileReader inputFile = new FileReader("C:\\nbA\\DXE\\SST.xml");
			FileWriter outputFile = new FileWriter("c:\\nba\\dxe\\generatedDXE.txt");
			char charbuf[];
			int chars_read = 0;
			charbuf = new char[64 * 1024];
			String returnRequest;
			TXLife txLife = null;
			NbaTXLife nbaTxlife = new NbaTXLife();
			NbaTXLife nbaTxlifeResponse = new NbaTXLife();
			TXLife txlifeResponse = new TXLife();

			try {
				chars_read = inputFile.read(charbuf, 0, (64 * 1024));
				inputFile.close();
				if (chars_read > 0) {
					String xmlDoc = new String(charbuf, 0, chars_read);
					ByteArrayInputStream inputstream = new ByteArrayInputStream(xmlDoc.getBytes());
					txLife = TXLife.unmarshal(inputstream);
					nbaTxlife.setTXLife(txLife);
					getLogger().logDebug("About to call adapter");
					nbaTxlifeResponse = submitRequestToHost(nbaTxlife);
					txlifeResponse = nbaTxlifeResponse.getTXLife();
					// SPR3290 code deleted
					ByteArrayOutputStream stream = new ByteArrayOutputStream();
					txlifeResponse.marshal(stream);
					returnRequest = stream.toString();
					getLogger().logDebug(returnRequest);
					outputFile.write(returnRequest);
					outputFile.close();
				}
			} catch (Exception e) {
				//
				// Check for any validation errors.
				//
				if (txLife != null) {
					java.util.Vector v = txLife.getValidationErrors();
					if (v != null) {
						for (int ndx = 0; ndx < v.size(); ndx++) {
							XmlValidationError error = (XmlValidationError) v.get(ndx);
							System.out.print("\tError(" + ndx + "): ");
							if (error != null)
								if (getLogger().isErrorEnabled()) {
									getLogger().logError(error.getErrorMessage());
								} else
									getLogger().logError("A problem occurred retrieving the validation error."); //NBA044
						}
					}
				}
				throw new NbaBaseException(NbaBaseException.BACKEND_ADAPTER_PARSE, e);
			}
		}
	// NBA195 code deleted

}
