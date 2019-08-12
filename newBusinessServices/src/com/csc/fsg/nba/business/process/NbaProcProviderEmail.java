package com.csc.fsg.nba.business.process;

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

import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Map;

import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.datamanipulation.NbaOinkRequest;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaEmailValidationException;
import com.csc.fsg.nba.exception.NbaVpmsException;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaAwdRetrieveOptionsVO;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaEmailVO;
import com.csc.fsg.nba.vo.NbaSource;
import com.csc.fsg.nba.vo.NbaSuspendVO;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTransOliCode;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.Attachment;
import com.csc.fsg.nba.vo.txlife.Party;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.RequirementInfo;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;
import com.csc.fsg.nba.vpms.NbaVpmsConstants;
import com.csc.fsg.nba.vpms.NbaVpmsResultsData;

/**
 * <code>NbaProcProviderEmail</code> handles communications from nbA
 * to producers and owners via email.
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA008</td><td>Version 2</td><td>Requirements Ordering and Receipting</td></tr>
 * <tr><td>NBA020</td><td>Version 2</td><td>AWD Priority</td></tr>
 * <tr><td>NBA027</td><td>Version 3</td><td>Performance Tuning</td></tr>
 * <tr><td>NBA044</td><td>Version 3</td><td>Architecture Changes</td></tr>
 * <tr><td>SPR1359</td><td>Version 3</td><td>Automated processes stop poller when unable to lock supplementary work items</td></tr>
 * <tr><td>SPR1770</td><td>Version 4</td><td>When requirements are first ordered and moved to the NBORDERD queue they should not be suspended initially.</td></tr>
 * <tr><td>SPR1926</td><td>Version 4</td><td>Email automated process stops with error message "Error sending the email-Email address is not valid"</td><tr>
 * <tr><td>NBA103</td><td>Version 4</td><td>Logging</td></tr>
 * <tr><td>SPR2380</td><td>Version 5</td><td>Cleanup</td></tr> 
 * <tr><td>SPR2639</td><td>Version 5</td><td>Automated process status should be based business function</td></tr>
 * <tr><td>NBA130</td><td>Version 6</td><td>Requirements Reinsurance Project</td></tr>
 * <tr><td>NBA213</td><td>Version 7</td><td>Unified User Interface</td></tr>
 * <tr><td>SPR3362</td><td>Version 7</td><td>Exceptions in Automated Processes and Logon Service Due to VP/MS Memory Leak</td></tr>
 * <tr><td>NBA196</td><td>Version 7</td><td>JCA Adapter for Email</td></tr>
 * <tr><td>SPR3290</td><td>Version 7</td><td>General source code clean up during version 7</td></tr>
 * <tr><td>NBA250</td><td>AXA Life Phase 1</td><td>nbA Requirement Form and Producer Email Management Project</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 2
 */
public class NbaProcProviderEmail extends NbaAutomatedProcess {
	//SPR2380 removed logger
/**
 * NbaProcProviderEmsi constructor comment.
 */
public NbaProcProviderEmail() {
	super();
}

//NBA103 - removed method

/**
 * This abstract method must be implemented by each subclass in order to
 * execute the automated process.
 * @param user the user/process for whom the process is being executed
 * @param work a DST value object for which the process is to occur
 * @return an NbaAutomatedProcessResult containing information about
 *         the success or failure of the process
 * properly.
 * @throws NbaBaseException
 */
public NbaAutomatedProcessResult executeProcess(NbaUserVO user, NbaDst work) throws NbaBaseException {
	if (getLogger().isDebugEnabled()) { //SPR1926
		getLogger().logDebug("ProviderEmail for contract " + work.getNbaLob().getPolicyNumber()); //SPR1926
	}
	// Initialization
	if (initialize(user, work)) {//SPR1926
		//SPR1926 code deleted
		// retrieve the sources for this work item
		//NBA213 deleted code
		NbaAwdRetrieveOptionsVO retrieveOptionsValueObject = new NbaAwdRetrieveOptionsVO();
		retrieveOptionsValueObject.setWorkItem(getWork().getID(), false);
		retrieveOptionsValueObject.requestSources();
		retrieveOptionsValueObject.setLockWorkItem();
		setWork(retrieveWorkItem(getUser(), retrieveOptionsValueObject));  //NBA213
		//NBA213 deleted code
		try { //SPR1926
			sendEMail();
			//begin SPR1926
		} catch (NbaEmailValidationException neve) {
			if ((neve.getMessage().equals(NbaEmailValidationException.EMAIL_ADDRESS))
				|| (neve.getMessage().equals(NbaEmailValidationException.EMAIL_VO))) {
				addComment(neve.getMessage());
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "", getFailStatus()));
			} else {
				throw neve;
			}
			//end SPR1926
		}
		changeStatus(getResult().getStatus());
		int suspendDays = getSuspendDays();
		if ((suspendDays <= 0) || (getResult().getReturnCode() == NbaAutomatedProcessResult.FAILED)) { //SPR1770 //SPR1926
			doUpdateWorkItem(); // also unlocks the case
		} else {
			suspendTransaction(suspendDays);
		}
	}
	//NBA020 code deleted
	return getResult();
}
/**
 * This method creates and initializes an <code>NbaVpmsAdaptor</code> object to
 * call VP/MS to execute the supplied entryPoint.
 * @return the results from the VP/MS call in the form of an <code>NbaVpmsResultsData</code> object
 * @throws NbaVpmsException
 */
public NbaVpmsResultsData getDataFromVpms(String entryPoint) throws NbaBaseException, NbaVpmsException {
    NbaVpmsAdaptor vpmsProxy = null; //SPR3362
        try {
            NbaOinkDataAccess oinkData = new NbaOinkDataAccess(getWork().getNbaLob());
            Map deOink = new HashMap();
            deOink.put(NbaVpmsAdaptor.A_PROCESS_ID, NbaUtils.getBusinessProcessId(getUser())); //SPR2639
            vpmsProxy = new NbaVpmsAdaptor(oinkData, NbaVpmsAdaptor.REQUIREMENTS); //SPR3362
            //BEGIN NBA130
            NbaOinkRequest oinkRequest = new NbaOinkRequest();
            oinkRequest.setRequirementIdFilter(requirementInfo.getId());
            vpmsProxy.setANbaOinkRequest(oinkRequest);
            oinkData.setContractSource(getNbaTxLife());
            //END NBA130
            vpmsProxy.setVpmsEntryPoint(entryPoint);
            vpmsProxy.setSkipAttributesMap(deOink);
            NbaVpmsResultsData data = new NbaVpmsResultsData(vpmsProxy.getResults());
            //SPR3362 code deleted
            return data;
        } catch (java.rmi.RemoteException re) {
            throw new NbaVpmsException("Provider Communication Process Problem" + NbaVpmsException.VPMS_EXCEPTION, re);
            //begin SPR3362
        } finally {
            try {
                if (vpmsProxy != null) {
					vpmsProxy.remove();					
				}
            } catch (RemoteException re) {
                getLogger().logError(NbaBaseException.VPMS_REMOVAL_FAILED);
            }
        }
		//end SPR3362
}
/**
 * This method suspends a work item by using the work item information and
 * the supplied suspend date to populate the suspendVO.
 * @param suspendDays the number of days to suspend
 * @throws NbaBaseException
 */
	public int getSuspendDays() throws NbaBaseException {
		getLogger().logDebug("Getting suspend days"); //NBA044
		NbaVpmsResultsData data = getDataFromVpms(NbaVpmsAdaptor.EP_GET_SUSPEND_DAYS);
		int suspendDays = 0;
		if (data != null && data.getResultsData() != null) {
			suspendDays = Integer.parseInt((String) data.getResultsData().get(0)); //NBA027
		}
		if (isResetFollowUpDaysNeeded()) {//ALS4843
			return suspendDays;
		}
		return getFollowUpFrequency(getWork().getNbaLob().getReqUniqueID());//ALS4843
	}
/**
 * This method first retrieve the xml source from the requirement work item. 
 * then tt retrieves all the required information from this source.
 * It instantiates the Email service and send the mail to the recipient.
 * @throws NbaBaseException
 * 
 */
public void sendEMail() throws NbaBaseException {
		ListIterator sourceList = getWork().getNbaSources().listIterator();
		NbaSource theSource = null;
		// SPR3290 code deleted
		while (sourceList.hasNext()) {
				theSource = (NbaSource) sourceList.next();
				if (theSource.getSource().getSourceType().equals(NbaConstants.A_ST_REQUIREMENT_XML_TRANSACTION)) {
						break;
				}
		}
		if (theSource == null) {
				addComment("Requirement XML Transaction source is missing");
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "", getFailStatus()));
				return;
		}

		NbaTXLife txLife = null;
		try {
				txLife = new NbaTXLife(theSource.getText());
		} catch (Exception e) {
				throw new NbaBaseException("XML source is invalid");
		}
		NbaEmailVO emailVO = new NbaEmailVO();

		String partyId = null;
		if (getWork().getNbaLob().getReqVendor().equals(NbaConstants.PROVIDER_OWNER)) {
				partyId = txLife.getPartyId(NbaOliConstants.OLI_REL_OWNER);
		} else {
				partyId = txLife.getPartyId(NbaOliConstants.OLI_REL_PRIMAGENT);
		}
		if (partyId == null) {
				throw new NbaBaseException("Invalid provider id");
		}

		//TO Address
		Party party = txLife.getParty(partyId).getParty();
		if (party.getEMailAddressCount() > 0) {
				if (party.getEMailAddressAt(0).getAddrLine() != null) {
						emailVO.setTo(party.getEMailAddressAt(0).getAddrLine().trim());
				}
		} else {
				throw new NbaBaseException("Recipient Email address is missing");
		}

		//REPLY TO Address
		partyId = txLife.getPartyId(NbaOliConstants.OLI_REL_REQUESTEDBY);
		party = txLife.getParty(partyId).getParty();
		if (party.getEMailAddressCount() > 0) {
				if (party.getEMailAddressAt(0).getAddrLine() != null) {
						emailVO.setReplyTo(party.getEMailAddressAt(0).getAddrLine().trim());
				}
		} else {
				throw new NbaBaseException("Reply To Email address is missing");
		}
		//NBA250
		StringBuffer subjectStr = new StringBuffer();
		subjectStr.append(getNbaTxLife().getPolicy().getCarrierCode());
		subjectStr.append(", ");
		subjectStr.append(getNbaTxLife().getPolicy().getPolNumber());
		subjectStr.append(", ");
		subjectStr.append(getNbaTxLife().getPrimaryParty().getFullName());
		emailVO.setSubject(subjectStr.toString());
		StringBuffer bodyStr = new StringBuffer();
		bodyStr.append("Please obtain ");
		try {
			for (int i = 0; i < txLife.getPolicy().getRequirementInfoCount(); i++) {
				RequirementInfo reqInfo = txLife.getPolicy().getRequirementInfoAt(i);
				String reqType = NbaTransOliCode.lookupText(NbaOliConstants.OLI_LU_REQCODE,reqInfo.getReqCode());
				bodyStr.append(reqType + " for " + getNbaTxLife().getPrimaryParty().getFullName() + ", ");
			}
		} catch (Exception ex) {
			throw new NbaBaseException("XML source attachment is invalid/missing");
		}
		emailVO.setBody(bodyStr.toString());
		//End NBA250
		//begin NBA196
		NbaUtils.validateNbaEmailVO(emailVO);	//Throws NbaEmailValidationException when an error occurs	//NBA196
		try {
            sendEmail(getUser(), emailVO);
        } catch (NbaBaseException e) {
            e.forceFatalExceptionType();
            throw e;
        }	
        //end NBA196
        //Begin NBA250
        Policy policy = nbaTxLife.getPolicy();
    	Iterator reqInfoIter = policy.getRequirementInfo().iterator();
    	while( reqInfoIter.hasNext()) {
    		RequirementInfo reqInfo = (RequirementInfo)reqInfoIter.next();
    		if( reqInfo.getRequirementInfoUniqueID() != null && 
    				reqInfo.getRequirementInfoUniqueID().equalsIgnoreCase(getWork().getNbaLob().getReqUniqueID())) {
    			for (int i = 0; i < reqInfo.getAttachmentCount(); i++) {
	    			Attachment attach = reqInfo.getAttachmentAt(i);
	    			if(NbaOliConstants.OLI_ATTACH_REQUIREREQUEST == attach.getAttachmentType()){
	    				reqInfo.removeAttachment(attach);
	    				reqInfo.setActionUpdate();
	    				break;
	    			}
    			}
    		}
    	}
    	setContractAccess(UPDATE); 
    	//End NBA250
    	handleHostResponse(doContractUpdate(nbaTxLife));
		setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", getPassStatus()));
}
/**
 * This method suspends a work item by using the work item information and
 * the supplied suspend date to populate the suspendVO.
 * @param suspendDays the number of days to suspend
 * @throws NbaBaseException
 */
public void suspendTransaction(int suspendDays) throws NbaBaseException {
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTime(new Date());
		calendar.add(Calendar.DAY_OF_WEEK, suspendDays);
		Date reqSusDate = (calendar.getTime());
		addComment("Suspended for provider follow-up");
		NbaSuspendVO suspendVO = new NbaSuspendVO();
		suspendVO.setTransactionID(getWork().getID());
		suspendVO.setActivationDate(reqSusDate);
		updateForSuspend(suspendVO);
}
/**
 * Since the work item must be suspended before it can be unlocked, this method
 * is used instead of the superclass method to update AWD.
 * <P>This method updates the work item in the AWD system, suspends the 
 * work item using the supsendVO, and then unlocks the work item.
 * @param suspendVO the suspend value object created by the process to be used
 *                                  in suspending the work item.
 * @throws NbaBaseException
 */
public void updateForSuspend(NbaSuspendVO suspendVO) throws NbaBaseException {
	getLogger().logDebug("Starting updateForSuspend"); //NBA044
	updateWork(getUser(), getWork());  //NBA213
	suspendWork(getUser(), suspendVO);  //NBA213
	unlockWork(getUser(), getWork());  //NBA213
}

/**
 * Calls VP/MS to check if resetting of followup days needed or not.
 * @return True or False 
 * @throws NbaBaseException
 */
//ALS4843 new Method
protected boolean isResetFollowUpDaysNeeded() throws NbaBaseException {
	NbaVpmsResultsData data = getDataFromVpms(NbaVpmsConstants.EP_IS_RESET_FOLLOWUP_DAYS_NEEDED);
	if (data.getResultsData() != null && data.getResultsData().size() > 0) {
		String strResult = (String) data.getResultsData().get(0);
		if (strResult != null && !strResult.trim().equals("")) {
			return Boolean.valueOf(strResult).booleanValue();
		}
	}
	return false;
}
}
