package com.csc.fsg.nba.business.process;

/*
 * ************************************************************** <BR>
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
 *     Copyright (c) 2002-2009 Computer Sciences Corporation. All Rights Reserved.<BR>
 * ************************************************************** <BR>
 */

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;

import com.csc.fsg.nba.business.transaction.NbaRequirementUtils;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaVpmsException;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.NbaXMLDecorator;
import com.csc.fsg.nba.vo.nbaschema.AutomatedProcess;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;
import com.csc.fsg.nba.vpms.results.VpmsModelResult;

/**
 * NbaProcRequirementOrdered2 attempts to match requirement results (received electronically) with ordered requirement.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>AXAL3.7.20G</td><td>AXA Life Phase 1</td><td>Workflow Gaps</td></tr>
 * <tr><td>ALPC137</td><td>AXA Life Phase 1</td><td>Miscellaneous Mail</td></tr>
 * <tr><td>AXAL3.7.20R</td><td>AxaLife Phase 1</td><td>Replacement Processing</td></tr>
 * <tr><td>ALS4937</td><td>AxaLife Phase 1</td><td>QC # 4095 - AXAL03.07.06 Lab Results go to Error when they arrive before an Application is scanned/entered</td></tr>
 */

public class NbaProcRequirementOrdered2 extends NbaProcRequirementOrdered {

	/** The static string representing PROCESS_PROBLEM */
	private final static java.lang.String PROCESS_PROBLEM = "Provider Results Matching problem:";
	private static final String SUSPEND_DAYS_335 = "335"; //NBLXA-1719 NBLXA-2119
	private static final int SUSPEND_DAYS_365 = 365; //NBLXA-2579
	
	public NbaAutomatedProcessResult executeProcess(NbaUserVO user, NbaDst work) throws NbaBaseException {
		return super.executeProcess(user, work);
	}

	/**
	 * Process unmatched miscellaneous mail work items, suspend them if APHL is not present on it
	 * Send to error queue with unmatched status if already suspended once
	 * @throws NbaBaseException throw base exception if unable to process unmatched work
	 */
    //ALPC137 New Method
	public void processUnmatchedMiscWork() throws NbaBaseException {
		NbaLob workLob = work.getNbaLob();
		if (isReg60PreSale()) {
			hasParentInd = false;
			reinitializeStatusFields();
			generateApplication();
			changeStatus(getPassStatus());
			addComment("Reg60 PreSale work item.");
			doUpdateWorkItem();
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, getPassStatus(), getPassStatus()));
			
			return;
		}
		VpmsModelResult maxSuspendData = getDataFromVpms(NbaVpmsAdaptor.EP_GET_MAXIMUM_SUSPENDS_ALLOWED);
		String maxSuspendCount = getFirstResult(maxSuspendData);
		int maxSuspends = Integer.parseInt(maxSuspendCount);
		//ALS5113 Begin
		Date createDate = NbaUtils.getDateFromStringInAWDFormat(workLob.getCreateDate());
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTime(createDate);
		calendar.add(Calendar.DAY_OF_WEEK, maxSuspends);
		Date maxSuspendDate = (calendar.getTime());
		if((new Date()).after(maxSuspendDate)){
		//ALS5113 end
			changeStatus(getFailStatus());
			addComment("Miscellaneous work not matched");
			doUpdateWorkItem();
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, getFailStatus(), getFailStatus()));
		} else {
			//ALS5113 removed
			VpmsModelResult data = getDataFromVpms(NbaVpmsAdaptor.EP_CASE_MATCH_SUSPEND_DAYS);  //AXAL3.7.20R //ALS5113
			String suspendDays = getFirstResult(data);
			int suspendDay = Integer.parseInt(suspendDays);
			if (suspendDay > 0) {
				calendar = new GregorianCalendar(); //ALS5113
				calendar.setTime(new Date());
				calendar.add(Calendar.DAY_OF_WEEK, suspendDay);
				Date suspendDate = (calendar.getTime());
				addComment("Suspended awaiting matching case");
				//ALS5113 removed
				suspendTransaction(suspendDate);
			}
		}
	}
	//ALS4937.  Override for temp requriements
	public void processUnmatchedWorkitem() throws NbaBaseException {
		setReqCtrlSrc(getWork().getRequirementControlSource());
		//NBA188 code deleted
		if (reqCtrlSrc == null) { // add a new source to the case
			NbaXMLDecorator xmlDecorator = new NbaXMLDecorator();
			xmlDecorator.addRequirement(getWork().getNbaTransaction(), getWork().getID());
			NbaRequirementUtils reqUtils = new NbaRequirementUtils(); //ACN014
			reqUtils.updateRequirementControlSource(null, work.getNbaTransaction(), xmlDecorator.toXmlString(), NbaRequirementUtils.actionAdd); //ACN014 SPR2992
			setReqCtrlSrc(getWork().getRequirementControlSource());
		}
		setReqCtlSrcXml(new NbaXMLDecorator(reqCtrlSrc.getText()));
		AutomatedProcess ap = reqCtlSrcXml.getAutomatedProcess(NbaUtils.getBusinessProcessId(getUser())); //SPR2639
		if (ap == null) {
			ap = new AutomatedProcess();
			ap.setProcessId(NbaUtils.getBusinessProcessId(getUser())); //SPR2639
			getReqCtlSrcXml().getRequirement().addAutomatedProcess(ap); //NBA192
		}
		//ALS5113 Begin
		
		// APSL5335 :: START -- Informative Comments when Six month Old Cases, NBLXA-2437 moved code outside if 
		String strComments = null;
		String commentsForTwelveMonth = null; //NBLXA-2437
		if(sixMonthsOldAppMatchingCases !=null && !sixMonthsOldAppMatchingCases.isEmpty() && sixMonthsOldAppMatchingCases.size()>0){
			strComments = getCommentsWithSixMonthsOldDispPolicy(sixMonthsOldAppMatchingCases,6);
		}
		if(strComments !=null){
			addComment(strComments);
		}
		//Begin NBLXA-2437
		if (!NbaUtils.isBlankOrNull(twelveMonthsOldAppMatchingCases)) {
			commentsForTwelveMonth = getCommentsWithSixMonthsOldDispPolicy(twelveMonthsOldAppMatchingCases,12);
		}
		if (commentsForTwelveMonth !=null) {
			addComment(commentsForTwelveMonth);
		} 
		// End NBLXA-2437
		if(strComments == null && commentsForTwelveMonth == null  ) { //NBLXA-2437
			addComment("Miscellaneous work not matched");
		}
		// APSL5335 :: END 
		
		VpmsModelResult maxSuspendData = getDataFromVpms(NbaVpmsAdaptor.EP_GET_MAXIMUM_SUSPENDS_ALLOWED);
		String maxSuspendCount = getFirstResult(maxSuspendData);
		int maxSuspends = Integer.parseInt(maxSuspendCount);
		Date createDate = NbaUtils.getDateFromStringInAWDFormat(getWork().getNbaLob().getCreateDate());
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTime(createDate);
		calendar.add(Calendar.DAY_OF_WEEK, maxSuspends);
		Date maxSuspendDate = (calendar.getTime());
		if((new Date()).after(maxSuspendDate)){
				//End ALS5113
				changeStatus(getFailStatus());
				doUpdateWorkItem();
				//NBA020 code deleted
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, getFailStatus(), getFailStatus()));
			} else {
				//Begin NBLXA-1719
				int suspendDay = SUSPEND_DAYS_365; // NBLXA-2579
				calendar = new GregorianCalendar(); // ALS5113
				calendar.setTime(new Date());
				calendar.add(Calendar.DAY_OF_WEEK, suspendDay);
				Date suspendDate = (calendar.getTime());
				setFollowupdate(calendar, suspendDay);// ALS4843
				addComment("Suspended awaiting matching case");
				// Update the Requirement Control Source to indicate that the work item was suspended by this process
				NbaRequirementUtils reqUtils = new NbaRequirementUtils();
				reqUtils.updateRequirementControlSource(null, work.getNbaTransaction(), getReqCtlSrcXml().toXmlString(),
						NbaRequirementUtils.actionUpdate);
				suspendTransaction(suspendDate);
				// End ALS5113

		} //ALS5113 removed
	}
//	 AXAL3.7.20R New Method
	protected int getSuspendDays() throws NbaBaseException, NbaVpmsException { 
		//Begin AXAL3.7.20R  
		//First try VPMS, if "-" is returned, use FollowupFrequency instead.
		int nextSuspend = getSuspendCount(work.getNbaLob()) + 1;
		HashMap deOink = new HashMap();
		deOink.put("A_NumberOfSuspends", String.valueOf(nextSuspend));
		VpmsModelResult data = getDataFromVpms(NbaVpmsAdaptor.REQUIREMENTS, NbaVpmsAdaptor.EP_MATCH_SUSPEND_DAYS, deOink, PROCESS_PROBLEM);

		int suspendDays = 0;
		String suspendDaysStr = getFirstResult(data); //ACN020
		if ( suspendDaysStr.length() > 0 && !suspendDaysStr.equals("-") ) {
			suspendDays = Integer.parseInt(suspendDaysStr.trim());
		}
		else {
			suspendDays = getFollowUpFrequency(work.getNbaLob().getReqUniqueID());
		}
		//end AXAL3.7.20R  
		return suspendDays;
    }
}
