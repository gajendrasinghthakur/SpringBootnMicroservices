package com.csc.fsg.nba.webservice.client;

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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaConfigurationConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.vo.NbaConfiguration;

/**
 * NbaTestWebService Class is the Web Service which accepts xml transaction in form of String object and returns
 * response xml from hard drive.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA092</td><td>Version 3</td><td>Architecture Changes</td></tr>
 * <tr><td>SPR2968</td><td>Version 6</td><td>Test web service should determine XML file name dynamically</td></tr>
 * <tr><td>SPR3292</td><td>Version 7</td><td>Canned webservice responses for demo purposes should allow for dynamic data</td></tr>
 * <tr><td>SPR3290</td><td>Version 7</td><td>General source code clean up during version 7</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 3
 */

public class NbaTestWebService {
	private NbaLogger logger;

	/**
	 * This method takes two arguments, first is xmlTransaction for actual WebService in form of String and another is fileName, 
	 * which is the name of response xml file stored in the folder specified under configuration key NbaConfigurationConstants.TEST_WS_FOLDER.
	 * @param element is the xml transaction in form of String
	 * @param fileName is the name of response xml file
	 * @return String
	 * @throws NbaBaseException Exceptions are handled by wrapping them in a new NbaBaseException
	 */
	// SPR2968 - signature changed to throw NbaBaseException
	public String getXmlResponse(String element, String fileName) throws NbaBaseException {
		StringBuffer qualifiedFile = new StringBuffer(); // SPR2968
		FileReader fr = null;
		BufferedReader br = null;
		String line = null;
		StringBuffer lines = new StringBuffer();
		// SPR3290 code deleted

		if (getLogger().isDebugEnabled()) {
			getLogger().logDebug("Request message received by NbaTestWebService: ");	//SPR3292
			getLogger().logDebug(element);
		}

		try {
			// begin SPR2968
		    //Check for a transformation. If one is not found read the response from disk
			qualifiedFile.append(NbaConfiguration.getInstance().getBusinessRulesAttributeValue(NbaConfigurationConstants.TEST_WS_FOLDER));
			qualifiedFile.append(fileName);
			//begin SPR3292
			String response = getTransformedResponse(element, qualifiedFile.toString());
		    if (response != null){
		        logResponse(response);	//SPR3292
		        return response;
		    }
		    qualifiedFile.append(".xml"); 
		    //end SPR3292
			fr = new FileReader(qualifiedFile.toString());
			// end SPR2968
		} // begin SPR2968
		 catch (Throwable t) {
			throw new NbaBaseException("Problem creating file reader in NbaTestWebService", t);
		}
		// end SPR2968

		try {
			br = new BufferedReader(fr);
			while (true) {
				line = br.readLine();
				if (line == null) {
					break;
				} else {
					lines.append(line);
				}
			}
			br.close();
			// SPR2968 code deleted
		} catch (Exception e) {
			throw new NbaBaseException("Problem reading NbaTestWebService Response XML", e); // SPR2968
		}
		String response = lines.toString(); // SPR2968 SPR3292
		logResponse(response);	//SPR3292
		return response;	//SPR3292
	}

	/**
	 * Return my <code>NbaLogger</code> implementation (e.g. NbaLogService).
	 * @return com.csc.fsg.nba.foundation.NbaLogger
	 */
	private NbaLogger getLogger() {
		if (logger == null) {
			try {
				logger = NbaLogFactory.getLogger("NbaTestWebService");
			} catch (Exception e) {
				NbaBootLogger.log("NbaTestWebService could not get a logger from the factory.");
				e.printStackTrace(System.out);
			}
		}
		return logger;
	}
	/**
     * Read the XML transformation to create a response. 
     * @return com.csc.fsg.nba.foundation.NbaLogger
     */
     //SPR3292 New Method
    protected String getTransformedResponse(String element, String fileName) {             
        try {
            Source xslSource = new StreamSource(fileName + ".xsl");
            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer(xslSource);
            InputStream is = new ByteArrayInputStream(element.getBytes());
            OutputStream os = new ByteArrayOutputStream();
            transformer.transform(new StreamSource(is), new StreamResult(os));
            return os.toString();
        } catch (Throwable t) {
            if (getLogger().isDebugEnabled()) {
    			getLogger().logDebug("NbaTestWebService could not find transformation");
    		}
            //do nothing. this means that the file was not found
        }
        return null;
    }
	/**
	 * Log the response
     * @param response
     */
    //SPR3292 New Method
    private void logResponse(String response) {
        if (getLogger().isDebugEnabled()) {
            getLogger().logDebug("Response message returned from NbaTestWebService: ");
            getLogger().logDebug(response);
        }
    }
    
}
