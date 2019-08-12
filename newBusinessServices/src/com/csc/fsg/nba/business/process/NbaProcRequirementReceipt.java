package com.csc.fsg.nba.business.process;

/*
 * **************************************************************<BR>
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
 * **************************************************************<BR>
 */

import java.util.Calendar;
import java.util.Date;

import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.txlife.OLifEExtension;
import com.csc.fsg.nba.vo.txlife.RequirementInfoExtension;

/**
 * NbaProcRequirementReceipt is the class to process transactions found in NBRECEPT queue.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA001</td><td>Version 1</td><td>Initial Development</td></tr>
 * <tr><td>NBA020</td><td>Version 2</td><td>AWD Priority</td></tr>
 * <tr><td>NBA050</td><td>Version 3</td><td>Pending Database</td></tr>
 * <tr><td>NBA044</td><td>Version 3</td><td>Architecture changes</td></tr>
 * <tr><td>SPR1851</td><td>Version 4</td><td>Locking Issues</td></tr>
 * <tr><td>ACN012</td><td>Version 4</td><td>Architecture Changes</td></tr>
 * <tr><td>NBA130</td><td>Version 6</td><td>Requirements Reinsurance Project</td></tr>
 * <tr><td>SPR2992</td><td>Version 6</td><td>General Code Clean Up Issues for Version 6</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @see com.csc.fsg.nba.business.process.NbaAutomatedProcess
 * @since New Business Accelerator - Version 1
 */
public class NbaProcRequirementReceipt extends NbaAutomatedProcess {
	/**
	 * NbaProcRequirementReceipt constructor comment.
	 */
	public NbaProcRequirementReceipt() {
		super();
		//SPR1851 code deleted
	}
	/**
	 * This method locates the requirement to be updated in the NbaTXLife structure
	 *  and updates applicable fields to indicate that the requirements has been
	 *  received.
	 * @return <code>true</code> if the requirement is located and updated;
	 * <code>false</code> is returned if the party is not located or the requirement does
	 *  not exist on the contract.
	 * @throws NbaBaseException
	 */
	// NBA050 New Method, NBA130 Changed method signature
	public void updateRequirementInfoObject() throws NbaBaseException {
		//Begin NBA130
		setReceiptDate();
		requirementInfo.setReqStatus(getWorkLobs().getReqStatus()); //SPR2992
		requirementInfo.setReceivedDate(getWorkLobs().getReqReceiptDate()); //SPR2992
		requirementInfo.setStatusDate(Calendar.getInstance().getTime());
		requirementInfo.setFulfilledDate(getWorkLobs().getReqReceiptDate()); //SPR2992
		requirementInfo.getActionIndicator().setUpdate();
		//End NBA130
		
		RequirementInfoExtension requirementInfoExt = NbaUtils.getFirstRequirementInfoExtension(requirementInfo);
		if (requirementInfoExt == null) {
			OLifEExtension olifeExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_REQUIREMENTINFO);
			requirementInfoExt = olifeExt.getRequirementInfoExtension();
		}
		//Begin AXAL3.7.01
		if (requirementInfo.getReqCode() == NbaOliConstants.OLI_REQCODE_PREMDUE) {
			//QC20240 - Moved code outside from if
			requirementInfoExt.setPremiumDueCarrierReceiptDate(getWorkLobs().getReqReceiptDate());			
		}
		//End AXAL3.7.01
		requirementInfoExt.setReceivedDateTime(getWorkLobs().getReqReceiptDateTime());//QC20240
		requirementInfoExt.setActionUpdate();
		
	}
	/**
	 * Set the Receipt Date LOB.
	 * @throws NbaBaseException
	 */
	public void setReceiptDate() throws NbaBaseException {
		if (null == getWorkLobs().getReqReceiptDate()) { //SPR2992
		    getWorkLobs().setReqReceiptDate(new Date()); //SPR2992
		    getWorkLobs().setReqReceiptDateTime(NbaUtils.getStringFromDateAndTime(new Date())); //QC20240
		}
	}

	/**
	 * This method post a receipt or waived status for a requirement
	 * to the back-end system.Update the AWD in order to sync with
	 * the back-emd system.
	 * @param user the user for whom the process is being executed
	 * @param work a DST value object for which the process is to occur
	 * @return an NbaAutomatedProcessResult containing information about
	 *         the success or failure of the process
	 * @throws NbaBaseException
	 */
	public NbaAutomatedProcessResult executeProcess(com.csc.fsg.nba.vo.NbaUserVO user, com.csc.fsg.nba.vo.NbaDst work)
		throws com.csc.fsg.nba.exception.NbaBaseException {
		// Initialization
	    
	    //SPR2922 Code deleted
	    
		if (!initialize(user, work)) {
			return getResult(); //NBA050
		}
		//begin SPR2992		
	    if (getWorkLobs().getReqStatus() == null || getWorkLobs().getReqStatus().equals(String.valueOf(NbaOliConstants.OLI_REQSTAT_ADD))
                || getWorkLobs().getReqStatus().equals(String.valueOf(NbaOliConstants.OLI_REQSTAT_ORDER))) {
            getWorkLobs().setReqStatus(String.valueOf(NbaOliConstants.OLI_REQSTAT_RECEIVED));
        }
	    //end SPR2992
		// NBA050 BEGIN
		//Begin NBA130
		updateRequirementInfoObject();
		handleHostResponse(doContractUpdate(nbaTxLife));
		// END NBA130
		// NBA050 END
		if (getResult() == null) {
			// NBA050 CODE DELETED
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", getPassStatus()));
		}
		changeStatus(getResult().getStatus());
		doUpdateWorkItem();
		//NBA020 code deleted
		return getResult();
	}

}
