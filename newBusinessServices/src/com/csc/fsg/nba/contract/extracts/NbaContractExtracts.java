package com.csc.fsg.nba.contract.extracts;
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
 *     Copyright (c) 2002-2008 Computer Sciences Corporation. All Rights Reserved.<BR>
 * *******************************************************************************<BR>
 */
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;
/**
* NbaContractExtracts class calls two different classes to create Accounting Extracts for Pending CWA Payments and 
* Disbursement Extracts for the refund of CWA.
* <p>
* <b>Modifications:</b><br>
* <table border=0 cellspacing=5 cellpadding=5>
* <thead>
* <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
* </thead>
* <tr><td>SPR1656</td><td>Version 4</td><td>Allow for Refund/Reversal Extracts.</td></tr>
* <tr><td>ACN012</td><td>Version 4</td><td>Architecture Changes.</td></tr>
* <tr><td>SPR1906</td><td>Version 4</td><td>General source code clean up</td></tr>
* <tr><td>SPR2817</td><td>Version 6</td><td>Pending Accounting Needs to Be Added to nbA</td></tr>
* <tr><td>NBA208-26</td><td>Version 7</td><td>Remove synchronized keyword from getLogger() methods</td></tr>
* <tr><td>AXAL3.7.23</td><td>AXA life Phase 1</td><td>Accounting Interface</td></tr>
* <tr><td>NBA228</td><td>Version 8</td><td>Cash Management Enhancement</td></tr>
* <tr><td>P2AXAL019</td><td>AXA Life Phase 2</td><td>Cash Management Enhancement</td></tr>
* <tr><td>ALPC119</td><td>AXA Life Phase 1</td><td>YRT Discount Accounting </td></tr>
* <tr><td>AXAL3.7.04</td><td>AXA life Phase 1</td><td>Paid Changes</td></tr> 
* </table>
* <p>
* @author CSC FSG Developer
 * @version 7.0.0
* @since New Business Accelerator - Version 4
*/
public class NbaContractExtracts {

	protected static NbaLogger logger = null;
	/**
	* Return my <code>NbaLogger</code> implementation (e.g. NbaLogService).
	* @return com.csc.fsg.nba.foundation.NbaLogger
	*/
	protected static NbaLogger getLogger() { // NBA208-26
		if (logger == null) {
			try {
				logger = NbaLogFactory.getLogger(NbaContractExtracts.class.getName());  //SPR1906
			} catch (Exception e) {
				NbaBootLogger.log("NbaContractExtracts could not get a logger from the factory.");
				e.printStackTrace(System.out);
			}
		}
		return logger;
	}

	/**
	 * This method calls NbaCwaPaymentsExtract and NbaDisbursementExtract classes to generate cwa payments extracts 
	 * and disbursement extracts and returns the Lits of connections.
	 * @param nbaTXLife NbaTXLife object
	 * @param connections List object
	 * @return connections List object
	 * @throws NbaBaseException
	 */
	//SPR2817 added new parameter, isIssue
	//AXAL3.7.23 - added parameter NbaUserVO	
	//NBA228 - added parameter dst
	//APSL2440 Internal Perfomance Issue changed method signature
	public void createExtracts(NbaTXLife nbaTXLife, NbaDst dst, boolean isIssue, NbaUserVO user) throws NbaBaseException {
		//APSL2440 Internal Perfomance Issue code deleted
		NbaCwaPaymentsExtract cwaPaymentsExtract = new NbaCwaPaymentsExtract();
		//begin SPR2817
		if (isIssue) {
			cwaPaymentsExtract.createAccountingExtractForPendingCWAPayment(nbaTXLife, dst, NbaConstants.ACCOUNTING_FOR_ISSUE, user); //APSL3460
			cwaPaymentsExtract.createCarryOverLoanExtract(nbaTXLife, user);//P2AXAL019 create 508 for any carry overloan case
			//NBA228 - create 508 accounting for over/short
			if (!nbaTXLife.isPaidReIssue()) {//AXAL3.7.04, APSL459
				cwaPaymentsExtract.createShortageAccountingExtract(nbaTXLife, dst, user); //NBA228 NBLXA-1457
				//APSL2704 QC10395 changed method signature - restrict multiple YRT webservice invoke calls.
				cwaPaymentsExtract.createYRTDiscountAccountingExtract(nbaTXLife, user,dst.getWorkType()); //ALPC119 
			}
		} else {
			cwaPaymentsExtract.createAccountingExtractForPendingCWAPayment(nbaTXLife, dst, NbaConstants.ACCOUNTING_FOR_NEW_CWA, user); //APSL3460
		}
		//end SPR2817
		new NbaDisbursementExtract().createDisbursementExtractForCWARefund(nbaTXLife, user, isIssue); //NBLXA-1457
		//APSL2440 Internal Perfomance Issue code deleted		
	}
}