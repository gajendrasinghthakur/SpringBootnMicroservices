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
 *     Copyright (c) 2002-2010 Computer Sciences Corporation. All Rights Reserved.<BR>
 * 
 * *******************************************************************************<BR>
 */

package com.csc.fsg.nba.webservice.invoke;

import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;

import com.csc.fsg.nba.business.transaction.NbaAxaServiceResponse;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.webservice.client.NbaAxaServiceRequestor;
import com.csc.fsg.nba.webservice.client.NbaWebServiceAdapter;
import com.csc.fsg.nba.webservice.client.NbaWebServiceAdapterFactory;

/**
 * This class is responsible for creating request for Compensation PCS webservice .
 * <p>
 * <b>Modifications: </b> <br>
 * <table border=0 cellspacing=5 cellpadding=5> <thead>
 * <th align=left>Project</th>
 * <th align=left>Release</th>
 * <th align=left>Description</th>
 * </thead>
 * <tr><td>AXAL3.7.13I</td><td>AXA Life Phase 1</td><td>Informal Correspondence</td></tr>
 * </table>
 * <p>
 * 
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */
public class AxaWSCorresCategoryRequestInvoker extends AxaWSInvokerBase {
	private static final String CATEGORY = "Correspondence";

	private static final String FUNCTIONID = "getListOfCategories";

	/**
	 * constructor from superclass
	 * @param userVO
	 * @param nbaTXLife
	 */
	public AxaWSCorresCategoryRequestInvoker(String operation, NbaUserVO userVO, NbaTXLife nbaTXLife, NbaDst nbaDst, Object object) {
		super(operation, userVO, nbaTXLife, nbaDst, object);
		setBackEnd(ADMIN_ID);
		setCategory(CATEGORY);
		setFunctionId(FUNCTIONID);
	}
	
	/**
	 * This method first calls the superclass createRequest() and then set the request specefic attribute.
	 * @return nbaTXLife
	 */
	public NbaTXLife createRequest() throws NbaBaseException {
		return null;
	}
	
	public void cleanRequest() {
        
    }
	
	 /**
     * @return
     * @throws NbaBaseException
     */
    public void invoke() throws NbaBaseException {
        NbaWebServiceAdapter service = NbaWebServiceAdapterFactory.createWebServiceAdapter(getBackEnd(), getCategory(), getFunctionId());
        Map requestParams = new HashMap();
        requestParams.put("operation", getOperation());
		requestParams.put("token", getUserVO() != null ? getUserVO().getToken() : null);
		requestParams.put("username", getUserVO() != null ? getUserVO().getUserID() : null);
		requestParams.put("password", getUserVO() != null ? getUserVO().getPassword() : null);
		requestParams.put("categoryName", null);
		requestParams.put("docName", null);
        Map results = null;
        try {//This try catch should be removed and the below two loggers should be moved to execute() method once all the code from NbaAxaServiceRequester
			 // is moved into this class.
        	results = service.invokeAxaWebService(requestParams);
        } catch (NbaBaseException nbe){
        	getLogger().logDebug("Axa webservice request for  " + getOperation() + ":" + requestParams.get(NbaAxaServiceRequestor.PARAM_SOAPENVELOP));
        	throw nbe;        	
        }
       
        if(!NbaUtils.isBlankOrNull(results.get(NbaAxaServiceResponse.ERRORMSG_ELEMENT))) {
            String errorText = (String) results.get(NbaAxaServiceResponse.ERRORMSG_ELEMENT);
            getLogger().logError("Error Messages in Response for " + getOperation() + ":" + errorText);
            throw new NbaBaseException("Failure response : " + errorText);

        }   
        try {
        	setWebserviceResponse(results.get(WS_OP_CORRESPONDENCE_GETCATEGORIES_RETURN));
        	
		} catch (Exception e) {
			getLogger().logException(e);
			NbaBaseException nbe = new NbaBaseException(e.getMessage());
			// enforcing the exception to be a Fatal exception so that All autoprocesses calling a web service will error stop.
			nbe.forceFatalExceptionType();
			if (null != e.getCause() && e.getCause() instanceof SocketTimeoutException) {
				nbe.forceFatalExceptionType();
			}
			throw nbe;
		}
        getLogger().logDebug("Axa webservice request for  " + getOperation() + ":" + requestParams.get(NbaAxaServiceRequestor.PARAM_SOAPENVELOP));
		getLogger().logDebug("Axa webservice response for " + getOperation() + ":" + ((String )getWebserviceResponse()).toString());
    }
    
    /**
     * @param nbaTXLife
     * @throws NbaBaseException
     */
    protected void handleResponse() throws NbaBaseException {
    	
    }
}
