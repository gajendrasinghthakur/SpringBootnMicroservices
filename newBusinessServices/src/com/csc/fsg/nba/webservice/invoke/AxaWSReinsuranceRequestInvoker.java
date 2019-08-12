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
import java.util.List;
import java.util.Map;

import com.csc.fsg.nba.business.transaction.NbaAxaServiceResponse;
import com.csc.fsg.nba.exception.AxaErrorStatusException;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaExceptionType;
import com.csc.fsg.nba.foundation.AxaStatusDefinitionConstants;
import com.csc.fsg.nba.foundation.NbaConfigurationConstants;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTransOliCode;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.ResultInfo;
import com.csc.fsg.nba.vo.txlife.TransResult;
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
 * <tr><td>AXAL3.7.32</td><td>AXA Life Phase 2</td><td>Reinsurance Interface</td></tr>
 * </table>
 * <p>
 * 
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */
public class AxaWSReinsuranceRequestInvoker extends AxaWSInvokerBase {
	private static final String CATEGORY = "Reinsurance";

	private static final String FUNCTIONID = "ReinsuranceRequest";

	/**
	 * constructor from superclass
	 * @param userVO
	 * @param nbaTXLife
	 */
	public AxaWSReinsuranceRequestInvoker(String operation, NbaUserVO userVO, NbaTXLife nbaTXLife, NbaDst nbaDst, Object object) {
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
		requestParams.put("reinsuranceRequest", getObject() != null ? (String)getObject() : null);
        Map results = null;
        try {
        	results = service.invokeAxaWebService(requestParams);
        } catch (NbaBaseException nbe){
        	//ALL2072 start
        	getLogger().logDebug("Exception occurred while making submitReinsurance interface call.");
    		String reinReqLogging = NbaConfiguration.getInstance().getBusinessRulesAttributeValue(NbaConfigurationConstants.REINSURANCE_REQUEST_LOGGING);
        	if(reinReqLogging != null & Boolean.valueOf(reinReqLogging).booleanValue()) { 
            	getLogger().logDebug("Axa webservice request for  " + getOperation() + ":" + requestParams.get(NbaAxaServiceRequestor.PARAM_SOAPENVELOP)); //ALII2072 - conditional full SOAP request logging.
        	} else {
        		getLogger().logDebug("Axa webservice request for  " + getOperation() + ":" + "Reinsurance Request sent to Reinsurer.");
        	}
        	//ALL2072 end
            throw nbe;        	
        }
       
        if(!NbaUtils.isBlankOrNull(results.get(NbaAxaServiceResponse.ERRORMSG_ELEMENT))) {
            String errorText = (String) results.get(NbaAxaServiceResponse.ERRORMSG_ELEMENT);
            getLogger().logError("Error Messages in Response for " + getOperation() + ":" + errorText);
            throw new NbaBaseException("Failure response : " + errorText);
        }   
        try {
        	//setWebserviceResponse(results.get(NbaAxaServiceResponse.REINSURANCE_RESPONSE));
        	setWebserviceResponse(results.get(NbaAxaServiceResponse.NBATXLIFE_ELEMENT));
        	
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
		//ALL2072 start
		String reinReqLogging = NbaConfiguration.getInstance().getBusinessRulesAttributeValue(NbaConfigurationConstants.REINSURANCE_REQUEST_LOGGING);
    	if(reinReqLogging != null & Boolean.valueOf(reinReqLogging).booleanValue()) { 
        	getLogger().logDebug("Axa webservice request for  " + getOperation() + ":" + requestParams.get(NbaAxaServiceRequestor.PARAM_SOAPENVELOP)); //ALII2072 - conditional full SOAP request logging.
    	} else {
    		getLogger().logDebug("Axa webservice request for  " + getOperation() + ":" + "Reinsurance Request sent to Reinsurer.");
    	}
		//ALL2072 end
        getLogger().logDebug("Axa webservice response for " + getOperation() + ":" + ((NbaTXLife )getWebserviceResponse()).toXmlString());
    }
    
    /**
     * @param nbaTXLife
     * @throws NbaBaseException
     */
    protected void handleResponse() throws NbaBaseException {
    	// APSL3874 code deleted
    		//String response = "<TxLife><TxLifeResponse>" + (String) getWebserviceResponse() + "</TxLifeResponse></TxLife>";
        	//NbaTXLife txLife = new NbaTXLife(response);
        	//setWebserviceResponse(txLife);
    		NbaTXLife txLife = (NbaTXLife) getWebserviceResponse();
        	TransResult transResult = txLife.getTransResult();
            if (transResult != null && transResult.getResultCode() != NbaOliConstants.TC_RESCODE_SUCCESS) {
                StringBuffer errorString = new StringBuffer();
                List resultInfoList = transResult.getResultInfo();
                long resultInfoCode = 0;
                if (resultInfoList != null && resultInfoList.size() > 0) {
                    for (int i = 0; i < resultInfoList.size(); i++) {
                        ResultInfo resultInfo = (ResultInfo) resultInfoList.get(i);
                        if(i>0)
                            errorString.append(" Error count : " + i );
                        resultInfoCode = resultInfo.getResultInfoCode();
                        if (!NbaUtils.isBlankOrNull(resultInfoCode))
                            errorString.append(" Error Code : (" + resultInfoCode + ") "
                                    + NbaTransOliCode.lookupText(NbaOliConstants.RESULT_INFO_CODES, resultInfoCode) + "\n");
                        if (!NbaUtils.isBlankOrNull(resultInfo.getResultInfoDesc()))
                            errorString.append(" Error Desc : " + resultInfo.getResultInfoDesc());

                        errorString.append("\n");
                    }
                    getLogger().logError("Error Messages in Response for " + getOperation() + ":" + errorString.toString());
                    if(NbaOliConstants.TC_RESINFO_GENERALERROR == resultInfoCode || NbaOliConstants.TC_RESINFO_SYSTEMNOTAVAIL == resultInfoCode){
                    	throw new NbaBaseException("Failure response : " + errorString.toString(),NbaExceptionType.FATAL);	
                    }
                    throw new AxaErrorStatusException(AxaStatusDefinitionConstants.VARIANCE_KEY_TECH_WS, AxaWSConstants.WS_OP_SUBMIT_REINSURER_REQUEST + ": "+errorString.toString());// APSL3874				
                }
            } //APSL3874
            // APSL3874 code deleted
    }
}