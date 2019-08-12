package com.csc.fsg.nba.business.transaction;

/*
 * *******************************************************************************<BR>
 * This program contains trade secrets and confidential information which are
 * proprietary to CSC Financial Services Group®.  The use, reproduction,
 * distribution or disclosure of this program, in whole or in part, without
 * the express written permission of CSC Financial Services Group is prohibited.
 * This program is also an unpublished work protected under the copyright laws
 * of the United States of America and other countries.
 *
 * If this program becomes published, the following notice shall apply:
 *    Property of Computer Sciences Corporation.<BR>
 *    Confidential. Not for publication.<BR>
 *    Copyright (c) 2002-2008 Computer Sciences Corporation. All Rights Reserved.<BR>
 * *******************************************************************************<BR>
 */

import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaOLifEId;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaSource;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTXRequestVO;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.FinancialActivity;
import com.csc.fsg.nba.vo.txlife.Holding;

/**
 * NbaInforcePaymentTransaction will be used to create NbaTXLife 508 transaction for use by by Host System. 
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA068</td><td>Version 3</td><td>Inforce Payment</td></tr>
 * <tr><td>NBA108</td><td>Version 4</td><td>Vantage Inforce Payment</td></tr>
 * <tr><td>SPR1890</td><td>Version 4</td><td>Removal of TransSubType from 508 transaction</td></tr>
 * <tr><td>SPR1775</td><td>Version 4</td><td>NBMNYDTM process error stops with the message  "Other error during nbA update" for Wire transfer Inforce Payment transaction</td></tr>
 * <tr><td>SPR2639</td><td>Version 5</td><td>Automated process status should be based business function</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 3
 */
public class NbaInforcePaymentTransaction extends NbaBusinessTransactions {
	/**
	 * Constructor for NbaAgentValidationClient.
	 */
	public NbaInforcePaymentTransaction() {
		super();
	}
	/**
	 * This method creates TXLife 508 transaction XML using details mentioned in arguments
	 * params@ NbaUserVO user the user/process for whom the process is being executed
	 * params@ nbaDst NbaDst value object for which the process is to occur
	 * params@ lob NbaLob object
	 * returns@ txLife508 NbaTXLife object
	 * @throws NbaBaseException
	 */
	public NbaTXLife createTXLife508(NbaDst nbaDst, NbaLob lob, NbaUserVO user) throws NbaBaseException {
		NbaTXRequestVO nbaTXRequest = new NbaTXRequestVO();
		nbaTXRequest.setTransType(NbaOliConstants.TC_TYPE_CWA);
		//SPR1890 line deleted
		nbaTXRequest.setTransMode(NbaOliConstants.TC_MODE_ORIGINAL);
		nbaTXRequest.setBusinessProcess(NbaUtils.getBusinessProcessId(user)); //SPR2639
		//			Get Lob
		nbaTXRequest.setNbaLob(lob);
		//create txlife with default request fields
		NbaTXLife newNbaTxLife = new NbaTXLife(nbaTXRequest);
		NbaOLifEId nbaOLifEId = new NbaOLifEId(newNbaTxLife);
		//Create Financial Activity Object
		FinancialActivity financialActivity = new FinancialActivity();
		nbaOLifEId.setId(financialActivity);
		financialActivity.setFinancialActivityKey("01");
		financialActivity.setFinActivityDate(lob.getCwaDate());
		financialActivity.setFinEffDate(lob.getInforcePaymentDate());
		financialActivity.setFinActivityType(Integer.toString(lob.getInforcePaymentType()));
		String sourceType = getSourceType(nbaDst);
		if (sourceType != null && sourceType.equals(NbaConstants.A_ST_XML508)) {
			if (lob.getCostBasis() >= 0) {
				financialActivity.setCostBasisAdjAmt(lob.getCostBasis());
			}
		} else {
			if (lob.getCostBasis() > 0) {
				financialActivity.setCostBasisAdjAmt(lob.getCostBasis());
			}
		}
		if (lob.getCwaAmount() > 0) {
			financialActivity.setFinActivityGrossAmt(lob.getCwaAmount());
		} else {
			financialActivity.setFinActivityGrossAmt(lob.getCheckAmount());
		}
		financialActivity.setUserCode(user.getUserID().substring(3, 7));
		financialActivity.getActionIndicator().setAdd();
		Holding holding = NbaTXLife.getPrimaryHoldingFromOLifE(newNbaTxLife.getOLifE());
		holding.getPolicy().setPolicyStatus(null);
		holding.getPolicy().setPaymentDueDate(lob.getInforcePaymentDate()); //SPR1775 
		holding.getPolicy().addFinancialActivity(financialActivity);
		return newNbaTxLife;
	}
	/**
		 * This method returns the type of source that is attached to the work item
		 * @return java.lang.String Source type
		 */
	protected String getSourceType(NbaDst nbaDst) {
		if (nbaDst.getNbaSources().size() > 0) {
			NbaSource nbaSource = (NbaSource) nbaDst.getNbaSources().get(0);
			return nbaSource.getSource().getSourceType();
		} else {
			return null;
		}
	}
}

