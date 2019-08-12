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
import java.util.Map;

import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.vo.NbaTXLife;

/**
 * This interface exposes methods that all nbA Web Service Adapters must provide.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA092</td><td>Version 3</td><td>Architecture Changes</td></tr>
 * <tr><td>SPR1829</td><td>Version 4</td><td>Modify to allow NbaBaseExceptions to be propagated</td></tr>
 * <tr><td>NBA129><td>Version 5/td><td>xPression Correspondence</td></tr>
 * <tr><td>SPR2968</td><td>Version 6</td><td>Test web service should determine XML file name dynamically</td></tr>
 * <tr><td>SPR3337</td><td>Version 7</td><td>PDF data for letters is not stored in the workflow system</td></tr>
 * <tr><td>AXAL3.7.68</td><td>Version 7</td><td>LDAP Interface</td></tr>
 * <tr><td>AXAL3.7.13I</td><td>AXA Life Phase 1</td><td>Informal Correspondence</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 3
 */

public interface NbaWebServiceAdapter {

/**
 * This invokeWebService() method will be used to call a Web Service. The NbaTXLife object 
 * is a XML transaction for the WebService.
 * @param nbATxLife An instance of <code>NbaTXLife</code>
 * @return NbaTXLife is the response XML
 * @throws NbaBaseException
 */
// SPR1829 - throw NbaBaseException
// SPR2968 - removed parameter Object obj
NbaTXLife invokeWebService(NbaTXLife nbATxLife) throws NbaBaseException;

/**
 * This invokeCorrespondenceWebService() method will be used to call a correspondence WebService. Six parameters will be passed -
 * the user id, password, transformation/batch type and the customer data and file name 
 * @param user xPression user id
 * @param password xPression password
 * @param categoryLetter Name of the xPression category of letter name
 * @param type xPression transformation or batch named defined in the xPressionAdapter.properties file
 * @param xml Customer data xml 
 * @param fileName Name of the webservice function id
 * @return Object response returned from WebService
 */
//NBA129 New Method
//SPR3337 changed method signature
//AXAL3.7.13I changed method signature
Object invokeCorrespondenceWebService(String user, String password, String categoryLetter, String type, String xml, String fileName, String token, Map keys) throws com.csc.fsg.nba.exception.NbaBaseException;  

/**
 * This invokeAxaWebService() method will be used to call an AXA web service. A Map object parameter will be passed
 * containing the appropriate information to validate the request. 
 * @param Map parameters from the single sign on request
 * @return Map response values returned from WebService
 */
//AXAL3.7.68 New Method
Map invokeAxaWebService(Map parameters) throws com.csc.fsg.nba.exception.NbaBaseException; 

//NBLXA-2402(NBLXA-2476) New Method
public void addUriParam(String aParam);

}
