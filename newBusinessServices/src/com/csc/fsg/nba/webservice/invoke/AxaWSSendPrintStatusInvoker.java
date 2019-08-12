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
 *     Copyright (c) 2002-2010 Computer Sciences Corporation. All Rights Reserved.<BR>
 * 
 * *******************************************************************************<BR>
 */

package com.csc.fsg.nba.webservice.invoke;

import java.util.List;

import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaOLifEId;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.vo.AxaGIPrintReleaseVO;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTXRequestVO;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.EPolicyData;
import com.csc.fsg.nba.vo.txlife.Holding;
import com.csc.fsg.nba.vo.txlife.OLifE;
import com.csc.fsg.nba.vo.txlife.OLifEExtension;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.PolicyExtension;

/**
 * This class is responsible for creating request for Send Print status webservice .
 * <p>
 * <b>Modifications: </b> <br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th>
 * <th align=left>Release</th>
 * <th align=left>Description</th>
 * </thead>
 * <tr>
 * <td>APSL5100</td>
 * <td>Discretionary</td>
 * <td>Print Preview</td>
 * </tr>
 * </table>
 * <p>
 * 
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */
public class AxaWSSendPrintStatusInvoker extends AxaWS1203Invoker {
	private static final String CATEGORY = "Epolicy";

	private static final String FUNCTIONID = "sendPrintPreviewStatus";

	/**
	 * constructor from superclass
	 * 
	 * @param userVO
	 * @param nbaTXLife
	 */
	public AxaWSSendPrintStatusInvoker(String operation, NbaUserVO userVO, NbaTXLife nbaTXLife, NbaDst nbaDst, Object object) {
		super(operation, userVO, nbaTXLife, nbaDst, object);
		setBackEnd(ADMIN_ID);
		setCategory(CATEGORY);
		setFunctionId(FUNCTIONID);
	}

	/**
	 * This method first calls the superclass createRequest() and then set the request specefic attribute.
	 * 
	 * @return nbaTXLife
	 */
	public NbaTXLife createRequest() throws NbaBaseException {
		NbaTXRequestVO nbaTXRequest = new NbaTXRequestVO();
		nbaTXRequest.setTransType(NbaOliConstants.TC_TYPE_HOLDINGINQTRANS);
		nbaTXRequest.setTransMode(NbaOliConstants.TC_MODE_ORIGINAL);
		nbaTXRequest.setNbaUser(getUserVO());
		NbaTXLife nbaReqTXLife = null;
		if (getObject() != null) {
			List<AxaGIPrintReleaseVO> axaGIPrintReleaseVO = (List<AxaGIPrintReleaseVO>) getObject();
			if (axaGIPrintReleaseVO != null && axaGIPrintReleaseVO.size() > 0) {
				nbaReqTXLife = gIprintRelease(nbaTXRequest, axaGIPrintReleaseVO);
			} else {
				nbaReqTXLife = printPreview(nbaTXRequest);
			}
		} else {
			nbaReqTXLife = printPreview(nbaTXRequest);
		}
		return nbaReqTXLife;
	}

	/**
	 * This method is responsible for udpate Request for sent print status to Epolicy 1203 request created.
	 * 
	 * @return void
	 */
	public void updateRequest() {
		EPolicyData ePolicyData = null;
		if (getNbaTXLife().getPolicy().getOLifEExtensionCount() > 0) {
			PolicyExtension policyExtn = getNbaTXLife().getPolicy().getOLifEExtensionAt(0).getPolicyExtension();
			if (policyExtn != null && policyExtn.getEPolicyDataCount() > 0) {
				for (int i = 0, j = policyExtn.getEPolicyDataCount(); i < j; i++) {
					ePolicyData = policyExtn.getEPolicyDataAt(i);
					if (ePolicyData != null && ePolicyData.getActive()) {
						break;
					}

				}
			}

			getNbaTxLifeRequest().getPolicy().setPolNumber(getNbaTXLife().getPolicy().getPolNumber());
			if (getNbaTxLifeRequest().getPolicy().getOLifEExtensionCount() > 0 && ePolicyData != null) {
				PolicyExtension locPolicyExtn = getNbaTxLifeRequest().getPolicy().getOLifEExtensionAt(0).getPolicyExtension();
				EPolicyData locePolicyData = locPolicyExtn.getEPolicyDataAt(0);
				locePolicyData.setEPolicyPrintID(ePolicyData.getEPolicyPrintID());
				locePolicyData.setPrintStatus(ePolicyData.getPrintStatus());
				locePolicyData.setReleasePrintInd(ePolicyData.getReleasePrintInd());// NBLXA-188
				locPolicyExtn.setGIBatchID(policyExtn.getGIBatchID());// NBLXA-188
			}
		}
	}

	/**
	 * @Purpose This wmethod will invoke when
	 * @param nbaTXRequest
	 * @throws NbaBaseException
	 */
	public NbaTXLife gIprintRelease(NbaTXRequestVO nbaTXRequest, List<AxaGIPrintReleaseVO> axaGIPrintReleaseVO) throws NbaBaseException {
		NbaTXLife nbaReqTXLife = new NbaTXLife(nbaTXRequest);
		OLifE olifE = new OLifE();
		olifE.setVersion("2.9.03");
		olifE.setSourceInfo(createSoureInfo());
		NbaOLifEId olifeId = new NbaOLifEId(nbaReqTXLife);
		for (int i = 0; i < axaGIPrintReleaseVO.size(); i++) {
			Holding holding = new Holding();
			olifeId.setId(holding);
			holding.setHoldingTypeCode(NbaOliConstants.OLI_HOLDTYPE_POLICY);
			Policy policy = new Policy();
			policy.setPolNumber(axaGIPrintReleaseVO.get(i).getPolicyNo());
			olifeId.setId(policy);
			holding.setPolicy(policy);
			OLifEExtension olifeExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_POLICY);
			PolicyExtension polExt = new PolicyExtension();
			polExt.setGIBatchID(axaGIPrintReleaseVO.get(i).getBatchID());
			polExt.setMDRConsentIND(axaGIPrintReleaseVO.get(i).getMdrConsentInd());
			olifeExt.setPolicyExtension(polExt);
			policy.addOLifEExtension(olifeExt);
			EPolicyData ePolData = new EPolicyData();
			olifeId.setId(ePolData);
			ePolData.setEPolicyPrintID(axaGIPrintReleaseVO.get(i).getEPolicyprintID());
			ePolData.setPrintStatus(NbaOliConstants.OLI_PRINT_STATUS_IGO);
			if (axaGIPrintReleaseVO.get(i).getPrintIndicator().equalsIgnoreCase("Yes")) {
				ePolData.setReleasePrintInd(true);
			} else {
				ePolData.setReleasePrintInd(false);
			}

			policy.getOLifEExtensionAt(0).getPolicyExtension().addEPolicyData(ePolData);
			olifE.addHolding(holding);
		}
		nbaReqTXLife.setOLifE(olifE);
		setNbaTxLifeRequest(nbaReqTXLife);
		// updateRequest();

		return nbaReqTXLife;
	}

	/**
	 * @Purpose This wmethod will invoke when
	 * @param nbaTXRequest
	 * @throws NbaBaseException
	 */
	public NbaTXLife printPreview(NbaTXRequestVO nbaTXRequest) throws NbaBaseException {
		NbaTXLife nbaReqTXLife = new NbaTXLife(nbaTXRequest);
		OLifE olifE = new OLifE();
		olifE.setVersion("2.9.03");
		olifE.setSourceInfo(createSoureInfo());
		Holding holding = new Holding();
		holding.setId("Holding_1");
		holding.setHoldingTypeCode(NbaOliConstants.OLI_HOLDTYPE_POLICY);
		Policy policy = new Policy();
		policy.setId("Policy_1");
		holding.setPolicy(policy);
		OLifEExtension olifeExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_POLICY);
		PolicyExtension extension = new PolicyExtension();
		olifeExt.setPolicyExtension(extension);
		policy.addOLifEExtension(olifeExt);
		EPolicyData ePolData = new EPolicyData();
		ePolData.setId("Epolicy_1");
		policy.getOLifEExtensionAt(0).getPolicyExtension().addEPolicyData(ePolData);
		olifE.addHolding(holding);
		nbaReqTXLife.setOLifE(olifE);
		setNbaTxLifeRequest(nbaReqTXLife);
		updateRequest();
		return nbaReqTXLife;
	}

}
