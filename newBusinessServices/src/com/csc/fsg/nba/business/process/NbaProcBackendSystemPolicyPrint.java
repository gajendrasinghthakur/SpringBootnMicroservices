package com.csc.fsg.nba.business.process;
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
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaExceptionType;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTXRequestVO;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.KeyValue;
import com.csc.fsg.nba.vo.txlife.KeyedValue;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.ResultInfo;
import com.csc.fsg.nba.vo.txlife.TransResult;
import com.csc.fsg.nba.vo.txlife.UserAuthResponseAndTXLifeResponseAndTXLifeNotify;
import com.csc.fsg.nba.webservice.client.NbaWebServiceAdapter;
import com.csc.fsg.nba.webservice.client.NbaWebServiceAdapterFactory;
/**
 * NbaProcBackendSystemPolicyPrint is the class that processes nbAccelerator cases found
 * on the AWD Backend System Policy Print (NBPRTBES) queue . 
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA133</td><td>Version 6</td><td>nbA CyberLife Interface for Calculations and Contract Print</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 6
 * @see NbaAutomatedProcess
 */
public class NbaProcBackendSystemPolicyPrint extends NbaAutomatedProcess {
	/**
	 * This constructor calls the superclass constructor which will set
	 * the appropriate statues for the process.
	 */
	public NbaProcBackendSystemPolicyPrint() {
		super();
	}
	/**
	 * This method drives the Backend System Policy Print process.	
	 * @param user the user for whom the work was retrieved
	 * @param work the AWD case to be processed
	 * @return NbaAutomatedProcessResult the results of the process
	 * @throws NbaBaseException
	 */
	public NbaAutomatedProcessResult executeProcess(NbaUserVO user, NbaDst work) throws NbaBaseException {
		if (!initialize(user, work)) {
            return getResult();
        }
        doProcess();
        if (getResult() == null) {
            setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "Successful", getPassStatus()));
        }
        changeStatus(getResult().getStatus());
        doUpdateWorkItem();
        return getResult();
	}
	
	protected void doProcess() throws NbaBaseException{
		NbaWebServiceAdapter service = NbaWebServiceAdapterFactory.createWebServiceAdapter(getWorkLobs().getBackendSystem(),
                NbaConfiguration.WEBSERVICE_CATEGORY_PRINT, NbaConfiguration.WEBSERVICE_FUNCTION_PRINT);
		NbaTXLife response = service.invokeWebService(createPrintRequest());
		handleWebServiceResponse(response);			    
	}
	
	protected NbaTXLife createPrintRequest(){
        NbaTXRequestVO nbaTXRequest = new NbaTXRequestVO();
        nbaTXRequest.setTransType(NbaOliConstants.TC_TYPE_NEWBUSSUBMISSION);
        nbaTXRequest.setTransSubType(NbaOliConstants.TC_SUBTYPE_BACKEND_PRINT);
        nbaTXRequest.setTransMode(NbaOliConstants.TC_MODE_ORIGINAL);
        nbaTXRequest.setBusinessProcess(NbaUtils.getBusinessProcessId(getUser()));
        nbaTXRequest.setNbaLob(getWorkLobs());
        //create txlife with default request fields
        NbaTXLife request = new NbaTXLife(nbaTXRequest);
        request.setOLifE(((NbaTXLife)getNbaTxLife().clone(false)).getOLifE());        
        updatePrintRequestForRequestedPages(request);        
        return request;
	}
	
	protected void updatePrintRequestForRequestedPages(NbaTXLife request){
	    String pages = getWorkLobs().getExtractComp();
	    if(pages != null && pages.trim().length() > 0){
	        String[] tokens = pages.split(",");
	        KeyedValue keyedValue = null;
	        KeyValue keyValue = null;
	        Policy policy = request.getPolicy();
	        for(int i=0; i<tokens.length; i++){
	            keyedValue = new KeyedValue();
	            policy.addKeyedValue(keyedValue);
	            keyValue = new KeyValue();
	            keyedValue.addKeyValue(keyValue);
	            keyValue.setPCDATA(tokens[i]);	            
	        }
	    }
	}
	
	/**
	 * Handle the NbaTXLife response from a Webservice.
	 * @param aTXLifeResponse the NbaTXLife response
	 */
	protected void handleWebServiceResponse(NbaTXLife aTXLifeResponse) throws NbaBaseException {
        if (aTXLifeResponse.isTransactionError()) {
            UserAuthResponseAndTXLifeResponseAndTXLifeNotify allResponses = aTXLifeResponse.getTXLife()
                    .getUserAuthResponseAndTXLifeResponseAndTXLifeNotify();
            TransResult transResult = allResponses.getTXLifeResponseAt(allResponses.getTXLifeResponseCount() - 1).getTransResult();
            StringBuffer buffer = new StringBuffer("Backend System Contract Print Failed.");
            int resultInfoCount = transResult.getResultInfoCount();
            ResultInfo resultInfo = null;
            for (int i = 0; i < resultInfoCount; i++) {
                resultInfo = transResult.getResultInfoAt(i);
                if (resultInfo.getResultInfoCode() == NbaOliConstants.TC_RESINFO_SYSTEMNOTAVAIL) {
                    throw new NbaBaseException("WebService not available", NbaExceptionType.FATAL);
                }
                buffer.append(" ");
                buffer.append(transResult.getResultInfoAt(i).getResultInfoDesc());                
            }
            addComment(buffer.toString());
            setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "Webservice Processing failed", getHostFailStatus()));
        }
    }
}
