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

import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTXRequestVO;
import com.csc.fsg.nba.vo.NbaUserVO;

/**
 * This class is responsible for creating request for Contract Print.
 * <p>
 * <b>Modifications: </b> <br>
 * <table border=0 cellspacing=5 cellpadding=5> <thead>
 * <th align=left>Project</th>
 * <th align=left>Release</th>
 * <th align=left>Description</th>
 * </thead>
 * <tr><td>AXAL3.7.17PC</td><td>AXA Life Phase 1</td><td>Paid Changes Interface</td></tr>
 * <tr><td>CR60956</td><td>AXA Life Phase 2</td><td>Life 70 Reissue</td></tr>
 * </table>
 * <p>
 * 
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */

public class AxaWSRetrievePolicyInvoker extends AxaWSInvokerBase {
	private static final String CATEGORY = "CAPSRetrieveInforce";

	private static final String FUNCTIONID = "retrieveInforcePolicy";
	
	private NbaLob nbaLob;
	
	/**
	 * constructor from superclass
	 * @param userVO
	 * @param nbaTXLife
	 */
	public AxaWSRetrievePolicyInvoker(String operation, NbaUserVO userVO, NbaTXLife nbaTXLife, NbaDst nbaDst, Object object) {
		super(operation, userVO, nbaTXLife, nbaDst, object);
		setBackEnd(ADMIN_ID);
		setCategory(CATEGORY);
		setFunctionId(FUNCTIONID);
		if (object != null) {
			nbaLob = (NbaLob) object;
		}
	}

	public NbaTXLife createRequest() throws NbaBaseException {
		NbaTXRequestVO nbaTXRequest = new NbaTXRequestVO();
		nbaTXRequest.setTransType(NbaOliConstants.TC_TYPE_HOLDINGINQ);
		nbaTXRequest.setTransMode(NbaOliConstants.TC_MODE_ORIGINAL);
		nbaTXRequest.setInquiryLevel(NbaOliConstants.TC_INQLVL_OBJECTALL);
		nbaTXRequest.setTransSubType(getTransSubType()); //CR60956		
		nbaTXRequest.setNbaLob(nbaLob);
		nbaTXRequest.setNbaUser(getUserVO());
		nbaTXRequest.setAccessIntent(NbaConstants.READ);
		nbaTXRequest.setBusinessProcess(NbaUtils.getBusinessProcessId(getUserVO()));
		// Begin CR60956
		NbaTXLife request = new NbaTXLife(nbaTXRequest);
		if(nbaLob.getBackendSystem().equals(NbaConstants.SYST_LIFE70)) {
			request.getPolicy().setCarrierAdminSystem(NbaConstants.SYST_LIFE70);
		}
		return request;
		//End CR60956
	}

	/**
	 * This method returns the transaction sub type based on the backend system.
	 * @return transSubType transaction sub type
	 */
	//CR60956 new method
	private long getTransSubType() {
		long transSubType = 0;
		if (NbaConstants.SYST_CAPS.equals(nbaLob.getBackendSystem())) {
			transSubType = Long.parseLong(nbaLob.getContractChgType());
		} else if (NbaConstants.SYST_LIFE70.equals(nbaLob.getBackendSystem())) {
			transSubType = NbaOliConstants.TC_SUBTYPE_REISSUE_REQUEST;
		}
		return transSubType;
	}
	
    /**
     * @param nbaTXLife
     * @throws NbaBaseException
     */
    protected void handleResponse() {
    	try {
    		super.handleResponse();
    	}catch(Exception e){    		 
    		if(getLogger().isDebugEnabled()){
    			getLogger().logError("Error in retrieving Policy : " + nbaLob != null ? nbaLob.getPolicyNumber() : null);//AXAL3.7.04
    		}
    	}
    }
}
