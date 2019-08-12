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

import java.util.ArrayList;

import com.csc.fsg.nba.business.transaction.NbaInforcePaymentTransaction;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaExceptionType;
import com.csc.fsg.nba.foundation.NbaOLifEId;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.FinancialActivity;
import com.csc.fsg.nba.vo.txlife.FinancialActivityExtension;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.TransResult;

/**
 * This class is responsible for creating request for Contract Print.
 * <p>
 * <b>Modifications: </b> <br>
 * <table border=0 cellspacing=5 cellpadding=5> <thead>
 * <th align=left>Project</th>
 * <th align=left>Release</th>
 * <th align=left>Description</th>
 * </thead>
 * <tr><td>AXAL3.7.17</td><td>AXA Life Phase 1</td><td>CAPS Interface</td></tr>
 * </table>
 * <p>
 * 
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */

public class AxaWSReinstatementPolicyInvoker extends AxaWSInvokerBase {
	private static final String CATEGORY = "CAPSInforceSubmit";

	private static final String FUNCTIONID = "submitPolicy";
	
	/**
	 * constructor from superclass
	 * @param userVO
	 * @param nbaTXLife
	 */
	public AxaWSReinstatementPolicyInvoker(String operation, NbaUserVO userVO, NbaTXLife nbaTXLife, NbaDst nbaDst, Object object) {
		super(operation, userVO, nbaTXLife, nbaDst, object);
		setBackEnd(ADMIN_ID);
		setCategory(CATEGORY);
		setFunctionId(FUNCTIONID);
	}

	public NbaTXLife createRequest() throws NbaBaseException {
		return createXml508Transaction();
	}
	/**
     * @param nbaTXLife
     * @throws NbaBaseException
     */
    protected void handleResponse(NbaTXLife nbaTXLifeResponse) throws NbaBaseException {
		TransResult transResult = nbaTXLifeResponse.getTransResult();
		if (transResult != null) {
			long resultCode = transResult.getResultCode();
			if (NbaOliConstants.TC_RESCODE_FAILURE == resultCode) {
				//if failure (5) result code is received, then throw an exception to stop poller.
				throw new NbaBaseException("CAPS WebService not available", NbaExceptionType.FATAL);
			}
		}
	}
    /**
	 * Return the FinancialActivity object which has FinActivityType of UNAPPLDCASHIN - 278
	 * @return FinancialActivity
	 */
	protected FinancialActivity getSuspenseMoney() {
		Policy policy = getNbaTXLife().getPrimaryHolding().getPolicy();
		int finActCount = policy.getFinancialActivity().size();
		FinancialActivity finActivity = null;
		for (int i = 0; i < finActCount; i++) {
			finActivity = policy.getFinancialActivityAt(i);
			if (NbaOliConstants.OLI_LU_FINACT_UNAPPLDCASHIN == finActivity.getFinActivityType()) {
				return finActivity;
			}
		}
		return null;
	}
	/**
	 * Create a xml508 transaction so that it can be passed to CLIF adapter or VNTG webservice. For CyberLife, the adapter
	 * creates a U101 Reinstatement payment.
	 * @param suspenseFinActivity FinancialActivity object
	 * @param olifeId NbaOLifEId
	 * @return xml508 NbaTXLife transaction
	 * @exception throws NbaBaseException
	 */
	protected NbaTXLife createXml508Transaction() throws NbaBaseException{
		NbaTXLife xml508 = new NbaInforcePaymentTransaction().createTXLife508(getNbaDst(), getNbaDst().getNbaLob(), getUserVO());
		Policy policy = xml508.getPrimaryHolding().getPolicy();
		Policy contractPolicy = getNbaTXLife().getPrimaryHolding().getPolicy();
		policy.setPolNumber(contractPolicy.getPolNumber());
		policy.setProductCode(contractPolicy.getProductCode());
		policy.setProductType(contractPolicy.getProductType());	//SPR2248
		policy.setCarrierCode(contractPolicy.getCarrierCode());
		policy.setPaymentDueDate(contractPolicy.getPaymentDueDate());
		policy.setReinstatementDate(contractPolicy.getReinstatementDate());
		ArrayList finActivities = policy.getFinancialActivity();
		int finActCount = finActivities.size();
		for (int i = finActCount; i > 0; i--) {
			policy.removeFinancialActivityAt(i - 1);
		}
		policy.addFinancialActivity(createFinActForReinstatement());
		if (getLogger().isDebugEnabled()) {
			getLogger().logDebug("Reinstatement 508 Transaction " + xml508.toXmlString());
		}
		return xml508;
	}
	
	/**
	 * Create a new FinancialActivity object with FinActivityType of REINPYMT - 248. In this new
	 * FinancialActivity object the AcctgExtractInd will be set to true so that extracts will not be generated for this
	 * Reinstatement payment.
	 * @param suspenseFinActivity FinancialActivity object
	 * @param olifeId NbaOLifEId
	 * @return FinancialActivity
	 */
	protected FinancialActivity createFinActForReinstatement() {
		FinancialActivity finActForReinstatement = getSuspenseMoney().clone(false);
		finActForReinstatement.deleteId();
		NbaOLifEId olifeId = new NbaOLifEId(getNbaTXLife());
		olifeId.setId(finActForReinstatement);
		finActForReinstatement.setAccountingActivity(new ArrayList());
		finActForReinstatement.setFinActivityType(NbaOliConstants.OLI_FINACT_REINPYMT);
		FinancialActivityExtension finActExtension = NbaUtils.getFirstFinancialActivityExtension(finActForReinstatement);
		if (finActExtension != null) {
			finActExtension.setDisbursedInd(false);
			finActExtension.setAcctgExtractInd(true);
			finActExtension.deleteErrCorrInd();
		}
		return finActForReinstatement;
	}
}
