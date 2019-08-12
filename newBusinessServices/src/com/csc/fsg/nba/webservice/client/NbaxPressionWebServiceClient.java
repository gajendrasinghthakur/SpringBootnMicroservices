package com.csc.fsg.nba.webservice.client;
/* 
 * *******************************************************************************<BR>
 * This program contains trade secrets and confidential information which<BR>
 * are proprietary to CSC Financial Services Group�.  The use,<BR>
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
import java.net.MalformedURLException;
import java.net.URL;
import org.apache.soap.SOAPException;
import com.csc.fsg.nba.correspondence.NbaxPressionAdapter;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaWebClientFaultException;
import com.csc.fsg.nba.exception.NbaWebServerFaultException;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.txlife.UserLoginNameAndUserPswd;
/**
 * NbaxPressionWebServiceClient is the warpper class on actual client class for the xPression WebServices.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA129</td><td>Version 5</td><td>xPression Correspondence</td></tr>
 * <tr><td>SPR2968</td><td>Version 6</td><td>Test web service should determine XML file name dynamically</td></tr>
 * <tr><td>SPR3337</td><td>Version 7</td><td>PDF data for letters is not stored in the workflow system</td></tr>
 * <tr><td>SPR3290</td><td>Version 7</td><td>General source code clean up during version 7</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 5
 */
public class NbaxPressionWebServiceClient extends NbaWebServiceAdapterBase {
	protected final static String CATEGORY_ID = "ContractPrint";
	protected final static String PDF_RETURN = "PDF Return to Application";
	/**
	 * Constructor for NbaxPressionWebServiceClient.
	 */
	public NbaxPressionWebServiceClient() {
		super();
	}
	/**
	 * Takes six parameters and calls the xPression proxy Client classes . After instantiating the proxy client 
	 * this method passes the xpression user, password, transformation/batch type and the customer data into the 
	 * client and gets the soap response back from WebService.
	 * @param user xPression user id
	 * @param password xPression password
	 * @param categoryLetter Name of the xPression category of letter name
	 * @param type xPression transformation or batch named defined in the xPressionAdapter.properties file
	 * @param xml Customer data xml 
	 * @param fileName Name of the webservice function id
	 * @return Object response returned from WebService
	 */
	 //SPR3337 changed method signature
	public Object invokeCorrespondenceWebService(String user, String password, String categoryLetter, String type, String xml, String fileName)
		throws NbaBaseException {
		Object response = null;	//SPR3337
		try {
			if (NbaxPressionAdapter.CATEGORY_REQUEST.equalsIgnoreCase(fileName)
				|| NbaxPressionAdapter.LETTER_REQUEST.equalsIgnoreCase(fileName)
				|| NbaxPressionAdapter.VARIABLE_REQUEST.equalsIgnoreCase(fileName)) {
				response = invokexPressionDocPrintWebService(user, password, categoryLetter, type, fileName);
			} else if (
				NbaxPressionAdapter.BATCH_PRINT_REQUEST.equalsIgnoreCase(fileName) || NbaxPressionAdapter.PDF_REQUEST.equalsIgnoreCase(fileName)) {
				response = invokexPressionRequestWebService(user, password, categoryLetter, type, xml, fileName);
			} else
				throw new NbaBaseException("Invalid Correspondence Type Requested");
		} catch (Exception e) {
			throw new NbaBaseException(e);
		}
		return response;
	}
	/**
	 * Marhall an NbaTXLife to a DOM Element. Invoke the Nba Contract Print Extract Web Service Client to 
	 * store Contract Print Extracts. Unmarshall the DOM Element response into an NbaTXLife. 
	 * @param nbATxLife An instance of <code>NbaTXLife</code>
	 * @param object
	 * @return NbaTXLife A <code>NbaTXLife</code> object containing the response from the webservice. 
	 */
	public NbaTXLife invokeWebService(NbaTXLife nbATxLife) throws NbaWebClientFaultException, NbaWebServerFaultException { // SPR2968
		NbaTXLife response = null;
		NbaxPressionRequestProxy proxyClient = null;
		try {
			URL url = new URL(getWsdlUrl());
			proxyClient = new NbaxPressionRequestProxy(); //object of proxy client generated by WSAD
			proxyClient.setEndPoint(url);
			String password =
				nbATxLife
					.getTXLife()
					.getUserAuthRequestAndTXLifeRequest()
					.getUserAuthRequest()
					.getUserLoginNameAndUserPswdOrUserSessionKey()
					.getUserLoginNameAndUserPswd()
					.getUserPswd()
					.getPswdOrCryptPswd()
					.getPswd();
			String userId =
				nbATxLife
					.getTXLife()
					.getUserAuthRequestAndTXLifeRequest()
					.getUserAuthRequest()
					.getUserLoginNameAndUserPswdOrUserSessionKey()
					.getUserLoginNameAndUserPswd()
					.getUserLoginName();
			//wipe out the user name and password before writing out the extract
			UserLoginNameAndUserPswd userPswd =
				nbATxLife
					.getTXLife()
					.getUserAuthRequestAndTXLifeRequest()
					.getUserAuthRequest()
					.getUserLoginNameAndUserPswdOrUserSessionKey()
					.getUserLoginNameAndUserPswd();
			userPswd.setUserLoginName(new String());
			userPswd.getUserPswd().getPswdOrCryptPswd().setPswd(new String());
			nbATxLife
				.getTXLife()
				.getUserAuthRequestAndTXLifeRequest()
				.getUserAuthRequest()
				.getUserLoginNameAndUserPswdOrUserSessionKey()
				.setUserLoginNameAndUserPswd(userPswd);
			String batchName = NbaConfiguration.getInstance().getBatchPrint(CATEGORY_ID).getBatch();
			proxyClient.postForBatch(batchName, userId, password, nbATxLife.toXmlString()); // SPR3290
			response = new NbaTXLife(nbATxLife);
		} catch (MalformedURLException e) {
			throw new NbaWebServerFaultException("Configuration error: Malformed URL", e);
		} catch (SOAPException e) {
			throw new NbaWebServerFaultException("SOAP Exception", e);
		} catch (Exception e) {
			throw new NbaWebServerFaultException("Exception", e);
		}
		return response;
	}
	/**
	 * Takes five parameters and calls the xPression proxy Client class for DocPrint web service. After instantiating the proxy client 
	 * this method passes the xpression user, password category/Letter name, transformation/batch type and the file name 
	 * into the client and gets the soap response back from WebService.
	 * @param user xPression user id
	 * @param password xPression password
	 * @param categoryLetter Name of the xPression category of letter name
	 * @param type xPression transformation or batch named defined in the xPressionAdapter.properties file
	 * @param fileName Name of the webservice function id
	 * @return String response returned from WebService
	 */
	public String invokexPressionDocPrintWebService(String user, String password, String categoryLetter, String type, String fileName)
		throws NbaBaseException {
		String response = null;
		NbaxPressionDocPrintProxy docPrintProxyClient = null;
		try {
			URL url = new URL(getWsdlUrl());
			docPrintProxyClient = new NbaxPressionDocPrintProxy(); //object of proxy client generated by WSAD
			docPrintProxyClient.setEndPoint(url); //set WsdlUrl
			if (NbaxPressionAdapter.CATEGORY_REQUEST.equalsIgnoreCase(fileName)) {
				response = docPrintProxyClient.getListOfCategories(user, password);
			} else if (NbaxPressionAdapter.LETTER_REQUEST.equalsIgnoreCase(fileName)) {
				response = docPrintProxyClient.getListOfDocuments(user, password, categoryLetter);
			} else if (NbaxPressionAdapter.VARIABLE_REQUEST.equalsIgnoreCase(fileName)) {
				response = docPrintProxyClient.getDocumentVariables(user, password, categoryLetter);
			} else
				throw new NbaBaseException("Invalid Correspondence Type Requested");
		} catch (Exception e) {
			throw new NbaBaseException(e);
		}
		return response;
	}
	/**
	 * Takes six parameters and calls the xPression proxy Client class for xPression request web service. After instantiating the proxy client 
	 * this method passes the xpression user, password, transformation/batch type and the customer data into the client and gets the soap
	 * response back from WebService.
	 * @param user xPression user id
	 * @param password xPression password
	 * @param categoryLetter Name of the xPression category of letter name
	 * @param type xPression transformation or batch named defined in the xPressionAdapter.properties file
	 * @param xml Customer data xml 
	 * @param fileName Name of the webservice function id
	 * @return Object response returned from WebService
	 */
	 //SPR3337 changed method signature
	public Object invokexPressionRequestWebService(String user, String password, String categoryLetter, String type, String xml, String fileName)
		throws NbaBaseException {
		Object response = null;	//SPR3337
		NbaxPressionRequestProxy requestProxyClient = null;
		try {
			URL url = new URL(getWsdlUrl());
			requestProxyClient = new NbaxPressionRequestProxy(); //object of proxy client generated by WSAD
			requestProxyClient.setEndPoint(url); //set WsdlUrl
			if (NbaxPressionAdapter.PDF_REQUEST.equalsIgnoreCase(fileName)) {
				response = requestProxyClient.previewPDF(categoryLetter, user, password, PDF_RETURN, type, xml);	//SPR3337
			} else if (NbaxPressionAdapter.BATCH_PRINT_REQUEST.equalsIgnoreCase(fileName)) {
				response = requestProxyClient.postForBatch(type, user, password, xml);
			} else
				throw new NbaBaseException("Invalid Correspondence Type Requested");
		} catch (Exception e) {
			throw new NbaBaseException(e);
		}
		return response;
	}
}
