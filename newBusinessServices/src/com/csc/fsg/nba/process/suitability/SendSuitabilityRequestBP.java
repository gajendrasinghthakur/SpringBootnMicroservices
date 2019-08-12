package com.csc.fsg.nba.process.suitability;
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
 *     Copyright (c) 2002-2012 Computer Sciences Corporation. All Rights Reserved.<BR>
 * 
 * *******************************************************************************<BR>
 */
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.csc.fs.Result;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fsg.nba.contract.validation.NbaSuitabilityCVResultsProcessor;
import com.csc.fsg.nba.database.NbaSuitabilityProcessingAccessor;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.process.NewBusinessAccelBP;
import com.csc.fsg.nba.vo.NbaSuitabilityProcessingContract;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTXRequestVO;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.SuitabilityVO;
import com.csc.fsg.nba.vo.txlife.ApplicationInfoExtension;
import com.csc.fsg.nba.webservice.invoke.AxaWSConstants;
import com.csc.fsg.nba.webservice.invoke.AxaWSInvoker;
import com.csc.fsg.nba.webservice.invoke.AxaWSInvokerFactory;


/**
 * Class Description.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA297</td><td>Version 1201</td><td>Suitability</td></tr>
 * <tr><td>PP2AXAL021</td><td>AXA Life Phase 2</td><td>Suitability</td></tr> *
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 1201
 * @since New Business Accelerator - Version 1201
 */

public class SendSuitabilityRequestBP extends NewBusinessAccelBP {
	//P2AXAL021 refactored for AXA 
	public Result process(Object input) {
		AccelResult result;
		try {
			result = invokeService((SuitabilityVO) input);
		} catch (Exception e) {
			result = new AccelResult();
			addExceptionMessage(result, e);
		}
		return result;
	}

	//P2AXAL021
	private AccelResult invokeService(SuitabilityVO vo) {
		AccelResult result = new AccelResult();
		try {
			List changeSubTypeList = createChangeSubTypeList(vo.getNbaTXLife()); //NBLXA2303[NBLXA-2304]
			AxaWSInvoker webServiceInvoker = AxaWSInvokerFactory.createWebServiceRequestor(AxaWSConstants.WS_OP_SUBMIT_SUITABIITY_REQUEST, vo.getNbaUserVO(),  constructRequest(vo.getNbaTXLife(), vo.getNbaUserVO()),null, changeSubTypeList);
			result.addResult(webServiceInvoker.execute());
		} catch (Exception e) {
			addExceptionMessage(result, e);
		}
		return result;
	}

	//NBLXA2303[NBLXA-2304] New Method
	private List createChangeSubTypeList(NbaTXLife txLife) throws Exception {
		String companyCode = txLife.getCarrierCode();
		String polNumber = txLife.getPolicy().getPolNumber();
		NbaSuitabilityProcessingContract suitabilityContract = NbaSuitabilityProcessingAccessor.retrieve(companyCode, polNumber);
		return NbaSuitabilityCVResultsProcessor.parseSuitabilityResubmitData(suitabilityContract.getSuitabilityResubmitData());
	}

	private NbaTXLife constructRequest(NbaTXLife contract, NbaUserVO user) {
		NbaTXRequestVO nbaTXRequest = new NbaTXRequestVO();
		nbaTXRequest.setTransType(NbaOliConstants.TC_TYPE_NEWBUSSUBMISSION);
		nbaTXRequest.setTransSubType(NbaOliConstants.TC_SUBTYPE_SUITABILITY_REQUEST);
		ApplicationInfoExtension extension = NbaUtils.getFirstApplicationInfoExtension(contract.getPolicy().getApplicationInfo());
		if (extension.getSuitabilityDecisionStatus() == -1) {
			nbaTXRequest.setTransMode(NbaOliConstants.TC_MODE_ORIGINAL);
		} else {
			nbaTXRequest.setTransMode(NbaOliConstants.TC_MODE_UPDATE);
		}
		nbaTXRequest.setNbaUser(user);
		NbaTXLife request = new NbaTXLife(nbaTXRequest);
		request.setOLifE(contract.getOLifE().clone(false));
		if (NbaUtils.isPlainTermConv(request)) { //APSL5262 -Setting ReplacementInd as 'False' in request when case is plain TCONV case 
			request.getPolicy().getApplicationInfo().setReplacementInd(false);
		}
		return request;
	}

}
