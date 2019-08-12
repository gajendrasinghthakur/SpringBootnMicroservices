package com.csc.fsg.nba.webservice.client;

/*
 * ************************************************************** <BR>
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
 * ************************************************************** <BR>
 * 
 */

import com.csc.fsg.nba.exception.NbaConfigurationException;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.configuration.Function;
import com.csc.fsg.nba.vo.configuration.WebService;


/** 
 * 
 * This is the factory class to get the instance of WebService client class. It decides which Web Service adapter instance 
 * should be returned based-on entries present in the configuration file.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 *  <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA092</td><td>Version 3</td><td>Architecture changes for phase 2c</td></tr> 
 * <tr><td>NBA108</td><td>Version 4</td><td>Vantage Inforce Payment</td></tr>
 * <tr><td>NBA109</td><td>Version 4</td><td>Vantage Loan Payment</td></tr>
 * <tr><td>ACN012</td><td>Version 4</td><td>Architecture Changes</td></tr>
 * <tr><td>SPR2968</td><td>Version 6</td><td>Test web service should determine XML file name dynamically</td></tr>
 * <tr><td>SPR3292</td><td>Version 7</td><td>Canned webservice responses for demo purposes should allow for dynamic data</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 3
 */
public class NbaWebServiceAdapterFactory {

/**
 * This method takes 3 arguments and reads the config file based on them and returns the instance of WebService client class.
 * @param backEnd (CLIF, VNTG etc...)
 * @param category (Agent or Client or...)
 * @param functionId (AgentVal, ClientSearch etc...)
 * @return NbaWebServiceAdapter which contains instance of NbaWebServiceAdapterBase
 * @throws NbaConfigurationException
 */
public static NbaWebServiceAdapter createWebServiceAdapter(String backEnd, String category, String functionId) throws NbaConfigurationException { //NBA103
	try{
		Function function = NbaConfiguration.getInstance().getIntegrationCategoryFunction(backEnd, category, functionId); //ACN012
		WebService service = NbaConfiguration.getInstance().getWebService(function.getWebService()); //ACN012
		if (NbaUtils.isNbaOffline()) { //NBLXA-1416
			service = NbaConfiguration.getInstance().getWebService("TestService"); //NBLXA-1416
		} //NBLXA-1416
		//ACN012 CODE DELETED
		NbaWebServiceAdapterBase webServiceAdapterBase = (NbaWebServiceAdapterBase)(NbaUtils.classForName(service.getAdapterClass()).newInstance());
	
		webServiceAdapterBase.setWsdlUrl(service.getWSDLURL()); //ACN012
		webServiceAdapterBase.setTargetUri(service.getTargetURI()); //ACN012
		webServiceAdapterBase.setAccess(service.getAccess());
		webServiceAdapterBase.setTimeout(String.valueOf(service.getTimeout()));//NBA108 NBA109
		// begin SPR2968
		StringBuffer fileName = new StringBuffer();
		fileName.append(backEnd);
		fileName.append("_");
		fileName.append(category);
		fileName.append("_");
		fileName.append(functionId);
		fileName.append("_Response"); //SPR3292
		webServiceAdapterBase.setTestWsFileName(fileName.toString());
		// end SPR2968
		webServiceAdapterBase.setFunction(function); //APSL4010
		System.out.println("Webservice stubbed reponse file backEnd==> " + backEnd);
		System.out.println("Webservice stubbed reponse file category==> " + category);
		System.out.println("Webservice stubbed reponse file functionId==> " + functionId);
		System.out.println("Webservice stubbed reponse file==> " + webServiceAdapterBase.getTestWsFileName());
		return webServiceAdapterBase;

	}catch(Throwable t){ //NBA103
		NbaConfigurationException nce = new NbaConfigurationException(t); //NBA103
		NbaLogFactory.getLogger(NbaWebServiceAdapter.class).logException("Unable to create WebService for " + functionId, nce); //NBA103
		t.printStackTrace();
		System.out.println(nce.getMessage());
		System.out.println(nce.getRootCause());
		throw nce; //NBA103
	}
}

}
