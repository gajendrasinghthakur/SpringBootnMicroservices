package com.csc.fsg.nba.webservice.invoke;

import java.util.ArrayList;
import java.util.Iterator;

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
 *     Copyright (c) 2002-2012 Computer Sciences Corporation. All Rights Reserved.<BR>
 * 
 * *******************************************************************************<BR>
 */

import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaOLifEId;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.ChangeSubType;
import com.csc.fsg.nba.vo.txlife.TXLife;
import com.csc.fsg.nba.vo.txlife.TXLifeRequest;
import com.csc.fsg.nba.vo.txlife.UserAuthRequestAndTXLifeRequest;

/**
 * 
 * <p>
 * <b>Modifications: </b> <br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead> 
 * <tr><td>PP2AXAL021</td><td>AXA Life Phase 2</td><td>Suitability</td></tr> * 
 * </table>
 * <p>
 * 
 * @author CSC FSG Developer
 * @version AXA Life Phase 2
 * @since New Business Accelerator - Version AXA Life Phase 2
 */
public class AxaWSSuitabilityRequestInvoker extends AxaWSInvokerBase {
	private static final String CATEGORY = "Suitability";
    private static final String FUNCTIONID = "SuitabilityRequest";

    public AxaWSSuitabilityRequestInvoker(String operation, NbaUserVO userVO, NbaTXLife nbaTXLife, NbaDst nbaDst, Object object) {
		super(operation, userVO, nbaTXLife, nbaDst, object);
        setBackEnd(ADMIN_ID); //TODO ?
        setCategory(CATEGORY);
        setFunctionId(FUNCTIONID);
	}
    
	public NbaTXLife createRequest() throws NbaBaseException {
		TXLifeRequest nbaResTXLifeRequest = getTXLifeRequest(getNbaTXLife());
		// Begin NBLXA2303[NBLXA2316]
		ArrayList changeSubTypeList = (ArrayList) getObject();
		NbaOLifEId nbaOLifEId = new NbaOLifEId(getNbaTXLife());
		if (!NbaUtils.isBlankOrNull(changeSubTypeList)) {
			Iterator changeSubTypeItr = changeSubTypeList.iterator();
			while (changeSubTypeItr.hasNext()) {
				ChangeSubType changeSubType = (ChangeSubType) changeSubTypeItr.next();
				nbaOLifEId.setId(changeSubType);
				nbaResTXLifeRequest.addChangeSubType(changeSubType);
			}
		}
		// End NBLXA2303[NBLXA2316]
		return getNbaTXLife();
	}

    protected void handleResponse() throws NbaBaseException {
    	return; //null operation
    }
    
  //NBLXA2303[NBLXA2316]
    protected TXLifeRequest getTXLifeRequest(NbaTXLife nbaReqTXLife) {
		TXLife resTXLife = nbaReqTXLife.getTXLife();
		UserAuthRequestAndTXLifeRequest userResandTXReq = resTXLife.getUserAuthRequestAndTXLifeRequest();
		TXLifeRequest nbaResTXLifeRequest = userResandTXReq.getTXLifeRequestAt(0);
		return nbaResTXLifeRequest;
	}
}
