package com.csc.fsg.nba.business.transaction;

/*
 * **************************************************************************<BR>
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
 * **************************************************************************<BR>
 */

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.csc.dip.jvpms.runtime.base.VpmsComputeResult;
import com.csc.fsg.nba.access.contract.NbaContractAccess;
import com.csc.fsg.nba.business.process.NbaAutoProcessProxy;
import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaDataAccessException;
import com.csc.fsg.nba.exception.NbaNetServerException;
import com.csc.fsg.nba.exception.NbaVpmsException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.process.workflow.WorkflowServiceHelper;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaProcessingErrorComment;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTXRequestVO;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.TXLifeResponse;
import com.csc.fsg.nba.vo.txlife.TransResult;
import com.csc.fsg.nba.vo.txlife.UserAuthResponseAndTXLifeResponseAndTXLifeNotify;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;


/**
 * An abstract class that Accepts coarse-grained business objects from a session façade 
 * or automated process and decomposes them into the proper granularity for invoking 
 * the services of other components (back-end, NetServer, VP/MS, etc).
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 *   <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA001</td><td>Version 1</td><td>Initial Development</td></tr>
 * <tr><td>NBA013</td><td>Version 2</td><td>Correspondence System</td></tr>
 * <tr><td>NBA022</td><td>Version 2</td><td>Case Manager view support</td></tr>
 * <tr><td>NBA027</td><td>Version 3</td><td>Performance Tuning</td></tr>
 * <tr><td>NBA044</td><td>Version 3</td><td>Architecture Changes</td></tr>
 * <tr><td>NBA050</td><td>Version 3</td><td>Nba Pending Database</td></tr>
 * <tr><td>ACN012</td><td>Version 4</td><td>Architecture Changes</td></tr>
 * <tr><td>ACN014</td><td>Version 4</td><td>121/1122 Migration</td></tr>
 * <tr><td>SPR2639</td><td>Version 5</td><td>Automated process status should be based business function</td></tr>
 * <tr><td>NBA213</td><td>Version 7</td><td>Unified User Interface</td></tr>
 * <tr><td>NBA186</td><td>Version 8</td><td>nbA Underwriter Additional Approval and Referral Project</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 1
 */
public abstract class NbaEventToRequest implements NbaConstants {
    protected static NbaLogger logger = null;
    protected NbaUserVO user = null;
    protected NbaDst work = null;
    protected NbaTXLife holdingInq = null;
    protected NbaAutoProcessProxy apProxy;
    protected String statusKey; // NBA022 - new field
   	protected int contractAccess = READ; // NBA050 default value

/**
 * Create an NbaEventToRequest for a user.
 * @param newUser an nbA user
 */
public NbaEventToRequest(NbaUserVO newUser) {
	super();
	this.setUser(newUser);
}
/**
 * Adds a new comment to the AWD system.  The process that added the
 * comment is recorded as the queue name of the AWD work item this
 * process is operating on.
 * @param aComment the comment to be added to the AWD system.
 */
public void addComment(String aComment) {
	addComment(aComment, getWork().getQueue());
}
/**
 * Adds a new comment to the AWD system.
 * @param aComment the comment to be added to the AWD system.
 * @param aProcess the process that added the comment.
 */
public void addComment(String aComment, String aProcess) {
	NbaProcessingErrorComment npec = new NbaProcessingErrorComment();
	npec.setActionAdd();
	npec.setOriginator(getUser().getUserID());
	npec.setEnterDate(NbaUtils.getStringFromDate(new java.util.Date()));
	npec.setProcess(aProcess);
	npec.setText(aComment);
	work.addManualComment(npec.convertToManualComment());
	if (getLogger().isDebugEnabled()) { // NBA027
		getLogger().logDebug("Comment added: " + aComment);
	} // NBA027
}
/**
 * Adds new comments to the AWD system.
 * @param aList  multiple comments that are to be added to the AWD system.
 */
public void addComments(List aList) {
	for (int i = 0; i < aList.size(); i++) {
		addComment((String) aList.get(i));
	};
}
/**
 * Changes the status of the Work Item.
 * @param newStatus the new status for the Work Item
 */
public void changeStatus(String newStatus) {
	getWork().setStatus(newStatus);
	if (getLogger().isDebugEnabled()) { // NBA027
		getLogger().logDebug("Work Item status changed to " + newStatus);
	} // NBA027
}

// NBA050 Code Deleted - method createHoldingInquiry() deleted
/**
 * Submit a holding inquiry request to the contract access facade.
 * @return a value object response from the contract access facade
 */
public NbaTXLife doHoldingInquiry() throws NbaBaseException {
	//return submitRequestToHost(createHoldingInquiry());
	return doHoldingInquiry(contractAccess, null);	//NBA050

}
/**
 * Update the work item in AWD. Unlock the work item and any locked children. 
 */
public void doUpdateWorkItem() throws NbaBaseException {
	//NBA213 deleted code
	try {
		WorkflowServiceHelper.update(getUser(), getWork());  //NBA213
	} catch (NbaBaseException nbe) {
		throw nbe;
	} catch (Exception e) {
		throw new NbaBaseException(NbaNetServerException.UPDATE, e);
	}
	try {
		WorkflowServiceHelper.unlockWork(getUser(), getWork());  //NBA213
	} catch (NbaBaseException nbe) {
		throw nbe;
	} catch (Exception e) {
		throw new NbaBaseException(NbaNetServerException.UNLOCK, e);
	}
}
/**
 * Updates the NbaTXLife value object with information required 
 * for the back end system.
 * @param aNbaTXLife a <code>NbaTXLife</code> value object
 */
public void doXMLMarkup(NbaTXLife aNbaTXLife) {
	aNbaTXLife.doXMLMarkUp();
}
/**
 * This abstract method must be implemented by each subclass in order to
 * execute the event-to-request.
 * @return information about the success or failure of the event-to-request
 */
public abstract NbaEventToRequestResult executeRequest() throws NbaBaseException;
/**
 * Get a proxy to the functionality of <code>NbaAutomatedProcess</code>.
 * @return the proxy
 */
protected com.csc.fsg.nba.business.process.NbaAutoProcessProxy getApProxy() {
	return apProxy;
}
/**
 * Get the holding object that possibly contains changes.
 * @return the nbA decorator of the Acord holding object
 */
protected NbaTXLife getHoldingInq() {
	return holdingInq;
}
/**
 * Return my <code>NbaLogger</code> implementation (e.g. NbaLogService).
 * @return com.csc.fsg.nba.foundation.NbaLogger
 */
protected static NbaLogger getLogger() {
	if (logger == null) {
		try {
			logger = NbaLogFactory.getLogger(NbaEventToRequest.class.getName()); //NBA044
		} catch (Exception e) {
			NbaBootLogger.log("NbaEventToRequest could not get a logger from the factory."); //NBA044
			e.printStackTrace(System.out);
		}
	}
	return logger;
}
//NBA213 deleted code
/**
 * Insert the method's description here.
 * @return com.csc.fsg.nba.vo.NbaUserVO
 */
public com.csc.fsg.nba.vo.NbaUserVO getUser() {
	return user;
}
/**
 * Insert the method's description here.
 * @return com.csc.fsg.nba.vo.NbaDst
 */
public com.csc.fsg.nba.vo.NbaDst getWork() {
	return work;
}
/**
 * Handle the NbaTXLife response from the Back End Adaptor.
 * @param aTXLifeResponse the NbaTXLife response
 */
public void handleHostResponse(NbaTXLife aTXLifeResponse) {
	UserAuthResponseAndTXLifeResponseAndTXLifeNotify allResponses = aTXLifeResponse.getTXLife().getUserAuthResponseAndTXLifeResponseAndTXLifeNotify();
	List responses = allResponses.getTXLifeResponse();
	TXLifeResponse theResponse = (TXLifeResponse) responses.get(0);
	TransResult aTransResult = theResponse.getTransResult();
	long resultCode = aTransResult.getResultCode();
	if (resultCode > 1) {
		ArrayList errors = new ArrayList();
		for (int i = 0; i < aTransResult.getResultInfoCount(); i++) {
			errors.add(aTransResult.getResultInfoAt(i).getResultInfoDesc());
		}
		addComments(errors);
	}
}
/**
 * Provides additional initialization support by setting the
 * case and user objects to the passed in parameters.
 * @param newUser the AWD User for the process
 * @param newWork the NbaDst value object to be processed
 */
public void initialize(NbaUserVO newUser, NbaDst newWork) {
	setUser(newUser);
	setWork(newWork);
}
/**
 * Set the holding object that possibly contains changes.
 * @param newHoldingInq the nbA decorator of the Acord holding object
 */
protected void setHoldingInq(NbaTXLife newHoldingInq) {
	holdingInq = newHoldingInq;
}
/**
 * Set the user.
 * @param newUser the AWD user
 */
public void setUser(NbaUserVO newUser) {
	user = newUser;
}
/**
 * Insert the method's description here.
 * @param newWork com.csc.fsg.nba.vo.NbaDst
 */
public void setWork(com.csc.fsg.nba.vo.NbaDst newWork) {
	work = newWork;
}
/**
 * Submit a holding inquiry request to the back end system.
 * @return a value object response from the back end system
 */
// NBA050 New Method
public NbaTXLife doHoldingInquiry(int access, String businessProcess) throws NbaBaseException {
	try {
		return NbaContractAccess.doContractInquiry(createRequestObject(access,businessProcess));  //NBA213
	} catch (NbaBaseException nbe) {
		if (nbe instanceof NbaDataAccessException) {
			return null;
		} else {
			throw nbe;
		}
	//NBA213 deleted code
	} 
}

// NBA050 Code Deleted - Deleted method submitRequestToHost()

/**
 * Create a TX Request value object that will be used to retrieve the contract.
 * @param access the access intent to be used to retrieve the data, either READ or UPDATE
 * @param businessProcess the name of the business function or process requesting the contract
 * @return a value object that is the request
 */
// NBA050 New Method
public NbaTXRequestVO createRequestObject(int access, String businessProcess) {
	NbaTXRequestVO nbaTXRequest = new NbaTXRequestVO();
	nbaTXRequest.setTransType(NbaOliConstants.TC_TYPE_HOLDINGINQ);
	nbaTXRequest.setTransMode(NbaOliConstants.TC_MODE_ORIGINAL);
	nbaTXRequest.setInquiryLevel(NbaOliConstants.TC_INQLVL_OBJECTALL);
	nbaTXRequest.setNbaLob(getWork().getNbaLob());
	nbaTXRequest.setNbaUser(getUser());
	nbaTXRequest.setWorkitemId(getWork().getID());
	nbaTXRequest.setCaseInd(getWork().isCase()); //ACN014 
	if( access != -1) {
		nbaTXRequest.setAccessIntent(access);  
	} else {
		nbaTXRequest.setAccessIntent(READ);
	}
	if( businessProcess != null) {
		nbaTXRequest.setBusinessProcess(businessProcess);
	} else {
		nbaTXRequest.setBusinessProcess(NbaUtils.getBusinessProcessId(getUser()));  //SPR2639
	}
	return nbaTXRequest;
}
/**
 * Update the LOB fields from the holding inquiry
 */
public void updateLOBFromHoldingInquiry(NbaTXLife nbaTxLife) {
	getWork().getNbaLob().updateLobFromNbaTxLife(nbaTxLife);
}
/**
 * Set the work item to be processed by <code>NbaAutoProcessProxy</code>.
 * @param proxyWork the item to be worked on
 */
protected void useApProxy(NbaDst proxyWork) throws NbaBaseException {
	if (apProxy == null) {
		apProxy = new NbaAutoProcessProxy(getUser(), proxyWork);
	} else {
		apProxy.setWork(proxyWork);
	}
}

/**
 * The method makes a call to the passed entry point of the VP/MS model passed as parameter and retruns the VP/MS results.
 * @param entryPoint the VP/MS model entry point
 * @param model name of the VP/MS model
 * @param deOink map of the deOinked variables
 * @return VpmsComputeResult vpms results
 * @throws NbaBaseException an instance of NbaBaseException
 * @throws NbaVpmsException an instance of NbaVpmsException
 */
//NBA186 new method
public VpmsComputeResult getDataFromVpms(String entryPoint, String model, Map deOink) throws NbaBaseException, NbaVpmsException {
	NbaVpmsAdaptor vpmsProxy = null;
	try {
		NbaOinkDataAccess oinkData = new NbaOinkDataAccess(getWork());
		oinkData.setContractSource(getHoldingInq());

		deOink.put(NbaVpmsAdaptor.A_PROCESS_ID, statusKey);
		deOink.put("A_LogonID", getUser().getUserID());

		vpmsProxy = new NbaVpmsAdaptor(oinkData, model);
		vpmsProxy.setVpmsEntryPoint(entryPoint);
		vpmsProxy.setSkipAttributesMap(deOink);
		return vpmsProxy.getResults();

	} catch (java.rmi.RemoteException re) {
		throw new NbaVpmsException("Error in getting VPMS data in NbaEventToRequest from  VPMS model " + model + " :"
				+ NbaVpmsException.VPMS_EXCEPTION, re);
	} finally {
		try {
			if (vpmsProxy != null) {
				vpmsProxy.remove();
			}
		} catch (Throwable th) {
			getLogger().logError(NbaBaseException.VPMS_REMOVAL_FAILED);
		}
	}

}

}
