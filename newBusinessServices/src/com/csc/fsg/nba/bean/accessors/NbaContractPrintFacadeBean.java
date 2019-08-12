package com.csc.fsg.nba.bean.accessors;
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
import java.util.Iterator;

import com.csc.fsg.nba.business.process.NbaProcessStatusProvider;
import com.csc.fsg.nba.business.process.NbaProcessWorkItemProvider;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.process.workflow.WorkflowServiceHelper;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaTransaction;
import com.csc.fsg.nba.vo.NbaUserVO;
/**
 * Create or update contract print extract work items. For new contract print extract work items,
 * a new Transaction is added to the Case. For reprints of contract print extract work items, the
 * Transaction status is changed based on the results of a VPMS model.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA012</td><td>Version 2</td><td>Initial Development</td></tr>
 * <tr><td>NBA100</td><td>Version 4</td><td>Create Contract Print Extracts for new Business Documents</td></tr>
 * <tr><td>NBA103</td><td>Version 4</td><td>Logging</td></tr>
 * <tr><td>NBA188</td><td>Version 7</td><td>nbA XML Sources to Auxiliary</td></tr>
 * <tr><td>NBA213</td><td>Version 7</td><td>Unified User Interface</td></tr>
 * <tr><td>AXAL3.7.14</td><td>AXA Life Phase 1</td><td>Contract Print</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @see com.csc.fsg.nba.business.support.NbaLogonViewHelper
 * @since New Business Accelerator - Version 2
 */
public class NbaContractPrintFacadeBean {	//NBA213
//NBA213 code deleted
//NBA103
	public NbaDst generateContractExtract(NbaUserVO userVO, NbaDst nbaDst, String extComp, boolean reprint, String vendor) throws NbaBaseException { // SR787006-APSL3702,APSL5111
		try {// NBA103
				// NBA213 code deleted

			NbaUserVO tempUserVO = new NbaUserVO("NBPRINT", "");
			NbaProcessWorkItemProvider provider = new NbaProcessWorkItemProvider(tempUserVO, nbaDst);
			//BEGIN NBLXA-1632
			if (NbaUtils.isBlankOrNull(provider.getInitialStatus())) {
				return null;
			}//END NBLXA-1632
			NbaTransaction nbaTransaction = nbaDst.addTransaction(provider.getWorkType(), provider.getInitialStatus());
			nbaTransaction.increasePriority(provider.getWIAction(), provider.getWIPriority()); // NBA020
			// Copy lobs from the case to the new transaction
			// begin NBA100
			NbaLob workNbaLob = nbaDst.getNbaLob();
			NbaLob tempTransNbaLob = nbaTransaction.getNbaLob();
			tempTransNbaLob.setPolicyNumber(workNbaLob.getPolicyNumber());
			tempTransNbaLob.setSsnTin(workNbaLob.getSsnTin());
			tempTransNbaLob.setTaxIdType(workNbaLob.getTaxIdType());
			tempTransNbaLob.setLastName(workNbaLob.getLastName());
			tempTransNbaLob.setFirstName(workNbaLob.getFirstName());
			tempTransNbaLob.setMiddleInitial(workNbaLob.getMiddleInitial());
			tempTransNbaLob.setCompany(workNbaLob.getCompany());
			tempTransNbaLob.setReview(workNbaLob.getReview());
			tempTransNbaLob.setAppDate(workNbaLob.getAppDate());
			tempTransNbaLob.setAppState(workNbaLob.getAppState());
			tempTransNbaLob.setIssueOthrApplied(workNbaLob.getIssueOthrApplied());
			tempTransNbaLob.setExtractComp(extComp);
			if (reprint) { // SR787006-APSL3702
				tempTransNbaLob.setPrintExtract(NbaConstants.REPRINT_EXTRACT);	//AXAL3.7.14 //APSL5055
			}
			if (vendor != null) { // APSL5111
				tempTransNbaLob.setReqVendor(vendor);
			}
			tempTransNbaLob.setFaceAmount(workNbaLob.getFaceAmount());// ALS5690
			tempTransNbaLob.setAgentID(workNbaLob.getAgentID());// ALS5690
			tempTransNbaLob.setWritingAgency(workNbaLob.getWritingAgency()); //NBLXA-2585
			tempTransNbaLob.setDistChannel(String.valueOf(workNbaLob.getDistChannel()));// ALS5690
			tempTransNbaLob.setReceiptDate(workNbaLob.getReceiptDate());// QC8125
			tempTransNbaLob.setPaidChgCMQueue(workNbaLob.getPaidChgCMQueue());// Start APSL5128
			tempTransNbaLob.setCaseManagerQueue(workNbaLob.getCaseManagerQueue());
			tempTransNbaLob.setUndwrtQueue(workNbaLob.getUndwrtQueue());
			tempTransNbaLob.setSpecialCase(workNbaLob.getSpecialCase());
			tempTransNbaLob.setReplacementIndicator(workNbaLob.getReplacementIndicator());
			tempTransNbaLob.setExchangeReplace(workNbaLob.getExchangeReplace()); // End APSL5128
			// end NBA100
			nbaDst.setUpdate();
			// NBA213 code deleted

			// Call netserver update method
			return WorkflowServiceHelper.update(userVO, nbaDst);// NBA188 NBA213
			// NBA213 code deleted
		} catch (NbaBaseException e) {// NBA103
			NbaLogFactory.getLogger(this.getClass()).logException(e);// NBA103
			throw e;// NBA103
		} catch (Throwable t) {// NBA103
			NbaBaseException e = new NbaBaseException(t);// NBA103
			NbaLogFactory.getLogger(this.getClass()).logException(e);// NBA103
			throw e;// NBA103
		}
	}
//NBA213 code deleted
/**
 * Send the Contract Print Extract for reprinting to EXTCREATED queue
 * @param userVO the user value object with the user id
 * @param nbaDst the NbaDst object encapsulating the AWD
 * @exception com.csc.fsg.nba.exception.NbaBaseException
 */
//NBA103
public void reprintContract(NbaUserVO userVO, NbaDst nbaDst, String transactionID) throws NbaBaseException {
	try {//NBA103
		//begin NBA213
		Iterator it = nbaDst.getNbaTransactions().iterator();
		NbaTransaction nbaTransaction;
		while (it.hasNext()){
		    nbaTransaction = (NbaTransaction) it.next();
			if (nbaTransaction.getID().equalsIgnoreCase(transactionID)) {
			    NbaUserVO tempUserVO = new NbaUserVO("NBPRINT", "");
			    NbaProcessStatusProvider nbaProcessStatusProvider = new NbaProcessStatusProvider(tempUserVO, nbaDst);
			    nbaTransaction.setStatus(nbaProcessStatusProvider.getPassStatus());
			    nbaTransaction.increasePriority(nbaProcessStatusProvider.getWIAction(), nbaProcessStatusProvider.getWIPriority());
			    //Call netserver update method
				WorkflowServiceHelper.update(userVO, nbaDst);
				break;
			}

		}
		//end NBA213
	} catch (NbaBaseException e) {//NBA103
		NbaLogFactory.getLogger(this.getClass()).logException(e);//NBA103
		throw e;//NBA103
	} catch (Throwable t) {//NBA103
		NbaBaseException e = new NbaBaseException(t);//NBA103
		NbaLogFactory.getLogger(this.getClass()).logException(e);	//NBA103
		throw e;//NBA103
	}
}
//NBA213 code deleted
}
